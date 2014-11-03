package koncept.sp.stage;

import koncept.sp.ProcData;
import koncept.sp.pipe.state.ProcState;

public interface SplitProcStage {
	public ProcData run(ProcState state) throws Exception;
}
