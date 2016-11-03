package dk.kb.webdanica.core.datamodel.criteria;

import java.util.Set;
import java.util.TreeSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CriteriaJson {
	private Set<String> keys = new TreeSet<String>();
	private boolean valid;
	private JSONObject o;

	public CriteriaJson(String line) {
		valid = parse(line);
	}
	
	private boolean parse(String line) {
		JSONParser parser = new JSONParser();
    	Object o1 = null;
        try {
	        o1 = parser.parse(line);
        } catch (ParseException e) {
	        e.printStackTrace();
	        return false;
        }
    	o = (JSONObject) o1;
    	keys = o.keySet();
    	return true;
	}
	
	public Set<String> getKeys() {
		return this.keys;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public String getValue(String key) {
		String value = null;
		try {
			value = (String) o.get(key);
		} catch (ClassCastException e) {
			System.err.println("Exception during fetch of value of key '" + key + "'" + e);
		}
		return value;
	}
}