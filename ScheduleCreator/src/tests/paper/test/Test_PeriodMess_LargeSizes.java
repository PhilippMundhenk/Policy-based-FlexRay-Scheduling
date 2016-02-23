package tests.paper.test;

import java.io.File;
import java.io.IOException;

import tests.FlexRayTester;
import SchedulingInterfaces.Schedule;
import flexRay.FlexRayConstants;
import flexRay.FlexRaySchedule;
import flexRay.FlexRayStaticMessageCollection;
import flexRay.FlexRayFileInput.FileFormat;
import flexRay.FlexRayFileInput.ReductionMode;


/**
 * This is a test case for the system.
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 */
public class Test_PeriodMess_LargeSizes implements FlexRayTester
{
	FlexRayStaticMessageCollection msgs;
	Schedule schedule;
//	Schedule schedule = new FlexRaySchedule(FlexRayConstants.MAX_CYCLE_NUMBER, FlexRayConstants.CYCLE_LENGTH_US/1000, 
//			FlexRayConstants.STATIC_SLOT_LEN_BIT/8, FlexRayConstants.STATIC_SLOTS_PER_CYCLE);
	
	public Test_PeriodMess_LargeSizes()
	{
		try
		{
			schedule = new FlexRaySchedule(new File("").getAbsolutePath().concat("/../ScheduleCreator/schedules/fibextest2.xml"), "FlexRayNetwork_1", "Channel_A_FlexRayNetwork_1", FlexRayConstants.MAX_CYCLE_NUMBER, "");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			setMsgs(flexRay.FlexRayFileInput.ReadFile(new File("").getAbsolutePath().concat("/../ScheduleCreator/testcases/paper/Test_PeriodMess_LargeSizes_newFormat.txt"), FileFormat.MULTI_MODE, ReductionMode.POLICY));
			
			msgs.printList();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public FlexRayStaticMessageCollection getMsgs()
	{
		return msgs;
	}

	public void setMsgs(FlexRayStaticMessageCollection msgs)
	{
		this.msgs = msgs;
	}

	public Schedule getSchedule()
	{
		return schedule;
	}

	@Override
	public String getTestCaseFile()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
