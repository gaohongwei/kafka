var net = require('net');
var client = new net.Socket();
var max = 1000000;
var count = 0;
client.connect(5000, '127.0.0.1', () => {
  console.log('Connected');
  for(var index=0;index<max;index++){
    client.write('Client'+index+'\n');
    if (index%10000 == 0)console.log('Client'+index);
  }
  client.end();
});

client.on('data', (data) => {
  console.log('Received: ' + data);

});

client.on('close', () => {
  console.log('Connection closed');
  client.destroy(); // kill client after server's response
});

