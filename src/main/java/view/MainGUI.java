package view;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;

import controller.MainController;

public class MainGUI extends Application {
	private static final String FXML_VIEW = "/gui/MainView2.fxml"; 
	
	public static int windowWidth = 1024;
	public static int windowHeight = 576;
	
	    @FXML private static BorderPane borderPaneContainer;
	    @FXML private static HBox simulationHeader;
	    @FXML private static TitledPane bottomLogArea;

	    @Override
	    public void start(Stage primaryStage) throws Exception {
	        System.out.println("Starting Traffic Simulator Application...");
	        
	        // Attempt to get the resource URL
	        URL fxmlUrl = getClass().getResource(FXML_VIEW);
	        
	        if (fxmlUrl == null) {
	            // Robust error message if the file is missing from the classpath
	            System.err.println("--- FATAL ERROR ---");
	            System.err.println("FXML resource not found: " + FXML_VIEW);
	            System.err.println("Please ensure the file is located at src/main/resources" + FXML_VIEW);
	            System.err.println("-------------------");
	            throw new IllegalStateException("FXML resource not found at " + FXML_VIEW);
	        }
	        
	        FXMLLoader loader = new FXMLLoader(fxmlUrl);
	        Parent root = loader.load();
	        MainController controller = loader.getController();

	        primaryStage.setTitle("Cool Traffic Simulator");
	        primaryStage.setScene(new Scene(root, windowWidth, windowHeight)); // Set default size
	        
	        primaryStage.setOnCloseRequest(event -> {
                System.out.println("Window closing...");
                controller.stopSimulation(); // Stop threads before exit
            });
	        primaryStage.show();
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }
}