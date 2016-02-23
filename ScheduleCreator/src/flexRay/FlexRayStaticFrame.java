package flexRay;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class FlexRayStaticFrame {
	private static int noFrames;
	private int frameNo;
	private int freePayload;
	private LinkedList<FlexRayStaticMessage> msgs;
	private LinkedHashSet<FlexRayStaticMessage> possibleMsgs;
	private LinkedHashSet<FlexRayStaticMessage> dueMsgs;
	private LinkedHashSet<FlexRayStaticMessage> deadlineMsgs;
	private String ecu;
	private int startTimeTenthMicro;
	private FlexRaySchedule schedule;
	private int initialPayloadSize;
	private String wrapper;
	private Boolean isPredefinedFrame;

	/**
	 * This constructor initializes all values for a frame. A counter determines the frame number. 
	 * Furthermore the time of the frame start transmission is calculated. Access via 
	 * 
	 * @param schedule
	 * schedule in which the frame is to be placed
	 */
	private FlexRayStaticFrame(FlexRaySchedule schedule, Boolean numbered)
	{
		int cycle;
		int offset;
		
		possibleMsgs = new LinkedHashSet<FlexRayStaticMessage>();
		dueMsgs = new LinkedHashSet<FlexRayStaticMessage>();
		deadlineMsgs = new LinkedHashSet<FlexRayStaticMessage>();
		initialPayloadSize = schedule.getSlotSize();
		freePayload = initialPayloadSize;
		msgs = new LinkedList<FlexRayStaticMessage>();
		ecu = new String();
		this.schedule = schedule;
		isPredefinedFrame = false;
		
		if(numbered)
		{
			frameNo = noFrames;
				
//			System.out.println("frameNo = "+frameNo);
			//first part: frame 0 per cycle is at multiple of the cycle duration, second part of equation is offset within cycle
			cycle = ((int)frameNo/(int)schedule.getSlotsPerCycle())*schedule.getCycleDuration()*1000*10;
//			System.out.println("cycle = "+cycle);
			offset = (int)((long)(frameNo % schedule.getSlotsPerCycle())*(schedule.getSlotDuration()*1000*10));
//			System.out.println("offset = "+offset);
			startTimeTenthMicro = cycle + (int)offset;
//			System.out.println("startTimeTenthMicro = "+startTimeTenthMicro);
		
			noFrames++;
		}
		else
		{
			frameNo = 0;
		}
	}
	
	/**
	 * This method overrides the frame number and sets the frame number passed as an argument.
	 * 
	 * @param frameNo
	 * slot number to set
	 */
	public void overrideFrameNo(int frameNo)
	{
		this.frameNo = frameNo;
		
		//first part: frame 0 per cycle is at multiple of the cycle duration, second part of equation is offset within cycle
		int cycle = ((int)frameNo/(int)schedule.getSlotsPerCycle())*schedule.getCycleDuration()*1000*10;
		int offset = (int)((long)(frameNo % schedule.getSlotsPerCycle())*(schedule.getSlotDuration()*1000*10));
		startTimeTenthMicro = cycle + (int)offset;
	}
	
	/**
	 * This method creates and returns a frame with frame number set. 
	 * The frame will increase the global frame count.
	 * 
	 * @return
	 * numbered frame
	 */
	public static FlexRayStaticFrame getNewNumberedFrame(FlexRaySchedule schedule)
	{
		return new FlexRayStaticFrame(schedule, true);
	}
	
	/**
	 * This method creates and returns a frame with frame number set to zero. 
	 * The frame will not increase the global frame count.
	 * 
	 * @return
	 * unnumbered frame
	 */
	public static FlexRayStaticFrame getNewUnnumberedFrame(FlexRaySchedule schedule)
	{
		return new FlexRayStaticFrame(schedule, false);
	}
	
	/**
	 * This method returns the start time of the frame in microseconds.
	 * 
	 * @return
	 * start time of frame in microseconds
	 */
	public double getStartTimeMicro() {
		return (double)startTimeTenthMicro/(double)10;
	}
	
	/**
	 * This method returns the start time of the frame in milliseconds.
	 * 
	 * @return
	 * start time of frame in milliseconds
	 */
	public double getStartTimeMilli() {
		return (double)getStartTimeMicro()/(double)1000;
	}
	
	/**
	 * This method returns the ECU that is occupying this frame.
	 * 
	 * @return
	 * name of the ECU occupying this frame
	 */
	public String getEcu() {
		return ecu;
	}

	/**
	 * This method returns the no of the frame.
	 * 
	 * @return
	 * number of frame
	 */
	public int getFrameNo() {
		return frameNo;
	}

	/**
	 * This message returns all messages assigned to this frame.
	 * 
	 * @return
	 * linked list of all messages
	 */
	public LinkedList<FlexRayStaticMessage> getMsgs()
	{
		return msgs;
	}
	
	/**
	 * This method returns the cycle this frame is assigned to.
	 * 
	 * @return
	 * cycle of frame
	 */
	public int getCycle()
	{
		return ((int)frameNo/(int)schedule.getSlotsPerCycle());
	}
	
	/**
	 * This method adds a message to the frame.
	 * 
	 * @param msg
	 * message to add
	 * 
	 * @return
	 * 0: message added correctly
	 * -1: not enough space in frame
	 * -2: frame is cannot be used for this message as it is occupied by another ECU
	 */
	public int addMessage(FlexRayStaticMessage msg)
	{
		if(ecu.isEmpty() || ecu.equalsIgnoreCase((String)msg.getSender()))
		{
			if((freePayload >= (msg.getSize() + FlexRayConstants.HEADER_LEN)))
			{
				/* enough space, message can be placed */
				msgs.add(msg);
				freePayload -= msg.getSize();
				freePayload -= FlexRayConstants.HEADER_LEN;
				return 0;
			}
			else
			{
				/* not enough space, return error */
				return -1;
			}
		}
		else
		{
			/* frame taken by other ecu */
			return -2;
		}
	}
	
	/**
	 * This method checks if the frame is free or occupied by any ECU.
	 * 
	 * @return
	 * true: frame is free
	 * false: frame is occupied
	 */
	public Boolean isFree()
	{
		if (this.ecu.isEmpty()) {
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * This method assigns the frame to a given ECU.
	 * 
	 * @param ecu
	 * name of ECU to assign to
	 * 
	 * @return
	 * 0: correctly assigned
	 * -1: frame is already in use by other ECU
	 */
	public int assignFrameToECU(String ecu)
	{
		if(this.ecu.isEmpty())
		{
			this.ecu = ecu;
			return 0;
		}
		else
		{
			return -1;
		}
	}
	
	/**
	 * This method checks the assigned ECU of this frame and the given message 
	 * and adds the message to this frame, if possible.
	 * 
	 * @param msg
	 * message to add
	 * 
	 * @return
	 * 0: message added correctly
	 * -1: not enough space in frame
	 * -2: frame is in use by other ECU
	 */
	public int addMessageToAssignedFrame(FlexRayStaticMessage msg)
	{
		if(isPredefinedFrame())
		{
			return -2;
		}
		if(ecu.equalsIgnoreCase((String)msg.getSender()))
		{
			if((freePayload >= (msg.getSize() + FlexRayConstants.HEADER_LEN)))
			{
				/* enough space, message can be placed */
				msgs.add(msg);
				freePayload -= msg.getSize();
				freePayload -= FlexRayConstants.HEADER_LEN;
				if(ecu.isEmpty())
				{
					ecu = (String)msg.getSender();
				}
				return 0;
			}
			else
			{
				/* not enough space, return error */
				return -1;
			}
		}
		else
		{
			/* frame taken by other ecu */
			return -2;
		}
	}
	
	/**
	 * This method adds a predefined message, e.g. from a FIBEX file, to the frame.
	 * Predefined messages do not need headers and can occupy full slots.
	 * 
	 * @param msg
	 * message to add to frame
	 * @return
	 *  0: message added correctly
	 * -1: not enough space in frame
	 * -2: frame is in use by other ECU
	 */
	public int addPredefinedMessage(FlexRayStaticMessage msg)
	{
		if(ecu.equalsIgnoreCase((String)msg.getSender()))
		{
			if(freePayload >= msg.getSize())
			{
				/* enough space, message can be placed */
				msgs.add(msg);
				freePayload -= msg.getSize();
				if(ecu.isEmpty())
				{
					ecu = (String)msg.getSender();
				}
				return 0;
			}
			else
			{
				/* not enough space, return error */
				return -1;
			}
		}
		else
		{
			/* frame taken by other ecu */
			return -2;
		}
	}
	
	/**
	 * This method deletes the assignment of frame to ECU.	
	 */
	public void deleteAssigment()
	{
		ecu = "";
	}
	
	/**
	 * This method returns the number of frames in the system.
	 * 
	 * @return
	 * number of frames in system
	 */
	public static int getNoOfFrames()
	{
		return noFrames;
	}
	
	/**
	 * This method removes all messages from the frame.
	 */
	public void resetPayload()
	{
		this.msgs.clear();
		this.freePayload = schedule.getSlotSize();
	}
	
	/**
	 * This method checks if a message is available in this frame.
	 */
	public Boolean containsMessage(FlexRayStaticMessage message)
	{
		return msgs.contains(message);
	}
	
	public Boolean containsMessage(String messageName)
	{
		for (Iterator<FlexRayStaticMessage> i = msgs.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage msg = (FlexRayStaticMessage) i.next();
			
			if (msg.getName().equals(messageName))
			{
				return true;
			}
		}
		return false;
	}
	
	public void addPossibleMsg(FlexRayStaticMessage msg)
	{
		possibleMsgs.add(msg);
	}
	
	public LinkedHashSet<FlexRayStaticMessage> getPossibleMsgs()
	{
		return possibleMsgs;
	}
	
	public int getFreePayload()
	{
		return freePayload;
	}
	
	public void removeMessage(FlexRayStaticMessage msg)
	{
		msgs.remove(msg);
		freePayload += msg.getSize();
		freePayload += FlexRayConstants.HEADER_LEN;
		if(initialPayloadSize == freePayload)
		{
			assignFrameToECU("");
		}
	}
	
	public Boolean isEmpty()
	{
		if (freePayload == initialPayloadSize)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String getWrapper()
	{
		return wrapper;
	}

	public void setWrapper(String wrapper)
	{
		this.wrapper = wrapper;
	}

	public void addDueMsg(FlexRayStaticMessage msg)
	{
		dueMsgs.add(msg);		
	}
	
	public Boolean containsDueMsg(FlexRayStaticMessage msg)
	{
		for (Iterator<FlexRayStaticMessage> i = dueMsgs.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage dueMsg = (FlexRayStaticMessage) i.next();
			
			if(dueMsg.getName().equals(msg.getName()))
			{
				return true;
			}
		}
		return false;
	}

	public void addDeadlineMsg(FlexRayStaticMessage msg)
	{
		deadlineMsgs.add(msg);
	}
	
	public Boolean containsDeadlineMsg(FlexRayStaticMessage msg)
	{
		for (Iterator<FlexRayStaticMessage> i = deadlineMsgs.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage deadlineMsg = (FlexRayStaticMessage) i.next();
			
			if(deadlineMsg.getName().equals(msg.getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public Boolean isPredefinedFrame()
	{
		return isPredefinedFrame;
	}

	public void setPredefinedFrame(Boolean isPredefinedFrame)
	{
		this.isPredefinedFrame = isPredefinedFrame;
	}
	
	public static void resetFrameNumbers()
	{
		noFrames = 0;
	}
}
