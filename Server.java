package ChatApp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Server {

	static ArrayList<ClientsHandler> clientHandlers = new ArrayList<>();
	static MessageQueue queue = new MessageQueue();
	static Semaphore lock = new Semaphore(1);
	public static void main(String[] args) throws Exception {
		
		 

		int i = 0; // used as naming scheme

		// Server socket properties
		InetAddress ip = InetAddress.getByName("localhost");
		int port = 15566;
		int queueLenght = 5;
		ServerSocket serverSocket = new ServerSocket(port, queueLenght, ip);
		MessageDispatcher md = null;
		if (serverSocket.isBound()) {
			System.out.println("Server listening: " + serverSocket);

			md = new MessageDispatcher();
			md.setDaemon(true);
			md.start();
		}

		Socket clientSocket = null;
		Thread th = null;
		ClientsHandler client = null;

		while (true) {
			//accept new client only when all messages are delivered
			
			
				//blocks here until socket accepted
				clientSocket = serverSocket.accept();
				System.out.println("[Server] client socket accepted, need lock, available permits "+lock.availablePermits());
			    lock.acquireUninterruptibly();
			    System.out.println("[Server] got lock, creating new client, available permits: "+lock.availablePermits());
				// get input and output streams from connected socket
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
				System.out.println("Client No. " + (i + 1) + " connected " + clientSocket);

				// create new client handler,thread, start it and add to list of existing
				// clients
				client = new ClientsHandler(clientSocket, dis, dos, i);
				th = new Thread(client);
				th.start();
				clientHandlers.add(client);
				i++;
				System.out.println("[Server] going to sleep for 3s before release, available permits "+lock.availablePermits());
				Thread.sleep(3000);
				lock.release();
				System.out.println("[Server] lock released "+lock.availablePermits());
			if (clientHandlers.isEmpty()) {
				// every client signed out break out of while loop
				break;
			}
		}
		System.out.println("Server socket closed");
		serverSocket.close();		
	}
	
}

/*
 * ClientHandler class creates new obj with client socket input and output data
 * streams. #when a client writes a message it adds it to MessageQueue
 */
class ClientsHandler extends Thread {
	protected int clientNumber;
	private DataInputStream inputStream = null;
	protected DataOutputStream outputStream = null;
	protected Socket clientSocket = null;
	private boolean isAlive = false;

	public ClientsHandler(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream, int num) {
		this.clientSocket = clientSocket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.clientNumber = num;
		isAlive = true;
	}

	@Override
	public void run() {
		try {
			while (isAlive) {
				String str = this.inputStream.readUTF();

				if (str.contentEquals("/quit")) {
					// this client is quitting
					this.isAlive = false; // to break out of while loop
					this.outputStream.writeUTF("BYE"); // to close client
					Server.queue.addToQueue(clientNumber + " has left the chat");
				} else {
					Server.queue.addToQueue(clientNumber + str);
				}
			}

		} catch (Exception e) {
			System.out.println("[ClientHandler Exception] " + e.getMessage());
		}

		finally {
			try {
				System.out.println("closing socket " + this.clientSocket);
				Server.clientHandlers.remove(this);
				this.inputStream.close();
				this.outputStream.close();
				this.clientSocket.close();
			} catch (Exception e) {
				System.out.println("[ClientHandler CleanUp Exception] " + e.getMessage());
			}
		}
	}
}