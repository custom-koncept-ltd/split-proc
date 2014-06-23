package koncept.sp.pipe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import koncept.sp.ProcSplit;
import koncept.sp.future.ProcPipeFuture;
import koncept.sp.pipe.internal.ProcPipeDefinition;
import koncept.sp.pipe.state.ProcState;
import koncept.sp.resource.ProcPipeCleaner;
import koncept.sp.resource.ProcTerminator;
import koncept.sp.resource.SimpleProcPipeCleaner;
import koncept.sp.stage.SplitProcStage;
import koncept.sp.tracker.NullJobTracker;
import koncept.sp.tracker.internal.JobTrackerDefinition;

/**
 * 
 * A Proc Pipe that uses a single re-entrant Executor<br/>
 * This should be enough for most uses<br/>
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public class SingleExecutorProcPipe<T> implements ProcPipeDefinition<T> {
	private final JobTrackerDefinition<T> tracker;
	private final ExecutorService executor;
	private final List<SplitProcStage> stages;
	
	private ProcTerminator<T> procTerminator;
	private ProcPipeCleaner errorCleaner = new SimpleProcPipeCleaner();
	
	public SingleExecutorProcPipe(ExecutorService executor, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this(new NullJobTracker<T>(), executor, stages, procTerminator);
	}
	
	public SingleExecutorProcPipe(JobTrackerDefinition<T> tracker, ExecutorService executor, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this.tracker = tracker;
		this.stages = stages;
		this.executor = executor;
		this.procTerminator = procTerminator;
	}

	public Future<T> submit(ProcSplit in) {
		ProcPipeFuture<T> futureResult = new ProcPipeFuture<>();
		ProcState<T> state = new ProcState<>(futureResult,in);
		tracker.submitted(state);
		getExecutor(0).execute(new RunnableSplitProcStage<>(this, state));
		return futureResult;
	}
	
	public void stop() {
		executor.shutdown();
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
			tracker.completed(state);
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
	
	public JobTrackerDefinition<T> tracker() {
		return tracker;
	}
}
