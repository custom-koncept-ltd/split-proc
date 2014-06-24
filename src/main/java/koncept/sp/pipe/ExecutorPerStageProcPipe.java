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
import koncept.sp.tracker.BlockingJobTracker;
import koncept.sp.tracker.internal.JobTrackerDefinition;

/**
 * 
 * A Proc Pipe that uses an Executor per stage<br/>
 * Probably most useful with a bunch of single thread executors<br/>
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public class ExecutorPerStageProcPipe<T> implements ProcPipeDefinition<T>{
	private final JobTrackerDefinition<T> tracker;
	private final List<ExecutorService> executors;
	private final List<SplitProcStage> stages;

	private final PipeStatus pipeStatus = new PipeStatus();
	
	private ProcTerminator<T> procTerminator;
	private ProcPipeCleaner errorCleaner = new SimpleProcPipeCleaner();
	
	public ExecutorPerStageProcPipe(List<ExecutorService> executors, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this(new BlockingJobTracker<T>(), executors, stages, procTerminator);
	}
	
	public ExecutorPerStageProcPipe(JobTrackerDefinition<T> tracker, List<ExecutorService> executors, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this.tracker = tracker;
		this.stages = stages;
		this.executors = executors;
		this.procTerminator = procTerminator;
	}
	
	public Future<T> submit(ProcSplit in) {
		if (isStopped()) throw new RuntimeException("ProcPipe cannot accept more jobs");
		ProcPipeFuture<T> futureResult = new ProcPipeFuture<>();
		ProcState<T> state = new ProcState<>(futureResult,in);
		tracker.submitted(state);
		getExecutor(0).execute(new RunnableSplitProcStage<>(this, state));
		return futureResult;
	}
	
	public void stop(boolean stopExecutorOnCompletion, boolean abortQueuedTasks, boolean interruptRunningTasks) {
		if (!abortQueuedTasks && interruptRunningTasks)
			throw new IllegalArgumentException("must abort queued tasks if interrupting running tasks");
		pipeStatus.stopped.set(true);
		pipeStatus.stopExecutorOnCompletion.set(stopExecutorOnCompletion);
		pipeStatus.abortQueuedTasks.set(abortQueuedTasks);
		pipeStatus.interruptRunningTasks.set(interruptRunningTasks);
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
	
	public boolean onStageStart(ProcState<T> state) {
		if (state.getNextStage() == 0 && pipeStatus.abortQueuedTasks.get())
			return false;
		
		if (pipeStatus.interruptRunningTasks.get())
			return false;
		
		if (state.isCancellationRequested()) {
			return false;
		}
		
		if (state.getNextStage() == 0) {
			state.markStarted();
			tracker.started(state);
		}
		return true;
	}
	
	public void onComplete(ProcState<T> state) {
		if (state.getNextStage() < getNumberOfStages()) {
			getExecutor(state.getNextStage()).execute(new RunnableSplitProcStage<T>(this, state));
		} else {
			tracker.completed(state);
			T result = procTerminator.terminate(state.getLastSplit());
			state.getProcPipeFuture().markCompleted(result);
		}
		shutDownIfRequired();
	}
	
	public void onCancel(ProcState<T> state) {
		tracker.completed(state);
		ProcPipeFuture<T> futureResult = state.getProcPipeFuture();
		futureResult.acknowledgeCancellation();
		errorCleaner.clean(state.getLastSplit());
		shutDownIfRequired();
	}
	
	public void onError(ProcState<T> state, Throwable error) {
		tracker.completed(state);
		errorCleaner.clean(state.getLastSplit());
		state.getProcPipeFuture().markErrored(error);
		shutDownIfRequired();
	}
	
	public void shutDownIfRequired() {
		if (pipeStatus.stopExecutorOnCompletion.get() && tracker.live().isEmpty()) {
			getExecutor(0).execute(new Runnable() {
				public void run(){
					for(ExecutorService executor: executors) {
						executor.shutdown();
					}
				}
			});
		}
	}
	
	public JobTrackerDefinition<T> tracker() {
		return tracker;
	}
	
	public boolean isStopped() {
		return pipeStatus.stopped.get();
	}
	
	public PipeStatus status() {
		return pipeStatus;
	}
	
}
