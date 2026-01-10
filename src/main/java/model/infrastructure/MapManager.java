package model.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controller.MainController;
import model.infrastructure.EdgeClass;
import model.infrastructure.LaneClass;
import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.objects.SumoGeometry;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.util.SumoCommand;
import de.tudresden.ws.container.SumoBoundingBox; // Or use simple doubles
import it.polito.appeal.traci.SumoTraciConnection;

public class MapManager {
	private SumoTraciConnection sumoConnection;
	private int totalEdge = 0;
	private int totalLane = 0;
	private int totalJunction = 0;

    private List<String> edgeIdList;
    private List<String> laneIdList;
    private List<String> junctionIdList;
    private SumoBoundingBox sumoBoundingBox;
    
    private Map<String, EdgeClass> edges;
    private Map<String, LaneClass> lanes;
    private Map<String, JunctionClass> junctions;
    
    
    //i use this for the converter
    private double minX = Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private double maxX = Double.MIN_VALUE;
    private double maxY = Double.MIN_VALUE;
    private static final Logger logger = LogManager.getLogger(MapManager.class);

    public MapManager(SumoTraciConnection sumoConnection) {
        if(!sumoConnection.isClosed()) {
        	try {
        		this.sumoConnection = sumoConnection;
				Object response = sumoConnection.do_job_get(Edge.getIDList());
				@SuppressWarnings("unchecked")
				List<String> edges = (List<String>)response;
				this.edgeIdList = edges;
				response = sumoConnection.do_job_get(Junction.getIDList());
				@SuppressWarnings("unchecked")
				List<String> junctions = (List<String>)response;
				this.junctionIdList = junctions;
				response = sumoConnection.do_job_get(Lane.getIDList());
				@SuppressWarnings("unchecked")
				List<String> lanes = (List<String>)response;
				this.laneIdList = lanes;
				response = sumoConnection.do_job_get(Simulation.getNetBoundary());
				
				this.edges = new HashMap<>();
				this.lanes = new HashMap<>();
				this.junctions = new HashMap<>();
				
				
				fetchEdgesFromSumo();//this also fetch lanes from Sumo
				fetchJunctionsFromSumo();
				
				
				
				SumoCommand getNetBoundary = Simulation.getNetBoundary();
				response = this.sumoConnection.do_job_get(getNetBoundary);
				
				if(response instanceof SumoBoundingBox) {
					SumoBoundingBox box = (SumoBoundingBox) response;
					this.maxX = box.x_max;
					this.maxY = box.y_max;
					this.minX = box.x_min;
					this.minY = box.y_min;
				}
				else if(response instanceof SumoGeometry) {
					SumoGeometry geometry = (SumoGeometry) response;
					
					for(SumoPosition2D point : geometry.coords) {
						if(point.x < this.minX) this.minX = point.x;
						if(point.y < this.minY) this.minY = point.y;
						if(point.x > this.maxX) this.maxX = point.x;
						if(point.y > this.maxY) this.maxY = point.y; 
					}
				} else {
				    minX = 0; minY = 0; maxX = 1000; maxY = 1000;
				    System.err.println("Unknown map boundary type: " + response.getClass().getName());
				}
				
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
        }
    }

    /**
     * Return a list of EdgeId
     * @return list of EdgeIds
     */
    public List<String> getEdgeIdList() { 
    	return new ArrayList<> (edgeIdList); 
    }
    /**
     * Return a list of LaneId
     * @return list of LaneIds
     */
    public List<String> getLaneIdList() { 
    	return new ArrayList<>(laneIdList); 
    }
    /**
     * Return a list of JunctionId
     * @return list of JunctionIds
     */
    public List<String> getJunctionIdList() { 
    	return new ArrayList<> (junctionIdList);
    }
    
    
    public double getWidth() { return maxX - minX; }
    public double getHeight() { return maxY - minY; }
    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }
    
    
    
    
    private void fetchEdgesFromSumo() throws Exception{
    	for(int i = 0; i < this.edgeIdList.size(); i++) {
    		String edgeId = this.edgeIdList.get(i);
    		EdgeClass edge = new EdgeClass(sumoConnection, edgeId);
    		this.edges.put(edgeId, edge);
    		Map<String, LaneClass> childLanes = edge.getLanes();
            this.lanes.putAll(childLanes);
    		this.totalEdge++;
    		this.totalLane += edge.getLaneCount();
    	}
    }

//    private void fetchLanesFromSumo() throws Exception{
//    	//write the laneObject first
//    }
    
    private void fetchJunctionsFromSumo() throws Exception{
    	for(int i=0;i<this.junctionIdList.size();i++) {
    		String junctionId = this.junctionIdList.get(i);
    		JunctionClass junction = new JunctionClass(sumoConnection, junctionId);
    		this.junctions.put(junctionId, junction);
    		this.totalJunction++;
    	}
    }
    /**
     * Get the Edge Objects in this Simulation
     * @return a HashMap of Edge IDs and Edge Objects
     */
    public Map<String, EdgeClass> getEdges() {
    	return new HashMap<>(this.edges);
	}
    /**
     * Get the Lane Objects in this Simulation
     * @return a HashMap of LaneIDs and Lane Objects
     */
    public Map<String, LaneClass> getLanes() {
    	return new HashMap<>(this.lanes);
    }
    
    /**
     * Get the Junction Objects in this Simulation
     * @return a HashMap of JunctionIDs and the Junction Objects
     */
    public Map<String, JunctionClass> getJunctions() {
    	return new HashMap<>(this.junctions);
    }
    
}