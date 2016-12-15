pkgs="default-jre zookeeperd  supervisor" #nodejs npm
for pkg in $pkgs;do
  dpkg -s $pkg 2>/dev/null >/dev/null || apt-get install $pkg -y

done
#apt-get install $pkgs -y

useradd kafka -m
#passwd kafka
adduser kafka sudo # add to sudo group

# SHould be the project directory
npm init
npm install kafka-node
npm install optimist

cd /tmp
ver=0.10.1.0
www="http://mirror.cc.columbia.edu/pub/software/apache/kafka/$ver/kafka_2.11-$ver.tgz"
wget $www -O kafka.tgz

kafka=/opt/kafka
mkdir $kafka
tar -xvf kafka.tgz -C $kafka --strip 1
tar -vxf kafka.tgz -C $kafka --strip-components=1


##
bin/kafka-server-start.sh config/server.properties

#bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic fast-messages
#bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic summary-markers
#bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test

bin/kafka-topics.sh --list --zookeeper localhost:2181
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic fast-messages
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic summary-markers
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
bin/kafka-topics.sh --list --zookeeper localhost:2181

bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic your_topic_name

# Service
apt-get install supervisor -y
service supervisor status
systemctl enable supervisor
service supervisor start
#Fix supervisor
systemctl enable supervisor
systemctl start supervisor

# Create /etc/supervisor/conf.d/kafka.conf

service supervisor start
service supervisor status
supervisorctl update
supervisorctl reread
supervisorctl restart kafka
supervisorctl status  kafka

# supervisor not started after reboot ????? 
