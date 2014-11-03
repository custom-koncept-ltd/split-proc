package koncept.sp.tracker.internal;

import koncept.sp.pipe.internal.InternalProcState;
import koncept.sp.tracker.JobTracker;

public interface JobTrackerDefinition<T> extends JobTracker<T> {
	
	public void submitted(InternalProcState<T> state);
	public void started(InternalProcState<T> state);
	/**
	 * on success or failure, the job is marked as completed
	 * @param state
	 */
	public void completed(InternalProcState<T> state);
	
}
