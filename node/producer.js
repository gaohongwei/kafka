'use strict';

var kafka = require('kafka-node');
var Producer = kafka.Producer;
var Client = kafka.Client;
var client = new Client('localhost:2181');
var producer = new Producer(client, { requireAcks: 1,ackTimeoutMs: 1 });

var argv = require('optimist').argv;
var topic = argv.topic || 'test';
var p = argv.p || 0;
var a = argv.a || 0;
var max = 100000;
var cycle = 1000;
var count = 0;

producer.on('ready', function () {
  for(var index=0;index<max;index++){
    var message = 'message ' + index;
    producer.send(
      [{topic: topic, messages: [message]}],
      function (err, data) {
        count++;
        if (err) console.log(err);
        if (count % cycle == 0) {
          console.log(data);
        }
        if (count > max) {
          process.exit();
        }        
      }
    );
  }
});

producer.on('error', function (err) {
  console.log('error', err);
});
