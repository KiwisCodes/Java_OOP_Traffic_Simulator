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
	private static final String FXML_VIEW = "/gui/MainView.fxml"; 
	
		
		@FXML private static BorderPane borderPaneContainer;
	    @FXML private static HBox simulationHeader;
	    @FXML private static TitledPane bottomLogArea;

	    @Override
	    public void start(Stage primaryStage) throws Exception {
	        System.out.println("Starting Traffic Simulator Application...");
	        
//	        get the resource URL
//	        URL fxmlUrl = getClass().getResource(FXML_VIEW);
	        URL fxmlUrl = view.MainGUI.class.getResource(FXML_VIEW);
	        
	        if (fxmlUrl == null) {
	            System.err.println("FXML not found: " + FXML_VIEW);
	        }
	        
	        FXMLLoader loader = new FXMLLoader(fxmlUrl);
	        
	        loader.setClassLoader(MainGUI.class.getClassLoader());//newly added
	        
	        Parent root = loader.load();
	        MainController controller = loader.getController();
	        controller.log("Current FXML path: " + fxmlUrl);

	        primaryStage.setTitle("Cool Traffic Simulator");
	        primaryStage.setScene(new Scene(root,1024,576)); // we can set default size here if we want but not now
	        
	        primaryStage.setOnCloseRequest(event -> {
                System.out.println("Window closing...");
                controller.stopSimulation(); 
            });
	        
	        primaryStage.show();
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }
}