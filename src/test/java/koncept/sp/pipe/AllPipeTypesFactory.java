package koncept.sp.pipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import koncept.sp.resource.SimpleProcPipeCleaner;
import koncept.sp.resource.SimpleProcTerminator;
import koncept.sp.stage.SplitProcStage;
import koncept.sp.stage.TrackedSplitProcStage;
import koncept.sp.tracker.BlockingJobTracker;
import koncept.sp.tracker.internal.JobTrackerDefinition;

public class AllPipeTypesFactory {
	private AllPipeTypesFactory(){}

	public static Collection<Object[]> allPipeTypesJUnit() {
		List<Object[]> types = new ArrayList<>();
		for(Class type: allPipeTypes())
			types.add(new Object[]{type});
		return types;
	}
	
	public static List<Class<? extends ProcPipe>> allPipeTypes(){
		return Arrays.asList(
				(Class<? extends ProcPipe>)SingleExecutorProcPipe.class,
				(Class<? extends ProcPipe>)ExecutorPerStageProcPipe.class
		);
	}
	
	public static ProcPipe create(Class<? extends ProcPipe> type) {
		return create(type, createStages(3));
	}
	
	public static ProcPipe create(Class<? extends ProcPipe> type, List<? extends SplitProcStage> stages) {
		return create(type, stages, new BlockingJobTracker());
	}
	
	public static ProcPipe create(Class<? extends ProcPipe> type, List<? extends SplitProcStage> stages, JobTrackerDefinition jobTracker) {
		if (type.equals(SingleExecutorProcPipe.class)) {
			return sepp(stages, jobTracker);
		} else if (type.equals(ExecutorPerStageProcPipe.class)) {
			return epspp(stages, jobTracker);
		} else {
			throw new IllegalArgumentException("unknown type: " + type);
		}
	}

	
	private static List<TrackedSplitProcStage> createStages(int numberOfStages) {
		List<TrackedSplitProcStage> stages = new ArrayList<TrackedSplitProcStage>();
		for(int i = 0; i < numberOfStages; i++) {
			stages.add(new TrackedSplitProcStage());
		}
		return stages;
	}
	
	private static ExecutorService executor() {
		return Executors.newFixedThreadPool(2);
	}
	
	private static SingleExecutorProcPipe sepp(List<? extends SplitProcStage> stages, JobTrackerDefinition jobTracker) {
		return new SingleExecutorProcPipe(null, jobTracker, executor(), stages, new SimpleProcTerminator(), new SimpleProcPipeCleaner());
		
	}
	
	private static ExecutorPerStageProcPipe epspp(List<? extends SplitProcStage> stages, JobTrackerDefinition jobTracker) {
		List<ExecutorService> executors = new ArrayList<>();
		for(int i = 0; i < stages.size(); i++) 
			executors.add(executor());
		return new ExecutorPerStageProcPipe(null, jobTracker, executors, stages, new SimpleProcTerminator(), new SimpleProcPipeCleaner());
	}
}
