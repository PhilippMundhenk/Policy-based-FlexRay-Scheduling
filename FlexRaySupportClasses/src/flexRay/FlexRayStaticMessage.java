package flexRay;

import java.util.Collection;

import SchedulingInterfaces.Message;

public class FlexRayStaticMessage extends Message<String> 
{
	/**
	 * This is the constructor for setting up a message. A valid message always needs to have the given parameters.
	 * 
	 * @param size
	 * length of the payload of the message in bytes
	 * @param period
	 * period of the message in milliseconds
	 * @param deadline
	 * deadline of the message in milliseconds
	 * @param name
	 * name (identifier) of the message
	 * @param sender
	 * sending node of the message
	 * @param receivers
	 * receiving nodes of the message
	 */
	public FlexRayStaticMessage(int size, double period, double deadline, String name,
			Object sender, Collection<String> receivers)
	{
		super(size, period, deadline, name, sender, receivers);
	}	
		
	/**
	 * This method returns the deadline in milliseconds.
	 * 
	 * @return
	 * deadline in milliseconds
	 */
	@Override
	public double getDeadline()
	{
		return super.getDeadline();
	}
}
