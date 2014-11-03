package koncept.sp.pipe.internal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import koncept.sp.ProcData;
import koncept.sp.future.ProcPipeFuture;
import koncept.sp.pipe.RunnableSplitProcStage;
import koncept.sp.resource.ProcPipeCleaner;
import koncept.sp.resource.ProcTerminator;
import koncept.sp.resource.SimpleProcPipeCleaner;
import koncept.sp.stage.SplitProcStage;
import koncept.sp.tracker.BlockingJobTracker;
import koncept.sp.tracker.internal.JobTrackerDefinition;

/**
 * Common proc pipe framework.
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public abstract  class ExecutorDrivenProcPipe<T> implements ProcPipeDefinition<T> {
	protected final Logger log;
	protected final JobTrackerDefinition<T> tracker;
	protected final List<SplitProcStage> stages;
	protected final ProcTerminator<T> procTerminator;
	protected final ProcPipeCleaner errorCleaner;

	protected final PipeStatus pipeStatus = new PipeStatus();
	
	public ExecutorDrivenProcPipe(List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this(null, new BlockingJobTracker<T>(), stages, procTerminator, new SimpleProcPipeCleaner());
	}
	
	public ExecutorDrivenProcPipe(Logger log, JobTrackerDefinition<T> tracker, List<SplitProcStage> stages, ProcTerminator<T> procTerminator, ProcPipeCleaner errorCleaner) {
		this.log = log == null ? Logger.getLogger(getClass().getName()) : log;
		this.tracker = tracker;
		this.stages = stages;
		this.procTerminator = procTerminator;
		this.errorCleaner = errorCleaner;
	}
	
	public abstract void shutDownIfRequired();
	
	//>> from ProcPipeDefinition
	public abstract ExecutorService getExecutor(int currentStage);
	
	public Future<T> submit(ProcData in) {
		if (in == null) throw new NullPointerException();
		if (isStopped()) throw new RuntimeException("ProcPipe cannot accept more jobs");
		log.log(Level.FINER, "Processing started");
		ProcPipeFuture<T> futureResult = new ProcPipeFuture<>();
		InternalProcState<T> state = new InternalProcState<>(futureResult,in);
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
	
	public int getNumberOfStages() {
		return stages.size();
	}
	
	public SplitProcStage getStage(int currentStage) {
		return stages.get(currentStage);
	}
	
	public boolean onStageStart(InternalProcState<T> state) {
		if (state.currentIndex() == 0 && pipeStatus.abortQueuedTasks.get())
			return false;
		
		if (pipeStatus.interruptRunningTasks.get())
			return false;
		
		if (state.cancelRequested()) {
			return false;
		}
		
		if (state.currentIndex() == 0) {
			state.markStarted();
			tracker.started(state);
		}
		return true;
	}
	
	public void onComplete(InternalProcState<T> state, ProcData newData) {
		state.increment(newData);
		if (state.currentIndex() < getNumberOfStages()) {
			getExecutor(state.currentIndex()).execute(new RunnableSplitProcStage<T>(this, state));
		} else {
			log.log(Level.FINER, "Processing completed");
			tracker.completed(state);
			T result = procTerminator.extractFinalResult(state.data());
			try {
				procTerminator.clean(state.data());
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception in procTerminator", e);
				clean(state.data());
			}
			state.future().markCompleted(result);
		}
		shutDownIfRequired();
	}
	
	public void onCancel(InternalProcState<T> state) {
		log.log(Level.FINE, "Processing cancelled at index " + state.currentIndex());
		tracker.completed(state);
		ProcPipeFuture<T> futureResult = state.future();
		futureResult.acknowledgeCancellation();
		clean(state.data());
		shutDownIfRequired();
	}
	
	public void onError(InternalProcState<T> state, Throwable error) {
		log.log(Level.WARNING, "Processing errored at index " + state.currentIndex(), error);
		tracker.completed(state);
		state.future().markErrored(error);
		clean(state.data());
		shutDownIfRequired();
	}
	
	private void clean(ProcData split) {
		try {
			errorCleaner.clean(split);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception in errorCleaner", e);
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
