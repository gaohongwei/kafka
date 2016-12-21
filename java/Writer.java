// File Name Writer.java
import java.net.*;
import java.io.*;
/*
import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
*/
public class GateWay {
   public static void main(String[] args) throws IOException {
      // set up the producer
      /*
      KafkaProducer<String, String> producer;
      try (InputStream props = Resources.getResource("producer.props").openStream()) {
         Properties properties = new Properties();
         properties.load(props);
         producer = new KafkaProducer<>(properties);
      }*/
      int port = 5000;
      Socket server = null;
      ServerSocket serverSocket = null;
      try {
         serverSocket= new ServerSocket(port);
         System.out.println("Waiting for client on port " +
            serverSocket.getLocalPort() + "...");
         server = serverSocket.accept();
         System.out.println("Just connected to " + server.getRemoteSocketAddress());
         DataInputStream in = new DataInputStream(server.getInputStream());

         String textFromClient = null;
         while ((textFromClient = in.readLine()) != null){
             System.out.println(textFromClient);
             //producer.send(new ProducerRecord<String, String>(
             //           "fast-messages", textFromClient ));
            // every so often send to a different topic
         }
      }catch(SocketTimeoutException s) {
         System.out.println("Socket timed out!");
      }catch(Throwable throwable) {
         System.out.printf("%s", throwable.getStackTrace());
      } finally {
         //producer.close();
         if ( server != null) server.close();
         System.out.println("Close all");
      }
    }
}
