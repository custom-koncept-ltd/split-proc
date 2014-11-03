package koncept.sp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import koncept.sp.pipe.SingleExecutorProcPipe;
import koncept.sp.pipe.state.ProcState;
import koncept.sp.resource.SimpleProcPipeCleaner;
import koncept.sp.resource.SimpleProcTerminator;
import koncept.sp.stage.TrackedSplitProcStage;
import koncept.sp.tracker.BlockingJobTracker;
import koncept.util.ExceptionRecordingLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcDataTest {

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
	public void stagesCount() throws Exception {
		StageNumberValidator stage1 = new StageNumberValidator();
		StageNumberValidator stage2 = new StageNumberValidator();
		StageNumberValidator stage3 = new StageNumberValidator();
		
		ExceptionRecordingLogger logger = new ExceptionRecordingLogger();
		
		SingleExecutorProcPipe<Object> executorProcPipe = 
				new SingleExecutorProcPipe(
						logger,
						new BlockingJobTracker(),
						executor,  
						Arrays.asList(stage1, stage2, stage3),
						new SimpleProcTerminator(null),
						new SimpleProcPipeCleaner());
		
		executorProcPipe.submit(new ProcData()).get(); //wait for execution
		
		assertThat(stage1.index(), is(0));
		assertThat(stage2.index(), is(1));
		assertThat(stage3.index(), is(2));
	}
	
	
	
	private static class StageNumberValidator extends TrackedSplitProcStage {
		private volatile int index = -1;
		@Override
		public ProcData run(ProcState last) throws Exception {
			index = last.getCurrentIndex();
			return super.run(last);
		}
		public int index() {
			return index;
		}
	}
}
