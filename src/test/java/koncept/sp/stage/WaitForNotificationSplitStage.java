package koncept.sp.stage;

import java.util.concurrent.atomic.AtomicBoolean;

import koncept.sp.ProcSplit;

/**
 * This class is designed for an external observer to wait for the stage to execute.
 * Note that this will NOT wait forever
 * @author koncept
 *
 */
public class WaitForNotificationSplitStage implements SplitProcStage {

	private AtomicBoolean hasBeenNotified = new AtomicBoolean(false);
	private final long timeout;
	
	public WaitForNotificationSplitStage() {
		timeout = 500;
	}
	
	public WaitForNotificationSplitStage(long timeout) {
		this.timeout = timeout;
	}
	
	
	public ProcSplit run(ProcSplit last) throws Exception {
		synchronized (hasBeenNotified) {
			if (!hasBeenNotified.get())
				hasBeenNotified.wait(timeout);
		}
		return last;
	}
	
	public void allowExecute() {
		synchronized(hasBeenNotified) {
			hasBeenNotified.set(true);
			hasBeenNotified.notify();
		}
	}
	
	public void reset() {
		hasBeenNotified.set(false);
	}

}
