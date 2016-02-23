package tests;

import scheduling.FR301_WorstCase_continuousSlots;
import support.Log;
import SchedulingInterfaces.Scheduler;
import flexRay.FlexRayStaticMessage;


public class ScheduleCreatorConfig
{
	public static final String module = "Conf";
	public static final int LOG_LEVEL = support.Log.LOG_LEVEL_LOW;
	public static Scheduler<FlexRayStaticMessage> SCHEDULER_UNDER_TEST = new FR301_WorstCase_continuousSlots();
	public static final FlexRayTester TEST_TO_RUN = new TestCase3();
	public static final Boolean VISUALIZATION = true;
	public static final int SIMULATOR_NUMBER_OF_CYCLES = 200;
	public static final Boolean GENERATE_DIAGRAM_COMMANDS = false;
	public static final int WRAPPER_PDU_LENGTH_BYTE = 42;  //needs to be exact divider of slot length! set to slot length to disable splitting
	
	public static void printConfig()
	{
		Log.logLowln("LOG_LEVEL = "+LOG_LEVEL, module);
		Log.logLowln("SCHEDULER_UNDER_TEST = "+SCHEDULER_UNDER_TEST.getClass().getName(), module);
		Log.logLowln("TEST_TO_RUN = "+TEST_TO_RUN.getClass().getName(), module);
		Log.logLowln("SIMULATOR_NUMBER_OF_CYCLES = "+SIMULATOR_NUMBER_OF_CYCLES, module);
	}
}
