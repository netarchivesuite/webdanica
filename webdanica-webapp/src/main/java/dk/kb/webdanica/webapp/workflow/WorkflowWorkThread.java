/*
 * Created on 14/05/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp.workflow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.webapp.Environment;

/*
import dk.netarkivet.dab.webadmin.DABEnvironment;
import dk.netarkivet.dab.webadmin.dao.Domain;
import dk.netarkivet.dab.webadmin.dao.Domains;
import dk.netarkivet.dab.webadmin.dao.DomainsListener;
import dk.netarkivet.dab.webadmin.dao.UrlRecord;
import dk.netarkivet.dab.webadmin.dao.UrlRecords;
import dk.netarkivet.dab.webadmin.dao.UrlRecordsListener;
*/
/**
 * Workflow thread worker for overall coordination.
 */
public class WorkflowWorkThread extends WorkThreadAbstract 
//implements DomainsListener, UrlRecordsListener 
{

    static {
        logger = Logger.getLogger(WorkflowWorkThread.class.getName());
    }

    //private Domains domainsInstance;

    //private UrlRecords urlRecordsInstance;

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

    private Email email;

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
        //Domains.getInstance(environment.dataSource).addDomainChangedListener(this);
        //UrlRecords.getInstance(environment.dataSource).addUrlRecordChangedListener(this);
    	dayCal = new GregorianCalendar();
    	dayDate = dayCal.getTime();

    	logger.log(Level.INFO, "Generating schedules.");
		long ctm = System.currentTimeMillis();
    	schedule_day(ctm);
    	schedule_trim_overdue(ctm);

        Connection conn = null;
/*
        try {
            conn = environment.dataSource.getConnection();
            
            urlRecordsInstance = UrlRecords.getInstance(environment.dataSource);
            List<UrlRecord> records = urlRecordsInstance.getUrlRecords(0);
            UrlRecord urlRecord;
            if (records.size() > 0) {
                for (int i=0; i<records.size(); ++i) {
                	urlRecord = records.get(i);
                	switch (urlRecord.status_url) {
                	case UrlRecord.S_URL_REJECTED:
                		if (urlRecord.reject_reason == UrlRecord.R_URL_IS_HTML_DOCUMENT) {
                			urlRecord.status_url = UrlRecord.S_URL_ADDED;
                			urlRecord.store(conn);
                		}
                		break;
                	case UrlRecord.S_URL_ADDED:
                	case UrlRecord.S_URL_ACCEPT_DOMAIN:
                	case UrlRecord.S_URL_ACCEPT_URL:
                	case UrlRecord.S_URL_ACCEPTED:
                	case UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE:
            			environment.lookup.enqueue(urlRecord);
                		break;
                	case UrlRecord.S_URL_IN_REMOTE_ARCHIVE:
            			environment.fetch.enqueue(urlRecord);
                		break;
                	case UrlRecord.S_URL_UNSUPPORTED_FORMAT:
            			environment.lookup.enqueue(urlRecord);
            			environment.fetch.enqueue(urlRecord);
                		break;
                	case UrlRecord.S_URL_FETCHED_FROM_REMOTE_ARCHIVE:
                	case UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE:
                		environment.wayback.enqueue(urlRecord);
                		break;
                	case UrlRecord.S_URL_ARCHIVED_LOCALLY:
                		environment.archive.enqueue(urlRecord);
                		break;
                	default:
                		break;
                	}
                	
                	
                	switch (urlRecord.status_nas) {
                	case LookupWorkThread.S_NAS_NONE:
                		environment.lookup.enqueue(urlRecord);
                		break;
                		
                	case LookupWorkThread.S_NAS_MATCHED:
                		break;
                	case LookupWorkThread.S_NAS_UNMATCHED:
                		break;
                	}
               
                }
            }
    	} catch (SQLException e) {
    		logger.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
    		logger.log(Level.SEVERE, e.toString(), e);
    	} finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, e.toString(), e);
                }
                conn = null;
            }
    	}
*/
    	email = new Email(environment);
    }

    @SuppressWarnings("unchecked")
	protected void schedule_day(long ctm) {
    	lookupSchedules = (List<Long>)environment.lookupSchedule.getScheduleList(nextReschedule);
        pidSchedules = (List<Long>)environment.pidSchedule.getScheduleList(nextReschedule);
        aliveCheckSchedules = (List<Long>)environment.aliveCheckSchedule.getScheduleList(nextReschedule);
        fetchSchedules = (List<Long>)environment.fetchSchedule.getScheduleList(nextReschedule);
        waybackCheckSchedules = (List<Long>)environment.waybackCheckSchedule.getScheduleList(nextReschedule);
        archiveCheckSchedules = (List<Long>)environment.archiveCheckSchedule.getScheduleList(nextReschedule);
        emailSchedules = (List<Long>)environment.emailSchedule.getScheduleList(nextReschedule);
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
        	logger.log(Level.INFO, "Generating schedules.");
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

        Connection conn = null;

        //try {
            //domainsInstance = Domains.getInstance(environment.dataSource);
            //urlRecordsInstance = UrlRecords.getInstance(environment.dataSource);

            lastWorkRun = System.currentTimeMillis();
            //conn = environment.dataSource.getConnection();
            /*
            List<UrlRecord> records = urlRecordsInstance.getUrlRecords(0);
            UrlRecord urlRecord;
            Domain domain;
            if (records.size() > 0) {
                for (int i=0; i<records.size(); ++i) {
                	urlRecord = records.get(i);
            		domain = domainsInstance.getDomainById(conn, urlRecord.domain_id);
            		switch (domain.status) {
            		case Domain.S_DOMAIN_UNDECIDED:
            			break;
            		case Domain.S_DOMAIN_ACCEPTED:
            			if (urlRecord.status_url == UrlRecord.S_URL_REJECTED && urlRecord.reject_reason == UrlRecord.R_URL_BELONGS_TO_REJECTED_DOMAIN) {
                			urlRecord.status_url = UrlRecord.S_URL_ACCEPTED;
                			urlRecord.reject_reason = 0;
                			urlRecord.store(conn);
            			}
            			break;
            		case Domain.S_DOMAIN_MANURL:
            			break;
            		case Domain.S_DOMAIN_REJECTED:
            			if (urlRecord.status_url != UrlRecord.S_URL_ADDED && urlRecord.status_url != UrlRecord.S_URL_REJECTED) {
            		    	urlRecordsInstance.deletePID(urlRecord);
            		    	urlRecordsInstance.deleteArchived(conn, urlRecord);
                			urlRecord.status_url = UrlRecord.S_URL_REJECTED;
                			urlRecord.reject_reason = UrlRecord.R_URL_BELONGS_TO_REJECTED_DOMAIN;
                			urlRecord.store(conn);
            			}
            			break;
            		default:
            			break;
            		}
                	switch (urlRecord.status_url) {
                	case UrlRecord.S_URL_REJECTED:
                		break;
                	case UrlRecord.S_URL_ADDED:
                		switch (domain.status) {
                		case Domain.S_DOMAIN_REJECTED:
                			urlRecord.status_url = UrlRecord.S_URL_REJECTED;
                			urlRecord.reject_reason = UrlRecord.R_URL_BELONGS_TO_REJECTED_DOMAIN;
                			urlRecord.store(conn);
                			break;
                		case Domain.S_DOMAIN_UNDECIDED:
                			urlRecord.status_url = UrlRecord.S_URL_ACCEPT_DOMAIN;
                			urlRecord.store(conn);
                			break;
                		case Domain.S_DOMAIN_ACCEPTED:
                			urlRecord.status_url = UrlRecord.S_URL_ACCEPTED;
                			urlRecord.store(conn);
                			break;
                		case Domain.S_DOMAIN_MANURL:
                			urlRecord.status_url = UrlRecord.S_URL_ACCEPT_URL;
                			urlRecord.store(conn);
                			break;
                		}
            			if (urlRecord.status_nas != LookupWorkThread.S_NAS_MATCHED) {
                			// Lookup URL.
                    		environment.lookup.enqueue(urlRecord);
            			}
                        if (urlRecord.status_online == 0) {
                    		// Check alive status.
                        	environment.alive.enqueue(urlRecord);
                        }
                		break;
                	case UrlRecord.S_URL_ACCEPT_DOMAIN:
                		switch (domain.status) {
                		case Domain.S_DOMAIN_REJECTED:
                			urlRecord.status_url = UrlRecord.S_URL_REJECTED;
                			urlRecord.reject_reason = UrlRecord.R_URL_BELONGS_TO_REJECTED_DOMAIN;
                			urlRecord.store(conn);
                			break;
                		case Domain.S_DOMAIN_UNDECIDED:
                			break;
                		case Domain.S_DOMAIN_ACCEPTED:
                			urlRecord.status_url = UrlRecord.S_URL_ACCEPTED;
                			urlRecord.store(conn);
                			break;
                		case Domain.S_DOMAIN_MANURL:
                			urlRecord.status_url = UrlRecord.S_URL_ACCEPT_URL;
                			urlRecord.store(conn);
                			break;
                		}
                		break;
                	case UrlRecord.S_URL_ACCEPT_URL:
                		break;
                	case UrlRecord.S_URL_ACCEPTED:
                		switch (urlRecord.status_nas) {
                		case LookupWorkThread.S_NAS_MATCHED:
                			urlRecord.status_url = UrlRecord.S_URL_IN_REMOTE_ARCHIVE;
                			urlRecord.store(conn);
                			break;
                		case LookupWorkThread.S_NAS_UNMATCHED:
                			urlRecord.status_url = UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE;
                			urlRecord.store(conn);
                			break;
                		}
                		break;
                	case UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE:
                		if (urlRecord.status_nas == LookupWorkThread.S_NAS_MATCHED) {
                			urlRecord.status_url = UrlRecord.S_URL_IN_REMOTE_ARCHIVE;
                			urlRecord.store(conn);
                		} else {
                			if (bScheduleLookup) {
                    			// Lookup URL.
                        		environment.lookup.enqueue(urlRecord);
                			}
                		}
                		break;
                	case UrlRecord.S_URL_IN_REMOTE_ARCHIVE:
                		if (bScheduleFetch) {
                			// Fetch from remote archive.
                			environment.fetch.enqueue(urlRecord);
                		}
                		break;
                	case UrlRecord.S_URL_UNSUPPORTED_FORMAT:
            			if (bScheduleLookup) {
                			// Lookup URL.
                    		environment.lookup.enqueue(urlRecord);
            			}
                		if (bScheduleFetch) {
                			// Fetch from remote archive.
                			environment.fetch.enqueue(urlRecord);
                		}
                		break;
                	case UrlRecord.S_URL_FETCHED_FROM_REMOTE_ARCHIVE:
                		break;
                	case UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE:
                        if (bScheduleWaybackCheck) {
                        	// Check Wayback access.
                        	environment.wayback.enqueue(urlRecord);
                        }
                		break;
                	case UrlRecord.S_URL_ARCHIVED_LOCALLY:
                		if (bScheduleArchiveCheck) {
                			environment.archive.enqueue(urlRecord);
                		}
                		break;
                	default:
                		break;
                	}
                	switch (urlRecord.status_nas) {
                	case LookupWorkThread.S_NAS_NONE:
            			if (bScheduleLookup) {
                			// Lookup URL.
                    		environment.lookup.enqueue(urlRecord);
            			}
                		break;
                	case LookupWorkThread.S_NAS_MATCHED:
                		break;
                	case LookupWorkThread.S_NAS_UNMATCHED:
                		break;
                	}
 */
 
 
                	/*
                	switch (urlRecord.status_pid) {
                	case PIDWorkThread.S_PID_NONE:
                		switch (urlRecord.status_url) {
                		case UrlRecord.S_URL_ACCEPTED:
                		case UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE:
                		case UrlRecord.S_URL_IN_REMOTE_ARCHIVE:
                		case UrlRecord.S_URL_UNSUPPORTED_FORMAT:
                		case UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE:
                		case UrlRecord.S_URL_FETCHED_FROM_REMOTE_ARCHIVE:
                		case UrlRecord.S_URL_ARCHIVED_LOCALLY:
                			// Original or archived PID.
                            environment.pid.enqueue(urlRecord);
                			break;
                		default:
                			if (bSchedulePid) {
                                environment.pid.enqueue(urlRecord);
                			}
                			break;
                		}
                		break;
                	case PIDWorkThread.S_PID_ORIGINAL:
                		switch (urlRecord.status_url) {
                		case UrlRecord.S_URL_REJECTED:
                		case UrlRecord.S_URL_ADDED:
                		case UrlRecord.S_URL_ACCEPT_DOMAIN :
                		case UrlRecord.S_URL_ACCEPT_URL:
                		case UrlRecord.S_URL_ARCHIVED_LOCALLY:
                			// Deleted or archived PID..
                            environment.pid.enqueue(urlRecord);
                			break;
                		default:
                			if (bSchedulePid) {
                                environment.pid.enqueue(urlRecord);
                			}
                			break;
                		}
                		break;
                	case PIDWorkThread.S_PID_ARCHIVED:
                		switch (urlRecord.status_url) {
                		case UrlRecord.S_URL_REJECTED:
                		case UrlRecord.S_URL_ADDED:
                		case UrlRecord.S_URL_ACCEPT_DOMAIN :
                		case UrlRecord.S_URL_ACCEPT_URL:
                		case UrlRecord.S_URL_ACCEPTED:
                		case UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE:
                		case UrlRecord.S_URL_IN_REMOTE_ARCHIVE:
                		case UrlRecord.S_URL_UNSUPPORTED_FORMAT:
                		case UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE:
                		case UrlRecord.S_URL_FETCHED_FROM_REMOTE_ARCHIVE:
                			// Deleted or original PID.
                            environment.pid.enqueue(urlRecord);
                			break;
                		default:
                			if (bSchedulePid) {
                                environment.pid.enqueue(urlRecord);
                			}
                			break;
                		}
                		break;
                	}
                	
                	if (bScheduleAliveCheck) {
                		// Check alive status.
                		//environment.alive.enqueue(urlRecord);
                	}
                }

                if (bScheduleEmail) {
                    //email.emailNotifications(conn);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e

				 {
                    logger.log(Level.SEVERE, e.toString(), e);
                }
                conn = null;
            }
        } 
	} */

	//@Override
    /*
	protected void process_cleanup() {
        Domains.getInstance( environment.dataSource ).removeDomainChangedListener( this );
        UrlRecords.getInstance( environment.dataSource ).removeUrlRecordChangedListener( this );
	}
	*/

    /*
     * Events.
     */

    //@Override
    /*
    public void domainChangeEvent(Domain domain) {
        switch (domain.status) {
        case Domain.S_DOMAIN_ACCEPTED:
            synchronized (bDomainsAccepted) {
                bDomainsAccepted = true;
            }
            break;
        case Domain.S_DOMAIN_REJECTED:
            synchronized (bDomainsRejected) {
                bDomainsRejected = true;
            }
            break;
        default:
            logger.log(Level.WARNING, "Unknown Changeevent");
        }
        logger.log(Level.INFO, "Domain changed: " + domain.domain);
    }
    */
    
    /*
    
    public void urlRecordChangeEvent(UrlRecord urlRecord) {
    	
        switch (urlRecord.status_url) {
        case UrlRecord.S_URL_ACCEPTED:
            synchronized (bDomainsAccepted) {
                bDomainsAccepted = true;
            }
            synchronized (bDomainsRejected) {
                bDomainsRejected = true;
            }
            synchronized (bUpdatePID) {
                if (urlRecord.status_pid == PIDWorkThread.S_PID_NONE) {
                    bUpdatePID = true;
                }
            }
            break;
        case UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE:
            synchronized (bUpdatePID) {
                if (urlRecord.status_pid == PIDWorkThread.S_PID_NONE) {
                    bUpdatePID = true;
                }
            }
            break;
        case UrlRecord.S_URL_IN_REMOTE_ARCHIVE:
            synchronized (bUpdatePID) {
                if (urlRecord.status_pid == PIDWorkThread.S_PID_NONE) {
                    bUpdatePID = true;
                }
            }
            break;
        case UrlRecord.S_URL_ARCHIVED_LOCALLY:
            synchronized (bUpdatePID) {
                if (urlRecord.status_pid != PIDWorkThread.S_PID_ORIGINAL) {
                    bUpdatePID = true;
                }
                if (urlRecord.status_pid != PIDWorkThread.S_PID_ARCHIVED) {
                    bUpdatePID = true;
                }
            }
            break;
        case UrlRecord.S_URL_REJECTED:
            // FIXME Should anything happen here?
            break;
        default:   logger.log(Level.INFO, "Unknown Changeevent");
        }
        logger.log(Level.INFO, "UrlRecord changed: " + urlRecord.url);
        */
    }
    
    @Override
    protected void process_cleanup() {
        // TODO Auto-generated method stub
        
    }
}
