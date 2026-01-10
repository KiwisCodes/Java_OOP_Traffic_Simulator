package model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controller.MainController;
import model.infrastructure.*;
import model.infrastructure.*;
import it.polito.appeal.traci.*;
import javafx.scene.control.TextField;
import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoStage;
import de.tudresden.sumo.objects.SumoStringList;
import model.vehicles.MeansOfTransportation;
import model.vehicles.VehicleClass;
// Import your vehicle classes
import model.vehicles.VehicleManager;
import util.ColorConverter;
import javafx.scene.paint.Color;
//import model.vehicles.Car;
//import model.vehicles.Bus;
//import model.vehicles.Truck;
//import model.vehicles.Bike;
import util.Util;
// Import Infrastructure
import model.infrastructure.MapManager;
import model.infrastructure.TrafficlightManager;
import data.*;
import de.tudresden.sumo.cmd.Vehicletype;



/**
 * The central controller for the SUMO simulation logic.
 * <p>
 * This class acts as the <b>Facade</b> between the Java application and the TraaS (Traffic as a Service) API.
 * It is responsible for:
 * <ul>
 * <li>Managing the lifecycle of the simulation (Start, Stop, Step).</li>
 * <li>Maintaining the connection to the SUMO process via TraCI.</li>
 * <li>Orchestrating the {@link VehicleManager}, {@link TrafficlightManager}, and {@link MapManager}.</li>
 * <li>Handling vehicle injection and stress testing scenarios.</li>
 * </ul>
 * </p>
 * @author pth, khoale
 * @version 1.0
 */
public class SimulationManager {
	private static final Logger logger = LogManager.getLogger(SimulationManager.class);
	//TODO insert your sumoPath here to run
    private String sumoPath = "/Users/apple/sumo/bin/sumo";
    private String sumoConfigFileName = "SumoConfig/frauasmap.sumocfg";
    private String sumoConfigFilePath;

    private String stepLength = "0.1";
    private SumoTraciConnection sumoConnection;

    private	Map<String, EdgeClass> listOfEdges;
	private	Map<String, MeansOfTransportation> listOfVehicles;
	private List<String> listOfTrafficlightIds;
	private	Map<String, Map<String, String>> listOfLanes;
	private	Map<String, Map<String, String>> listOfJunctions;
    
    private StatisticsManager statisticsManager;
    private ReportManager reportManager;
    private MapManager mapManager; // Holds static map data (Lanes, Edges)
    private VehicleManager vehicleManager;
    private TrafficlightManager trafficlightManager;
    

    private SimulationState simulationState;
    private static int vehicleCounter = 0;
	private double standardSpeed = 3.6;
	
	/** Flag indicating if the simulation loop is currently active. */
	public boolean isRunning = false;
	
	/**
     * Constructs a new Simulation Manager.
     * @param queue The queue used for thread-safe communication (not currently stored in this class but kept for architecture consistency).
     * @param statsManager The manager responsible for calculating traffic statistics.
     */
    public SimulationManager(SimulationQueue queue, StatisticsManager statsManager) {
    		this.sumoConnection = new SumoTraciConnection(sumoPath, sumoConfigFileName);
    		this.statisticsManager = statsManager;
    }
    
    
    /**
     * Establishes the connection to the SUMO server and initializes all sub-managers.
     * <p>
     * This method locates the configuration files, starts the SUMO binary, and creates instances
     * of {@link MapManager}, {@link VehicleManager}, etc.
     * </p>
     * @return {@code true} if the connection was successfully established, {@code false} otherwise.
     */
    public boolean startConnection() {
        if (!setupPaths()) return false;

        logger.info("Creating connection with:");
        logger.info("  > Binary: " + this.sumoPath);
        logger.info("  > Config: " + this.sumoConfigFilePath);
        
        this.sumoConnection = new SumoTraciConnection(this.sumoPath, this.sumoConfigFilePath);
        this.sumoConnection.addOption("start", null); // Auto-start simulation
        this.sumoConnection.addOption("step-length", this.stepLength);
        this.sumoConnection.printSumoOutput(true);
        this.sumoConnection.printSumoError(true);

        try {
            logger.info("Launching SUMO... (This may pause until TraCI connects)");
            this.sumoConnection.runServer(); 
            
            if(this.sumoConnection.isClosed()) {
        		logger.info("Is closed");
        	}
        	else {
        		logger.info("Is not closed");
        	}

            this.mapManager = new MapManager(sumoConnection);
            this.vehicleManager = new VehicleManager(sumoConnection);
    			this.trafficlightManager = new TrafficlightManager(sumoConnection);
    			this.reportManager = new ReportManager();
            logger.info("Connection established!");
            
         // --- DEBUG: PRINT ALL LOADED VEHICLE TYPES ---
            logger.info("=== LOADED VEHICLE TYPES ===");
            
            // Retrieve the list of IDs from SUMO
            // Note: usage of do_job_get() is required for TraaS
            @SuppressWarnings("unchecked")
            List<String> types = (List<String>) this.sumoConnection.do_job_get(Vehicletype.getIDList());

            for (String t : types) {
                logger.info("Found Type: " + t);
            }
            logger.info("============================");
            
            this.isRunning = true;
            return true;

        } catch (Exception e) {
            logger.warn("Error starting SUMO: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    /**
     * Resolves the absolute paths for the SUMO binary and the configuration file.
     * @return {@code true} if files exist and are executable.
     */
    private boolean setupPaths() {
        try {
            URL resource = SimulationManager.class.getClassLoader().getResource(this.sumoConfigFileName);
            if (resource == null) {
                logger.warn(this.sumoConfigFileName + "' not found in resources!");
                return false;
            }
            File file = new File(resource.toURI());
            this.sumoConfigFilePath = file.getAbsolutePath();
            File sumoBin = new File(this.sumoPath);
            if(!sumoBin.exists() || !sumoBin.canExecute()) {
                logger.warn("SUMO binary not found at: " + this.sumoPath);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.warn("Error resolving file paths: " + e.getMessage());
            return false;
        }
    }

    
    /**
     * Runs the main simulation loop in a blocking manner.
     * <p>
     * <b>Note:</b> This method blocks the calling thread until the simulation stops. 
     * Ideally, it should be run in a separate thread from the UI.
     * </p>
     */
    public void runSimulationLoop() {
        logger.info("   -> Simulation Loop Started.");

        while (isRunning && !this.sumoConnection.isClosed()) {
            step();
            try { Thread.sleep(10); } catch (InterruptedException e) { break; }
        }
        
        stopSimulation();
        logger.info("Simulation loop finished.");
    }

    /**
     * Advances the simulation by one timestep.
     * <p>
     * This triggers the {@code do_timestep()} command in SUMO, updates all vehicle and traffic light managers,
     * and captures a new {@link SimulationState} snapshot.
     * </p>
     */
    public void step() {
        try {
            this.sumoConnection.do_timestep();
            this.vehicleManager.step();
            this.simulationState = new SimulationState(
            										this.vehicleManager.getVehiclesData(),
            										this.trafficlightManager.getTrafficlightData()
            										);
        } catch (Exception e) {
            e.printStackTrace();
            stopSimulation(); 
        }
    }
    
    
    
    
    
    /**
     * Generates simulation reports (CSV or PDF) based on the given type.
     *
     * <p>The method can export:
     * <ul>
     *   <li>Vehicle data (CSV)</li>
     *   <li>Edge data (CSV)</li>
     *   <li>Summary report (PDF)</li>
     * </ul>
     *
     * @param outputDir directory where report files will be saved
     * @param type report type: "VEHICLE", "EDGE", or "PDF"
     * @param currentStepCount current simulation timestep
     */
    public void generateReports(String outputDir, Map<String, MeansOfTransportation>vehicleData, String type, List<String> filteredVehicleIDs, int currentStepCount, boolean edgeFilter, double maxAvgSpeed, int minDensity) {
        logger.debug("--- DEBUG: Starting Report Generation ---");
        if (this.reportManager == null || this.statisticsManager == null || this.mapManager == null) {
            logger.warn("❌ ERROR: Managers are NULL. Did you click 'Start Simulation' first?");
            return;
        }
        for(String i: filteredVehicleIDs) {
        		if(i == null) {
        			logger.warn("❌ ERROR: Vehicles in filteredVehicleIDs is NULL!");
        		}
        }
        File folder = new File(outputDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        logger.info("📂 Saving files to: " + folder.getAbsolutePath());

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");

        String timestamp = LocalDateTime.now().format(formatter);
        Map<String, MeansOfTransportation> filteredData = new HashMap<>();
        if (filteredVehicleIDs == null || filteredVehicleIDs.isEmpty()) {
        		filteredData = vehicleData;
        }
        else {
        		for(String i: filteredVehicleIDs) {
        			if(vehicleData.get(i) != null) {
            			filteredData.put(i, vehicleData.get(i));        				
        			}
        		}
        }
        try {
            if (type.equals("VEHICLE")) {
                logger.info("   > Collecting Vehicle Data...");
                List<VehicleInfo> vehicleList = new ArrayList<>();
                Map<String, MeansOfTransportation> rawData = filteredData;
                for (Map.Entry<String, MeansOfTransportation> entry : rawData.entrySet()) {
                    String id = entry.getKey();
                    MeansOfTransportation attr = entry.getValue();
                    double speed = (Double) attr.getSpeed();
                    String color = ColorConverter.colorToWebString(ColorConverter.toFXColor(attr.getColor()));
                    double depart = (Double) attr.getDeparture();
                    double timeAlive = currentStepCount - depart;
                    vehicleList.add(new VehicleInfo(id, speed, timeAlive, color, "car"));
                }

                String fileName = outputDir + "/vehicles_" + timestamp + ".csv";
                this.reportManager.exportVehiclesCSV(vehicleList, fileName);
                logger.info("   ✅ Vehicle CSV saved.");
            }
        } catch (Exception e) {
            logger.warn("❌ CRASH during Vehicle Export: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            if (type.equals("EDGE")) {
                logger.info("   > Collecting Edge Data...");
                List<EdgeInfo> edgeList = new ArrayList<>();
                Map<String, Integer> densityMap = this.statisticsManager.calculateVehicleDensity(filteredData);
                List<String> allEdges = mapManager.getEdgeIdList();
                
                for (String edgeId : allEdges) {
                    double density = densityMap.getOrDefault(edgeId, 0);
                    
                    double avgSpeed = (density > 0) ? this.statisticsManager.avgVehiclesSpeed(filteredData) : 0.0;
                    double width = 3.5; 

                    edgeList.add(new EdgeInfo(edgeId, width, density, avgSpeed));
                }

                String fileName = outputDir + "/edges_" + timestamp + ".csv";
                this.reportManager.exportEdgesCSV(edgeList, fileName, edgeFilter, maxAvgSpeed, minDensity);
                logger.info("   ✅ Edge CSV saved.");
            }
        } catch (Exception e) {
            logger.warn("❌ CRASH during Edge Export: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (type.equals("PDF")) {
                logger.info("   > Generating PDF...");
                String pdfName = outputDir + "/SimulationReport_" + timestamp + ".pdf";
                reportManager.exportReportPDF(this.statisticsManager, pdfName, filteredData, currentStepCount);
                logger.info("   ✅ PDF saved.");
            }
        } catch (Exception e) {
            logger.warn("❌ CRASH during PDF Export: " + e.getMessage());
            e.printStackTrace();
        }

        logger.debug("--- DEBUG: Finished ---");
    }
    
    /**
     * Inject Vehicle to SUMO
     * @param vehType: type of Vehicle
     * @param sumoColor: Color of Vehicle
     * @param Speed: Speed of Vehicle
     * @param firstEdge: starting Edge of the Vehicle
     * @param lastEdge: exiting Edge of the Vehicle
     * @return true if injected
     * @throws Execption if no Route found between 2 edges
     */
    public boolean injectMeansOfTransportation(String vehType, SumoColor sumoColor, double Speed, String firstEdge, String lastEdge) {
        try {
            // 1. Calculate the route using your existing function
            // This works for Pedestrians too, as long as 'vehType' is "pedestrian" in SUMO
            SumoStringList edges = getRouteFromEdges(firstEdge, lastEdge, vehType);
            
            if(edges == null || edges.size() == 0) {
                logger.info("ERROR: No path found for type " + vehType + 
                        " from edge " + firstEdge + " to edge " + lastEdge);
                return false;
            }

            // 2. CHECK IF THIS IS A PEDESTRIAN
            // You can check via String comparison, or instanceof if you have the object.
            // Assuming "pedestrian" is the ID string from your SUMO rou.xml
            if (vehType.equalsIgnoreCase("DEFAULT_PEDTYPE")) {
                
                String personID = "person_" + vehicleCounter++;
                
                // Call the new specific method in VehicleManager
                vehicleManager.injectPerson(personID, vehType, edges, sumoColor, Speed);
                
            } else {
                // 3. STANDARD VEHICLE LOGIC (Existing Code)
                String routeID = "routes_" + vehicleCounter;
                String vehicleID = "vehicle_" + vehicleCounter++;
                
                sumoConnection.do_job_set(Route.add(routeID, edges));
                vehicleManager.injectVehicle(vehicleID, vehType, routeID, sumoColor, Speed);
            }

        } catch (Exception e){
            logger.info("Exception in InjectVehicle: " + e);
            e.printStackTrace();
            return false;
        }
        return true;
    }
	
    /**
     * Random Stress Test a certain number of vehicles
     * @param number: number of vehicle wanted for the Stress Test
     * @throws Exception: throws when communication with SUMO fails
     */
	public void stressTest(int number) throws Exception {
		int N = number;
		String vehicleStringIDs = String.valueOf(sumoConnection.do_job_get(Vehicle.getIDList()));
		List<String> vehicleIDs = Util.parseStringToList(vehicleStringIDs);
		if(vehicleIDs == null || vehicleIDs.size() == 0){
			logger.info("LOG: PLEASE TRY AGAIN");
			return;
		}
		List<String> randomVehicleIDs = Util.getRandomElementsWithReplacement(vehicleIDs, N);
		SumoColor sumoColor =  new SumoColor(0,0,0,0);
		for(int i = 0; i < N; i++) {
			String routeID = "route_" + vehicleCounter;
			SumoStringList edges =  (SumoStringList) sumoConnection.do_job_get(Vehicle.getRoute(randomVehicleIDs.get(i)));
			sumoConnection.do_job_set(Route.add(routeID, edges));
			
			vehicleManager.injectVehicle(String.valueOf("vehicle_" + vehicleCounter++), "DEFAULT_VEHTYPE", routeID, sumoColor, standardSpeed);
		}
	}
	/**
	 * Random Stress Test with default number of vehicles (50)
	 * @throws Exception throws when communication with SUMO fails
	 */
	public void stressTest() throws Exception {
		int N = 50;
		String vehicleStringIDs = String.valueOf(sumoConnection.do_job_get(Vehicle.getIDList()));
		List<String> vehicleIDs = Util.parseStringToList(vehicleStringIDs);
		if(vehicleIDs == null || vehicleIDs.size() == 0){
			logger.info("LOG: PLEASE TRY AGAIN");
			return;
		}
		List<String> randomVehicleIDs = Util.getRandomElementsWithReplacement(vehicleIDs, N);
		SumoColor sumoColor =  new SumoColor(0,0,0,0);
		for(int i = 0; i < N; i++) {
			String routeID = "route_" + vehicleCounter;
			SumoStringList edges =  (SumoStringList) sumoConnection.do_job_get(Vehicle.getRoute(randomVehicleIDs.get(i)));
			sumoConnection.do_job_set(Route.add(routeID, edges));
			
			vehicleManager.injectVehicle(String.valueOf("vehicle_" + vehicleCounter++), "DEFAULT_VEHTYPE", routeID, sumoColor, standardSpeed);
		}
	}
	
	/**
	 * Get list of Edge Objects of the current state
	 * @return a HashMap of Edge Ids and Edge Objects
	 */
	public Map<String, EdgeClass> getListOfEdges() {
		return listOfEdges;
	};
	
	/**
	 * Get list of Vehicle Objects of the current state
	 * @return a HashMap of Vehicle Ids and Edge Objects
	 */
	public Map<String, MeansOfTransportation> getListOfVehicles() {
		return listOfVehicles;
	};
	

	private SumoStringList getRouteFromEdges(String firstEdge, String lastEdge, String vehType) throws Exception {
		double offset = 5;
		double currentTime = (double) sumoConnection.do_job_get(Simulation.getTime());
		double depart = currentTime + offset;
		int routingMode = 0;
		SumoStage stage =  (SumoStage) sumoConnection.do_job_get(Simulation.findRoute(firstEdge, lastEdge, vehType, depart, routingMode));
		SumoStringList edges = stage.edges;
		return edges;
	}
	
	/**
	 * Stop Simulation by disconnecting with SUMO
	 */
    public void stopSimulation() {
        this.isRunning = false;
        if (this.sumoConnection != null && !this.sumoConnection.isClosed()) {
            this.sumoConnection.close();
            logger.info("Connection closed.");
        }
    }

    /**
     * Updates the path to the SUMO binary based on user input.
     * @param textField The UI text field containing the new path.
     * @return {@code true} if the input was valid (not null/empty), {@code false} otherwise.
     */
    public boolean setSumoBinary(TextField textField) {
    	String userSumoPath = textField.getText();
    	if(userSumoPath != null && userSumoPath != "") {
    		this.sumoPath = userSumoPath;
    		return true;
    	}
    	return false;
    }
    
    
    
    /**
     * Get Statistic Manger
     * @return Statistic Manager
     */
    public StatisticsManager getStatisticsManager() { return statisticsManager; }
    /**
     * Get Report Manager 
     * @return Report Manager
     */
    public ReportManager getReportManager() { return reportManager; }
    /**
     * Get Traffic Light Manager
     * @return Traffic Light Manager
     */
    public TrafficlightManager getTrafficlightManager() { return trafficlightManager; }
    /**
     * Get SUMO connection
     * @return SUMO connection
     */
    public SumoTraciConnection getConnection() { return sumoConnection; }
    
    /** @return The Map Manager instance. */
    public MapManager getMapManager() { return mapManager; }
    
    /** @return The most recent snapshot of the simulation state. */
    public SimulationState getState() {
        return this.simulationState;
    }
    
    /**
     * Returns the simulation step length if it is a valid non-negative number.
     *
     * If the stored step length cannot be parsed as a number or is negative,
     * the method returns {@code -1} to indicate an invalid value.
     *
     * @return step length value, or {@code -1} if the value is invalid
     */
    public double getStepLength() {
        boolean check_validity = false;
        try {
            double val = Double.parseDouble(this.stepLength);
            if(val >= 0) {
                check_validity = true;
            }
        } catch (NumberFormatException e) {
            check_validity = false;
        }
        if(!check_validity) {
            return -1;
        }
        else {
            return Double.parseDouble(this.stepLength);
        }
    }
    
    
    // KHOA FILTERING
	public List<Color> getUniqueColors(SimulationState state){
		Map<String, MeansOfTransportation> vehicleData = state.getMeansOfTransportations();
		//Hung changed to SumoColor
		List<Color> fxColorRGBA = new ArrayList<>();
		for(Map.Entry<String, MeansOfTransportation> vehicle : vehicleData.entrySet()) {
			MeansOfTransportation props = vehicle.getValue();
			SumoColor sumoColor = props.getColor();
			Color fxColor = ColorConverter.toFXColor(sumoColor);
			if(!fxColorRGBA.contains(fxColor)) {
				fxColorRGBA.add(ColorConverter.toFXColor(sumoColor));				
			}
		}
		return fxColorRGBA;
	}
	
	// ADD THIS METHOD
	public List<String> getFilteredVehicleIDs(Predicate<MeansOfTransportation> criteria, SimulationState state) {
	    if (state == null || state.getMeansOfTransportations() == null) return new ArrayList<>();

	    List<String> list = state.getMeansOfTransportations().values().stream()
	                .filter(criteria)
	                .map(MeansOfTransportation::getId)
	                .toList();
	    
	    return new ArrayList<>(list); // Returns a mutable copy or else it wont work, race condition
	}
	
}