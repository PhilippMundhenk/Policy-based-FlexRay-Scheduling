package simulator.components.central;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import support.SimLog;
import SchedulingInterfaces.Message;

public class Statistics
{
	private static final String module = "Stat";
	private int time = 0;
	private static final String SND_VALUES = "SND_VALUES";
	private static final String RCV_VALUES = "RCV_VALUES";
	@SuppressWarnings("rawtypes")
	private HashMap<Message, HashMap<String, Collection>> timings = new HashMap<Message, HashMap<String, Collection>>();
	
	public void tick(int tickLenMicrosec)
	{
		time += tickLenMicrosec;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void sendMsg_ECU(Message msg)
	{
		//TODO: check missing RCV
		if(!timings.containsKey(msg))
		{
			HashMap<String, Collection> valueMap = new HashMap<String, Collection>();
			Collection snd_values = new ArrayList<Integer>();
			Collection rcv_values = new ArrayList<Integer>();
			valueMap.put(SND_VALUES, snd_values);
			valueMap.put(RCV_VALUES, rcv_values);
			timings.put(msg, valueMap);
		}
		((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).add(time);
	}
	
	@SuppressWarnings("unchecked")
	public void receiveMsg_bus(@SuppressWarnings("rawtypes") Message msg)
	{
		((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).add(time);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int getDelayAvg()
	{
		int sum = 0;
		int cnt = 0;
		for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry timingEntry = (Map.Entry)i.next();
			Message msg = ((Message)timingEntry.getKey());
		
			int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
			for (int j = 0; j < size; j++)
			{
				sum = sum + (((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(j) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(j));
				cnt++;
			}
		}
		
		if(cnt == 0)
		{
			cnt = 1;
		}
		
		return sum/cnt;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int getDelayMin()
	{
		int min = time;
		for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry timingEntry = (Map.Entry)i.next();
			Message msg = ((Message)timingEntry.getKey());
			
			int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
			for (int j = 0; j < size; j++)
			{
				int delay = ((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(j) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(j);
				if(delay < min)
				{
					min = delay;
				}
			}
		}
		
		if(min == time)
		{
			min = 0;
		}
		
		return min;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int getDelayMax()
	{
		int max = 0;
		for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry timingEntry = (Map.Entry)i.next();
			Message msg = ((Message)timingEntry.getKey());
			
			int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
			for (int j = 0; j < size; j++)
			{
				int delay = ((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(j) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(j);
				if(delay > max)
				{
					max = delay;
				}
			}
		}
		
		return max;
	}
	
	@SuppressWarnings("unchecked")
	public int getDelayAvg(@SuppressWarnings("rawtypes") Message msg)
	{
		int sum = 0;
		int cnt = 0;
		int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
		for (int i = 0; i < size; i++)
		{
			sum = sum + (((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(i) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(i));
			cnt++;
		}
		
		if(cnt == 0)
		{
			cnt = 1;
		}
		
		return sum/cnt;
	}
	
	@SuppressWarnings("unchecked")
	public int getDelayMin(@SuppressWarnings("rawtypes") Message msg)
	{
		int min = time;
		int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
		for (int i = 0; i < size; i++)
		{
			int delay = ((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(i) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(i);
			if(delay < min)
			{
				min = delay;
			}
		}
		
		if(min == time)
		{
			min = 0;
		}
		
		return min;
	}
	
	@SuppressWarnings("unchecked")
	public int getDelayMax(@SuppressWarnings("rawtypes") Message msg)
	{
		int max = 0;
		int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
		for (int i = 0; i < size; i++)
		{
			int delay = ((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(i) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(i);
			if(delay > max)
			{
				max = delay;
			}
		}
		
		return max;
	}	
	
	@SuppressWarnings("rawtypes")
	public void printStatistics()
	{
		SimLog.logLowln("\tStatistics:", module);
		for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry timingEntry = (Map.Entry)i.next();
			Message msg = ((Message)timingEntry.getKey());
		
			SimLog.logLow(msg.getName()+",   \t", module);
			SimLog.logLow("delays (µs): min: \t" + getDelayMin(msg) + " \t avg: \t" + getDelayAvg(msg) + " \t max: \t" + getDelayMax(msg), module);
			SimLog.logLowln(", requirements (ms): deadline: " + msg.getDeadline() + " ("+String.format("%.3g", getDelayMax(msg)/(msg.getDeadline()*1000/100))+"%)" + "   \t period: " + msg.getPeriod() + ", \t" + (String)msg.getSender(), module);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void matlabExport(String fileName, Boolean generateDiagrams)
	{
		//TODO: sort into matrices, need to fill up with zeros, Matlab doesnt to automatically.
		//TODO: correct diagram output
		try
		{
			File file = new File(fileName);
			Writer output = new BufferedWriter(new FileWriter(file));
			output.write("numberOfMessages = "+timings.size()+";\n");
			
			output.write("messages = {");
			for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry timingEntry = (Map.Entry)i.next();
				Message msg = ((Message)timingEntry.getKey());
				
				output.write("'"+msg.getName()+"'");
				if(i.hasNext())
				{
					output.write(",");
				}
			}
			output.write("};\n");
			
			output.write("deadlines_millisec = [");
			for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry timingEntry = (Map.Entry)i.next();
				Message msg = ((Message)timingEntry.getKey());
				
				output.write(String.valueOf(msg.getDeadline()));
				if(i.hasNext())
				{
					output.write(",");
				}
			}
			output.write("];\n");
			
			output.write("periods_millisec = [");
			for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry timingEntry = (Map.Entry)i.next();
				Message msg = ((Message)timingEntry.getKey());
				
				output.write(String.valueOf(msg.getPeriod()));
				if(i.hasNext())
				{
					output.write(",");
				}
			}
			output.write("];\n");
			
			output.write("rcvTimes_microsec = [");
			for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry timingEntry = (Map.Entry)i.next();
				Message msg = ((Message)timingEntry.getKey());
				
				output.write("[");
				for (Iterator j = ((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).iterator(); j.hasNext();)
				{
					Integer rcvTime = (Integer) j.next();
					output.write(String.valueOf(rcvTime));
					if(j.hasNext())
					{
						output.write(",");
					}
				}
				output.write("]");
				if(i.hasNext())
				{
					output.write(";");
				}
			}
			output.write("];\n");
			
			output.write("sndTimes_microsec = [");
			for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry timingEntry = (Map.Entry)i.next();
				Message msg = ((Message)timingEntry.getKey());
				
				output.write("[");
				for (Iterator j = ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).iterator(); j.hasNext();)
				{
					Integer sndTime = (Integer) j.next();
					output.write(String.valueOf(sndTime));
					if(j.hasNext())
					{
						output.write(",");
					}
				}
				output.write("]");
				if(i.hasNext())
				{
					output.write(";");
				}
			}
			output.write("];\n");
			
			output.write("delays_microsec = [");
			for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry timingEntry = (Map.Entry)i.next();
				Message msg = ((Message)timingEntry.getKey());
				
				output.write("[");
				int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
				for (int j = 0; j < size; j++)
				{
					int delay = ((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(j) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(j);
					output.write(String.valueOf(delay));
					if(j!=size-1)
					{
						output.write(",");
					}
				}
				output.write("]");
				if(i.hasNext())
				{
					output.write(";");
				}
			}
			output.write("];\n");
			
			output.write("delays_percent = [");
			for (Iterator i = timings.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry timingEntry = (Map.Entry)i.next();
				Message msg = ((Message)timingEntry.getKey());
				
				output.write("[");
				int size = Math.min(((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).size(), ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).size());
				for (int j = 0; j < size; j++)
				{
					int delay = ((ArrayList<Integer>)timings.get(msg).get(RCV_VALUES)).get(j) - ((ArrayList<Integer>)timings.get(msg).get(SND_VALUES)).get(j);
					output.write(String.valueOf(delay/(msg.getDeadline()*1000/100)));
					if(j!=size-1)
					{
						output.write(",");
					}
				}
				output.write("]");
				if(i.hasNext())
				{
					output.write(";");
				}
			}
			output.write("];\n");
			
			if(generateDiagrams)
			{
				output.write("edges = 0:10:100;\n");
				for (int i = 1; i <= timings.size(); i++)
				{
					output.write("figure; title(['Histogram of delay percentages for ', messages("+i+")]); histc(delays_percent("+ i +"), edges);\n");
				}
			}
			
			output.close();
			SimLog.logLowln("exported to file "+fileName, module);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
