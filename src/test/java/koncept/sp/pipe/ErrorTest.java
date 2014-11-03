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

import koncept.sp.ProcData;
import koncept.sp.resource.ProcPipeCleaner;
import koncept.sp.resource.SimpleProcPipeCleaner;
import koncept.sp.resource.SimpleProcTerminator;
import koncept.sp.stage.TrackedExceptionSplitProcStage;
import koncept.sp.stage.TrackedSplitProcStage;
import koncept.sp.stage.WaitForExecutionSplitStage;
import koncept.sp.tracker.BlockingJobTracker;
import koncept.util.ExceptionRecordingLogger;
import koncept.util.ExceptionRecordingLogger.LogMessage;

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
		
		ExceptionRecordingLogger logger = new ExceptionRecordingLogger();
		
		SingleExecutorProcPipe<Object> executorProcPipe = 
				new SingleExecutorProcPipe(
						logger,
						new BlockingJobTracker(),
						executor,  
						Arrays.asList(stage1, stage2, stage3, stage4),
						new SimpleProcTerminator(null),
						new SimpleProcPipeCleaner());
		
		Future<Object> procPipeFuture = executorProcPipe.submit(new ProcData());
		
		try { //has to fail with the test exception
			procPipeFuture.get();
			fail("Should have thrown an exception");
		} catch (ExecutionException e) {
			assertThat(e.getCause().getMessage(), is(TrackedExceptionSplitProcStage.TEST_EXCEPTION_MESSAGE));
		}
		
		assertTrue(procPipeFuture.isDone());
		
		assertThat(stage1.count(), is(1));
		assertThat(stage4.count(), is(0));
//		logger.output(System.out);
	}
	
	@Test
	public void exceptionOnErrorIsHandled() throws Exception {
		ProcPipeCleaner exceptionThrowingStub = new ExceptionThrowingStub();
		ExceptionRecordingLogger logger = new ExceptionRecordingLogger();
		SingleExecutorProcPipe<Object> executorProcPipe = 
				new SingleExecutorProcPipe (
						logger,
						new BlockingJobTracker(),
						executor,  
						Arrays.asList(new TrackedExceptionSplitProcStage()),
						new SimpleProcTerminator(null),
						exceptionThrowingStub);
		
		Future<Object> procPipeFuture = executorProcPipe.submit(new ProcData());
		
		//wait for the future to be fulfilled - will be an error in this case (!!)
		try {
			procPipeFuture.get();
		} catch (ExecutionException e) {
			assertThat(e.getCause().getMessage(), is(TrackedExceptionSplitProcStage.TEST_EXCEPTION_MESSAGE));
		}
		
		boolean foundErrorCleanerException = false;
		for(LogMessage log: logger.logs) 
			if (log.thrown != null)
				if (ExceptionThrowingStub.TEST_EXCEPTION_MESSAGE.equals(log.thrown.getMessage()))
						foundErrorCleanerException = true;
		
		assertThat(foundErrorCleanerException, is(true));
//		logger.output(System.out);
	}
	
	
	
	
	private static class ExceptionThrowingStub implements ProcPipeCleaner {
		public static final String TEST_EXCEPTION_MESSAGE = "ExceptionThrowingStub Test Exception Message";
		@Override
		public void clean(ProcData last) throws Exception {
			throw new Exception(TEST_EXCEPTION_MESSAGE);
		}
	}
}
