package simulator.components.distributed.ecuModules;

import simulator.components.distributed.ECU;
import simulator.config.Config;
import support.SimLog;


public class Clk implements EcuModule
{
	private String module = "Clk_";
	public static final int NO_DRIFT = -1;
	public static final int RANDOM_DRIFT = -2;
	
	private static final String MODULE_TYPE = "Clk";
	
	private double time;
	private double drift_ppmPerTick;
	private int cycle = -1;
	private ECU ecu;
	
	public Clk(double drift_ppmPerTick, ECU ecu)
	{
		this.ecu = ecu;
		module += this.ecu.getName();
		
		time = (-1)*Config.TICK_LEN_MICROSEC;
		if(drift_ppmPerTick == NO_DRIFT)
		{
			this.drift_ppmPerTick = 0;
		}
		else if(drift_ppmPerTick == RANDOM_DRIFT)
		{
			//TODO: add random number
		}
		else
		{
			this.drift_ppmPerTick = drift_ppmPerTick;
		}
	}

	public static String getModuleType()
	{
		return MODULE_TYPE;
	}

	@Override
	public String getType()
	{
		return MODULE_TYPE;
	}

	public void tick(double tickLenMicrosec, int cycle, double commonTime)
	{
		time = getTimeMicrosec() + (tickLenMicrosec*(1 + (drift_ppmPerTick/1000000)));
		SimLog.logTraceln("[SimTime: " + time + "µs] Tack (local time)", module);
		if(this.cycle != cycle)
		{
			time = commonTime;
			this.cycle = cycle;
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

	@Override
	public void execute()
	{
		/* not necessary */
	}
}
