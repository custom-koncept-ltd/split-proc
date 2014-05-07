package koncept.sp.resource;

import koncept.sp.ProcSplit;

public class SimpleProcTerminator<T> extends ProcTerminator<T> {

	private final String key;
	private final ProcPipeCleaner cleaner;
	
	public SimpleProcTerminator(String key) {
		this(key, new SimpleProcPipeCleaner());
	}
	
	public SimpleProcTerminator(String key, ProcPipeCleaner cleaner) {
		this.key = key;
		this.cleaner = cleaner;
	}
	
	@Override
	public T extractFinalResult(ProcSplit finalResult) {
		CleanableResource resource = finalResult.removeCleanableResource(key);
		return resource == null ? null : (T)resource.get();
	}

	@Override
	public void clean(ProcSplit finalResult) {
		cleaner.clean(finalResult);
	}

}
