package koncept.sp.resource;

public class NonCleanableResource implements CleanableResource {

	private final Object resource;
	
	public NonCleanableResource(Object resource) {
		this.resource = resource;
	}
	
	@Override
	public Object get() {
		return resource;
	}

	@Override
	public void clean() throws Exception {
	}

}
