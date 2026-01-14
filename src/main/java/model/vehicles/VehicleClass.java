package model.vehicles;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the state of a single vehicle within the SUMO simulation at a specific step.
 * <p>
 * This class stores a snapshot of a vehicle's
 * attributes (such as position, speed, and color) retrieved from the simulation via TraCI.
 * <p>
 * Logging is implemented using {@link java.util.logging.Logger}.
 *
 * @author Minh Khoi
 */
public class VehicleClass {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(VehicleClass.class.getName());

    private String id;
    private SumoColor color;
    private SumoPosition2D position;
    private double speed;
    private String edgeId;
    private double angle;
    private double departure;

    /**
     * Constructs a new {@code VehicleClass} instance with the specified attributes.
     *
     * @param id        The unique identifier of the vehicle (e.g., "veh_0").
     * @param color     The visual color of the vehicle in the simulation.
     * @param position  The current 2D coordinates (x, y) of the vehicle.
     * @param speed     The current velocity of the vehicle in m/s.
     * @param edgeId    The ID of the road segment (edge) the vehicle is currently traveling on.
     * @param angle     The navigation angle of the vehicle in degrees.
     * @param departure The simulation time (in seconds) when the vehicle entered the network.
     */
    public VehicleClass(String id, SumoColor color, SumoPosition2D position, double speed, String edgeId, double angle, double departure) {
        this.id = id;
        this.color = color;
        this.position = position;
        this.speed = speed;
        this.edgeId = edgeId;
        this.angle = angle;
        this.departure = departure;

        // Logging at FINER level because this constructor is called extremely frequently (per vehicle per step).
        // Standard INFO or FINE logging here would flood the logs and impact performance.
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Created Vehicle snapshot: [ID=" + id + ", Speed=" + speed + ", Edge=" + edgeId + "]");
        }
    }

    /**
     * Gets the unique identifier of the vehicle.
     *
     * @return The vehicle ID string.
     */
    public String getId() { return id; }

    /**
     * Gets the color object associated with this vehicle.
     *
     * @return A {@link SumoColor} object representing the vehicle's color.
     */
    public SumoColor getColor() { return color; }

    /**
     * Gets the current 2D position of the vehicle.
     *
     * @return A {@link SumoPosition2D} object containing x and y coordinates.
     */
    public SumoPosition2D getPosition() { return position; }

    /**
     * Gets the current speed of the vehicle.
     *
     * @return The speed in meters per second (m/s).
     */
    public double getSpeed() { return speed; }

    /**
     * Gets the ID of the edge (road) the vehicle is currently on.
     *
     * @return The edge ID string.
     */
    public String getEdgeId() { return edgeId; }

    /**
     * Gets the orientation angle of the vehicle.
     *
     * @return The angle in degrees.
     */
    public double getAngle() { return angle; }

    /**
     * Gets the simulation time when the vehicle started its route.
     *
     * @return The departure time in seconds.
     */
    public double getDeparture() { return departure; }

    @Override
    public String toString() {
        return "Vehicle [id=" + id + ", speed=" + speed + ", edge=" + edgeId + "]";
    }
}