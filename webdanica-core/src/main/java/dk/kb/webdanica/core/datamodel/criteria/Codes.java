package dk.kb.webdanica.core.datamodel.criteria;

import java.util.HashSet;
import java.util.Set;

import dk.kb.webdanica.core.exceptions.WebdanicaException;

public class Codes {
	
	public static enum Category {
		ERROR, // error. should not be used
		IGNORED, 
		NOTLIKELY_DK, 
		UNKNOWN, // values <= 0
		MAYBE_DK,
		LIKELY_DK,
		BUG // Signifies bug in code allocation
	};
	
	/**
	 * Constants for belonging to different categories.
	 */
	public static final int cat_ERROR_dk = 9000; //getCodesForUdgaaede
    public static final int cat_ignored_dk = 9100; //getCodesForFrasorterede
    public static final int cat_not_likely_dk = 9101; //getCodesForNOTDanishResults
    public static final int cat_unknown_dk = 9200;	//Not decided 0 and negative
    public static final int cat_maybes_dk = 9500;	//getCodesForMaybees
    public static final int cat_likely_dk = 9999; //getCodesForDanishResults

    //cat_likely_dk
    public static Set<Integer> getCodesForLikelyDanishResults() {
		Set<Integer> codeSet = new HashSet<Integer>();
		codeSet.add(4); // language is 'da' with 90 % certainty
	    for (int code=20; code<=27; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=40; code<=47; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(72); 
	    codeSet.add(110); 
	    codeSet.add(120); 
	    codeSet.add(123); 
	    codeSet.add(126); 
	    codeSet.add(310); 
	    codeSet.add(315); 
	    codeSet.add(320); 
	    return codeSet;
	}

    //cat_ignored_dk
	public static Set<Integer> getCodesForFrasorterede() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(1); 
	    codeSet.add(3); 
	    return codeSet;
	}
		
    //ERROR_dk
	public static Set<Integer> getCodesForUdgaaede() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(5); 
	    codeSet.add(6); 
	    codeSet.add(7); 
	    codeSet.add(8); 
	    for (int code=70; code<=79; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=200; code<=203; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=206; code<=209; code++) {
	    	codeSet.add(code);
	    }
	    return codeSet;
	}
	
	//cat_maybes_dk
	public static Set<Integer> getCodesForMaybees() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(111); 
	    codeSet.add(121); 
	    codeSet.add(124); 
	    codeSet.add(127); 
	    codeSet.add(130);
	    codeSet.add(230);
	    codeSet.add(311); 
	    codeSet.add(313); 
	    codeSet.add(316); 
	    codeSet.add(318); 
	    codeSet.add(321); 
	    codeSet.add(322); 
	    codeSet.add(323); 
	    codeSet.add(324); 
	    codeSet.add(326); 
	    return codeSet;
	}
	
	//cat_not_likely_dk
	public static Set<Integer> getCodesForNOTDanishResults() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(2); //ignore 1 and 3
	    for (int code=10; code<=12; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=30; code<=35; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(38); 
	    for (int code=50; code<=55; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(58); 
	    for (int code=100; code<=107; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(112); 
	    codeSet.add(122); 
	    codeSet.add(125); 
	    codeSet.add(128); 
	    codeSet.add(220); 
	    codeSet.add(301); 
	    codeSet.add(302);
	    codeSet.add(312); 
	    codeSet.add(317); 
	    codeSet.add(327); 
	    return codeSet;
	}
	
	public static Set<Integer> findCategoryCodes(int category) {
		Set<Integer> codeSet = new HashSet<Integer>();
		switch (category) {
		case 0: 
			codeSet.add(0);
			break;
		case Codes.cat_ignored_dk: //9100;
			codeSet = Codes.getCodesForFrasorterede();
			break;
		case Codes.cat_not_likely_dk: 	// = 9101;
			codeSet = Codes.getCodesForNOTDanishResults();
			break;
		case Codes.cat_maybes_dk: 		// = 9500;
			codeSet = Codes.getCodesForMaybees();
			break;
		case Codes.cat_likely_dk: // = 9999
			codeSet = Codes.getCodesForLikelyDanishResults();
			break;
			//case cat_unknown_dk:	not implemented 	//= 9200; Not decided 0 and negative
		default:	 
			throw new WebdanicaException("Unknown category: " + category);
		}
		return codeSet;
	}
	
	public static Codes.Category getCategory(int code) {
		if (code <= 0) {
			return Codes.Category.UNKNOWN;
		} else if (getCodesForFrasorterede().contains(code)) {
			return Codes.Category.IGNORED;
		} else if (getCodesForLikelyDanishResults().contains(code)) {
			return Codes.Category.LIKELY_DK;
		} else if (getCodesForMaybees().contains(code)) {
			return Codes.Category.MAYBE_DK;
		} else if (getCodesForNOTDanishResults().contains(code)) {
			return Codes.Category.NOTLIKELY_DK;
		} else if (getCodesForUdgaaede().contains(code)) {
			return Codes.Category.ERROR;
		} else {
			return Codes.Category.BUG; // Catch-all-the-rest signifying bug in code allocation
		}
		
	}
}
