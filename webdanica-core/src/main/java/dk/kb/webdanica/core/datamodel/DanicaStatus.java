package dk.kb.webdanica.core.datamodel;

import dk.kb.webdanica.core.exceptions.WebdanicaException;

public enum DanicaStatus {
   BEING_INVESTIGATED, 
   NO,
   IMPROBABLE,
   PROBABLE,
   YES;
   
   /**
	 * Helper method that gives a proper object from e.g. a DB-stored value.
	 *
	 * @param status a certain integer
	 * @return the DanicaStatus related to a certain integer
	 * @throws WebdanicaException
	 */
	public static DanicaStatus fromOrdinal(int status) {
		switch (status) {
		case 0:
			return BEING_INVESTIGATED; // OR UNKNOWN / UNDECIDED
		case 1:
			return NO;
		case 2:
			return IMPROBABLE;
		case 3:
			return PROBABLE;
		case 4:
			return YES;
		default:
			throw new WebdanicaException("Unknown DanicaStatus value: " + status);
		}
	}
   
   
}
