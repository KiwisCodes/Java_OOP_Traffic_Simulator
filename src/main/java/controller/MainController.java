package controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

// Model & View Imports
import model.SimulationManager;
import model.infrastructure.MapManger;
import model.vehicles.VehicleManager;
import view.Renderer;
import util.CoordinateConverter; // Ensure this is imported from your util/view package

// Java Imports
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MainController {
	
	@FXML private HBox topHbox;

    // --- FXML View Elements ---
    @FXML private ScrollPane leftControlPanel;
//    @FXML private ScrollPane mapScrollPane;
    @FXML private StackPane rootStackPane;

    // Simulation Control
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button stepButton;

    // Vehicle Actions
    @FXML private TextField vehicleIdField;
    @FXML private TextField routeIdField;
    @FXML private Button injectVehicleButton;
    @FXML private Button setVehicleSpeedButton;
    @FXML private TextField vehicleSpeedField;
    @FXML private Button setVehicleColorButton;
    @FXML private TextField vehicleColorField;

    // Traffic Light Actions
    @FXML private TextField trafficLightIdField;
    @FXML private Button setRedPhaseButton;
    @FXML private Button setGreenPhaseButton;
    @FXML private Button resumeAutoButton;
    @FXML private Button setPhaseDurationButton;
    @FXML private TextField phaseDurationField;
    @FXML private CheckBox adaptiveTrafficCheck;

    // Filtering
    @FXML private TextField filterColorField;
    @FXML private TextField filterMinSpeedField;
    @FXML private TextField filterEdgeField;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;

    // Stress Testing
    @FXML private TextField stressEdgeField;
    @FXML private TextField stressCountField;
    @FXML private Button stressTestButton;

    // Sumo-GUI Integration
    @FXML private TextField pathToSumocfgFile;
    @FXML private TextField pathToSumoGui; 
    @FXML private Button insertSumocfgButton;
    @FXML private Button startSumoGuiButton;

    // Live Statistics
    @FXML private Label simStepLabel;
    @FXML private Label vehicleCountLabel;
    @FXML private Label avgSpeedLabel;
    @FXML private Label avgTravelTimeLabel;
    @FXML private Label congestionLabel;
    @FXML private Button showChartsButton;

    // Data Export
    @FXML private CheckBox exportFilterCheck;
    @FXML private Button exportCsvButton;
    @FXML private Button exportPdfButton;

    // Map & Log
    @FXML private AnchorPane mapAnchorPane;
    @FXML private StackPane rightMapStackPane;
    @FXML private Group rightMapPaneGroup;
    @FXML private Pane vehiclePane;
    @FXML private Pane baseMapPane;
    @FXML private Pane lanePane;     // Static roads go here
    @FXML private Pane junctionPane;
    @FXML private Pane routePane;
    @FXML private Pane carPane;      // Dynamic cars go here
    @FXML private Pane busPane;
    @FXML private Pane truckPane;
    @FXML private Pane bikePane;
    @FXML private Label logLabel;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button resetViewButton;
    @FXML private ToggleButton toggle3DButton;
    @FXML private TitledPane bottomLogArea;

    // --- Logic & State ---
    private SimulationManager simManager;
    private Renderer renderer; 
    private CoordinateConverter converter;
    
    // --- THREAD MANAGEMENT ---
    // 1. UI Thread: Handled by JavaFX & AnimationTimer
    private AnimationTimer uiLoop; 
    
    // 2. Background Threads: Handled by ExecutorService
    // Pool size 2: One for sumo connection and stuff, One for Statistics, by doing this we wont freeze the GUI while doing stats and con
    private ExecutorService threadPool; 
    private final int NUMBER_OF_THREADS = 2; 
    
    // Flags
    private volatile boolean isSimulationRunning = false;

    // --- Visualization ---
    private MapInteractionHandler mapInteractionHandler;


    public MainController() {
        this.simManager = new SimulationManager();  
        this.converter = new CoordinateConverter(); 
        this.renderer = new Renderer(); 
        this.threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }
    
    // main entry point if running standalone (optional)
    public static void main(String[] args) {
    	//guys if you dont want to open the gui you can start the code here
    }

    @FXML
    public void initialize() {
        log("Controller initialized. Waiting to start...");
        this.mapInteractionHandler = new MapInteractionHandler();
        
        
        
    }


    @FXML private void startSimulation() {
        log("Attempting to connect to SUMO...");
        boolean connected = this.simManager.startConnection();

        if (connected) {
            log("Connected! Preparing simulation...");
            isSimulationRunning = true;
            
            //we make instance for the rest of the uninstantiated attributes;
            MapManger sumoMap = this.simManager.getSumoMap();

            
            
            
            
            //1. thread 2: We should use this background thread for the simulationManager ---
            threadPool.submit(() -> {
                log("Simulation Thread Started.");
                while (isSimulationRunning) {
                    try {
                        simManager.step(); 
                        Thread.sleep(100); //this thread stop for 0.1 second before moving on
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break; 
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            //2. thread 3: We use this thread for running the statisticManager ---
            //But not yet
//            threadPool.submit(() -> {
//                log("Stats Thread Started.");
//                while (isSimulationRunning) {
//                    try {
//                        // Calculate stats less frequently (every 1 second)
//                        Thread.sleep(1000);
//                        
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        break;
//                    }
//                }
//            });

            //3. thread 1: for the javafx
            startUiLoop();
            
            startButton.setDisable(true); // Prevent double start
        } else {
            log("Failed to connect to SUMO.");
        }
    }
    
    private void startUiLoop() {
        uiLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // this func runs on the javafx UI thread, which is always the main one
            	/* AI explanation:
 * handle(long now): 
 * This method is called automatically by JavaFX roughly 60 times per second (depending on your monitor's refresh rate).

Why it's special: Code running inside handle() is executed on the JavaFX Application Thread. 
This is the only thread allowed to modify UI elements (like moving a Circle or changing a Label text).
            	 */
                updateView();
            }
        };
        uiLoop.start();
    }

    private void updateView() {
    	
    }
    
    
    public void log(String message) {
        System.out.println(message);
        if (logLabel != null) {
            // This Platform.runLater() thing comes from the javafx thing
            Platform.runLater(() -> logLabel.setText(message + "\n" + logLabel.getText()));
        }
    }


    public void stopSimulation() {
        System.out.println("Stopping simulation...");
        isSimulationRunning = false;
        if (uiLoop != null) {
            uiLoop.stop();
        }
        if (threadPool != null) {
            threadPool.shutdownNow(); 
        }
        if (simManager != null) {
            simManager.stopSimulation();
        }
    }
    
    //other not yet functions (these are connected to the buttons of the GUI)
    @FXML private void pauseSimulation() {}
    @FXML private void stepSimulation() {}
    @FXML private void injectVehicle() {}
    @FXML private void startSumoGUI() {}
    @FXML private void insertSumoConfigFile() {}
    @FXML private void applyFilter() {}
    @FXML private void clearFilter() {}
    @FXML private void runStressTest() {}
}