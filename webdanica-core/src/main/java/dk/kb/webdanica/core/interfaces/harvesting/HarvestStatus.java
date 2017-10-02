package dk.kb.webdanica.core.interfaces.harvesting;

import dk.netarkivet.harvester.datamodel.JobStatusInfo;

public class HarvestStatus {

    public static void main(String[] args) {
        if (args.length !=1) {
            System.out.println("Missing arg: hid/hname");
            System.exit(1);
        }
        
        String argAsString = args[0];
        boolean IslongArgument = true;
        JobStatusInfo jsi = null;
        Long longArg = null;
        try {
            longArg = Long.parseLong(argAsString);
        } catch (NumberFormatException e) {
            IslongArgument = false;
            System.out.println("Argument '" + argAsString + "' is not a valid long. Therefore we consider the argument a harvestdefinitionname");
        }
        if (IslongArgument) {
            jsi = NetarchiveSuiteTools.getHarvestStatus(longArg);
        } else {
            jsi = NetarchiveSuiteTools.getHarvestStatus(argAsString);
        }

        if (jsi != null) {
            System.out.println("JobId=" + jsi.getJobID() + ", jobstatus=" + jsi.getStatus());
        } else {
            System.out.println("No valid information for harvestdefinition " + argAsString);
        }
        
        System.exit(0);
    }

}
