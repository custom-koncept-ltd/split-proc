package koncept.sp.pipe.internal;

import java.util.concurrent.ExecutorService;

import koncept.sp.pipe.ProcPipe;
import koncept.sp.pipe.state.ProcState;
import koncept.sp.stage.SplitProcStage;
import koncept.sp.tracker.internal.JobTrackerDefinition;


public interface ProcPipeDefinition<T> extends ProcPipe<T> {
	
	public ExecutorService getExecutor(int currentStage);
	public SplitProcStage getStage(int currentStage);
	public int getNumberOfStages();
	
	public void onComplete(ProcState<T> state);
	public void onCancel(ProcState<T> state);
	public void onError(ProcState<T> state, Throwable error);
	
	@Override
	public JobTrackerDefinition<T> tracker();
}
