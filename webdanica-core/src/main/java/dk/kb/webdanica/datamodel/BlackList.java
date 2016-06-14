package dk.kb.webdanica.datamodel;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;


public class BlackList {
	
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
				+ ", lastupdate = " + last_update + ", list=[" + StringUtils.join(theList, ",") + "]");
		return sb.toString();
		
	}
		
}
