package scheduling.ILP;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import support.Log;
import SchedulingInterfaces.Schedule;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticFrame;
import flexRay.FlexRayStaticMessage;

public class SchedulerILPFl21<Message> implements Scheduler<FlexRayStaticMessage>
{
	
	private int timeout=300;
	private static final String module = "SchedulerILPFl21";
	private Schedule schedule;

	@Override
	public Schedule schedule(Schedule schedule, Collection<FlexRayStaticMessage> messages) throws Exception
	{
		SlotDist slotDist = new SlotDist();
		Log.logTrace("calculating slot dists", module);
		System.out.println("calculating slot dists");
		Map<String,Integer> dists =slotDist.calculateSlotDistanceFL21(schedule, messages);
		Log.logTrace("generating problem", module);
		System.out.println("generating problem");
		MpProblem problem =generateProblem(schedule, dists);
		Log.logTrace("solving ILP", module);
		System.out.println("solving ILP");
		MpResult result=solve(problem);
		if(result==null){
			Log.logTrace("problem not feasible", module);
			System.out.println("problem not feasible");
			return null;
		}
		Log.logTrace("starting plausibility check", module);
		System.out.println("starting plausibility check");
		updateSchedule(result,schedule,dists, messages);
		
		this.schedule = schedule;
		return this.schedule;
	}

	private MpProblem generateProblem(Schedule schedule, Map<String, Integer> dists)
	{
		SchedulerILP scheduler=new SchedulerILP();
		int n=schedule.getSlotsPerCycle();
		return scheduler.generateProblem(schedule,dists,n);

	}
	
	private MpResult solve(MpProblem problem){
		SchedulerILP scheduler=new SchedulerILP();
		return scheduler.solve(problem,timeout);
	}
	
	private void updateSchedule(MpResult result, Schedule schedule,Map<String, Integer> dists, Collection<FlexRayStaticMessage> messages)
	{
		SchedulerILP scheduler=new SchedulerILP();
		int n=schedule.getSlotsPerCycle();
		
		LinkedList<FlexRayStaticFrame> frames = new LinkedList<FlexRayStaticFrame>();
		for (int i = 0; i < (schedule.getSlotsPerCycle()*schedule.getNumberOfCycles()); i++) 
		{
			FlexRayStaticFrame frame = FlexRayStaticFrame.getNewNumberedFrame((FlexRaySchedule)schedule);
			frames.add(frame);
		}
		
		Map<String,List<Integer>> ecuToFrames = scheduler.updateSchedule(result, schedule, dists, n);
		for(String e:ecuToFrames.keySet()){
			for(int f:ecuToFrames.get(e)){
				
				frames = setFramesForAllCycles(frames, f, e, schedule);
			}
		}
		
		PlausibilityCheck checker = new PlausibilityCheck();
		checker.check(ecuToFrames, schedule, dists, n);
		
		WrapperPDU creator= new WrapperPDU();
		creator.constructWrapperPDUs21(frames, (FlexRaySchedule)schedule, scheduler.getReceivers(messages), schedule.getSlotSize());
	}
	
	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}
	
	
	private LinkedList<FlexRayStaticFrame> setFramesForAllCycles(LinkedList<FlexRayStaticFrame> frames, int slotNo, String ecu, Schedule schedule)
	{
		frames.get(slotNo).assignFrameToECU(ecu);
		for (int i = 0; i < schedule.getNumberOfCycles(); i++)
		{
			frames.get(i*schedule.getSlotsPerCycle()+slotNo).assignFrameToECU(ecu);
		}
		
		return frames;
	}

	@Override
	public int getOverall()
	{
		Integer[] slots = new Integer[schedule.getSlotsPerCycle()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = 0;
		}
		
		for (int i = 0; i < schedule.getNumberOfCycles(); i++)
		{
			for (int j = 0; j < schedule.getSlotsPerCycle(); j++)
			{
				if(schedule.getMessages(i, j).size() > 0)
				{
					slots[j]++;
				}
			}
		}

		Integer slotCounter = 0;
		for (int i = 0; i < slots.length; i++)
		{
			if(slots[i] > 0)
			{
				slotCounter++;
			}
		}
		
		return slotCounter;
	}
}
