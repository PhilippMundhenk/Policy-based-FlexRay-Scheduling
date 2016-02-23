package tests;

import java.io.File;

import simulator.FlexRaySimulator_continuousSlots;
import simulator.Simulator;
import support.Log;
import visualization.Visualizer;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticMessage;

public class Test
{
	private static final String module = "Test";	
	
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
		
		ScheduleCreatorConfig.printConfig();
		
		/* load test case */
		FlexRayTester testCase = ScheduleCreatorConfig.TEST_TO_RUN;
		/* load scheduler */
		Scheduler<FlexRayStaticMessage> scheduler = ScheduleCreatorConfig.SCHEDULER_UNDER_TEST;
		
		/* start test */
		scheduler.schedule(testCase.getSchedule(), testCase.getMsgs().getMessages());
		
//		((FlexRaySchedule)testCase.getSchedule()).print();
		
		if (ScheduleCreatorConfig.VISUALIZATION)
		{
			Visualizer vis = new Visualizer();
			vis.visualize(testCase.getSchedule());
//			PolicyVisualizer vis = new PolicyVisualizer();
//			vis.visualize(testCase.getSchedule());
		}
		
		((FlexRaySchedule)testCase.getSchedule()).exportToFIBEX(new File("").getAbsolutePath().concat("/../../examples/testoutput.xml"), "FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1");
		//((FlexRaySchedule)testCase.getSchedule()).exportToFIBEX("FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1");
		
//		//debug: test of FIBEX importer:
//		/* loads and parses XML document if parser for correct version is available */
//		FIBEXFile file = new FIBEXFile("C:\\Philipp\\others\\examples\\testinput_1ECU_1Slot.xml", "");
//		
//		if(file.getDocument() == null)
//		{
//			Log.logLowln("no document loaded", module);
//		}
//		else if(file.getDocumentAsInterface("fibex.document.FIBEXDocumentBasicInteraction") != null)
//		{
//			Log.logLowln("document loaded", module);
//			FIBEXBasicInterface doc = file.getDocumentAsInterface("fibex.document.FIBEXDocumentBasicInteraction");
//			Log.logTraceln(((FIBEXDocumentBasicInteraction)doc).getProjectName(), module);
//			for (Iterator<FibexCluster> i = ((FIBEXDocumentBasicInteraction)doc).getClusters().iterator(); i.hasNext();)
//			{
//				FibexCluster cluster = (FibexCluster) i.next();
//				
//				Log.logTraceln("Cluster: " + cluster.getName() + " of type " + cluster.getClusterType(), module);
//				Log.logTraceln("Cluster data:", module);
//				Log.logTraceln("number of static slots: " + ((FlexRayCluster)cluster).getNumberOfStaticSlots(), module);
//				Log.logTraceln("number of mini slots: " + ((FlexRayCluster)cluster).getNumberOfMinislots(), module);
//				Log.logTraceln("Cycle duration (us/macroticks): "+((FlexRayCluster)cluster).getCycleDuration_micros()+"/"+((FlexRayCluster)cluster).getCycleDuration_Macroticks(), module);
//				Log.logTraceln("Minislot duration (us/macroticks): "+((FlexRayCluster)cluster).getMinislotDuration_micros()+"/"+((FlexRayCluster)cluster).getMinislotDuration_Macroticks(), module);
//				Log.logTraceln("NIT duration (us/macroticks): "+((FlexRayCluster)cluster).getNITDuration_micros()+"/"+((FlexRayCluster)cluster).getNITDuration_Macroticks(), module);
//				Log.logTraceln("Symbol Window duration (us/macroticks): "+((FlexRayCluster)cluster).getSymbolWindowDuration_micros()+"/"+((FlexRayCluster)cluster).getSymbolWindowDuration_Macroticks(), module);
//				Log.logTraceln("static slot duration (us/macroticks): "+((FlexRayCluster)cluster).getStaticSlotDuration_micros()+"/"+((FlexRayCluster)cluster).getStaticSlotDuration_Macroticks(), module);
//				Log.logTraceln("Bit Duration (us): " + ((FlexRayCluster)cluster).getBitDuration_micros(), module);
//				Log.logTraceln("Macrotick Duration (us): " + ((FlexRayCluster)cluster).getMacrotickDuration_micros(), module);
//				Log.logTraceln("Contains channels: ", module);
//				
//				for (Iterator<FibexChannel> j = cluster.getChannels().iterator(); j.hasNext();)
//				{
//					FibexChannel channel = (FibexChannel)j.next();
//					
//					Log.logTraceln(channel.getName(), module);
//					Log.logTraceln("FlexRayChannelName: " + ((FlexRayChannel)channel).getFlexRayChannelName(), module);
//					Log.logTraceln("Frames: ", module);
//					if(channel.getFrames() == null)
//					{
//						Log.logTraceln("channel does not contain frames", module);
//					}
//					else
//					{
//						for (Iterator<FibexFrame> m = channel.getFrames().iterator(); m.hasNext();)
//						{
//							FibexFrame frame = (FibexFrame) m.next();
//							
//							Log.logTraceln("name: "+frame.getName(), module);
//							Log.logTraceln("length: "+frame.getLength_Byte(), module);
//							Log.logTraceln("base cycle: "+frame.getBaseCycle(), module);
//							Log.logTraceln("cycle repetition: "+frame.getCycleRepetition(), module);
//							Log.logTraceln("slot: "+frame.getSlot(), module);
//							Log.logTraceln("signals: ", module);
//							for (Iterator<FibexMessage> n = frame.getMessages().iterator(); n.hasNext();)
//							{
//								FibexMessage signal = (FibexMessage) n.next();
//								
//								Log.logTraceln("name: "+signal.getName(), module);
//								Log.logTraceln("length: "+signal.getLength_Bit(), module);
//								Log.logTraceln("offset: "+signal.getOffset_Bit(), module);
//							}
//						}
//					}
//				}
//			}
//			
//		}
		
//		Simulator sim = new FlexRaySimulator_continuousSlots();
//		sim.simulate(testCase.getSchedule(), testCase.getMsgs().getMessages(), ScheduleCreatorConfig.SIMULATOR_NUMBER_OF_CYCLES);
//		sim.getStats().printStatistics();
//		sim.getStats().matlabExport("debug\\exportedStatistics.m", ScheduleCreatorConfig.GENERATE_DIAGRAM_COMMANDS);
		Log.logLowln("program finished", module);
		
		System.out.println("getOverall="+scheduler.getOverall());
	}
}
