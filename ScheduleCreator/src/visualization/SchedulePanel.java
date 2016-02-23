package visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;

public class SchedulePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	protected JPanel right;
	protected JPanel intermediatePanel;
	
	protected String[] selectedElements;
	
	protected JTextArea slotViewer;
	protected JTextArea msgViewer;
	protected JTabbedPane infoPane;
	
	protected DetailedViewWrapper detailedViewWrapper;
	
	private Boolean initialized = false;
	
	protected JPanel scheduleTable;
	
	public ScheduleTablePanel getScheduleTable()
	{
		return (ScheduleTablePanel)scheduleTable;
	}
	
	protected JList wrapperList;
	protected ListSelectionHandler listSelectionHandler = new ListSelectionHandler();
	
	protected static Color slotColors[][];

	protected Color fixedSlotColors[][];
	
	protected Boolean frameListSelected = false;
	public int[] selectedListIndices = null;
	
	protected Schedule schedule;
	protected SettingsPanel settings;
	
	public SchedulePanel(Schedule schedule, SettingsPanel settings, DetailedViewWrapper detailedViewWrapper)
	{
		this.schedule = schedule;
		this.settings = settings;
		this.detailedViewWrapper = detailedViewWrapper;
		
		slotColors = new Color[schedule.getNumberOfCycles()][schedule.getSlotsPerCycle()];
		for (int i = 0; i < slotColors.length; i++)
		{
			for (int j = 0; j < slotColors[i].length; j++)
			{
				slotColors[i][j] = new Color(255,255,255);
			}
		}
		fixedSlotColors = new Color[slotColors.length][slotColors[0].length];
		for (int i = 0; i < fixedSlotColors.length; i++)
		{
			for (int j = 0; j < fixedSlotColors[i].length; j++)
			{
				fixedSlotColors[i][j] = slotColors[i][j];
			}
		}
	}
	
	public void colorsChanged()
	{
		wrapperList.clearSelection();
	}	
	
	class ListSelectionHandler implements ListSelectionListener 
	{
		public void valueChanged(ListSelectionEvent e) 
		{ 
			if (e.getValueIsAdjusting() == false) 
			{
				JList list = (JList)e.getSource();
				
				// Get all selected items
//				Object[] selected = list.getSelectedValuesList().toArray();
				Object[] selected = list.getSelectedValues();
				selectedListIndices = list.getSelectedIndices();
				if(selectedListIndices.length == 0)
				{
					frameListSelected = false;
				}
				else
				{
					frameListSelected = true;
				}
				
				for (int i=0; i<slotColors.length; i++) 
				{
					for (int j = 0; j < slotColors[i].length; j++)
					{
						slotColors[i][j] = new Color(255, 255, 255); 
					}
				}
				
				msgViewer.setText("");
				
				selectedElements = new String[selected.length];
				detailedViewWrapper.setSelectedElements(selectedElements);
				
				// Iterate all selected items
				for (int i=0; i<selected.length; i++) 
				{
					String sel = (String)selected[i];
					
					selectedElements[i] = sel;
					
					@SuppressWarnings("rawtypes")
					Message selMsg = null;
					
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
					scheduleTable.repaint();
				}
			}
		}
	}

	protected void markSlot(@SuppressWarnings("rawtypes") Message msg)
	{
		Color slotColor = new Color(0,0,0);
		if(settings.getEcuMode())
		{
			for (@SuppressWarnings("rawtypes") Iterator m = schedule.getECUs().iterator(); m.hasNext();)
			{
				String ecu = (String) m.next();
			
				if(msg.getSender().equals(ecu))
				{
					slotColor = EcuColors.get(ecu);
				}
			}
		}
		else
		{
			Random random = new Random();
			slotColor = fixedSlotColors[schedule.getBaseCycle(msg)][schedule.getSlot(msg)%schedule.getSlotsPerCycle()];
			if(slotColor.getRed() == 255 && slotColor.getBlue() == 255 && slotColor.getGreen() == 255)
			{
				slotColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
				fixedSlotColors[schedule.getBaseCycle(msg)][schedule.getSlot(msg)%schedule.getSlotsPerCycle()] = slotColor;
			}
		}
		
		for (int y = schedule.getBaseCycle(msg); y < slotColors.length; y+=schedule.getRepetition(msg)) 
		{
			slotColors[y][schedule.getSlot(msg)%schedule.getSlotsPerCycle()] = slotColor;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void paint(Graphics g) 
	{
		super.paint(g);
		
		if(!initialized)
		{
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		
			setLayout(new BorderLayout());
		
			String listData[] = new String[schedule.getMessages().size()];
			int cnt = 0;
			Collection<Message> msgs = schedule.getMessages();
			List msgList = new LinkedList(msgs);
			Collections.sort(msgList, new MessageNameComparator());
			   
			for (Iterator i = msgList.iterator(); i.hasNext();)
			{
				Message msg = (Message) i.next();
						
				listData[cnt] = msg.getName();
				cnt++;
			}
    
			wrapperList = new JList(listData);
			wrapperList.setLayoutOrientation(JList.VERTICAL);
			wrapperList.setVisibleRowCount(-1);
			wrapperList.addListSelectionListener(listSelectionHandler);
	    
			scheduleTable = new ScheduleTablePanel(schedule, settings);
		
			right = new JPanel();
			right.setName("right_panel");
			
			JScrollPane scrollList = new JScrollPane(wrapperList);
			scrollList.getVerticalScrollBar().setUnitIncrement(10);
			right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
			intermediatePanel = new JPanel();
			intermediatePanel.add(scrollList);
			intermediatePanel.setLayout(new BoxLayout(intermediatePanel, BoxLayout.X_AXIS));
			intermediatePanel.setMinimumSize(new Dimension(200,800));
			right.add(intermediatePanel);
			right.setPreferredSize(new Dimension(200, this.getHeight()));
			right.setBackground(new Color(255,255,255));
			right.setOpaque(true);
			
			slotViewer = new JTextArea("");
			slotViewer.setEditable(false);
			JScrollPane slotViewerPane = new JScrollPane(slotViewer);
			slotViewerPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			
			msgViewer = new JTextArea("");
			msgViewer.setEditable(false);
			JScrollPane msgViewerPane = new JScrollPane(msgViewer);
			msgViewerPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			
			infoPane = new JTabbedPane();
			infoPane.add("Slot", slotViewerPane);
			infoPane.add("Messages", msgViewerPane);
			infoPane.setPreferredSize(new Dimension(215, 50));
			right.add(infoPane);
			
			this.add(scheduleTable, BorderLayout.CENTER);
			this.add(right, BorderLayout.LINE_END);
		}
		
		initialized = true;
	}
	
	public class ScheduleTablePanel extends JPanel implements MouseMotionListener
	{

		private static final long serialVersionUID = 1L;
		protected static final int SLOT_HEIGHT = 14;
		protected static final int SLOT_WIDTH = 14;
		
		protected Schedule schedule;
		protected SettingsPanel settings;
		
		public ScheduleTablePanel(Schedule schedule, SettingsPanel settings)
		{
			this.schedule = schedule;
			this.settings = settings;
			setBackground(new Color(255,255,255));
			setOpaque(true);
			addMouseMotionListener(this);
		}
		
		protected class Slot
		{
			int cycle;
			int slotInCycle;
			
			public Slot(int cycle, int slotInCycle)
			{
				this.cycle = cycle;
				this.slotInCycle = slotInCycle;
			}
		}
		
		protected Slot getSlot(int x, int y)
		{
			for (int i = 0; i < schedule.getNumberOfCycles(); i++) 
			{
				for (int j = 0; j < schedule.getSlotsPerCycle(); j++) 
				{
					if((x < (((j+1)*SLOT_WIDTH+30)+SLOT_WIDTH)) && (x > ((j+1)*SLOT_WIDTH+30)))
					{
						if((y < (((i+1)*SLOT_HEIGHT+15)+SLOT_HEIGHT) && (y > ((i+1)*SLOT_HEIGHT+15))))
						{
							return new Slot(i, j);
						}
					}
				}
			}
			
			return new Slot(schedule.getNumberOfCycles(), schedule.getSlotsPerCycle());
		}
		
		@Override
		public void mouseMoved(MouseEvent e)
		{
			int x = e.getX();
			int y = e.getY();
			
			Slot slot = getSlot(x, y);
			if(slot.cycle == schedule.getNumberOfCycles() && slot.slotInCycle == schedule.getSlotsPerCycle())
			{
				slotViewer.setText("");
			}
			else
			{
				/* cursor is in slot array */
				if(settings.getAutoSwitchPane())
				{
					infoPane.setSelectedIndex(infoPane.indexOfTab("Slot"));
				}
				slotViewer.setText("");
				if(slot.cycle<10 && slot.slotInCycle<10)
				{
					slotViewer.append("Cycle 0" + slot.cycle + " Slot 0" + slot.slotInCycle +": ");
				}
				else if(slot.cycle>=10 && slot.slotInCycle>=10)
				{
					slotViewer.append("Cycle " + slot.cycle + " Slot " + slot.slotInCycle +": ");
				}
				else if(slot.cycle<10 && slot.slotInCycle>=10)
				{
					slotViewer.append("Cycle 0" + slot.cycle + " Slot " + slot.slotInCycle +": ");
				}
				else if(slot.cycle>=10 && slot.slotInCycle<10)
				{
					slotViewer.append("Cycle " + slot.cycle + " Slot 0" + slot.slotInCycle +": ");
				}
				
				for (@SuppressWarnings("rawtypes") Iterator m = schedule.getMessages(slot.cycle*schedule.getSlotsPerCycle()+slot.slotInCycle).iterator(); m.hasNext();)
				{
					@SuppressWarnings("rawtypes")
					Message msg = (Message) m.next();
				
					slotViewer.append((String)msg.getSender() + "\n");			
					slotViewer.append(msg.getName()+ "(" + new DecimalFormat("#").format(msg.getPeriod()) + "," + new DecimalFormat("#").format(msg.getDeadline())+ ")" +":\n");
				}
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent arg0)
		{
				
		}
		
		public void paint(Graphics g) 
		{
			super.paint(g);
			
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.setColor(new Color(255, 255, 255));
			
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
			        RenderingHints.VALUE_ANTIALIAS_ON);
			
			
			rh.put(RenderingHints.KEY_RENDERING,
			       RenderingHints.VALUE_RENDER_QUALITY);
			
			g2d.setRenderingHints(rh);
			
			g2d.setFont(new Font("Arial", Font.PLAIN, 10));
			
			g2d.setColor(new Color(0, 0, 0));
			g2d.drawString("CycleNo.", 10, 20);
			for (int i = 0; i < schedule.getNumberOfCycles(); i++) 
			{
				g2d.drawString(Integer.toString(i), 20, i*SLOT_HEIGHT+40);
			}
				
			for (int i = 0; i < schedule.getNumberOfCycles(); i++) 
			{
				for (int j = 0; j < schedule.getSlotsPerCycle(); j++) 
				{
					g2d.setColor(slotColors[i][j]);
					g2d.fillRect((j+1)*SLOT_WIDTH+30, (i+1)*SLOT_HEIGHT+15, SLOT_WIDTH, SLOT_HEIGHT);
					        
					g2d.setColor(new Color(0, 0, 0));
					g2d.drawRect((j+1)*SLOT_WIDTH+30, (i+1)*SLOT_HEIGHT+15, SLOT_WIDTH, SLOT_HEIGHT);
				}
			}
		}
	}
}