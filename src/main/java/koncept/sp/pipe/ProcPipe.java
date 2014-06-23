package koncept.sp.pipe;

import java.util.concurrent.Future;

import koncept.sp.ProcSplit;
import koncept.sp.tracker.JobTracker;

/**
 * 
 * Simple handline interface for a Proc Pipe.<br/>
 * <br/>
 * Basic process:<br/>
 * <ul>
 * <li>ProcSplit passed in</li>
 * <li>As long as the future isn't cancelled, execute the next step</li>
 * <li>When all steps are complete, execute the clean up code</li>
 * <li>return true for success</li>
 * </ul>
 * <br/>
 * If a failure happens, the cleanup code is still executed, and the
 * first exception is it set into the future.
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public interface ProcPipe<T> {
	
	/**
	 * Starts the process
	 * @param in
	 * @return
	 */
	public Future<T> submit(ProcSplit in);
	
	/**
	 * Stops the pipe (specifically, calls shutdown() on any underlying executors)
	 */
	public void stop();
	
	/**
	 * Gets the job tracker
	 * @return
	 */
	public JobTracker<T> tracker();
}
