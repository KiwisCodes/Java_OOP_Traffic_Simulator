package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.ws.container.SumoPosition2D;

public class BusClass extends VehicleClass{
    public BusClass(String id, double speed, de.tudresden.sumo.objects.SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, speed, position, color, edgeId, angle, departureTime);
    }
    
//    @Override
//    public String toString() {
//        return String.format("[BUS] %s | Spd: %.1f m/s | Edge: %s | Size: Large", 
//                             id, speed, edgeId);
//    }
}