package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.util.Locale; // <--- CRITICAL IMPORT FOR THE FIX

/**
 * Represents a Bus in the simulation.
 * Includes detailed SVG geometry for visualization.
 */
public class BusClass extends VehicleClass {

    public BusClass(String id, double speed, SumoPosition2D position, SumoColor color, String edgeId, double angle, double departureTime) {
        super(id, speed, position, color, edgeId, angle, departureTime);
    }
    
    /**
     * Returns a Node representing the bus, facing UP (-Y), centered at (0,0).
     * Uses Locale.US to ensure SVG paths are valid on all systems.
     * This is AI generated, prompts, usage,  benefit, drawbacks are written in the document
     * @param simColor The body color of the bus.
     */
    @Override
    public Node getShape(Color simColor) {
        Group busGroup = new Group();

        // ==========================================
        // TUNING VARIABLES
        // ==========================================
        final double BUS_SCALE = 0.05;

        // Dimensions (Half-sizes relative to center 0,0)
        final double BODY_LEN = 75.0;       // Distance from center to nose/tail
        final double BODY_WID = 24.0;       // Half-width
        final double NOSE_CHAMFER = 15.0;   // Length of the angled nose section

        // Wheel Dimensions
        final double WHEEL_Y_OFFSET_F = 50.0; // Front Axle distance
        final double WHEEL_Y_OFFSET_R = 50.0; // Rear Axle distance
        final double WHEEL_LEN = 18.0;

        // Colors
        final Color TIRE_COLOR = Color.web("#121212");
        final Color TRIM_COLOR = Color.BLACK;
        final Color HEADLIGHT_COLOR = Color.WHITE;
        final Color TAILLIGHT_COLOR = Color.web("#D50000");

        // 1. TIRES (Vertical Rectangles)
        SVGPath wheels = new SVGPath();
        double wX_Inner = BODY_WID - 2; 
        double wX_Outer = BODY_WID + 4; 
        double wY_F_Start = -WHEEL_Y_OFFSET_F - (WHEEL_LEN / 2);
        double wY_F_End   = -WHEEL_Y_OFFSET_F + (WHEEL_LEN / 2);
        double wY_R_Start = WHEEL_Y_OFFSET_R - (WHEEL_LEN / 2);
        double wY_R_End   = WHEEL_Y_OFFSET_R + (WHEEL_LEN / 2);

        // FIX: Added Locale.US
        wheels.setContent(String.format(Locale.US,
            // Front Right & Left
            "M %1$f,%3$f L %2$f,%3$f L %2$f,%4$f L %1$f,%4$f Z " +
            "M -%1$f,%3$f L -%2$f,%3$f L -%2$f,%4$f L -%1$f,%4$f Z " +
            // Rear Right & Left
            "M %1$f,%5$f L %2$f,%5$f L %2$f,%6$f L %1$f,%6$f Z " +
            "M -%1$f,%5$f L -%2$f,%5$f L -%2$f,%6$f L -%1$f,%6$f Z",
            wX_Inner, wX_Outer,    // 1, 2 (X)
            wY_F_Start, wY_F_End, // 3, 4 (Front Y)
            wY_R_Start, wY_R_End  // 5, 6 (Rear Y)
        ));
        wheels.setFill(TIRE_COLOR);

        // 2. MAIN BODY (Chamfered Box)
        SVGPath body = new SVGPath();
        double noseY = -BODY_LEN;
        double tailY = BODY_LEN;
        double chamferY = noseY + NOSE_CHAMFER;
        double noseX = BODY_WID - 8; 

        // FIX: Added Locale.US
        body.setContent(String.format(Locale.US,
            "M %1$f,%3$f " +   // Nose Top-Right
            "L %2$f,%4$f " +   // Shoulder Right
            "L %2$f,%5$f " +   // Tail Right
            "L -%2$f,%5$f " +  // Tail Left
            "L -%2$f,%4$f " +  // Shoulder Left
            "L -%1$f,%3$f Z",  // Nose Top-Left
            noseX, BODY_WID,   // 1, 2 (X)
            noseY, chamferY, tailY // 3, 4, 5 (Y)
        ));
        body.setFill(simColor);
        body.setStroke(TRIM_COLOR);
        body.setStrokeWidth(1.0);

        // 3. ROOF BASE (The Single Dark Rectangle)
        SVGPath roofBase = new SVGPath();
        double roofW = BODY_WID - 8.0; // Inset from sides
        double roofY_F = noseY + 12.0; // Inset from front
        double roofY_R = tailY - 12.0; // Inset from back
        double roofChamfer = 6.0;      // Slight chamfer at front to match body

        // FIX: Added Locale.US
        roofBase.setContent(String.format(Locale.US,
            "M %1$f,%3$f " +     // Front Right (Chamfer start)
            "L %2$f,%4$f " +     // Front Right (Chamfer end)
            "L %2$f,%5$f " +     // Rear Right
            "L -%2$f,%5$f " +    // Rear Left
            "L -%2$f,%4$f " +    // Front Left (Chamfer end)
            "L -%1$f,%3$f Z",    // Front Left (Chamfer start)
            roofW - roofChamfer, roofW, // 1, 2 (X)
            roofY_F, roofY_F + roofChamfer, roofY_R // 3, 4, 5 (Y)
        ));
        roofBase.setFill(simColor); // Or GLASS_COLOR if you prefer

        // 4. ROOF VENTS (Black details inside the roof)
        SVGPath roofVents = new SVGPath();
        
        double vWidth = 10.0;   
        double vLenF  = 12.0;   
        double vLenR  = 16.0;   
        
        double vPosF = -32.0;  // Front vent center
        double vPosR = 32.0;   // Rear vent center
        
        // FIX: Added Locale.US
        roofVents.setContent(String.format(Locale.US,
            // Front Vent
            "M -%1$f,%2$f L %1$f,%2$f L %1$f,%3$f L -%1$f,%3$f Z " +
            // Rear Vent
            "M -%1$f,%4$f L %1$f,%4$f L %1$f,%5$f L -%1$f,%5$f Z " +
            // Connecting Line
            "M -0.5,%3$f L 0.5,%3$f L 0.5,%4$f L -0.5,%4$f Z",
            vWidth,                 // 1. Width
            vPosF - vLenF, vPosF + vLenF, // 2, 3 (Front Y Start/End)
            vPosR - vLenR, vPosR + vLenR  // 4, 5 (Rear Y Start/End)
        ));
        roofVents.setFill(Color.BLACK);

        // 5. MIRRORS (Restored "Bigger" Style)
        SVGPath mirrors = new SVGPath();
        double mY = chamferY;   
        double mX = BODY_WID;   
        double mOut = 8.0;      
        double mFwd = 4.0;      
        double mLen = 10.0;      

        // FIX: Added Locale.US
        mirrors.setContent(String.format(Locale.US,
            // Right Mirror
            "M %1$f,%2$f " +      // Base on Body
            "L %3$f,%4$f " +      // Tip Front-Outer
            "L %3$f,%5$f " +      // Tip Rear-Outer
            "L %1$f,%6$f Z " +    // Base Back on Body
            
            // Left Mirror
            "M -%1$f,%2$f " +
            "L -%3$f,%4$f " +
            "L -%3$f,%5$f " +
            "L -%1$f,%6$f Z",
            mX, mY,                 // 1, 2 (Base)
            mX + mOut, mY - mFwd,   // 3, 4 (Outer Tip X, Y)
            mY - mFwd + mLen,       // 5    (Outer Rear Y)
            mY + 8.0                // 6    (Base Rear Y)
        ));
        mirrors.setFill(TRIM_COLOR);

        SVGPath lights = new SVGPath();
        // FIX: Added Locale.US
        lights.setContent(String.format(Locale.US,
            // Headlights
            "M %1$f,%2$f L %3$f,%2$f L %3$f,%4$f L %1$f,%4$f Z " +
            "M -%1$f,%2$f L -%3$f,%2$f L -%3$f,%4$f L -%1$f,%4$f Z",
            noseX - 2, noseY + 1, BODY_WID - 2, noseY + 4
        ));
        lights.setFill(HEADLIGHT_COLOR);

        SVGPath tailLight = new SVGPath();
        // FIX: Added Locale.US
        tailLight.setContent(String.format(Locale.US,
            "M %1$f,%2$f L -%1$f,%2$f L -%1$f,%3$f L %1$f,%3$f Z",
            BODY_WID - 4, tailY - 3, tailY
        ));
        tailLight.setFill(TAILLIGHT_COLOR);

        // Add all parts
        busGroup.getChildren().addAll(wheels, body, roofBase, roofVents, mirrors, lights, tailLight);

        // --- FINAL TRANSFORMS ---
        busGroup.setScaleX(BUS_SCALE);
        busGroup.setScaleY(BUS_SCALE);
        busGroup.setManaged(false); 
        
        return busGroup;
    }
    
    @Override
    public String toString() {
        // Optional: Use Locale.US here too if you want consistent logging format
        return "Bus {" +
               "ID='" + getId() + '\'' +
               ", Speed=" + String.format(Locale.US, "%.2f", getSpeed()) + " m/s" +
               ", Edge='" + getEdgeId() + '\'' +
               ", Pos=(" + String.format(Locale.US, "%.2f", getPosition().x) + ", " + 
                           String.format(Locale.US, "%.2f", getPosition().y) + ")" +
               '}';
    }
}