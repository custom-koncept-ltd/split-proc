package koncept.sp.pipe;

import java.util.ArrayList;
import java.util.List;

import koncept.sp.stage.TrackedSplitProcStage;

public class AllPipeTypesFactory {

//	public List<ProcPipe> createAllPipeTypes(int numberOfStages){
//	}
	
	private List<TrackedSplitProcStage> createStages(int numberOfStages) {
		List<TrackedSplitProcStage> stages = new ArrayList<TrackedSplitProcStage>();
		for(int i = 0; i < numberOfStages; i++) {
			stages.add(new TrackedSplitProcStage());
		}
		return stages;
	}
	
	private SingleExecutorProcPipe sepp() {
//		return new SingleExecutorProcPipe(ExecutorService.)
		return null;
	}
}
