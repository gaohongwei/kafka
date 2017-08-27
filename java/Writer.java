import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.common.errors.WakeupException;

public class Writer implements Runnable {
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final KafkaConsumer<String, String> consumer;
  private String fname = null;
  private Boolean to_db = false;
  private Boolean to_file = false;
  private String JDBC_DRIVER = null; //"com.mysql.jdbc.Driver";
  private String DB_URL = null; //"jdbc:mysql://localhost/keydb";
  private String USER = "root";
  private String PASS = "dbroot";
  private String listen_to = "all";
  private String[] listen_to_array = {"all"};
  private int CHECK_NUMBER = 10000;
  private Boolean CHECK_SPEED = false;

  public Writer(String url_path) {
    Properties properties = rest_property(url_path);
    System.out.println("Original properties: "+properties);
    JDBC_DRIVER = (String)properties.remove("jdbc_driver");
    DB_URL = (String)properties.remove("db_url");
    fname = (String)properties.remove("output_file");
    listen_to = (String)properties.remove("listen_to");
    String check_string = (String)properties.remove("check_number");
    try {
      if ( check_string != null ){
        CHECK_NUMBER = Integer.parseInt(check_string);
        CHECK_SPEED = true;
      }
    }catch (NumberFormatException e) {}
    if ( listen_to == null) listen_to = "all";
    try{
      listen_to_array = listen_to.split(",", -1);
    }catch(Exception e){
      System.out.println("The source should be sperated by comma.");
    }
    if ( JDBC_DRIVER == null || DB_URL == null ){
      properties = new Properties();
      SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
      String ts  = dateFormat.format(new Date());
      if ( fname == null ) fname = "/tmp/update." + ts;
      to_file = true;
    } else {
      to_db = true;
      if (fname != null )  to_file = true;
    }
    normalize_property(properties);
    System.out.println("Normalize properties: "+properties);
    consumer = new KafkaConsumer<>(properties);
    System.out.println("Consumer Created");
  }

  public void run() {
    // File or DB output
    Connection conn = null;
    Statement stmt = null;
    PrintWriter printWriter = null;
    FileWriter fileWriter = null;

    try{
      if (to_db){
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        stmt = conn.createStatement();
        System.out.printf("Connected to the database:\ndb_url=%s\njdbc_driver=%s\n",DB_URL,JDBC_DRIVER);
      }
      if (to_file){
        fileWriter = new FileWriter(fname);
        printWriter = new PrintWriter(fileWriter);
        System.out.printf("Writing to the file:\n%s\n",fname);
      }

      consumer.subscribe(Arrays.asList(listen_to_array));
      System.out.println("Listen to channels: "+Arrays.toString(listen_to_array));
      int  count = 1;
      long  TimeStart = System.currentTimeMillis();
      float TimePassed;

      while (!closed.get()) {
         ConsumerRecords<String, String> records = consumer.poll(100);
         for (ConsumerRecord<String, String> record : records){
            count++;
            if (to_db){
              try{
                stmt.executeUpdate(record.value());
              }catch(SQLException e){
                System.out.printf("Failed to run this as a sql:%s, offset:%d\n",  record.value(), record.offset());
                e.printStackTrace();
              }
            }
            if (to_file){
              printWriter.printf("offset: %d, text: %s, key: %s\n", record.offset(), record.value(), record.key());
            }

            if ( CHECK_SPEED && count > CHECK_NUMBER){
              TimePassed  = (System.currentTimeMillis()- TimeStart)/1000;
              System.out.printf("Writer Summary:\nToatal messages:%d\nTotal Time(seconds):%.1f\nWrite message per second: %.1f\n",CHECK_NUMBER,TimePassed, CHECK_NUMBER/TimePassed);
              count = 0;
              TimeStart = System.currentTimeMillis();
            }
         }
      }
    }catch(IOException e) {
       System.out.println(e.getMessage());
    }catch(SQLException e){
       System.out.println(e.getMessage());
    }catch (WakeupException e) {
         // Ignore exception if closing
      System.out.println(e.getMessage());
      if (!closed.get()) throw e;
    }catch(Exception e) {
       System.out.println(e.getMessage());
    } finally {
      consumer.close();
      try{
         if(stmt!=null) conn.close();
      }catch(SQLException e){
        System.out.println(e.getMessage());
      }// do nothing
      try{
         if(conn!=null) conn.close();
      }catch(SQLException e){
        System.out.println(e.getMessage());
      }//end finally try

      if ( printWriter != null) printWriter.close();
      System.out.println("Close all");
    }
    System.out.println("worker stopped.");
  }
     // Shutdown hook which can be called from a separate thread
  public void shutdown() {
    closed.set(true);
    consumer.wakeup();
    System.out.println("Shutdown worker");
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
    String url_path = String.format("http://localhost:%d/api/cfg?q=writer", port);

    Writer writer = new Writer(url_path);
    // Thread worker =new Thread(writer);  worker.start();
    System.out.println("Before writer.run");
    writer.run();
    System.out.println("After writer.run");

    try {
        Thread.sleep(5000);
    } catch (InterruptedException ie) {
        System.out.println("InterruptedException by user");
    }
    System.out.println("Before writer.shutdown");
    writer.shutdown();
    System.out.println("After writer.shutdown");
  }
  private static void normalize_property(Properties properties) {
    set_default_property(properties, "bootstrap.servers", "localhost:9092");
    set_default_property(properties, "group.id", "group-" + new Random().nextInt(100000));
    set_default_property(properties, "session.timeout.ms", "10000");
    set_default_property(properties, "fetch.min.bytes", "50000");
    set_default_property(properties, "receive.buffer.bytes", "262144");
    set_default_property(properties, "CHECK_NUMBER.partition.fetch.bytes", "2097152");
    set_default_property(properties, "auto.commit.interval.ms", "1000");
    set_default_property(properties,"enable.auto.commit", "true");
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
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
        properties = new Properties();
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
