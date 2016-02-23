package scheduling.ILP;

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

import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;
import flexRay.FlexRayComparator;
import flexRay.FlexRayConstants;
import flexRay.FlexRayStaticMessage;

//TODO: check with Philipp if correct
public class SlotDist
{
	
	public Map<String,Integer> calculateSlotDistanceFL21(Schedule schedule, Collection<FlexRayStaticMessage> FlexRayStaticMessages){

		HashMap<String, Collection<String>> receivers = new HashMap<String, Collection<String>>();
		//get all ECUs:
		Set<String> ecus = new LinkedHashSet<String>();
		
		for (Iterator<FlexRayStaticMessage> i = FlexRayStaticMessages.iterator(); i.hasNext();)
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
				throw new IllegalArgumentException("this system cannot be scheduled, at least one of the deadlines is shorter than the given schedule allows");
			}
		}
		
		HashMap<String, Integer> dists = new HashMap<String, Integer>();
		
		/* for all ECUs */
		for (Iterator<String> i = ecus.iterator(); i.hasNext();)
		{
			String ecu = (String) i.next();
			Collection<String> ecuReceivers = new LinkedHashSet<String>();
			
			LinkedList<FlexRayStaticMessage> sortedMsgsForEcu = new LinkedList<FlexRayStaticMessage>();
			/* remove all FlexRayStaticMessages not for this ECU */
			for (Iterator<FlexRayStaticMessage> j = FlexRayStaticMessages.iterator(); j.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage)j.next();
				
				if((ecu.equalsIgnoreCase((String)msg.getSender())))
				{
					sortedMsgsForEcu.add(msg);
					ecuReceivers.addAll(msg.getReceivers());
				}
			}
			
			receivers.put(ecu, ecuReceivers);
			
			/* sort FlexRayStaticMessages according to deadline and size of FlexRayStaticMessage */
			Collections.sort(sortedMsgsForEcu, new MsgComparator());
			
			/* calculate distance between slots required for ECU via bandwidth requirements */
			double numberOfBytes = 0;
			for (Iterator<FlexRayStaticMessage> j = sortedMsgsForEcu.iterator(); j.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage) j.next();
				
				numberOfBytes += (((double)schedule.getCycleDuration()*(double)schedule.getNumberOfCycles())/(double)msg.getPeriod())*(double)(msg.getSize()+FlexRayConstants.HEADER_LEN);
			}
			double numberOfSlots = numberOfBytes/schedule.getSlotSize();
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
				int numberOfSlotsDelaying = delayedBytes / schedule.getSlotSize();
				int sizeLeftInSlot = schedule.getSlotSize() - (delayedBytes % schedule.getSlotSize());
				int responseTime = Integer.MAX_VALUE;
				while(responseTime > (msg.getDeadline() / schedule.getSlotDuration()))
				{
					int numberOfDynamicSegments = (int)Math.ceil((double)(numberOfSlotsDelaying*dist)/(double)schedule.getSlotsPerCycle());
					int dynamicSegmentDelay = numberOfDynamicSegments*(int)Math.ceil(((schedule.getCycleDuration()
								- (schedule.getSlotDuration()*schedule.getSlotsPerCycle()))/schedule.getSlotDuration()));
					
					if(sizeLeftInSlot >= (msg.getSize()+FlexRayConstants.HEADER_LEN))
					{
						/* fits in a slot already started, one extra slot for transmission time */
						responseTime = (dist * numberOfSlotsDelaying) + dist + 1 + dynamicSegmentDelay;
					}
					else
					{
						/* new slot is necessary for this FlexRayStaticMessage, one extra slot for transmission time */
						responseTime = (dist * (numberOfSlotsDelaying + 1)) + dist + 1 + dynamicSegmentDelay;
					}
					
					if(responseTime > (msg.getDeadline() / schedule.getSlotDuration()))
					{
						dist--;
					}
				}
				higherEqualPrioMsgs.add(msg);
			}
			
//			System.out.println("dist of "+ecu + " = "+dist);
			
			/* mapping of pure static distances to ILP distances (include dynamic segment, NIT and symbol window */
//			int nAll = (int)Math.ceil(schedule.getCycleDuration()/schedule.getSlotDuration());
//			int nAll = schedule.getSlotsPerCycle();
//			
//			if(dist == schedule.getSlotsPerCycle())
//			{
//				dist = nAll;
//			}
//			else if(dist > schedule.getSlotsPerCycle())
//			{
//				dist = (int)((Math.floor(dist/schedule.getSlotsPerCycle())*nAll)+dist%schedule.getSlotsPerCycle());
//			}
//			else if(dist < schedule.getSlotsPerCycle())
//			{
//				/* nothing to do here */
//			}
			
//			System.out.println("After ILP remapping of dist: ");
//			System.out.println("dist of "+ecu + " = "+dist);
			
			dists.put(ecu, dist);
		}
		
//		System.out.println("dists:");
//		for (Iterator<Map.Entry<String, Integer>> i = dists.entrySet().iterator(); i.hasNext();)
//		{
//			Map.Entry<String, Integer> entry = i.next();
//			System.out.println(entry.getKey()+": "+entry.getValue());			
//		}
		
		return dists;
	}
	
	public Map<String,Integer> calculateBandwidthRequirementFL30(Schedule schedule, Collection<FlexRayStaticMessage> FlexRayStaticMessages) throws Exception{
		
		LinkedList<FlexRayStaticMessage> msgs = new LinkedList<FlexRayStaticMessage>(FlexRayStaticMessages);
		
		/* sort all messages */
		Collections.sort(msgs, new FlexRayComparator());
		
		double notStaticSegmentDuration = schedule.getCycleDuration() - (schedule.getSlotDuration()*schedule.getSlotsPerCycle());
		HashMap<String, Integer> numberOfFrames = new HashMap<String, Integer>();
		Set<String> ecus = new LinkedHashSet<String>();
		HashMap<String, Double> shortestDeadlines = new HashMap<String, Double>(); //it is enough to only check for whole ECU, as this is only relevant for deadlines shorter than cycle length and as such affects all cycles
		HashMap<String, Collection<String>> allReceivers = new HashMap<String, Collection<String>>();
		
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
					int numberOfOccurences = (int)Math.ceil((schedule.getCycleDuration()*schedule.getNumberOfCycles())/msg.getPeriod());
					neededBytes += (numberOfOccurences * (msg.getSize()+FlexRayConstants.HEADER_LEN));
				}
			}
			
			numberOfFrames.put(ecu, (int)Math.ceil((double)neededBytes/(double)schedule.getSlotSize()));
		}
		
		HashMap<String, Integer> minimumFrames = new HashMap<String, Integer>();
		int notStaticSegmentInSlots =  schedule.getNumberOfCycles() * (int)Math.ceil(notStaticSegmentDuration/schedule.getSlotDuration());
		int numberOfFramesAvailable = (schedule.getNumberOfCycles()*schedule.getSlotsPerCycle()) + notStaticSegmentInSlots;
		
		System.out.println("notStaticSegmentInSlots = " + notStaticSegmentInSlots);
		
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
				int numberOfSlotsDelaying = delayedBytes / schedule.getSlotSize();
				int sizeLeftInSlot = schedule.getSlotSize() - (delayedBytes % schedule.getSlotSize());
				int responseTime = Integer.MAX_VALUE;
				while(responseTime > (msg.getDeadline() / schedule.getSlotDuration()))
				{
//					System.out.println("dist = "+dist);
					if(sizeLeftInSlot >= (msg.getSize()+FlexRayConstants.HEADER_LEN))
					{
						/* fits in a slot already started, one extra slot for transmission time */
						responseTime = (dist * numberOfSlotsDelaying) + dist + 1;
					}
					else
					{
						/* new slot is necessary for this message, one extra slot for transmission time */
						responseTime = (dist * (numberOfSlotsDelaying + 1)) + dist + 1;
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
		
		System.out.println("Bandwidth requirements (distance between slots):");
		for (Iterator<Map.Entry<String, Integer>> i = dists.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, Integer> entry = i.next();
			System.out.println(entry.getKey()+": "+entry.getValue());			
		}
		
		return dists;
	}
	
	public Map<String,Integer> calculateDeadlineRequirementFL30(Schedule schedule, Collection<FlexRayStaticMessage> FlexRayStaticMessages) throws Exception{
		LinkedList<FlexRayStaticMessage> msgs = new LinkedList<FlexRayStaticMessage>(FlexRayStaticMessages);
		
		/* sort all messages */
		Collections.sort(msgs, new FlexRayComparator());
		
		Set<String> ecus = new LinkedHashSet<String>();
		HashMap<String, Double> shortestDeadlines = new HashMap<String, Double>(); //it is enough to only check for whole ECU, as this is only relevant for deadlines shorter than cycle length and as such affects all cycles
		HashMap<String, Integer> returnValues = new HashMap<String, Integer>();
		
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
		
		for (Iterator<Map.Entry<String, Double>> i = shortestDeadlines.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) i.next();
			
			returnValues.put(entry.getKey(), (int)Math.floor(entry.getValue()/schedule.getSlotDuration()));
		}
		
		System.out.println("Deadline requirements (distance between slots):");
		for (Iterator<Map.Entry<String, Integer>> i = returnValues.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, Integer> entry = i.next();
			System.out.println(entry.getKey()+": "+entry.getValue());			
		}
		
		return returnValues;
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

}
