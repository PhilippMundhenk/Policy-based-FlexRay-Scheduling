package scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import support.Log;
import tests.ScheduleCreatorConfig;
import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayComparator;
import flexRay.FlexRayConstants;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticFrame;
import flexRay.FlexRayStaticMessage;


/**
 * This scheduler schedules messages for FlexRay. It is built according to version 2.1 of the FlexRay standard.
 * The schedule repeats after 64 cycles. Slot multiplexing of different ECUs over multiple cycles is not allowed.
 * ECU internal cycle multiplexing, to send different messages in different cycles in the same slot is allowed.
 * The aim of this scheduler is to reduce oversampling and schedule all messages in a minimum number of slots per ECU. 
 * It is assumed that all messages are ready and waiting to be sent at time t0. It is assumed that deadline = period.
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 *
 */
public class FR301_WorstCase_continuousSlots implements Scheduler<FlexRayStaticMessage>
{
	private static final String module = "WorstCase";
	public static final int SCHEDULER_OPTIMIZATION_LEVEL = 1;
	static int numberOfWrappersCreated = 0;
	private int overallFrames = 0;
	
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
		
		if (0 == calcFrames((FlexRaySchedule)schedule, messages))
		{
			return schedule;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * This method is the main scheduler. It is divided into three phases:
	 * 1. Place all messages per ECU (by priorities)
	 * 2. optimize frame usage by replacing messages within their deadline constraints
	 * 3. generate wrapper-PDUs from the schedule built in phase 1 and 2
	 * 
	 * @param schedule
	 * schedule defining the parameters to align to
	 * @param messages
	 * messages to schedule
	 * 
	 * @return
	 * 0: all messages scheduled successfully
	 * -1: failed to schedule messages
	 */
	private int calcFrames(FlexRaySchedule schedule, Collection<FlexRayStaticMessage> messages) throws Exception
	{
		LinkedList<FlexRayStaticFrame> frames = new LinkedList<FlexRayStaticFrame>();
		
		/* create frame array */
		for (int i = 0; i < (schedule.getSlotsPerCycle()*schedule.getNumberOfCycles()); i++) 
		{
			FlexRayStaticFrame frame = FlexRayStaticFrame.getNewNumberedFrame(schedule);
			frames.add(frame);
		}
		
		/* copy imported messages */
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
		
		/* add one more frame so that deadline violation can occur in the end and frames are added correctly */
		frames.add(FlexRayStaticFrame.getNewNumberedFrame(schedule));
		
		/* start scheduling */
		if(-1 == calcFrames(messages, frames, schedule))
		{
			Log.logLowln("Scheduling failed!", module);
			return -1;
		}
				
		/* output complete schedule */
		Log.logMediumln(module);
		Log.logMediumln("Schedule:", module);
		Log.logMediumln(module);
		for (Iterator<FlexRayStaticFrame> j = frames.iterator(); j.hasNext();)
		{
			FlexRayStaticFrame frame = (FlexRayStaticFrame)j.next();
			
			Log.logMediumln("Frame " + frame.getFrameNo() + " (time=" + frame.getStartTimeMicro() + "us; cycle=" + frame.getCycle() + "; " + frame.getEcu() + ") :", module);
			for (Iterator<FlexRayStaticMessage> m = frame.getMsgs().iterator(); m.hasNext();) 
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage)m.next();
				Log.logMediumln("MsgID=" + msg.getName() + " Period=" + msg.getPeriod() + " " + msg.getSender() + " Len=" + msg.getSize(), module);
			}
			Log.logMediumln(module);
		}
		
		Log.logLowln("All messages scheduled!", module);
		
		/* output list of used frames (for external verification tools) */
		Log.logTraceln("Frames in use:", module);
		for (Iterator<FlexRayStaticFrame> j = frames.iterator(); j.hasNext();)
		{
			FlexRayStaticFrame frame = (FlexRayStaticFrame)j.next();
			
			if (!frame.isFree())
			{
				Log.logTraceln(String.valueOf(frame.getFrameNo()), module);
			}
		}
		
		return 0;
	}
	
	/**
	 * This method places all given messages for a given ECU into frames.
	 * 
	 * @param messages
	 * messages to schedule
	 * @param frames
	 * array of available frames
	 * @param schedule
	 * schedule defining the parameters to align to
	 * 
	 * @return
	 *  0: all messages scheduled successfully
	 * -1: failed to schedule messages
	 * @throws Exception 
	 */
	private int calcFrames(Collection<FlexRayStaticMessage> messages, LinkedList<FlexRayStaticFrame> frames, FlexRaySchedule schedule) throws Exception
	{
		LinkedList<FlexRayStaticMessage> msgs = new LinkedList<FlexRayStaticMessage>(messages);
				
		/* sort all messages */
		Collections.sort(msgs, new FlexRayComparator());
		for (Iterator<FlexRayStaticMessage> i = msgs.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage msg = (FlexRayStaticMessage) i.next();
			
			Log.logTraceln(msg.getName() + ",\t" + msg.getPeriod() + ",\t" + msg.getSize() + ",\t" + msg.getSender(), module);
		}
		
		Set<String> ecus = new LinkedHashSet<String>();
		HashMap<String, Double> shortestDeadlines = new HashMap<String, Double>(); //it is enough to only check for whole ECU, as this is only relevant for deadlines shorter than cycle length and as such affects all cycles
		
		/* generate ECU list from senders and receivers */
		for (Iterator<FlexRayStaticMessage> i = msgs.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage msg = (FlexRayStaticMessage) i.next();
			
			/* sender */
			ecus.add((String)msg.getSender());
			
			/* receivers */
			for (Iterator<String> j = msg.getReceivers().iterator(); j.hasNext();)
			{
				String string = (String) j.next();
				
				ecus.add(string);
			}
			
			/* save shortest deadline per ECU */
			if((!shortestDeadlines.containsKey((String)msg.getSender()) || (msg.getDeadline() < shortestDeadlines.get(msg.getSender()))))
			{
				shortestDeadlines.put((String)msg.getSender(), msg.getDeadline());
			}
		}
		
		/* try scheduling all messages */
		Log.logTraceln(module);
		Log.logTraceln("Scheduling...", module);
		Log.logTraceln(module);
		
		/* generate possible periods for wrapper-PDU repetition */
		ArrayList<Integer> periods = new ArrayList<Integer>();
		for (int i = 1; i <= schedule.getNumberOfCycles(); i++)
		{
			if ((schedule.getNumberOfCycles() % i) == 0)
			{
				periods.add(i);
			}
		}
		
		HashMap<String, Collection<String>> allReceivers = new HashMap<String, Collection<String>>();
		HashMap<String, Integer> numberOfFrames = new HashMap<String, Integer>();
		
		/* do scheduling for each ECU */
		for (Iterator<String> k = ecus.iterator(); k.hasNext();)
		{
			String ecu = (String) k.next();

			@SuppressWarnings("unchecked")
			Collection<FlexRayStaticMessage> messagesPerECU = (Collection<FlexRayStaticMessage>) msgs.clone();
			
			/* remove all messages not for this ECU */
			for (Iterator<FlexRayStaticMessage> i = messagesPerECU.iterator(); i.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage)i.next();
				
				if(!(ecu.equalsIgnoreCase((String)msg.getSender())))
				{
					i.remove();
				}
			}
			
			/* generate receiver list */
			Collection<String> receivers = new LinkedHashSet<String>();
			receivers.addAll(ecus);
			receivers.remove(ecu);
			allReceivers.put(ecu, receivers);
			
			/* Step 1: schedule all messages in each messages best frame */
			/* iterate over all messages */
			int neededBytes = 0;
			for (Iterator<FlexRayStaticMessage> i = messagesPerECU.iterator(); i.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage)i.next();
				
				if(!msg.getName().startsWith("imported_"))
				{
					//numberOfOccurences needs to consider 1 additional cycle to accommodate worst-case of message from last schedule
					int numberOfOccurences = (int)Math.ceil((schedule.getCycleDuration()*(schedule.getNumberOfCycles()+1))/msg.getPeriod());
					neededBytes += (numberOfOccurences * (msg.getSize()+FlexRayConstants.HEADER_LEN));
				}
			}
			
			numberOfFrames.put(ecu, (int)Math.ceil((double)neededBytes/(double)getUsableSlotSize(schedule)));
		}
		
		/* Step 3: spread used frames */
		/* set up of new frame list */
		double notStaticSegmentDuration = schedule.getCycleDuration() - (schedule.getSlotDuration()*schedule.getSlotsPerCycle());
		LinkedList<FlexRayStaticFrame> framesFinalized = new LinkedList<FlexRayStaticFrame>();
		String notStaticSegmentMarker = "NOT_STATIC_SEGMENT";
		
		int notStaticSegmentInSlots =  schedule.getNumberOfCycles() * (int)Math.ceil(notStaticSegmentDuration/schedule.getSlotDuration());
		int numberOfFramesAvailable = (schedule.getNumberOfCycles()*schedule.getSlotsPerCycle()) + notStaticSegmentInSlots;
		
		for (int i = 0; i < numberOfFramesAvailable; i++)
		{
			framesFinalized.add(FlexRayStaticFrame.getNewUnnumberedFrame(schedule));
		}
		
		/* renumber frames in new list */
		int newFrameNo = 0;
		for (Iterator<FlexRayStaticFrame> i = framesFinalized.iterator(); i.hasNext();)
		{
			FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
			frame.overrideFrameNo(newFrameNo);
			
			/* mark frames which do not belong to the static segment */
			int allFramesInCycle = numberOfFramesAvailable/schedule.getNumberOfCycles();
			int cycleNumber = (int)((newFrameNo-1)/(allFramesInCycle));
			if((newFrameNo >= cycleNumber*allFramesInCycle+schedule.getSlotsPerCycle()) && (newFrameNo < (cycleNumber+1)*allFramesInCycle))
			{
				frame.assignFrameToECU(notStaticSegmentMarker);
			}
			newFrameNo++;
		}
		
		/* copy imported messages */
		int offsetCounter = 0;
		for (Iterator<FlexRayStaticFrame> i = frames.iterator(); i.hasNext();)
		{
			FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
			
			if(!frames.get(frame.getFrameNo()).getMsgs().isEmpty())
			{
				if(frames.get(frame.getFrameNo()).getMsgs().get(0).getDeadline() == 0)
				{
					framesFinalized.set(frame.getFrameNo()+(notStaticSegmentInSlots/schedule.getNumberOfCycles())*offsetCounter, frame);
					framesFinalized.get(frame.getFrameNo()+(notStaticSegmentInSlots/schedule.getNumberOfCycles())*offsetCounter).setPredefinedFrame(true);
					offsetCounter++;
				}
			}
		}		
		
		HashMap<String, Integer> minimumFrames = new HashMap<String, Integer>();
		LinkedHashSet<String> schedulingOrder = new LinkedHashSet<String>();
		
		/* for every ECU find the smallest distance between frames, based on deadline and number of frames needed */
		HashMap<String, Integer> dists = new HashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Double>> i = shortestDeadlines.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, Double> deadline = (Map.Entry<String, Double>) i.next();
			
			/* it is not possible to schedule messages reliable with a shorter deadline than length of dynamic segment+NIT+symbol window */
			if(deadline.getValue() < notStaticSegmentDuration)
			{
				throw new Exception("Message deadline is too short for given schedule (shorter than Dynamic Segement + NIT + Symbol Window)");
			}

			/* count number of frames as determined above */
			int noFramesNeeded = numberOfFrames.get(deadline.getKey());
			
			minimumFrames.put(deadline.getKey(), noFramesNeeded);
			
			/* find distance between two frames in frames */
			double frameSpacingByBandwidth = numberOfFramesAvailable/noFramesNeeded;
			int dist = (int)Math.floor(frameSpacingByBandwidth);
			
			@SuppressWarnings("rawtypes")
			LinkedList<Message> sortedMsgsForEcu = new LinkedList<Message>();
			/* remove all messages not for this ECU */
			for (Iterator<FlexRayStaticMessage> j = msgs.iterator(); j.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage)j.next();
				
				if((deadline.getKey().equalsIgnoreCase((String)msg.getSender())))
				{
					sortedMsgsForEcu.add(msg);
				}
			}

			/* sort messages according to deadline and size of message */
			Collections.sort(sortedMsgsForEcu, new MsgComparator());
			
			/* calculate worst-case response times and adjust dist accordingly to accommodate 
			 * all higher or equal priority messages including their sizes */
			@SuppressWarnings("rawtypes")
			ArrayList<Message> higherEqualPrioMsgs = new ArrayList<Message>();
			for (@SuppressWarnings("rawtypes") Iterator<Message> j = sortedMsgsForEcu.iterator(); j.hasNext();)
			{
				@SuppressWarnings("rawtypes")
				Message msg = (Message) j.next();

				/* calculation is based on slots */
				int delayedBytes = 0;
				for (@SuppressWarnings("rawtypes") Iterator<Message> m = higherEqualPrioMsgs.iterator(); m.hasNext();)
				{
					@SuppressWarnings("rawtypes")
					Message higherPriorityMsg = (Message) m.next();
					
					delayedBytes += (int)Math.ceil((msg.getDeadline() / higherPriorityMsg.getPeriod()))*(higherPriorityMsg.getSize()+FlexRayConstants.HEADER_LEN);
				}
				int numberOfSlotsDelaying = delayedBytes / getUsableSlotSize(schedule);
				int sizeLeftInSlot = getUsableSlotSize(schedule) - (delayedBytes % getUsableSlotSize(schedule));
				int responseTime = Integer.MAX_VALUE;
				while(responseTime > (msg.getDeadline() / schedule.getSlotDuration()))
				{
//					System.out.println("dist = "+dist);
					if(sizeLeftInSlot >= (msg.getSize()+FlexRayConstants.HEADER_LEN))
					{
						/* fits in a slot already started */
						responseTime = (dist * numberOfSlotsDelaying) + dist;
					}
					else
					{
						/* new slot is necessary for this message*/
						responseTime = (dist * (numberOfSlotsDelaying + 1)) + dist;
					}
					
					if(responseTime > (msg.getDeadline() / schedule.getSlotDuration()))
					{
						dist--;
					}
				}
				higherEqualPrioMsgs.add(msg);				
			}
			
			dists.put(deadline.getKey(), dist);
		}
		
		System.out.println("dists:");
		for (Iterator<Map.Entry<String, Integer>> i = dists.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, Integer> dist = (Map.Entry<String, Integer>) i.next();
			
			System.out.println(dist.getKey()+" "+dist.getValue());
		}
		
		/* iterate over all dists in ascending length */
		int dist = Integer.MAX_VALUE;
		while(!dists.isEmpty())
		{
			String ecu = "";
			for (Iterator<Map.Entry<String, Integer>> i = dists.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry<String, Integer> distEntry = (Map.Entry<String, Integer>) i.next();
				
				if(distEntry.getValue() < dist)
				{
					dist = distEntry.getValue();
					ecu = distEntry.getKey();
				}
			}
			
			schedulingOrder.add(ecu);
			
			/* check schedule for all offsets within allowed dist, record number of frames necessary for every offset (dry-run for placing) */
			int necessaryFrames[] = new int[dist];
			for (int i = 0; i < dist; i++)
			{
				Boolean finish = false;
				Boolean secondRun = false;
				int j_old = -1;
				int lastPlaced = -1;
				int firstPlaced = numberOfFramesAvailable;
				for (int j = i; (j < numberOfFramesAvailable && j>j_old && !finish);)
				{
					j_old = j;
					if(secondRun)
					{
						finish = true;
					}
					while(!framesFinalized.get(j).isFree())
					{
						j++;
						if(j >= numberOfFramesAvailable)
						{
							j_old = j-numberOfFramesAvailable;
							lastPlaced = j_old;
							j = j-numberOfFramesAvailable;
							secondRun = true;
						}
						if((j-lastPlaced)*schedule.getSlotDuration() >= shortestDeadlines.get(ecu))
						{
							/* deadline violation, trace backwards */
							j--;
							while(!framesFinalized.get(j).isFree())
							{
								j--;
								if(j == -1)
								{
									j = numberOfFramesAvailable-1;
									finish = false;
								}
								if(j == lastPlaced)
								{
									throw new Exception("Cannot schedule frames, not enough space");
								}
							}
						}
					}
					necessaryFrames[i]++;
					if(firstPlaced == -1)
					{
						firstPlaced = j;
					}
					if(((j > j_old) && (j_old != 0)) || finish)
					{
						j = j_old;
					}
					lastPlaced = j;
					if(j+dist >= numberOfFramesAvailable)
					{
						j_old = j-numberOfFramesAvailable;
						lastPlaced = j_old;
						j = j-numberOfFramesAvailable;
						secondRun = true;
					}
					j+=dist;
				}
			}
			
			/* find smallest number of frames needed */
			int minFramesNeeded = Integer.MAX_VALUE;
			int offsetToUse = 0;
			for (int i = 0; i < necessaryFrames.length; i++)
			{
				if(necessaryFrames[i] < minFramesNeeded)
				{
					minFramesNeeded = necessaryFrames[i];
					offsetToUse = i;
				}
			}
			
			/* assign frames as determined above */
			Boolean finish = false;
			Boolean secondRun = false;
			int j_old = -1;
			int lastPlaced = -1;
			int firstPlaced = numberOfFramesAvailable;
			for (int j = offsetToUse; (j < numberOfFramesAvailable && j>j_old && !finish);)
			{
				j_old = j;
				if(secondRun)
				{
					finish = true;
				}
				if(secondRun && (j >= firstPlaced))
				{
					break;
				}
				while(!framesFinalized.get(j).isFree())
				{
					j++;
					if(j >= numberOfFramesAvailable)
					{
						j_old = j-numberOfFramesAvailable;
						lastPlaced = j_old;
						j = j-numberOfFramesAvailable;
						secondRun = true;
					}
					if((j-lastPlaced)*schedule.getSlotDuration() >= shortestDeadlines.get(ecu))
					{
						/* deadline violation, trace backwards */
						j--;
						while(!framesFinalized.get(j).isFree())
						{
							j--;
							if(j == -1)
							{
								j = numberOfFramesAvailable;
								finish = false;
							}
							if(j == lastPlaced)
							{
								throw new Exception("Cannot schedule frames, not enough space");
							}
						}
					}
				}
				framesFinalized.get(j).assignFrameToECU(ecu);
				if(firstPlaced == numberOfFramesAvailable)
				{
					firstPlaced = j;
				}
				if(((j > j_old) && (j_old != 0)) || finish)
				{
					j = j_old;
				}
				lastPlaced = j;
				if(j+dist >= numberOfFramesAvailable)
				{
					j_old = j-numberOfFramesAvailable;
					lastPlaced = j_old;
					j = j-numberOfFramesAvailable;
					secondRun = true;
				}
				j+=dist;
			}
			
			dists.remove(ecu);
			dist = Integer.MAX_VALUE;			
		}
		
		frames = new LinkedList<FlexRayStaticFrame>();
		
		/* record new distances */
		HashMap<String, Double> longestDistances = new HashMap<String, Double>();
		for (Iterator<String> j = schedulingOrder.iterator(); j.hasNext();)
		{
			String ecu = (String) j.next();
		
			double longestDistance = 0;
			FlexRayStaticFrame lastOccurence = FlexRayStaticFrame.getNewUnnumberedFrame(schedule);
			lastOccurence.assignFrameToECU(ecu);
			for (Iterator<FlexRayStaticFrame> i = framesFinalized.iterator(); i.hasNext();)
			{
				FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
				
				if(frame.getEcu().equals(ecu) && !frame.isPredefinedFrame())
				{
					if((frame.getFrameNo() - lastOccurence.getFrameNo())*schedule.getSlotDuration() > longestDistance)
					{
						longestDistance = (frame.getFrameNo() - lastOccurence.getFrameNo()) * schedule.getSlotDuration();
					}
					lastOccurence = frame;
				}
			}
			longestDistances.put(ecu, longestDistance);
		}
		
		/* renumber static frames, remove others */
		newFrameNo = 0;
		for (Iterator<FlexRayStaticFrame> i = framesFinalized.iterator(); i.hasNext();)
		{
			FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
			
			if(!frame.getEcu().equals(notStaticSegmentMarker))
			{
				frame.overrideFrameNo(newFrameNo);
				frames.add(frame);
				newFrameNo++;
			}
		}
		
		/* statistics */
		Log.logLowln("Statistics:", module);

		Log.logLow("Scheduling order: ", module);
		for (Iterator<String> i = schedulingOrder.iterator(); i.hasNext();)
		{
			String ecu = (String) i.next();
			
			Log.logLow(ecu + ", ", module);
		}
		Log.logLowln(module);
		
		HashMap<String, Integer> actualFrameUsage = new HashMap<String, Integer>();
		int actualFrameUsageComplete = 0;
		int minimumFrameUsageComplete = 0;
		for (Iterator<String> i = schedulingOrder.iterator(); i.hasNext();)
		{
			String ecu = (String) i.next();
			
			int actualNoFrames = 0;
			for (Iterator<FlexRayStaticFrame> j = frames.iterator(); j.hasNext();)
			{
				FlexRayStaticFrame frame = (FlexRayStaticFrame) j.next();
				
				if(ecu.equals(frame.getEcu()) && !frame.isPredefinedFrame())
				{
					actualNoFrames++;
				}
			}
			actualFrameUsage.put(ecu, actualNoFrames);
			actualFrameUsageComplete += actualNoFrames;
			minimumFrameUsageComplete += minimumFrames.get(ecu);
			
			Log.logLowln(ecu+": minimum frames: "+minimumFrames.get(ecu) + " after spreading: " + actualFrameUsage.get(ecu) 
					+ " => additional: " + (actualFrameUsage.get(ecu) - minimumFrames.get(ecu)) + " ("
					+ (double)(actualFrameUsage.get(ecu) - minimumFrames.get(ecu))/((float)minimumFrames.get(ecu)/(float)100) + "%)", module);
			
			Log.logLowln(ecu + ": longest distance between slots: " + longestDistances.get(ecu) + "ms, maximum allowed: " + shortestDeadlines.get(ecu) + "ms", module);
		}
		Log.logLowln("overall: minimum frames: "+minimumFrameUsageComplete + " after spreading: " +actualFrameUsageComplete 
					+ " => additional: " + (actualFrameUsageComplete-minimumFrameUsageComplete) + " (" 
					+ (float)(actualFrameUsageComplete-minimumFrameUsageComplete)/((float)minimumFrameUsageComplete/(float)100) + "%)", module);
		
		overallFrames = actualFrameUsageComplete;
		
		
		/* Step 4: Find periods, fill periods if necessary, set up wrapper PDUs */
		for (Iterator<String> k = ecus.iterator(); k.hasNext();)
		{
			String ecu = (String) k.next();
			
			/* iterate over all cycles */
			for (int i = 0; i < schedule.getSlotsPerCycle(); i++)
			{
				int frameUsage[] = new int[schedule.getNumberOfCycles()];
				
				/* initialization (get frames in use by other ECUs so far, cannot be used in periods) */
				frameUsage = schedule.getFrameUsage(i, 2);
				
				/* mark used frames */
				for (int j = 0; j < schedule.getNumberOfCycles(); j++)
				{
					if (frames.get(j*schedule.getSlotsPerCycle()+i).getEcu().equalsIgnoreCase(ecu))
					{
						frameUsage[j] = 1;
					}
				}
				
				/* search for first used frame */
				for (int j = 0; j < frameUsage.length; j++)
				{
					int baseCycle = -1;
					if (frameUsage[j] == 1)
					{
						baseCycle = j;
						/* look for next used frame */
						for (int l = j+1; l < frameUsage.length; l++)
						{
							if (frameUsage[l] == 1)
							{
								int cnt = l;
								int error = 0;
								int repetition = l-j;
								if (!periods.contains(repetition))
								{
									/* this repetition is not supported (not divider of schedule size) */
									continue;
								}
								if(baseCycle > (repetition-1))
								{
									/* this base cycle and repetition combo is not valid, problems at schedule borders exist */
									continue;
								}
								/* check if found spacing is valid for complete schedule */
								while (cnt < frameUsage.length)
								{
									if(frameUsage[cnt] != 1)
									{
										/* this period cannot be used for full schedule */
										error = 1;
										break;
									}
									cnt+=repetition;
								}
								/* no error */
								if (0 == error)
								{
									/* create new wrapper-PDU with baseCycle = baseCycle and repetition = l-j, use all receivers of this ECU */
									frames = constructWrapper(i, baseCycle, repetition, frames, schedule, allReceivers.get(ecu), ScheduleCreatorConfig.WRAPPER_PDU_LENGTH_BYTE);
									/* remove all marks */
									for (int m = baseCycle; m < frameUsage.length; m+=repetition)
									{
										frameUsage[m] = 0;
									}
									break;
								}
							}
						}
						if (frameUsage[j] == 1)
						{
							/* create new wrapper-PDU with baseCycle = baseCycle and repetition = cycle_length, use all receivers of this ECU */
							frames = constructWrapper(i, baseCycle, schedule.getNumberOfCycles(), frames, schedule, allReceivers.get(ecu), ScheduleCreatorConfig.WRAPPER_PDU_LENGTH_BYTE);
						}
					}
				}
			}
		}
		Log.logLowln("number of wrapper PDUs created: " + numberOfWrappersCreated, module);
		
		schedule.setAdditionalData(frames);
		
		return 0;
	}
	
	private Integer getUsableSlotSize(Schedule schedule)
	{
		return schedule.getSlotSize()-1;
	}
	
	/**
	 * This method generates the wrapper PDU for the given data and adds it to the schedule.
	 * 
	 * @param frame
	 * frame where the wrapper-PDU is located
	 * @param baseCycle
	 * base cycle of the wrapper-PDU to generate
	 * @param repetition
	 * repetition of the wrapper-PDU to generate
	 * @param frames
	 * list of frames with scheduled messages
	 * @param schedule
	 * given schedule
	 * @param receivers
	 * all receivers of the handled ECU
	 */
	private LinkedList<FlexRayStaticFrame> constructWrapper(int frame, int baseCycle, int repetition, LinkedList<FlexRayStaticFrame> frames, FlexRaySchedule schedule, Collection<String> receivers, int wrapperLenByte)
	{
		String sender = frames.get(baseCycle*schedule.getSlotsPerCycle()+frame).getEcu();
		FlexRayStaticFrame localFrames = frames.get(baseCycle*schedule.getSlotsPerCycle()+frame);
		String name = "";
		int[] frameUsage = schedule.getFrameUsage(frame, 1);
		
		if(localFrames.isPredefinedFrame())
		{
			return frames;
		}
		
		if(sender.equals(""))
		{
			/* wrong frame is provided */
			return frames;
		}
		
		numberOfWrappersCreated++;
		
//		for (int i = 0; i < getUsableSlotSize(schedule)/wrapperLenByte; i++)
		for (int i = 0; i < schedule.getSlotSize()/wrapperLenByte; i++)
		{
			String nameSuffix;
			if(i < 10)
			{
				nameSuffix = "0"+i;
			}
			else
			{
				nameSuffix = Integer.toString(i);
			}
			
			if(localFrames.getFrameNo()<10)
			{
				name = "wrapper000"+String.valueOf(localFrames.getFrameNo())+"_"+nameSuffix;
				FlexRayStaticMessage msg = new FlexRayStaticMessage(wrapperLenByte,  /* wrapperPDU always uses complete frame */
																	repetition, 				/* repetition found by calling method */
																	schedule.getSlotDuration(), /* deadline = minimal (standard slot duration) */
																	name, 						/* name of PDU */
																	sender, 					/* sender is ECU determined in calling method */
																	receivers);					/* complete list of receivers for ECU, determined in method before */
				schedule.addMessage(msg, localFrames.getFrameNo()%schedule.getSlotsPerCycle(), repetition, baseCycle, i*wrapperLenByte);		/* add new message with given parameters, offset is 0 (frame is filled completely) */
			}
			else if(localFrames.getFrameNo()<100)
			{
				name = "wrapper00"+String.valueOf(localFrames.getFrameNo())+"_"+nameSuffix;
				FlexRayStaticMessage msg = new FlexRayStaticMessage(wrapperLenByte, 
																	repetition, 
																	schedule.getSlotDuration(), 
																	name, 
																	sender, 
																	receivers);
				schedule.addMessage(msg, localFrames.getFrameNo()%schedule.getSlotsPerCycle(), repetition, baseCycle, i*wrapperLenByte);
			}
			else if(localFrames.getFrameNo()<1000)
			{
				name = "wrapper0"+String.valueOf(localFrames.getFrameNo())+"_"+nameSuffix;
				FlexRayStaticMessage msg = new FlexRayStaticMessage(wrapperLenByte, 
																	repetition, 
																	schedule.getSlotDuration(), 
																	name, 
																	sender, 
																	receivers);
				schedule.addMessage(msg, localFrames.getFrameNo()%schedule.getSlotsPerCycle(), repetition, baseCycle, i*wrapperLenByte);
			}
			else
			{
				name = "wrapper"+String.valueOf(localFrames.getFrameNo())+"_"+nameSuffix;
				FlexRayStaticMessage msg = new FlexRayStaticMessage(wrapperLenByte, 
																	repetition, 
																	schedule.getSlotDuration(), 
																	name, 
																	sender, 
																	receivers);
				schedule.addMessage(msg, localFrames.getFrameNo()%schedule.getSlotsPerCycle(), repetition, baseCycle, i*wrapperLenByte);
			}
		}
		
		int[] frameUsageNew = schedule.getFrameUsage(frame, 1);
		for (int i = 0; i < frameUsageNew.length; i++)
		{
			frameUsageNew[i] = frameUsageNew[i] - frameUsage[i];
		}
		
		for (int i = 0; i < frameUsageNew.length; i++)
		{
			if(frameUsageNew[i] == 1)
			{
				frames.get(i*schedule.getSlotsPerCycle()+frame).setWrapper(name);
			}
		}
		
		return frames;
	}
	
	@SuppressWarnings("rawtypes")
	public static class MsgComparator implements Comparator<Message>
	{
		@Override
	    public int compare(Message x, Message y)
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
		return overallFrames;
	}
}
