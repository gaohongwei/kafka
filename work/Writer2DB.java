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

public class Writer2DB {
  public static void main(String[] args) throws IOException {
    Boolean to_db = false;
    if ( args.length == 0 ||  args[0] == "db" || args[0] == "database" ) to_db = true;
    final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    String DB_URL = "jdbc:mysql://localhost/keys";
    String USER = "root";
    String PASS = "dbroot";
    Connection conn = null;
    Statement stmt = null;

    PrintWriter printWriter = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
    String ts  = dateFormat.format(new Date());
    try{
      FileWriter fileWriter = new FileWriter("/tmp/update." + ts );
      printWriter = new PrintWriter(fileWriter);
      Class.forName(JDBC_DRIVER);
      conn = DriverManager.getConnection(DB_URL, USER, PASS);
      stmt = conn.createStatement();

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
            } else {
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
}
