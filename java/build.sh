#Build
apt install openjdk-9-jdk-headless maven -y
git clone https://github.com/gaohongwei/kafka

javac -cp "/opt/kafka/libs/*" *.java
java  -cp "/opt/kafka/libs/*":. SimpleProducer test
java  -cp "/opt/kafka/libs/*":. Producer test
