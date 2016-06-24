package dk.kb.webdanica.webapp.resources;


public class Resource {

	final int resource_id;

    final ResourceAbstract resources;

    final boolean bSecured;

    public Resource(int resourceId, ResourceAbstract resources, boolean secured) {
    	this.resource_id = resourceId;
    	this.resources = resources;
    	this.bSecured = secured;
    }
    
    public boolean isSecured() {
    	return bSecured;
    }
    
    public int getId() {
    	return resource_id;
    }
    
    public ResourceAbstract getResources() {
    	return resources;
    }
    
    public String toString(){
    	return "Resource w/ id= " + resource_id 
    			+ ", Resource-class=" + resources.getClass().getName() + ", secured=" +  bSecured;
    }    
}