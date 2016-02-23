package support;


/**
 * This class contains all logging functions
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 */
public class Log
{
	public static final int LOG_LEVEL_OFF = -10;
	public static final int LOG_LEVEL_LOW = 0;
	public static final int LOG_LEVEL_MEDIUM = 10;
	public static final int LOG_LEVEL_TRACE = 20;
	
	private static Object[][] LOG_EXCEPTIONS;

	private static Boolean newLine = true;
	private static long startTime;
	
	public static void initLog()
	{
		startTime =  System.currentTimeMillis();
	}
	
	public static Object[][] getLOG_EXCEPTIONS()
	{
		return LOG_EXCEPTIONS;
	}

	public static void setLOG_EXCEPTIONS(Object[][] lOG_EXCEPTIONS)
	{
		LOG_EXCEPTIONS = lOG_EXCEPTIONS;
	}
	
	public static void log(String debugMessage, int debugLevel, String module)
	{
		Boolean print = false;
		
		long time =  System.currentTimeMillis() - startTime;
		for (int i = 0; i < LOG_EXCEPTIONS.length; i++)
		{
			if((((String)LOG_EXCEPTIONS[i][0]).equals(module)) && (debugLevel <= ((Integer)LOG_EXCEPTIONS[i][1])))
			{
				print = true;
			}
		}
		if(debugLevel <= tests.ScheduleCreatorConfig.LOG_LEVEL || print)
		{
			if(newLine)
			{
				System.out.print("[" + module + "; " + time + "ms] ");
			}
			System.out.print(debugMessage);
		}
		
		newLine = false;
	}
	
	public static void logln(String debugMessage, int debugLevel, String module)
	{
		Boolean print = false;
	
		long time =  System.currentTimeMillis() - startTime;
		for (int i = 0; i < LOG_EXCEPTIONS.length; i++)
		{
			if((((String)LOG_EXCEPTIONS[i][0]).equals(module)) && (debugLevel <= ((Integer)LOG_EXCEPTIONS[i][1])))
			{
				print = true;
			}
		}
		if(debugLevel <= tests.ScheduleCreatorConfig.LOG_LEVEL || print)
		{
			if(newLine)
			{
				System.out.print("[" + module + "; " + time + "ms] ");
			}
			System.out.println(debugMessage);
		}
		
		newLine = true;
	}
	
	public static void logln(int debugLevel)
	{
		if(debugLevel <= tests.ScheduleCreatorConfig.LOG_LEVEL)
		{
			System.out.println();
		}
		
		newLine = true;
	}
	
	public static void logLow(String debugMessage, String module)
	{
		log(debugMessage, LOG_LEVEL_LOW, module);
	}
	
	public static void logLowln(String debugMessage, String module)
	{
		logln(debugMessage, LOG_LEVEL_LOW, module);
	}
	
	public static void logLowln(String module)
	{
		Boolean print = false;
		for (int i = 0; i < LOG_EXCEPTIONS.length; i++)
		{
			if(((String)LOG_EXCEPTIONS[i][0]).equals(module))
			{
				print = true;
				logln(LOG_LEVEL_LOW);
			}
		}
		if(!print)
		{
			logln(LOG_LEVEL_LOW);
		}
	}
	
	public static void logMedium(String debugMessage, String module)
	{
		log(debugMessage, LOG_LEVEL_MEDIUM, module);
	}
	
	public static void logMediumln(String debugMessage, String module)
	{
		logln(debugMessage, LOG_LEVEL_MEDIUM, module);
	}
	
	public static void logMediumln(String module)
	{
		Boolean print = false;
		for (int i = 0; i < LOG_EXCEPTIONS.length; i++)
		{
			if(((String)LOG_EXCEPTIONS[i][0]).equals(module))
			{
				print = true;
				logln(LOG_LEVEL_LOW);
			}
		}
		if(!print)
		{
			logln(LOG_LEVEL_MEDIUM);
		}
	}
	
	public static void logTrace(String debugMessage, String module)
	{
		log(debugMessage, LOG_LEVEL_TRACE, module);
	}
	
	public static void logTraceln(String debugMessage, String module)
	{
		logln(debugMessage, LOG_LEVEL_TRACE, module);
	}
	
	public static void logTraceln(String module)
	{
		Boolean print = false;
		for (int i = 0; i < LOG_EXCEPTIONS.length; i++)
		{
			if(((String)LOG_EXCEPTIONS[i][0]).equals(module))
			{
				print = true;
				logln(LOG_LEVEL_LOW);
			}
		}
		if(!print)
		{
			logln(LOG_LEVEL_TRACE);
		}
	}
}
