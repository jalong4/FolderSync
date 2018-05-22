package ca.jimlong.FolderSync;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import ca.jimlong.FolderSync.Models.ChecksumFileProperties;
import ca.jimlong.FolderSync.Models.ChecksumFolder;
import ca.jimlong.FolderSync.Models.CompareTwoFolders;
import ca.jimlong.FolderSync.Models.Settings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
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
    private Menu taskMenu;
    
    @FXML
    private Menu helpMenu;
    
    
    //  File MenuItems

    @FXML
    private MenuItem openSourceFolderMenuItem;

    @FXML
    private MenuItem openDestinationFolderMenuItem;
    
    @FXML
    private MenuItem quitMenuItem;
    
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
        
		sourceFolderLabel.setText(settings.getSrcFolder());
		destinationFolderLabel.setText(settings.getDestFolder());
		
		setTreeView();
		setTreeViewCallbacks();
		
		nameCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("name"));
		dateCreatedCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("dateCreated"));
		kindCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("kind"));
		sizeCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("size"));
		checksumCol.setCellValueFactory(new PropertyValueFactory<ChecksumFileProperties, String>("checksum"));		
	
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
			
			System.out.println(folder + " parent: " + parentFolder + " Child count: " + Integer.toString(item.getChildren().size()));
			
			if (e.getButton() == MouseButton.SECONDARY) {				
				if (item.getChildren().size() == 0) {
					if (folder.equals("Source Folder")) {
						performChecksumForSrcFolder();
					} else if (folder.equals("Destination Folder")) {
						performChecksumForDestFolder();
					} else if (folder.equals("Comparison Results")) {
						if (!compareFoldersMenuItem.isDisable()) {
							performCompareFolders();
						}
					}
				} else if (folder.equals("Folders")) {
					performAllTasks();
				}
			} else if (e.getButton() == MouseButton.PRIMARY) {
				if (parentFolder.equals("Source Folder"))
					populateTableView(src, folder);
				else if (parentFolder.equals("Destination Folder")) {
					populateTableView(dest, folder);
				} else if (parentFolder.equals("Comparison Results")) {
					if (folder.startsWith("Src Files ->")) {
						tableView.setItems(compareTwoFolders.notInOther);
					} else if (folder.startsWith("Dest Files ->")) {
						tableView.setItems(compareTwoFolders.notInThis);
					} else if (folder.startsWith("Matched Files")) {
						tableView.setItems(compareTwoFolders.matched);
					} else {
						System.out.println("Unknown folder: " + folder);
					}
				}
			}
			
        });
	}


	private void populateTableView(ChecksumFolder parent, String folder) {
		
		if (folder.startsWith("Duplicates")) {
			tableView.setItems(parent.duplicateFiles);
		} else if (folder.startsWith("Unique")) {
			tableView.setItems(parent.getObservableListOfMapValues());
		} else if (folder.startsWith("Skipped")) {
			tableView.setItems(parent.skippedFiles);
		} else {
			System.out.println("Unknown folder: " + folder);
		}
		
	}


	private void setupMenuCallbacks() {
		
        openSourceFolderMenuItem.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(settings.getSrcFolder()));
            directoryChooser.setTitle("Select Source Folder");
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
            directoryChooser.setTitle("Select Destination Folder");
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

	private void performChecksumForDestFolder() {
		File folder = new File(settings.getDestFolder());
        dest = null;
        compareTwoFolders = null;
        destinationFolderTreeItem.getChildren().clear();
        comparisionResultsTreeItem.getChildren().clear();
		dest = new ChecksumFolder(folder, settings.getValidFiletypes());
		destinationProgressBar.progressProperty().bind(dest.percentComplete);
		destinationProgressBar.setVisible(true);

		new Thread() {
		    public void run() {
		        dest.generateChecksumMapForFolder();
		        Platform.runLater(() -> {
		            compareFoldersMenuItem.setDisable(src == null ? true : !src.isChecksumCompleted());
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
		src = new ChecksumFolder(folder, settings.getValidFiletypes());
		sourceProgressBar.progressProperty().bind(src.percentComplete);
		sourceProgressBar.setVisible(true);

		new Thread() {
		    public void run() {
		        src.generateChecksumMapForFolder();
		        Platform.runLater(() -> {
		            compareFoldersMenuItem.setDisable(dest == null ? true : !dest.isChecksumCompleted());
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
			treeItem.getChildren().add(new TreeItem<String>("Duplicates Files (" + Integer.toString(folder.duplicateFiles.size()) + ")"));			
		}
		
		if (folder.skippedFiles.size() > 0) {
			treeItem.getChildren().add(new TreeItem<String>("Skipped Files (" + Integer.toString(folder.skippedFiles.size()) + ")"));			
		}
		
		if (folder.map.size() > 0) {
			treeItem.getChildren().add(new TreeItem<String>("Unique Files (" + Integer.toString(folder.map.size()) + ")"));			
		}

		if (treeItem.getChildren().size() == 0) {
			treeItem.getChildren().add(new TreeItem<String>("No matching files found"));
		}
		treeItem.setExpanded(true);
	}
	
	private void showFolderComparisonDetailsOnTreeView() {
		
		if (compareTwoFolders == null) return;
		
		if (compareTwoFolders.notInOther.size() > 0) {
			comparisionResultsTreeItem.getChildren().add(new TreeItem<String>("Src Files -> Dest Folder ("  + Integer.toString(compareTwoFolders.notInOther.size()) + ")"));
		}
		
		if (compareTwoFolders.notInThis.size() > 0) {
			comparisionResultsTreeItem.getChildren().add(new TreeItem<String>("Dest Files -> Src Folder ("  + Integer.toString(compareTwoFolders.notInThis.size()) + ")"));
		}
		
		if (compareTwoFolders.matched.size() > 0) {
			comparisionResultsTreeItem.getChildren().add(new TreeItem<String>("Matched Files ("  + Integer.toString(compareTwoFolders.matched.size()) + ")"));
		}
				
		if (comparisionResultsTreeItem.getChildren().size() == 0) {
			if (src == null || src.map.size() == 0) {
				comparisionResultsTreeItem.getChildren().add(new TreeItem<String>("Source Folder must have files"));
			}
		}
		comparisionResultsTreeItem.setExpanded(true);
	}

	@SuppressWarnings("unchecked")
	private void setTreeView() {
		root = new TreeItem<String>();
		treeView.setRoot(root);
		
		sourceFolderTreeItem = new TreeItem<String>("Source Folder", getFolderIcon());
		destinationFolderTreeItem = new TreeItem<String>("Destination Folder", getFolderIcon());
		comparisionResultsTreeItem = new TreeItem<String>("Comparison Results", getFolderIcon());
		
		root.setValue("Folders");
		

		root.setGraphic(getFolderIcon());
		root.getChildren().addAll(sourceFolderTreeItem, destinationFolderTreeItem, comparisionResultsTreeItem);
		root.setExpanded(true);
				
	}	

}
