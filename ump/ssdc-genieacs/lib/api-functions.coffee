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
db = require './db'
http = require 'http'
common = require './common'
util = require 'util'
URL = require 'url'
auth = require './auth'


connectionRequest = (deviceId, callback) ->
  # ensure socket is reused in case of digest authentication
  agent = new http.Agent({maxSockets : 1})

  statusToError = (statusCode) ->
    switch statusCode
      when 200, 204
        null
      when 401
        new Error('Incorrect connection request credentials')
      when 0
        new Error('Device is offline')
      else
        new Error("Unexpected response code from device: #{statusCode}")

  conReq = (url, authString, callback) ->
    options = URL.parse(url)
    options.agent = agent

    if authString
      options.headers = {'Authorization' : authString}

    request = http.get(options, (res) ->
      if res.statusCode == 401 and res.headers['www-authenticate']?
        authHeader = auth.parseAuthHeader(res.headers['www-authenticate'])
      callback(res.statusCode, authHeader)
      # don't need body, go ahead and emit events to free up the socket for possible reuse
      res.resume()
    )

    request.on('error', (err) ->
      # error event when request is aborted
      request.abort()
      callback(0)
    )

    request.on('socket', (socket) ->
      socket.setTimeout(config.get('CONNECTION_REQUEST_TIMEOUT', deviceId))
      socket.on('timeout', () ->
        request.abort()
      )
    )

  proj = {
    'Device.ManagementServer.ConnectionRequestURL._value' : 1,
    'InternetGatewayDevice.ManagementServer.ConnectionRequestURL._value' : 1
  }

  db.devicesCollection.findOne({_id : deviceId}, proj, (err, device)->
    if err
      callback(err)
      return

    if device.Device? # TR-181 data model
      connectionRequestUrl = device.Device.ManagementServer.ConnectionRequestURL._value
    else # TR-098 data model
      connectionRequestUrl = device.InternetGatewayDevice.ManagementServer.ConnectionRequestURL._value

    # for testing
    #connectionRequestUrl = connectionRequestUrl.replace(/^(http:\/\/)([0-9\.]+)(\:[0-9]+\/[a-zA-Z0-9]+\/?$)/, '$110.1.1.254$3')
    conReq(connectionRequestUrl, null, (statusCode, authHeader) ->
      if statusCode == 401
        [username, password] = config.auth.connectionRequest(deviceId)
        if authHeader.method is 'Basic'
          authString = auth.basic(username, password)
        else if authHeader.method is 'Digest'
          uri = URL.parse(connectionRequestUrl)
          authString = auth.digest(username, password, uri.path, 'GET', null, authHeader)

        conReq(connectionRequestUrl, authString, (statusCode, authHeader) ->
          if statusCode == 0
            # Workaround for some devices unexpectedly closing the connection
            return conReq(connectionRequestUrl, authString, (statusCode) -> callback(statusToError(statusCode)))
          callback(statusToError(statusCode))
        )
      else
        callback(statusToError(statusCode))
    )
  )


watchTask = (taskId, timeout, callback) ->
  setTimeout( () ->
    db.tasksCollection.findOne({_id : taskId}, {'_id' : 1, 'fault' : 1}, (err, task) ->
      return callback(err) if err

      if task
        timeout -= 500
        if task.fault?
          callback(null, 'fault')
        else if timeout <= 0
          callback(null, 'timeout')
        else
          watchTask(taskId, timeout, callback)
      else
        callback(null, 'completed')
    )
  , 500)


expandParam = (param, aliases) ->
  params = [param]
  for a,aa of aliases
    if a == param or common.startsWith(a, "#{param}.")
      for p in aa
        params.push(p) if p[p.lastIndexOf('.') + 1] != '_'

  return params


sanitizeTask = (task, aliases, callback) ->
  task.timestamp = new Date(task.timestamp ? Date.now())
  if task.expiry?
    if common.typeOf(task.expiry) is common.DATE_TYPE or isNaN(task.expiry)
      task.expiry = new Date(task.expiry)
    else
      task.expiry = new Date(task.timestamp.getTime() + +task.expiry * 1000)
  switch task.name
    when 'getParameterValues'
      projection = {}
      for p in task.parameterNames
        for pp in expandParam(p, aliases)
          projection[pp] = 1
      db.devicesCollection.findOne({_id : task.device}, projection, (err, device) ->
        parameterNames = []
        for k of projection
          #if common.getParamValueFromPath(device, k)?
            #parameterNames.push(k)
          parameterNames.push(k)
        task.parameterNames = parameterNames
        callback(task)
      )
    when 'setParameterValues'
      projection = {}
      values = {}
      for p in task.parameterValues
        for pp in expandParam(p[0], aliases)
          projection[pp] = 1
          values[pp] = p[1]
      db.devicesCollection.findOne({_id : task.device}, projection, (err, device) ->
        parameterValues = []
        for k of projection
          param = common.getParamValueFromPath(device, k)
          if param?
            parameterValues.push([k, values[k], param._type])
        task.parameterValues = parameterValues
        callback(task)
      )
    else
      # TODO implement setParameterValues
      callback(task)


addAliases = (device, aliases) ->
  for k,v of aliases
    for p in v
      pp = p.split('.')
      obj = device
      for i in pp
        if not obj[i]?
          obj = null
          break
        obj = obj[i]

      device[k] = obj if obj?


insertTasks = (tasks, aliases, callback) ->
  if tasks? and common.typeOf(tasks) isnt common.ARRAY_TYPE
    tasks = [tasks]
  else if not tasks? or tasks.length == 0
    return callback(tasks)

  counter = tasks.length
  for task in tasks
    sanitizeTask(task, aliases, (t) ->
      if t.uniqueKey?
        db.tasksCollection.remove({device : t.device, uniqueKey : t.uniqueKey}, (err, removed) ->
        )

      --counter
      if counter == 0
        db.tasksCollection.insert(tasks, (err, _tasks) ->
            callback(err, tasks)
        )
    )


deleteDevice = (deviceId, callback) ->
  db.tasksCollection.remove({'device' : deviceId}, (err, removed) ->
    return callback(err) if err
    db.devicesCollection.remove({'_id' : deviceId}, (err, removed) ->
      return callback(err) if err
      db.redisClient.del("#{deviceId}_presets_hash", "#{deviceId}_inform_hash", (err) ->
        callback(err)
      )
    )
  )

updatePreset = (presetId, callback) ->
  projection = {'_id' : 1,'maxNumber' : 1,'currentNumber' : 1}
  db.presetsCollection.findOne({_id : presetId}, projection, (err, preset) ->
    if preset.maxNumber?
      if (preset.currentNumber + 1) >= preset.maxNumber
        db.presetsCollection.remove({'_id' : presetId}, (err, removed) ->
          return callback(err) if err
          db.redisClient.del('presets', 'presets_hash', (err) ->
            return callback(err) if err
          )
        )
      else
        db.presetsCollection.update({_id : presetId}, {$set : {currentNumber : (preset.currentNumber + 1)}}, (err) ->
          return callback(err) if err
          callback
        )
  )

exports.addAliases = addAliases
exports.sanitizeTask = sanitizeTask
exports.connectionRequest = connectionRequest
exports.watchTask = watchTask
exports.insertTasks = insertTasks
exports.deleteDevice = deleteDevice
exports.updatePreset = updatePreset
