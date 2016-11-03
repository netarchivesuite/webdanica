package dk.kb.webdanica.core.datamodel;

public enum URL_REJECT_REASON {
	BAD_URL, // improper url (no scheme
	BAD_SCHEME, // mailto,... (configurable)
    MISSING_DOMAIN, // url imcomplete: no domain e.g. 
	DUPLICATE, // url already in seeds table
	NONE // url is ok
}
