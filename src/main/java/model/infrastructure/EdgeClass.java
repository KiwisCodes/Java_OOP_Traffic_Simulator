package model.infrastructure;

import de.tudresden.sumo.cmd.Edge;
import de.tudresden.sumo.cmd.Lane;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent an Edge Object on the map
 * 
 * @author khoale
 */
public class EdgeClass {
    private final SumoTraciConnection sumoConnection;
    private final String edgeId;
    private final int laneCount;


    private final Map<String, LaneClass> lanes;

    //we get this permission by going through each lane, yes i think we dont need that, we can just do sumo do job get but this has better structure
    private boolean allowsPassenger;
    private boolean allowsBicycle;
    private boolean isInternal;
    /**
     * Constructor
     * 
     * also it creates Lane Objects that are in this Edge
     * @param sumoConnection: connection with SUMO to get necessary data
     * @param the ID of the edge
     * @throws Exception: throws when communication with SUMO fails
     */
    public EdgeClass(SumoTraciConnection sumoConnection, String edgeId) throws Exception {
        this.sumoConnection = sumoConnection;
        this.edgeId = edgeId;
        this.laneCount = (int) sumoConnection.do_job_get(Edge.getLaneNumber(edgeId));
        this.lanes = new HashMap<>();
        fetchLanes();
    }

    private void fetchLanes() throws Exception {
        this.allowsBicycle = false;
        this.allowsPassenger = false;
        this.isInternal = false;

        for (int i = 0; i < this.laneCount; i++) {
            String laneID = this.edgeId + "_" + i;
            LaneClass lane = new LaneClass(sumoConnection, laneID, this.edgeId);
            this.lanes.put(laneID, lane);

            
            if (laneID.startsWith(":")) this.isInternal = true;
            if (lane.isBicycleAllowed()) this.allowsBicycle = true;
            if (lane.isPassengerAllowed()) this.allowsPassenger = true;
        }
    }

    /**
     * Get Edge Id
     * @return the Id of this Edge
     */
    public String getId() { return edgeId; }
    /**
     * Get number of lane in the Edge
     * @return the number of lane in Edge
     */
    public int getLaneCount() { return laneCount; }
    /**
     * Showing if this edge allow Car type
     * @return true if the Edge allows
     */
    public boolean isPassengerAllowed() { return allowsPassenger; }
    /**
     * Showing if this edge allow Bicycle type
     * @return true if the Edge allows
     */
    public boolean isBicycleAllowed() { return allowsBicycle; }
    /**
     * Showing if this Edge is an Internal Edge
     * @return true if the Edge is an Internal Edge
     */
    public boolean isInternal() { return isInternal; }

    /**
     * Get the Lane Objects in this Edge
     * @return a HashMap of Lane IDs and Lane Objects in this Edge
     */
    public Map<String, LaneClass> getLanes() {
        return new HashMap<>(this.lanes);
    }
    /**
     * Return a string representation of the Edge object
     * @return a String in the format "Edge[ID= edgeId, Number of lanes= laneCount, Internal= true/false]" 
     */
    @Override
    public String toString() {
        return "Edge[ID= " + this.edgeId + ", Number of lanes= " + this.getLaneCount() + ", Internal= " + this.isInternal()+ "]";
    }
}