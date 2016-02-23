package SchedulingInterfaces;

import java.util.Collection;

/**
 * This class is to represent a message in a communication system.
 * 
 * @author TUM CREATE - RP3
 */
public abstract class Message<receiverType> {
	
	private int size;
	private double period;
	private double deadline;
	private String name;
	private Object sender;
	private Collection<receiverType> receivers;
	
	/**
	 * This constructor initializes all values of the message object to the values passed as arguments.
	 * 
	 * @param size
	 * size of message in bytes
	 * @param period
	 * period of message in milliseconds (ms)
	 * @param name
	 * unique identifier for message
	 * @param sender
	 * sending node of message
	 * @param receivers
	 * receiving node(s) of message
	 */
	public Message(int size, double period, double deadline, String name, Object sender,
			Collection<receiverType> receivers) {
		super();
		this.size = size;
		this.period = period;
		this.name = name;
		this.sender = sender;
		this.receivers = receivers;
		this.deadline = deadline;
	}
	
	/**
	 * This method returns the size of the message in bytes.
	 * 
	 * @return
	 * size of message in byte
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * This method returns the period of the message in milliseconds (ms).
	 * 
	 * @return
	 * period of message in milliseconds (ms)
	 */
	public double getPeriod() {
		return period;
	}
	
	/**
	 * This method returns the identifier of the message.
	 * 
	 * @return
	 * identifier of the message
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * This method returns the sending node of the message as an object.
	 * 
	 * @return
	 * sender of the message
	 */
	public Object getSender() {
		return sender;
	}
	
	/**
	 * This methods returns the receiver(s) of a message as a collection.
	 * 
	 * @return
	 * receiver(s) of message
	 */
	public Collection<receiverType> getReceivers() {
		return receivers;
	}
	
	/**
	 * This method returns the deadline in milliseconds.
	 * 
	 * @return
	 * deadline in milliseconds
	 */
	public double getDeadline()
	{
		return deadline;
	}
}
