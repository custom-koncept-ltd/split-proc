package koncept.sp.pipe;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import koncept.sp.ProcSplit;
import koncept.sp.resource.SimpleProcTerminator;
import koncept.sp.stage.TrackedExceptionSplitProcStage;
import koncept.sp.stage.TrackedSplitProcStage;
import koncept.sp.stage.WaitForExecutionSplitStage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ErrorTest {

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
	public void cancelWillInterruptPipe() throws Exception {
		TrackedSplitProcStage stage1 = new TrackedSplitProcStage();
		WaitForExecutionSplitStage stage2 = new WaitForExecutionSplitStage();
		TrackedExceptionSplitProcStage stage3 = new TrackedExceptionSplitProcStage();
		TrackedSplitProcStage stage4 = new TrackedSplitProcStage();
		
		
		SingleExecutorProcPipe<Object> executorProcPipe = 
				new SingleExecutorProcPipe<Object>(
						executor,  
						Arrays.asList(stage1, stage2, stage3, stage4),
						new SimpleProcTerminator(null));
		
		Future<Object> procPipeFuture = executorProcPipe.submit(new ProcSplit());
		
		try { //has to fail with the test exception
			procPipeFuture.get();
			fail("Should have thrown an exception");
		} catch (ExecutionException e) {
			assertThat(e.getCause().getMessage(), is(TrackedExceptionSplitProcStage.TEST_EXCEPTION_MESSAGE));
		}
		
		assertTrue(procPipeFuture.isDone());
		
		assertThat(stage1.count(), is(1));
		assertThat(stage4.count(), is(0));
	}
}
