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
###

config = require './config'
db = require './db'


PATH_REGEX = /\w+|(\/.+?\/\w*)/g

# Javascript represents numbers as 64-bit floating point numbers
# Integers up to 2^53 can be encoded precisely
MAX_PARAMETERS = 52


# get the index of the least significant bit
getLsb = (num) ->
  i = -1
  while num > 0
    ++ i
    break if num & 1
    num >>= 1
  return i


createParameterAttributeFinder = (attributeName) ->
  # compile attributes into data types optimized for quick lookup
  attributes = []
  stringSegments = {}
  regexSegments = {}
  paramRegex = {}

  for k, v of config.PARAMETERS
    continue if not v[attributeName]?
    attributeIndex = attributes.push(v[attributeName]) - 1
    throw new Error("There cannot be more than #{MAX_PARAMETERS} configured parameters") if attributeIndex >= MAX_PARAMETERS
    segmentIndex = 0
    parts = k.match(PATH_REGEX)
    for i in [0 ... parts.length]
      part = parts[i]
      positionHash = i + parts.length * MAX_PARAMETERS

      if part[0] == '/' # this is a regex part
        j = part.lastIndexOf('/')
        regExp = new RegExp(part[1...j], part[j+1..])
        regexSegments[positionHash] ?= 0
        regexSegments[positionHash] |= Math.pow(2, attributeIndex)
        paramRegex[attributeIndex] ?= {}
        paramRegex[attributeIndex][i] = regExp
      else
        stringSegments[positionHash] ?= {}
        stringSegments[positionHash][part] ?= 0
        stringSegments[positionHash][part] |= Math.pow(2, attributeIndex)

  # Finder function
  return (param) ->
    parts = param.split('.')
    positionHash = parts.length * MAX_PARAMETERS

    stringIndices = 0 | stringSegments[positionHash]?[parts[0]]
    allIndices = stringIndices | regexSegments[positionHash]

    for i in [1...parts.length]
      positionHash = i + parts.length * MAX_PARAMETERS
      strIdx = stringSegments[positionHash]?[parts[i]]
      rgxIdx = regexSegments[positionHash]
      stringIndices &= strIdx
      allIndices &= (strIdx | rgxIdx)

    if stringIndices > 0
      return attributes[getLsb(stringIndices)]

    attributeIndex = 0
    while allIndices > 0
      if allIndices & 1
        match = true
        for i, r of paramRegex[attributeIndex]
          if not r.test(parts[i])
            match = false
            break

        if match
          return attributes[attributeIndex]

      ++ attributeIndex
      allIndices >>= 1

    return null


splitIds = (batches, callback) ->
  db.devicesCollection.count((err, count) ->
    throw err if err
    return callback(null) if count == 0
    ids = [{}]
    return callback(ids) if count <= 1000 or batches == 1
    batchSize = Math.floor(count / batches)
    for i in [1 ... batches]
      ids[i] = {}
      do (i) ->
        cursor = db.devicesCollection.find({}, {_id:1}).sort({'_id':1}).skip(i * batchSize).limit(1)
        cursor.nextObject((err, obj) ->
          ids[i]['$gte'] = obj._id
          ids[i-1]['$lt'] = obj._id
          if i == batches - 1
            callback(ids)
        )
  )


compileAliases = (callback) ->
  map = () ->
    recurse = (partIndex, ref) ->
      part = parts[partIndex]
      res = []
      if part.test?
        for p of ref
          if part.test(p)
            if partIndex == parts.length - 1
              res.push(p)
            else
              for r in recurse(partIndex + 1, ref[p])
                res.push("#{p}.#{r}")
      else
        if ref[part]?
          if partIndex == parts.length - 1
            return [part]
          else
            for r in recurse(partIndex + 1, ref[part])
              res.push("#{part}.#{r}")

      return res

    for key, parts of keys
      for alias in recurse(0, this)
        emit(alias, key)
    return

  reduce = (k, v) ->
    return v[0]

  aliases = {}
  keys = {}
  for k, v of config.PARAMETERS
    alias = v.alias
    continue if not alias?
    isStatic = true
    ar = []
    for p in k.match(/\w+|(\/.+?\/\w*)/g)
      if p[0] == '/' # this is a regex
        isStatic = false
        i = p.lastIndexOf('/')
        r = new RegExp(p[1...i], p[i+1..])
        ar.push(r)
      else
        ar.push(p)

    if isStatic
      aliases[alias] ?= []
      aliases[alias].push(k)
    else
      keys[k] = ar

  if keys.length > 0
    counter = 0
    splitIds(4, (batches) ->
      if not batches?
        return callback(null, aliases)

      for batch in batches
        options = {
          out : {inline : 1}
          jsMode : true
          sort : {$natural : 1}
          scope : {keys : keys, flags : {}}
          query : {_id : batch} if batches.length > 1
        }

        db.devicesCollection.mapReduce(map, reduce, options, (err, out) ->
          if err
            callback?(err)
            return callback = null

          for o in out
            alias = config.PARAMETERS[o.value].alias
            aliases[alias] ?= []
            id = String(o._id)
            if id not in aliases[alias]
              aliases[alias].push(id)

          if ++counter == batches.length
            callback?(null, aliases)
        )
      return
    )
   else
      return callback(null, aliases)


exports.getType = createParameterAttributeFinder('type')
exports.getAlias = createParameterAttributeFinder('alias')
exports.compileAliases = compileAliases
