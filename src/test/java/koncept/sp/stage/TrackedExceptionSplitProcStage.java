package koncept.sp.stage;

import koncept.sp.ProcSplit;

public class TrackedExceptionSplitProcStage extends TrackedSplitProcStage {
	
	public static final String TEST_EXCEPTION_MESSAGE = "Test Exception Message";
	
	public ProcSplit run(ProcSplit last) throws Exception {
		super.run(last);
		throw new Exception(TEST_EXCEPTION_MESSAGE);
	}
	
}
