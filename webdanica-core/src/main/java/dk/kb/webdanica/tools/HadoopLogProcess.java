package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class HadoopLogProcess {

    /**
     * @param args 0: Lines of Current split .... 1: sourcedir 2: destinationdir
     * @throws IOException
     * 
     */
    public static void main(String[] args) throws IOException {
        if (args.length!= 3) {
            System.out.println("Missing args: <Lines with Current split> sourcedir destinationdir");
            System.exit(1);
        }
        File logFile = new File(args[0]);
        File sourceDir = new File(args[1]);
        File destDir = new File(args[2]);
        
        if (!destDir.isDirectory()) {
            System.err.println("Given destdir is not a directory");
            System.exit(1);
        }
        
        File[] gzFiles = sourceDir.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith("json.gz")) {
                    return true;
                }
                return false;
            }
        });
        
        
        
        Set<String> processedFiles = new HashSet<String>();
        
        
        BufferedReader fr = new BufferedReader(new FileReader(logFile));        
        String line;
        
        String trimmedLine = null;
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                doAction(trimmedLine, processedFiles);
            }
        }
        
        System.out.println("#Files found already processed: " + processedFiles.size());
        
        IOUtils.closeQuietly(fr);
        
        System.out.println("Moving files in source dir '" +  sourceDir.getAbsolutePath() 
                + "' with files " + gzFiles.length + " that is not yet processed to dir '" 
                + destDir.getAbsolutePath() + "'");
        
        
        for (File gzFile: gzFiles) {
           if (!processedFiles.contains(gzFile.getName())) {
               System.out.println(gzFile.getAbsolutePath() + " can be moved. Hasn't been processed yet");
           } else {
               System.out.println("File '" + gzFile.getAbsolutePath() + "' has already been processed");
           }
        }
        
        
        
    }

    private static void doAction(String trimmedLine, Set<String> processedFiles) {
        String parts[] = trimmedLine.split("file:"); 
        if (parts.length > 0) {
            String fileparts[] = parts[1].split(":0+");
            String fileparts2[] = fileparts[0].split("/");
            String filename = fileparts2[fileparts2.length -1];
            processedFiles.add(filename);
        } else {
            System.out.println("ignored line: " + trimmedLine);
        }
    }
           
}
