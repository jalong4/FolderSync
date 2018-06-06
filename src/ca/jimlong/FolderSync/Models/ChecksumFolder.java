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
    private SimilarFiles _similarFiles;
    private ChecksumCache cache;
    private List<String> validFiletypes;
    public TreeMap<String, FileProperties> map;

    public ObservableList<FileProperties> duplicateFiles;
    public ObservableList<FileProperties> similarFiles;
    public ObservableList<FileProperties> skippedFiles;
    public DoubleProperty percentComplete;
    public DoubleProperty comparePercentComplete;
    private boolean checksumCompleted;
    


    public ChecksumFolder(File folder, Settings settings, ChecksumCache cache) {
        this.folder = folder;
        this.validFiletypes = settings.getValidFiletypes();
        this.duplicateFiles = FXCollections.observableArrayList();
        this.similarFiles = FXCollections.observableArrayList();
        this.skippedFiles = FXCollections.observableArrayList();
        this.percentComplete = new SimpleDoubleProperty(0.0);
        this.comparePercentComplete = new SimpleDoubleProperty(0.0);
        this.map =  new TreeMap<String, FileProperties>();
        this.checksumCompleted = false;
        this.cache = cache;
        this._similarFiles = new SimilarFiles(folder, settings);
    }
    
	public File getFolder() {
		return folder;
	}



	private byte[] createChecksum(String filename) throws Exception {
		
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];

        MessageDigest complete = MessageDigest.getInstance("MD5");

        int numRead = 0;
 
        do {
			try {
				numRead = fis.read(buffer);
			} catch (Exception e) {
				System.out.println("error reading " + filename);
				e.printStackTrace();
			}
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
        	_similarFiles.processFolder(files);
        	this.similarFiles = _similarFiles.getSimilar();
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
                    skippedFiles.add(new FileProperties(this.folder.getAbsolutePath(), fileEntry));
                    continue;
                }
                files.add(fileEntry);
            }
        }

        return files;
    }

    private TreeMap<String, FileProperties> generateChecksumMapForFolder(List<File> files, List<String> validFiletypes) {

        TreeMap<String, FileProperties> map = new TreeMap<>();

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
                if (map.containsKey(checksum)) {
                	FileProperties originalChecksumFile = map.get(checksum);
                	File originalFile = originalChecksumFile.getFile();
                	
                    System.out.println("Filename: " + path + " is a Duplicate of: " + map.get(checksum).getName() + " Checksum: "
                            + checksum);
                    
                    if (originalFile.getParent().equals(fileEntry.getParent())) {
                    	String originalBaseName = FileUtils.getBaseName(originalFile.getName());
                    	String baseName = FileUtils.getBaseName(fileEntry.getName());
                    	if (originalBaseName.startsWith(baseName)) {
                    		duplicateFiles.add(originalChecksumFile);
                    		map.remove(checksum);
                    		map.put(checksum, new FileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));
                    	} else {
                    		duplicateFiles.add(new FileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));  
                    	}
                    } else {
                        duplicateFiles.add(new FileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));                    	
                    }
                } else {
                    map.put(checksum, new FileProperties(this.folder.getAbsolutePath(), fileEntry, checksum));
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
    
    public ObservableList<FileProperties> getObservableListOfMapValues() {
    	ObservableList<FileProperties> values = FXCollections.observableArrayList();
    	
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

    private ObservableList<FileProperties> createObservableListOfFileProperties(TreeMap<String, FileProperties> map, List<String> list) {
    	ObservableList<FileProperties> results = FXCollections.observableArrayList();
    	
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
        
        compareTwoFolders.notInThis = createObservableListOfFileProperties(other.map, notInThis);
        compareTwoFolders.notInOther = createObservableListOfFileProperties(map, notInOther);
        compareTwoFolders.matched = createObservableListOfFileProperties(map, matched);

        updateProgressBar(comparePercentComplete, 1.0);
        
        return compareTwoFolders;

    }

	public boolean isChecksumCompleted() {
		return checksumCompleted;
	}
    
} 

