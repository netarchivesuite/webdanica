package dk.kb.webdanica.datamodel;

import dk.kb.webdanica.exceptions.WebdanicaException;

public enum Status {
	NEW,
	READY_FOR_HARVESTING,
	HARVESTING_IN_PROGRESS,
	HARVESTING_FINISHED,
	READY_FOR_ANALYSIS,
	ANALYSIS_COMPLETED,
	REJECTED,
	AWAITS_CURATOR_FINALAPPROVAL;

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
			return AWAITS_CURATOR_FINALAPPROVAL;
		default:
			throw new WebdanicaException("Unknown status value: " + status);
		}
	}
}

