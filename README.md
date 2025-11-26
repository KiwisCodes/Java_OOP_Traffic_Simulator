# Real-Time Traffic Simulation (OOP Java Project) <br/>  Milestone 1: System Design & Prototype

**Winter 2025-2026 | Prof. Dr.-Eng. Ghadi Mahmoudi**

This repository contains the source code for Real-Time Traffic Simulation for the Object-Oriented Programming in Java module.

---

## 1. Project Overview

This project is a Java-based application that connects to the SUMO (Simulation of Urban MObility) traffic simulator in real-time. For this first milestone, we have developed a program that:

* **Live SUMO Integration:** Connects to a running SUMO instance via the TraaS API.
* **GUI:** A GUI that allows user to start Simulation (other buttons for other functions will be implemented for later milestones).
* **Class Design for TraaS wrapper:** a folder with packages of java files that manage multiple functiona

### Key Classes

* `MainController.java`: the main controller of the whole application
* `MapInteractionHandler.java`: to interact with the map (zooming, panning,..)
* `VehicleManager.java`: manage the vehicles in the application
* `ReportManager.java`: generating report with statistics onto the GUI of the application
* `SimulationManager.java`: manage the simulation process
* `StatisticsManager.java`: analyse the statistics of the timesteps during the Simulation
* `CoordinateConverter.java`: convert Coordination from meters
* `MainGUI.java`: runner for the program, load FXML and controller
* `Renderer.java`: render the map and other components onto the GUI

## 1.1 Directory Structure

```text
root
├── Architectural-Diagram-Java-Sumo.pdf
├── ClassDiagramJavaOOP.drawio.pdf
├── GUI DESCRIPTION.pdf
├── JavaClassDiagram.drawio.png
├── JavaClassDiagram.drawio.svg
├── JavaOOPClassDiagram.drawio.svg
├── Project Overview.pdf
├── README.md
├── lib
│   └── TraaS.jar
├── pom.xml
├── run.command
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── controller
│   │   │   │   ├── MainController.java
│   │   │   │   └── MapInteractionHandler.java
│   │   │   ├── model
│   │   │   │   ├── ReportManager.java
│   │   │   │   ├── SimulationManager.java
│   │   │   │   ├── StatisticsManager.java
│   │   │   │   ├── infrastructure
│   │   │   │   │   ├── MapManger.java
│   │   │   │   │   └── TrafficlightManager.java
│   │   │   │   └── vehicles
│   │   │   │       └── VehicleManager.java
│   │   │   ├── testjava
│   │   │   │   └── TestSumo.java
│   │   │   ├── util
│   │   │   │   └── CoordinateConverter.java
│   │   │   └── view
│   │   │       ├── Launcher.java
│   │   │       ├── MainGUI.java
│   │   │       └── Renderer.java
│   │   └── resources
│   │       ├── SumoConfig
│   │       │   ├── frauasmap.net.xml
│   │       │   ├── frauasmap.osm
│   │       │   ├── frauasmap.rou.xml
│   │       │   ├── frauasmap.sumocfg
│   │       │   ├── minimal.sumocfg
│   │       │   └── trips.trips.xml
│   │       └── gui
│   │           └── MainView.fxml
│   └── test
│       ├── java
│       └── resources
├── target
│   ├── classes
│   │   ├── META-INF
│   │   │   ├── MANIFEST.MF
│   │   │   └── maven
│   │   │       └── com.htkkk
│   │   │           └── traffic-simulator
│   │   │               ├── pom.properties
│   │   │               └── pom.xml
│   │   ├── SumoConfig
│   │   │   ├── frauasmap.net.xml
│   │   │   ├── frauasmap.osm
│   │   │   ├── frauasmap.rou.xml
│   │   │   ├── frauasmap.sumocfg
│   │   │   ├── minimal.sumocfg
│   │   │   └── trips.trips.xml
│   │   ├── controller
│   │   │   ├── MainController$1.class
│   │   │   ├── MainController.class
│   │   │   └── MapInteractionHandler.class
│   │   ├── gui
│   │   │   └── MainView.fxml
│   │   ├── model
│   │   │   ├── ReportManager.class
│   │   │   ├── SimulationManager.class
│   │   │   ├── StatisticsManager.class
│   │   │   ├── infrastructure
│   │   │   │   ├── MapManger.class
│   │   │   │   └── TrafficlightManager.class
│   │   │   └── vehicles
│   │   │       └── VehicleManager.class
│   │   ├── testjava
│   │   │   └── TestSumo.class
│   │   ├── util
│   │   │   └── CoordinateConverter.class
│   │   └── view
│   │       ├── Launcher.class
│   │       ├── MainGUI.class
│   │       └── Renderer.class
│   ├── generated-sources
│   │   └── annotations
│   ├── maven-status
│   │   └── maven-compiler-plugin
│   │       └── compile
│   │           └── default-compile
│   │               ├── createdFiles.lst
│   │               └── inputFiles.lst
│   └── test-classes
├── traffic-simulator-1.0.0-jar-with-dependencies.jar
└── traffic-simulator-1.0.0.jar
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

**3.2. Add your SUMO Path**

You can set the path to sumo when you open the app. 

Or else, you can set it manually, following these instructions:

Go to file `Java_OOP_Traffic_Simulator/src/main/java/model/SimulationManager.java` and set your variable `sumoPath` (currently `"/Users/apple/sumo/bin/sumo"`) to your SUMO path in your local device.

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

## 4. Acknowledgments

This project is part of the **Object-Oriented Programming in Java** module at **Frankfurt University of Applied Sciences**. Special thanks to **Prof. Dr.-Eng. Ghadi Mahmoudi** for guidance and supervision throughout the development of this milestone.

## 5. Future Work

* Additional GUI features (vehicle and traffic light control, map interaction tools)
* Statistical reporting feature
* Map visualisation with manual-control options
* Extended SUMO–Java communication features

---





