package koncept.sp.pipe;

import java.util.concurrent.Future;

import koncept.sp.ProcSplit;

/**
 * 
 * Simple handline interface for a Proc Pipe.<br/>
 * <br/>
 * Basic process:<br/>
 * <ul>
 * <li>ProcSplit passed in</li>
 * <li>As long as the future isn't cancelled, execute the next step</li>
 * <li>When all steps are complete, execute the clean up code</li>
 * <li>return true for success</li>
 * </ul>
 * <br/>
 * If a failure happens, the cleanup code is still executed, and the
 * first exception is it set into the future.
 * 
 * @author nicholas.krul@gmail.com
 *
 */
public interface ProcPipe<T> {
	public Future<T> handle(ProcSplit in);
}
