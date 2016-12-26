import java.io.*;
import java.net.*;

public class SocketClient {
  public static void main(String argv[]) throws Exception {
    String str_write;
    String str_read;
    if (argv.length < 2 ){
      System.out.println("Usage: SocketClient port count");
      return;
    }
    int port = Integer.parseInt(argv[0]);
    int max  = Integer.parseInt(argv[1]);
    Socket clientSocket = new Socket("localhost", port);
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    long  TimeStart = System.currentTimeMillis();
    float TimePassed;    
    for(int index = 0; index < max; index++){
      str_write = "Hello " + index + '\n';
      outToServer.writeBytes(str_write);
    }
    TimePassed  = (System.currentTimeMillis()- TimeStart)/1000;
    System.out.printf("Summary:\nToatal messages:%d\nTotal Time(seconds):%.1f\nSend message per second: %.1f\n",max,TimePassed, max/TimePassed);    
    //str_read = inFromServer.readLine();
    //System.out.println(str_read);
    clientSocket.close();
  }
}
