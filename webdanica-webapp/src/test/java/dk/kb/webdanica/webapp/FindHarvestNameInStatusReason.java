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
		String statusReason2 = "Ready for analysis retry of harvest 'webdanica-trial-1481183255636'";
		
		String hname3 = SeedsResource.findHarvestNameInStatusReason(statusReason2);
		System.out.println(hname3);
		
		String statusReason3 = "Ready for analysis retry of harvest 'webdanica-trial-1481183255636";
		String hname4 = SeedsResource.findHarvestNameInStatusReason(statusReason3);
		System.out.println(hname4);
		
		SeedsRequest sr = SeedsRequest.getUrlFromPathinfo("/seeds/3/1/", SeedsResource.SEEDS_PATH);
		System.out.println(sr);
		sr = SeedsRequest.getUrlFromPathinfo("/seeds/3/", SeedsResource.SEEDS_PATH);
		System.out.println(sr);
	}
}
