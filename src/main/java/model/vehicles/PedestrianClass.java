package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;

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
    @Override public Node getShape(Color color) {
    	double s = 1.2; 
    	Group pedestrianGroup = new Group();
    	Ellipse shoulders = new Ellipse(0, 0, 0.6 * s, 0.3 * s);
        shoulders.setFill(color);
        shoulders.setStroke(Color.BLACK);
        shoulders.setStrokeWidth(0.2);
        Circle head = new Circle(0, 0, 0.3 * s, Color.PEACHPUFF);

        pedestrianGroup.getChildren().addAll(shoulders, head);
        return pedestrianGroup;
    }
    
    @Override
    public String toString() {
        return "Pedestrian {" +
               "ID='" + id + '\'' +
               ", Speed=" + String.format("%.2f", speed) + " m/s" +
               ", Edge='" + edgeId + '\'' +
               ", Pos=(" + String.format("%.2f", position.x) + ", " + String.format("%.2f", position.y) + ")" +
               '}';
    }
}