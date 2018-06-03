package ca.jimlong.FolderSync.Models;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CompareTwoFolders {
    public File srcFolder;
    public File destFolder;
    public ObservableList<FileProperties> notInOther;
    public ObservableList<FileProperties> notInThis;
    public ObservableList<FileProperties> matched;

    public CompareTwoFolders(File srcFolder, File destFolder) {
        this.srcFolder = srcFolder;
        this.destFolder = destFolder;
        notInOther = FXCollections.observableArrayList();
        notInThis = FXCollections.observableArrayList();
        matched = FXCollections.observableArrayList();
    }
}