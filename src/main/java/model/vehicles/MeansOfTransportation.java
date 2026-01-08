package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public interface MeansOfTransportation {
    String getId();
    SumoColor getColor();
    SumoPosition2D getPosition();
    double getSpeed();
    String getEdgeId();
    double getAngle();
    double getDeparture();
    Node getShape(Color simColor);
}