package dk.kb.webdanica.oldtools;

import java.io.File;

public class FindDataToSave {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Missing arg: directory");
            System.exit(1);
        }
        File f = new File(args[0]);
        for (File harvestDir : f.listFiles()) {
            String dirname = harvestDir.getName();
            if (harvestDir.isDirectory() && dirname.contains("-")) {
                String[] nameparts = dirname.split("-");
                int number = Integer.parseInt(nameparts[nameparts.length -1]);
                if (divisibleBy3(number)) {
                    System.out.println("Save harvestdata in directory " + dirname);
                }
            } else {
                System.out.println("Ignore normal file " +  harvestDir.getPath());
            }
        }

    }

    private static boolean divisibleBy3(int number) {
        if ((number % 3) == 0) {
            return true;
        }
        return false;
    }

}
