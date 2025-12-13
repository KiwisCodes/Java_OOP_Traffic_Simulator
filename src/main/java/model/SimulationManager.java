package model;

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
import data.SimulationQueue;
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


public class SimulationManager {

    private String sumoPath = "/Users/apple/sumo/bin/sumo";
    private String sumoConfigFileName = "frauasmap.sumocfg";
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
	public boolean isRunning = false;
    public SimulationManager(SimulationQueue queue, StatisticsManager statsManager) {
    		this.sumoConnection = new SumoTraciConnection(sumoPath, sumoConfigFileName);
    		this.statisticsManager = statsManager;
    }

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
            this.sumoConnection.runServer(); // Starts the SUMO process
            
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

    public void runSimulationLoop() {
        System.out.println("   -> Simulation Loop Started.");

        while (isRunning && !this.sumoConnection.isClosed()) {
            step();
            try { Thread.sleep(10); } catch (InterruptedException e) { break; }
        }
        
        stopSimulation();
        System.out.println("Simulation loop finished.");
    }


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

    public boolean InjectVehicle(String vehType, SumoColor sumoColor, double Speed, String firstEdge, String lastEdge) {
		try {
			System.out.println(sumoColor);
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
	
	public void StressTest(int number) throws Exception {
		int N = number;
		String vehicleStringIDs = String.valueOf(sumoConnection.do_job_get(Vehicle.getIDList()));
		List<String> vehicleIDs = Util.parseStringToList(vehicleStringIDs);
		if(vehicleIDs == null || vehicleIDs.size() == 0){
			System.out.println("LOG: PLEASE TRY AGAIN");
			return;
		}
		List<String> randomVehicleIDs = Util.getRandomElementsWithReplacement(vehicleIDs, N);
//		System.out.println(vehicleIDs);
//        System.out.println("Original List Size: " + vehicleIDs.size());
//        System.out.println("Sampled List (N=" + N + "): " + randomVehicleIDs);
		SumoColor sumoColor =  new SumoColor(0,0,0,0);
		for(int i = 0; i < N; i++) {
			String routeID = "route_" + vehicleCounter;
			SumoStringList edges =  (SumoStringList) sumoConnection.do_job_get(Vehicle.getRoute(randomVehicleIDs.get(i)));
			sumoConnection.do_job_set(Route.add(routeID, edges));
			
			vehicleManager.injectVehicle(String.valueOf("vehicle_" + vehicleCounter++), "DEFAULT_VEHTYPE", routeID, sumoColor, standardSpeed);
		}
	}
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
	
	public Map<String, EdgeClass> getListOfEdges() {
		return listOfEdges;
	};
	
	public Map<String, VehicleClass> getListOfVehicles() {
		return listOfVehicles;
	};
	
	public SumoStringList getRouteFromEdges(String firstEdge, String lastEdge, String vehType) throws Exception {
		double offset = 5;
		double currentTime = (double) sumoConnection.do_job_get(Simulation.getTime());
		double depart = currentTime + offset;
		int routingMode = 0;
		SumoStage stage =  (SumoStage) sumoConnection.do_job_get(Simulation.findRoute(firstEdge, lastEdge, vehType, depart, routingMode));
		SumoStringList edges = stage.edges;
		return edges;
	}

    public void stopSimulation() {
        this.isRunning = false;
        if (this.sumoConnection != null && !this.sumoConnection.isClosed()) {
            this.sumoConnection.close();
            System.out.println("Connection closed.");
        }
    }

    public boolean setSumoBinary(TextField textField) {
    	String userSumoPath = textField.getText();
    	if(userSumoPath != null && userSumoPath != "") {
    		this.sumoPath = userSumoPath;
    		return true;
    	}
    	return false;
    }
    
    
    

    public StatisticsManager getStatisticsManager() { return statisticsManager; }
    public ReportManager getReportManager() { return reportManager; }
    public TrafficlightManager getTrafficlightManager() { return trafficlightManager; }
    public SumoTraciConnection getConnection() { return sumoConnection; }
    public MapManager getMapManager() { return mapManager; }
    public SimulationState getState() {
    	return this.simulationState;
    }
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
}