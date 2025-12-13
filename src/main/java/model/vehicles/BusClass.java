package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.ws.container.SumoPosition2D;

public class BusClass extends VehicleClass{
    public BusClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, color, position, speed, edgeId, angle, departureTime);
    }
    
//    @Override
//    public String toString() {
//        return String.format("[BUS] %s | Spd: %.1f m/s | Edge: %s | Size: Large", 
//                             id, speed, edgeId);
//    }
}