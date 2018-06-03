package ca.jimlong.FolderSync.Models;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.jimlong.FolderSync.Utils.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrganizeFolder {
    private File folder;
    private List<String> validFiletypes;
    private List<String> validVideoFiletypes;
    private List<String> validPhotoFiletypes;
    private List<String> systemFiles;

    public ObservableList<FileProperties> skippedFiles;
    public ObservableList<FileProperties> moved;
    public DoubleProperty percentComplete;
    public DoubleProperty comparePercentComplete;
    private boolean completed;
	Map <String, Map<Integer, Map<Integer, List<FileProperties>>>> map;


    public OrganizeFolder(File folder, Settings settings) {
        this.folder = folder;
        this.validFiletypes = settings.getValidFiletypes();
        this.validVideoFiletypes = settings.getValidVideoFiletypes();
        this.validPhotoFiletypes = settings.getValidPhotoFiletypes();
        this.systemFiles = settings.getSystemFiles();
        this.skippedFiles = FXCollections.observableArrayList();
        this.moved = FXCollections.observableArrayList();
        this.percentComplete = new SimpleDoubleProperty(0.0);
        this.comparePercentComplete = new SimpleDoubleProperty(0.0);
        this.completed = false;
        this.map = new HashMap<>();

    }
    
	public File getFolder() {
		return folder;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	
    public void organizeFolder() {
    	
        completed = false;
        List<File> files = getAllFilesInFolder(folder, validFiletypes);
        if (files.size() > 0) {
        	System.out.println("Found " + String.valueOf(files.size()) + " files");
        	System.out.println("Skipping " + String.valueOf(skippedFiles.size()) + " files");
        	
            int total = files.size();
            double current = 0.0;

            for (final File file : files) {
            	String filename = file.getName().toLowerCase();
            	String filetype = FileUtils.getFileType(filename);

            	if (file.isDirectory()) {
            		System.out.println("Unexcepted file entry type of Directory " + file.getAbsolutePath());
            	} else if (!validFiletypes.contains(filetype)) {
            		System.out.println("Unexcepted filetype for file: " + file.getAbsolutePath().toLowerCase());
            	} else {


            		String mediaType = (validPhotoFiletypes.contains(filetype)) ? "Photos" : (validVideoFiletypes.contains(filetype)) ? "Videos" : "Unknown";
        			FileProperties fileProperties = new FileProperties(this.folder.getAbsolutePath(), file, "", true);  // true means extract metadata
        			Integer year = new Integer(fileProperties.getCreatedYear());
        			Integer month = new Integer(fileProperties.getCreatedMonth());
        			
            		if (map.containsKey(mediaType)) {
            			Map <Integer, Map<Integer, List<FileProperties>>> yearMap = map.get(mediaType);

            			if (yearMap.containsKey(year)) {
            				Map<Integer, List<FileProperties>> monthMap = yearMap.get(year);

            				if (monthMap.containsKey(month)) {
            					List<FileProperties> monthFiles = monthMap.get(month);
            					if (monthFiles.isEmpty()) {
            						monthFiles = new ArrayList<>();
            					}
            					monthFiles.add(fileProperties);
            				} else {
            					List<FileProperties> monthFiles = new ArrayList<>();
            					monthFiles.add(fileProperties);
            					monthMap.put(month, monthFiles);
            				}
            			} else {
            				Map<Integer, List<FileProperties>> monthMap = new HashMap<>();
            				List<FileProperties> monthFiles = new ArrayList<>();
            				monthFiles.add(fileProperties);
            				monthMap.put(month, monthFiles);
            				yearMap.put(year, monthMap);
            			}
            			
            		
            		} else {
            			Map <Integer, Map<Integer, List<FileProperties>>> yearMap = new HashMap<>();
            			Map<Integer, List<FileProperties>> monthMap = new HashMap<>();
            			List<FileProperties> monthFiles = new ArrayList<>();
        				monthFiles.add(fileProperties);
        				monthMap.put(month, monthFiles);
            			yearMap.put(year, monthMap);
            			map.put(mediaType, yearMap);            			
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
        }        
        percentComplete.set(1.0);
        completed = true;
        
        printMap();
        moveFiles();
        removeEmptyDirectories(this.folder);     
        
        System.out.println("Moved " + String.valueOf(moved.size()) + " of " + String.valueOf(files.size()));
        
    }
	
	
    private void printMap() {
    	for (String mediaType : map.keySet()) {
    		System.out.println();
    		System.out.println("Media: " + mediaType);
    		System.out.println();
    		Map <Integer, Map<Integer, List<FileProperties>>> yearMap = map.get(mediaType);
    		for (Integer year : yearMap.keySet()) {
    			Map<Integer, List<FileProperties>> monthMap = yearMap.get(year);
    			for (Integer month : monthMap.keySet()) {
    				List<FileProperties> monthFiles = monthMap.get(month);
    				for (FileProperties file : monthFiles) {
    					String yearPrefix = (map.keySet().size() == 1) ? "" : year.toString() + "/";
    					String filename = Paths.get(file.getName()).getFileName().toString();
    					System.out.println("Moving " + file.getName() + " to " + yearPrefix + String.format("%02d", month.intValue()) + '/' + filename);
    				}
    			}
    		}
    	}
	}
    
    private void moveFiles() {
    	for (String mediaType : map.keySet()) {
    		System.out.println();
    		System.out.println("Media: " + mediaType);
    		System.out.println();
    		Map <Integer, Map<Integer, List<FileProperties>>> yearMap = map.get(mediaType);
    		for (Integer year : yearMap.keySet()) {
    			Map<Integer, List<FileProperties>> monthMap = yearMap.get(year);
    			for (Integer month : monthMap.keySet()) {
    				List<FileProperties> monthFiles = monthMap.get(month);
    				for (FileProperties file : monthFiles) {
    					Path fromPath = file.getFile().toPath();
    					String mediaPrefix = (map.keySet().size() == 1) ? "" : mediaType + "/";
    					String yearPrefix = (yearMap.keySet().size() == 1) ? "" : year.toString() + "/";
    					String filename = Paths.get(file.getName()).getFileName().toString();
    					Path toPath = Paths.get(folder.getAbsolutePath(), mediaPrefix, yearPrefix + String.format("%02d", month.intValue()) + '/' + filename);
    					System.out.println("Moving " + file.getName() + " to " + toPath.toString());
    					if (FileUtils.moveFile(fromPath, toPath)) {
    						moved.add(file);
    					}
    				}
    			}
    		}
    	}
		
	}
    
    private void removeEmptyDirectories(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                System.out.println("Folder:" + fileEntry.getName());
                removeEmptyDirectories(fileEntry);
                if (fileEntry.listFiles().length == 0) {
                	if (FileUtils.deleteFile(fileEntry)) {
                		System.out.println("Removed empty directory " + fileEntry.getAbsolutePath());
                	}
                } else {
                	System.out.println("Directory " + fileEntry.getAbsolutePath() + " is not empty.");
                }
            } else {
                 if (systemFiles.contains(fileEntry.getName())) {
                 	if (FileUtils.deleteFile(fileEntry)) {
                		System.out.println("Removed system file: " + fileEntry.getAbsolutePath());
                	} 
                 }           	
            }
        }	
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
}