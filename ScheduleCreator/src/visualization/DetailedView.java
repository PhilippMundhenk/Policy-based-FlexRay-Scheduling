package visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JPanel;

import SchedulingInterfaces.Message;
import SchedulingInterfaces.Schedule;

public class DetailedView extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final int SLOT_WIDTH = 50;
	private static final int SLOT_HEIGHT = 50;
	private static final int LEFT_OFFSET = 100;
	private static final int TOP_OFFSET = 20;
	private static final int RIGHT_OFFSET = 20;
	private static final int BOTTOM_OFFSET = 50;
	
	private static final int ARROW_ANGLE = 3;
	
	private float[] dotPattern = { 1, 2 };
	private Stroke dottedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dotPattern, 0);
	private Stroke wideDottedStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dotPattern, 0);
	private float[] linePattern = { 1, 0 };
	private Stroke lineStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, linePattern, 0);
	private Stroke wideLineStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, linePattern, 0);
	
	private int width;
	private int height;
	
	protected String[] selectedElements;
	protected Schedule schedule;
	protected DetailedViewerDataDelivery dd;
	protected int cycle = 0;

	public DetailedView(Schedule schedule, DetailedViewerDataDelivery dataDelivery)
	{
		setBackground(new Color(255,255,255));
		setOpaque(true);
		this.schedule = schedule;
		this.dd = dataDelivery;
		//width = getWidth();
		//height = getHeight();
	}
	
	public void setSelectedElements(String[] selectedElements)
	{
		this.selectedElements = selectedElements;
		repaint();
	}
	
	public int getLocalWidth()
	{
		return width;
	}
	
	public int getLocalHeight()
	{
		return height;
	}
	
	public void setCycle(int cycle)
	{
		this.cycle = cycle;
	}
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setColor(new Color(255, 255, 255));
		
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		g2d.setRenderingHints(rh);
		
		g2d.setFont(new Font("Arial", Font.PLAIN, 10));
		
		g2d.setColor(new Color(0, 0, 0));
		
		if(selectedElements == null)
		{
			g2d.drawString("Please select frames to draw in \"Schedule\" tab", LEFT_OFFSET-10, TOP_OFFSET);
			return;
		}
		
		g2d.drawString("Slot number:", LEFT_OFFSET-(SLOT_WIDTH/2), TOP_OFFSET);
		g2d.setStroke(lineStroke);
		g2d.drawLine(LEFT_OFFSET-(SLOT_WIDTH/2), TOP_OFFSET+10, LEFT_OFFSET-(SLOT_WIDTH/2), TOP_OFFSET+25);
		
		int cnt = 0;
		for (int i = 0; i < selectedElements.length; i++)
		{
			@SuppressWarnings("rawtypes")
			Message msg = (Message)dd.getSelectedElement(selectedElements[i]);
			if(dd.isInCycle(cycle, msg) || dd.isDueWaiting(cycle, msg) || dd.isDeadlineWaiting(cycle, msg))
			{
				g2d.setColor(new Color(0, 0, 0));
				g2d.drawString(selectedElements[i], 10, cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2);
				
				g2d.setStroke(lineStroke);
				/* rectangle */
				if(dd.getScheduledSlot(cycle, msg) != -1)
				{
					/* message is due in this cycle, but scheduled in later cycle */
					Rectangle rect = new Rectangle(
												(dd.getScheduledSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+5, 
												cnt*SLOT_HEIGHT+TOP_OFFSET+25+5, 
												SLOT_WIDTH-10, 
												SLOT_HEIGHT-10);
					g2d.setColor(EcuColors.get((String)msg.getSender()));
					g2d.fill(rect);
				}
				
				g2d.setColor(new Color(0, 0, 0));
				
				/* draw lines */
				if(dd.getDueSlot(cycle, msg) != -1)
				{
					if(dd.getScheduledSlot(cycle, msg) == -1 && !dd.isDueWaiting(cycle, msg))
					{
						/* scenario 1 */
						
						g2d.setStroke(wideLineStroke);
						/* left end */
						g2d.drawLine(
								(dd.getDueSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								(dd.getDueSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
						/* line */
						g2d.drawLine(
								(dd.getDueSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
						/* right end */
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2
							);
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
					}
					else if(dd.getScheduledSlot(cycle, msg) != -1 && !dd.isDueWaiting(cycle, msg))
					{
						/* scenario 4 */
						
						g2d.setStroke(wideLineStroke);
						/* left end */
						g2d.drawLine(
								(dd.getDueSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								(dd.getDueSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
						/* line */
						g2d.drawLine(
								(dd.getDueSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								((dd.getScheduledSlot(cycle, msg)%schedule.getSlotsPerCycle())-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+5, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
					}
				}
				else if(dd.getDueSlot(cycle, msg) == -1)
				{
					if(dd.getScheduledSlot(cycle, msg) == -1 && dd.isDueWaiting(cycle, msg))
					{
						/* scenario 2 */
						
						g2d.setStroke(wideLineStroke);
						/* left end */
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2
							);
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2,
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
						/* line */
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
						/* right end */
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2
							);
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
					}
					else if(dd.getScheduledSlot(cycle, msg) != -1 && !dd.isDueWaiting(cycle, msg))
					{
						/* scenario 3 */
						
						g2d.setStroke(wideLineStroke);
						/* left end */
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
						/* line */
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								((dd.getScheduledSlot(cycle, msg)%schedule.getSlotsPerCycle())-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+5, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
					}
				}
				
				if(dd.getDeadlineSlot(cycle, msg) != -1)
				{
					if(dd.getScheduledSlot(cycle, msg) == -1 && !dd.isDeadlineWaiting(cycle, msg))
					{
						/* scenario 7 */
						
						g2d.setStroke(wideLineStroke);
						/* left end */
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2
							);
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2,
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
						/* line */
						g2d.setStroke(wideDottedStroke);
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
						/* right end */
						g2d.setStroke(wideLineStroke);
						g2d.drawLine(
								((dd.getDeadlineSlot(cycle, msg)%schedule.getSlotsPerCycle())+1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								((dd.getDeadlineSlot(cycle, msg)%schedule.getSlotsPerCycle())+1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
					}
					else if(dd.getScheduledSlot(cycle, msg) != -1 && !dd.isDueWaiting(cycle, msg))
					{
						/* scenario 5 */
						
						g2d.setStroke(wideLineStroke);
						/* right end */
						g2d.drawLine(
								((dd.getDeadlineSlot(cycle, msg)%schedule.getSlotsPerCycle())+1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								((dd.getDeadlineSlot(cycle, msg)%schedule.getSlotsPerCycle())+1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2), 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
						/* line */
						g2d.setStroke(wideDottedStroke);
						g2d.drawLine(
								((dd.getScheduledSlot(cycle, msg)%schedule.getSlotsPerCycle())+1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-5, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								(dd.getDeadlineSlot(cycle, msg)%schedule.getSlotsPerCycle())*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
					}
				}
				else if(dd.getDeadlineSlot(cycle, msg) == -1)
				{
					if(dd.getScheduledSlot(cycle, msg) == -1 && dd.isDeadlineWaiting(cycle, msg))
					{
						/* scenario 8 */
						
						g2d.setStroke(wideLineStroke);
						/* left end */
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2
							);
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2,
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
						/* line */
						g2d.setStroke(wideDottedStroke);
						g2d.drawLine(
								0*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
						/* right end */
						g2d.setStroke(wideLineStroke);
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2
							);
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
					}
					else if(dd.getScheduledSlot(cycle, msg) != -1 && !dd.isDueWaiting(cycle, msg))
					{
						/* scenario 6 */
						
						/* line */
						g2d.setStroke(wideDottedStroke);
						g2d.drawLine(
								((dd.getScheduledSlot(cycle, msg)%schedule.getSlotsPerCycle())+1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)-5, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT/2
							);
						/* right marker */
						g2d.setStroke(wideLineStroke);
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+10, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE,
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2
							);
						g2d.drawLine(
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH+ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+5+(SLOT_HEIGHT-10)/2, 
								(schedule.getSlotsPerCycle()-1)*SLOT_WIDTH+LEFT_OFFSET-(SLOT_WIDTH/2)+SLOT_WIDTH-ARROW_ANGLE, 
								cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT-10
							);
					}
				}
				
				
				
				cnt++;
			}
		}
		
		width = schedule.getSlotsPerCycle()*SLOT_WIDTH+LEFT_OFFSET+RIGHT_OFFSET;
		height = cnt*SLOT_HEIGHT+TOP_OFFSET+BOTTOM_OFFSET;
		
		this.setSize(width, height);
		
		if(cnt == 0)
		{
			g2d.setColor(new Color(0, 0, 0));
			g2d.drawString("no selected items scheduled in this cycle", 10, cnt*SLOT_HEIGHT+TOP_OFFSET+25+SLOT_HEIGHT);
		}
		
		for (int i = 0; i < schedule.getSlotsPerCycle(); i++)
		{
			if(i<10)
			{
				if(i == 0)
				{
					g2d.setStroke(dottedStroke);
					g2d.setColor(new Color(0, 0, 0));
					g2d.drawLine(LEFT_OFFSET-(SLOT_WIDTH/2), TOP_OFFSET+25, LEFT_OFFSET-(SLOT_WIDTH/2), height+10);
				}
				g2d.setColor(new Color(0, 0, 0));
				g2d.drawString(Integer.toString(i), i*SLOT_WIDTH+LEFT_OFFSET-2, 40);
			}
			else
			{
				g2d.setColor(new Color(0, 0, 0));
				g2d.drawString(Integer.toString(i), i*SLOT_WIDTH+LEFT_OFFSET-5, 40);
			}
			g2d.setColor(new Color(0, 0, 0));
			g2d.setStroke(lineStroke);
			g2d.drawLine(i*SLOT_WIDTH+LEFT_OFFSET+(SLOT_WIDTH/2), TOP_OFFSET+10, i*SLOT_WIDTH+LEFT_OFFSET+(SLOT_WIDTH/2), TOP_OFFSET+25);
			g2d.setStroke(dottedStroke);
			g2d.drawLine(i*SLOT_WIDTH+LEFT_OFFSET+(SLOT_WIDTH/2), TOP_OFFSET+25, i*SLOT_WIDTH+LEFT_OFFSET+(SLOT_WIDTH/2), height+10);
		}
				
		setPreferredSize(new Dimension(getLocalWidth(),getLocalHeight()));
		
		invalidate();
		repaint();
	}
}
