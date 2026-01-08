package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class CarClass extends VehicleClass {
	//this is the "DEFAULT_VEHTYPE"
    public CarClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, speed, position, color, edgeId, angle, departureTime);
    }
    
    @Override public Node getShape(Color simColor){
    	Group carGroup = new Group();

        // ==========================================
        // TUNING VARIABLES
        // ==========================================
        final double GLOBAL_SCALE = 0.05;    // Size multiplier
        
        // Material Colors
        final Color TIRE_COLOR = Color.web("#121212");
        final Color GLASS_COLOR = Color.web("#0e0e0e"); // Nearly black
        final Color TRIM_COLOR = Color.web("#000000");  // Stark black outlines
        final Color HEADLIGHT_COLOR = Color.web("#E0F7FA"); // Cold LED white
        final Color TAILLIGHT_COLOR = Color.web("#D50000"); // Aggressive Red

        // 1. WHEELS (Transformed: x=y, y=-x)
        // Front wheels are now at negative Y (top), Rear wheels at positive Y (bottom)
        SVGPath wheels = new SVGPath();
        wheels.setContent(
            // Old Front Left (23,-24) -> New Top Left (-24, -23)
            "M -24,-23 L -24,-37 L -16,-37 L -16,-23 Z " + 
            // Old Front Right (23,16) -> New Top Right (16, -23)
            "M 16,-23  L 16,-37  L 24,-37  L 24,-23 Z " + 
            // Old Rear Left (-35,-24) -> New Bottom Left (-24, 35)
            "M -24,35  L -24,21  L -16,21  L -16,35 Z " + 
            // Old Rear Right (-35,16) -> New Bottom Right (16, 35)
            "M 16,35   L 16,21   L 24,21   L 24,35 Z"
        );
        wheels.setFill(TIRE_COLOR);

        // 2. MAIN BODY (Transformed: Nose is now at y=-55, Tail at y=+55)
        SVGPath body = new SVGPath();
        body.setContent(
            "M 0,-55 " +         // Sharp Nose Tip (was 55,0)
            "L 12,-40 " +        // Nose Angle (was 40,12)
            "L 22,-37 " +        // Front Fender Flare Front (was 37,22)
            "L 22,-20 " +        // Front Fender Flare Back (was 20,22)
            "L 18,-10 " +        // Pinched Waist Start (was 10,18)
            "L 18,25 " +         // Pinched Waist End (was -25,18)
            "L 24,35 " +         // Rear Fender Flare Front (was -35,24)
            "L 24,50 " +         // Rear Fender Flare Back (was -50,24)
            "L 15,55 " +         // Rear Corner Right (was -55,15)
            "L -15,55 " +        // Rear Corner Left (was -55,-15) - Flat Tail
            // Mirror Logic (Left Side)
            "L -24,50 " +
            "L -24,35 " +
            "L -18,25 " +
            "L -18,-10 " +
            "L -22,-20 " +
            "L -22,-37 " +
            "L -12,-40 Z"
        );
        body.setFill(simColor);
        body.setStroke(TRIM_COLOR);
        body.setStrokeWidth(0.8);

        // 3. MECHANICAL DETAILS (Transformed)
        SVGPath details = new SVGPath();
        details.setContent(
            // Rear Engine Slats (now at bottom, positive Y)
            "M 0,30 L 0,50 M 5,30 L 8,47 M -5,30 L -8,47 " +
            // Front Hood Vent (now at top, negative Y)
            // Was M 30,0 L 20,5 L 20,-5 Z
            "M 0,-30 L 5,-20 L -5,-20 Z"
        );
        details.setStroke(TRIM_COLOR);
        details.setStrokeWidth(1.2);
        details.setFill(null);

        // 4. THE CANOPY (Transformed)
        SVGPath canopy = new SVGPath();
        canopy.setContent(
            "M 0,-15 " +         // Windshield Front Point (was 15,0)
            "L 14,-5 " +         // A-Pillar Right (was 5,14)
            "L 12,20 " +         // Side Window Line Right (was -20,12)
            "L 0,30 " +          // Roof Rear Point (was -30,0)
            "L -12,20 " +        // Side Window Line Left (was -20,-12)
            "L -14,-5 Z"         // A-Pillar Left (was 5,-14)
        );
        canopy.setFill(GLASS_COLOR);

        // 5. LIGHTS (Transformed)
        SVGPath headlights = new SVGPath();
        headlights.setContent(
            // Right Headlight (was M 40,14...)
            "M 14,-40 L 8,-47 L 8,-41 Z " + 
            // Left Headlight (was M 40,-14...)
            "M -14,-40 L -8,-47 L -8,-41 Z"
        );
        headlights.setFill(HEADLIGHT_COLOR);

        SVGPath taillights = new SVGPath();
        taillights.setContent(
            // Right Taillight (was M -55,15...)
            "M 15,55 L 15,51 L 10,53 Z " + 
            // Left Taillight (was M -55,-15...)
            "M -15,55 L -15,51 L -10,53 Z"
        );
        taillights.setFill(TAILLIGHT_COLOR);

        // 6. MIRRORS (Transformed)
        SVGPath mirrors = new SVGPath();
        mirrors.setContent(
            // Right Mirror (was M 10,20...)
            "M 20,-10 L 26,-7 L 24,-13 Z " +
            // Left Mirror (was M 10,-20...)
            "M -20,-10 L -26,-7 L -24,-13 Z"
        );
        mirrors.setFill(TRIM_COLOR);

        // Add all parts
        carGroup.getChildren().addAll(wheels, body, details, canopy, mirrors, headlights, taillights);

        // --- FINAL TRANSFORMS ---
        carGroup.setScaleX(GLOBAL_SCALE);
        carGroup.setScaleY(GLOBAL_SCALE);
        carGroup.setManaged(false); 
        
        // DIRECTION: ROTATION REMOVED. The SVG geometry now points UP naturally.
        // carGroup.setRotate(-90); <--- REMOVED THIS LINE

        return carGroup;
    }
    
    @Override
    public String toString() {
        return "Car {" +
               "ID='" + getId() + '\'' +
               ", Speed=" + String.format("%.2f", getSpeed()) + " m/s" +
               ", Edge='" + getEdgeId() + '\'' +
               ", Pos=(" + String.format("%.2f", getPosition().x) + ", " + String.format("%.2f", getPosition().y) + ")" +
               '}';
    }
    
}