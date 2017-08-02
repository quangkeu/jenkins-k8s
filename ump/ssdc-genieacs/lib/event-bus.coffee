easyamqp = require('easy-amqp')
config = require './config'

connect = () ->
  connection = easyamqp.createConnection({
    host: config.get('RABBIT_HOST'),
    port: config.get('RABBIT_PORT'),
    login: config.get('RABBIT_LOGIN'),
    password: config.get('RABBIT_PASSWORD')
  })

  exports.connection = connection

publish = (routingKey, event) ->
#  this.connection.queue('test.queue').bind('ssdc.ump.event','#')
  this.connection
      .exchange('ssdc.ump.event',{durable:true, auto_delete:true})
      .publish(routingKey,JSON.stringify(event))

disconnect = () ->
  this.connection.disconnect()

exports.publish = publish
exports.connect = connect
exports.disconnect = disconnect