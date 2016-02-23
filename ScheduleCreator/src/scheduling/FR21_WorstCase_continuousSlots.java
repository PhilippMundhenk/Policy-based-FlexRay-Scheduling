package scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import support.Log;
import tests.ScheduleCreatorConfig;
import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayConstants;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticFrame;
import flexRay.FlexRayStaticMessage;


/**
 * This scheduler schedules messages for FlexRay. It is built according to version 2.1 of the FlexRay standard.
 * The schedule repeats after 64 cycles. Slot multiplexing of different ECUs over multiple cycles is not allowed.
 * ECU internal cycle multiplexing, to send different messages in different cycles in the same slot is allowed.
 * The aim of this scheduler is to reduce oversampling and schedule all messages in a minimum number of slots per ECU. 
 * It is assumed that all messages are ready and waiting to be sent at time t0.
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 *
 */
public class FR21_WorstCase_continuousSlots implements Scheduler<FlexRayStaticMessage>
{
	private static final String module = "WorstCase";
	private int overallSlots = 0;
	private HashMap<String, Integer> ecuStats;
	
	/**
	 * This method is called by the test module.
	 * 
	 * @param schedule
	 * schedule defining the parameters to align to
	 * @param messages
	 * messages to schedule
	 */
	@Override
	public Schedule schedule(Schedule schedule, Collection<FlexRayStaticMessage> messages) throws Exception
	{
		FlexRayStaticFrame.resetFrameNumbers();
		ecuStats = new HashMap<String, Integer>();
		
		if (0 == calcSlots((FlexRaySchedule)schedule, messages))
		{
			return schedule;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * This method does all initialization and starts the message placement for all ECUs.
	 * 
	 * @param schedule
	 * schedule defining the parameters to align to
	 * @param messages
	 * messages to schedule
	 * 
	 * @return
	 * 0: all messages scheduled successfully
	 * -1: failed to schedule messages
	 * @throws Exception 
	 */
	private int calcSlots(FlexRaySchedule schedule, Collection<FlexRayStaticMessage> messages) throws Exception
	{
		LinkedList<FlexRayStaticFrame> frames = new LinkedList<FlexRayStaticFrame>();
		HashMap<String, Collection<String>> receivers = new HashMap<String, Collection<String>>();
		
		for (int i = 0; i < (schedule.getSlotsPerCycle()*schedule.getNumberOfCycles()); i++) 
		{
			FlexRayStaticFrame frame = FlexRayStaticFrame.getNewNumberedFrame(schedule);
			frames.add(frame);
		}
		
		for (@SuppressWarnings("rawtypes")
		Iterator<Message> i = schedule.getMessages().iterator(); i.hasNext();)
		{
			FlexRayStaticMessage message = (FlexRayStaticMessage) i.next();
			
			int cnt = schedule.getBaseCycle(message);
			while(cnt*schedule.getSlotsPerCycle()+schedule.getSlot(message) < (schedule.getSlotsPerCycle()*schedule.getNumberOfCycles()))
			{
				frames.get(cnt*schedule.getSlotsPerCycle()+schedule.getSlot(message)).assignFrameToECU((String)message.getSender());
				frames.get(cnt*schedule.getSlotsPerCycle()+schedule.getSlot(message)).addPredefinedMessage(message);
				frames.get(cnt*schedule.getSlotsPerCycle()+schedule.getSlot(message)).setPredefinedFrame(true);
				cnt += schedule.getRepetition(message);
			}
		}
		
		Set<String> ecus = new LinkedHashSet<String>();
		
		for (Iterator<FlexRayStaticMessage> i = messages.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage msg = (FlexRayStaticMessage) i.next();
			
			ecus.add((String)msg.getSender());
			
			for (Iterator<String> j = msg.getReceivers().iterator(); j.hasNext();)
			{
				String string = (String) j.next();
				
				ecus.add(string);
			}
			
			if(msg.getDeadline() < (schedule.getCycleDuration() - (schedule.getSlotsPerCycle()*schedule.getSlotDuration())))
			{
				/* this system cannot be scheduled, at least one of the deadlines is shorter than the given schedule allows */
				return -1;
			}
		}
		
		HashMap<String, Integer> dists = new HashMap<String, Integer>();
		
		/* for all ECUs */
		for (Iterator<String> i = ecus.iterator(); i.hasNext();)
		{
			String ecu = (String) i.next();
			Collection<String> ecuReceivers = new LinkedHashSet<String>();
			
			LinkedList<FlexRayStaticMessage> sortedMsgsForEcu = new LinkedList<FlexRayStaticMessage>();
			/* remove all messages not for this ECU */
			for (Iterator<FlexRayStaticMessage> j = messages.iterator(); j.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage)j.next();
				
				if((ecu.equalsIgnoreCase((String)msg.getSender())))
				{
					sortedMsgsForEcu.add(msg);
					ecuReceivers.addAll(msg.getReceivers());
				}
			}
			
			receivers.put(ecu, ecuReceivers);
			
			/* sort messages according to deadline and size of message */
			Collections.sort(sortedMsgsForEcu, new MsgComparator());
			
			/* calculate distance between slots required for ECU via bandwidth requirements */
			double numberOfBytes = 0;
			for (Iterator<FlexRayStaticMessage> j = sortedMsgsForEcu.iterator(); j.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage) j.next();
//				System.out.println("msg="+msg.getName()+" period="+msg.getPeriod()+" size="+msg.getSize());
				Double bytesPerMessage = (double)(msg.getSize()+FlexRayConstants.HEADER_LEN);
				Double scheduleLength = ((double)schedule.getCycleDuration()*(double)schedule.getNumberOfCycles());
				Double msgsPerSchedule = scheduleLength/(double)msg.getPeriod();
				numberOfBytes += msgsPerSchedule*(double)bytesPerMessage;
//				System.out.println("numberOfBytes="+numberOfBytes);
			}
			double numberOfSlots = numberOfBytes/getUsableSlotSize(schedule);
			int dist = schedule.getSlotsPerCycle()/((int)Math.ceil(numberOfSlots/schedule.getNumberOfCycles()));
			
			/* calculation is based on slots */
			ArrayList<FlexRayStaticMessage> higherEqualPrioMsgs = new ArrayList<FlexRayStaticMessage>();
			for (Iterator<FlexRayStaticMessage> j = sortedMsgsForEcu.iterator(); j.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage) j.next();
				
				int delayedBytes = 0;
				for (Iterator<FlexRayStaticMessage> m = higherEqualPrioMsgs.iterator(); m.hasNext();)
				{
					FlexRayStaticMessage higherPriorityMsg = (FlexRayStaticMessage) m.next();
					
					delayedBytes += (int)Math.ceil((msg.getDeadline() / higherPriorityMsg.getPeriod()))*(higherPriorityMsg.getSize()+FlexRayConstants.HEADER_LEN);
				}
				
//				System.out.println("delayedBytes = "+delayedBytes);
				
				int numberOfSlotsDelaying = delayedBytes / getUsableSlotSize(schedule);
				int sizeLeftInSlot = getUsableSlotSize(schedule) - (delayedBytes % getUsableSlotSize(schedule));
				int responseTime = Integer.MAX_VALUE;
				
//				if(ecu.equals("ECU_4"))
//				{
//					System.out.println("dist = "+dist);
//				}
				
				while(responseTime > (msg.getDeadline() / schedule.getSlotDuration()))
				{
					int numberOfDynamicSegments = (int)Math.ceil((double)(numberOfSlotsDelaying*dist)/(double)schedule.getSlotsPerCycle());
					int dynamicSegmentDelay = numberOfDynamicSegments*(int)Math.ceil(((schedule.getCycleDuration()
								- (schedule.getSlotDuration()*schedule.getSlotsPerCycle()))/schedule.getSlotDuration()));
					
//					System.out.println("dynamicSegmentDelay = "+dynamicSegmentDelay);
					
					if(sizeLeftInSlot >= (msg.getSize()+FlexRayConstants.HEADER_LEN))
					{
						/* fits in a slot already started */
						responseTime = (dist * numberOfSlotsDelaying) + dist + dynamicSegmentDelay;
					}
					else
					{
						/* new slot is necessary for this message */
						responseTime = (dist * (numberOfSlotsDelaying + 1)) + dist + dynamicSegmentDelay;
					}
					
					if(responseTime > (msg.getDeadline() / schedule.getSlotDuration()))
					{
						dist--;
					}
					
//					if(ecu.equals("ECU_4"))
//					{
//						System.out.println("dist = "+dist);
//					}
					
				}
				higherEqualPrioMsgs.add(msg);
			}
			
			dists.put(ecu, dist);
		}
		
//		for (Iterator<Map.Entry<String, Integer>> iterator = dists.entrySet().iterator(); iterator.hasNext();)
//		{
//			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
//			
//			System.out.println(entry.getKey()+" "+entry.getValue());
//		}
			
		//place slots in distance found in response time analysis over whole schedule (use setFramesForAllCycles, check if frame in use by predefined ECU slots)
		
		/* iterate over all dists in ascending length */
		int dist = Integer.MAX_VALUE;
		Log.logLow("Scheduling: ", module);		
		while(!dists.isEmpty())
		{
//			System.out.println("dists: ");
//			for (Iterator<Map.Entry<String, Integer>> iterator = dists.entrySet().iterator(); iterator.hasNext();)
//			{
//				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
//				
//				System.out.println(entry.getKey()+" "+entry.getValue());
//			}
//			System.out.println();
			
			String ecu = "";
			for (Iterator<Map.Entry<String, Integer>> i = dists.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry<String, Integer> distEntry = (Map.Entry<String, Integer>) i.next();
				
//				System.out.println(distEntry.getValue()+" "+dist);
				if(distEntry.getValue() < dist)
				{
					dist = distEntry.getValue();
					ecu = distEntry.getKey();
//					System.out.println(ecu+" "+dist);
				}
			}
			
//			System.out.println();
//			System.out.println(ecu+" "+dist);
//			System.out.println();
			
//			Log.logLow(ecu + ", ", module);
			
			FlexRayStaticFrame firstPlaced = null;
			FlexRayStaticFrame lastPlaced = null;
			FlexRayStaticFrame lastFree = null;
			for (Iterator<FlexRayStaticFrame> i = frames.iterator(); i.hasNext();)
			{
				FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
				
				if(firstPlaced != null)
				{
					if(frame.getStartTimeMilli() >= firstPlaced.getStartTimeMilli() + schedule.getCycleDuration())
					{
						/* looped once around, stop here */
						break;
					}
				}
				
				if(frame.isFree())
				{
					/* this frame is not assigned to any ECU */
					lastFree = frame;
					if((firstPlaced == null) || (lastPlaced.getFrameNo() + dist == frame.getFrameNo()))
					{
						/* please first frame or place frame in exact dist from last placed frame */
						frame.assignFrameToECU(ecu);
//						System.out.println("assigning frame "+frame.getFrameNo()+"to "+ecu);
						if(firstPlaced == null)
						{
							/* if this is the first slot, remember it */
							firstPlaced = frame;
						}
						lastPlaced = frame;
						frames = setFramesForAllCycles(frames, frame.getFrameNo(), ecu, schedule);
					}
				}
				else
				{
					/* slot is not free */
					if((lastPlaced != null) && (lastPlaced.getFrameNo() + dist == frame.getFrameNo()))
					{
						/* slot is in ideal distance from last placed slot, assign to last free slot seen */
						lastFree.assignFrameToECU(ecu);
//						System.out.println("assigning frame "+frame.getFrameNo()+"to "+ecu);
						lastPlaced = frame;
						frames = setFramesForAllCycles(frames, lastFree.getFrameNo(), ecu, schedule);
					}
				}
			}
			
//			System.out.println("finished ecu" +ecu);
			dists.remove(ecu);
			dist = Integer.MAX_VALUE;			
		}
		
		Log.logLowln(module);
		
		
		/* statistics */
		for (int i = 0; i < schedule.getSlotsPerCycle(); i++)
//		for (int i = 0; i < frames.size(); i++)
		{
			FlexRayStaticFrame frame = (FlexRayStaticFrame)frames.get(i);
			
			if(frame.getEcu().isEmpty())
			{
				continue;
			}
			
			if(!ecuStats.containsKey(frame.getEcu()))
			{
				ecuStats.put(frame.getEcu(), 1);
			}
			else
			{
				int oldNumber = ecuStats.get(frame.getEcu());
				ecuStats.remove(frame.getEcu());
				ecuStats.put(frame.getEcu(), oldNumber+1);
			}
		}
		Log.logLowln("All messages scheduled!", module);
		Log.logLowln("Statistics:", module);
		List<String> ecuNames = new LinkedList<String>(ecus);
		Collections.sort(ecuNames);
		int complete = 0;
		
//		System.out.println("ecuStats.size() = "+ecuStats.size());
//		for (Iterator<Map.Entry<String, Integer>> iterator = ecuStats.entrySet().iterator(); iterator.hasNext();)
//		{
//			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
//			
//			System.out.println(entry.getKey()+" "+entry.getValue());
//		}
		
		for (Iterator<String> i = ecuNames.iterator(); i.hasNext();)
		{
			String ecu = (String)i.next();
//			System.out.println("ecuStats.size() = "+ecuStats.size());
//			System.out.println("ecu = "+ecu);
//			System.out.println("ecuStats.get(ecu) = "+ecuStats.get(ecu));
			complete += ecuStats.get(ecu);
			Log.logLowln(ecu + " needs " + ecuStats.get(ecu) + " slots", module);
		}
		Log.logLow("=> The system needs "+complete+" slots", module);
		Log.logLowln(module);
		
		overallSlots = complete;
		
		/* construct wrapper PDUs and add to schedule */
		constructWrapperPDUs(frames, schedule, receivers, ScheduleCreatorConfig.WRAPPER_PDU_LENGTH_BYTE);
		
		return 0;
	}
	
	private LinkedList<FlexRayStaticFrame> setFramesForAllCycles(LinkedList<FlexRayStaticFrame> frames, int slotNo, String ecu, Schedule schedule)
	{
		frames.get(slotNo).assignFrameToECU(ecu);
		for (int i = 0; i < schedule.getNumberOfCycles(); i++)
		{
			frames.get(i*schedule.getSlotsPerCycle()+slotNo).assignFrameToECU(ecu);
		}
		
		return frames;
	}
	
	private Integer getUsableSlotSize(Schedule schedule)
	{
		return schedule.getSlotSize()-1;
	}
	
	/**
	 * This method generates the wrapper PDUs from the slots.
	 * 
	 * @param frames
	 * list of slots with scheduled messages
	 * @param schedule
	 * schedule to write data to 
	 */
	private void constructWrapperPDUs(LinkedList<FlexRayStaticFrame> frames, FlexRaySchedule schedule, HashMap<String, Collection<String>> receivers, int wrapperLenByte)
	{
		String[] sender = new String[schedule.getSlotsPerCycle()];
		
		for (Iterator<FlexRayStaticFrame> i = frames.iterator(); i.hasNext();)
		{
			FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
			
			if(!frame.getMsgs().isEmpty())
			{
				if(frame.getMsgs().get(0).getDeadline() == 0)
				{
					sender[frame.getFrameNo() % schedule.getSlotsPerCycle()] = "";
					continue;
				}
			}
			sender[frame.getFrameNo() % schedule.getSlotsPerCycle()] = frame.getEcu();
		}
		
		for (int i = 0; i < schedule.getSlotsPerCycle(); i++)
		{
			for (int j = 0; j < schedule.getSlotSize()/wrapperLenByte; j++)
			{
				String nameSuffix;
				if(j < 10)
				{
					nameSuffix = "0"+j;
				}
				else
				{
					nameSuffix = Integer.toString(j);
				}
				
				if(sender[i].equals(""))
				{
					continue;
				}
				
				if(i<10)
				{
					FlexRayStaticMessage msg = new FlexRayStaticMessage(wrapperLenByte,
																		schedule.getSlotDuration(),
																		schedule.getSlotDuration(),
																		"wrapper000"+String.valueOf(i)+"_"+nameSuffix,
																		sender[i],
																		receivers.get(sender[i]));
					schedule.addMessage(msg, i, 1, 0, j*wrapperLenByte);
				}
				else if(i<100)
				{
					FlexRayStaticMessage msg = new FlexRayStaticMessage(wrapperLenByte,
																		schedule.getSlotDuration(),
																		schedule.getSlotDuration(),
																		"wrapper00"+String.valueOf(i)+"_"+nameSuffix,
																		sender[i],
																		receivers.get(sender[i]));
					schedule.addMessage(msg, i, 1, 0, j*wrapperLenByte);
				}
				else if(i<1000)
				{
					FlexRayStaticMessage msg = new FlexRayStaticMessage(wrapperLenByte,
																		schedule.getSlotDuration(),
																		schedule.getSlotDuration(),
																		"wrapper0"+String.valueOf(i)+"_"+nameSuffix,
																		sender[i],
																		receivers.get(sender[i]));
					schedule.addMessage(msg, i, 1, 0, j*wrapperLenByte);
				}
			}
		}
	}
	
	public static class MsgComparator implements Comparator<FlexRayStaticMessage>
	{
		@Override
	    public int compare(FlexRayStaticMessage x, FlexRayStaticMessage y)
	    {
			if (x.getDeadline() < y.getDeadline())
	        {
	            return -1;
	        }
	        else if (x.getDeadline() > y.getDeadline())
	        {
	            return 1;
	        }
	        else
	        {
	        	if (x.getSize() > y.getSize())
	        	{
	        		return -1;
	        	}
	        	else if (x.getSize() < y.getSize())
	        	{
	        		return 1;
	        	}
	        	else
	        	{
	        		return 0;
	        	}
	        }
	    }
	}

	@Override
	public int getOverall()
	{
		return overallSlots;
	}
}
