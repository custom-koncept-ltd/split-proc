package koncept.sp.pipe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import koncept.sp.ProcSplit;
import koncept.sp.future.ProcPipeFuture;
import koncept.sp.pipe.state.ProcState;
import koncept.sp.resource.ProcPipeCleaner;
import koncept.sp.resource.ProcTerminator;
import koncept.sp.resource.SimpleProcPipeCleaner;
import koncept.sp.stage.SplitProcStage;

/**
 * 
 * A Proc Pipe that uses an Executor per stage<br/>
 * Probably most useful with a bunch of single thread executors<br/>
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public class ExecutorPerStageProcPipe<T> implements ProcPipeDefinition<T>, ProcPipe<T> {
	private final List<ExecutorService> executors;
	private final List<SplitProcStage> stages;
	
	private ProcTerminator<T> procTerminator;
	private ProcPipeCleaner errorCleaner = new SimpleProcPipeCleaner();
	
	public ExecutorPerStageProcPipe(List<ExecutorService> executors, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this.stages = stages;
		this.executors = executors;
		this.procTerminator = procTerminator;
	}
	
	public Future<T> handle(ProcSplit in) {
		ProcPipeFuture<T> futureResult = new ProcPipeFuture<T>();
		getExecutor(0).execute(new RunnableSplitProcStage(this, new ProcState(futureResult,in)));
		return futureResult;
	}
	
	public ExecutorService getExecutor(int currentStage) {
		return executors.get(currentStage);
	}
	
	public int getNumberOfStages() {
		return stages.size();
	}
	
	public SplitProcStage getStage(int currentStage) {
		return stages.get(currentStage);
	}
	
	public void onComplete(ProcState<T> state) {
		if (state.getNextStage() < getNumberOfStages()) {
			getExecutor(state.getNextStage()).execute(new RunnableSplitProcStage<T>(this, state));
		} else {
			T result = procTerminator.terminate(state.getLastSplit());
			state.getProcPipeFuture().markCompleted(result);
		}
	}
	
	public void onCancel(ProcState<T> state) {
		ProcPipeFuture<T> futureResult = state.getProcPipeFuture();
		futureResult.acknowledgeCancellation();
	}
	
	public void onError(ProcState<T> state, Throwable error) {
		
		//clean everything...
		errorCleaner.clean(state.getLastSplit());
		
		state.getProcPipeFuture().markErrored(error);
	}
	
}
