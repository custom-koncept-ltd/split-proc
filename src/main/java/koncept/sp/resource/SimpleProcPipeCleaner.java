package koncept.sp.resource;

import koncept.sp.ProcData;

public class SimpleProcPipeCleaner implements ProcPipeCleaner {
	public void clean(ProcData last) throws Exception {
		if(last != null) for(String resourceName: last.getResourceNames())
			last.clean(resourceName);
	}
}