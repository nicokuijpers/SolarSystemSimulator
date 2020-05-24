/*
 * Copyright (c) 2020 Nico Kuijpers
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

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Settings for visualization of events such as spacecraft flybys, solar eclipses, etc.
 */
public class VisualizationSettings {
    // Name of event
    private String eventName = "Current date/time";

    // Start date for event
    private GregorianCalendar simulationStartDateTime = null;

    // Bodies to be shown on screen
    private Set<String> bodiesShown = new HashSet<>();

    // Selected body
    private String selectedBody = "Sun";

    // Flag to indicate whether general relativity should be applied
    private boolean generalRelativity = false;

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

    // Value for slider to set top-front view
    private double valueTopFrontView = 90.0;

    // Value for slider to set zoom of view
    private double valueZoomView = 30.0;

    // Value for slider simulation speed
    private double valueSimulationSpeed = 50.0;

    /**
     * Default constructor.
     */
    public VisualizationSettings() {
        bodiesShown.add("Sun");
        bodiesShown.add("Earth");
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public GregorianCalendar getSimulationStartDateTime() {
        return simulationStartDateTime;
    }

    public void setSimulationStartDateTime(GregorianCalendar simulationStartDateTime) {
        this.simulationStartDateTime = simulationStartDateTime;
    }

    public Set<String> getBodiesShown() {
        return bodiesShown;
    }

    public void setBodiesShown(Set<String> bodiesShown) {
        this.bodiesShown = bodiesShown;
    }

    public String getSelectedBody() {
        return selectedBody;
    }

    public void setSelectedBody(String selectedBody) {
        this.selectedBody = selectedBody;
    }

    public boolean isGeneralRelativity() {
        return generalRelativity;
    }

    public void setGeneralRelativityFlag(boolean generalRelativity) {
        this.generalRelativity = generalRelativity;
    }

    public boolean isShowEphemeris() {
        return showEphemeris;
    }

    public void setShowEphemeris(boolean showEphemeris) {
        this.showEphemeris = showEphemeris;
    }

    public boolean isShowSimulation() {
        return showSimulation;
    }

    public void setShowSimulation(boolean showSimulation) {
        this.showSimulation = showSimulation;
    }

    public boolean isObservationFromEarth() {
        return observationFromEarth;
    }

    public void setObservationFromEarth(boolean observationFromEarth) {
        this.observationFromEarth = observationFromEarth;
    }

    public boolean isShowRuler() {
        return showRuler;
    }

    public void setShowRuler(boolean showRuler) {
        this.showRuler = showRuler;
    }

    public boolean isStepMode() {
        return stepMode;
    }

    public void setStepMode(boolean stepMode) {
        this.stepMode = stepMode;
    }

    public double getValueTopFrontView() {
        return valueTopFrontView;
    }

    public void setValueTopFrontView(double valueTopFrontView) {
        this.valueTopFrontView = valueTopFrontView;
    }

    public double getValueZoomView() {
        return valueZoomView;
    }

    public void setValueZoomView(double valueZoomView) {
        this.valueZoomView = valueZoomView;
    }

    public double getValueSimulationSpeed() {
        return valueSimulationSpeed;
    }

    public void setValueSimulationSpeed(double valueSimulationSpeed) {
        this.valueSimulationSpeed = valueSimulationSpeed;
    }
}
