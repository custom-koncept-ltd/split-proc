package koncept.sp.future;

public interface CancellationToken {
	
	public void cancel();
	public boolean isCancelled();
	public boolean isDone();
	
	
	/**
	 * Wrapped cancellation token - Mostly so that Future.get()
	 * can't be called from inside the processor (deadlock)
	 * @author nicholas.krul@gmail.com
	 *
	 */
	public class WrappedCancellationToken implements CancellationToken {
		private final CancellationToken cancellationToken;
		public WrappedCancellationToken(CancellationToken cancellationToken) {
			this.cancellationToken = cancellationToken;
		}
		@Override
		public void cancel() {
			cancellationToken.cancel();
		}
		
		@Override
		public boolean isCancelled() {
			return cancellationToken.isCancelled();
		}
		
		@Override
		public boolean isDone() {
			return cancellationToken.isDone();
		}
	}
}
