package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;

public class PedestrianClass implements MeansOfTransportation {

    private String id;
    private SumoColor color;
    private SumoPosition2D position;
    private double speed;
    private String edgeId;
    private double angle;
    private double departure;

    public PedestrianClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departure) {
        this.id = id;
        this.color = color;
        this.position = position;
        this.speed = speed;
        this.edgeId = edgeId;
        this.angle = angle;
        this.departure = departure;
    }


    @Override public String getId() { return id; }
    @Override public SumoColor getColor() { return color; }
    @Override public SumoPosition2D getPosition() { return position; }
    @Override public double getSpeed() { return speed; }
    @Override public String getEdgeId() { return edgeId; }
    @Override public double getAngle() { return angle; }
    @Override public double getDeparture() { return departure; }
    
    @Override
    public String toString() {
        return "Pedestrian [id=" + id + " @ " + edgeId + "]";
    }
}