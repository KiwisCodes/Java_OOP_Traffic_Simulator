package model.vehicles;

import de.tudresden.sumo.cmd.Person;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import de.tudresden.sumo.config.Constants;
import org.apache.logging.log4j.LogManager;


/**
 * Manages all vehicles information and interaction related to vehicles in SUMO via TraCI interface
 * <p>
 * This class retrieves and stores all of the information related to all vehicles at each step (speed, color) and also helps inject vehicles into SUMO
 * It stores simulation state at every {@link #step()}.
 *
 * @author Minh Khoi
 */
public class VehicleManager {
    private static final Logger LOGGER = Logger.getLogger(VehicleManager.class.getName());
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
        LOGGER.info("Initializing VehicleManager...");
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
                LOGGER.warning("Connection is null or closed during step execution.");
                return;
            }

            this.updateVehiclesInfo();

        } catch (IllegalStateException e){
            LOGGER.warning("VehicleManager: Connection closed. Stopping updates.");
            this.vehiclesIds = new ArrayList<>();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during simulation step.", e);
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
                    LOGGER.log(Level.SEVERE, "Error requesting data for vehicle: " + id, e);
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
                    LOGGER.log(Level.SEVERE, "Error requesting data for person: " + id, e);
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
            
            synchronized(this.conn) {
            	SumoCommand addCmd = Vehicle.add(vehicleId, typeId, routeId, depart, pos, Speed, lane);
            	this.conn.do_job_set(addCmd);
            	
            	SumoCommand setColorCmd = Vehicle.setColor(vehicleId, sumoColor);
            	this.conn.do_job_set(setColorCmd);            	
            }

            LOGGER.info("Vehicle Injected: " + vehicleId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error at Injection of Vehicle " + vehicleId, e);
        }
    }

    public void injectPerson(String personId, String typeId, SumoStringList edgeList, SumoColor sumoColor, double speed) {
        try {
            if (edgeList == null || edgeList.size() == 0) {
                LOGGER.warning("Injection skipped for person " + personId + ": Edge list is empty or null.");
                return;
            }
            String firstEdge = edgeList.get(0);
            
            synchronized(this.conn) {
            	this.conn.do_job_set(Person.add(personId, firstEdge, 0, Constants.DEPARTFLAG_NOW, typeId));
            	
            	this.conn.do_job_set(Person.setColor(personId, sumoColor));
            	this.conn.do_job_set(Person.setSpeed(personId, speed));
            	
            	this.conn.do_job_set(Person.appendWalkingStage(
            			personId,
            			edgeList,
            			-1.0,
            			-1.0,
            			speed,
            			""
            			));
            }
            LOGGER.info("Person Injected: " + personId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error at Injection of Person " + personId, e);
        }
    }

    public int getVehicleCount() {
        try {
            SumoCommand idCountCmd = Vehicle.getIDCount();
            return (Integer) this.conn.do_job_get(idCountCmd);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting vehicle count.", e);
            return 0;
        }
    }
}