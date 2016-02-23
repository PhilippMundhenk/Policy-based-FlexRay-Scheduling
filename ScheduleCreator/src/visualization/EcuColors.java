package visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import SchedulingInterfaces.Schedule;

public class EcuColors extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static final int MAX_NUMBER = 1;
	protected static HashMap<String, Color> ecuColors = new HashMap<String, Color>();
	protected static Schedule schedule;
	protected static LinkedHashSet<EcuColors> instances = new LinkedHashSet<EcuColors>();
	
	@SuppressWarnings("static-access")
	public EcuColors(Schedule schedule)
	{
		this.schedule = schedule;
		instances.add(this);
	}
	
	private EcuColors()
	{
		instances.add(this);
	}
	
	public static EcuColors getNewEcuColorPanel()
	{
		if(schedule != null && instances.size() <= MAX_NUMBER)
		{
			return new EcuColors();
		}
		else
		{
			return null;
		}
	}
	
	public static void setSchedule(Schedule sched)
	{
		schedule = sched;		
	}
	
	@SuppressWarnings("unchecked")
	public static void newColors()
	{
		ecuColors.clear();
		Random random = new Random();
		
		HashMap<Integer, Color> colours = new HashMap<Integer, Color>();
		colours.put(0, new Color(255,0,0));
		colours.put(1, new Color(0,255,0));
		colours.put(2, new Color(0,0,255));
		colours.put(3, new Color(255,255,0));
		colours.put(4, new Color(0,255,255));
		colours.put(5, new Color(255,0,255));
		colours.put(6, new Color(255,125,0));
		colours.put(7, new Color(125,255,0));
		colours.put(8, new Color(0,255,125));
		colours.put(9, new Color(0,125,255));
		colours.put(10, new Color(125,0,255));
		colours.put(11, new Color(255,0,125));
		
		int cnt = 0;
		for (Iterator<String> i = schedule.getECUs().iterator(); i.hasNext();)
		{
			String ecu = (String) i.next();
			
			if(cnt < colours.size())
			{
				ecuColors.put(ecu, colours.get(cnt));
			}
			else
			{
				ecuColors.put(ecu, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
			}
			cnt++;
		}
		for (Iterator<EcuColors> i = instances.iterator(); i.hasNext();)
		{
			EcuColors panel = (EcuColors) i.next();
			panel.repaint();
		}
	}
	
	public static Color get(String ecu)
	{
		return ecuColors.get(ecu);
	}

	public void paint(Graphics g) 
	{
		super.paint(g);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBackground(new Color(255,255,255));
		this.setOpaque(true);
		
		this.removeAll();
		
		@SuppressWarnings("unchecked")
		LinkedList<String> ecus = new LinkedList<String>(schedule.getECUs());
		Collections.sort(ecus);
		for (Iterator<String> i = ecus.iterator(); i.hasNext();)
		{
			String ecu = (String) i.next();
			
			JLabel name = new JLabel();
			name.setText("  " + ecu + "  ");
			Font font = new Font(name.getFont().getName(),Font.BOLD,name.getFont().getSize()); 
			name.setFont(font);
			name.setForeground(ecuColors.get(ecu));
			name.setBackground(new Color(255,255,255));
			name.setOpaque(true);
			this.add(name);
		}
	}
	
	public static void triggerRepaint()
	{
		for (Iterator<EcuColors> i = instances.iterator(); i.hasNext();)
		{
			EcuColors panel = (EcuColors) i.next();
			panel.repaint();
		}
	}
	
}
