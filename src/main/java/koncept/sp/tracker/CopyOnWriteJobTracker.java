package koncept.sp.tracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import koncept.sp.pipe.state.ProcState;
import koncept.sp.tracker.internal.JobTrackerDefinition;

public class CopyOnWriteJobTracker<T> implements JobTrackerDefinition<T> {

	private final CopyOnWriteArrayList<ProcState<T>> queued = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArraySet<ProcState<T>> live = new CopyOnWriteArraySet<>();
	
	@Override
	public List<ProcState<T>> queued() {
		return new ArrayList<ProcState<T>>(queued);
	}

	@Override
	public Set<ProcState<T>> live() {
		return new HashSet<ProcState<T>>(live);
	}

	@Override
	public void submitted(ProcState<T> state) {
		queued.add(state);
	}

	@Override
	public void started(ProcState<T> state) {
		queued.remove(state);
		live.add(state);
	}

	@Override
	public void completed(ProcState<T> state) {
		live.remove(state);
	}

	
	
}
