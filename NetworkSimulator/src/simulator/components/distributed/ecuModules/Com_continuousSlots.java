package simulator.components.distributed.ecuModules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;

import simulator.components.central.bus.Bus;
import simulator.components.distributed.ECU;
import support.SimLog;
import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;

public class Com_continuousSlots extends Com implements EcuModule
{
	public Com_continuousSlots(Bus bus, Schedule schedule, @SuppressWarnings("rawtypes") Collection msgs, ECU ecu)
	{
		super(bus, schedule, msgs, ecu);
	}

	Boolean freezeMsgs = false;
	int frozenSlot = -1;
	int frozenCycle = -1;
	char outBuf[];
	int remainingLength = 0;
	OutgoingMessage remainingMsg = null;
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
			SimLog.logMediumln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) cycle: " + currentCycle + " slot: "+currentSlot, module);
			SimLog.logMediumln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) sending messages", module);
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
			SimLog.logMediumln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) cycle: " + currentCycle + " slot: "+currentSlot, module);
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
							if(outgoingMsgs.size() == 0 && remainingMsg == null)
							{
								SimLog.logTraceln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) no messages waiting, skipping frame", module);
								return;
							}
							SimLog.logMediumln("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) freezing messages for this frame", module);
							SimLog.logMedium("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) freezing msgs: ", module);
							
							outBuf = new char[schedule.getSlotSize()];
							for (int i = 0; i < outBuf.length; i++)
							{
								outBuf[i] = 0;
							}
							frozenMsgs = new ArrayList<Message>();
							int outBufCursor = 0;
							
							if((remainingMsg != null) && (outBufCursor < schedule.getSlotSize()))
							{
								/* take rest of message from last frame */
								Message outMsg = remainingMsg.msg;
								SimLog.logMedium(outMsg.getName() + ", ", module);
								int size = remainingLength;
								remainingLength = 0;
								remainingMsg = null;
								frozenMsgs.add(outMsg);
								for (int i = 0; i < size; i++)
								{
									outBuf[outBufCursor] = 1; //TODO: use data that makes more sense, especially for header
									outBufCursor++;
								}
							}
							
							PriorityQueue<OutgoingMessage> tempList = null;
							if(!outgoingMsgs.isEmpty())
							{
								tempList = new PriorityQueue<OutgoingMessage>(outgoingMsgs.size(), new DeadlineComparator());
							}
							while(!outgoingMsgs.isEmpty())
							{
									if(outgoingMsgs.peek().msg.getSize()+3 <= schedule.getSlotSize()-outBufCursor)
									{
										/* enough space left to put next message with header */
										Message outMsg = outgoingMsgs.poll().msg;
										SimLog.logMedium(outMsg.getName() + ", ", module);
										int size = 0;
										if(remainingLength != 0)
										{
											size = remainingLength;
											remainingLength = 0;
											remainingMsg = null;
										}
										else
										{
											size = outMsg.getSize()+3;
										}
										frozenMsgs.add(outMsg);
										for (int i = 0; i < size; i++)
										{
											outBuf[outBufCursor] = 1; //TODO: use data that makes more sense, especially for header
											outBufCursor++;
										}
									}
									else if(outBufCursor < schedule.getSlotSize())
									{
										/* not enough space for full message, put only start of message */
										remainingLength = outgoingMsgs.peek().msg.getSize()+3 - (schedule.getSlotSize()-outBufCursor);
										SimLog.logMedium("first " + (schedule.getSlotSize()-outBufCursor) + " bytes of " + outgoingMsgs.peek().msg.getName(), module);
										while(outBufCursor < schedule.getSlotSize())
										{
											outBuf[outBufCursor] = 1; //TODO: use data that makes more sense, especially for header
											outBufCursor++;
										}
										remainingMsg = outgoingMsgs.poll();
									}
									else
									{
										/* not enough space for this message, keep in queue */
										tempList.add(outgoingMsgs.poll());
									}
							}
							SimLog.logMediumln(module);
							freezeMsgs = true;
							frozenSlot = currentSlot;
							frozenCycle = currentCycle;
							if(!(tempList == null))
							{
								outgoingMsgs = tempList;
							}
							SimLog.logMedium("[SimTime: " + ecu.getClk().getTimeMicrosec() + "탎] (execute) remaining messages: ", module);
							if(!(remainingMsg == null))
							{
								SimLog.logMedium(remainingMsg.msg.getName() + ", ", module);
							}
							if(outgoingMsgs.isEmpty())
							{
								SimLog.logMedium("none", module);
							}
							else
							{
								for (OutgoingMessage message : outgoingMsgs)
								{
									SimLog.logMedium(message.msg.getName() + ", ", module);
								}
							}
							SimLog.logMediumln(module);
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
}
