package dk.kb.webdanica.webapp;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.antiaction.common.cron.CrontabSchedule;
import com.antiaction.common.cron.ScheduleAbstract;

public class CrontabTester {
  @Test
  public void evaluateDefaultCrontabs() {
	  String filteringCrontab = dk.kb.webdanica.webapp.Constants.DEFAULT_FILTERING_CRONTAB;
	  String harvestingCrontab = dk.kb.webdanica.webapp.Constants.DEFAULT_HARVESTING_CRONTAB;
	  String statecachingCrontab = dk.kb.webdanica.webapp.Constants.DEFAULT_STATECACHING_CRONTAB;

	  ScheduleAbstract filterSchedule = CrontabSchedule.crontabFactory(filteringCrontab);
	  ScheduleAbstract harvestingSchedule = CrontabSchedule.crontabFactory(harvestingCrontab);
	  ScheduleAbstract cacheUpdatingSchedule = CrontabSchedule.crontabFactory(statecachingCrontab);
	  
	  List timesF = filterSchedule.getScheduleList(System.currentTimeMillis());
	  //System.out.println("Found #schedules: " + timesF.size());
	  List timesH = harvestingSchedule.getScheduleList(System.currentTimeMillis());
	  //System.out.println("Found #schedules: " + timesH.size());
	  List timesC = cacheUpdatingSchedule.getScheduleList(System.currentTimeMillis());
	  //System.out.println("Found #schedules: " + timesC.size());
	  /*
	  System.out.println("Found #schedules: " + times.size());
	  for (Object o: times) {
		  System.out.print(o); System.out.println(" " +  new Date((Long) o));
	  }
	  */
	  Assert.assertTrue("Expect default filtering schedule to be every ten minutes, i.e. 144 times each day", timesF.size() == 144);
	  Assert.assertTrue("Expect default harvesting schedule to be once every hour, i.e. 24 times each day", timesH.size() == 24);
	  Assert.assertTrue("Expect default cachingupdate schedule to be every fifteen minutes, i.e. 96 times each day", timesC.size() == 96);
  }
}
