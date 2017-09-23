package dk.kb.webdanica.core.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;

/**
 * Tool to examine the files in a folder using the tika AutoDetectParser. 
 * TODO Not really relevant for this project. Should be moved into another project 
 */
public class ShowContentType {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Need folder as argument");
			System.exit(1);
		}
		File toExamine = new File(args[0]);
		if (!toExamine.exists()) {
			System.err.println("Need existing file or folder as argument, but file '" + toExamine.getAbsolutePath() + "' does not exist");
			System.exit(1);
		}

		List<String> contents = getContentType(toExamine, true);
		
		Map<String, Long> contentCount = new HashMap<String, Long>();
		for (String contentType: contents) {
			Long longValue = contentCount.get(contentType);
			if (longValue == null) {
				longValue = new Long(0L);
			} 
			longValue = longValue + 1L;
			contentCount.put(contentType, longValue);
		}
		System.out.println("Files found: " + contents.size());
		for (String key: contentCount.keySet()) {
			System.out.println(key + ": " + contentCount.get(key));
		}
		/*
		File toExamine = new File("/home/svc/Downloads/CVR-print.pdf");
		
		System.out.println(getMediaTypeUsingTika(toExamine));
		System.out.println(getMediaTypeUsingJavaFiles(toExamine));
		*/
	}
	
	/**
	 * @param toExamine The file to examine
	 * @return the MediaType detected by Tika
	 */
	public static String getMediaTypeUsingTika(File toExamine) {
		String theFileName = toExamine.getName();
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(toExamine));
			AutoDetectParser parser = new AutoDetectParser();
			Detector detector = parser.getDetector();
			Metadata md = new Metadata();
			md.add(Metadata.RESOURCE_NAME_KEY, theFileName);
			return detector.detect(bis, md).toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(bis);
		}
		return null;
	}
	
	/**
     * @param f The file to examine
     * @return the MediaType detected by Files.probeContentType
     */
	public static String getMediaTypeUsingJavaFiles(File f) {
		Path p = Paths.get(f.getAbsolutePath());
		String contenttype = null;
        try {
	        contenttype = Files.probeContentType(p);
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return contenttype;
	}
	
	/**
	 * 
	 * @param f
	 * @param useTika
	 * @return
	 */
	public static List<String> getContentType(File f, boolean useTika) {
		List<String>resultList = new ArrayList<String>(); 
		if (f.isFile()) { // should only happen if the original file argument is a normal file
			String contentType = "";
			if (useTika){
				contentType = getMediaTypeUsingTika(f);
			} else {
				contentType = getMediaTypeUsingJavaFiles(f);
			}
			resultList.add(contentType);
			return resultList;
		} else {
			System.out.println("Looking at data in foldler " +  f.getAbsolutePath()) ;
			File[] subfiles = f.listFiles();
			for (File file: subfiles) {
				if (!file.isDirectory()) {
					String contentType = "";
					if (useTika){
						contentType = getMediaTypeUsingTika(file);
					} else {
						contentType = getMediaTypeUsingJavaFiles(file);
					}
					resultList.add(contentType);
				} else {
					resultList.addAll(getContentType(file, useTika));
				}
			}
		}
		return resultList;
	}
}
