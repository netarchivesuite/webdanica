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

    /** Have domains been accepted since last check. */
    private Boolean bDomainsAccepted = false;

    /** Have domains been rejected since last check. */
    private Boolean bDomainsRejected = false;

    /** Is it time to check for URLs where the PID can be created/updated. */
    private Boolean bUpdatePID = false;

	private GregorianCalendar dayCal;
	private Date dayDate;
	private long nextReschedule = System.currentTimeMillis();
    private List<Long> lookupSchedules = new LinkedList<Long>();
    private List<Long> pidSchedules = new LinkedList<Long>();
    private List<Long> aliveCheckSchedules = new LinkedList<Long>();
    private List<Long> fetchSchedules = new LinkedList<Long>();
    private List<Long> waybackCheckSchedules = new LinkedList<Long>();
    private List<Long> archiveCheckSchedules = new LinkedList<Long>();
    private List<Long> emailSchedules = new LinkedList<Long>();

    private boolean bScheduleLookup = false;
    private boolean bSchedulePid = false;
    private boolean bScheduleAliveCheck = false;
    private boolean bScheduleFetch = false;
    private boolean bScheduleWaybackCheck = false;
    private boolean bScheduleArchiveCheck = false;
    private boolean bScheduleEmail = false;


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
    	/*
    	lookupSchedules = (List<Long>)environment.lookupSchedule.getScheduleList(nextReschedule);
        pidSchedules = (List<Long>)environment.pidSchedule.getScheduleList(nextReschedule);
        aliveCheckSchedules = (List<Long>)environment.aliveCheckSchedule.getScheduleList(nextReschedule);
        fetchSchedules = (List<Long>)environment.fetchSchedule.getScheduleList(nextReschedule);
        waybackCheckSchedules = (List<Long>)environment.waybackCheckSchedule.getScheduleList(nextReschedule);
        archiveCheckSchedules = (List<Long>)environment.archiveCheckSchedule.getScheduleList(nextReschedule);
        emailSchedules = (List<Long>)environment.emailSchedule.getScheduleList(nextReschedule);
        */
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
        if (lookupSchedules != null && lookupSchedules.size() > 0 && ctm > lookupSchedules.get(0)) {
        	while (lookupSchedules.size() > 0 && ctm > lookupSchedules.get(0)) {
            	lookupSchedules.remove(0);
        	}
        }
        if (pidSchedules != null && pidSchedules.size() > 0 && ctm > pidSchedules.get(0)) {
        	while (pidSchedules.size() > 0 && ctm > pidSchedules.get(0)) {
        		pidSchedules.remove(0);
        	}
        }
        if (aliveCheckSchedules != null && aliveCheckSchedules.size() > 0 && ctm > aliveCheckSchedules.get(0)) {
        	while (aliveCheckSchedules.size() > 0 && ctm > aliveCheckSchedules.get(0)) {
        		aliveCheckSchedules.remove(0);
        	}
        }
        if (fetchSchedules != null && fetchSchedules.size() > 0 && ctm > fetchSchedules.get(0)) {
        	while (fetchSchedules.size() > 0 && ctm > fetchSchedules.get(0)) {
        		fetchSchedules.remove(0);
        	}
        }
        if (waybackCheckSchedules != null && waybackCheckSchedules.size() > 0 && ctm > waybackCheckSchedules.get(0)) {
        	while (waybackCheckSchedules.size() > 0 && ctm > waybackCheckSchedules.get(0)) {
        		waybackCheckSchedules.remove(0);
        	}
        }
        if (archiveCheckSchedules != null && archiveCheckSchedules.size() > 0 && ctm > archiveCheckSchedules.get(0)) {
        	while (archiveCheckSchedules.size() > 0 && ctm > archiveCheckSchedules.get(0)) {
        		archiveCheckSchedules.remove(0);
        	}
        }
        if (emailSchedules != null && emailSchedules.size() > 0 && ctm > emailSchedules.get(0)) {
        	while (emailSchedules.size() > 0 && ctm > emailSchedules.get(0)) {
            	emailSchedules.remove(0);
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

        bScheduleLookup = false;
        if (lookupSchedules != null && lookupSchedules.size() > 0 && ctm > lookupSchedules.get(0)) {
        	while (lookupSchedules.size() > 0 && ctm > lookupSchedules.get(0)) {
            	lookupSchedules.remove(0);
        	}
        	bScheduleLookup = true;
        }
        bSchedulePid = false;
        if (pidSchedules != null && pidSchedules.size() > 0 && ctm > pidSchedules.get(0)) {
        	while (pidSchedules.size() > 0 && ctm > pidSchedules.get(0)) {
        		pidSchedules.remove(0);
        	}
        	bSchedulePid = true;
        }
        bScheduleAliveCheck = false;
        if (aliveCheckSchedules != null && aliveCheckSchedules.size() > 0 && ctm > aliveCheckSchedules.get(0)) {
        	while (aliveCheckSchedules.size() > 0 && ctm > aliveCheckSchedules.get(0)) {
        		aliveCheckSchedules.remove(0);
        	}
        	bScheduleAliveCheck = true;
        }
        bScheduleFetch = false;
        if (fetchSchedules != null && fetchSchedules.size() > 0 && ctm > fetchSchedules.get(0)) {
        	while (fetchSchedules.size() > 0 && ctm > fetchSchedules.get(0)) {
        		fetchSchedules.remove(0);
        	}
        	bScheduleFetch = true;
        }
        bScheduleWaybackCheck = false;
        if (waybackCheckSchedules != null && waybackCheckSchedules.size() > 0 && ctm > waybackCheckSchedules.get(0)) {
        	while (waybackCheckSchedules.size() > 0 && ctm > waybackCheckSchedules.get(0)) {
        		waybackCheckSchedules.remove(0);
        	}
        	bScheduleWaybackCheck = true;
        }
        bScheduleArchiveCheck = false;
        if (archiveCheckSchedules != null && archiveCheckSchedules.size() > 0 && ctm > archiveCheckSchedules.get(0)) {
        	while (archiveCheckSchedules.size() > 0 && ctm > archiveCheckSchedules.get(0)) {
        		archiveCheckSchedules.remove(0);
        	}
        	bScheduleArchiveCheck = true;
        }
        bScheduleEmail = false;
        if (emailSchedules != null && emailSchedules.size() > 0 && ctm > emailSchedules.get(0)) {
        	while (emailSchedules.size() > 0 && ctm > emailSchedules.get(0)) {
            	emailSchedules.remove(0);
        	}
        	bScheduleEmail = true;
        }
    }
    
    @Override
    protected void process_cleanup() {
        // TODO Auto-generated method stub    
    }

}
