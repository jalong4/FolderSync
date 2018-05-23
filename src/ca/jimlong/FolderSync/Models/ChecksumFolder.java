package ca.jimlong.FolderSync.Models;
import ca.jimlong.FolderSync.Utils.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class ChecksumFolder {

    private File folder;
    private ChecksumCache cache;
    private List<String> validFiletypes;
    public TreeMap<String, ChecksumFileProperties> map;

    public ObservableList<ChecksumFileProperties> duplicateFiles;
    public ObservableList<ChecksumFileProperties> skippedFiles;
    public DoubleProperty percentComplete;
    public DoubleProperty comparePercentComplete;
    private boolean checksumCompleted;


    public ChecksumFolder(File folder, List<String> validFiletypes, ChecksumCache cache) {
        this.folder = folder;
        this.validFiletypes = validFiletypes;
        this.duplicateFiles = FXCollections.observableArrayList();
        this.skippedFiles = FXCollections.observableArrayList();
        this.percentComplete = new SimpleDoubleProperty(0.0);
        this.comparePercentComplete = new SimpleDoubleProperty(0.0);
        this.map =  new TreeMap<String, ChecksumFileProperties>();
        this.checksumCompleted = false;
        this.cache = cache;
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
    	
		
		if (cache.getMap().containsKey(filename)){
//			System.out.println("Checksum for: " + filename + "found in cache");
			return cache.getMap().get(filename);
		}
		

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
                Thread.sleep(1); 
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
    
    private Set<String> symmetricDifference(Set<String> a, Set<String> b) {
        Set<String> result = new HashSet<>(a);
        for (String element : b) {
            if (result.contains(element)) {
                result.remove(element);
            }
        }
        return result;
    }
    
    private void updateProgressBar(DoubleProperty pb, final double percent) {
        Platform.runLater(() -> pb.set(percent));
        try {
            Thread.sleep(100); 
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private ObservableList<ChecksumFileProperties> createObservableListOfChecksumFileProperties(TreeMap<String, ChecksumFileProperties> map, List<String> list) {
    	ObservableList<ChecksumFileProperties> results = FXCollections.observableArrayList();
    	
    	for (String key : list) {
    		results.add(map.get(key));
        }
    	
    	return results;
    }
    
    public CompareTwoFolders compare(ChecksumFolder other) {

        CompareTwoFolders compareTwoFolders = new CompareTwoFolders(this.folder, other.folder);
        System.out.println("Comparing checksum Mappings");

        if (map == null || map.isEmpty()) {
            System.out.println( "Source Map is empty, exitting...");
            return compareTwoFolders;
        }

        Set<String> src = new HashSet<>(map.keySet());
        Set<String> dest = new HashSet<String>(other.map.keySet());
        updateProgressBar(comparePercentComplete, 0.3);
        
        List<String> notInThis = new ArrayList<>(symmetricDifference(dest, src));
        updateProgressBar(comparePercentComplete, 0.6);
        
        List<String> notInOther = new ArrayList<>(symmetricDifference(src, dest));
        
        List<String> matched = new ArrayList<>(src);
        matched.retainAll(dest);
        updateProgressBar(comparePercentComplete, 0.9);
        
        compareTwoFolders.notInThis = createObservableListOfChecksumFileProperties(other.map, notInThis);
        compareTwoFolders.notInOther = createObservableListOfChecksumFileProperties(map, notInOther);
        compareTwoFolders.matched = createObservableListOfChecksumFileProperties(map, matched);

        updateProgressBar(comparePercentComplete, 1.0);
        
        return compareTwoFolders;

    }

	public boolean isChecksumCompleted() {
		return checksumCompleted;
	}

	public void setChecksumCompleted(boolean checksumCompleted) {
		this.checksumCompleted = checksumCompleted;
	}
    
} 

