package koncept.sp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import koncept.sp.resource.CleanableResource;
import koncept.sp.resource.SimpleCleanableResource;

import org.junit.Test;

public class ProcSplitTest {

	@Test
	public void defaultKey() {
		CleanableResource cr = new SimpleCleanableResource("testString", null);
		ProcSplit ps = new ProcSplit(cr);
		assertThat(ps.getResourceNames().size(), is(1));
		assertThat(ps.getCleanableResource(ProcSplit.DEFAULT_VALUE_KEY), is(cr));
		assertThat(ps.getResource(ProcSplit.DEFAULT_VALUE_KEY), is((Object)"testString"));
	}
	
}
