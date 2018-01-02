import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

 
 public class Server_new{
 // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;
  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int MAX_CLIENTS_COUNT = 3;
  private static final ClientsHandler[] clientThreads = new ClientsHandler[MAX_CLIENTS_COUNT];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 2222;
   
     //Open a server socket on the portNumber (default 2222).
    try {
      serverSocket = new ServerSocket(portNumber);
      System.out.println("Server listening at port number: "+ portNumber);
    }catch (IOException e) {
      System.out.println(e);
    }
   
     //Create a client socket for each connection and pass it to a new clients handler.
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < MAX_CLIENTS_COUNT; i++) {
          if (clientThreads[i] == null) {
            (clientThreads[i] = new ClientsHandler(clientSocket, clientThreads, String.valueOf(i+1))).start();
            break;
          }
        }
        if (i == MAX_CLIENTS_COUNT) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server reached its maximum "+MAX_CLIENTS_COUNT+" connections. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients.
 *  When a client leaves the chat room this thread informs also all the 
 *  clients about that and terminates.
 */
class ClientsHandler extends Thread {
  public static int counter = 0;
  private String clientName = "Client num:";
  private DataInputStream inputStream = null;
  private PrintStream outputStream = null;
  private Socket clientSocket = null;
  private final ClientsHandler[] clientThreads;
  private int maxClientsCount;

  public ClientsHandler(Socket clientSocket, ClientsHandler[] clientThreads, String num) {
    this.clientSocket = clientSocket;
    this.clientThreads = clientThreads;
    maxClientsCount = clientThreads.length;
    this.clientName += num;
    counter++;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    ClientsHandler[] clientThreads = this.clientThreads;

   try {
      /*
       * Create input and output streams for this client.
       */
      inputStream = new DataInputStream(clientSocket.getInputStream());
      outputStream = new PrintStream(clientSocket.getOutputStream());
           
      /* Welcome the new the client. */
      outputStream.println("Welcome " + this.clientName + " to our chat room.\nTo leave enter /quit in a new line.");
      
      synchronized (this) {      
        //let other clients know I joined the chat
        System.out.println("Server: Synchronizing all "+counter+" clients");
        for (int i = 0; i < maxClientsCount; i++) {
          if (clientThreads[i] != null && clientThreads[i] != this) {
              clientThreads[i].outputStream.println("A new user " + this.clientName + " entered the chat");
          }
        }
      }
      /* Start the conversation. */
      while (true) {        
          synchronized (this){
              String line = inputStream.readLine();
              if (line.startsWith("/quit")) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (clientThreads[i] != null && clientThreads[i] != this) {
                        clientThreads[i].outputStream.println("The user " + this.clientName+ " left the chat room.");
                    }
                    // Clean up. Set the current thread variable to null so that a new client
                    //could be accepted by the server.
                    else if (clientThreads[i] == this) {
                        outputStream.println("Bye " + this.clientName +"\nYou can close this terminal");
                        clientThreads[i] = null;
                        counter--;
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
              for (int i = 0; i < maxClientsCount; i++) {
                  if (clientThreads[i] != null && !clientThreads[i].clientName.equals(this.clientName)) {
                      clientThreads[i].outputStream.println("<" + this.clientName + "> " + line);
                    }           
                }
            } 
        }
      }
            
    }catch(NullPointerException e){
        try{
        if(clientLeft(this.clientName)){
        }
        }catch(IOException ioexception){
           System.out.println("Cant close IO -- exception on line 144");
        }
    }catch (IOException e) {  //need this exception to OPEN IO
    }
  }
  public boolean clientLeft (String cN)throws IOException{
      System.out.println("Server: "+this.clientName+" left the chat");
        synchronized(this){
            for (int i = 0; i < maxClientsCount; i++) {
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
    }
}