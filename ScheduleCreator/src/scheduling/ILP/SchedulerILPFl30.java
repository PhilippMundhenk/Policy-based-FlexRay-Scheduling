package scheduling.ILP;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import SchedulingInterfaces.Schedule;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticFrame;
import flexRay.FlexRayStaticMessage;

public class SchedulerILPFl30<Message> implements Scheduler<FlexRayStaticMessage>
{
	
	private int timeout=900;
	private Schedule schedule;

	@Override
	public Schedule schedule(Schedule schedule, Collection<FlexRayStaticMessage> messages) throws Exception
	{
		this.schedule = schedule;
		SlotDist slotDist = new SlotDist();
		Map<String,Integer> dists =slotDist.calculateDeadlineRequirementFL30(schedule, messages);
		Map<String,Integer> n_slots=getSlots(slotDist.calculateBandwidthRequirementFL30(schedule, messages),dists);
//		System.out.println("lowerbound: " + lowerbound(n_slots,dists));
		System.out.println("Generating problem");
		MpProblem problem =generateProblem(schedule, messages);
//		MpProblem problem = generateProblem(schedule, dists, n_slots);
		System.out.println("problem.getVariablesCount()="+problem.getVariablesCount());
		System.out.println("problem.getConstraintsCount()="+problem.getConstraintsCount());
		System.out.println("Starting ILP solver");
		long startTime= System.currentTimeMillis();
		MpResult result=solve(problem);
		long finishTime= System.currentTimeMillis();
		System.out.println("Runtime ILP: " + (finishTime-startTime));
		
		if(result==null){
			System.out.println("Problem not solvable");
			return null;
		}else{updateSchedule(result,schedule,dists,messages);
		
			this.schedule = schedule;
			return this.schedule;
		}
	}

	private int lowerbound(Map<String, Integer> n_slots,
			Map<String, Integer> dists) {
		int n_all= (int)Math.ceil(schedule.getCycleDuration()/schedule.getSlotDuration());
		int slots=0;
		for(String e: n_slots.keySet()){
			slots+=n_slots.get(e)*(double)n_all/(double)dists.get(e)*schedule.getNumberOfCycles();
		}
		
		return slots;
	}

	private Map<String, Integer> getSlots(Map<String, Integer> distsBandwidth,Map<String, Integer> dists) {
		Map<String,Integer> n_slots = new LinkedHashMap<String, Integer>();
		System.out.println("Slots per diste:");
		for(String e:distsBandwidth.keySet()){
			int slots=(int)((double)dists.get(e)/(double)distsBandwidth.get(e));
			n_slots.put(e,slots>0?slots:1);
			System.out.println("real val: " + (double)dists.get(e)/(double)distsBandwidth.get(e));
			System.out.println(e+ ": "+(slots>0?slots:1));
		}
		return n_slots;
	}

	private MpProblem generateProblem(Schedule schedule, Map<String, Integer> dists, Map<String, Integer> n_slots)
	{
		SchedulerILP scheduler=new SchedulerILP();
		int n_allC=scheduler.getN_all(schedule)*schedule.getNumberOfCycles();
		return scheduler.generateProblemDyn(schedule,dists,n_slots,n_allC);

	}
	
	private MpProblem generateProblem(Schedule schedule, Collection<FlexRayStaticMessage> messages)
	{
		SchedulerILP scheduler=new SchedulerILP();
		int n_allC=scheduler.getN_all(schedule)*schedule.getNumberOfCycles();
		return scheduler.generateProblem(schedule,messages,n_allC);

	}
	
	private MpResult solve(MpProblem problem){
		SchedulerILP scheduler=new SchedulerILP();
		return scheduler.solve(problem,timeout);
	}
	
	private void updateSchedule(MpResult result, Schedule schedule,Map<String, Integer> dists, Collection<FlexRayStaticMessage> messages)
	{
		SchedulerILP scheduler=new SchedulerILP();
		int n_all=scheduler.getN_all(schedule);
		int n_allC=scheduler.getN_all(schedule)*schedule.getNumberOfCycles();
		
		LinkedList<FlexRayStaticFrame> frames = new LinkedList<FlexRayStaticFrame>();
		for (int i = 0; i < (schedule.getSlotsPerCycle()*schedule.getNumberOfCycles()); i++) 
		{
			FlexRayStaticFrame frame = FlexRayStaticFrame.getNewNumberedFrame((FlexRaySchedule)schedule);
			frames.add(frame);
		}
		
		Map<String,List<Integer>> ecuToFrames = scheduler.updateSchedule(result, schedule, dists, n_allC);
		System.out.println("+++++++++++++++++++++++++++++\n number of frames occupied by ILP: " + frameNumber(ecuToFrames) +"\n+++++++++++++++++++++++++++++\n");
//		printFrames(ecuToFrames);
		for(String e:ecuToFrames.keySet()){
			for(int f:ecuToFrames.get(e)){
				
				int frame=(int)Math.floor((double)f/(double)n_all)*schedule.getSlotsPerCycle() + f%n_all;
				
				frames.get(frame).assignFrameToECU(e);
			}
		}
		
		PlausibilityCheck checker = new PlausibilityCheck();
		checker.check(ecuToFrames, schedule, dists, n_allC);
		
		WrapperPDU creator= new WrapperPDU();
		creator.constructWrapperPDUs30(frames, (FlexRaySchedule)schedule, scheduler.getReceivers(messages), schedule.getSlotSize());
	}
	
	private void printFrames(Map<String, List<Integer>> ecuToFrames) {
		for(String e: ecuToFrames.keySet()){
			System.out.println(e + ": " + ecuToFrames.get(e).size());
//			for(Integer i:ecuToFrames.get(e)){
//				System.out.println(i +"\n");
//			}
		}
		
	}

	private int frameNumber(Map<String, List<Integer>> ecuToFrames) {
		int frames=0;
		for(String e:ecuToFrames.keySet()){
			frames+=ecuToFrames.get(e).size();
		}
		return frames;
	}

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	@Override
	public int getOverall()
	{
		if(schedule == null)
		{
			System.err.println("Problem not solvable");
		}
			
		Integer slotsPerCycle = schedule.getSlotsPerCycle();
		Integer numberOfCycles = schedule.getNumberOfCycles();
		Integer[] frames = new Integer[slotsPerCycle*numberOfCycles];
		for (int i = 0; i < frames.length; i++)
		{
			frames[i] = 0;
		}
		
		for (int i = 0; i < schedule.getNumberOfCycles(); i++)
		{
			for (int j = 0; j < schedule.getSlotsPerCycle(); j++)
			{
				if(schedule.getMessages(i, j).size() > 0)
				{
					frames[i*schedule.getSlotsPerCycle()+j]++;
				}
			}
		}

		Integer frameCounter = 0;
		for (int i = 0; i < frames.length; i++)
		{
			if(frames[i] > 0)
			{
				frameCounter++;
			}
		}
		
		return frameCounter;
	}

}
