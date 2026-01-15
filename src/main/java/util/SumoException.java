package util;

/**
 * Custom exception for errors related to the SUMO simulation connection.
 */
public class SumoException extends Exception {

    // 1. Basic constructor with just a message
    public SumoException(String message) {
        super(message);
    }

    public SumoException(String message, Throwable cause) {
        super(message, cause);
    }
}