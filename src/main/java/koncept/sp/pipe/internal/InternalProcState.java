package koncept.sp.pipe.internal;

import koncept.sp.ProcData;
import koncept.sp.future.ProcPipeFuture;

/**
 * Only tracks the last proc state, because we would like to allow the
 * garbage collector to be able to clean up when it wants
 * @author nick
 *
 */
public class InternalProcState<T> {

	private final ProcPipeFuture<T> procPipeFuture;
	private volatile int currentIndex;
	private ProcData data;
	
	public InternalProcState(ProcPipeFuture<T> procPipeFuture, ProcData initial) {
		this.procPipeFuture = procPipeFuture;
		data = initial;
		currentIndex = 0;
	}
	
	public ProcData data() {
		return data;
	}
	
	public ProcPipeFuture<T> future() {
		return procPipeFuture;
	}
	
	public int currentIndex() {
		return currentIndex;
	}
	
	public void increment(ProcData data) {
		currentIndex++;
		this.data = data;
	}
	
	public boolean cancelRequested() {
		return procPipeFuture.cancelRequested();
	}
	
	public void markStarted() {
		procPipeFuture.markStarted();
	}	
	
}
