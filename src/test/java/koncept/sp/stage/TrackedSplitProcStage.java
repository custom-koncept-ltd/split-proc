package koncept.sp.stage;

import java.util.ArrayList;
import java.util.List;

import koncept.sp.ProcData;
import koncept.sp.pipe.state.ProcState;

public class TrackedSplitProcStage implements SplitProcStage {
	private List<Long> executionTimes = new ArrayList<Long>();
	public ProcData run(ProcState last) throws Exception {
		executionTimes.add(System.currentTimeMillis());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return last.getData();
	}
	
	public int count() {
		return executionTimes.size();
	}
	
	public List<Long> executionTimes() {
		return executionTimes;
	}
}
