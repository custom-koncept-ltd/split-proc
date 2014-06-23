package koncept.sp.tracker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import koncept.sp.pipe.state.ProcState;
import koncept.sp.tracker.internal.JobTrackerDefinition;

public class NullJobTracker<T> implements JobTrackerDefinition<T> {

	@Override
	public List<ProcState<T>> queued() {
		return Collections.emptyList();
	}

	@Override
	public Set<ProcState<T>> live() {
		return Collections.emptySet();
	}

	@Override
	public void submitted(ProcState<T> state) {
	}

	@Override
	public void started(ProcState<T> state) {
	}

	@Override
	public void completed(ProcState<T> state) {
	}

	
	
}
