package view;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.Map;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow; 
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polyline;   
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Lane;           
import de.tudresden.sumo.cmd.Junction;      
import de.tudresden.sumo.objects.SumoGeometry;  
import de.tudresden.sumo.objects.SumoPosition2D;
import model.vehicles.BikeClass;
import model.vehicles.BusClass;
import model.vehicles.CarClass;
import model.vehicles.MeansOfTransportation;
import model.vehicles.PedestrianClass;
import model.vehicles.VehicleClass;
import util.ColorConverter;
import model.infrastructure.*;
import util.CoordinateConverter;
import de.tudresden.sumo.cmd.Trafficlight; 
import de.tudresden.sumo.cmd.Junction;    
import javafx.scene.shape.Circle;          
import de.tudresden.sumo.objects.SumoColor;     
import javafx.scene.shape.Polygon;   
import java.util.Map;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;

public class Renderer {
	// Top of your Renderer class
//	private static final Image CAR_IMAGE = new Image("/images/car_gemini.png");
//	private static final Image BIKE_IMAGE = new Image("/images/bike.png");
	
    public void setConverter(MapManager mapManager) {
        this.converter.setBound(mapManager);
    }
    public CoordinateConverter getConverter() {
        return this.converter;
    }
    
    private CoordinateConverter converter; 
    
    private static final DropShadow HOVER_GLOW = new DropShadow();
    
    private Map<Character, Color> tl_color_map = new HashMap<>(); // map the state of each traffic light to each color

    public Renderer() {
        this.converter = new CoordinateConverter(); 
        HOVER_GLOW.setColor(Color.CYAN);
        HOVER_GLOW.setRadius(10);
        HOVER_GLOW.setSpread(0.6);
        
    	//Khang's
  		this.tl_color_map.put('r', Color.rgb(255, 80, 80));            // bright_red

  		this.tl_color_map.put('y', Color.rgb(255, 255, 120));          // yellow

  		this.tl_color_map.put('g', Color.GREEN);                       // green
  		this.tl_color_map.put('G', Color.rgb(120, 255, 120));          // bright_green

  		// JavaFX has no blinking colors. You must implement blinking using transitions.
  		// Here: normal + brighter versions.
  		this.tl_color_map.put('o', Color.rgb(255, 200, 0));            // blinking_yellow (base)
  		this.tl_color_map.put('O', Color.rgb(255, 230, 50));           // bright_blinking_yellow (base)

  		this.tl_color_map.put('a', Color.rgb(139, 0, 0));              // dark_red (≈ Firebrick / DarkRed)
  		this.tl_color_map.put('b', Color.rgb(184, 134, 11));           // dark_yellow (≈ DarkGoldenRod)
  		this.tl_color_map.put('c', Color.rgb(0, 100, 0));              // dark_green (≈ DarkGreen)
    }



    /**
     * Renders all lanes onto the visualization map by categorizing them into specific UI layers (Panes)
     * based on their vehicle access permissions.
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Clears all existing children in car, bike, and mixed panes.</li>
     * <li>Iterates through the provided {@code laneData}.</li>
     * <li>Filters out internal junction lanes (IDs starting with ":").</li>
     * <li>Generates a {@link Shape} for each valid lane.</li>
     * <li>Assigns the shape to the appropriate pane:
     * <ul>
     * <li><b>Mixed Pane:</b> If both cars and bikes are allowed.</li>
     * <li><b>Bike Pane:</b> If only bikes are allowed.</li>
     * <li><b>Car Pane:</b> If only cars are allowed.</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * * <b>Note:</b> Lanes that do not fall into the above categories (e.g., restricted roads) 
     * are added to the {@code carPane} but set to be <b>mouse-transparent</b> (non-interactive).
     *
     * @param laneData A map containing lane IDs and their corresponding {@code LaneClass} properties.
     * @param laneClickHandler A consumer callback to handle mouse click events on the generated lane shapes.
     */
    
    public void renderLanes(Map<String, LaneClass> laneData, Pane lanePane, Consumer<LaneClass> laneClickHandler) {
        // 1. Xóa các lane cũ
        lanePane.getChildren().clear();
        
        System.out.println("Renderer: Drawing lanes...");

        try {
            // 2. Duyệt qua danh sách laneId
            for (String laneId : laneData.keySet()) {
                // Lọc bỏ làn nội bộ (Internal Lanes)
                if (laneId.startsWith(":")) continue; 

                LaneClass props = laneData.get(laneId);
                if (props == null) continue;

                // 3. Gọi hàm tạo hình (Hàm này bạn đã viết riêng)
                Shape laneShape = createLaneShape(props, laneData, laneClickHandler);
                
                if (laneShape != null) {
                    lanePane.getChildren().add(laneShape);
                }
            }
            
            // Lệnh này phải nằm TRONG hàm và SAU vòng lặp
            System.out.println("Renderer: Done drawing lanes.");

        } catch (Exception e) {
            // Bắt lỗi chung cho quá trình vẽ
            e.printStackTrace();
        }
    } // Kết thúc hàm renderLanes
    
    /**
     * Constructs a graphical representation (Shape) of a specific lane to be rendered on the map.
     * <p>
     * This method orchestrates the visual creation process by:
     * <ol>
     * <li>Retrieving the raw geometry (list of X, Y coordinates) from the SUMO simulation data.</li>
     * <li>Converting these real-world coordinates into JavaFX screen coordinates using the {@code converter}.</li>
     * <li>Creating a {@link Polyline} and applying visual styles (stroke width, color, line caps).</li>
     * <li>Attaching mouse interaction logic (Hover effects and Click delegation).</li>
     * </ol>
     * </p>
     *
     * @param props       The {@code LaneClass} object containing the lane's properties (ID, width, geometry shape).
     * @param laneData    The map containing data for all lanes (used for context if necessary).
     * @param onLaneClick A {@code Consumer} callback used to handle mouse click events.
     * <br><b>Note on Architecture:</b> This parameter facilitates a <i>delegation pattern</i>.
     * The {@code Renderer} detects the click event, but delegates the actual business logic 
     * (such as updating text fields in the UI) back to the {@code MainController} via this callback.
     * @return A fully styled {@link Shape} (specifically a {@link Polyline}) ready to be added to the UI pane, 
     * or {@code null} if the geometry data is invalid.
     */
    
    private Shape createLaneShape(LaneClass props, Map<String,LaneClass> laneData,Consumer<LaneClass> onLaneClick) {
        try {
            SumoGeometry geometry = props.getShape();
            Polyline lanePolyline = new Polyline();

            for (SumoPosition2D pos : geometry.coords) {
                double realX = pos.x; 
                double realY = pos.y;

                double screenX = converter.toScreenX(realX);
                double screenY = converter.toScreenY(realY);

                lanePolyline.getPoints().addAll(screenX, screenY);
            }
            double laneWidth = props.getWidth();
            lanePolyline.setStroke(Color.rgb(75, 75, 75)); 
            lanePolyline.setStrokeWidth(laneWidth);   
            lanePolyline.setStrokeLineCap(StrokeLineCap.ROUND);
            lanePolyline.setUserData(props);

            lanePolyline.setOnMouseEntered(e -> {
                lanePolyline.setEffect(HOVER_GLOW);
                lanePolyline.setStroke(Color.LIGHTGRAY);
                lanePolyline.setCursor(Cursor.HAND);
            });
            lanePolyline.setOnMouseExited(e -> {
                lanePolyline.setEffect(null);
                lanePolyline.setStroke(Color.rgb(75, 75, 75)); 
                lanePolyline.setCursor(Cursor.DEFAULT);
            });
            
            lanePolyline.setOnMouseClicked(e -> {
            	onLaneClick.accept(props);
            });

            return lanePolyline;
        } catch (Exception e) {
            return null; 
        }
    }


        /**
         * Renders all valid junctions (intersections) onto the visualization map.
         * <p>
         * <b>Processing Steps:</b>
         * <ol>
         * <li>Clears the {@code junctionPane} to remove old artifacts.</li>
         * <li>Iterates through the provided {@code junctionData}.</li>
         * <li><b>Filtering:</b> Skips internal SUMO junctions (IDs starting with ":") to avoid visual clutter.</li>
         * <li>Generates a geometric {@link Shape} for each valid junction.</li>
         * <li>Attaches a mouse click listener to delegate the selection event back to the Controller via {@code onJunctionClick}.</li>
         * <li>Adds the generated shape directly to the UI pane.</li>
         * </ol>
         * </p>
         *
         * @param junctionData    A map containing junction IDs and their properties (geometry, position).
         * @param junctionPane    The JavaFX {@link Pane} layer dedicated to displaying junctions.
         * @param onJunctionClick A callback (Consumer) to handle user interactions.
         * <br>When a user clicks a junction, its ID is passed to this consumer, allowing the MainController to react (e.g., show details).
         */
    public void renderJunctions(Map<String,JunctionClass>junctionData, Pane junctionPane, Consumer<String> onJunctionClick) {
        junctionPane.getChildren().clear();

        try {
            for (String juncId : junctionData.keySet()) {
                if (juncId.startsWith(":")) continue;
                
                JunctionClass props = junctionData.get(juncId);
                Shape junctionShape = createJunctionShape(props);
                
                if (junctionShape != null) {
                    junctionShape.setOnMouseClicked(e -> {
                        if(onJunctionClick != null) onJunctionClick.accept(juncId);
                    });
                    junctionPane.getChildren().add(junctionShape);
                }
            }
            System.out.println("Renderer: Done Drawing Junctions.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Constructs the graphical representation (Polygon) for a single junction based on its geometry.
     * * <p>
     * This method converts real-world SUMO coordinates into screen pixel coordinates 
     * to form a closed {@link Polygon}.
     * </p>
     *
     * @param props The {@code JunctionClass} object containing the junction's ID and geometry shape.
     * @return A styled {@link Polygon} representing the intersection, ready for rendering.
     * Returns {@code null} if the junction has no geometry data or is invalid.
     */
    private Shape createJunctionShape(JunctionClass props) {
        try {
        	SumoGeometry geometry = props.getShape();
        	if (geometry == null || geometry.coords.isEmpty()) {
                return null;
            }
            Polygon junctionShape = new Polygon();
        	for (SumoPosition2D pos : geometry.coords) {
                double realX = pos.x; 
                double realY = pos.y;
                double screenX = converter.toScreenX(realX);
                double screenY = converter.toScreenY(realY);
                junctionShape.getPoints().addAll(screenX, screenY);
            }
        	
        	junctionShape.setFill(Color.rgb(80, 80, 80)); 
            junctionShape.setStrokeWidth(0.5);
            junctionShape.setUserData(props.getId());
            
            return junctionShape;
        } catch (Exception e) {
            return null;
        }
    }
    
    
    private Map<String, Node> vehicleVisualCache = new HashMap<>();
    
    /**
     * Renders and synchronizes the visual representation of vehicles on the map based on the latest simulation state.
     * <p>
     * <b>Performance Optimization Strategy:</b>
     * Instead of clearing and redrawing all vehicles every frame (which is computationally expensive), 
     * this method employs a <b>Caching Mechanism</b> ({@code vehicleVisualCache}) to synchronize the UI state:
     * <ul>
     * <li><b>Garbage Collection:</b> Identifies and removes vehicles that are present in the cache 
     * but no longer exist in the new {@code vehicleData} (i.e., vehicles that have left the simulation).</li>
     * <li><b>Update vs. Create:</b> 
     * <ul>
     * <li>If a vehicle ID exists in the cache, its existing {@link Polygon} shape is updated with new coordinates and rotation (Low cost).</li>
     * <li>If a vehicle ID is new, a new {@link Polygon} shape is instantiated, styled, and added to the cache (One-time cost).</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param vehiclePane The JavaFX {@link Pane} layer dedicated to displaying vehicles.
     * @param vehicleData A map containing the latest snapshot of vehicle data (ID -> Vehicle Properties) from the simulation core.
     */
    public void renderVehicles(Pane vehiclePane, Map<String, MeansOfTransportation> vehicleData, List<String> validIDs, boolean isFilterApplied) {
        if (vehicleData == null || vehicleData.isEmpty()) {
            vehiclePane.getChildren().clear();  
            vehicleVisualCache.clear();
            return;
        }

        java.util.Set<String> visibleIDs = isFilterApplied ? 
            (validIDs == null ? new java.util.HashSet<>() : new java.util.HashSet<>(validIDs)) : 
            vehicleData.keySet();

        // 1. CLEANUP
        List<String> toRemove = new ArrayList<>();
        for (String cachedId : vehicleVisualCache.keySet()) {
            if (!vehicleData.containsKey(cachedId) || (isFilterApplied && !visibleIDs.contains(cachedId))) { 
                toRemove.add(cachedId);
            }
        }
        for (String id : toRemove) {
            vehiclePane.getChildren().remove(vehicleVisualCache.get(id)); 
            vehicleVisualCache.remove(id);     
        }

        // 2. DRAW / UPDATE
        for (String vehicleId : visibleIDs) {
            MeansOfTransportation props = vehicleData.get(vehicleId);
            try {
                double screenX = converter.toScreenX(props.getPosition().x);
                double screenY = converter.toScreenY(props.getPosition().y);
                double angle = props.getAngle(); 
                Color fxColor = ColorConverter.toFXColor(props.getColor());

                Node vehicleNode = vehicleVisualCache.get(vehicleId);

                if (vehicleNode != null) {
                    // Update existing
                    vehicleNode.setTranslateX(screenX); 
                    vehicleNode.setTranslateY(screenY); 
                    vehicleNode.setRotate(angle); 
                } else {
                    // Create new detailed shape
//                    vehicleNode = createVehicleShape(props, fxColor);
                	vehicleNode = this.createMeansOfTransportationShape(props, fxColor);
                    
                    vehicleNode.setTranslateX(screenX);
                    vehicleNode.setTranslateY(screenY);
                    vehicleNode.setRotate(angle);
                    vehicleNode.setUserData(props);

                    // Re-add your hover effects and click events
                    setupVehicleEvents(vehicleNode);

                    vehiclePane.getChildren().add(vehicleNode);
                    vehicleVisualCache.put(vehicleId, vehicleNode);
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    // Helper for cleaner code
    private void setupVehicleEvents(Node node) {
        node.setOnMouseEntered(e -> {
            node.setEffect(HOVER_GLOW);
            node.setCursor(Cursor.HAND);
        });
        node.setOnMouseExited(e -> {
            node.setEffect(null);
            node.setCursor(Cursor.DEFAULT);
        });
        node.setOnMouseClicked(e -> {
            VehicleClass info = (VehicleClass) node.getUserData();
//            log("Clicked Vehicle: " + info.getId());
        });
    }
	
    private Node createMeansOfTransportationShape(MeansOfTransportation meansOfTransportation, Color color) {
        Node visual = meansOfTransportation.getShape(color);
        if(meansOfTransportation instanceof PedestrianClass) {
        	return visual;
        }
        
//        visual.setRotate(-90);
        return visual;
    }
    
//    private Node createCarFromSVG(Color simColor) {
//        Group carGroup = new Group();
//
//        // ==========================================
//        // TUNING VARIABLES
//        // ==========================================
//        final double GLOBAL_SCALE = 0.05;    // Size multiplier
//        
//        // Material Colors
//        final Color TIRE_COLOR = Color.web("#121212");
//        final Color GLASS_COLOR = Color.web("#0e0e0e"); // Nearly black
//        final Color TRIM_COLOR = Color.web("#000000");  // Stark black outlines
//        final Color HEADLIGHT_COLOR = Color.web("#E0F7FA"); // Cold LED white
//        final Color TAILLIGHT_COLOR = Color.web("#D50000"); // Aggressive Red
//
//        // 1. WHEELS (Shifted -5 on X axis to center the mass)
//        SVGPath wheels = new SVGPath();
//        wheels.setContent(
//            "M 23,-24 L 37,-24 L 37,-16 L 23,-16 Z " + // Front Left
//            "M 23,16  L 37,16  L 37,24  L 23,24 Z " + // Front Right
//            "M -35,-24 L -21,-24 L -21,-16 L -35,-16 Z " + // Rear Left
//            "M -35,16  L -21,16  L -21,24  L -35,24 Z"    // Rear Right
//        );
//        wheels.setFill(TIRE_COLOR);
//
//        // 2. MAIN BODY (Centered: Nose +55, Tail -55)
//        SVGPath body = new SVGPath();
//        body.setContent(
//            "M 55,0 " +          // Sharp Nose Tip
//            "L 40,12 " +         // Nose Angle
//            "L 37,22 " +         // Front Fender Flare (Front)
//            "L 20,22 " +         // Front Fender Flare (Back)
//            "L 10,18 " +         // Pinched Waist Start
//            "L -25,18 " +        // Pinched Waist End
//            "L -35,24 " +        // Rear Fender Flare (Front)
//            "L -50,24 " +        // Rear Fender Flare (Back)
//            "L -55,15 " +        // Rear Corner
//            "L -55,-15 " +       // Rear Width (Flat Tail)
//            // Mirror Logic (Negative Y)
//            "L -50,-24 " +
//            "L -35,-24 " +
//            "L -25,-18 " +
//            "L 10,-18 " +
//            "L 20,-22 " +
//            "L 37,-22 " +
//            "L 40,-12 Z"
//        );
//        body.setFill(simColor);
//        body.setStroke(TRIM_COLOR);
//        body.setStrokeWidth(0.8);
//
//        // 3. MECHANICAL DETAILS (Engine slats & Hood vent)
//        SVGPath details = new SVGPath();
//        details.setContent(
//            // Rear Engine Slats (Shifted -5)
//            "M -30,0 L -50,0 M -30,5 L -47,8 M -30,-5 L -47,-8 " +
//            // Front Hood Vent (Shifted -5)
//            "M 30,0 L 20,5 L 20,-5 Z"
//        );
//        details.setStroke(TRIM_COLOR);
//        details.setStrokeWidth(1.2);
//        details.setFill(null);
//
//        // 4. THE CANOPY (Hexagonal Fighter Jet Style)
//        SVGPath canopy = new SVGPath();
//        canopy.setContent(
//            "M 15,0 " +          // Windshield Front Point
//            "L 5,14 " +          // A-Pillar
//            "L -20,12 " +        // Side Window Line
//            "L -30,0 " +         // Roof Rear Point
//            "L -20,-12 " +       // Side Window Line
//            "L 5,-14 Z"          // A-Pillar
//        );
//        canopy.setFill(GLASS_COLOR);
//
//        // 5. LIGHTS (Shifted -5)
//        SVGPath headlights = new SVGPath();
//        headlights.setContent(
//            "M 40,14 L 47,8 L 41,8 Z " + 
//            "M 40,-14 L 47,-8 L 41,-8 Z"
//        );
//        headlights.setFill(HEADLIGHT_COLOR);
//
//        SVGPath taillights = new SVGPath();
//        taillights.setContent(
//            "M -55,15 L -51,15 L -53,10 Z " + 
//            "M -55,-15 L -51,-15 L -53,-10 Z"
//        );
//        taillights.setFill(TAILLIGHT_COLOR);
//
//        // 6. MIRRORS (Shifted -5)
//        SVGPath mirrors = new SVGPath();
//        mirrors.setContent(
//            "M 10,20 L 7,26 L 13,24 Z " +
//            "M 10,-20 L 7,-26 L 13,-24 Z"
//        );
//        mirrors.setFill(TRIM_COLOR);
//
//        // Add all parts
//        carGroup.getChildren().addAll(wheels, body, details, canopy, mirrors, headlights, taillights);
//
//        // --- FINAL TRANSFORMS ---
//        carGroup.setScaleX(GLOBAL_SCALE);
//        carGroup.setScaleY(GLOBAL_SCALE);
//        carGroup.setManaged(false); 
//        
//        // DIRECTION: Point the nose (+X) to the Top of the Screen (-Y)
//        carGroup.setRotate(-90);
//
//        return carGroup;
//    }
//    
//    private Node createBikeFromSVG(Color simColor) {
//        Group bikeGroup = new Group();
//
//        // ==========================================
//        // TUNING VARIABLES
//        // ==========================================
//        final double BIKE_SCALE = 0.08; 
//
//        // --- Dimensions ---
//        final double WHEEL_OFFSET = 18.0;   // Distance to axles
//        final double TIRE_LEN     = 14.0;
//        final double TIRE_WID     = 2.8;    // Slightly thicker tires
//        
//        // Frame Geometry (The Diamond Shape)
//        final double STEM_X       = 13.0;   // Front of frame
//        final double REAR_HUB_X   = -18.0;  // Rear of frame
//        final double CRANK_WID    = 3.5;    // Width of frame at center (pedals)
//        
//        // Handlebars
//        final double BAR_WIDTH    = 17.0;
//        final double BAR_SWEEP    = 6.0;
//        
//        // Rider Anatomy
//        final double SHOULDER_X   = 2.0;    // Shoulders forward
//        final double SHOULDER_W   = 6.5;    // Broad shoulders
//        final double WAIST_X      = -7.0;   // Near seat
//        final double WAIST_W      = 3.5;    // Narrow waist
//        
//        final double HAND_X       = STEM_X - BAR_SWEEP; 
//        final double HAND_Y       = BAR_WIDTH;
//
//        // --- Colors ---
//        final Color TIRE_COLOR   = Color.web("#121212");
//        final Color FRAME_COLOR  = Color.web("#222222"); // Carbon Fiber Dark Grey
//        final Color METAL_COLOR  = Color.web("#AAAAAA"); // Bars/Mechanicals
//        final Color SKIN_COLOR   = Color.web("#FFCCBC"); 
//        final Color HAIR_COLOR   = Color.web("#202020"); 
//        final Color JERSEY_COLOR = simColor;             // Team Color
//        final Color SHORT_COLOR  = Color.web("#151515"); // Cycling shorts (Thighs)
//
//        // 1. TIRES (Rounded Rectangles)
//        SVGPath tires = new SVGPath();
//        double tLx = TIRE_LEN / 2;
//        double tWy = TIRE_WID / 2;
//        tires.setContent(String.format(
//            "M %1$f,-%2$f L %3$f,-%2$f L %3$f,%2$f L %1$f,%2$f Z " + // Front
//            "M %4$f,-%2$f L %5$f,-%2$f L %5$f,%2$f L %4$f,%2$f Z",   // Rear
//            WHEEL_OFFSET - tLx, tWy, WHEEL_OFFSET + tLx, 
//            -WHEEL_OFFSET - tLx, -WHEEL_OFFSET + tLx
//        ));
//        tires.setFill(TIRE_COLOR);
//
//        // 2. FRAME (The "Diamond" Structure)
//        SVGPath frame = new SVGPath();
//        frame.setContent(String.format(
//            "M %1$f,0 " +          // 1. Head Tube (Front)
//            "L 0,%2$f " +          // 2. Crank Right (Widest point)
//            "L %3$f,0 " +          // 3. Rear Dropouts
//            "L 0,-%2$f Z",         // 4. Crank Left
//            STEM_X, CRANK_WID, REAR_HUB_X + 4 // +4 ensures it doesn't touch the very back of tire
//        ));
//        frame.setFill(FRAME_COLOR);
//        frame.setStroke(Color.BLACK);
//        frame.setStrokeWidth(0.5);
//
//        // 3. MECHANICALS (Crankset/Pedals center)
//        SVGPath mechanics = new SVGPath();
//        mechanics.setContent("M -2,-2 L 2,-2 L 2,2 L -2,2 Z"); // Center block
//        mechanics.setFill(METAL_COLOR);
//
//        // 4. HANDLEBARS (Swept Curve)
//        SVGPath handlebars = new SVGPath();
//        handlebars.setContent(String.format(
//            "M %1$f,-%2$f Q %3$f,0 %1$f,%2$f",
//            HAND_X, HAND_Y, STEM_X + 2.5
//        ));
//        handlebars.setStroke(METAL_COLOR);
//        handlebars.setStrokeWidth(2.0);
//        handlebars.setStrokeLineCap(StrokeLineCap.ROUND);
//        handlebars.setFill(null);
//
//        // 5. RIDER THIGHS (Cycling Shorts - adds connection to bike)
//        SVGPath thighs = new SVGPath();
//        thighs.setContent(String.format(
//            "M %1$f,%2$f L %3$f,%4$f L %3$f,-%4$f L %1$f,-%2$f Z",
//            WAIST_X, WAIST_W, WAIST_X - 3, WAIST_W + 1 // Small flared rects near seat
//        ));
//        thighs.setFill(SHORT_COLOR);
//
//        // 6. RIDER TORSO (The "Jersey" - Trapezoid)
//        SVGPath torso = new SVGPath();
//        torso.setContent(String.format(
//            "M %1$f,%2$f " +     // Shoulder Right
//            "L %3$f,%4$f " +     // Waist Right
//            "L %3$f,-%4$f " +    // Waist Left
//            "L %1$f,-%2$f " +    // Shoulder Left
//            "L %5$f,0 Z",        // Neck (Front Point)
//            SHOULDER_X, SHOULDER_W, // 1,2
//            WAIST_X, WAIST_W,       // 3,4
//            SHOULDER_X + 1.5        // 5
//        ));
//        torso.setFill(JERSEY_COLOR);
//        torso.setStroke(JERSEY_COLOR.darker());
//        torso.setStrokeWidth(0.5);
//
//        // 7. RIDER ARMS (Sleeves)
//        SVGPath arms = new SVGPath();
//        arms.setContent(String.format(
//            // Right Arm
//            "M %1$f,%2$f L %3$f,%4$f L %3$f,%5$f L %1$f,%6$f Z " +
//            // Left Arm
//            "M %1$f,-%2$f L %3$f,-%4$f L %3$f,-%5$f L %1$f,-%6$f Z",
//            SHOULDER_X, SHOULDER_W - 0.5, // Shoulder Outer
//            HAND_X, HAND_Y, HAND_Y - 1.5, // Hand Outer/Inner
//            SHOULDER_W - 2.0              // Shoulder Inner
//        ));
//        arms.setFill(JERSEY_COLOR.darker()); // Sleeves slightly darker for depth
//
//        // 8. RIDER HEAD (Circle + Helmet/Hair)
//        Group headGroup = new Group();
//        
//        SVGPath face = new SVGPath(); // Skin
//        face.setContent("M 0,0 m -3.2,0 a 3.2,3.2 0 1,0 6.4,0 a 3.2,3.2 0 1,0 -6.4,0");
//        face.setFill(SKIN_COLOR);
//        
//        SVGPath helmet = new SVGPath(); // Helmet/Cap
//        helmet.setContent("M 0,3.2 A 3.2,3.2 0 0,1 0,-3.2 L -2,-3.2 L -2,3.2 Z");
//        helmet.setFill(HAIR_COLOR);
//
//        headGroup.getChildren().addAll(face, helmet);
//        // Position Head slightly forward of shoulders
//        headGroup.setTranslateX(SHOULDER_X + 1); 
//
//        // Add all parts (Order matters for layering!)
//        bikeGroup.getChildren().addAll(
//            tires, frame, mechanics, handlebars, thighs, torso, arms, headGroup
//        );
//
//        // --- FINAL TRANSFORMS ---
//        bikeGroup.setScaleX(BIKE_SCALE);
//        bikeGroup.setScaleY(BIKE_SCALE);
//        bikeGroup.setManaged(false); 
//        
//        // DIRECTION: Point the nose (+X) to the Top of the Screen (-Y)
//        bikeGroup.setRotate(-90);
//
//        return bikeGroup;
//    }
//    
//    private Node createBusFromSVG(Color simColor) {
//        Group busGroup = new Group();
//
//        // ==========================================
//        // TUNING VARIABLES (Modular Design)
//        // ==========================================
//        final double BUS_SCALE = 0.05;
//
//        // Body Dimensions (Half-sizes since center is 0,0)
//        final double BODY_LEN = 80.0;     // Distance from center to nose/tail
//        final double BODY_WID = 28.0;      // Distance from center to side (Total width ~56)
//        final double NOSE_PINCH = 15.0;    // Width at the very front/back tip (Chamfer)
//        
//        // Wheel Positioning
//        final double FRONT_AXLE_X = 80.0;  // Position of front wheels
//        final double REAR_AXLE_X = 78.0;   // Position of rear wheels (Negative in logic)
//        final double WHEEL_LEN = 20.0;     // Length of tire block
//        final double WHEEL_WID = 8.0;      // Thickness of tire
//        
//        // Detail Offsets
//        final double WIN_INSET = 5.0;      // How much windows are inset from the edge
//        final double ROOF_WID = 20.0;      // Width of roof details
//
//        // Colors
//        final Color TIRE_COLOR = Color.web("#121212");
//        final Color GLASS_COLOR = Color.web("#0e0e0e");
//        final Color TRIM_COLOR = Color.web("#000000");
//        final Color ROOF_DETAIL_COLOR = Color.web("#202020");
//        final Color HEADLIGHT_COLOR = Color.web("#E0F7FA");
//        final Color TAILLIGHT_COLOR = Color.web("#D50000");
//
//        // 1. WHEELS (Parametric)
//        SVGPath wheels = new SVGPath();
//        // Helper vars for wheel bounding boxes
//        double fWheelFront = FRONT_AXLE_X + (WHEEL_LEN / 2);
//        double fWheelBack  = FRONT_AXLE_X - (WHEEL_LEN / 2);
//        double rWheelFront = -REAR_AXLE_X + (WHEEL_LEN / 2) + 5; // Slightly larger rear box
//        double rWheelBack  = -REAR_AXLE_X - (WHEEL_LEN / 2) - 5;
//        double wheelYOuter = BODY_WID;
//        double wheelYInner = BODY_WID - WHEEL_WID;
//
//        wheels.setContent(String.format(
//            // Front Right
//            "M %1$f,%3$f L %2$f,%3$f L %2$f,%4$f L %1$f,%4$f Z " +
//            // Front Left
//            "M %1$f,-%3$f L %2$f,-%3$f L %2$f,-%4$f L %1$f,-%4$f Z " +
//            // Rear Right (Wider/Double axle look)
//            "M %5$f,%3$f L %6$f,%3$f L %6$f,%4$f L %5$f,%4$f Z " +
//            // Rear Left
//            "M %5$f,-%3$f L %6$f,-%3$f L %6$f,-%4$f L %5$f,-%4$f Z",
//            fWheelBack, fWheelFront, -wheelYOuter, -wheelYInner, // 1-4
//            rWheelBack, rWheelFront // 5-6
//        ));
//        wheels.setFill(TIRE_COLOR);
//
//        // 2. MAIN BODY (Parametric Chamfered Box)
//        SVGPath body = new SVGPath();
//        // Calculated corners to keep code clean
//        double cornerX = BODY_LEN - 10; // Where the chamfer starts
//        
//        body.setContent(String.format(
//            "M %1$f,%2$f " +    // Nose Right
//            "L %3$f,%4$f " +    // Front Corner Right
//            "L -%3$f,%4$f " +   // Rear Corner Right
//            "L -%1$f,%2$f " +   // Tail Right
//            "L -%1$f,-%2$f " +  // Tail Left
//            "L -%3$f,-%4$f " +  // Rear Corner Left
//            "L %3$f,-%4$f " +   // Front Corner Left
//            "L %1$f,-%2$f Z",   // Nose Left
//            BODY_LEN, NOSE_PINCH, cornerX, BODY_WID
//        ));
//        body.setFill(simColor);
//        body.setStroke(TRIM_COLOR);
//        body.setStrokeWidth(1.0);
//
//        // 3. WINDOWS (Derived from Body Size)
//        SVGPath windows = new SVGPath();
//        double winXFront = BODY_LEN - 5;
//        double winXSideStart = BODY_LEN - 25;
//        double winXSideEnd = -BODY_LEN + 20;
//        double winY = BODY_WID - 4; // Slight inset
//
//        windows.setContent(String.format(
//            // Front Windshield (Hexagon)
//            "M %1$f,0 L %2$f,%3$f L %2$f,-%3$f Z " +
//            // Right Side Strip
//            "M %4$f,%5$f L %6$f,%5$f L %7$f,%8$f L %9$f,%8$f Z " +
//            // Left Side Strip (Mirrored Y)
//            "M %4$f,-%5$f L %6$f,-%5$f L %7$f,-%8$f L %9$f,-%8$f Z " +
//            // Rear Slot
//            "M -%10$f,%11$f L -%1$f,%11$f L -%1$f,-%11$f L -%10$f,-%11$f Z",
//            winXFront, winXSideStart - 5, winY - 2,     // 1,2,3 (Windshield)
//            winXSideStart, winY, winXSideEnd, winY,     // 4,5,6 (Side Outer)
//            winXSideEnd - 5, winY - 6, winXSideStart + 5, // 7,8,9 (Side Inner)
//            BODY_LEN - 8, 10.0 // 10,11 (Rear)
//        ));
//        windows.setFill(GLASS_COLOR);
//
//        // 4. ROOF DETAILS (Centered and Scaled)
//        SVGPath roof = new SVGPath();
//        roof.setContent(String.format(
//            // Front A/C
//            "M 80,%1$f L 100,%1$f L 100,-%1$f L 80,-%1$f Z " +
//            // Rear Vent
//            "M -60,%2$f L -100,%2$f L -100,-%2$f L -60,-%2$f Z " +
//            // Spine
//            "M 70,0 L -50,0",
//            ROOF_WID / 2, ROOF_WID / 2 + 2
//        ));
//        roof.setFill(ROOF_DETAIL_COLOR);
//        roof.setStroke(TRIM_COLOR);
//        roof.setStrokeWidth(0.8);
//
//        // 5. MIRRORS (Attached to Front Corners)
//        SVGPath mirrors = new SVGPath();
//        double mirrorX = BODY_LEN - 15;
//        double mirrorYBase = BODY_WID;
//        double mirrorWTip = BODY_WID + 8;
//        
//        mirrors.setContent(String.format(
//            "M %1$f,%2$f L %3$f,%4$f L %5$f,%4$f Z " + // Right
//            "M %1$f,-%2$f L %3$f,-%4$f L %5$f,-%4$f Z", // Left
//            mirrorX, mirrorYBase, mirrorX + 10, mirrorWTip, mirrorX + 5
//        ));
//        mirrors.setFill(TRIM_COLOR);
//
//        // 6. LIGHTS (Positions fixed relative to Body Length)
//        SVGPath headlights = new SVGPath();
//        headlights.setContent(String.format(
//            "M %1$f,%2$f L %3$f,%2$f L %3$f,%4$f L %1$f,%4$f Z " +
//            "M %1$f,-%2$f L %3$f,-%2$f L %3$f,-%4$f L %1$f,-%4$f Z",
//            BODY_LEN - 2, 20.0, BODY_LEN, 10.0
//        ));
//        headlights.setFill(HEADLIGHT_COLOR);
//
//        SVGPath taillights = new SVGPath();
//        taillights.setContent(String.format(
//            "M -%1$f,20 L -%2$f,20 L -%2$f,-20 L -%1$f,-20 Z",
//            BODY_LEN, BODY_LEN - 2
//        ));
//        taillights.setFill(TAILLIGHT_COLOR);
//
//        // Add all parts
//        busGroup.getChildren().addAll(wheels, body, windows, roof, mirrors, headlights, taillights);
//
//        // --- FINAL TRANSFORMS ---
//        busGroup.setScaleX(BUS_SCALE);
//        busGroup.setScaleY(BUS_SCALE);
//        busGroup.setManaged(false); 
//        busGroup.setRotate(-90);
//
//        return busGroup;
//    }
    
    
	
	
	/**
     * Clears the internal cache of vehicle visual objects.
     * <p>
     * This method removes all stored vehicle shapes ({@link Polygon}) from the {@code vehicleVisualCache}.
     * <b>Usage:</b> This should be called explicitly when:
     * <ul>
     * <li>Resetting or restarting the simulation.</li>
     * <li>Loading a new map.</li>
     * </ul>
     * This ensures that no stale visual artifacts ("ghost vehicles") from the previous session remain in memory or on screen.
     * </p>
     */
	public void clearVehicleCache() {
        this.vehicleVisualCache.clear();
    }
	

		public void renderTrafficLights(Pane trafficLightPane, Map<TrafficlightClass,Character>trafficLightsData, Consumer<TrafficlightClass> onTrafficLightClick) {
			
			if (trafficLightsData == null || trafficLightsData.isEmpty()) {
		        System.out.println("Empty traffic light map");
		        return;
		    }

		    // Check if traffic lights already exist
		    if (trafficLightPane.getChildren().isEmpty()) {
		        // First time: create all traffic lights
		        for (TrafficlightClass tl_link : trafficLightsData.keySet()) {
		            Character tl_color_char = trafficLightsData.get(tl_link);
		            try {
		                SumoPosition2D pos = tl_link.get_position();
		                double screenX = converter.toScreenX(pos.x);
		                double screenY = converter.toScreenY(pos.y);

		                Group lightGroup = new Group();
		                // Housing
		                Rectangle box = new Rectangle(-0.75, -2.125, 1.5, 4.25);
		                box.setArcWidth(0.75);
		                box.setArcHeight(0.75);
		                box.setFill(Color.rgb(30, 30, 30));
		                box.setStroke(Color.BLACK);

		                // Circles
		                Circle redLamp = new Circle(0, -1.125, 0.5);
		                Circle yellowLamp = new Circle(0, 0, 0.5);
		                Circle greenLamp = new Circle(0, 1.125, 0.5);

		                redLamp.setId("red");
		                yellowLamp.setId("yellow");
		                greenLamp.setId("green");

		                lightGroup.getChildren().addAll(box, redLamp, yellowLamp, greenLamp);
		                lightGroup.setTranslateX(screenX);
		                lightGroup.setTranslateY(screenY);
		                lightGroup.setUserData(tl_link);

		                // Click & Hover
		                lightGroup.setOnMouseClicked(e -> {
		                    if (onTrafficLightClick != null) {
		                        onTrafficLightClick.accept(tl_link);
		                    }
		                });
		                lightGroup.setOnMouseEntered(e -> {
		                    lightGroup.setEffect(HOVER_GLOW);
		                    lightGroup.setCursor(Cursor.HAND);
		                });
		                lightGroup.setOnMouseExited(e -> {
		                    lightGroup.setEffect(null);
		                    lightGroup.setCursor(Cursor.DEFAULT);
		                });

		                trafficLightPane.getChildren().add(lightGroup);

		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
		    }

		    // Update colors of all existing traffic lights
		    for (var node : trafficLightPane.getChildren()) {
		        if (!(node instanceof Group)) continue;
		        Group lightGroup = (Group) node;
		        TrafficlightClass tl_link = (TrafficlightClass) lightGroup.getUserData();
		        Character tl_color_char = trafficLightsData.get(tl_link);
		        if (tl_color_char == null) continue; // skip if no data

		        for (var child : lightGroup.getChildren()) {
		            if (!(child instanceof Circle)) continue;
		            Circle lamp = (Circle) child;
		            switch (lamp.getId()) {
		                case "red" -> {
		                    lamp.setFill((tl_color_char == 'r') ? tl_color_map.get(tl_color_char) : tl_color_map.get('a'));
		                }
		                case "yellow" -> {
		                    lamp.setFill((tl_color_char == 'y' || tl_color_char == 'O' || tl_color_char == 'o') ? tl_color_map.get(tl_color_char) : tl_color_map.get('b'));
		                }
		                case "green" -> {
		                    lamp.setFill((tl_color_char == 'G' || tl_color_char == 'g') ? tl_color_map.get(tl_color_char) : tl_color_map.get('c'));
		                }
		            }
		        }
		    }
		}
}