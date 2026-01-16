package model.infrastructure;

import java.util.List;
import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.objects.SumoGeometry;
import it.polito.appeal.traci.SumoTraciConnection;


public class LaneClass {
    private final String id;
    private final SumoGeometry geometry; 
    private final double width; 
    private final String parentEdgeId;
    private final boolean allowsPassenger;
    private final boolean allowsBicycle; 
    private final List<String> allowedClasses;

    @SuppressWarnings("unchecked")
	public LaneClass(SumoTraciConnection connection, String laneId, String parentEdgeId) throws Exception {
        this.id = laneId;
        this.parentEdgeId = parentEdgeId;


        this.geometry = (SumoGeometry) connection.do_job_get(Lane.getShape(laneId));
        this.width = (double) connection.do_job_get(Lane.getWidth(laneId));
        String allowed = String.valueOf(connection.do_job_get(Lane.getAllowed(laneId)));
        this.allowsPassenger = allowed.contains("passenger") || allowed.isEmpty();
        this.allowsBicycle = allowed.contains("bicycle") || allowed.isEmpty();
        this.allowedClasses = (List<String>) connection.do_job_get(Lane.getAllowed(laneId));
        
    }
        
    public boolean isVehicleAllowed(String vehicleType) {
        if (allowedClasses == null || allowedClasses.isEmpty()) {
            return true;
        }
        return allowedClasses.contains(vehicleType);
    }

    public String getId() { return id; }
    public SumoGeometry getShape() { return geometry; }
    public String getParentEdge() { return parentEdgeId; }
    public double getWidth() { return width; } 
    public boolean isPassengerAllowed() { return allowsPassenger; } 
    public boolean isBicycleAllowed() { return allowsBicycle; } 
    public List<String> getAllowedClasses() { return allowedClasses; }
    
    @Override
    public String toString() {
        return "Lane[ID=" + id + ", Width=" + width + "]";
    }
}