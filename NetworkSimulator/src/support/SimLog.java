package support;

import java.io.PrintStream;


/**
 * This class contains all logging functions
 * 
 * @author TUM CREATE - RP3 - Philipp Mundhenk
 */
public class SimLog
{
	public static final int LOG_LEVEL_OFF = -10;
	public static final int LOG_LEVEL_LOW = 0;
	public static final int LOG_LEVEL_MEDIUM = 10;
	public static final int LOG_LEVEL_TRACE = 20;
	
	private static Object[][] LOG_EXCEPTIONS;

	private static Boolean newLine = true;
	private static long startTime;
	
	private static PrintStream outStream;
	
	public static void initLog()
	{
		startTime =  System.currentTimeMillis();
		setLOG_EXCEPTIONS(new Object[][]{{"Example", SimLog.LOG_LEVEL_TRACE}});
		try
		{
			outStream = System.out; //new PrintStream(new File("C:\\Philipp\\others\\workspace\\ScheduleCreator\\debug\\simOut.txt"));  //
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 
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
		if(debugLevel <= simulator.config.Config.LOG_LEVEL || print)
		{
			if(newLine)
			{
				outStream.print("[" + module + "; " + time + "ms] ");
			}
			outStream.print(debugMessage);
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
		if(debugLevel <= simulator.config.Config.LOG_LEVEL || print)
		{
			if(newLine)
			{
				outStream.print("[" + module + "; " + time + "ms] ");
			}
			outStream.println(debugMessage);
		}
		
		newLine = true;
	}
	
	public static void logln(int debugLevel)
	{
		if(debugLevel <= simulator.config.Config.LOG_LEVEL)
		{
			outStream.println();
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
