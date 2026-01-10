package model;

import model.vehicles.MeansOfTransportation;
import model.vehicles.VehicleClass;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

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
 *
 * @author Minh Khoi
 */
public class StatisticsManager {

    /** A snapshot of all vehicles currently active in the simulation. */
    private Map<String, MeansOfTransportation> vehiclesData;

    /** The current simulation step number. */
    private int step;

    /**
     * Initializes the StatisticsManager with an empty data set.
     */
	public StatisticsManager() {
		this.vehiclesData = new HashMap<>();
	}

    /**
     * Updates the internal state with the latest vehicle data from the simulation.
     * <p>
     * This method should be called at the end of every simulation loop iteration
     * to ensure statistics are calculated based on the most recent data.
     *
     * @param statsData A map containing the current vehicles, keyed by their ID.
     * @param step         The current time step index of the simulation.
     */
	public void step(Map<String, MeansOfTransportation> statsData, int step) {
		this.vehiclesData = statsData;
		this.step = step;
	}

    /**
     * Calculates the arithmetic mean speed of all active vehicles in the network.
     *
     * @return The average speed in m/s, or 0.0 if no vehicles are active.
     */
	public double avgVehiclesSpeed(Map<String, MeansOfTransportation> vehiclesInfo) {
		if (vehiclesInfo.isEmpty()) {
			System.err.println("There are no vehicles currently");
			return 0.0;
		}
		
		double totalSpeed = 0;
		for (MeansOfTransportation vehicle : this.vehiclesData.values()) {
			totalSpeed += vehicle.getSpeed();
		}
		return totalSpeed / vehiclesData.size();
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
		Map<String, List<Double>> edgeSpeeds = new HashMap<>();
		
		for(MeansOfTransportation vehicle : this.vehiclesData.values()) {
			String edgeId = vehicle.getEdgeId();
			double speed = vehicle.getSpeed();
			
			edgeSpeeds.putIfAbsent(edgeId, new ArrayList<>());
			edgeSpeeds.get(edgeId).add(speed);
		}
		
		List<String> congestedEdges = new ArrayList<>();
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
		
		return congestedEdges;
	}

    /**
     * Calculates the density of vehicles on each edge.
     *
     * @return A map associating Edge IDs (Key) with the number of vehicles currently on that edge (Value).
     */
	public Map<String, Integer> calculateVehicleDensity(Map<String, MeansOfTransportation> vehiclesInfo) {
		Map<String, Integer> densityMap = new HashMap<>();
		
		if (vehiclesInfo.isEmpty()) {
			return densityMap;
		}
		
		for (MeansOfTransportation vehicle : this.vehiclesData.values()) {
			String edgeId = vehicle.getEdgeId();
			
			densityMap.put(edgeId, densityMap.getOrDefault(edgeId, 0) + 1);
		}
		
		return densityMap;
	}
	
	
	// Calculate travel time distribution
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
	public Map<String, Integer> calculateTravelTimeDistribution(Map<String, MeansOfTransportation> vehiclesInfo, int binSizeSeconds) {
		Map<String, Integer> distribution = new TreeMap<>((a, b) -> {
			int lowerA = Integer.parseInt(a.split("-")[0]);
			int lowerB = Integer.parseInt(b.split("-")[0]);
			return Integer.compare(lowerA, lowerB);
		});
		
		if (vehiclesInfo.isEmpty()) {
			return distribution;
		}
		
		for (MeansOfTransportation vehicle : this.vehiclesData.values()) {
			
			double departureTime = vehicle.getDeparture();		
			double currentTravelTime = this.step * 0.1 - departureTime;

			
			if (currentTravelTime < 0) {
				continue;
			}
			int binIndex = (int) (currentTravelTime / binSizeSeconds);
			int lowerBound = binIndex * binSizeSeconds;
			int upperBound = (binIndex + 1) * binSizeSeconds;
			
			String key = lowerBound + "-" + upperBound;
			distribution.put(key, distribution.getOrDefault(key, 0) + 1);
		}
		return distribution;
	}
	
}