package koncept.sp.pipe;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import koncept.sp.ProcSplit;
import koncept.sp.resource.SimpleProcTerminator;
import koncept.sp.stage.TrackedSplitProcStage;
import koncept.sp.stage.WaitForExecutionSplitStage;
import koncept.sp.stage.WaitForNotificationSplitStage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InterruptionTest {

	@Before
	public void init() {
		executor = Executors.newFixedThreadPool(1);
	}
	
	@After
	public void clean() {
		executor.shutdown();
	}
	
	private ExecutorService executor;

	@Test
	public void cancelAndInterruptSplit() throws Exception {
		TrackedSplitProcStage stage1 = new TrackedSplitProcStage();
		WaitForExecutionSplitStage stage2 = new WaitForExecutionSplitStage();
		WaitForNotificationSplitStage stage3 = new WaitForNotificationSplitStage();
		TrackedSplitProcStage stage4 = new TrackedSplitProcStage();
		
		
		SingleExecutorProcPipe<Object> executorProcPipe = 
				new SingleExecutorProcPipe<Object>(
						executor,  
						Arrays.asList(stage1, stage2, stage3, stage4),
						new SimpleProcTerminator(null));
		
		Future<Object> procPipeFuture = executorProcPipe.submit(new ProcSplit());
		
		stage2.waitForExecution(500); //wait for stage 2 to execute
		assertFalse(procPipeFuture.isCancelled());
		boolean cancelRequested = procPipeFuture.cancel(true);
		assertTrue(cancelRequested);
		stage3.allowExecute();
		
		try {
		   //needs to be able to wait till the future is completely completed
			procPipeFuture.get(500, MILLISECONDS);
			fail();
		} catch (CancellationException e) {
		}
		
		assertThat(stage1.count(), is(1));
		assertThat(stage4.count(), is(0));
		
		assertTrue(procPipeFuture.isCancelled());
		assertTrue(procPipeFuture.isDone());
		
		
	}
	
	@Test
	public void cancelAndAllowSplitToComplete() throws Exception {
		TrackedSplitProcStage stage1 = new TrackedSplitProcStage();
		WaitForExecutionSplitStage stage2 = new WaitForExecutionSplitStage();
		WaitForNotificationSplitStage stage3 = new WaitForNotificationSplitStage();
		TrackedSplitProcStage stage4 = new TrackedSplitProcStage();
		
		
		SingleExecutorProcPipe<Object> executorProcPipe = 
				new SingleExecutorProcPipe<Object>(
						executor,  
						Arrays.asList(stage1, stage2, stage3, stage4),
						new SimpleProcTerminator(null));
		
		Future<Object> procPipeFuture = executorProcPipe.submit(new ProcSplit());
		
		stage2.waitForExecution(500); //wait for stage 2 to execute
		assertFalse(procPipeFuture.isCancelled());
		boolean cancelRequested = procPipeFuture.cancel(false);
		assertFalse(cancelRequested); //FALSE - not able to interrupt (already started)
		stage3.allowExecute();
		
	   //needs to be able to wait till the future is completely completed
		Object value = procPipeFuture.get(500, MILLISECONDS);
		assertNull(value);
		
		assertThat(stage1.count(), is(1));
		assertThat(stage4.count(), is(1));
		
		assertFalse(procPipeFuture.isCancelled());
		assertTrue(procPipeFuture.isDone());
		
	}
	
}
