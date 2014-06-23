package koncept.sp.tracker;

import java.util.List;
import java.util.Set;

import koncept.sp.pipe.state.ProcState;

public interface JobTracker<T> {

	List<ProcState<T>> queued();
	Set<ProcState<T>> live();
	
}
