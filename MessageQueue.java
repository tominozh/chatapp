package ChatApp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class MessageQueue {

	List<String> queue = new ArrayList<>();
	Calendar calendar = GregorianCalendar.getInstance();

	public synchronized void addToQueue(String msg) {
		Instant timestamp = Instant.now();

		System.out.println("Message added to queue at " + timestamp);
		this.queue.add(msg);
		notify();
	}

	public synchronized String readFromQueue() {
		while (this.queue.isEmpty()) {
			try {
				System.out.println("Inside readFromQueue -- Waiting");
				wait();
			} catch (Exception ex) {
				System.out.println("Exception occured in readFromQueue");
			}
		}
		Instant timestamp = Instant.now();
		String message = this.queue.remove(0);
		System.out.println("Message from " + message.substring(0, 1) +" : "+message.substring(1)+ " dispatched at " + timestamp + " size of queue " + queue.size());
		return message;
	}
}
