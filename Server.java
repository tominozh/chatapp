package ChatApp;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.*;

// Server class
public class Server {

	// List to store active clients
	static List<ClientHandler> allClients = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		
	    //to open server socket we need
		InetAddress ip = InetAddress.getByName("localhost");
		int port = 15566;
		int queueLenght = 5;
		//we open server socket
		ServerSocket serverSocket = new ServerSocket(port, queueLenght, ip);
		
		if (!serverSocket.isClosed()) {
			System.out.println(serverSocket);
			Socket clientSoc = null;

			// running infinite loop for getting client request
			while (true) {
								
				System.out.println("number of live clients: " + allClients.size());
				
				clientSoc = serverSocket.accept();
				System.out.println("New client request received : " + clientSoc);

				// obtain input and output streams from connected socket
				DataInputStream dis = new DataInputStream(clientSoc.getInputStream());
				DataOutputStream dos = new DataOutputStream(clientSoc.getOutputStream());
				//create new ClientHandler and start it
				ClientHandler ch = new ClientHandler(clientSoc, "client "+allClients.size(), dis, dos);
				ch.start();
				// add this client to active clients list
				allClients.add(ch);
				//if everybody log out we close server socket as well
				if(allClients.isEmpty()) {
					break;
				}
			}
			serverSocket.close();
		}
	}
}

// ClientHandler class
class ClientHandler extends Thread {
	Scanner scn = new Scanner(System.in);
	private String name;
	final DataInputStream dis;
	final DataOutputStream dos;
	Socket socket;
	boolean isloggedin;
	private AtomicBoolean isAlive = new AtomicBoolean(true);

	// constructor
	public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
		this.dis = dis;
		this.dos = dos;
		this.name = name;
		this.socket = s;
		this.isloggedin = true;
	}

	@Override
	public void run() {

		while (isAlive.get()) {
			// to make sure all threads are alive
			for (ClientHandler ch : Server.allClients) {
				if (!ch.isloggedin) {
					System.out.println("removing client "+ch.name);
					Server.allClients.remove(ch);
				}
			}
			
			String received;
			try {
				received = dis.readUTF();

				if (received.contains("logout")) {
					this.dos.writeUTF("BYE");
					isloggedin = false;
					isAlive.set(false);
					this.dis.close();
					this.dos.close();
					this.socket.close();
					for(ClientHandler ch:Server.allClients) {
						if(ch!=this) {
							ch.dos.writeUTF(this.name+" logged out");
						}
					}
					
				} else if (!received.isEmpty()) {
					System.out.println("Server echo: "+received);
					for (ClientHandler ch : Server.allClients) {
						if (ch.isloggedin && !ch.equals(this)) {
							ch.dos.writeUTF(this.name + ": " + received);
						}else if(mc.equals(this)) {
							ch.dos.writeUTF("I wrote: "+received);
						}
					}
				}
			} catch (IOException e) {
				isloggedin = false;
				
			}
			break;
		}

	}
}
