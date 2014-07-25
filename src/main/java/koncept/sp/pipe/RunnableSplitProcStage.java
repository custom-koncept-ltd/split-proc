package koncept.sp.pipe;

import koncept.sp.ProcSplit;
import koncept.sp.pipe.internal.ProcPipeDefinition;
import koncept.sp.pipe.state.ProcState;

public class RunnableSplitProcStage<T> implements Runnable {
	private final ProcPipeDefinition<T> pipeDefinition;
	private final ProcState<T> state;
	
	public RunnableSplitProcStage(ProcPipeDefinition<T> pipeDefinition, ProcState<T> state) {
		this.pipeDefinition = pipeDefinition;
		this.state = state;
	}
	
	public void run() {
		boolean run = pipeDefinition.onStageStart(state);
		if (!run) {
			pipeDefinition.onCancel(state);
			return;
		}
		
		try {
			ProcSplit out = pipeDefinition.getStage(state.getNextStage()).run(state.getLastSplit());
			state.addSplit(out); //getNextStage is now +1
			pipeDefinition.onComplete(state);
		} catch (Throwable t) {
			pipeDefinition.onError(state, t);
		}
	}
}
