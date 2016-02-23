package verify;

import java.util.HashMap;
import java.util.concurrent.Callable;

import SchedulingInterfaces.Schedule;
import flexRay.FlexRayStaticMessageCollection;

public class VerifyTask implements Callable<String>
{
	private Schedule schedule;
	private FlexRayStaticMessageCollection messages;
	private HashMap<String, Double> delays;
	
	public VerifyTask(Schedule schedule, FlexRayStaticMessageCollection messages)
	{
		this.schedule = schedule;
		this.messages = messages;
	}

	@Override
	public String call() throws Exception
	{
		Verifier v = new Verifier(schedule, messages);
		v.verify();
		this.delays = v.getDelays();
		return null;
	}
	
	public HashMap<String, Double> getDelays()
	{
		return delays;
	}
}
