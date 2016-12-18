// File Name GreetingServer.java
import java.net.*;
import java.io.*;

public class GreetingServer {
   public static void main(String[] args) throws IOException {
      try{
         int port = Integer.parseInt(args[0]);
         ServerSocket serverSocket = new ServerSocket(port);
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
      }catch (Exception e0){
         e0.printStackTrace();
      }
   }
}


