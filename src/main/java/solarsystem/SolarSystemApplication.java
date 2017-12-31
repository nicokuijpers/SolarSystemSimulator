/*
 * Copyright (c) 2017 Nico Kuijpers
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished 
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR I
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package solarsystem;

import ephemeris.EphemerisUtil;
import particlesystem.Particle;
import ephemeris.SolarSystemParameters;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.Vector3D;

/**
 * Solar System Application.
 * @author Nico Kuijpers
 */
public class SolarSystemApplication extends Application {
    
    // Screen to display the bodies of the Solar System
    private Canvas screen;
    private final int BORDERSIZE = 10;
    private final int SCREENWIDTH = 900;
    private final int SCREENHEIGHT = 900;
    private final double SCREENSCALE = 180.0 * SolarSystemParameters.ASTRONOMICALUNIT;
    
    // Animation timer for drawing
    private AnimationTimer animationTimer = null;
    
    // Thread for simulation   
    private Thread threadSimulate = null;
    
    // Flag to indicate whether simulation is running
    private boolean simulationIsRunning = false;
    
    // Flag to indicate whether simulation is running in step mode
    private boolean simulationIsRunningStepMode = false;
    
    // Flag to indicate whether simulation is running fast
    private boolean simulationIsRunningFast = false;
    
    // Flag to indicate whether simulation is running forward
    private boolean simulationIsRunningForward = true;
    
    // Lock to enforce synchronization between simulating and drawing
    private final ReentrantLock simulationLock = new ReentrantLock();
    
    // Condition to indicate that simulation state may be drawn
    private final Condition simulationMayBeDrawn = simulationLock.newCondition();
    
    // Condition to indicate that simulation may advance
    private final Condition simulationMayAdvance = simulationLock.newCondition();
    
    // Flag to indicate that simulation state is being drawn
    private boolean simulationIsBeingDrawn = false;
    
    // Flag to indicate that simulation state is being updated
    private boolean simulationIsBeingUpdated = false;
    
    // Date/time selector to view and set simulation era, date, and time
    private DateTimeSelector dateTimeSelector;
    
    // Slider to set top-front view
    Slider sliderTopFrontView;
    
    // Slider to set zoom of view
    Slider sliderZoomView;
    
    // Slider to set speed of simulation
    Slider sliderSimulationSpeed;
    
    // Check box to select Earth to Sun view
    CheckBox checkBoxEarthToSunView;
    
    // Check box to indicate whether ruler should be shown
    CheckBox checkBoxShowRuler;
    
    // Check box to select step mode
    CheckBox checkBoxStepMode;
    
    // Translate
    private double translateX = 0.0;
    private double translateY = 0.0;
    private double lastDragX = 0.0;
    private double lastDragY = 0.0;
    
    // The Solar System
    private SolarSystem solarSystem;
    
    // Circles representing the bodies of the Solar System.
    // Functions as storage for position, radius, and color of circles
    // representing the bodies of the Solar System.
    private Map<String,Circle> bodies;
    
    // Bodies to be shown on screen
    private List<String> bodiesShown;
    
    // Selected body
    private String selectedBody;
    
    // Flag to indicate whether ephemeris is shown on screen
    private boolean showEphemeris = true;
    
    // Flag to indicate whether simulation results are shown on screen 
    private boolean showSimulation = true;
    
    // Flag to indicate whether view from Earth to the Sun is selected
    private boolean earthToSunView = false;
    
    // Flag to indicate whether ruler is shown on screen
    private boolean showRuler = false;
    
    // Flag to indicate whether step mode for simulation is selected
    private boolean stepMode = false;
   
    @Override
    public void start(final Stage primaryStage) {
        
        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(BORDERSIZE,BORDERSIZE,BORDERSIZE,BORDERSIZE));
        
        // For debug purposes
        // Make the grid lines visible
        // grid.setGridLinesVisible(true);
        
        // Screen to draw trajectories
        screen = new Canvas(SCREENWIDTH,SCREENHEIGHT);
        grid.add(screen,0,0,1,31);
        initTranslate();
        clearScreen();
        
        // Add mouse clicked event to screen
        screen.addEventHandler(MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    screenMouseClicked(event);
                }
            });
        
        // Add mouse pressed event to screen
        screen.addEventHandler(MouseEvent.MOUSE_PRESSED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    screenMousePressed(event);
                }
            });
        
        // Add mouse dragged event to screen
        screen.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                screenMouseDragged(event);
            }
        });
        
        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, SCREENWIDTH+2*BORDERSIZE+330, SCREENHEIGHT+2*BORDERSIZE);
        root.getChildren().add(grid);
        
        // Create the Solar System
        solarSystem = new SolarSystem();
        
        // Date/time selector to view and set era, date, and time
        dateTimeSelector = new DateTimeSelector(solarSystem.getSimulationDateTime());
        dateTimeSelector.setFont(new Font("Courier",16));
        dateTimeSelector.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (!simulationIsRunning()) {
                    initializeSimulation();
                }
            }
        });
        grid.add(dateTimeSelector,1,1,28,1);
        
        // Button to initialize simulation
        Button buttonInitialize = new Button("Initialize");
        Tooltip tooltipInitialize = 
                new Tooltip("Initialize simulation state for given date");
        buttonInitialize.setTooltip(tooltipInitialize);
        buttonInitialize.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                initializeSimulation();
            }
        });
        buttonInitialize.setMinWidth(70.0);
        grid.add(buttonInitialize,1,2,9,1);
        
        // Button to load simulation state from file
        Button buttonLoadState = new Button("Load");
        Tooltip tooltipLoadState = 
                new Tooltip("Load simulation state from file");
        buttonLoadState.setTooltip(tooltipLoadState);
        buttonLoadState.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                loadSimulationState(primaryStage);
            }
        });
        buttonLoadState.setMinWidth(70.0);
        grid.add(buttonLoadState,8,2,10,1);
        
        // Button to save current simulation state to file
        Button buttonSaveState = new Button("Save");
        Tooltip tooltipSaveState = 
                new Tooltip("Save simulation state to file");
        buttonSaveState.setTooltip(tooltipSaveState);
        buttonSaveState.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                saveSimulationState(primaryStage);
            }
        });
        buttonSaveState.setMinWidth(70.0);
        grid.add(buttonSaveState,15,2,10,1);
        
        // Button to pause simulation
        Button buttonPause = new Button("Pause");
        Tooltip tooltipPause = 
                new Tooltip("Pause simulation to view current state");
        buttonPause.setTooltip(tooltipPause);
        buttonPause.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                pauseSimulation();
            }
        });
        buttonPause.setMinWidth(70.0);
        grid.add(buttonPause,22,2,10,1);
        
        // Button to start fast backward simulation
        Button buttonFastBackward = new Button("<<");
        Tooltip tooltipFastBackward = 
                new Tooltip("Normal mode: fast backward, step mode: backward with selected speed");
        buttonFastBackward.setTooltip(tooltipFastBackward);
        buttonFastBackward.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (stepMode) {
                    startSimulationStepModeBackward();
                }
                else {
                    startSimulationFastBackward();
                }
            }
        });
        buttonFastBackward.setMinWidth(70.0);
        grid.add(buttonFastBackward,1,3,7,1);
        
        // Button to start backward simulation
        Button buttonBackward = new Button("<");
        Tooltip tooltipBackward = 
                new Tooltip("Normal mode: backward with selected speed, step mode: single step of 60 s");
        buttonBackward.setTooltip(tooltipBackward);
        buttonBackward.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (stepMode) {
                    smallStepBackward();
                }
                else {
                    startSimulationBackward();
                }
            }
        });
        buttonBackward.setMinWidth(70.0);
        grid.add(buttonBackward,8,3,7,1);
        
        // Button to start forward simulation
        Button buttonForward = new Button(">");
        Tooltip tooltipForward = 
                new Tooltip("Normal mode: forward with selected speed, step mode: single step of 60 s");
        buttonForward.setTooltip(tooltipForward);
        buttonForward.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (stepMode) {
                    smallStepForward();
                }
                else {
                    startSimulationForward();
                }
            }
        });
        buttonForward.setMinWidth(70.0);
        grid.add(buttonForward,15,3,7,1);
        
        // Button to start fast forward simulation
        Button buttonFastForward = new Button(">>");
        Tooltip tooltipFastForward = 
                new Tooltip("Normal mode: fast forward, step mode: forward with selected speed");
        buttonFastForward.setTooltip(tooltipFastForward);
        buttonFastForward.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (stepMode) {
                    startSimulationStepModeForward();
                }
                else {
                    startSimulationFastForward();
                }
            }
        });
        buttonFastForward.setMinWidth(70.0);
        grid.add(buttonFastForward,22,3,7,1);
        
        // Radio buttons to set simulation method
        // 1. Newton Mechanics
        // 2. General Relativity
        Label labelSimulationMethod = new Label("Simulation Method:");
        grid.add(labelSimulationMethod,1,4,20,1);
        RadioButton radioNewtonMechanics =
                new RadioButton("Newton Mechanics");
        Tooltip tooltipNewtonMechanics = 
                new Tooltip("Simulation based on Newton Mechanics is faster");
        radioNewtonMechanics.setTooltip(tooltipNewtonMechanics);
        radioNewtonMechanics.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                solarSystem.setGeneralRelativityFlag(false);
            }
        });
        RadioButton radioGeneralRelativity =
                new RadioButton("General Relativity");
        Tooltip tooltipGeneralRelativity = 
                new Tooltip("Simulation based on General Relativity is even more accurate");
        radioGeneralRelativity.setTooltip(tooltipGeneralRelativity);
        radioGeneralRelativity.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                solarSystem.setGeneralRelativityFlag(true);
            }
        });
        ToggleGroup simulationMethod = new ToggleGroup();
        radioNewtonMechanics.setToggleGroup(simulationMethod);
        radioGeneralRelativity.setToggleGroup(simulationMethod);
        radioNewtonMechanics.setSelected(true);
        grid.add(radioNewtonMechanics,1,5,20,1);
        grid.add(radioGeneralRelativity,1,6,20,1);
        
        // Radio buttons to set visualization of ephemeris/simulation results
        // 1. Show ephemeris only
        // 2. Show simulation only
        // 3. Show ephemeris and simulation
        Label labelVisualization = new Label("Visualization:");
        grid.add(labelVisualization,1,7,20,1);
        RadioButton radioEphemerisOnly = 
                new RadioButton("Ephemeris only");
        Tooltip tooltipEphemerisOnly = 
                new Tooltip("Show ephemeris only; simulation results are not shown");
        radioEphemerisOnly.setTooltip(tooltipEphemerisOnly);
        radioEphemerisOnly.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                showEphemeris = true;
                showSimulation = false;
            }
        });
        RadioButton radioSimulationOnly = 
                new RadioButton("Simulation only");
        Tooltip tooltipSimulationOnly = 
                new Tooltip("Show simulation only; ephemeris is not shown");
        radioSimulationOnly.setTooltip(tooltipSimulationOnly);
        radioSimulationOnly.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                showEphemeris = false;
                showSimulation = true;
            }
        });
        RadioButton radioEphemerisAndSimulation = 
                new RadioButton("Ephemeris and simulation");
        Tooltip tooltipEphemerisAndSimulation = 
                new Tooltip("Show ephemeris (green) and simulation results (blue)");
        radioEphemerisAndSimulation.setTooltip(tooltipEphemerisAndSimulation);
        radioEphemerisAndSimulation.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                showEphemeris = true;
                showSimulation = true;
            }
        });
        ToggleGroup visualizationMethod = new ToggleGroup();
        radioEphemerisOnly.setToggleGroup(visualizationMethod);
        radioSimulationOnly.setToggleGroup(visualizationMethod);
        radioEphemerisAndSimulation.setToggleGroup(visualizationMethod);
        radioEphemerisAndSimulation.setSelected(true);
        grid.add(radioEphemerisOnly,1,8,20,1);
        grid.add(radioSimulationOnly,1,9,20,1);
        grid.add(radioEphemerisAndSimulation,1,10,20,1);
        
        // Check box to select Earth-to-Sun view
        checkBoxEarthToSunView = new CheckBox("Set view from Earth to Sun");
        checkBoxEarthToSunView.setSelected(earthToSunView);
        checkBoxEarthToSunView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                earthToSunView = checkBoxEarthToSunView.selectedProperty().getValue();
                if (earthToSunView) {
                    sliderTopFrontView.setValue(0.0);
                    checkBoxStepMode.setSelected(true);
                    stepMode = true;
                }
                else {
                    sliderTopFrontView.setValue(90.0);
                }
            }
        });
        Tooltip toolTipEarthToSunView = 
                new Tooltip("Check to set view from surface of the Earth toward the Sun");
        checkBoxEarthToSunView.setTooltip(toolTipEarthToSunView);
        grid.add(checkBoxEarthToSunView,1,11,20,1);
        
        // Check box to indicate whether ruler should be shown
        checkBoxShowRuler = new CheckBox("Show ruler");
        checkBoxShowRuler.setSelected(showRuler);
        checkBoxShowRuler.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showRuler = checkBoxShowRuler.selectedProperty().getValue();
            }
        });
        Tooltip toolTipShowRuler = 
                new Tooltip("Check to show ruler indicating distance or angular diameter");
        checkBoxShowRuler.setTooltip(toolTipShowRuler);
        grid.add(checkBoxShowRuler,1,12,20,1);
        
        // Check box to select step mode
        checkBoxStepMode = new CheckBox("Single-step mode");
        checkBoxStepMode.setSelected(stepMode);
        checkBoxStepMode.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stepMode = checkBoxStepMode.selectedProperty().getValue();
                if (stepMode) {
                    pauseSimulation();
                }
            }
        });
        Tooltip toolTipStepMode = 
                new Tooltip("Check to simulate in single-step mode and advance 60 s at a time");
        checkBoxStepMode.setTooltip(toolTipStepMode);
        grid.add(checkBoxStepMode,1,13,20,1);
        
        // Slider to set top-bottom view
        Label labelView = new Label("View");
        grid.add(labelView,1,14,9,1);
        sliderTopFrontView = new Slider();
        sliderTopFrontView.setMin(-90);
        sliderTopFrontView.setMax(90);
        sliderTopFrontView.setValue(90);
        sliderTopFrontView.setShowTickLabels(true);
        sliderTopFrontView.setShowTickMarks(true);
        sliderTopFrontView.setSnapToTicks(false);
        sliderTopFrontView.setMajorTickUnit(20);
        sliderTopFrontView.setMinorTickCount(10);
        sliderTopFrontView.setBlockIncrement(10);
        grid.add(sliderTopFrontView,1,15,28,1);
        
        // Slider to set zoom of view
        Label labelZoom = new Label("Zoom");
        grid.add(labelZoom,1,16,9,1);
        sliderZoomView = new Slider();
        sliderZoomView.setMin(0);
        sliderZoomView.setMax(100);
        sliderZoomView.setValue(30);
        sliderZoomView.setShowTickLabels(true);
        sliderZoomView.setShowTickMarks(true);
        sliderZoomView.setSnapToTicks(false);
        sliderZoomView.setMajorTickUnit(10);
        sliderZoomView.setMinorTickCount(10);
        sliderZoomView.setBlockIncrement(10);
        grid.add(sliderZoomView,1,17,28,1);
        
        // Slider to set simulation speed
        Label labelSpeed = new Label("Speed");
        grid.add(labelSpeed,1,18,9,1);
        sliderSimulationSpeed = new Slider();
        sliderSimulationSpeed.setMin(0);
        sliderSimulationSpeed.setMax(100);
        sliderSimulationSpeed.setValue(50);
        sliderSimulationSpeed.setShowTickLabels(true);
        sliderSimulationSpeed.setShowTickMarks(true);
        sliderSimulationSpeed.setSnapToTicks(false);
        sliderSimulationSpeed.setMajorTickUnit(10);
        sliderSimulationSpeed.setMinorTickCount(10);
        sliderSimulationSpeed.setBlockIncrement(10);
        grid.add(sliderSimulationSpeed,1,19,28,1);
        
        // Define circles for each body of the Solar System
        // Functions as storage for position, radius, and color of circles
        // representing the bodies of the Solar System.
        bodies = new HashMap<>();
        bodiesShown = new ArrayList<>();
        createCircle("sun", 10, Color.YELLOW);
        createCircle("mercury", 3, Color.ORANGE);
        createCircle("venus", 5, Color.BROWN);
        createCircle("moon", 3, Color.GRAY);
        createCircle("earth", 5, Color.AQUAMARINE);
        createCircle("mars", 4, Color.RED);
        createCircle("jupiter", 10, Color.ROSYBROWN);
        createCircle("saturn", 9, Color.ORANGE);
        createCircle("uranus", 7, Color.LIGHTBLUE);
        createCircle("neptune", 7, Color.BLUE);
        createCircle("pluto", 3, Color.LIGHTBLUE);
        createCircle("eris", 3, Color.LIGHTSALMON);
        createCircle("chiron", 4, Color.CRIMSON);
        createCircle("ceres", 3, Color.ORANGE);
        createCircle("pallas", 3, Color.LIGHTGREEN);
        createCircle("juno", 3, Color.ROSYBROWN);
        createCircle("vesta", 3, Color.YELLOW);
        createCircle("eros", 3, Color.LIGHTBLUE);
        createCircle("halley", 7, Color.YELLOW);
        createCircle("encke", 6, Color.LIGHTGREEN);
        createCircle("p67cg", 5, Color.ORANGE);
        createCircle("halebopp", 5, Color.LIGHTBLUE);
        createCircle("shoelevy9", 5, Color.PINK);
        createCircle("florence", 3, Color.LIGHTGREEN);

        // Define check box for each body of the solar system
        int hor = 1;
        int ver = 20;
        int horsize = 10;
        int versize = 1;
        grid.add(createCheckBox("sun", "Sun",
                "The Sun is in fact a star and is the largest object in our "
                + "Solar System.",
                true), hor, ver++, horsize, versize);
        grid.add(createCheckBox("mercury", "Mercury",
                "Mercury is the smallest and innermost planet. "
                + "It orbits around the Sun in 88 days.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("venus", "Venus",
                "Venus is the second planet from the Sun and is of similar size "
                + " as the Earth.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("earth", "Earth",
                "Earth is the third planet from the Sun and the only "
                + "planet known to harbor life.",
                true), hor, ver++, horsize, versize);
        grid.add(createCheckBox("moon", "Moon",
                "Zoom in to see the moon orbiting around the Earth.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("mars", "Mars",
                "Mars is the second-smallest planet and is also known as the Red Planet.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("jupiter", "Jupiter",
                "Jupiter is the largest planet in the Solar System. "
                + "Galileo Galilei discovered the four largest moons in 1610.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("saturn", "Saturn",
                "Saturn is the second-largest planet and is famous for his rings.",
                // "Visited by Pioneer 11, Voyager 1 and 2, and Cassini-Huygens.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("uranus", "Uranus",
                "Uranus was discovered in 1781 by William Hershel. "
                + "Visited by Voyager 2 in 1986.", false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("neptune", "Neptune",
                "Neptune was discovered in 1846. "
                + "Visited by Voyager 2 on 25 August 1989.", false), hor, ver++, horsize, versize);
        hor = hor + 9;
        ver = 20;
        grid.add(createCheckBox("pluto", "Pluto",
                "Pluto was discovered in 1930 and was considered the "
                + "ninth planet until 2006. Visited by New Horizons on 14 July 2015.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("eris", "Eris",
                "Eris is the most massive and second-largest dwarf planet known "
                + "in the Solar System.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("chiron", "Chiron",
                "Chiron was discovered in 1977 and orbits between Saturn and Uranus. "
                + "It is the first object of the Centaur class",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("ceres", "Ceres",
                "Ceres is a dwarf planet and the largest object in the asteroid belt.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("pallas", "2 Pallas",
                "Pallas was the second asteroid discovered after Ceres and "
                + "the third-most-massive asteroid after Vesta",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("juno", "3 Juno",
                "Juno was the third asteroid discovered and is the 11th largest asteroid",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("vesta", "4 Vesta",
                "Vesta is the second-largest body in the asteroid belt after Ceres",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("eros", "433 Eros",
                "Eros is a near-Earth astroid. NASA spacecraft NEAR Shoemaker "
                + "entered orit around Eros in 2000, and landed in 2001.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("florence", "Florence",
                "Asteroid 3122 Florence approached Earth within 0.047 au on "
                + "1 September 2017.",
                false), hor, ver++, horsize, versize);
        hor = hor + 9;
        ver = 20;
        grid.add(createCheckBox("halley", "1P/Halley",
                "Halley's Comet has a period of 76 years. Last perihelion 9 Feb 1986. "
                + "Next perihelion 28 July 2061.", false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("encke", "2P/Encke",
                "P2/Encke was the first periodic comet discovered after "
                + "Halley's Comet.", false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("p67cg", "67P/Ch-Ge",
                "67P/Churyumov-Gerasimenko was visited "
                + "by ESA's Rosetta mission on 6 August 2014.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("shoelevy9", "Shoe-Lev 9",
                "Shoemaker-Levy 9 collided with Jupiter in July 1994.",
                false), hor, ver++, horsize, versize);
        grid.add(createCheckBox("halebopp", "Hale-Bopp",
                "Hale-Bopp passed perihelion on 1 April 1997 and "
                + "was visible to the naked eye for 18 months.",
                false), hor, ver++, horsize, versize);
        
        // Define title and assign the scene for main window
        primaryStage.setTitle("Solar System Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Start thread to simulate the Solar System
        threadSimulate = new Thread(new SimRunnable());
        threadSimulate.start();
        
        // Start animation timer to draw the Solar System each 20 ms
        animationTimer = new DrawAnimationTimer();
        animationTimer.start();
    }
    
    private synchronized void initializeSimulation() {
        // Simulation is not running
        simulationIsRunning = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;
        
        // Initialize simulation
        try {
            solarSystem.initializeSimulation(dateTimeSelector.getDateTime());
        } catch (SolarSystemException ex) {
            showMessage("Error",ex.getMessage());
        }
    }
    
    private synchronized void startSimulationFastBackward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = true;
        simulationIsRunningForward = false;
        // Start thread to simulate solar system
        // threadSimulate = new Thread(new SimRunnable());
        // threadSimulate.start();
    }
    
    private synchronized void startSimulationBackward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = false;
        // Start thread to simulate solar system
        // threadSimulate = new Thread(new SimRunnable());
        // threadSimulate.start();
    }
    
    private synchronized void startSimulationStepModeBackward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = true;
        simulationIsRunningFast = false;
        simulationIsRunningForward = false;
    }
    
    private synchronized void smallStepBackward() {
        // Pause simulation when running
        pauseSimulation();
        
        // Advance 1 minute backward
        solarSystem.advanceSimulationSingleStep(-60);
        
        // Update simulation date/time shown in date/time selector
        dateTimeSelector.setDateTime(solarSystem.getSimulationDateTime());
    }
    
    private synchronized void smallStepForward() {
        // Pause simulation when running
        pauseSimulation();
        
        // Advance 1 minute forward
        solarSystem.advanceSimulationSingleStep(60);
        
        // Update simulation date/time shown in date/time selector
        dateTimeSelector.setDateTime(solarSystem.getSimulationDateTime());
    }
    
    private synchronized void startSimulationStepModeForward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = true;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;
    }
    
    private synchronized void startSimulationForward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;
        // Start thread to simulate solar system
        // threadSimulate = new Thread(new SimRunnable());
        // threadSimulate.start();
    }
    
    private synchronized void startSimulationFastForward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = true;
        simulationIsRunningForward = true;
        // Start thread to simulate solar system
        // threadSimulate = new Thread(new SimRunnable());
        // threadSimulate.start();
    }
    
    private synchronized void pauseSimulation() {
        simulationIsRunning = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;
        // Stop thread to simulate solar system
        // threadSimulate = new Thread(new SimRunnable());
        // threadSimulate.interrupt();
    }
    
    private synchronized boolean simulationIsRunning() {
        return simulationIsRunning;
    }
    
    private synchronized void loadSimulationState(Stage stage) {
        if (simulationIsRunning()) {
            pauseSimulation();
        }
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            simulationLock.lock();
            try {
                if (simulationIsBeingDrawn) {
                    simulationMayAdvance.await();
                }
                simulationIsBeingUpdated = true;
                try {
                    solarSystem.loadSimulationState(file);
                } catch (SolarSystemException ex) {
                    showMessage("Error",ex.getMessage());
                }
                simulationIsBeingUpdated = false;
                simulationMayBeDrawn.signal();  
            } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
            } finally {
                simulationLock.unlock();
            }
        }

        // Update simulation date/time shown in date/time selector
        dateTimeSelector.setDateTime(solarSystem.getSimulationDateTime());
    }
    
    private synchronized void saveSimulationState(Stage stage) {
        if (simulationIsRunning()) {
            pauseSimulation();
        }
        String dateTimeString = dateTimeSelector.getText();
        dateTimeString = dateTimeString.substring(0,19);
        dateTimeString = dateTimeString.replace(":", "-");
        dateTimeString = dateTimeString.replaceAll(" ", "_");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(dateTimeString + ".sol");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                solarSystem.saveSimulationState(file);
            } catch (SolarSystemException ex) {
                showMessage("Error",ex.getMessage());
            }
        }
    }
    
    private Circle createCircle(String name, int radius, Color color) {
        Circle circle = new Circle(0.0,0.0,radius,color);
        circle.setVisible(false);
        bodies.put(name, circle);
        return circle;
    }
    
    private CheckBox createCheckBox(final String name, String label, String toolTipText,
            boolean selected) {
        if (selected) {
            bodiesShown.add(name);
        }
        final CheckBox checkBox = new CheckBox(label);
        checkBox.setSelected(selected);
        checkBox.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean isSelected = checkBox.selectedProperty().getValue();
                if (isSelected) {
                    bodiesShown.add(name);
                }
                else {
                    bodiesShown.remove(name);
                }
            }
        });
        Tooltip toolTip = new Tooltip(toolTipText);
        checkBox.setTooltip(toolTip);
        return checkBox;
    }
    
    private double screenX(Vector3D position) {
        return translateX + SCREENWIDTH * (position.getX() / SCREENSCALE);
    }
    
    private double screenY(Vector3D position) {
        return translateY + SCREENHEIGHT * (1.0 - position.getY() / SCREENSCALE);
    }
    
    private Vector3D rotateForEarthToSunView(Vector3D position) {
        // Rotate along z-axis for Earth to Sun view
        // It is assumed that the Sun's position in is in the origin, i.e,
        // the (x, y, z) position of the Sun should be (0.0, 0.0, 0.0)
        Vector3D positionEarth = null;
        if (showSimulation) {
            positionEarth = solarSystem.getParticle("earth").getPosition();
        } else {
            positionEarth = solarSystem.getBody("earth").getPosition();
        }
        double angleRadZ = Math.atan2(positionEarth.getY(), positionEarth.getX());
        Vector3D positionRotatedZ = position.rotateZrad(-angleRadZ - Math.PI/2.0);
        
        // Translate in z-direction to correct for deviation of the Earth from the xy-plane
        // This deviation is near zero around 2000 and increases for earlier or later dates. 
        // Use slider top-front view to determine the lattitude of the viewing position on 
        // the surface of the Earth. The distance in z-direction from the center of the 
        // Earth is equal to sin(lattitude) * diameter/2.
        // The latter correction is necessary to get a total solar eclipse on March 20, 2015
        // with the slider set to approximately +70 degrees. This solar eclipse was only visible 
        // in the far north.
        // https://en.wikipedia.org/wiki/Solar_eclipse_of_March_20,_2015
        double lattitudeDeg = sliderTopFrontView.getValue();
        double lattitudeRad = Math.toRadians(lattitudeDeg);
        double distanceFromCenter = Math.sin(lattitudeRad) * solarSystem.getBody("earth").getDiameter()/2.0;
        Vector3D positionTranslatedZ = 
                positionRotatedZ.minus(new Vector3D(0.0,0.0,positionEarth.getZ() + distanceFromCenter));
        
        // Take perspective into account in x- and z-direction
        // Assume that we are standing on the surface of the Earth
        double distance = position.euclideanDistance(positionEarth);
        distance = distance - solarSystem.getBody("earth").getDiameter()/2.0;
        double factor = SolarSystemParameters.ASTRONOMICALUNIT/distance;
        double x = positionTranslatedZ.getX() * factor;
        double y = positionTranslatedZ.getY();
        double z = positionTranslatedZ.getZ() * factor;
        return new Vector3D(x,y,z);
    }
    
    private Vector3D convertToScreenView(Vector3D position) {
        // Translate position when a body is selected
        Vector3D positionAfterTranslation = new Vector3D(position);
        if (!earthToSunView && selectedBody != null) {
            if (showEphemeris && !showSimulation) {
                // Place computed position of selected body in the center
                SolarSystemBody body = solarSystem.getBody(selectedBody);
                positionAfterTranslation = position.minus(body.getPosition());
            }
            else {
                // Place simulated position of selected body in the center
                Particle particle = solarSystem.getParticle(selectedBody);
                if (particle != null) {
                    positionAfterTranslation = position.minus(particle.getPosition());
                }
            }
        }
        
        // Rotate along x-axis
        Vector3D positionAfterRotationX = null;
        if (earthToSunView) {
            // Rotate along x-axis such that the solar system is viewed from the 
            // epliptic. The slider top-front view is used to adjust the viewing position
            // for lattitude (see method rotateForEarthToSunView)
            positionAfterRotationX = positionAfterTranslation.rotateXrad(-Math.PI/2.0);
        }
        else {
            // Use slider top-front view to select between viewing the solar system
            // from the north ecliptic pole (90 degrees), the ecliptic (0 degrees) or 
            // the south ecliptic pole (-90 degrees)
            // https://en.wikipedia.org/wiki/Ecliptic
            double rotationAngleX = sliderTopFrontView.getValue() - 90.0;
            positionAfterRotationX = positionAfterTranslation.rotateXdeg(rotationAngleX);
        }
        
        // Use slider zoom view to zoom in on the scene
        double zoom = Math.exp(0.1*sliderZoomView.getValue());
        return positionAfterRotationX.scalarProduct(zoom);
    }
    
    /**
     * Draw circle for body to be shown on screen.
     * @param circle   circle properties corresponding to body   
     * @param body     the body for which circle is drawn
     * @param position position of the body to be shown
     */
    private void drawCircle(Circle circle, SolarSystemBody body, Vector3D position) {
        double diameter = body.getDiameter();
        if (earthToSunView && !"earth".equals(body.getName())) {
            // Scale diameter with distance from surface of the Earth
            // The Sun and the Moon will have the same apparent size
            // This is necessary for a correct representation of a total solar eclipse
            Vector3D positionEarth = null;
            if (showSimulation) {
                positionEarth = solarSystem.getParticle("earth").getPosition();
            }
            else {
                positionEarth = solarSystem.getBody("earth").getPosition();
            }
            double distance = position.euclideanDistance(positionEarth);
            distance = distance - solarSystem.getBody("earth").getDiameter()/2.0;
            diameter = (SolarSystemParameters.ASTRONOMICALUNIT * diameter) / distance;
        }
        
        // Determine position on screen
        Vector3D positionView = null;
        if (earthToSunView) {
            positionView = convertToScreenView(rotateForEarthToSunView(position));
        }
        else {
            positionView = convertToScreenView(position);
        }
        double posx = screenX(positionView);
        double posy = screenY(positionView);
        circle.setCenterX(posx);
        circle.setCenterY(posy);
        
        // Determine radius from body diameter with a minimum equal to circle diameter
        Vector3D diameterBegin = new Vector3D();
        Vector3D diameterEnd = new Vector3D(diameter, 0.0, 0.0);
        Vector3D diameterBeginView = convertToScreenView(diameterBegin);
        Vector3D diameterEndView = convertToScreenView(diameterEnd);
        double diameterPixels = screenX(diameterEndView) - screenX(diameterBeginView);
        double radius = Math.max(circle.getRadius(),diameterPixels/2.0);
 
        // Draw circle on screen using color and radius from Circle-object
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.setFill(circle.getFill());
        gc.fillOval(posx - radius, posy - radius, 2 * radius, 2 * radius);
    }
    
    /**
     * Draw circles for bodies to show on screen.
     * @param bodiesToShow bodies to show on screen
     */
    private void drawCircles(List<SolarSystemBody> bodiesToShow) { 
        for (SolarSystemBody body : bodiesToShow) {
            String bodyName = body.getName();
            Circle circle = bodies.get(bodyName);
            if (showEphemeris && !showSimulation) {
                // Draw circle at positon of body
                drawCircle(circle, body, body.getPosition());
            } else {
                // Draw circle at particle position
                Particle particle = solarSystem.getParticle(bodyName);
                if (particle != null) {
                    drawCircle(circle, body, particle.getPosition());
                }
            }
        }
    }

    /**
     * Set stroke and fill color depending on position with respect to the Sun.
     * @param gc         graphics context
     * @param position   position
     * @param frontColor color when in front of the Sun
     * @param backColor  color when behind the Sun
     */
    private void setColor(GraphicsContext gc, Vector3D position, 
            Color frontColor, Color backColor) {
        if (position.getY() <= 0.0) {
            gc.setStroke(frontColor);
            gc.setFill(frontColor);
        }
        else {
            gc.setStroke(backColor);
            gc.setFill(backColor);
        }
    }
    
    /**
     * Draw orbit of body. Orbit segments in front of the Sun are drawn
     * using frontColor and Orbit segments behind the Sun are drawn
     * using backColor. A small circle to indicate the position of the body
     * is drawn when drawSmallCircle is true.
     * @param orbit            the orbit
     * @param frontColor       color for orbit segments in front of the Sun
     * @param backColor        color for orbit segments behind the Sun
     * @param drawSmallCircle  indicates whether small circle should be drawn
     */
    private void drawOrbit(Vector3D[] orbit, Color frontColor, Color backColor, 
            boolean drawSmallCircle) {
        
        GraphicsContext gc = screen.getGraphicsContext2D();
        Vector3D positionView = null;
        if (earthToSunView) {
            Vector3D orbitPositionRotated = rotateForEarthToSunView(orbit[0]);
            setColor(gc,orbitPositionRotated,frontColor,backColor);
            positionView = convertToScreenView(orbitPositionRotated);
        }
        else {
            positionView = convertToScreenView(orbit[0]);
            setColor(gc,orbit[0],frontColor,backColor);
        }
        double x1 = screenX(positionView);
        double y1 = screenY(positionView);
        if (drawSmallCircle) {
            // Draw circle to indicate first position of orbit
            gc.fillOval(x1 - 3, y1 - 3, 6, 6);
        }
        for (int i = 1; i < orbit.length; i++) {
            if (earthToSunView) {
                Vector3D orbitPositionRotated = rotateForEarthToSunView(orbit[i]);
                setColor(gc,orbitPositionRotated,frontColor,backColor);
                positionView = convertToScreenView(orbitPositionRotated);
            }
            else {
                positionView = convertToScreenView(orbit[i]);
                setColor(gc,orbit[i],frontColor,backColor);
            }
            double x2 = screenX(positionView);
            double y2 = screenY(positionView);
            gc.strokeLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
            // Draw dot to indicate beginning of each line segment
            // gc.fillOval(x1-2, y1-2, 4, 4);
        }
        if (earthToSunView) {
            Vector3D orbitPositionRotated = rotateForEarthToSunView(orbit[0]);
            setColor(gc,orbitPositionRotated,frontColor,backColor);
            positionView = convertToScreenView(orbitPositionRotated);
        }
        else {
            positionView = convertToScreenView(orbit[0]);
            setColor(gc,orbit[0],frontColor,backColor);
        }
        double x2 = screenX(positionView);
        double y2 = screenY(positionView);
        gc.strokeLine(x1, y1, x2, y2);
    }

    /**
     * Draw computed position and orbit of bodies.
     * Positions are drawn as green circles.
     * Orbits are drawn as green lines.
     * @param bodiesToShow bodies to show on screen
     */
    private void drawOrbits(List<SolarSystemBody> bodiesToShow) {
        if (showEphemeris) {
            for (SolarSystemBody body : bodiesToShow) {
                Vector3D[] orbit = body.getOrbit();
                // Draw orbit as a green line
                if (orbit != null) {
                    drawOrbit(orbit, Color.LIGHTGREEN, Color.GREEN, showSimulation);
                }
            }
        }
    }

    /**
     * Draw orbit corresponding to current position and velocity of particle.
     * Orbit is drawn as a dark cyan line.
     * @param centerBody name of the center body
     * @param particle the particle
     */
    private void drawOrbitCorrespondingToPositionVelocity(String centerBody, Particle particle) {
        // Position and velocity of center body
        Vector3D positionCenterBody = solarSystem.getParticle(centerBody).getPosition();
        Vector3D velocityCenterBody = solarSystem.getParticle(centerBody).getVelocity();
        
        // Position and velocity of particle
        Vector3D positionParticle = particle.getPosition();
        Vector3D velocityParticle = particle.getVelocity();
        
        // Compute orbit of particle relative to center body
        Vector3D positionRelativeToCenterBody = positionParticle.minus(positionCenterBody);
        Vector3D velocityRelativeToCenterBody = velocityParticle.minus(velocityCenterBody);
        Vector3D[] orbitRelativeToCenterBody = EphemerisUtil.computeOrbit(centerBody,
                positionRelativeToCenterBody,velocityRelativeToCenterBody);
        
        // Compute orbit
        Vector3D[] orbit = new Vector3D[orbitRelativeToCenterBody.length];
        for (int i = 0; i < orbitRelativeToCenterBody.length; i++) {
            orbit[i] = positionCenterBody.plus(orbitRelativeToCenterBody[i]);
        }

        // Draw the orbit of the particle as a cyan line
        if (orbit != null) {
            drawOrbit(orbit,Color.CYAN,Color.DARKCYAN,false);
        }
    }
    
    /**
     * Draw orbits corresponding to current positions and velocities of particles.
     * It is assumed that the particle is orbiting the sun (exception "moon").
     * Orbits are drawn as dark cyan lines.
     * @param bodiesToShow bodies to show on screen
     */
    private void drawOrbitsCorrespondingToPositionVelocity(List<SolarSystemBody> bodiesToShow) {
        if (showSimulation) {
            for (SolarSystemBody body : bodiesToShow) {
                String bodyName = body.getName();
                Particle particle = solarSystem.getParticle(bodyName);
                if (particle != null) {
                    if ("moon".equals(bodyName)) {
                        drawOrbitCorrespondingToPositionVelocity("earth", particle);
                    } else {
                        drawOrbitCorrespondingToPositionVelocity("sun", particle);
                    }
                }
            }
        }
    }
    
    /**
     * Draw ruler at the bottom of the screen to get an indication of distances.
     */
    private void drawRulerDistance() {
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.setStroke(Color.CYAN);
        gc.setFill(Color.CYAN);
        Vector3D scaleBegin = new Vector3D();
        Vector3D scaleEnd = new Vector3D(1E11, 0.0, 0.0); // 100 million km
        Vector3D scaleBeginView = convertToScreenView(scaleBegin);
        Vector3D scaleEndView = convertToScreenView(scaleEnd);
        double scaleLength = screenX(scaleEndView) - screenX(scaleBeginView);
        long scale = 100000000; // 100 million km
        // Adjust scale length and scale to fit screen
        while (scaleLength < 50.0) {
            scaleLength *= 10;
            scale *= 10;
        }
        while (scaleLength > 600.0) {
            scaleLength /= 10;
            scale /= 10;
        }
        double x = 50.0;
        double y = 860.0;
        // Draw the scale als a cyan line
        gc.strokeLine(x, y, x + scaleLength, y);
        // Place text "0" at the left end
        String textBegin = 0 + "";
        // Place scale length in km at the right end
        String textEnd = "";
        if (scale >= 1000000000) {
            textEnd = scale / 1000000000 + " billion km";
        } else if (scale >= 1000000) {
            textEnd = scale / 1000000 + " million km";
        } else {
            textEnd = scale / 1000 + " thousand km";
        }
        gc.fillText(textBegin, x - 5.0, y - 15.0);
        gc.fillText(textEnd, x + scaleLength - 30.0, y - 15.0);
        // Place ticks on the scale
        for (int i = 0; i <= 10; i++) {
            gc.strokeLine(x + i * 0.1 * scaleLength, y - 4, x + i * 0.1 * scaleLength, y);
        }
    }

    /**
     * Draw ruler at the bottom of the screen to get an indication of angular diameters.
     */
    private void drawRulerAngularDiameter() {
        // http://hudsonvalleygeologist.blogspot.nl/2012/04/size-of-sun.html
        // Viewed from the surface of the Earth, the size of the Sun is about 0.5 degrees
        double diameterSun = SolarSystemParameters.getInstance().getDiameter("sun");
        double distance = SolarSystemParameters.ASTRONOMICALUNIT;
        double angularDiameterSunRad = 2 * Math.atan((diameterSun / 2.0) / distance);
        double angularDiameterSunDeg = Math.toDegrees(angularDiameterSunRad);
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.setStroke(Color.CYAN);
        gc.setFill(Color.CYAN);
        Vector3D scaleBegin = new Vector3D();
        Vector3D scaleEnd = new Vector3D(diameterSun / angularDiameterSunDeg, 0.0, 0.0); // 1 degree
        Vector3D scaleBeginView = convertToScreenView(scaleBegin);
        Vector3D scaleEndView = convertToScreenView(scaleEnd);
        double scaleLength = screenX(scaleEndView) - screenX(scaleBeginView);
        double scale = 1.0; // 1 degree
        // Adjust scale length and scale to fit screen
        while (scaleLength < 50.0) {
            scaleLength *= 10;
            scale *= 10;
        }
        while (scaleLength > 600.0) {
            scaleLength /= 10;
            scale /= 10;
        }
        double x = 50.0;
        double y = 860.0;
        // Draw the scale as a cyan line
        gc.strokeLine(x, y, x + scaleLength, y);
        // Place text "0 " at the left end
        String textBegin = 0 + "";
        // Place scale length in degrees at the right end
        String textEnd = "";
        if (scale >= 1.0) {
            textEnd = scale + " degrees";
        } else if (scale >= 1.0 / 60) {
            textEnd = scale * 60 + " arcminutes";
        } else {
            textEnd = scale * 3600 + " arcseconds";
        }
        gc.fillText(textBegin, x - 5.0, y - 15.0);
        gc.fillText(textEnd, x + scaleLength - 30.0, y - 15.0);
        // Place ticks on the scale
        for (int i = 0; i <= 10; i++) {
            gc.strokeLine(x + i * 0.1 * scaleLength, y - 4, x + i * 0.1 * scaleLength, y);
        }
    }
    
    /**
     * Compute distance of mouse-position to a circle.
     * This method is used to set the selected body when the
     * left mouse button is clicked, see method screenMouseClicked().
     * @param event  Mouse event containing mouse-position
     * @param circle Circle for which distance should be computed
     * @return distance to the circle in pixels
     */
    private double distanceCircle(MouseEvent event, Circle circle) {
        double dx = event.getX() - circle.getCenterX();
        double dy = event.getY() - circle.getCenterY();
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    /**
     * Set selected body when the left mouse button is clicked.
     * @param event Mouse event
     */
    private void screenMouseClicked(MouseEvent event) {
        selectedBody = null;
        double minDistance = 20.0;
        for (String bodyName : bodiesShown) {
            Circle circle = bodies.get(bodyName);
            double distance = distanceCircle(event, circle);
            if (distance < minDistance) {
                selectedBody = bodyName;
                minDistance = distance;
            }
        }
    }

    private void screenMouseDragged(MouseEvent event) {
        translateX = translateX + event.getX() - lastDragX;
        translateY = translateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    private void screenMousePressed(MouseEvent event) {
        lastDragX = event.getX();
        lastDragY = event.getY();
    }                                                                        

    private void initTranslate() {
        translateX = 0.5 * SCREENWIDTH;
        translateY = -0.5 * SCREENHEIGHT;
        lastDragX = 0.0;
        lastDragY = 0.0;
    }
    
    /**
     * Clear screen and make background blue for Earth-to-Sun view
     * or black for normal view.
     */
    private void clearScreen() {
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.clearRect(0.0,0.0,SCREENWIDTH,SCREENHEIGHT);
        if (earthToSunView) {
            gc.setFill(Color.BLUE);
        }
        else {
            gc.setFill(Color.BLACK);
        }
        gc.fillRect(0.0,0.0,SCREENWIDTH,SCREENHEIGHT);
    }
    
    @Override
    public void stop() {
        if (threadSimulate != null) {
            threadSimulate.interrupt();
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
    
    /**
     * Main method. 
     * Not used for JavaFX application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Sort bodies such that they are drawn in an order that corresponds
     * to the currently selected view (Earth-to-Sun view or normal view).
     * For Earth-to-Sun view, the bodies are sorted such that bodies behind the
     * Sun as seen from the Earth are drawn first.
     * For normal view, the bodies are sorted such that bodies behind the Sun
     * are drawn first.
     */
    private void sortBodiesShown() {
        if (earthToSunView) {
            // Sort bodies, such that bodies behind the sun as seen from the Earth are drawn first
            Collections.sort(bodiesShown, new Comparator<String>() {
                @Override
                public int compare(String bodyName1, String bodyName2) {
                    SolarSystemBody body1 = solarSystem.getBody(bodyName1);
                    SolarSystemBody body2 = solarSystem.getBody(bodyName2);
                    Vector3D positionRotatedBody1 = rotateForEarthToSunView(body1.getPosition());
                    Vector3D positionRotatedBody2 = rotateForEarthToSunView(body2.getPosition());
                    if (positionRotatedBody1.getY() < positionRotatedBody2.getY()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
        } else {
            // Sort bodies, such that bodies behind the Sun are drawn first
            Collections.sort(bodiesShown, new Comparator<String>() {
                @Override
                public int compare(String bodyName1, String bodyName2) {
                    SolarSystemBody body1 = solarSystem.getBody(bodyName1);
                    SolarSystemBody body2 = solarSystem.getBody(bodyName2);
                    if (body1.getPosition().getY() < body2.getPosition().getY()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
        }
    }
    
    /**
     * Draw simulation state of Solar System on screen.
     */
    private void drawSimulationState() {
        // Update current simulation date/time
        if (simulationIsRunning()) {
            dateTimeSelector.setDateTime(solarSystem.getSimulationDateTime());
        }
        
        // Draw bodies of the solar system and their orbits
        sortBodiesShown();
        clearScreen();
        List<SolarSystemBody> bodiesToShow = new ArrayList<>();
        for (String bodyName : bodiesShown) {
            bodiesToShow.add(solarSystem.getBody(bodyName));
        }
        SolarSystemBody earth = solarSystem.getBody("earth");
        if (earthToSunView) {
            // Do not show the Earth for Earth-to-Sun view
            bodiesToShow.remove(earth);
        }
        drawOrbitsCorrespondingToPositionVelocity(bodiesToShow);
        drawOrbits(bodiesToShow);
        drawCircles(bodiesToShow);
        if (showRuler && !earthToSunView) {
            drawRulerDistance();
        }
        if (showRuler && earthToSunView) {
            drawRulerAngularDiameter();
        }
    }
    
    /**
     * Show an alert message. 
     * The message will disappear when the user presses ok.
     * @param header   Header of the alert message
     * @param content  Content of the alert message
     */
    private void showMessage(final String header, final String content) {
        // Use Platform.runLater() to ensure that code concerning 
        // the Alert message is executed by the JavaFX Application Thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Solar System Simulator");
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.showAndWait();
            }
        });  
    }
    
    
    /**
     * Inner-class for drawing the Solar System.
     */
    private class DrawAnimationTimer extends AnimationTimer {

        private long prevUpdate;

        @Override
        public void handle(long now) {

            long lag = now - prevUpdate;
            if (lag >= 20000000) {
                simulationLock.lock();
                try {
                    if (simulationIsBeingUpdated) {
                        simulationMayBeDrawn.await();
                    }
                    simulationIsBeingDrawn = true;
                    drawSimulationState();
                    simulationIsBeingDrawn = false;
                    simulationMayAdvance.signal();
                } catch (InterruptedException ex) {
                    stop();
                } finally {
                    simulationLock.unlock();
                }
                prevUpdate = now;
            }
        }

        @Override
        public void start() {
            prevUpdate = System.nanoTime();
            super.start();
        }
    }

    /**
     * Inner-class for simulating the Solar System.
     */
    private class SimRunnable implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int pause = 0;
                    int nrTimeSteps = 0;
                    if (simulationIsRunning()) {
                        simulationLock.lock();
                        
                        try {
                            if (simulationIsBeingDrawn) {
                                simulationMayAdvance.await();
                            }
                            simulationIsBeingUpdated = true;
                            
                            if (simulationIsRunningFast) {
                                nrTimeSteps = 24;
                                pause = 1;
                            }
                            else {
                                nrTimeSteps = 1;
                                pause = 1 + (20 - (int)(sliderSimulationSpeed.getValue()/5.0));
                                if (simulationIsRunningStepMode) {
                                    pause = pause * 10;
                                }
                            }
                            if (simulationIsRunningForward) {
                                if (simulationIsRunningStepMode) {
                                    solarSystem.advanceSimulationSingleStep(60);
                                }
                                else {
                                    solarSystem.advanceSimulationForward(nrTimeSteps);
                                }
                            }
                            else {
                                if (simulationIsRunningStepMode) {
                                    solarSystem.advanceSimulationSingleStep(-60);
                                }
                                else {
                                    solarSystem.advanceSimulationBackward(nrTimeSteps);
                                }
                            }
                            simulationIsBeingUpdated = false;
                            simulationMayBeDrawn.signal();
                        } finally {
                            simulationLock.unlock();
                        }
                    }
                    Thread.sleep(pause);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
