package tests.ILP.sizes;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import scheduling.ILP.SchedulerILPFl30;
import support.Log;
import tests.FlexRayTester;
import tests.ScheduleCreatorConfig;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayFileInput.FileFormat;
import flexRay.FlexRayFileInput.ReductionMode;
import flexRay.FlexRayStaticMessage;

public class Test {

	private static final String module = "PeriodVariations_ILP";
	
	private static LinkedHashMap<String, Integer> results = new LinkedHashMap<String, Integer>();
//	private static LinkedHashMap<String, Integer> deadlineViolations = new LinkedHashMap<String, Integer>();
	
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
//		Log.initLog();
//		Log.setLOG_EXCEPTIONS(new Object[][]{{"Example", Log.LOG_LEVEL_TRACE}});
//		
//		/* load test case */
//		FlexRayTester testCase = new TestCase();
//		/* load scheduler */
//		Scheduler<FlexRayStaticMessage> scheduler = new SchedulerILPFl21<FlexRayStaticMessage>();
//		
//		/* start test */
//		scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
//		
//		((FlexRaySchedule)testCase.getSchedule()).print();
//		
//		if (ScheduleCreatorConfig.VISUALIZATION)
//		{
//			Visualizer vis = new Visualizer();
//			vis.visualize(testCase.getSchedule());
//		}
//		
//		Simulator sim = new FlexRaySimulator_continuousSlots();
//		sim.simulate(testCase.getSchedule(), testCase.getMsgs().getMessages(), ScheduleCreatorConfig.SIMULATOR_NUMBER_OF_CYCLES);
//		sim.getStats().printStatistics();
//		sim.getStats().matlabExport("debug\\exportedStatistics.m", ScheduleCreatorConfig.GENERATE_DIAGRAM_COMMANDS);
//		Log.logLowln("program finished", module);
		
		Log.initLog();
		Log.setLOG_EXCEPTIONS(new Object[][]{{"Example", Log.LOG_LEVEL_TRACE}});
		
		double avgRuntime = 0;
		Integer numberOfTestsRun = 0;
		for (int j = 1; j <= 30; j++)
		{
			LinkedHashSet<FlexRayTester> testsToRun = new LinkedHashSet<FlexRayTester>();
			
			testsToRun.add(new TestCase("Test"+j+"_Sizes1", FileFormat.MULTI_MODE, ReductionMode.POLICY));
			testsToRun.add(new TestCase("Test"+j+"_Sizes2", FileFormat.MULTI_MODE, ReductionMode.POLICY));
			testsToRun.add(new TestCase("Test"+j+"_Sizes3", FileFormat.MULTI_MODE, ReductionMode.POLICY));
			testsToRun.add(new TestCase("Test"+j+"_Sizes4", FileFormat.MULTI_MODE, ReductionMode.POLICY));
			testsToRun.add(new TestCase("Test"+j+"_Sizes5", FileFormat.MULTI_MODE, ReductionMode.POLICY));
			testsToRun.add(new TestCase("Test"+j+"_Sizes6", FileFormat.MULTI_MODE, ReductionMode.POLICY));
			testsToRun.add(new TestCase("Test"+j+"_Sizes7", FileFormat.MULTI_MODE, ReductionMode.POLICY));
		
	//		testsToRun.add(new Paper_2Periods());
	//		testsToRun.add(new Paper_3Periods());
	//		testsToRun.add(new Paper_5Periods());
	//		testsToRun.add(new Paper_7Periods());
	//		testsToRun.add(new Paper_9Periods());
	//		testsToRun.add(new Paper_11Periods());
	//		testsToRun.add(new Paper_13Periods());
	//		testsToRun.add(new Paper_15Periods());
			
	//		testsToRun.add(new TestCase3());
//			testsToRun.add(new Test_PeriodMess_LargeSizes());
			
			ScheduleCreatorConfig.printConfig();
			
			for (Iterator<FlexRayTester> i = testsToRun.iterator(); i.hasNext();)
			{
				FlexRayTester test = (FlexRayTester) i.next();
				
				/* load test case */
				FlexRayTester testCase = test;
				/* load scheduler */
//				Scheduler<FlexRayStaticMessage> scheduler = new SchedulerILPFl21<FlexRayStaticMessage>();
				Scheduler<FlexRayStaticMessage> scheduler = new SchedulerILPFl30<FlexRayStaticMessage>();
				
				try
				{
					Log.logLowln("starting "+testCase.getTestCaseFile(), module);
					long start = System.currentTimeMillis();
					/* start test */
					scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
					avgRuntime += System.currentTimeMillis() - start;
					numberOfTestsRun++;
					
		//			Visualizer vis = new Visualizer();
		//			vis.visualize(testCase.getSchedule());
					
					results.put(test.getTestCaseFile(), scheduler.getOverall());
					Log.logLowln(test.getTestCaseFile()+": "+scheduler.getOverall(), module);
				}
				catch(Exception e)
				{
					System.err.println("Test failed. Skipping test...");
					e.printStackTrace();
					results.put(test.getTestCaseFile(), 0);
				}
	//			Simulator sim = new FlexRaySimulator_continuousSlots();
	//			sim.simulate(testCase.getSchedule(), testCase.getMsgs().getMessages(), ScheduleCreatorConfig.SIMULATOR_NUMBER_OF_CYCLES);
	//			sim.getStats().printStatistics();
	//			sim.getStats().matlabExport("debug\\exportedStatistics.m", ScheduleCreatorConfig.GENERATE_DIAGRAM_COMMANDS);
				
//				Verifier v = new Verifier(testCase.getSchedule(), testCase.getMsgs());
//				v.verify();
//				
//				Log.logLowln("delays:", module);
//				for (Iterator<FlexRayStaticMessage> it = testCase.getMsgs().getMessages().iterator(); it.hasNext();)
//				{
//					FlexRayStaticMessage msg = (FlexRayStaticMessage) it.next();
//					
//					Log.logLowln(msg.getName()+": "+v.getDelays().get(msg.getName()), module);
//				}
				
	//			deadlineViolations.put(test.getClass().getSimpleName(), (new Verifier(testCase.getSchedule(), testCase.getMsgs())).verify());
				
	//			((FlexRaySchedule)testCase.getSchedule()).exportToFIBEX(new File("").getAbsolutePath().concat("/../../examples/paper/testoutput_"+test.getClass().getSimpleName()+".xml"), "FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1");
				
				Log.logLowln("program finished", module);
				
				Log.logLowln("results:", module);
				for (Iterator<Map.Entry<String, Integer>> k = results.entrySet().iterator(); k.hasNext();)
				{
					Map.Entry<String, Integer> entry= (Map.Entry<String, Integer>) k.next();
					
					Log.logLowln(entry.getKey()+","+entry.getValue(), module);
				}
			}
		}
		
		avgRuntime /= numberOfTestsRun;
		
		Log.logLowln("all tests done", module);
		Log.logLowln("average runtime per scheduling run: "+avgRuntime+"ms", module);
		Log.logLowln("results:", module);
		for (Iterator<Map.Entry<String, Integer>> i = results.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, Integer> entry= (Map.Entry<String, Integer>) i.next();
			
			Log.logLowln(entry.getKey()+","+entry.getValue(), module);
		}
//		Log.logLowln(module);
//		Log.logLowln("deadline violations:", module);
//		for (Iterator<Map.Entry<String, Integer>> i = deadlineViolations.entrySet().iterator(); i.hasNext();)
//		{
//			Map.Entry<String, Integer> entry= (Map.Entry<String, Integer>) i.next();
//			
//			Log.logLowln(entry.getKey()+","+entry.getValue(), module);
//		}
	}
}
