package model;

import model.vehicles.VehicleClass;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculates real-time statistics based on the current state of vehicles in the simulation.
 * <p>
 * This class processes raw vehicle data to derive meaningful metrics such as:
 * <ul>
 *   <li>Average network speed</li>
 *   <li>Congestion spots (edges with low average speeds)</li>
 *   <li>Vehicle density per edge</li>
 *   <li>Travel time distribution (histograms)</li>
 * </ul>
 * It is designed to be updated in every simulation step via {@link #step(Map, int)}.
 * <p>
 * Logging is implemented using {@link java.util.logging.Logger}.
 *
 * @author Minh Khoi
 */
public class StatisticsManager {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(StatisticsManager.class.getName());

    /** A snapshot of all vehicles currently active in the simulation. */
    private Map<String, VehicleClass> vehiclesData;

    /** The current simulation step number. */
    private int step;

    /**
     * Initializes the StatisticsManager with an empty data set.
     */
    public StatisticsManager() {
        LOGGER.info("Initializing StatisticsManager...");
        this.vehiclesData = new HashMap<>();
        LOGGER.fine("StatisticsManager initialized with empty data.");
    }

    /**
     * Updates the internal state with the latest vehicle data from the simulation.
     * <p>
     * This method should be called at the end of every simulation loop iteration
     * to ensure statistics are calculated based on the most recent data.
     *
     * @param vehiclesInfo A map containing the current vehicles, keyed by their ID.
     * @param step         The current time step index of the simulation.
     */
    public void step(Map<String, VehicleClass> vehiclesInfo, int step) {
        if (vehiclesInfo == null) {
            LOGGER.warning("Received null vehiclesInfo map in step " + step + ". Statistics will not be updated.");
            return;
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Updating statistics for step " + step + " with " + vehiclesInfo.size() + " vehicles.");
        }

        this.vehiclesData = vehiclesInfo;
        this.step = step;
    }

    /**
     * Calculates the arithmetic mean speed of all active vehicles in the network.
     *
     * @return The average speed in m/s, or 0.0 if no vehicles are active.
     */
    public double avgVehiclesSpeed() {
        if (this.vehiclesData.isEmpty()) {
            LOGGER.fine("No vehicles currently in simulation. Returning avg speed 0.0.");
            return 0.0;
        }

        try {
            double totalSpeed = 0;
            for (VehicleClass vehicle : this.vehiclesData.values()) {
                totalSpeed += vehicle.getSpeed();
            }
            return totalSpeed / vehiclesData.size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating average vehicle speed.", e);
            return 0.0;
        }
    }

    /**
     * Identifies road segments (edges) where traffic is congested.
     * <p>
     * Congestion is defined as an edge where the average speed of all vehicles
     * on that edge is below a threshold of 5.0 m/s.
     *
     * @return A list of edge IDs representing the congestion spots.
     */
    public List<String> findCongestionSpots() {
        LOGGER.finer("Identifying congestion spots...");
        List<String> congestedEdges = new ArrayList<>();

        try {
            Map<String, List<Double>> edgeSpeeds = new HashMap<>();

            for(VehicleClass vehicle : this.vehiclesData.values()) {
                String edgeId = vehicle.getEdgeId();
                double speed = vehicle.getSpeed();

                edgeSpeeds.putIfAbsent(edgeId, new ArrayList<>());
                edgeSpeeds.get(edgeId).add(speed);
            }

            double congestionThreshold = 5.0;

            for (Map.Entry<String, List<Double>> entry : edgeSpeeds.entrySet()) {
                String edgeId = entry.getKey();
                List<Double> speeds = entry.getValue();

                double sum = 0;
                for (Double s : speeds) {
                    sum += s;
                }

                double avg = sum / speeds.size();

                if (avg < congestionThreshold) {
                    congestedEdges.add(edgeId);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error identifying congestion spots.", e);
        }

        return congestedEdges;
    }

    /**
     * Calculates the density of vehicles on each edge.
     *
     * @return A map associating Edge IDs (Key) with the number of vehicles currently on that edge (Value).
     */
    public Map<String, Integer> calculateVehicleDensity() {
        Map<String, Integer> densityMap = new HashMap<>();

        if (this.vehiclesData.isEmpty()) {
            LOGGER.finer("No vehicles data available for density calculation.");
            return densityMap;
        }

        try {
            for (VehicleClass vehicle : this.vehiclesData.values()) {
                String edgeId = vehicle.getEdgeId();
                densityMap.put(edgeId, densityMap.getOrDefault(edgeId, 0) + 1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating vehicle density.", e);
        }

        return densityMap;
    }


    /**
     * Calculates a histogram of current travel times for all active vehicles.
     * <p>
     * The method groups vehicles into time bins (e.g., "0-10", "10-20") based on how long
     * they have been in the simulation. The travel time is calculated as:
     * {@code currentSimTime - departureTime}.
     * <p>
     * <b>Note:</b> This method assumes a simulation step length of 0.1 seconds.
     *
     * @param binSizeSeconds The size of the time buckets in seconds (e.g., 10 for 10-second intervals).
     * @return A {@link TreeMap} sorted by time range, mapping the range string (e.g., "10-20") to the vehicle count.
     */
    public Map<String, Integer> calculateTravelTimeDistribution(int binSizeSeconds) {
        Map<String, Integer> distribution = new TreeMap<>((a, b) -> {
            try {
                int lowerA = Integer.parseInt(a.split("-")[0]);
                int lowerB = Integer.parseInt(b.split("-")[0]);
                return Integer.compare(lowerA, lowerB);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Error parsing distribution keys: " + a + ", " + b, e);
                return a.compareTo(b); // Fallback
            }
        });

        if (this.vehiclesData.isEmpty()) {
            return distribution;
        }

        try {
            for (VehicleClass vehicle : this.vehiclesData.values()) {

                double departureTime = vehicle.getDeparture();
                double currentTravelTime = this.step * 0.1 - departureTime;

                if (currentTravelTime < 0) {
                    LOGGER.fine(() -> "Negative travel time detected for vehicle " + vehicle + ". Skipping.");
                    continue;
                }

                int binIndex = (int) (currentTravelTime / binSizeSeconds);
                int lowerBound = binIndex * binSizeSeconds;
                int upperBound = (binIndex + 1) * binSizeSeconds;

                String key = lowerBound + "-" + upperBound;
                distribution.put(key, distribution.getOrDefault(key, 0) + 1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating travel time distribution.", e);
        }

        return distribution;
    }
}