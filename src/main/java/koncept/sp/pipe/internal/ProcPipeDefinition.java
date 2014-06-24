package koncept.sp.pipe.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import koncept.sp.pipe.ProcPipe;
import koncept.sp.pipe.state.ProcState;
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
	public boolean onStageStart(ProcState<T> state);
	public void onComplete(ProcState<T> state);
	public void onCancel(ProcState<T> state);
	public void onError(ProcState<T> state, Throwable error);
	
	@Override
	public JobTrackerDefinition<T> tracker();
	
	
	public static class PipeStatus {
		public final AtomicBoolean stopped = new AtomicBoolean(false);
		public final AtomicBoolean stopExecutorOnCompletion = new AtomicBoolean(false);
		public final AtomicBoolean abortQueuedTasks = new AtomicBoolean(false);
		public final AtomicBoolean interruptRunningTasks = new AtomicBoolean(false);
	}
}
