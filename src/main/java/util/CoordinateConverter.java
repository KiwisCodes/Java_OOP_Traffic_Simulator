package util;

import javafx.geometry.Point2D;
import model.infrastructure.MapManger;
import de.tudresden.ws.container.SumoPosition2D; 


public class CoordinateConverter {

    // --- Static World Data (From SumoMap) ---
    private double mapMinX;
    private double mapMaxY; // The "Ceiling" of the map
    private double mapWidth;
    private double mapHeight;

    // --- Dynamic View Data (Zoom & Pan) ---
    private double scale = 1;     // Pixels per Meter
    private double offsetX = 0.0;   // Panning X
    private double offsetY = 0.0;   // Panning Y
    private final double padding = 50.0; // Empty space around map edges
    
    public CoordinateConverter() {}

	}
