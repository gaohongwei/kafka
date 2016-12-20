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

public class puller {
  public static void main(String[] args) throws IOException {
    // set up house-keeping
    // and the consumer
    PrintWriter printWriter = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
    String ts  = dateFormat.format(new Date());
    try{
      FileWriter fileWriter = new FileWriter("/tmp/update." + ts );
      printWriter = new PrintWriter(fileWriter);

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

      int i = 0;

      while (true) {
         ConsumerRecords<String, String> records = consumer.poll(100);
         for (ConsumerRecord<String, String> record : records)
         // print the offset,key and value for the consumer records.
         printWriter.printf("offset = %d, key = %s, value = %s\n",
            record.offset(), record.key(), record.value());
      }
    }catch(IOException io) {
       System.out.printf("%s", io.getStackTrace());
    }catch(Exception e) {
       System.out.printf("%s", e.getStackTrace());
    } finally {
       //producer.close();
       if ( printWriter != null) printWriter.close();
       System.out.println("Close all");
    }
  }
}
