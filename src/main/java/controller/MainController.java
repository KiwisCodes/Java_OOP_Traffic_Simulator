package controller;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

//Charts
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Map;

 //Model & View Imports
import model.SimulationManager;
import model.StatisticsManager;
import model.infrastructure.LaneClass;
import model.infrastructure.MapManager;
import model.infrastructure.TrafficlightManager;
import model.infrastructure.TrafficlightObject;
import model.vehicles.VehicleClass;
import model.vehicles.VehicleManager;
import view.Renderer;
import util.CoordinateConverter; // Ensure this is imported from your util/view package
import util.ColorConverter;

// Java Imports
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.tudresden.sumo.objects.SumoColor;
import java.util.HashMap;
import de.tudresden.ws.container.SumoPosition2D; 
import de.tudresden.sumo.objects.SumoColor;    
import javafx.animation.AnimationTimer;
import data.SimulationState;

import data.SimulationQueue;
import data.SimulationState;
import view.ChartWindow;

public class MainController {
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
    @FXML private TitledPane injectionPane;
//    @FXML private RadioButton carRadio;
//    @FXML private RadioButton bikeRadio;          
    @FXML private ToggleGroup vehicleTypeGroup;   
    @FXML private TextField firstEdgeField;       
    @FXML private TextField secondEdgeField;      
    @FXML private ColorPicker injectVehicleColorPickerButton; 
    @FXML private Slider injectVehicleSpeedSlider;
    @FXML private Button injectionCarButton;
    @FXML private Button injectionBikeButton;
    @FXML private Button injectionBusButton;
    @FXML private Button injectionPedestrianButton;
    @FXML private Button injectionSelectedVehicleTypeButton;

    // Traffic Light Actions
    @FXML private TitledPane trafficLightControlPane;
    @FXML private TextField trafficLightIdField;
    @FXML private Button setRedPhaseButton;
    @FXML private Button setYellowPhaseButton;
    @FXML private Button setGreenPhaseButton;
    @FXML private Button resumeAutoButton;
    @FXML private Button setTrafficLightColorandorDurationButton;
    @FXML private TextField phaseDurationField;
    @FXML private Button switchTrafficLightPhaseButton;
    @FXML private Button selectedColorButton = null; // keep track of which button is selected
    private Consumer<TrafficlightObject> trafficLightClickHandler;
    private TrafficlightObject currentTrafficLightLink;

    // Filtering KHOA
    @FXML private TitledPane filterPane;
    @FXML private CheckBox filterByColorCheck;
    @FXML private CheckBox filterBySpeedCheck;
    @FXML private VBox colorFilterControlsVBox;        // New FXML for Color section VBox
    @FXML private VBox dynamicColorCheckBoxContainer;  // **THE VBOX WHERE CHECKBOXES ARE INJECTED**
    @FXML private VBox speedFilterControlsVBox;        // New FXML for Speed section VBox
    @FXML private Slider filterMaxSpeedSlider;
    @FXML private Button applyFilterButton; 
    @FXML private Button clearFilterButton;
    
    

    // Stress Testing
    @FXML private TitledPane stressTestPane;
    @FXML private Button stressTestButton;
//    @FXML private RadioButton carRadio1;
//    @FXML private RadioButton bikeRadio1;
    @FXML private ToggleGroup vehicleTypeGroup1;
    @FXML private TextField firstEdgeField1;
    @FXML private TextField secondEdgeField1;
    @FXML private ColorPicker injectVehicleColorPickerButton1;
    @FXML private Slider injectVehicleSpeedSlider1;
    @FXML private Slider numberOfVehicleSlider1;
    @FXML private Button stressTestCarButton;
    @FXML private Button stressTestBikeButton;
    @FXML private Button stressTestBusButton;
    @FXML private Button stressTestPedestrianButton;
    @FXML private Button stressTestSelectedVehicleTypeButton;
    
    

    // Sumo-GUI Integration
    @FXML private TextField pathToSumocfgFileField;
    @FXML private TextField pathToSumoGuiField; 
    @FXML private Button loadSumoPathButton;
    @FXML private Button loadSumoConfigButton;

    // Live Statistics
    @FXML private Label simStepLabel;
    @FXML private Label vehicleCountLabel;
    @FXML private Label avgSpeedLabel;
    @FXML private Label avgTravelTimeLabel;
    @FXML private Label congestionLabel;
    @FXML private Button showChartsButton;

    // Data Export
    @FXML private Button exportVehiclesCsvButton;
    @FXML private Button exportEdgesCsvButton;
    @FXML private Button exportPdfButton;
    @FXML private CheckBox edgeFilterCheckbox;
    @FXML private Slider edgeSpeedSlider;
    @FXML private Slider edgeDensitySlider;
    private boolean isExportingVehicleCSV;
    private boolean isExportingEdgeCSV;
    private boolean isExportingPDF;
    private String reportPath;

    // Map & Log
    @FXML private AnchorPane centerMapAnchorPane;
    @FXML private StackPane centerMapStackPane;
    @FXML private Group centerMapPaneGroup;
    @FXML private Pane vehiclePane;
    @FXML private ScrollPane bottomLogScrollPane;
    private MapManager mapManager;
    @FXML private Pane baseMapPane;
    @FXML private Pane lanePane;     
    @FXML private Pane junctionPane;
    @FXML private Pane trafficLightPane;
    @FXML private Pane routePane;
    @FXML private Label logLabel;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button resetViewButton;
    @FXML private ToggleButton toggle3DButton;
    @FXML private TitledPane bottomLogArea;
    private LinkedList<String> logHistory = new java.util.LinkedList<>();
    private static final int MAX_LOG_LINES = 100;


//    Logic & State
    private SimulationManager simManager;
    private StatisticsManager statsManager;
    private TrafficlightManager trafficLightManager;
    private Renderer renderer; 
    private ChartWindow chartWindow;
    
   
//    Thread
    private AnimationTimer uiLoop; 
    private ExecutorService threadPool; 
    private final int NUMBER_OF_THREADS = 4; //sim, stat, stress, extra things...
    
//    Flags
    private volatile static boolean isSimulationRunning = false;
    private volatile static int currentStep = 0;
    private volatile static boolean isPaused = false;

//     Visualization
    private Map<String, Shape> vehicleVisuals = new HashMap<>();
    private Group mapContentGroup; // Container for zooming/panning
    private MapInteractionHandler mapInteractionHandler;
    private SimulationQueue uiQueue;	
    private SimulationQueue statQueue;
    private SimulationQueue exportQueue;
    
//     Filtering KHOA
    private Map<Color, CheckBox> colorCheckBoxMap = new HashMap<>();
    private List<Color> realColors = new ArrayList<>();
    private List<String> filteredVehicleIDs = new ArrayList<>();
    private boolean isFilterCurrentlyApplied = false;
    
//     Initialization
    public MainController() {
		this.uiQueue = new SimulationQueue(2);
		this.statsManager = new StatisticsManager();
        this.simManager = new SimulationManager(uiQueue, this.statsManager);
        this.renderer = new Renderer();
        this.threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        this.statQueue = new SimulationQueue(2);
        this.exportQueue = new SimulationQueue(2);
    }
    
    // Main entry point if running stand alone (optional)
    public static void main(String[] args) {
        // JavaFX launching logic usually goes in MainGUI.java
    }

    @FXML
    public void initialize() {
    	
        log("Controller initialized. Waiting to start...");
        this.mapInteractionHandler = new MapInteractionHandler(centerMapStackPane, centerMapPaneGroup);
        if (injectionPane != null) {
            injectionPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
            	updateLaneVisuals();
                if (isNowExpanded) {
                    if (stressTestPane != null && stressTestPane.isExpanded()) {
                        stressTestPane.setExpanded(false);
                    }
                }
            });
        }
        
        if (stressTestPane != null) {
            stressTestPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
            	updateLaneVisuals();
                if (isNowExpanded) {
                    if (injectionPane != null && injectionPane.isExpanded()) {
                        injectionPane.setExpanded(false);
                    }
                }
            });
        }
//        if (vehicleTypeGroup != null) {
//            vehicleTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
//            	updateLaneVisuals();
//                if (firstEdgeField != null) firstEdgeField.clear();   
//                if (secondEdgeField != null) secondEdgeField.clear(); 
//            });
//        }
//        if (vehicleTypeGroup1 != null) {
//            vehicleTypeGroup1.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
//            	updateLaneVisuals();
//            	if (firstEdgeField1 != null) firstEdgeField1.clear();   
//                if (secondEdgeField1 != null) secondEdgeField1.clear();
//            });
//        }
//        if (this.injectionSelectedVehicleTypeButton != null) {
//        	
//        }
        
        
        
        disableButtons(true);
        this.chartWindow = new ChartWindow();
        
//        showChartsButton.setOnAction(e -> this.chartWindow.show());

//        setRedPhaseButton.setOnAction(e -> toggleColorButton(setRedPhaseButton));
//        setYellowPhaseButton.setOnAction(e -> toggleColorButton(setYellowPhaseButton));
//        setGreenPhaseButton.setOnAction(e -> toggleColorButton(setGreenPhaseButton));
        
//        this.carRadio.setOnMouseClicked(e -> {
//        	this.firstEdgeField.clear();
//        	this.secondEdgeField.clear();
//        });
//        this.bikeRadio.setOnMouseClicked(e -> {
//        	this.firstEdgeField.clear();
//        	this.secondEdgeField.clear();
//        });
//        this.carRadio1.setOnMouseClicked(e -> {
//        	this.firstEdgeField1.clear();
//        	this.secondEdgeField1.clear();
//        });
//        this.bikeRadio1.setOnMouseClicked(e -> {
//        	this.firstEdgeField1.clear();
//        	this.secondEdgeField1.clear();
//        });
        
     // 1. Assign the pointers to the Car buttons by default
        this.injectionSelectedVehicleTypeButton = injectionCarButton;
        this.stressTestSelectedVehicleTypeButton = stressTestCarButton;

        // 2. Apply the CSS styling (which you already have)
        this.injectionCarButton.getStyleClass().add("selected-button");
        this.stressTestCarButton.getStyleClass().add("selected-button");
        
        String projectPath = System.getProperty("user.dir");
        reportPath = projectPath + File.separator + "reports";
        File reportDir = new File(reportPath);
        if (!reportDir.exists()) {
            boolean created = reportDir.mkdirs();
            if (created) log("Created new reports directory: " + reportPath);
        }
        if (exportVehiclesCsvButton != null) {
            exportVehiclesCsvButton.setOnAction(e -> { 
                log("Exporting Vehicle CSV...");
                isExportingVehicleCSV = true;
            });
        }

        if (exportEdgesCsvButton != null) {
            exportEdgesCsvButton.setOnAction(e -> {
                log("Exporting Edge CSV...");
                isExportingEdgeCSV = true;
            });
        }

        if (exportPdfButton != null) {
            exportPdfButton.setOnAction(e -> {
                log("Exporting PDF Report...");
                isExportingPDF = true;
            });
        }
    }

    @FXML 
    private void startSimulation() {
        this.startButton.setDisable(true); // Prevent double start
        log("Attempting to connect to SUMO...");
        boolean connected = this.simManager.startConnection();

        if (connected) {
            log("Connected! Preparing simulation...");
            isSimulationRunning = true;
            disableButtons(false);
            MapManager mapManager = this.simManager.getMapManager();
            this.trafficLightManager = this.simManager.getTrafficlightManager();
            this.renderer.setConverter(mapManager);
            

            Consumer<LaneClass> laneClickHandler = (selectedLane) -> {
                if (selectedLane == null) return;

                // get information of Lane 
                String laneId = selectedLane.getId();
                
                // Lấy edgeId từ laneId (ví dụ: "edge1_0" -> "edge1") như code hiện tại của bạn
                String edgeId = laneId.substring(0, laneId.indexOf("_")); 

                // 2. Xác định trạng thái các Menu và lấy Mode tập trung
                String currentMode = getCurrentSelectedMode();
                boolean isInjecting = (injectionPane != null && injectionPane.isExpanded());
                boolean isStressTesting = (stressTestPane != null && stressTestPane.isExpanded());

                if (isInjecting) {
                    // --- LOGIC CHO INJECTION PANE ---
                    // Sử dụng currentMode đã lấy từ getCurrentSelectedMode()
                    if (!selectedLane.isVehicleAllowed(currentMode)) {
                        log("Warning: This lane does not allow " + currentMode);
                        return; 
                    }

                    if (firstEdgeField.getText().isEmpty()) {
                        firstEdgeField.setText(edgeId);
                        log("Selected First Edge (Injection): " + edgeId);
                    } else if (secondEdgeField.getText().isEmpty()) {
                        secondEdgeField.setText(edgeId);
                        log("Selected Second Edge (Injection): " + edgeId);
                    } else {
                        firstEdgeField.setText(edgeId);
                        secondEdgeField.clear();
                        log("Selected Another First Edge (Injection): " + edgeId);
                    }

                } else if (isStressTesting) {
                    // --- LOGIC CHO STRESS TEST PANE ---
                    // Sử dụng currentMode đã lấy từ getCurrentSelectedMode()
                    if (!selectedLane.isVehicleAllowed(currentMode)) {
                        log("Warning: This lane does not allow " + currentMode);
                        return; 
                    }

                    if (firstEdgeField1.getText().isEmpty()) {
                        firstEdgeField1.setText(edgeId);
                        log("Selected First Edge (Stress Test): " + edgeId);
                    } else if (secondEdgeField1.getText().isEmpty()) {
                        secondEdgeField1.setText(edgeId);
                        log("Selected Second Edge (Stress Test): " + edgeId);
                    } else {
                        firstEdgeField1.setText(edgeId);
                        secondEdgeField1.clear();
                        log("Selected Another First Edge (Stress Test): " + edgeId);
                    }   

                } 
                else {
                    // --- CHẾ ĐỘ KHÁM PHÁ (Discovery Mode) ---
                    // Khi không có menu nào mở, chỉ hiện ID lên log như bạn muốn
                    log("Edge ID: " + edgeId + " | Lane ID: " + laneId);
                }
            };
            
            trafficLightClickHandler = (trafficLightLink) -> {
                if (trafficLightControlPane != null && trafficLightControlPane.isExpanded()) {
                    	trafficLightIdField.setText(trafficLightLink.get_link_id().toString());
                    	this.currentTrafficLightLink = trafficLightLink;
                    log("Selected Traffic Light: " + trafficLightLink.get_link_id().toString());   
                } else {
                    log("Traffic Light ID: " + trafficLightLink.get_link_id().toString());
                }
            };
            
            
            
            this.renderer.renderLanes(
            	this.simManager.getMapManager().getLanes(),
                this.lanePane,
                laneClickHandler
            );
	         
            this.renderer.renderJunctions(
            		this.simManager.getMapManager().getJunctions(), 
            		this.junctionPane,
            		juncId -> log("Selected Junction: " + juncId)
            		);
            this.updateLaneVisuals();//this is to default the car option for both injection and stress test
            
	        log("Static Map drawn (Separated Car/Bike lanes)");

            threadPool.submit(() -> {
                log("Simulation Thread Started.");
                while (isSimulationRunning) {
                	if(this.simManager == null || simManager.getConnection().isClosed()) {
                		log("Connection lost, stopping loop");
                		break;
                	}
                	if(isPaused) {
            			try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
						}
            			continue;
                	}
                    try {
                    	this.simManager.step();
                    	if(this.simManager == null) {
                    		log("Simulation Manager is now null, maybe no more connection");
                    		isSimulationRunning = false;
                    		break;
                    	}
                    	SimulationState simulationState = this.simManager.getState();
                    	if(simulationState == null) {
                    		log("Current state is null, maybe no connection");
                    		isSimulationRunning = false;
                    		break;
                    	}
                        this.uiQueue.offerState(simulationState);
                        this.statQueue.offerState(simulationState);
                        this.exportQueue.offerState(simulationState);
                        currentStep++;
                    } catch (InterruptedException e) {
                        System.out.println("Simulation loop interrupted. Stopping safely.");
                        break;
                    } catch (Exception e) {
                        System.err.println("Unexpected error in simulation loop:");
                        e.printStackTrace();
                        break;
                    }
                }
            });


            threadPool.submit(() -> {
                log("Stats Thread Started.");
                while (isSimulationRunning) {
                	if(this.simManager.getConnection().isClosed()) {
                		log("Connection lost, stopping loop");
                		break;
                	}
                	if(isPaused) {
            			try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							e.printStackTrace();
						}
            			continue;
                	}
                	try {
                        SimulationState state = statQueue.takeState(); 
                        
                        if (state == null) continue;
                        Map<String, VehicleClass> statsData = state.getVehicles();
                        this.statsManager.step(statsData, currentStep);
                        double avgSpeed = this.statsManager.avgVehiclesSpeed(statsData);
                        Map<String, Integer> density = this.statsManager.calculateVehicleDensity(statsData);
                        Map<String, Integer> travelTimeDist = this.statsManager.calculateTravelTimeDistribution(statsData, 60);

                        this.chartWindow.updateData(currentStep, avgSpeed, density, travelTimeDist);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log("Stats Thread Interrupted");
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            startUiLoop();
        } else {
            log("Failed to connect to SUMO.");
        }
    }
    
    private String getCurrentSelectedMode() {
        // If Injection is open, check those buttons
        if (injectionPane != null && injectionPane.isExpanded()) {
            if (injectionSelectedVehicleTypeButton == injectionBikeButton) return "bicycle";
            if (injectionSelectedVehicleTypeButton == injectionBusButton) return "bus";
            if (injectionSelectedVehicleTypeButton == injectionPedestrianButton) return "pedestrian";
            return "passenger"; // Default for this pane
        } 

        // If Stress Test is open, check those buttons
        if (stressTestPane != null && stressTestPane.isExpanded()) {
            if (stressTestSelectedVehicleTypeButton == stressTestBikeButton) return "bicycle";
            if (stressTestSelectedVehicleTypeButton == stressTestBusButton) return "bus";
            if (stressTestSelectedVehicleTypeButton == stressTestPedestrianButton) return "pedestrian";
            return "passenger"; // Default for this pane
        }

        // Global fallback (Discovery mode or if something is null)
        return "passenger"; 
    }
    
    private void updateLaneVisuals() {
        // 1. Check if any Injection Pane is used:
        boolean isInjecting = injectionPane.isExpanded();
        boolean isStressTesting = stressTestPane.isExpanded();
        
        // If no Injection Pane is use, return to the original state of lane:
        if (!isInjecting && !isStressTesting) {
            for (Node node : lanePane.getChildren()) {
                if (node instanceof javafx.scene.shape.Shape shape) {
                    shape.setOpacity(1.0);
                    shape.setDisable(false);
                }
            }
            return;
        }

        // If a Injection pane is opened or Stress Test Pane, get what vehicle is choosen by user:
        String currentMode = getCurrentSelectedMode();

       // Update the color of lane base on what vehicle is being selected by user:
        for (Node node : lanePane.getChildren()) {
            if (node instanceof Shape shape) {
                LaneClass laneData = (LaneClass) shape.getUserData();
                if (laneData != null) {
                    if (laneData.isVehicleAllowed(currentMode)) {
                        shape.setOpacity(1.0);      // clear color
                        shape.setDisable(false);    // allow click
                    } else {
                        shape.setOpacity(0.15);     // blur color
                        shape.setDisable(true);     // cannot click
                    }
                }
            }
        }
    }
    
    
    private void startUiLoop() {
        uiLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateView();
            }
        };
        uiLoop.start();
    }

//    private void updateView() {
//
//    		SimulationState simulationState;
//		try {
//			simulationState = this.uiQueue.pollState();
//			if(simulationState == null) return;
//			// KHOA CODE UPDATE VIEW
//			this.refreshColorFilterUI(simulationState);
//			if (this.isFilterCurrentlyApplied) {
//	            this.filteredVehicleIDs.clear(); // Ensure list is cleared so it's not passed with old IDs
//				boolean filterColorActive = filterByColorCheck.isSelected();
//		        boolean filterSpeedActive = filterBySpeedCheck.isSelected();
//		        // --- 1. DETERMINE WHICH FILTER(S) TO APPLY ---
//		        if (filterColorActive || filterSpeedActive) {		            
//		            // Start the valid list with ALL vehicles (we will remove those that fail a filter)
//		            List<String> combinedValidIDs = new ArrayList<>(simulationState.getVehicles().keySet());
//		            
//		            // --- 2. APPLY COLOR FILTER (INTERSECTION) ---
//		            if (filterColorActive) {
//		                // A temporary list to hold the IDs that ONLY match the color criteria
//		                List<String> colorValidIDs = new ArrayList<>();
//		                
//		                // Get the colors the user selected
//		                List<javafx.scene.paint.Color> selectedFXColors = new ArrayList<>();
//		                for (Map.Entry<javafx.scene.paint.Color, CheckBox> entry : colorCheckBoxMap.entrySet()) {
//		                    if (entry.getValue().isSelected()) {
//		                        selectedFXColors.add(entry.getKey());
//		                    }
//		                }
//		                
//		                // Perform the color filter logic (using your existing simManager function)
//		                for (javafx.scene.paint.Color fxColor : selectedFXColors) {
//		                    int[] rgba = fxColorToRgbaInts(fxColor);
//		                    // You must update getIDColor to accept SimulationState and not call vehicleManager
//		                    List<String> idsForColor = this.simManager.getIDColor(rgba[0], rgba[1], rgba[2], 255, simulationState);
//		                    
//		                    // Union the results of different selected colors
//		                    for (String id : idsForColor) {
//		                        if (!colorValidIDs.contains(id)) {
//		                            colorValidIDs.add(id);
//		                        }
//		                    }
//		                }
//		                
//		                // CRITICAL STEP: INTERSECT (AND) the current list with the new color list
//		                combinedValidIDs.retainAll(colorValidIDs);
//		            } 
//		            
//		            // --- 3. APPLY SPEED FILTER (INTERSECTION) ---
//		            if (filterSpeedActive) {
//		                double maxSpeedCriteria = filterMaxSpeedSlider.getValue();
//		                
//		                // A temporary list to hold the IDs that ONLY match the speed criteria
//		                List<String> speedValidIDs = new ArrayList<>();
//		                
//		                // You must update getIDSpeed to accept SimulationState and not call vehicleManager
//		                speedValidIDs.addAll(this.simManager.getIDSpeed(maxSpeedCriteria, simulationState));
//		                
//		                // CRITICAL STEP: INTERSECT (AND) the current list with the new speed list
//		                combinedValidIDs.retainAll(speedValidIDs);
//		            }
//		            
//		            // --- 4. Finalize the list and Log ---
//		            this.filteredVehicleIDs.addAll(combinedValidIDs);
//
//		            if (this.filteredVehicleIDs.isEmpty()) {
//		                log("Filter applied: 0 vehicles visible.");
//		            } else {
//		                 log("Filter applied: " + this.filteredVehicleIDs.size() + " vehicles visible.");
//		            }
//
//		        } else {
//		            // No filters are active
//		            this.isFilterCurrentlyApplied = false;
//		            this.filteredVehicleIDs.clear(); // Ensure list is cleared so it's not passed with old IDs
//		        }
//			}
//			this.renderer.renderVehicles(vehiclePane, simulationState.getVehicles(),this.filteredVehicleIDs,this.isFilterCurrentlyApplied);
//			// KHOA CODE UPDATE VIEW
//
////			this.renderer.renderVehicles(vehiclePane, simulationState.getVehicles());
//			this.renderer.renderTrafficLights(trafficLightPane, simulationState.getTrafficLights(), trafficLightClickHandler);
//			
//			int currentVehicleCount = simulationState.getVehicles().size();
//			updateCurrentStep();
//			updateCurrentVehicleCount(currentVehicleCount);
//			
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			System.err.print(e.getMessage());
//		}
//    }
    
    private void updateView() {
        SimulationState simulationState;
        try {
            // 1. Get the next state from the queue
            simulationState = this.uiQueue.pollState();
            if (simulationState == null) return;

            // 2. Refresh the dynamic Color Checkboxes in the UI
            this.refreshColorFilterUI(simulationState);

            if (this.isFilterCurrentlyApplied) {
                // --- REFACTORED FILTER LOGIC ---
                
                // Capture UI state once (Optimization: don't call getters inside the loop)
                boolean filterColorActive = filterByColorCheck.isSelected();
                boolean filterSpeedActive = filterBySpeedCheck.isSelected();
                double maxSpeedCriteria = filterMaxSpeedSlider.getValue();

                // Collect only the colors that are checked by the user
                java.util.Set<javafx.scene.paint.Color> selectedFXColors = colorCheckBoxMap.entrySet().stream()
                        .filter(entry -> entry.getValue().isSelected())
                        .map(Map.Entry::getKey)
                        .collect(java.util.stream.Collectors.toSet());

                // 3. Define the "Filter Rule" (Predicate)
                // 'v' represents an individual VehicleClass object in the stream
                Predicate<VehicleClass> filterRule = v -> {
                    // Check Speed: Pass if filter is off OR if vehicle speed is within limit
                    boolean speedPass = !filterSpeedActive || v.getSpeed() <= maxSpeedCriteria;

                    // Check Color: Pass if filter is off OR if vehicle color matches selection
                    boolean colorPass = true;
                    if (filterColorActive && !selectedFXColors.isEmpty()) {
                        // Use your Util class to get a JavaFX-compatible color
                    	SumoColor sumoColor = v.getColor();
                    	if(sumoColor != null) {
                    		Color vehicleFXColor = ColorConverter.toFXColor(sumoColor);
                    		colorPass = selectedFXColors.contains(vehicleFXColor);                    		
                    	}
                    }
                    else {
                    	colorPass = false;
                    }

                    return speedPass && colorPass; // Intersection Logic (AND)
                };

                // 4. Ask the manager to execute the filter
                this.filteredVehicleIDs = this.simManager.getFilteredVehicleIDs(filterRule, simulationState);

                log("Filter applied: " + this.filteredVehicleIDs.size() + " vehicles visible.");
                
            } else {
                // No filters active: Clear the list
                this.filteredVehicleIDs.clear();
            }

            // 5. Tell the renderer what to draw (it now knows if filters are active)
            this.renderer.renderVehicles(
                vehiclePane, 
                simulationState.getVehicles(), 
                this.filteredVehicleIDs, 
                this.isFilterCurrentlyApplied
            );

            // 6. Update Traffic Lights and Stats
            this.renderer.renderTrafficLights(trafficLightPane, simulationState.getTrafficLights(), trafficLightClickHandler);
            
            int currentVehicleCount = simulationState.getVehicles().size();
            updateCurrentStep();
            updateCurrentVehicleCount(currentVehicleCount);
            
            // 7. Export CSV/PDF
            if(isExportingVehicleCSV) {
	            	Platform.runLater(() -> {
	            		// since there will always be some good speed, even at default, we let filter = True
	            		simManager.generateReports(reportPath, simulationState.getVehicles(), "VEHICLE", this.filteredVehicleIDs, currentStep, false, 0, 0);
	            });
            		isExportingVehicleCSV = false;
            }
            
            if(isExportingEdgeCSV) {
	            	Platform.runLater(() -> {
	            		boolean edgeFilter = edgeFilterCheckbox.isSelected();
	            	    double maxSpeed = edgeSpeedSlider.getValue();
	            	    int minDensity = (int) edgeDensitySlider.getValue();
	            	    simManager.generateReports(reportPath, simulationState.getVehicles(), "EDGE", this.filteredVehicleIDs, currentStep, edgeFilter, maxSpeed, minDensity);
	            });
            		isExportingEdgeCSV = false;
            }
            
            if(isExportingPDF) {
            		isExportingPDF = false;
            		threadPool.submit(() -> {
                		try {          
                			// since there will always be some good speed, even at default, we let filter = True
                			simManager.generateReports(reportPath, simulationState.getVehicles(), "PDF", this.filteredVehicleIDs, currentStep, false, 0 ,0);
                			Platform.runLater(() -> log("✅ PDF Saved to Desktop!"));
                    } catch (Throwable ex) {
                    		System.err.println("CRITICAL THREAD ERROR:"); 
                    		ex.printStackTrace();      
                    		Platform.runLater(() -> log("❌ CRASH: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()));
                    	}
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.print("UI Update Interrupted: " + e.getMessage());
        }
    }
    

    private void log(String message) {
        // 1. Ensure we update on the JavaFX Application Thread
        Platform.runLater(() -> {
            // 2. Add the new message with a timestamp or prefix
            logHistory.add("> " + message);

            // 3. If we exceed the limit, remove the oldest line (the head)
            if (logHistory.size() > MAX_LOG_LINES) {
                logHistory.removeFirst();
            }

            // 4. Join the lines back together and update the Label
            String joinedLog = String.join("\n", logHistory);
            logLabel.setText(joinedLog);

            // 5. Auto-scroll to the bottom of the ScrollPane
            bottomLogScrollPane.setVvalue(1.0);
        });
    }
    
    private void updateCurrentStep() {
//    	System.out.println(currentStep);
    	if(simStepLabel != null) {
    		simStepLabel.setText("" + currentStep);
    	}
    }
    
    private void updateCurrentVehicleCount(int currentVehicleCount) {
//    	System.out.println(currentVehicleCount);
    	if(vehicleCountLabel != null) {
    		vehicleCountLabel.setText("" + currentVehicleCount);
    	}
    }
    
   


    public void stopSimulation() {
        System.out.println("Stopping simulation...");
        
        isSimulationRunning = false;
        
        if (uiLoop != null) {
            uiLoop.stop();
        }

        if (renderer != null) {
            renderer.clearVehicleCache();
        }
        if (vehiclePane != null) {
            Platform.runLater(() -> vehiclePane.getChildren().clear());
        }

        if (threadPool != null) {
            threadPool.shutdownNow(); 
            try {
                if (!threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                     System.out.println("Thread pool did not terminate gracefully");
                }
            } catch (InterruptedException e) {
                System.out.println("Shutdown interrupted");
            }
        }

        if (simManager != null) {
        	simManager.stopSimulation();
        }
    }
    
    private void disableButtons(boolean state) {
		this.pauseButton.setDisable(state);
        this.stepButton.setDisable(state);
        this.injectVehicleButton.setDisable(state);
        this.showChartsButton.setDisable(state);
        this.exportPdfButton.setDisable(state);
        this.exportVehiclesCsvButton.setDisable(state);
        this.exportEdgesCsvButton.setDisable(state);
        this.stressTestButton.setDisable(state);
    }

    @FXML private void pauseSimulation() {
    	if(pauseButton == null) return;
    	isPaused = !isPaused;
    	if(isPaused == true) {
    		log("Simulation Paused");
			pauseButton.setText("Resume");
    	}
    	else {
    		log("Resume Simulation");
    		pauseButton.setText("⏸ Pause");
    	}
    }
    
    @FXML private void stepSimulation() {
    	if(!isPaused) {
    		log("Please pause the simulation first");
    		return;
    	}
    	this.simManager.step();
    	currentStep++;
    	SimulationState newSimulationState = this.simManager.getState();
    	try {
			this.uiQueue.offerState(newSimulationState);
			this.statQueue.offerState(newSimulationState);
			log("Step Forward -> " + currentStep);
		} catch (InterruptedException e) {
		    System.out.println("Simulation loop interrupted. Stopping safely.");
		    return;
		} catch (Exception e) {
		    System.err.println("Unexpected error in simulation loop:");
		    e.printStackTrace();
		    return;
		}
    	
    }
    
    @FXML private void loadSumoPath() {
    	if(this.simManager.setSumoBinary(this.pathToSumoGuiField)) {
    		log("Successfully set path to sumo or sumo-gui");
    		this.loadSumoPathButton.setDisable(true);
    	}
    	else {
    		log("Set sumo or sumo-gui path fail");
    	}
    }
    
    @FXML private void injectVehicle() {
    	if(this.firstEdgeField.getText().isEmpty() || this.secondEdgeField.getText().isEmpty()) {
    		log("Please choose 2 edges please");
    		return;
    	}
    	
    	String firstEdgeId = this.firstEdgeField.getText();
    	String secondEdgeId = this.secondEdgeField.getText();
    	String vehicleType = null;
    	double speed = 10;
    	if(this.injectVehicleSpeedSlider!=null) {
    		speed=this.injectVehicleSpeedSlider.getValue();
    	}
    	
    	
//    	if(this.carRadio.isSelected()) vehicleType = "DEFAULT_VEHTYPE";
//    	else if(this.bikeRadio.isSelected()){
//            vehicleType = "DEFAULT_BIKETYPE";
//            speed = (speed>5) ?  5: speed;
//        }
    	
    	if(this.injectionSelectedVehicleTypeButton == this.injectionCarButton) {
    		vehicleType = "DEFAULT_VEHTYPE";
    	}
    	else if(this.injectionSelectedVehicleTypeButton == this.injectionBikeButton) {
    		vehicleType = "DEFAULT_BIKETYPE";
            speed = (speed>5) ?  5: speed;
    	}
    	else if(this.injectionSelectedVehicleTypeButton == this.injectionBusButton) {
    		vehicleType = "BUS";
    		speed = (speed>5) ?  5: speed;
    	}
    	else if(this.injectionSelectedVehicleTypeButton == this.injectionPedestrianButton) {
    		vehicleType = "DEFAULT_PEDTYPE";
            speed = (speed>4) ?  4: speed;
    	}
    	
        Color fxColor = injectVehicleColorPickerButton.getValue();
        SumoColor sumoColor = ColorConverter.toSumoColor(fxColor);
    	if(this.simManager.InjectVehicle(vehicleType, sumoColor, speed, firstEdgeId, secondEdgeId)) {
    		log("Injected vehicle");  
    		this.updateView(); // KHOA FILTERING
    	}
    	else {
    		log("Fail injecting vehicle");
    	}
    	this.firstEdgeField.clear();
    	this.secondEdgeField.clear();
    }
    
    /**
     * Switches the current traffic light to its next phase.
     *
     * If no traffic light is selected, the operation is aborted and a warning
     * message is logged.
     */
    @FXML private void switchTrafficLightPhase() {
    	if(this.trafficLightIdField.getText().isEmpty() || this.currentTrafficLightLink == null) {
    		log("Please choose a Traffic Light please");
    		return;
    	}
    	this.trafficLightManager.setCurrentPhaseDuration(this.currentTrafficLightLink, 0.0);
    	log("Switched to the next Phase");
		return;
}

    /**
     * Sets the traffic light color and/or phase duration based on user input.
     *
     * The method validates user selections and applies one of the following:
     * - Only phase duration
     * - Only signal color
     * - Both signal color and phase duration
     *
     * If traffic light id is missing or duration is invalid, the operation is aborted
     * and an explanatory message is logged.
     */
    @FXML private void setTrafficLightColorandorDuration() {
    	if(this.trafficLightIdField.getText().isEmpty() || this.currentTrafficLightLink == null) {
    		log("Please choose a Traffic Light please");
    		return;
    	}
    	if(this.phaseDurationField.getText().isEmpty() && this.selectedColorButton == null) {
    		log("Please choose a color and/or duration to set");
    		return;
    	}
    	else if(!this.phaseDurationField.getText().isEmpty() && this.selectedColorButton == null) {
    		boolean check_validity = false;
	    	try {
	    		double val = Double.parseDouble(this.phaseDurationField.getText());
	        if(val >= 0) {
	        		check_validity = true;
	        }
	    } catch (NumberFormatException e) {
	    		check_validity = false;
	    }
	    	if(!check_validity) {
	    		log("Duration must be non-negative double.");
	    		return;
	    	}
	    	else {
	    		this.trafficLightManager.setCurrentPhaseDuration(this.currentTrafficLightLink, Double.parseDouble(this.phaseDurationField.getText()));
	    		log("Duration of this phase is set to " + Double.parseDouble(this.phaseDurationField.getText()) + "s.");
	    	}
    	}
    	else if(this.phaseDurationField.getText().isEmpty() && this.selectedColorButton != null) {
    		if(this.selectedColorButton == setRedPhaseButton) {
    			this.trafficLightManager.setCurrentLightState(this.currentTrafficLightLink, 'r');
    			log("Color of Traffic Light is set to Red");
    		}
    		else if(this.selectedColorButton == setYellowPhaseButton) {
    			this.trafficLightManager.setCurrentLightState(this.currentTrafficLightLink, 'y');
    			log("Color of Traffic Light is set to Yellow");
    		}
    		else if(this.selectedColorButton == setGreenPhaseButton) {
    			this.trafficLightManager.setCurrentLightState(this.currentTrafficLightLink, 'G');
    			log("Color of Traffic Light is set to Green");
    		}
    	}
    	else {
    		boolean check_validity = false;
	    	try {
	    		double val = Double.parseDouble(this.phaseDurationField.getText());
	        if(val >= 0) {
	        		check_validity = true;
	        }
	    } catch (NumberFormatException e) {
	    		check_validity = false;
	    }
	    	if(!check_validity) {
	    		log("Duration must be non-negative double.");
	    		return;
	    	}
	    	if(this.selectedColorButton == setRedPhaseButton) {
    			this.trafficLightManager.setCurrentLightState(this.currentTrafficLightLink, 'r');
	    		this.trafficLightManager.setCurrentPhaseDuration(this.currentTrafficLightLink, Double.parseDouble(this.phaseDurationField.getText()));		    		
    			log("Color of Traffic Light is set to Red with duration of " + Double.parseDouble(this.phaseDurationField.getText()) + "s");
    		}
    		else if(this.selectedColorButton == setYellowPhaseButton) {
    			this.trafficLightManager.setCurrentLightState(this.currentTrafficLightLink, 'y');
	    		this.trafficLightManager.setCurrentPhaseDuration(this.currentTrafficLightLink, Double.parseDouble(this.phaseDurationField.getText()));		    			    			
    			log("Color of Traffic Light is set to Yellow with duration of " + Double.parseDouble(this.phaseDurationField.getText()) + "s");
    		}
    		else if(this.selectedColorButton == setGreenPhaseButton) {
    			this.trafficLightManager.setCurrentLightState(this.currentTrafficLightLink, 'G');
	    		this.trafficLightManager.setCurrentPhaseDuration(this.currentTrafficLightLink, Double.parseDouble(this.phaseDurationField.getText()));		    			    			
    			log("Color of Traffic Light is set to Green with duration of " + Double.parseDouble(this.phaseDurationField.getText()) + "s");
    		}
    	}
}
    
    /**
     * Toggles the selection state of a traffic light color button.
     *
     * If the given button is already selected, it is deselected.
     * Otherwise, the previous selection is cleared and the new button
     * becomes the active color selection.
     *
     * @param button color selection button to toggle
     */
//    @FXML private void toggleColorButton(Button button) {
//	    if (selectedColorButton == button) {
//	        button.getStyleClass().remove("selected-button");
//	        selectedColorButton = null;
//	    } else {
//	        if (selectedColorButton != null) {
//	            selectedColorButton.getStyleClass().remove("selected-button");
//	        }
//	        button.getStyleClass().add("selected-button");
//	        selectedColorButton = button;
//	    }
//}
    
    //pth modified
    @FXML
    private void toggleColorButton(ActionEvent event) {
        // Get the button that was clicked from the event
        Button button = (Button) event.getSource();

        if (selectedColorButton == button) {
            button.getStyleClass().remove("selected-button");
            selectedColorButton = null;
        } else {
            if (selectedColorButton != null) {
                selectedColorButton.getStyleClass().remove("selected-button");
            }
            button.getStyleClass().add("selected-button");
            selectedColorButton = button;
        }
    }
    
    @FXML
    private void toggleInjectionVehicleType(ActionEvent event) {
        // Get the button that was clicked from the event
        Button button = (Button) event.getSource();

        if (this.injectionSelectedVehicleTypeButton == button) {
//            button.getStyleClass().remove("selected-button");
//            this.injectionSelectedVehicleTypeButton = null;
            log("You must choose at least 1 type");
        } else {
            if (this.injectionSelectedVehicleTypeButton != null) {
            	this.injectionSelectedVehicleTypeButton.getStyleClass().remove("selected-button");
            }
            button.getStyleClass().add("selected-button");
            this.injectionSelectedVehicleTypeButton = button;
            if (firstEdgeField != null) firstEdgeField.clear();
            if (secondEdgeField != null) secondEdgeField.clear();
            updateLaneVisuals();
        }
    }
    
    @FXML
    private void toggleStressTestVehicleType(ActionEvent event) {
        // Get the button that was clicked from the event
        Button button = (Button) event.getSource();

        if (this.stressTestSelectedVehicleTypeButton == button) {
//            button.getStyleClass().remove("selected-button");
//            this.stressTestSelectedVehicleTypeButton = null;
        	log("You must choose at least 1 type");
        } else {
            if (this.stressTestSelectedVehicleTypeButton != null) {
            	this.stressTestSelectedVehicleTypeButton.getStyleClass().remove("selected-button");
            }
            button.getStyleClass().add("selected-button");
            this.stressTestSelectedVehicleTypeButton = button;
            if (firstEdgeField1 != null) firstEdgeField1.clear();
            if (secondEdgeField1 != null) secondEdgeField1.clear();

            // 4. Update the map visuals immediately
            updateLaneVisuals();
        }
    }
    
    @FXML private void zoomIn() {
    	this.mapInteractionHandler.handleZoomIn();
    }
    @FXML private void zoomOut() {
    	this.mapInteractionHandler.handleZoomOut();
    }
    @FXML private void resetView() {
    	this.mapInteractionHandler.handleResetView();
    }
    
    @FXML private void showCharts() {
    	this.chartWindow.show();
    }
    @FXML private void startSumoGUI() {}
    @FXML private void insertSumoConfigFile() {}
    // KHOA FILTERING CODE
    @FXML private void applyFilter() {
        
        // Clear previous filter state
    	filteredVehicleIDs.clear();
        
        // 2. Determine if ANY filter checkbox is selected
        boolean filterColorActive = filterByColorCheck.isSelected();
        boolean filterSpeedActive = filterBySpeedCheck.isSelected();
        
        // 3. Set the global flag based on the UI state
        // The filter is "applied" (ON) if either checkbox is ticked.
        this.isFilterCurrentlyApplied = filterColorActive || filterSpeedActive;
        
        // 4. Log the state (and check if any color checkboxes are actually selected)
        if (this.isFilterCurrentlyApplied) {
            log("Filter status: ON. Color=" + filterColorActive + ", Speed=" + filterSpeedActive);
        } else {
            log("Filter status: OFF (No criteria selected).");
        }
        
//        if (isPaused) updateView(); // Redraw immediately
    }


    @FXML private void clearFilter() {
        
        // Reset all UI controls to their default/cleared state
        if (filterByColorCheck != null) filterByColorCheck.setSelected(false);
        if (filterBySpeedCheck != null) filterBySpeedCheck.setSelected(false);
        this.filteredVehicleIDs.clear();
        this.isFilterCurrentlyApplied = false;
        // Reset all dynamically created color checkboxes
        for (CheckBox cb : colorCheckBoxMap.values()) {
            cb.setSelected(false);
        }
        
        // Reset slider to its maximum value (to show all speeds)
        if (filterMaxSpeedSlider != null) {
            filterMaxSpeedSlider.setValue(filterMaxSpeedSlider.getMax()); 
        }
        
        log("✅ All filter inputs cleared. Click 'Apply Filter' to finalize.");
        if (isPaused) updateView(); // Redraw immediately
    }
    
    // KHOA FILTERING CODE
    @FXML private void runStressTest() {
    	try {
			this.simManager.StressTest();
			// KHOA CODED THIS
//	        this.isFilterCurrentlyApplied = false; 
			log("Default Stress Test with " + " random cars with random Routes");
//			this.refreshColorFilterUI();
			this.updateView();
		} catch (Exception e) {
			System.err.print(e.getMessage());
			e.printStackTrace();
		}
    }
    
    @FXML 
    private void runStressTestOnSpecificEdges() {
    	clearFilter(); // KHOA CODED FILTERING
        if(this.firstEdgeField1.getText().isEmpty() || this.secondEdgeField1.getText().isEmpty()) {
            log("Please choose 2 edges first");
            return;
        }
        
        this.stressTestButton.setDisable(true);
        PauseTransition unlockTimer = new PauseTransition(Duration.seconds(20));
        unlockTimer.setOnFinished(e -> this.stressTestButton.setDisable(false));
        unlockTimer.play();

        final String firstEdgeId = this.firstEdgeField1.getText();
        final String secondEdgeId = this.secondEdgeField1.getText();

        double spd = (this.injectVehicleSpeedSlider1 != null) ? this.injectVehicleSpeedSlider1.getValue() : 5;
        String type = "DEFAULT_VEHTYPE";
        
        if(this.stressTestSelectedVehicleTypeButton == this.stressTestCarButton) {
    		type = "DEFAULT_VEHTYPE";
    	}
    	else if(this.stressTestSelectedVehicleTypeButton == this.stressTestBikeButton) {
    		type = "DEFAULT_BIKETYPE";
            spd = (spd>5) ?  5: spd;
    	}
    	else if(this.stressTestSelectedVehicleTypeButton == this.stressTestBusButton) {
    		type = "BUS";
    		spd = (spd>5) ?  5: spd;
    	}
    	else if(this.stressTestSelectedVehicleTypeButton == this.stressTestPedestrianButton) {
    		type = "DEFAULT_PEDTYPE";
            spd = (spd>3) ?  3: spd;
    	}

        final String vehicleType = type;
        final double speed = spd;
        final int totalVehicles = (int) this.numberOfVehicleSlider1.getValue();
        Color fxColor = injectVehicleColorPickerButton1.getValue();
        SumoColor sumoColor = ColorConverter.toSumoColor(fxColor);

        log("Starting Stress Test: Injecting " + totalVehicles + " vehicles...");

        threadPool.submit(() -> {
            int successCount = 0;
            
            for (int i = 0; i < totalVehicles; i++) {
            	
                if (!isSimulationRunning || Thread.currentThread().isInterrupted()) {
                    break; 
                }
                if (isPaused) {
                    i--; 
                    try { 
                        Thread.sleep(200); 
                    } catch (InterruptedException e) { 
                        break; 
                    }
                    continue;
                }

                try {
                    if(this.simManager == null || this.simManager.getConnection().isClosed()) {
                         break;
                    }

                    boolean success = this.simManager.InjectVehicle(vehicleType, sumoColor, speed, firstEdgeId, secondEdgeId);
                    
                    if (success) {
                        successCount++;
                        Platform.runLater(() -> log("Injected vehicle")); 
                    } else {
                        Platform.runLater(() -> log("Injection failed"));
                    }
                    Thread.sleep(200); 
                    
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt();
                    break; 
                } catch (Exception e) {
                    if (e.toString().contains("connection is closed")) {
                        break; 
                    }
                    e.printStackTrace(); 
                }
            }
            
            final int finalCount = successCount;
            Platform.runLater(() -> {
            	log("Stress Test Complete. Total: " + finalCount);
            	this.firstEdgeField1.clear();
            	this.secondEdgeField1.clear();
            });
        });
    }
    // KHOA FILTERING CODE
    public void refreshColorFilterUI(SimulationState state) {
        // Submit the task to the thread pool to avoid blocking the JavaFX thread or UI thread
        threadPool.submit(() -> {
            try {
                // Short delay might still be useful if injection takes a moment to register in SUMO
//                Thread.sleep(50); 
                
                // 1. Get the raw colors (AWT type assumed)
                List<Color> fxColorRGBA = this.simManager.getUniqueColors(state); 
                
                // 2. Convert and store
//                List<javafx.scene.paint.Color> newColors = convertAwtToFxColors(rawColors);
                
                // Optimization: Only update the UI if the color list has actually changed size
                if (fxColorRGBA.size() != this.realColors.size() || !fxColorRGBA.containsAll(this.realColors)) {
                    this.realColors = fxColorRGBA;
                    
                    // 3. Update the UI on the JavaFX Application Thread
                    Platform.runLater(() -> this.showColorsAsCheckboxes(this.realColors));
                }
                
            } catch (Exception e) {
                log("Error refreshing colors: " + e.getMessage());
                // In a real app, you might only print the stack trace for non-InterruptedException errors
            }
        });
    }
    
//    private List<javafx.scene.paint.Color> convertAwtToFxColors(List<java.awt.Color> awtColors) {
//        List<javafx.scene.paint.Color> fxColors = new ArrayList<>();
//        
//        for (java.awt.Color awtColor : awtColors) {
//            // Get the R, G, B components (0-255)
//            int r = awtColor.getRed();
//            int g = awtColor.getGreen();
//            int b = awtColor.getBlue();
//            
//            // Get the Alpha component (0-255) and convert to a double for Opacity (0.0 - 1.0)
//            double opacity = awtColor.getAlpha() / 255.0;
//            
//            // Create the new JavaFX Color object
//            javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(r, g, b, opacity);
//            
//            fxColors.add(fxColor);
//        }
//        return fxColors;
//    }
    
    private void showColorsAsCheckboxes(List<Color> colorsToShow) {
        // UI manipulation must always be done on the JavaFX Application Thread
        Platform.runLater(() -> {
            if (dynamicColorCheckBoxContainer == null) {
                log("Error: dynamicColorCheckBoxContainer is null. FXML load failed or ID mismatch.");
                return;
            }
            
            dynamicColorCheckBoxContainer.getChildren().clear();
            colorCheckBoxMap.clear();

            for (Color color : colorsToShow) {
                String webColor = ColorConverter.colorToWebString(color);
                String labelText = "Color: " + webColor; 

                CheckBox cb = new CheckBox(labelText);
                cb.setUserData(color); // Store the actual Color object
                // Style the text to be the color it represents
                cb.setStyle("-fx-text-fill: " + webColor + "; -fx-font-weight: bold;"); 

                dynamicColorCheckBoxContainer.getChildren().add(cb);
                colorCheckBoxMap.put(color, cb);
            }
            log("Filter UI Populated with " + colorsToShow.size() + " unique colors.");
        });
    }
//    private String colorToWebString(Color color) {
//        // Uses 255 * R/G/B values and formats them as a 6-digit hex string
//        return String.format("#%02X%02X%02X", 
//            (int) (color.getRed() * 255), 
//            (int) (color.getGreen() * 255), 
//            (int) (color.getBlue() * 255));
//    }
    
    
//    private int[] fxColorToRgbaInts(javafx.scene.paint.Color fxColor) {
//        int r = (int) (fxColor.getRed() * 255);
//        int g = (int) (fxColor.getGreen() * 255);
//        int b = (int) (fxColor.getBlue() * 255);
//        int a = 255;
//        return new int[] {r, g, b, a};
//    }
    
}