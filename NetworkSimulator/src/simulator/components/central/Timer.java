package simulator.components.central;

import java.util.Iterator;
import java.util.LinkedHashSet;

import simulator.components.central.bus.Bus;
import simulator.components.distributed.ECU;
import simulator.components.distributed.ecuModules.Clk;
import simulator.config.Config;
import support.SimLog;

public class Timer
{
	private static final String module = "Timer";
	LinkedHashSet<Clk> clocks = new LinkedHashSet<Clk>();
	Bus bus;
	Statistics stats;
	double time;
	double cycleDurationMillisec;
	
	public Timer(Statistics stats, double cycleDurationMillisec)
	{
		this.stats = stats;
		this.cycleDurationMillisec = cycleDurationMillisec;
		time = (-1)*Config.TICK_LEN_MICROSEC;
	}

	public void registerECU(ECU ecu)
	{
		clocks.add(ecu.getClk());
	}

	public void registerBus(Bus bus)
	{
		this.bus = bus;
	}

	public void tick()
	{
		time += Config.TICK_LEN_MICROSEC;
		SimLog.logTraceln("[SimTime: " + time + "µs] Tick", module);
		
		stats.tick(Config.TICK_LEN_MICROSEC);
		bus.tick(Config.TICK_LEN_MICROSEC);
		for (Iterator<Clk> i = clocks.iterator(); i.hasNext();)
		{
			Clk clk = (Clk) i.next();
			
			clk.tick(Config.TICK_LEN_MICROSEC, (int)(time/cycleDurationMillisec), time);
		}
	}

	public double getTimeMicrosec()
	{
		return time;
	}
	
	public double getTimeMillisec()
	{
		return time/1000;
	}	
}
