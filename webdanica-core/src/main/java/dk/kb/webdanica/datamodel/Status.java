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
	 * The max valid int value to use in the fromOrdinal(), getInternationalizationHeaderLabel(), 
	 * and getInternationalizationDescriptionLabel() methods.
	 */
	private static final int MAX_VALID_ORDINAL = 7;
	
	
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
	
	public static String getInternationalizationHeaderLabel(int status) {
		switch (status) {
		case 0:
			return "seed.header.created";
		case 1:
			return "seed.header.ready.for.havesting";
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
			return "seed.header.awaiting.final.curator.approval";
		default:
			throw new WebdanicaException("Unknown status value: " + status);
		}
	}
	public static String getInternationalizationDescriptionLabel(int status) {
		switch (status) {
		case 0:
			return "seed.description.created";
		case 1:
			return "seed.description.ready.for.havesting";
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
			return "seed.description.awaiting.final.curator.approval";
		default:
			throw new WebdanicaException("Unknown status value: " + status);
		}
	}
	
	public static int getMaxValidOrdinal() {
		return MAX_VALID_ORDINAL;
	}
}

