package data;

import java.util.concurrent.atomic.*;


import model.infrastructure.*;
import model.vehicles.VehicleClass;

import java.util.*;

public class SimulationState {
	/*
	this is a class that expects copies of data from managers, not reference to those,
	althought the golden rule of java that it always pass by value
	however it is intuitive for primitive data
	as for complex objects, classes, like a Hashmap or a connection or other objects with many attribute
	when you pass those objects into other functions, you are just passing the address of the regions in the heap that
	contains the real value
	so to conclude -> managers must return a copy, not the reference to the real thing.
	*/

//	private final Map<String, EdgeClass> lastEdges;
//	private final Map<String, LaneObject> lastLanes;
    private final Map<String, VehicleClass> lastVehicles;//need to change this
//    private final List<String> laneIdList; //we use this to draw all the lanes;
    private final Map<TrafficlightObject, Character> lastTrafficLightIDs;
//    private final List<String> lastTrafficLightIDs; commented all traffic light to test vehicle and edges/lanes
//    private final Map<String, Map<String, String>> lastLanes;
//    private final Map<String, Map<String, String>> lastJunctions;
    public SimulationState(
//    		Map<String, EdgeClass> lastEdges,
    		Map<String, VehicleClass> lastVehicles,
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
    public Map<String, VehicleClass> getVehicles() { return lastVehicles; }
    public Map<TrafficlightObject, Character> getTrafficLights() { return lastTrafficLightIDs;}
}