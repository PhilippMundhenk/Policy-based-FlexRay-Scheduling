package simulator.components.distributed.ecuModules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import simulator.components.central.bus.Bus;
import simulator.components.distributed.ECU;
import support.SimLog;
import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;

public class Com implements EcuModule
{
	protected String module = "Com_";
	protected static final String MODULE_TYPE = "Com";
	protected Bus bus;
	protected Schedule schedule;
	protected PriorityQueue<OutgoingMessage> outgoingMsgs;
	protected ECU ecu;
	
	@SuppressWarnings("rawtypes")
	public Com(Bus bus, Schedule schedule, Collection msgs, ECU ecu)
	{
		this.bus = bus;
		this.schedule = schedule;
		this.ecu = ecu;
		module += ecu.getName();
		this.outgoingMsgs = new PriorityQueue<OutgoingMessage>(msgs.size(), new DeadlineComparator());
	}

	public static String getModuleType()
	{
		return MODULE_TYPE;
	}
	
	@Override
	public String getType()
	{
		return MODULE_TYPE;
	}

	public void sendMsg(@SuppressWarnings("rawtypes") Message msg)
	{
		SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] saved for sending", module);
		OutgoingMessage outMsg = new OutgoingMessage();
		outMsg.msg = msg;
		outMsg.sendingTime = ecu.getClk().getTimeMillisec();
		outgoingMsgs.add(outMsg);
	}

	Boolean freezeMsgs = false;
	int frozenSlot = -1;
	int frozenCycle = -1;
	char outBuf[];
	@SuppressWarnings("rawtypes")
	Collection<Message> frozenMsgs;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute()
	{
		int currentCycle = (int)(ecu.getClk().getTimeMillisec()/schedule.getCycleDuration());
		int currentSlot = (int)((ecu.getClk().getTimeMillisec() - currentCycle*schedule.getCycleDuration())/schedule.getSlotDuration());
		currentCycle %= schedule.getNumberOfCycles();
		
		if(freezeMsgs && currentSlot != frozenSlot)
		{
			/* switch to new frame occurred, send prepared messages */
			SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) cycle: " + currentCycle + " slot: "+currentSlot, module);
			SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) sending messages", module);
			bus.sendMsg(outBuf, frozenMsgs);
			freezeMsgs = false;
			frozenSlot = currentSlot;
			frozenCycle = currentCycle;
			return;
		}
		
		if(currentSlot >= schedule.getSlotsPerCycle())
		{
			SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) outside static segment, nothing to do", module);
		}		
		else
		{
			SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) cycle: " + currentCycle + " slot: "+currentSlot, module);
			LinkedList<Message> msgsInCurrentSlot = new LinkedList<Message>(schedule.getMessages(currentCycle, currentSlot));
			if(!(msgsInCurrentSlot.size() == 0))
			{
				if((msgsInCurrentSlot.get(0) != null) && !freezeMsgs)
				{
					if(!((String)msgsInCurrentSlot.get(0).getName()).startsWith("imported_"))
					{
						if(msgsInCurrentSlot.get(0).getSender().equals(ecu.getName()))
						{
							/* current slot is for this ECU */
							if(outgoingMsgs.size() == 0)
							{
								SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) no messages waiting, skipping frame", module);
								return;
							}
							SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) freezing messages for this frame", module);
							SimLog.logTrace("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) freezing msgs: ", module);
							PriorityQueue<OutgoingMessage> tempList = new PriorityQueue<OutgoingMessage>(outgoingMsgs.size(), new DeadlineComparator());
							outBuf = new char[schedule.getSlotSize()];
							for (int i = 0; i < outBuf.length; i++)
							{
								outBuf[i] = 0;
							}
							frozenMsgs = new ArrayList<Message>();
							int outBufCursor = 0;
							while(!outgoingMsgs.isEmpty())
							{
									if(outgoingMsgs.peek().msg.getSize()+3 <= schedule.getSlotSize()-outBufCursor)
									{
										/* enough space left to put next message with header */
										Message outMsg = outgoingMsgs.poll().msg;
										SimLog.logTrace(outMsg.getName() + ", ", module);
										int size = outMsg.getSize()+3;
										frozenMsgs.add(outMsg);
										for (int i = 0; i < size; i++)
										{
											outBuf[outBufCursor] = 1; //TODO: use data that makes more sense, especially for header
											outBufCursor++;
										}
									}
									else
									{
										/* not enough space for this message, keep in queue */
										tempList.add(outgoingMsgs.poll());
									}
							}
							SimLog.logTraceln(module);
							freezeMsgs = true;
							frozenSlot = currentSlot;
							frozenCycle = currentCycle;
							outgoingMsgs = tempList;
							SimLog.logTrace("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) remaining messages: ", module);
							if(outgoingMsgs.isEmpty())
							{
								SimLog.logTrace("none", module);
							}
							else
							{
								for (OutgoingMessage message : outgoingMsgs)
								{
									SimLog.logTrace(message.msg.getName() + ", ", module);
								}
							}
							SimLog.logTraceln(module);
						}
						else
						{
							SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) frame is in use by "+ msgsInCurrentSlot.get(0).getSender() +", skipping frame", module);
						}
					}
					else
					{
						SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) frame is reserved for static messages, skipping frame", module);
					}
				}
				else
				{
					SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) waiting until end of frame to send messages", module);
				}
			}
			else
			{
				SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) frame not assigned, skipping frame", module);
			}
		}
	}
	
	public class DeadlineComparator implements Comparator<OutgoingMessage>
	{
		@Override
	    public int compare(OutgoingMessage x, OutgoingMessage y)
	    {
			double x_remainingDeadline = ((x.sendingTime + x.msg.getDeadline()) - ecu.getClk().getTimeMillisec());
			double y_remainingDeadline = ((y.sendingTime + y.msg.getDeadline()) - ecu.getClk().getTimeMillisec());
	        if (x_remainingDeadline < y_remainingDeadline)
	        {
	            return -1;
	        }
	        else if (x_remainingDeadline > y_remainingDeadline)
	        {
	            return 1;
	        }
	        else
	        {
	        	if (x.msg.getSize() > y.msg.getSize())
	        	{
	        		return -1;
	        	}
	        	else if (x.msg.getSize() < y.msg.getSize())
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
	
	public class OutgoingMessage
	{
		@SuppressWarnings("rawtypes")
		public Message msg;
		public double sendingTime;
	}
}
