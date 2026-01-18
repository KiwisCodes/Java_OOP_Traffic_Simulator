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
import javafx.scene.shape.Shape;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.ToggleGroup;

//Charts
import javafx.util.Duration;

import java.util.Map;
import java.util.Set;

//Model & View Imports
import model.SimulationManager;
import model.StatisticsManager;
import model.infrastructure.LaneClass;
import model.infrastructure.MapManager;
import model.infrastructure.TrafficlightManager;
import model.infrastructure.TrafficlightClass;
import model.vehicles.MeansOfTransportation;
import view.Renderer;
import util.SumoException;
import util.ColorConverter;

// Java Imports
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.tudresden.sumo.objects.SumoColor;
import data.SimulationState;

import data.SimulationQueue;
import view.ChartWindow;

//Log
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main "brain" of the app that connects the SUMO sim with the JavaFX screen.
 * * This class handles all the buttons and sliders you see on the UI. It uses a few 
 * different threads to make sure the map renders smooth while the simulation 
 * runs in the backround. 
 * * Main jobs:
 * <ul>
 * <li>Starts and stops the SUMO conection.</li>
 * <li>Updates the map and vehicles using a Renderer class.</li>
 * <li>Handles mouse clicks for selecting lanes and traffic lights.</li>
 * <li>Calculates stats for the charts and exports PDF reports.</li>
 * <li>Lets users "inject" new cars or change light colors manualy.</li>
 * </ul>
 * * Its basically the middle-man between the data and the user.
 */
public class MainController {
    // FXML View Elements
    @FXML private ScrollPane leftControlPanel;
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
    private Consumer<MeansOfTransportation> meansOfTransportationClickHandler;

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
    @FXML private Button selectedColorButton = null; 
    private Consumer<TrafficlightClass> trafficLightClickHandler;
    private TrafficlightClass currentTrafficLightLink;

    // Filtering
    @FXML private TitledPane filterPane;
    @FXML private CheckBox filterByColorCheck;
    @FXML private CheckBox filterBySpeedCheck;
    @FXML private VBox colorFilterControlsVBox;       
    @FXML private VBox dynamicColorCheckBoxContainer;  
    @FXML private VBox speedFilterControlsVBox;        
    @FXML private Slider filterMaxSpeedSlider;
    @FXML private Button applyFilterButton; 
    @FXML private Button clearFilterButton;
    
    

    // Stress Testing
    @FXML private TitledPane stressTestPane;
    @FXML private Button stressTestButton;
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
    @FXML private Pane baseMapPane;
    @FXML private Pane lanePane;    
    @FXML private Consumer<LaneClass> laneClickHandler;
    @FXML private Pane junctionPane;
    @FXML private Pane trafficLightPane;
    @FXML private Pane routePane;
    @FXML private Label logLabel;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button resetViewButton;
    @FXML private ToggleButton toggle3DButton;
    @FXML private TitledPane bottomLogArea;
    private LinkedList<String> logHistory = new LinkedList<>();
    private static final int MAX_LOG_LINES = 100;
    private static final Logger logger = LogManager.getLogger(MainController.class);
    


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
    private MapInteractionHandler mapInteractionHandler;
    private SimulationQueue uiQueue;	
    private SimulationQueue statQueue;
    
//     Filtering
    private Map<Color, CheckBox> colorCheckBoxMap = new HashMap<>();
    private List<Color> realColors = new ArrayList<>();
    private List<String> filteredVehicleIDs = new ArrayList<>();
    private boolean isFilterCurrentlyApplied = false;
    
    public MainController() {
		this.uiQueue = new SimulationQueue(2);
		this.statsManager = new StatisticsManager();
        this.simManager = new SimulationManager(uiQueue, this.statsManager);
        this.renderer = new Renderer();
        this.threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        this.statQueue = new SimulationQueue(2);
    }
    
    /**
     * Automatically called by the JavaFX FXMLLoader after the FXML file has been loaded.
     * <p>
     * This method performs the initial setup for the UI and simulation controllers:
     * <ul>
     * <li>Initializes map interaction and data visualization windows.</li>
     * <li>Sets up mutually exclusive expansion logic for the Injection and Stress Test panes.</li>
     * <li>Configures the initial state of UI buttons and default vehicle selection styles.</li>
     * <li>Ensures the local directory for simulation reports exists.</li>
     * <li>Attaches event handlers to data export buttons (CSV and PDF).</li>
     * </ul>
     */
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
        
        disableButtons(true);
        this.chartWindow = new ChartWindow();
        this.injectionSelectedVehicleTypeButton = injectionCarButton;
        this.stressTestSelectedVehicleTypeButton = stressTestCarButton;
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

    /**
     * Initiates the simulation sequence by connecting to SUMO and launching background processing threads.
     * <p>
     * This method coordinates the following startup activities:
     * <ul>
     * <li>Establishes a connection to the SUMO engine and initializes the coordinate converter.</li>
     * <li>Renders the static map infrastructure (lanes and junctions) and sets up mouse interaction handlers.</li>
     * <li><b>Simulation Thread:</b> Launches a background loop to step the simulation and offer state snapshots to the UI and Stat queues.</li>
     * <li><b>Stats Thread:</b> Launches a background loop to calculate performance metrics (speed, density, travel time) and update the chart window.</li>
     * <li>Triggers the UI update loop to synchronize the visual representation with the simulation state.</li>
     * </ul>
     */
    @FXML 
    private void startSimulation() {
        this.startButton.setDisable(true); // prevent double start
        log("Attempting to connect to SUMO...");
        boolean connected = true;
		try {
			connected = this.simManager.startConnection();
		} catch (SumoException e) {
		    if (e.isFatal()) {
		        connected = false;
		        logger.error(e);
		    } else {
		    	logger.warn(e);
		    }
		}

        if (connected) {
            log("Connected! Preparing simulation...");
            isSimulationRunning = true;
            disableButtons(false);
            MapManager mapManager = this.simManager.getMapManager();
            this.trafficLightManager = this.simManager.getTrafficlightManager();
            this.renderer.setConverter(mapManager);
            this.setClickHandler();
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
            
	        log("Static Map drawn");

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
                        currentStep++;
                    } catch (InterruptedException e) {
                        log("Simulation loop interrupted. Stopping safely.");
                        break;
                    } catch (Exception e) {
                        logger.error("Unexpected error in simulation loop:");
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
                        SimulationState state = statQueue.pollState();
                        if (state == null) continue;
                        Map<String, MeansOfTransportation> statsData = state.getMeansOfTransportations();
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
        if (injectionPane != null && injectionPane.isExpanded()) {
            if (injectionSelectedVehicleTypeButton == injectionBikeButton) return "bicycle";
            if (injectionSelectedVehicleTypeButton == injectionBusButton) return "bus";
            if (injectionSelectedVehicleTypeButton == injectionPedestrianButton) return "pedestrian";
            return "passenger";
        } 
        if (stressTestPane != null && stressTestPane.isExpanded()) {
            if (stressTestSelectedVehicleTypeButton == stressTestBikeButton) return "bicycle";
            if (stressTestSelectedVehicleTypeButton == stressTestBusButton) return "bus";
            if (stressTestSelectedVehicleTypeButton == stressTestPedestrianButton) return "pedestrian";
            return "passenger";
        }
        return "passenger"; 
    }
    
    private void updateLaneVisuals() {
        boolean isInjecting = injectionPane.isExpanded();
        boolean isStressTesting = stressTestPane.isExpanded();
        if (!isInjecting && !isStressTesting) {
            for (Node node : lanePane.getChildren()) {
                if (node instanceof Shape shape) {
                    shape.setOpacity(1.0);
                    shape.setDisable(false);
                }
            }
            return;
        }
        String currentMode = getCurrentSelectedMode();
        for (Node node : lanePane.getChildren()) {
            if (node instanceof Shape shape) {
                LaneClass laneData = (LaneClass) shape.getUserData();
                if (laneData != null) {
                    if (laneData.isVehicleAllowed(currentMode)) {
                        shape.setOpacity(1.0);     
                        shape.setDisable(false);    
                    } else {
                        shape.setOpacity(0.15);     
                        shape.setDisable(true);    
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

    
    private void updateView() {
        SimulationState simulationState;
        try {
            simulationState = this.uiQueue.pollState();
            if (simulationState == null) return;
            
            this.refreshColorFilterUI(simulationState);
            
            if (this.isFilterCurrentlyApplied) {
                boolean filterColorActive = filterByColorCheck.isSelected();
                boolean filterSpeedActive = filterBySpeedCheck.isSelected();
                double maxSpeedCriteria = filterMaxSpeedSlider.getValue();
                Set<Color> selectedFXColors = colorCheckBoxMap.entrySet().stream()
                        .filter(entry -> entry.getValue().isSelected())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
                Predicate<MeansOfTransportation> filterRule = v -> {
                	if(v == null) return false; 
                    boolean speedPass = !filterSpeedActive || v.getSpeed() <= maxSpeedCriteria;
                    boolean colorPass = true;
                    if (filterColorActive) {
                        if (selectedFXColors.isEmpty()) {
                            colorPass = false;
                        } else {
                            SumoColor sumoColor = v.getColor();
                            if(sumoColor != null) {
                                Color vehicleFXColor = ColorConverter.toFXColor(sumoColor);
                                colorPass = selectedFXColors.contains(vehicleFXColor);                          
                            } else {
                                colorPass = false;
                            }
                        }
                    }

                    return speedPass && colorPass;
                };
                this.filteredVehicleIDs = this.simManager.getFilteredVehicleIDs(filterRule, simulationState);

                log("Filter applied: " + this.filteredVehicleIDs.size() + " vehicles visible.");
                
            } else {
                this.filteredVehicleIDs.clear();
            }
            this.renderer.renderMeansOfTransportation(
                vehiclePane, 
                simulationState.getMeansOfTransportations(), 
                this.filteredVehicleIDs, 
                this.isFilterCurrentlyApplied,
                this.meansOfTransportationClickHandler
            );
            this.renderer.renderTrafficLights(
            		trafficLightPane, 
            		simulationState.getTrafficLights(), 
            		trafficLightClickHandler);
            
            int currentVehicleCount = simulationState.getMeansOfTransportations().size();
            updateCurrentStep();
            updateCurrentVehicleCount(currentVehicleCount);
            
            if(isExportingVehicleCSV) {
	            	Platform.runLater(() -> {
	            		simManager.generateReports(reportPath, simulationState.getMeansOfTransportations(), "VEHICLE", this.filteredVehicleIDs, currentStep, false, 0, 0);
	            });
            		isExportingVehicleCSV = false;
            }
            
            if(isExportingEdgeCSV) {
	            	Platform.runLater(() -> {
	            		boolean edgeFilter = edgeFilterCheckbox.isSelected();
	            	    double maxSpeed = edgeSpeedSlider.getValue();
	            	    int minDensity = (int) edgeDensitySlider.getValue();
	            	    simManager.generateReports(reportPath, simulationState.getMeansOfTransportations(), "EDGE", this.filteredVehicleIDs, currentStep, edgeFilter, maxSpeed, minDensity);
	            });
            		isExportingEdgeCSV = false;
            }
            
            if(isExportingPDF) {
            		isExportingPDF = false;
            		threadPool.submit(() -> {
                		try {          
                			simManager.generateReports(reportPath, simulationState.getMeansOfTransportations(), "PDF", this.filteredVehicleIDs, currentStep, false, 0 ,0);
                			Platform.runLater(() -> log("PDF Saved to Desktop!"));
                    } catch (Throwable ex) {
                    		logger.error("CRITICAL THREAD ERROR:"); 
                    		ex.printStackTrace();      
                    		Platform.runLater(() -> log("CRASH: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()));
                    	}
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("UI Update Interrupted: " + e.getMessage());
        }
    }
    

    private void log(String message) {
    	logger.info(message);
        Platform.runLater(() -> {
            logHistory.add("> " + message);
            if (logHistory.size() > MAX_LOG_LINES) {
                logHistory.removeFirst();
            }
            String joinedLog = String.join("\n", logHistory);
            logLabel.setText(joinedLog);
            bottomLogScrollPane.setVvalue(1.0);
        });
    }
    
    private void updateCurrentStep() {
    	if(simStepLabel != null) {
    		simStepLabel.setText("" + currentStep);
    	}
    }
    
    private void updateCurrentVehicleCount(int currentVehicleCount) {
    	if(vehicleCountLabel != null) {
    		vehicleCountLabel.setText("" + currentVehicleCount);
    	}
    }

    public void stopSimulation() {
        log("Stopping simulation...");
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
                     log("Thread pool did not terminate gracefully");
                }
            } catch (InterruptedException e) {
                log("Shutdown interrupted");
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
        this.injectionCarButton.setDisable(state);
        this.injectionBikeButton.setDisable(state);
        this.injectionBusButton.setDisable(state);
        this.injectionPedestrianButton.setDisable(state);
        this.stressTestCarButton.setDisable(state);
        this.stressTestBikeButton.setDisable(state);
        this.stressTestBusButton.setDisable(state);
        this.stressTestPedestrianButton.setDisable(state);
        this.setRedPhaseButton.setDisable(state);
        this.setYellowPhaseButton.setDisable(state);
        this.setGreenPhaseButton.setDisable(state);
        this.setTrafficLightColorandorDurationButton.setDisable(state);
        this.switchTrafficLightPhaseButton.setDisable(state);
        this.showChartsButton.setDisable(state);
        this.exportVehiclesCsvButton.setDisable(state);
        this.exportEdgesCsvButton.setDisable(state);
        this.exportPdfButton.setDisable(state);
        this.stressTestButton.setDisable(state);
        this.applyFilterButton.setDisable(state);
        this.clearFilterButton.setDisable(state);
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
		    log("Simulation loop interrupted. Stopping safely.");
		    return;
		} catch (Exception e) {
		    logger.error("Unexpected error in simulation loop:");
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
    	if(this.simManager.injectMeansOfTransportation(vehicleType, sumoColor, speed, firstEdgeId, secondEdgeId)) {
    		log("Injected vehicle");  
    		this.updateView(); 
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
	    		logger.warn(e);
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
    
    @FXML
    private void toggleColorButton(ActionEvent event) {
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
        Button button = (Button) event.getSource();

        if (this.injectionSelectedVehicleTypeButton == button) {
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
        Button button = (Button) event.getSource();
        if (this.stressTestSelectedVehicleTypeButton == button) {
        	log("You must choose at least 1 type");
        } else {
            if (this.stressTestSelectedVehicleTypeButton != null) {
            	this.stressTestSelectedVehicleTypeButton.getStyleClass().remove("selected-button");
            }
            button.getStyleClass().add("selected-button");
            this.stressTestSelectedVehicleTypeButton = button;
            if (firstEdgeField1 != null) firstEdgeField1.clear();
            if (secondEdgeField1 != null) secondEdgeField1.clear();
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

    @FXML private void applyFilter() {
    	filteredVehicleIDs.clear();
        boolean filterColorActive = filterByColorCheck.isSelected();
        boolean filterSpeedActive = filterBySpeedCheck.isSelected();
        this.isFilterCurrentlyApplied = filterColorActive || filterSpeedActive;
        if (this.isFilterCurrentlyApplied) {
            log("Filter status: ON. Color=" + filterColorActive + ", Speed=" + filterSpeedActive);
        } else {
            log("Filter status: OFF (No criteria selected).");
        }
    }

    @FXML private void clearFilter() {
        if (filterByColorCheck != null) filterByColorCheck.setSelected(false);
        if (filterBySpeedCheck != null) filterBySpeedCheck.setSelected(false);
        this.filteredVehicleIDs.clear();
        this.isFilterCurrentlyApplied = false;
        for (CheckBox cb : colorCheckBoxMap.values()) {
            cb.setSelected(false);
        }
        if (filterMaxSpeedSlider != null) {
            filterMaxSpeedSlider.setValue(filterMaxSpeedSlider.getMax()); 
        }
        log("All filter inputs cleared. Click 'Apply Filter' to finalize.");
        if (isPaused) updateView();
    }

    @FXML private void runStressTest() {
    	try {
			this.simManager.stressTest();
			log("Default Stress Test with " + " random cars with random Routes");
			this.updateView();
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.trace(e);
		}
    }
    
    @FXML 
    private void runStressTestOnSpecificEdges() {
    	clearFilter();
    	log("Clear all filtering...");
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
                    boolean success = this.simManager.injectMeansOfTransportation(vehicleType, sumoColor, speed, firstEdgeId, secondEdgeId);
                    final int stressTestVehicleCount = i;
                    if (success) {
                        successCount++;
                        Platform.runLater(() -> log("Injected vehicle " + stressTestVehicleCount)); 
                    } else {
                        Platform.runLater(() -> {
                        	log("Injection failed, no route between 2 edges");
                        	}
                        );
                        break;
                    }
                    Thread.sleep(200); 
                    
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt();
                    break; 
                } catch (Exception e) {
                    if (e.toString().contains("Connection is closed")) {
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

    /**
     * Synchronizes the color filter UI with the colors currently present in the simulation.
     * <p>
     * This method runs a background task to:
     * <ul>
     * <li>Retrieve unique vehicle colors from the current simulation state.</li>
     * <li>Detect changes in the available color set (e.g., when all vehicles of a certain color finish their journey).</li>
     * <li>Reset the filter and refresh the UI checkbox list via {@link Platform#runLater(Runnable)} if the color set has changed.</li>
     * </ul>
     *
     * @param state The current snapshot of the simulation containing vehicle data.
     */
    public void refreshColorFilterUI(SimulationState state) {
        threadPool.submit(() -> {
            try {
                List<Color> fxColorRGBA = this.simManager.getUniqueColors(state); 
                if (fxColorRGBA.size() != this.realColors.size() || !fxColorRGBA.containsAll(this.realColors)) {
                	log("Vehicle of 1 color type finished their journey");
                	this.clearFilter();
                	log("Clearing all filter...");
                    this.realColors = fxColorRGBA;
                    Platform.runLater(() -> this.showColorsAsCheckboxes(this.realColors));
                }
                
            } catch (Exception e) {
                log("Error refreshing colors: " + e.getMessage());
                logger.trace(e);
            }
        });
    }
    
    private void showColorsAsCheckboxes(List<Color> colorsToShow) {
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
                cb.setUserData(color);
                cb.setStyle("-fx-text-fill: " + webColor + "; -fx-font-weight: bold;"); 

                dynamicColorCheckBoxContainer.getChildren().add(cb);
                colorCheckBoxMap.put(color, cb);
            }
            log("Filter UI Populated with " + colorsToShow.size() + " unique colors.");
        });
    }

    private void setClickHandler() {
    	laneClickHandler = (selectedLane) -> {
            if (selectedLane == null) return;
            String laneId = selectedLane.getId();
            String edgeId = laneId.substring(0, laneId.indexOf("_")); 
            boolean isInjecting = (injectionPane != null && injectionPane.isExpanded());
            boolean isStressTesting = (stressTestPane != null && stressTestPane.isExpanded());

            if (isInjecting) {
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
                log("Edge ID: " + edgeId + " | Lane ID: " + laneId);
            }
        };
        trafficLightClickHandler = (trafficLightLink) -> {
            if (trafficLightControlPane != null && trafficLightControlPane.isExpanded()) {
                	trafficLightIdField.setText(trafficLightLink.getLinkId().toString());
                	this.currentTrafficLightLink = trafficLightLink;
                log("Selected Traffic Light: " + trafficLightLink.getLinkId().toString());   
            } else {
                log("Traffic Light ID: " + trafficLightLink.getLinkId().toString());
            }
        };
        meansOfTransportationClickHandler = (selectedVehicle) -> {
            if (selectedVehicle == null) return;
            log(selectedVehicle.getSpeed()+"");
            String vehicleInfo = selectedVehicle.toString();
            log("Vehicle Selected: " + vehicleInfo);
        };
    }
    
}