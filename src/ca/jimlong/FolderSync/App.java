package ca.jimlong.FolderSync;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class App extends Application {
    
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
   
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainLayout.fxml"));
		Pane pane = loader.load();

		Scene scene = new Scene(pane);

		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Folder Sync");
		
		MainController controller = (MainController) loader.getController();
		controller.setWindow(primaryStage);		
		
		primaryStage.show();

	}

}
