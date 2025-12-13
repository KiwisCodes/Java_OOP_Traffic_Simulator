package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;

public class VehicleClass {

    private String id;
    private SumoColor color;
    private SumoPosition2D position;
    private double speed;
    private String edgeId;
    private double angle;
    private double departure;

    public VehicleClass(String id, SumoColor color, SumoPosition2D position, double speed, String edgeId, double angle, double departure) {
        this.id = id;
        this.color = color;
        this.position = position;
        this.speed = speed;
        this.edgeId = edgeId;
        this.angle = angle;
        this.departure = departure;
    }

    public String getId() { return id; }
    public SumoColor getColor() { return color; }
    public SumoPosition2D getPosition() { return position; }
    public double getSpeed() { return speed; }
    public String getEdgeId() { return edgeId; }
    public double getAngle() { return angle; }
    public double getDeparture() { return departure; }

    @Override
    public String toString() {
        return "Vehicle [id=" + id + ", speed=" + speed + ", edge=" + edgeId + "]";
    }
}