//package testjava;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import data.SimulationQueue;
//import data.SimulationState;
//import model.infrastructure.*;
//public class TestSimulationThreads {
//
//    public static void main(String[] args) {
//        // Create a small queue
//        SimulationQueue queue = new SimulationQueue(5);
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//        AtomicBoolean injected = new AtomicBoolean(false);
//
//        // Producer thread (simulates SUMO)
//        executor.submit(() -> {
//            try {
//                int step = 0;
//                while (step < 10) { // simulate 10 timesteps
//                    System.out.println("[Producer] Step " + step + " running");
//
//                    // Fake edges
//                    Map<String, EdgeObject> edges = new HashMap<>();
//                    edges.put("edge_" + step, new EdgeObject());
//
//                    // Fake vehicles
//                    Map<String, Map<String, Object>> vehicles = new HashMap<>();
//                    vehicles.put("500" + step, Map.of("speed", 10 + step));
//
//                    // Fake traffic lights
//                    List<String> trafficLights = List.of("TL_1", "TL_2");
//
//                    // Create a frame and put it into queue
//                    SimulationState frame = new SimulationState(edges, vehicles, trafficLights);
//                    queue.putState(frame);
//
//                    System.out.println("[Producer] Step " + step + " pushed to queue");
//
//                    step++;
//                    Thread.sleep(500); // simulate SUMO timestep
//                }
//            } catch (InterruptedException e) {
//                System.out.println("[Producer] Interrupted");
//            }
//        });
//
//        // Consumer thread (simulates StatisticManager)
//        executor.submit(() -> {
//            try {
//                int count = 0;
//                while (count < 10) { // consume 10 frames
//                    SimulationState frame = queue.takeState();
//                    System.out.println("[Consumer] Received frame:");
//                    System.out.println("  Edges: " + frame.getEdges().keySet());
//                    System.out.println("  Vehicles: " + frame.getVehicles().keySet());
////                    System.out.println("  TrafficLights: " + frame.getTrafficLights());
//                    count++;
//                    Thread.sleep(300); // simulate processing time
//                }
//            } catch (InterruptedException e) {
//                System.out.println("[Consumer] Interrupted");
//            }
//        });
//
//        executor.shutdown();
//    }
//}
