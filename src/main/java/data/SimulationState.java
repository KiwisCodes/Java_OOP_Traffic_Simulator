package data;

import java.util.Map;
import model.infrastructure.TrafficlightObject;
import model.vehicles.MeansOfTransportation;
import model.vehicles.VehicleClass;

/**
 * Represents an <b>immutable snapshot</b> of the simulation world at a specific moment in time (tick).
 * <p>
 * This class is a fundamental part of the <b>Thread-Safe Rendering</b> architecture:
 * </p>
 * <ul>
 * <li>The <b>Simulation Manager</b> creates a deep copy of the current vehicle and infrastructure data.</li>
 * <li>It wraps that data in this {@code SimulationState} object.</li>
 * <li>This state is passed to the UI thread via the {@link SimulationQueue}.</li>
 * </ul>
 * <p>
 * <b>Why copy?</b> As Java passes object references by value, passing raw manager objects 
 * to the UI thread would lead to {@link java.util.ConcurrentModificationException} 
 * (race conditions) if the simulation modifies the data while the UI is trying to draw it. 
 * By storing copies, this class ensures the View renders a stable state.
 * </p>
 * @author khoale
 * @version 1.0
 */
public class SimulationState {

    /** * A map containing snapshots of all vehicles active in the current tick. 
     * Key: Vehicle ID, Value: The Vehicle object (copy).
     */
    private final Map<String, MeansOfTransportation> lastVehicles;
    
    /** * A map containing the state of all traffic lights in the current tick.
     * 
     */
    private final Map<TrafficlightObject, Character> lastTrafficLightIDs;
//    private final List<String> lastTrafficLightIDs; commented all traffic light to test vehicle and edges/lanes
//    private final Map<String, Map<String, String>> lastLanes;
//    private final Map<String, Map<String, String>> lastJunctions;
    /**
     * Constructor
     * 
     * @param lastVehicles: data about Vehicles
     * @param lastTrafficLightIDs: data about Traffic Lights
     */
    public SimulationState(
//    		Map<String, EdgeClass> lastEdges,
    		Map<String, MeansOfTransportation> lastVehicles,
    		Map<TrafficlightObject, Character> lastTrafficLightIDs
//    		List<String> laneIdList
    		) 
    {	
//    	this.lastEdges = lastEdges;
		this.lastVehicles = lastVehicles;
		this.lastTrafficLightIDs = lastTrafficLightIDs;
//		this.laneIdList = laneIdList;
	}
//	public Map<String, EdgeClass> getEdges() { return lastEdges; }
    /**
     * return the current data about Vehicles
     * @return current state data about Vehicles
     */
    public Map<String, MeansOfTransportation> getVehicles() { return lastVehicles; }
    /**
     * return the current data about Traffic Lights
     * @return current state data about Traffic Lights
     */
    public Map<TrafficlightObject, Character> getTrafficLights() { return lastTrafficLightIDs;}
}