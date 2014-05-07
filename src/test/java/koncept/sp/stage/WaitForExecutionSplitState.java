package koncept.sp.stage;

import java.util.concurrent.atomic.AtomicBoolean;

import koncept.sp.ProcSplit;

public class WaitForExecutionSplitState implements SplitProcStage {

	private AtomicBoolean hasExecuted = new AtomicBoolean(false);
	
	public ProcSplit run(ProcSplit last) {
		synchronized(hasExecuted) {
			hasExecuted.set(true);
			hasExecuted.notify();
		}
		return last;
	}
	
	public void waitForExecution(long timeout) throws InterruptedException {
		synchronized (hasExecuted) {
			if (hasExecuted.get()) return;
			hasExecuted.wait(timeout);
		}
	}
	
	public void reset() {
		hasExecuted.set(false);
	}

}
