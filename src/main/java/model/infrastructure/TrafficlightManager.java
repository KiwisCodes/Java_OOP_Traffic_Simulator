package model.infrastructure;

import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.*;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import model.SimulationManager;
import util.SumoException;



/**
 * Manages traffic light control, duration, and state manipulation
 * for a SUMO simulation using a TraCI connection.
 *
 * @author khang
 */
public class TrafficlightManager {
	private SumoTraciConnection sumoConnection;
	private List<String> trafficlightIdList = new ArrayList<>();
	private List<TrafficlightClass> trafficlightlinkList = new ArrayList<>();
	private Map<String, SumoTLSProgram> program_map = new HashMap<>();
	private static final Logger logger = LogManager.getLogger(SimulationManager.class);
	
	/**
     * Creates a traffic light manager, saves all traffic light
     * junctions and {@link TrafficlightClass} from the SUMO simulation.
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
		catch (SumoException e) {
            logger.error("SUMO error fetching traffic light IDs: {}", e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Type error fetching traffic light IDs: {}", e.getMessage());
        } catch (NullPointerException e) {
            logger.error("Null value fetching traffic light IDs: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception fetching traffic light IDs: {}", e.getMessage());
        }
		for (String tlId : this.trafficlightIdList) {
            try {
                Object result2 = this.sumoConnection.do_job_get(Trafficlight.getControlledLinks(tlId));
                SumoLinkList linkIdList = (SumoLinkList) result2;

                int run_var = 0;
                for (SumoLink link : linkIdList) {
                    TrafficlightClass tmp = new TrafficlightClass(link, tlId, Integer.toString(run_var));
                    String laneId = tmp.getFromLaneIndex();

                    if (laneId != null && !laneId.isEmpty()) {
                        try {
                            Object tmp_obj = sumoConnection.do_job_get(Lane.getShape(laneId));
                            SumoPosition2D posObj = ((SumoGeometry) tmp_obj).coords.getLast();
                            tmp.setPosition(posObj);
                        } catch (SumoException e) {
                            logger.error("SUMO error getting lane shape for {}: {}", laneId, e.getMessage());
                        } catch (ClassCastException e) {
                            logger.error("Type error getting lane shape for {}: {}", laneId, e.getMessage());
                        } catch (NullPointerException | IndexOutOfBoundsException e) {
                            logger.error("Invalid lane shape data for {}: {}", laneId, e.getMessage());
                        } catch (Exception e) {
                            logger.error("Unexpected exception getting lane shape for {}: {}", laneId, e.getMessage());
                        }
                    }

                    trafficlightlinkList.add(tmp);
                    run_var++;
                }
            } catch (SumoException e) {
                logger.error("SUMO error getting controlled links for {}: {}", tlId, e.getMessage());
            } catch (ClassCastException e) {
                logger.error("Type error getting controlled links for {}: {}", tlId, e.getMessage());
            } catch (NullPointerException e) {
                logger.error("Null value getting controlled links for {}: {}", tlId, e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected exception getting controlled links for {}: {}", tlId, e.getMessage());
            }
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
     * Returns all {@link TrafficlightClass} managed by this {@link TrafficlightManager}.
     *
     * @return list of {@link TrafficlightClass}
     */
	public List<TrafficlightClass> getTrafficlightlinkList(){
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
     * Retrieves the current signal state for a specific {@link TrafficlightClass}.
     *
     * @param connection {@link TrafficlightClass} to query
     * @return signal state character of the queried {@link TrafficlightClass}
     */
	public Character getCurrentLightState(TrafficlightClass connection) {
		Character output = 'a';
		try {
			Object result = this.sumoConnection.do_job_get(Trafficlight.getRedYellowGreenState(connection.getHostJunctionId()));
			String tmp = (String) result;
			output = tmp.charAt(Integer.parseInt(connection.getLinkIndex()));
		} catch (SumoException e) {
            logger.error("SUMO error getting current light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Type error getting current light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            logger.error("Invalid link index for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (NullPointerException e) {
            logger.error("Null reference getting current light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception getting current light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        }
		return output;
	}
	
	
	/**
     * Retrieves the full signal state string of a traffic light junction.
     *
     * @param {@link TrafficlightClass} belonging to the junction
     * @return full signal state string
     */
	public String getCurrentLightFullState(TrafficlightClass connection) {
		String output = "";
		try {
			Object result = this.sumoConnection.do_job_get(Trafficlight.getRedYellowGreenState(connection.getHostJunctionId()));
			output = (String) result;
		} catch (SumoException e) {
            logger.error("SUMO error getting full light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Type error getting full light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception getting full light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        }
		return output;
	}
	
	/**
     * Retrieves the simulation time of the next traffic light phase switch.
     *
     * @param connection {@link TrafficlightClass} to query
     * @return time of the next phase switch of the corresponding Traffic Light Junction of the queried {@link TrafficlightClass}
     */
	public double getTrafficlightNextSwitch(TrafficlightClass connection) {
		double output = 0.0;
		try {
			Object result = this.sumoConnection.do_job_get(Trafficlight.getNextSwitch(connection.getHostJunctionId()));
			output = (double) result;
		} catch (SumoException e) {
            logger.error("SUMO error getting next switch time for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Type error getting next switch time for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception getting next switch time for {}: {}", connection.getHostJunctionId(), e.getMessage());
        }
		return output;
	}
	
	/**
     * Forces the traffic light junction to switch to a phase matching the given signal state of the queried {@code TrafficlightObject}.
     *
     * @param {@link TrafficlightClass} to control
     * @param new_state desired signal state character
     */
	public void setCurrentLightState(TrafficlightClass connection, char new_state) {
	    String cur_state = this.getCurrentLightFullState(connection);

	    try {
	        SumoTLSController controller = (SumoTLSController) this.sumoConnection.do_job_get(Trafficlight.getCompleteRedYellowGreenDefinition(connection.getHostJunctionId()));
	        SumoTLSProgram prog = controller.programs.get("0");
	        int colorIndex = -1;
	        List<SumoTLSPhase> phases = prog.phases;
	        for(int run_var = 0; run_var < phases.size(); run_var++) {
	        		SumoTLSPhase curPhase = phases.get(run_var);
	        		if(curPhase.phasedef.charAt(Integer.parseInt(connection.getLinkIndex())) == new_state){
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
		        		if(curPhase.phasedef.charAt(Integer.parseInt(connection.getLinkIndex())) == new_state){
		        			colorIndex = run_var;
		        			break;
		        		}
		        }
	        }
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

	        try {
                this.sumoConnection.do_job_set(Trafficlight.setCompleteRedYellowGreenDefinition(connection.getHostJunctionId(), new_prog));
                this.sumoConnection.do_job_set(Trafficlight.setProgram(connection.getHostJunctionId(), "0"));
                this.sumoConnection.do_job_set(Trafficlight.setPhase(connection.getHostJunctionId(), 0));
            } catch (Exception e) {
                logger.error("Unexpected exception setting traffic light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
            }

	    } catch (SumoException e) {
            logger.error("SUMO error preparing light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (ClassCastException e) {
            logger.error("Type error preparing light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            logger.error("Invalid link index preparing light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (NullPointerException e) {
            logger.error("Null reference preparing light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception preparing light state for {}: {}", connection.getHostJunctionId(), e.getMessage());
        }
	}

	/**
     * Updates the duration of the current traffic light phase for the whole junction of the queried {@code TrafficlightObject}.
     *
     * @param {@link TrafficlightClass} to control
     * @param newPhaseDuration new phase duration value
     */
	public void setCurrentPhaseDuration(TrafficlightClass connection, double newPhaseDuration) {
		try {
			this.sumoConnection.do_job_set(Trafficlight.setPhaseDuration(connection.getHostJunctionId(), newPhaseDuration));
		} catch (SumoException e) {
            logger.error("SUMO error setting phase duration for {}: {}", connection.getHostJunctionId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception setting phase duration for {}: {}", connection.getHostJunctionId(), e.getMessage());
        }
	}
	
	/**
     * Retrieves the current signal state of all {@code TrafficlightObject}.
     *
     * @return map of {@link TrafficlightClass} to their current signal states
     */
	public Map<TrafficlightClass, Character> getTrafficlightData() {
		Map<TrafficlightClass, Character>result_map = new HashMap<>();
		for(TrafficlightClass i : trafficlightlinkList) {
			
			result_map.put(i, this.getCurrentLightState(i));
		}
		return new HashMap<>(result_map);
	}


}