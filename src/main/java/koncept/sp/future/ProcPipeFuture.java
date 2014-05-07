package koncept.sp.future;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * 
 * Generic future implementation.<br/>
 * Includes full cancellation ability
 * 
 * @author nicholas.krul@gmail.com
 *
 * @param <V>
 */
public class ProcPipeFuture<V> implements Future<V> {
	private final Object lock = new Object();
	private boolean cancelRequested;
	private boolean cancelled;
	private boolean started;
	private boolean completed;
	private V result; //N.B. - there is no reason that the result can't be null
	private Throwable error;
	
	public boolean markStarted() {
		synchronized(lock) {
			if (cancelled) return false;
			started = true;
			return true;
		}
	}
	
	public void markErrored(Throwable error) {
		synchronized(lock) {
			completed = true;
			this.error = error;
			lock.notifyAll();
		}
	}
	
	public void markCompleted(V result) {
		synchronized(lock) {
			completed = true;
			this.result = result;
			lock.notifyAll();
		}
	}
	
	public void acknowledgeCancellation() {
		synchronized(lock) {
			cancelled = true;
			lock.notifyAll();
		}
	}
	
	public boolean cancelRequested() {
		synchronized(lock) {
			return cancelRequested;
		}
	}
	
	/*
cancel
boolean cancel(boolean mayInterruptIfRunning)
Attempts to cancel execution of this task. This attempt will fail if the task has already completed, has already been cancelled, or could not be cancelled for some other reason. If successful, and this task has not started when cancel is called, this task should never run. If the task has already started, then the mayInterruptIfRunning parameter determines whether the thread executing this task should be interrupted in an attempt to stop the task. 
After this method returns, subsequent calls to isDone() will always return true. Subsequent calls to isCancelled() will always return true if this method returned true.

Parameters:
mayInterruptIfRunning - true if the thread executing this task should be interrupted; otherwise, in-progress tasks are allowed to complete 
Returns:
false if the task could not be cancelled, typically because it has already completed normally; true otherwise
	 */
	public boolean cancel(boolean mayInterruptIfRunning ) {
		synchronized(lock) {
			if (completed)
				return false;
			
			if (!started) {
				cancelled = true;
				return true;
			} else if (mayInterruptIfRunning) {
				cancelRequested = true;
				try {
					lock.wait(); //will wait till cancel is acknowledged, or the job completes
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				if (completed) return false;
				return true;
			} else {
				return false; //started, not 'mayInterruptIfRunning'
			}
		}
	}
	
	/*
get
V get()
      throws InterruptedException,
             ExecutionException
Waits if necessary for the computation to complete, and then retrieves its result.
Returns:
the computed result 
Throws: 
CancellationException - if the computation was cancelled 
ExecutionException - if the computation threw an exception 
InterruptedException - if the current thread was interrupted while waiting
	 */
	public V get() throws InterruptedException, ExecutionException {
		synchronized(lock) {
			if (cancelled)
				throw new CancellationException();
			if (completed) {
				if (error != null) throw new ExecutionException(error);
				return result;
			}
			//otherwise wait
			lock.wait();
			
			if (cancelled)
				throw new CancellationException();
			
			//if it wasn't cancelled, and the lock was passed, it must have completed
			if (error != null) throw new ExecutionException(error);
			return result;
		}
	}
	
	/*
get
V get(long timeout,
    TimeUnit unit)
      throws InterruptedException,
             ExecutionException,
             TimeoutException
Waits if necessary for at most the given time for the computation to complete, and then retrieves its result, if available.
Parameters:
timeout - the maximum time to wait
unit - the time unit of the timeout argument 
Returns:
the computed result 
Throws: 
CancellationException - if the computation was cancelled 
ExecutionException - if the computation threw an exception 
InterruptedException - if the current thread was interrupted while waiting 
TimeoutException - if the wait timed out
	 */
	public V get(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException ,ExecutionException ,java.util.concurrent.TimeoutException {
		long millisecondsTimout = unit.toMillis(timeout);
		synchronized(lock) {
			if (cancelled)
				throw new CancellationException();
			if (completed) {
				if (error != null) throw new ExecutionException(error);
				return result;
			}
			
			//otherwise wait
			lock.wait(millisecondsTimout);

			
			if (cancelled)
				throw new CancellationException();
			
			if (!completed)
				throw new TimeoutException(); // wait time expired, not yet completed (or cancelled)
			
			//if it wasn't cancelled, and the lock was passed, it must have completed
			if (error != null) throw new ExecutionException(error);
			return result;
		}
	};
	
	public boolean isCancelled() {
		synchronized(lock) {
			return cancelled;
		}
	}
	
	public boolean isDone() {
		synchronized(lock) {
			return completed || cancelled;
		}
	}
}
