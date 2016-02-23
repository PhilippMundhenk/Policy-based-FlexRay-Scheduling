package visualization;

import java.util.Iterator;
import java.util.LinkedList;

import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticMessage;
import flexRay.FlexRayStaticFrame;
import SchedulingInterfaces.Schedule;

public class PolicyVisualizer extends Visualizer
{
	PolicySchedulePanel schedulePanel;
	
	public PolicyVisualizer()
	{
		super();
	}
	
	public void visualize(Schedule sched)
	{
		super.visualize(sched);
		schedulePanel = new PolicySchedulePanel((FlexRaySchedule)schedule, settings, detailedViewWrapper);
		super.changePanel(0, schedulePanel, "Schedule");
	}
	
	public void colorsChanged()
	{
		schedulePanel.colorsChanged();
	}
	
	@SuppressWarnings("unchecked")
	public void setSchedule(FlexRaySchedule sched)
	{
		super.setSchedule(sched);
		schedulePanel.setSlots((LinkedList<FlexRayStaticFrame>)((FlexRaySchedule)schedule).getAdditionalData());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getSelectedElement(Object identifier)
	{
		for (Iterator j = schedulePanel.getSlots().iterator(); j.hasNext();)
		{
			FlexRayStaticFrame slot = (FlexRayStaticFrame) j.next();
			
			for (Iterator i = slot.getMsgs().iterator(); i.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage) i.next();
				
				if(msg.getName().equals((String)identifier))
				{
					return msg;
				}
			}
		}
		return super.getSelectedElement(identifier);
	}
	
	@Override
	public Boolean isInCycle(int cycle, Object msg)
	{
		if(getScheduledSlot(cycle, msg) >= 0)
		{
			return true;
		}
		else
		{
			for (Iterator<FlexRayStaticFrame> i = schedulePanel.getSlots().iterator(); i.hasNext();)
			{
				FlexRayStaticFrame slot = (FlexRayStaticFrame) i.next();

				if(slot.getFrameNo()/schedule.getSlotsPerCycle() == cycle)
				{
					if(slot.containsDueMsg((FlexRayStaticMessage)msg))
					{
						return true;
					}
				}
			}
			
			for (Iterator<FlexRayStaticFrame> i = schedulePanel.getSlots().iterator(); i.hasNext();)
			{
				FlexRayStaticFrame slot = (FlexRayStaticFrame) i.next();
				
				if(slot.getFrameNo()/schedule.getSlotsPerCycle() == cycle)
				{
					if(slot.containsDeadlineMsg((FlexRayStaticMessage)msg))
					{
						return true;
					}
				}
			}
			
			return false;
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public int getDueSlot(int cycle, Object msg)
	{
		if(schedulePanel.getSelectedMsgType() == schedulePanel.SELECTED_WRAPPER)
		{
			return super.getDueSlot(cycle, msg);
		}
		else
		{
			for (Iterator<FlexRayStaticFrame> i = schedulePanel.getSlots().iterator(); i.hasNext();)
			{
				FlexRayStaticFrame slot = (FlexRayStaticFrame) i.next();

				if(slot.getFrameNo()/schedule.getSlotsPerCycle() == cycle)
				{
					if(slot.containsDueMsg((FlexRayStaticMessage)msg))
					{
						return slot.getFrameNo();
					}
				}
			}
			return -1;
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public int getScheduledSlot(int cycle, Object msg)
	{
		if(schedulePanel.getSelectedMsgType() == schedulePanel.SELECTED_WRAPPER)
		{
			return super.getScheduledSlot(cycle, msg);
		}
		else
		{
			for (Iterator<FlexRayStaticFrame> i = schedulePanel.getSlots().iterator(); i.hasNext();)
			{
				FlexRayStaticFrame slot = (FlexRayStaticFrame) i.next();
				
				if(slot.getFrameNo()/schedule.getSlotsPerCycle() == cycle)
				{
					for (Iterator<FlexRayStaticMessage> j = slot.getMsgs().iterator(); j.hasNext();)
					{
						FlexRayStaticMessage message = (FlexRayStaticMessage) j.next();
						
						if(message.getName().equals(((FlexRayStaticMessage)msg).getName()))
						{
							return slot.getFrameNo();
						}
					}
				}
			}
		}
		return -1;
	}

	@SuppressWarnings("static-access")
	@Override
	public int getDeadlineSlot(int cycle, Object msg)
	{
		if(schedulePanel.getSelectedMsgType() == schedulePanel.SELECTED_WRAPPER)
		{
			return super.getDeadlineSlot(cycle, msg);
		}
		else
		{
			for (Iterator<FlexRayStaticFrame> i = schedulePanel.getSlots().iterator(); i.hasNext();)
			{
				FlexRayStaticFrame slot = (FlexRayStaticFrame) i.next();
				
				if(slot.getFrameNo()/schedule.getSlotsPerCycle() == cycle)
				{
					if(slot.containsDeadlineMsg((FlexRayStaticMessage)msg))
					{
						return slot.getFrameNo();
					}
				}
			}
			return -1;
		}
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Boolean isDueWaiting(int cycle, Object msg)
	{
		if(schedulePanel.getSelectedMsgType() == schedulePanel.SELECTED_WRAPPER)
		{
			return super.isDueWaiting(cycle, msg);
		}
		else
		{
			int dueCycle = cycle;
			while(getDueSlot(dueCycle, msg) == -1)
			{
				if(getScheduledSlot(dueCycle, msg) != -1)
				{
					return false;
				}
				dueCycle--;
			}
			int scheduledCycle = cycle;
			while(getScheduledSlot(scheduledCycle, msg) == -1)
			{
				scheduledCycle++;
				if(scheduledCycle == schedule.getNumberOfCycles())
				{
					return false;
				}
			}
			if(dueCycle < cycle && cycle < scheduledCycle)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}



	@SuppressWarnings("static-access")
	@Override
	public Boolean isDeadlineWaiting(int cycle, Object msg)
	{
		if(schedulePanel.getSelectedMsgType() == schedulePanel.SELECTED_WRAPPER)
		{
			return super.isDeadlineWaiting(cycle, msg);
		}
		else
		{
			int scheduledCycle = cycle;
			while(getScheduledSlot(scheduledCycle, msg) == -1)
			{
				if(getDeadlineSlot(scheduledCycle, msg) >= 0)
				{
					return false;
				}
				scheduledCycle--;
				if(scheduledCycle == -1)
				{
					return false;
				}
			}
			int deadlineCycle = cycle;
			while(getDeadlineSlot(deadlineCycle, msg) == -1)
			{
				deadlineCycle++;
				if(deadlineCycle == schedule.getNumberOfCycles())
				{
					return false;
				}
			}
			if(deadlineCycle > cycle && cycle > scheduledCycle)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

}
