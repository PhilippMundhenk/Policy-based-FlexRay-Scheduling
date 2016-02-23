package simulator.components.distributed;

import java.util.HashMap;

import flexRay.FlexRayStaticMessage;
import simulator.components.distributed.ecuModules.Clk;
import simulator.components.distributed.ecuModules.EcuModule;

public abstract class ECU
{	
	public abstract void addModule(EcuModule module);
	public abstract Clk getClk();
	public abstract void execute();
	public abstract String getName();
	public abstract void setNextMsgStart(HashMap<FlexRayStaticMessage, Double> nextMsgStart);
}
