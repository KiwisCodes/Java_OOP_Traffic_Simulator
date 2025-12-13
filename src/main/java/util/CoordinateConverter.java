package util;

import javafx.geometry.Point2D;
import model.infrastructure.MapManager;
import view.MainGUI;
import de.tudresden.ws.container.SumoPosition2D; 


public class CoordinateConverter {
    private double mapMinX;
    private double mapMaxY; //the ceiling of the map
    private double mapWidth;
    private double mapHeight;

    private double scale = 1;     //pixels per Meter
    private final double padding = 50.0; // Empty space around map edges
    
    private int windowWidth = MainGUI.windowWidth;
    private int windowHeight = MainGUI.windowHeight;
    

    // Constructor: Locks onto a specific Map
    public CoordinateConverter() {
    	
    }
    
    
    public void setBound(MapManager map) {
        this.mapMinX = map.getMinX();
        this.mapMaxY = map.getMaxY();
        this.mapWidth = map.getWidth();
        this.mapHeight = map.getHeight();
    }


    public double toScreenX(double sumoX) {
        return (sumoX - mapMinX) * scale;
    }

    public double toScreenY(double sumoY) {
        return (mapMaxY - sumoY) * scale;
    }

    //i dont use this point2d
    public Point2D toScreen(SumoPosition2D sumoPoint) {
        return new Point2D(toScreenX(sumoPoint.x), toScreenY(sumoPoint.y));
    }

//    public double toSumoX(double screenX) {
//        return ((screenX / scale) + mapMinX;
//    }
//    public double toSumoY(double screenY) {
//        return mapMaxY - ((screenY) / scale);
//    }
    
    public double getScale() { return scale; }
}