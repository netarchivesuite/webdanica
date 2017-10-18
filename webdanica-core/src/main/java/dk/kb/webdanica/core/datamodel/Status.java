package dk.kb.webdanica.core.datamodel;

import dk.kb.webdanica.core.exceptions.WebdanicaException;

public enum Status {
	NEW,
	READY_FOR_HARVESTING,
	HARVESTING_IN_PROGRESS,
	HARVESTING_FINISHED, // remove or don't show
	READY_FOR_ANALYSIS,
	ANALYSIS_COMPLETED, // remove or don't show
	REJECTED,
	AWAITS_CURATOR_DECISION,
	HARVESTING_FAILED,
	DONE, // The seed is now either danica or not danica
	ANALYSIS_FAILURE;

	/**
	 * The max valid int value to use in the fromOrdinal(), getInternationalizationHeaderLabel(), 
	 * and getInternationalizationDescriptionLabel() methods.
	 */
	private static final int MAX_VALID_ORDINAL = 10;
	
	
	/**
	 * Helper method that gives a proper object from e.g. a DB-stored value.
	 *
	 * @param status a certain integer
	 * @return the Status related to a certain integer
	 * @throws WebdanicaException
	 */
	public static Status fromOrdinal(int status) {
		switch (status) {
		case 0:
			return NEW;
		case 1:
			return READY_FOR_HARVESTING;
		case 2:
			return HARVESTING_IN_PROGRESS;
		case 3:
			return HARVESTING_FINISHED;
		case 4:
			return READY_FOR_ANALYSIS;
		case 5:
			return ANALYSIS_COMPLETED;
		case 6:
			return REJECTED;
		case 7:
			return AWAITS_CURATOR_DECISION;
		case 8:
			return HARVESTING_FAILED;
		case 9:
			return DONE;
		case 10:
			return ANALYSIS_FAILURE;
		default:
			throw new WebdanicaException("Unknown status value: " + status);
		}
	}
	
	public static String getInternationalizationHeaderLabel(int status) {
		switch (status) {
		case 0:
			return "seed.header.created";
		case 1:
			return "seed.header.ready.for.harvesting";
		case 2:
			return "seed.header.being.harvested";
		case 3:
			return "seed.header.harvesting.finished";
		case 4:
			return "seed.header.ready.for.analysis";
		case 5:
			return "seed.header.analysis.complete";
		case 6:
			return "seed.header.rejected";
		case 7:
			return "seed.header.awaiting.curator.decision";
		case 8:
			return "seed.header.harvesting.failure";
		case 9:
			return "seed.header.done";
		case 10:
			return "seed.header.analysis.failed";
		default:
			throw new WebdanicaException("Unknown status value: " + status);
		}
	}
	public static String getInternationalizationDescriptionLabel(int status) {
		switch (status) {
		case 0:
			return "seed.description.created";
		case 1:
			return "seed.description.ready.for.harvesting";
		case 2:
			return "seed.description.being.harvested";
		case 3:
			return "seed.description.harvesting.finished";
		case 4:
			return "seed.description.ready.for.analysis";
		case 5:
			return "seed.description.analysis.complete";
		case 6:
			return "seed.description.rejected";
		case 7:
			return "seed.description.awaiting.curator.decision";
		case 8: 
			return "seed.description.harvesting.failure";
		case 9:
			return "seed.description.done";	
		case 10:	
			return "seed.description.analysis.failed";
		default:
			throw new WebdanicaException("Unknown status value: " + status);
		}
	}
	
	public static int getMaxValidOrdinal() {
		return MAX_VALID_ORDINAL;
	}

	public static boolean isValidNewState(Integer newState) {
	    if (newState < 0 || newState > MAX_VALID_ORDINAL) {
	    	return false;
	    } 
	    return true;
    }

	public static boolean ignoredState(int i) {
		if (i == 3 || i == 5) {
			return true;
		}
	    return false;
    }
	
}

