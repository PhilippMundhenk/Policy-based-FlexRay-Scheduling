package SchedulingInterfaces;

import java.util.Collection;

/**
 * This interface defines a scheduler needed in TDMA based communication systems.
 * 
 * @author TUM CREATE - RP3
 */
public interface Scheduler<MessageType>{
	
	/**
	 * This method is responsible for scheduling all messages.
	 * 
	 * @param schedule
	 * schedule containing all predefined parameters
	 * 
	 * @param messages
	 * set of messages to schedule
	 * 
	 * @return
	 * complete schedule, null in case of error
	 * 
	 * @throws Exception
	 * allows to further identify the error
	 */
	public Schedule schedule(Schedule schedule, Collection<MessageType> messages) throws Exception;
	
	public int getOverall();
}
