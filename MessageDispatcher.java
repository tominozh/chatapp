package ChatApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MessageDispatcher extends Thread {
	

	@Override
	public synchronized void run() {
		System.out.println("[Message Dispatcher] -- Started");

		while (true) {		           	
            
			String str = Server.queue.readFromQueue();
			//list for recipients
			ArrayList<Object> numbers = new ArrayList<>();
			//sender clientNumber (ID)
			int num = Integer.valueOf(str.substring(0, 1));
			String message = str.substring(1);
			
			try {
				System.out.println("going to sleep for 10s...");
				Thread.sleep(10000);
				System.out.println("waking up...");

			} catch (InterruptedException e1) {

				System.out.println("[Message Dispatcher Thread Sleep Exception] " + e1.getMessage());
			}
			//get recipients
			if(Server.recipients.containsKey(str)) {
				
				numbers.addAll(Server.recipients.get(str));
				//remove time stamp from recipients
				numbers.remove(numbers.size()-1);
				//remove the entry from recipients
				Server.recipients.remove(str);
			}				
			
			System.out.println("[Message Dispatcher] -- recipients list size " + numbers.size());

			//loop through recipients client numbers
			numbers.forEach((number) -> {

				ClientsHandler client = Server.clientHandlers.get((int) number);
				System.out.println("delivering message to client " + client.clientNumber);

				try {
					//num is senders clientNumber
					if (num == client.clientNumber) {
						/*
						 *  convert List<Integer> to String 
						 *  credit: https://stackoverflow.com/questions/599161/best-way-to-convert-an-arraylist-to-a-string
						 */
						String listString = numbers.stream().map(Object::toString).collect(Collectors.joining(", "));
						//TODO - bug - remove number num from listString
						
						client.outputStream.writeUTF("message delivered to clients : " + listString);

					} else {

						client.outputStream.writeUTF("<Client " + num + "> " + message);
					}

				} catch (IOException e) {

					System.out.println("[Message Dispatcher Exception] " + e.getMessage());
				}

			});	//end forEach

		}
	}
}