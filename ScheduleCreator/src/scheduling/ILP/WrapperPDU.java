package scheduling.ILP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import support.Log;
import tests.ScheduleCreatorConfig;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticFrame;
import flexRay.FlexRayStaticMessage;


public class WrapperPDU {
	
	/**
	 * This method generates the wrapper PDUs from the slots.
	 * 
	 * @param frames
	 * list of slots with scheduled messages
	 * @param schedule
	 * schedule to write data to 
	 */
	public void constructWrapperPDUs21(LinkedList<FlexRayStaticFrame> frames, FlexRaySchedule schedule, HashMap<String, Collection<String>> receivers, int wrapperLenByte)
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
		
		schedule.setAdditionalData(frames);
	}
	
	
	public void constructWrapperPDUs30(LinkedList<FlexRayStaticFrame> frames,
			FlexRaySchedule schedule,
			HashMap<String, Collection<String>> receivers, int slotSize) {

		System.out.println("frames.size() = "+frames.size());
		
		for (Iterator<FlexRayStaticFrame> i = frames.iterator(); i.hasNext();) {
			FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
			if (frame.getEcu() != null && !frame.getEcu().equals("")) {
//				System.out.println(frame.getFrameNo()+" for "+frame.getEcu());
			}
		}
		
		/* generate possible periods for wrapper-PDU repetition */
		ArrayList<Integer> periods = new ArrayList<Integer>();
		for (int i = 1; i <= schedule.getNumberOfCycles(); i++)
		{
			if ((schedule.getNumberOfCycles() % i) == 0)
			{
				periods.add(i);
			}
		}
		
		Collection<String> ecus = new HashSet<String>();
		for (Iterator<FlexRayStaticFrame> i = frames.iterator(); i.hasNext();) {
			FlexRayStaticFrame frame = (FlexRayStaticFrame) i.next();
			
			if(!ecus.contains(frame.getEcu()))
			{
				ecus.add(frame.getEcu());
			}
		}
		HashMap<String, Collection<String>> allReceivers = new HashMap<String, Collection<String>>();
		for (Iterator<String> i = ecus.iterator(); i.hasNext();)
		{
			String string = (String) i.next();
			
			allReceivers.put(string, ecus);
		}
		
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
		
		schedule.setAdditionalData(frames);
	}
			
	static Integer numberOfWrappersCreated = 0;
		
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
	private static LinkedList<FlexRayStaticFrame> constructWrapper(int frame, int baseCycle, int repetition, LinkedList<FlexRayStaticFrame> frames, FlexRaySchedule schedule, Collection<String> receivers, int wrapperLenByte)
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
}
