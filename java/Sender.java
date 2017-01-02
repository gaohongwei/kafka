// File Name sender.java
import java.net.*;
import java.io.*;

import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.util.concurrent.atomic.AtomicBoolean;

public class Sender {
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private KafkaProducer<String, String> producer = null;
  private String send_to = "all";
  private int listen_port = 9000;

  public Sender(String url_path) {
    Properties properties = rest_property(url_path);
    System.out.println("Original properties: "+properties);
    send_to = (String)properties.remove("send_to");
    String listen_port_str = (String)properties.remove("listen_port");
    if ( send_to == null ) {
      send_to = "all";
    }
    try {
      listen_port = Integer.parseInt(listen_port_str);
    } catch (NumberFormatException e) {
      System.out.printf("%s is an invalid port.\n",listen_port_str);
    }
    normalize_property(properties);
    System.out.println("Normalized properties: "+properties);
    producer = new KafkaProducer<>(properties);
    System.out.println("Producer Created");
    System.out.printf("The server will listen on port %d for all incoming changes.\n",listen_port);
    System.out.println("The data change will be sent to all.\n");
  }
  public void run(){
    ServerSocket serverSocket = null;
    try{
      serverSocket= new ServerSocket(listen_port);
      System.out.println("Waiting for client on port " +
            serverSocket.getLocalPort() + "...");
    }catch(Exception e) {
      System.out.println(e.getMessage());
      return;
    }
    while(true){
      Socket server = null;
      int  count = 0;
      long  TimeStart = 0L;
      float TimePassed;
      try{
        server = serverSocket.accept();
        System.out.println("Just connected to " + server.getRemoteSocketAddress());
        //DataInputStream in = new DataInputStream(server.getInputStream());
        BufferedReader  in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        String textFromClient = null;
        TimeStart = System.currentTimeMillis();
        while ((textFromClient = in.readLine()) != null){
           count++;
           producer.send(new ProducerRecord<String, String>(send_to, textFromClient ));
        }
      }catch(SocketTimeoutException s) {
         System.out.println("Socket timed out!");
      }catch(IOException e) {
         System.out.printf("%s\n", e.getMessage());
      }catch(Exception e) {
         System.out.printf("%s\n", e.getMessage());
      }finally {
        try{
          if ( server != null) server.close();
        }catch(Exception e) {
         System.out.printf("%s\n", e.getMessage());
        }
      } // try
      TimePassed  = (System.currentTimeMillis()- TimeStart)/1000;
      System.out.printf("One client left.Summary:\nToatal messages:%d\nTotal Time(seconds):%.1f\nSend message per second: %.1f\n",count,TimePassed, count/TimePassed);
    } // while(true)
  }
  public void shutdown() {
    try{
      if ( producer != null) producer.close();
    }catch(Exception e) {}
    System.out.println("Producer closed.");
    System.out.println("Shutdown Sender");
  }
  public static void main(String[] args) {
    int port = 4567;
    if ( args.length > 0 ) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.printf("%s is an invalid port.\n",args[0]);
      }
    }
    String url_path = String.format("http://localhost:%d/api/cfg?q=sender", port);

    Sender sender = new Sender(url_path);
    // Thread thread_Sender =new Thread(Sender);  thread_sender.start();
    System.out.println("Before sender.run");
    sender.run();
    System.out.println("After sender.run");

    try {
        Thread.sleep(5000);
    } catch (InterruptedException ie) {
        System.out.println("InterruptedException by user");
    }
    System.out.println("Before sender.shutdown");
    sender.shutdown();
    System.out.println("After sender.shutdown");
  }

  private static void normalize_property(Properties properties) {
    set_default_property(properties, "bootstrap.servers", "localhost:9092");
    set_default_property(properties, "acks", "all");
    set_default_property(properties, "retries", "0");
    set_default_property(properties, "batch.size", "16384");
    set_default_property(properties, "auto.commit.interval.ms", "1000");
    set_default_property(properties, "linger.ms", "0");
    set_default_property(properties, "block.on.buffer.full", "true");
    properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
  }
  private static void set_default_property(Properties properties, String key, String value) {
    if (properties.getProperty(key) == null) {
        properties.put(key, value);
    }
  }
  private static Properties rest_property(String url_path) {
    Properties properties = new Properties();
    System.out.printf("Accessing %s\n",url_path);
    try {
      URL url = new URL(url_path);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      if (conn.getResponseCode() != 200) {
        System.out.println("Failed : HTTP error code : "
            + conn.getResponseCode());
      }

      try (InputStream props = conn.getInputStream()) {
        properties.load(props);
      }
      conn.disconnect();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties;
  }
}
