package util;

/**
 * Custom exception for errors related to the SUMO simulation connection.
 */
public class SumoException extends Exception {

    // 1. Basic constructor with just a message
    public SumoException(String message) {
        super(message);
    }

    // 2. Constructor that takes a message AND the original cause (another exception)
    // This is useful for "Exception Chaining"
    public SumoException(String message, Throwable cause) {
        super(message, cause);
    }
}