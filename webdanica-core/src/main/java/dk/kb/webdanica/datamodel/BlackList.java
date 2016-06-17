package dk.kb.webdanica.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

public class BlackList {
	
	 /** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(BlackList.class.getName());
    
	private List<String> theList;
	private UUID uid; // null, if not added to the database
	private boolean active;
	private String name; // name of list
	private String description; // description of list
	private Long last_update; // milliseconds since epoch
	
	public BlackList(String name, String description, List<String> aList, boolean isActive) {
		this.name = name;
		this.description = description;
		this.theList = aList;
		this.active = isActive;
		
	}
	public BlackList(UUID uid, String name, String description, List<String> aList, Long lastupdate, boolean isActive) {
		this.name = name;
		this.description = description;
		this.theList = aList;
		this.last_update = lastupdate;
		this.active = isActive;
		this.uid = uid;
	}
	
	public List<String> getList() {
		return this.theList;
	}
	
	public List<Pattern> getListAsPatterns() {
		List<Pattern> patternList = new ArrayList<Pattern>();
		for (String regex : theList) {
			if (isValidPattern(regex)){
				Pattern p = Pattern.compile(regex);
				patternList.add(p);
			} else {
				logger.warning("Ignoring invalid pattern '" + regex + "'");
			}
		}
		return patternList;
	}
	
	public UUID getUid() {
		return this.uid;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	public Long getLastUpdate() {
		return this.last_update;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Blacklist with uid=" + uid + ", name= " + name + ", description=" + description + ", isactive=" +  isActive() 
				+ ", lastupdate = " + last_update + ", list of size " + theList.size() + " = [" + StringUtils.join(theList, ",") + "]");
		return sb.toString();
		
	}
	
	/**
	 * Check a given pattern if it is a valid java regular expression.  
	 * @param regexp a given regular expression
	 * @return true, if the pattern is a valid java regular expression.
	 */
	public static boolean isValidPattern(String regexp) {
		try {
			Pattern.compile(regexp);
		} catch (PatternSyntaxException e) {
			return false;
		}
		return true;
	}

	/**
	 * Evaluates a given url on blacklist
	 * @param url a given url
	 * @return the first matching regular expression in the blacklist, or null if no matches  
	 */
	public String evaluateUrl(String url) {

		List<Pattern> regexes = getListAsPatterns();
		if(regexes.size()==0){
			logger.warning("Evaluating url on empty blacklist");
			return null;
		}
		for (Pattern p: regexes) {
			boolean matches = p.matcher(url).matches();
			if (matches) {
				logger.info("Url '" + url + "' matched pattern '" + p.pattern() + "'"); 
				return p.pattern();
			}
		}
		return null;
	}		
}
