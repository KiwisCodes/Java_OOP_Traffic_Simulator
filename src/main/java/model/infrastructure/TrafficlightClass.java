package model.infrastructure;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import model.SimulationManager;
import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a single traffic light link controlled by a junction.
 *
 * This class stores the association between a SUMO traffic light junction
 * and one of its controlled links, including lane indices and spatial position.
 *
 * @author khang
 */
public class TrafficlightClass{
	private SumoLink link;  // the link (SumoLink)
	private String host_junction_id; // the traffic light (tls_id)
	private String link_index; // the index of this link (SumoLink) in the traffic light (tls_id)
	private SumoPosition2D link_pos; 
	private static final Logger logger = LogManager.getLogger(SimulationManager.class);
	
	
	/**
     * Creates a traffic light object for a specific controlled link.
     *
     * @param object_link SumoLink controlled by the traffic light junction
     * @param junction_id identifier of the hosting traffic light junction
     * @param index index of the link within the traffic light definition
     */
	public TrafficlightClass(SumoLink object_link, String junction_id, String index) {
		this.link = object_link;
		this.host_junction_id = junction_id;
		this.link_index = index;
	}
	
	/**
     * Returns the id of the hosting traffic light junction.
     *
     * @return traffic light junction id
     */
	public String getHostJunctionId() {
		String result = this.host_junction_id;
		return result;
	}
	
	/**
     * Returns the index of this link within the traffic light definition.
     *
     * @return link index as a string
     */
	public String getLinkIndex() {
		String result = this.link_index;
		return result;
	}
	
	/**
     * Returns the originating lane id of the link.
     *
     * @return source lane id
     */

	public String getFromLaneIndex() {
		String result = this.link.from;
		return result;
	}
	
	/**
     * Returns the destination lane id of the link.
     *
     * @return target lane id
     */
	public String getToLaneIndex() {
		String result = this.link.to;
		return result;
	}

	/**
     * Returns the intermediate lane id crossed by the link.
     *
     * @return intermediate lane id
     */
	public String getOverLaneIndex() {
		String result = this.link.over;
		return result;
	}
	
	/**
     * Sets the spatial position (SumoPosition2D) of the traffic light link.
     *
     * @param pos position in simulation coordinates
     */
	public void setPosition(SumoPosition2D pos) {
		this.link_pos = pos;
		return;
	}
	
	/**
     * Returns the spatial position of the traffic light link.
     *
     * @return position in simulation coordinates
     */
	public SumoPosition2D getPosition() {
		SumoPosition2D result = this.link_pos;
		return result;
	}
	
	/**
     * Returns the underlying SumoLink object.
     *
     * @return controlled SumoLink
     */
	public SumoLink getLinkId() {
		SumoLink result = this.link;
		return result;
	}
}