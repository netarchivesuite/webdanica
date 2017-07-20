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
	private List<Long> harvestingSchedules = new LinkedList<Long>();
    private List<Long> filteringSchedules = new LinkedList<Long>();
    private List<Long> cacheUpdatingSchedules = new LinkedList<Long>();
    

    /**
     * Constructor for the workflow thread worker object.
     * @param environment The environment information for the workflow
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
    	harvestingSchedules = (List<Long>)environment.harvestSchedule.getScheduleList(nextReschedule);
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
    	trimSchedule(harvestingSchedules, ctm);
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

	@Override
	protected void process_run() {
		long ctm = System.currentTimeMillis();
        if (ctm >= nextReschedule) {
        	logger.log(Level.INFO, "Generating new daily schedules.");
        	schedule_day(ctm);
        }
        environment.bScheduleHarvesting = false;
        if (harvestingSchedules != null && harvestingSchedules.size() > 0 && ctm > harvestingSchedules.get(0)) {
        	while (harvestingSchedules.size() > 0 && ctm > harvestingSchedules.get(0)) {
            	harvestingSchedules.remove(0);
        	}
        	environment.bScheduleHarvesting = true;
        }
        
        environment.bScheduleFiltering = false;
        if (filteringSchedules != null && filteringSchedules.size() > 0 && ctm > filteringSchedules.get(0)) {
        	while (filteringSchedules.size() > 0 && ctm > filteringSchedules.get(0)) {
        		filteringSchedules.remove(0);
        	}
        	environment.bScheduleFiltering = true;
        }
        
        environment.bScheduleCacheUpdating = false;
        if (cacheUpdatingSchedules != null && cacheUpdatingSchedules.size() > 0 && ctm > cacheUpdatingSchedules.get(0)) {
        	while (cacheUpdatingSchedules.size() > 0 && ctm > cacheUpdatingSchedules.get(0)) {
        		cacheUpdatingSchedules.remove(0);
        	}
        	environment.bScheduleCacheUpdating = true;
        }        
    }
    
    @Override
    protected void process_cleanup() {
    	// Nothing to do
    }

}
