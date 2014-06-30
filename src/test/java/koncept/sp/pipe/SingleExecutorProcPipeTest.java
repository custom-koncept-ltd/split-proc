package koncept.sp.pipe;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import koncept.sp.ProcSplit;
import koncept.sp.resource.SimpleProcTerminator;
import koncept.sp.stage.TrackedSplitProcStage;
import koncept.sp.stage.WaitForExecutionSplitStage;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SingleExecutorProcPipeTest {
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{new InThreadExecutor()},
				{Executors.newSingleThreadExecutor()}
		});
	}
	
	private final ExecutorService executor;
	
	public SingleExecutorProcPipeTest(ExecutorService executor) {
		this.executor = executor;
	}
	
	@After
	public void cleanup() {
		executor.shutdown();
	}
	
	
	@Test
	public void inThread() throws InterruptedException {
		TrackedSplitProcStage stage1 = new TrackedSplitProcStage();
		TrackedSplitProcStage stage2 = new TrackedSplitProcStage();
		TrackedSplitProcStage stage3 = stage1;
		WaitForExecutionSplitStage trackerStage = new WaitForExecutionSplitStage();
		
		SingleExecutorProcPipe executorProcPipe = 
				new SingleExecutorProcPipe(
						executor,  
						Arrays.asList(stage1, stage2, stage3, trackerStage),
						new SimpleProcTerminator(null));
		
		executorProcPipe.submit(new ProcSplit());
		
		trackerStage.waitForExecution(500); //needs to be compatable with in-thread AND external thread
		
		assertThat(stage1.count(), is(2));
		assertThat(stage2.count(), is(1));
		//check for sequential execution
		assertTrue(stage1.executionTimes().get(0) < stage2.executionTimes().get(0));
		assertTrue(stage2.executionTimes().get(0) < stage3.executionTimes().get(1));
	}
	
	public static class InThreadExecutor implements ExecutorService {
		public void execute(Runnable command) {
			command.run();
		}
		
		public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
			throw new UnsupportedOperationException();
		}
		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> tasks)
				throws InterruptedException {
			throw new UnsupportedOperationException();
		}
		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> tasks, long timeout,
				TimeUnit unit) throws InterruptedException {
			throw new UnsupportedOperationException();
		}
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			throw new UnsupportedOperationException();
		}
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
				long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			throw new UnsupportedOperationException();
		}
		public boolean isShutdown() {
			throw new UnsupportedOperationException();
		}
		public boolean isTerminated() {
			throw new UnsupportedOperationException();
		}
		public void shutdown() { //nop
		}
		public List<Runnable> shutdownNow() {
			throw new UnsupportedOperationException();
		}
		public <T> Future<T> submit(Callable<T> task) {
			throw new UnsupportedOperationException();
		}
		public Future<?> submit(Runnable task) {
			throw new UnsupportedOperationException();
		}
		public <T> Future<T> submit(Runnable task, T result) {
			throw new UnsupportedOperationException();
		}
		
		
	}
	
}
