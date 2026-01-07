package model.infrastructure;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.*;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.paint.Color;
import model.ReportManager;



/**
 * Manages traffic light control, duration, and state manipulation
 * for a SUMO simulation using a TraCI connection.
 *
 * @author khang
 */
public class TrafficlightManager {
	private SumoTraciConnection sumoConnection;
	private List<String> trafficlightIdList = new ArrayList<>();
	private List<TrafficlightObject> trafficlightlinkList = new ArrayList<>();
	private Map<String, SumoTLSProgram> program_map = new HashMap<>();
	
	/**
     * Creates a traffic light manager, saves all traffic light
     * junctions and {@link TrafficlightObject} from the SUMO simulation.
     *
     * @param sumoConnection active TraCI connection to the SUMO simulation
     */
	
	public TrafficlightManager(SumoTraciConnection sumoConnection){
		this.sumoConnection = sumoConnection;
		SumoStringList tlsIdList = new SumoStringList();
		try {
			Object result1 = this.sumoConnection.do_job_get(Trafficlight.getIDList());
			tlsIdList = (SumoStringList) result1;
			this.trafficlightIdList.addAll(tlsIdList);
		}
		catch (Exception e) {
//            alertError("SUMO Traffic Light Connection Failed", e.getMessage());
			System.err.println(e.getMessage());
        }
		
		try {
			for(String i: this.trafficlightIdList) {
				SumoLinkList linkIdList = new SumoLinkList();
				Object result2 = this.sumoConnection.do_job_get(Trafficlight.getControlledLinks(i));
				linkIdList = (SumoLinkList) result2;
				int run_var = 0;
				for(SumoLink j : linkIdList) {
					TrafficlightObject tmp = new TrafficlightObject(j, i, Integer.toString(run_var));
					String laneId = tmp.get_from_lane_index();

		            if (laneId != null && !laneId.isEmpty()) {
		                Object tmp_obj = sumoConnection.do_job_get(
		                    Lane.getShape(laneId)   // returns list of coordinates
		                );
		                SumoPosition2D posObj = ((SumoGeometry) tmp_obj).coords.getLast();
		                tmp.set_position(posObj);
		            }
		            trafficlightlinkList.add(tmp);
					run_var++;
				}
			}
		}
		catch (Exception e) {
//            alertError("SUMO Traffic Light Connection Failed", e.getMessage());
			System.err.println(e.getMessage());
        }
	}
	
	/**
     * Returns the list of all traffic light junction IDs.
     *
     * @return list of traffic light IDs
     */
	public List<String> getTrafficlightIdList(){
		return new ArrayList<>(this.trafficlightIdList);
	}
	
	/**
     * Returns all {@link TrafficlightObject} managed by this {@code TrafficlightManager}.
     *
     * @return list of {@link TrafficlightObject}
     */
	public List<TrafficlightObject> getTrafficlightlinkList(){
		return new ArrayList<> (this.trafficlightlinkList);
	}
	
	/**
     * Returns the traffic light program mapping.
     *
     * @return map of traffic light junction IDs to traffic light programs
     */
	public Map<String, SumoTLSProgram> getTrafficlightProgramMap(){
		return new HashMap<> (this.program_map);
	}


	/**
     * Retrieves the current signal state for a specific {@link TrafficlightObject}.
     *
     * @param connection {@link TrafficlightObject} to query
     * @return signal state character of the queried {@link TrafficlightObject}
     */
	public Character getCurrentLightState(TrafficlightObject connection) {
		Character output = 'a';
		try {
			Object result = this.sumoConnection.do_job_get(Trafficlight.getRedYellowGreenState(connection.get_host_junction_id()));
			String tmp = (String) result;
			output = tmp.charAt(Integer.parseInt(connection.get_link_index()));
		}
		catch (Exception e) {
//            alertError("SUMO Get Current Light State Failed", e.getMessage());
			System.err.println(e.getMessage());
        }
		return output;
	}
	
	
	/**
     * Retrieves the full signal state string of a traffic light junction.
     *
     * @param {@link TrafficlightObject} belonging to the junction
     * @return full signal state string
     */
	public String getCurrentLightFullState(TrafficlightObject connection) {
		String output = "";
		try {
			Object result = this.sumoConnection.do_job_get(Trafficlight.getRedYellowGreenState(connection.get_host_junction_id()));
			output = (String) result;
		}
		catch (Exception e) {
//            alertError("SUMO Get Current Light State Failed", e.getMessage());
			System.err.println(e.getMessage());
        }
		return output;
	}
	
	/**
     * Retrieves the simulation time of the next traffic light phase switch.
     *
     * @param connection {@link TrafficlightObject} to query
     * @return time of the next phase switch of the corresponding Traffic Light Junction of the queried {@link TrafficlightObject}
     */
	public double getTrafficLightNextSwitch(TrafficlightObject connection) {
		double output = 0.0;
		try {
			Object result = this.sumoConnection.do_job_get(Trafficlight.getNextSwitch(connection.get_host_junction_id()));
			output = (double) result;
		}
		catch (Exception e) {
//            alertError("SUMO Get Current Light State Failed", e.getMessage());
			System.err.println(e.getMessage());
        }
		return output;
	}
	
	/**
     * Forces the traffic light junction to switch to a phase matching the given signal state of the queried {@code TrafficlightObject}.
     *
     * @param {@link TrafficlightObject} to control
     * @param new_state desired signal state character
     */
	public void setCurrentLightState(TrafficlightObject connection, char new_state) {
	    String cur_state = this.getCurrentLightFullState(connection);

	    try {
	        SumoTLSController controller = (SumoTLSController) this.sumoConnection.do_job_get(Trafficlight.getCompleteRedYellowGreenDefinition(connection.get_host_junction_id()));
	        SumoTLSProgram prog = controller.programs.get("0");
	        int colorIndex = -1;
	        List<SumoTLSPhase> phases = prog.phases;
	        for(int run_var = 0; run_var < phases.size(); run_var++) {
	        		SumoTLSPhase curPhase = phases.get(run_var);
	        		if(curPhase.phasedef.charAt(Integer.parseInt(connection.get_link_index())) == new_state){
	        			colorIndex = run_var;
	        			break;
	        		}
	        }
	        if(colorIndex == -1) {
	        		if(new_state == 'G') {
	        			new_state = 'g';
	        		}
	        		else if(new_state == 'g') {
	        			new_state = 'G';
	        		}
	        		for(int run_var = 0; run_var < phases.size(); run_var++) {
		        		SumoTLSPhase curPhase = phases.get(run_var);
		        		if(curPhase.phasedef.charAt(Integer.parseInt(connection.get_link_index())) == new_state){
		        			colorIndex = run_var;
		        			break;
		        		}
		        }
	        }
	        // Create new program starting from current phase
	        SumoTLSProgram new_prog = new SumoTLSProgram("0", prog.type, 0);

	        for (int run_var = colorIndex; run_var < phases.size(); run_var++) {
	            SumoTLSPhase origPhase = phases.get(run_var);
	            ArrayList<Integer> nextCopy = origPhase.next == null ? null : new ArrayList<>(origPhase.next);
	            SumoTLSPhase newPhase = new SumoTLSPhase(
	                    origPhase.duration,
	                    origPhase.minDur,
	                    origPhase.maxDur,
	                    origPhase.phasedef,
	                    nextCopy,
	                    origPhase.name
	            );
	            new_prog.add(newPhase);
	        }
	        for (int run_var = 0; run_var < colorIndex; run_var++) {
	            SumoTLSPhase origPhase = phases.get(run_var);
	            ArrayList<Integer> nextCopy = origPhase.next == null ? null : new ArrayList<>(origPhase.next);
	            SumoTLSPhase newPhase = new SumoTLSPhase(
	                    origPhase.duration,
	                    origPhase.minDur,
	                    origPhase.maxDur,
	                    origPhase.phasedef,
	                    nextCopy,
	                    origPhase.name
	            );
	            new_prog.add(newPhase);
	            System.out.println(origPhase.duration);
	        }

	        this.sumoConnection.do_job_set(Trafficlight.setCompleteRedYellowGreenDefinition(connection.get_host_junction_id(), new_prog));
	        this.sumoConnection.do_job_set(Trafficlight.setProgram(connection.get_host_junction_id(), "0")); // without this the program will not run recursively your program
	        this.sumoConnection.do_job_set(Trafficlight.setPhase(connection.get_host_junction_id(), 0));

	    } catch (Exception e) {
	        // handle error
			System.err.println(e.getMessage());
	    }
	}

	/**
     * Updates the duration of the current traffic light phase for the whole junction of the queried {@code TrafficlightObject}.
     *
     * @param {@link TrafficlightObject} to control
     * @param newPhaseDuration new phase duration value
     */
	public void setCurrentPhaseDuration(TrafficlightObject connection, double newPhaseDuration) {
		try {
			this.sumoConnection.do_job_set(Trafficlight.setPhaseDuration(connection.get_host_junction_id(), newPhaseDuration));
		}
		catch (Exception e) {
//            alertError("SUMO Set Current Light Phase Duration Failed", e.getMessage());
			System.err.println(e.getMessage());
        }
	}
	
	/**
     * Retrieves the current signal state of all {@code TrafficlightObject}.
     *
     * @return map of {@link TrafficlightObject} to their current signal states
     */
	public Map<TrafficlightObject, Character> getTrafficlightData() {
		Map<TrafficlightObject, Character>result_map = new HashMap<>();
		for(TrafficlightObject i : trafficlightlinkList) {
			
			result_map.put(i, this.getCurrentLightState(i));
		}
		return new HashMap<>(result_map);
	}


}