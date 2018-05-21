package ca.jimlong.FolderSync.Models;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import ca.jimlong.FolderSync.Utils.FileUtils;
import javafx.beans.property.SimpleStringProperty;

public class ChecksumFileProperties {

	public static Map<String, Long> formattedValues = new HashMap<String, Long>();
	private File file;
	private SimpleStringProperty name;
	private SimpleStringProperty dateCreated;
	private SimpleStringProperty kind;
	private SimpleStringProperty size;
	private SimpleStringProperty checksum;
	
	public ChecksumFileProperties(String basePath, File file, String checksum) {

		String name = new File(basePath).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
		
		Path p = Paths.get(file.getAbsolutePath());
		BasicFileAttributes attr = null;
		
	    try {
			attr = Files.getFileAttributeView(p, BasicFileAttributeView.class)
			          .readAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    String dateCreated =  (attr == null) ? "" : attr.creationTime().toString();
		String kind = FileUtils.getFileType(name);
		long size = file.length();
		
		this.file = file;
		this.name = new SimpleStringProperty(name);
		this.dateCreated = new SimpleStringProperty(dateCreated);
		this.kind = new SimpleStringProperty(kind);
		this.size = new SimpleStringProperty(formatSize(size));
		this.checksum = new SimpleStringProperty(checksum);
		
	}
	
	public File getFile(){
		return file;
	}
	
	public String getName(){
		return name.get();
	}
	public String getDateCreated(){
		return dateCreated.get();
	}
	public String getKind(){
		return kind.get();
	}
	public String getSize(){
		return size.get();
	}
	public String getChecksum(){
		return checksum.get();
	}
	
	private String formatSize(long size){
		String returnValue;
		if(size<= 0){
			returnValue =  "0";}
		
		else if(size<1024){
			returnValue = size + " B";
		}
		else if(size < 1048576){
			returnValue = size/1024 + " kB";
		}else{
			returnValue = size/1048576 + " MB";
		}
		formattedValues.put(returnValue, size);
		return returnValue;
		
	}
	
}

