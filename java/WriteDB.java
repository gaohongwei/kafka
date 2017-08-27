import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.*;

public class Writer {
  public static void main(String[] args) throws IOException {
    String url = "http://localhost:4567/api";
    String fname = null;
    Boolean to_db = false;
    Boolean to_file = false;
    if ( args.length == 0 ){
      SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
      String ts  = dateFormat.format(new Date());
      fname = "/tmp/update." + ts;
    } else {
      switch (args[0]) {
        case "db":
        case "database":
          to_db = true;
          break;
        default:
          fname = args[0];
      }
    }

    if ( fname == null ) to_db = true;

    // DB
    String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    String DB_URL = "jdbc:mysql://localhost/keydb";
    String USER = "root";
    String PASS = "dbroot";
    Connection conn = null;
    Statement stmt = null;

    // File output
    PrintWriter printWriter = null;
    FileWriter fileWriter = null;
    try{
      if (to_db){
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        stmt = conn.createStatement();
      }
      if (to_file){
        fileWriter = new FileWriter(fname);
        printWriter = new PrintWriter(fileWriter);
      }

      KafkaConsumer<String, String> consumer;
      try (InputStream props = Resources.getResource("consumer.props").openStream()) {
        Properties properties = new Properties();
        properties.load(props);
        if (properties.getProperty("group.id") == null) {
            properties.setProperty("group.id", "group-" + new Random().nextInt(100000));
        }
        consumer = new KafkaConsumer<>(properties);
      }
      consumer.subscribe(Arrays.asList("fast-messages", "summary-markers"));

      while (true) {
         ConsumerRecords<String, String> records = consumer.poll(100);
         for (ConsumerRecord<String, String> record : records){
            if (to_db){
              stmt.executeUpdate(record.value());
            }
            if (to_file){
              printWriter.printf("offset: %d, value: %s, key: %s\n", record.offset(), record.value(), record.key());
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
  public static Properties get_property(url_path) {
    Properties properties = new Properties();
    try {
      URL url = new URL(url_path);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : "
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
