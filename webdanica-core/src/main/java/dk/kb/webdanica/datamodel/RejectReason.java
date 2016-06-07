package dk.kb.webdanica.datamodel;

/** Reason for the seed being blacklisted, ie. skipped 
 * TODO discuss with development group 
 */
public enum RejectReason {
   DOMAIN_BLACKLISTED,
   SUFFIX_IGNORED,
   DOMAIN_ALREADY_KNOWN_DANICA,
   MATCHES_BLACKLIST_REGEXPS
}
