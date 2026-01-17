package view;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.SimulationManager;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controller.MainController;



/**
 * The main entry point for the Traffic Simulator Application (JavaFX).
 * <p>
 * This class extends {@link Application} and is responsible for:
 * <ul>
 * <li>Loading the FXML user interface layout.</li>
 * <li>Setting up the primary stage (window) and scene.</li>
 * <li>Handling application lifecycle events (start and close).</li>
 * </ul>
 * </p>
 * @author pth
 * @version 1.0
 */
public class MainGUI extends Application {
	private static final String FXML_VIEW = "/gui/MainView2.fxml"; 
	private static final Logger logger = LogManager.getLogger(SimulationManager.class);
	public static int windowWidth = 1024;
	public static int windowHeight = 576;
	
    @FXML private static BorderPane borderPaneContainer;
    @FXML private static HBox simulationHeader;
    @FXML private static TitledPane bottomLogArea;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Starting Traffic Simulator Application...");
        
        URL fxmlUrl = getClass().getResource(FXML_VIEW);
        
        if (fxmlUrl == null) {
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

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be launched through deployment artifacts,
     * e.g., in IDEs with limited FX support.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}