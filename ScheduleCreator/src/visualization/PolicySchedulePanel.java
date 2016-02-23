package visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticMessage;
import flexRay.FlexRayStaticFrame;

public class PolicySchedulePanel extends SchedulePanel
{
	private static final long serialVersionUID = 1L;
	public static final int SELECTED_WRAPPER = 1;
	public static final int SELECTED_MSGS = 2;
	private MsgListSelectionHandler msgListSelectionHandler = new MsgListSelectionHandler();
	
	private JList messageList;
	protected Boolean wrapperListSelected = false;
	private Boolean initialized = false;
	
	protected LinkedList<FlexRayStaticFrame> slots;
	
	@SuppressWarnings("unchecked")
	public PolicySchedulePanel(FlexRaySchedule sched, SettingsPanel settings, DetailedViewWrapper detailedViewWrapper)
	{
		super(sched, settings, detailedViewWrapper);
		slots = (LinkedList<FlexRayStaticFrame>)((FlexRaySchedule)schedule).getAdditionalData();
	}
	
	public LinkedList<FlexRayStaticFrame> getSlots()
	{
		return slots;
	}

	public void setSlots(LinkedList<FlexRayStaticFrame> slots)
	{
		this.slots = slots;
	}
	
	public int getSelectedMsgType()
	{
		if(wrapperListSelected)
		{
			return SELECTED_WRAPPER;
		}
		else
		{
			return SELECTED_MSGS;
		}
	}

	class ListSelectionHandler implements ListSelectionListener 
	{
		public void valueChanged(ListSelectionEvent e) 
		{ 
			if (e.getValueIsAdjusting() == false) 
			{
				JList list = (JList)e.getSource();
				
				// Get all selected items
//				Object[] selected = list.getSelectedValuesList().toArray(); //Java 7
				Object[] selected = list.getSelectedValues(); //Java 6
				selectedListIndices = list.getSelectedIndices();
				frameListSelected = false;
				if(selectedListIndices.length == 0)
				{
					wrapperListSelected = false;
				}
				else
				{
					wrapperListSelected = true;
				}
				
				for (int i=0; i<slotColors.length; i++) 
				{
					for (int j = 0; j < slotColors[i].length; j++)
					{
						slotColors[i][j] = new Color(255, 255, 255); 
					}
				}
				
				selectedElements = new String[selected.length];
				detailedViewWrapper.setSelectedElements(selectedElements);
				
				msgViewer.setText("");
				
				// Iterate all selected items
				for (int i=0; i<selected.length; i++) 
				{
					String sel = (String)selected[i];
					
					@SuppressWarnings("rawtypes")
					Message selMsg = null;
					
					selectedElements[i] = sel;
					
					for (@SuppressWarnings("rawtypes") Iterator j = schedule.getMessages().iterator(); j.hasNext();)
					{
						@SuppressWarnings({ "rawtypes" })
						Message msg = (Message) j.next();
						
						if(msg.getName().equalsIgnoreCase(sel))
						{
							markSlot(msg);
							selMsg = msg;
							break;
						}
					}
					if(!(selMsg == null))
					{
						msgViewer.append(sel+"\n");
						msgViewer.append("Size: "+selMsg.getSize()+"\n");
						msgViewer.append("Period: "+selMsg.getPeriod()+"\n");
						msgViewer.append("Deadline: "+selMsg.getDeadline()+"\n");
						msgViewer.append("Sender: "+((String)selMsg.getSender())+"\n");
						msgViewer.append("\n");
					}
					infoPane.setSelectedIndex(infoPane.indexOfTab("Messages"));
					
					messageList.clearSelection();
					scheduleTable.repaint();
				}
			}
		}
	}
	
	class MsgListSelectionHandler implements ListSelectionListener 
	{
		public void valueChanged(ListSelectionEvent e) 
		{ 
			if (e.getValueIsAdjusting() == false) 
			{
				JList list = (JList)e.getSource();
				
//				Object[] selected = list.getSelectedValuesList().toArray();  //Java 7
				Object[] selected = list.getSelectedValues();  //Java 6
				selectedListIndices = list.getSelectedIndices();
				if(selectedListIndices.length == 0)
				{
					frameListSelected = false;
				}
				else
				{
					frameListSelected = true;
				}
				wrapperListSelected = false;
				
				selectedElements = new String[selected.length];
				detailedViewWrapper.setSelectedElements(selectedElements);
				
				for (int j=0; j<slotColors.length; j++) 
				{
					for (int m = 0; m<slotColors[j].length; m++)
					{
						slotColors[j][m] = new Color(255, 255, 255); 
					}
				}
				
				msgViewer.setText("");
				
				for (int i=0; i<selected.length; i++) 
				{
					String sel = (String)selected[i];
					
					selectedElements[i] = sel;
					
					FlexRayStaticMessage message = null;
					
					for (Iterator<FlexRayStaticFrame> j = slots.iterator(); j.hasNext();)
					{
						FlexRayStaticFrame slot = (FlexRayStaticFrame) j.next();
						
						if(slot.containsMessage(sel))
						{
							/* mark slot in correct color */
							/* only ECU mode supported, other modes dont make sense */
							slotColors[slot.getFrameNo()/schedule.getSlotsPerCycle()][slot.getFrameNo()%schedule.getSlotsPerCycle()] = EcuColors.get(slot.getEcu());
						}
						for (Iterator<FlexRayStaticMessage> m = slot.getMsgs().iterator(); m.hasNext();)
						{
							FlexRayStaticMessage msg = (FlexRayStaticMessage) m.next();
							
							if(msg.getName().equals(sel))
							{
								message = msg;
							}
						}
					}
					msgViewer.append(sel+"\n");
					msgViewer.append("Size: "+message.getSize()+"\n");
					msgViewer.append("Period: "+message.getPeriod()+"\n");
					msgViewer.append("Deadline: "+message.getDeadline()+"\n");
					msgViewer.append("Sender: "+((String)message.getSender())+"\n");
					msgViewer.append("Receivers: \n");
					for (Iterator<String> j = message.getReceivers().iterator(); j.hasNext();)
					{
						String rcver = (String) j.next();
						
						msgViewer.append(rcver+"\n");
					}
					msgViewer.append("\n");
					
					if(settings.getAutoSwitchPane())
					{
						infoPane.setSelectedIndex(infoPane.indexOfTab("Messages"));
					}
					
					wrapperList.clearSelection();
					scheduleTable.repaint();
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void paint(Graphics g)
	{
		super.paint(g);
		
		if(!initialized)
		{
			LinkedHashSet<Message> msgs = new LinkedHashSet<Message>();
		
			for (Iterator i = ((LinkedList<FlexRayStaticFrame>)((FlexRaySchedule)schedule).getAdditionalData()).iterator(); i.hasNext();)
			{
				FlexRayStaticFrame slot = (FlexRayStaticFrame) i.next();
				
				for (Iterator j = slot.getMsgs().iterator(); j.hasNext();)
				{
					Message msg = (Message) j.next();
				
					msgs.add(msg);
				}
			}
		
			LinkedList<Message> msgList = new LinkedList<Message>(msgs);
			Collections.sort(msgList, new MessageNameComparator());

			String[] list = new String[msgList.size()];
			int cnt = 0;
			for (Iterator i = msgList.iterator(); i.hasNext();)
			{
				Message message = (Message) i.next();
				
				list[cnt] = message.getName();
				cnt++;
			}

			messageList = new JList(list);
			messageList.setLayoutOrientation(JList.VERTICAL);
			messageList.setVisibleRowCount(-1);
			messageList.addListSelectionListener(msgListSelectionHandler);
		
			JScrollPane scrollList = new JScrollPane(messageList);
			scrollList.getVerticalScrollBar().setUnitIncrement(10);
			intermediatePanel.add(scrollList);
		}
		
		if(!initialized)
		{
			wrapperList.removeListSelectionListener(listSelectionHandler);
			wrapperList.addListSelectionListener(new ListSelectionHandler());
		}
		initialized = true;
	}
	
	public void visualize(Schedule sched)
	{
		visualize((FlexRaySchedule)sched);
	}
	
	public void colorsChanged()
	{
		messageList.clearSelection();
		wrapperList.clearSelection();
	}	
	
	public void mouseMoved(MouseEvent e)
	{
		super.getScheduleTable().mouseMoved(e);
		
		int x = e.getX();
		int y = e.getY();
		
		@SuppressWarnings("unchecked")
		LinkedList<FlexRayStaticFrame> slots = (LinkedList<FlexRayStaticFrame>)((FlexRaySchedule)schedule).getAdditionalData();
		
		SchedulePanel.ScheduleTablePanel.Slot selectedSlot = super.getScheduleTable().getSlot(x, y);
		
		for (Iterator<FlexRayStaticFrame> i = slots.iterator(); i.hasNext();)
		{
			FlexRayStaticFrame slot = (FlexRayStaticFrame) i.next();
			
			if(slot.getFrameNo() == selectedSlot.cycle*schedule.getSlotsPerCycle()+selectedSlot.slotInCycle)
			{
				for (Iterator<FlexRayStaticMessage> j = slot.getMsgs().iterator(); j.hasNext();)
				{
					FlexRayStaticMessage msg = (FlexRayStaticMessage) j.next();
					
					slotViewer.append(msg.getName()+ "(" + new DecimalFormat("#").format(msg.getPeriod()) + "," + new DecimalFormat("#").format(msg.getDeadline())+ ")" +"\n");
				}
			}
		}
	}
	
	
}
