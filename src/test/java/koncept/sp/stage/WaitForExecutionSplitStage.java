package koncept.sp.stage;

import java.util.concurrent.atomic.AtomicBoolean;

import koncept.sp.ProcData;
import koncept.sp.pipe.state.ProcState;

/**
 * This class is designed for an external observer to wait for the stage to execute
 * @author koncept
 *
 */
public class WaitForExecutionSplitStage implements SplitProcStage {

	private AtomicBoolean hasExecuted = new AtomicBoolean(false);
	
	public ProcData run(ProcState last) {
		synchronized(hasExecuted) {
			hasExecuted.set(true);
			hasExecuted.notify();
		}
		return last.getData();
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
