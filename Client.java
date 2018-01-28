package ChatApp;

import java.io.*;
import java.net.*;
import java.util.Scanner;
/*
*@Author Tomas Haladek
*/

public class Client implements Runnable {
	final static int ServerPort = 15566;
	static DataInputStream dis = null;
	static DataOutputStream dos = null;
	static boolean isAlive = false;
	static Scanner scn = new Scanner(System.in);
	static Socket socket = null;

	public static void main(String args[]) throws UnknownHostException, IOException {

		// establish the connection
		socket = new Socket("localhost", ServerPort);

		if (socket.isConnected()) {
			// obtaining input and out streams
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			isAlive = true;
		}
		// if all good
		if (socket.isConnected() && dis != null && dos != null) {
			System.out.println("all good to start");

			// new thread to read and write message
			Thread t = new Thread(new Client());
			t.setDaemon(true);
			t.start();			
			while (isAlive) {
				dos.writeUTF(scn.nextLine());
			}
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

				if (responseLine.equals("BYE")) {
					isAlive = false;
					try {						
						System.out.println("You can close the terminal " + Thread.activeCount());
						dos.close();
						dis.close();
						scn.close();
						socket.close();

					} catch (Exception e) {
						System.out.println("[Client Exception] " + e.getMessage());
					}
				}
			}
		} catch (Exception e1) {
			System.out.println("[Client Exception - DIS] " + e1.getMessage());
		}
	}
}