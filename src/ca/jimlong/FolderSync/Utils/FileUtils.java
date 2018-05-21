package ca.jimlong.FolderSync.Utils;

public class FileUtils {

    public static String getFileType(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(i + 1);
        }

        return "";
    }
    
    public static String getBaseName(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(0, i);
        }
        return "";
    }

}
