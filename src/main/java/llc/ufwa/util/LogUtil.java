package llc.ufwa.util;

import java.lang.StackTraceElement;

/**
 * @author kevin hollingshead
 * 
 * This is a logger-independent class that provides stack information in an easy way.
 * 
 * Examples:
 * 
 * 		Log.v(TAG, LogUtil.loc() + "Value = " + value); // Outputs "MyClass.myMethod:123 Value = 45".
 * 
 * 		logger.debug(LogUtil.dump(); // Outputs the entire stack formatted as for LogUtil.loc().
 *
 */
public class LogUtil {

	/**
	 * @return A string containing the class (file) name, the method and the line number 
	 * within the file that correspond with the location of the call to this method.  Note
	 * that the first three stack trace elements are ignored since they are related to the 
	 * logging call itself.
	 */
	public static String loc() {
		final StackTraceElement[] st = Thread.currentThread().getStackTrace();
		final StackTraceElement ste = st[getStartIndex(st)];
		return String.format("%s %s %s ", ste.getClassName(), ste.getMethodName(), ste.getLineNumber());
	}
	
	/**
	 * @return A string containing one line per level in the current stack. Note
	 * that the first three stack trace elements are ignored since they are related to the 
	 * logging call itself.
	 */
	public static String dump() {
		final StackTraceElement[] st = Thread.currentThread().getStackTrace();
		return formatStackTrace(st);
	}
	
	/**
	 * @param st The current stack trace.  The bottom element helps determine if we're 
	 * running java native or Android, which has a second call to the Dalvik VM to get
	 * the stack trace.
	 * 
	 * @return The appropriate index to start with for a dump.  There's no sense in dumping
	 * the stack elements releated to logging and getting the stack trace.
	 */
	private static int getStartIndex (StackTraceElement[] st) {
		
		for (int index = 0; index < st.length; index++) {
			if (   (st[index].getClassName().equalsIgnoreCase("java.lang.thread")) 
				&& (st[index].getMethodName().equalsIgnoreCase("getStackTrace"))) {
				// We're at the call that gets the stack trace, ignore all stack elements above this.
				return (index + 2);
			}
		}
		return 0;
	}

	/**
	 * @param st The stack trace to format.
	 * 
	 * @return The formatted trace with one level per line.  Each line has the same format as the loc() function.
	 */
	private static String formatStackTrace (StackTraceElement[] st) {
		String retStr = "Stack dump:\n";
		for (int ii = getStartIndex(st); ii < st.length; ii++) {
			retStr += String.format("%s %s %s\n", st[ii].getClassName(), st[ii].getMethodName(), st[ii].getLineNumber());
		}
		return retStr;
	}
	
}
