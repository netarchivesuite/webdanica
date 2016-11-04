package dk.kb.webdanica.core.datamodel;

import dk.kb.webdanica.core.exceptions.WebdanicaException;

public enum DanicaStatus {
   UNDECIDED, 
   NO,
   IMPROBABLE,
   PROBABLE,
   YES,
   PARTIALLY;
   
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
			return UNDECIDED;  
		case 1:
			return NO;
		case 2:
			return IMPROBABLE;
		case 3:
			return PROBABLE;
		case 4:
			return YES;
		case 5:	
			return PARTIALLY;
		default:
			throw new WebdanicaException("Unknown DanicaStatus value: " + status);
		}
	}
   
   
}
