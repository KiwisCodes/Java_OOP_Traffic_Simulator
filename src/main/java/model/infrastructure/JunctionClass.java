package model.infrastructure;

import de.tudresden.sumo.cmd.Junction;
import de.tudresden.sumo.objects.SumoGeometry;
import it.polito.appeal.traci.SumoTraciConnection;

public class JunctionClass {
    private final String junctionId;
    private final SumoGeometry shape;

    public JunctionClass(SumoTraciConnection connection, String junctionjunctionId) throws Exception {
        this.junctionId = junctionjunctionId;
        this.shape = (SumoGeometry) connection.do_job_get(Junction.getShape(junctionjunctionId));
    }
    public String getId() { 
        return junctionId; 
    }
    public SumoGeometry getShape() { 
        return shape; 
    }
    @Override
    public String toString() {
        return "Junction[ID=" + junctionId + "]";
    }
}