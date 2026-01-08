package model.vehicles;

import de.tudresden.sumo.cmd.Vehicle; 
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.util.SumoCommand;
import de.tudresden.sumo.objects.SumoStringList;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D; 
import de.tudresden.sumo.config.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all vehicles information and interaction related to vehicles in SUMO via TraCI interface
 * <p>
 * This class retrieves and stores all of the information related to all vehicles at each step (speed, color) and also helps inject vehicles into SUMO
 * It stores simulation state at every {@link #step()}.
 *
 * @author Minh Khoi
 */
public class VehicleManager {

    /** The connectiion object used to communicate with the running SUMO instance */
    private SumoTraciConnection conn;
    /** A list containing the IDs of all vehicles currently active in the simulation */
    private List<String> vehiclesIds;
    /** A map storing all informations related to each vehicle, using their ID */
    private Map<String, MeansOfTransportation> vehiclesData;

    /**
     * Constructs a new VehicleManager.
     *
     * @param connection The active {@link SumoTraciConnection} to the SUMO simulation.
     */
	public VehicleManager(SumoTraciConnection connection) {
		this.conn = connection;
        this.vehiclesData = new HashMap<>();
		this.vehiclesIds = new ArrayList<>();
	}

    /**
     * Synchronizes the local vehicle data with the current state of the simulation.
     * <p>
     * This method performs the following actions:
     * <ol>
     *   <li>Retrieves the list of all active vehicle IDs from SUMO.</li>
     *   <li>Resets the local data storage.</li>
     *   <li>Calls {@link #updateVehiclesInfo()} to fetch detailed attributes for each vehicle.</li>
     * </ol>
     * If the connection is lost or closed, the method handles the exception
     * and clears the local list of vehicles.
     */
	public void step() {
		try {
			SumoCommand idListCmd = Vehicle.getIDList();
			Object response = this.conn.do_job_get(idListCmd);
			
			if (response instanceof SumoStringList) {
				this.vehiclesIds = (SumoStringList) response;
			}
			
			this.vehiclesData = new HashMap<>();
			
			if(this.conn == null || this.conn.isClosed()) {
				return;
			}
			
			this.updateVehiclesInfo();
			
		} catch (IllegalStateException e){
			System.out.println("VehicleManager: Connection closed. Stopping updates.");
	        this.vehiclesIds = new ArrayList<>(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * Fetches detailed attributes for every vehicle currently active in the simulation.
     * <p>
     * For each vehicle ID, multiple TraCI commands are executed to retrieve:
     * <ul>
     *   <li>Color</li>
     *   <li>Position (2D)</li>
     *   <li>Speed</li>
     *   <li>Current Edge (Road) ID</li>
     *   <li>Angle</li>
     *   <li>Departure time</li>
     * </ul>
     * The gathered information is packaged into a {@link VehicleClass} object and stored in the map.
     */
	private void updateVehiclesInfo() {
	    for (String id : this.vehiclesIds) {
	        if (this.conn == null || this.conn.isClosed()) return;

	        try {
	            // 1. Get the Vehicle Type ID (e.g., "bus", "car_express", "ped_type")
	            SumoCommand typeCmd = Vehicle.getTypeID(id); 
	            String typeId = ((String) this.conn.do_job_get(typeCmd)).toLowerCase();

	            // 2. Fetch shared data from SUMO
	            SumoColor color = (SumoColor) this.conn.do_job_get(Vehicle.getColor(id));
	            SumoPosition2D position = (SumoPosition2D) this.conn.do_job_get(Vehicle.getPosition(id));
	            double speed = (Double) this.conn.do_job_get(Vehicle.getSpeed(id));
	            String edgeId = (String) this.conn.do_job_get(Vehicle.getRoadID(id));
	            double angle = (Double) this.conn.do_job_get(Vehicle.getAngle(id));
	            
	            // Fetch Departure Time (Custom Command)
	            SumoCommand departureCmd = new SumoCommand(
	                    Constants.CMD_GET_VEHICLE_VARIABLE, Constants.VAR_DEPARTURE, id,
	                    Constants.RESPONSE_GET_VEHICLE_VARIABLE, Constants.TYPE_DOUBLE
	            );
	            double departure = (Double) this.conn.do_job_get(departureCmd);

	            // 3. Instantiate the correct subclass
	            // IMPORTANT: We use the order from your BusClass: (id, speed, position, color, edgeId, angle, departure)
	            MeansOfTransportation vehicle;

	            if (typeId.contains("bus")) {
	                vehicle = new BusClass(id, speed, position, color, edgeId, angle, departure);
	            } else if (typeId.contains("bike") || typeId.contains("bicycle")) {
	                vehicle = new BikeClass(id, speed, position, color, edgeId, angle, departure);
	            } else if (typeId.contains("pedestrian") || typeId.contains("ped")) {
	                vehicle = new PedestrianClass(id, speed, position, color, edgeId, angle, departure);
	            } else {
	                // Default to CarClass for anything else
	                vehicle = new CarClass(id, speed, position, color, edgeId, angle, departure);
	            }

	            // 4. Store in the Map
	            // Because BusClass, CarClass, etc., all EXTEND VehicleClass, this works perfectly.
	            this.vehiclesData.put(id, vehicle);

	        } catch (Exception e) {
	            System.err.println("Error requesting data for vehicle: " + id);
	            e.printStackTrace();
	        }
	    }
	}

    /**
     * Returns a copy of the current vehicle data map.
     *
     * @return A {@link Map} where the key is the vehicle ID and the value is the {@link VehicleClass} object.
     */
	public Map<String, MeansOfTransportation> getVehiclesData() {
		return new HashMap<>(this.vehiclesData);
	}

    /**
     * Injects a new vehicle into the simulation dynamically.
     *
     * @param vehicleId The unique identifier for the new vehicle.
     * @param typeId    The vehicle type ID (must be defined in the SUMO config/routes).
     * @param routeId   The route ID (must be defined in the SUMO config/routes).
     * @param sumoColor The color of the vehicle.
     * @param Speed     The initial speed of the vehicle in m/s.
     */
	public void injectVehicle(String vehicleId, String typeId, String routeId, SumoColor sumoColor, double Speed) {
		try {
			int depart = 0; 
			double pos = 0.0;
			byte lane = (byte) 0;
			
			SumoCommand addCmd = Vehicle.add(vehicleId, typeId, routeId, depart, pos, Speed, lane);
			this.conn.do_job_set(addCmd);
			
			SumoCommand setColorCmd = Vehicle.setColor(vehicleId, sumoColor);
			this.conn.do_job_set(setColorCmd);
			System.out.println("Vehicle Injected: " + vehicleId);

		} catch (Exception e) {
			System.out.println("Error at Injection of Vehicle " + vehicleId);
			e.printStackTrace();
		}
	}
	
	public int getVehicleCount() {
		try {
	
			SumoCommand idCountCmd = Vehicle.getIDCount();
			return (Integer) this.conn.do_job_get(idCountCmd);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public void printVehiclesData() {
		if (this.vehiclesData.isEmpty()) {
			System.out.println("No vehicles are active");
			return;
		}
		
		System.out.println("----Actual Vehicles Data----");
		
		for (MeansOfTransportation v : this.vehiclesData.values()) {
			System.out.println("ID " + v.getId());
			System.out.println(" - Color: " + v.getColor());
			System.out.println(" - Position: " + v.getPosition().x + ", " + v.getPosition().y);
			System.out.println(" - Speed: " + v.getSpeed());
			System.out.println(" - Edge: " + v.getEdgeId());
			System.out.println("--------------------------");
		}
	}
	
	public void printIdList(int step) {
		if (this.vehiclesIds != null) {
			for (String id : this.vehiclesIds) {
				System.out.println(id);
			}
            System.out.println("Step " + step + " Active Vehicles: " + this.vehiclesIds.size());
		}
	}
}