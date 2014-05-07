package koncept.sp.stage;

import java.util.ArrayList;
import java.util.List;

import koncept.sp.ProcSplit;

public class TrackedSplitProcStage implements SplitProcStage {
	private List<Long> executionTimes = new ArrayList<Long>();
	public ProcSplit run(ProcSplit last) {
		executionTimes.add(System.currentTimeMillis());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return last;
	}
	
	public int count() {
		return executionTimes.size();
	}
	
	public List<Long> executionTimes() {
		return executionTimes;
	}
}
