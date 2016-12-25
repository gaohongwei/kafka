import java.lang.*;
import java.io.*;
public class Worker implements Runnable {
  public void run() {
    try {
      for (int i = 0;;i++) {            
        Thread.sleep(1000);
        System.out.println("Hello "+ i);
      } 
    } catch (InterruptedException ie) {
        System.out.printf("InterruptedException in thread, wait...");      
    } catch (Exception ie) {
        System.out.printf("Exception...");             
    }          
  }
  public void cleanup() {
    System.out.printf("Will cleanup soon"); 
  }

  public static void main1(String args[]){  
    Worker worker=new Worker();  
    Thread worker_thread =new Thread(worker);  
    worker_thread.start(); 
    try {
        while(true)Thread.sleep(1000);
    } catch (InterruptedException ie) {
        System.out.printf("InterruptedException in main thread, wait...");    
    } catch (Exception ie) {
        System.out.printf("Exception...");             
    } 
    worker.cleanup();
  }  
  public static void main2(String args[]){  
    Worker worker=new Worker();  
    worker.run(); 
    try {
        Thread.sleep(10000);
    } catch (InterruptedException ie) {
        System.out.printf("InterruptedException in main thread, wait...");    
    }
    worker.cleanup();
  }   
}
