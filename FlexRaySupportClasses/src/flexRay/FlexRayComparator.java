package flexRay;

import java.util.Comparator;

/**
 * Sorts messages in descending priority and descending payload length order
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 * 
 */
public class FlexRayComparator implements Comparator<FlexRayStaticMessage>
{
		   
	/**
	 * This method compares the given two FlexRay messages and gives an indication of ordering 
	 * of the messages by highest priority, highest payload first.
	 * 
	 * @param msg1
	 * first message for comparison
	 * @param msg2
	 * second element for comparison
	 * 
	 * @return
	 * -1: msg1 before msg2
	 * 0: no ordering for msg1 and msg2 (equal)
	 * 1: msg2 before msg1
	 */
    public int compare(FlexRayStaticMessage msg1, FlexRayStaticMessage msg2)
    {
   
    	if(msg1.getPeriod() < msg2.getPeriod())
        {
        	return -1;
        }
    	else if(msg1.getPeriod() > msg2.getPeriod())
        {
        	return 1;
        }
        else if(msg1.getPeriod() == msg2.getPeriod())
        {
        	if(msg1.getSize() > msg2.getSize())
        	{
        		return -1;
        	}
        	else if(msg1.getSize() < msg2.getSize())
        	{
        		return 1;
        	}
        	else
        	{
        		return 0;
        	}
        }
        else
        {
        	return 0;
        }    
    }  
}
