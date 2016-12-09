package dk.kb.webdanica.core.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import dk.kb.webdanica.core.Constants;


public class StreamUtils {
	
	/** Logging mechanism. */
    private static final Logger LOG = Logger.getLogger(StreamUtils.class.getName());
    
	public static void writeline(FileOutputStream ftest, String txt) throws FileNotFoundException, IOException {
		byte[] contentInBytes = txt.getBytes();
		ftest.write(contentInBytes);
		ftest.write("\n".getBytes());
		ftest.flush();
	}
	
	public static BufferedReader getBufferedReader(File ingestFile) throws IOException {
		BufferedReader br = null;
		if (isGzippedFile(ingestFile)) {
			br = new BufferedReader(new InputStreamReader(
			        new GZIPInputStream(new FileInputStream(ingestFile))));
		} else {
			 br = new BufferedReader(new FileReader(ingestFile)); 
		}
		
	    return br;
    }

	private static boolean isGzippedFile(File ingestFile) {
	    return ingestFile.getName().endsWith(".gz"); 
    }
	
	
	
	 /** Constant for UTF-8. */
    private static final String UTF8_CHARSET = "UTF-8";

    public static synchronized String getInputStreamAsString(InputStream in) {
    	StringBuilder res = new StringBuilder();
    	
    	if (in == null){
    		LOG.warning("NULL inputstream to method getInputStreamAsString");
    		return res.toString();
    	}
    	
    	byte[] buf = new byte[Constants.IO_BUFFER_SIZE];
    	int read = 0;
    	try {
    		try {
    			while ((read = in.read(buf)) != -1) {
    				LOG.info("read: " + read);
    				res.append(new String(buf, UTF8_CHARSET), 0, read);
    			}
    		} finally {
    			in.close();
    		}
    	} catch (Throwable e) {
    		String errMsg = "Trouble reading inputstream '" + in + "'";
    		LOG.log(Level.WARNING, errMsg, e);
    	}

    	return res.toString();
    }
	
	
}
