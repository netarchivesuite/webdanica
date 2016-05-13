package dk.kb.webdanica.oldtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.utils.DomainCheck;
import dk.netarkivet.harvester.datamodel.Domain;
import dk.netarkivet.harvester.datamodel.DomainDAO;
import dk.netarkivet.harvester.datamodel.SeedList;
import dk.netarkivet.harvester.datamodel.TLDInfo;


/**
 * Find 
 * Use Definitions-find-domains.jsp and Definitions-domain-statistics.jsp
 * as inspiration.
 * 
 * dk.kb.webdanica.FindDomainsOutsideDKInDatabase
 */
public class FindDomainsOutsideDKInDatabase {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        DomainDAO dao = DomainDAO.getInstance();
        // Brug algoritme i domainStats.jsp side 
        long totalNonDKDomains = 0;
        long possibleDeadDomains = 0;
        long domainsWithNoDefaultSeeds = 0;
 
        //int domainCount = dao.getCountDomains();
        List<TLDInfo> tldList = dao.getTLDs(2);
        for (TLDInfo tld : tldList) {
            String domain;
            if (tld.isIP()) {
                System.out.println("PSEUDO IP TLD ignored");
            } else {
                domain = tld.getName();
                String domainWithoutDot = domain.replaceAll("\\.", "");
                String filename = "tld-" + domainWithoutDot + "-seeds.txt";
                if (!domain.equalsIgnoreCase("dk")) {
                    // Fetch subdomains & write the seeds to tld-domain-seeds.txt
                    List<String> subdomains = dao.getDomains("*." + domain);
                    // This list seems to contain duplicates, so copying to hashset
                    Set<String> subdomainsTrimmed = new HashSet<String>(subdomains);
                    System.out.println("Found #duplicates in list: " 
                            +(subdomains.size() - subdomainsTrimmed.size())); 
                    totalNonDKDomains += subdomainsTrimmed.size();
                    System.out.println("Writing seeds of tld-domain '" + domain 
                            + "' (w. " + subdomainsTrimmed.size() 
                            + " subdomains) to " + filename);
                    
                    File outputFile = new File(filename);
                    Writer output = null;
                    try {
                        output = makeWriter(outputFile);
                        for (String subdomain : subdomainsTrimmed) {

                            boolean possibleDeadDomain = !DomainCheck.isDomainAlive(subdomain);
                            if (possibleDeadDomain) {
                                possibleDeadDomains++;
                            }
                            Domain d = dao.readKnown(subdomain);
                            SeedList defaultseeds = d.getSeedList("defaultseeds");
                            List<String> savedSeeds = defaultseeds.getSeeds();
                            List<String> usefulSeeds = new ArrayList<String>();
                            // Remove uncommented seeds

                            if (savedSeeds.isEmpty()) {
                                output.write("# Domain '" + subdomain 
                                        + "' ignored. no default seeds\n");
                            } else {
                                for (String seed : savedSeeds){
                                    String trimmedSeed = seed.trim();
                                    if (trimmedSeed.isEmpty()){
                                        continue;
                                    }
                                    if (!trimmedSeed.startsWith("#")) {
                                        usefulSeeds.add(trimmedSeed);
                                    } else {
                                        System.out.println("Ignoring uncommented seed: " +  trimmedSeed);
                                    }
                                }
                                if (usefulSeeds.isEmpty()) {
                                    output.write("# Domain '" + subdomain 
                                            + "' ignored. no default seeds\n");
                                    domainsWithNoDefaultSeeds++;
                                } else {
                                    for (String seed: usefulSeeds){
                                        output.write(seed + "\n");
                                    }
                                }
                            }
                            if (possibleDeadDomain) {
                                output.write("# Domain '" + subdomain 
                                        + "' seems dead. nslookup '" + subdomain 
                                        + "' does not respond\n");

                            }
                        }
                    }finally {
                        if (output != null) {
                            output.flush();
                            output.close();
                        }
                    }

                }
            }
            
        }
        System.out.println("TotalNonDKdomains: " + totalNonDKDomains);
        System.out.println("TotalNonDKDomains without defaultseeds: " 
                + domainsWithNoDefaultSeeds);
        System.out.println("Possible dead non-dk domains: " + possibleDeadDomains);
    }
        public static Writer makeWriter(File outputfile) throws IOException {
            BufferedWriter out = null;
            FileWriter fstream = new FileWriter(outputfile, true); //true tells to append data.
            out = new BufferedWriter(fstream);
            return out;       
        }
}
