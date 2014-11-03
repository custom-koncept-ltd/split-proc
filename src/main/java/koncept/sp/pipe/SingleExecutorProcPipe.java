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
 * A Proc Pipe that uses a single re-entrant Executor<br/>
 * This should be enough for most uses<br/>
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public class SingleExecutorProcPipe<T> extends ExecutorDrivenProcPipe<T> {
	private final ExecutorService executor;
	
	public SingleExecutorProcPipe(ExecutorService executor, List<SplitProcStage> stages, ProcTerminator<T> procTerminator) {
		this(null, new BlockingJobTracker<T>(), executor, stages, procTerminator, new SimpleProcPipeCleaner());
	}
	
	public SingleExecutorProcPipe(Logger log, JobTrackerDefinition<T> tracker, ExecutorService executor, List<SplitProcStage> stages, ProcTerminator<T> procTerminator, ProcPipeCleaner errorCleaner) {
		super(log, tracker, stages, procTerminator, errorCleaner);
		this.executor = executor;
	}
	
	public ExecutorService getExecutor(int currentStage) {
		return executor;
	}
	
	public void shutDownIfRequired() {
		if (pipeStatus.stopExecutorOnCompletion.get() && tracker.live().isEmpty()) {
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
