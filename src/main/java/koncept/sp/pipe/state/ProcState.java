package koncept.sp.pipe.state;

import koncept.sp.ProcData;
import koncept.sp.future.CancellationToken;
import koncept.sp.future.CancellationToken.WrappedCancellationToken;
import koncept.sp.pipe.internal.InternalProcState;

public class ProcState {

	private final CancellationToken cancellationToken;
	private final int currentIndex;
	private final ProcData data;
	
	public ProcState(InternalProcState<?> internalState) {
		this(internalState.future(), internalState.data(), internalState.currentIndex());
	}
	
	public ProcState(CancellationToken cancellationToken, ProcData data, int currentIndex) {
		this.cancellationToken = new WrappedCancellationToken(cancellationToken);
		this.currentIndex = currentIndex;
		this.data= data;
	}
	
	public CancellationToken getCancellationToken() {
		return cancellationToken;
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}
	
	public ProcData getData() {
		return data;
	}
	
	
	
}
