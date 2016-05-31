package dk.kb.webdanica.datamodel;

import java.util.List;

public class BlackList {
	
	private List<String> theList;
	private Long id; // null, if not added to the database
	private boolean active;
	
	public BlackList(Long id, List<String> aList, boolean isActive) {
		this.id = id;
		this.theList = aList;
		this.active = isActive;
	}

	public List<String> getList() {
		return this.theList;
	}
	
	public void setList(List<String> aList) {
		this.theList = aList;
	}
	
	public Long getId() {
		return this.id;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
}
