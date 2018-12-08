package Libs;

import java.util.Calendar;

/**
 * Created by qwesd on 2015/11/20.
 */
public class Log
{
	private static int logSetting = 0xffffffff;
	public static final int TRACE_LOG = 0x1;
	public static final int DEBUG_LOG = 0x2;
	public static final int INFO_LOG = 0x4;
	public static final int WARNING_LOG = 0x8;
	public static final int ERROR_LOG = 0x10;
	public static final int OUTPUT_LOG = 0x20;
	public static final int TIME_LOG = 0x40;
	public static final int LABEL_LOG = 0x80;
	public static final int SIMPLE_LOG = 0x100;

	public volatile static Log default_log = new Log(System.out::print);

	public static void setDefault_log(Log log)
	{
		default_log = log;
	}

	public LogSteamInterface logStreamInterface;

	public Log(LogSteamInterface logStreamInterface)
	{
		this.logStreamInterface = logStreamInterface;
	}

	public interface LogSteamInterface
	{
		void write(String string);
	}

	public void simple(String data)
	{
		if ((logSetting & SIMPLE_LOG) != 0)
			writeToStream(data);
	}

	public void output(String output)
	{
		if ((logSetting & OUTPUT_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[OUTPUT] " + output : output);
	}

	public void trace(String trace)
	{
		if ((logSetting & TRACE_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[TRACE] " + trace : trace);
	}

	public void debug(String debug)
	{
		if ((logSetting & DEBUG_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[DEBUG] " + debug : debug);
	}

	public void info(String info)
	{
		if ((logSetting & INFO_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[INFO] " + info : info);
	}

	public void warn(String warning)
	{
		if ((logSetting & WARNING_LOG) != 0)
			wrapper(((logSetting & LABEL_LOG) != 0) ? "[WARNING] " + warning : warning);
	}

	public void error(String error)
	{
		if ((logSetting & ERROR_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[ERROR] " + error : error);
	}

	private void wrapper(String str)
	{
		Calendar calendar = Calendar.getInstance();
		if (!str.endsWith("\n"))
			str = str + "\n";
		String time = String.format("[%04d-%02d-%02d|%02d:%02d:%02d] %s",
				calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), str);
		if ((logSetting & TIME_LOG) != 0)
			writeToStream(time);
		else
			writeToStream(str);
	}

	private void writeToStream(String str)
	{
		logStreamInterface.write(str);
	}

	public void printException(Exception e)
	{
		error(e.getClass().getName() + e.getLocalizedMessage());
		e.printStackTrace();
		if (e.getCause() != null)
			error("Cause" + e.getCause().getClass().getName() + e.getCause().getLocalizedMessage());
	}

	public void enableLog(int logIndex)
	{
		logSetting |= logIndex;
	}

	public void disableLog(int logIndex)
	{
		logSetting &= ~logIndex;
	}
}