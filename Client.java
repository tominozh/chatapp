package ChatApp;

import java.io.*;
import java.net.*;
import java.util.Scanner;
/*
*@Author Tomas Haladek
*/

public class Client implements Runnable {
	final static int ServerPort = 15566;
	static int counter = 0;
	static DataInputStream dis = null;
	static DataOutputStream dos = null;
	static boolean isAlive = false;

	public static void main(String args[]) throws UnknownHostException, IOException {
		Scanner scn = new Scanner(System.in);
		// establish the connection
		Socket socket = new Socket("localhost", ServerPort);
		if (socket.isConnected()) {
			// obtaining input and out streams
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			isAlive = true;
		}
		//if all good
		if (socket.isConnected() && dis != null && dos != null) {
           //new thread to read and write message
			new Thread(new Client()).start();
			while (isAlive) {
				dos.writeUTF(scn.nextLine());
			}
			dos.close();
			dis.close();
			socket.close();
		}
	}


	@Override
	public void run() {
		String responseLine;
		try {
			while (isAlive) {
				responseLine = dis.readUTF();
				if (!responseLine.isEmpty()) {
					
					System.out.println(responseLine);
				}

				if (responseLine.contains("Bye"))
					isAlive = false;
			}
		} catch (Exception e) {

		}

	}
}