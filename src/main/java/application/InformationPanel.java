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

import ephemeris.EphemerisUtil;
import ephemeris.SolarSystemParameters;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import solarsystem.SolarSystem;
import solarsystem.SolarSystemBody;
import util.Vector3D;

import java.text.DecimalFormat;


/**
 * Information panel to show information on body of the Solar System.
 * @author Nico Kuijpers
 */
public class InformationPanel extends Stage {

    // Definition of constant values
    private static final int PANELWIDTH = 420;
    private static final int PANELHEIGHT = 880;
    private static final int PANELHEIGHTSUN = 270;
    private static final int GAPSIZE = 10;
    private static final int BORDERSIZE = 10;
    private static final int TEXTFIELDWIDTH = 115;
    private static final int BUTTONWIDTH = 115;
    private static final int SLIDERSIZE = 360;
    private static final int SLIDERTICKCOUNT = 5;
    private static final int SLIDERBLOCKINCREMENT = 5;
    private static final DecimalFormat DFNODEC = new DecimalFormat("0");
    private static final DecimalFormat DFTHREEDEC = new DecimalFormat("0.###");
    private static final DecimalFormat DFEXP = new DecimalFormat("0.######E0");
    private static final double MINDIFF = 1.0E-3;
    private static final double MINAXISKM = 1.0E3;
    private static final double MAXAXISKM = 0.5E7;
    private static final double MINAXISAU = 1.0E-1;
    private static final double MAXAXISAU = 1.0E2;

    // Reference to solar system
    private final SolarSystem solarSystem;

    // Body name, center body name
    private final String bodyName;
    private final String centerBodyName;

    // Indicates whether this body is the Sun
    private final boolean sun;

    // Indicates whether this body is a moon
    private final boolean moon;

    // Initial mass
    double initialMass;

    // Elliptical or hyperbolic orbit
    boolean ellipticOrbit;

    // Position corresponding to selected orbital elements
    Vector3D position = null;

    // Orbit corresponding to selected orbital elements
    Vector3D[] orbit = null;

    // Labels
    private final Label labelDistance;
    private final Label labelVelocity;
    private final Label labelDiameter;
    private final Label labelInitialMass;
    private final Label labelMass;
    private final Label labelAxis;
    private final Label labelEccentricity;
    private final Label labelInclination;
    private final Label labelMeanAnomaly;
    private final Label labelArgPerihelion;
    private final Label labelLongNode;

    // Text fields
    private final TextField textFieldMass;
    private final TextField textFieldAxis;
    private final TextField textFieldEccentricity;
    private final TextField textFieldInclination;
    private final TextField textFieldMeanAnomaly;
    private final TextField textFieldArgPerihelion;
    private final TextField textFieldLongNode;

    // Sliders
    private final Slider sliderMass;
    private final Slider sliderAxis;
    private final Slider sliderEccentricity;
    private final Slider sliderInclination;
    private final Slider sliderMeanAnomaly;
    private final Slider sliderArgPerihelion;
    private final Slider sliderLongNode;

    // Buttons
    private Button buttonApplyMass;
    private Button buttonCancelMass;
    private Button buttonResetMass;
    private Button buttonApplyOrbit;
    private Button buttonCancelOrbit;
    private Button buttonResetOrbit;

    // Radio buttons
    private RadioButton radioButtonEllipticOrbit;
    private RadioButton radioButtonHyperbolicOrbit;

    // Current values of mass and orbital elements
    double currentMass;
    double currentAxis;
    double currentEccentricity;
    double currentInclination;
    double currentMeanAnomaly;
    double currentArgPerihelion;
    double currentLongNode;

    /**
     * Constructor.
     * @param solarSystem Reference to the Solar System
     * @param bodyName    Name of Solar System Body to which this information panel belongs
     */
    public InformationPanel(SolarSystem solarSystem, String bodyName) {
        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(GAPSIZE);
        grid.setVgap(GAPSIZE);
        grid.setPadding(new Insets(BORDERSIZE, BORDERSIZE, BORDERSIZE, BORDERSIZE));

        // For debug purposes
        // Make the grid lines visible
        // grid.setGridLinesVisible(true);

        // Set reference to solar system
        this.solarSystem = solarSystem;

        // Set name of body and center body
        this.bodyName = bodyName;
        if (!bodyName.equals("Sun")) {
            centerBodyName = solarSystem.getBody(bodyName).getCenterBody().getName();
            sun = false;
        }
        else {
            centerBodyName = "";
            sun = true;
        }

        // Determine whether this body is a moon
        moon = !bodyName.equals("Sun") && !centerBodyName.equals("Sun");

        // Row index in grid
        int rowIndex = 1;

        // Column span for labels, text fields, sliders and buttons
        final int colSpanLabel = 2;
        final int colSpanTextField = 2;
        final int colSpanSlider = 4;
        final int colSpanButton = 1;

        // Column index for text fields
        final int colIndexTextField = 3;

        // Label to show current distance to center body
        labelDistance = new Label();
        grid.add(labelDistance, 1, rowIndex++, colSpanLabel, 1);

        // Label to show current velocity relative to center body
        labelVelocity = new Label();
        grid.add(labelVelocity, 1, rowIndex++, colSpanLabel, 1);

        // Label to show diameter
        labelDiameter = new Label();
        grid.add(labelDiameter, 1, rowIndex++, colSpanLabel, 1);

        // Label to show initial mass
        labelInitialMass = new Label();
        grid.add(labelInitialMass, 1, rowIndex++, colSpanLabel, 1);

        // Leave some room
        rowIndex++;

        // Label for current mass
        labelMass = new Label("Mass [kg]");
        grid.add(labelMass, 1, rowIndex, colSpanLabel, 1);

        // Text field to show and enter value of current mass
        textFieldMass = new TextField();
        textFieldMass.setMinWidth(TEXTFIELDWIDTH);
        textFieldMass.setMaxWidth(TEXTFIELDWIDTH);
        grid.add(textFieldMass,colIndexTextField,rowIndex++,colSpanTextField,1);

        // Slider to adapt body mass
        sliderMass = createSlider(0.0, 100.0, 50.0, 10.0);
        sliderMass.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateMass(valueToMass(sliderMass.getValue()));
            }
        });
        grid.add(sliderMass, 1, rowIndex++, colSpanSlider, 1);

        // Button to apply selected mass
        buttonApplyMass = new Button("Apply Mass");
        buttonApplyMass.setMinWidth(BUTTONWIDTH);
        buttonApplyMass.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                applySelectedMass();
            }
        });
        grid.add(buttonApplyMass, 1, rowIndex, colSpanButton, 1);

        // Button to cancel selected mass
        buttonCancelMass = new Button("Cancel Mass");
        buttonCancelMass.setMinWidth(BUTTONWIDTH);
        buttonCancelMass.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                resetSelectedMass();
            }
        });
        grid.add(buttonCancelMass, 2, rowIndex, colSpanButton, 1);


        // Button to reset mass
        buttonResetMass = new Button("Reset Mass");
        buttonResetMass.setMinWidth(BUTTONWIDTH);
        buttonResetMass.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                setInitialMass();
            }
        });
        grid.add(buttonResetMass, 3, rowIndex++, colSpanButton, 1);

        // Leave some room
        rowIndex++;

        // Sliders and text fields to show and enter orbital elements
        if (moon) {
            labelAxis = new Label("Semi-major axis [km]");
        }
        else {
            labelAxis = new Label("Semi-major axis [A.U.]");
        }
        labelEccentricity = new Label("Eccentricity [-]");
        labelInclination = new Label("Inclination [degrees]");
        labelMeanAnomaly = new Label("Mean anomaly [degrees]");
        labelArgPerihelion = new Label("Argument of perihelion [degrees]");
        labelLongNode = new Label("Longitude of ascending node [degrees]");
        textFieldAxis = new TextField();
        textFieldEccentricity = new TextField();
        textFieldInclination = new TextField();
        textFieldMeanAnomaly = new TextField();
        textFieldArgPerihelion = new TextField();
        textFieldLongNode = new TextField();
        if (moon) {
            // Axis in km
            sliderAxis = createSlider(0.0, 5000000.0, 1000000.0, 1000000.0);
        }
        else {
            // Axis in A.U.
            sliderAxis = createSlider(0.0, 100, 1, 10);
        }
        sliderEccentricity = createSlider(0.0, 1.0, 1.0, 0.1);
        sliderInclination = createSlider(0.0, 180.0, 0.0, 15.0);
        sliderMeanAnomaly = createSlider(-180.0, 180.0, 0.0, 30.0);
        sliderArgPerihelion = createSlider(-180.0, 180.0, 0.0, 30.0);
        sliderLongNode = createSlider(-180.0, 180.0, 0.0, 30.0);

        if (!sun) {
            // Radio buttons to choose between elliptic and hyperbolic orbit
            // 1. Elliptic orbit
            // 2. Hyperbolic orbit
            radioButtonEllipticOrbit = new RadioButton("Elliptic orbit");
            Tooltip tooltipEllipticOrbit =
                    new Tooltip("Elliptic orbit: semi-major axis > 0 and 0 <= eccentricity < 1");
            radioButtonEllipticOrbit.setTooltip(tooltipEllipticOrbit);
            radioButtonEllipticOrbit.setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                    setEllipticOrbit();
                }
            });
            radioButtonHyperbolicOrbit = new RadioButton("Hyperbolic orbit");
            Tooltip tooltipHyperbolicOrbit =
                    new Tooltip("Hyperbolic orbit: semi-major axis < 0 and eccentricity > 1");
            radioButtonHyperbolicOrbit.setTooltip(tooltipHyperbolicOrbit);
            radioButtonHyperbolicOrbit.setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                    setHyperbolicOrbit();
                }
            });
            ToggleGroup toggleGroupOrbitType = new ToggleGroup();
            radioButtonEllipticOrbit.setToggleGroup(toggleGroupOrbitType);
            radioButtonHyperbolicOrbit.setToggleGroup(toggleGroupOrbitType);
            radioButtonEllipticOrbit.setSelected(true);
            grid.add(radioButtonEllipticOrbit,1,rowIndex++,colSpanSlider,1);
            grid.add(radioButtonHyperbolicOrbit,1,rowIndex++,colSpanSlider,1);

            // Label for semi-major axis
            grid.add(labelAxis, 1, rowIndex, colSpanLabel, 1);

            // Text field to show and enter value of semi-major axis [km] or [au]
            textFieldAxis.setMinWidth(TEXTFIELDWIDTH);
            textFieldAxis.setMaxWidth(TEXTFIELDWIDTH);
            grid.add(textFieldAxis,colIndexTextField,rowIndex++,colSpanTextField,1);

            // Slider to adapt semi-major axis [km] or [au]
            sliderAxis.valueProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    double value = sliderAxis.getValue();
                    if (!ellipticOrbit) {
                        value = -value;
                    }
                    updateAxis(value);
                    computeOrbit();
                }
            });
            grid.add(sliderAxis, 1, rowIndex++, colSpanSlider, 1);

            // Label for eccentricity
            grid.add(labelEccentricity, 1, rowIndex, colSpanLabel, 1);

            // Text field to show and enter value of eccentricity [-]
            textFieldEccentricity.setMinWidth(TEXTFIELDWIDTH);
            textFieldEccentricity.setMaxWidth(TEXTFIELDWIDTH);
            grid.add(textFieldEccentricity,colIndexTextField,rowIndex++,colSpanTextField,1);

            // Slider to adapt eccentricity
            sliderEccentricity.valueProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    computeOrbit();
                    updateEccentricity(sliderEccentricity.getValue());
                }
            });
            grid.add(sliderEccentricity, 1, rowIndex++, colSpanSlider, 1);

            // Label for inclination
            grid.add(labelInclination, 1, rowIndex, colSpanLabel, 1);

            // Text field to show and enter value of inclination [degrees]
            textFieldInclination.setMaxWidth(TEXTFIELDWIDTH);
            textFieldInclination.setMaxWidth(TEXTFIELDWIDTH);
            grid.add(textFieldInclination,colIndexTextField,rowIndex++,colSpanTextField,1);

            // Slider to adapt inclination (range 0 - 180 degrees)
            sliderInclination.valueProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    computeOrbit();
                    updateInclination(sliderInclination.getValue());
                }
            });
            grid.add(sliderInclination, 1, rowIndex++, colSpanSlider, 1);

            // Label to for mean anomaly
            grid.add(labelMeanAnomaly, 1, rowIndex, colSpanLabel, 1);

            // Text field to show and enter value of mean anomaly [degrees]
            textFieldMeanAnomaly.setMinWidth(TEXTFIELDWIDTH);
            textFieldMeanAnomaly.setMaxWidth(TEXTFIELDWIDTH);
            grid.add(textFieldMeanAnomaly,colIndexTextField,rowIndex++,colSpanTextField,1);

            // Slider to adapt mean anomaly
            sliderMeanAnomaly.valueProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    computeOrbit();
                    updateMeanAnomaly(sliderMeanAnomaly.getValue());
                }
            });
            grid.add(sliderMeanAnomaly, 1, rowIndex++, colSpanSlider, 1);

            // Label for argument of perihelion
            grid.add(labelArgPerihelion, 1, rowIndex, colSpanLabel, 1);

            // Text field to show and enter argument of perihelion [degrees]
            textFieldArgPerihelion.setMinWidth(TEXTFIELDWIDTH);
            textFieldArgPerihelion.setMaxWidth(TEXTFIELDWIDTH);
            grid.add(textFieldArgPerihelion,colIndexTextField,rowIndex++,colSpanTextField,1);

            // Slider to adapt argument of perihelion
            sliderArgPerihelion.valueProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    computeOrbit();
                    updateArgPerihelion(sliderArgPerihelion.getValue());
                }
            });
            grid.add(sliderArgPerihelion, 1, rowIndex++, colSpanSlider, 1);

            // Label for longitude of ascending node
            grid.add(labelLongNode, 1, rowIndex, colSpanLabel, 1);

            // Text field to show and enter longitude of ascending node [degrees]
            textFieldLongNode.setMinWidth(TEXTFIELDWIDTH);
            textFieldLongNode.setMaxWidth(TEXTFIELDWIDTH);
            grid.add(textFieldLongNode,colIndexTextField,rowIndex++,colSpanTextField,1);

            // Slider to adapt longitude of ascending node
            sliderLongNode.valueProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    computeOrbit();
                    updateLongNode(sliderLongNode.getValue());
                }
            });
            grid.add(sliderLongNode, 1, rowIndex++, colSpanSlider, 1);

            // Button to set orbital elements
            buttonApplyOrbit = new Button("Apply Orbit");
            buttonApplyOrbit.setMinWidth(BUTTONWIDTH);
            buttonApplyOrbit.setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                    try {
                        setPositionVelocityParticle();
                        updatePanel();
                    }
                    catch (SolarSystemException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            });
            grid.add(buttonApplyOrbit, 1, rowIndex, colSpanButton, 1);

            // Button to cancel selection of orbital elements
            buttonCancelOrbit = new Button("Cancel Orbit");
            buttonCancelOrbit.setMinWidth(BUTTONWIDTH);
            buttonCancelOrbit.setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                    try {
                        updatePanel();
                    }
                    catch (SolarSystemException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            });
            grid.add(buttonCancelOrbit, 2, rowIndex, colSpanButton, 1);

            // Button to reset orbital elements
            buttonResetOrbit = new Button("Reset Orbit");
            buttonResetOrbit.setMinWidth(BUTTONWIDTH);
            buttonResetOrbit.setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                    try {
                        SolarSystemBody body = solarSystem.getBody(bodyName);
                        solarSystem.setPositionVelocity(bodyName, body.getPosition(), body.getVelocity());
                        updatePanel();
                    }
                    catch (SolarSystemException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            });
            grid.add(buttonResetOrbit, 3, rowIndex++, colSpanButton, 1);
        }

        // Set font for all labeled objects
        for (Node n : grid.getChildren()) {
            if (n instanceof Labeled) {
                ((Labeled) n).setFont(new Font("Arial",13));
            }
        }

        // Initialize labels and sliders
        try {
            initialMass = solarSystem.getMass(bodyName);
            updateDiameter(solarSystem.getBody(bodyName).getDiameter());
            updateInitialMass(initialMass);
            updatePanel();
        }
        catch(SolarSystemException ex) {
            System.err.println(ex.getMessage());
        }

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene;
        if (!sun) {
            scene = new Scene(root,PANELWIDTH,PANELHEIGHT);
        }
        else
        {
            scene = new Scene(root,PANELWIDTH,PANELHEIGHTSUN);
        }
        root.getChildren().add(grid);
        String title = bodyName;
        if (moon) {
           title += " (moon of " + centerBodyName + ")";
        }
        this.setTitle(title);
        this.setScene(scene);
        this.show();
    }

    /**
     * Get position corresponding to selected orbital elements.
     * @return position
     */
    public Vector3D getPosition() {
        computePosition();
        return position;
    }

    /**
     * Get orbit corresponding to selected orbital elements.
     * @return orbit
     */
    public Vector3D[] getOrbit() {
        computeOrbit();
        return orbit;
    }

    /**
     * Update diameter on panel.
     * @param diameter [m]
     */
    private void updateDiameter(double diameter) {
        double diameterKm = 0.001 * diameter;
        labelDiameter.setText("Diameter " + DFNODEC.format(diameterKm) + " km");
    }

    /**
     * Update value of initial mass on panel.
     * @param initMassVal [kg]
     */
    private void updateInitialMass(double initMassVal) {
        labelInitialMass.setText("Initial mass " + DFEXP.format(initMassVal) + " kg");
    }

    /**
     * Update value of mass on panel.
     * @param massVal [kg]
     */
    private void updateMass(double massVal) {
        textFieldMass.setText(DFEXP.format(massVal));
    }

    /**
     * Update distance to center body on panel.
     * @param distance [m]
     */
    private void updateDistance(double distance) {
        double distanceKm = 0.001 * distance;
        labelDistance.setText("Distance to " + centerBodyName + " " + DFEXP.format(distanceKm) + " km");
    }

    /**
     * Update velocity relative to center body on panel.
     * @param velocity [m/s]
     */
    private void updateVelocity(double velocity) {
        labelVelocity.setText("Velocity relative to " + centerBodyName + " " + DFNODEC.format(velocity) + " m/s");
    }

    /**
     * Update value of semi-major axis on panel.
     * @param axis Axis in km (moon) or A.U. (not moon)
     */
    private void updateAxis(double axis) {
        if (moon) {
            // Axis in km
            textFieldAxis.setText(DFNODEC.format(axis));
        }
        else {
            // Axis in A.U.
            textFieldAxis.setText(DFTHREEDEC.format(axis));
        }
    }

    /**
     * Update value of eccentricity on panel.
     */
    private void updateEccentricity(double eccentricity) {
        textFieldEccentricity.setText(DFTHREEDEC.format(eccentricity));
    }

    /**
     * Update value of inclination on panel.
     */
    private void updateInclination(double inclination) {
        textFieldInclination.setText(DFTHREEDEC.format(inclination));
    }

    /**
     * Update value of mean anomaly on panel.
     */
    private void updateMeanAnomaly(double meanAnomaly) {
        textFieldMeanAnomaly.setText(DFTHREEDEC.format(meanAnomaly));
    }

    /**
     * Update value of argument of perihelion on panel.
     */
    private void updateArgPerihelion(double argPerihelion) {
        textFieldArgPerihelion.setText(DFTHREEDEC.format(argPerihelion));
    }

    /**
     * Update value of longitude of ascending node on panel.
     */
    private void updateLongNode(double longNode) {
        textFieldLongNode.setText(DFTHREEDEC.format(longNode));
    }

    /**
     * Update information of the body.
     * @throws SolarSystemException when particle does not exist
     */
    public void updatePanel() throws SolarSystemException {

        // Current position and velocity of particle
        Vector3D position = solarSystem.getPosition(bodyName);
        Vector3D velocity = solarSystem.getVelocity(bodyName);

        // Update mass
        currentMass = solarSystem.getMass(bodyName);
        updateMass(currentMass);
        sliderMass.setValue(massToValue(currentMass));

        // Update remaining labels when this body is not the Sun
        if (!"Sun".equals(bodyName)) {
            // Compute orbital elements from current position and velocity
            position = position.minus(solarSystem.getPosition(centerBodyName));
            velocity = velocity.minus(solarSystem.getVelocity(centerBodyName));
            double muCenterBody = solarSystem.getMu(centerBodyName);
            double[] orbitElements =
                    EphemerisUtil.computeOrbitalElementsFromPositionVelocity(muCenterBody, position, velocity);

            // Update text fields
            updateDistance(position.magnitude());    // Distance to center body [m]
            updateVelocity(velocity.magnitude());    // Velocity relative to center body [m/s]
            if (moon) {
                // semi-major axis [km], convert from A.U. to km
                currentAxis = convertAUtoKM(orbitElements[0]);
            }
            else {
                // semi-major axis [A.U.]
                currentAxis = orbitElements[0];
            }
            if (currentAxis > 0.0) {
                radioButtonEllipticOrbit.setSelected(true);
                setEllipticOrbit();
            }
            else {
                radioButtonHyperbolicOrbit.setSelected(true);
                setHyperbolicOrbit();
            }
            sliderAxis.setValue(Math.abs(currentAxis));
            updateAxis(currentAxis);
            currentEccentricity = orbitElements[1];  // eccentricity [-]
            sliderEccentricity.setValue(currentEccentricity);
            updateEccentricity(currentEccentricity);
            currentInclination = orbitElements[2];   // inclination [degrees]
            sliderInclination.setValue(currentInclination);
            updateInclination(currentInclination);
            currentMeanAnomaly = orbitElements[3];   // mean anomaly [degrees]
            sliderMeanAnomaly.setValue(currentMeanAnomaly);
            updateMeanAnomaly(currentMeanAnomaly);
            currentArgPerihelion = orbitElements[4]; // argument of perihelion [degrees]
            sliderArgPerihelion.setValue(currentArgPerihelion);
            updateArgPerihelion(currentArgPerihelion);
            currentLongNode = orbitElements[5];      // longitude of ascending node [degrees]
            sliderLongNode.setValue(currentLongNode);
            updateLongNode(currentLongNode);
        }
    }

    /**
     * Convert A.U. to km.
     * @param valueAU value to be converted [A.U.]
     * @return converted value [km]
     */
    private double convertAUtoKM(double valueAU) {
        return 0.001 * valueAU * SolarSystemParameters.ASTRONOMICALUNIT;
    }

    /**
     * Convert km to A.U.
     * @param valueKM value to be converted [km]
     * @return converted value [A.U.]
     */
    private double convertKMtoAU(double valueKM) {
        return 1000 * valueKM / SolarSystemParameters.ASTRONOMICALUNIT;
    }

    /**
     * Convert value in range 0 - 100 to mass in kg.
     * Value of 50 corresponds to initial mass.
     * @param value [0 - 100]
     * @return mass [kg]
     */
    private double valueToMass(double value) {
        return Math.exp(0.1*(value - 50.0)) * initialMass;
    }

    /**
     * Convert mass in kg to value in range 0 - 100.
     * Value of 50 corresponds to initial mass.
     * @param mass [kg]
     * @return value [0 - 100]
     */
    private double massToValue(double mass) {
        return 10.0*Math.log(mass/initialMass) + 50.0;
    }

    /**
     * Obtain value from text field. If the value obtained from the text field differs at least
     * MINDIFF from the current value, the text field is value is taken, otherwise the current value
     * is retained. The value is bounded by a minimum and a maximum value. In case of a parse errror,
     * the current value is retained.
     * @param textField text field to obtain value from
     * @param currentValue current value
     * @param minValue minimum value
     * @param maxValue maximum value
     * @return value in range [minValue maxValue] or currentValue in case of parse exception
     */
    private double getValueFromTextField(TextField textField, double currentValue, double minValue, double maxValue) {
        double value;
        try {
            // Value is bounded by given minimum and maximum
            value = Double.parseDouble(textField.getText());
            value = Math.min(value, maxValue);
            value = Math.max(value, minValue);

            // Note that in case the value has not been changed by the user,
            // the string representation in the text field is not accurate enough.
            // Therefore, if the string value differs less than MINDIFF from the
            // current value, the current value is retained.
            if (Math.abs(value - currentValue) < MINDIFF) {
                value = currentValue;
            }
        }
        catch (Exception e) {
            // Current value is retained in case of parse error
            value = currentValue;
        }
        return value;
    }

    /**
     * Obtain orbital elements from text fields.
     * @return orbital elements
     */
    private double[] getOrbitElementsFromTextFields() {
        double[] orbitElements = new double[6];
        double axis;
        if (moon) {
            // Moon: major axis in km
            axis = getValueFromTextField(textFieldAxis,currentAxis,-MAXAXISKM,MAXAXISKM);
        }
        else {
            // Not a moon: major axis in A.U.
            axis = getValueFromTextField(textFieldAxis,currentAxis,-MAXAXISAU,MAXAXISAU);
        }
        if (ellipticOrbit) {
            // Elliptic orbit: major axis is positive
            axis = Math.abs(axis);
            if (moon) {
                axis = Math.max(axis, MINAXISKM);
            }
            else {
                axis = Math.max(axis, MINAXISAU);
            }
        }
        else {
            // Hyperbolic orbit: major axis is negative
            axis = -Math.abs(axis);
            if (moon) {
                axis = Math.min(axis, -MINAXISKM);
            }
            else {
                axis = Math.min(axis, -MINAXISAU);
            }
        }
        if (moon) {
            // Moon: major axis in km; convert to A.U.
            orbitElements[0] = convertKMtoAU(axis);
        }
        else {
            // Not a moon: major axis in A.U.
            orbitElements[0] = axis;
        }
        if (ellipticOrbit) {
            // Elliptic orbit: 0.0 <= eccentricity < 1.0
            orbitElements[1] = getValueFromTextField(textFieldEccentricity, currentEccentricity, 0.0, 0.995);
        }
        else {
            // Hyperbolic orbit: 1.0 < eccentricity
            orbitElements[1] = getValueFromTextField(textFieldEccentricity, currentEccentricity, 1.005, Double.MAX_VALUE);
        }
        orbitElements[2] = getValueFromTextField(textFieldInclination,currentInclination,0.0,180.0);
        if (ellipticOrbit) {
            // Elliptic orbit: mean anomaly between -180 and 180 degrees
            orbitElements[3] = getValueFromTextField(textFieldMeanAnomaly,currentMeanAnomaly,-180.0,180.0);
        }
        else {
            // Hyperbolic orbit: mean anomaly may have any value
            // Note that Double.MIN_VALUE is the smallest positive value and therefore -Double.MAX_VALUE is used as minimum
            // See e.g. https://stackoverflow.com/questions/3884793/why-is-double-min-value-in-not-negative
            orbitElements[3] = getValueFromTextField(textFieldMeanAnomaly,currentMeanAnomaly,-Double.MAX_VALUE,Double.MAX_VALUE);
        }
        orbitElements[4] = getValueFromTextField(textFieldArgPerihelion,currentArgPerihelion,-180.0,180.0);
        orbitElements[5] = getValueFromTextField(textFieldLongNode,currentLongNode,-180.0,180.0);
        return orbitElements;
    }

    /**
     * Compute position corresponding to selected orbital elements.
     */
    private void computePosition() {
        Vector3D positionCenterParticle;
        try {
            positionCenterParticle = solarSystem.getPosition(centerBodyName);
        }
        catch(SolarSystemException ex) {
            positionCenterParticle = new Vector3D();
        }
        double[] orbitElements = getOrbitElementsFromTextFields();
        position = EphemerisUtil.computePosition(orbitElements);
        position.addVector(positionCenterParticle);
    }

    /**
     * Compute orbit corresponding to selected orbital elements.
     */
    private void computeOrbit() {
        Vector3D positionCenterParticle;
        try {
            positionCenterParticle = solarSystem.getPosition(centerBodyName);
        }
        catch(SolarSystemException ex) {
            positionCenterParticle = new Vector3D();
        }
        double[] orbitElements = getOrbitElementsFromTextFields();
        orbit = EphemerisUtil.computeOrbit(orbitElements);
        for (int i = 0; i < orbit.length; i++) {
            orbit[i].addVector(positionCenterParticle);
        }
    }

    /**
     * Set position and velocity of particle based on selected orbital elements.
     * @throws SolarSystemException when particle does not exist
     */
    private void setPositionVelocityParticle() throws SolarSystemException {
        double[] orbitElements = getOrbitElementsFromTextFields();
        Vector3D position = EphemerisUtil.computePosition(orbitElements);
        double muCenterBody = solarSystem.getMu(centerBodyName);
        Vector3D velocity = EphemerisUtil.computeVelocity(muCenterBody, orbitElements);
        position.addVector(solarSystem.getPosition(centerBodyName));
        velocity.addVector(solarSystem.getVelocity(centerBodyName));
        solarSystem.setPositionVelocity(bodyName, position, velocity);
    }

    /**
     * Create a new slider.
     * @param minVal minimum value
     * @param maxVal maximum value
     * @param initVal initial value
     * @param tick major tick unit
     * @return the new slider
     */
    private Slider createSlider(double minVal, double maxVal, double initVal, double tick) {
        Slider slider = new Slider();
        slider.setMinWidth(SLIDERSIZE);
        slider.setMin(minVal);
        slider.setMax(maxVal);
        slider.setValue(initVal);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(false);
        slider.setMajorTickUnit(tick);
        slider.setMinorTickCount(SLIDERTICKCOUNT);
        slider.setBlockIncrement(SLIDERBLOCKINCREMENT);
        return slider;
    }

    /**
     * Apply selected mass to particle.
     */
    private void applySelectedMass() {
        double minMass = valueToMass(sliderMass.getMin());
        double maxMass = valueToMass(sliderMass.getMax());
        double mass = getValueFromTextField(textFieldMass,initialMass,minMass,maxMass);
        updateMass(mass);
        sliderMass.setValue(massToValue(mass));
        try {
            solarSystem.setMass(bodyName,mass);
        }
        catch (SolarSystemException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Reset selected mass to current mass.
     */
    private void resetSelectedMass() {
        updateMass(currentMass);
        sliderMass.setValue(massToValue(currentMass));
    }

    /**
     * Set mass of particle to initial value.
     */
    private void setInitialMass() {
        updateMass(initialMass);
        sliderMass.setValue(massToValue(initialMass));
        try {
            solarSystem.setMass(bodyName,initialMass);
        }
        catch (SolarSystemException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Set sliders and orbit elements to elliptic orbit
     * For an elliptic orbit it holds:
     * major axis > 0.0
     * 0.0 <= eccentricity < 1.0
     */
    private void setEllipticOrbit() {
        ellipticOrbit = true;
        if (currentAxis < 0.0) {
            currentAxis = -currentAxis;
        }
        if (currentEccentricity > 1.0) {
            currentEccentricity %= 1.0;
        }
        updateAxis(currentAxis);
        sliderEccentricity.setMin(0.0);
        sliderEccentricity.setMax(1.0);
        sliderEccentricity.setValue(currentEccentricity);
        updateEccentricity(currentEccentricity);
    }

    /**
     * Set sliders and orbit elements to hyperbolic orbit.
     * For a hyperbolic orbit it holds:
     * major axis < 0.0
     * 1.0 < eccentricity
     */
    private void setHyperbolicOrbit() {
        ellipticOrbit = false;
        if (currentAxis > 0.0) {
            currentAxis = -currentAxis;
        }
        if (currentEccentricity < 1.0) {
            currentEccentricity += 1.0;
        }
        updateAxis(currentAxis);
        sliderEccentricity.setMin(1.0);
        sliderEccentricity.setMax(2.0);
        sliderEccentricity.setValue(currentEccentricity);
        updateEccentricity(currentEccentricity);
    }
}
