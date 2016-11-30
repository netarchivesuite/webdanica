package dk.kb.webdanica.core.tools;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.kb.webdanica.core.criteria.C1;
import dk.kb.webdanica.core.criteria.C2;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;

public class TestRules {

	public static void main(String[] args) throws Exception {
		boolean onlyOneUrl = false;
		String url = null;
		if (args.length == 1) {
			onlyOneUrl = true;
			url = args[0];
		}
		
		DAOFactory daofactory = DatabaseUtils.getDao();
		CriteriaResultsDAO cdao = daofactory.getCriteriaResultsDAO();
		List<SingleCriteriaResult> results = null;
		if (onlyOneUrl) {
			results = cdao.getResultsByUrl(url);
		} else {
			results = cdao.getResults();
		}
		if (results != null && !results.isEmpty()) {
			if (onlyOneUrl) {
				System.out.println("Found " + results.size() + " criteria results for url '" + url + "'"); 
			} else {
				System.out.println("Found " + results.size() + " criteria results in database");
			}
			for (SingleCriteriaResult s: results) {
				System.out.println("Handling url '" + s.url + "' harvested by harvest '" +  
						s.harvestName + "' inserted at + '" +  s.insertedDate + "'");
				 System.out.println();
			computeC1(s);
			computeC2b(s);
			}
		} else {
			System.out.println("No results found for url: " + url);
		}
		System.exit(0);
	}

	private static void computeC1(SingleCriteriaResult s) throws IOException {
		 List<String> links = s.getClinks(); // Converted from BAs64 when read from database
		 String text = s.getCText();
		 if (links != null && !links.isEmpty()) {
			 System.out.println("links found in DB");
			 /*
			 for (String link: links) {
				 System.out.println(link);
			 }*/
			 Set<String> resultSet = new HashSet<String>();
			 resultSet.addAll(C1.computeC1(text));
			 System.out.println("C1 hits found after looking in text: " + resultSet.size());
			 resultSet.addAll(C1.computeC1a(links));
			 System.out.println("C1 hits found after looking in linkset: " + resultSet.size());
			 System.out.println();
			 System.out.println("the "+ resultSet.size() + " C1 hits:");
			 for (String res: resultSet) {
				 System.out.println(res);
				 
			 }
		 } else {
			 System.out.println("No links found in DB for url '" + s.url + "'.");
		 }
    }
	private static void computeC2b(SingleCriteriaResult s) throws IOException {
		 System.out.println();
		 String text = s.getCText();
		 if (text != null) {
			 System.out.println("text found in DB");
			 System.out.println("result of c2b: " + C2.computeC2b(text));
			 System.out.println("result of c2bAlt: " + C2.computeC2bAlt(text));
			 System.out.println("result of c2bNoCase: " + C2.computeC2bNoCase(text));
		 } else {
			 System.out.println("No text found in DB for url '" + s.url + "'.");
		 }
		 
    }

}
