package dk.kb.webdanica.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.harvester.datamodel.HeritrixTemplate;

public class CreateLargeHarvests {

    /**
     * @param args
     * @throws DocumentException 
     * @throws IOException 
     */
    public static void main(String[] args) throws DocumentException, IOException {
        if (args.length != 4) {
            System.err.println("Missing args. Usage is CreateHarvests heritrixfolder "
            		+ "seedsfolder destinationfolder template");
            System.exit(1);
        }
        File heritrixFolder = new File(args[0]);
        File seedsFolder = new File(args[1]);
        File destinationFolder = new File(args[2]);
        File template = new File(args[3]);
        if (!heritrixFolder.isDirectory()) {
            System.err.println("HeritrixFolder '" 
                    + heritrixFolder.getAbsolutePath() + "' not found");
            System.exit(1);
        }
        
        if (!seedsFolder.isDirectory()) {
            System.err.println("seedsFolder '" 
                    + seedsFolder.getAbsolutePath() + "' not found");
            System.exit(1);
        }
        
        if (destinationFolder.isDirectory()) {
            System.err.println("destinationFolder '" 
                    + destinationFolder.getAbsolutePath() + "' already found. Probably an error");
            System.exit(1);
        }
        destinationFolder.mkdirs();
        if (!destinationFolder.isDirectory()) {
            System.err.println("Unable to create destinationFolder '" 
                    + destinationFolder.getAbsolutePath() + "'");
            System.exit(1);
        } 
        
        if (!template.isFile()) {
            System.err.println("template '" 
                    + template.getAbsolutePath() + "' not found");
            System.exit(1);
        }
        
        File[] seedsfiles = seedsFolder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith("-seeds.txt")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        
        for (File seedsfile : seedsfiles) {
            
            // create subdir of destination
            String seedsfilename = seedsfile.getName();
            String tld = seedsfilename.split("-")[1] + "-" + seedsfilename.split("-")[2];  
            File dest = new File(destinationFolder, tld);
            String destinationFullPath = dest.getAbsolutePath();
            dest.mkdir();
            if (!dest.isDirectory()) {
                System.err.println("Unable to create dir '" 
                        + dest.getAbsolutePath() + "'");
                System.exit(1); 
            }
            
            // copy heritrix-dir to subdir
            File heritrixFolderCopy = new File(dest, heritrixFolder.getName());
            FileUtils.copyDirectory(heritrixFolder, heritrixFolderCopy);
            
            // copy template & seedsfile to heritrix-dir
            File copiedTemplate = new File(heritrixFolderCopy, template.getName());
            File copiedSeedsFile = new File(heritrixFolderCopy, seedsfilename);
            
            FileUtils.copyFile(template, copiedTemplate);
            FileUtils.copyFile(seedsfile, copiedSeedsFile);
            // modify copied-template
            FileUtils.copyFile(copiedTemplate, new File(copiedTemplate.getParentFile(), copiedTemplate.getName() 
                    + ".org"));
            
            SAXReader reader = new SAXReader();
            Document orderXMLdoc = reader.read(copiedTemplate);
            updateXpath(orderXMLdoc, HeritrixTemplate.QUOTA_ENFORCER_ENABLED_XPATH,
                    Boolean.toString(true));
            
           
            updateXpath(orderXMLdoc, HeritrixTemplate.DISK_PATH_XPATH, destinationFullPath); 
            updateXpath(orderXMLdoc, HeritrixTemplate.SEEDS_FILE_XPATH, 
                    copiedSeedsFile.getAbsolutePath());
            updateXpath(orderXMLdoc, HeritrixTemplate.ARCHIVEFILE_PREFIX_XPATH, "Webdanica-tld-" + tld);
            // Dette virker ikke for facebook / myspace
            //updateXpath(orderXMLdoc, HeritrixTemplate.MAXTIMESEC_PATH_XPATH, 
            //        "9000"); // max 2.5 h pr. høstning
            //updateXpath(orderXMLdoc, HeritrixTemplate.GROUP_MAX_ALL_KB_XPATH, "10000"); 
            //updateXpath(orderXMLdoc, HeritrixTemplate.GROUP_MAX_FETCH_SUCCESS_XPATH, "200");
            
            OutputStream os = new FileOutputStream(copiedTemplate);
            XMLWriter writer = new XMLWriter(os);
            writer.write(orderXMLdoc);
            // print start-command to STDOUT
        }   
    }
    
    public static void updateXpath(Document doc, String xpath, String newValue) throws DocumentException {
        Node node = doc.selectSingleNode(xpath);
        if (node != null) {
            node.setText(newValue);
        } else {
            throw new DocumentException("Xpath '" + xpath + "' not found");
        }
    }
}



