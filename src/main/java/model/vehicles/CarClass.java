package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.util.Locale; // <--- CRITICAL IMPORT

public class CarClass extends VehicleClass {

    // This is the "DEFAULT_VEHTYPE"
    public CarClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, speed, position, color, edgeId, angle, departureTime);
    }
    
    /**
     * Returns a Node representing the bus, facing UP (-Y), centered at (0,0).
     * Uses Locale.US to ensure SVG paths are valid on all systems.
     * This is AI generated, prompts, usage,  benefit, drawbacks are written in the document
     * @param simColor The body color of the car.
     */
    @Override 
    public Node getShape(Color simColor){
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
            "M 0,-55 " +         // Sharp Nose Tip
            "L 12,-40 " +        // Nose Angle
            "L 22,-37 " +        // Front Fender Flare Front
            "L 22,-20 " +        // Front Fender Flare Back
            "L 18,-10 " +        // Pinched Waist Start
            "L 18,25 " +         // Pinched Waist End
            "L 24,35 " +         // Rear Fender Flare Front
            "L 24,50 " +         // Rear Fender Flare Back
            "L 15,55 " +         // Rear Corner Right
            "L -15,55 " +        // Rear Corner Left - Flat Tail
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
            "M 0,-30 L 5,-20 L -5,-20 Z"
        );
        details.setStroke(TRIM_COLOR);
        details.setStrokeWidth(1.2);
        details.setFill(null);

        // 4. THE CANOPY (Transformed)
        SVGPath canopy = new SVGPath();
        canopy.setContent(
            "M 0,-15 " +         // Windshield Front Point
            "L 14,-5 " +         // A-Pillar Right
            "L 12,20 " +         // Side Window Line Right
            "L 0,30 " +          // Roof Rear Point
            "L -12,20 " +        // Side Window Line Left
            "L -14,-5 Z"         // A-Pillar Left
        );
        canopy.setFill(GLASS_COLOR);

        // 5. LIGHTS (Transformed)
        SVGPath headlights = new SVGPath();
        headlights.setContent(
            // Right Headlight
            "M 14,-40 L 8,-47 L 8,-41 Z " + 
            // Left Headlight
            "M -14,-40 L -8,-47 L -8,-41 Z"
        );
        headlights.setFill(HEADLIGHT_COLOR);

        SVGPath taillights = new SVGPath();
        taillights.setContent(
            // Right Taillight
            "M 15,55 L 15,51 L 10,53 Z " + 
            // Left Taillight
            "M -15,55 L -15,51 L -10,53 Z"
        );
        taillights.setFill(TAILLIGHT_COLOR);

        // 6. MIRRORS (Transformed)
        SVGPath mirrors = new SVGPath();
        mirrors.setContent(
            // Right Mirror
            "M 20,-10 L 26,-7 L 24,-13 Z " +
            // Left Mirror
            "M -20,-10 L -26,-7 L -24,-13 Z"
        );
        mirrors.setFill(TRIM_COLOR);

        // Add all parts
        carGroup.getChildren().addAll(wheels, body, details, canopy, mirrors, headlights, taillights);

        // --- FINAL TRANSFORMS ---
        carGroup.setScaleX(GLOBAL_SCALE);
        carGroup.setScaleY(GLOBAL_SCALE);
        carGroup.setManaged(false); 
        
        return carGroup;
    }
    
    @Override
    public String toString() {
        // FIX: Added Locale.US to ensure dots are used in logs (e.g. "10.50" not "10,50")
        return "Car {" +
               "ID='" + getId() + '\'' +
               ", Speed=" + String.format(Locale.US, "%.2f", getSpeed()) + " m/s" +
               ", Edge='" + getEdgeId() + '\'' +
               ", Pos=(" + String.format(Locale.US, "%.2f", getPosition().x) + ", " + 
                           String.format(Locale.US, "%.2f", getPosition().y) + ")" +
               '}';
    }
    
}