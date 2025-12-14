package controller;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
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
import javafx.geometry.Pos; // import để căn giữa map
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

//--- PHẦN TẠO DỮ LIỆU GIẢ (MOCK DATA) ---
// Import các thư viện này
import de.tudresden.sumo.objects.SumoColor;
import java.util.HashMap;
import de.tudresden.ws.container.SumoPosition2D; // Sửa lỗi SumoPosition2D
import de.tudresden.sumo.objects.SumoColor;     // Sửa lỗi SumoColor (chắc chắn bạn sẽ bị tiếp theo)

// Thêm để vẽ xe chuyển 
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
    @FXML private RadioButton carRadio;
    @FXML private RadioButton bikeRadio;          
    @FXML private ToggleGroup vehicleTypeGroup;   
    @FXML private TextField firstEdgeField;       
    @FXML private TextField secondEdgeField;      
    @FXML private ColorPicker injectVehicleColorPickerButton; 
    @FXML private Slider injectVehicleSpeedSlider;

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
    private Button selectedColorButton = null; // keep track of which button is selected
    private Consumer<TrafficlightObject> trafficLightClickHandler;
    private TrafficlightObject currentTrafficLightLink;

    // Filtering
    @FXML private TextField filterColorField;
    @FXML private TextField filterMinSpeedField;
    @FXML private TextField filterEdgeField;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;

    // Stress Testing
    @FXML private TitledPane stressTestPane;
    @FXML private Button stressTestButton;
    @FXML private RadioButton carRadio1;
    @FXML private RadioButton bikeRadio1;
    @FXML private ToggleGroup vehicleTypeGroup1;
    @FXML private TextField firstEdgeField1;
    @FXML private TextField secondEdgeField1;
    @FXML private ColorPicker injectVehicleColorPickerButton1;
    @FXML private Slider injectVehicleSpeedSlider1;
    @FXML private Slider numberOfVehicleSlider1;
    

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
    @FXML private CheckBox exportFilterCheck;
    @FXML private Button exportVehiclesCsvButton;
    @FXML private Button exportEdgesCsvButton;
    @FXML private Button exportPdfButton;

    // Map & Log
    @FXML private AnchorPane centerMapAnchorPane;
    @FXML private StackPane centerMapStackPane;
    @FXML private Group centerMapPaneGroup;
    @FXML private Pane vehiclePane;
    @FXML private Pane carLanePane;
    @FXML private Pane bikeLanePane;
    @FXML private ScrollPane bottomLogScrollPane;
    private MapManager mapManager;
    @FXML private Pane baseMapPane;
    @FXML private Pane lanePane;     // Static roads go here
    @FXML private Pane junctionPane;
    @FXML private Pane trafficLightPane;
    @FXML private Pane routePane;
    @FXML private Pane carPane;
    @FXML private Pane busPane;
    @FXML private Pane truckPane;
    @FXML private Pane bikePane;
    @FXML private Pane mixedLanePane;
    @FXML private Label logLabel;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button resetViewButton;
    @FXML private ToggleButton toggle3DButton;
    @FXML private TitledPane bottomLogArea;


//    Logic & State
    private SimulationManager simManager;
    private StatisticsManager statsManager;
    private TrafficlightManager trafficLightManager;
    private Renderer renderer; 
    private ChartWindow chartWindow;
    
   
//    Thread
    private AnimationTimer uiLoop; 
    private ExecutorService threadPool; 
    private final int NUMBER_OF_THREADS = 3;
    
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
    
    
//     Initialization

    public MainController() {
		this.uiQueue = new SimulationQueue(2);
		this.statsManager = new StatisticsManager();
        this.simManager = new SimulationManager(uiQueue, this.statsManager);
        this.renderer = new Renderer();
        this.threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        this.statQueue = new SimulationQueue(2);
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
                // CHỈ CHẠY KHI ĐANG MỞ
                if (isNowExpanded) {
                    // 1. Đóng cái kia
                    if (stressTestPane != null && stressTestPane.isExpanded()) {
                        stressTestPane.setExpanded(false);
                    }
                    // 2. Kích hoạt logic bản đồ
                    InteractWithVehicleInjectionDropMenu();
                }
                // Nếu bạn thích giữ text cũ thì xoá đoạn else clear() đi cũng được
            });
        }
        
        if (stressTestPane != null) {
            stressTestPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                // CHỈ CHẠY KHI ĐANG MỞ
                if (isNowExpanded) {
                    if (injectionPane != null && injectionPane.isExpanded()) {
                        injectionPane.setExpanded(false);
                    }
                    InteractWithVehicleStressTestDropMenu();
                }
            });
        }
        if (vehicleTypeGroup != null) {
            vehicleTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            	InteractWithVehicleInjectionDropMenu();
            	// Khi đổi loại xe, xoá trắng 2 ô chọn đường
                if (firstEdgeField != null) firstEdgeField.clear();   
                if (secondEdgeField != null) secondEdgeField.clear(); 
            });
        }
        if (vehicleTypeGroup1 != null) {
            vehicleTypeGroup1.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            	InteractWithVehicleStressTestDropMenu();
            	if (firstEdgeField1 != null) firstEdgeField1.clear();   
                if (secondEdgeField1 != null) secondEdgeField1.clear();
            });
        }
        
        
        disableButtons(true);
        this.chartWindow = new ChartWindow();
        
        showChartsButton.setOnAction(e -> this.chartWindow.show());

        setRedPhaseButton.setOnAction(e -> toggleColorButton(setRedPhaseButton));
        setYellowPhaseButton.setOnAction(e -> toggleColorButton(setYellowPhaseButton));
        setGreenPhaseButton.setOnAction(e -> toggleColorButton(setGreenPhaseButton));
        
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

            Consumer<String> laneClickHandler = (laneId) -> {
            	String edgeId = laneId.substring(0,laneId.indexOf("_"));                
            	//we derive the edgeId from the the laneId
            	//ex: laneId: 3242345_234 -> we just want the things before _ so we find the index of _ and take the substring before it
                if (injectionPane != null && injectionPane.isExpanded()) {
                    if (firstEdgeField.getText().isEmpty()) {
                        firstEdgeField.setText(edgeId);
                        log("Selected First Edge: " + edgeId);
                        
                    } else if (secondEdgeField.getText().isEmpty()) {
                        secondEdgeField.setText(edgeId);
                        log("Selected Second Edge: " + edgeId);
                        
                    } else {
                        firstEdgeField.setText(edgeId);
                        secondEdgeField.clear();
                        log("Selected Another First Edge  " + edgeId);
                    }
                    
                } else if (stressTestPane != null && stressTestPane.isExpanded()) {
                    if (firstEdgeField1.getText().isEmpty()) {
                        firstEdgeField1.setText(edgeId);
                        log("Selected First Edge: " + edgeId);
                        
                    } else if (secondEdgeField1.getText().isEmpty()) {
                        secondEdgeField1.setText(edgeId);
                        log("Selected Second Edge: " + edgeId);
                        
                    } else {
                        firstEdgeField1.setText(edgeId);
                        secondEdgeField1.clear();
                        log("Selected Another First Edge  " + edgeId);
                    }
                    
                }
                else
                {
                    log("Edge ID: " + edgeId); 
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
                this.carLanePane,
                this.bikeLanePane,
                this.mixedLanePane,
                laneClickHandler
            );
	         
            this.renderer.renderJunctions(
            		this.simManager.getMapManager().getJunctions(), 
            		this.junctionPane,
            		juncId -> log("Selected Junction: " + juncId)
            		);
            
            InteractWithVehicleInjectionDropMenu();
            InteractWithVehicleStressTestDropMenu();
	         log("Static Map drawn (Separated Car/Bike lanes)");

            threadPool.submit(() -> {
                log("Simulation Thread Started.");
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
                        double avgSpeed = this.statsManager.avgVehiclesSpeed();
                        Map<String, Integer> density = this.statsManager.calculateVehicleDensity();
                        Map<String, Integer> travelTimeDist = this.statsManager.calculateTravelTimeDistribution(60);

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
    private void startUiLoop() {
        uiLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateView();
            }
        };
        uiLoop.start();
        log("Đã khởi động Animation Loop.");
    }

    private void updateView() {

    		SimulationState simulationState;
		try {
			simulationState = this.uiQueue.pollState();
			if(simulationState == null) return;
			
			this.renderer.renderVehicles(vehiclePane, simulationState.getVehicles());
			this.renderer.renderTrafficLights(trafficLightPane, simulationState.getTrafficLights(), trafficLightClickHandler);
			
			int currentVehicleCount = simulationState.getVehicles().size();
			updateCurrentStep();
			updateCurrentVehicleCount(currentVehicleCount);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.print(e.getMessage());
		}
    }
    

    private void log(String message) {
        System.out.println(message);
        if (logLabel != null) {
            Platform.runLater(() -> {
                logLabel.setText(message + "\n" + logLabel.getText());
                if (this.bottomLogScrollPane != null) {
                    this.bottomLogScrollPane.setVvalue(1.0);
                }
            });
        }
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
    
    private void InteractWithVehicleInjectionDropMenu() {
        if (injectionPane == null || !injectionPane.isExpanded()) {
            if (carLanePane != null) carLanePane.setMouseTransparent(false);
            if (bikeLanePane != null) bikeLanePane.setMouseTransparent(false);
            if (mixedLanePane != null) mixedLanePane.setMouseTransparent(false);
            return;
        }

        boolean isBikeMode = bikeRadio.isSelected();

        if (isBikeMode) {
            if (carLanePane != null) carLanePane.setMouseTransparent(true);
            if (bikeLanePane != null) bikeLanePane.setMouseTransparent(false);
            if (mixedLanePane != null) mixedLanePane.setMouseTransparent(false);
            
        } else {
            if (carLanePane != null) carLanePane.setMouseTransparent(false);
            // Đường Xe đạp: Tắt tương tác (Không sáng)
            if (bikeLanePane != null) bikeLanePane.setMouseTransparent(true);
            if (mixedLanePane != null) mixedLanePane.setMouseTransparent(false);
        }
    }
    
    private void InteractWithVehicleStressTestDropMenu() {
        if (stressTestPane == null || !stressTestPane.isExpanded()) {
            if (carLanePane != null) carLanePane.setMouseTransparent(false);
            if (bikeLanePane != null) bikeLanePane.setMouseTransparent(false);
            if (mixedLanePane != null) mixedLanePane.setMouseTransparent(false);
            return;
        }
        boolean isBikeMode = bikeRadio1.isSelected();

        if (isBikeMode) {
            if (carLanePane != null) carLanePane.setMouseTransparent(true);
            if (bikeLanePane != null) bikeLanePane.setMouseTransparent(false);
            if (mixedLanePane != null) mixedLanePane.setMouseTransparent(false);
            
        } else {
            if (carLanePane != null) carLanePane.setMouseTransparent(false);
            if (bikeLanePane != null) bikeLanePane.setMouseTransparent(true);
            if (mixedLanePane != null) mixedLanePane.setMouseTransparent(false);
        }
    }


    public void stopSimulation() {
        System.out.println("Stopping simulation...");
        isSimulationRunning = false;
        if (uiLoop != null) {
            uiLoop.stop();
        }

        if (simManager != null) {
        	simManager.stopSimulation();
        }
        if (renderer != null) {
            renderer.clearVehicleCache();
        }
	        if (vehiclePane != null) {
	            Platform.runLater(() -> vehiclePane.getChildren().clear());
	        if (threadPool != null) {
	            threadPool.shutdownNow();
	            try {
	                if (!threadPool.awaitTermination(500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
	                     System.out.println("Thread pool did not terminate");
	                }
	            } catch (InterruptedException e) {
	                System.out.println("Shutdown interrupted");
	            }
	        }
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
    	if(this.carRadio.isSelected()) vehicleType = "DEFAULT_VEHTYPE";
    	else if(this.bikeRadio.isSelected()){
            vehicleType = "DEFAULT_BIKETYPE";
            speed = (speed>12) ?  12 : speed;
        }
        Color fxColor = injectVehicleColorPickerButton.getValue();
        SumoColor sumoColor = ColorConverter.toSumoColor(fxColor);
    	if(this.simManager.InjectVehicle(vehicleType, sumoColor, speed, firstEdgeId, secondEdgeId)) {
    		log("Injected vehicle");    		
    	}
    	else {
    		log("Fail injecting vehicle");
    	}
    }
    
    @FXML private void switchTrafficLightPhase() {
    	if(this.trafficLightIdField.getText().isEmpty() || this.currentTrafficLightLink == null) {
    		log("Please choose a Traffic Light please");
    		return;
    	}
    	this.trafficLightManager.setCurrentPhaseDuration(this.currentTrafficLightLink, 0.0);
    	log("Switched to the next Phase");
		return;
}

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

	private void toggleColorButton(Button button) {
	    if (selectedColorButton == button) {
	        // Deselect this button
	        button.getStyleClass().remove("selected-button");
	        selectedColorButton = null;
	    } else {
	        // Deselect previous button
	        if (selectedColorButton != null) {
	            selectedColorButton.getStyleClass().remove("selected-button");
	        }
	        // Select new
	        button.getStyleClass().add("selected-button");
	        selectedColorButton = button;
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
    
    @FXML private void startSumoGUI() {}
    @FXML private void insertSumoConfigFile() {}
    @FXML private void applyFilter() {}
    @FXML private void clearFilter() {}
    
    @FXML private void runStressTest() {
    	try {
			this.simManager.StressTest();
			log("Default Stress Test with 50 random cars with random Routes");
		} catch (Exception e) {
			System.err.print(e.getMessage());
			e.printStackTrace();
		}
    }
    
    @FXML 
    private void runStressTestOnSpecificEdges() {
        if(this.firstEdgeField1.getText().isEmpty() || this.secondEdgeField1.getText().isEmpty()) {
            log("Please choose 2 edges first.");
            return;
        }
        
        this.stressTestButton.setDisable(true);

        PauseTransition unlockTimer = new PauseTransition(Duration.seconds(20));
        unlockTimer.setOnFinished(e -> this.stressTestButton.setDisable(false));
        unlockTimer.play();

        final String firstEdgeId = this.firstEdgeField1.getText();
        final String secondEdgeId = this.secondEdgeField1.getText();

        double spd = 10;
        if (this.injectVehicleSpeedSlider1 != null) {
            spd = this.injectVehicleSpeedSlider1.getValue();
        }

        String type = "DEFAULT_VEHTYPE";
        if (this.bikeRadio1.isSelected()){
            type = "DEFAULT_BIKETYPE";
            spd = (spd>12) ? 12 : spd;
        }

        final String vehicleType = type;
        final double speed = spd;
        
        final int totalVehicles = (int) this.numberOfVehicleSlider1.getValue();

        Color fxColor = injectVehicleColorPickerButton1.getValue();
        SumoColor sumoColor = ColorConverter.toSumoColor(fxColor);

        log("Starting Stress Test: Injecting " + totalVehicles + " vehicles...");

        new Thread(() -> {
            int successCount = 0;
            
            for (int i = 0; i < totalVehicles; i++) {

                boolean success = this.simManager.InjectVehicle(vehicleType, sumoColor, speed, firstEdgeId, secondEdgeId);
                
                if (success) {
                    successCount++;
                    Platform.runLater(() -> log("Injected vehicle")); 
                } else {
                    Platform.runLater(() -> log("Injection failed (Road full?)"));
                }
                try { 
                    Thread.sleep(200); 
                } catch (InterruptedException e) { 
                    break; 
                }
            }
            final int finalCount = successCount;
            Platform.runLater(() -> log("Stress Test Complete. Total Injected: " + finalCount + "vehicles"));
        }).start();
    }
    
    
}