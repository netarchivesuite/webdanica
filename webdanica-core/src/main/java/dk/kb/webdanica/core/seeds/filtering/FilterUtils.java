package dk.kb.webdanica.core.seeds.filtering;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import dk.kb.webdanica.core.datamodel.BlackList;
import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.datamodel.dao.DomainsDAO;

public class FilterUtils {
    
    private static final Logger logger = Logger.getLogger(FilterUtils.class.getName());
    
    /**
     * Investigate if the given seed is to be rejected.
     * 
     * @param s a given seed
     * @param blacklists a list of Blacklists
     * @return true if the seed is to be rejected by the filtering methods;
     *         false, if not rejected           
     * @throws Exception If anything goes wrong (database problems)
     * 
     */
    public static boolean doFilteringOnSeed(Seed s, List<BlackList> blacklists,
            boolean rejectDKUrls, DomainsDAO domainDAO) throws Exception {
        // We test on the redirectedUrl, if it exists
        String urlInvestigated = s.getUrl();
        String redUrl = s.getRedirectedUrl();
        if (redUrl != null) {
            logger.info("Testing on redirect url '" + redUrl
                    + "' instead of original url '" + s.getUrl() + "'");
            urlInvestigated = redUrl;
        }
        
        // Test 0: test for ignored protocols (if not already rejected at Ingest)
        String ignoredProtocol = IgnoredProtocols.matchesIgnoredProtocol(urlInvestigated);
        if (ignoredProtocol != null) {
            s.setStatus(Status.REJECTED);
            s.setStatusReason("REJECTED because it matches ignored protocol '"
                    + ignoredProtocol + "'");
            return true;
        }
        
        // Test 1: test for ignored suffixes
        String ignoredSuffix = IgnoredSuffixes
                .matchesIgnoredExtension(urlInvestigated);
        if (ignoredSuffix != null) {
            s.setStatus(Status.REJECTED);
            s.setStatusReason("REJECTED because it matches ignored suffix '"
                    + ignoredSuffix + "'");
            return true;
        }
        // Test 2: test for matching any regular expression in the active
        // blacklists
        for (BlackList blackList : blacklists) {
            String result = blackList.evaluateUrl(urlInvestigated);
            if (result != null) {
                s.setStatus(Status.REJECTED);
                s.setStatusReason("REJECTED because it matches regular expression '"
                        + result
                        + "' in blacklist '"
                        + blackList.getName()
                        + "'");
                return true;
            }
        }

        // Test 3: test that url is not from the .DK top level domain. unless
        // WebdanicaSettings.REJECT_DK_URLS is set to false
        if (rejectDKUrls && belongsToDK(urlInvestigated)) {
            s.setStatus(Status.REJECTED);
            s.setStatusReason("REJECTED because the seed '"
                    + urlInvestigated
                    + "' belongs to the .dk toplevel and by default is part of legal deposit");
            return true;
        }
        // Test 4: test that the url does not belong to a domain which
        // danicastatus is YES or NO
        Domain d = domainDAO.getDomain(s.getDomain());
        if (d != null) {
            if (d.getDomain().equals("N/A")) {
                logger.warning("Seed '" + s.getUrl()
                        + "' belongs to bogus domain '" + d.getDomain() + "'");
            }
            DanicaStatus ds = d.getDanicaStatus();
            if (ds.equals(DanicaStatus.NO)) {
                s.setStatus(Status.REJECTED);
                s.setStatusReason("REJECTED because the seed '"
                        + urlInvestigated + "' belongs to the domain '"
                        + d.getDomain()
                        + "' which has been rejected as DANICA domain");
                return true;
            } else if (ds.equals(DanicaStatus.YES)) {
                s.setStatus(Status.REJECTED);
                s.setStatusReason("REJECTED because the seed '"
                        + urlInvestigated + "' belongs to the domain '"
                        + d.getDomain()
                        + "' which has been accepted as a full DANICA domain");
                return true;
            }
        } else {
            logger.warning("Domain '" + s.getDomain() + "' of seed '"
                    + s.getUrl() + "' is not in the database");
        }
        return false;
    }

    public static boolean belongsToDK(String urlInvestigated) {
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

}
