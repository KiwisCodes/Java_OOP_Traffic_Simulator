package model.infrastructure;

public class TrafficlightManager {
    
    private String id;
    private String currentState;
    private double phaseDuration;

    public TrafficlightManager(String id) {
        this.id = id;
    }
}