package simulator;

import java.util.Collection;

import simulator.components.central.Statistics;
import SchedulingInterfaces.Schedule;

public abstract class Simulator
{
	public abstract void simulate(Schedule schedule, @SuppressWarnings("rawtypes") Collection msgs, int numberOfCycles);
	public abstract Statistics getStats();
}
