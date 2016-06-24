package dk.kb.webdanica.webapp.resources;

public class ResourceDescription {

	private String path;
	private boolean secured;

	public ResourceDescription(String path, boolean secured) {
	    this.path = path;
	    this.secured = secured;
    }

	public boolean isSecure() {
		return this.secured;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String toString() {
		return "Path='" + path + "', secured=" + secured;
	}
}



