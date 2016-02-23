package tests;

import SchedulingInterfaces.Schedule;
import flexRay.FlexRayStaticMessageCollection;

public interface FlexRayTester
{
	public FlexRayStaticMessageCollection getMsgs();

	public void setMsgs(FlexRayStaticMessageCollection msgs);
	
	public Schedule getSchedule();
	
	public String getTestCaseFile();
}
