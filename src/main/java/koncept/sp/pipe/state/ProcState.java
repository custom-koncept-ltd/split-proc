package koncept.sp.pipe.state;

import koncept.sp.ProcSplit;
import koncept.sp.future.ProcPipeFuture;

/**
 * Only tracks the last proc state, because we would like to allow the
 * garbage collector to be able to clean up when it wants
 * @author nick
 *
 */
public class ProcState<T> {

	private final ProcPipeFuture<T> procPipeFuture;
//	private final List<ProcSplit> splits = new ArrayList<ProcSplit>();
	private int currentIndex = -1;
	private ProcSplit lastSplit;
	
	public ProcState(ProcPipeFuture<T> procPipeFuture, ProcSplit initial) {
		this.procPipeFuture = procPipeFuture;
		addSplit(initial);
	}
	
	public void addSplit(ProcSplit split) {
//		splits.add(split);
		lastSplit = split;
		currentIndex++;
	}
	
	public ProcSplit getLastSplit() {
//		return splits.get(splits.size() - 1);
		return lastSplit;
	}
	
	public ProcPipeFuture<T> getProcPipeFuture() {
		return procPipeFuture;
	}
	
	public int getNextStage() {
//		return splits.size() - 1;
		return currentIndex;
	}
	
	public boolean isCancellationRequested() {
		return procPipeFuture.cancelRequested();
	}
	
	public void markStarted() {
		procPipeFuture.markStarted();
	}	
	
}
