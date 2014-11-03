package koncept.sp.stage;

import koncept.sp.ProcData;
import koncept.sp.pipe.state.ProcState;

public class TrackedExceptionSplitProcStage extends TrackedSplitProcStage {
	
	public static final String TEST_EXCEPTION_MESSAGE = "TrackedExceptionSplitProcStage Test Exception Message";
	
	public ProcData run(ProcState last) throws Exception {
		super.run(last);
		throw new Exception(TEST_EXCEPTION_MESSAGE);
	}
	
}
