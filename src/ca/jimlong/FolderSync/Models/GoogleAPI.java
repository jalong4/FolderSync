package ca.jimlong.FolderSync.Models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import com.google.gson.Gson;

public class GoogleAPI {
    private String key;
    private String scheme;
    private String host;
    private String staticMapAPIRoute;


	public String getKey() {
		return key;
	}
	
	public String getScheme() {
		return scheme;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getBaseUrl() {
		return scheme + "://" + host;
	}
	
	public String getStaticMapAPIRoute() {
		return staticMapAPIRoute;
	}

	public GoogleAPI(File file) {
         
        if (!file.exists()) {
            System.out.println("Missing " + file.getName()  + " file, exitting...");
            return;
        }
         
        String json;
		try {
			json = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        Gson gson = new Gson();

        GoogleAPI googleAPI = gson.fromJson(json, GoogleAPI.class);
		this.key = googleAPI.key;
		this.scheme = googleAPI.scheme;
		this.host = googleAPI.host;
		this.staticMapAPIRoute = googleAPI.staticMapAPIRoute;
		
	}


}

