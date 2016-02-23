package flexRay;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import support.Log;
import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;
import fibex.FIBEXFile;
import fibex.document.FIBEXBasicInterface;
import fibex.document.FIBEXDocumentBasicInteraction;
import fibex.exceptions.FIBEXNotValidFileException;
import fibex.exceptions.FibexException;
import fibex.protocols.flexray.FlexRayCluster;
import fibex.structures.FibexChannel;
import fibex.structures.FibexCluster;
import fibex.structures.FibexECU;
import fibex.structures.FibexFrame;
import fibex.structures.FibexMessage;

/**
 * This class implements a FlexRay schedule. It is based on the Schedule interface.
 * It saves all attributes necessary to describe the schedule itself as well as all 
 * messages contained in the schedule. Only the static segment is considered here.
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 */
public class FlexRaySchedule implements Schedule
{
	private int maxCycleNumber;
	private int cycleDuration;
	private int payloadSize;
	private int slotsPerCycle;
	private double slotDuration;
	private Collection<ScheduleMessage> messages;
	private static final String module = "Sched";
	private Object additionalData;
	private FIBEXFile file;

	/**
	 * This subclass is a data storage for all messages contained in the schedule 
	 * and the assigned slot, repetition, base cycle and offset.
	 * 
	 * @author TUM CREATE - RP3
	 */
	private class ScheduleMessage
	{
		public Message<String> msg;	
		public int slot;
		public int repetition;
		public int baseCycle;
		public int offset;
	}
	
	/**
	 * This constructor is initializing all attributes necessary to describe a FlexRay schedule.
	 * 
	 * @param maxCycleNumber
	 * after this number, the schedule is repeated
	 * @param cycleDuration
	 * this is the duration of one cycle in milliseconds
	 * @param slotSize
	 * this is the length of the payload of one slot in byte
	 * @param slotsPerCycle
	 * this is the number of slots contained in one cycle
	 */
	public FlexRaySchedule(int maxCycleNumber, int cycleDuration, int slotSize, int slotsPerCycle)
	{
		this.maxCycleNumber = maxCycleNumber;
		this.cycleDuration = cycleDuration;
		this.payloadSize = slotSize;
		this.slotsPerCycle = slotsPerCycle;
		this.slotDuration = ((double)this.payloadSize*8/(double)FlexRayConstants.BUS_SPEED_BIT_P_SEC)*1000;
		
		messages = new HashSet<ScheduleMessage>();
	}
	
	/**
	 * This constructor is initializing all attributes necessary to describe a FlexRay schedule from a given FIBEX file.
	 * 
	 * @param fileName
	 * path and name of the FIBEX file
	 * @param clusterName
	 * name of the cluster in the FIBEX file to use for schedule
	 * @param numberOfCycles
	 * number of cycles for schedule (0: value in FIBEX file will be used)
	 * @param versionOverride
	 * use this version to parse, instead of version inside FIBEX file (empty String: no override)
	 */
	public FlexRaySchedule(String fileName, String clusterName, String channelShortName, int numberOfCycles, String versionOverride) throws FibexException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
		file = new FIBEXFile(fileName, versionOverride);
		
		if(file.getDocument() == null)
		{
			Log.logLowln("no document loaded", module);
		}
		else if(file.getDocumentAsInterface("fibex.document.FIBEXDocumentBasicInteraction") != null)
		{
			FIBEXBasicInterface doc = file.getDocumentAsInterface("fibex.document.FIBEXDocumentBasicInteraction");
			for (Iterator<FibexCluster> i = ((FIBEXDocumentBasicInteraction)doc).getClusters().iterator(); i.hasNext();)
			{
				FibexCluster cluster = (FibexCluster) i.next();
							
				if(cluster.getName().equals(clusterName))
				{
					if(cluster.getClusterType().equals("FlexRay"))
					{
						this.cycleDuration = (int)(((FlexRayCluster)cluster).getCycleDuration_micros()/1000);
						this.slotsPerCycle = ((FlexRayCluster)cluster).getNumberOfStaticSlots();
						this.payloadSize = ((FlexRayCluster)cluster).getStaticSlotPayloadLength_Byte();
						this.slotDuration = ((FlexRayCluster)cluster).getStaticSlotDuration_micros()/1000;
						if(numberOfCycles == 0)
						{
							this.maxCycleNumber = cluster.getNumberOfCycles();
						}
						else
						{
							this.maxCycleNumber = numberOfCycles;
						}

						messages = new HashSet<ScheduleMessage>();
						for (Iterator<FibexChannel> j = ((FibexCluster)cluster).getChannels().iterator(); j.hasNext();)
						{
							FibexChannel chan = (FibexChannel) j.next();
							
							if(chan.getName().equals(channelShortName))
							{
								if(chan.getFrames() != null)
								{
									for (Iterator<FibexFrame> k = chan.getFrames().iterator(); k.hasNext();)
									{
										FibexFrame frame = (FibexFrame) k.next();
										
										for (Iterator<FibexMessage> l = frame.getMessages().iterator(); l.hasNext();)
										{
											FibexMessage msg = (FibexMessage) l.next();
											
											ScheduleMessage message = new ScheduleMessage();
											
											message.baseCycle = frame.getBaseCycle();
											Collection<String> receivers = new LinkedHashSet<String>();
											for (Iterator<Object> m = msg.getReceivers().iterator(); m.hasNext();)
											{
												Object receiver = (Object) m.next();
												
												receivers.add((String)receiver);
											}
											message.msg = new FlexRayStaticMessage((int)frame.getLength_Byte(), frame.getCycleRepetition(), 0, "imported_"+msg.getName(), frame.getSender(), receivers);
											message.offset = msg.getOffset_Bit();
											message.repetition = frame.getCycleRepetition();
											message.slot = frame.getSlot();
											
											messages.add(message);
										}
									}
								}
							}
						}
					}
				}
				else
				{
					throw new FIBEXNotValidFileException();
				}
			}
			
			Log.logLowln("Schedule loaded from FIBEX with cycle duration: " + this.cycleDuration+
							"ms, slots per cycle: " + this.slotsPerCycle +
							" slot size: " + this.payloadSize +
							" Byte, slot duration: " + this.slotDuration+
							"ms, max cycle number: " + this.maxCycleNumber, module);
		}
	}
	
	/**
	 * This message returns the complete set of messages contained in this schedule.
	 * 
	 * @return
	 * collection of all messages
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Collection<Message> getMessages()
	{
		Collection<Message> LocalMessages = new HashSet<Message>();
		
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			
		
			LocalMessages.add(message.msg);
		}
		
		return LocalMessages;
	}

	/**
	 * This method returns the number of the slot (0..slotsPerCycle-1) in which 
	 * the given message is contained.
	 * 
	 * @param msg
	 * message to find
	 * 
	 * @return
	 * slot in which the message is contained
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public int getSlot(Message msg)
	{
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			
		
			if(message.msg.getName().equals(msg.getName()))
			{
				return message.slot;
			}
		}
		
		return -1;
	}
	
	/**
	 * This method returns the number of the slot (0..slotsPerCycle-1) in which 
	 * the given message is contained.
	 * 
	 * @param msg
	 * name of message to find
	 * 
	 * @return
	 * slot in which the message is contained
	 */
	@SuppressWarnings("rawtypes")
	public int getSlot(String msgName)
	{
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			

			if(message.msg.getName().equalsIgnoreCase(msgName))
			{
				return message.slot;
			}
		}
		
		return -1;
	}

	/**
	 * This method returns all messages contained in a given slot.
	 * 
	 * @param slot
	 * number of slot
	 * 
	 * @return
	 * all messages in given slot
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Collection<Message> getMessages(int slot)
	{
		Collection<Message> LocalMessages = new HashSet<Message>();
		
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			
		
			for (int j = message.baseCycle*getSlotsPerCycle()+message.slot; j < maxCycleNumber*getSlotsPerCycle(); j+=(message.repetition*getSlotsPerCycle()))
			{
				if(j == slot)
				{
					LocalMessages.add(message.msg);
				}
			}
		}
		
		return LocalMessages;
	}

	/**
	 * This method returns the repetition cycle of the given message in cycles.
	 * 
	 * @param msg
	 * message in question
	 * 
	 * @return
	 * repetition of message
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public int getRepetition(Message msg)
	{
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			
		
			if(message.msg.getName().equals(msg.getName()))
			{
				return message.repetition;
			}
		}
		
		return -1;
	}

	/**
	 * This method returns the base cycle of a given message in number of cycles.
	 * 
	 * @param msg
	 * message in question
	 * 
	 * @return
	 * base cycle of message
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public int getBaseCycle(Message msg)
	{
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			
		
			if(message.msg.getName().equals(msg.getName()))
			{
				return message.baseCycle;
			}
		}
		
		return -1;
	}

	/**
	 * This method returns the offset of the given message within its slot in bytes.
	 * 
	 * @param msg
	 * message in question
	 * 
	 * @return
	 * offset in bytes
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public int getOffset(Message msg)
	{
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			
		
			if(message.msg.getName().equals(msg.getName()))
			{
				return message.offset;
			}
		}
		
		return -1;
	}

	/**
	 * This method returns the duration of one cycle in milliseconds
	 * 
	 * @return
	 * duration of cycle in milliseconds
	 */
	@Override
	public int getCycleDuration()
	{
		return this.cycleDuration;
	}

	/**
	 * This method returns the duration of one slot in milliseconds.
	 * 
	 * @return
	 * duration of slot in milliseconds
	 */
	@Override
	public double getSlotDuration()
	{
		return slotDuration;
	}

	/**
	 * This method returns the slot size (payload size) set in bytes for this schedule.
	 * 
	 * @return
	 * size of slot in bytes
	 */
	@Override
	public int getSlotSize()
	{
		return this.payloadSize;
	}

	/**
	 * This method returns the number of slots in each cycle.
	 * 
	 * @return
	 * number of slots per cycle
	 */
	@Override
	public int getSlotsPerCycle()
	{
		return this.slotsPerCycle;
	}

	/**
	 * This method adds a given message with the given parameters to the schedule. 
	 * It should be called by the scheduler to add messages.
	 * 
	 * @param msg
	 * message to add
	 * @param slot
	 * slot of message within one cycle (0..slotsPerCycle-1)
	 * @param repetition
	 * repetition of message in cycles
	 * @param baseCycle
	 * base cycle of message
	 * @param offset
	 * offset of message within slot in byte
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addMessage(Message msg, int slot, int repetition, int baseCycle, int offset)
	{
		ScheduleMessage LocalMessage = new ScheduleMessage();	
		LocalMessage.msg = msg;
		LocalMessage.slot = slot;
		LocalMessage.repetition = repetition;
		LocalMessage.baseCycle = baseCycle;
		LocalMessage.offset = offset;
		this.messages.add(LocalMessage);
	}
	
	/**
	 * This method prints the schedule.
	 */
	public void print()
	{
		for (Iterator<ScheduleMessage> i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage msg = (ScheduleMessage)i.next();
			
			Log.logLowln("Message: " + msg.msg.getName() + "\t" + 
						 ": slot " + msg.slot + "\t" + 
						 "; rep " + msg.repetition + 
						 "; base " + msg.baseCycle +
						 "; offset " + msg.offset +
						 "; sender: " + msg.msg.getSender(), module);
		}
		
		LinkedList<slotAssignment> slotAssignments = new LinkedList<slotAssignment>();
		
		for (Iterator<ScheduleMessage> i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage msg = (ScheduleMessage)i.next();
			
			int cnt = msg.baseCycle*getSlotsPerCycle()+msg.slot;
			while(cnt < getSlotsPerCycle()*getNumberOfCycles())
			{
				slotAssignments.add(new slotAssignment(cnt, (String)msg.msg.getSender(), msg.msg.getName()));
				cnt += msg.repetition*getSlotsPerCycle();
			}
		}
		Collections.sort(slotAssignments, new innerComparator());
		
		for (Iterator<slotAssignment> i = slotAssignments.iterator(); i.hasNext();)
		{
			slotAssignment slotAssignment = (slotAssignment) i.next();
			
			Log.logLowln("slot: " + slotAssignment.slot + " " + slotAssignment.ecu + " " + slotAssignment.msgName, module);
		}
	}
	
	/**
	 * This class is a simple comparator to compare slot numbers
	 * 
	 * @author TUM CREATE - RP3 - Philipp Mundhenk
	 */
	class innerComparator implements Comparator<slotAssignment>
	{
	    public int compare(slotAssignment sa1, slotAssignment sa2)
	    {
	   
	    	if(sa1.slot < sa2.slot)
	        {
	        	return -1;
	        }
	    	else if(sa1.slot > sa2.slot)
	        {
	        	return 1;
	        }
	        else
	        {
	        	return 0;
	        }    
	    }  
	}
	
	/**
	 * This class describes the assignment of ECUs and messages to slots
	 * 
	 * @author TUM CREATE - RP3 - Philipp Mundhenk
	 */
	class slotAssignment
	{
		int slot;
		String ecu;
		String msgName;
		
		public slotAssignment(int slot, String ecu, String msgName)
		{
			this.ecu = ecu;
			this.slot= slot;
			this.msgName = msgName;
		}
	}

	/**
	 * This method returns all ECUs included in the schedule.
	 * 
	 * @return
	 * collection of all ECUs in the schedule
	 */
	@SuppressWarnings("rawtypes")
	public Collection getECUs()
	{
		LinkedHashSet<String> ecus = new LinkedHashSet<String>();
		for (Iterator i = this.messages.iterator(); i.hasNext();)
		{
			ScheduleMessage msg = (ScheduleMessage) i.next();
			
			ecus.add((String)msg.msg.getSender());
			for (Iterator j = msg.msg.getReceivers().iterator(); j.hasNext();)
			{
				String str = (String) j.next();
				
				ecus.add(str);
			}
		}
		
		return ecus;
	}

	/**
	 * This method returns the number of cycles before the schedule repeats.
	 * E.g. 64 for FlexRay 2.1A
	 * 
	 * @return
	 * number of cycles
	 */
	public int getNumberOfCycles()
	{
		return maxCycleNumber;
	}

	/**
	 * This method returns an array describing the usage of one slot over all cycles in the schedule
	 * 
	 * @param slot
	 * slot to check
	 * @param marker
	 * marker to use for slots in use (0 is used for free slots)
	 * @return
	 * array of integers, where 0 describes an empty slot and marker describes a slot in use 
	 */
	public int[] getFrameUsage(int slot, int marker)
	{
		int slotUsage[] = new int[getNumberOfCycles()];
		
		LinkedList<slotAssignment> slotAssignments = new LinkedList<slotAssignment>();
		
		for (Iterator<ScheduleMessage> i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage msg = (ScheduleMessage)i.next();
			
			int cnt = msg.baseCycle*getSlotsPerCycle()+msg.slot;
			while(cnt < getSlotsPerCycle()*getNumberOfCycles())
			{
				slotAssignments.add(new slotAssignment(cnt, (String)msg.msg.getSender(), msg.msg.getName()));
				cnt += msg.repetition*getSlotsPerCycle();
			}
		}
		Collections.sort(slotAssignments, new innerComparator());
		int cycle = 0;
		
		for (Iterator<slotAssignment> i = slotAssignments.iterator(); i.hasNext();)
		{
			slotAssignment slotAssignment = (slotAssignment) i.next();
			
			cycle = ((int)slotAssignment.slot/(int)getSlotsPerCycle());
			if(slotAssignment.slot % getSlotsPerCycle() == slot)
			{
				slotUsage[cycle] = marker;
			}
		}
		
		return slotUsage;
	}
	
	/**
	 * This method returns the additional data saved in the schedule
	 * 
	 * @return
	 * additional data
	 */
	public Object getAdditionalData()
	{
		return additionalData;
	}

	/**
	 * This method sets the additional data in the schedule
	 * 
	 * @param additionalData
	 * additional data
	 */
	public void setAdditionalData(Object additionalData)
	{
		this.additionalData = additionalData;
	}
	
	/**
	 * This method exports the schedule to a FIBEX file. Call via exportToFIBEX()
	 * 
	 * @param clusterName
	 * name of the cluster to save to
	 * @param channelName
	 * name of the channel to save schedule to
	 * @param targetFileName
	 * name of the file to save to
	 * @throws FibexException
	 * @throws Exception
	 */
	private void export(String clusterName, String channelName, String targetFileName) throws FibexException, Exception
	{
		if(file.getDocument() == null)
		{
			Log.logLowln("no document loaded", module);
		}
		else if(file.getDocumentAsInterface("fibex.document.FIBEXDocumentBasicInteraction") != null)
		{
			FIBEXBasicInterface doc = file.getDocumentAsInterface("fibex.document.FIBEXDocumentBasicInteraction");
			for (Iterator<FibexCluster> i = ((FIBEXDocumentBasicInteraction)doc).getClusters().iterator(); i.hasNext();)
			{
				FibexCluster cluster = (FibexCluster) i.next();
							
				if(cluster.getName().equals(clusterName))
				{
					if(cluster.getClusterType().equals("FlexRay"))
					{
						cluster.setNumberofCycles((short)maxCycleNumber);
						
						for (Iterator<FibexChannel> j = cluster.getChannels().iterator(); j.hasNext();)
						{
							FibexChannel channel = (FibexChannel) j.next();
							
							if(channel.getName().equals(channelName))
							{
								String controllerName = "Controller0";
								for (@SuppressWarnings("unchecked")Iterator<String> k = getECUs().iterator(); k.hasNext();)
								{
									String ecuName = (String) k.next();
									
									FibexECU ecu = cluster.createNewECU(ecuName);
									ecu.getNewController(controllerName);
								}
								
								for (Iterator<ScheduleMessage> k = messages.iterator(); k.hasNext();)
								{
									ScheduleMessage message = (ScheduleMessage) k.next();
									String frameName = "";
									frameName = message.msg.getName().substring(0, message.msg.getName().lastIndexOf('_'));
									FibexFrame frame = channel.getOrCreateFrame(frameName, (short)message.baseCycle, (short)message.repetition, getSlotSize(), (short)message.slot);
									frame.createNewMessage(message.offset*8, message.msg.getSize()*8, message.msg.getName(), message.msg.getSender(), message.msg.getReceivers());
									
									FibexECU senderECU = cluster.getECUByName((String)message.msg.getSender());
									senderECU.getControllerByName(controllerName).addFrameOutput(frame, channel);
									for (Iterator<String> l = message.msg.getReceivers().iterator(); l.hasNext();)
									{
										String receiver = (String)l.next();
										
										FibexECU receiverECU = cluster.getECUByName(receiver);
										receiverECU.getControllerByName(controllerName).addFrameInput(frame, channel);
									}
								}
							}
						}
					}
				}
				else
				{
					throw new FIBEXNotValidFileException();
				}
			}
			
			if(targetFileName.equals(""))
			{
				file.save(file.getFileName());
			}
			else
			{
				file.save(targetFileName);
			}
		}
	}
	
	/**
	 * This method exports the schedule into the FIBEX file it has been loaded from
	 * 
	 * @param clusterName
	 * cluster to export to
	 * @param channelName
	 * channel to export to
	 * @throws FibexException
	 * @throws Exception
	 */
	public void exportToFIBEX(String clusterName, String channelName) throws FibexException, Exception
	{
		export(clusterName, channelName, "");
	}
	
	/**
	 * This method exports the schedule to a new FIBEX file
	 * 
	 * @param fileName
	 * file to export schedule to
	 * @param clusterName
	 * cluster to export to
	 * @param channelName
	 * channel to export to
	 * @throws FibexException
	 * @throws Exception
	 */
	public void exportToFIBEX(String fileName, String clusterName, String channelName) throws FibexException, Exception
	{
		export(clusterName, channelName, fileName);
	}

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
	@Override
	public Collection<Message> getMessages(int cycle, int slot)
	{
		Collection<Message> LocalMessages = new HashSet<Message>();
		
		if(cycle >= getNumberOfCycles() || slot > getSlotsPerCycle())
		{
			return null;
		}
		
		for (Iterator i = messages.iterator(); i.hasNext();)
		{
			ScheduleMessage message = (ScheduleMessage) i.next();			
		
			for (int j = message.baseCycle*getSlotsPerCycle(); j < maxCycleNumber*getSlotsPerCycle(); j+=(message.repetition*getSlotsPerCycle()))
			{
				if(j == cycle*getSlotsPerCycle())
				{
					if(j+message.slot == j+slot)
					{
						LocalMessages.add(message.msg);
					}
				}
			}
		}
		
		return LocalMessages;
	}
}
