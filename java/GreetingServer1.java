// File Name GreetingServer.java
import java.net.*;
import java.io.*;

public class GreetingServer extends Thread {
   private ServerSocket serverSocket;

   public GreetingServer(int port) throws IOException {
      serverSocket = new ServerSocket(port);
      //serverSocket.setSoTimeout(10000);
   }

   public void run_server(){
      try{
         System.out.println("Waiting for client on port " +
            serverSocket.getLocalPort() + "...");
         Socket server = serverSocket.accept();
         System.out.println("Just connected to " + server.getRemoteSocketAddress());
         DataInputStream in = new DataInputStream(server.getInputStream());

         String textFromClient = null;
         while ((textFromClient = in.readLine()) != null){
             System.out.println(textFromClient);
         }
         server.close();
      }catch(SocketTimeoutException s) {
         System.out.println("Socket timed out!");
      }catch(IOException e) {
         e.printStackTrace();
      }
   }

   public void run() {
      while(true) {
         run_server();
      }
   }

   public static void main(String [] args) {
      int port = Integer.parseInt(args[0]);
      try {
         Thread t = new GreetingServer(port);
         t.start();
      }catch(IOException e) {
         e.printStackTrace();
      }
   }
}
