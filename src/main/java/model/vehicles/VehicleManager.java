package model.vehicles;

import de.tudresden.sumo.cmd.Vehicle; 
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.util.SumoCommand;
import de.tudresden.sumo.objects.SumoStringList;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D; 
import de.tudresden.sumo.config.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleManager {
	
	private SumoTraciConnection conn;
	private List<String> vehiclesIds;
	private Map<String, VehicleClass> vehiclesData;
	
	public VehicleManager(SumoTraciConnection connection) {
		this.conn = connection;
		this.vehiclesData = new HashMap<>();
		this.vehiclesIds = new ArrayList<>();
	}
	
	public void step() {
		try {
			SumoCommand idListCmd = Vehicle.getIDList();
			Object response = this.conn.do_job_get(idListCmd);
			
			if (response instanceof SumoStringList) {
				this.vehiclesIds = (SumoStringList) response;
			}
			
			this.vehiclesData = new HashMap<>();
			
			if(this.conn == null || this.conn.isClosed()) {
				return;
			}
			
			this.updateVehiclesInfo();
			
		} catch (IllegalStateException e){
			System.out.println("VehicleManager: Connection closed. Stopping updates.");
	        this.vehiclesIds = new ArrayList<>(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateVehiclesInfo() {
		for (String id: this.vehiclesIds) {
			
			if(this.conn == null || this.conn.isClosed()) {
				return;
			}
			
			try {
				SumoCommand colorCmd = Vehicle.getColor(id);
				SumoColor color = (SumoColor) this.conn.do_job_get(colorCmd);
				
				SumoCommand posCmd = Vehicle.getPosition(id);
				SumoPosition2D position = (SumoPosition2D) this.conn.do_job_get(posCmd);
				
				SumoCommand speedCmd = Vehicle.getSpeed(id);
				double speed = (Double) this.conn.do_job_get(speedCmd);
				
				SumoCommand edgeCmd = Vehicle.getRoadID(id);
				String edgeId = (String) this.conn.do_job_get(edgeCmd);
				
				SumoCommand angleCommand = Vehicle.getAngle(id);
				double angle = (Double) this.conn.do_job_get(angleCommand);
				
				SumoCommand departureCmd = new SumoCommand(
						Constants.CMD_GET_VEHICLE_VARIABLE,
						Constants.VAR_DEPARTURE,
						id,
						Constants.RESPONSE_GET_VEHICLE_VARIABLE,
						Constants.TYPE_DOUBLE
				);
				double departure = (Double) this.conn.do_job_get(departureCmd);
				
				VehicleClass vehicle = new VehicleClass(id, color, position, speed, edgeId, angle, departure);
				
				this.vehiclesData.put(id, vehicle);
				
			} catch (Exception e) {
				System.err.println("Error at Request from Vehicle " + id);
				e.printStackTrace();
			}
		}
	}
	
	public Map<String, VehicleClass> getVehiclesData() {
		return new HashMap<>(this.vehiclesData);
	}
	
	public void injectVehicle(String vehicleId, String typeId, String routeId, SumoColor sumoColor, double Speed) {
		try {
			int depart = 0; 
			double pos = 0.0;
			byte lane = (byte) 0;
			
			SumoCommand addCmd = Vehicle.add(vehicleId, typeId, routeId, depart, pos, Speed, lane);
			this.conn.do_job_set(addCmd);
			
			SumoCommand setColorCmd = Vehicle.setColor(vehicleId, sumoColor);
			this.conn.do_job_set(setColorCmd);
			System.out.println("Vehicle Injected: " + vehicleId);

		} catch (Exception e) {
			System.out.println("Error at Injection of Vehicle " + vehicleId);
			e.printStackTrace();
		}
	}
	
	public int getVehicleCount() {
		try {
	
			SumoCommand idCountCmd = Vehicle.getIDCount();
			return (Integer) this.conn.do_job_get(idCountCmd);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public void printVehiclesData() {
		if (this.vehiclesData.isEmpty()) {
			System.out.println("No vehicles are active");
			return;
		}
		
		System.out.println("----Actual Vehicles Data----");
		
		for (VehicleClass v : this.vehiclesData.values()) {
			System.out.println("ID " + v.getId());
			System.out.println(" - Color: " + v.getColor());
			System.out.println(" - Position: " + v.getPosition().x + ", " + v.getPosition().y);
			System.out.println(" - Speed: " + v.getSpeed());
			System.out.println(" - Edge: " + v.getEdgeId());
			System.out.println("--------------------------");
		}
	}
	
	public void printIdList(int step) {
		if (this.vehiclesIds != null) {
			for (String id : this.vehiclesIds) {
				System.out.println(id);
			}
            System.out.println("Step " + step + " Active Vehicles: " + this.vehiclesIds.size());
		}
	}
}