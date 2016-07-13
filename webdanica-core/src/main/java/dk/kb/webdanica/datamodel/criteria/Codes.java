package dk.kb.webdanica.datamodel.criteria;

import java.util.HashSet;
import java.util.Set;

public class Codes {
	
	public static int  cat_ERROR_dk = 9000; //getCodesForUdgaaede
    public static int  cat_ignored_dk = 9100; //getCodesForFrasorterede
    public static int  cat_not_likely_dk = 9101; //getCodesForNOTDanishResults
    public static int  cat_unknown_dk = 9200;	//Not decided 0 and negative
    public static int  cat_maybes_dk = 9500;	//getCodesForMaybees
    public static int  cat_likely_dk = 9999; //getCodesForDanishResults

    //cat_likely_dk
    public static Set<Integer> getCodesForDanishResults() {
		Set<Integer> codeSet = new HashSet<Integer>();
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

}
