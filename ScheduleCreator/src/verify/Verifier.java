package verify;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import support.Log;
import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;
import flex.FlexRayVerifier;
import flexRay.FlexRayStaticMessageCollection;

public class Verifier
{
	private static String module = "Verifier";
	private Schedule schedule;
	private FlexRayStaticMessageCollection msgs;
	private String ecu;
	private HashMap<String, Double> delays = new HashMap<String, Double>();
	
	public Verifier(Schedule schedule, FlexRayStaticMessageCollection msgs)
	{
		this.schedule = schedule;
		this.msgs = msgs;
		this.ecu = null;
	}
	
	public Verifier(Schedule schedule, FlexRayStaticMessageCollection msgs, String ecu)
	{
		this.schedule = schedule;
		this.msgs = msgs;
		this.ecu = ecu;
	}
	
	private Integer verifyECU(String ecu)
	{
		Integer deadlineViolations = 0;
		
		Log.logLowln("processing "+ecu, module);
		FlexRayVerifier<String> verifier = new FlexRayVerifier<String>(
				schedule.getSlotsPerCycle(), 
				schedule.getNumberOfCycles(), 
				schedule.getCycleDuration(),
				schedule.getSlotDuration(),
				schedule.getSlotSize()-1);
		
		for (int k = 0; k < schedule.getNumberOfCycles(); k++)
		{
			for (int k2 = 0; k2 < schedule.getSlotsPerCycle(); k2++)
			{
				if(!schedule.getMessages(k, k2).isEmpty())
				{
					if(((Message)schedule.getMessages(k, k2).toArray()[0]).getSender().equals(ecu))
					{
						verifier.addSlotAtCycle(k2, k);
					}
				}
			}
		}
		
		Collection<Message> messages = new HashSet<Message>();
		messages.addAll(msgs.getMessages());
		
		/* remove all messages not of this ECU */
		for (Iterator<Message> it = messages.iterator(); it.hasNext();)
		{
			Message msg = (Message) it.next();
			
			if(!msg.getSender().equals(ecu))
			{
				it.remove();
			}
		}
		
		/* sort messages by deadline and length */
		List<Message> listOfMsgs = new LinkedList<Message>();
		listOfMsgs.addAll(messages);
		Collections.sort(listOfMsgs, new MsgComparator());
		
		int cnt = 1;
		for (Iterator it = listOfMsgs.iterator(); it.hasNext();)
		{
			Message message = (Message) it.next();
			
//			System.out.println(message.getName()+" period: "+message.getPeriod()+" size: "+message.getSize()+" priority: "+cnt);
			verifier.addMessage(message.getName(), cnt, message.getSize(), (int)message.getPeriod(), message.getName());
			cnt++;
		}
		
		verifier.calculate();
		for (Iterator it = listOfMsgs.iterator(); it.hasNext();)
		{
			Message message = (Message) it.next();
			
			Double wcrt = verifier.getDelay(message.getName());
			if(message.getDeadline()+schedule.getSlotDuration() < wcrt)
			{
				System.err.println(ecu+": Deadline violation for "+message.getName()+", deadline: "+message.getDeadline()+", WCRT: "+wcrt+", size: "+message.getSize());
				deadlineViolations++;
			}
			delays.put(message.getName(), wcrt);
//			Log.logLowln(message.getName()+" "+wcrt, module);
		}
		
		return deadlineViolations;
	}
	
	public Integer verify()
	{
		Integer deadlineViolations = 0;
		
		Log.logLowln("starting verification...", module);
		
		if(ecu == null)
		{
			for (Iterator j = schedule.getECUs().iterator(); j.hasNext();)
			{
				String ecu = (String) j.next();
				
				deadlineViolations += verifyECU(ecu);
			}
		}
		else
		{
			deadlineViolations += verifyECU(ecu);
		}
		Log.logLowln("verification finished with "+deadlineViolations+" deadline violations", module);
		return deadlineViolations;
	}
	
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
	        		/* messages basically identical, but no identical priorities allowed */
	        		return x.getName().compareTo(y.getName());
	        	}
	        }
	    }
	}
	
	public HashMap<String, Double> getDelays()
	{
		return delays;
	}
}
