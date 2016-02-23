package visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import SchedulingInterfaces.Schedule;

public class DetailedViewWrapper extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private int cycle = 0;
	
	private DetailedView detailedView;
	private Schedule savedSchedule;
	private JTextField cycleNumber;
	private JButton lastCycleBtn;
	private JButton nextCycleBtn;
	private JPanel navigationBar;
	private JScrollPane scrollPane;
	
	public DetailedViewWrapper(Schedule schedule, DetailedViewerDataDelivery dataDelivery)
	{
		setBackground(new Color(255,255,255));
		setOpaque(true);
		setLayout(new BorderLayout());
		savedSchedule = schedule;
		detailedView = new DetailedView(schedule, dataDelivery);
		scrollPane = new JScrollPane(detailedView);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
		
		navigationBar = new JPanel();
		navigationBar.setLayout(new BoxLayout(navigationBar, BoxLayout.X_AXIS));
		navigationBar.setBackground(new Color(255,255,255));
		navigationBar.setOpaque(true);
		navigationBar.setAlignmentX(CENTER_ALIGNMENT);
		
		lastCycleBtn = new JButton("Previous Cycle");
		lastCycleBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(!(cycle == 0))
				{
					cycle--;
					nextCycleBtn.setEnabled(true);
				}

				if(cycle == 0)
				{
					lastCycleBtn.setEnabled(false);
				}
				detailedView.setCycle(cycle);
				detailedView.repaint();
				cycleNumber.setText("current cycle: "+cycle);
			}
		});
		if(cycle == 0)
		{
			lastCycleBtn.setEnabled(false);
		}
		navigationBar.add(lastCycleBtn);
		
		cycleNumber = new JTextField();
		cycleNumber.setEditable(false);
		cycleNumber.setText("current cycle: "+cycle);
		cycleNumber.setHorizontalAlignment(JTextField.CENTER);
		navigationBar.add(cycleNumber);
		
		nextCycleBtn = new JButton("Next Cycle");
		nextCycleBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(!(cycle == (savedSchedule.getNumberOfCycles()-1)))
				{
					cycle++;
					lastCycleBtn.setEnabled(true);
				}

				if(cycle == (savedSchedule.getNumberOfCycles()-1))
				{
					nextCycleBtn.setEnabled(false);
				}
				detailedView.setCycle(cycle);
				detailedView.revalidate();
				detailedView.repaint();
				scrollPane.revalidate();
				scrollPane.repaint();
				cycleNumber.setText("current cycle: "+cycle);
			}
		});
		navigationBar.add(nextCycleBtn);
		
		add(navigationBar, BorderLayout.PAGE_END);
	}
	
	public void paintComponents(Graphics g)
	{
		navigationBar.removeAll();
		navigationBar.setLayout(new BoxLayout(navigationBar, BoxLayout.X_AXIS));
		navigationBar.setBackground(new Color(255,255,255));
		navigationBar.setOpaque(true);
		navigationBar.setAlignmentX(CENTER_ALIGNMENT);
		
		navigationBar.add(EcuColors.getNewEcuColorPanel());
		
		lastCycleBtn = new JButton("Previous Cycle");
		lastCycleBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(!(cycle == 0))
				{
					cycle--;
					nextCycleBtn.setEnabled(true);
				}

				if(cycle == 0)
				{
					lastCycleBtn.setEnabled(false);
				}
				detailedView.setCycle(cycle);
				detailedView.repaint();
				cycleNumber.setText("current cycle: "+cycle);
			}
		});
		if(cycle == 0)
		{
			lastCycleBtn.setEnabled(false);
		}
		navigationBar.add(lastCycleBtn);
		
		cycleNumber = new JTextField();
		cycleNumber.setEditable(false);
		cycleNumber.setText("current cycle: "+cycle);
		cycleNumber.setHorizontalAlignment(JTextField.CENTER);
		navigationBar.add(cycleNumber);
		
		nextCycleBtn = new JButton("Next Cycle");
		nextCycleBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(!(cycle == (savedSchedule.getNumberOfCycles()-1)))
				{
					cycle++;
					lastCycleBtn.setEnabled(true);
				}

				if(cycle == (savedSchedule.getNumberOfCycles()-1))
				{
					nextCycleBtn.setEnabled(false);
				}
				detailedView.setCycle(cycle);
				detailedView.repaint();
				cycleNumber.setText("current cycle: "+cycle);
			}
		});
		navigationBar.add(nextCycleBtn);
		
		add(navigationBar, BorderLayout.PAGE_END);
	}
	
	public void setSelectedElements(String[] selectedElements)
	{
		detailedView.setSelectedElements(selectedElements);
	}
}
