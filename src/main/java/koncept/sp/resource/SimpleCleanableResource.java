package koncept.sp.resource;

import koncept.sp.ResourceCleaner;

public class SimpleCleanableResource implements CleanableResource {

	private final Object resource;
	private final ResourceCleaner cleaner;
	
	public SimpleCleanableResource(Object resource, ResourceCleaner cleaner) {
		this.resource = resource;
		this.cleaner = cleaner;
	}
	
	public Object get() {
		return resource;
	}
	
	public void clean() {
		if (cleaner != null)
			cleaner.clean(resource);
	}
	
	@Override
	public String toString() {
		return "{resource=" + resource + ", cleaner=" + cleaner + "}";
	}
	
}
