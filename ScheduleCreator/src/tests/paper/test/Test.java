package tests.paper.test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import support.Log;
import tests.FlexRayTester;
import tests.ScheduleCreatorConfig;
import verify.Verifier;
import visualization.Visualizer;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayStaticMessage;

public class Test
{
	private static final String module = "SizeVariations";	
	
	private static LinkedHashSet<FlexRayTester> testsToRun = new LinkedHashSet<FlexRayTester>();
	private static LinkedHashMap<String, Integer> results = new LinkedHashMap<String, Integer>();
	
	/**
	 * This is the main entry point of the system. The test case is set up here
	 * 
	 * @param args
	 * command line parameters
	 * 
	 * @throws Exception
	 * exceptions of all modules
	 */
	public static void main(String[] args) throws Exception
	{
		Log.initLog();
		Log.setLOG_EXCEPTIONS(new Object[][]{{"Example", Log.LOG_LEVEL_TRACE}});
		
		testsToRun.add(new Test_PeriodMess_LargeSizes());
		
		double avgRuntime = 0;
		
		for (Iterator<FlexRayTester> i = testsToRun.iterator(); i.hasNext();)
		{
			FlexRayTester test = (FlexRayTester) i.next();
			
			/* load test case */
			FlexRayTester testCase = test;
			/* load scheduler */
			Scheduler<FlexRayStaticMessage> scheduler = ScheduleCreatorConfig.SCHEDULER_UNDER_TEST;
		
			Log.logLowln("launching test "+test.getClass().getCanonicalName(), module);
			
			long start = System.currentTimeMillis();
			
			System.out.println("number of messages: "+testCase.getMsgs().getMessages().size());
			
			/* start test */
			scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
			avgRuntime += System.currentTimeMillis() - start;
			
			Visualizer vis = new Visualizer();
			vis.visualize(testCase.getSchedule());
			
			results.put(test.getClass().getSimpleName(), scheduler.getOverall());
			Log.logLowln(test.getClass().getSimpleName()+": "+scheduler.getOverall(), module);
			
//			((FlexRaySchedule)testCase.getSchedule()).exportToFIBEX(new File("").getAbsolutePath().concat("/../../examples/paper/testoutput_"+test.getClass().getSimpleName()+".xml"), "FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1");
			
			Log.logLowln("program finished", module);
			
//			Simulator sim = new FlexRaySimulator_continuousSlots();
//			sim.simulate(testCase.getSchedule(), testCase.getMsgs().getMessages(), ScheduleCreatorConfig.SIMULATOR_NUMBER_OF_CYCLES);
//			sim.getStats().printStatistics();
//			sim.getStats().matlabExport("debug\\exportedStatistics.m", ScheduleCreatorConfig.GENERATE_DIAGRAM_COMMANDS);

			Verifier v = new Verifier(testCase.getSchedule(), testCase.getMsgs());
			v.verify();
			
			Log.logLowln("delays:", module);
			for (Iterator<FlexRayStaticMessage> it = testCase.getMsgs().getMessages().iterator(); it.hasNext();)
			{
				FlexRayStaticMessage msg = (FlexRayStaticMessage) it.next();
				
				Log.logLowln(msg.getName()+": period="+msg.getPeriod()+" size= "+msg.getSize()+" delay="+v.getDelays().get(msg.getName()), module);
			}
		}
		
		Log.logLowln("verification finished", module);
		
		avgRuntime /= testsToRun.size();
		
		Log.logLowln("all tests done", module);
		Log.logLowln("average runtime per scheduling run: "+avgRuntime+"ms", module);
		Log.logLowln("results:", module);
		for (Iterator<Map.Entry<String, Integer>> i = results.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, Integer> entry= (Map.Entry<String, Integer>) i.next();
			
			Log.logLowln(entry.getKey()+","+entry.getValue(), module);
		}
	}
	
	
}
