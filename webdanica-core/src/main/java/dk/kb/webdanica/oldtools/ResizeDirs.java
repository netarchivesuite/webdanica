package dk.kb.webdanica.oldtools;

import java.io.File;

public class ResizeDirs {

    /**
     * @param args Dir #files prefix
     */
    public static void main(String[] args) {
        
        if (args.length != 3) {
            System.err.println("Missing args. Needed are: existing dir #files prefix");
            System.exit(1);
        }
        int suffix = 1;
        int count=0;
        int maxfiles = Integer.parseInt(args[1]);
        String prefix = args[2];
        File dir = new File(args[0]);
        
        File[] filesInDir = dir.listFiles();
        
        if (filesInDir == null || filesInDir.length == 0) {
            System.err.println("The given dir is either empty or does not exist");
            System.exit(1);
        }
        
        File movetoDir = new File(dir, prefix + "-" + suffix);
        movetoDir.mkdir();
        System.out.println("initial dir to move to: " + movetoDir.getAbsolutePath());
        
        for (File f : filesInDir) {
            count++;
            if (count > maxfiles){
                count=0;
                suffix++;
                movetoDir = new File(dir, prefix + "-" + suffix);
                movetoDir.mkdir();
                System.out.println("now moving files to move to: " + movetoDir.getAbsolutePath());
            }
            File newfile = new File(movetoDir, f.getName());
            f.renameTo(newfile);
        }

    }

}
