package ca.jimlong.FolderSync;

import java.nio.file.*;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import ca.jimlong.FolderSync.Models.ChecksumCache;
import ca.jimlong.FolderSync.Models.FileProperties;
import ca.jimlong.FolderSync.Models.ChecksumFolder;
import ca.jimlong.FolderSync.Models.CompareTwoFolders;
import ca.jimlong.FolderSync.Models.GoogleAPI;
import ca.jimlong.FolderSync.Models.GoogleGeoCodeAPI;
import ca.jimlong.FolderSync.Models.GoogleGeoCodeResponse;
import ca.jimlong.FolderSync.Models.OrganizeFolder;
import ca.jimlong.FolderSync.Models.Settings;
import ca.jimlong.FolderSync.Models.SimilarFiles;
import ca.jimlong.FolderSync.Utils.FileUtils;
import ca.jimlong.FolderSync.Utils.Utils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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
    private MenuItem setSourceFolderMenuItem;

    @FXML
    private MenuItem setDestinationFolderMenuItem;
    
    @FXML
    private MenuItem openSelectedFolderMenuItem;
    
    @FXML
    private MenuItem openFileViewSelectedViewerMenuItem;
    
    
    @FXML
    private MenuItem quitMenuItem;
    
    // Edit MenuItems
   
    @FXML
    private MenuItem selectAllMenuItem;
    
    @FXML
    private MenuItem deleteFilesMenuItem;
    @FXML
    private MenuItem copyFilesMenuItem;
    @FXML
    private MenuItem overwriteFileMenuItem;
    @FXML
    private MenuItem copyAndKeepOriginalFileMenuItem;


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
    private MenuItem organizeFolderMenuItem;
    @FXML   
    private MenuItem findSimilarInFolderMenuItem;  
    @FXML
    private MenuItem clearCacheMenuItem;

    
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
    private TableView<FileProperties> tableView;
    
    @FXML
    private TableColumn<FileProperties, String> sizeCol;

    @FXML
    private TableColumn<FileProperties, String> checksumCol;
    
    @FXML
    private TableColumn<FileProperties, String> locationCol;
  
    @FXML
    private TableColumn<FileProperties, Number> sequenceNumberCol;
    
    @FXML
    private TableColumn<FileProperties, String> nameCol;

    @FXML
    private TableColumn<FileProperties, String> kindCol;

    @FXML
    private TableColumn<FileProperties, String> dateCreatedCol;

   
    @FXML
    private Label imageFilenameTitleLabelLeft;  
    @FXML
    private Label imageFilenameLabelLeft; 
    @FXML
    private ImageView imageViewLeft;
    @FXML
    private Label imageSizeTitleLabelLeft;
    @FXML
    private Label imageSizeLabelLeft;
    @FXML
    private Label imageAddressTitleLabelLeft;
    @FXML
    private Label imageAddressLabelLeft;  
    @FXML
    private Label imageOrientationTitleLabelLeft;
    @FXML
    private Label imageOrientationLabelLeft; 
    
    @FXML
    private Button rotateImageLeftButton;

    @FXML
    void onRotateImageLeftButtonClicked(ActionEvent event) {
    	imageLeftRotation = (imageLeftRotation + 90) % 360;
        imageViewLeft.setRotate(imageLeftRotation);
    }
    
    @FXML
    void onRotateImageRightButtonClicked(ActionEvent event) {
    	imageRightRotation = (imageRightRotation + 90) % 360;
        imageViewRight.setRotate(imageRightRotation);
    }
    

    @FXML
    private Label imageFilenameTitleLabelRight;
    @FXML
    private Label imageFilenameLabelRight;  
    @FXML
    private ImageView imageViewRight;
    @FXML
    private Label imageSizeTitleLabelRight;
    @FXML
    private Label imageSizeLabelRight;
    @FXML
    private Label imageAddressTitleLabelRight;
    @FXML
    private Label imageAddressLabelRight;
    @FXML
    private Label imageOrientationTitleLabelRight;
    @FXML
    private Label imageOrientationLabelRight;
    @FXML
    private Button rotateImageRightButton;
    

    private final static String settingsFile = "settings.json";
    private final static String googleAPIFile = "googleAPI.json";
    private Stage window;
    private ChecksumFolder src;
    private ChecksumFolder dest;
    private ChecksumCache cache;
    private Integer imageLeftRotation;
    private Integer imageRightRotation;
    
	private CompareTwoFolders compareTwoFolders;
    private Settings settings;
    private GoogleAPI googleAPI;
    private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
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
        googleAPI = new GoogleAPI( new File(getClass().getResource(googleAPIFile).getFile()));
        
        cache = new ChecksumCache(settings.getCacheFile());
        
		sourceFolderLabel.setText(settings.getSrcFolder());
		destinationFolderLabel.setText(settings.getDestFolder());
		
		setTreeView();
		setTreeViewCallbacks();
		
		nameCol.setCellValueFactory(new PropertyValueFactory<FileProperties, String>("name"));
		dateCreatedCol.setCellValueFactory(new PropertyValueFactory<FileProperties, String>("dateCreated"));
		kindCol.setCellValueFactory(new PropertyValueFactory<FileProperties, String>("kind"));
		sizeCol.setCellValueFactory(new PropertyValueFactory<FileProperties, String>("size"));
		checksumCol.setCellValueFactory(new PropertyValueFactory<FileProperties, String>("checksum"));	
		locationCol.setCellValueFactory(new PropertyValueFactory<FileProperties, String>("location"));
		sequenceNumberCol.setCellValueFactory(column-> new ReadOnlyObjectWrapper<Number>(tableView.getItems().indexOf(column.getValue()) + 1));
		sequenceNumberCol.setSortable(false);
		
		
		dateCreatedCol.setComparator((String s1, String s2) -> {
			Instant i1 = LocalDateTime.parse(s1, DateTimeFormatter.ofPattern(FileProperties.datePattern)).atZone(ZoneId.systemDefault()).toInstant();
			Instant i2 = LocalDateTime.parse(s2, DateTimeFormatter.ofPattern(FileProperties.datePattern)).atZone(ZoneId.systemDefault()).toInstant();
			return i1.compareTo(i2);			
		});
		
		sizeCol.setComparator((String s1, String s2) -> {
			Long l1 = FileProperties.formattedValues.get(s1);
			Long l2 = FileProperties.formattedValues.get(s2);
			return l1.compareTo(l2);			
		});
		
		selectAllMenuItem.setDisable(true);
		deleteFilesMenuItem.setDisable(true);
		copyFilesMenuItem.setDisable(true);
		overwriteFileMenuItem.setDisable(true);
		copyAndKeepOriginalFileMenuItem.setDisable(true);
		showOnMapMenuItem.setDisable(true);
		copyLocationToClipboardMenuItem.setDisable(true);
		copyFullFilenameToClipboardMenuItem.setDisable(true);
		openFileViewSelectedViewerMenuItem.setDisable(true);
		
		tableView.getSelectionModel().selectedItemProperty().addListener((observer, oldSelection, newSelection) -> {
			updateEditMenuItems();
		});
		
		tableView.idProperty().addListener((observable, oldValue, newValue) -> {
            updateEditMenuItems();
        });
		
		tableView.setRowFactory(tv -> {
		    TableRow<FileProperties> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY 
		             && event.getClickCount() == 2) {

		        	FileProperties clickedRow = row.getItem();
		            Image image = new Image(clickedRow.getFile().toURI().toString());
		            imageFilenameLabelLeft.setText(clickedRow.getFile().getAbsolutePath());
		            imageSizeLabelLeft.setText(clickedRow.getSize());
		            imageViewLeft.setImage(image);
		            imageViewLeft.setFitHeight(400);
		            
		            imageLeftRotation = clickedRow.getRotation();
		            imageViewLeft.setRotate(imageLeftRotation);

		            setLeftImageViewComponentsVisible(true);
		            
		            imageAddressLabelLeft.setText("");
		            if (!clickedRow.getLocation().equals("")) {
		            	GoogleGeoCodeResponse geo = GoogleGeoCodeAPI.getGeoCodeForCoordinates(googleAPI.getBaseUrl(), googleAPI.getKey(), clickedRow.getGeoLocation());

		            	if (geo.results.length > 0) {
		            		imageAddressLabelLeft.setText(geo.results[0].formatted_address);
		            	}
		            } else {
		            	imageAddressTitleLabelLeft.setVisible(false);
		            }
		            
		    	    imageOrientationTitleLabelLeft.setVisible(false);
		    	    imageOrientationLabelLeft.setVisible(false);
		    	    imageOrientationLabelLeft.setText("");
		    	    if (clickedRow.getOrientation() != 1) {
		    		    imageOrientationLabelLeft.setText(String.valueOf(clickedRow.getOrientation()));
		    		    imageOrientationTitleLabelLeft.setVisible(true);
		    		    imageOrientationLabelLeft.setVisible(true);
		    	    }
		            
		            updateRightImageView(clickedRow);
		        }
		    });
		    return row ;
		});
		
		imageViewLeft.yProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("YProperty changed from " + oldValue + " to " + newValue);
		});
		
	    setImageViewComponentsVisible(false);
	    
		rotateImageLeftButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ROTATE_RIGHT));
		rotateImageRightButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ROTATE_RIGHT));
		
	}


	private void setImageViewComponentsVisible(boolean visible) {
	    setLeftImageViewComponentsVisible(visible);
	    setRightImageViewComponentsVisible(visible);
	}

	private void setLeftImageViewComponentsVisible(boolean visible) {
		imageFilenameTitleLabelLeft.setVisible(visible); 
	    imageFilenameLabelLeft.setVisible(visible);   
	    imageViewLeft.setVisible(visible);
	    imageSizeTitleLabelLeft.setVisible(visible); 
	    imageSizeLabelLeft.setVisible(visible); 
	    imageAddressTitleLabelLeft.setVisible(visible); 
	    imageAddressLabelLeft.setVisible(visible); 
	    imageOrientationTitleLabelLeft.setVisible(visible);
	    imageOrientationLabelLeft.setVisible(visible);
	    rotateImageLeftButton.setVisible(visible);
	}
	
	private void setRightImageViewComponentsVisible(boolean visible) {
		imageFilenameTitleLabelRight.setVisible(visible);  
	    imageFilenameLabelRight.setVisible(visible);   
	    imageViewRight.setVisible(visible);
	    imageSizeTitleLabelRight.setVisible(visible); 
	    imageSizeLabelRight.setVisible(visible); 
	    imageAddressTitleLabelRight.setVisible(visible); 
	    imageAddressLabelRight.setVisible(visible); 
	    imageOrientationTitleLabelRight.setVisible(visible);
	    imageOrientationLabelRight.setVisible(visible);
	    rotateImageRightButton.setVisible(visible);
	}
	
	private void updateEditMenuItems() {
		
		String[] tags = tableView.getId().split("\\/");
		
		setImageViewComponentsVisible(false);
		
		selectAllMenuItem.setDisable(true);
		deleteFilesMenuItem.setDisable(true);
		copyFilesMenuItem.setDisable(true);
		overwriteFileMenuItem.setDisable(true);
		copyAndKeepOriginalFileMenuItem.setDisable(true);
		showOnMapMenuItem.setDisable(true);
		copyLocationToClipboardMenuItem.setDisable(true);
		copyFullFilenameToClipboardMenuItem.setDisable(true);
		openFileViewSelectedViewerMenuItem.disableProperty().bind(copyFullFilenameToClipboardMenuItem.disableProperty());
		
		if (tags.length != 2 || tags[1].equals(settings.constants.folderNames.rootFolder)
				|| tableView.getItems().isEmpty()) {
			return;
		}		
		
		selectAllMenuItem.setDisable(false);
		
		boolean multiSelect = tableView.getSelectionModel().getSelectedIndices().size() > 1;
		
		if (tableView.getSelectionModel().getSelectedIndices().size() > 0) {
    		String folderName = tags[1];
    		if (folderName.startsWith(settings.constants.folderNames.duplicateFiles)) {
    			deleteFilesMenuItem.setDisable(false);
    			copyLocationToClipboardMenuItem.setDisable(multiSelect);
    			copyFullFilenameToClipboardMenuItem.setDisable(multiSelect);
    		} else if (folderName.startsWith(settings.constants.folderNames.similarFiles)) {
        			deleteFilesMenuItem.setDisable(false);
        			copyLocationToClipboardMenuItem.setDisable(multiSelect);
        			copyFullFilenameToClipboardMenuItem.setDisable(multiSelect);
    		} else if (folderName.startsWith(settings.constants.folderNames.uniqueFiles)) {
    			showOnMapMenuItem.setDisable(false);
    			deleteFilesMenuItem.setDisable(multiSelect);
    			copyLocationToClipboardMenuItem.setDisable(multiSelect);
    			copyFullFilenameToClipboardMenuItem.setDisable(multiSelect);
    		} else if (folderName.startsWith(settings.constants.folderNames.skippedFiles)) {
    			deleteFilesMenuItem.setDisable(false);
    			copyFullFilenameToClipboardMenuItem.setDisable(multiSelect);
    		} else if (folderName.startsWith(settings.constants.folderNames.notInOther)) {
    			copyFilesMenuItem.setDisable(false);
    			copyAndKeepOriginalFileMenuItem.setDisable(false);
    			overwriteFileMenuItem.setDisable(multiSelect);
    			deleteFilesMenuItem.setDisable(false);
    			showOnMapMenuItem.setDisable(false);
    			copyLocationToClipboardMenuItem.setDisable(multiSelect);
    			copyFullFilenameToClipboardMenuItem.setDisable(multiSelect);
    		} else if (folderName.startsWith(settings.constants.folderNames.notInThis)) {
    			showOnMapMenuItem.setDisable(false);
    			copyLocationToClipboardMenuItem.setDisable(multiSelect);
    			copyFullFilenameToClipboardMenuItem.setDisable(multiSelect);
    		} else if (folderName.startsWith(settings.constants.folderNames.matched)) {
    			showOnMapMenuItem.setDisable(false);
    			copyLocationToClipboardMenuItem.setDisable(multiSelect);
    			copyFullFilenameToClipboardMenuItem.setDisable(multiSelect);
    		}
		}
		
	}
	
	private void updateRightImageView(FileProperties clickedRow) {
		
		String[] tags = tableView.getId().split("\\/");
		
		setRightImageViewComponentsVisible(false);
		
		if (tags.length != 2 || tags[1].equals(settings.constants.folderNames.rootFolder)
				|| tableView.getItems().isEmpty()) {
			return;
		}		
		
		String parentFolder = tags[0];
		String folderName = tags[1];
		ChecksumFolder folder = (parentFolder.equals(settings.constants.folderNames.srcFolder)) ? src : dest;
		
		if (tableView.getSelectionModel().getSelectedIndices().size() == 1) {

    		if (folderName.startsWith(settings.constants.folderNames.duplicateFiles)) {
    			FileProperties unique = folder.map.get(clickedRow.getChecksum());
	            displayRightImage(clickedRow, unique);
				
    		} else if (folderName.startsWith(settings.constants.folderNames.uniqueFiles)) {
    			// in priority order, show either the duplicate if there is one in the same folder/subfolder or 
    			// show the file with the same name in the dest folder if it is a src folder
    			
    		} else if (folderName.startsWith(settings.constants.folderNames.skippedFiles)) {
    			// maybe try to see if is a displayable image?
    			
    		} else if (folderName.startsWith(settings.constants.folderNames.notInOther)) {
    			// show the file with the same name in the dest folder (if it exists)
    			
    			Path toPath = Paths.get(dest.getFolder().getAbsolutePath(), clickedRow.getName()); 
    			
    			if (Files.exists(toPath)) {
    				displayRightImage(clickedRow, new FileProperties(dest.getFolder().getAbsolutePath(), toPath.toFile(), "", true));
    			}
    			
    		} else if (folderName.startsWith(settings.constants.folderNames.notInThis)) {
    			// show the file with the same name in the src folder (if it exists)
    		} else if (folderName.startsWith(settings.constants.folderNames.matched)) {
    			// Maybe show the webView of the location?
    		}
		}
		
	}

	private void displayRightImage(FileProperties clickedRow, FileProperties other) {
		Image image = new Image(other.getFile().toURI().toString());
		imageFilenameLabelRight.setText(other.getName());
		imageSizeLabelRight.setText(other.getSize());
		imageViewRight.setImage(image);
		imageViewRight.setFitHeight(400);
		
		imageRightRotation = other.getRotation();
        imageViewRight.setRotate(imageRightRotation);
		setRightImageViewComponentsVisible(true);

		imageAddressLabelRight.setText("");
		if (!other.getLocation().equals("")) {
			GoogleGeoCodeResponse geo = GoogleGeoCodeAPI.getGeoCodeForCoordinates(googleAPI.getBaseUrl(), googleAPI.getKey(), other.getGeoLocation());
			if (geo.results.length > 0) {
				imageAddressLabelRight.setText(geo.results[0].formatted_address);
			}
		} else {
			imageAddressTitleLabelRight.setVisible(false);
		}
		
	    imageOrientationTitleLabelRight.setVisible(false);
	    imageOrientationLabelRight.setVisible(false);
	    imageOrientationLabelRight.setText("");
	    if (other.getOrientation() != 1) {
		    imageOrientationLabelRight.setText(String.valueOf(other.getOrientation()));
		    imageOrientationTitleLabelRight.setVisible(true);
		    imageOrientationLabelRight.setVisible(true);
	    }
	}

	private ImageView getFolderIcon() {
		ImageView folderIcon = new ImageView(new Image(getClass().getResourceAsStream("images/folder.png")));
		folderIcon.setFitHeight(16);
		folderIcon.setFitWidth(16);	
		
		return folderIcon;
	}
	
	
	@SuppressWarnings("unchecked")
	private void setTreeViewCallbacks() {
		
		treeView.setOnMouseClicked(e -> {			
			TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
			
			organizeFolderMenuItem.setDisable(true);
			if (item == null) {
				return;
			}
			
			String folder = item.getValue();
			String parentFolder = item.getParent() == null ? "(No Parent)" : item.getParent().getValue();

				
			if (e.getButton() == MouseButton.SECONDARY || (e.getButton() == MouseButton.PRIMARY 
		             && e.getClickCount() == 2)) {				
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
				
				if (folder.equals(settings.constants.folderNames.destFolder)) {
					organizeFolderMenuItem.setDisable(false);
				}
				
				if (parentFolder.equals(settings.constants.folderNames.srcFolder))
					populateTableView(src, folder);
				else if (parentFolder.equals(settings.constants.folderNames.destFolder)) {
					organizeFolderMenuItem.setDisable(false);
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
				tableView.getSortOrder().addAll(dateCreatedCol, nameCol);
				tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			}
			
        });
	}


	private void populateTableView(ChecksumFolder parent, String folder) {
		
		if (folder.startsWith(settings.constants.folderNames.duplicateFiles)) {
			tableView.setItems(parent.duplicateFiles);
		} else if (folder.startsWith(settings.constants.folderNames.similarFiles)) {
			tableView.setItems(parent.similarFiles);
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
			ObservableList<FileProperties> deleted = FXCollections.observableArrayList();
			
			for (Integer index : selectedIndices) {
				FileProperties row = tableView.getItems().get(index.intValue());
				if (FileUtils.deleteFile(row.getFile())) {
					deleted.add(row);
				}
			}
			
	        Platform.runLater(() -> {
	        	tableView.getItems().removeAll(deleted);
				updateDetailsOnTreeView();
	        });

		});
		
		copyFilesMenuItem.setOnAction(e -> {
			ObservableList<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
			ObservableList<FileProperties> copied = FXCollections.observableArrayList();
			
			for (Integer index : selectedIndices) {
				FileProperties row = tableView.getItems().get(index.intValue());
				if (FileUtils.copyFile(row, dest.getFolder().getAbsolutePath(), false)) {
					copied.add(row);
				}
			}
			
			System.out.println("Copied " + Integer.toString(copied.size()) + " files out of " + Integer.toString(tableView.getItems().size()));
			
			performChecksumForDestFolder();
			performCompareFolders();
			
	        Platform.runLater(() -> {
	        	tableView.getItems().removeAll(copied);
				updateDetailsOnTreeView();
	        });
		});
		
		copyAndKeepOriginalFileMenuItem.setOnAction(e -> {
			ObservableList<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
			ObservableList<FileProperties> copied = FXCollections.observableArrayList();
			
			for (Integer index : selectedIndices) {
				FileProperties row = tableView.getItems().get(index.intValue());
				if (FileUtils.copyAndKeepOriginalFile(row, dest.getFolder().getAbsolutePath())) {
					copied.add(row);
				}
			}
			
			System.out.println("Copied " + Integer.toString(copied.size()) + " files out of " + Integer.toString(tableView.getItems().size()));
			
			performChecksumForDestFolder();
			performCompareFolders();
			
	        Platform.runLater(() -> {
	        	tableView.getItems().removeAll(copied);
				updateDetailsOnTreeView();
	        });
		});
		
		
		copyLocationToClipboardMenuItem.setOnAction(e -> {
			FileProperties row = tableView.getSelectionModel().getSelectedItem();
			StringSelection content = new StringSelection(row.getLocation());
			clipboard.setContents(content, content);
		});
		
		copyFullFilenameToClipboardMenuItem.setOnAction(e -> {
			FileProperties row = tableView.getSelectionModel().getSelectedItem();
			StringSelection content = new StringSelection(row.getFile().getAbsolutePath());
			clipboard.setContents(content, content);
			
		});
		
		openFileViewSelectedViewerMenuItem.setOnAction(e -> {
			FileProperties row = tableView.getSelectionModel().getSelectedItem();
			try {
				Desktop.getDesktop().open(row.getFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		

		
		overwriteFileMenuItem.setOnAction(e -> {
			FileProperties row = tableView.getSelectionModel().getSelectedItem();
			
			if (FileUtils.copyFile(row, dest.getFolder().getAbsolutePath(), true)) {
				System.out.println("Overwriting " + row.getName() + " on " + dest.getFolder().getAbsolutePath());
				performChecksumForDestFolder();
				performCompareFolders();

				Platform.runLater(() -> {
					tableView.getItems().removeAll(row);
					updateDetailsOnTreeView();
				});
			}
		});
       
		
        setSourceFolderMenuItem.setOnAction(e -> {
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

        setDestinationFolderMenuItem.setOnAction(e -> {
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
        
        treeView.getSelectionModel().selectedItemProperty().addListener(e -> {
        	System.out.println("treeView item(s) selected");
        	openSelectedFolderMenuItem.setDisable(true);
        	
        	if (treeView.getSelectionModel().getSelectedItems().size() != 1) {
        		return;
        	}
        	
        	TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();

			if (item == null) {
				return;
			}
			
			String folder = item.getValue();
			if (folder.equals(settings.constants.folderNames.srcFolder) || folder.equals(settings.constants.folderNames.destFolder)) {
				openSelectedFolderMenuItem.setDisable(false);
			}
		});
        
        openSelectedFolderMenuItem.setDisable(true);
        openSelectedFolderMenuItem.setOnAction(e -> {
			TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
			
			if (item == null) {
				return;
			}
			
			String folder = item.getValue();
			if (folder.equals(settings.constants.folderNames.srcFolder)) {
				Utils.openFolder(new File(settings.getSrcFolder()));
			} else if (folder.equals(settings.constants.folderNames.destFolder)) {
				Utils.openFolder(new File(settings.getDestFolder()));
			}


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
        
        organizeFolderMenuItem.setDisable(true);
       
        organizeFolderMenuItem.setOnAction(e -> {
            performOrganizeFolder(); 
        });
        
		findSimilarInFolderMenuItem.disableProperty().bind(organizeFolderMenuItem.disableProperty());
		findSimilarInFolderMenuItem.setOnAction(e -> {
            performFindSimilarInFolder(new File(settings.getDestFolder())); 
        });		
        
        clearCacheMenuItem.setOnAction(e -> {
            cache.clear(); 
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
	
	private void performFindSimilarInFolder(File folder) {
		System.out.println("Looking for simimal files in Folder " + settings.getDestFolder() + "...");


 
		SimilarFiles similarFiles = new SimilarFiles(folder, settings);
		destinationProgressBar.progressProperty().bind(similarFiles.percentComplete);
		destinationProgressBar.setVisible(true);
      
		new Thread() {
		    public void run() {
		    	similarFiles.processFolder();
		    	dest.similarFiles = similarFiles.getSimilar();
		    }
		}.start();	
	}

	private void performOrganizeFolder() {
		System.out.println("organizing Folder " + settings.getDestFolder() + "...");

		OrganizeFolder folder = new OrganizeFolder(new File(settings.getDestFolder()), settings);
		destinationProgressBar.progressProperty().bind(folder.percentComplete);
		destinationProgressBar.setVisible(true);
      
		new Thread() {
		    public void run() {
		    	folder.organizeFolder();		    	
		    }
		}.start();
		
	}

	private void updateDetailsOnTreeView() {
		
		String[] tags = tableView.getId().split("\\/");
		if (tags.length != 2) {
			return;
		}
		
		String parentFolderName = tags[0];
//		String folderName = tags[1];
		
		if (parentFolderName.equals(settings.constants.folderNames.srcFolder)) {
            sourceFolderTreeItem.getChildren().clear();
			showFolderDetailsOnTreeView(src, sourceFolderTreeItem);
		} else if (parentFolderName.equals(settings.constants.folderNames.destFolder)) {
			destinationFolderTreeItem.getChildren().clear();
			showFolderDetailsOnTreeView(dest, destinationFolderTreeItem);
		} else if (parentFolderName.equals(settings.constants.folderNames.comparisonResults)) {
			comparisionResultsTreeItem.getChildren().clear();
			showFolderComparisonDetailsOnTreeView();
		}
		
	}


	private void performChecksumForDestFolder() {
		File folder = new File(settings.getDestFolder());
        dest = null;
        compareTwoFolders = null;
        destinationFolderTreeItem.getChildren().clear();
        comparisionResultsTreeItem.getChildren().clear();
		dest = new ChecksumFolder(folder, settings.getDestFolderFilter(), settings, cache);
		destinationProgressBar.progressProperty().bind(dest.percentComplete);
		destinationProgressBar.setVisible(true);

        dest.generateChecksumMapForFolder();
    	cache.update(dest);
    	boolean disabled = src == null ? true : !src.isChecksumCompleted();
        if (!disabled) {
        	cache.rewrite();
        }
        
		new Thread() {
		    public void run() {
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
		src = new ChecksumFolder(folder, settings.getSrcFolderFilter(), settings, cache);
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
		
		if (folder.similarFiles.size() > 0) {
			treeItem.getChildren().add(new TreeItem<String>(settings.constants.folderNames.similarFiles + " (" + Integer.toString(folder.similarFiles.size()) + ")"));			
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
