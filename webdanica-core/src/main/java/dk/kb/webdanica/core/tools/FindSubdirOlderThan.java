package dk.kb.webdanica.core.tools;

import java.io.File;

import dk.netarkivet.common.utils.FileUtils;

/**
 * Tool for deleting subfolders under a given folder 
 * @author svc
 *
 */
public class FindSubdirOlderThan {
	private static String DELETE_PARAM = "--delete";
	public static void main(String[] args) {
		boolean deletefiles = false;
		int deletedFolders = 0;

		if (args.length < 1 || args.length > 3) {
			System.err.println("Wrong number of arguments. Only one or two arguments needed. You gave us " + args.length + " arguments");
			System.err.println("Correct usage: java FindSubdirOlderThan <dir> <maxdays> [--delete]");
			System.err.println("Exiting program");
			System.exit(1);
		}
		File dir = new File(args[0]);
		if (!dir.isDirectory()){
			System.err.println("The dir located '" + dir.getAbsolutePath() + "' does not exist or is not a proper directory");
			System.err.println("Exiting program");
			System.exit(1);
		}
		int maxageInDays = Integer.parseInt(args[1]);

		if (args.length > 2) {
			if (args[2].equalsIgnoreCase(DELETE_PARAM)) {
				deletefiles = true;
			} else {
				System.err.println("Ignoring unknown argument '" +  args[2] + "'");
			}
		}

		System.out.println("Looking for subdirs in folder '" + dir.getAbsolutePath() + "' older than " + maxageInDays + " days"); 
		File[] subfolders = dir.listFiles();
		for (File f: subfolders) {
			if (f.isDirectory() && ageInDays(f) > maxageInDays) {
				System.out.print("Subdir '" + f.getAbsolutePath() + "' is older than " +  maxageInDays + " days");
				if (deletefiles) {
					System.out.println(". Deleting directory now");
					FileUtils.removeRecursively(f);
					deletedFolders++;
				} else {
					System.out.println();
				}
			} 
		}
		if (deletefiles) {
			System.out.println("Deleted " + deletedFolders + " folders");
		}

	}

	public static int ageInDays(File f) {
		long ageInMillis = System.currentTimeMillis() - f.lastModified();
		long age = ageInMillis / 1000 / 60 / 60 / 24;
		return (int)age; 
	}
}
