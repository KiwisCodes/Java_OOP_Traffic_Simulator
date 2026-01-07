package view;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.Map;
import javafx.scene.Group;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow; 
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;     
import javafx.scene.shape.Polyline;   
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Lane;           
import de.tudresden.sumo.cmd.Junction;      
import de.tudresden.sumo.objects.SumoGeometry;  
import de.tudresden.sumo.objects.SumoPosition2D; 
import model.infrastructure.MapManager;
import model.infrastructure.TrafficlightObject;
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
public class Renderer {
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
     * @param onLaneClick A consumer callback to handle mouse click events on the generated lane shapes.
     */
    
    public void renderLanes(Map<String, LaneClass> laneData, Pane lanePane, Consumer<String> onLaneClick) {
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
                Shape laneShape = createLaneShape(props, laneData, onLaneClick);
                
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
    
    private Shape createLaneShape(LaneClass props, Map<String,LaneClass> laneData,Consumer<String> onLaneClick) {
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
            lanePolyline.setStroke(Color.rgb(50, 50, 50)); 
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
                lanePolyline.setStroke(Color.rgb(50, 50, 50)); 
                lanePolyline.setCursor(Cursor.DEFAULT);
            });
            
            lanePolyline.setOnMouseClicked(e -> {
            	onLaneClick.accept(props.getId());
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
    
    
    private Map<String, Polygon> vehicleVisualCache = new HashMap<>();
    
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
	public void renderVehicles(Pane vehiclePane, Map<String, VehicleClass> vehicleData, List<String> validIDs, boolean isFilterApplied) {
//        if (vehicleData == null || vehicleData.isEmpty()) {
//            vehiclePane.getChildren().clear();  
//            vehicleVisualCache.clear();
//            return;
//        }
//        List<String> toRemove = new ArrayList<>();
//        for (String cachedId : vehicleVisualCache.keySet()) {
//            if (!vehicleData.containsKey(cachedId)) { 
//                toRemove.add(cachedId);
//            }
//        }
//        for (String id : toRemove) {
//            Polygon shape = vehicleVisualCache.get(id); 
//            vehiclePane.getChildren().remove(shape); 
//            vehicleVisualCache.remove(id);     
//        }
//        // KHOA CODE FILTERING
//        List<String> vehiclesToDraw;
//	    if (isFilterApplied && validIDs != null && !validIDs.isEmpty()) {
//	        // If filter is applied, only draw the pre-filtered IDs
//	        vehiclesToDraw = validIDs;
//	    } else if (isFilterApplied && (validIDs == null || validIDs.isEmpty())) {
//	         // If filter is applied but no vehicles match, draw nothing.
//	         return; 
//	    } else {
//	        // Otherwise (no filter), draw all vehicle IDs
//	        vehiclesToDraw = new ArrayList<>(vehicleData.keySet());
//	    }
//	    THIS DOESNT WORK DUE TO NEW RENDERING METHOD, CHANGING OF METHOD IS IMPLEMENTED BELOW
		
		// 1. Safety check: if no data, clear everything
	    if (vehicleData == null || vehicleData.isEmpty()) {
	        vehiclePane.getChildren().clear();  
	        vehicleVisualCache.clear();
	        return;
	    }

	    // 2. Determine exactly which IDs should be visible on screen
	    // We use a Set for much faster lookup performance (.contains is faster on a Set)
	    java.util.Set<String> visibleIDs;
	    if (isFilterApplied) {
	        visibleIDs = (validIDs == null) ? new java.util.HashSet<>() : new java.util.HashSet<>(validIDs);
	    } else {
	        visibleIDs = vehicleData.keySet(); // No filter? Everyone is visible.
	    }

	    // 3. CLEANUP: Identify shapes to remove
	    List<String> toRemove = new ArrayList<>();
	    for (String cachedId : vehicleVisualCache.keySet()) {
	        // Condition A: Vehicle left the simulation
	        // Condition B: Filter is ON, but this vehicle is NOT in the allowed list
	        if (!vehicleData.containsKey(cachedId) || (isFilterApplied && !visibleIDs.contains(cachedId))) { 
	            toRemove.add(cachedId);
	        }
	    }

	    // Actually remove the "hidden" or "dead" vehicles from the UI
	    for (String id : toRemove) {
	        Polygon shape = vehicleVisualCache.get(id); 
	        vehiclePane.getChildren().remove(shape); 
	        vehicleVisualCache.remove(id);     
	    }
        
        for (String vehicleId : visibleIDs) { // KHOA CODE FILTERING
            VehicleClass props = vehicleData.get(vehicleId);
            try {
	            double simX = 0;
	            double simY = 0;
	            double angle = 0;
	            Color carColor = Color.YELLOW;
	            SumoPosition2D posObj = props.getPosition(); 
	            simX = posObj.x; 
	            simY = posObj.y;
                double screenX = converter.toScreenX(simX);
                double screenY = converter.toScreenY(simY);
                angle = props.getAngle(); 
	            SumoColor color = props.getColor();      
	            carColor = ColorConverter.toFXColor(color);
                Polygon carShape = vehicleVisualCache.get(vehicleId);

                if (carShape != null) {
                    carShape.setTranslateX(screenX); 
                    carShape.setTranslateY(screenY); 
                    carShape.setRotate(angle); 
                    carShape.setUserData(props); 
                } else {
                    carShape = new Polygon();
                    double size = 2.0; 
                    carShape.getPoints().addAll(new Double[]{
    	                0.0, -size,      
    	                -size/2, size,  
    	                size/2, size    
    	            });

                    carShape.setTranslateX(screenX);
                    carShape.setTranslateY(screenY);
                    carShape.setRotate(angle);
                    carShape.setFill(carColor);
                    carShape.setStrokeWidth(1);
                    carShape.setUserData(props);
                    final Polygon finalShape = carShape; 
                    carShape.setOnMouseClicked(e -> {
                    	VehicleClass info =(VehicleClass) finalShape.getUserData();
                        System.out.println("Clicked Vehicle: " + info.getId());
                    });
                    
                    carShape.setOnMouseEntered(e -> {
                        finalShape.setEffect(HOVER_GLOW);
                        finalShape.setCursor(Cursor.HAND);
                    });
                    
                    carShape.setOnMouseExited(e -> {
                        finalShape.setEffect(null);
                        finalShape.setCursor(Cursor.DEFAULT);
                    });

                    vehiclePane.getChildren().add(carShape);
                    vehicleVisualCache.put(vehicleId, carShape);
                }

            } catch (Exception e) {
                System.err.println("Error rendering vehicle: " + vehicleId);
                continue;
            }
        }
    }
    
    
	
	
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
	

		public void renderTrafficLights(Pane trafficLightPane, Map<TrafficlightObject,Character>trafficLightsData, Consumer<TrafficlightObject> onTrafficLightClick) {
			
			if (trafficLightsData == null || trafficLightsData.isEmpty()) {
		        System.out.println("Empty traffic light map");
		        return;
		    }

		    // Check if traffic lights already exist
		    if (trafficLightPane.getChildren().isEmpty()) {
		        // First time: create all traffic lights
		        for (TrafficlightObject tl_link : trafficLightsData.keySet()) {
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
		        TrafficlightObject tl_link = (TrafficlightObject) lightGroup.getUserData();
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