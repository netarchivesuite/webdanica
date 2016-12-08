package dk.kb.webdanica.webapp;

import dk.kb.webdanica.webapp.resources.SeedsRequest;
import dk.kb.webdanica.webapp.resources.SeedsResource;

public class FindHarvestNameInStatusReason {
	
	public static void main(String[] args) {	
		String statusReason = "Harvesting finished successfully. harvestname is webdanica-trial-1480526988439";
		String hname = SeedsResource.findHarvestNameInStatusReason(statusReason);
		System.out.println(hname);
		
		String statusReason1 = "Harvesting finished successfully. harvestname is 'webdanica-trial-1481124207853'. Now ready for analysis";
		String hname2 = SeedsResource.findHarvestNameInStatusReason(statusReason1);
		System.out.println(hname2);
		SeedsRequest sr = SeedsRequest.getUrlFromPathinfo("/seeds/3/1/", SeedsResource.SEEDS_PATH);
		System.out.println(sr);
		sr = SeedsRequest.getUrlFromPathinfo("/seeds/3/", SeedsResource.SEEDS_PATH);
		System.out.println(sr);
	}
}
