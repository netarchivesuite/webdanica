package dk.kb.webdanica.seeds.filtering;

/** TODO: Read the list of ignored suffixes from settings
 * instead of hardwired.  */
public class IgnoredSuffixes {

	 private static String[] ignoredExts = new String[]{
             ".jpg", ".avi", ".waw", ".gif",
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
	 
	public static boolean hasIgnoredExtension(String name) {
		String low = name.toLowerCase();
		for (String ign: ignoredExts) {
			if (low.endsWith(ign)) {
				return true;
			}
		}
		return false;
	}
	
}
