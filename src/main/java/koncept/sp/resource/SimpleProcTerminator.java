package koncept.sp.resource;

import koncept.sp.ProcData;

public class SimpleProcTerminator<T> implements ProcTerminator<T> {

	private final String key;
	private final ProcPipeCleaner cleaner;
	
	public SimpleProcTerminator() {
		this(ProcData.DEFAULT_VALUE_KEY);
	}
	
	public SimpleProcTerminator(String key) {
		this(key, new SimpleProcPipeCleaner());
	}
	
	public SimpleProcTerminator(String key, ProcPipeCleaner cleaner) {
		this.key = key;
		this.cleaner = cleaner;
	}
	
	@Override
	public T extractFinalResult(ProcData finalResult) {
		CleanableResource resource = finalResult.removeCleanableResource(key);
		return resource == null ? null : (T)resource.get();
	}

	@Override
	public void clean(ProcData finalResult) throws Exception {
		cleaner.clean(finalResult);
	}

}
