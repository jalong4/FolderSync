package ca.jimlong.FolderSync.Models;
import ca.jimlong.FolderSync.Utils.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;



public class ChecksumFolder {

    private File folder;
    private List<String> validFiletypes;
    public TreeMap<String, ChecksumFileProperties> map;

    public ObservableList<ChecksumFileProperties> duplicateFiles;
    public ObservableList<ChecksumFileProperties> skippedFiles;
    public DoubleProperty percentComplete;
    public DoubleProperty comparePercentComplete;
    private boolean checksumCompleted;


    public ChecksumFolder(File folder, List<String> validFiletypes) {
        this.folder = folder;
        this.validFiletypes = validFiletypes;
        this.duplicateFiles = FXCollections.observableArrayList();
        this.skippedFiles = FXCollections.observableArrayList();
        this.percentComplete = new SimpleDoubleProperty(0.0);
        this.comparePercentComplete = new SimpleDoubleProperty(0.0);
        this.map =  new TreeMap<String, ChecksumFileProperties>();
        this.checksumCompleted = false;

    }


    private byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];

        MessageDigest complete = MessageDigest.getInstance("MD5");

        int numRead;
 
        do {
			numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
 
		fis.close();

        return complete.digest();
    }
 
    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    private  String getMD5Checksum(String filename) {

        byte[] b;
		try {
			b = createChecksum(filename);
		} catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        String result = "";
 
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public void generateChecksumMapForFolder() {
        checksumCompleted = false;
        List<File> files = getAllFilesInFolder(folder, validFiletypes);
        if (files.size() > 0) {
        	map = generateChecksumMapForFolder(files, validFiletypes);
        } else {
        	percentComplete.set(1.0);
        }
        checksumCompleted = true;
    }

    private List<File> getAllFilesInFolder(File folder, List<String> validFiletypes) {

        List<File> files = new ArrayList<File>();

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                System.out.println("Folder:" + fileEntry.getName());
                List<File> subFolderFiles = getAllFilesInFolder(fileEntry, validFiletypes);
                files.addAll(subFolderFiles);
            } else {
                String filename = fileEntry.getName().toLowerCase();
                String filetype = FileUtils.getFileType(filename);

                if (!validFiletypes.contains(filetype)) {
                    System.out.println("Skipping: " + filename);
                    skippedFiles.add(new ChecksumFileProperties(this.folder.getAbsolutePath(), fileEntry, ""));
                    continue;
                }
                files.add(fileEntry);
            }
        }

        return files;
    }

    private TreeMap<String, ChecksumFileProperties> generateChecksumMapForFolder(List<File> files, List<String> validFiletypes) {

        TreeMap<String, ChecksumFileProperties> map = new TreeMap<>();

        int total = files.size();
        double current = 0.0;

        for (final File fileEntry : files) {
            String filename = fileEntry.getName().toLowerCase();
            String filetype = FileUtils.getFileType(filename);

            if (fileEntry.isDirectory()) {
                System.out.println("Unexcepted file entry type of Directory " + fileEntry.getAbsoluteFile());
            } else if (!validFiletypes.contains(filetype)) {
                System.out.println("Unexcepted filetype for file: " + fileEntry.getAbsolutePath().toLowerCase());
            } else {

                String path = fileEntry.getAbsolutePath();
                String checksum = getMD5Checksum(path);
                // System.out.println("Filename: " + filename + " Checksum: " + checksum);
                if (map.containsKey(checksum)) {
                	ChecksumFileProperties originalChecksumFile = map.get(checksum);
                	File originalFile = originalChecksumFile.getFile();
                	System.out.println("orig getName " + originalFile.getName());
                	System.out.println("orig getParent " + originalFile.getParent());
                	System.out.println("orig getPath " + originalFile.getPath());
                	
                    System.out.println("Filename: " + path + " is a Duplicate of: " + map.get(checksum) + " Checksum: "
                            + checksum);
                    
                    if (originalFile.getParent().equals(fileEntry.getParent())) {
                    	String originalBaseName = FileUtils.getBaseName(originalFile.getName());
                    	String baseName = FileUtils.getBaseName(fileEntry.getName());
                    	if (originalBaseName.startsWith(baseName)) {
                    		duplicateFiles.add(originalChecksumFile);
                    		map.remove(checksum);
                    		map.put(checksum, new ChecksumFileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));
                    	} else {
                    		duplicateFiles.add(new ChecksumFileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));  
                    	}
                    } else {
                        duplicateFiles.add(new ChecksumFileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));                    	
                    }
                } else {
                    map.put(checksum, new ChecksumFileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));
                }
            }

            current++;
            final double percent = new Double(current / total).doubleValue();
            Platform.runLater(() -> percentComplete.set(percent));
            try {
                Thread.sleep(100); 
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
        return map;
    }
    
    public ObservableList<ChecksumFileProperties> getObservableListOfMapValues() {
    	ObservableList<ChecksumFileProperties> values = FXCollections.observableArrayList();
    	
    	for (String key : map.keySet()) {
    		values.add(map.get(key));
    	}
    	return values;
    	
    }

    public CompareTwoFolders compare(ChecksumFolder other) {

        CompareTwoFolders compareTwoFolders = new CompareTwoFolders(this.folder, other.folder);
        System.out.println("Comparing checksum Mappings");

        if (map == null || map.isEmpty()) {
            System.out.println( "Source Map is empty, exitting...");
            return compareTwoFolders;
        }

        Iterator<String> src = map.keySet().iterator();
        Iterator<String> dest = other.map.keySet().iterator();

        String srcKey = src.next();


        int total = this.map.size() + other.map.size();
        double current = 2.0;  // got the first two already

        if (other.map.size() > 0) {
            String destKey = dest.next();
	  
	        while (src.hasNext() && (dest != null) && dest.hasNext()) {
	            // System.out.println( "Src file: " + map.get(srcKey) + ", Dest file: " + other.map.get(destKey));
	            int compareTo = srcKey.compareTo(destKey);
	
	            if (compareTo == 0) {
	                srcKey = src.next();
	                destKey = dest.next(); 
	                current += 2;
	
	            } else if (compareTo < 0) {  // src < dest
	                // System.out.println("Not in Dest -> Filename: " + map.get(srcKey) + " Checksum: " + srcKey);
	                compareTwoFolders.notInOther.add(map.get(srcKey));
	                srcKey = src.next();
	                current++;
	            } else {
	                // System.out.println("Not in Src -> Filename: " + other.map.get(destKey) + " Checksum: " + destKey);
	                compareTwoFolders.notInThis.add(other.map.get(destKey));
	                destKey = dest.next(); 
	                current++;
	            }
	
	            final double percent = new Double(current / total).doubleValue();
	            Platform.runLater(() -> comparePercentComplete.set(percent));
	            try {
	                Thread.sleep(100); 
	            } catch(InterruptedException ex) {
	                Thread.currentThread().interrupt();
	            }
	        }
	        
	        
	        while (dest.hasNext()) {
	            // System.out.println("Not in Src -> Filename: " + other.map.get(destKey) + " Checksum: " + destKey);
	            compareTwoFolders.notInThis.add(other.map.get(destKey));
	            destKey = dest.next();
	            current++;

	            final double percent = new Double(current / total).doubleValue();
	            Platform.runLater(() -> comparePercentComplete.set(percent));
	            try {
	                Thread.sleep(100); 
	            } catch(InterruptedException ex) {
	                Thread.currentThread().interrupt();
	            }
	        }
	        
        }

        while (src.hasNext()) {
            // System.out.println("Not in Dest -> Filename: " + map.get(srcKey) + " Checksum: " + srcKey);
            compareTwoFolders.notInOther.add(map.get(srcKey));
            srcKey = src.next();
            current++;

            final double percent = new Double(current / total).doubleValue();
            Platform.runLater(() -> comparePercentComplete.set(percent));
            try {
                Thread.sleep(100); 
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        return compareTwoFolders;

    }

	public boolean isChecksumCompleted() {
		return checksumCompleted;
	}

	public void setChecksumCompleted(boolean checksumCompleted) {
		this.checksumCompleted = checksumCompleted;
	}
    
} 

