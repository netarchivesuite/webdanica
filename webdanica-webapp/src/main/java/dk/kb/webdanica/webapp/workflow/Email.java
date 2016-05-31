/*
 * Created on 07/03/2014
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dk.kb.webdanica.webapp.workflow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dk.kb.webdanica.webapp.Environment;
/*
import dk.netarkivet.dab.webadmin.DABEnvironment;
import dk.netarkivet.dab.webadmin.UserResource;
import dk.netarkivet.dab.webadmin.dao.Domain;
import dk.netarkivet.dab.webadmin.dao.DomainChange;
import dk.netarkivet.dab.webadmin.dao.Domains;
import dk.netarkivet.dab.webadmin.dao.NotificationSubscription;
import dk.netarkivet.dab.webadmin.dao.UrlRecord;
import dk.netarkivet.dab.webadmin.dao.UrlRecordChange;
import dk.netarkivet.dab.webadmin.dao.UrlRecords;
import dk.netarkivet.dab.webadmin.dao.User;
*/
public class Email {

	public static final int NSUBT_DOMAIN = 0;
	public static final int NSUBT_URL = 1;
	public static final int NSUBT_PID = 2;

	private Environment environment;

    private Domains domainsInstance;

    //private UrlRecords urlRecordsInstance;

    public Email(Environment environment2) {
        this.environment = environment2;
	} 
/*
	@SuppressWarnings("unchecked")
	public void emailNotifications(Connection conn) throws IOException {
        domainsInstance = Domains.getInstance(environment.dataSource);
        urlRecordsInstance = UrlRecords.getInstance(environment.dataSource);

		List<User> users = User.getUserList(conn);
        Iterator<User> userIter = users.iterator();
        User user;
        List<NotificationSubscription> notificationSubscriptionsList = null;
    	NotificationSubscription nsub;
    	int emailSections;
		List<DomainChange> domainChangeList;
		DomainChange domainChange;
		Domain domain;
		List<UrlRecordChange> urlRecordChangeList;
		UrlRecordChange urlRecordChange;
		UrlRecord urlRecord;
		int last_id;
		User changeUser;
		StringBuilder sb = new StringBuilder();
		Timestamp newLastEventNotification = new Timestamp(System.currentTimeMillis());
        while (userIter.hasNext()) {
        	user = userIter.next();
        	//  && user.id == 1
        	if (user.email != null && user.email.length() > 0 && user.active) {
                Map<Integer, NotificationSubscription>[] nsubs = new TreeMap[UserResource.allowedSubscriptionsSet.length];
            	for (int i=0; i<nsubs.length; ++i) {
            		nsubs[i] = new TreeMap<Integer, NotificationSubscription>();
            	}
            	notificationSubscriptionsList = user.getNotificationSubscriptions();
            	for (int i=0; i<notificationSubscriptionsList.size(); ++i) {
            		nsub = notificationSubscriptionsList.get(i);
            		nsubs[nsub.notification_type].put(nsub.notification_id, nsub);
            	}

            	if (nsubs[NSUBT_DOMAIN].size() > 0) {
                	domainChangeList = DomainChange.getChangesAfter(conn, user.last_event_notification);
            		if (domainChangeList.size() > 0) {
            			Object[][] changed = new Object[][] {
            					{"Afviste domæner", new ArrayList<DomainChange>()},
            					{"Tilføjede domæner", new ArrayList<DomainChange>()},
            					{"Godkendte domæner", new ArrayList<DomainChange>()},
            					{"Domæner godkendt til manuel URL godkendelse", new ArrayList<DomainChange>()}
            			};
    					emailSections = 0;
            			int idx;
            			last_id = 0;
            			for (int i=0; i<domainChangeList.size(); ++i) {
            				domainChange = domainChangeList.get(i);
            				if (domainChange.domain_id != last_id) {
            					last_id = domainChange.domain_id;
    	        				switch (domainChange.status) {
    	        				case Domain.S_DOMAIN_REJECTED:
    	        					idx = 0;
    	        					break;
    	        				case Domain.S_DOMAIN_UNDECIDED:
    	        					idx = 1;
    	        					break;
    	        				case Domain.S_DOMAIN_ACCEPTED:
    	        					idx = 2;
    	        					break;
    	        				case Domain.S_DOMAIN_MANURL:
    	        					idx = 3;
    	        					break;
    	        				default:
    	        					idx = -1;
    	        					break;
    	        				}
    	        				if (nsubs[NSUBT_DOMAIN].containsKey(domainChange.status)) {
        	        				((List<DomainChange>)changed[idx][1]).add(domainChange);
    	        					++emailSections;
    	        				}
            				}
            			}
            			if (emailSections > 0) {
                			sb.setLength(0);
                			for (int i=0; i<changed.length; ++i) {
                				String header = (String)changed[i][0];
                				domainChangeList = (List<DomainChange>)changed[i][1];
                    			if (domainChangeList.size() > 0) {
                    				sb.append(header);
                    				sb.append(":");
            						sb.append("\r\n");
                    				sb.append("--");
            						sb.append("\r\n");
                    				for (int j=0; j<domainChangeList.size(); ++j) {
                    					domainChange = domainChangeList.get(j);
                    					domain = domainsInstance.getDomainById(conn, domainChange.domain_id);
                    					changeUser = User.getUserById(conn, domainChange.user_id);
                    					if (domain != null && changeUser != null) {
                    						sb.append(domain.domain);
                    						sb.append(" (");
                    						sb.append(changeUser.name);
                    						sb.append(" / ");
                    						sb.append(changeUser.email);
                    						sb.append(")");
                    						sb.append("\r\n");
                    					}
                    				}
            						sb.append("\r\n");
                    			}
                			}
                			environment.emailer.send(user.email, "[DAB-"  + environment.env + "] Domæneændringer", sb.toString());
            			}
            		}
            	}

            	if (nsubs[NSUBT_URL].size() > 0 || nsubs[NSUBT_PID].size() > 0) {
            		urlRecordChangeList = UrlRecordChange.getChangesAfter(conn, user.last_event_notification);
            		if (urlRecordChangeList.size() > 0) {
            			Object[][] changed = new Object[][] {
            					{"Oprettede url'er", new ArrayList<UrlRecordChange>()},
            					{"Afviste url'er", new ArrayList<UrlRecordChange>()},
            					{"Url'er afventer domæne godkendelse", new ArrayList<UrlRecordChange>()},
            					{"Url'er afventer godkendelse", new ArrayList<UrlRecordChange>()},
            					{"Accepterede url'er", new ArrayList<UrlRecordChange>()},
            					{"Url findes ikke i arkivet", new ArrayList<UrlRecordChange>()},
            					{"Url findes i arkivet", new ArrayList<UrlRecordChange>()},
            					{"Url har ikke godkendt filformat", new ArrayList<UrlRecordChange>()},
            					{"Url har udtræksproblemer", new ArrayList<UrlRecordChange>()},
            					{"Url hentet fra fjern arkiv", new ArrayList<UrlRecordChange>()},
            					{"Url arkiveret i local arkiv", new ArrayList<UrlRecordChange>()}
            			};
    					emailSections = 0;
    					List<UrlRecordChange> pidUrlChanges = new LinkedList<UrlRecordChange>();
            			int idx;
            			last_id = 0;
            			for (int i=0; i<urlRecordChangeList.size(); ++i) {
            				urlRecordChange = urlRecordChangeList.get(i);
            				if (urlRecordChange.url_id != last_id) {
            					last_id = urlRecordChange.url_id;
    	        				switch (urlRecordChange.status_url) {
    	        				case UrlRecord.S_URL_ADDED:
    	        					idx = 0;
    	        					break;
    	        				case UrlRecord.S_URL_REJECTED:
    	        					idx = 1;
    	        					break;
    	        				case UrlRecord.S_URL_ACCEPT_DOMAIN:
    	        					idx = 2;
    	        					break;
    	        				case UrlRecord.S_URL_ACCEPT_URL:
    	        					idx = 3;
    	        					break;
    	        				case UrlRecord.S_URL_ACCEPTED:
    	        					idx = 4;
    	        					break;
    	        				case UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE:
    	        					idx = 5;
    	        					break;
    	        				case UrlRecord.S_URL_IN_REMOTE_ARCHIVE:
    	        					idx = 6;
    	        					break;
    	        				case UrlRecord.S_URL_UNSUPPORTED_FORMAT:
    	        					idx = 7;
    	        					break;
    	        				case UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE:
    	        					idx = 8;
    	        					break;
    	        				case UrlRecord.S_URL_FETCHED_FROM_REMOTE_ARCHIVE:
    	        					idx = 9;
    	        					break;
    	        				case UrlRecord.S_URL_ARCHIVED_LOCALLY:
    	        					idx = 10;
    	        					pidUrlChanges.add(urlRecordChange);
    	        					break;
    	        				default:
    	        					idx = -1;
    	        					break;
    	        				}
    	        				if (nsubs[NSUBT_URL].containsKey(urlRecordChange.status_url)) {
        	        				((List<UrlRecordChange>)changed[idx][1]).add(urlRecordChange);
        	    					++emailSections;
    	        				}
            				}
            			}
            			if (emailSections > 0) {
                			sb.setLength(0);
                			for (int i=0; i<changed.length; ++i) {
                				String header = (String)changed[i][0];
                				urlRecordChangeList = (List<UrlRecordChange>)changed[i][1];
                    			if (urlRecordChangeList.size() > 0) {
                    				sb.append(header);
                    				sb.append(":");
            						sb.append("\r\n");
                    				sb.append("--");
            						sb.append("\r\n");
                    				for (int j=0; j<urlRecordChangeList.size(); ++j) {
                    					urlRecordChange = urlRecordChangeList.get(j);
                    					urlRecord = urlRecordsInstance.getUrlRecordById(conn, urlRecordChange.url_id);
                    					changeUser = User.getUserById(conn, urlRecordChange.user_id);
                    					if (urlRecord != null && changeUser != null) {
                    						sb.append(urlRecord.sysno);
                    						sb.append(";");
                    						sb.append(urlRecord.url);
                    						sb.append(" (");
                    						sb.append(changeUser.name);
                    						sb.append(" / ");
                    						sb.append(changeUser.email);
                    						sb.append(")");
                    						sb.append("\r\n");
                    					}
                    				}
            						sb.append("\r\n");
                    			}
                			}
                			environment.emailer.send(user.email, "[DAB-"  + environment.env + "] Urlændringer", sb.toString());
            			}

            		}
            	}

            	user.last_event_notification = newLastEventNotification;
        		user.store(conn);
        	}
        }
    }
*/
}
