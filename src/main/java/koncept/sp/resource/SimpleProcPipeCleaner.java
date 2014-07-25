package koncept.sp.resource;

import koncept.sp.ProcSplit;

public class SimpleProcPipeCleaner implements ProcPipeCleaner {
	public void clean(ProcSplit last) throws Exception {
		if(last != null) for(String resourceName: last.getResourceNames())
			last.clean(resourceName);
	}
}