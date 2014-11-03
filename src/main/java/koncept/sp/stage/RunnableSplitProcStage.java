package koncept.sp.stage;

import koncept.sp.ProcData;
import koncept.sp.pipe.state.ProcState;

public class RunnableSplitProcStage implements SplitProcStage {
	private final Runnable runnable;
	public RunnableSplitProcStage(Runnable runnable) {
		this.runnable = runnable;
	}
	public ProcData run(ProcState last) {
		runnable.run();
		return last.getData();
	}
}