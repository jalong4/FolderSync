package ca.jimlong.FolderSync.Models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import com.google.gson.Gson;

public class Settings {
    private String srcFolder;
    private String destFolder;
    private String srcFolderFilter;
    private String destFolderFilter;
    private List<String> validFiletypes;
    private List<String> validVideoFiletypes;
    private List<String> validPhotoFiletypes;
    private List<String> systemFiles;
    private String cacheFile;
    private boolean limitDupsToSameParentFolder;
	public Constants constants = new Constants();
    
    public class Constants {

    	public class FolderNames {
    		public String rootFolder = "";
    		public String srcFolder = "";
    		public String destFolder  = "";
    		public String skippedFiles = "";
    		public String uniqueFiles = "";
    		public String duplicateFiles = "";
    		public String similarFiles = "";
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
	
	public String getSrcFolderFilter() {
		return srcFolderFilter;
	}
	
	public String getDestFolderFilter() {
		return destFolderFilter;
	}

	public List<String> getValidPhotoFiletypes() {
		return validPhotoFiletypes;
	}
	
	public List<String> getValidVideoFiletypes() {
		return validVideoFiletypes;
	}
	
	public List<String> getValidFiletypes() {	
		return validFiletypes;
	}
	
	public List<String> getSystemFiles() {
		return systemFiles;
	}

	public String getCacheFile() {
		return cacheFile;
	}

	public boolean isLimitDupsToSameParentFolder() {
		return limitDupsToSameParentFolder;
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
		this.srcFolderFilter = settings.srcFolderFilter;
		this.destFolderFilter = settings.destFolderFilter;
		this.validPhotoFiletypes = settings.validPhotoFiletypes;
		this.validVideoFiletypes = settings.validVideoFiletypes;
		
		if (validPhotoFiletypes != null) {
			this.validFiletypes = new ArrayList<>(validPhotoFiletypes);
			this.validFiletypes.addAll(validVideoFiletypes);
		} else {
			this.validFiletypes = new ArrayList<>(validVideoFiletypes);
		}
		
		this.systemFiles = settings.systemFiles;
		this.cacheFile = settings.cacheFile;
		this.limitDupsToSameParentFolder = settings.limitDupsToSameParentFolder;
		this.constants = settings.constants;
	}


}

