import java.lang.*;
import java.io.*;
public class MyThread implements Runnable {
  public void run() {
    try {
      for (int i = 0;;i++) {            
        Thread.sleep(1000);
        System.out.println("Hello "+ i);
      } 
    } catch (Exception ie) {
        System.out.printf("InterruptedException by user, wait...");             
    }          
  }
  public void shutdown() {
    System.out.printf("shutdown"); 
  }

  public static void main(String args[]){  
    MyThread m1=new MyThread();  
    Thread t1 =new Thread(m1);  
    t1.start(); 
    try {
      Thread.currentThread().sleep(10000);
    } catch (InterruptedException ie) {
        System.out.printf("InterruptedException by user, wait...");      
    }
    m1.shutdown();
  }      
}
