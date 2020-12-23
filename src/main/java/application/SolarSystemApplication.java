/*
 * Copyright (c) 2019 Nico Kuijpers
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
package application;

import ephemeris.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import particlesystem.Particle;
import solarsystem.SolarSystem;
import solarsystem.SolarSystemBody;
import util.Vector3D;
import util.VectorUtil;

import java.io.*;
import java.util.*;

/**
 * Solar System Application.
 * @author Nico Kuijpers
 */
public class SolarSystemApplication extends Application {

    // Screen size
    private static final int BORDERSIZE = 10;
    private static final int SCREENWIDTH = 900;
    private static final int SCREENHEIGHT = 900;
    private static final double SELECTORWIDTH = 310.0;
    private static final double BUTTONWIDTH = 70.0;
    private static final double SCREENSCALE = 180.0 * SolarSystemParameters.ASTRONOMICALUNIT;

    // Screen to display the bodies of the Solar System
    private Canvas screen;

    // Monitor for thread synchronization
    private Monitor monitor = null;

    // Animation timer for drawing
    private AnimationTimer animationTimer = null;

    // Pausable task for simulation
    private SimPausableTask taskSimulate = null;

    // Flag to indicate whether simulation is running
    private boolean simulationIsRunning = false;

    // Flag to indicate whether simulation is running in step mode
    private boolean simulationIsRunningStepMode = false;

    // Flag to indicate whether simulation is running fast
    private boolean simulationIsRunningFast = false;

    // Flag to indicate whether simulation is running forward
    private boolean simulationIsRunningForward = true;

    // Date/time selector to view and set simulation era, date, and time
    private DateTimeSelector dateTimeSelector;

    // Event selector
    private ComboBox eventSelector;

    // Radio buttons to set simulation method
    private RadioButton radioNewtonMechanics;
    private RadioButton radioGeneralRelativity;

    // Radio buttons to set visualization of ephemeris/simulation results
    private RadioButton radioEphemerisOnly;
    private RadioButton radioSimulationOnly;
    private RadioButton radioEphemerisAndSimulation;

    // Slider to set top-front view
    private Slider sliderTopFrontView;

    // Slider to set zoom of view
    private Slider sliderZoomView;

    // Slider to set speed of simulation
    private Slider sliderSimulationSpeed;

    // Check box to select observation from Earth
    private CheckBox checkBoxObservationFromEarth;

    // Check box to indicate whether ruler should be shown
    private CheckBox checkBoxShowRuler;

    // Check box to select step mode
    private CheckBox checkBoxStepMode;

    // Check boxes for bodies to be shown
    private Map<String,CheckBox> checkBoxesBodies;

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
    private Set<String> bodiesShown;

    // Selected body
    private String selectedBody;

    // Names of moons per planet
    private Map<String,List<String>> moons;

    // Names of spacecraft
    private List<String> spacecraftNames;

    // Start dates for trajectories of spacecraft
    private Map<String, Calendar> trajectoryStartDate;

    // Information panels
    private Map<String, InformationPanel> informationPanels;

    // Flag to indicate whether ephemeris is shown on screen
    private boolean showEphemeris = true;

    // Flag to indicate whether simulation results are shown on screen
    private boolean showSimulation = true;

    // Flag to indicate whether observation from Earth is selected
    private boolean observationFromEarth = false;

    // Flag to indicate whether ruler is shown on screen
    private boolean showRuler = false;

    // Flag to indicate whether step mode for simulation is selected
    private boolean stepMode = false;

    // Time step in seconds for single-step simulation
    private double singleStepTimeStep = 60.0;

    // Time step in seconds for simulation step mode
    private double stepModeTimeStep = 60.0;

    // Time of last update of time step for step mode
    private long lastUpdateStepModeTimeStep = System.nanoTime();

    // Flag to indicate whether moons of planet are shown
    private Map<String,Boolean> showMoons;

    // Reference to the primary stage
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {

        // Set reference to the primary stage
        this.primaryStage = primaryStage;

        // Create the scene
        Scene scene = createScene();

        // Information panels
        informationPanels = new HashMap<>();

        // Close information panels when primary stage closes
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                for (InformationPanel panel : informationPanels.values()) {
                    panel.close();
                }
            }
        });

        // Define title and assign the scene for main window
        primaryStage.setTitle("Solar System Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Monitor for thread synchronization
        monitor = new Monitor();

        // Pausable task for simulation of the Solar System
        taskSimulate = new SimPausableTask();

        // Start animation timer to draw the Solar System each 20 ms
        animationTimer = new DrawAnimationTimer();
        animationTimer.start();

        // Initial settings for visualization
        setVisualizationSettings((VisualizationSettings) eventSelector.getValue());

        // Initialize simulation
        initializeSimulation();
    }

    /**
     * Create the scene, i.e., canvas, buttons, sliders, etc.
     * @return scene
     */
    public Scene createScene() {

        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(BORDERSIZE, BORDERSIZE, BORDERSIZE, BORDERSIZE));

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, SCREENWIDTH + 2 * BORDERSIZE + 330, SCREENHEIGHT + 2 * BORDERSIZE);
        root.getChildren().add(grid);

        // For debug purposes
        // Make the grid lines visible
        // grid.setGridLinesVisible(true);

        // Screen to draw trajectories
        screen = new Canvas(SCREENWIDTH, SCREENHEIGHT);
        grid.add(screen, 0, 0, 1, 31);
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


        // Create the Solar System
        solarSystem = new SolarSystem();

        // The stars STARS
        //stars = EphemerisStars.getInstance().getAllStars();

        // Start dates for trajectories of spacecraft
        trajectoryStartDate = new HashMap<>();
        trajectoryStartDate.put("Voyager 1", new GregorianCalendar(1977,8,5,12,56));
        trajectoryStartDate.put("Voyager 2", new GregorianCalendar(1977,7,20,14,29));
        trajectoryStartDate.put("New Horizons", new GregorianCalendar(2006,0,19,19,0));
        trajectoryStartDate.put("Rosetta", new GregorianCalendar(2004, 2, 2, 7, 17));
        trajectoryStartDate.put("Apollo 8", new GregorianCalendar(1968, 11, 21, 12, 51));
        trajectoryStartDate.put("ISS", new GregorianCalendar(1998, 10, 21));

        // Define spacecraft names
        spacecraftNames = new ArrayList<>();
        spacecraftNames.addAll(trajectoryStartDate.keySet());

        // Row index for vertical placement of labels, buttons, etc
        int rowIndex = 1;

        // Date/time selector to view and set era, date, and time
        dateTimeSelector = new DateTimeSelector(solarSystem.getSimulationDateTime());
        dateTimeSelector.setFont(new Font("Courier", 16));
        dateTimeSelector.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (!simulationIsRunning()) {
                    initializeSimulation();
                }
            }
        });
        dateTimeSelector.setMinWidth(SELECTORWIDTH);
        grid.add(dateTimeSelector, 1, rowIndex, 28, 1);

        // Event selector
        rowIndex++;
        eventSelector = new ComboBox();
        eventSelector.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setVisualizationSettings((VisualizationSettings) eventSelector.getValue());
            }
        });
        ObservableList<VisualizationSettings> events =
                FXCollections.observableArrayList(new ArrayList<>());
        events.addAll(createVisualizationSettings());
        eventSelector.setItems(events);
        eventSelector.setValue(events.get(0));
        eventSelector.setMinWidth(SELECTORWIDTH);
        grid.add(eventSelector, 1, rowIndex, 28, 1);

        // Button to initialize simulation
        rowIndex++;
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
        buttonInitialize.setMinWidth(BUTTONWIDTH);
        grid.add(buttonInitialize, 1, rowIndex, 9, 1);

        // Button to load simulation state from file
        Button buttonLoadState = new Button("Load");
        Tooltip tooltipLoadState =
                new Tooltip("Load simulation state from file");
        buttonLoadState.setTooltip(tooltipLoadState);
        buttonLoadState.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (observationFromEarth) {
                    checkBoxObservationFromEarth.setSelected(false);
                }
                loadSimulationState();
            }
        });
        buttonLoadState.setMinWidth(BUTTONWIDTH);
        grid.add(buttonLoadState, 8, rowIndex, 10, 1);

        // Button to save current simulation state to file
        Button buttonSaveState = new Button("Save");
        Tooltip tooltipSaveState =
                new Tooltip("Save simulation state to file");
        buttonSaveState.setTooltip(tooltipSaveState);
        buttonSaveState.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (observationFromEarth) {
                    checkBoxObservationFromEarth.setSelected(false);
                }
                saveSimulationState();
            }
        });
        buttonSaveState.setMinWidth(BUTTONWIDTH);
        grid.add(buttonSaveState, 15, rowIndex, 10, 1);

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
        buttonPause.setMinWidth(BUTTONWIDTH);
        grid.add(buttonPause, 22, rowIndex, 10, 1);

        // Button to start fast backward simulation
        rowIndex++;
        Button buttonFastBackward = new Button("<<");
        Tooltip tooltipFastBackward =
                new Tooltip("Normal mode: fast backward, step mode: backward with selected speed");
        buttonFastBackward.setTooltip(tooltipFastBackward);
        buttonFastBackward.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (stepMode) {
                    startSimulationStepModeBackward();
                } else {
                    startSimulationFastBackward();
                }
            }
        });
        buttonFastBackward.setMinWidth(BUTTONWIDTH);
        grid.add(buttonFastBackward, 1, rowIndex, 7, 1);

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
                } else {
                    startSimulationBackward();
                }
            }
        });
        buttonBackward.setMinWidth(BUTTONWIDTH);
        grid.add(buttonBackward, 8, rowIndex, 7, 1);

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
                } else {
                    startSimulationForward();
                }
            }
        });
        buttonForward.setMinWidth(BUTTONWIDTH);
        grid.add(buttonForward, 15, rowIndex, 7, 1);

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
                } else {
                    startSimulationFastForward();
                }
            }
        });
        buttonFastForward.setMinWidth(BUTTONWIDTH);
        grid.add(buttonFastForward, 22, rowIndex, 7, 1);

        // Radio buttons to set simulation method
        // 1. Newton Mechanics
        // 2. General Relativity
        radioNewtonMechanics =
                new RadioButton("Newton Mechanics");
        Tooltip tooltipNewtonMechanics =
                new Tooltip("Simulation based on Newton Mechanics is faster");
        radioNewtonMechanics.setTooltip(tooltipNewtonMechanics);
        radioNewtonMechanics.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                solarSystem.setGeneralRelativityFlag(!newValue);
            }
        });
        radioGeneralRelativity =
                new RadioButton("General Relativity");
        Tooltip tooltipGeneralRelativity =
                new Tooltip("Simulation based on General Relativity is even more accurate");
        radioGeneralRelativity.setTooltip(tooltipGeneralRelativity);
        radioGeneralRelativity.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                solarSystem.setGeneralRelativityFlag(newValue);
            }
        });
        ToggleGroup simulationMethod = new ToggleGroup();
        radioNewtonMechanics.setToggleGroup(simulationMethod);
        radioGeneralRelativity.setToggleGroup(simulationMethod);
        radioNewtonMechanics.setSelected(true);
        rowIndex++;
        grid.add(radioNewtonMechanics, 1, rowIndex, 20, 1);
        grid.add(radioGeneralRelativity, 15, rowIndex, 20, 1);

        // Radio buttons to set visualization of ephemeris/simulation results
        // 1. Show ephemeris only
        // 2. Show simulation only
        // 3. Show ephemeris and simulation
        rowIndex++;
        Label labelVisualization = new Label("Visualization:");
        grid.add(labelVisualization, 1, rowIndex, 20, 1);
        radioEphemerisOnly =
                new RadioButton("Ephemeris only");
        Tooltip tooltipEphemerisOnly =
                new Tooltip("Show ephemeris only; simulation results are not shown");
        radioEphemerisOnly.setTooltip(tooltipEphemerisOnly);
        radioEphemerisOnly.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    showEphemeris = true;
                    showSimulation = false;
                }
            }
        });
        radioSimulationOnly =
                new RadioButton("Simulation only");
        Tooltip tooltipSimulationOnly =
                new Tooltip("Show simulation only; ephemeris is not shown");
        radioSimulationOnly.setTooltip(tooltipSimulationOnly);
        radioSimulationOnly.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    showEphemeris = false;
                    showSimulation = true;
                }
            }
        });
        radioEphemerisAndSimulation =
                new RadioButton("Ephemeris and simulation");
        Tooltip tooltipEphemerisAndSimulation =
                new Tooltip("Show ephemeris (green) and simulation results (blue)");
        radioEphemerisAndSimulation.setTooltip(tooltipEphemerisAndSimulation);
        radioEphemerisAndSimulation.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    showEphemeris = true;
                    showSimulation = true;
                }
            }
        });
        ToggleGroup visualizationMethod = new ToggleGroup();
        radioEphemerisOnly.setToggleGroup(visualizationMethod);
        radioSimulationOnly.setToggleGroup(visualizationMethod);
        radioEphemerisAndSimulation.setToggleGroup(visualizationMethod);
        radioEphemerisAndSimulation.setSelected(true);
        rowIndex++;
        grid.add(radioEphemerisOnly, 1, rowIndex, 20, 1);
        rowIndex++;
        grid.add(radioSimulationOnly, 1, rowIndex, 20, 1);
        rowIndex++;
        grid.add(radioEphemerisAndSimulation, 1, rowIndex, 20, 1);

        // Check box to select observation from Earth
        rowIndex++;
        checkBoxObservationFromEarth = new CheckBox("Set observation from Earth");
        checkBoxObservationFromEarth.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                handleObservationFromEarth();
            }
        });
        Tooltip toolTipObservationFromEarth =
                new Tooltip("Check to set observation from surface of the Earth toward the selected body");
        checkBoxObservationFromEarth.setTooltip(toolTipObservationFromEarth);
        checkBoxObservationFromEarth.setSelected(observationFromEarth);
        grid.add(checkBoxObservationFromEarth, 1, rowIndex, 20, 1);

        // Check box to indicate whether ruler should be shown
        rowIndex++;
        checkBoxShowRuler = new CheckBox("Show ruler");
        checkBoxShowRuler.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                showRuler = newValue;
            }
        });
        Tooltip toolTipShowRuler =
                new Tooltip("Check to show ruler indicating distance or angular diameter");
        checkBoxShowRuler.setTooltip(toolTipShowRuler);
        checkBoxShowRuler.setSelected(showRuler);
        grid.add(checkBoxShowRuler, 1, rowIndex, 20, 1);

        // Check box to select step mode
        rowIndex++;
        checkBoxStepMode = new CheckBox("Single-step mode");
        checkBoxStepMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                stepMode = newValue;
                if (stepMode) {
                    pauseSimulation();
                }
            }
        });
        Tooltip toolTipStepMode =
                new Tooltip("Check to simulate in single-step mode and advance 60 s at a time");
        checkBoxStepMode.setTooltip(toolTipStepMode);
        checkBoxStepMode.setSelected(stepMode);
        grid.add(checkBoxStepMode, 1, rowIndex, 20, 1);

        // Slider to set top-bottom view
        rowIndex++;
        Label labelView = new Label("View");
        grid.add(labelView, 1, rowIndex, 9, 1);
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
        rowIndex++;
        grid.add(sliderTopFrontView, 1, rowIndex, 28, 1);

        // Slider to set zoom of view
        rowIndex++;
        Label labelZoom = new Label("Zoom");
        grid.add(labelZoom, 1, rowIndex, 9, 1);
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
        rowIndex++;
        grid.add(sliderZoomView, 1, rowIndex, 28, 1);

        // Slider to set simulation speed
        rowIndex++;
        Label labelSpeed = new Label("Speed");
        grid.add(labelSpeed, 1, rowIndex, 9, 1);
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
        rowIndex++;
        grid.add(sliderSimulationSpeed, 1, rowIndex, 28, 1);

        // Define circles for each body of the Solar System
        // Functions as storage for position, radius, and color of circles
        // representing the bodies of the Solar System.
        bodies = new HashMap<>();
        bodiesShown = new HashSet<>();
        createCircle("Sun", 10, Color.YELLOW);
        createCircle("Mercury", 3, Color.ORANGE);
        createCircle("Venus", 5, Color.BROWN);
        createCircle("Moon", 3, Color.GRAY);
        createCircle("Earth", 5, Color.AQUAMARINE);
        createCircle("Mars", 4, Color.RED);
        createCircle("Jupiter", 10, Color.ROSYBROWN);
        createCircle("Saturn", 9, Color.ORANGE);
        createCircle("Uranus", 7, Color.LIGHTBLUE);
        createCircle("Neptune", 7, Color.CADETBLUE);
        createCircle("Pluto", 3, Color.LIGHTBLUE);
        createCircle("Eris", 3, Color.LIGHTSALMON);
        createCircle("Chiron", 4, Color.CRIMSON);
        createCircle("Ceres", 3, Color.ORANGE);
        createCircle("Pallas", 3, Color.LIGHTGREEN);
        createCircle("Juno", 3, Color.ROSYBROWN);
        createCircle("Vesta", 3, Color.YELLOW);
        createCircle("Eros", 3, Color.LIGHTBLUE);
        createCircle("Bennu", 3, Color.LIGHTGRAY);
        createCircle("Halley", 7, Color.YELLOW);
        createCircle("Encke", 6, Color.LIGHTGREEN);
        createCircle("67P/Churyumov-Gerasimenko", 5, Color.ORANGE);
        createCircle("Hale-Bopp", 5, Color.LIGHTBLUE);
        createCircle("Shoemaker-Levy 9", 5, Color.PINK);
        createCircle("Florence", 3, Color.LIGHTGREEN);
        createCircle("Ultima Thule", 3, Color.RED);
        createCircle("Io", 3, Color.YELLOW);
        createCircle("Europa", 3, Color.LIGHTBLUE);
        createCircle("Ganymede", 3, Color.LIGHTGRAY);
        createCircle("Callisto", 3, Color.ORANGE);
        createCircle("Mimas", 3, Color.LIGHTGRAY);
        createCircle("Enceladus", 3, Color.ALICEBLUE);
        createCircle("Tethys", 3, Color.DARKGOLDENROD);
        createCircle("Dione", 3, Color.BISQUE);
        createCircle("Rhea", 3, Color.ORANGE);
        createCircle("Titan", 3, Color.PEACHPUFF);
        createCircle("Hyperion", 3, Color.LIGHTCORAL);
        createCircle("Iapetus", 3, Color.ALICEBLUE);
        createCircle("Miranda", 3, Color.LIGHTGRAY);
        createCircle("Ariel", 3, Color.ALICEBLUE);
        createCircle("Umbriel", 3, Color.PEACHPUFF);
        createCircle("Titania", 3, Color.LIGHTSALMON);
        createCircle("Oberon", 3, Color.BISQUE);
        createCircle("Triton", 3, Color.LIGHTGRAY);
        createCircle("EarthMoonBarycenter", 2, Color.WHITE);
        for (String spacecraftName : spacecraftNames) {
            createCircle(spacecraftName, 3, Color.LIGHTYELLOW);
        }

        // Initialize flags to indicate whether moons are shown
        showMoons = new HashMap<>();
        showMoons.put("Jupiter", false);
        showMoons.put("Saturn", false);
        showMoons.put("Uranus", false);
        showMoons.put("Neptune", false);

        // Names of moons per planet
        moons = new HashMap<>();

        // Names of moons of Jupiter
        List<String> jupiterMoons = new ArrayList<>();
        jupiterMoons.add("Io");
        jupiterMoons.add("Europa");
        jupiterMoons.add("Ganymede");
        jupiterMoons.add("Callisto");
        moons.put("Jupiter", jupiterMoons);

        // Names of moons of Saturn
        List<String> saturnMoons = new ArrayList<>();
        saturnMoons.add("Mimas");
        saturnMoons.add("Enceladus");
        saturnMoons.add("Tethys");
        saturnMoons.add("Dione");
        saturnMoons.add("Rhea");
        saturnMoons.add("Titan");
        saturnMoons.add("Iapetus");
        moons.put("Saturn", saturnMoons);

        // Names of moons of Uranus
        List<String> uranusMoons = new ArrayList<>();
        uranusMoons.add("Miranda");
        uranusMoons.add("Ariel");
        uranusMoons.add("Umbriel");
        uranusMoons.add("Titania");
        uranusMoons.add("Oberon");
        moons.put("Uranus", uranusMoons);

        // Names of moons of Neptune
        List<String> neptuneMoons = new ArrayList<>();
        neptuneMoons.add("Triton");
        moons.put("Neptune", neptuneMoons);

        // Define check box for each body of the solar system
        checkBoxesBodies = new HashMap<>();
        rowIndex++;
        int hor = 1;
        int ver = rowIndex;
        int horsize = 10;
        int versize = 1;
        grid.add(createCheckBox("Sun", "Sun",
                "The Sun is in fact a star and is the largest object in our "
                        + "Solar System."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Mercury", "Mercury",
                "Mercury is the smallest and innermost planet. "
                        + "It orbits around the Sun in 88 days."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Venus", "Venus",
                "Venus is the second planet from the Sun and is of similar size "
                        + " as the Earth."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Earth", "Earth",
                "Earth is the third planet from the Sun and the only "
                        + "planet known to harbor life."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Moon", "Moon",
                "Zoom in to see the moon orbiting around the Earth."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Mars", "Mars",
                "Mars is the second-smallest planet and is also known as the Red Planet."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Jupiter", "Jupiter",
                "Jupiter is the largest planet in the Solar System. "
                        + "Galileo Galilei discovered the four largest moons in 1610."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Saturn", "Saturn",
                "Saturn is the second-largest planet and is famous for his rings."),
                // "Visited by Pioneer 11, Voyager 1 and 2, and Cassini-Huygens.",
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Uranus", "Uranus",
                "Uranus was discovered in 1781 by William Hershel. "
                        + "Visited by Voyager 2 in 1986."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Neptune", "Neptune",
                "Neptune was discovered in 1846. "
                        + "Visited by Voyager 2 on 25 August 1989."),
                hor, ver++, horsize, versize);
        hor = hor + 9;
        ver = rowIndex;
        grid.add(createCheckBox("Pluto", "Pluto",
                "Pluto was discovered in 1930 and was considered the "
                        + "ninth planet until 2006. Visited by New Horizons on 14 July 2015."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Eris", "Eris",
                "Eris is the most massive and second-largest dwarf planet known "
                        + "in the Solar System."), hor, ver++, horsize, versize);
        grid.add(createCheckBox("Chiron", "Chiron",
                "Chiron was discovered in 1977 and orbits between Saturn and Uranus. "
                        + "It is the first object of the Centaur class"), hor, ver++, horsize, versize);
        grid.add(createCheckBox("Ceres", "Ceres",
                "Ceres is a dwarf planet and the largest object in the asteroid belt."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Pallas", "2 Pallas",
                "Pallas was the second asteroid discovered after Ceres and "
                        + "the third-most-massive asteroid after Vesta"),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Juno", "3 Juno",
                "Juno was the third asteroid discovered and is the 11th largest asteroid"),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Vesta", "4 Vesta",
                "Vesta is the second-largest body in the asteroid belt after Ceres"),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Eros", "433 Eros",
                "Eros is a near-Earth astroid. NASA spacecraft NEAR Shoemaker "
                        + "entered orbit around Eros in 2000, and landed in 2001."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Bennu", "Bennu",
                "Astroid 101955 Bennu was discovered on 11 September 1999. "
                        + "The OSIRIS-REx spacecraft arrived at Bennu on 3 December 2018"),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Florence", "Florence",
                "Asteroid 3122 Florence approached Earth within 0.047 au on "
                        + "1 September 2017."),
                hor, ver++, horsize, versize);
        hor = hor + 9;
        ver = rowIndex;
        grid.add(createCheckBox("Ultima Thule", "Ultima Thule",
                "Kuiper belt object Ultima Thule was visitied by New Horizons on "
                        + "1 January 2019."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Halley", "1P/Halley",
                "Halley's Comet has a period of 76 years. Last perihelion 9 Feb 1986. "
                        + "Next perihelion 28 July 2061."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("Encke", "2P/Encke",
                "P2/Encke was the first periodic comet discovered after "
                        + "Halley's Comet."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("67P/Churyumov-Gerasimenko", "67P/Ch-Ge",
                "67P/Churyumov-Gerasimenko was visited "
                        + "by ESA's Rosetta mission on 6 August 2014."),
                hor, ver++, horsize, versize);
        /*
        grid.add(createCheckBox("Shoemaker-Levy 9", "Shoe-Lev 9",
                "Shoemaker-Levy 9 collided with Jupiter in July 1994."),
                hor, ver++, horsize, versize);
        */
        grid.add(createCheckBox("Hale-Bopp", "Hale-Bopp",
                "Hale-Bopp passed perihelion on 1 April 1997 and "
                        + "was visible to the naked eye for 18 months."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("JupiterMoons", "Jupiter Sys",
                "The four largest moons of Jupiter are the Galilean moons " +
                        "Io, Europa, Ganymede, and Callisto."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("SaturnMoons", "Saturn Sys",
                "Saturn moons Mimas, Enceladus, Tethys, Dione, Rhea, Titan, and Iapetus."),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("UranusMoons", "Uranus Sys",
                "Uranus moons Miranda, Ariel, Umbriel, Titania, and Oberon"),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("NeptuneMoons", "Neptune Sys",
                "Neptune moon Triton"),
                hor, ver++, horsize, versize);
        grid.add(createCheckBox("EarthMoonBarycenter", "E-M Bary",
                "Earth-Moon barycenter is located on average 4671 km from Earth's center."),
                hor, ver++, horsize, versize);

        // Set font for all labeled objects
        for (Node n : grid.getChildren()) {
            if (n instanceof Labeled) {
                ((Labeled) n).setFont(new Font("Arial", 13));
            }
        }

        // The scene is created
        return scene;
    }

    /**
     * Update current simulation date/time in time selector.
     * When observing from the surface of the Earth, displayed date/time is
     * corrected speed of light depending on the distance between the Earth
     * and the selected Solar System object.
     */
    private void updateDateTimeSelector() {
        GregorianCalendar currentSimulationDateTime = solarSystem.getSimulationDateTime();
        if (observationFromEarth) {
            // Correct for light speed
            double distance = positionSelectedBody().euclideanDistance(positionEarth()); // m
            double correction = distance / SolarSystemParameters.SPEEDOFLIGHT; // s
            currentSimulationDateTime.add(Calendar.SECOND,(int) Math.round(correction));
        }
        dateTimeSelector.setDateTime(currentSimulationDateTime);
    }

    /**
     * Handle method to be invoked when check box for observation from Earth is clicked.
     */
    private void handleObservationFromEarth() {
        observationFromEarth = checkBoxObservationFromEarth.selectedProperty().getValue();
        if (observationFromEarth) {
            sliderTopFrontView.setValue(0.0);
            checkBoxStepMode.setSelected(true);
            stepMode = true;
            updateDateTimeSelector();
        }
        else {
            sliderTopFrontView.setValue(90.0);
            checkBoxStepMode.setSelected(false);
            stepMode = false;
            updateDateTimeSelector();
        }
    }

    /**
     * Initialize the state of the Solar System corresponding to the current date/time
     * in the time selector.
     */
    private synchronized void initializeSimulation() {
        // Simulation is not running
        simulationIsRunning = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;

        // Initialize simulation
        try {
            solarSystem.initializeSimulation(dateTimeSelector.getDateTime());
        }
        catch (SolarSystemException ex) {
            showMessage("Error",ex.getMessage());
        }
    }

    /**
     * Start simulation in fast backward mode.
     */
    private synchronized void startSimulationFastBackward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = true;
        simulationIsRunningForward = false;
        if (taskSimulate.isPaused()) {
            taskSimulate.resume();
        }
    }

    /**
     * Start simulation in backward mode.
     */
    private synchronized void startSimulationBackward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = false;
        if (taskSimulate.isPaused()) {
            taskSimulate.resume();
        }
    }

    /**
     * Start simulation in backward step mode.
     */
    private synchronized void startSimulationStepModeBackward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = true;
        simulationIsRunningFast = false;
        simulationIsRunningForward = false;
        if (taskSimulate.isPaused()) {
            taskSimulate.resume();
        }
    }

    /**
     * Advance one minute backward in time.
     */
    private synchronized void smallStepBackward() {
        // Pause simulation when running
        pauseSimulation();

        // Advance 1 minute backward
        solarSystem.advanceSimulationSingleStep(-singleStepTimeStep);

        // Move all bodies to positions corresponding to simulation date/time
        solarSystem.moveBodies();

        // Update simulation date/time shown in date/time selector
        updateDateTimeSelector();
    }

    /**
     * Advance one minute forward in time.
     */
    private synchronized void smallStepForward() {
        // Pause simulation when running
        pauseSimulation();

        // Advance 1 minute forward
        solarSystem.advanceSimulationSingleStep(singleStepTimeStep);

        // Move all bodies to positions corresponding to simulation date/time
        solarSystem.moveBodies();

        // Update simulation date/time shown in date/time selector
        updateDateTimeSelector();
    }

    /**
     * Start simulation in forward step mode.
     */
    private synchronized void startSimulationStepModeForward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = true;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;
        if (taskSimulate.isPaused()) {
            taskSimulate.resume();
        }
    }

    /**
     * Start simulation in forward mode.
     */
    private synchronized void startSimulationForward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;
        if (taskSimulate.isPaused()) {
            taskSimulate.resume();
        }
    }

    /**
     * Start simulatin in fast forward mode.
     */
    private synchronized void startSimulationFastForward() {
        simulationIsRunning = true;
        simulationIsRunningStepMode = false;
        simulationIsRunningFast = true;
        simulationIsRunningForward = true;
        if (taskSimulate.isPaused()) {
            taskSimulate.resume();
        }
    }

    /**
     * Pause simulation.
     */
    private synchronized void pauseSimulation() {
        simulationIsRunning = false;
        simulationIsRunningFast = false;
        simulationIsRunningForward = true;
        taskSimulate.pause();
        updateDateTimeAndPanels();
    }

    /**
     * Check if simulation is running.
     * @return true when simulation is running, false otherwise.
     */
    private synchronized boolean simulationIsRunning() {
        return simulationIsRunning;
    }

    /**
     * Load simulation state from file. Primary stage must be defined.
     */
    private synchronized void loadSimulationState() {
        if (simulationIsRunning()) {
            pauseSimulation();
        }
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                monitor.startSimulating();
                try {
                    FileInputStream fileIn = new FileInputStream(file);
                    try (ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
                        solarSystem = (SolarSystem) objectIn.readObject();
                    }
                }
                catch (IOException | ClassNotFoundException | ClassCastException ex) {
                    showMessage("Error","Cannot load simulation state from file " + file.getName());
                }
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            finally {
                monitor.stopSimulating();
            }
        }

        // Update simulation date/time shown in date/time selector
        updateDateTimeSelector();
    }

    /**
     * Save simulation state to file. Primary stage must be defined.
     */
    private synchronized void saveSimulationState() {
        if (simulationIsRunning()) {
            pauseSimulation();
        }
        try {
            monitor.startDrawing();
            String dateTimeString = dateTimeSelector.getText();
            dateTimeString = dateTimeString.substring(0, 19);
            dateTimeString = dateTimeString.replace(":", "-");
            dateTimeString = dateTimeString.replaceAll(" ", "_");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(dateTimeString + ".sol");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                FileOutputStream fileOut = null;
                try {
                    fileOut = new FileOutputStream(file);
                }
                catch (FileNotFoundException ex) {
                    showMessage("Error","File not found");
                }
                try (ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
                    objectOut.writeObject(solarSystem);
                }
                catch (IOException ex) {
                    showMessage("Error","Cannot save simulation state to file");
                }
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        finally {
            monitor.stopSimulating();
        }
    }

    /**
     * Create a circle to store position, radius, and color of circle
     * representing a body of the Solar System.
     * @param name    Name of Solar System body
     * @param radius  Radius of circle (pixels)
     * @param color   Color of circle
     * @return instance of Circle
     */
    private Circle createCircle(String name, int radius, Color color) {
        Circle circle = new Circle(0.0,0.0,radius,color);
        circle.setVisible(false);
        bodies.put(name, circle);
        return circle;
    }

    /**
     * Create a check box to indicate whether a certain body of the Solar System
     * should be drawn on the screen.
     * Left mouse button click: select / deselect Solar System body to be drawn
     * Right mouse button click: show information panel of the Solar System body
     * @param name         Name of Solar System body
     * @param label        Text to be placed on the label of the check box
     * @param toolTipText  Text to be placed in the tool tip of the check box
     * @return instance of CheckBox
     */
    private CheckBox createCheckBox(String name, String label, String toolTipText) {
        CheckBox checkBox = new CheckBox(label);
        checkBoxesBodies.put(name,checkBox);
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                boolean isSelected = !oldValue && newValue;
                if (name.endsWith("Moons")) {
                    String planetName = name.substring(0, name.length() - 5);
                    if (isSelected) {
                        try {
                            solarSystem.createPlanetSystem(planetName);
                        } catch (SolarSystemException ex) {
                            checkBox.setSelected(false);
                            showMessage("Error", ex.getMessage());
                        }
                    } else {
                        solarSystem.removePlanetSystem(planetName);
                        for (String moonName : moons.get(planetName)) {
                            if (informationPanels.containsKey(moonName)) {
                                informationPanels.get(moonName).close();
                                informationPanels.remove(moonName);
                            }
                        }
                    }
                    showMoons.put(planetName, isSelected);
                    updateBodiesShown();
                }
                else {
                    if (isSelected) {
                        bodiesShown.add(name);
                    }
                    else {
                        bodiesShown.remove(name);
                        if (informationPanels.containsKey(name)) {
                            informationPanels.get(name).close();
                            informationPanels.remove(name);
                        }
                    }
                }
            }
        });
        checkBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    if (!name.endsWith("Moons")) {
                        boolean isSelected = checkBox.selectedProperty().getValue();
                        if (!isSelected) {
                            checkBox.setSelected(true);
                            bodiesShown.add(name);
                        }
                        showInformationPanel(name);
                    }
                }
            }
        });
        Tooltip toolTip = new Tooltip(toolTipText);
        checkBox.setTooltip(toolTip);
        return checkBox;
    }

    /**
     * Compute x-position on the screen for 3D position in the Solar System.
     * @param position 3D position in m
     * @return x-position in pixels
     */
    private double screenX(Vector3D position) {
        return translateX + SCREENWIDTH * (position.getX() / SCREENSCALE);
    }

    /**
     * Compute y-position on the screen for 3D position in the Solar System.
     * @param position 3D position in m
     * @return y-postion in pixels
     */
    private double screenY(Vector3D position) {
        return translateY + SCREENHEIGHT * (1.0 - position.getY() / SCREENSCALE);
    }

    /**
     * Determine the position of the selected body.
     * Return zero vector when no body is selected.
     * @return position of selected body
     */
    private Vector3D positionSelectedBody() {
        if (selectedBody != null) {
            if (showSimulation) {
                Particle particle = solarSystem.getParticle(selectedBody);
                if (particle != null) {
                    return particle.getPosition();
                }
            }
            else {
                SolarSystemBody body = solarSystem.getBody(selectedBody);
                if (body != null) {
                    return body.getPosition();
                }
            }
        }
        selectedBody = null;
        return new Vector3D();
    }

    /**
     * Determine the position of the Earth.
     * @return position of the Earth
     */
    private Vector3D positionEarth() {
        if (showSimulation) {
            return solarSystem.getParticle("Earth").getPosition();
        }
        else {
            return solarSystem.getBody("Earth").getPosition();
        }
    }

    /**
     * Translate and rotate position for observation from the surface of the Earth.
     * Perspective is taken into account.
     * @param position 3D position in m
     * @return translated and rotated position
     */
    private Vector3D observationFromEarthView(Vector3D position) {

        // Use slider top-front view to determine the latitude of the viewing position on
        // the surface of the Earth. The distance in z-direction from the center of the
        // Earth is equal to sin(latitude) * diameter/2.
        // The latter correction is necessary to get a total solar eclipse on March 20, 2015
        // with the slider set to approximately +70 degrees. This solar eclipse was only visible
        // in the far north.
        // https://en.wikipedia.org/wiki/Solar_eclipse_of_March_20,_2015
        double latitudeDeg = sliderTopFrontView.getValue();
        double latitudeRad = Math.toRadians(latitudeDeg);
        double radiusEarth = solarSystem.getBody("Earth").getDiameter()/2.0;
        double distanceFromCenterZ = Math.sin(latitudeRad) * radiusEarth;

        // Viewing position is translated along Z-axis with respect to center of the Earth
        Vector3D viewingPosition = positionEarth().plus(new Vector3D(0.0,0.0,distanceFromCenterZ));

        // Viewing direction of camera
        Vector3D viewingDirection = positionSelectedBody().minus(viewingPosition);

        // Upward viewing orientation of camera is is along earth's axis
        // double axialTiltRad = Math.toRadians(SolarSystemParameters.AXIALTILT);
        // double uvoX = 0.0;
        // double uvoY = Math.tan(axialTiltRad);
        // double uvoZ = 1.0;
        // Upward viewing orientation of camera is towards celestial north pole
        double uvoX = 0.0;
        double uvoY = 0.0;
        double uvoZ = 1.0;
        Vector3D upwardViewingOrientation = (new Vector3D(uvoX,uvoY,uvoZ)).normalize();

        // Camera coordinates
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D zc = (viewingDirection.scalarProduct(-1.0)).normalize();
        Vector3D xc = (upwardViewingOrientation.crossProduct(zc)).normalize();
        Vector3D yc = zc.crossProduct(xc);

        // Translate
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D positionTranslated = position.minus(viewingPosition);

        // Rotate
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D positionRotated = positionTranslated.rotate(xc,yc,zc);

        // Take perspective into account
        // Assume that we are standing on the surface of the Earth
        double distance = position.euclideanDistance(positionEarth());
        distance = distance - radiusEarth;
        double factor = SolarSystemParameters.ASTRONOMICALUNIT/distance;
        return positionRotated.scalarProduct(factor);
    }

    /**
     * Translate and rotate position for rendering.
     * @param position 3D position in m
     * @return translated and rotated position
     */
    private Vector3D convertToScreenView(Vector3D position) {

        // Use slider zoom view to zoom in on the scene
        //double zoom = Math.exp(0.1*sliderZoomView.getValue());
        double zoom = Math.exp(0.12*sliderZoomView.getValue());

        if (observationFromEarth) {
            return position.scalarProduct(10.0*zoom);
        }
        else {
            // Translate position such that selected body will be shown in the center
            Vector3D positionAfterTranslation = position.minus(positionSelectedBody());

            // Use slider top-front view to select between viewing the solar system
            // from the north ecliptic pole (90 degrees), the ecliptic (0 degrees) or
            // the south ecliptic pole (-90 degrees)
            // https://en.wikipedia.org/wiki/Ecliptic
            double rotationAngleX = sliderTopFrontView.getValue() - 90.0;
            Vector3D positionAfterRotationX = positionAfterTranslation.rotateXdeg(rotationAngleX);

            return positionAfterRotationX.scalarProduct(zoom);
        }
    }

    /**
     * Draw rings of Saturn or Uranus. When boolean parameter front is set, ring elements in front of
     * the planet will be drawn, otherwise the ring elements behind the planet will be drawn.
     * @param positionPlanet position of Saturn or Uranus
     * @param front          indicates whether circle elements in front of planet or behind planet will be drawn.
     */
    private void drawRings(String planetName, Vector3D positionPlanet, boolean front) {

        Vector3D[] innerRingPositions;
        Vector3D[] outerRingPositions;
        if ("Saturn".equals(planetName)) {
            innerRingPositions = EphemerisRingsOfSaturn.innerRingPositions(solarSystem.getSimulationDateTime());
            outerRingPositions = EphemerisRingsOfSaturn.outerRingPositions(solarSystem.getSimulationDateTime());
        }
        else {
            innerRingPositions = EphemerisRingsOfUranus.innerRingPositions(solarSystem.getSimulationDateTime());
            outerRingPositions = EphemerisRingsOfUranus.outerRingPositions(solarSystem.getSimulationDateTime());
        }
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.setFill(Color.BEIGE);
        for (int i = 0; i < innerRingPositions.length; i++) {
            innerRingPositions[i].addVector(positionPlanet);
            outerRingPositions[i].addVector(positionPlanet);
        }
        int step = 1;
        if (observationFromEarth) {
            double distanceToBody = positionEarth().euclideanDistance(positionPlanet);
            for (int i = 0; i < innerRingPositions.length; i+=step) {
                int nPoints = 4;
                double[] xPoints = new double[nPoints];
                double[] yPoints = new double[nPoints];
                if (front == positionEarth().euclideanDistance(outerRingPositions[i]) < distanceToBody) {
                    xPoints[0] = screenX(convertToScreenView(observationFromEarthView(innerRingPositions[i])));
                    yPoints[0] = screenY(convertToScreenView(observationFromEarthView(innerRingPositions[i])));
                    xPoints[1] = screenX(convertToScreenView(observationFromEarthView(outerRingPositions[i])));
                    yPoints[1] = screenY(convertToScreenView(observationFromEarthView(outerRingPositions[i])));
                    int index = (i + step) % innerRingPositions.length;
                    xPoints[2] = screenX(convertToScreenView(observationFromEarthView(outerRingPositions[index])));
                    yPoints[2] = screenY(convertToScreenView(observationFromEarthView(outerRingPositions[index])));
                    xPoints[3] = screenX(convertToScreenView(observationFromEarthView(innerRingPositions[index])));
                    yPoints[3] = screenY(convertToScreenView(observationFromEarthView(innerRingPositions[index])));
                    gc.fillPolygon(xPoints, yPoints, nPoints);
                }
            }
        }
        else {
            for (int i = 0; i < innerRingPositions.length; i+=step) {
                int nPoints = 4;
                double[] xPoints = new double[nPoints];
                double[] yPoints = new double[nPoints];
                if (front == outerRingPositions[i].getY() < positionPlanet.getY()) {
                    xPoints[0] = screenX(convertToScreenView(innerRingPositions[i]));
                    yPoints[0] = screenY(convertToScreenView(innerRingPositions[i]));
                    xPoints[1] = screenX(convertToScreenView(outerRingPositions[i]));
                    yPoints[1] = screenY(convertToScreenView(outerRingPositions[i]));
                    int index = (i + step) % innerRingPositions.length;
                    xPoints[2] = screenX(convertToScreenView(outerRingPositions[index]));
                    yPoints[2] = screenY(convertToScreenView(outerRingPositions[index]));
                    xPoints[3] = screenX(convertToScreenView(innerRingPositions[index]));
                    yPoints[3] = screenY(convertToScreenView(innerRingPositions[index]));
                    gc.fillPolygon(xPoints, yPoints, nPoints);
                }
            }
        }
    }

    /**
     * Draw circle for body to be shown on screen.
     * @param circle   circle properties corresponding to body
     * @param body     the body for which circle is drawn
     * @param position position of the body to be shown
     */
    private void drawCircle(Circle circle, SolarSystemBody body, Vector3D position) {

        // Diameter as observed
        double diameter = body.getDiameter();
        if (observationFromEarth && !"Earth".equals(body.getName())) {
            // Scale diameter with distance from surface of the Earth
            // The Sun and the Moon will have the same apparent size
            // This is necessary for a correct representation of a total solar eclipse
            double distance = position.euclideanDistance(positionEarth());
            distance = distance - solarSystem.getBody("Earth").getDiameter()/2.0;
            diameter = (SolarSystemParameters.ASTRONOMICALUNIT * diameter) / distance;
        }

        // Determine position on screen
        Vector3D positionView = null;
        if (observationFromEarth) {
            positionView = convertToScreenView(observationFromEarthView(position));
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

        // Draw ring elements of Saturn or Uranus behind the planet
        if (("Saturn".equals(body.getName()) || "Uranus".equals(body.getName()))
                && radius > circle.getRadius()) {
            drawRings(body.getName(), position, false);
        }

        // Draw circle on screen using color and radius from Circle-object
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.setFill(circle.getFill());
        gc.fillOval(posx - radius, posy - radius, 2*radius, 2*radius);

        // Draw ring elements of Saturn or Uranus in front of the planet
        if (("Saturn".equals(body.getName()) || "Uranus".equals(body.getName()))
                && radius > circle.getRadius()) {
            drawRings(body.getName(), position, true);
        }

        // Draw shadow of Galilean Moon on the surface of Jupiter
        if (observationFromEarth && "Jupiter".equals(selectedBody) && body.getCenterBody() != null &&
                "Jupiter".equals(body.getCenterBody().getName())) {
            double radiusJupiter = 0.5*SolarSystemParameters.getInstance().getDiameter("Jupiter");
            Vector3D positionSun, positionJupiter;
            if (showSimulation) {
                positionSun = solarSystem.getParticle("Sun").getPosition();
                positionJupiter = solarSystem.getParticle("Jupiter").getPosition();
            }
            else {
                positionSun = solarSystem.getBody("Sun").getPosition();
                positionJupiter = solarSystem.getBody("Jupiter").getPosition();
            }
            Vector3D direction = positionSun.direction(position);
            Vector3D positionShadow = VectorUtil.computeIntersectionLineSphere(direction,positionSun,positionJupiter,radiusJupiter);
            if (positionShadow != null) {
                if (positionSun.euclideanDistance(position) < positionSun.euclideanDistance(positionShadow)) {
                    Vector3D positionShadowView = convertToScreenView(observationFromEarthView(positionShadow));
                    double shadowPosx = screenX(positionShadowView);
                    double shadowPosy = screenY(positionShadowView);
                    gc.setFill(Color.BLACK);
                    gc.fillOval(shadowPosx - radius, shadowPosy - radius, 2 * radius, 2 * radius);
                    gc.fillText(body.getName(),shadowPosx + 0.5*radius,shadowPosy - radius);
                }
            }
        }

        // Draw name on screen using color from Circle-object
        gc.setFill(circle.getFill());
        gc.fillText(body.getName(),posx + 0.5*radius,posy - radius);
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
            }
            else {
                // Draw circle at particle position
                Particle particle = solarSystem.getParticle(bodyName);
                if (particle != null) {
                    if (!spacecraftNames.contains(bodyName)) {
                        // Not a spacecraft
                        drawCircle(circle, body, particle.getPosition());
                    }
                    else {
                        if (solarSystem.getSimulationDateTime().after(trajectoryStartDate.get(bodyName))) {
                            // Spacecraft - do not draw before trajectory start date
                            drawCircle(circle, body, particle.getPosition());
                        }
                    }
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
    private void setColor(GraphicsContext gc, Vector3D position, Color frontColor, Color backColor) {
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
     * Draw orbit and position of body. Orbit segments in front of the Sun are drawn
     * using frontColor and orbit segments behind the Sun are drawn
     * using backColor. A small circle to indicate the position of the body
     * is drawn when drawSmallCircle is true
     * @param orbit            the orbit
     * @param position         position
     * @param frontColor       color for orbit segments in front of the Sun
     * @param backColor        color for orbit segments behind the Sun
     * @param drawSmallCircle  indicates whether small circle should be drawn
     */
    private void drawOrbit(Vector3D[] orbit, Vector3D position,
                           Color frontColor, Color backColor, boolean drawSmallCircle) {

        // Draw orbit
        GraphicsContext gc = screen.getGraphicsContext2D();
        Vector3D positionView;
        if (observationFromEarth) {
            positionView = convertToScreenView(observationFromEarthView(orbit[0]));
            setColor(gc,positionView,frontColor,backColor);
        }
        else {
            positionView = convertToScreenView(orbit[0]);
            setColor(gc,orbit[0],frontColor,backColor);
        }
        double x1 = screenX(positionView);
        double y1 = screenY(positionView);
        for (int i = 0; i < orbit.length; i++) {
            if (observationFromEarth) {
                positionView = convertToScreenView(observationFromEarthView(orbit[i]));
                setColor(gc,positionView,frontColor,backColor);
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

        // Draw circle to indicate position in orbit
        if (drawSmallCircle) {
            if (observationFromEarth) {
                positionView = convertToScreenView(observationFromEarthView(position));
                setColor(gc,positionView,frontColor,backColor);
            }
            else {
                positionView = convertToScreenView(position);
                setColor(gc,position,frontColor,backColor);
            }
            double x = screenX(positionView);
            double y = screenY(positionView);
            gc.fillOval(x - 3, y - 3, 6, 6);
        }
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
                Vector3D position = body.getPosition();
                // Draw orbit as a green line
                if (orbit != null) {
                    drawOrbit(orbit, position, Color.LIGHTGREEN, Color.GREEN, showSimulation);
                }
            }
        }
    }

    /**
     * Draw orbit corresponding to current position and velocity of particle.
     * Orbit is drawn as a dark cyan line.
     * @param centerBodyName name of the center body
     * @param particle the particle
     */
    private void drawOrbitCorrespondingToPositionVelocity(String centerBodyName, Particle particle) {
        // Position and velocity of center body
        Vector3D positionCenterBody = solarSystem.getParticle(centerBodyName).getPosition();
        Vector3D velocityCenterBody = solarSystem.getParticle(centerBodyName).getVelocity();

        // Position and velocity of particle
        Vector3D positionParticle = particle.getPosition();
        Vector3D velocityParticle = particle.getVelocity();

        // Compute orbit of particle relative to center body
        Vector3D positionRelativeToCenterBody = positionParticle.minus(positionCenterBody);
        Vector3D velocityRelativeToCenterBody = velocityParticle.minus(velocityCenterBody);
        double muCenterBody = solarSystem.getParticle(centerBodyName).getMu();
        Vector3D[] orbitRelativeToCenterBody = EphemerisUtil.computeOrbit(muCenterBody,
                positionRelativeToCenterBody,velocityRelativeToCenterBody);

        // Compute orbit
        Vector3D[] orbit = new Vector3D[orbitRelativeToCenterBody.length];
        for (int i = 0; i < orbitRelativeToCenterBody.length; i++) {
            orbit[i] = positionCenterBody.plus(orbitRelativeToCenterBody[i]);
        }

        // Draw the orbit of the particle as a cyan line
        if (orbit != null) {
            drawOrbit(orbit, positionParticle, Color.CYAN, Color.DARKCYAN,false);
        }
    }

    /**
     * Draw trajectory of spacecraft. Trajectory segments in front of the Sun
     * are drawn using frontColor and segments behind the Sun are drawn using
     * backColor.
     * @param centerBodyName   name of the center body
     * @param trajectory       trajectory to be drawn
     * @param frontColor       color for segments in front of the Sun
     * @param backColor        color for segments behind the Sun
     */
    private void drawTrajectorySpacecraft(String centerBodyName, List<Vector3D> trajectory, Color frontColor, Color backColor) {
        GraphicsContext gc = screen.getGraphicsContext2D();
        Vector3D positionView;
        Vector3D positionCenterBody = solarSystem.getBody(centerBodyName).getPosition();
        Vector3D position = positionCenterBody.plus(trajectory.get(0));
        if (observationFromEarth) {
            positionView = convertToScreenView(observationFromEarthView(position));
            setColor(gc,positionView,frontColor,backColor);
        }
        else {
            positionView = convertToScreenView(position);
            setColor(gc,trajectory.get(0),frontColor,backColor);
        }
        double x1 = screenX(positionView);
        double y1 = screenY(positionView);
        for (int i = 1; i < trajectory.size(); i++) {
            position = positionCenterBody.plus(trajectory.get(i));
            if (observationFromEarth) {
                positionView = convertToScreenView(observationFromEarthView(position));
                setColor(gc,positionView,frontColor,backColor);
            }
            else {
                positionView = convertToScreenView(position);
                setColor(gc,trajectory.get(i),frontColor,backColor);
            }
            double x2 = screenX(positionView);
            double y2 = screenY(positionView);
            gc.strokeLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
            // Draw dot to indicate beginning of each line segment
            // gc.fillOval(x1-2, y1-2, 4, 4);
        }
    }

    /**
     * Draw orbits or trajectories corresponding to current positions and velocities
     * of particles. Orbits are drawn as cyan lines. Spacecraft trajectories as red lines.
     * @param bodiesToShow bodies to show on screen
     */
    private void drawOrbitsCorrespondingToPositionVelocity(List<SolarSystemBody> bodiesToShow) {
        if (showSimulation) {
            for (SolarSystemBody body : bodiesToShow) {
                String bodyName = body.getName();
                Particle particle = solarSystem.getParticle(bodyName);
                if (particle != null) {
                    if (!spacecraftNames.contains(bodyName)) {
                        // Draw orbit
                        SolarSystemBody centerBody = body.getCenterBody();
                        if (centerBody != null) {
                            String centerBodyName = body.getCenterBody().getName();
                            drawOrbitCorrespondingToPositionVelocity(centerBodyName, particle);
                        } else {
                            drawOrbitCorrespondingToPositionVelocity("Sun", particle);
                        }
                    } else {
                        // Draw trajectory
                        if (solarSystem.getSimulationDateTime().after(trajectoryStartDate.get(bodyName))) {
                            body.updateTrajectory(particle.getPosition(), particle.getVelocity());
                            if (!body.getTrajectory().isEmpty()) {
                                SolarSystemBody centerBody = body.getCenterBody();
                                if (centerBody != null) {
                                    String centerBodyName = body.getCenterBody().getName();
                                    drawTrajectorySpacecraft(centerBodyName, body.getTrajectory(), Color.RED, Color.RED);
                                } else {
                                    drawTrajectorySpacecraft("Sun", body.getTrajectory(), Color.RED, Color.RED);
                                }
                            }
                        }
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
        double diameterSun = SolarSystemParameters.getInstance().getDiameter("Sun");
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
        }
        else if (scale >= 1.0 / 60) {
            textEnd = scale * 60 + " arcminutes";
        }
        else {
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
     * Draw ecliptic.
     * Ecliptic is represented by a red line.
     */
    private void drawEcliptic() {
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.setStroke(Color.RED);
        Vector3D eclipticA = (positionEarth().rotateZrad(-0.5*Math.PI)).scalarProduct(100.0);
        Vector3D eclipticB = (positionEarth().rotateZrad(0.5*Math.PI)).scalarProduct(100.0);
        Vector3D eclipticViewA = convertToScreenView(observationFromEarthView(eclipticA));
        Vector3D eclipticViewB = convertToScreenView(observationFromEarthView(eclipticB));
        double x1 = screenX(eclipticViewA);
        double y1 = screenY(eclipticViewA);
        double x2 = screenX(eclipticViewB);
        double y2 = screenY(eclipticViewB);
        gc.strokeLine(x1,y1,x2,y2);
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
     * Update set of bodies to be shown.
     */
    private void updateBodiesShown() {
        for (String planetName : showMoons.keySet()) {
            if (showMoons.get(planetName)) {
                bodiesShown.addAll(moons.get(planetName));
            }
            else {
                bodiesShown.removeAll(moons.get(planetName));
            }
        }
    }

    /**
     * Set selected body when the left mouse button is clicked.
     * @param event Mouse event
     */
    private void screenMouseClicked(MouseEvent event) {
        double minDistance = 20.0;
        for (String bodyName : bodiesShown) {
            Circle circle = bodies.get(bodyName);
            double distance = distanceCircle(event, circle);
            if (distance < minDistance) {
                selectedBody = bodyName;
                minDistance = distance;
                initTranslate();
            }
        }
        updateBodiesShown();
        updateDateTimeSelector();
        if (event.getButton() == MouseButton.SECONDARY) {
            showInformationPanel(selectedBody);
        }
    }

    /**
     * Handle method for mouse drag event.
     * @param event Mouse event
     */
    private void screenMouseDragged(MouseEvent event) {
        translateX = translateX + event.getX() - lastDragX;
        translateY = translateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    /**
     * Handle method for mouse pressed event.
     * @param event Mouse event
     */
    private void screenMousePressed(MouseEvent event) {
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    /**
     * Initialize translate parameters for dragging.
     */
    private void initTranslate() {
        translateX = 0.5 * SCREENWIDTH;
        translateY = -0.5 * SCREENHEIGHT;
        lastDragX = 0.0;
        lastDragY = 0.0;
    }

    /**
     * Clear screen and make background blue for observation from
     * the Earth, or black for normal view.
     */
    private void clearScreen() {
        GraphicsContext gc = screen.getGraphicsContext2D();
        gc.clearRect(0.0,0.0,SCREENWIDTH,SCREENHEIGHT);
        if (observationFromEarth) {
            gc.setFill(Color.BLUE);
        }
        else {
            gc.setFill(Color.BLACK);
        }
        gc.fillRect(0.0,0.0,SCREENWIDTH,SCREENHEIGHT);
    }

    @Override
    public void stop() {
        if (taskSimulate != null) {
            taskSimulate.stop();
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    /**
     * Main method. Not used for JavaFX application.
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Create sorted list of bodies such that they are drawn in an order that corresponds
     * to the currently selected view (observation from earth or normal view).
     * @return sorted list of solar system bodies
     */
    private List<SolarSystemBody> sortBodiesShown() {
        List<SolarSystemBody> bodies = new ArrayList<>();
        for (String bodyName : bodiesShown) {
            bodies.add(solarSystem.getBody(bodyName));
        }
        if (observationFromEarth) {
            // Sort bodies, such that bodies further away from the Earth are drawn first
            Collections.sort(bodies, new Comparator<SolarSystemBody>() {
                @Override
                public int compare(SolarSystemBody body1, SolarSystemBody body2) {
                    double distanceBody1 = positionEarth().euclideanDistance(body1.getPosition());
                    double distanceBody2 = positionEarth().euclideanDistance(body2.getPosition());
                    if (distanceBody1 < distanceBody2) {
                        return 1;
                    }
                    else {
                        return -1;
                    }
                }
            });
        }
        else {
            // Sort bodies, such that bodies behind the Sun are drawn first
            // Earth-Moon Barycenter is drawn latest
            Collections.sort(bodies, new Comparator<SolarSystemBody>() {
                @Override
                public int compare(SolarSystemBody body1, SolarSystemBody body2) {
                    if (body1.getPosition().getY() < body2.getPosition().getY() ||
                            "EarthMoonBarycenter".equals(body1.getName())) {
                        return 1;
                    }
                    else {
                        return -1;
                    }
                }
            });
        }
        return bodies;
    }

    /**
     * Update current simulation date/time and information panels
     */
    private void updateDateTimeAndPanels() {
        try {
            monitor.startDrawing();
            updateDateTimeSelector();
            for (InformationPanel panel : informationPanels.values()) {
                try {
                    panel.updatePanel();
                }
                catch (SolarSystemException ex) {
                    showMessage("Error",ex.getMessage());
                }
            }
        }
        catch (InterruptedException e) {
            stop();
            Thread.currentThread().interrupt();
        }
        finally {
            monitor.stopDrawing();
        }
    }

    /**
     * Draw simulation state of Solar System on screen.
     */
    private void drawSimulationState() {
        // Update current simulation date/time and information panels
        if (simulationIsRunning()) {
            updateDateTimeSelector();
            for (InformationPanel panel : informationPanels.values()) {
                try {
                    panel.updatePanel();
                }
                catch (SolarSystemException ex) {
                    showMessage("Error",ex.getMessage());
                }
            }
        }

        // Draw bodies of the solar system and their orbits
        clearScreen();
        List<SolarSystemBody> bodiesToShow = sortBodiesShown();
        if (observationFromEarth) {
            // Do not show the Earth and Earth-Moon Barycenter for observation from Earth
            SolarSystemBody earth = solarSystem.getBody("Earth");
            SolarSystemBody earthMoonBarycenter = solarSystem.getBody("EarthMoonBarycenter");
            bodiesToShow.remove(earth);
            bodiesToShow.remove(earthMoonBarycenter);
        }

        // Do not draw orbits/trajectories for observation from Earth
        if (!observationFromEarth) {
            drawOrbitsCorrespondingToPositionVelocity(bodiesToShow);
            drawOrbits(bodiesToShow);
        }

        // Draw orbits corresponding to selected orbital elements at information panels
        for (InformationPanel panel : informationPanels.values()) {
            Vector3D[] orbit = panel.getOrbit();
            Vector3D position = panel.getPosition();
            if (orbit != null) {
                drawOrbit(orbit, position, Color.LIGHTYELLOW, Color.YELLOW, true);
            }
        }

        // Draw circles indicating either the simulated or ephemeris positions of bodies
        drawCircles(bodiesToShow);

        // Draw rulers
        if (showRuler && !observationFromEarth) {
            drawRulerDistance();
        }
        if (showRuler && observationFromEarth) {
            drawRulerAngularDiameter();
            // drawEcliptic();
        }
    }

    /**
     * Update time step in seconds for simulation step mode.
     */
    private void updateStepModeTimeStep() {
        long now = System.nanoTime();
        long elapsedTimeNanoSeconds = now - lastUpdateStepModeTimeStep;
        double elapsedTimeSeconds = elapsedTimeNanoSeconds/1.0E9;
        elapsedTimeSeconds = Math.min(1.0,elapsedTimeSeconds);
        lastUpdateStepModeTimeStep = now;
        if (sliderSimulationSpeed.getValue() < 1.0) {
            // Real-time simulation
            stepModeTimeStep = elapsedTimeSeconds;
        }
        else {
            // Faster than real-time simulation
            stepModeTimeStep = 0.01 * Math.exp(0.08 * sliderSimulationSpeed.getValue());
            stepModeTimeStep = Math.max(elapsedTimeSeconds,stepModeTimeStep);
        }
    }

    /**
     * Advance simulation of Solar System.
     * Number of time steps depends on simulation mode.
     */
    private void advanceSimulation() {
        int nrTimeSteps;
        if (simulationIsRunningFast) {
            nrTimeSteps = 24;
        }
        else {
            nrTimeSteps = 1;
        }
        if (simulationIsRunningForward) {
            if (simulationIsRunningStepMode) {
                updateStepModeTimeStep();
                solarSystem.advanceSimulationSingleStep(stepModeTimeStep);
            }
            else {
                solarSystem.advanceSimulationForward(nrTimeSteps);
            }
        }
        else {
            if (simulationIsRunningStepMode) {
                updateStepModeTimeStep();
                solarSystem.advanceSimulationSingleStep(-stepModeTimeStep);
            }
            else {
                solarSystem.advanceSimulationBackward(nrTimeSteps);
            }
        }
        solarSystem.moveBodies();
    }

    /**
     * Halt simulation of Solar System for a period of time.
     * Length of period depends on simulation mode and slider settings.
     * @throws InterruptedException
     */
    private void haltSimulation() throws InterruptedException {
        int period;
        if (simulationIsRunningFast) {
            period = 1;
        }
        else {
            period = 1 + (20 - (int) (sliderSimulationSpeed.getValue() / 5.0));
            if (simulationIsRunningStepMode) {
                period = period * 10;
            }
        }
        taskSimulate.sleep(period);
    }

    /**
     * Show an alert message.
     * The message will disappear when the user presses ok.
     * @param header   Header of the alert message
     * @param content  Content of the alert message
     */
    private void showMessage(String header, String content) {
        // Use Platform.runLater() to ensure that code concerning
        // the Alert message is executed by the JavaFX Application Thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Solar System");
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.showAndWait();
            }
        });
    }

    /**
     * Show information panel for body of the Solar System.
     * @param bodyName Name of the body
     */
    private void showInformationPanel(final String bodyName) {
        if (informationPanels.containsKey(bodyName)) {
            informationPanels.get(bodyName).toFront();
        }
        else {
            InformationPanel panel = new InformationPanel(solarSystem,bodyName);
            panel.setOnCloseRequest((WindowEvent event) -> {
                informationPanels.remove(bodyName);
            });
            informationPanels.put(bodyName, panel);
            panel.show();
        }
    }

    /**
     * Set visualization settings.
     * @param settings settings for visualization
     */
    private void setVisualizationSettings(VisualizationSettings settings) {
        pauseSimulation();
        initTranslate();

        for (String spacecraftName : spacecraftNames) {
            if (settings.getBodiesShown().contains(spacecraftName)) {
                solarSystem.createSpacecraft(spacecraftName);
                bodiesShown.add(spacecraftName);
                showInformationPanel(spacecraftName);
            }
            else {
                solarSystem.removeSpacecraft(spacecraftName);
                bodiesShown.remove(spacecraftName);
                if (informationPanels.containsKey(spacecraftName)) {
                    informationPanels.get(spacecraftName).close();
                    informationPanels.remove(spacecraftName);
                }
            }
        }

        GregorianCalendar eventDateTime;
        if (settings.getSimulationStartDateTime() == null) {
            eventDateTime = new GregorianCalendar();
            eventDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        else {
            eventDateTime = CalendarUtil.createGregorianCalendar(settings.getSimulationStartDateTime());
        }
        dateTimeSelector.setDateTime(eventDateTime);
        try {
            solarSystem.initializeSimulation(eventDateTime);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }

        for (String bodyName : checkBoxesBodies.keySet()) {
            if (settings.getBodiesShown().contains(bodyName)) {
                checkBoxesBodies.get(bodyName).setSelected(true);
            }
            else {
                checkBoxesBodies.get(bodyName).setSelected(false);
            }
        }
        selectedBody = settings.getSelectedBody();

        radioGeneralRelativity.setSelected(settings.isGeneralRelativity());
        if (settings.isShowEphemeris() && settings.isShowSimulation()) {
            radioEphemerisAndSimulation.setSelected(true);
        }
        else {
            radioEphemerisOnly.setSelected(settings.isShowEphemeris());
            radioSimulationOnly.setSelected(settings.isShowSimulation());
        }
        checkBoxObservationFromEarth.setSelected(settings.isObservationFromEarth());
        checkBoxShowRuler.setSelected(settings.isShowRuler());
        checkBoxStepMode.setSelected(settings.isStepMode());
        sliderTopFrontView.setValue(settings.getValueTopFrontView());
        sliderZoomView.setValue(settings.getValueZoomView());
        sliderSimulationSpeed.setValue(settings.getValueSimulationSpeed());
    }

    /**
     * Create a list of visualization settings for event selector.
     */
    private List<VisualizationSettings> createVisualizationSettings() {
        List<VisualizationSettings> events = new ArrayList<>();
        VisualizationSettings init = new VisualizationSettings();
        init.setEventName("Initial settings (current time)");
        events.add(init);
        VisualizationSettings inner = new VisualizationSettings();
        inner.setEventName("Inner planets (current time)");
        inner.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Mercury","Venus",
                "Earth","Mars")));
        inner.setValueTopFrontView(15);
        inner.setValueZoomView(32);
        events.add(inner);
        VisualizationSettings outer = new VisualizationSettings();
        outer.setEventName("Outer planets (current time)");
        outer.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Jupiter","Saturn",
                "Uranus","Neptune","Pluto")));
        outer.setValueTopFrontView(15);
        outer.setValueZoomView(5);
        events.add(outer);
        VisualizationSettings em = new VisualizationSettings();
        em.setEventName("Earth-Moon system (current time)");
        em.setBodiesShown(new HashSet<>(Arrays.asList("Earth","Moon","EarthMoonBarycenter")));
        em.setSelectedBody("EarthMoonBarycenter");
        em.setValueZoomView(85);
        events.add(em);
        VisualizationSettings ast = new VisualizationSettings();
        ast.setEventName("Jupiter and asteroids (current time)");
        ast.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Jupiter","Ceres","Pallas",
                "Juno","Vesta","Eros","Bennu","Florence")));
        ast.setValueZoomView(23);
        events.add(ast);
        VisualizationSettings jup = new VisualizationSettings();
        jup.setEventName("Jupiter system (current time)");
        jup.setBodiesShown(new HashSet<>(Arrays.asList("Jupiter","JupiterMoons")));
        jup.setSelectedBody("Jupiter");
        jup.setValueZoomView(73);
        events.add(jup);
        VisualizationSettings sat = new VisualizationSettings();
        sat.setEventName("Saturn system (current time)");
        sat.setBodiesShown(new HashSet<>(Arrays.asList("Saturn","SaturnMoons")));
        sat.setSelectedBody("Saturn");
        sat.setValueZoomView(68);
        events.add(sat);
        VisualizationSettings ura = new VisualizationSettings();
        ura.setEventName("Uranus system (current time)");
        ura.setBodiesShown(new HashSet<>(Arrays.asList("Uranus","UranusMoons")));
        ura.setSelectedBody("Uranus");
        ura.setValueTopFrontView(10);
        ura.setValueZoomView(83);
        events.add(ura);
        VisualizationSettings nep = new VisualizationSettings();
        nep.setEventName("Neptune system (current time)");
        nep.setBodiesShown(new HashSet<>(Arrays.asList("Neptune","NeptuneMoons")));
        nep.setSelectedBody("Neptune");
        nep.setValueZoomView(88);
        events.add(nep);
        VisualizationSettings voy1 = new VisualizationSettings();
        voy1.setEventName("Launch Voyager 1 (1977-09-05  12:56)");
        voy1.setSimulationStartDateTime(trajectoryStartDate.get("Voyager 1"));
        voy1.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn",
                "Voyager 1","Voyager 2")));
        voy1.setSelectedBody("Voyager 1");
        voy1.setShowRuler(true);
        voy1.setStepMode(false);
        voy1.setValueZoomView(15);
        voy1.setValueSimulationSpeed(100);
        events.add(voy1);
        VisualizationSettings voy2 = new VisualizationSettings();
        voy2.setEventName("Launch Voyager 2 (1977-08-20  14:29)");
        voy2.setSimulationStartDateTime(trajectoryStartDate.get("Voyager 2"));
        voy2.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn","Uranus","Neptune",
                "Voyager 1","Voyager 2")));
        voy2.setSelectedBody("Voyager 2");
        voy2.setShowRuler(true);
        voy2.setStepMode(false);
        voy2.setValueZoomView(8);
        voy2.setValueSimulationSpeed(100);
        events.add(voy2);
        VisualizationSettings ros = new VisualizationSettings();
        ros.setEventName("Launch Rosetta (2004-03-02  07:17)");
        ros.setSimulationStartDateTime(trajectoryStartDate.get("Rosetta"));
        ros.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Mars","67P/Churyumov-Gerasimenko",
                "Rosetta")));
        ros.setSelectedBody("Rosetta");
        ros.setShowRuler(true);
        ros.setStepMode(false);
        ros.setValueZoomView(30);
        ros.setValueSimulationSpeed(100);
        events.add(ros);
        VisualizationSettings nh = new VisualizationSettings();
        nh.setEventName("Launch New Horizons (2006-01-19  19:00)");
        nh.setSimulationStartDateTime(trajectoryStartDate.get("New Horizons"));
        nh.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn","Uranus","Neptune","Pluto",
                "Ultima Thule","New Horizons")));
        nh.setSelectedBody("New Horizons");
        nh.setShowRuler(true);
        nh.setStepMode(false);
        nh.setValueZoomView(7);
        nh.setValueSimulationSpeed(100);
        events.add(nh);
        VisualizationSettings iss = new VisualizationSettings();
        iss.setEventName("International Space Station (current time)");
        iss.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Moon","Earth","ISS")));
        iss.setSelectedBody("ISS");
        iss.setShowEphemeris(false);
        iss.setShowRuler(false);
        iss.setStepMode(true);
        iss.setValueZoomView(100);
        iss.setValueSimulationSpeed(1);
        events.add(iss);
        VisualizationSettings ap8 = new VisualizationSettings();
        ap8.setEventName("Launch Apollo 8 S-IVB (1968-12-21  12.51)");
        ap8.setSimulationStartDateTime(trajectoryStartDate.get("Apollo 8"));
        ap8.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Moon","Earth","Apollo 8")));
        ap8.setSelectedBody("Earth");
        ap8.setShowRuler(true);
        ap8.setStepMode(true);
        ap8.setValueZoomView(85);
        ap8.setValueSimulationSpeed(100);
        events.add(ap8);
        return events;
    }

    /**
     * Inner-class for drawing the Solar System.
     * Drawing is done by the JavaFX Application Thread.
     */
    private class DrawAnimationTimer extends AnimationTimer {

        private long prevUpdate;

        @Override
        public void handle(long now) {
            long lag = now - prevUpdate;
            if (lag >= 20000000) {
                try {
                    monitor.startDrawing();
                    drawSimulationState();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    stop();
                }
                finally {
                    monitor.stopDrawing();
                }
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
     * Simulation runs on a separate thread and can be paused and resumed.
     */
    private class SimPausableTask extends PausableTask {

        @Override
        void task() {
            try {
                monitor.startSimulating();
                advanceSimulation();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finally {
                monitor.stopSimulating();
            }
            try {
                haltSimulation();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
