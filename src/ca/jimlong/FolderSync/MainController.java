package ca.jimlong.FolderSync;

import java.nio.file.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import ca.jimlong.FolderSync.Models.ChecksumCache;
import ca.jimlong.FolderSync.Models.ChecksumFileProperties;
import ca.jimlong.FolderSync.Models.ChecksumFolder;
import ca.jimlong.FolderSync.Models.CompareTwoFolders;
import ca.jimlong.FolderSync.Models.Settings;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainController implements Initializable {
	

    @FXML
    private MenuBar menuBar;
    
    @FXML
    private Menu fileMenu;
    
    @FXML
    private Menu editMenu;
    
    @FXML
    private Menu taskMenu;
    
    @FXML
    private Menu helpMenu;
    
    
    //  File MenuItems
    
    @FXML
    private MenuItem selectAllMenuItem;
    
    @FXML
    private MenuItem openSourceFolderMenuItem;

    @FXML
    private MenuItem openDestinationFolderMenuItem;
    
    @FXML
    private MenuItem quitMenuItem;
    
    // Edit MenuItems
    
    @FXML
    private MenuItem copyFilesMenuItem;
    @FXML
    private MenuItem deleteFilesMenuItem;

    @FXML
    private MenuItem showOnMapMenuItem;
   
    @FXML
    private MenuItem copyLocationToClipboardMenuItem;
    @FXML
    private MenuItem copyFullFilenameToClipboardMenuItem;
    
    
    // Task MenuItems

    @FXML
    private MenuItem checksumSourceFolderMenuItem;
    
    @FXML
    private MenuItem checksumDestinationFolderMenuItem;

    @FXML
    private MenuItem compareFoldersMenuItem;

    
    @FXML
    private Label sourceFolderLabel;
    
    @FXML
    private Label destinationFolderLabel;
    
    @FXML
    private Label compareFoldersLabel;
    
    @FXML
    private ProgressBar sourceProgressBar;
    
    @FXML
    private ProgressBar destinationProgressBar;
    
    @FXML
    private ProgressBar compareProgressBar;


    @FXML
    private TreeView<String> treeView;
    private TreeItem<String> root;
	TreeItem<String> sourceFolderTreeItem;
	TreeItem<String> destinationFolderTreeItem;
	TreeItem<String> comparisionResultsTreeItem;

    @FXML
    private TableView<ChecksumFileProperties> tableView;
    
    @FXML
    private TableColumn<ChecksumFileProperties, String> sizeCol;

    @FXML
    private TableColumn<ChecksumFileProperties, String> checksumCol;
    
    @FXML
    private TableColumn<ChecksumFileProperties, String> locationCol;
  
    @FXML
    private TableColumn<ChecksumFileProperties, Number> sequenceNumberCol;
    
    @FXML
    private TableColumn<ChecksumFileProperties, String> nameCol;

    @FXML
    private TableColumn<ChecksumFileProperties, String> kindCol;

    @FXML
    private TableColumn<ChecksumFileProperties, String> dateCreatedCol;

    
    @FXML
    private ImageView imageView;
    

    private final static String settingsFile = "settings.json";
    private Stage window;
    private ChecksumFolder src;
    private ChecksumFolder dest;
    private ChecksumCache cache;
    
	private CompareTwoFolders compareTwoFolders;
    private Settings settings;
    
    public void setWindow(Stage window) {
    	this.window = window;
    }
    
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println( "Launching Folder Sync Application..." );
		
		setupMenuCallbacks();
		
        sourceProgressBar.setVisible(false);
        destinationProgressBar.setVisible(false);
        compareProgressBar.setVisible(false);
        
		
        
        File file = new File(getClass().getResource(settingsFile).getFile());
        settings = new Settings(file);
        cache = new ChecksumCache(settings.getCacheFile());
        
		sourceFolderLabel.setText(settings.getSrcFolder());
		destinationFolderLabel.setText(settings.getDestFolder());
		
		setTreeView();
		setTreeViewCallbacks();
		
		nameCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("name"));
		dateCreatedCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("dateCreated"));
		kindCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("kind"));
		sizeCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("size"));
		checksumCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("checksum"));	
		locationCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("location"));
		sequenceNumberCol.setCellValueFactory(column-> new ReadOnlyObjectWrapper<Number>(tableView.getItems().indexOf(column.getValue()) + 1));
		sequenceNumberCol.setSortable(false);
		
		
		dateCreatedCol.setComparator((String s1, String s2) -> {
			Instant i1 = LocalDateTime.parse(s1, DateTimeFormatter.ofPattern(ChecksumFileProperties.datePattern)).atZone(ZoneId.systemDefault()).toInstant();
			Instant i2 = LocalDateTime.parse(s2, DateTimeFormatter.ofPattern(ChecksumFileProperties.datePattern)).atZone(ZoneId.systemDefault()).toInstant();
			return i1.compareTo(i2);			
		});
		
		sizeCol.setComparator((String s1, String s2) -> {
			Long l1 = ChecksumFileProperties.formattedValues.get(s1);
			Long l2 = ChecksumFileProperties.formattedValues.get(s2);
			return l1.compareTo(l2);			
		});
		
	}

	private ImageView getFolderIcon() {
		ImageView folderIcon = new ImageView(new Image(getClass().getResourceAsStream("images/folder.png")));
		folderIcon.setFitHeight(16);
		folderIcon.setFitWidth(16);	
		
		return folderIcon;
	}
	
	private void setTreeViewCallbacks() {
		
		treeView.setOnMouseClicked(e -> {
//			if (e.getClickCount() == 2) {
			
			TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
			String folder = item.getValue();
			String parentFolder = item.getParent() == null ? "(No Parent)" : item.getParent().getValue();

				
			if (e.getButton() == MouseButton.SECONDARY) {				
				if (item.getChildren().size() == 0) {
					if (folder.equals(settings.constants.folderNames.srcFolder)) {
						performChecksumForSrcFolder();
					} else if (folder.equals(settings.constants.folderNames.destFolder)) {
						performChecksumForDestFolder();
					} else if (folder.equals(settings.constants.folderNames.comparisonResults)) {
						if (!compareFoldersMenuItem.isDisable()) {
							performCompareFolders();
						}
					}
				} else if (folder.equals("Folders")) {
					performAllTasks();
				}
			} else if (e.getButton() == MouseButton.PRIMARY) {
				if (parentFolder.equals(settings.constants.folderNames.srcFolder))
					populateTableView(src, folder);
				else if (parentFolder.equals(settings.constants.folderNames.destFolder)) {
					populateTableView(dest, folder);
				} else if (parentFolder.equals(settings.constants.folderNames.comparisonResults)) {
					if (folder.startsWith(settings.constants.folderNames.notInOther)) {
						tableView.setItems(compareTwoFolders.notInOther);
					} else if (folder.startsWith(settings.constants.folderNames.notInThis)) {
						tableView.setItems(compareTwoFolders.notInThis);
					} else if (folder.startsWith(settings.constants.folderNames.matched)) {
						tableView.setItems(compareTwoFolders.matched);
					} else {
						tableView.setItems(null);
						System.out.println("Ignoring folder: " + folder);
					}
				}
				tableView.setId(parentFolder + "/" + folder);
				dateCreatedCol.setSortType(TableColumn.SortType.ASCENDING);
				tableView.getSortOrder().add(dateCreatedCol);
				tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			}
			
        });
	}


	private void populateTableView(ChecksumFolder parent, String folder) {
		
		if (folder.startsWith(settings.constants.folderNames.duplicateFiles)) {
			tableView.setItems(parent.duplicateFiles);
		} else if (folder.startsWith(settings.constants.folderNames.uniqueFiles)) {
			tableView.setItems(parent.getObservableListOfMapValues());
		} else if (folder.startsWith(settings.constants.folderNames.skippedFiles)) {
			tableView.setItems(parent.skippedFiles);
		} else {
			tableView.setItems(null);
			System.out.println("Ignoring folder: " + folder);
		}
		
	}


	private void setupMenuCallbacks() {
		
		selectAllMenuItem.setOnAction(e -> {
			tableView.getSelectionModel().selectAll();
		});
		
		deleteFilesMenuItem.setOnAction(e -> {
			ObservableList<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
			ObservableList<ChecksumFileProperties> deleted = FXCollections.observableArrayList();
			
			for (Integer index : selectedIndices) {
				ChecksumFileProperties row = tableView.getItems().get(index.intValue());
				if (deleteFile(row.getFile())) {
					deleted.add(row);
				}
			}
			
	        Platform.runLater(() -> tableView.getItems().removeAll(deleted));

		});
		
		copyFilesMenuItem.setOnAction(e -> {
			ObservableList<ChecksumFileProperties> selectedRows = tableView.getSelectionModel().getSelectedItems();
			processTableViewTags();
			
			for (ChecksumFileProperties row : selectedRows) {
				File file = row.getFile();
				Path path = file.toPath();
				System.out.println("Copying file: " + file.getAbsolutePath());
			}
		});
       
		
        openSourceFolderMenuItem.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(settings.getSrcFolder()));
            directoryChooser.setTitle("Select " + settings.constants.folderNames.srcFolder);
            File selectedDirectory = directoryChooser.showDialog(window);
            settings.setSrcFolder(selectedDirectory == null ? "" : selectedDirectory.getAbsolutePath());
            sourceFolderLabel.setText(settings.getSrcFolder());
            src = null;
            compareTwoFolders = null;

            sourceFolderTreeItem.getChildren().clear();
            comparisionResultsTreeItem.getChildren().clear();
            compareFoldersMenuItem.setDisable(true);
            sourceProgressBar.setVisible(false);
            showFolderDetailsOnTreeView(src, sourceFolderTreeItem);
        });

        openDestinationFolderMenuItem.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(settings.getDestFolder()));
            directoryChooser.setTitle("Select " + settings.constants.folderNames.destFolder);
            File selectedDirectory = directoryChooser.showDialog(window);
            settings.setDestFolder(selectedDirectory == null ? "" : selectedDirectory.getAbsolutePath());
            destinationFolderLabel.setText(settings.getDestFolder());
            dest = null;
            compareTwoFolders = null;
            destinationFolderTreeItem.getChildren().clear();
            comparisionResultsTreeItem.getChildren().clear();
            compareFoldersMenuItem.setDisable(true);
            destinationProgressBar.setVisible(false);
            showFolderDetailsOnTreeView(dest, destinationFolderTreeItem);
        });

        checksumSourceFolderMenuItem.setOnAction(e -> {
            performChecksumForSrcFolder();

        });

        checksumDestinationFolderMenuItem.setOnAction(e -> {
            performChecksumForDestFolder();
        });


        compareFoldersMenuItem.setDisable(true);
        compareFoldersMenuItem.setOnAction(e -> {
            performCompareFolders(); 
        });
        
		
		compareFoldersMenuItem.disableProperty().addListener(e -> {
			System.out.println("event fired!!!");
			if (!compareFoldersMenuItem.isDisable()) {
				performCompareFolders();
			}
		});

        quitMenuItem.setOnAction(e -> {
            window.close();
        });
        
	}


	private boolean deleteFile(File file) {
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

	private void processTableViewTags() {
		
		String[] tags = tableView.getId().split("\\/");
		if (tags.length != 2) {
			return;
		}
		
		
		String parentFolder = tags[0];
		String folder = tags[1];
		
		
		
	}


	private void performChecksumForDestFolder() {
		File folder = new File(settings.getDestFolder());
        dest = null;
        compareTwoFolders = null;
        destinationFolderTreeItem.getChildren().clear();
        comparisionResultsTreeItem.getChildren().clear();
		dest = new ChecksumFolder(folder, settings.getValidFiletypes(), cache);
		destinationProgressBar.progressProperty().bind(dest.percentComplete);
		destinationProgressBar.setVisible(true);

		new Thread() {
		    public void run() {
		        dest.generateChecksumMapForFolder();
	        	cache.update(dest);
	        	boolean disabled = src == null ? true : !src.isChecksumCompleted();
	            if (!disabled) {
	            	cache.rewrite();
	            }
		        Platform.runLater(() -> {
		            compareFoldersMenuItem.setDisable(disabled);
		            showFolderDetailsOnTreeView(dest, destinationFolderTreeItem);
		        });
		    }
		}.start();
	}

	private void performChecksumForSrcFolder() {
		File folder = new File(settings.getSrcFolder());
        src = null;
        compareTwoFolders = null;
        sourceFolderTreeItem.getChildren().clear();
        comparisionResultsTreeItem.getChildren().clear();
		src = new ChecksumFolder(folder, settings.getValidFiletypes(), cache);
		sourceProgressBar.progressProperty().bind(src.percentComplete);
		sourceProgressBar.setVisible(true);

		new Thread() {
		    public void run() {
		        src.generateChecksumMapForFolder();
	        	cache.update(src);
	        	boolean disabled = dest == null ? true : !dest.isChecksumCompleted();
	            if (!disabled) {
	            	cache.rewrite();
	            }
		        Platform.runLater(() -> {
		            compareFoldersMenuItem.setDisable(disabled);
		            showFolderDetailsOnTreeView(src, sourceFolderTreeItem);
		        });
		    }
		}.start();
	}
	

	private void performCompareFolders() {
        compareTwoFolders = null;
        comparisionResultsTreeItem.getChildren().clear();
		compareProgressBar.progressProperty().bind(src.comparePercentComplete);
		compareProgressBar.setVisible(true);
		new Thread() {
		    public void run() {
		        compareTwoFolders = src.compare(dest);
//		        Platform.runLater(() -> status.setText("Done comparing checksum Mappings"));
		        showFolderComparisonDetailsOnTreeView();
		    }
		}.start();
	}
	
    
	
	private void performAllTasks() {
		performChecksumForSrcFolder();
		performChecksumForDestFolder();
	}
	
	
	private void showFolderDetailsOnTreeView(ChecksumFolder folder, TreeItem<String> treeItem) {
		
		if (folder == null) return;
		
		if (folder.duplicateFiles.size() > 0) {
			treeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.duplicateFiles + " (" + Integer.toString(folder.duplicateFiles.size()) + ")"));			
		}
		
		if (folder.skippedFiles.size() > 0) {
			treeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.skippedFiles + " (" + Integer.toString(folder.skippedFiles.size()) + ")"));			
		}
		
		if (folder.map.size() > 0) {
			treeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.uniqueFiles + " ("  + Integer.toString(folder.map.size()) + ")"));			
		}

		if (treeItem.getChildren().size() == 0) {
			treeItem.getChildren().add(new TreeItem<String>("No matching files found"));
		}
		treeItem.setExpanded(true);
	}
	
	private void showFolderComparisonDetailsOnTreeView() {
		
		if (compareTwoFolders == null) return;
		
		if (compareTwoFolders.notInOther.size() > 0) {
			comparisionResultsTreeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.notInOther + " ("  + Integer.toString(compareTwoFolders.notInOther.size()) + ")"));
		}
		
		if (compareTwoFolders.notInThis.size() > 0) {
			comparisionResultsTreeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.notInThis + " ("  + Integer.toString(compareTwoFolders.notInThis.size()) + ")"));
		}
		
		if (compareTwoFolders.matched.size() > 0) {
			comparisionResultsTreeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.matched + " ("  + Integer.toString(compareTwoFolders.matched.size()) + ")"));
		}
				
		if (comparisionResultsTreeItem.getChildren().size() == 0) {
			if (src == null || src.map.size() == 0) {
				comparisionResultsTreeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.srcFolder + " must have files"));
			}
		}
		comparisionResultsTreeItem.setExpanded(true);
	}

	@SuppressWarnings("unchecked")
	private void setTreeView() {
		root = new TreeItem<String>();
		treeView.setRoot(root);
		
		sourceFolderTreeItem = new TreeItem<String>(settings.constants.folderNames.srcFolder, getFolderIcon());
		destinationFolderTreeItem = new TreeItem<String>(settings.constants.folderNames.destFolder, getFolderIcon());
		comparisionResultsTreeItem = new TreeItem<String>(settings.constants.folderNames.comparisonResults, getFolderIcon());
		
		root.setValue(settings.constants.folderNames.rootFolder);
		

		root.setGraphic(getFolderIcon());
		root.getChildren().addAll(sourceFolderTreeItem, destinationFolderTreeItem, comparisionResultsTreeItem);
		root.setExpanded(true);
				
	}	

}
