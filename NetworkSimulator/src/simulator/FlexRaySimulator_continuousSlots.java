package simulator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import flexRay.FlexRayStaticMessage;

import simulator.components.central.Statistics;
import simulator.components.central.Timer;
import simulator.components.central.bus.Bus;
import simulator.components.central.bus.FlexRayBus;
import simulator.components.distributed.ECU;
import simulator.components.distributed.FlexRayECU;
import simulator.components.distributed.ecuModules.Clk;
import simulator.components.distributed.ecuModules.Com_continuousSlots;
import support.SimLog;
import SchedulingInterfaces.Schedule;


public class FlexRaySimulator_continuousSlots extends Simulator
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
		SimLog.setLOG_EXCEPTIONS(new Object[][]{{"Sim", SimLog.LOG_LEVEL_TRACE}, {"Stat", SimLog.LOG_LEVEL_TRACE}});
//		SimLog.setLOG_EXCEPTIONS(new Object[][]{{"Sim", SimLog.LOG_LEVEL_TRACE}, {"Stat", SimLog.LOG_LEVEL_TRACE}, {"ECU_ECU_1", SimLog.LOG_LEVEL_TRACE}, {"Com_ECU_1", SimLog.LOG_LEVEL_TRACE}});
		SimLog.logLowln("\t############################################", module);
		SimLog.logLowln("\tSimulator started", module);
		
		for (@SuppressWarnings("rawtypes")
		Iterator i = schedule.getECUs().iterator(); i.hasNext();)
		{
			String ecuName = (String)i.next();
			
			ECU ecu = new FlexRayECU(ecuName, stats, schedule, msgs);
			ecu.addModule(new Clk(Clk.NO_DRIFT, ecu));
			ecu.addModule(new Com_continuousSlots(bus, schedule, msgs, ecu));
			
			ecus.add(ecu);
		}
		
		/* find longest pause between two messages over whole schedule (per ECU) and make sure msg arrives at beginning of this pause. */
		for (Iterator<ECU> i = ecus.iterator(); i.hasNext();)
		{
			ECU ecu = (ECU) i.next();
			
			int cycle = 0;
			int slot = 0;
			
			while(!(schedule.getMessages(cycle, slot).isEmpty()) && !(((FlexRayStaticMessage)schedule.getMessages(cycle, slot).toArray()[0]).getSender().equals(ecu.getName())))
			{
				slot++;
				if(slot == schedule.getSlotsPerCycle())
				{
					slot = 0;
					cycle++;
				}
			}
			
			int firstFrame = cycle * schedule.getSlotsPerCycle() + slot;
			
			slot++;
			if(slot == schedule.getSlotsPerCycle())
			{
				slot = 0;
				cycle++;
			}
			while(!(schedule.getMessages(cycle, slot).isEmpty()) && !(((FlexRayStaticMessage)schedule.getMessages(cycle, slot).toArray()[0]).getSender().equals(ecu.getName())))
			{
				slot++;
				if(slot == schedule.getSlotsPerCycle())
				{
					slot = 0;
					cycle++;
				}
			}
			
			int diff = (cycle * schedule.getSlotsPerCycle() + slot) - firstFrame;
			
			HashMap<FlexRayStaticMessage, Double> nextMsgStart = new HashMap<FlexRayStaticMessage, Double>();
			for (@SuppressWarnings("rawtypes") Iterator j = msgs.iterator(); j.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage) j.next();
				
				if(msg.getSender().equals(ecu.getName()))
				{
					nextMsgStart.put(msg, (diff*schedule.getSlotDuration()) % schedule.getCycleDuration());
				}
			}
			ecu.setNextMsgStart(nextMsgStart);
		}
		
		for (Iterator<ECU> i = ecus.iterator(); i.hasNext();)
		{
			ECU ecu = (ECU) i.next();
			
			tim.registerECU(ecu);
		}
		tim.registerBus(bus);
		
		int cycle = 0;
		int old_cycle = 0;
		while(cycle < numberOfCycles)
		{
			if(cycle % 15 == 0 && cycle != old_cycle)
			{
				SimLog.logLowln("\tSimulation running... (" + cycle/(numberOfCycles/100) + "%)", module);
			}
			tim.tick();
			
			for (Iterator<ECU> i = ecus.iterator(); i.hasNext();)
			{
				ECU ecu = (ECU) i.next();
				
				ecu.execute();
			}
			
			old_cycle = cycle;
			cycle = (int)(tim.getTimeMillisec()/schedule.getCycleDuration());
		}
		
		SimLog.logLowln("\t############################################", module);
	}
	
	public Statistics getStats()
	{
		return stats;
	}
}
