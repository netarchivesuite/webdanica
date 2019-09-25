package dk.kb.webdanica.webapp.workflow;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.webapp.Environment;

/**
 * Workflow thread worker for overall coordination.
 */
public class WorkflowWorkThread extends WorkThreadAbstract {

    static {
        logger = Logger.getLogger(WorkflowWorkThread.class.getName());
    }

    private GregorianCalendar dayCal;
	private Date dayDate;
	private long nextReschedule = System.currentTimeMillis();
	private List<Long> harvestingInitSchedules = new LinkedList<Long>();
	private List<Long> harvestingFinishSchedules = new LinkedList<Long>();
    private List<Long> filteringSchedules = new LinkedList<Long>();
    private List<Long> cacheUpdatingSchedules = new LinkedList<Long>();

    /**
     * Constructor for the workflow thread worker object.
     * @param environment The environment information for the workflow
     * @param threadName The name of the thread
     */
    public WorkflowWorkThread(Environment environment, String threadName) {
        this.environment = environment;
        this.threadName = threadName;
    }

    @Override
    public int getQueueSize() {
    	return 0;
    }

    @Override
	protected void process_init() {
    	dayCal = new GregorianCalendar();
    	dayDate = dayCal.getTime();

    	logger.log(Level.INFO, "Generating the initial schedules.");
		long ctm = System.currentTimeMillis();
    	schedule_day(ctm);
    	schedule_trim_overdue(ctm);
    }

    @SuppressWarnings("unchecked")
    protected void schedule_day(long ctm) {
        harvestingInitSchedules = (List<Long>)environment.harvestInitSchedule.getScheduleList(nextReschedule);
		harvestingFinishSchedules = (List<Long>)environment.harvestFinishSchedule.getScheduleList(nextReschedule);
        filteringSchedules = (List<Long>)environment.filterSchedule.getScheduleList(nextReschedule);
        cacheUpdatingSchedules = (List<Long>)environment.cacheUpdatingSchedule.getScheduleList(nextReschedule);

        dayDate.setTime(ctm);
        dayCal.setTime(dayDate);
        dayCal.add(Calendar.DATE, 1);
        dayCal.set(Calendar.HOUR_OF_DAY, 0);
        dayCal.set(Calendar.MINUTE, 0);
        dayCal.set(Calendar.SECOND, 0);
        dayCal.set(Calendar.MILLISECOND, 0);
        nextReschedule = dayCal.getTimeInMillis();
    }

    protected void schedule_trim_overdue(long ctm) {
    	trimSchedule(harvestingInitSchedules, ctm);
    	trimSchedule(harvestingFinishSchedules, ctm);
    	trimSchedule(filteringSchedules, ctm);
    	trimSchedule(cacheUpdatingSchedules, ctm);
    }

    private void trimSchedule(List<Long> schedules, long ctm) {
    	if (schedules != null && schedules.size() > 0 && ctm > schedules.get(0)) {
        	while (schedules.size() > 0 && ctm > schedules.get(0)) {
            	schedules.remove(0);
        	}
        }
		
	}

	private boolean setSchedulingIfneeded(List<Long> scheduledTimes, long ctm) {
		if (scheduledTimes != null && scheduledTimes.size() > 0 && ctm > scheduledTimes.get(0)) {
			while (scheduledTimes.size() > 0 && ctm > scheduledTimes.get(0)) {
				scheduledTimes.remove(0);
			}
			return true;
		}
    	return false;
	}


	@Override
	protected void process_run() {
	    long ctm = System.currentTimeMillis();
	    if (ctm >= nextReschedule) {
	        logger.log(Level.INFO, "Generating new daily schedules.");
	        schedule_day(ctm);
	    }
	    environment.bScheduleHarvestingInit = setSchedulingIfneeded(harvestingInitSchedules, ctm);
		environment.bScheduleHarvestingFinish = setSchedulingIfneeded(harvestingFinishSchedules, ctm);
	    environment.bScheduleFiltering = setSchedulingIfneeded(filteringSchedules, ctm);
	    environment.bScheduleCacheUpdating = setSchedulingIfneeded(cacheUpdatingSchedules, ctm);
	}
    
    @Override
    protected void process_cleanup() {
    	// Nothing to do
    }

}
