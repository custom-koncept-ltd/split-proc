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
	 * Starts a task.
	 * @param in
	 * @return
	 * @throws IllegalStateException if the pipe has been 'stopped'
	 */
	public Future<T> submit(ProcSplit in) throws IllegalStateException;
	
	/**
	 * if the pipe has been stopped,
	 * @return if the pipe has been stopped, 
	 */
	public boolean isStopped();
	
	/**
	 * Stops the pipe from accepting any more requests.
	 * tasks currently being serviced will continue to run
	 * @param stopExecutorOnCompletion - if true, will stop the underlying executor when the last task has completed
	 * @param abortQueuedTasks - if true, any tasks on the queue will not be started.
	 * @param interruptRunningTasks - if true, any running tasks won't start the next stage of their processing
	 */
	public void stop(boolean stopExecutorOnCompletion, boolean abortQueuedTasks, boolean interruptRunningTasks);
	
	/**
	 * Gets the job tracker
	 * @return
	 */
	public JobTracker<T> tracker();
}
