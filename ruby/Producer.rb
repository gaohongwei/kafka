# https://github.com/bpot/poseidon
# http://ruby-doc.org/stdlib-1.9.3/libdoc/socket/rdoc/TCPServer.html
require 'socket'
require 'poseidon'

server = TCPServer.new 5000
producer = Poseidon::Producer.new(["localhost:9092"], "my_test_producer")

client =server.accept
count = 1
while(line = client.gets)
  producer.send_messages(["topic1", line])
  client.puts 0
  count+=1
  puts line if (count%20000 == 0)  
end
client.close
producer.close
