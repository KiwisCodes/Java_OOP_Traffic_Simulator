package model.infrastructure;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.objects.SumoGeometry;
import it.polito.appeal.traci.SumoTraciConnection;
import model.SimulationManager;


//lane and edge have composition relationship (1 cant live without another)
public class LaneClass {
	private static final Logger logger = LogManager.getLogger(LaneClass.class);
    private final String id;
    private final SumoGeometry geometry; 
    private final double width; 
    private final String parentEdgeId;
    private final boolean allowsPassenger; // use for edge class
    private final boolean allowsBicycle; // use for edge class
    // Instead of using each boolean for allowed lane, using a  contains everything 
    private final List<String> allowedClasses;

    public LaneClass(SumoTraciConnection connection, String laneId, String parentEdgeId) throws Exception {
        this.id = laneId;
        this.parentEdgeId = parentEdgeId;


        this.geometry = (SumoGeometry) connection.do_job_get(Lane.getShape(laneId));

        this.width = (double) connection.do_job_get(Lane.getWidth(laneId));

        String allowed = String.valueOf(connection.do_job_get(Lane.getAllowed(laneId)));
//        System.out.println(allowed);
        this.allowsPassenger = allowed.contains("passenger") || allowed.isEmpty();
        this.allowsBicycle = allowed.contains("bicycle") || allowed.isEmpty();
        
        // Sumo return list off all allowed vehicles in this lane:
        this.allowedClasses = (List<String>) connection.do_job_get(Lane.getAllowed(laneId));
        logger.info("Lanes created.");
        
    }
        
       // Function that check the allowed vehices:
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
    public boolean isPassengerAllowed() { return allowsPassenger; } // use for edgeClass 
    public boolean isBicycleAllowed() { return allowsBicycle; } // use for edgeClass
    public List<String> getAllowedClasses() { return allowedClasses; }
    
    @Override
    public String toString() {
        return "Lane[ID=" + id + ", Width=" + width + "]";
    }
}