package simulator.components.distributed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import simulator.components.central.Statistics;
import simulator.components.distributed.ecuModules.Clk;
import simulator.components.distributed.ecuModules.Com;
import simulator.components.distributed.ecuModules.EcuModule;
import support.SimLog;
import SchedulingInterfaces.Schedule;
import flexRay.FlexRayStaticMessage;

public class FlexRayECU extends ECU
{
	private String module = "ECU_";
	private String name;
	private HashMap<String, EcuModule> modules = new HashMap<String, EcuModule>();
	private Statistics stats;
	private Collection<FlexRayStaticMessage> msgs;
	private HashMap<FlexRayStaticMessage, Double> nextMsgStart = new HashMap<FlexRayStaticMessage, Double>();

	@SuppressWarnings("unchecked")
	public FlexRayECU(String name, Statistics stats, Schedule schedule, @SuppressWarnings("rawtypes") Collection msgs)
	{		
		this.name = name;
		module += name;
		this.stats = stats;
		
		this.msgs = new LinkedHashSet<FlexRayStaticMessage>();
		for (Iterator<FlexRayStaticMessage> i = msgs.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage msg = (FlexRayStaticMessage) i.next();
			
			if(msg.getSender().equals(name))
			{
				this.msgs.add(msg);
			}
		}
		
		for (Iterator<FlexRayStaticMessage> i = this.msgs.iterator(); i.hasNext();)
		{
			FlexRayStaticMessage msg = (FlexRayStaticMessage) i.next();
			
			nextMsgStart.put(msg, 0.0);
		}
	}

	@Override
	public void addModule(EcuModule module)
	{
		modules.put(module.getType(), module);
	}

	@Override
	public Clk getClk()
	{
		return (Clk)modules.get(Clk.getModuleType());
	}
	
	@Override
	public void setNextMsgStart(HashMap<FlexRayStaticMessage, Double> nextMsgStart)
	{
		this.nextMsgStart = nextMsgStart;
	}

	@Override
	public void execute()
	{
		for (Iterator<Map.Entry<FlexRayStaticMessage, Double>> i = nextMsgStart.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<FlexRayStaticMessage, Double> next = (Map.Entry<FlexRayStaticMessage, Double>) i.next();
			
			if(((Clk)modules.get(Clk.getModuleType())).getTimeMillisec() >= next.getValue())
			{
				/* message is ready to send */
				SimLog.logMediumln("[SimTime: " + getClk().getTimeMicrosec() + "µs] (execute) handing message " + next.getKey().getName() + " to Com module", module);
				((Com)modules.get(Com.getModuleType())).sendMsg(next.getKey());
				stats.sendMsg_ECU(next.getKey());
				next.setValue(next.getValue()+next.getKey().getPeriod());
			}
		}
		
		for (Iterator<Map.Entry<String, EcuModule>> i = modules.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, EcuModule> module = (Map.Entry<String, EcuModule>) i.next();
			
			module.getValue().execute();
		}
	}

	@Override
	public String getName()
	{
		return name;
	}
}
