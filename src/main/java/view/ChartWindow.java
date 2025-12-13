package view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

public class ChartWindow {
    private final Stage stage;

    private XYChart.Series<Number, Number> speedSeries;
    private LineChart<Number, Number> speedChart;

    private XYChart.Series<String, Number> densitySeries;
    private BarChart<String, Number> densityChart;

    private XYChart.Series<String, Number> travelTimeSeries;
    private BarChart<String, Number> travelTimeChart;

    public ChartWindow() {
        this.stage = new Stage();
        this.stage.setTitle("Live Simulation Statistics");

        initCharts();

        VBox layout = new VBox(10);
        layout.getChildren().addAll(speedChart, densityChart, travelTimeChart);

        Scene scene = new Scene(layout, 600, 900);
        stage.setScene(scene);
    }

    private void initCharts() {
        // Speed Chart
        NumberAxis xAxisSpeed = new NumberAxis();
        xAxisSpeed.setLabel("Step");
        NumberAxis yAxisSpeed = new NumberAxis();
        yAxisSpeed.setLabel("Avg Speed (m/s)");

        speedChart = new LineChart<>(xAxisSpeed, yAxisSpeed);
        speedChart.setTitle("Average Network Speed");
        speedChart.setAnimated(false);

        speedSeries = new XYChart.Series<>();
        speedSeries.setName("Avg Speed");
        speedChart.getData().add(speedSeries);

        // Density Chart
        CategoryAxis xAxisDens = new CategoryAxis();
        xAxisDens.setLabel("Edge ID");
        NumberAxis yAxisDens = new NumberAxis();
        yAxisDens.setLabel("Count");

        densityChart = new BarChart<>(xAxisDens, yAxisDens);
        densityChart.setTitle("Vehicle Density per Edge");
        densityChart.setAnimated(false);

        densitySeries = new XYChart.Series<>();
        densitySeries.setName("Vehicles");
        densityChart.getData().add(densitySeries);

        // Travel Time Chart
        CategoryAxis xAxisTime = new CategoryAxis();
        xAxisTime.setLabel("Travel Time Range (s)");
        NumberAxis yAxisTime = new NumberAxis();
        yAxisTime.setLabel("Number of Vehicles");

        travelTimeChart = new BarChart<>(xAxisTime, yAxisTime);
        travelTimeChart.setTitle("Travel Time Distribution");
        travelTimeChart.setAnimated(false);

        travelTimeSeries = new XYChart.Series<>();
        travelTimeSeries.setName("Frequency");
        travelTimeChart.getData().add(travelTimeSeries);
    }

    public void show() {
        if (!stage.isShowing()) {
            stage.show();
        } else {
            stage.toFront();
        }
    }

    public void updateData(int currentStep, double avgSpeed,
                           Map<String, Integer> densityMap, Map<String, Integer> travelTimeMap) {
        Platform.runLater(() -> {
            // Update Speed
            speedSeries.getData().add(new XYChart.Data<>(currentStep, avgSpeed));
            
            // Limit history to keep memory low (optional optimization)
            if (speedSeries.getData().size() > 100) {
                speedSeries.getData().remove(0);
            }

            // Update Density
            densitySeries.getData().clear();
            for (Map.Entry<String, Integer> entry : densityMap.entrySet()) {
                densitySeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            // Update Travel Time
            travelTimeSeries.getData().clear();
            for (Map.Entry<String, Integer> entry : travelTimeMap.entrySet()) {
                travelTimeSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        });
    }
}