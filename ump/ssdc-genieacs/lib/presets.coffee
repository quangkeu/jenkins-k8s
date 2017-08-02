###
# Copyright 2013, 2014  Zaid Abdulla
#
# GenieACS is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# GenieACS is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with GenieACS.  If not, see <http://www.gnu.org/licenses/>.
#
# This file incorporates work covered by the following copyright and
# permission notice:
#
# Copyright 2013 Fanoos Telecom
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
###

config = require './config'
common = require './common'
db = require './db'
mongoQuery = require './mongo-query'
query = require './query'
log = require('util').log
customCommands = require './custom-commands'
util = require 'util'

exports.calculatePresetsHash = (presets, objects) ->
  crypto = require('crypto')
  hash = crypto.createHash('md5').update(JSON.stringify(presets) + JSON.stringify(objects)).digest('hex')
  return hash


matchObject = (object, param) ->
  flatObject = common.flattenObject(object)
  if object._keys?.length
    keys = object._keys
  else
    # Consider all parameters as keys if none are defined
    keys = (k for k in Object.keys(flatObject) when k[0] != '_')

  for k in keys
    p = common.getParamValueFromPath(param, k)
    return false if not p?
    v = common.matchType(p._value, flatObject[k])

    if p._value != common.matchType(p._value, flatObject[k])
      return false

  return true


exports.getDevicePreset = (deviceId, presets, objects, aliases, eventCodes, callback) ->
  deviceCustomCommands = customCommands.getDeviceCustomCommands(deviceId)
  # only fetch relevant params
  projection = {_id : 1}
  for p in presets
    if common.typeOf(p.precondition) is common.STRING_TYPE
      p.precondition = query.expand(JSON.parse(p.precondition), aliases)
    else
      # Accept an object for backward compatiblity
      p.precondition = query.expand(p.precondition ? {}, aliases)
    mongoQuery.projection(p.precondition, projection)

    for c in p.configurations
      switch c.type
        when 'value', 'age'
          projection[c.name] = 1
        when 'custom_command_value', 'custom_command_age'
          commandfile = c.command.split(' ', 1)
          if commandfile of deviceCustomCommands
            projection["_customCommands.#{c.command.split(' ', 1)}"] = 1
        when 'backup'
          projection['Device.DeviceInfo.SoftwareVersion'] = 1
          projection['InternetGatewayDevice.DeviceInfo.SoftwareVersion'] = 1
        when 'add_tag', 'delete_tag'
          projection['_tags'] = 1
        when 'add_group', 'delete_group'
          projection['_groups'] = 1
        when 'add_object', 'delete_object'
          projection[c.name] = 1
        when 'download', 'reboot', 'factoryReset'
        else
          throw new Error('Unknown configuration type')

  mongoQuery.optimizeProjection(projection)

  db.devicesCollection.findOne({'_id' : deviceId}, projection, (err, device) ->
    throw err if err
    devicePresets = []
    for p in presets
      checkEvent = false
      for eventCode, eventValue of p.events
        if eventValue && eventCode in eventCodes
          checkEvent = true
      if checkEvent && mongoQuery.test(device, p.precondition)
        devicePresets.push(p)

    configurations = accumulateConfigurations(devicePresets, objects)

    getParamDetails = (device, path) ->
      p = common.getParamValueFromPath(device, path)
      return undefined if not p?
      details = {}
      for k,v of p
        details[k] = v if k[0] == '_'
      return details

    devicePreset = {}
    for c in configurations
      switch c.type
        when 'value'
          devicePreset.parameters ?= {}
          devicePreset.parameters[c.name] ?= {}
          devicePreset.parameters[c.name].preset ?= {}
          devicePreset.parameters[c.name].preset.value = c.value
          devicePreset.parameters[c.name].current = getParamDetails(device, c.name)
          devicePreset.parameters.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.parameters.policyJobIds
            devicePreset.parameters.policyJobIds.push(c.policyJobId)

        when 'age'
          devicePreset.parameters ?= {}
          devicePreset.parameters[c.name] ?= {}
          devicePreset.parameters[c.name].preset ?= {}
          devicePreset.parameters[c.name].preset.expiry = parseInt(c.age)
          devicePreset.parameters[c.name].current = getParamDetails(device, c.name)
          devicePreset.parameters.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.parameters.policyJobIds
            devicePreset.parameters.policyJobIds.push(c.policyJobId)

        when 'add_tag'
          devicePreset.tags ?= {}
          devicePreset.tags[c.tag] = 2 | c.tag in (device._tags ? [])

        when 'delete_tag'
          devicePreset.tags ?= {}
          devicePreset.tags[c.tag] = 0 | c.tag in (device._tags ? [])

        when 'add_group'
          devicePreset.groups ?= {}
          devicePreset.groups[c.group] = 2 | c.group in (device._groups ? [])

        when 'delete_group'
          devicePreset.groups ?= {}
          devicePreset.groups[c.group] = 0 | c.group in (device._groups ? [])

        when 'add_object'
          devicePreset.objects ?= {}
          devicePreset.objects[c.name] ?= {}
          devicePreset.objects[c.name][c.object] ?= {}
          devicePreset.objects[c.name][c.object].preset = objects[c.object]
          continue if not param = common.getParamValueFromPath(device, c.name)
          devicePreset.objects[c.name][c.object].current ?= {}
          for k,p of param
            continue if k[0] == '_'
            if matchObject(objects[c.object], p)
              devicePreset.objects[c.name][c.object].current[k] = p
          devicePreset.objects.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.objects.policyJobIds
            devicePreset.objects.policyJobIds.push(c.policyJobId)

        when 'delete_object'
          devicePreset.objects ?= {}
          devicePreset.objects[c.name] ?= {}
          devicePreset.objects[c.name][c.object] ?= {}
          continue if not param = common.getParamValueFromPath(device, c.name)
          devicePreset.objects[c.name][c.object].current ?= {}
          for k,p of param
            continue if k[0] == '_'
            if matchObject(objects[c.object], p)
              devicePreset.objects[c.name][c.object].current[k] = p
          devicePreset.objects.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.objects.policyJobIds
            devicePreset.objects.policyJobIds.push(c.policyJobId)

        when 'custom_command_value'
          [filename, commandName] = c.command.split(' ', 2)
          devicePreset.customCommands ?= {}
          devicePreset.customCommands[filename] ?= {}
          devicePreset.customCommands[filename].preset ?= {}
          devicePreset.customCommands[filename].preset.value = c.value
          devicePreset.customCommands[filename].preset.valueCommand = c.command
          continue if not deviceCustomCommands[filename]? or commandName not in deviceCustomCommands[filename]
          if cmd = common.getParamValueFromPath(device, "_customCommands.#{filename}")
            devicePreset.customCommands[filename].current = cmd
          else
            devicePreset.customCommands[filename].current ?= {}

        when 'custom_command_age'
          [filename, commandName] = c.command.split(' ', 2)
          devicePreset.customCommands ?= {}
          devicePreset.customCommands[filename] ?= {}
          devicePreset.customCommands[filename].preset ?= {}
          devicePreset.customCommands[filename].preset.expiry = parseInt(c.age)
          devicePreset.customCommands[filename].preset.expiryCommand = c.command
          continue if not deviceCustomCommands[filename]? or commandName not in deviceCustomCommands[filename]
          if cmd = common.getParamValueFromPath(device, "_customCommands.#{filename}")
            devicePreset.customCommands[filename].current = cmd
          else
            devicePreset.customCommands[filename].current ?= {}

        when 'restore'
          if device?.Device? # TR-181 data model
            softwareVersion = common.getParamValueFromPath(device, 'Device.DeviceInfo.SoftwareVersion')
          else # TR-098 data model
            softwareVersion = common.getParamValueFromPath(device, 'InternetGatewayDevice.DeviceInfo.SoftwareVersion')
          query = { filename: new RegExp(softwareVersion._value + "-" + deviceId) }
          db.filesCollection.findOne(query,{'_id' : true}, (err, file) ->
            throw err if err
            devicePreset.restoreFile ?= {}
            devicePreset.restoreFile.fileId = file._id
            callback(devicePreset)
          )
          devicePreset.restoreFile.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.restoreFile.policyJobIds
            devicePreset.restoreFile.policyJobIds.push(c.policyJobId)

        when 'backup'
          devicePreset.backupFile ?= {}
          devicePreset.backupFile.fileType = c.fileType
          if device?.Device? # TR-181 data model
            softwareVersion = common.getParamValueFromPath(device, 'Device.DeviceInfo.SoftwareVersion')
          else # TR-098 data model
            softwareVersion = common.getParamValueFromPath(device, 'InternetGatewayDevice.DeviceInfo.SoftwareVersion')
          devicePreset.backupFile.url = c.url + "-" + (new Date).getTime() + "-" + softwareVersion._value + "-" + deviceId;
          devicePreset.backupFile.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.backupFile.policyJobIds
            devicePreset.backupFile.policyJobIds.push(c.policyJobId)

        when 'download'
          devicePreset.downloadFileId = c.fileId
          devicePreset.downloadFileId.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.downloadFileId.policyJobIds
            devicePreset.downloadFileId.policyJobIds.push(c.policyJobId)

        when 'reboot'
          devicePreset.reboot ?= {}
          devicePreset.reboot.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.reboot.policyJobIds
            devicePreset.reboot.policyJobIds.push(c.policyJobId)

        when 'factoryReset'
          devicePreset.factoryReset ?= {}
          devicePreset.factoryReset.policyJobIds ?= []
          if c.policyJobId? and c.policyJobId not in devicePreset.factoryReset.policyJobIds
            devicePreset.factoryReset.policyJobIds.push(c.policyJobId)

        else
          throw new Error('Unknown configuration type')

    callback(devicePreset)
  )


exports.processDevicePreset = (deviceId, devicePreset, callback) ->
  PRESETS_TIME_PADDING = config.get('PRESETS_TIME_PADDING')
  now = new Date()
  expiry = config.get('PRESETS_CACHE_DURATION', deviceId)
  getParameterValues = []
  setParameterValues = []
  addTags = []
  deleteTags = []
  addGroups = []
  deleteGroups = []
  taskList = []

  # Parameters
  for parameterPath, parameterDetails of devicePreset.parameters ? {}
    continue if not parameterDetails.current?
    expiry = Math.min(expiry, parameterDetails.preset.expiry) if parameterDetails.preset.expiry?
    presetValue = parameterDetails.preset.value
    currentValue = parameterDetails.current._value
    # if presetValue? and currentValue != (presetValue = common.matchType(currentValue, presetValue))
    if presetValue
      setParameterValues.push([parameterPath, presetValue, parameterDetails.current._type])
    else if parameterDetails.preset.expiry?
      diff = parameterDetails.preset.expiry - (now - (parameterDetails.current._timestamp ? 0)) / 1000
      if diff <= PRESETS_TIME_PADDING
        if parameterDetails.current._value?
          getParameterValues.push(parameterPath)
        else
          if devicePreset.parameters.policyJobIds?
            taskList.push({device : deviceId, name : 'refreshObject', objectName : parameterPath, policyJobIds : devicePreset.parameters.policyJobIds})
          else
            taskList.push({device : deviceId, name : 'refreshObject', objectName : parameterPath})
      else
        expiry = Math.min(expiry, diff)

  # Objects
  for parameterPath, object of devicePreset.objects ? {}
    for objectName, objectDetails of object
      continue if not objectDetails.current?
      if objectDetails.preset?
        flatObject = common.flattenObject(objectDetails.preset)
        if Object.keys(objectDetails.current).length > 0
          for i, obj of objectDetails.current
            for paramName, paramValue of flatObject
              continue if paramName[0] == '_'
              currentValue = common.getParamValueFromPath(obj, paramName)
              if currentValue?
                presetValue = common.matchType(currentValue._value, flatObject[paramName])
                # if currentValue._value != presetValue
                  # setParameterValues.push(["#{parameterPath}.#{i}.#{paramName}", presetValue, currentValue._type])
                setParameterValues.push(["#{parameterPath}.#{i}.#{paramName}", presetValue, currentValue._type])
        else
          vals = []
          for k,v of flatObject
            vals.push([k, v]) if k[0] != '_'
          if devicePreset.objects.policyJobIds?
            taskList.push({device : deviceId, name : 'addObject', objectName : parameterPath, parameterValues : vals, policyJobIds : devicePreset.objects.policyJobIds})
          else
            taskList.push({device : deviceId, name : 'addObject', objectName : parameterPath, parameterValues : vals})
      else if Object.keys(objectDetails.current).length > 0
        for i, obj of objectDetails.current
          if devicePreset.objects.policyJobIds?
            taskList.push({device : deviceId, name : 'deleteObject', objectName : "#{parameterPath}.#{i}", policyJobIds : devicePreset.objects.policyJobIds})
          else
            taskList.push({device : deviceId, name : 'deleteObject', objectName : "#{parameterPath}.#{i}"})

  # Tags
  for tag, v of devicePreset.tags ? {}
    if v == 1
      deleteTags.push(tag)
    else if v == 2
      addTags.push(tag)

  # Groups
  for group, v of devicePreset.groups ? {}
    if v == 1
      deleteGroups.push(group)
    else if v == 2
      addGroups.push(group)

  #Restore file
  if devicePreset.restoreFile?
    if devicePreset.restoreFile.policyJobIds?
      taskList.push {name : 'download', file : devicePreset.restoreFile.fileId, device : deviceId, policyJobId : devicePreset.restoreFile.policyJobIds}
    else
      taskList.push {name : 'download', file : devicePreset.restoreFile.fileId, device : deviceId}

  #Download file
  if devicePreset.downloadFileId?
    if devicePreset.downloadFileId.policyJobIds?
      taskList.push {name : 'download', file : devicePreset.downloadFileId, device : deviceId, policyJobId : devicePreset.downloadFileId.policyJobIds}
    else
      taskList.push {name : 'download', file : devicePreset.downloadFileId, device : deviceId}

  #Backup file
  if devicePreset.backupFile?
    if devicePreset.backupFile.policyJobIds?
      taskList.push {name : 'upload', fileType : devicePreset.backupFile.fileType, url : devicePreset.backupFile.url, device : deviceId, policyJobId : devicePreset.backupFile.policyJobIds}
    else
      taskList.push {name : 'upload', fileType : devicePreset.backupFile.fileType, url : devicePreset.backupFile.url, device : deviceId}

  #Reboot
  if devicePreset.reboot?
    if devicePreset.reboot.policyJobIds?
      taskList.push {name : 'reboot', device : deviceId, policyJobId : devicePreset.reboot.policyJobIds}
    else
      taskList.push {name : 'reboot', device : deviceId}

  #factoryReset
  if devicePreset.factoryReset?
    if devicePreset.factoryReset.policyJobIds?
      taskList.push {name : 'factoryReset', device : deviceId, policyJobId : devicePreset.factoryReset.policyJobIds}
    else
      taskList.push {name : 'factoryReset', device : deviceId}

  # Custom commands
  for commandName, commandDetails of devicePreset.customCommands ? {}
    continue if not commandDetails.current?
    expiry = Math.min(expiry, commandDetails.preset.expiry) if commandDetails.preset.expiry?
    currentValue = commandDetails.current._value
    presetValue = common.matchType(currentValue, commandDetails.preset.value)
    if commandDetails.preset.value? and currentValue != presetValue
      taskList.push({device : deviceId, name : 'customCommand', command : commandDetails.preset.valueCommand})
    else if commandDetails.preset.expiry?
      diff = commandDetails.preset.expiry - (now - (commandDetails.current._timestamp ? 0)) / 1000
      if diff <= PRESETS_TIME_PADDING
        taskList.push({device : deviceId, name : 'customCommand', command : commandDetails.preset.expiryCommand})
      else
        expiry = Math.min(expiry, diff)

  if setParameterValues.length
    if devicePreset.parameters.policyJobIds?
      taskList.push {device : deviceId, name : 'setParameterValues', parameterValues: setParameterValues, timestamp : now, policyJobIds : devicePreset.parameters.policyJobIds}
    else
      taskList.push {device : deviceId, name : 'setParameterValues', parameterValues: setParameterValues, timestamp : now}

  if getParameterValues.length
    if devicePreset.parameters.policyJobIds?
      taskList.push {device : deviceId, name : 'getParameterValues', parameterNames: getParameterValues, timestamp : now, policyJobIds : devicePreset.parameters.policyJobIds}
    else
      taskList.push {device : deviceId, name : 'getParameterValues', parameterNames: getParameterValues, timestamp : now}

  callback(taskList, addTags, deleteTags, addGroups, deleteGroups, expiry)


getObjectHash = (object) ->
  if object._keys?.length
    keys = object._keys
  else
    keys = (k for k in Object.keys(object) when k[0] != '_')

  keys.sort()

  hash = []
  for k in keys
    hash.push(k)
    hash.push(common.getParamValueFromPath(object, k))

  return JSON.stringify(hash)


accumulateConfigurations = (presets, objects) ->
  maxWeights = {}
  configurations = {}
  for p in presets
    for c in p.configurations
      configurationHash = switch c.type
        when 'add_tag', 'delete_tag'
          "tag_#{c.tag}"
        when 'add_group', 'delete_group'
          "group_#{c.group}"
        when 'add_object', 'delete_object'
          objectHash = getObjectHash(objects[c.object])
          "object_#{c.name}_#{objectHash}"
        when 'custom_command_age', 'custom_command_value'
          [filename, commandName] = c.command.split(' ', 2)
          "#{c.type}_#{filename}_#{commandName}"
        else
          "#{c.type}_#{c.name}"

      if not maxWeights[configurationHash]? or p.weight > maxWeights[configurationHash]
        configurations[configurationHash] = c
        maxWeights[configurationHash] = p.weight

  configurationsList = (configurations[c] for c of configurations)
  return configurationsList
