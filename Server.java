package ChatApp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Server {

	static ArrayList<ClientsHandler> clientHandlers = new ArrayList<>();
	static MessageQueue queue = new MessageQueue();
	static Map<String, List<Object>> recipients = new HashMap<>();
	static ConcurrentHashMap<String, Object> logMap = new ConcurrentHashMap<>();

	public static void main(String[] args) throws Exception {

		int i = 0; // used as naming scheme
		
        // Server socket properties
		InetAddress ip = InetAddress.getByName("localhost");
		int port = 15566;
		int queueLenght = 5;
		ServerSocket serverSocket = new ServerSocket(port, queueLenght, ip);

		if (serverSocket.isBound()) {
			
			System.out.println("[Server] -- listening: " + serverSocket);

			MessageDispatcher md = new MessageDispatcher();
			md.setDaemon(true);
			md.start();

		}

		Socket clientSocket = null;
		Thread th = null;
		ClientsHandler client = null;

		while (true) {
			
			if(!logMap.isEmpty()) {
	        	for(Map.Entry<String, Object> entry : logMap.entrySet()) {
	        		System.out.println(entry.getKey());
	        		ArrayList<Object> objects = (ArrayList) entry.getValue();
	        		for(Object obj : objects) {
	        			System.out.println("[LOG] -- "+obj.toString());
	        		}
	        	}
			}
			
			// blocks here until socket accepted
			clientSocket = serverSocket.accept();

			// get input and output streams from connected socket
			DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
			System.out.println("[Server] -- Client No. " + (i + 1) + " connected " + clientSocket);

			// create new client handler,thread, start it and add to list of existing
			// clients
			client = new ClientsHandler(clientSocket, dis, dos, i);
			th = new Thread(client);
			th.start();
			clientHandlers.add(client);
			//Thread.sleep(3000);
			i++;
            if(clientHandlers.isEmpty()) {
            	break;
            }
		}
		serverSocket.close();
		System.out.println("[Server] -- server socket closed");

	}

}

/*
 * ClientHandler class creates new obj with client socket input and output data
 * streams. #when a client writes a message it adds it to MessageQueue
 */
class ClientsHandler extends Thread {
	protected int clientNumber;
	protected DataInputStream inputStream = null;
	protected DataOutputStream outputStream = null;
	protected Socket clientSocket = null;
	protected boolean isInChatRoom = false;

	public ClientsHandler(Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream, int num) {
		this.clientSocket = clientSocket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.clientNumber = num;
		isInChatRoom = true;
		
	}

	@Override
	public void run() {
		try {			
			while (isInChatRoom) {					
					String str = this.inputStream.readUTF();

					if (str.contentEquals("/quit")) {
						// this client is quitting
						this.isInChatRoom = false; // to break out of while loop
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
				System.out.println("[Server] -- closing socket " + this.clientSocket);
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