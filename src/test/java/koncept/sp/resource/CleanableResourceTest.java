package koncept.sp.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import koncept.sp.ProcData;
import koncept.sp.pipe.SingleExecutorProcPipe;
import koncept.sp.pipe.state.ProcState;
import koncept.sp.stage.SplitProcStage;
import koncept.sp.stage.WaitForExecutionSplitStage;

import org.junit.Test;

public class CleanableResourceTest {

	@Test
	public void cleanIsCalledAfterPipeCompletes() throws Exception {
		WaitForExecutionSplitStage trackerStage = new WaitForExecutionSplitStage();
		
		final CleanableResourceCounter resource = new CleanableResourceCounter();
		SingleExecutorProcPipe executorProcPipe = 
				new SingleExecutorProcPipe(
						Executors.newSingleThreadExecutor(), 
						Arrays.asList(new AddCleanableResource(resource), trackerStage),
						new SimpleProcTerminator(null));
		
		Future<Boolean> future = executorProcPipe.submit(new ProcData());
		future.get();
		
		assertThat(resource.cleans, is(1));
	}
	
	public static class AddCleanableResource implements SplitProcStage {
		private final CleanableResource cleanableResource;
		public AddCleanableResource(CleanableResource cleanableResource) {
			this.cleanableResource = cleanableResource;
		}
		public ProcData run(ProcState last) {
			return last.getData().add("name", cleanableResource);
		}
	}
}
