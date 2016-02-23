package visualization;


public interface DetailedViewerDataDelivery
{
	public Object getSelectedElement(Object identifier);
	public Boolean isInCycle(int cycle, Object msg);
	public int getDueSlot(int cycle, Object msg);
	public int getScheduledSlot(int cycle, Object msg);
	public int getDeadlineSlot(int cycle, Object msg);
	public Boolean isDueWaiting(int cycle, Object msg);
	public Boolean isDeadlineWaiting(int cycle, Object msg);
}
