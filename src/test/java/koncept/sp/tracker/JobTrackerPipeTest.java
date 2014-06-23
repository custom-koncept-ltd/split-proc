package koncept.sp.tracker;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import koncept.sp.ProcSplit;
import koncept.sp.pipe.AllPipeTypesFactory;
import koncept.sp.pipe.ProcPipe;
import koncept.sp.stage.AllJobTrackerTypesFactory;
import koncept.sp.stage.TrackedSplitProcStage;
import koncept.sp.stage.WaitForExecutionSplitStage;
import koncept.sp.stage.WaitForNotificationSplitStage;
import koncept.sp.tracker.internal.JobTrackerDefinition;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JobTrackerPipeTest {
	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object[]> data = new ArrayList<>();
		for(Class pipeType: AllPipeTypesFactory.allPipeTypes()) {
			for(Class trackerType: AllJobTrackerTypesFactory.allTrackerTypes())
				data.add(new Object[]{pipeType, trackerType});
		}
		return data;
	}
	
	private final Class<? extends ProcPipe> pipeType;
	private final Class<? extends JobTrackerDefinition> trackerType;
	private ProcPipe pipe;
	
	TrackedSplitProcStage stage1;
	WaitForExecutionSplitStage stage2;
	WaitForNotificationSplitStage stage3;
	WaitForExecutionSplitStage stage4;
	TrackedSplitProcStage stage5;
	
	public JobTrackerPipeTest(Class<? extends ProcPipe> pipeType, Class<? extends JobTrackerDefinition> trackerType) {
		this.pipeType = pipeType;
		this.trackerType = trackerType;
	}
	
	@Before
	public void init() throws Exception {
		stage1 = new TrackedSplitProcStage();
		stage2 = new WaitForExecutionSplitStage();
		stage3 = new WaitForNotificationSplitStage();
		stage4 = new WaitForExecutionSplitStage();
		stage5 = new TrackedSplitProcStage();
		pipe = AllPipeTypesFactory.create(pipeType, Arrays.asList(stage1, stage2, stage3, stage4, stage5), AllJobTrackerTypesFactory.create(trackerType));
	}
	
	@After
	public void cleanup() {
		pipe.stop();
	}
	
	@Test
	public void initiallyEmpty() {
		assertTrue(pipe.tracker().queued().isEmpty());
		assertTrue(pipe.tracker().live().isEmpty());
	}
	
	@Test
	public void movesToLiveAndThenOutOnSuccess() throws Exception {
		Future future = pipe.submit(new ProcSplit());
		stage2.waitForExecution(500);
		assertTrue(pipe.tracker().queued().isEmpty());
		assertThat(pipe.tracker().live().size(), is(1));
		stage3.allowExecute();

		future.get(500, TimeUnit.MILLISECONDS); //delay till the execution finishes
		assertTrue(pipe.tracker().live().isEmpty());
	}
	
	@Test
	public void movesToLiveAndThenOutOnCancel() throws Exception {
		Future future = pipe.submit(new ProcSplit());
		stage2.waitForExecution(500);
		assertTrue(pipe.tracker().queued().isEmpty());
		assertThat(pipe.tracker().live().size(), is(1));
		future.cancel(true);
		stage3.allowExecute();

		try {
			future.get(500, TimeUnit.MILLISECONDS); //delay till the execution finishes
			fail("Expected a CancellationException");
		} catch (CancellationException e) {
		}
		assertTrue(pipe.tracker().live().isEmpty());
	}
	
	
	
}
