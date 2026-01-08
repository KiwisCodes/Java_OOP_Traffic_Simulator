package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.ws.container.SumoPosition2D;

public class MotorbikeClass extends VehicleClass {
    public MotorbikeClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, speed, position, color, edgeId, angle, departureTime);
    }
}
