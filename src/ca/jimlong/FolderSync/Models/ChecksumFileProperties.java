package ca.jimlong.FolderSync.Models;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.GpsDirectory;

import ca.jimlong.FolderSync.Utils.FileUtils;
import javafx.beans.property.SimpleStringProperty;

public class ChecksumFileProperties {

	public static Map<String, Long> formattedValues = new HashMap<String, Long>();
	public static final String datePattern = "MMM dd, yyyy 'at' hh:mm a";
	private File file;
	private GeoLocation geoLocation;
	private SimpleStringProperty name;
	private SimpleStringProperty dateCreated;
	private SimpleStringProperty kind;
	private SimpleStringProperty size;
	private SimpleStringProperty checksum;
	private SimpleStringProperty location;
	
	public ChecksumFileProperties(String basePath, File file, String checksum) {		
		// some filename's have an \r at the end eg. Icon files on Google photos
		
		String name = new File(basePath).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath().replaceAll("\\r","");

		Path p = Paths.get(file.getAbsolutePath());
		BasicFileAttributes attr = null;
		String dateCreated = "";
		
		try {
			
			attr = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
		    dateCreated =  (attr == null) ? "" : getFormattedDate(attr.creationTime());
		    
			
		    // Use date created from EXIF metadata if it exists
			if (checksum.length() > 0) {
				Metadata metadata = ImageMetadataReader.readMetadata(file);

				// Read Exif Data
				Directory directory = metadata.getFirstDirectoryOfType( ExifDirectoryBase.class );
				if (directory != null) {
					Date date = directory.getDate( ExifDirectoryBase.TAG_DATETIME );
					String exifDateCreated = (date == null) ? dateCreated : getFormattedDate(date);

					if (!dateCreated.equals(exifDateCreated)) {
						dateCreated = exifDateCreated;
					}
				}

				// Read GPS Data
				GpsDirectory gpsDirectory = (GpsDirectory) metadata.getFirstDirectoryOfType(GpsDirectory.class);
				if (gpsDirectory != null) {
					geoLocation = gpsDirectory.getGeoLocation();
				}
			}


		} catch (ImageProcessingException e1) {
			System.out.println("Warning: an ImageProcessingException occurred while trying to get metadata for file: " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Warning: an IOException occurred while trying to get metadata for file: " + file.getAbsolutePath());
		} catch (NullPointerException e) {
			System.out.println("Warning: a NullPointerException occurred while trying to get metadata for file: " + file.getAbsolutePath());
		}
	    
		String kind = FileUtils.getFileType(name).toUpperCase();
		long size = file.length();
		
		this.file = file;
		this.name = new SimpleStringProperty(name);
		this.dateCreated = new SimpleStringProperty(dateCreated);
		this.kind = new SimpleStringProperty(kind);
		this.size = new SimpleStringProperty(formatSize(size));
		this.checksum = new SimpleStringProperty(checksum);
		this.location = (geoLocation != null) ? new SimpleStringProperty(getFormattedLocation(geoLocation)) : new SimpleStringProperty("");
		
	}
	
	private String getFormattedLocation(GeoLocation geoLocation) {
    	return String.format("%.2f", geoLocation.getLatitude()) + "," + String.format("%.2f", geoLocation.getLongitude());
	}
	
	private String getFormattedDate(FileTime date) {
		SimpleDateFormat df = new SimpleDateFormat(datePattern);
		return df.format(date.toMillis());
	}
	
	private String getFormattedDate(Date date) {
		
		LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
		DateTimeFormatter df = DateTimeFormatter.ofPattern(datePattern);
		return df.format(localDate);
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
	
	public String getLocation() {
		return location.get();
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	private String formatSize(long size){
		String returnValue;
		
		if (size <= 0) {
			returnValue = "0";
		}
		else if (size < 1024) {
			returnValue = size + " B";
		} else if (size < 1048576) {
			returnValue = size / 1024 + " kB";
		} else {
			double sizeDouble= new Long(size).doubleValue();
			returnValue = String.format("%.1f", sizeDouble / 1048576.0) + " MB";
		}
		
		formattedValues.put(returnValue, size);
		return returnValue;
		
	}
	
}

