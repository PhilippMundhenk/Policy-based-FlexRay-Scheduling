package visualization;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;

public class Visualizer implements DetailedViewerDataDelivery
{
	protected Schedule schedule;
	
	protected DetailedViewWrapper detailedViewWrapper;
	protected SchedulePanel schedulePanel;
	
	protected JFrame frame;
	
	protected static final int MAX_NO_PANELS = 15;
	protected JTabbedPane tabbedPane;
	protected static JComponent[] panels = new JComponent[MAX_NO_PANELS];
	protected static String[] panelNames = new String[MAX_NO_PANELS];
	protected static int panelCnt = 0;
	
	protected SettingsPanel settings;
		
	
	public Visualizer()
	{
		super();
	}
	
	
	
	public void visualize(Schedule sched)
	{
		setSchedule(sched);
		
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		new EcuColors(sched);
		EcuColors.newColors();
		
		frame = new JFrame("Schedule");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(1200, 1000);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());

		settings = new SettingsPanel(this);
		detailedViewWrapper = new DetailedViewWrapper(schedule, this);
		
		panelNames[panelCnt] = "Schedule";
		schedulePanel = new SchedulePanel(schedule, settings, detailedViewWrapper);
		panels[panelCnt] = schedulePanel;
		panelCnt++;
		
		panels[panelCnt] = detailedViewWrapper;
		panelNames[panelCnt] = "Detailed View";
		panelCnt++;

		panelNames[panelCnt] = "Settings";
		panels[panelCnt] = settings;
		panelCnt++;

		panelNames[panelCnt] = "Help";
		panels[panelCnt] = new JPanel();
		panelCnt++;
		
		tabbedPane = new JTabbedPane();
		for (int i = 0; i < panels.length; i++)
		{
			if(panels[i] != null)
			{
				tabbedPane.addTab(panelNames[i], panels[i]);
			}
		}
		topPanel.add(tabbedPane, BorderLayout.CENTER);
		
		topPanel.add(EcuColors.getNewEcuColorPanel(), BorderLayout.PAGE_END);

		frame.add(topPanel);
		
		frame.setVisible(true);
	}
	
	public void changePanel(int panelNumber, JPanel panel, String name)
	{
		tabbedPane.removeAll();
		panels[panelNumber] = panel;
		if(name != "")
		{
			panelNames[panelNumber] = name;
		}
		for (int i = 0; i < panels.length; i++)
		{
			if(panels[i] != null)
			{
				tabbedPane.addTab(panelNames[i], panels[i]);
			}
		}
	}
	
	public void setSchedule(Schedule sched)
	{
		schedule = sched;
	}
	
	public SettingsPanel getSettings()
	{
		return settings;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getSelectedElement(Object identifier)
	{
		for (Iterator i = schedule.getMessages().iterator(); i.hasNext();)
		{
			Message msg = (Message) i.next();
			
			if(msg.getName().equals((String)identifier))
			{
				return msg;
			}
		}
		return null;
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
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int getDueSlot(int cycle, Object msg)
	{
		return schedule.getSlot(((Message)msg));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int getScheduledSlot(int cycle, Object msg)
	{
		int calculatedCycle = -1;
		int cnt = 0;
		int baseCycle = -1;
		int repetition = -1;
		for (Iterator i = schedule.getMessages().iterator(); i.hasNext();)
		{
			Message message = (Message) i.next();
			
			if(message.getName().equals(((Message)msg).getName()))
			{
				baseCycle = schedule.getBaseCycle(((Message)msg));
				repetition = schedule.getRepetition(((Message)msg));
			}
		}
		
		if(baseCycle == -1)
		{
			return -1;
		}
		
		while(calculatedCycle < schedule.getNumberOfCycles())
		{
			calculatedCycle = baseCycle+cnt*repetition;
			if(cycle == calculatedCycle)
			{
				return schedule.getSlot(((Message)msg));
			}
			cnt++;
		}
		return -1;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int getDeadlineSlot(int cycle, Object msg)
	{
		return getScheduledSlot(cycle, msg)+(int)((double)((Message)msg).getDeadline()/(double)schedule.getSlotDuration())-1;
	}
	
	public void setSelectedTab(int tab)
	{
		tabbedPane.setSelectedIndex(tab);
	}
	
	public void colorsChanged()
	{
		schedulePanel.colorsChanged();
	}



	@Override
	public Boolean isDueWaiting(int cycle, Object msg)
	{
		return false;
	}



	@Override
	public Boolean isDeadlineWaiting(int cycle, Object msg)
	{
		return false;
	}
}
