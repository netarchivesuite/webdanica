package dk.kb.webdanica.core.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.IngestLog;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.DomainsDAO;
import dk.kb.webdanica.core.datamodel.dao.IngestLogDAO;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.netarkivet.common.utils.DomainUtils;

/**
 * Tool for ingesting domains into the webdanica system.
 * Usage java LoadDomains domainfile [--accept] 
 * 
 * If --accept is used, the domains are marked as danica
 * Otherwise their state is Unknown
 * 
 * In any case, if the domain is already in the domains table, the domain is unchanged
 */
public class LoadDomains {
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Need domainfile as argument.");
			PrintUsage();
			System.exit(1);
		} else if (args.length > 2) {
			System.err.println("Too many arguments. Only two accepted.");
			PrintUsage();
			System.exit(1);
		}

		File domainsfile = new File(args[0]);
		if (!domainsfile.isFile()){
			System.err.println("The domainfile located '" + domainsfile.getAbsolutePath() + "' does not exist or is not a proper file");
			System.exit(1);
		}
		boolean acceptAsDanica = false;
		if (args.length == 2){
			if (args[1].equalsIgnoreCase("--accept")) {
				acceptAsDanica = true;
			} else {
				System.err.println("Unknown argument '" + args[1] + "' ignored.");
			}
		}
		DAOFactory daofactory = DatabaseUtils.getDao();
		DomainsDAO dao = daofactory.getDomainsDAO();
		IngestLogDAO idao = daofactory.getIngestLogDAO();
		System.out.println("Processing domains from file '" + domainsfile.getAbsolutePath() + "'. AcceptAsDanica= " +  acceptAsDanica);
		System.out.println();
		BufferedReader fr = null;
		Set<String> logentries = new TreeSet<String>();
		String line = null;
		String domain = null;
		long insertedCount = 0;
		long linecount = 0;
		long rejectedCount = 0;
		long duplicateCount = 0;
		try {
			fr = new BufferedReader(new FileReader(domainsfile));
			while ((line = fr.readLine()) != null) {
				domain = line.trim();
				linecount++;
				boolean isValidDomain = DomainUtils.isValidDomainName(domain);
				if (!isValidDomain) {
					logentries.add("REJECTED: '" + domain + "' is not considered a valid domain");
					rejectedCount++;
					continue;
				} 					
				if (dao.existsDomain(domain)) {
					if (acceptAsDanica) { // check if the domain is already marked as danica
						Domain d = dao.getDomain(domain);
						if (!d.getDanicaStatus().equals(DanicaStatus.YES)) {
							DanicaStatus oldstate = d.getDanicaStatus();
							d.setDanicaStatus(DanicaStatus.YES);
							d.setDanicaStatusReason("Accepted as danica domain by user");
							String notes = d.getNotes();
							String notesToAdd = "[" + new Date() + "] Domain '" 
							 + domain + "' changed from danicastate '" +  oldstate + "' to danicastate '" +  DanicaStatus.YES + "' by user of LoadDomains"; 
							if (notes == null || notes.isEmpty()) {
								d.setNotes(notesToAdd);
							} else {
								d.setNotes(notes + "," + notesToAdd);
							}
							dao.update(d);
							logentries.add("UPDATED: domain '" + domain + "' changed from DanicaStatus '" +  oldstate + "' to '" + DanicaStatus.YES + "'");
							// TODO update state of all seeds belonging to domain not yet processed (READY_FOR_HARVEST)
						} else {
							logentries.add("NOT UPDATED: domain '" + domain + "' already has DanicaStatus DanicaStatus.YES");
						}
					} else {
						logentries.add("DUPLICATE: domain '" + domain + "' already exists");
						duplicateCount++;
					}
				} else {
					Domain newdomain = null;
					if (acceptAsDanica) {
						newdomain = Domain.createNewAcceptedDomain(domain);
						logentries.add("INSERTED: added domain '" + domain + "' as known Danica domain");
					} else {
						newdomain = Domain.createNewUndecidedDomain(domain);
						logentries.add("INSERTED: added domain '" + domain + "' with undecided Danica status");
					}
					dao.insertDomain(newdomain);
					insertedCount++;
				}
			}
			List<String> logentriesList = new ArrayList<String>(logentries);
			idao.insertLog(new IngestLog(logentriesList,domainsfile.getName(), linecount, insertedCount, rejectedCount, duplicateCount));
			File updateLog = null;
			boolean writeUpdateLog = true;
			if (writeUpdateLog) {
	        	updateLog = new File(domainsfile.getParentFile(), domainsfile.getName() + ".ingestlog.txt");
	        	int count=0;
	        	while (updateLog.exists()) {
	        		updateLog = new File(domainsfile.getParentFile(), domainsfile.getName() + ".ingestlog.txt" + "." + count);
	        		count++;
	        	}
	        	PrintWriter updatedWriter = new PrintWriter(new BufferedWriter(new FileWriter(updateLog)));
	        	String updatedHeader = "Update and domain Log for file '" + domainsfile.getAbsolutePath() + "' ingested at '" 
	        			+ new Date() + "'";
	        	updatedWriter.println(updatedHeader);
	        	updatedWriter.println();
	        	updatedWriter.println("Stats: inserted = " + insertedCount + ", rejected = " + rejectedCount + ", duplicateCount = " + duplicateCount + ", linecount=" + linecount);
	        	updatedWriter.println();
	        	if (!logentries.isEmpty()) {
	        		updatedWriter.println("domain-log - entries:");
	        		for (String rej: logentries) {
	        			updatedWriter.println(rej);
	        		}
	        	}
	        	updatedWriter.close();
	        }
			System.out.println("Finished processing domains file '" + domainsfile.getAbsolutePath() + "'.");
			if (updateLog != null) {
				System.out.println("Result of loaddomains operation is written to  '" + domainsfile.getAbsolutePath() + "'.");
			}	
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("Loaddomains program crashed");
			System.exit(1);
		}
	}
  
	private static void PrintUsage() {
		System.err.println("Usage: java LoadDomains domainsfile [--accept]");
	    
    }
}
