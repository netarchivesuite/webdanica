package dk.kb.webdanica.datamodel;

import java.io.File;

public class WgetSettings {
	
	final File defaultWgetPath = new File("/usr/bin/wget");
	final int defaultTries = 1;
	final File defaultTmpFolder = new File("/tmp");
	final int defaultDelayInSecs = 0;
	
	public WgetSettings() {
	}
	
	public File getPath() {
		return defaultWgetPath;
	}
	
	public File getTmpFolder() {
		return defaultTmpFolder;
	}
	
	public int getTries() {
		return defaultTries;
	}
	
	public int getDelayInSecs() {
		return defaultDelayInSecs;
	}
	
	
}
