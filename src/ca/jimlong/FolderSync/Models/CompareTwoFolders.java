package ca.jimlong.FolderSync.Models;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CompareTwoFolders {
    public File srcFolder;
    public File destFolder;
    public ObservableList<ChecksumFileProperties> notInOther;
    public ObservableList<ChecksumFileProperties> notInThis;

    public CompareTwoFolders(File srcFolder, File destFolder) {
        this.srcFolder = srcFolder;
        this.destFolder = destFolder;
        notInOther = FXCollections.observableArrayList();
        notInThis = FXCollections.observableArrayList();
    }
}