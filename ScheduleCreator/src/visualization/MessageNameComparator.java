package visualization;

import java.util.Comparator;

import SchedulingInterfaces.Message;

/**
 * Sorts messages in descending priority and descending payload length order
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 * 
 */
@SuppressWarnings("rawtypes")
public class MessageNameComparator implements Comparator<Message>
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
    public int compare(Message msg1, Message msg2)
    {
   
    	return msg1.getName().compareTo(msg2.getName());
    }  
}
