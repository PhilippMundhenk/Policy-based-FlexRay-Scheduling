package flexRay;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;

import support.Log;

/**
 * This class contains all necessary functions to read in csv formatted *.txt files containing FlexRay messages. 
 * The first line of this file has to list all ECUs being part of the system, either as sender or as receiver. 
 * The other lines contain one FlexRay message each in the following format:
 * sender, name, size, period, (receiver, )+
 * One message can contain one or multiple receivers, seperated by comma.
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 */
public class FlexRayFileInput 
{
	public enum FileFormat {WITH_DEADLINE, MULTI_MODE};
	public enum ReductionMode {NONE, CONVENTIONAL, POLICY}
	private static final String module = "FlexRayFileInput";
	
	public static FlexRayStaticMessageCollection ReadFile(String filename) throws IOException
	{
		return ReadFile(filename, FileFormat.WITH_DEADLINE, ReductionMode.NONE);
	}
	
	/**
	 * This method reads in a given file, processes it and enters all messages and participating ECUs into one 
	 * new object of type FlexRayStaticMessageCollection. This object is returned.
	 * 
	 * @param filename
	 * name of the file to load
	 * @param fileformat
	 * format of the file to read
	 * @param reductionMode
	 * in case of multi-mode messages, this parameter descirbes how to reduce the messages
	 * 
	 * @return
	 * collection of all messages contained in the given file
	 * 
	 * @throws IOException
	 * exception for file handling
	 */
	public static FlexRayStaticMessageCollection ReadFile(String filename, FileFormat fileFormat, ReductionMode reductionMode) throws IOException
	{
		String[] allEcu = new String[1];
		String[][] allMessages;
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(filename));
		lnr.skip(Long.MAX_VALUE);
		
		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String str;
		int cnt = -1;
		
		if(fileFormat.equals(FileFormat.WITH_DEADLINE))
		{
			allMessages = new String[lnr.getLineNumber()][];
			while ((str = br.readLine()) != null)
			{
				cnt++;
				if(cnt == 0)
				{
					allEcu = str.split(",");
				}
				else
				{
					allMessages[cnt-1] = str.split(",");
				}
			}
			in.close();
			lnr.close();
			
			FlexRayStaticMessageCollection messages = new FlexRayStaticMessageCollection();
			messages.setEcus(allEcu);
			messages.setMessages(allMessages, fileFormat, reductionMode, 0);
			
			return messages;
		}
		else if(fileFormat.equals(FileFormat.MULTI_MODE))
		{
			allMessages = new String[lnr.getLineNumber()-1][];
			Integer numberOfConfigs = 0;
			while ((str = br.readLine()) != null)
			{
				cnt++;
				if(cnt == 0)
				{
					numberOfConfigs = Integer.parseInt(str);
				}
				else if(cnt == 1)
				{
					allEcu = str.split(",");
				}
				else
				{
					allMessages[cnt-2] = str.split(",");
				}
			}
			in.close();
			lnr.close();
			
			FlexRayStaticMessageCollection messages = new FlexRayStaticMessageCollection();
			messages.setEcus(allEcu);
			Log.logLowln("number of read messages from file = "+allMessages.length, module);
			messages.setMessages(allMessages, fileFormat, reductionMode, numberOfConfigs);
			Log.logLowln("collapsed (multi-mode messages) to = "+messages.getMessages().size(), module);
			
			Log.logLowln("reduced messages:", module);
			for (Iterator<FlexRayStaticMessage> it = messages.getMessages().iterator(); it.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage) it.next();
				
				Log.logLowln(msg.getName() + ": period: "+msg.getPeriod()+" size: "+msg.getSize(), module);
			}
			
			return messages;
		}
		else
		{
			in.close();
			lnr.close();
			return null;
		}
	}
}
