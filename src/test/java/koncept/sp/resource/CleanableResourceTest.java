package koncept.sp.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import koncept.sp.ProcSplit;
import koncept.sp.pipe.SingleExecutorProcPipe;
import koncept.sp.stage.SplitProcStage;
import koncept.sp.stage.WaitForExecutionSplitState;

import org.junit.Test;

public class CleanableResourceTest {

	@Test
	public void cleanIsCalledAfterPipeCompletes() throws Exception {
		WaitForExecutionSplitState trackerStage = new WaitForExecutionSplitState();
		
		final CleanableResourceCounter resource = new CleanableResourceCounter();
		SingleExecutorProcPipe executorProcPipe = 
				new SingleExecutorProcPipe(
						Executors.newSingleThreadExecutor(), 
						Arrays.asList(new AddCleanableResource(resource), trackerStage),
						new SimpleProcTerminator(null));
		
		Future<Boolean> future = executorProcPipe.handle(new ProcSplit());
		future.get();
		
		assertThat(resource.cleans, is(1));
	}
	
	public static class AddCleanableResource implements SplitProcStage {
		private final CleanableResource cleanableResource;
		public AddCleanableResource(CleanableResource cleanableResource) {
			this.cleanableResource = cleanableResource;
		}
		public ProcSplit run(ProcSplit last) {
			return last.add("name", cleanableResource);
		}
	}
}
