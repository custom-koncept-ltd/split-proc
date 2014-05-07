package koncept.sp.pipe;

import koncept.sp.ProcSplit;
import koncept.sp.future.ProcPipeFuture;

public class ProcStageThread implements Runnable {
	private final ProcPipeDefinition pipeDefinition;
	private final ProcPipeFuture<Boolean> futureResult;
	private final int currentIndex;
	private final ProcSplit in;
	public ProcStageThread(ProcPipeDefinition pipeDefinition, ProcPipeFuture<Boolean> futureResult, int currentIndex, ProcSplit in) {
		this.pipeDefinition = pipeDefinition;
		this.futureResult = futureResult;
		this.currentIndex = currentIndex;
		this.in = in;
	}
	
	public void run() {
		if(futureResult.cancelRequested()) {
			futureResult.acknowledgeCancellation();
			return;
		}
		
		if (currentIndex == 0)
			futureResult.markStarted();
		
		try {
			ProcSplit out = pipeDefinition.getStage(currentIndex).run(in);
			if (currentIndex + 1 < pipeDefinition.getNumberOfStages()) {
				pipeDefinition.getExecutor(currentIndex + 1).execute(new ProcStageThread(pipeDefinition, futureResult, currentIndex + 1, out));
			} else {
				pipeDefinition.getCompletionHandler().clean(out);
				futureResult.markCompleted(true);
			}
		} catch (Throwable t) {
			pipeDefinition.getErrorHandler().clean(in);
			futureResult.markErrored(t);
		}
		
	}
}
