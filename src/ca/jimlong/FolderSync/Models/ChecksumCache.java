package ca.jimlong.FolderSync.Models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChecksumCache {
	private File file;
	private Cache cache;
	
	private class Cache {
		private Map<String, String> map;
		
		private Cache() {
			map = new HashMap<>();
		}
	}
	

	public Map<String, String> getMap() {
		return cache.map;
	}
 
	public ChecksumCache(String filename) {
         
		this.file = new File(filename);
		
        if (!file.exists()) {
        	this.cache = new Cache();
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

        cache = gson.fromJson(json, Cache.class);
		
		if (cache == null || cache.map == null) {
			cache = new Cache();
		}
	}
	
	public void update(ChecksumFolder folder) {
    	
		for (String checksum : folder.map.keySet()) {
			String filename = folder.map.get(checksum).getFile().toPath().toString();
			if (!cache.map.containsKey(filename)) {
				cache.map.put(filename, checksum);
			}
    	}
		
	}
	
	public void flush() {
		cache.map = new HashMap<>();
	}
	
	public void rewrite() {
		
		Path path = file.toPath();
		
		try {
			Files.deleteIfExists(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try (Writer writer = new FileWriter(path.toString())) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(cache, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
