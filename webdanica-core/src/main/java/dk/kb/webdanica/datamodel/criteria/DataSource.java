package dk.kb.webdanica.datamodel.criteria;

import dk.kb.webdanica.exceptions.WebdanicaException;

public enum DataSource {
	NETARKIVET, // extracts of harvested outlinks from netarkivet
	IA, // extracts from harvests from Internet Archive (archive.org)
	OTHER // extracts from other sources
;

	public static DataSource fromOrdinal(int int1) {
		DataSource s;
	    switch(int1) {
	    	case 0: s = DataSource.NETARKIVET; break;
	    	case 1: s = DataSource.IA; break;
	    	case 2: s = DataSource.OTHER; break;
	    	default: 
	    		throw new WebdanicaException("DataSource for integer value '" +  int1 + "' unknown");
	    }	
	    return s;
    }
}
