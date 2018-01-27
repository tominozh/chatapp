package ChatApp;

import java.io.IOException;

public class MessageDispatcher extends Thread {

	@Override
	public void run() {
		System.out.println("Message Dispatcher Started");

		while (true) {

			String str = Server.queue.readFromQueue();
			int num = Integer.valueOf(str.substring(0, 1));
			String message = str.substring(1);

			try {

				System.out.println("going to sleep for 10s...");
				Thread.sleep(10000);
				System.out.println("waking up...");

			} catch (InterruptedException e1) {

				System.out.println("[Message Dispatcher Thread Sleep Exception] " + e1.getMessage());
			}

			for (ClientsHandler client : Server.clientThreads) {
				System.out.println("delivering message to client " + client.clientNumber);
				try {
					if (num == client.clientNumber) {
						client.outputStream.writeUTF("I said: " + message);
					} else {
						client.outputStream.writeUTF("<Client " + num + "> " + message);
					}

				} catch (IOException e) {

					System.out.println("[Message Dispatcher Exception] " + e.getMessage());
				}
			}
		}
	}
}