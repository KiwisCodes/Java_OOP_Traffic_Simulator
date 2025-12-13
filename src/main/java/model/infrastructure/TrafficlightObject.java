package model.infrastructure;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;


public class TrafficlightObject{
	private SumoLink link;  // the link (SumoLink)
	private String host_junction_id; // the traffic light (tls_id)
	private String link_index; // the index of this link (SumoLink) in the traffic light (tls_id)
	private SumoPosition2D link_pos; 
	public TrafficlightObject(SumoLink object_link, String junction_id, String index) {
		this.link = object_link;
		this.host_junction_id = junction_id;
		this.link_index = index;
	}
	
	public String get_host_junction_id() {
		String result = this.host_junction_id;
		return result;
	}
	
	public String get_link_index() {
		String result = this.link_index;
		return result;
	}
	
	public String get_from_lane_index() {
		String result = this.link.from;
		return result;
	}
	
	public String get_to_lane_index() {
		String result = this.link.to;
		return result;
	}

	public String get_over_lane_index() {
		String result = this.link.over;
		return result;
	}
	
	public void set_position(SumoPosition2D pos) {
		this.link_pos = pos;
		return;
	}
	
	public SumoPosition2D get_position() {
		SumoPosition2D result = this.link_pos;
		return result;
	}
	
	public SumoLink get_link_id() {
		SumoLink result = this.link;
		return result;
	}
}