package simulator.components.central.bus;

import java.util.Collection;
import java.util.Iterator;

import simulator.components.central.Statistics;
import simulator.config.Config;
import support.SimLog;
import SchedulingInterfaces.Message;

public class FlexRayBus implements Bus
{
	private static final String module = "FRBus";
	private Statistics stats;
	private int time;
	
	public FlexRayBus(Statistics stats)
	{
		this.stats = stats;
		time = (-1)*Config.TICK_LEN_MICROSEC;
	}

	@Override
	public void tick(double tickLenMicrosec)
	{
		time += tickLenMicrosec;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void sendMsg(char[] outBuf, Collection<Message> msgs)
	{
		SimLog.logMedium("[SimTime: " + time + "µs] msgs rcvd: ", module);
		for (Iterator i = msgs.iterator(); i.hasNext();)
		{
			Message msg = (Message) i.next();
			
			SimLog.logMedium(msg.getName() + ", ", module);
		}
		SimLog.logMediumln(module);
		for (Iterator<Message> i = msgs.iterator(); i.hasNext();)
		{
			Message msg = (Message) i.next();
			
			stats.receiveMsg_bus(msg);
		}
	}

}
