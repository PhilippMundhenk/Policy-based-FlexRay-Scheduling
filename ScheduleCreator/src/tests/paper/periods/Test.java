package tests.paper.periods;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import support.Log;
import tests.FlexRayTester;
import tests.ScheduleCreatorConfig;
import verify.VerifyTask;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayFileInput.FileFormat;
import flexRay.FlexRayFileInput.ReductionMode;
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
		
		double avgRuntime = 0;
		Integer numberOfTestsRun = 0;
		for (int j = 1; j <= 100; j++)
		{
			LinkedHashSet<FlexRayTester> testsToRun = new LinkedHashSet<FlexRayTester>();
		
//			String path = "/../ScheduleCreator/testcases/";
//			testsToRun.add(new Testcase("minimal_multimode", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
			String path = "/../ScheduleCreator/testcases/paper/periods/100/";
//			testsToRun.add(new Testcase("Test"+j+"_Periods1", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
			testsToRun.add(new Testcase("Test"+j+"_Periods_Original", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new Testcase("Test"+j+"_Periods2", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new Testcase("Test"+j+"_Periods3", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new Testcase("Test"+j+"_Periods4", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new Testcase("Test"+j+"_Periods5", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new Testcase("Test"+j+"_Periods6", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
		
			for (Iterator<FlexRayTester> i = testsToRun.iterator(); i.hasNext();)
			{
				FlexRayTester test = (FlexRayTester) i.next();

				try
				{
					/* load test case */
					FlexRayTester testCase = test;
					/* load scheduler */
					Scheduler<FlexRayStaticMessage> scheduler = ScheduleCreatorConfig.SCHEDULER_UNDER_TEST;
				
					Log.logLowln("launching test "+test.getTestCaseFile(), module);
					
					long start = System.currentTimeMillis();
					
					System.out.println("number of messages: "+testCase.getMsgs().getMessages().size());
					
					/* start test */
					scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
					avgRuntime += System.currentTimeMillis() - start;
					numberOfTestsRun++;
				
		//			Visualizer vis = new Visualizer();
		//			vis.visualize(testCase.getSchedule());
					
					results.put(test.getTestCaseFile(), scheduler.getOverall());
					Log.logLowln(test.getTestCaseFile()+": "+scheduler.getOverall(), module);
					
		//			((FlexRaySchedule)testCase.getSchedule()).exportToFIBEX(new File("").getAbsolutePath().concat("/../../examples/paper/testoutput_"+test.getClass().getSimpleName()+".xml"), "FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1");
					
					Log.logLowln("program finished", module);
				
		//			Simulator sim = new FlexRaySimulator_continuousSlots();
		//			sim.simulate(testCase.getSchedule(), testCase.getMsgs().getMessages(), ScheduleCreatorConfig.SIMULATOR_NUMBER_OF_CYCLES);
		//			sim.getStats().printStatistics();
		//			sim.getStats().matlabExport("debug\\exportedStatistics.m", ScheduleCreatorConfig.GENERATE_DIAGRAM_COMMANDS);
		
					VerifyTask t = new VerifyTask(testCase.getSchedule(), testCase.getMsgs());
					ExecutorService executor = Executors.newSingleThreadExecutor();
					executor.submit(t);
					executor.awaitTermination(300, TimeUnit.SECONDS);
					executor.shutdown();
					HashMap<String, Double> delays = t.getDelays();
					if(delays != null)
					{
						for (Iterator<Map.Entry<String, Double>> it = delays.entrySet().iterator(); it.hasNext();)
						{
							Map.Entry<String, Double> entry = (Map.Entry<String, Double>) it.next();
							
							System.out.println(entry.getKey()+":"+entry.getValue());
							
						}
						System.exit(0);
					}
					else
					{
						System.out.println("Verification failed, trying next testcase");
					}
					
	//				Verifier v = new Verifier(testCase.getSchedule(), testCase.getMsgs());
	//				v.verify();
				}
				catch(Exception e)
				{
					results.put(test.getTestCaseFile(), 0);
				}
				
//				Log.logLowln("delays:", module);
//				for (Iterator<FlexRayStaticMessage> it = testCase.getMsgs().getMessages().iterator(); it.hasNext();)
//				{
//					FlexRayStaticMessage msg = (FlexRayStaticMessage) it.next();
//					
//					Log.logLowln(msg.getName()+": period="+msg.getPeriod()+" size= "+msg.getSize()+" delay="+v.getDelays().get(msg.getName()), module);
//				}
			}
		}
		
		Log.logLowln("verification finished", module);
		
		avgRuntime /= numberOfTestsRun;
		
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
