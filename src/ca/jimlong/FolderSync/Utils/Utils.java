package ca.jimlong.FolderSync.Utils;

import java.io.File;
import java.io.IOException;

public class Utils {
	public static boolean isWindows() {

	    String os = System.getProperty("os.name").toLowerCase();
	    // windows
	    return (os.indexOf("win") >= 0);

	}
	
	public static boolean isMac() {

	    String os = System.getProperty("os.name").toLowerCase();
	    // windows
	    return (os.indexOf("mac") >= 0);

	}
	
	public static String getOsName() {
		return System.getProperty("os.name").toLowerCase();
	}
	
	public static void openFolder(File file) {
		
		File workingDir = file.getParentFile();
		String filename = file.getName();

		String openCmd = isMac() ? "open" : "explorer.exe";
		String openOptions = isMac() ? "-R" : "/select";
		String[] cmd = new String[] {openCmd, openOptions, filename} ;
		try {
			Runtime.getRuntime().exec(cmd, null, workingDir);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	  
}
