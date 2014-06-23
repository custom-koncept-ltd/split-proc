package koncept.sp.stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import koncept.sp.tracker.BlockingJobTracker;
import koncept.sp.tracker.CopyOnWriteJobTracker;
import koncept.sp.tracker.internal.JobTrackerDefinition;

/**
 * Test factory for all (non dummy) job tracker types
 * @author nick
 *
 */
public class AllJobTrackerTypesFactory {
	private AllJobTrackerTypesFactory(){}

	public static Collection<Object[]> allTrackerTypesJUnit() {
		List<Object[]> types = new ArrayList<>();
		for(Class type: allTrackerTypes())
			types.add(new Object[]{type});
		return types;
	}
	
	public static List<Class<? extends JobTrackerDefinition>> allTrackerTypes(){
		return Arrays.asList(
				(Class<? extends JobTrackerDefinition>)BlockingJobTracker.class,
				(Class<? extends JobTrackerDefinition>)CopyOnWriteJobTracker.class
		);
	}
	
	public static JobTrackerDefinition create(Class<? extends JobTrackerDefinition> type) throws InstantiationException, IllegalAccessException {
		return type.newInstance();
	}
	
}
