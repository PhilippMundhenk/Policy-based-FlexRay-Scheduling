package tests.ILP.periods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import scheduling.ILP.SchedulerILPFl30;
import support.Log;
import tests.FlexRayTester;
import tests.ScheduleCreatorConfig;
import verify.VerifyTask;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayFileInput.FileFormat;
import flexRay.FlexRayFileInput.ReductionMode;
import flexRay.FlexRayStaticMessage;

public class Test {

	private static final String module = "PeriodVariations_ILP";
	
	private static final Integer timeout_min = 1;
	
	private static LinkedHashSet<String> results = new LinkedHashSet<String>();
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
		if(args.length == 1 && args[0].equals("help"))
		{
			System.out.println("call with java -jar x.jar messagesPerCluster testcaseName pathToTextCaseFiles pathToOutputFile verificationOn");
			System.out.println("set either all or no parameters!");
			System.exit(0);
		}
		
		Integer messagesPerCluster;
		try
		{
			messagesPerCluster = Integer.parseInt(args[0]);
		}
		catch(Exception e)
		{
			messagesPerCluster = 100;
		}
		
		System.out.println("messagesPerCluster="+messagesPerCluster);
		
		String testcaseName;
		try
		{
			testcaseName = args[1];
		}
		catch(Exception e)
		{
			testcaseName = "Test1_Periods1";
		}
		
		System.out.println("testcaseName="+testcaseName);
		
		String pathToTextCaseFiles;
		try
		{
			pathToTextCaseFiles = args[2];
		}
		catch(Exception e)
		{
			pathToTextCaseFiles = "/../ScheduleCreator/testcases/paper/periods/";
		}
		
		System.out.println("pathToTextCaseFiles="+pathToTextCaseFiles);
		
		String outputFile;
		try
		{
			outputFile = args[3];
		}
		catch(Exception e)
		{
			outputFile = "output/Policy_ILP_FR301_Periods.txt";
		}
		
		System.out.println("outputFile="+outputFile);
		
		Integer verificationOn;
		try
		{
			verificationOn = Integer.parseInt(args[4]);
		}
		catch(Exception e)
		{
			verificationOn = 1;
		}
		
		System.out.println("verificationOn="+verificationOn);
		
		Log.initLog();
		Log.setLOG_EXCEPTIONS(new Object[][]{{"Example", Log.LOG_LEVEL_TRACE}});
		
//		File file = new File(outputPath+"/Policy_ILP_FR301_Periods.txt");
		File file = new File(outputFile);
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		double avgRuntime = 0;
		Integer numberOfTestsStarted = 0;
		Integer numberOfTestsDone = 0;
//		for (int j = firstIndex; j <= lastIndex; j++)
//		{
			LinkedHashSet<FlexRayTester> testsToRun = new LinkedHashSet<FlexRayTester>();
			
			String path = pathToTextCaseFiles+messagesPerCluster+"/";
			testsToRun.add(new TestCase(testcaseName, FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new TestCase("Test"+j+"_Periods1", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new TestCase("Test"+j+"_Periods_Original", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new TestCase("Test"+j+"_Periods2", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new TestCase("Test"+j+"_Periods3", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new TestCase("Test"+j+"_Periods4", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new TestCase("Test"+j+"_Periods5", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
//			testsToRun.add(new TestCase("Test"+j+"_Periods6", FileFormat.MULTI_MODE, ReductionMode.POLICY, path));
		
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
//				try
//				{

					Log.logLowln("starting test "+test.getTestCaseFile(), module);
					
					/* load test case */
					FlexRayTester testCase = test;
					/* load scheduler */
					Scheduler<FlexRayStaticMessage> scheduler = new SchedulerILPFl30<FlexRayStaticMessage>();
					
					long start = System.currentTimeMillis();
//					results.put(test.getTestCaseFile(), 0);
					/* start test */
					scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
					long finish = System.currentTimeMillis()-start;
					bw.write("time:"+finish+"ms\r\n"+testcaseName+","+scheduler.getOverall());
					
					System.out.println("ILP finished");
					System.out.println("result:");
					System.out.println(testcaseName+","+scheduler.getOverall());
					
					if(verificationOn == 1)
					{
						System.out.println("verifying...");
						VerifyTask t = new VerifyTask(testCase.getSchedule(), testCase.getMsgs());
						ExecutorService executor = Executors.newSingleThreadExecutor();
						executor.submit(t);
						executor.awaitTermination(300, TimeUnit.SECONDS);
						executor.shutdown();
						HashMap<String, Double> delays = t.getDelays();
						if(delays != null)
						{
							System.out.println("verification succeeded...");
							for (Iterator<Map.Entry<String, Double>> it = delays.entrySet().iterator(); it.hasNext();)
							{
								Map.Entry<String, Double> entry = (Map.Entry<String, Double>) it.next();
								
								System.out.println(entry.getKey()+":"+entry.getValue());
								
							}
						}
						else
						{
							System.out.println("verification failed");
						}
					}
					
//					ScheduleTask task = new ScheduleTask(scheduler, testCase);
//					final Future<String> f = executor.submit(task);
//					canceller.schedule(new Callable<Void>() {
//						public Void call() throws Exception {
//							System.out.println("thread exceeded time limit, exiting now...");
//							f.cancel(true);
//							return null;
//						}
//					}, timeout_min, TimeUnit.MINUTES);
//					System.out.println("going to sleep");
//					Thread.sleep(timeout_min*60*1000);
//					System.out.println("shutting down now");
//					executor.shutdownNow();
//					Thread.sleep(100);
//					System.exit(0);
//					futures.put(f, testCase.getTestCaseFile());
//					System.out.println("added task "+testCase.getTestCaseFile());
//					scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
					
//					avgRuntime += System.currentTimeMillis() - start;
//					numberOfTestsStarted++;
					
//					System.out.println("numberOfTestsDone = "+numberOfTestsDone);
//					System.out.println("(lastIndex-firstIndex+1)*testsToRun.size() = "+(lastIndex-firstIndex+1)*testsToRun.size());
					
//					System.out.println("checking if there is space for new threads...");
//					while(numberOfTestsDone+numberOfThreads <= numberOfTestsStarted)
//					{
//						for (Iterator<Map.Entry<Future<String>, String>> it = futures.entrySet().iterator(); it.hasNext();)
//						{
//							Map.Entry<Future<String>, String> entry = (Map.Entry<Future<String>, String>) it.next();
//							
//							if(entry.getKey().isDone())
//							{
//								numberOfTestsDone++;
//								String r = entry.getValue()+",0";
//								if(!entry.getKey().isCancelled())
//								{
//									r = entry.getKey().get();									
//								}
//								results.add(r);
//								bw.write(r+"\r\n");
//								bw.flush();
//								it.remove();
//							}
//						}
//						if(numberOfTestsDone == (lastIndex-firstIndex+1)*testsToRun.size())
//						{
//							System.exit(0);
//						}
//						Thread.sleep(100);
//					}
//	//				Visualizer vis = new Visualizer();
//	//				vis.visualize(testCase.getSchedule());
//					
//					results.put(test.getTestCaseFile(), scheduler.getOverall());
//					
//		//			((FlexRaySchedule)testCase.getSchedule()).exportToFIBEX(new File("").getAbsolutePath().concat("/../../examples/paper/testoutput_"+test.getClass().getSimpleName()+".xml"), "FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1");
//					
//					Log.logLowln("program finished", module);
//				}
//				catch(Exception e)
//				{
//					System.err.println("Test failed. Skipping test...");
//					e.printStackTrace();
//				}
				
//				bw.write(test.getTestCaseFile()+","+results.get(test.getTestCaseFile())+"\r\n");
//				bw.flush();
//				
//				Log.logLowln("results:", module);
//				for (Iterator<Map.Entry<String, Integer>> k = results.entrySet().iterator(); k.hasNext();)
//				{
//					Map.Entry<String, Integer> entry= (Map.Entry<String, Integer>) k.next();
//					
//					Log.logLowln(entry.getKey()+","+entry.getValue(), module);
//				}
			}
			
			bw.close();
			fw.close();
		}
		
//		avgRuntime /= numberOfTestsRun;
//		
//		Log.logLowln("all tests done", module);
//		Log.logLowln("average runtime per scheduling run: "+avgRuntime+"ms", module);
//		Log.logLowln("results:", module);
//		for (Iterator<Map.Entry<String, Integer>> i = results.entrySet().iterator(); i.hasNext();)
//		{
//			Map.Entry<String, Integer> entry= (Map.Entry<String, Integer>) i.next();
//			
//			Log.logLowln(entry.getKey()+","+entry.getValue(), module);
//		}
		
//		Integer finished = 0;
//		while(finished!=numberOfTestsStarted)
//		{
//			Iterator<Future<String>> it = futures.iterator();
//			while(it.hasNext())
//			{
//				Future<String> f = it.next();
//				try
//				{
//					String r = f.get(1, TimeUnit.SECONDS);
//					results.add(r);
//					bw.write(r+"\r\n");
//					bw.flush();
//					finished++;
//					it.remove();
//				}
//				catch(Exception e)
//				{
//					
//				}
//			}
//		}
		
//		for (Iterator<String> it = results.iterator(); it.hasNext();) {
//			String r = (String) it.next();
//			
//			bw.write(r+"\r\n");
//			bw.flush();
//		}
//	}
}
