package model;

import model.vehicles.VehicleClass;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

public class StatisticsManager {
	
	private Map<String, VehicleClass> vehiclesData;
	private int step;
	
	public StatisticsManager() {
		this.vehiclesData = new HashMap<>();
	}
	
	public void step(Map<String, VehicleClass> vehiclesInfo, int step) {
		this.vehiclesData = vehiclesInfo;
		this.step = step;
	}
	
	public double avgVehiclesSpeed() {
		if (this.vehiclesData.isEmpty()) {
			System.err.println("There are no vehicles currently");
			return 0.0;
		}
		
		double totalSpeed = 0;
		for (VehicleClass vehicle : this.vehiclesData.values()) {
			totalSpeed += vehicle.getSpeed();
		}
		return totalSpeed / vehiclesData.size();
	}
	
	
	public List<String> findCongestionSpots() {
		Map<String, List<Double>> edgeSpeeds = new HashMap<>();
		
		for(VehicleClass vehicle : this.vehiclesData.values()) {
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
	
	
	public Map<String, Integer> calculateVehicleDensity() {
		Map<String, Integer> densityMap = new HashMap<>();
		
		if (this.vehiclesData.isEmpty()) {
			return densityMap;
		}
		
		for (VehicleClass vehicle : this.vehiclesData.values()) {
			String edgeId = vehicle.getEdgeId();
			
			densityMap.put(edgeId, densityMap.getOrDefault(edgeId, 0) + 1);
		}
		
		return densityMap;
	}
	
	
	// Calculate travel time distribution
	public Map<String, Integer> calculateTravelTimeDistribution(int binSizeSeconds) {
		Map<String, Integer> distribution = new TreeMap<>((a, b) -> {
			int lowerA = Integer.parseInt(a.split("-")[0]);
			int lowerB = Integer.parseInt(b.split("-")[0]);
			return Integer.compare(lowerA, lowerB);
		});
		
		if (this.vehiclesData.isEmpty()) {
			return distribution;
		}
		
		for (VehicleClass vehicle : this.vehiclesData.values()) {
			
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