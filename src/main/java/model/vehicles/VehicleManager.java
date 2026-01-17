package model.vehicles;

import de.tudresden.sumo.cmd.Person;
import de.tudresden.sumo.cmd.Vehicle; 
import it.polito.appeal.traci.SumoTraciConnection;
import model.SimulationManager;
import de.tudresden.sumo.util.SumoCommand;
import de.tudresden.sumo.objects.SumoStringList;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D; 
import de.tudresden.sumo.config.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages all vehicles information and interaction related to vehicles in SUMO via TraCI interface
 * <p>
 * This class retrieves and stores all of the information related to all vehicles at each step (speed, color) and also helps inject vehicles into SUMO
 * It stores simulation state at every {@link #step()}.
 *
 * @author Minh Khoi
 */
public class VehicleManager {

	private static final Logger logger = LogManager.getLogger(VehicleManager.class);
    /** The connectiion object used to communicate with the running SUMO instance */
    private SumoTraciConnection conn;
    /** A list containing the IDs of all vehicles currently active in the simulation */
    private List<String> vehiclesIds;
    
    private List<String> pedestrianIds;
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
		this.pedestrianIds = new ArrayList<>();
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
			idListCmd = Person.getIDList();
			response = this.conn.do_job_get(idListCmd);
			if (response instanceof SumoStringList) {
				this.pedestrianIds = (SumoStringList) response;
			}
			
			
			
			this.vehiclesData = new HashMap<>();
			
			if(this.conn == null || this.conn.isClosed()) {
				return;
			}
			
			this.updateVehiclesInfo();
			
		} catch (IllegalStateException e){
			logger.info("VehicleManager: Connection closed. Stopping updates.");
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
		if(this.vehiclesIds != null) {
			for (String id : this.vehiclesIds) {
				if (this.conn == null || this.conn.isClosed()) return;
				
				try {
					SumoCommand typeCmd = Vehicle.getTypeID(id); 
					String typeId = ((String) this.conn.do_job_get(typeCmd)).toLowerCase();
					

					SumoColor color = (SumoColor) this.conn.do_job_get(Vehicle.getColor(id));
					SumoPosition2D position = (SumoPosition2D) this.conn.do_job_get(Vehicle.getPosition(id));
					double speed = (Double) this.conn.do_job_get(Vehicle.getSpeed(id));
					String edgeId = (String) this.conn.do_job_get(Vehicle.getRoadID(id));
					double angle = (Double) this.conn.do_job_get(Vehicle.getAngle(id));
					

					SumoCommand departureCmd = new SumoCommand(
							Constants.CMD_GET_VEHICLE_VARIABLE, Constants.VAR_DEPARTURE, id,
							Constants.RESPONSE_GET_VEHICLE_VARIABLE, Constants.TYPE_DOUBLE
							);
					double departure = (Double) this.conn.do_job_get(departureCmd);
					MeansOfTransportation vehicle = null;
					
					if (typeId.contains("bus")) {
						vehicle = new BusClass(id, speed, position, color, edgeId, angle, departure);
					} else if (typeId.contains("bike") || typeId.contains("bicycle")) {
						vehicle = new BikeClass(id, speed, position, color, edgeId, angle, departure);
					} else if (typeId.contains("passenger") || typeId.contains("veh")) {
						vehicle = new CarClass(id, speed, position, color, edgeId, angle, departure);
					}

					if(vehicle != null) {
						this.vehiclesData.put(id, vehicle);	            	
					}
					
				} catch (Exception e) {
					logger.error("Error requesting data for vehicle: " + id);
					e.printStackTrace();
				}
			}
			
		}

		if(this.pedestrianIds != null) {
			for (String id : this.pedestrianIds) {
				try {
					String typeId = ((String) this.conn.do_job_get(Person.getTypeID(id))).toLowerCase();
					SumoColor color = (SumoColor) this.conn.do_job_get(Person.getColor(id));
					SumoPosition2D position = (SumoPosition2D) this.conn.do_job_get(Person.getPosition(id));
					double speed = (Double) this.conn.do_job_get(Person.getSpeed(id));
					String edgeId = (String) this.conn.do_job_get(Person.getRoadID(id));
					double angle = (Double) this.conn.do_job_get(Person.getAngle(id));
					
					double departure = 0.0; 
					MeansOfTransportation pedestrian = new PedestrianClass(id, speed, position, color, edgeId, angle, departure);
					
					if(pedestrian != null) {
						this.vehiclesData.put(id, pedestrian);                	
					}
					
				} catch (Exception e) {
					logger.error("Error data for person: " + id);
				}
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
			logger.info("Vehicle Injected: " + vehicleId);

		} catch (Exception e) {
			logger.error("Error at Injection of Vehicle " + vehicleId);
			e.printStackTrace();
		}
	}
	
	public void injectPerson(String personId, String typeId, SumoStringList edgeList, SumoColor sumoColor, double speed) {
	    try {
	        if (edgeList == null || edgeList.size() == 0) return;

	        // 1. Get the Start Edge (Person.add needs a String, NOT a list)
	        String firstEdge = edgeList.get(0);
	        this.conn.do_job_set(Person.add(personId, firstEdge, 0, (byte) 0, typeId));

	        // 3. Set Visuals and Speed
	        this.conn.do_job_set(Person.setColor(personId, sumoColor));
	        this.conn.do_job_set(Person.setSpeed(personId, speed));

	        // 4. FIX: Pass the 'edgeList' object directly
	        // Do NOT convert to string. The method signature wants SumoStringList.
	        this.conn.do_job_set(Person.appendWalkingStage(
	            personId, 
	            edgeList,   // <--- Pass the SumoStringList directly here
	            0.0,        // arrivalPos (0 usually means end of edge in this context)
	            -1.0,       // duration (-1 = calculated by speed)
	            speed,      // speed
	            ""          // stopID
	        ));

	        logger.info("Person Injected: " + personId);

	    } catch (Exception e) {
	        logger.error("Error at Injection of Person " + personId);
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
			logger.info("No vehicles are active");
			return;
		}
		
		logger.info("----Actual Vehicles Data----");
		
		for (MeansOfTransportation v : this.vehiclesData.values()) {
			logger.info("ID " + v.getId());
			logger.info(" - Color: " + v.getColor());
			logger.info(" - Position: " + v.getPosition().x + ", " + v.getPosition().y);
			logger.info(" - Speed: " + v.getSpeed());
			logger.info(" - Edge: " + v.getEdgeId());
			logger.info("--------------------------");
		}
	}
	
	public void printIdList(int step) {
	    // Print Cars
	    if (this.vehiclesIds != null) {
	        for (String id : this.vehiclesIds) logger.info("Car: " + id);
	    }
	    
	    // Print Pedestrians
	    if (this.pedestrianIds != null) {
	        for (String id : this.pedestrianIds) logger.info("Person: " + id);
	    }

	    int vCount = (vehiclesIds != null) ? vehiclesIds.size() : 0;
	    int pCount = (pedestrianIds != null) ? pedestrianIds.size() : 0;
	    
	    logger.info("Step " + step + " | Active Cars: " + vCount + " | Active People: " + pCount);
	}
}