package koncept.sp.resource;

import koncept.sp.ProcSplit;

public abstract class ProcTerminator<T> {

	public T terminate(ProcSplit finalResult) {
		T result = extractFinalResult(finalResult);
		clean(finalResult);
		return result;
	}
	
	public abstract T extractFinalResult(ProcSplit finalResult);
	
	public abstract void clean(ProcSplit finalResult);
	
}
