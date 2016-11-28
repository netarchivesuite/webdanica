package dk.kb.webdanica.core.datamodel;

import dk.kb.webdanica.core.exceptions.WebdanicaException;

public enum AnalysisStatus {
   AWAITING_ANALYSIS, 
   NO_ANALYSIS,
   ANALYSIS_SUCCESSFUL,
   ANALYSIS_FAILED, 
   UNKNOWN_STATUS;
   
   /**
	 * Helper method that gives a proper object from e.g. a DB-stored value.
	 *
	 * @param status a certain integer
	 * @return the AnalysisStatus related to a certain integer
	 * @throws WebdanicaException
	 */
	public static AnalysisStatus fromOrdinal(Integer status) {
		if (status==null) {
			return null;
		}
		switch (status) {
		case 0:
			return AWAITING_ANALYSIS;  
		case 1:
			return NO_ANALYSIS; // with reason= no data available
		case 2:
			return ANALYSIS_SUCCESSFUL;
		case 3:
			return ANALYSIS_FAILED;
		case 4:
			return UNKNOWN_STATUS;
		default:
			throw new WebdanicaException("Unknown AnalysisStatus value: " + status);
		}
	}
   
   
}
