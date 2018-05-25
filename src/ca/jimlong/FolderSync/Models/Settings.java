package ca.jimlong.FolderSync.Models;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import com.google.gson.Gson;

public class Settings {
    private String srcFolder;
    private String destFolder;
    private List<String> validFiletypes;
    private String cacheFile;
	public Constants constants = new Constants();
    
    public class Constants {

    	public class FolderNames {
    		public String rootFolder = "";
    		public String srcFolder = "";
    		public String destFolder  = "";
    		public String skippedFiles = "";
    		public String uniqueFiles = "";
    		public String duplicateFiles = "";
    		public String comparisonResults = "";
    		public String notInThis  = "";
    		public String notInOther = "";
    		public String matched  = "";
    	}

    	public FolderNames folderNames = new FolderNames();
    }
    

	public String getSrcFolder() {
		return srcFolder;
	}

	public void setSrcFolder(String srcFolder) {
		this.srcFolder = srcFolder;
	}

	public String getDestFolder() {
		return destFolder;
	}

	public void setDestFolder(String destFolder) {
		this.destFolder = destFolder;
	}

	public List<String> getValidFiletypes() {
		return validFiletypes;
	}

	public String getCacheFile() {
		return cacheFile;
	}

	public Settings(File settingsFile) {
         
        if (!settingsFile.exists()) {
        	System.out.println("Missing " + settingsFile.getName()  + " file, exitting...");
            return;
        }
         
        String json;
		try {
			json = new String(Files.readAllBytes(settingsFile.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        Gson gson = new Gson();

		Settings settings = gson.fromJson(json, Settings.class);
		this.srcFolder = settings.srcFolder;
		this.destFolder = settings.destFolder;
		this.validFiletypes = settings.validFiletypes;
		this.cacheFile = settings.cacheFile;
		this.constants = settings.constants;
	}


}

