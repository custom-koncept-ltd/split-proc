package koncept.sp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import koncept.sp.resource.CleanableResource;
import koncept.sp.resource.SimpleCleanableResource;

public class ProcSplit {
	public static final String DEFAULT_VALUE_KEY = "value";
	private final Map<String, CleanableResource> cleanableResources;

	public ProcSplit() {
		this(new HashMap<String, CleanableResource>());
	}
	
	public ProcSplit(ProcSplit previousStep) {
		this(new HashMap<String, CleanableResource>(previousStep.cleanableResources));
	}
	
	public ProcSplit(CleanableResource value) {
		this();
		add(DEFAULT_VALUE_KEY, value);
	}
	
	private ProcSplit(Map<String, CleanableResource> cleanableResources) {
		this.cleanableResources = cleanableResources;
	}
	
	public CleanableResource getCleanableResource(String name) {
		return cleanableResources.get(name);
	}
	
	public void removeCleanableResource(String name) {
		cleanableResources.remove(name);
	}
	
	public ProcSplit add(String name, CleanableResource cleanableResource) {
		cleanableResources.put(name, cleanableResource);
		return this;
	}
	
	public Object get(String name) {
		CleanableResource cleanableResource = cleanableResources.get(name);
		return cleanableResource == null ? null : cleanableResource.get();
	}
	
	public ProcSplit clean(String name) {
		CleanableResource cleanableResource = cleanableResources.remove(name);
		if (cleanableResource != null)
			cleanableResource.clean();
		return this;
	}
	
	public Collection<String> getResourceNames() {
		return new HashSet<String>(cleanableResources.keySet());
	}

	@Override
	public String toString() {
		return cleanableResources.toString();
	}
}
