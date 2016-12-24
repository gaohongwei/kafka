
  public void Props() {
      try {
          File myFile = new File("props.properties");
          Properties properties = new Properties();
          properties.load(new FileInputStream(myFile));
          properties.remove("deletethis");
          OutputStream out = new FileOutputStream(myFile);
          properties.store(out, null);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

    Properties props = new Properties();
    FileInputStream in = new FileInputStream(file);
    props.load(in);
    in.close();
    if (props.remove("key") != null) {
        FileOutputStream out = new FileOutputStream(file);
        props.store(out, "");
        out.close();
    }

import java.net.*;
import java.io.*;
public class URLConnectionReader {
    public static void main(String[] args) throws Exception {
        URL oracle = new URL("http://www.oracle.com/");
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) 
            System.out.println(inputLine);
        in.close();
    }
}

props.load(new StringReader(string));
