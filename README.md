# Real-Time Traffic Simulation (OOP Java Project) <br/>  Milestone 2: Functional Prototype

**Winter 2025-2026 | Prof. Dr.-Eng. Ghadi Mahmoudi**

This repository contains the source code for Real-Time Traffic Simulation for the Object-Oriented Programming in Java module.

---

## 1. Project Overview

This project is a Java-based application that connects to the SUMO (Simulation of Urban MObility) traffic simulator in real-time. For this second milestone, we have developed a program that:

* **Live SUMO Integration:** Connects to a running SUMO instance via the TraaS API.
* **GUI:** A GUI that allows user to start Simulation (other buttons for other functions will be implemented for later milestones).
* **Class Design for TraaS wrapper:** a folder with packages of java files that manage multiple functions.
* **Map Visualisation:** Visualising edges, lanes, vehicles and traffic light onto our GUI
* **Vehicle Injection:** Allowing user to inject vehicles from chosen source and destination edges, with options of speed and color
* **Stress Test:** Allowing user to inject chosen number vehicles from chosen source and destination edges, with options of speed and color
* **Traffic Light Control:** Allowing user to control traffic light (switch to the next phase and modify color and duration)


### Key Classes

* `MainController.java`: the main controller of the whole application
* `MapInteractionHandler.java`: to interact with the map (zooming, panning,..)
* `VehicleManager.java`: manage the vehicles in the application
* `ReportManager.java`: generating report with statistics onto the GUI of the application
* `TrafficlightManager.java`: manage the traffic light for the whole application
* `SimulationManager.java`: manage the simulation process
* `StatisticsManager.java`: analyse the statistics of the timesteps during the Simulation
* `SimulationQueue.java`: the queue that contains SimulationState for different threads to input and extract data with each other
* `SimulationState.java`: the updated data (by the SimulationManager) required to update Vehicles and Traffic Light Panes (by the MainController) for each time step
* `CoordinateConverter.java`: convert Coordination from meters
* `ColorConverter.java`: convert SumoColor to RGBA color
* `MainGUI.java`: runner for the program, load FXML and controller
* `Renderer.java`: render the map and other components onto the GUI


## 1.1 Directory Structure

```text
root
├── README.md
├── User-Guide-Sumo-Simulation-App-Group-2.pdf
├── Project Overview.pdf
├── OOP_Java.drawio.html
├── OOP_Java-Page-1.drawio.png
├── OOP_Java-Page-1.drawio.svg
├── OOP_Java-Page-1.jpg
├── lib
│   └── TraaS.jar
├── pom.xml
├── reports
│   └── (created reports)
├── Maven Dependencies
│   └── (dependencies in pom.xml)
├── JRE System Library
│   └── (Java runtime libraries)
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── controller
│   │   │   │   ├── MainController.java
│   │   │   │   └── MapInteractionHandler.java
│   │   │   ├── data
│   │   │   │   ├── SimulationQueue.java
│   │   │   │   └── SimulationState.java
│   │   │   ├── model
│   │   │   │   ├── EdgeInfo.java
│   │   │   │   ├── ReportManager.java
│   │   │   │   ├── SimulationManager.java
│   │   │   │   ├── StatisticsManager.java
│   │   │   │   └── VehicleInfo.java
│   │   │   │   ├── infrastructure
│   │   │   │   │   ├── EdgeClass.java
│   │   │   │   │   ├── JunctionClass.java
│   │   │   │   │   ├── LaneClass.java
│   │   │   │   │   ├── MapManager.java
│   │   │   │   │   ├── TrafficlightManager.java
│   │   │   │   │   └── TrafficlightObject.java
│   │   │   │   │   └── mapInSumo.txt
│   │   │   │   └── vehicles
│   │   │   │       ├── BikeClass.java
│   │   │   │       ├── BusClass.java
│   │   │   │       ├── CarClass.java
│   │   │   │       ├── MotorbikeClass.java
│   │   │   │       ├── PedestrianClass.java
│   │   │   │       ├── VehicleClass.java
│   │   │   │       └── VehicleManager.java
│   │   │   ├── testjava
│   │   │   │   └── TestSimulationThreads.java
│   │   │   ├── util
│   │   │   │   ├── ColorConverter.java
│   │   │   │   ├── CoordinateConverter.java
│   │   │   │   └── Util.java
│   │   │   └── view
│   │   │       ├── ChartWindow.java
│   │   │       ├── MainGUI.java
│   │   │       └── Renderer.java
│   │   └── resources
│   │       ├── gui
│   │       │   ├── MainView.fxml
│   │       │   └── MainView2.fxml
│   │       │   └── style.css
│   │       └── SumoConfig
│   │           ├── frauasmap.net.xml
│   │           ├── frauasmap.osm
│   │           ├── frauasmap.rou.xml
│   │           ├── frauasmap.sumocfg
│   │           ├── minimal.sumocfg
│   │           ├── trips.trips.xml
│   │           └── vehicle_types.add.xml
├── target
│   └── (build output)
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

Or if you download the .zip file then you would need to unzip it, then go inside the folder traffic-simulator-main, there might be one more traffic-simulator-main (exact same name with the root), if you need to go to that directory to access the code.

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

* Color and Speed Filter for Vehicles Rendering onto the GUI
* Exportable reports (CSV, PDF)

---





