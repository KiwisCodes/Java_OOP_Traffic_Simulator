package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;

public interface MeansOfTransportation {
    String getId();
    SumoColor getColor();
    SumoPosition2D getPosition();
    double getSpeed();
    String getEdgeId();
    double getAngle();
    double getDeparture();
}