package dk.kb.webdanica.core.tools;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaIngest;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;

/**
 * This tool is the final step in the automatic analysis workflow.
 * The automatic-workflow/ingestTool.sh is a wrapper around this tool 
 * called from the automatic-workflow/automatic.sh script.
 * 
 * Usage is: java dk.kb.webdanica.core.tools.CriteriaIngestTool <harvestlogfile> <criteria-results-dir> 
 * [--no-add-harvests-to-database] [--no-add-criteriaResults-to-database]
 */
public class CriteriaIngestTool {
    
    /**
     * Main function for the CriteriaIngestTool.
     * Takes between 2 and 4 arguments.
     * Returns exit-code 1 if missing arguments, or failure when ingesting into the database
     * @param args The arguments (from 2 to 4 arguments)
     * @throws IOException
     * @throws SQLException
     */
	public static void main(String[] args) throws IOException, SQLException {
		if (args.length < 2 || args.length > 4) {
			System.err.println(CriteriaIngestTool.class.getName() + " has missing arguments or too many args (#args given = " + args.length + "). Required #args is between 2 and 4");
			System.err.println("Usage: CriteriaIngestTool <harvestlogfile> <criteria-results-dir> [--no-add-harvests-to-database] [--no-add-criteriaResults-to-database]");
			System.exit(1);
		}
		File harvestLogFile = new File(args[0]);
		File criteriaresultsdir = new File(args[1]);
		if (!harvestLogFile.isFile()) {
			System.err.println("The harvestLogfile located at '" + harvestLogFile.getAbsolutePath() 
					+ "' does not exist or is not a proper file");
			System.exit(1);
		}

		if (!criteriaresultsdir.isDirectory()) {
			System.err.println("The criteriaresultdir located at '" + criteriaresultsdir.getAbsolutePath() 
					+ "' does not exist or is not a proper directory");
			System.exit(1);
		}
		boolean addHarvestToDatabase = true;
		boolean addCriteriaResultsToDatabase = true;
		
		String OPTIONAL_ARG_ONE = "--no-add-harvests-to-database";
		String OPTIONAL_ARG_TWO = "--no-add-criteriaResults-to-database";
 		
		if (args.length == 4) { // args.length == 4
			if (args[2].equalsIgnoreCase(OPTIONAL_ARG_TWO)) {
				addCriteriaResultsToDatabase= false;
			} else if (args[3].equalsIgnoreCase(OPTIONAL_ARG_TWO)) {
				addCriteriaResultsToDatabase= false;
			} else {
				System.err.println("Argument '" + OPTIONAL_ARG_TWO + "' not found in command.");
			}
			if (args[2].equalsIgnoreCase(OPTIONAL_ARG_ONE)) {
				addHarvestToDatabase= false;
			} else if (args[3].equalsIgnoreCase(OPTIONAL_ARG_ONE)) {
				addHarvestToDatabase = false;
			} else {
				System.err.println("Argument '" + OPTIONAL_ARG_ONE + "' not found in command.");
			}
		} else if (args.length == 3) {
			if (args[2].equalsIgnoreCase(OPTIONAL_ARG_ONE)) {
				addHarvestToDatabase = false;
			} else if (args[2].equalsIgnoreCase(OPTIONAL_ARG_TWO))  {
				addCriteriaResultsToDatabase = false;
			} else {
				System.err.println("Argument '" +  args[2] + "' invalid and thus ignored");
			}
		}
		
		if (!addHarvestToDatabase) {
			System.err.println("User disabled adding harvests to database");
		}
		if (!addCriteriaResultsToDatabase) {
			System.err.println("User disabled adding criteriaresults to database");	
		}
		DAOFactory daofactory = DatabaseUtils.getDao();
		try {
			CriteriaIngest.ingest(harvestLogFile, criteriaresultsdir, addHarvestToDatabase, addCriteriaResultsToDatabase, daofactory);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			daofactory.close();			
		}
		System.exit(0);
	}

}
