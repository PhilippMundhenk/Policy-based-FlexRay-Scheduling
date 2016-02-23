package SchedulingInterfaces;

import java.util.Collection;

/**
 * This interface is to represent a schedule of a time-triggered, cycle-based communication system (e.g. FlexRay static segment).
 * 
 * @author TUM CREATE - RP3
 */
public interface Schedule {
	
	/**
	 * This method returns all messages included in the schedule.
	 * 
	 * @return
	 * collection of all messages in the schedule
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Message> getMessages();
	
	/**
	 * This method returns all ECUs included in the schedule.
	 * 
	 * @return
	 * collection of all ECUs in the schedule
	 */
	@SuppressWarnings("rawtypes")
	public Collection getECUs();
	
	/**
	 * This method returns the slot number where a given message is scheduled in the system. Used for FlexRay 2
	 * 
	 * @param msg
	 * message in question
	 * 
	 * @return
	 * slot number of message
	 */
	@SuppressWarnings("rawtypes")
	public int getSlot(Message msg);
	
	/**
	 * This method returns all messages for one selected slot. Used for FlexRay 2
	 * 
	 * @param slot
	 * slot in question
	 * 
	 * @return
	 * collection of all messages in the requested slot
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Message> getMessages(int slot);
	
	/**
	 * This method returns all messages for one selected frame. Used for FlexRay 3
	 * 
	 * @param cycle
	 * cycle defining the frame
	 * @param slot
	 * slot defining the frame
	 * 
	 * @return
	 * collection of all messages in the requested frame, empty if no messages in frame, null if frame does not exist
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Message> getMessages(int cycle, int slot);
	
	/**
	 * This method returns the repetition of the given message.
	 * 
	 * @param msg
	 * message in question
	 * 
	 * @return
	 * repetition of the message in cycles
	 */
	@SuppressWarnings("rawtypes")
	public int getRepetition(Message msg);
	
	/**
	 * This method returns the base cycle of a given message.
	 * 
	 * @param msg
	 * message in question
	 * 
	 * @return
	 * base cycle of the message in cycles
	 */
	@SuppressWarnings("rawtypes")
	public int getBaseCycle(Message msg);
	
	/**
	 * This method returns the offset of a given message inside a TDMA slot.
	 * 
	 * @param msg
	 * message in question
	 * 
	 * @return
	 * slot internal offset of the message in byte
	 */
	@SuppressWarnings("rawtypes")
	public int getOffset(Message msg);
	
	/**
	 * This method returns the duration of one complete cycle in milliseconds
	 * 
	 * @return
	 * duration of cycle in milliseconds
	 */
	public int getCycleDuration();
	
	/**
	 * This method returns the duration of one TDMA slot in milliseconds.
	 * 
	 * @return
	 * duration of slot in milliseconds
	 */
	public double getSlotDuration();
	
	/**
	 * This method returns the size of one TDMA slot in byte.
	 * 
	 * @return
	 * size of slot in byte
	 */
	public int getSlotSize();
	
	/**
	 * This method returns the number of TDMA slots per cycle.
	 * 
	 * @return
	 * number of slots per cycle
	 */
	public int getSlotsPerCycle();
	
	/**
	 * This method returns the number of cycles before the schedule repeats.
	 * E.g. 64 for FlexRay 2.1A
	 * 
	 * @return
	 * number of cycles
	 */
	public int getNumberOfCycles();
	
	/**
	 * After scheduling, this method adds a scheduled message to the schedule.
	 * 
	 * @param msg
	 * scheduled message
	 * 
	 * @param slot
	 * slot in which the message is scheduled
	 * 
	 * @param repetition
	 * repetition of the message in number of cycles
	 * 
	 * @param baseCycle
	 * base cycle of the message in number of cycles
	 * 
	 * @param offset
	 * offset of the message within one slot in byte
	 */
	@SuppressWarnings("rawtypes")
	public void addMessage(Message msg, int slot, int repetition, int baseCycle, int offset);
	
}
