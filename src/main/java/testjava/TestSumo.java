package testjava;

import java.io.IOException;
import java.util.List;

import de.tudresden.sumo.cmd.*;
import it.polito.appeal.traci.*;
import model.SimulationManager;

public class TestSumo {

    public static void main(String[] args) {
        
        // --- 1. Define paths and load config file ---
        String sumoBinary = "/Users/apple/sumo/bin/sumo-gui"; 
        
        String configFileName = "frauasmap.sumocfg";
        String configFile;
        
        try {
            java.net.URL resource = SimulationManager.class.getClassLoader().getResource(configFileName);
            
            if (resource == null) {
                System.err.println("❌ CRITICAL ERROR: Could not find '" + configFileName + "' in resources!");
                System.err.println("Please make sure the file exists in 'src/main/resources' and run Maven clean/install.");
                return; 
            }
            
            configFile = resource.getPath();

        } catch (Exception e) {
            System.err.println("Error loading config file path: " + e.getMessage());
            return; 
        }

        // --- 2. Create and configure connection ---
        SumoTraciConnection conn = new SumoTraciConnection(sumoBinary, configFile);

        conn.printSumoOutput(true);
        conn.printSumoError(true);

        // 3. Add options
        conn.addOption("start", null);            
        conn.addOption("quit-on-end", "true");    
        
        try {
            // 4. Start SUMO process and connect
            conn.runServer();
            System.out.println("✅ Connected to SUMO-GUI. Running with 1s step delay.");

            // 5. Run simulation for 300 steps
            for (int step = 0; step < 300; step++) {
                conn.do_timestep();
                
                // 🚀 ADDED: PAUSES EXECUTION FOR 1 SECOND AFTER EACH STEP
                try {
                    Thread.sleep(1000); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Delay interrupted.");
                }
                // --------------------------------------------------------

                @SuppressWarnings("unchecked")
                List<String> vehicleIds = (List<String>)
                    conn.do_job_get(Vehicle.getIDList());

                // Print every 50 steps
                if (step % 50 == 0) {
                    System.out.println("Step " + step + " Active Vehicles: " + vehicleIds.size());
                }
            }
            
            // --- 6. Final cleanup pause ---
            System.out.println("⏳ Simulation loop complete. Waiting 5 seconds before closing GUI...");
            Thread.sleep(5000); 
            
            // --- 7. Close connection ---
            conn.close();
            System.out.println("✅ Simulation finished and connection closed.");

        } catch (IOException e) {
            System.err.println("I/O error during connection: " + e.getMessage());
            System.err.println("Possible cause: SUMO executable failed to launch or connect.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Cleanup pause interrupted.");
        } catch (Exception e) {
            System.err.println("TraCI error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
