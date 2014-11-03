package koncept.sp.tracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import koncept.sp.pipe.internal.InternalProcState;
import koncept.sp.tracker.internal.JobTrackerDefinition;

/**
 * Simple job tracker, using basic synchronisation to ensure exact state
 * @author nick
 *
 * @param <T>
 */
public class BlockingJobTracker<T> implements JobTrackerDefinition<T> {

	private final List<InternalProcState<T>> queued = new LinkedList<>();
	private final Set<InternalProcState<T>> live = new HashSet<>();
	
	@Override
	public List<InternalProcState<T>> queued() {
		return new ArrayList<InternalProcState<T>>(queued);
	}

	@Override
	public Set<InternalProcState<T>> live() {
		return new HashSet<InternalProcState<T>>(live);
	}

	@Override
	public synchronized void submitted(InternalProcState<T> state) {
		queued.add(state);
	}

	@Override
	public synchronized void started(InternalProcState<T> state) {
		queued.remove(state);
		live.add(state);
	}

	@Override
	public synchronized void completed(InternalProcState<T> state) {
		live.remove(state);
	}

	
	
}
