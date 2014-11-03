package koncept.sp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import koncept.sp.resource.CleanableResource;
import koncept.sp.resource.NonCleanableResource;

import org.junit.Test;

public class ProcSplitTest {

	@Test
	public void defaultKey() {
		CleanableResource cr = new NonCleanableResource("testString");
		ProcData ps = new ProcData(cr);
		assertThat(ps.getResourceNames().size(), is(1));
		assertThat(ps.getCleanableResource(ProcData.DEFAULT_VALUE_KEY), is(cr));
		assertThat(ps.getResource(ProcData.DEFAULT_VALUE_KEY), is((Object)"testString"));
	}
	
}
