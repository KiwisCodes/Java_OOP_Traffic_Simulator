package view;

import java.util.List;
import java.util.function.Consumer; // [NEW] Needed for the callback

import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.*;
import it.polito.appeal.traci.*;
import util.CoordinateConverter;

// Helper class to handle the creation of JavaFX shapes, groups
public class Renderer {
    private CoordinateConverter converter;

    public Renderer(){
        this.converter = new CoordinateConverter();
    }
}
    
    