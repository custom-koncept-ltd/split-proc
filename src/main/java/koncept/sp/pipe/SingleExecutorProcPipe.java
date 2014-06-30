package koncept.sp.pipe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * A Proc Pipe that uses a single re-entrant Executor<br/>
 * This should be enough for most uses<br/>
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public class SingleExecutorProcPipe<T> implements ProcPipeDefinition<T> {
	private final Logger log;
	private final JobTrackerDefinition<T> tracker;
	private final ExecutorService executor;
	private final List<SplitProcStage> stages;
	private final ProcTerminator<T> procTerminator;
	private final ProcPipeCleaner errorCleaner;
	private final PipeStatus pipeStatus = new PipeStatus();
	
	public SingleExecutorProcPipe(ExecutorService executor, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this(SingleExecutorProcPipe.class.getName(), new BlockingJobTracker<T>(), executor, stages, procTerminator, new SimpleProcPipeCleaner());
	}
	
	public SingleExecutorProcPipe(String loggerName, JobTrackerDefinition<T> tracker, ExecutorService executor, List<SplitProcStage> stages, ProcTerminator<T> procTerminator, SimpleProcPipeCleaner errorCleaner) {
		this.tracker = tracker;
		this.stages = stages;
		this.executor = executor;
		this.procTerminator = procTerminator;
		this.errorCleaner = errorCleaner;
		log = Logger.getLogger(loggerName);
	}

	public Future<T> submit(ProcSplit in) {
		if (in == null) throw new NullPointerException();
		if (isStopped()) throw new RuntimeException("ProcPipe cannot accept more jobs");
		log.log(Level.FINER, "Processing started");
		ProcPipeFuture<T> futureResult = new ProcPipeFuture<>();
		ProcState<T> state = new ProcState<>(futureResult,in);
		tracker.submitted(state);
		getExecutor(0).execute(new RunnableSplitProcStage<>(this, state));
		return futureResult;
	}
	
	public void stop(boolean stopExecutorOnCompletion, boolean abortQueuedTasks, boolean interruptRunningTasks) {
		if (!abortQueuedTasks && interruptRunningTasks)
			throw new IllegalArgumentException("must abort queued tasks if interrupting running tasks");
		if (stopExecutorOnCompletion) {
			log.log(Level.INFO, "stop requested. Executor will be shut down on completion");
		} else {
			log.log(Level.INFO, "stop requested. Executor will NOT be shut down on completion");
		}
		pipeStatus.stopped.set(true);
		pipeStatus.stopExecutorOnCompletion.set(stopExecutorOnCompletion);
		pipeStatus.abortQueuedTasks.set(abortQueuedTasks);
		pipeStatus.interruptRunningTasks.set(interruptRunningTasks);
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
			log.log(Level.FINER, "Processing completed");
			tracker.completed(state);
			T result = procTerminator.extractFinalResult(state.getLastSplit());
			procTerminator.clean(state.getLastSplit());
			state.getProcPipeFuture().markCompleted(result);
		}
		shutDownIfRequired();
	}

	
	public void onCancel(ProcState<T> state) {
		log.log(Level.FINE, "Processing cancelled at index " + state.getNextStage());
		tracker.completed(state);
		ProcPipeFuture<T> futureResult = state.getProcPipeFuture();
		futureResult.acknowledgeCancellation();
		errorCleaner.clean(state.getLastSplit());
		shutDownIfRequired();
	}
	
	public void onError(ProcState<T> state, Throwable error) {
		log.log(Level.WARNING, "Processing errored at index " + state.getNextStage(), error);
		tracker.completed(state);
		errorCleaner.clean(state.getLastSplit());
		state.getProcPipeFuture().markErrored(error);
		shutDownIfRequired();
	}
	
	private void shutDownIfRequired() {
		if (pipeStatus.stopExecutorOnCompletion.get() && tracker.live().isEmpty()) {
			executor.execute(new Runnable() {
				public void run(){
					log.log(Level.INFO, "Executor.shutdown()");
					executor.shutdown();
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
