package koncept.sp.tracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import koncept.sp.pipe.state.ProcState;
import koncept.sp.tracker.internal.JobTrackerDefinition;

/**
 * Simple job tracker, using basic synchronisation to ensure exact state
 * @author nick
 *
 * @param <T>
 */
public class BlockingJobTracker<T> implements JobTrackerDefinition<T> {

	private final List<ProcState<T>> queued = new LinkedList<>();
	private final Set<ProcState<T>> live = new HashSet<>();
	
	@Override
	public List<ProcState<T>> queued() {
		return new ArrayList<ProcState<T>>(queued);
	}

	@Override
	public Set<ProcState<T>> live() {
		return new HashSet<ProcState<T>>(live);
	}

	@Override
	public synchronized void submitted(ProcState<T> state) {
		queued.add(state);
	}

	@Override
	public synchronized void started(ProcState<T> state) {
		queued.remove(state);
		live.add(state);
	}

	@Override
	public synchronized void completed(ProcState<T> state) {
		live.remove(state);
	}

	
	
}
