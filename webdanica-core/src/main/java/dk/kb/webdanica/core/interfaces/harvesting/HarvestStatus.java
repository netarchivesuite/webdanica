package dk.kb.webdanica.core.interfaces.harvesting;

/**
 * HarvestStatus program to test, if any jobs exists for a given harvestdefinition name or id.
 * If the argument is parseable as a long value, the argument is considered as a valid harvestdefinitionID.
 * Otherwise, we will consider the argument as a name of a harvestdefinition.  
 */
public class HarvestStatus {

    public static void main(String[] args) {
        if (args.length !=1) {
            System.out.println("Missing arg: hid/hname");
            System.exit(1);
        }
        
        String argAsString = args[0];
        boolean IslongArgument = true;
        Long longArg = null;
        try {
            longArg = Long.parseLong(argAsString);
        } catch (NumberFormatException e) {
            IslongArgument = false;
            System.out.println("Argument '" + argAsString + "' is not a valid long. Therefore we consider the argument a harvestdefinitionname");
        }
        NasJob job = null;
        if (IslongArgument) {
            job = NetarchiveSuiteTools.getNewHarvestStatus(longArg);
        } else {
            Long hid = NetarchiveSuiteTools.getHarvestDefinitionID(argAsString);
            if (hid != null) {
                job = NetarchiveSuiteTools.getNewHarvestStatus(hid);
            } 
        }
        if (job == null) {
            System.out.println("No jobs yet available for harvestdefinition " + argAsString);
        } else {
            System.out.println("Found job: " + job);
        }       
        System.exit(0);
    }

}
