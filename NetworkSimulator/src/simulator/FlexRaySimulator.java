package simulator;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import simulator.components.central.Statistics;
import simulator.components.central.Timer;
import simulator.components.central.bus.Bus;
import simulator.components.central.bus.FlexRayBus;
import simulator.components.distributed.ECU;
import simulator.components.distributed.FlexRayECU;
import simulator.components.distributed.ecuModules.Clk;
import simulator.components.distributed.ecuModules.Com;
import support.SimLog;
import SchedulingInterfaces.Schedule;


public class FlexRaySimulator extends Simulator
{
	private static final String module = "Sim";
	private Statistics stats;

	@Override
	public void simulate(Schedule schedule, @SuppressWarnings("rawtypes") Collection msgs, int numberOfCycles)
	{
		stats = new Statistics();
		Timer tim = new Timer(stats, schedule.getCycleDuration());
		LinkedHashSet<ECU> ecus = new LinkedHashSet<ECU>();
		Bus bus = new FlexRayBus(stats);
		
		SimLog.initLog();
		SimLog.setLOG_EXCEPTIONS(new Object[][]{{"Com_Ecu_6", SimLog.LOG_LEVEL_TRACE}, {"ECU_Ecu_6", SimLog.LOG_LEVEL_TRACE}, {"Sim", SimLog.LOG_LEVEL_TRACE}, {"Stat", SimLog.LOG_LEVEL_TRACE}});
		SimLog.logLowln("\t############################################", module);
		SimLog.logLowln("\tSimulator started", module);
		
		for (@SuppressWarnings("rawtypes")
		Iterator i = schedule.getECUs().iterator(); i.hasNext();)
		{
			String ecuName = (String)i.next();
			
			ECU ecu = new FlexRayECU(ecuName, stats, schedule, msgs);
			ecu.addModule(new Clk(Clk.NO_DRIFT, ecu));
			ecu.addModule(new Com(bus, schedule, msgs, ecu));
			
			ecus.add(ecu);
		}
		
		//TODO: find longest pause between two messages over whole schedule (per ECU) and make sure msg arrives at beginning of this pause. msgOffset = pause%cycleLen		
		
		for (Iterator<ECU> i = ecus.iterator(); i.hasNext();)
		{
			ECU ecu = (ECU) i.next();
			
			tim.registerECU(ecu);
		}
		tim.registerBus(bus);
		
		int cycle = 0;
		while(cycle < numberOfCycles)
		{
			tim.tick();
			
			for (Iterator<ECU> i = ecus.iterator(); i.hasNext();)
			{
				ECU ecu = (ECU) i.next();
				
				ecu.execute();
			}
			
			cycle = (int)(tim.getTimeMillisec()/schedule.getCycleDuration());
		}
		
		SimLog.logLowln("\t############################################", module);
	}
	
	public Statistics getStats()
	{
		return stats;
	}
}
