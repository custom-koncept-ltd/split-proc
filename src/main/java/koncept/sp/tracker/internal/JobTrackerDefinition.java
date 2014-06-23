package koncept.sp.tracker.internal;

import koncept.sp.pipe.state.ProcState;
import koncept.sp.tracker.JobTracker;

public interface JobTrackerDefinition<T> extends JobTracker<T> {
	
	public void submitted(ProcState<T> state);
	public void started(ProcState<T> state);
	/**
	 * on success or failure, the job is marked as completed
	 * @param future
	 */
	public void completed(ProcState<T> state);
	
}
