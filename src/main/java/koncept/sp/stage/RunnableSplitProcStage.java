package koncept.sp.stage;

import koncept.sp.ProcSplit;

public class RunnableSplitProcStage implements SplitProcStage {
	private final Runnable runnable;
	public RunnableSplitProcStage(Runnable runnable) {
		this.runnable = runnable;
	}
	public ProcSplit run(ProcSplit last) {
		runnable.run();
		return last;
	}
}