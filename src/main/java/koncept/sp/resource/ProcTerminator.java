package koncept.sp.resource;

import koncept.sp.ProcData;

public interface ProcTerminator<T> {
	
	public T extractFinalResult(ProcData finalResult);
	public void clean(ProcData finalResult) throws Exception;
	
}
