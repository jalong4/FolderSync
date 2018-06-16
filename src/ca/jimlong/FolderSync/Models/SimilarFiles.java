package ca.jimlong.FolderSync.Models;

import java.io.File;
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

public class SimilarFiles {

    private File folder;
    private List<String> systemFiles;
    private Map<FileProperties, List<FileProperties>> similarMap;

    public ObservableList<FileProperties> skippedFiles;
    public ObservableList<FileProperties> similar;
    public DoubleProperty percentComplete;
    public DoubleProperty comparePercentComplete;
    private boolean completed;
	
    public SimilarFiles(File folder, Settings settings) {
        this.folder = folder;
        this.systemFiles = settings.getSystemFiles();
        this.skippedFiles = FXCollections.observableArrayList();
        this.similar = FXCollections.observableArrayList();
        this.percentComplete = new SimpleDoubleProperty(0.0);
        this.comparePercentComplete = new SimpleDoubleProperty(0.0);
        this.completed = false;
        this.similarMap = new HashMap<>();

    }
    
	public File getFolder() {
		return folder;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public ObservableList<FileProperties> getSimilar() {
        this.similar = FXCollections.observableArrayList();
		for (FileProperties fp : similarMap.keySet()) {
			for (FileProperties sfp : similarMap.get(fp)) {
				similar.add(sfp);
			}
		}
		return similar;
	}
	
	
	public void processFolder(List<File> files) {
		completed = false;
		
		files.sort((f1, f2) -> {
			String name = FileUtils.getFileNameWithoutType(f1.getName());
			String otherName = FileUtils.getFileNameWithoutType(f2.getName());
			return name.compareTo(otherName);
		});
		
		if (files.size() == 0) {
			completed = true;
			System.out.println("No files found.");
			return;
		}

		System.out.println("Found " + String.valueOf(files.size()) + " files");

		int total = files.size();
		double current = 0.0;

		for (final File file : files) {
			String filename = file.getName();
			String filetype = FileUtils.getFileType(filename);
			String filenameWithoutType = FileUtils.getFileNameWithoutType(filename);

			FileProperties fileProperties = new FileProperties(this.folder.getAbsolutePath(), file, "", true);  // true means extract metadata
			current++;
	
			for (int i = new Double(current).intValue(); i < files.size(); i++) {
				
				File similarFile = files.get(i);
				if (!similarFile.getName().startsWith(filenameWithoutType)) {
					continue;
				}
				
				if (!filetype.equals(FileUtils.getFileType(similarFile.getName()))) {
					continue;
				}
				

				FileProperties similarFileProperties = new FileProperties(this.folder.getAbsolutePath(), similarFile, "", true);
				
				if ((fileProperties.getRawSize() != similarFileProperties.getRawSize())
						|| !fileProperties.getRawDateCreated().equals(similarFileProperties.getRawDateCreated())) {
					continue;
				}
				
				List<FileProperties> similarFiles = (!similarMap.containsKey(fileProperties)) ? new ArrayList<FileProperties>() : similarMap.get(fileProperties);
				similarFiles.add(similarFileProperties);
				similarMap.put(fileProperties, similarFiles);
			}


			final double percent = new Double(current / total).doubleValue();
			Platform.runLater(() -> percentComplete.set(percent));
			try {
				Thread.sleep(1); 
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}      
		percentComplete.set(1.0);
		completed = true;		
		return;
	}
	
	public void processFolder() {
		

		List<File> files = getAllFilesInFolder(folder);
		processFolder(files);
		

	}
	
	private List<File> getAllFilesInFolder(File folder) {

        List<File> files = new ArrayList<File>();

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                System.out.println("Folder:" + fileEntry.getName());
                List<File> subFolderFiles = getAllFilesInFolder(fileEntry);
                files.addAll(subFolderFiles);
            } else {
                String filename = fileEntry.getName().toLowerCase();
                if (systemFiles.contains(filename)) {
                    skippedFiles.add(new FileProperties(this.folder.getAbsolutePath(), fileEntry));
                    continue;
                }
                files.add(fileEntry);
            }
        }

        return files;
    }
	
	public void printMap() {
		for (FileProperties fp : similarMap.keySet()) {
			List<FileProperties> similarFiles = similarMap.get(fp);
			System.out.println();
			System.out.println("Similar Files for: " + fp.getFile().getName());
			System.out.println();

			for (FileProperties sfp : similarFiles) {
				System.out.println(sfp.getFile().getName());
			}
		}
	}
	
}
