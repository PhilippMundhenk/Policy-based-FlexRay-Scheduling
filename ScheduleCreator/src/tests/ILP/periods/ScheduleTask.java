package tests.ILP.periods;

import java.util.concurrent.Callable;

import tests.FlexRayTester;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayStaticMessage;

public class ScheduleTask implements Callable<String>
{
	private Scheduler<FlexRayStaticMessage> scheduler;
	private FlexRayTester testCase;
	
	public ScheduleTask(Scheduler<FlexRayStaticMessage> scheduler, FlexRayTester testCase)
	{
		this.scheduler = scheduler;
		this.testCase = testCase;
	}
	
	public String call() throws Exception {
		String result = ""+testCase.getTestCaseFile()+","+0;
		try
		{
			scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
			result = ""+testCase.getTestCaseFile()+","+scheduler.getOverall();
			System.out.println("ILP finished with result: "+result);
		}
		catch(Exception e)
		{
			System.out.println("ILP failed with "+e.getClass().getSimpleName());
		}
		return result;
	}
	
	public Scheduler<FlexRayStaticMessage> getScheduler()
	{
		return scheduler;
	}
	
	public FlexRayTester getTestCase()
	{
		return testCase;
	}
}
