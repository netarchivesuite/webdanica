package dk.kb.webdanica.webapp.resources;

import dk.kb.webdanica.core.datamodel.criteria.CriteriaUtils;

public class HarvestRequest {

	private String seedUrl;
	private String pathInfo;

	public static void main (String[] args) {
		String pathInfo = "/harvests/GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG/";
		HarvestRequest hr = HarvestRequest.getRequest(HarvestsResource.HARVESTS_PATH, pathInfo);
		System.out.println("hr.viewall:" +  hr.viewAll());
		System.out.println("hr.seedUrl:" +  hr.getSeedUrl());
		System.out.println("hr.pathinfo:" +  hr.getPathInfo());
		pathInfo = "/harvests/";
		hr = HarvestRequest.getRequest(HarvestsResource.HARVESTS_PATH, pathInfo);
		System.out.println("hr.viewall:" +  hr.viewAll());
		System.out.println("hr.seedUrl:" +  hr.getSeedUrl());
		System.out.println("hr.pathinfo:" +  hr.getPathInfo());
		
	}
	
	public HarvestRequest(String seedUrl, String pathInfo) {
		this.seedUrl = seedUrl;
		this.pathInfo = pathInfo;
    }

	public static HarvestRequest getRequest(String harvestsPath, String pathInfo) {
		String[] split = pathInfo.split(harvestsPath);
		HarvestRequest resultKeys = new HarvestRequest(null, pathInfo);
        if (split.length > 1) {
        	String arguments = split[1];
            String[] argumentParts = arguments.split("/");
            resultKeys = new HarvestRequest(CriteriaUtils.fromBase64(argumentParts[0]), pathInfo);
 /*           
            if (argumentParts.length == 2) {
            	Status newStatus = Status.fromOrdinal(Integer.parseInt(argumentParts[1]));
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), newStatus, pathInfo);
            	//logger.info("Found Criteriakeys: " + resultKeys);
            } else {
            	resultKeys = new SeedRequest(CriteriaUtils.fromBase64(argumentParts[0]), null, pathInfo);
            }
   */
        }
        return resultKeys;
    }

	public boolean viewAll() {
		return seedUrl == null;
	}
	
	public String getSeedUrl() {
		return this.seedUrl;
	}
	
	public String getPathInfo() {
		return this.pathInfo;
	}
	
	
}
