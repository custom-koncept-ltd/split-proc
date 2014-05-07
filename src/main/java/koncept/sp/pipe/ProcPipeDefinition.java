package koncept.sp.pipe;

import java.util.concurrent.ExecutorService;

import koncept.sp.pipe.state.ProcState;
import koncept.sp.stage.SplitProcStage;


public interface ProcPipeDefinition<T>  {
	
	public ExecutorService getExecutor(int currentStage);
	public SplitProcStage getStage(int currentStage);
	public int getNumberOfStages();
	
	public void onComplete(ProcState<T> state);
	public void onCancel(ProcState<T> state);
	public void onError(ProcState<T> state, Throwable error);
}
