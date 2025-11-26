package model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.appeal.traci.*;
import de.tudresden.sumo.cmd.*;
// Import your vehicle classes
import model.vehicles.VehicleManager;
//import model.vehicles.Car;
//import model.vehicles.Bus;
//import model.vehicles.Truck;
//import model.vehicles.Bike;

// Import Infrastructure
import model.infrastructure.MapManger;
import model.infrastructure.TrafficlightManager;

 //this is pretty much the core logic
public class SimulationManager {


    private String sumoPath = "/Users/apple/sumo/bin/sumo"; 
    private String sumoConfigFileName = "SumoConfig/frauasmap.sumocfg";
    private String sumoConfigFilePath;
    private String stepLength = "0.001"; 
    
    private SumoTraciConnection sumoConnection;
    
    // other sub managers;
    private StatisticsManager statisticsManager;
    private ReportManager reportManager;
    private MapManger sumoMap; 
    
    private int currentStep = 0;
    private boolean isRunning = false;

    public SimulationManager() {
        this.statisticsManager = new StatisticsManager();
        this.reportManager = new ReportManager();
    }
    
    public boolean startConnection() {
        if (!setupPaths()) return false;

        System.out.println("Creating connection with:");
        System.out.println("sumoPaht: " + this.sumoPath);
        System.out.println(".cfgFile: " + this.sumoConfigFilePath);
        
        this.sumoConnection = new SumoTraciConnection(this.sumoPath, this.sumoConfigFilePath);
        this.sumoConnection.addOption("start", null); 
        this.sumoConnection.addOption("step-length", this.stepLength);
        this.sumoConnection.printSumoOutput(true);
        this.sumoConnection.printSumoError(true);

        try {
            System.out.println("Launching SUMO...");
            this.sumoConnection.runServer(); 
            this.sumoMap = new MapManger(sumoConnection);
            
            System.out.println("Connection established!");
            this.isRunning = true;
            return true;

        } catch (Exception e) {
            System.err.println("Error with SUMO: " + e.getMessage());
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
                System.err.println("Sumo path not found at: " + this.sumoPath);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("File path erros: " + e.getMessage());
            return false;
        }
    }

    public void runSimulationLoop() {
        System.out.println("Simulation Loop Started.");

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
        } catch (Exception e) {
            System.err.println("Error during timestep " + currentStep);
            e.printStackTrace();
            stopSimulation(); 
        }
    }

    public void stopSimulation() {
        this.isRunning = false;
        if (this.sumoConnection != null && !this.sumoConnection.isClosed()) {
            this.sumoConnection.close();
            System.out.println("Connection closed.");
        }
    }

    
    
    

    public StatisticsManager getStatisticsManager() { return statisticsManager; }
    public ReportManager getReportManager() { return reportManager; }
    public int getCurrentStep() { return currentStep; } 
    public SumoTraciConnection getConnection() { return sumoConnection; }
    public MapManger getSumoMap() { return sumoMap; }
}