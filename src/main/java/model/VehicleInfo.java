package model;

public record VehicleInfo(
    String id, 
    double speed, 
    double timeFromSpawn, 
    String color, 
    String type
) {}