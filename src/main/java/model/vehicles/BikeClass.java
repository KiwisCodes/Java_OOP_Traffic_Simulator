package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import java.util.Locale; // <--- CRITICAL IMPORT FOR THE FIX

public class BikeClass extends VehicleClass {

    public BikeClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, speed, position, color, edgeId, angle, departureTime);
    }
    
    /**
     * Returns a Node representing the bike, facing UP (-Y), centered at (0,0).
     * Uses Locale.US to ensure SVG paths are valid on all systems.
     * @param simColor The team color (Jersey).
     */
    @Override
    public Node getShape(Color simColor) {
        Group bikeGroup = new Group();

        // ==========================================
        // TUNING VARIABLES
        // ==========================================
        final double BIKE_SCALE = 0.08;

        // Colors extracted from the visual style
        final Color TIRE_COLOR  = Color.BLACK;      // "give wheel a black color"
        final Color METAL_COLOR = Color.web("#AAAAAA"); 
        final Color SKIN_COLOR  = Color.web("#FFCCBC"); 
        final Color SADDLE_COLOR = Color.web("#101010"); // "the chair"
        final Color ARM_COLOR   = Color.BLACK;      // Arms in image look like thin black lines

        // Dimensions (Centered at 0,0)
        // Y-Axis: Negative is UP (Front), Positive is DOWN (Rear)
        final double WHEEL_DIST = 18.0; 
        final double TIRE_LEN   = 14.0;
        final double TIRE_WID   = 3.0;
        
        // 1. TIRES (Vertical Black Rectangles)
        // Front: Center (0, -18). Rear: Center (0, 18).
        SVGPath tires = new SVGPath();
        double tX = TIRE_WID / 2.0;
        double tY_Front_Start = -WHEEL_DIST - (TIRE_LEN / 2.0); // -25
        double tY_Front_End   = -WHEEL_DIST + (TIRE_LEN / 2.0); // -11
        double tY_Rear_Start  = WHEEL_DIST - (TIRE_LEN / 2.0);  // 11
        double tY_Rear_End    = WHEEL_DIST + (TIRE_LEN / 2.0);  // 25
        
        // FIX: Added Locale.US
        tires.setContent(String.format(Locale.US,
            // Front Tire
            "M -%1$f,%2$f L %1$f,%2$f L %1$f,%3$f L -%1$f,%3$f Z " +
            // Rear Tire
            "M -%1$f,%4$f L %1$f,%4$f L %1$f,%5$f L -%1$f,%5$f Z",
            tX, tY_Front_Start, tY_Front_End, tY_Rear_Start, tY_Rear_End
        ));
        tires.setFill(TIRE_COLOR);

        // 2. FRAME & HANDLEBARS (Simple Geometry)
        SVGPath frame = new SVGPath();
        double barY = -14.0; 
        double barW = 16.0;  // Width of handlebars
        double barSweep = 4.0; // Curve back
        
        // FIX: Added Locale.US
        frame.setContent(String.format(Locale.US,
            // Handlebar (Curved Arc)
            // Start Left (-16, -10) -> Curve via Center (0, -14) -> End Right (16, -10)
            "M -%1$f,%2$f Q 0,%3$f %1$f,%2$f " +
            // Frame Spine (Connects bars to rear)
            "M 0,%3$f L 0,%4$f",
            barW, barY + barSweep, barY, 12.0
        ));
        frame.setStroke(METAL_COLOR);
        frame.setStrokeWidth(2.0);
        frame.setStrokeLineCap(StrokeLineCap.ROUND);
        frame.setFill(null);

        // 3. SADDLE ("The Chair")
        // Small black shape behind the rider
        SVGPath saddle = new SVGPath();
        saddle.setContent("M -3,8 L 3,8 L 3,14 L -3,14 Z"); // Rectangle behind waist (Simple integers, no locale needed)
        saddle.setFill(SADDLE_COLOR);

        // 4. RIDER ARMS
        // Thin black lines connecting shoulders to handlebars (Visual match to image)
        SVGPath arms = new SVGPath();
        double shoulderY = -2.0;
        double shoulderX = 6.0;
        
        // FIX: Added Locale.US
        arms.setContent(String.format(Locale.US,
            // Left Arm
            "M -%1$f,%2$f L -%3$f,%4$f " +
            // Right Arm
            "M %1$f,%2$f L %3$f,%4$f",
            shoulderX, shoulderY, barW - 2, barY + barSweep - 1
        ));
        arms.setStroke(ARM_COLOR);
        arms.setStrokeWidth(1.5);

        // 5. RIDER BODY (The Green Trapezoid)
        // Matches the "Arrowhead" look in the screenshot
        SVGPath body = new SVGPath();
        double waistX = 3.5;
        double waistY = 8.0;
        
        // FIX: Added Locale.US
        body.setContent(String.format(Locale.US,
            "M -%1$f,%2$f " +  // Shoulder Left
            "L %1$f,%2$f " +   // Shoulder Right
            "L %3$f,%4$f " +   // Waist Right
            "L -%3$f,%4$f Z",  // Waist Left
            shoulderX, shoulderY, waistX, waistY
        ));
        body.setFill(simColor);
        body.setStroke(simColor.darker());
        body.setStrokeWidth(0.5);

        // 6. RIDER HEAD
        // Simple circle centered on shoulders
        Circle head = new Circle(0, shoulderY, 3.8);
        head.setFill(SKIN_COLOR);

        // Add all parts
        // Layering: Tires -> Frame -> Saddle -> Arms -> Body -> Head
        bikeGroup.getChildren().addAll(tires, frame, saddle, arms, body, head);

        // --- FINAL TRANSFORMS ---
        bikeGroup.setScaleX(BIKE_SCALE);
        bikeGroup.setScaleY(BIKE_SCALE);
        bikeGroup.setManaged(false); 
        
        // No rotation needed. Natively faces UP (-Y).
        return bikeGroup;
    }
    
    @Override
    public String toString() {
        // FIX: Added Locale.US for consistent logging format
        return "Bike {" +
               "ID='" + getId() + '\'' +
               ", Speed=" + String.format(Locale.US, "%.2f", getSpeed()) + " m/s" +
               ", Edge='" + getEdgeId() + '\'' +
               ", Pos=(" + String.format(Locale.US, "%.2f", getPosition().x) + ", " + 
                           String.format(Locale.US, "%.2f", getPosition().y) + ")" +
               '}';
    }
}