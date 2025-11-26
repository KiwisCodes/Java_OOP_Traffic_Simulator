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

Go to file `Java_OOP_Traffic_Simulator/src/main/java/model/SimulationManager.java` and set your variable `sumoPath` (currently `"/Users/apple/sumo/bin/sumo"`) to your SUMO path in your local device.

```java
private String sumoPath = yourSUMOpath; 
```

**3.3. Set your dependency**

Go to file `Java_OOP_Traffic_Simulator/pom.xml` and save it

=> You will see a Maven Dependencies library imported to your Directory.

**3.4. Start the SUMO Server:**

Open a terminal and run SUMO with a configuration file and the `--remote-port` argument.

```bash
sumo -c src/main/resources/frauasmap.sumocfg --remote-port 9999
```

**3.5. Run the Java Application:**

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

