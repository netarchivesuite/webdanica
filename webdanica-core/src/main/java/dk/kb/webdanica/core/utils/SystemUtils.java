package dk.kb.webdanica.core.utils;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

/**
 * Provide some general System Utilities.
 */
public class SystemUtils {

    /** Logging mechanism. */
    private static final Logger LOG = Logger.getLogger(SystemUtils.class.getName());

    /**
    * Provide the hostname of the machine on which the program is running.
    * @return the hostname as a {@link String}
    * Borrowed from Yggdrasil code base
    */
    public static String getHostName() throws UnknownHostException {
        String hostName;
        try {
            //Trying to get hostname through InetAddress
            InetAddress iAddress = InetAddress.getLocalHost();
            hostName = iAddress.getHostName();

            //Trying to do better and get Canonical hostname
            String canonicalHostName = iAddress.getCanonicalHostName();         
            hostName = canonicalHostName;

            if (StringUtils.isNotEmpty(hostName)) {
                LOG.info("Hostname provided  by iAddress: " + hostName);
                return hostName;
            }

        } catch (UnknownHostException  e) {
            // Failed the standard Java way, trying alternative ways.
        }

        // Trying to get hostname through environment properties.
        //      
        hostName = System.getenv("COMPUTERNAME");
        if (hostName != null) {
            LOG.info("Hostname provided by System.getenv COMPUTERNAME: " + hostName);
            return hostName;
        }
        hostName = System.getenv("HOSTNAME");
        if (hostName != null) {
            LOG.info("Hostname provided by System.getenv HOSTNAME: " + hostName);
            return hostName;
        }
        // Nothing worked, hostname undetermined.
        LOG.warning("Hostname undetermined");
        throw new UnknownHostException("Hostname undetermined");
    }

	/**
     * Arc-style date stamp in the format yyyyMMddHHmmssSSS and UTC time zone.
     */
    private static final ThreadLocal<SimpleDateFormat> 
    TIMESTAMP17 = threadLocalDateFormat("yyyyMMddHHmmssSSS");

    public static String get17DigitDate(Date date){
    	return TIMESTAMP17.get().format(date);
    }

    private static ThreadLocal<SimpleDateFormat> threadLocalDateFormat(final String pattern) {
    	ThreadLocal<SimpleDateFormat> tl = new ThreadLocal<SimpleDateFormat>() {
    		protected SimpleDateFormat initialValue() {
    			SimpleDateFormat df = new SimpleDateFormat(pattern);
    			df.setTimeZone(TimeZone.getTimeZone("UTC"));
    			return df;
    		}
    	};
    	return tl;
    }
    
    public static boolean isGzippedWarcfile(String warcfilename) {
	    return warcfilename.toLowerCase().endsWith(".gz"); 
    }

    public static void writeToPrintStream(PrintStream out, Throwable t) {
    	String message;
    	if (t != null) {
    		out.append(t.getClass().getName());
    		message = t.getMessage();
    		if (message != null) {
    			out.append(": ");
    			out.append(t.getMessage());
    		}
    		out.append("\n");
    		StringBuilder sb = new StringBuilder();
    		stacktrace_dump(t.getStackTrace(), sb);
    		out.append(sb.toString());
    		while ((t = t.getCause()) != null) {
    			out.append("caused by ");
    			out.append(t.getClass().getName());
    			message = t.getMessage();
    			if (message != null) {
    				out.append(": ");
    				out.append(t.getMessage());
    			}
    			out.append("\n");
    			sb = new StringBuilder();
    			stacktrace_dump(t.getStackTrace(), sb);
    			out.append(sb.toString());
    		}
    	}
    }
    
    public static void stacktrace_dump(StackTraceElement[] stackTraceElementArr, StringBuilder sb) {
    	StackTraceElement stackTraceElement;
    	String fileName;
    	if (stackTraceElementArr != null && stackTraceElementArr.length > 0) {
    		for (int i=0; i<stackTraceElementArr.length; ++i) {
    			stackTraceElement = stackTraceElementArr[i];
    			sb.append("\tat ");
    			sb.append(stackTraceElement.getClassName());
    			sb.append(".");
    			sb.append(stackTraceElement.getMethodName());
    			sb.append("(");
    			fileName = stackTraceElement.getFileName();
    			if (fileName != null) {
    				sb.append(fileName);
    				sb.append(":");
    				sb.append(stackTraceElement.getLineNumber());
    			} else {
    				sb.append("Unknown source");
    			}
    			sb.append(")");
    			sb.append("\n");
    		}
    	}
    }


}


