package util;

/**
 * A custom exception for SUMO errors.
 */
public class SumoException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private final boolean fatal; 

    /**
     * Use this for minor errors (logic issues, invalid IDs).
     * The simulation can continue.
     */
    public SumoException(String message) {
        super(message);
        this.fatal = false; 
    }

    /**
     * Specify if the error is fatal.
     */
    public SumoException(String message, boolean isFatal) {
        super(message);
        this.fatal = isFatal;
    }


    public SumoException(String message, Throwable cause, boolean isFatal) {
        super(message, cause);
        this.fatal = isFatal;
    }

    public boolean isFatal() {
        return fatal;
    }
}