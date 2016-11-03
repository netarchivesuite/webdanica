package dk.kb.webdanica.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class StringCompressor {
	    
	    public static byte[] compress(String text) {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        try {
	            OutputStream out = new DeflaterOutputStream(baos);
	            out.write(text.getBytes("UTF-8"));
	            out.close();
	        } catch (IOException e) {
	            throw new AssertionError(e);
	        }
	        return baos.toByteArray();
	    }

	    public static String decompress(byte[] bytes) throws IOException {
	        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
	    	//InputStream in = new GzipCompressorInputStream(new ByteArrayInputStream(bytes));
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        try {
	            byte[] buffer = new byte[8192];
	            int len;
	            while((len = in.read(buffer))>0)
	                baos.write(buffer, 0, len);
	            return new String(baos.toByteArray(), "UTF-8");
	        } catch (IOException e) {
	            throw new AssertionError(e);
	        }
	    }
	}

