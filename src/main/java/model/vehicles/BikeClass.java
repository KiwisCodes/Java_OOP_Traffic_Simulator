package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.ws.container.SumoPosition2D;

public class BikeClass extends VehicleClass{
    public BikeClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, color, position, speed, edgeId, angle, departureTime);
    }
    
//    @Override
//    public String toString() {
//        return String.format("[BIKE] %s | Spd: %.1f m/s | Edge: %s ", 
//                             id, speed, edgeId);
//    }
}