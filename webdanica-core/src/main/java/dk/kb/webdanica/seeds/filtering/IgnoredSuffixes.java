package dk.kb.webdanica.seeds.filtering;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.utils.Settings;

public class IgnoredSuffixes {
	/*
	private static String[] ignoredExts = new String[]{
             ".jpg", ".avi", ".waw", ".gif", ".ico",
             ".bmp", ".doc", ".docx", ".dot", 
             ".eps", ".exe", ".jp2", ".jpe",".jpeg",
             ".mdb", ".mov", ".mp3", ".mp4", ".mpeg",
             ".odt",
             ".pdd",
             ".pdf",
             ".pict", 
             ".png", ".psd", ".rar", ".raw", 
             ".rtf", ".swf", 
             ".tif",
             ".tiff",
             ".wps", ".xls" };
	*/
	private static String[] ignoredExts = Settings.getAll(WebdanicaSettings.IGNORED_SUFFIXES);
	
	public static String matchesIgnoredExtension(String name) {
		String low = name.toLowerCase();
		for (String ign: ignoredExts) {
			if (low.endsWith(ign)) {
				return ign;
			}
		}
		return null;
	}
	
}
