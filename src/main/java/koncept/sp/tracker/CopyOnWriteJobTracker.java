package koncept.sp.tracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import koncept.sp.pipe.internal.InternalProcState;
import koncept.sp.tracker.internal.JobTrackerDefinition;

public class CopyOnWriteJobTracker<T> implements JobTrackerDefinition<T> {

	private final CopyOnWriteArrayList<InternalProcState<T>> queued = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArraySet<InternalProcState<T>> live = new CopyOnWriteArraySet<>();
	
	@Override
	public List<InternalProcState<T>> queued() {
		return new ArrayList<InternalProcState<T>>(queued);
	}

	@Override
	public Set<InternalProcState<T>> live() {
		return new HashSet<InternalProcState<T>>(live);
	}

	@Override
	public void submitted(InternalProcState<T> state) {
		queued.add(state);
	}

	@Override
	public void started(InternalProcState<T> state) {
		queued.remove(state);
		live.add(state);
	}

	@Override
	public void completed(InternalProcState<T> state) {
		live.remove(state);
	}

	
	
}
