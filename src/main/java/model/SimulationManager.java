package model;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.MainController;
import model.infrastructure.*;
import model.infrastructure.*;
import it.polito.appeal.traci.*;
import javafx.scene.control.TextField;
import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoStage;
import de.tudresden.sumo.objects.SumoStringList;
import model.vehicles.VehicleClass;
// Import your vehicle classes
import model.vehicles.VehicleManager;
//import model.vehicles.Car;
//import model.vehicles.Bus;
//import model.vehicles.Truck;
//import model.vehicles.Bike;
import util.Util;
// Import Infrastructure
import model.infrastructure.MapManager;
import model.infrastructure.TrafficlightManager;
import data.*;



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
	
	//TODO insert your sumoPath here to run
    private String sumoPath = "/Users/apple/sumo/bin/sumo";
    private String sumoConfigFileName = "SumoConfig/frauasmap.sumocfg";
    private String sumoConfigFilePath;

    private String stepLength = "0.1";
    private SumoTraciConnection sumoConnection;

    private	Map<String, EdgeClass> listOfEdges;
	private	Map<String, VehicleClass> listOfVehicles;
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

        System.out.println("Creating connection with:");
        System.out.println("  > Binary: " + this.sumoPath);
        System.out.println("  > Config: " + this.sumoConfigFilePath);
        
        this.sumoConnection = new SumoTraciConnection(this.sumoPath, this.sumoConfigFilePath);
        this.sumoConnection.addOption("start", null); // Auto-start simulation
        this.sumoConnection.addOption("step-length", this.stepLength);
        this.sumoConnection.printSumoOutput(true);
        this.sumoConnection.printSumoError(true);

        try {
            System.out.println("Launching SUMO... (This may pause until TraCI connects)");
            this.sumoConnection.runServer(); 
            
            if(this.sumoConnection.isClosed()) {
        		System.out.println("Is closed");
        	}
        	else {
        		System.out.println("Is not closed");
        	}

            this.mapManager = new MapManager(sumoConnection);
            this.vehicleManager = new VehicleManager(sumoConnection);
    			this.trafficlightManager = new TrafficlightManager(sumoConnection);
    			this.reportManager = new ReportManager();
            System.out.println("Connection established!");
            this.isRunning = true;
            return true;

        } catch (Exception e) {
            System.err.println("Error starting SUMO: " + e.getMessage());
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
                System.err.println(this.sumoConfigFileName + "' not found in resources!");
                return false;
            }
            File file = new File(resource.toURI());
            this.sumoConfigFilePath = file.getAbsolutePath();
            File sumoBin = new File(this.sumoPath);
            if(!sumoBin.exists() || !sumoBin.canExecute()) {
                System.err.println("SUMO binary not found at: " + this.sumoPath);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error resolving file paths: " + e.getMessage());
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
        System.out.println("   -> Simulation Loop Started.");

        while (isRunning && !this.sumoConnection.isClosed()) {
            step();
            try { Thread.sleep(10); } catch (InterruptedException e) { break; }
        }
        
        stopSimulation();
        System.out.println("Simulation loop finished.");
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
     * Inject Vehicle to SUMO
     * @param vehType: type of Vehicle
     * @param sumoColor: Color of Vehicle
     * @param Speed: Speed of Vehicle
     * @param firstEdge: starting Edge of the Vehicle
     * @param lastEdge: exiting Edge of the Vehicle
     * @return true if injected
     * @throws Execption if no Route found between 2 edges
     */
    public boolean InjectVehicle(String vehType, SumoColor sumoColor, double Speed, String firstEdge, String lastEdge) {
		try {
//			System.out.println(sumoColor);
			String routeID = "routes_" + vehicleCounter;			
			SumoStringList edges = getRouteFromEdges(firstEdge, lastEdge, vehType);
			if(edges == null || edges.size() == 0) {
				System.out.println("ERROR: No path found for vehicle type " + vehType + 
						" from edge " + firstEdge + " to edge " + lastEdge);
				return false;
			}
			
			sumoConnection.do_job_set(Route.add(routeID, edges));
			vehicleManager.injectVehicle(String.valueOf("vehicle_" + vehicleCounter++), vehType, routeID, sumoColor, Speed);
		} catch (Exception e){
			System.out.println(e);
		}
		return true;
	}
	
    /**
     * Random Stress Test a certain number of vehicles
     * @param number: number of vehicle wanted for the Stress Test
     * @throws Exception: throws when communication with SUMO fails
     */
	public void StressTest(int number) throws Exception {
		int N = number;
		String vehicleStringIDs = String.valueOf(sumoConnection.do_job_get(Vehicle.getIDList()));
		List<String> vehicleIDs = Util.parseStringToList(vehicleStringIDs);
		if(vehicleIDs == null || vehicleIDs.size() == 0){
			System.out.println("LOG: PLEASE TRY AGAIN");
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
	public void StressTest() throws Exception {
		int N = 50;
		String vehicleStringIDs = String.valueOf(sumoConnection.do_job_get(Vehicle.getIDList()));
		List<String> vehicleIDs = Util.parseStringToList(vehicleStringIDs);
		if(vehicleIDs == null || vehicleIDs.size() == 0){
			System.out.println("LOG: PLEASE TRY AGAIN");
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
	public Map<String, VehicleClass> getListOfVehicles() {
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
            System.out.println("Connection closed.");
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
		Map<String, VehicleClass> vehicleData = state.getVehicles();
		List<String> colors = new ArrayList<>();
		List<Color> colorRGBA = new ArrayList<>();
		for(Map.Entry<String, VehicleClass> vehicle : vehicleData.entrySet()) {
			VehicleClass innerMap = vehicle.getValue();
			String colorValue = String.valueOf(innerMap.getColor());
			if(colors.contains(colorValue)) {
				continue;
			}
			else {
				colors.add(colorValue);
			}
		}
		/**
		 * 
		 * hello
		 */
		for(String c: colors) {
			String[] parts = c.split("#");
			int r = (Integer.parseInt(parts[0]) + 256) % 256;
		    int g = (Integer.parseInt(parts[1]) + 256) % 256;
		    int b = (Integer.parseInt(parts[2]) + 256) % 256;
		    int a =	255;
//		    System.out.println("RGBA = " + r + ", " + g + ", " + b + ", " + a);
		    Color color = new Color(r,g,b,a);
		    colorRGBA.add(color);
		}
		return colorRGBA;
	}
	public List<String> getIDColor(int r, int g, int b, int a, SimulationState state){
		Map<String, VehicleClass> vehicleData = state.getVehicles();
		List<String> validIDs = new ArrayList<>();
		for(Map.Entry<String, VehicleClass> vehicle : vehicleData.entrySet()) {
			String vehicleId = vehicle.getKey();
		    VehicleClass innerMap = vehicle.getValue();
		    String colorValue = String.valueOf(innerMap.getColor());
		    String[] parts = colorValue.split("#");
		    int r1 = (Integer.parseInt(parts[0]) + 256) % 256;
		    int g1 = (Integer.parseInt(parts[1]) + 256) % 256;
		    int b1 = (Integer.parseInt(parts[2]) + 256) % 256;
//		    int a1 = (Integer.parseInt(parts[3]) + 256) % 256;
		    System.out.println("DEBUG: Vehicle Color: " + r1 + "," + g1 + "," + b1 + " | Filter Color: " + r + "," + g + "," + b);		    
		    if (r1 == r && g1 == g && b1 == b) {
		    	validIDs.add(vehicleId);
		    }
		}
//		System.out.println(validIDs);
		return validIDs;
	}
	public List<String> getIDSpeed(double speed, SimulationState state){
		List<String> validIDs = new ArrayList<>();
		Map<String, VehicleClass> vehicleData = state.getVehicles();
		for(Map.Entry<String, VehicleClass> vehicle : vehicleData.entrySet()) {
			String vehicleId = vehicle.getKey();
		    VehicleClass innerMap = vehicle.getValue();
		    String currentSpeed = String.valueOf(innerMap.getSpeed());
		    double speedDouble = Double.parseDouble(currentSpeed);
		    if(speedDouble <= speed) {
		    	validIDs.add(vehicleId);
		    }		    
		}
		return validIDs;
	}
	
}