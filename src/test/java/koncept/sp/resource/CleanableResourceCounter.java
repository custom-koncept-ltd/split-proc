package koncept.sp.resource;

public class CleanableResourceCounter implements CleanableResource {
	public int gets;
	public int cleans;
	public void clean() {
		cleans++;
	}
	public Object get() {
		gets++;
		return this;
	}
}
