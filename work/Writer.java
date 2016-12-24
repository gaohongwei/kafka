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

public class Writer {
  public static void main(String[] args) throws IOException {
    int port = 4567;
    if ( args.length > 0 ) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.printf("%s is an invalid port.\n",args[0]);
      }
    }
    String url_path = String.format("http://localhost:%d/api/cfg", port);

    // File or DB output
    Connection conn = null;
    Statement stmt = null;
    PrintWriter printWriter = null;
    FileWriter fileWriter = null;

    String fname = null;
    Boolean to_db = false;
    Boolean to_file = false;
    String JDBC_DRIVER = null; //"com.mysql.jdbc.Driver";
    String DB_URL = null; //"jdbc:mysql://localhost/keydb";
    String USER = "root";
    String PASS = "dbroot";
    String subscribe_to =null;
    String[] subscribe_to_array = null;
    Properties properties = rest_property(url_path);
    System.out.println("Original properties: "+properties);
    if ( properties == null ){
      properties = new Properties();
      SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
      String ts  = dateFormat.format(new Date());
      fname = "/tmp/update." + ts;
      to_file = true;
    } else {
      JDBC_DRIVER = (String)properties.remove("jdbc_driver");
      DB_URL = (String)properties.remove("db_url");
      fname = (String)properties.remove("output_file");
      subscribe_to = (String)properties.remove("subscribe_to");
      try{
        subscribe_to_array = subscribe_to.split(",", -1);
      }catch(Exception e){
        System.out.println("The source should be sperated by comma.");
      }
      to_db = true;
      if (fname != null )  to_file = true;
    }
    normalize_property(properties);
    System.out.println("Normalize properties: "+properties);
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

      KafkaConsumer<String, String> consumer;
      consumer = new KafkaConsumer<>(properties);

      System.out.println("Subscribed to: "+Arrays.toString(subscribe_to_array));
      consumer.subscribe(Arrays.asList(subscribe_to_array));

      while (true) {
         ConsumerRecords<String, String> records = consumer.poll(100);
         for (ConsumerRecord<String, String> record : records){
            if (to_db){
              try{
                stmt.executeUpdate(record.value());
              }catch(SQLException se){
                printWriter.printf("Failed to run:%s, offset:%d\n",  record.value(), record.offset());
                se.printStackTrace();
              }
            }
            if (to_file){
              printWriter.printf("offset: %d, text: %s, key: %s\n", record.offset(), record.value(), record.key());
            }
         }
      }
    }catch(IOException io) {
       System.out.printf("%s", io.getStackTrace());
    }catch(SQLException se){
      se.printStackTrace();
    }catch(Exception e) {
       System.out.printf("%s", e.getStackTrace());
    } finally {
      try{
         if(stmt!=null) conn.close();
      }catch(SQLException se){}// do nothing
      try{
         if(conn!=null) conn.close();
      }catch(SQLException se){
         se.printStackTrace();
      }//end finally try
      if ( printWriter != null) printWriter.close();
      System.out.println("Close all");
    }
  }
  private static void normalize_property(Properties properties) {
    set_default_property(properties, "bootstrap.servers", "localhost:9092");
    set_default_property(properties, "group.id", "group-" + new Random().nextInt(100000));
    set_default_property(properties, "session.timeout.ms", "10000");
    set_default_property(properties, "fetch.min.bytes", "50000");
    set_default_property(properties, "receive.buffer.bytes", "262144");
    set_default_property(properties, "max.partition.fetch.bytes", "2097152");
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
    Properties properties = null;
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
