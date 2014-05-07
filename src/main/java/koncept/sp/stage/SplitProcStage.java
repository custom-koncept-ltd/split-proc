package koncept.sp.stage;

import koncept.sp.ProcSplit;

public interface SplitProcStage {
	public ProcSplit run(ProcSplit last);
}
