#Build
apt install openjdk-9-jdk-headless maven -y
git clone https://github.com/gaohongwei/kafka

apt install openjdk-9-jdk-headless
javac -cp "/opt/kafka/libs/*" Producer.java
java  -cp "/opt/kafka/libs/*":. Producer 
java  -cp "/opt/kafka/libs/*":. SimpleProducer test

view /etc/environment
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export CLASSPATH="/opt/kafka/libs/*:."

javac Producer.java
java  Producer 


update-alternatives --config java
update-alternatives --config javac
