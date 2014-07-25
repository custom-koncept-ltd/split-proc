package koncept.sp.resource;

import java.io.Closeable;
import java.io.Flushable;

public class SimpleCloseableResource implements CleanableResource {

	private final Closeable closeable;
	
	public SimpleCloseableResource(Closeable closeable) {
		this.closeable = closeable;
	}
	
	@Override
	public Object get() {
		return closeable;
	}

	@Override
	public void clean() throws Exception {
		// TODO Auto-generated method stub

		try {
			if (closeable instanceof Flushable) {
				((Flushable)closeable).flush();
			}
		} finally {
			closeable.close();
		}
	}

}
