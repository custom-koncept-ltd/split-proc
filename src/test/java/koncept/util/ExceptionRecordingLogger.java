package koncept.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionRecordingLogger extends Logger {
	public final List<LogMessage> logs = Collections.synchronizedList(new ArrayList<LogMessage>());
	
	public ExceptionRecordingLogger() {
		this(ExceptionRecordingLogger.class.getName());
	}
	public ExceptionRecordingLogger(String name) {
		super(name, null);
	}
	
	@Override
	public void log(Level level, String msg) {
		super.log(level, msg);
		logs.add(new LogMessage(level, msg, null));
	}
	
	@Override
	public void log(Level level, String msg, Throwable thrown) {
		super.log(level, msg, thrown);
		logs.add(new LogMessage(level, msg, thrown));
	}
	
	
	public void output(PrintStream out) {
		for(LogMessage log: logs) {
			out.println(log);
		}
	}
	
	public static class LogMessage {
		public final Level level;
		public final String msg;
		public final Throwable thrown;
		
		public LogMessage(Level level, String msg, Throwable thrown) {
			this.level = level;
			this.msg = msg;
			this.thrown = thrown;
		}
		
		@Override
		public String toString() {
			if (thrown == null)
				return level + ":" + msg;
			return level + ":" + msg + ":" + thrown;
		}
	}
	
}