package koncept.sp.pipe;

import java.util.concurrent.ExecutorService;

import koncept.sp.resource.ProcPipeCleaner;
import koncept.sp.stage.SplitProcStage;


public interface ProcPipeDefinition  {
	
	public ExecutorService getExecutor(int currentStage);
	public SplitProcStage getStage(int currentStage);
	public int getNumberOfStages();
	
	public ProcPipeCleaner getErrorHandler();
	public ProcPipeCleaner getCompletionHandler();
}
