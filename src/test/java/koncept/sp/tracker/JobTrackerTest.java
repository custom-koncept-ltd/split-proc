package koncept.sp.tracker;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import koncept.sp.ProcData;
import koncept.sp.future.ProcPipeFuture;
import koncept.sp.pipe.internal.InternalProcState;
import koncept.sp.tracker.internal.JobTrackerDefinition;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JobTrackerTest {
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{CopyOnWriteJobTracker.class},
				{BlockingJobTracker.class}
		});
	}
	
	private final Class<JobTrackerDefinition> jobTrackerDefinitionType;
	private JobTracker<?> jobTracker;
	private JobTrackerDefinition<?> jobTrackerDefinition;
	
	public JobTrackerTest(Class<JobTrackerDefinition> jobTrackerDefinitionType) {
		this.jobTrackerDefinitionType = jobTrackerDefinitionType;
	}
	
	@Before
	public void init() throws InstantiationException, IllegalAccessException {
		this.jobTrackerDefinition = jobTrackerDefinitionType.newInstance();
		this.jobTracker = jobTrackerDefinition;
		
	}
	
	
	@Test
	public void initiallyEmpty() {
		assertTrue(jobTracker.queued().isEmpty());
		assertTrue(jobTracker.live().isEmpty());
	}
	
	@Test
	public void canQueueJobs() {
		jobTrackerDefinition.submitted(newProcState());
		assertThat(jobTracker.queued().size(), is(1));
	}
	
	@Test
	public void canStartJobs() {
		InternalProcState ps = newProcState();
		jobTrackerDefinition.submitted(ps);
		jobTrackerDefinition.started(ps);
		
		assertThat(jobTracker.queued().size(), is(0));
		assertThat(jobTracker.live().size(), is(1));
	}
	
	@Test
	public void startingJobsWithAQueue() {
		jobTrackerDefinition.submitted(newProcState());
		InternalProcState ps = newProcState();
		jobTrackerDefinition.submitted(ps);
		jobTrackerDefinition.started(ps);
		
		assertThat(jobTracker.queued().size(), is(1));
		assertThat(jobTracker.live().size(), is(1));
	}
	
	
	private InternalProcState newProcState() {
		return new InternalProcState(new ProcPipeFuture(), new ProcData());
	}
	
}
