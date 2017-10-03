package dk.kb.webdanica.core.interfaces.harvesting;

public class HarvestStatusTest {

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
            job = NetarchiveSuiteTools.getNewHarvestStatus(hid);
        }
        if (job == null) {
            System.out.println("No jobs available for harvestdefinition " + argAsString);
        } else {
            System.out.println("Found job: " + job);
        }
        System.exit(0);
    }

}
