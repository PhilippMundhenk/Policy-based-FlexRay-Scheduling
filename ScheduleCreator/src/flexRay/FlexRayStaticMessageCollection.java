package flexRay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import support.Log;
import flexRay.FlexRayFileInput.FileFormat;
import flexRay.FlexRayFileInput.ReductionMode;

/**
 * This class acts as a storage for all messages in the schedule.
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 */
public class FlexRayStaticMessageCollection
{
	private static final String module = "MsgCollct";
	
	private String[] ecus;
	private Collection<FlexRayStaticMessage> messages;
	
	/**
	 * This method returns all ECUs which are part of the system.
	 * 
	 * @return
	 * all ECUs in the system
	 */
	public String[] getEcus()
	{
		return ecus;
	}
	
	/**
	 * This method stores all ECUs received as a parameter.
	 * 
	 * @param ecus
	 * all ECUs in the system
	 */
	public void setEcus(String[] ecus) 
	{
		this.ecus = ecus;
	}
	
	/**
	 * This method returns all messages in the system as a collection.
	 * 
	 * @return
	 * collection of all messages in the system
	 */
	public Collection<FlexRayStaticMessage> getMessages()
	{
		return messages;
	}
	
	/**
	 * This method stores all messages given as a collection.
	 * 
	 * @param messages
	 * collection of all messages in the system
	 */
	public void setMessages(Collection<FlexRayStaticMessage> messages) 
	{
		this.messages = messages;
	}
	
	/**
	 * This method stores all messages given as an array of string arrays. 
	 * Each line contains the data for one message, the columns are sorted 
	 * as in the csv file representation of a FlexRay system:
	 * sender, name, size, period, (receiver, )+
	 * 
	 * @param messages
	 * table of all messages
	 */
	public void setMessages(String[][] messages, FileFormat fileFormat, ReductionMode reductionMode, Integer numberOfConfigs) 
	{
		this.messages = new LinkedHashSet<FlexRayStaticMessage>();
		
		if(fileFormat.equals(FileFormat.WITH_DEADLINE))
		{
			FlexRayStaticMessage message;
			for (int i = 0; i < messages.length; i++)
			{
				Set<String> receivers = new HashSet<String>();
				for (int j = 5; j < messages[i].length; j++)
				{
					receivers.add(messages[i][j]);
				}
				message = new FlexRayStaticMessage(Integer.parseInt(messages[i][2]), Double.parseDouble(messages[i][3]), 
									Double.parseDouble(messages[i][4]), messages[i][1], (Object)messages[i][0], receivers);
				this.messages.add(message);
			}
		}
		else if(fileFormat.equals(FileFormat.MULTI_MODE))
		{
			if(reductionMode.equals(ReductionMode.CONVENTIONAL))
			{
				LinkedHashMap<String, LinkedHashSet<MultiModeMessage>> multiModeMessages = new LinkedHashMap<String, LinkedHashSet<MultiModeMessage>>();
				
				for (int i = 0; i < messages.length; i++)
				{
					Set<String> receivers = new HashSet<String>();
					for (int j = 6; j < messages[i].length; j++)
					{
						receivers.add(messages[i][j]);
					}
					
					MultiModeMessage msg = new MultiModeMessage();
					msg.size = Integer.parseInt(messages[i][3]);
					msg.period = Double.parseDouble(messages[i][4]);
					msg.name = messages[i][1];
					msg.deadline = Double.parseDouble(messages[i][4]);
					msg.sender = (Object)messages[i][0];
					msg.receivers = receivers;
					msg.config = Integer.parseInt(messages[i][2]);

					if(null == multiModeMessages.get(msg.name))
					{
						multiModeMessages.put(msg.name, new LinkedHashSet<MultiModeMessage>());
					}
					multiModeMessages.get(msg.name).add(msg);
				}
				
				for (Iterator<String> it = multiModeMessages.keySet().iterator(); it.hasNext();)
				{
					String msgName = (String) it.next();
					
					if(multiModeMessages.get(msgName).size() > 1)
					{
						Double smallestPeriod = Double.MAX_VALUE;
						Integer biggestSize = 0;
						for (Iterator<MultiModeMessage> i = multiModeMessages.get(msgName).iterator(); i.hasNext();)
						{
							MultiModeMessage msg = (MultiModeMessage) i.next();
							
							if(msg.period < smallestPeriod)
							{
								smallestPeriod = msg.period;
							}
							if(msg.size > biggestSize)
							{
								biggestSize = msg.size;
							}
						}
						
						FlexRayStaticMessage message;
						MultiModeMessage tmpMsg = ((MultiModeMessage)multiModeMessages.get(msgName).toArray()[0]);
						message = new FlexRayStaticMessage(
											biggestSize,
											smallestPeriod,
											smallestPeriod,
											tmpMsg.name,
											tmpMsg.sender,
											tmpMsg.receivers);
						this.messages.add(message);
					}
					else if(multiModeMessages.get(msgName).size() == 1)
					{
						FlexRayStaticMessage message;
						MultiModeMessage tmpMsg = ((MultiModeMessage)multiModeMessages.get(msgName).toArray()[0]);
						message = new FlexRayStaticMessage(
											tmpMsg.size,
											tmpMsg.period,
											tmpMsg.deadline,
											tmpMsg.name,
											tmpMsg.sender,
											tmpMsg.receivers);
						this.messages.add(message);
					}
				}
			}
			else if(reductionMode.equals(ReductionMode.POLICY))
			{
				LinkedHashMap<Object, LinkedHashMap<Integer, TreeSet<MultiModeMessage>>> multiModeMessages = new LinkedHashMap<Object, LinkedHashMap<Integer, TreeSet<MultiModeMessage>>>();
				
				for (int i = 0; i < messages.length; i++)
				{
					/* message is equal for all configs */

					Set<String> receivers = new HashSet<String>();
					for (int j = 6; j < messages[i].length; j++)
					{
						receivers.add(messages[i][j]);
					}
					
					MultiModeMessage msg = new MultiModeMessage();
					msg.size = Integer.parseInt(messages[i][3]);
					msg.period = Double.parseDouble(messages[i][4]);
					msg.name = messages[i][1];
					msg.deadline = Double.parseDouble(messages[i][4]);
					msg.sender = (Object)messages[i][0];
					msg.receivers = receivers;
					msg.config = Integer.parseInt(messages[i][2]);
					
					if(null == multiModeMessages.get(msg.sender))
					{
						multiModeMessages.put(msg.sender, new LinkedHashMap<Integer, TreeSet<MultiModeMessage>>());
					}
					if(null == multiModeMessages.get(msg.sender).get(msg.config) && msg.config != 0)
					{
						multiModeMessages.get(msg.sender).put(msg.config, new TreeSet<MultiModeMessage>(new MultiModeMsgComparator()));
					}
					
					if(0 == msg.config)
					{
						/* config=0 messages are equal for all configs */
						for (int k = 1; k <= numberOfConfigs; k++)
						{
							if(null == multiModeMessages.get(msg.sender).get(k))
							{
								multiModeMessages.get(msg.sender).put(k, new TreeSet<MultiModeMessage>(new MultiModeMsgComparator()));
							}
							multiModeMessages.get(msg.sender).get(k).add(msg);
						}
					}
					else
					{
						multiModeMessages.get(msg.sender).get(msg.config).add(msg);
					}
				}
				
				int cnt = 0;
				/* calculate for all ECUs separately */
				for (Iterator<Object> i = multiModeMessages.keySet().iterator(); i.hasNext();)
				{
					Object sender = (Object) i.next();
					
					Log.logLowln("reducing ECU "+sender, module);
					/* compare configs to receive message periods (all configs should be of equal length) */
					for (int j = 0; j < multiModeMessages.get(sender).get(1).size(); j++)
					{
						Double smallestPeriod = Double.MAX_VALUE;
						Integer biggestSize = 0;
						for (int k = 1; k <= numberOfConfigs; k++)
						{
							if(((MultiModeMessage)multiModeMessages.get(sender).get(k).toArray()[j]).period < smallestPeriod)
							{
								smallestPeriod = ((MultiModeMessage)multiModeMessages.get(sender).get(k).toArray()[j]).period;
							}
							if(((MultiModeMessage)multiModeMessages.get(sender).get(k).toArray()[j]).size > biggestSize)
							{
								biggestSize = ((MultiModeMessage)multiModeMessages.get(sender).get(k).toArray()[j]).size;
							}
						}
						
						FlexRayStaticMessage message;
						message = new FlexRayStaticMessage(
											biggestSize,
											smallestPeriod,
											smallestPeriod,
											"msg"+cnt,
											sender,
											new HashSet<String>(Arrays.asList(getEcus())));
						this.messages.add(message);
//						System.out.println("added msg "+message.getName()+" with size="+message.getSize()+" & deadline= "+message.getDeadline());
						cnt++;
					}
				}
			}
		}
	}
	
	private class MultiModeMessage
	{
		public Object sender;
		public Set<String> receivers;
		public Integer size;
		public Double period;
		public Double deadline;
		public String name;
		public Integer config;
	}
	
	public static class MultiModeMsgComparator implements Comparator<MultiModeMessage>
	{
		@Override
	    public int compare(MultiModeMessage x, MultiModeMessage y)
	    {
			if (x.deadline < y.deadline)
	        {
	            return -1;
	        }
	        else if (x.deadline > y.deadline)
	        {
	            return 1;
	        }
	        else
	        {
//	        	if (x.size > y.size)
//	        	{
//	        		return -1;
//	        	}
//	        	else if (x.size < y.size)
//	        	{
//	        		return 1;
//	        	}
//	        	else
//	        	{
	        		return x.name.compareTo(y.name);
//	        	}
	        }
	    }
	}
	
	/**
	 * This method prints all ECUs and all messages contained in the 
	 * system to the standard output.
	 */
	public void printList()
	{
		for (int i = 0; i < ecus.length; i++) 
		{
			Log.logTrace(ecus[i]+",", module);
		}
		Log.logTraceln(module);
		Log.logTraceln("sender,      name,           size,   period, deadline, receivers", module);
		for (FlexRayStaticMessage msg : messages)
		{
			Log.logTrace(msg.getSender() + ",\t" + msg.getName() + ",", module);
			if(((String)msg.getSender()).length() < 7)
			{
				Log.logTrace(" ", module);
			}
			if(((String)msg.getSender()).length() < 8)
			{
				Log.logTrace(" ", module);
			}
			Log.logTrace("\t" + msg.getSize() + ",\t" + msg.getPeriod() + ",\t" + msg.getDeadline() + ",\t", module);
			for (Iterator<String> i = msg.getReceivers().iterator(); i.hasNext();)
			{
				String receiver = (String) i.next();
				Log.logTrace(receiver+",", module);
			}
			Log.logTraceln(module);
		}
	}
}
