package dk.kb.webdanica.core.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Domain;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.DomainsDAO;
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
		
		System.out.println("Processing domains from file '" + domainsfile.getAbsolutePath() + "'. AcceptAsDanica= " +  acceptAsDanica);

		System.out.println();
		BufferedReader fr = null;
		List<String> logentries = new ArrayList<String>();
		String line = null;
		String domain = null;
		try {
			fr = new BufferedReader(new FileReader(domainsfile));
			while ((line = fr.readLine()) != null) {
				domain = line.trim();
				boolean isValidDomain = DomainUtils.isValidDomainName(domain);
				if (!isValidDomain) {
					logentries.add("REJECTED: '" + domain + "' is not considered a valid domain");
				}
				if (dao.existsDomain(domain)) {
					logentries.add("DUPLICATE: domain '" + domain + "' already exists");
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
						}
					}
				} else {
					Domain newdomain = null;
					if (acceptAsDanica) {
						newdomain = Domain.createNewAcceptedDomain(domain);
					} else {
						newdomain = Domain.createNewUndecidedDomain(domain);
					}
					dao.insertDomain(newdomain);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
    
    
    
    
  
	private static void PrintUsage() {
		System.err.println("Usage: java LoadDomains domainsfile [--accept]");
	    
    }
}
