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

	public Settings(File settingsFile) {
         
        if (!settingsFile.exists()) {
            System.out.println("Missing Settings.json file, exitting...");
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
	}


}

