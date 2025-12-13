package model;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ReportManager {

    public ReportManager() {}

    // 1. Export Vehicles CSV
    public void exportVehiclesCSV(List<VehicleInfo> vehicles, String filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write("vehicle_id,speed,time_from_spawn,color,type\n");
            for (VehicleInfo v : vehicles) {
                writer.write(v.id() + "," + v.speed() + "," + v.timeFromSpawn() + "," + v.color() + "," + v.type() + "\n");
            }
            System.out.println("Vehicles CSV exported to: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 2. Export Edges CSV
    public void exportEdgesCSV(List<EdgeInfo> edges, String filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write("edge_id,width,density,average_speed\n");
            for (EdgeInfo e : edges) {
                writer.write(e.id() + "," + e.width() + "," + e.density() + "," + e.avgSpeed() + "\n");
            }
            System.out.println("Edges CSV exported to: " + filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 3. Export PDF Report
    public void exportReportPDF(StatisticsManager stat, String filepath, int currentStep) {
        System.out.println(">>> Starting PDF Export...");

        // 1. Prepare Data
        double avgSpeed = stat.avgVehiclesSpeed();
        Map<String, Integer> densityMap = stat.calculateVehicleDensity();
        Map<String, Integer> travelTimeMap = stat.calculateTravelTimeDistribution(10);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            // -------------------- TEXT SECTION --------------------
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.newLineAtOffset(40, 780);
            content.showText("SUMO Simulation Report");
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(40, 750);
            content.showText(String.format("Average Network Speed: %.2f m/s", avgSpeed));
            content.newLineAtOffset(0, -20);
            int totalVehicles = densityMap.values().stream().mapToInt(Integer::intValue).sum();
            content.showText("Total Active Vehicles: " + totalVehicles);
            content.newLineAtOffset(0, -20);
            content.showText(String.format("Timestep in Simulation: %d", currentStep));
            content.endText();
            // -------------------- CHART SECTION --------------------
            
            // 1. Generate Density Chart
            System.out.println("   > Generating Density Chart...");
            BufferedImage densityImg = generateChartImage(densityMap, "Vehicle Density Per Edge", "Edge", "Count");
            if (densityImg != null) {
                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, densityImg);
                content.drawImage(pdImage, 40, 350, 500, 350);
                System.out.println("   > Density Chart added.");
            } else {
                System.err.println("   ❌ Density Chart Image is NULL.");
            }

            // 2. Generate Travel Time Chart
            System.out.println("   > Generating Time Chart...");
            BufferedImage timeImg = generateChartImage(travelTimeMap, "Travel Time Distribution (s)", "Time Bin", "Vehicles");
            if (timeImg != null) {
                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, timeImg);
                content.drawImage(pdImage, 40, 10, 500, 350);
                System.out.println("   > Time Chart added.");
            } else {
                System.err.println("   ❌ Time Chart Image is NULL.");
            }

            content.close();
            doc.save(filepath);
            System.out.println("✅ PDF Report exported successfully to: " + filepath);

        } catch (Exception e) {
            System.err.println("❌ CRASH in PDF Generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper: Creates a chart AND takes a snapshot safely on the JavaFX thread.
     */
    private BufferedImage generateChartImage(Map<String, Integer> data, String title, String xLabel, String yLabel) {
        CountDownLatch latch = new CountDownLatch(1);
        final BufferedImage[] imageHolder = new BufferedImage[1];

        Platform.runLater(() -> {
            try {
                // 1. Create Chart
                BarChart<String, Number> chart = createBarChartNode(data, title, xLabel, yLabel);
                
                // 2. WRAP IN STACKPANE & SCENE (Crucial for Layout Calculation)
                StackPane root = new StackPane(chart);
                Scene dummyScene = new Scene(root, 600, 400); // Set exact size here
                
                // 3. Force CSS and Layout
                chart.applyCss();
                chart.layout();
                
                // 4. Snapshot
                WritableImage fxImage = chart.snapshot(new SnapshotParameters(), null);
                imageHolder[0] = SwingFXUtils.fromFXImage(fxImage, null);
                
            } catch (Throwable t) {
                // Catch Throwable to catch NoClassDefFoundError (Missing Libraries)
                System.err.println("❌ CRASH INSIDE JAVAFX THREAD: " + t.getMessage());
                t.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(); // Wait for FX thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return imageHolder[0];
    }

    private BarChart<String, Number> createBarChartNode(Map<String, Integer> data, String title, String xLabel, String yLabel) {
        CategoryAxis x = new CategoryAxis();
        x.setLabel(xLabel);
        NumberAxis y = new NumberAxis();
        y.setLabel(yLabel);

        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle(title);
        chart.setAnimated(false); 
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Sort and limit data
        data.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(15)
            .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));

        chart.getData().add(series);
        
        // Explicitly set background to white to avoid transparent PNG issues in PDF
        chart.setStyle("-fx-background-color: white;");
        
        return chart;
    }
}