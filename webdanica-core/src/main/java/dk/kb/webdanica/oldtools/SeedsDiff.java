package dk.kb.webdanica.oldtools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResults;

import static org.grep4j.core.Grep4j.grep;
import static org.grep4j.core.Grep4j.constantExpression;
import static org.grep4j.core.fluent.Dictionary.on;


public class SeedsDiff {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        File oldseedsdir=new File("/home/svc/workspace/Webdanica-2014-digiliv/seedsdir4");
        File newseedsdir=new File("/home/svc/workspace/Webdanica-2014-digiliv/jobs3/seeds.1");
        File outputdir= new File("/home/svc/workspace/Webdanica-2014-digiliv/jobs3/seeds.new");
        outputdir.mkdirs();
        
        File[] newseeds =  newseedsdir.listFiles(); 
       
        for (File newseed: newseeds) {
            File oldSeedsFile = new File(oldseedsdir, newseed.getName());
            if (!oldSeedsFile.exists()) {
                System.out.println(newseed.getPath() + " has no oldseed. Copying seed-file to outputdir");
                FileUtils.copyFile(newseed, new File(outputdir, newseed.getName()));
            } else {
                System.out.println("Finding new seeds in " + newseed.getAbsolutePath());
                diff(newseed, oldSeedsFile, outputdir);
            }
        }
    }

    private static void diff(File newseed, File oldSeedsFile, File outputDir) throws IOException {
        BufferedReader fr = new BufferedReader(new FileReader(newseed));        
        String line;
        int skipped=0;
        int newseeds=0;
        int lines=0;
        File destinationFile = new File(outputDir, newseed.getName() + ".diff");
        while ((line = fr.readLine()) != null) {
            lines++;
            if ((lines % 5000)== 0) { 
                System.out.print(".");
            }
            String url = line.trim();
            String aUrl = null;
            if (url.endsWith("/")) {
                aUrl = url.substring(0, url.length() -1);
            } else {
                aUrl = url + "/";
            }
            if (!found(url, aUrl, oldSeedsFile)) {
                Writer outWriter = makeWriter(destinationFile);
                outWriter.write(url + "\n");
                outWriter.close();
                newseeds++;
            } else {
                skipped++;
            }
        }
        System.out.println("\nIn file '" + newseed.getPath() + "': (skipped, newseeds) = (" +  skipped + "," 
                +  newseeds + ").");
            
        fr.close();    
            
            
    }

    private static boolean found(String url, String aUrl, File oldSeedsFile) {
        Profile localProfile = ProfileBuilder.newBuilder()
                .name("Local server log")
                .filePath(oldSeedsFile.getAbsolutePath())
                .onLocalhost()
                .build();
        GrepResults resultsUrl = grep(constantExpression(url), on(localProfile));
        GrepResults resultsAurl = grep(constantExpression(aUrl), on(localProfile));
        if (resultsAurl.totalLines() > 0 || resultsUrl.totalLines() > 0) {
            return true;
        }
        return false;
    }

public static Writer makeWriter(File outputfile) throws IOException {
    BufferedWriter out = null;
    FileWriter fstream = new FileWriter(outputfile, true); //true tells to append data.
    out = new BufferedWriter(fstream);
    return out;       
}
    
    
    

}
