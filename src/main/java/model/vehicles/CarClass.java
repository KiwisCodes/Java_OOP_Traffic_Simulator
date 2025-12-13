package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.ws.container.SumoPosition2D;

public class CarClass extends VehicleClass {
	//this is the "DEFAULT_VEHTYPE"
    public CarClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, color, position, speed, edgeId, angle, departureTime);
    }
    
//    @Override
//    public String toString() {
//        return String.format("[CAR] %s | Spd: %.1f m/s | Edge: %s | Pos: (%.1f, %.1f)", 
//                             id, speed, edgeId, position.x, position.y);
//    }
}