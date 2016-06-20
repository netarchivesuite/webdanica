package dk.kb.webdanica.webapp.workflow;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.kb.webdanica.datamodel.BlackList;
import dk.kb.webdanica.datamodel.BlackListDAO;
import dk.kb.webdanica.datamodel.Seed;
import dk.kb.webdanica.datamodel.SeedCassandraDAO;
import dk.kb.webdanica.datamodel.Status;
import dk.kb.webdanica.seeds.filtering.IgnoredSuffixes;
import dk.kb.webdanica.seeds.filtering.ResolveRedirects;
import dk.kb.webdanica.webapp.Configuration;
import dk.kb.webdanica.webapp.Environment;

/**
 * Seeds filter work-thread.
 * A worker thread to reject seeds matching ignored suffixes or matching regexps in our active blacklists or 
 * matching blacklisted/rejected domains in our domain table.
 */
public class FilterWorkThread extends WorkThreadAbstract {

	static {
		logger = Logger.getLogger(FilterWorkThread.class.getName());
	}

    private List<Seed> queueList = new LinkedList<Seed>();

    private List<Seed> workList = new LinkedList<Seed>();

	private SeedCassandraDAO seeddao;
	private BlackListDAO blacklistDao;
	
	private ResolveRedirects resolveRedirects;

	private Configuration configuration;
    /**
     * Constructor for the NAS thread worker object.
     * @param environment DAB environment object
     */
    public FilterWorkThread(Environment environment, String threadName) {
        this.environment = environment;
        this.threadName = threadName;
    }

    public void enqueue(Seed urlRecord) {
        synchronized (queueList) {
            queueList.add(urlRecord);
        }
    }

    public void enqueue(List<Seed> urlRecords) {
        synchronized (queueList) {
            queueList.addAll(urlRecords);
        }
    }

    @Override
    public int getQueueSize() {
    	int queueSize = 0;
        synchronized (queueList) {
        	queueSize = queueList.size();
        }
        return queueSize;
    }

    @Override
	protected void process_init() {
    	seeddao = environment.seedDao;
    	blacklistDao = environment.blacklistDao;
    	configuration = new Configuration();
    	resolveRedirects = new ResolveRedirects(configuration.getWgetSettings());	
	}

	@Override
	protected void process_run() {
		logger.log(Level.FINE, "Running process of thread '" +  threadName + "' at '" + new Date() + "'");
		List<Seed> seedsNeedFiltering = seeddao.getSeeds(Status.NEW); // limit this 
		enqueue(seedsNeedFiltering);
		if (seedsNeedFiltering.size() > 0) {
			logger.log(Level.INFO, "Found '" + seedsNeedFiltering.size() + "' seeds ready for filtering");
		}
        try {
            synchronized (queueList) {
            	for (int i=0; i<queueList.size(); ++i) { // FIXME Possibly have a limit to how much is processed at a time?
            		Seed urlRecord = queueList.get(i);
            		workList.add(urlRecord);
            		/*
            		if (!workList.contains(urlRecord)) {
            			workList.add(urlRecord);
            		}*/
            	}
                queueList.clear();
            }
        	if (workList.size() > 0) {
                logger.log(Level.INFO, "Filter queue: " + workList.size());
                lastWorkRun = System.currentTimeMillis();
                filter(workList);
                startProgress(workList.size());
                
                stopProgress();
                workList.clear();
        	}
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.toString(), e);
        } finally {
           // TODO is there something we should do here
        }
	}

	private void filter(List<Seed> workList) {
		List<BlackList> activeBlackLists = blacklistDao.getLists(true); // only retrieve the active lists 
	    for (Seed s: workList) {
	    	String url = s.getUrl();
	    	if (ResolveRedirects.isPossibleUrlredirect(url)) {
	    		logger.info("Identified possible redirect url '" + url + "'. Trying to resolve it");
	    		String redirectedUrl = resolveRedirects.resolveRedirectedUrl(url);
	    		if (redirectedUrl != null && !redirectedUrl.isEmpty()) {
	    			s.setRedirectedUrl(redirectedUrl);
	    			logger.info("Identified '" + url + "' as redirecting to '" + redirectedUrl + "'");
	    		seeddao.updateRedirectedUrl(s);
	    		}
	    	}
	    	doFilteringOnSeed(s, activeBlackLists);	
	    	seeddao.updateState(s);
	    }
    }
	private void doFilteringOnSeed(Seed s, List<BlackList> blacklists) {
		// We test on the redirectedUrl, if it exists
		String urlInvestigated = s.getUrl();
		String redUrl = s.getRedirectedUrl();
		if (redUrl != null) {
			logger.info("Testing on redirect url '" + redUrl + "' instead of original url '" + s.getUrl() + "'");
			urlInvestigated = redUrl;
		}
		
		// Test 1: test for ignored suffixes
		String ignoredSuffix = IgnoredSuffixes.matchesIgnoredExtension(urlInvestigated);
		if (ignoredSuffix != null) {
			s.setState(Status.REJECTED);
			s.setStatusReason("REJECTED because it matches ignored suffix '" + ignoredSuffix + "'");
			return;
		} 
		// Test 2: test for matching any regular expression in the active blacklists
		for (BlackList blackList: blacklists) {
			String result = blackList.evaluateUrl(urlInvestigated);
			if (result != null) {
				s.setState(Status.REJECTED);
				s.setStatusReason("REJECTED because it matches regular expression '" + result + "' in blacklist '" + blackList.getName() + "'");
				return;
			}
		}
		
		// Test 3: test that url is not from the .DK top level domain.
		if (belongsToDK(urlInvestigated)) {
			s.setState(Status.REJECTED);
			s.setStatusReason("REJECTED because the seed '" + urlInvestigated 
					+ "' belongs to the .dk toplevel and by default is part of legal deposit"); 
			return;
		}
		
		// Otherwise set status to READY_FOR_HARVESTING and status_reason to the empty String
		s.setState(Status.READY_FOR_HARVESTING);
		s.setStatusReason("");
	}

	private boolean belongsToDK(String urlInvestigated) {
		String urlLower = urlInvestigated.toLowerCase();
		URL netUrl;
		String host = null;
		try {
			netUrl = new URL(urlLower);
			host = netUrl.getHost();
		} catch (MalformedURLException e) {
			return false;
		}
		if (host == null) {
			return false;
		}
		if (host.endsWith(".dk")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void process_cleanup() {
	}

}
