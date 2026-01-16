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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@code ChartWindow} represents a separate window for visualization
 * of Live-Simulation statistic using JavaFX
 * <p>
 * It includes these three different diagrams:
 * <ul>
 *     <li>A line diagram for the average vehicle speed over time step</li>
 *     <li>A bar chart for the vehicle density per edge id</li>
 *     <li>A bar chart for the distribution of vehicle travel time</li>
 * </ul>
 *
 * @author Minh Khoi
 */
public class ChartWindow {

    private static final Logger LOGGER = Logger.getLogger(ChartWindow.class.getName());

    /** Main Window (Stage) for the diagrams.*/
    private final Stage stage;

    private XYChart.Series<Number, Number> speedSeries;
    private LineChart<Number, Number> speedChart;

    private XYChart.Series<String, Number> densitySeries;
    private BarChart<String, Number> densityChart;

    private XYChart.Series<String, Number> travelTimeSeries;
    private BarChart<String, Number> travelTimeChart;

    /**
     * Create a new instance of the ChartWindow
     * <p>
     * This constructor initialises the {@link Stage}, configures the diagrams
     * over {@link #initCharts()} and sets the layout of the Scene
     */
    public ChartWindow() {
        LOGGER.info("Initializing ChartWindow components...");
        this.stage = new Stage();
        this.stage.setTitle("Live Simulation Statistics");

        initCharts();

        VBox layout = new VBox(10);
        layout.getChildren().addAll(speedChart, densityChart, travelTimeChart);

        Scene scene = new Scene(layout, 600, 900);
        stage.setScene(scene);
        LOGGER.info("ChartWindow initialized.");
    }

    /**
     * Initialises the axes, titles and data rows for all three diagrams
     */
    private void initCharts() {
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

    /**
     * Shows the statistic window
     * <p>
     * In case if the window is not yet shown, it would be opened.
     * In case if it is already open, it would
     */
    public void show() {
        if (!stage.isShowing()) {
            LOGGER.info("Showing ChartWindow.");
            stage.show();
        } else {
            LOGGER.info("ChartWindow already visible, bringing to front.");
            stage.toFront();
        }
    }

    /**
     * Updates the data in all diagrams
     * <p>
     * This method uses {@link Platform#runLater(Runnable)} to make sure
     * that the changes in UI of JavaFX Application Thread would be carried out
     * This prevents the Threading problem, when the method was called from the Simulation Thread.
     * @param currentStep The step of the actual simulation (used as X value in the average speed diagram)
     * @param avgSpeed The average speed of all vehicles in the current step
     * @param densityMap The map that records the number of vehicles for each edge ID
     *                   The old data of the vehicle density diagram would be erased every step und refilled with new data
     * @param travelTimeMap The map that contains the time interval of vehicle travel time as key and number of vehicles in the interval as value
     *                      Here the data would be erased every step and renewed
     */
    public void updateData(int currentStep, double avgSpeed,
                           Map<String, Integer> densityMap, Map<String, Integer> travelTimeMap) {

        if (densityMap == null || travelTimeMap == null) {
            LOGGER.warning("Update skipped: Received null map data for step " + currentStep);
            return;
        }

        Platform.runLater(() -> {
            try {
                speedSeries.getData().add(new XYChart.Data<>(currentStep, avgSpeed));

                densitySeries.getData().clear();
                for (Map.Entry<String, Integer> entry : densityMap.entrySet()) {
                    densitySeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }

                travelTimeSeries.getData().clear();
                for (Map.Entry<String, Integer> entry : travelTimeMap.entrySet()) {
                    travelTimeSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }

                if (currentStep % 100 == 0) {
                    LOGGER.info("Charts updated successfully for step " + currentStep);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating charts on UI thread", e);
            }
        });
    }
}