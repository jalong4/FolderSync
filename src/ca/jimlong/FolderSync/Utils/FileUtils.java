package ca.jimlong.FolderSync.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ca.jimlong.FolderSync.Models.ChecksumFolder;
import ca.jimlong.FolderSync.Models.FileProperties;
import javafx.collections.ObservableList;

public class FileUtils {

    public static String getFileType(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(i + 1);
        }

        return "";
    }
    
    public static String getFileType(File file) {
        String filename = file.getName().toLowerCase();
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(i + 1);
        }

        return "";
    }
    
    public static String getFileNameWithoutType(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
        	if (i == 1) {
        		return "";  // eg. .cache returns ""
        	}
        	String result = filename.substring(0, i); 
            return result;
        }

        return filename;
    }
    
    public static String getBaseName(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(0, i);
        }
        return "";
    }
    
    public static boolean copyFile(FileProperties fileProperties, String toBasePath, boolean overwriteFile) {

		File file = fileProperties.getFile();
		String name = fileProperties.getName();
		
		Path fromPath = file.toPath();
		Path toPath = Paths.get(toBasePath, name);

		// create all non existing sub folders
		Path parent = toPath.getParent();


		
		try {
			if (Files.notExists(parent) ) {
				Files.createDirectories(parent);
			}
					
			if (overwriteFile) {
				Files.deleteIfExists(toPath);
			}
			
			Files.copy(fromPath, toPath, StandardCopyOption.COPY_ATTRIBUTES);
			System.out.println("Copied file: " + file.getAbsolutePath() + " to " + toPath.toString());
			return true;
		} catch (IOException e1) {
			System.out.println("Warning: an IOException occurred while trying to copy file: " + file.getAbsolutePath() + " to path: " + toPath.toString());
			return false;
		}

	}
    
    public static boolean copyAndKeepOriginalFile(FileProperties fileProperties, String toBasePath) {
    	
		Path toPath = Paths.get(toBasePath, fileProperties.getName());
		Path mvPath = toPath;
    	
		int fileNo = 0;
		String filename = fileProperties.getName();
		String filetype = FileUtils.getFileType(filename);
		
		while(Files.exists(mvPath) && !Files.isDirectory(mvPath)) { 
		    fileNo++; 
		    String newName = filename.replaceAll("." + filetype, " (" + fileNo + ")." + filetype);   
		    mvPath = Paths.get(toBasePath, newName);
		}
		
		if (Files.exists(toPath)) {
			try {
				Files.move(toPath, mvPath);
				System.out.println("Original file on target folder saved to " + mvPath.toFile().getName());
			} catch (IOException e1) {
				System.out.println("Error:  Unable to rename original file, aborting copy");
				return false;
			}
		}
		
		return (copyFile(fileProperties, toBasePath, false));
    }

    public static boolean moveFileAndRenameTargetIfTargetExists(Path fromPath, Path toPath) {
    	
    	if (fromPath.equals(toPath)) {
    		return true;
    	}
    	
		int fileNo = 0;
		String filename = toPath.getFileName().toString();
		String basePath = (toPath.getParent() == null) ? "" : toPath.getParent().toString();
		String filetype = FileUtils.getFileType(filename);
		
		Path mvPath = toPath;
		
		while(Files.exists(mvPath) && !Files.isDirectory(mvPath)) { 
		    fileNo++; 
		    String newName = filename.replaceAll("." + filetype, " (" + fileNo + ")." + filetype);   
		    mvPath = Paths.get(basePath, newName);
		}
    	
    	return moveFile(fromPath, mvPath);
    }
    public static boolean moveFile(Path fromPath, Path toPath) {

		// create all non existing sub folders
		Path parent = toPath.getParent();


		
		try {
			if (Files.notExists(parent) ) {
				Files.createDirectories(parent);
			}
			
			Files.move(fromPath, toPath);
			System.out.println("Moved file from: " + fromPath.toString() + " to " + toPath.toString());
			return true;
		} catch (IOException e1) {
			System.out.println("Warning: an IOException occurred while trying to move file from: " + fromPath.toString() + " to path: " + toPath.toString());
			return false;
		}

	}
    
    public static boolean deleteFile(File file) {
		Path path = file.toPath();
		try {
			Files.delete(path);
			System.out.println("Successfully deleted: " + file.getAbsolutePath());
			return true;
		} catch (DirectoryNotEmptyException error) {
			System.out.println("Failed to deleted: " + file.getAbsolutePath());
			error.printStackTrace();
			return false;
		} catch (IOException error) {
			System.out.println("Failed to deleted: " + file.getAbsolutePath());
			error.printStackTrace();
			return false;
		} catch (SecurityException error) {
			System.out.println("Failed to deleted: " + file.getAbsolutePath());
			error.printStackTrace();
			return false;
		}
	}
    
    public static List<File> getAllFilesInFolder(ChecksumFolder checksumBaseFolder, File folder, List<String> validFiletypes, ObservableList<FileProperties> skippedFiles) {

        List<File> files = new ArrayList<File>();
        File baseFolder = checksumBaseFolder.getFolder();
        String filter = checksumBaseFolder.getFilter();

        File[] allFiles = folder.listFiles(( dir, name ) -> {
        	
        	// only filter top level folders
        	
        	if (filter == null || filter.isEmpty()) {
        		return true;
        	}
        	
        	if (!dir.equals(baseFolder)) {
        		return true;
        	}
       	
        	File filename = Paths.get(dir.getAbsolutePath(), name).toFile();
        	if (filename.isFile()) {
        		return true;
        	}
        	
        	boolean matches = Pattern.compile(filter).matcher(name).matches();
        	if (!matches) {
        		System.out.println("Filtering folder: " + name);
        	}
        	return matches;
        });
        
        for (final File fileEntry : allFiles) {
            if (fileEntry.isDirectory()) {
                System.out.println("Folder:" + fileEntry.getName());
                List<File> subFolderFiles = getAllFilesInFolder(checksumBaseFolder, fileEntry, validFiletypes, skippedFiles);
                files.addAll(subFolderFiles);
            } else {
                String filename = fileEntry.getName().toLowerCase();
                String filetype = FileUtils.getFileType(filename);

                if (!validFiletypes.contains(filetype)) {
                    skippedFiles.add(new FileProperties(baseFolder.getAbsolutePath(), fileEntry));
                    continue;
                }
                files.add(fileEntry);
            }
        }

        return files;
    }

}
