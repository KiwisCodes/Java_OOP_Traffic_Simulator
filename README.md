# Real-Time Traffic Simulation (OOP Java Project) <br/>  Milestone 3: Final Submission

**Winter 2025-2026 | Prof. Dr.-Eng. Ghadi Mahmoudi**

This repository contains the source code for Real-Time Traffic Simulation for the Object-Oriented Programming in Java module.

---

## 1. Project Overview

This project is a Java-based application that connects to the SUMO (Simulation of Urban MObility) traffic simulator in real-time. For this second milestone, we have developed a program that:

* **Live SUMO Integration:** Connects to a running SUMO instance via the TraaS API.
* **GUI:** A GUI that allows user to start Simulation (other buttons for other functions will be implemented for later milestones).
* **Class Design for TraaS wrapper:** a folder with packages of java files that manage multiple functions.
* **Map Visualisation:** Visualising edges, lanes, vehicles and traffic light onto our GUI.
* **Means of Transportation Injection:** Allowing user to inject vehicles / person from chosen source and destination edges, with options of speed and color.
* **Stress Test:** Allowing user to inject chosen number vehicles / people from chosen source and destination edges, with options of speed and color.
* **Traffic Light Control:** Allowing user to control traffic light (switch to the next phase and modify color and duration).
* **Vehicles Filter:** Allowing user to filter (only show / export) vehicles with filtered speed / color.
* **Export Reports in CSV/PDF:** Allowing user to Export Vehicle CSV, Edge CSV and PDF Report with Filtered Data from the Filter feature and seperate Congestion Feature for Edge CSV.
* **Statistics Real-Time Chart Window:** Allowing user to see real-time update on Statistics (number of vehicles, vehicle distribution chart, average speed...) of the simulation.


** 1.1. Key Classes **

* `MainController.java`: the main controller of the entire application
* `MapInteractionHandler.java`: handles user interaction with the map (zooming, panning, etc.)
* `VehicleManager.java`: manages all vehicles / pedestrians in the application
* `ReportManager.java`: generating report with statistics onto the GUI of the application
* `TrafficlightManager.java`: manages traffic lights for the entire application
* `SimulationManager.java`: controls the simulation lifecycle and execution
* `StatisticsManager.java`: analyzes timestep statistics during the simulation
* `ReportManager.java`: handles the logic of exports of Vehicle and Edge CSV, PDF Reports of the simulation.
* `SimulationQueue.java`: thread-safe queue used to exchange SimulationState objects between threads
* `SimulationState.java`: the updated data (by the SimulationManager) required to update Vehicles and Traffic Light Panes (by the MainController) for each time step
* `CoordinateConverter.java`: converts coordinates from meters to screen coordinates
* `ColorConverter.java`: converts SumoColor to RGBA color
* `MainGUI.java`: application entry point responsible for loading FXML and controllers
* `Renderer.java`: renders the map and simulation components onto the GUI
* `SumoException.java`: extends Exception to define a SUMO-specific error, manually thrown by developer code to represent simulation or connection-related failures for clearer error handling and debugging
* `MeansOfTransportation.java`: an interface defining the common methods that all pedestrians and vehicles must implement



** 1.2. Directory Structure **

```text
root
├── lib/
│   └── TraaS.jar                         		# External library for TraCI (SUMO connection)

├── logs/
│   └── app.log                           		# Application runtime logs

├── reports/
│   └── (created reports)                        # Reports folder

├── src/
│   └── main/
│       ├── java/
│       │   ├── controller/               		# Handles UI events and input logic
│       │   │   ├── MainController.java
│       │   │   └── MapInteractionHandler.java
│       │   │
│       │   ├── data/                     		# Thread-safe data objects for concurrency
│       │   │   ├── SimulationQueue.java
│       │   │   └── SimulationState.java
│       │   │
│       │   ├── model/                    		# Core simulation logic (Model layer)
│       │   │   ├── EdgeInfo.java
│       │   │   ├── VehicleInfo.java
│       │   │   ├── SimulationManager.java
│       │   │   ├── StatisticsManager.java
│       │   │   ├── ReportManager.java
│       │   │   │
│       │   │   ├── infrastructure/       		# Roads, junctions, traffic lights
│       │   │   │   ├── MapManager.java
│       │   │   │   ├── EdgeClass.java
│       │   │   │   ├── LaneClass.java
│       │   │   │   ├── JunctionClass.java
│       │   │   │   ├── TrafficlightClass.java
│       │   │   │   ├── TrafficlightManager.java
│       │   │   │   └── mapInSumo.txt
│       │   │   │
│       │   │   ├── vehicles/             		# Polymorphic vehicle entities
│       │   │   │   ├── MeansOfTransportation.java   # Interface
│       │   │   │   ├── VehicleClass.java
│       │   │   │   ├── PedestrianClass.java
│       │   │   │   ├── CarClass.java
│       │   │   │   ├── BusClass.java
│       │   │   │   ├── BikeClass.java
│       │   │   │   └── VehicleManager.java
│       │   │
│       │   ├── util/                     		# Utility helpers
│       │   │   ├── CoordinateConverter.java
│       │   │   ├── ColorConverter.java
│       │   │   ├── SumoException.java
│       │   │   └── Util.java
│       │   │
│       │   └── view/                     		# JavaFX visualization (View layer)
│       │       ├── MainGUI.java
│       │       ├── Renderer.java
│       │       └── ChartWindow.java
│       │
│       └── resources/                    		# Non-code assets
│           ├── SumoConfig/               		# SUMO map files (.net.xml, .rou.xml)
│           │
│           ├── gui/                      		# FXML layouts and CSS
│           │   ├── MainView2.fxml
│           │   └── style.css
│           │
│           └── log4j2.xml                 		# Logging configuration
│
├── pom.xml                               		# Maven build configuration
├── README.md                                    # Project documentation
├── Project Overview.pdf                         # Project Overview
├── User-Guide-Sumo-Simulation-App-Group-2.pdf   # User manual
├── Class Diagram.drawio.png              		# Architectural diagram
└── Producer-Consumer Pattern.png         		# Concurrency architecture diagram
```

## 2. Technology Stack

* **Programming Language:** Java 17
* **IDE:** Eclipse IDE
* **Build tool:** Apache Maven
* **SUMO TraaS:** TraCI as a Service API
* **Visualization:** JavaFX (21.0.2)

## 3. Setup

**3.1. Clone the Repository:**

```bash
git clone https://github.com/KiwisCodes/Java_OOP_Traffic_Simulator.git
cd Java_OOP_Traffic_Simulator
```

Or if you download the .zip file then you would need to unzip it, then go inside the folder traffic-simulator-main, there might be one more traffic-simulator-main (exact same name with the root), if you need to go to that directory to access the code.

**3.2. Add your SUMO Path**

You can set the path to sumo when you open the app. 

Or else, you can set it manually, following these instructions:

Go to file `Java_OOP_Traffic_Simulator/src/main/java/model/SimulationManager.java` and set your variable `sumoPath` to your SUMO path in your local device.

```java
private String sumoPath = yourSUMOpath; 
```

**3.3. Set your dependency**

Go to file `Java_OOP_Traffic_Simulator/pom.xml` and save it

=> You will see a Maven Dependencies library imported to your Directory.


**3.4. Run the Java Application:**

Open a second terminal in the project root and use Maven to run the application.

```bash
# This will compile and run the JavaFX application
mvn clean javafx:run
```

---

## 4. Documentation (Javadoc)

The project includes comprehensive Javadoc comments for all major classes and methods. You can generate a searchable HTML version of this documentation using Maven.

** 4.1. Generation Step **

To generate the documentation, open your terminal in the project root directory and run the following command:

```bash
mvn javadoc:javadoc -Dmaven.javadoc.failOnError=false -Ddoclint=none
```

** 4.2. Open Javadoc **

To open the javadoc:

```bash
open target/reports/apidocs/index.html
```
---

## 5. Acknowledgments

This project is part of the **Object-Oriented Programming in Java** module at **Frankfurt University of Applied Sciences**. Special thanks to **Prof. Dr.-Eng. Ghadi Mahmoudi** for guidance and supervision throughout the development of this milestone.

---
