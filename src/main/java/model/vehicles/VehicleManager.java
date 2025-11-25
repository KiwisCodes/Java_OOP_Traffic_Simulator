package model.vehicles;

import de.tudresden.ws.container.*;

public class VehicleManager {
    
    private String id;
    private SumoPosition2D position; // Using the TraaS object for coordinates
    private double speed;
    private double angle;
    private String color;
    private static int numberOfVehicles = 0;

    public VehicleManager(String id) {
        this.id = id;
        numberOfVehicles++;
    }

    // Abstract method forces subclasses to define their type
    public String getVehicleType() {return "";}	

    // --- Getters and Setters ---
    public String getId() { return id; }
    
    public SumoPosition2D getPosition() { return position; }
    public void setPosition(SumoPosition2D position) { this.position = position; }
    
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public static int getNumberOfVehicles() { return numberOfVehicles; }
}