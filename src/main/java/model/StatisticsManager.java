package model;

public class StatisticsManager {
    
    private double averageSpeed;
    private int totalVehicles;
    private int totalTrafficlights;
    private double averageTravelTime;
    private int congestionCount;
    
    public StatisticsManager() {
    }
    
    public void updateStatistics(double currentAvgSpeed, int currentVehicleCount) {
        this.averageSpeed = currentAvgSpeed;
        this.totalVehicles = currentVehicleCount;
        //... more stats but right now idk
    }

    public double getAverageSpeed() { return averageSpeed; }
    public int getTotalVehicles() { return totalVehicles; }
    public int getTotalTrafficlights() { return totalTrafficlights; }
    public double getAverageTravelTime() { return averageTravelTime; }
    public int getCongestionCount() { return congestionCount; }
}