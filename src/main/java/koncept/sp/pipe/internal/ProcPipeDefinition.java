package koncept.sp.pipe.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import koncept.sp.ProcData;
import koncept.sp.pipe.ProcPipe;
import koncept.sp.stage.SplitProcStage;
import koncept.sp.tracker.internal.JobTrackerDefinition;


public interface ProcPipeDefinition<T> extends ProcPipe<T> {
	
	public ExecutorService getExecutor(int currentStage);
	public SplitProcStage getStage(int currentStage);
	public int getNumberOfStages();
	
	public PipeStatus status();
	
	/**
	 * 
	 * @param state
	 * @return true if processing can continue, false to just abort
	 */
	public boolean onStageStart(InternalProcState<T> state);
	public void onComplete(InternalProcState<T> state, ProcData newData);
	public void onCancel(InternalProcState<T> state);
	public void onError(InternalProcState<T> state, Throwable error);
	
	@Override
	public JobTrackerDefinition<T> tracker();
	
	
	public static class PipeStatus {
		public final AtomicBoolean stopped = new AtomicBoolean(false);
		public final AtomicBoolean stopExecutorOnCompletion = new AtomicBoolean(false);
		public final AtomicBoolean abortQueuedTasks = new AtomicBoolean(false);
		public final AtomicBoolean interruptRunningTasks = new AtomicBoolean(false);
	}
}
