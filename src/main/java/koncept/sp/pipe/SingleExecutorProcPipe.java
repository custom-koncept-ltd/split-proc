package koncept.sp.pipe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import koncept.sp.ProcSplit;
import koncept.sp.future.ProcPipeFuture;
import koncept.sp.resource.ProcPipeCleaner;
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
public class SingleExecutorProcPipe implements ProcPipeDefinition, ProcPipe {
	private final ExecutorService executor;
	private final List<SplitProcStage> stages;
	
	private ProcPipeCleaner completionHandler = new SimpleProcPipeCleaner();
	private ProcPipeCleaner errorHandler = new SimpleProcPipeCleaner();
	
	public SingleExecutorProcPipe(ExecutorService executor, List<SplitProcStage> stages) {
		this.stages = stages;
		this.executor = executor;
	}
	
	public void setCompletionHandler(ProcPipeCleaner completionHandler) {
		this.completionHandler = completionHandler;
	}
	
	public void setErrorHandler(ProcPipeCleaner errorHandler) {
		this.errorHandler = errorHandler;
	}
	
	public Future<Boolean> handle(ProcSplit in) {
		ProcPipeFuture<Boolean> futureResult = new ProcPipeFuture<Boolean>();
		executor.execute(new ProcStageThread(this, futureResult, 0, in));
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
	
	public ProcPipeCleaner getCompletionHandler() {
		return completionHandler;
	}
	
	public ProcPipeCleaner getErrorHandler() {
		return errorHandler;
	}
	
}
