package koncept.sp.tracker;

import java.util.List;
import java.util.Set;

import koncept.sp.pipe.internal.InternalProcState;

public interface JobTracker<T> {

	List<InternalProcState<T>> queued();
	Set<InternalProcState<T>> live();
	
}
