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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import com.drew.metadata.mp4.Mp4Directory;

import ca.jimlong.FolderSync.Utils.FileUtils;
import javafx.beans.property.SimpleStringProperty;
import us.fatehi.pointlocation6709.PointLocation;
import us.fatehi.pointlocation6709.parse.PointLocationParser;

public class FileProperties {

	public static Map<String, Long> formattedValues = new HashMap<String, Long>();
	public static final String datePattern = "MMM dd, yyyy 'at' hh:mm a";
	public static final int TAG_MOV_LOCATION                           = 0x050D;
	public static final int TAG_CREATION_DATE                          = 0x0506;

	private File file;
	private GeoLocation geoLocation;
	private int orientation;
    private Date rawDateCreated;
    private long rawSize;
	private SimpleStringProperty name;
	private SimpleStringProperty dateCreated;
	private SimpleStringProperty kind;
	private SimpleStringProperty size;
	private SimpleStringProperty checksum;
	private SimpleStringProperty location;
	
	
	public FileProperties(String basePath, File file, String checksum, boolean extractMetadata) {		
		// some filename's have an \r at the end eg. Icon files on Google photos
		
		String name = new File(basePath).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath().replaceAll("\\r","");

		Path p = Paths.get(file.getAbsolutePath());
		BasicFileAttributes attr = null;
		String dateCreated = "";
		this.orientation = 1;
		
		try {
			
			attr = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
		    dateCreated =  (attr == null) ? "" : getFormattedDate(attr.creationTime());
		    rawDateCreated =  (attr == null) ? new Date(0) : new Date(attr.creationTime().toMillis());
		    String fileType = FileUtils.getFileType(file);
			
			if (extractMetadata) {
				if (fileType.equals("mov")) {
					
					Date date = getVideoCreationDate(file);

					String videoDateCreated = (date == null) ? dateCreated : getFormattedLocalDate(date);

					if (!dateCreated.equals(videoDateCreated)) {
						dateCreated = videoDateCreated;
						rawDateCreated = date;
					}
					
					geoLocation = getVideoGeoLocation(file);

				} else {
					// Read Exif Data
					Metadata metadata = ImageMetadataReader.readMetadata(file);
					Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
					Date date = getCreationDateFromDirectory(directory, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
					
					String exifDateCreated = (date == null) ? dateCreated : getFormattedDate(date);

					if (!dateCreated.equals(exifDateCreated)) {
						dateCreated = exifDateCreated;
						rawDateCreated = date;
					}
					
					this.orientation = getOrientationFromDirectory(directory, ExifDirectoryBase.TAG_ORIENTATION);
					geoLocation = getGeoLocation(metadata, directory);
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
		this.rawSize = file.length();
		
		this.file = file;
		this.name = new SimpleStringProperty(name);
		this.dateCreated = new SimpleStringProperty(dateCreated);
		this.kind = new SimpleStringProperty(kind);
		this.size = new SimpleStringProperty(formatSize(this.rawSize));
		this.checksum = new SimpleStringProperty(checksum);
		this.location = (geoLocation != null) ? new SimpleStringProperty(getFormattedLocation(geoLocation)) : new SimpleStringProperty("");
		
	}
	
	private GeoLocation getGeoLocation(Metadata metadata, Directory directory) {
		if (directory != null) {
			GpsDirectory gpsDirectory = (GpsDirectory) metadata.getFirstDirectoryOfType(GpsDirectory.class);
			geoLocation = (gpsDirectory != null) ? gpsDirectory.getGeoLocation() : null;
		}
		return null;
	}

	private int getOrientationFromDirectory(Directory directory, int tagOrientation) {
		if (directory != null) {
			try {
				return directory.getInt(ExifDirectoryBase.TAG_ORIENTATION);
			} catch (MetadataException e) {
				return 0;
			}
		}
		return 0;
	}
	
	@SuppressWarnings("unused")
	private void printTag(Directory directory, Tag tag) {
		System.out.format("[%s] - %s = %s", directory.getName(), tag.getTagName(), tag.getDescription());
		System.out.println();
	}

	private GeoLocation getVideoGeoLocation(File file) {
		
		Metadata metadata;
		try {
			metadata = ImageMetadataReader.readMetadata(file);
			Directory directory = metadata.getFirstDirectoryOfType(QuickTimeMetadataDirectory.class);

			if (directory != null) {
				for (Tag tag : directory.getTags()) {
					if (tag.getTagType() == TAG_MOV_LOCATION) {
						return parseLocation(tag);
					}
				}												
			}
		} catch (Exception e) {
			return null;
		}
		
		return null;

	}

	private Date getCreationDateFromDirectory(Directory directory, int tagType) {
		return (directory != null) ? directory.getDate(tagType) : null;	
	}
	
	private Date getVideoCreationDate(File file) {
		
		// Try ImageMetadataReader first, and fallback to Mp4MetadataReader
		Metadata metadata;
		try {
			metadata = ImageMetadataReader.readMetadata(file);
			Directory directory = metadata.getFirstDirectoryOfType(QuickTimeMetadataDirectory.class);
			return getCreationDateFromDirectory(directory, TAG_CREATION_DATE);
		} catch (Exception e) {
			try {
				metadata = Mp4MetadataReader.readMetadata(file);
				Directory directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
				return getCreationDateFromDirectory(directory, Mp4Directory.TAG_CREATION_TIME);
				

			} catch (Exception e1) {
				return null;
			}
		}
	}

	private GeoLocation parseLocation(Tag geoTag) {
		try {
			PointLocation pointLocation = PointLocationParser.parsePointLocation(geoTag.getDescription());
			GeoLocation geoLocation = new GeoLocation(pointLocation.getLatitude().getDegrees(), pointLocation.getLongitude().getDegrees());
//			System.out.println("Location Found: " + geoLocation.toString());
			return geoLocation;
		} catch (Exception e) {
			return null;
		}
	}

	public FileProperties(String basePath, File file, String checksum) {
		// Default case
		this(basePath, file, checksum, true);
		
	}
	
	public FileProperties(String basePath, File file) {	
		// used for Skipped files
		this(basePath, file, "", false);
		
	}
	
	private String getFormattedLocation(GeoLocation geoLocation) {
    	return String.format("%.2f", geoLocation.getLatitude()) + "," + String.format("%.2f", geoLocation.getLongitude());
	}
	
	private String getFormattedDate(FileTime date) {
		SimpleDateFormat df = new SimpleDateFormat(datePattern);
		return df.format(date.toMillis());
	}
	
	
	private String getFormattedLocalDate(Date date) {
		LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.systemDefault());
		DateTimeFormatter df = DateTimeFormatter.ofPattern(datePattern);
		return df.format(localDate);
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
	public Date getRawDateCreated() {
		return rawDateCreated;
	}

	public long getRawSize() {
		return rawSize;
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

	public int getOrientation() {
		return orientation;
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
	
	@SuppressWarnings("deprecation")
	public int getCreatedMonth(){
		return rawDateCreated.getMonth() + 1;
	}
	
	@SuppressWarnings("deprecation")
	public int getCreatedYear(){
		return rawDateCreated.getYear() + 1900;
	}
	
	public int getRotation() {
		
		int rotation = 0;
		
	    switch (orientation) {
	    case 1: // "Top, left side (Horizontal / normal)"
	        break;
	    case 2: // "Top, right side (Mirror horizontal)"
	        break;
	    case 3: // "Bottom, right side (Rotate 180)"
	    	rotation = 180;
	        break;
	    case 4: // "Bottom, left side (Mirror vertical)"
	        break;
	    case 5: // "Left side, top (Mirror horizontal and rotate 270 CW)"
	    	rotation = 270;
	        break;
	    case 6: // "Right side, top (Rotate 90 CW)"
	    	rotation = 90;
	        break;
	    case 7: // "Right side, bottom (Mirror horizontal and rotate 90 CW)"
	    	rotation = 90;
	        break;
	    case 8: // "Left side, bottom (Rotate 270 CW)"
	    	rotation = 270;
	        break;
	    }
	    
	    return rotation;
		
	}
}

