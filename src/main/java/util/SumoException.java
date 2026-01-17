package util;

/**
 * Custom exception for errors related to the SUMO simulation connection.
 */
public class SumoException extends Exception {

    private static final long serialVersionUID = 1L;

	public SumoException(String message) {
        super(message);
    }

    public SumoException(String message, Throwable cause) {
        super(message, cause);
    }
}