package koncept.sp.pipe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import koncept.sp.pipe.internal.ExecutorDrivenProcPipe;
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
public class ExecutorPerStageProcPipe<T> extends ExecutorDrivenProcPipe<T> {
	private final List<ExecutorService> executors;
	
	public ExecutorPerStageProcPipe(List<ExecutorService> executors, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this(null, new BlockingJobTracker<T>(), executors, stages, procTerminator, new SimpleProcPipeCleaner());
	}
	
	public ExecutorPerStageProcPipe(Logger log, JobTrackerDefinition<T> tracker, List<ExecutorService> executors, List<SplitProcStage> stages, ProcTerminator<T> procTerminator, ProcPipeCleaner errorCleaner) {
		super(log, tracker, stages, procTerminator, errorCleaner);
		this.executors = executors;
		if (executors.size() != stages.size()) throw new RuntimeException("Must have 1 executor per stage. Got " + executors.size() + " executors for " + stages.size() + " stages");
	}
	
	public ExecutorService getExecutor(int currentStage) {
		return executors.get(currentStage);
	}
	
	public void shutDownIfRequired() {
		if (pipeStatus.stopExecutorOnCompletion.get() && tracker.live().isEmpty()) {
			
			for(final ExecutorService executor: executors) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						log.log(Level.INFO, "Executor.shutdown()");
						executor.shutdown();
					}
				});
			}
		}
	}
	
}
