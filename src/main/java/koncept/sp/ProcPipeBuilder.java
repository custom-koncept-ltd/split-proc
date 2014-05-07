package koncept.sp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import koncept.sp.pipe.ProcPipe;
import koncept.sp.pipe.SingleExecutorProcPipe;
import koncept.sp.resource.ProcTerminator;
import koncept.sp.resource.SimpleProcTerminator;
import koncept.sp.stage.RunnableSplitProcStage;
import koncept.sp.stage.SplitProcStage;

/**
 * 
 * Builder interface for a simple proc pipe
 * 
 * @author nicholas.krul@gmail.com
 *
 * @param <T>
 */
public class ProcPipeBuilder<T> {
	private List<SplitProcStage> stages = new ArrayList<SplitProcStage>();
	private ProcTerminator<T> terminator = new SimpleProcTerminator<T>(null);
	
	public ProcPipe inThreadProcPipe(ExecutorService executor) {
		if (stages.isEmpty())
			throw new RuntimeException("No stages");
		return new SingleExecutorProcPipe(executor, stages, terminator);
	}
	
	public ProcPipeBuilder<T> addStage(SplitProcStage stage) {
		stages.add(stage);
		return this;
	}
	public ProcPipeBuilder<T> addStage(Runnable stage) {
		stages.add(new RunnableSplitProcStage(stage));
		return this;
	}
	
	public ProcPipeBuilder<T> withTerminator(ProcTerminator<T> terminator) {
		this.terminator = terminator;
		return this;
	}
	
}
