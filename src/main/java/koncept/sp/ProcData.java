package koncept.sp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import koncept.sp.resource.CleanableResource;

public class ProcData {
	public static final String DEFAULT_VALUE_KEY = "value";
	private final List<NamedCleanableResource> namedCleanableResources;

	public ProcData() {
		this(new LinkedList<NamedCleanableResource>());
	}
	
	public ProcData(ProcData previousStep) {
		this(new LinkedList<NamedCleanableResource>(previousStep.namedCleanableResources));
	}
	
	public ProcData(CleanableResource value) {
		this();
		add(DEFAULT_VALUE_KEY, value);
	}
	
	private ProcData(List<NamedCleanableResource> cleanableResources) {
		this.namedCleanableResources = cleanableResources;
	}
	
	public CleanableResource getCleanableResource(String name) {
		for(NamedCleanableResource namedCleanableResource: namedCleanableResources)
			if (namedCleanableResource.name.equals(name))
				return namedCleanableResource.cleanableResources;
		return null;
	}
	
	public CleanableResource removeCleanableResource(String name) {
		for(NamedCleanableResource namedCleanableResource: namedCleanableResources)
			if (namedCleanableResource.name.equals(name)) {
				namedCleanableResources.remove(namedCleanableResource);
				return namedCleanableResource.cleanableResources;
			}
		return null;
	}
	
	//push it to the front - the default cleanup will clean newer resources first (its a stack)
	public ProcData add(String name, CleanableResource cleanableResource) {
		namedCleanableResources.add(0, new NamedCleanableResource(name, cleanableResource));
		return this;
	}
	
	public Object getResource(String name) {
		CleanableResource cleanableResource = getCleanableResource(name);
		return cleanableResource == null ? null : cleanableResource.get();
	}
	
	public ProcData clean(String name) throws Exception {
		CleanableResource cleanableResource = removeCleanableResource(name);
		if (cleanableResource != null)
			cleanableResource.clean();
		return this;
	}
	
	public ProcData clean() throws Exception {
		for(NamedCleanableResource namedCleanableResource: namedCleanableResources)
			namedCleanableResource.cleanableResources.clean();
		namedCleanableResources.clear();
		return this;
	}
	
	public List<String> getResourceNames() {
		List<String> names = new ArrayList<>(namedCleanableResources.size());
		for(NamedCleanableResource namedCleanableResource: namedCleanableResources)
			names.add(namedCleanableResource.name);
		return names;
	}

	@Override
	public String toString() {
		return namedCleanableResources.toString();
	}
	
	private static class NamedCleanableResource {
		private final String name;
		private final CleanableResource cleanableResources;
		
		public NamedCleanableResource(String name, CleanableResource cleanableResources) {
			this.name = name;
			this.cleanableResources = cleanableResources;
		}
	}
}
