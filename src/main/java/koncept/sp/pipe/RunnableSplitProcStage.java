package koncept.sp.pipe;

import koncept.sp.ProcData;
import koncept.sp.pipe.internal.InternalProcState;
import koncept.sp.pipe.internal.ProcPipeDefinition;
import koncept.sp.pipe.state.ProcState;

public class RunnableSplitProcStage<T> implements Runnable {
	private final ProcPipeDefinition<T> pipeDefinition;
	private final InternalProcState<T> state;
	
	public RunnableSplitProcStage(ProcPipeDefinition<T> pipeDefinition, InternalProcState<T> state) {
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
			ProcData data = pipeDefinition.getStage(state.currentIndex()).run(new ProcState(state));
			pipeDefinition.onComplete(state, data);
		} catch (Throwable t) {
			pipeDefinition.onError(state, t);
		}
	}
}
