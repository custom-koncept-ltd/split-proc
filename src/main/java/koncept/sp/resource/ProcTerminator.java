package koncept.sp.resource;

import koncept.sp.ProcSplit;

public interface ProcTerminator<T> {
	
	public T extractFinalResult(ProcSplit finalResult);
	public void clean(ProcSplit finalResult);
	
}
