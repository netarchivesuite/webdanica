package dk.kb.webdanica.webapp;

import dk.kb.webdanica.webapp.resources.SeedsResource;

public class FindHarvestNameInStatusReason {
	
	public static void main(String[] args) {
		
		String statusReason = "Harvesting finished successfully. harvestname is webdanica-trial-1480526988439";
		String hname = SeedsResource.findHarvestNameInStatusReason(statusReason);
		System.out.println(hname);
	}
	
	
}
