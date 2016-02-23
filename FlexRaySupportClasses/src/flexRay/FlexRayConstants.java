package flexRay;

/**
 * This class holds all constants necessary for a FlexRay implementation
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 */
public class FlexRayConstants {
	public static final int HEADER_LEN = 3;
	public static final int STATIC_SLOTS_PER_CYCLE = 62;
	public static final int CYCLE_LENGTH_US = 5000;
	public static final int MAX_PAYLOAD_SIZE = 42;
	public static final int STATIC_SLOT_HEADER_LEN_BIT = 40;
	public static final int STATIC_SLOT_TRAILER_LEN_BIT = 24;
	public static final int STATIC_SLOT_LEN_BIT = STATIC_SLOT_HEADER_LEN_BIT + STATIC_SLOT_TRAILER_LEN_BIT + MAX_PAYLOAD_SIZE*8;
	public static final int BUS_SPEED_BIT_P_SEC = 10000000;
	public static final int MAX_CYCLE_NUMBER = 64;
	public static final int CYCLE_REPETITIONS[] = {1,2,4,5,8,10,16,20,32,40,50,64};
}
