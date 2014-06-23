package koncept.sp.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

/**
 * A set of tests around the proc pipe future.
 * 
 * Includes some basic concurrency checks as well
 * 
 * @author nicholas.krul@gmail.com
 */
public class ProcPipeFutureTest {

	private final ProcPipeFuture<?> procPipeFuture = new ProcPipeFuture();
	private final Future<?> future = procPipeFuture;
	
	@Test
	public void initialState() throws Exception {
		assertFalse(future.isDone());
		assertFalse(future.isCancelled());
		try{
			future.get(1, TimeUnit.MILLISECONDS);
			fail("No return expected = no result to return");
		} catch (TimeoutException e) {
		}
	}
	
	@Test
	public void cancellationBeforeStarting() {
		future.cancel(false);
		assertTrue(future.isCancelled());
		assertFalse(procPipeFuture.markStarted()); //should return false - cannot start
	}
	
	//Hmm.... cancel() is a blocking call
	@Test
	public void twoPhaseCancellationAfterStarting() throws Exception {
		assertTrue(procPipeFuture.markStarted());
		assertFalse(procPipeFuture.cancelRequested());

		Thread cancelFutureThread = ensureThreadStarted(new Runnable(){
			public void run() {future.cancel(true);}
		});
		
		assertFalse(future.isCancelled());
		assertTrue(procPipeFuture.cancelRequested());

		procPipeFuture.acknowledgeCancellation();
		assertTrue(future.isCancelled());
		
		//ensure that we wait for the spawned thread to finish
		cancelFutureThread.join();
	}
	
	
	private Thread ensureThreadStarted(final Runnable r) throws InterruptedException {
		final AtomicBoolean executionHasStarted = new AtomicBoolean(false); 
		Thread thread = new Thread() {
			public void run() {
				synchronized(executionHasStarted) {
					executionHasStarted.set(true);
					executionHasStarted.notifyAll();
				}
				r.run();
			}
		};
		thread.start();
		
		while(!executionHasStarted.get()) 
			synchronized(executionHasStarted) {
				executionHasStarted.wait(50);
			}
		
		//try and allow the next part of the thread to exec.
		//note that its possible for no part of the runnable to have executed
		//possible, but unlikely.
		Thread.sleep(50); 
		
		
		return thread;
	}
	
}
