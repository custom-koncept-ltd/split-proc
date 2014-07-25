package koncept.sp.resource;

import koncept.sp.ProcSplit;

public interface ProcPipeCleaner {

	public void clean(ProcSplit last) throws Exception;
	
}
