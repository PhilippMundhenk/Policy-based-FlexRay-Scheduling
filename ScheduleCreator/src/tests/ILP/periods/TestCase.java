package tests.ILP.periods;

import java.io.File;
import java.io.IOException;

import tests.FlexRayTester;
import SchedulingInterfaces.Schedule;
import flexRay.FlexRayConstants;
import flexRay.FlexRayFileInput.FileFormat;
import flexRay.FlexRayFileInput.ReductionMode;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticMessageCollection;


/**
 * This is a test case for the system.
 * 
 * @author TUM CREATE - RP3
 */
public class TestCase implements FlexRayTester
{
	FlexRayStaticMessageCollection msgs;
	String testCaseFile;
	//Schedule schedule = new FlexRaySchedule(FlexRayConstants.MAX_CYCLE_NUMBER, FlexRayConstants.CYCLE_LENGTH_US/1000, 
	//		FlexRayConstants.STATIC_SLOT_LEN_BIT/8, FlexRayConstants.STATIC_SLOTS_PER_CYCLE);
	
	Schedule schedule;
	
	/**
	 * This constructor initializes the test case and loads all data.
	 */
	public TestCase(String testCaseFile, FileFormat fileFormat, ReductionMode reductionMode, String path)
	{
		this.testCaseFile = testCaseFile;
		try
		{
			schedule = new FlexRaySchedule(new File("").getAbsolutePath().concat("/../ScheduleCreator/schedules/fibextest2.xml"), "FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1", FlexRayConstants.MAX_CYCLE_NUMBER, "");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			//setMsgs(flexRay.FlexRayFileInput.ReadFile("msgs\\config1a.txt"));
			setMsgs(flexRay.FlexRayFileInput.ReadFile(new File("").getAbsolutePath().concat(path+testCaseFile+"_newFormat.txt"), fileFormat, reductionMode));
			
			msgs.printList();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method returns all messages to be scheduled.
	 * 
	 * @return
	 * collection of all messages
	 */
	public FlexRayStaticMessageCollection getMsgs()
	{
		return msgs;
	}

	/**
	 * This method sets all messages to be scheduled.
	 * 
	 * @param messages
	 * collection of all messages
	 */
	public void setMsgs(FlexRayStaticMessageCollection msgs)
	{
		this.msgs = msgs;
	}

	/**
	 * This method returns the schedule defined by the test case.
	 * 
	 * @return
	 * schedule defined by the test case.
	 */
	public Schedule getSchedule()
	{
		return schedule;
	}

	@Override
	public String getTestCaseFile() {
		return testCaseFile;
	}
}
