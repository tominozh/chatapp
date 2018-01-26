

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.net.InetAddress;
import java.net.ServerSocket;

 
 public class Server{
 // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;
 
  static List<ClientsHandler> clientThreads = new ArrayList<>();
  static ConcurrentLinkedQueue<ClientsHandler> queue = new ConcurrentLinkedQueue<>();
  static int i = 0;
  public static void main(String args[]) throws UnknownHostException {

    // Server socket properties
	  InetAddress ip = InetAddress.getByName("localhost");
		int port = 15566;
		int queueLenght = 5;

    try {
      serverSocket = new ServerSocket(port, queueLenght, ip);
      System.out.println("Server listening: "+serverSocket);
    }catch (IOException e) {
      System.out.println(e);
    }
   
     //Create a client socket for each connection and pass it to a new clients handler.
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        
       
     // obtain input and output streams from connected socket
		DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
		
		ClientsHandler client = new ClientsHandler(clientSocket, dis,dos, String.valueOf(i));
        client.start();
        clientThreads.add(client);
        i++;
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * Client class, when connection is accepted ClientHandler gets instantiated,
 * as long as connection is alive it echoes message send to all other clients.
 *if client enters "/quit" object gets destroyed and its place in clientThreads[] is
 *set to null. If client close terminal, NullPointerException is thrown and handled
 */
class ClientsHandler extends Thread {
  public static int counter = 0;
  private String clientName = "Client num:";
  private DataInputStream inputStream = null;
  private DataOutputStream outputStream = null;
  private Socket clientSocket = null;
 

  public ClientsHandler(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream, String num) {
	this.clientSocket = clientSocket;
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.clientName += num;
    counter++;
  }

  public void run() {
    

   try {
                 
      /* Welcome the new the client. */
      outputStream.writeUTF("Welcome " + this.clientName + " to our chat room.\nTo leave enter /quit in a new line.");
      
      synchronized (this) {      
        //let other clients know I joined the chat
        System.out.println("Server: Synchronizing all "+counter+" clients");
        for (ClientsHandler ch : Server.clientThreads) {
          if (ch != this) {
              ch.outputStream.writeUTF("A new user " + this.clientName + " entered the chat");
          }
        }
      }
      // Start the conversation. 
      while (true) {        
          synchronized (this){        	  
              String line = inputStream.readUTF();
              if (line.startsWith("/quit")) {
                for (ClientsHandler ch : Server.clientThreads) {
                    if ( ch != this) {
                        ch.outputStream.writeUTF("The user " + this.clientName+ " left the chat room.");
                    }
                    // Clean up. Set the current thread variable to null so that a new client
                    //could be accepted by the server.
                    else if (ch == this) {
                        outputStream.writeUTF("Bye " + this.clientName +"\nYou can close this terminal");
                        Server.clientThreads.remove(ch);
                        inputStream.close();
                        outputStream.close();
                        clientSocket.close();
                    }
                }
                System.out.println("Server: Synchronizing all "+counter+" clients");
            
            break; //from while if a client is leaving
        }else{            
        //write message to every client other than this client           
              System.out.println("Server: Synchronizing all "+counter+" clients");     
              System.out.println("going to sleep with message "+line);
              Thread t = Thread.currentThread();
              t.sleep(15000);
              System.out.println("woken up with message "+line);
              for (ClientsHandler ch : Server.clientThreads) {
                  if (!ch.clientName.equals(this.clientName)) {
                      ch.outputStream.writeUTF("<" + this.clientName + "> " + line);
                    }           
                }
            } 
        }
      }
            
    }catch(NullPointerException e){
        /*try{
        if(clientLeft(this.clientName)){
        }
        }catch(IOException ioexception){
           System.out.println("Cant close IO -- exception on line 144");
        }*/
    }catch (IOException e) {  //need this exception to OPEN IO
    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  

/*public boolean clientLeft (String cN)throws IOException{
      System.out.println("Server: "+this.clientName+" left the chat");
        synchronized(this){
            for (int i = 0; i <= maxClientsCount; i++) {
                if (clientThreads[i] != null && !clientThreads[i].clientName.equals(cN)) {
                    clientThreads[i].outputStream.println(cN+" left the chat");
                } 
                                    
                    try{
                    this.inputStream.close();
                    this.outputStream.close();
                    this.clientSocket.close();
                    counter--;
                    }catch(IOException e){
                        throw new IOException ("cant close IO");
                    }finally{
                    clientThreads[i] = null;
                    System.out.println("Server: Synchronizing all "+counter+" clients");
                    }
                    return true;
                
            }
        }
        return false;
    }*/
}