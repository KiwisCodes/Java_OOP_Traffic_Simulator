package util;

import javafx.geometry.Point2D;
import model.infrastructure.MapManager;
import de.tudresden.ws.container.SumoPosition2D; 



/**
 * Handles the coordinate transformation between SUMO's Cartesian system and JavaFX's Screen system.
 * <p>
 * <b>The Coordinate Problem:</b>
 * <ul>
 * <li><b>SUMO:</b> Uses a standard Cartesian system (Meters). Origin (0,0) is usually at the <i>Bottom-Left</i>. Y increases upwards.</li>
 * <li><b>JavaFX:</b> Uses a Screen coordinate system (Pixels). Origin (0,0) is at the <i>Top-Left</i>. Y increases downwards.</li>
 * </ul>
 * This class applies linear transformations to map world coordinates (meters) to screen coordinates (pixels),
 * handling the necessary Y-axis inversion and scaling.
 * </p>
 * * @author pth
 * @version 1.0
 */
public class CoordinateConverter {
    private double mapMinX;
    private double mapMaxY; //the ceiling of the map
    private double scale = 1;     //pixels per Meter
    

    /**
     * Constructs a new CoordinateConverter.
     * <p>
     * <b>Note:</b> You must call {@link #setBound(MapManager)} before using conversion methods
     * to ensure the converter knows the map dimensions.
     * </p>
     */
    public CoordinateConverter() {
    	
    }
    
    
    /**
     * Initializes the converter with the boundaries of the loaded SUMO map.
     * <p>
     * This method calculates the dimensions required to fit the map onto the screen
     * and sets the base offsets (minX, maxY).
     * </p>
     * * @param map The {@link MapManager} containing the loaded map's boundary data.
     */
    public void setBound(MapManager map) {
        this.mapMinX = map.getMinX();
        this.mapMaxY = map.getMaxY();
    }

    
    /**
     * Converts a SUMO X-coordinate (meters) to a JavaFX Screen X-coordinate (pixels).
     * <p>
     * <b>Formula:</b> {@code (sumoX - mapMinX) * scale}
     * </p>
     * * @param sumoX The X coordinate from the simulation.
     * @return The corresponding pixel X coordinate on the screen.
     */
    public double toScreenX(double sumoX) {
        return (sumoX - mapMinX) * scale;
    }
    
    /**
     * Converts a SUMO Y-coordinate (meters) to a JavaFX Screen Y-coordinate (pixels).
     * <p>
     * <b>Formula:</b> {@code (mapMaxY - sumoY) * scale}
     * <br>
     * <i>Note: This performs the Y-axis inversion.</i>
     * </p>
     * * @param sumoY The Y coordinate from the simulation.
     * @return The corresponding pixel Y coordinate on the screen.
     */
    public double toScreenY(double sumoY) {
        return (mapMaxY - sumoY) * scale;
    }

    /**
     * Converts a 2D SUMO position object into a JavaFX Point2D object.
     * * @param sumoPoint The position object from the TraaS library.
     * @return A JavaFX {@link Point2D} representing the screen location.
     */
    public Point2D toScreen(SumoPosition2D sumoPoint) {
        return new Point2D(toScreenX(sumoPoint.x), toScreenY(sumoPoint.y));
    }
    
    /**
     * Retrieves the current scale factor.
     * @return The number of pixels per meter.
     */
    public double getScale() { return scale; }
}