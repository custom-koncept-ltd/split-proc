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
 * A Proc Pipe that uses a single re-entrant Executor<br/>
 * This should be enough for most uses<br/>
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public class SingleExecutorProcPipe<T> implements ProcPipeDefinition<T>, ProcPipe<T> {
	private final ExecutorService executor;
	private final List<SplitProcStage> stages;
	
	private ProcTerminator<T> procTerminator;
	private ProcPipeCleaner errorCleaner = new SimpleProcPipeCleaner();
	
	public SingleExecutorProcPipe(ExecutorService executor, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this.stages = stages;
		this.executor = executor;
		this.procTerminator = procTerminator;
	}
	
	public Future<T> handle(ProcSplit in) {
		ProcPipeFuture<T> futureResult = new ProcPipeFuture<T>();
		executor.execute(new RunnableSplitProcStage(this, new ProcState(futureResult,in)));
		return futureResult;
	}
	
	public ExecutorService getExecutor(int currentStage) {
		return executor;
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
