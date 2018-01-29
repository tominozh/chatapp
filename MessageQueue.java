package ChatApp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MessageQueue {

	List<String> queue = new ArrayList<>();	
	
	public synchronized void addToQueue(String msg) {	
		//recipient's clientNumber
		ArrayList<Object> numbers = new ArrayList<>(); 
		
		for (ClientsHandler client : Server.clientHandlers) {
			numbers.add(client.clientNumber);
		}
		//entry for recipients
		Server.recipients.put(msg, numbers);
		
		//for logging we need Message, recipients and time stamp
		Instant timestamp = Instant.now();
		numbers.add(timestamp);
		Server.logMap.put(msg, numbers);
		queue.add(msg);
		//TODO notify thread responsible for logging to write out the file 
		System.out.println("[MessageQueue] --" + msg + " added to queue at " + timestamp);
		notify();
	}

	public synchronized String readFromQueue() {
		while (queue.isEmpty()) {
			try {
				System.out.println("[MessageQueue] -- Waiting");
				wait();
			} catch (Exception ex) {
				System.out.println("[MessageQueue exception] " + ex.getMessage());
			}
		}
		Instant timestamp = Instant.now();
		String message = queue.remove(0);
		System.out.println("[MessageQueue]" + message.substring(1) + " dispatched at " + timestamp
				+ " messages in queue " + (queue.size() + 1));
		return message;
	}
}
