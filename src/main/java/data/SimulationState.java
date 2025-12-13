package data;

import java.util.Map;
import model.infrastructure.TrafficlightObject;
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
 * @author pth
 * @version 1.0
 */
public class SimulationState {

    /** * A map containing snapshots of all vehicles active in the current tick. 
     * Key: Vehicle ID, Value: The Vehicle object (copy).
     */
    private final Map<String, VehicleClass> lastVehicles;
    
    /** * A map containing the state of all traffic lights in the current tick.
     * 
     */
    private final Map<TrafficlightObject, Character> lastTrafficLightIDs;

    /**
     * Constructs a new simulation snapshot.
     * <p>
     * <b>Note:</b> The maps passed to this constructor should contain <b>copies</b> 
     * of the data, not references to the live manager collections.
     * </p>
     *
     * @param lastVehicles A map of vehicle IDs to Vehicle objects containing position/speed data.
     * @param lastTrafficLightIDs A map of TrafficLights to their current phase state.
     */
    public SimulationState(
            Map<String, VehicleClass> lastVehicles,
            Map<TrafficlightObject, Character> lastTrafficLightIDs
            ) 
    {   
        this.lastVehicles = lastVehicles;
        this.lastTrafficLightIDs = lastTrafficLightIDs;
    }

    /**
     * Retrieves the map of vehicles for this frame.
     * @return A map where the key is the Vehicle ID and the value is the Vehicle object.
     */
    public Map<String, VehicleClass> getVehicles() { 
        return lastVehicles; 
    }

    /**
     * Retrieves the map of traffic light states for this frame.
     * @return A map where the key is the TrafficLight object and the value is the phase char.
     */
    public Map<TrafficlightObject, Character> getTrafficLights() { 
        return lastTrafficLightIDs;
    }
}