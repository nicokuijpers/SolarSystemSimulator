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
import visualization.SolarSystemViewMode;
import visualization.SolarSystemVisualization;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Solar System Application.
 * @author Nico Kuijpers
 */
public class SolarSystemApplication extends Application {

    // Screen size
    private static final int BORDERSIZE = 10;
    //private static final int BORDERSIZE = 0; // TO REMOVE WHITE BORDERS
    private static final int SCREENWIDTH = 900;
    private static final int SCREENHEIGHT = 900;
    //private static final int SCREENWIDTH = 1940; // VIDEO WIDTH
    //private static final int SCREENHEIGHT = 1100; // VIDEO HEIGHT
    //private static final int SCREENWIDTH = 450; // VIDEO SMALL RIGHT UPPER CORNER
    //private static final int SCREENHEIGHT = 450; // VIDEO SMALL RIGHT UPPER CORNER
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

    // Flag to indicate whether automatic simulation should run fast
    private boolean automaticSimulationFast = true;

    // Date/time selector to view and set simulation era, date, and time
    private DateTimeSelector dateTimeSelector;

    // Event selector
    private ComboBox eventSelector;

    // Radio buttons to set simulation method
    private RadioButton radioNewtonMechanics;
    private RadioButton radioGeneralRelativity;
    private RadioButton radioCurvatureWavePropagation;

    // Radio buttons to set visualization of ephemeris/simulation results
    private RadioButton radioEphemerisOnly;
    private RadioButton radioSimulationOnly;
    private RadioButton radioEphemerisAndSimulation;

    // Radio buttons and check box to set view mode
    private RadioButton radioTelescopeView;
    private RadioButton radioSpacecraftView;
    private CheckBox checkBoxAutomaticView;
    private boolean automaticView = false;
    private String closestBody = "";
    private String observedBody = "";

    // Location Amsterdam, The Netherlands
    private double latitude = 52.3676; // degrees
    private double longitude = 4.9041; // degrees

    // Decimal formats
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat DECIMAL_FORMAT_LATLON = new DecimalFormat("0.0000");

    // Slider to set latitude of location
    private Label labelLatitude;
    private TextField textFieldLatitude;
    private Slider sliderLatitude;

    // Slider to set longitude of location
    private Label labelLongitude;
    private TextField textFieldLongitude;
    private Slider sliderLongitude;

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

    // Body selector panel with check boxes for each body to be shown
    private BodySelectorPanel bodySelectorPanel;

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

    // 3D visualization
    private SolarSystemVisualization visualization;

    // View mode for 3D visualization
    private SolarSystemViewMode viewMode;

    /*
     * Date/times in UTC to control 3D visualization from Apollo 8
     * January = 1, February = 2, etc
     *
     * Launch 21 December 1968 at 12.51.00 UTC
     * Entry trajectory initialization 27 December 1968 at 15:36:52
     * Splashdown 27 December 1968 at 15.52 UTC
     * National Aeronautics and Space Administration - Apollo 8 Mission Report
     * https://www.hq.nasa.gov/alsj/a410/A08_MissionReport.pdf
     *
     * NASA Scientific Visualization Studio - Earthrise
     * https://svs.gsfc.nasa.gov/3936
     */
    private GregorianCalendar startEarthRise =
            CalendarUtil.createGregorianCalendar(1968, 12, 24, 16, 37, 50);
    private GregorianCalendar endEarthRise =
            CalendarUtil.createGregorianCalendar(1968, 12, 24, 16, 39, 0);
    private GregorianCalendar entryTrajectInitApolloEight =
            CalendarUtil.createGregorianCalendar(1968, 12, 27, 15, 36, 52);

    /*
     * Date/times in UTC to control 3D visualization from Pioneer 10 during flyby of Jupiter
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Pioneer_10
     * 1973-12-03  12:26:00 Callisto flyby at 1,392,300 km
     * 1973-12-03  13:56:00 Ganymede flyby at 446,250 km
     * 1973-12-03  19:26:00 Europa flyby at 321,000 km
     * 1973-12-03  22:56:00 Io flyby at 357,000 km
     * 1973-12-04  02:26:00 Jupiter closest approach at 200,000 km
     * 1973-12-04  02:36:00 Jupiter equator plane crossing
     */
    private GregorianCalendar startPioneerTenCallisto =
            CalendarUtil.createGregorianCalendar(1973,12,2,12,0,0);
    private GregorianCalendar startPioneerTenGanymede =
            CalendarUtil.createGregorianCalendar(1973,12,3,0,0,0);
    private GregorianCalendar startPioneerTenEuropa =
            CalendarUtil.createGregorianCalendar(1973,12,3,15,0,0);
    private GregorianCalendar startPioneerTenIo =
            CalendarUtil.createGregorianCalendar(1973,12,3,20,0,0);
    private GregorianCalendar startPioneerTenJupiter =
            CalendarUtil.createGregorianCalendar(1973,12,3,23,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Pioneer 11 during flyby of Jupiter
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Pioneer_11
     * 1974-12-02  08:21:00 Callisto flyby at 786,500 km.
     * 1974-12-02  22:09:00 Ganymede flyby at 692,300 km.
     * 1974-12-03  03:11:00 Io flyby at 314,000 km.
     * 1974-12-03  04:15:00 Europa flyby at 586,700 km.
     * 1974-12-03  05:21:19 Jupiter closest approach at 42,828 km.
     * 1974-12-03  22:29:00 Amalthea flyby at 127,500 km.
     * Note that Amalthea is not simulated.
     */
    private GregorianCalendar startPioneerElevenCallisto =
            CalendarUtil.createGregorianCalendar(1974,12,2,0,0,0);
    private GregorianCalendar startPioneerElevenGanymede =
            CalendarUtil.createGregorianCalendar(1974,12,2,8,0,0);
    private GregorianCalendar startPioneerElevenIo =
            CalendarUtil.createGregorianCalendar(1974,12,2,23,0,0);
    private GregorianCalendar startPioneerElevenEuropa =
            CalendarUtil.createGregorianCalendar(1974,12,3,3,0,0);
    private GregorianCalendar startPioneerElevenJupiter =
            CalendarUtil.createGregorianCalendar(1974,12,3,7,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Pioneer 11 during flyby of Saturn
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Pioneer_11
     * 1979-08-29  06:06:10 Iapetus flyby at 1,032,535 km.
     * 1979-08-29  11:53:33 Phoebe flyby at 13,713,574 km.
     * 1979-08-31  12:32:33 Hyperion flyby at 666,153 km.
     * 1979-09-01  14:26:56 Descending ring plane crossing.
     * 1979-09-01  14:50:55 Epimetheus flyby at 6,676 km.
     * 1979-09-01  15:06:32 Atlas flyby at 45,960 km.
     * 1979-09-01  15:59:30 Dione flyby at 291,556 km.
     * 1979-09-01  16:26:28 Mimas flyby at 104,263 km.
     * 1979-09-01  16:29:34 Saturn closest approach at 20,591 km.
     * 1979-09-01  16:35:00 Saturn occultation entry.
     * 1979-09-01  16:35:57 Saturn shadow entry.
     * 1979-09-01  16:51:11 Janus flyby at 228,988 km.
     * 1979-09-01  17:53:32 Saturn occultation exit.
     * 1979-09-01  17:54:47 Saturn shadow exit.
     * 1979-09-01  18:21:59 Ascending ring plane crossing.
     * 1979-09-01  18:25:34 Tethys flyby at 329,197 km.
     * 1979-09-01  18:30:14 Enceladus flyby at 222,027 km.
     * 1979-09-01  20:04:13 Calypso flyby at 109,916 km.
     * 1979-09-01  22:15:27 Rhea flyby at 345,303 km.
     * 1979-09-02  18:00:33 Titan flyby at 362,962 km.
     */
    private GregorianCalendar startPioneerElevenIapetus =
            CalendarUtil.createGregorianCalendar(1979,8,29,0,0,0);
    private GregorianCalendar startPioneerElevenSaturnA =
            CalendarUtil.createGregorianCalendar(1979,8,29,12,0,0);
    private GregorianCalendar startPioneerElevenMimas =
            CalendarUtil.createGregorianCalendar(1979,9,1,15,0,0);
    private GregorianCalendar startPioneerElevenSaturnB =
            CalendarUtil.createGregorianCalendar(1979,9,1,18,0,0);
    private GregorianCalendar startPioneerElevenTitan =
            CalendarUtil.createGregorianCalendar(1979,9,2,12,0,0);
    private GregorianCalendar startPioneerElevenSaturnC =
            CalendarUtil.createGregorianCalendar(1979,9,2,21,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Voyager 1 during flyby of Jupiter
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Voyager_1
     * 1979-03-05  06:54	Amalthea flyby at 420,200 km.
     * 1979-03-05  12:05:26	Jupiter closest approach at 348,890 km from the center of mass.
     * 1979-03-05  15:14	Io flyby at 20,570 km.
     * 1979-03-05  18:19	Europa flyby at 733,760 km.
     * 1979-03-06  02:15	Ganymede flyby at 114,710 km.
     * 1979-03-06  17:08	Callisto flyby at 126,400 km.
     * Note that Amalthea is not simulated.
     */
    private GregorianCalendar startVoyagerOneIo =
            CalendarUtil.createGregorianCalendar(1979,3,5,12,0,0);
    private GregorianCalendar startVoyagerOneEuropa =
            CalendarUtil.createGregorianCalendar(1979,3,5,15,0,0);
    private GregorianCalendar startVoyagerOneGanymede =
            CalendarUtil.createGregorianCalendar(1979,3,5,21,0,0);
    private GregorianCalendar startVoyagerOneCallisto =
            CalendarUtil.createGregorianCalendar(1979,3,6,2,0,0);
    private GregorianCalendar startVoyagerOneJupiter =
            CalendarUtil.createGregorianCalendar(1979,3,6,18,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Voyager 1 during flyby of Saturn
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Voyager_1
     * 1980-11-12  05:41:21  Titan flyby at 6,490 km.
     * 1980-11-12  22:16:32	 Tethys flyby at 415,670 km.
     * 1980-11-12  23:46:30	 Saturn closest approach at 184,300 km from the center of mass.
     * 1980-11-13  01:43:12	 Mimas flyby at 88,440 km.
     * 1980-11-13  01:51:16	 Enceladus flyby at 202,040 km.
     * 1980-11-13  06:21:53	 Rhea flyby at 73,980 km.
     * 1980-11-13  16:44:41	 Hyperion flyby at 880,440 km.
     * Note that Hyperion is not simulated.
     */
    private GregorianCalendar startVoyagerOneTitan =
            CalendarUtil.createGregorianCalendar(1980,11,12,0,0,0);
    private GregorianCalendar startVoyagerOneSaturnAfterTitan =
            CalendarUtil.createGregorianCalendar(1980,11,12,5,0,0);
    private GregorianCalendar startVoyagerOneRhea =
            CalendarUtil.createGregorianCalendar(1980,11,13,3,0,0);
    private GregorianCalendar startVoyagerOneSaturnAfterRhea =
            CalendarUtil.createGregorianCalendar(1980,11,13,7,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Voyager 2 during flyby of Jupiter
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Voyager_2
     * 1979-07-08  12:21  Callisto flyby at 214,930 km.
     * 1979-07-09  07:14  Ganymede flyby at 62,130 km.
     * 1979-07-09  17:53  Europa flyby at 205,720 km.
     * 1979-07-09  20:01  Amalthea flyby at 558,370 km.
     * 1979-07-09  22:29  Jupiter closest approach at 721,670 km from the center of mass.
     * 1979-07-09  23:17  Io flyby at 1,129,900 km.
     * Note that Amalthea is not simulated.
     */
    private GregorianCalendar startVoyagerTwoCallisto =
            CalendarUtil.createGregorianCalendar(1979,7,8,5,0,0);
    private GregorianCalendar startVoyagerTwoGanymede =
            CalendarUtil.createGregorianCalendar(1979,7,8,14,0,0);
    private GregorianCalendar startVoyagerTwoEuropa =
            CalendarUtil.createGregorianCalendar(1979,7,9,9,0,0);
    private GregorianCalendar startVoyagerTwoJupiter =
            CalendarUtil.createGregorianCalendar(1979,7,9,18,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Voyager 2 during flyby of Saturn
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Voyager_2
     * 1981-08-22  01:26:57  Iapetus flyby at 908,680 km.
     * 1981-08-25  01:25:26  Hyperion flyby at 431,370 km.
     * 1981-08-25  09:37:46	 Titan flyby at 666,190 km.
     * 1981-08-26  01:04:32  Dione flyby at 502,310 km.
     * 1981-08-26  02:24:26  Mimas flyby at 309,930 km.
     * 1981-08-26  03:24:05  Saturn closest approach at 161,000 km from the center of mass.
     * 1981-08-26  03:45:16  Enceladus flyby at 87,010 km.
     * 1981-08-26  06:12:30  Tethys flyby at 93,010 km.
     * 1981-08-26  06:28:48  Rhea flyby at 645,260 km.
     * Note that Hyperion is not simulated.
     */
    private GregorianCalendar startVoyagerTwoTitan =
            CalendarUtil.createGregorianCalendar(1981,8,25,0,0,0);
    private GregorianCalendar startVoyagerTwoSaturnAfterTitan =
            CalendarUtil.createGregorianCalendar(1981,8,25,9,0,0);
    private GregorianCalendar startVoyagerTwoTethys =
            CalendarUtil.createGregorianCalendar(1981,8,26,3,0,0);
    private GregorianCalendar startVoyagerTwoSaturnAfterTethys =
            CalendarUtil.createGregorianCalendar(1981,8,26,9,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Voyager 2 during flyby of Uranus
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Voyager_2
     * 1986-01-24  16:50     Miranda flyby at 29,000 km.
     * 1986-01-24  17:25     Ariel flyby at 127,000 km.
     * 1986-01-24  17:25     Umbriel flyby at 325,000 km.
     * 1986-01-24  17:25     Titania flyby at 365,200 km.
     * 1986-01-24  17:25     Oberon flyby at 470,600 km.
     * 1986-01-24  17:59:47  Uranus closest approach at 107,000 km from the center of mass.
     */
    private GregorianCalendar startVoyagerTwoOberon =
            CalendarUtil.createGregorianCalendar(1986,1,24,12,0,0);
    private GregorianCalendar startVoyagerTwoTitania =
            CalendarUtil.createGregorianCalendar(1986,1,24,13,0,0);
    private GregorianCalendar startVoyagerTwoUmbriel =
            CalendarUtil.createGregorianCalendar(1986,1,24,14,0,0);
    private GregorianCalendar startVoyagerTwoAriel =
            CalendarUtil.createGregorianCalendar(1986,1,24,15,0,0);
    private GregorianCalendar startVoyagerTwoMiranda =
            CalendarUtil.createGregorianCalendar(1986,1,24,16,0,0);
    private GregorianCalendar startVoyagerTwoUranus =
            CalendarUtil.createGregorianCalendar(1986,1,24,17,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Voyager 2 during flyby of Neptune
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Voyager_2
     * 1989-08-25  03:56:36  Neptune closest approach at 4,950 km.
     * 1989-08-25  09:23     Triton flyby at 39,800 km.
     */
    private GregorianCalendar startVoyagerTwoTriton =
            CalendarUtil.createGregorianCalendar(1989,8,25,6,0,0);
    private GregorianCalendar startVoyagerTwoNeptune =
            CalendarUtil.createGregorianCalendar(1989,8,25,10,0,0);

    /*
     * Date/times in UTC to control 3D visualization from Cassini during flybys in Saturn System
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Timeline_of_Cassini–Huygens
     * Phoebe                   2004 June 11, 19:33         1,997 km
     * Passage through rings    2004 June 30, (twice)           0 km
     * Orbital insertion        2004 July 1, 02:48	            0 km
     * Enceladus	            2005 July 14, 19:58           175 km
     * Tethys	                2005 September 24, 01:36    1,500 km
     * Hyperion                 2005 September 26, 01:46      500 km
     * Dione                    2005 October 11, 17:59        500 km
     * Titan                    2005 October 28, 04:04      1,451 km
     * Rhea                     2005 November 26, 22:37       500 km
     * Iapetus                  2007 September 10, 12:34    1,227 km
     * Enceladus                2008 October 9, 19:07          25 km
     * Mimas                    2010 February 13            9,500 km
     * Rhea                     2010 March 2, 17:41           101 km
     * Titan                    2010 June 21, 01:27           880 km (below the ionosphere)
     * Dione                    2011 December 12, 09:39        99 km
     * Enceladus                2015 October 28, 15:23         49 km
     * Titan                    2017 April 22, 02:08          979 km
     * Passage within rings     2017 April 26
     * End of mission           2017 September 15, 06:58
     */
    private GregorianCalendar startCassiniPhoebe =
            CalendarUtil.createGregorianCalendar(2004,6,11,18,0,0);
    private GregorianCalendar stopCassiniPhoebe =
            CalendarUtil.createGregorianCalendar(2004,6,11,21,0,0);
    private GregorianCalendar startCassiniPassageThroughRings =
            CalendarUtil.createGregorianCalendar(2004,6,20,14,0,0);
    private GregorianCalendar stopCassiniPassageThroughRings =
            CalendarUtil.createGregorianCalendar(2004,7,3,12,0,0);
    private GregorianCalendar startCassiniEnceladus =
            CalendarUtil.createGregorianCalendar(2005,3,9,4,0,0);
    private GregorianCalendar stopCassiniEnceladus =
            CalendarUtil.createGregorianCalendar(2005,3,9,13,0,0);
    private GregorianCalendar startCassiniSaturnMimasA =
            CalendarUtil.createGregorianCalendar(2005,4,9,21,0,0);
    private GregorianCalendar stopCassiniSaturnMimasA =
            CalendarUtil.createGregorianCalendar(2005,4,15,21,30,0);
    private GregorianCalendar startCassiniSaturnMimasB =
            CalendarUtil.createGregorianCalendar(2005,4,14,22,30,0);
    private GregorianCalendar stopCassiniSaturnMimasB =
            CalendarUtil.createGregorianCalendar(2005,4,15,3,00,0);
    private GregorianCalendar startCassiniSaturnA =
            CalendarUtil.createGregorianCalendar(2005,4,28,20,30,0);
    private GregorianCalendar stopCassiniSaturnA =
            CalendarUtil.createGregorianCalendar(2005,5,3,3,00,0);
    private GregorianCalendar startCassiniEnceladusA =
            CalendarUtil.createGregorianCalendar(2005,7,14,18,0,0);
    private GregorianCalendar stopCassiniEnceladusA =
            CalendarUtil.createGregorianCalendar(2005,7,14,21,0,0);
    private GregorianCalendar startCassiniTethys =
            CalendarUtil.createGregorianCalendar(2005,9,24,0,0,0);
    private GregorianCalendar stopCassiniTethys =
            CalendarUtil.createGregorianCalendar(2005,9,24,4,0,0);
    private GregorianCalendar startCassiniHyperion =
            CalendarUtil.createGregorianCalendar(2005,9,26,0,0,0);
    private GregorianCalendar stopCassiniHyperion =
            CalendarUtil.createGregorianCalendar(2005,9,26,3,0,0);
    private GregorianCalendar startCassiniDioneA =
            CalendarUtil.createGregorianCalendar(2005,10,11,14,0,0);
    private GregorianCalendar stopCassiniDioneA =
            CalendarUtil.createGregorianCalendar(2005,10,11,20,0,0);
    private GregorianCalendar startCassiniTitanA =
            CalendarUtil.createGregorianCalendar(2005,10,27,20,0,0);
    private GregorianCalendar stopCassiniTitanA =
            CalendarUtil.createGregorianCalendar(2005,10,28,6,0,0);
    private GregorianCalendar startCassiniRheaA =
            CalendarUtil.createGregorianCalendar(2005,11,26,9,0,0);
    private GregorianCalendar stopCassiniRheaA =
            CalendarUtil.createGregorianCalendar(2005,11,28,0,0,0);
    private GregorianCalendar startCassiniIapetus =
            CalendarUtil.createGregorianCalendar(2007,9,10,4,0,0);
    private GregorianCalendar stopCassiniIapetus =
            CalendarUtil.createGregorianCalendar(2007,9,11,0,0,0);
    private GregorianCalendar startCassiniEnceladusB =
            CalendarUtil.createGregorianCalendar(2008,10,9,13,0,0);
    private GregorianCalendar stopCassiniEnceladusB =
            CalendarUtil.createGregorianCalendar(2008,10,9,23,0,0);
    private GregorianCalendar startCassiniMimas =
            CalendarUtil.createGregorianCalendar(2010,2,13,0,0,0);
    private GregorianCalendar stopCassiniMimas =
            CalendarUtil.createGregorianCalendar(2010,2,14,3,0,0);
    private GregorianCalendar startCassiniRheaB =
            CalendarUtil.createGregorianCalendar(2010,3,2,8,0,0);
    private GregorianCalendar stopCassiniRheaB =
            CalendarUtil.createGregorianCalendar(2010,3,2,21,0,0);
    private GregorianCalendar startCassiniTitanB =
            CalendarUtil.createGregorianCalendar(2010,6,20,17,0,0);
    private GregorianCalendar stopCassiniTitanB =
            CalendarUtil.createGregorianCalendar(2010,6,21,6,0,0);
    private GregorianCalendar startCassiniDioneB =
            CalendarUtil.createGregorianCalendar(2011,12,12,1,0,0);
    private GregorianCalendar stopCassiniDioneB =
            CalendarUtil.createGregorianCalendar(2011,12,12,14,0,0);
    private GregorianCalendar startCassiniEnceladusC =
            CalendarUtil.createGregorianCalendar(2015,10,28,7,0,0);
    private GregorianCalendar stopCassiniEnceladusC =
            CalendarUtil.createGregorianCalendar(2015,10,28,17,0,0);
    private GregorianCalendar startCassiniTitanC =
            CalendarUtil.createGregorianCalendar(2017,4,21,19,0,0);
    private GregorianCalendar stopCassiniTitanC =
            CalendarUtil.createGregorianCalendar(2017,4,22,16,0,0);
    private GregorianCalendar startCassiniInsideRings =
            CalendarUtil.createGregorianCalendar(2017,4,25,0,0,0);
    private GregorianCalendar stopCassiniInsideRings =
            CalendarUtil.createGregorianCalendar(2017,4,26,10,0,0);
    private GregorianCalendar startCassiniEndOfMission =
            CalendarUtil.createGregorianCalendar(2017,9,14,0,0,0);
    private GregorianCalendar stopCassiniEndOfMission =
            CalendarUtil.createGregorianCalendar(2017,9,15,7,0,0);
    private List<GregorianCalendar> startFlybysCassini;
    private List<GregorianCalendar> stopFlybysCassini;
    private Iterator<GregorianCalendar> startFlybysCassiniIterator;
    private Iterator<GregorianCalendar> stopFlybysCassiniIterator;
    private GregorianCalendar stopCurrentFlybyCassini;
    private List<String> bodyFlybysCassini;
    private Iterator<String> bodyFlybysCassiniIterator;
    private String currentBodyFlybyCassini;

    /*
     * Date/times in UTC to control 3D visualization from New Horizons during flyby of Pluto
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/New_Horizons
     * 2015-07-14  11:49  Pluto fly by
     */
    private GregorianCalendar startNewHorizonsPlutoFlyby =
            CalendarUtil.createGregorianCalendar(2015, 7, 1, 0, 0, 0);
    private GregorianCalendar endNewHorizonsPlutoFlyby =
            CalendarUtil.createGregorianCalendar(2015, 7, 14, 12, 0, 0);

    /*
     * Date/times in UTC to control 3D visualization from Giotto during encounter with
     * Halley's Comet.
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Giotto_(spacecraft)
     * Flyby of Comet Halley
     * Closest approach	14 March 1986
     * Distance	596 km (370 mi)
     */
    private GregorianCalendar startGiottoHalley =
            CalendarUtil.createGregorianCalendar(1986, 3, 1, 0, 0, 0);

    /*
     * Date/times in UTC to control 3D visualization from Rosetta during encounter with 67P
     * January = 1, February = 2, etc
     *
     * https://en.wikipedia.org/wiki/Rosetta_(spacecraft)
     * Rosetta reached 67P/Churyumov–Gerasimenko on 7 May 2014.
     * It performed a series of manoeuvres to enter orbit between then and 6 August 2014.
     */
    private GregorianCalendar startRosetta67P =
            CalendarUtil.createGregorianCalendar(2014, 7, 11, 0, 0, 0);

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

        // Body selector panel with check boxes for each body to be shown
        bodySelectorPanel = new BodySelectorPanel();
        bodySelectorPanel.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                bodySelectorPanel.hide();
            }
        });

        // Create check box for each body of the solar system
        checkBoxesBodies = new HashMap<>();
        createAllCheckBoxes();

        // Create the scene
        Scene scene = createScene();

        // Information panels
        informationPanels = new HashMap<>();

        // 3D visualization
        visualization = new SolarSystemVisualization(solarSystem);
        visualization.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                visualization.hide();
            }
        });

        // Close all panels when primary stage closes
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                for (InformationPanel panel : informationPanels.values()) {
                    panel.close();
                }
                if (bodySelectorPanel != null) {
                    bodySelectorPanel.close();
                }
                if (visualization != null) {
                    visualization.close();
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
        Scene scene = new Scene(root, SCREENWIDTH + 2 * BORDERSIZE + 320, SCREENHEIGHT + 2 * BORDERSIZE);
        root.getChildren().add(grid);

        // For debug purposes
        // Make the grid lines visible
        // grid.setGridLinesVisible(true);

        // Screen to draw trajectories
        screen = new Canvas(SCREENWIDTH, SCREENHEIGHT);
        grid.add(screen, 0, 0, 1, 28);
        // grid.add(screen, 0, 0, 1, 14); // USE FOR VIDEO SMALL RIGHT UPPER CORNER
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

        // Start dates for trajectories of spacecraft
        trajectoryStartDate = new HashMap<>();
        trajectoryStartDate.put("Pioneer 10", CalendarUtil.createGregorianCalendar(1972,3,3,1,49,0));
        trajectoryStartDate.put("Pioneer 11", CalendarUtil.createGregorianCalendar(1973,4,6,2,11,0));
        trajectoryStartDate.put("Mariner 10", CalendarUtil.createGregorianCalendar(1973,11,3,17,45,0));
        trajectoryStartDate.put("Voyager 1", CalendarUtil.createGregorianCalendar(1977,9,5,12,56,0));
        trajectoryStartDate.put("Voyager 2", CalendarUtil.createGregorianCalendar(1977,8,20,14,29,0));
        trajectoryStartDate.put("New Horizons", CalendarUtil.createGregorianCalendar(2006,1,19,19,0,0));
        trajectoryStartDate.put("Giotto", CalendarUtil.createGregorianCalendar(1985, 7, 2, 11, 23, 0));
        trajectoryStartDate.put("Rosetta", CalendarUtil.createGregorianCalendar(2004, 3, 2, 7, 17, 0));
        trajectoryStartDate.put("Apollo 8", CalendarUtil.createGregorianCalendar(1968, 12, 21, 12, 51, 0));
        trajectoryStartDate.put("ISS", CalendarUtil.createGregorianCalendar(1998, 11, 21, 0, 0, 0));
        trajectoryStartDate.put("Galileo", CalendarUtil.createGregorianCalendar(1989, 10, 19, 1, 30, 0));
        trajectoryStartDate.put("Cassini", CalendarUtil.createGregorianCalendar(1997, 10, 15, 11, 30, 0));

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
                automaticSimulationFast = true;
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
                automaticSimulationFast = false;
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
                automaticSimulationFast = false;
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
                automaticSimulationFast = true;
            }
        });
        buttonFastForward.setMinWidth(BUTTONWIDTH);
        grid.add(buttonFastForward, 22, rowIndex, 7, 1);

        // Radio buttons to set simulation method
        // 1. Newton Mechanics
        // 2. General Relativity (PPN)
        // 3. Curvature of Wave Propagation Method
        rowIndex++;
        Label labelSimulationMethod = new Label("Simulation Method:");
        grid.add(labelSimulationMethod,1,rowIndex,20,1);
        radioNewtonMechanics =
                new RadioButton("Newton Mechanics");
        Tooltip tooltipNewtonMechanics =
                new Tooltip("Simulation based on Newton Mechanics is faster");
        radioNewtonMechanics.setTooltip(tooltipNewtonMechanics);
        radioNewtonMechanics.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                solarSystem.setGeneralRelativityFlag(!newValue);
                solarSystem.setCurvatureWavePropagationFlag(!newValue);
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
                solarSystem.setCurvatureWavePropagationFlag(!newValue);
            }
        });
        radioCurvatureWavePropagation =
                new RadioButton("CWPM");
        Tooltip tooltipCurvatureWavePropagation =
                new Tooltip("Curvature of Wave Propagation Method is both fast and accurate");
        radioCurvatureWavePropagation.setTooltip(tooltipCurvatureWavePropagation);
        radioCurvatureWavePropagation.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                solarSystem.setGeneralRelativityFlag(newValue);
                solarSystem.setCurvatureWavePropagationFlag(newValue);
            }
        });
        ToggleGroup simulationMethod = new ToggleGroup();
        radioNewtonMechanics.setToggleGroup(simulationMethod);
        radioGeneralRelativity.setToggleGroup(simulationMethod);
        radioCurvatureWavePropagation.setToggleGroup(simulationMethod);
        radioNewtonMechanics.setSelected(true);
        rowIndex++;
        grid.add(radioNewtonMechanics, 1, rowIndex, 20, 1);
        grid.add(radioGeneralRelativity, 15, rowIndex, 20, 1);
        grid.add(radioCurvatureWavePropagation, 15, rowIndex + 1, 20, 1);

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
                new Tooltip("Check to simulate in single-step mode and advance 60 s or less");
        checkBoxStepMode.setTooltip(toolTipStepMode);
        checkBoxStepMode.setSelected(stepMode);
        grid.add(checkBoxStepMode, 1, rowIndex, 20, 1);

        // Radio buttons to set visualization of ephemeris/simulation results
        // 1. Show ephemeris only
        // 2. Show simulation only
        // 3. Show ephemeris and simulation
        rowIndex++;
        Label labelVisualization = new Label("2D Visualization:");
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
        grid.add(radioSimulationOnly, 15, rowIndex, 20, 1);
        rowIndex++;
        grid.add(radioEphemerisAndSimulation, 1, rowIndex, 20, 1);

        // Check box to select observation from Earth
        rowIndex++;
        checkBoxObservationFromEarth = new CheckBox("Observe from Earth");
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
        grid.add(checkBoxShowRuler, 15, rowIndex, 20, 1);

        // Radio buttons to set view mode for 3D visualization
        // 1. View selected object from the Earth
        // 2. View observed object from position of spacecraft
        rowIndex++;
        Label labelVisualization3D = new Label("3D Visualization:");
        grid.add(labelVisualization3D, 1, rowIndex, 20, 1);
        radioTelescopeView = new RadioButton("Telescope view");
        Tooltip tooltipTelescopeView = new Tooltip("View selected object from the Earth");
        radioTelescopeView.setTooltip(tooltipTelescopeView);
        radioTelescopeView.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    viewMode = SolarSystemViewMode.TELESCOPE;
                }
            }
        });
        radioSpacecraftView = new RadioButton("Spacecraft view");
        Tooltip tooltipFromSpacecraftView = new Tooltip("View observed object from spacecraft");
        radioSpacecraftView.setTooltip(tooltipFromSpacecraftView);
        radioSpacecraftView.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    viewMode = SolarSystemViewMode.FROMSPACECRAFT;
                }
            }
        });
        ToggleGroup visualizationViewMode = new ToggleGroup();
        radioTelescopeView.setToggleGroup(visualizationViewMode);
        radioSpacecraftView.setToggleGroup(visualizationViewMode);
        radioTelescopeView.setSelected(true);
        rowIndex++;
        grid.add(radioTelescopeView, 1, rowIndex, 20, 1);
        grid.add(radioSpacecraftView, 15, rowIndex, 20, 1);

        // Check box to select automatic update of visualization settings
        checkBoxAutomaticView = new CheckBox("Automatic");
        Tooltip tooltipAutomicView = new Tooltip("Set automatic view");
        checkBoxAutomaticView.setTooltip(tooltipAutomicView);
        checkBoxAutomaticView.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                automaticView = newValue;
                if (automaticView) {
                    updateVisualizationSettings();
                }
            }
        });
        checkBoxAutomaticView.setSelected(automaticView);
        rowIndex++;
        grid.add(checkBoxAutomaticView, 1, rowIndex, 20, 1);

        // Slider to set latitude
        rowIndex++;
        labelLatitude = new Label("Latitude");
        grid.add(labelLatitude,1, rowIndex, 20, 1);
        textFieldLatitude = new TextField();
        textFieldLatitude.setMaxWidth(100.0);
        textFieldLatitude.setText(DECIMAL_FORMAT_LATLON.format(latitude));
        textFieldLatitude.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    latitude = Double.parseDouble(textFieldLatitude.getText());
                    latitude = Math.max(-90.0,Math.min(90.0,latitude));
                    textFieldLatitude.setText(DECIMAL_FORMAT_LATLON.format(latitude));
                    sliderLatitude.setValue(latitude);
                }
                catch (Exception e) {
                    latitude = sliderLatitude.getValue();
                    textFieldLatitude.setText(DECIMAL_FORMAT_LATLON.format(latitude));
                }
            }
        });
        grid.add(textFieldLatitude, 20, rowIndex, 20, 1);
        sliderLatitude = new Slider();
        sliderLatitude.setMin(-90);
        sliderLatitude.setMax(90);
        sliderLatitude.setValue(latitude);
        sliderLatitude.setShowTickLabels(true);
        sliderLatitude.setShowTickMarks(true);
        sliderLatitude.setSnapToTicks(false);
        sliderLatitude.setMajorTickUnit(20);
        sliderLatitude.setMinorTickCount(10);
        sliderLatitude.setBlockIncrement(10);
        sliderLatitude.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                latitude = (double) newValue;
                textFieldLatitude.setText(DECIMAL_FORMAT_LATLON.format(latitude));
            }
        });
        rowIndex++;
        grid.add(sliderLatitude, 1, rowIndex, 28,1);

        // Slider to set longitude
        rowIndex++;
        labelLongitude = new Label("Longitude");
        grid.add(labelLongitude,1, rowIndex, 20, 1);
        textFieldLongitude= new TextField();
        textFieldLongitude.setMaxWidth(100.0);
        textFieldLongitude.setText(DECIMAL_FORMAT_LATLON.format(longitude));
        textFieldLongitude.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    longitude = Double.parseDouble(textFieldLongitude.getText());
                    longitude = Math.max(-180.0,Math.min(180.0,longitude));
                    textFieldLongitude.setText(DECIMAL_FORMAT_LATLON.format(longitude));
                    sliderLongitude.setValue(longitude);
                }
                catch (Exception e) {
                    longitude = sliderLongitude.getValue();
                    textFieldLongitude.setText(DECIMAL_FORMAT_LATLON.format(longitude));
                }
            }
        });
        grid.add(textFieldLongitude, 20, rowIndex, 20, 1);
        sliderLongitude = new Slider();
        sliderLongitude.setMin(-180);
        sliderLongitude.setMax(180);
        sliderLongitude.setValue(longitude);
        sliderLongitude.setShowTickLabels(true);
        sliderLongitude.setShowTickMarks(true);
        sliderLongitude.setSnapToTicks(false);
        sliderLongitude.setMajorTickUnit(40);
        sliderLongitude.setMinorTickCount(20);
        sliderLongitude.setBlockIncrement(20);
        sliderLongitude.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                longitude = (double) newValue;
                textFieldLongitude.setText(DECIMAL_FORMAT_LATLON.format(longitude));
            }
        });
        rowIndex++;
        grid.add(sliderLongitude, 1, rowIndex, 28,1);

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
        createCircle("Pluto System", 2, Color.WHITE);
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
        createCircle("26P/Grigg-Skjellerup", 5, Color.WHITESMOKE);
        createCircle("Shoemaker-Levy 9", 5, Color.PINK);
        createCircle("Florence", 3, Color.LIGHTGREEN);
        createCircle("Arrokoth", 3, Color.RED);
        createCircle("Gaspra", 3, Color.LIGHTGRAY);
        createCircle("Ida", 3, Color.LIGHTGOLDENRODYELLOW);
        createCircle("Phobos", 3, Color.BROWN);
        createCircle("Deimos", 3, Color.LIGHTGRAY);
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
        createCircle("Phoebe", 3, Color.CORAL);
        createCircle("Miranda", 3, Color.LIGHTGRAY);
        createCircle("Ariel", 3, Color.ALICEBLUE);
        createCircle("Umbriel", 3, Color.PEACHPUFF);
        createCircle("Titania", 3, Color.LIGHTSALMON);
        createCircle("Oberon", 3, Color.BISQUE);
        createCircle("Triton", 3, Color.LIGHTGRAY);
        createCircle("Nereid", 3, Color.LIGHTSKYBLUE);
        createCircle("Proteus", 3, Color.BISQUE);
        createCircle("Pluto", 3, Color.LIGHTBLUE);
        createCircle("Charon", 3, Color.GRAY);
        createCircle("Nix", 2, Color.LIGHTGRAY);
        createCircle("Hydra", 2, Color.LIGHTCORAL);
        createCircle("Kerberos", 2, Color.LIGHTGOLDENRODYELLOW);
        createCircle("Styx", 2, Color.LIGHTSKYBLUE);
        createCircle("EarthMoonBarycenter", 2, Color.WHITE);
        for (String spacecraftName : spacecraftNames) {
            createCircle(spacecraftName, 3, Color.LIGHTYELLOW);
        }

        // Initialize flags to indicate whether moons are shown
        showMoons = new HashMap<>();
        showMoons.put("Mars", false);
        showMoons.put("Jupiter", false);
        showMoons.put("Saturn", false);
        showMoons.put("Uranus", false);
        showMoons.put("Neptune", false);
        showMoons.put("Pluto System", false);

        // Names of moons per planet
        moons = new HashMap<>();

        // Names of moons of Mars
        List<String> marsMoons = new ArrayList<>();
        marsMoons.add("Phobos");
        marsMoons.add("Deimos");
        moons.put("Mars", marsMoons);

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
        saturnMoons.add("Hyperion");
        saturnMoons.add("Iapetus");
        saturnMoons.add("Phoebe");
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
        neptuneMoons.add("Nereid");
        neptuneMoons.add("Proteus");
        moons.put("Neptune", neptuneMoons);

        // Names of moons of Pluto System
        List<String> plutoSystemMoons = new ArrayList<>();
        plutoSystemMoons.add("Pluto");
        plutoSystemMoons.add("Charon");
        plutoSystemMoons.add("Nix");
        plutoSystemMoons.add("Hydra");
        plutoSystemMoons.add("Kerberos");
        plutoSystemMoons.add("Styx");
        moons.put("Pluto System", plutoSystemMoons);

        // Button to select Solar System bodies
        rowIndex++;
        Button buttonBodySelector = new Button("Select Solar System bodies");
        buttonBodySelector.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bodySelectorPanel.show();
                bodySelectorPanel.toFront();
            }
        });
        buttonBodySelector.setMinWidth(SELECTORWIDTH);
        grid.add(buttonBodySelector, 1, rowIndex, 28, 1);

        // Button to show 3D visualization
        rowIndex++;
        Button buttonVisualization = new Button("3D Visualization");
        buttonVisualization.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                visualization.show();
                visualization.toFront();
            }
        });
        buttonVisualization.setMinWidth(SELECTORWIDTH);
        grid.add(buttonVisualization, 1, rowIndex, 28, 1);

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
     * Create check box for each body of the Solar System.
     */
    private void createAllCheckBoxes() {

        // Define check box for each body of the solar system
        createCheckBox("Sun", "Sun",
                "The Sun is in fact a star and is the largest object in our "
                        + "Solar System.");
        createCheckBox("Mercury", "Mercury",
                "Mercury is the smallest and innermost planet. "
                        + "It orbits around the Sun in 88 days.");
        createCheckBox("Venus", "Venus",
                "Venus is the second planet from the Sun and is of similar size "
                        + " as the Earth.");
        createCheckBox("Earth", "Earth",
                "Earth is the third planet from the Sun and the only "
                        + "planet known to harbor life.");
        createCheckBox("Moon", "Moon",
                "Zoom in to see the moon orbiting around the Earth.");
        createCheckBox("Mars", "Mars",
                "Mars is the second-smallest planet and is also known as the Red Planet.");
        createCheckBox("Jupiter", "Jupiter",
                "Jupiter is the largest planet in the Solar System. "
                        + "Galileo Galilei discovered the four largest moons in 1610.");
        createCheckBox("Saturn", "Saturn",
                "Saturn is the second-largest planet and is famous for his rings.");
        createCheckBox("Uranus", "Uranus",
                "Uranus was discovered in 1781 by William Hershel. "
                        + "Visited by Voyager 2 in 1986.");
        createCheckBox("Neptune", "Neptune",
                "Neptune was discovered in 1846. "
                        + "Visited by Voyager 2 on 25 August 1989.");
        createCheckBox("Pluto System", "Pluto",
                "Pluto was discovered in 1930 and was considered the "
                        + "ninth planet until 2006. Visited by New Horizons on 14 July 2015.");
        createCheckBox("Eris", "Eris",
                "Eris is the most massive and second-largest dwarf planet known "
                        + "in the Solar System.");
        createCheckBox("Chiron", "Chiron",
                "Chiron was discovered in 1977 and orbits between Saturn and Uranus. "
                        + "It is the first object of the Centaur class");
        createCheckBox("Ceres", "Ceres",
                "Ceres is a dwarf planet and the largest object in the asteroid belt.");
        createCheckBox("Pallas", "2 Pallas",
                "Pallas was the second asteroid discovered after Ceres and "
                        + "the third-most-massive asteroid after Vesta");
        createCheckBox("Juno", "3 Juno",
                "Juno was the third asteroid discovered and is the 11th largest asteroid");
        createCheckBox("Vesta", "4 Vesta",
                "Vesta is the second-largest body in the asteroid belt after Ceres");
        createCheckBox("Ida", "243 Ida",
                "Astroid 243 Ida was discovered in 1884 and was visited by the Galileo spacecraft "
                        + "on 28 August 1993");
        createCheckBox("Eros", "433 Eros",
                "Eros is a near-Earth astroid. NASA spacecraft NEAR Shoemaker "
                        + "entered orbit around Eros in 2000, and landed in 2001.");
        createCheckBox("Gaspra", "951 Gaspra",
                "Astroid 951 Gaspra was discovered in 1916 and was visited by the Galileo spacecraft "
                        + "on 29 October 1991");
        createCheckBox("Bennu", "Bennu",
                "Astroid 101955 Bennu was discovered on 11 September 1999. "
                        + "The OSIRIS-REx spacecraft arrived at Bennu on 3 December 2018");
        createCheckBox("Florence", "Florence",
                "Asteroid 3122 Florence approached Earth within 0.047 au on "
                        + "1 September 2017.");
        createCheckBox("Arrokoth", "Arrokoth",
                "Kuiper belt object Arrokoth was visited by New Horizons on "
                        + "1 January 2019.");
        createCheckBox("Halley", "1P/Halley",
                "Halley's Comet has a period of 76 years. Last perihelion 9 Feb 1986. "
                        + "Next perihelion 28 July 2061.");
        createCheckBox("Encke", "2P/Encke",
                "P2/Encke was the first periodic comet discovered after "
                        + "Halley's Comet.");
        createCheckBox("67P/Churyumov-Gerasimenko", "67P/Ch-Ge",
                "67P/Churyumov-Gerasimenko was visited "
                        + "by ESA's Rosetta mission on 6 August 2014.");
        createCheckBox("Hale-Bopp", "Hale-Bopp",
                "Hale-Bopp passed perihelion on 1 April 1997 and "
                        + "was visible to the naked eye for 18 months.");
        createCheckBox("26P/Grigg-Skjellerup", "26P/Grigg-Skjellerup",
                "26P/Grigg-Skjellerup was visited by ESA's Giotto space probe on 10 July 1992.");
        createCheckBox("MarsMoons", "Mars Sys",
                "Mars has two moons, Phobos and Deimos");
        createCheckBox("JupiterMoons", "Jupiter Sys",
                "The four largest moons of Jupiter are the Galilean moons " +
                        "Io, Europa, Ganymede, and Callisto.");
        createCheckBox("SaturnMoons", "Saturn Sys",
                "Saturn moons Mimas, Enceladus, Tethys, Dione, Rhea, Titan, Hyperion, Iapetus, and Phoebe.");
        createCheckBox("UranusMoons", "Uranus Sys",
                "Uranus moons Miranda, Ariel, Umbriel, Titania, and Oberon");
        createCheckBox("NeptuneMoons", "Neptune Sys",
                "Neptune moons Triton, Nereid, and Proteus");
        createCheckBox("Pluto SystemMoons", "Pluto moons",
                "The Pluto System consists of Pluto, Charon, Nix, Hydra, Kerberos, and Styx");
        createCheckBox("EarthMoonBarycenter", "E-M Bary",
                "Earth-Moon barycenter is located on average 4671 km from Earth's center.");
    }

    /**
     * Correction for speed of light when observing from the Earth. The correction is related to
     * the distance between the Earth and the observed Solar System body. In case of Solar eclipse,
     * Mercury transit or Venus transit, correction is related to the nearest body.
     * @return correction [s]
     */
    private double correctionSpeedOfLight() {
        double distance; // m
        Vector3D positionEarth = this.positionBody("Earth");
        if ("Sun".equals(selectedBody)) {
            // Check for Solar eclipse, Venus transit, Mercury transit
            Vector3D positionSun = this.positionBody("Sun");
            Vector3D positionMercury = this.positionBody("Mercury");
            Vector3D positionVenus = this.positionBody("Venus");
            Vector3D positionMoon = this.positionBody("Moon");
            Vector3D positionSunEarth = positionSun.minus(positionEarth);
            distance = positionSun.euclideanDistance(positionEarth);
            if (positionMercury.minus(positionEarth).angleDeg(positionSunEarth) < 0.5) {
                distance = positionMercury.euclideanDistance(positionEarth);
            }
            if (positionVenus.minus(positionEarth).angleDeg(positionSunEarth) < 1.0) {
                distance = positionVenus.euclideanDistance(positionEarth);
            }
            if (positionMoon.minus(positionEarth).angleDeg(positionSunEarth) < 2.0) {
                distance = positionMoon.euclideanDistance(positionEarth);
            }
        }
        else {
            distance = positionSelectedBody().euclideanDistance(positionEarth);
        }
        return distance / SolarSystemParameters.SPEEDOFLIGHT; // s
    }

    /**
     * Current simulation date/time corrected for speed of light when observing from the Earth.
     * @return simulation date/time corrected [GC]
     */
    private GregorianCalendar currentSimulationDateTimeCorrected() {
        GregorianCalendar currentSimulationDateTime = solarSystem.getSimulationDateTime();
        if (observationFromEarth) {
            // Correction for speed of light
            double correction = this.correctionSpeedOfLight();
            currentSimulationDateTime.add(Calendar.SECOND,(int) Math.round(correction));
        }
        return currentSimulationDateTime;
    }

    /**
     * Update current simulation date/time in time selector.
     * When observing from the surface of the Earth, displayed date/time is
     * corrected for speed of light depending on the distance between the Earth
     * and the selected Solar System object.
     */
    private void updateDateTimeSelector() {
        dateTimeSelector.setDateTime(this.currentSimulationDateTimeCorrected());
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
            GregorianCalendar dateTime = dateTimeSelector.getDateTime();
            if (observationFromEarth) {
                double correction = correctionSpeedOfLight();
                dateTime.add(Calendar.SECOND,(int) Math.round(-correction));
            }
            solarSystem.initializeSimulation(dateTime);
            updateDateTimeSelector();
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
     * Start simulation in fast forward mode.
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
        CheckBox checkBox = bodySelectorPanel.createCheckBox(label,toolTipText);
        checkBoxesBodies.put(name,checkBox);
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                boolean isSelected = !oldValue && newValue;
                try {
                    monitor.startDrawing();
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
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    stop();
                }
                finally {
                    monitor.stopDrawing();
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
        return checkBox;
    }

    /**
     * Compute x-position on the screen for 3D position in the Solar System.
     * @param position 3D position in m
     * @return x-position in pixels
     */
    private double screenX(Vector3D position) {
        // USE SCREENHEIGHT FOR VIDEO
        // return translateX + SCREENWIDTH * (position.getX() / SCREENSCALE);
        return translateX + SCREENHEIGHT * (position.getX() / SCREENSCALE);
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
     * Determine the position of body with given name.
     * @return position of selected body [m]
     */
    private Vector3D positionBody(String name) {
        if (showSimulation) {
            Particle particle = solarSystem.getParticle(name);
            if (particle != null) {
                return particle.getPosition();
            }
        }
        else {
            SolarSystemBody body = solarSystem.getBody(name);
            if (body != null) {
                return body.getPosition();
            }
        }
        return new Vector3D();
    }

    /**
     * Determine the position of the selected body.
     * Return zero vector when no body is selected.
     * @return position of selected body [m]
     */
    private Vector3D positionSelectedBody() {
        if (selectedBody != null) {
            return positionBody(selectedBody);
        }
        selectedBody = null;
        return new Vector3D();
    }

    /**
     * Determine the position of the Earth.
     * @return position of the Earth [m]
     */
    private Vector3D positionEarth() {
        return positionBody("Earth");
    }

    /**
     * Translate and rotate position for observation from the surface of the Earth.
     * Perspective is taken into account.
     * @param position 3D position in m
     * @return translated and rotated position
     */
    private Vector3D observationFromEarthView(Vector3D position) {

        // Correct for speed of light
        GregorianCalendar currentSimulationDateTimeCorrected = currentSimulationDateTimeCorrected();

        // Viewing position of camera is determined by latitude and longitude, height = 0
        Vector3D geocentricPosition =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 0.0,
                        currentSimulationDateTimeCorrected);
        Vector3D viewingPosition = positionEarth().plus(geocentricPosition);


        // Viewing direction of camera
        Vector3D viewingDirection = positionSelectedBody().minus(viewingPosition);

        // Set up local camera frame
        Vector3D geocentricPositionHigh =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 10000.0,
                        currentSimulationDateTimeCorrected);
        Vector3D normalVector = geocentricPositionHigh.minus(geocentricPosition).normalize();
        Vector3D sideVector = new Vector3D(viewingDirection.crossProduct(normalVector).normalize());
        Vector3D cameraUp = new Vector3D(sideVector.crossProduct(viewingDirection).normalize());

        // Camera coordinates
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D xc = sideVector;
        Vector3D yc = cameraUp;
        Vector3D zc = (viewingDirection.scalarProduct(-1.0)).normalize();

        // Translate
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D positionTranslated = position.minus(viewingPosition);

        // Rotate
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D positionRotated = positionTranslated.rotate(xc,yc,zc);

        // Take perspective into account
        // https://www.cse.unr.edu/~bebis/CS791E/Notes/PerspectiveProjection.pdf
        // positionRotated is defined in camera frame units, with z-axis negative for viewing direction
        // to be consistent with basic CG theory with focal distance f = 1.
        double distance = Math.abs(positionRotated.getZ());
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

        // USE FOR VIDEO NEW HORIZONS
        // if ("Pluto".equals(observedBody) || "Charon".equals(observedBody)) {
        //     zoom *= 1.5;
        //}

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
                    GraphicsContext gc = screen.getGraphicsContext2D();
                    gc.setFill(Color.BLACK);
                    gc.fillOval(shadowPosx - radius, shadowPosy - radius, 2 * radius, 2 * radius);
                    gc.fillText(body.getName(),shadowPosx + 0.5*radius,shadowPosy - radius);
                }
            }
        }

        // Draw ring elements of Saturn or Uranus behind the planet
        if (("Saturn".equals(body.getName()) || "Uranus".equals(body.getName()))
                && radius > circle.getRadius()) {
            drawRings(body.getName(), position, false);
        }

        // Draw circle on screen using color and radius from Circle-object
        GraphicsContext gc = screen.getGraphicsContext2D();
        if (!showMoons.get("Pluto System") || !"Pluto System".equals(body.getName())) {
            gc.setFill(circle.getFill());
            gc.fillOval(posx - radius, posy - radius, 2 * radius, 2 * radius);
        }

        // Draw ring elements of Saturn or Uranus in front of the planet
        if (("Saturn".equals(body.getName()) || "Uranus".equals(body.getName()))
                && radius > circle.getRadius()) {
            drawRings(body.getName(), position, true);
        }

        // Draw name on screen using color from Circle-object
        gc.setFill(circle.getFill());
        String label;
        if (observationFromEarth && showRuler) {
            GregorianCalendar currentSimulationDateTimeCorrected = this.currentSimulationDateTimeCorrected();
            double[] result =
                    EphemerisUtil.computeAzimuthElevationDistance(position,latitude,longitude,currentSimulationDateTimeCorrected);
            double azimuth = result[0];
            double elevation = result[1];
            StringBuilder sb = new StringBuilder(body.getName());
            sb.append(" (azi ");
            sb.append(DECIMAL_FORMAT.format(azimuth));
            sb.append(" ele ");
            sb.append(DECIMAL_FORMAT.format(elevation));
            sb.append(")");
            label = sb.toString();
        }
        else {
            label = body.getName();
        }
        if (!showMoons.get("Pluto System") || !"Pluto System".equals(body.getName())) {
            gc.fillText(label, posx + 0.5 * radius, posy - radius);
        }
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
                // Draw circle at position of body
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
        double muCenterBody;
        if ("Pluto System".equals(centerBodyName)) {
            /*
             * In the Pluto System, all particles orbit around the barycenter which is
             * located in between Pluto and Charon, outside of Pluto.
             * To visualise the orbit of a particle of the Pluto System, the gravitational
             * parameter is estimated under the assumption that the orbit is circular.
             * For circular orbits, the gravitational parameter mu can be obtained from
             * v = sqrt(mu/r),
             * where v is the velocity relative to the barycenter and r the distance to
             * the barycenter
             * https://en.wikipedia.org/wiki/Circular_orbit
             */
            double v = velocityRelativeToCenterBody.magnitude();
            double r = positionRelativeToCenterBody.magnitude();
            muCenterBody = v * v * r;
        }
        else {
            muCenterBody = solarSystem.getParticle(centerBodyName).getMu();
        }
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
        while (scaleLength > 600.0) { // USE 400.0 FOR VIDEO SMALL RIGHT UPPER CORNER
            scaleLength /= 10;
            scale /= 10;
        }
        double x = 50.0;  // USE 10.0 FOR VIDEO SMALL RIGHT UPPER CORNER
        double y = SCREENHEIGHT - 40.0;
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

        /* VIDEO: USE CODE BELOW TO SHOW DATE IN RIGHT LOWER CORNER */
        gc.setStroke(Color.LIGHTYELLOW);
        gc.setFill(Color.LIGHTYELLOW);
        gc.setFont(new Font("Arial", 16));
        x = SCREENWIDTH - 150.0;
        y = SCREENHEIGHT - 40.0;
        String textDate = CalendarUtil.calendarToString(solarSystem.getSimulationDateTime());
        String displayDate = textDate.substring(0,textDate.length() - 13); // Date only
        //String displayDate = textDate.substring(0,textDate.length() - 4); // Date + time
        x = x - 50.0; // Date + time
        gc.fillText(displayDate, x, y);
        gc.setFont(new Font("Arial", 13));
        /* */
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
                if (viewMode.equals(SolarSystemViewMode.FROMSPACECRAFT) && !spacecraftNames.contains(bodyName)) {
                    observedBody = bodyName;
                }
                else {
                    selectedBody = bodyName;
                }
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

        // 3D visualization
        if (visualization.isShowing()) {
            visualization.update(bodiesShown, selectedBody, observedBody, viewMode, latitude, longitude);
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

        // Draw stars STARS
        //drawStars();

        // Draw rulers
        if (showRuler && !observationFromEarth) {
            drawRulerDistance();
        }
        if (showRuler && observationFromEarth) {
            drawRulerAngularDiameter();
            //drawEcliptic();
        }
    }

    /**
     * Update time step in seconds for simulation step mode.
     */
    private void updateStepModeTimeStep() {
        long now = System.nanoTime();
        long elapsedTimeNanoSeconds = now - lastUpdateStepModeTimeStep;
        double elapsedTimeMilliSeconds = Math.round(elapsedTimeNanoSeconds/1.0E6);
        double elapsedTimeSeconds = elapsedTimeMilliSeconds/1.0E3;
        elapsedTimeSeconds = Math.min(1.0,elapsedTimeSeconds);
        lastUpdateStepModeTimeStep = now;
        if (sliderSimulationSpeed.getValue() < 1.0) {
            // Real-time simulation
            stepModeTimeStep = elapsedTimeSeconds;
        }
        else {
            // Faster than real-time simulation
            // stepModeTimeStep = Math.round(0.01 * Math.exp(0.08 * sliderSimulationSpeed.getValue()));
            stepModeTimeStep = sliderSimulationSpeed.getValue()*0.3;
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
        if (simulationIsRunningFast || simulationIsRunningStepMode) {
            period = 1;
        }
        else {
            period = 1 + (20 - (int) (sliderSimulationSpeed.getValue() / 5.0));
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
     * Update visualization settings.
     */
    private void updateVisualizationSettings() {
        if (viewMode.equals(SolarSystemViewMode.TELESCOPE)) {
            updateVisualizationSettingsTelescopeView();
        }
        else {
            updateVisualizationSettingsSpacecraftView();
        }
    }

    /**
     * Update visualization settings for telescope view.
     */
    private void updateVisualizationSettingsTelescopeView() {
        VisualizationSettings currentSettings = (VisualizationSettings) eventSelector.getValue();
        if (currentSettings.getEventName().contains("Shoemaker-Levy") && simulationIsRunning) {
            Vector3D shoemakerLevyPosition = solarSystem.getParticle("Shoemaker-Levy 9").getPosition();
            Vector3D jupiterPosition = solarSystem.getParticle("Jupiter").getPosition();
            double distance = shoemakerLevyPosition.euclideanDistance(jupiterPosition);
            double value = Math.min(1.0E10,distance)/1.0E08;
            if (value > 10.0) {
                checkBoxStepMode.setSelected(false);
                startSimulationForward();
                sliderSimulationSpeed.setValue(value / 2.0);
                sliderZoomView.setValue(Math.max(62.0,75.0 - value/4.0));
            }
            else {
                checkBoxStepMode.setSelected(true);
                startSimulationStepModeForward();
                sliderSimulationSpeed.setValue(3.0*value);
                sliderZoomView.setValue(Math.max(62.0,75.0 - value/4.0));
                if (distance < 0.49*SolarSystemParameters.getInstance().getDiameter("Jupiter")) {
                    pauseSimulation();
                }
                if (distance < 0.48*SolarSystemParameters.getInstance().getDiameter("Jupiter")) {
                    bodiesShown.remove("Shoemaker-Levy 9");
                }
            }
        }
        if (currentSettings.getEventName().startsWith("Launch") && simulationIsRunning) {
            Vector3D spacecraftPosition = solarSystem.getParticle(selectedBody).getPosition();
            Vector3D closestBodyPosition = new Vector3D();
            double closestBodyDiameter = 0.0;
            String closestBodyFound = "";
            double minDistance = Double.MAX_VALUE;
            for (String bodyName : bodiesShown) {
                Vector3D bodyPosition = null;
                double bodyDiameter = 0.0;
                try {
                    bodyPosition = solarSystem.getPosition(bodyName);
                    if (!spacecraftNames.contains(bodyName)) {
                        bodyDiameter = SolarSystemParameters.getInstance().getDiameter(bodyName);
                    }
                } catch (SolarSystemException ex) {
                    showMessage("Error",ex.getMessage());
                }
                double distanceFromCenter = spacecraftPosition.euclideanDistance(bodyPosition);
                double distanceFromSurface = distanceFromCenter - 0.5*bodyDiameter;
                if (distanceFromCenter > 1000.0 && distanceFromSurface < minDistance) {
                    minDistance = distanceFromSurface;
                    closestBodyPosition = new Vector3D(bodyPosition);
                    closestBodyDiameter = bodyDiameter;
                    closestBodyFound = bodyName;
                }
            }
            if (!"".equals(closestBodyFound)) {
                closestBody = closestBodyFound;
            }
            observedBody = closestBody;
            if (minDistance < 1.0E10) {
                double angle;
                if ("Earth".equals(observedBody)) {
                    angle = spacecraftPosition.angleDeg(positionEarth());
                }
                else {
                    angle = spacecraftPosition.minus(positionEarth()).angleDeg(closestBodyPosition.minus(positionEarth()));
                }
                if (((angle < closestBodyDiameter/5.0E09) || "Jupiter".equals(closestBody)) &&
                        minDistance < 2.5E9) {
                    checkBoxStepMode.setSelected(true);
                    startSimulationStepModeForward();
                    double value = Math.min(100.0, Math.max(5.0, minDistance / 1.0E07));
                    sliderZoomView.setValue(95.0 - 0.3 * value);
                    sliderSimulationSpeed.setValue(Math.max(15.0,Math.min(100.0,value)));
                } else {
                    checkBoxStepMode.setSelected(false);
                    startSimulationForward();
                    double value = Math.min(100.0, Math.max(0.0, (minDistance - 1.0E09) / 7.0E07));
                    sliderZoomView.setValue(65.0 - 0.3 * value);
                    sliderSimulationSpeed.setValue(value);
                }
            } else {
                checkBoxStepMode.setSelected(false);
                startSimulationFastForward();
                sliderZoomView.setValue(20.0);
                sliderSimulationSpeed.setValue(100.0);
            }
            if (currentSettings.getEventName().contains("Apollo 8")) {
                if (solarSystem.getSimulationDateTime().after(entryTrajectInitApolloEight)) {
                    bodiesShown.remove("Apollo 8");
                    selectedBody = "Earth";
                    pauseSimulation();
                }
            }
        }
    }

    /**
     * Update visualization settings for spacecraft view.
     */
    private void updateVisualizationSettingsSpacecraftView() {
        VisualizationSettings currentSettings = (VisualizationSettings) eventSelector.getValue();
        // Observation of Shoemaker-Levy 9 impact from Galileo spacecraft
        if (currentSettings.getEventName().contains("Shoemaker-Levy")) {
            updateVisualizationSettingsTelescopeView();
            return;
        }
        if (currentSettings.getEventName().startsWith("Launch") && simulationIsRunning) {
            Vector3D spacecraftPosition = solarSystem.getParticle(selectedBody).getPosition();
            Vector3D closestBodyPosition = new Vector3D();
            String closestBodyFound = "";
            double minDistance = Double.MAX_VALUE;
            for (String bodyName : currentSettings.getBodiesShown()) {
                Vector3D bodyPosition = null;
                try {
                    bodyPosition = solarSystem.getPosition(bodyName);
                } catch (SolarSystemException ex) {
                    showMessage("Error",ex.getMessage());
                }
                double distance = spacecraftPosition.euclideanDistance(bodyPosition);
                if (distance > 1000.0 && distance < minDistance) {
                    minDistance = distance;
                    closestBodyPosition = new Vector3D(bodyPosition);
                    closestBodyFound = bodyName;
                }
            }
            if (!closestBody.equals(closestBodyFound)) {
                if ("Jupiter".equals(closestBody) || "Saturn".equals(closestBody) ||
                        "Uranus".equals(closestBody) || "Neptune".equals(closestBody)) {
                    checkBoxesBodies.get(closestBody + "Moons").setSelected(false);
                }
                closestBody = closestBodyFound;
            }
            observedBody = closestBody;
            if (minDistance < 8.0E09 ||
                    (minDistance < 1.5E10 && spacecraftPosition.magnitude() < closestBodyPosition.magnitude()) ||
                    (selectedBody.startsWith("Pioneer") && minDistance < 5.0E10)) {
                if ("Pioneer 10".equals(selectedBody)) {
                    if ("Jupiter".equals(closestBody)) {
                        checkBoxesBodies.get("JupiterMoons").setSelected(true);
                        /* https://en.wikipedia.org/wiki/Pioneer_10
                         * 1973-12-03  12:26:00 Callisto flyby at 1,392,300 km
                         * 1973-12-03  13:56:00 Ganymede flyby at 446,250 km
                         * 1973-12-03  19:26:00 Europa flyby at 321,000 km
                         * 1973-12-03  22:56:00 Io flyby at 357,000 km
                         * 1973-12-04  02:26:00 Jupiter closest approach at 200,000 km
                         * 1973-12-04  02:36:00 Jupiter equator plane crossing
                         */
                        if (solarSystem.getSimulationDateTime().after(startPioneerTenCallisto)) {
                            observedBody = "Callisto";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerTenGanymede)) {
                            observedBody = "Ganymede";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerTenEuropa)) {
                            observedBody = "Europa";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerTenIo)) {
                            observedBody = "Io";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerTenJupiter)) {
                            observedBody = "Jupiter";
                        }
                    }
                    try {
                        Vector3D observedBodyPosition = solarSystem.getPosition(observedBody);
                        minDistance = spacecraftPosition.euclideanDistance(observedBodyPosition);
                    } catch (SolarSystemException ex) {
                        showMessage("Error",ex.getMessage());
                    }
                }
                if ("Pioneer 11".equals(selectedBody)) {
                    if ("Jupiter".equals(closestBody)) {
                        checkBoxesBodies.get("JupiterMoons").setSelected(true);
                        /* https://en.wikipedia.org/wiki/Pioneer_11
                         * 1974-12-02  08:21:00 Callisto flyby at 786,500 km.
                         * 1974-12-02  22:09:00 Ganymede flyby at 692,300 km.
                         * 1974-12-03  03:11:00 Io flyby at 314,000 km.
                         * 1974-12-03  04:15:00 Europa flyby at 586,700 km.
                         * 1974-12-03  05:21:19 Jupiter closest approach at 42,828 km.
                         * 1974-12-03  22:29:00 Amalthea flyby at 127,500 km.
                         * Note that Amalthea is not simulated.
                         */
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenCallisto)) {
                            observedBody = "Callisto";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenGanymede)) {
                            observedBody = "Ganymede";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenIo)) {
                            observedBody = "Io";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenEuropa)) {
                            observedBody = "Europa";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenJupiter)) {
                            observedBody = "Jupiter";
                        }
                    }
                    if ("Saturn".equals(closestBody)) {
                        checkBoxesBodies.get("SaturnMoons").setSelected(true);
                        /* https://en.wikipedia.org/wiki/Pioneer_11
                         * 1979-08-29  06:06:10 Iapetus flyby at 1,032,535 km.
                         * 1979-08-29  11:53:33 Phoebe flyby at 13,713,574 km.
                         * 1979-08-31  12:32:33 Hyperion flyby at 666,153 km.
                         * 1979-09-01  14:26:56 Descending ring plane crossing.
                         * 1979-09-01  14:50:55 Epimetheus flyby at 6,676 km.
                         * 1979-09-01  15:06:32 Atlas flyby at 45,960 km.
                         * 1979-09-01  15:59:30 Dione flyby at 291,556 km.
                         * 1979-09-01  16:26:28 Mimas flyby at 104,263 km.
                         * 1979-09-01  16:29:34 Saturn closest approach at 20,591 km.
                         * 1979-09-01  16:35:00 Saturn occultation entry.
                         * 1979-09-01  16:35:57 Saturn shadow entry.
                         * 1979-09-01  16:51:11 Janus flyby at 228,988 km.
                         * 1979-09-01  17:53:32 Saturn occultation exit.
                         * 1979-09-01  17:54:47 Saturn shadow exit.
                         * 1979-09-01  18:21:59 Ascending ring plane crossing.
                         * 1979-09-01  18:25:34 Tethys flyby at 329,197 km.
                         * 1979-09-01  18:30:14 Enceladus flyby at 222,027 km.
                         * 1979-09-01  20:04:13 Calypso flyby at 109,916 km.
                         * 1979-09-01  22:15:27 Rhea flyby at 345,303 km.
                         * 1979-09-02  18:00:33 Titan flyby at 362,962 km.
                         */
                        /*
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenIapetus)) {
                            observedBody = "Iapetus";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenSaturnA)) {
                            observedBody = "Saturn";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenMimas)) {
                            observedBody = "Mimas";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenSaturnB)) {
                            observedBody = "Saturn";
                        }
                        */
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenTitan)) {
                            observedBody = "Titan";
                        }
                        if (solarSystem.getSimulationDateTime().after(startPioneerElevenSaturnC)) {
                            observedBody = "Saturn";
                        }
                    }
                    try {
                        Vector3D observedBodyPosition = solarSystem.getPosition(observedBody);
                        minDistance = spacecraftPosition.euclideanDistance(observedBodyPosition);
                    } catch (SolarSystemException ex) {
                        showMessage("Error",ex.getMessage());
                    }
                }
                if ("Mariner 10".equals(selectedBody)) {
                    if (minDistance < 2.0E10 && ("Mercury".equals(observedBody) ||
                            "Venus".equals(observedBody))) {
                        checkBoxStepMode.setSelected(true);
                        startSimulationStepModeForward();
                        double value = Math.min(100.0, Math.max(5.0, minDistance / 2.0E06));
                        sliderZoomView.setValue(95.0 - 0.3 * value);
                        if ("Mercury".equals(observedBody)) {
                            sliderSimulationSpeed.setValue(value*0.1);
                        }
                        else {
                            sliderSimulationSpeed.setValue(value);
                        }
                    } else {
                        checkBoxStepMode.setSelected(false);
                        startSimulationForward();
                        double value = Math.min(100.0, Math.max(0.0, (minDistance - 2.0E08) / 7.0E06));
                        sliderZoomView.setValue(65.0 - 0.3 * value);
                        sliderSimulationSpeed.setValue(value);
                    }
                }
                if ("Voyager 1".equals(selectedBody)) {
                    if ("Jupiter".equals(closestBody)) {
                        checkBoxesBodies.get("JupiterMoons").setSelected(true);
                        /*
                         * https://en.wikipedia.org/wiki/Voyager_1
                         * 1979-03-05  06:54	Amalthea flyby at 420,200 km.
                         * 1979-03-05  12:05:26	Jupiter closest approach at 348,890 km from the center of mass.
                         * 1979-03-05  15:14	Io flyby at 20,570 km.
                         * 1979-03-05  18:19	Europa flyby at 733,760 km.
                         * 1979-03-06  02:15	Ganymede flyby at 114,710 km.
                         * 1979-03-06  17:08	Callisto flyby at 126,400 km.
                         * Note that Amalthea is not simulated.
                         */
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneIo)) {
                            observedBody = "Io";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneEuropa)) {
                            observedBody = "Europa";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneGanymede)) {
                            observedBody = "Ganymede";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneCallisto)) {
                            observedBody = "Callisto";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneJupiter)) {
                            observedBody = "Jupiter";
                        }
                    }
                    if ("Saturn".equals(closestBody)) {
                        checkBoxesBodies.get("SaturnMoons").setSelected(true);
                        /*
                         * https://en.wikipedia.org/wiki/Voyager_1
                         * 1980-11-12  05:41:21  Titan flyby at 6,490 km.
                         * 1980-11-12  22:16:32	 Tethys flyby at 415,670 km.
                         * 1980-11-12  23:46:30	 Saturn closest approach at 184,300 km from the center of mass.
                         * 1980-11-13  01:43:12	 Mimas flyby at 88,440 km.
                         * 1980-11-13  01:51:16	 Enceladus flyby at 202,040 km.
                         * 1980-11-13  06:21:53	 Rhea flyby at 73,980 km.
                         * 1980-11-13  16:44:41	 Hyperion flyby at 880,440 km.
                         * Note that Hyperion is not simulated.
                         */
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneTitan)) {
                            observedBody = "Titan";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneSaturnAfterTitan)) {
                            observedBody = "Saturn";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneRhea)) {
                            observedBody = "Rhea";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerOneSaturnAfterRhea)) {
                            observedBody = "Saturn";
                        }
                    }
                    try {
                        Vector3D observedBodyPosition = solarSystem.getPosition(observedBody);
                        minDistance = spacecraftPosition.euclideanDistance(observedBodyPosition);
                    } catch (SolarSystemException ex) {
                        showMessage("Error",ex.getMessage());
                    }
                }
                if ("Voyager 2".equals(selectedBody)) {
                    if ("Jupiter".equals(closestBody)) {
                        checkBoxesBodies.get("JupiterMoons").setSelected(true);
                        /*
                         * https://en.wikipedia.org/wiki/Voyager_2
                         * 1979-07-08  12:21  Callisto flyby at 214,930 km.
                         * 1979-07-09  07:14  Ganymede flyby at 62,130 km.
                         * 1979-07-09  17:53  Europa flyby at 205,720 km.
                         * 1979-07-09  20:01  Amalthea flyby at 558,370 km.
                         * 1979-07-09  22:29  Jupiter closest approach at 721,670 km from the center of mass.
                         * 1979-07-09  23:17  Io flyby at 1,129,900 km.
                         * Note that Amalthea is not simulated.
                         */
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoCallisto)) {
                            observedBody = "Callisto";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoGanymede)) {
                            observedBody = "Ganymede";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoEuropa)) {
                            observedBody = "Europa";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoJupiter)) {
                            observedBody = "Jupiter";
                        }
                    }
                    if ("Saturn".equals(closestBody)) {
                        checkBoxesBodies.get("SaturnMoons").setSelected(true);
                        /*
                         * https://en.wikipedia.org/wiki/Voyager_2
                         * 1981-08-22  01:26:57  Iapetus flyby at 908,680 km.
                         * 1981-08-25  01:25:26  Hyperion flyby at 431,370 km.
                         * 1981-08-25  09:37:46	 Titan flyby at 666,190 km.
                         * 1981-08-26  01:04:32  Dione flyby at 502,310 km.
                         * 1981-08-26  02:24:26  Mimas flyby at 309,930 km.
                         * 1981-08-26  03:24:05  Saturn closest approach at 161,000 km from the center of mass.
                         * 1981-08-26  03:45:16  Enceladus flyby at 87,010 km.
                         * 1981-08-26  06:12:30  Tethys flyby at 93,010 km.
                         * 1981-08-26  06:28:48  Rhea flyby at 645,260 km.
                         * Note that Hyperion is not simulated.
                         */
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoTitan)) {
                            observedBody = "Titan";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoSaturnAfterTitan)) {
                            observedBody = "Saturn";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoTethys)) {
                            observedBody = "Tethys";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoSaturnAfterTethys)) {
                            observedBody = "Saturn";
                        }
                    }
                    if ("Uranus".equals(closestBody)) {
                        checkBoxesBodies.get("UranusMoons").setSelected(true);
                        /*
                         * https://en.wikipedia.org/wiki/Voyager_2
                         * 1986-01-24  16:50     Miranda flyby at 29,000 km.
                         * 1986-01-24  17:25     Ariel flyby at 127,000 km.
                         * 1986-01-24  17:25     Umbriel flyby at 325,000 km.
                         * 1986-01-24  17:25     Titania flyby at 365,200 km.
                         * 1986-01-24  17:25     Oberon flyby at 470,600 km.
                         * 1986-01-24  17:59:47  Uranus closest approach at 107,000 km from the center of mass.
                         */
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoOberon)) {
                            observedBody = "Oberon";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoTitania)) {
                            observedBody = "Titania";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoUmbriel)) {
                            observedBody = "Umbriel";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoAriel)) {
                            observedBody = "Ariel";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoMiranda)) {
                            observedBody = "Miranda";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoUranus)) {
                            observedBody = "Uranus";
                        }
                    }
                    if ("Neptune".equals(closestBody)) {
                        checkBoxesBodies.get("NeptuneMoons").setSelected(true);
                        /*
                         * https://en.wikipedia.org/wiki/Voyager_2
                         * 1989-08-25  03:56:36  Neptune closest approach at 4,950 km.
                         * 1989-08-25  09:23     Triton flyby at 39,800 km.
                         */
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoTriton)) {
                            observedBody = "Triton";
                        }
                        if (solarSystem.getSimulationDateTime().after(startVoyagerTwoNeptune)) {
                            observedBody = "Neptune";
                        }
                    }
                    try {
                        Vector3D observedBodyPosition = solarSystem.getPosition(observedBody);
                        minDistance = spacecraftPosition.euclideanDistance(observedBodyPosition);
                    } catch (SolarSystemException ex) {
                        showMessage("Error",ex.getMessage());
                    }
                }
                if ("New Horizons".equals(selectedBody)) {
                    /*
                     * https://en.wikipedia.org/wiki/New_Horizons
                     * 2015-07-14  11:49  Pluto fly by
                     */
                    if (solarSystem.getSimulationDateTime().after(startNewHorizonsPlutoFlyby) &&
                            solarSystem.getSimulationDateTime().before(endNewHorizonsPlutoFlyby)) {
                        checkBoxesBodies.get("Pluto SystemMoons").setSelected(true);
                        observedBody = "Pluto";
                        // observedBody = "Charon";
                        sliderZoomView.setValue(100.0);
                    }
                    else {
                        checkBoxesBodies.get("Pluto SystemMoons").setSelected(false);
                    }
                    if (solarSystem.getSimulationDateTime().after(endNewHorizonsPlutoFlyby)) {
                        observedBody = "Arrokoth";
                    }
                    try {
                        Vector3D observedBodyPosition = solarSystem.getPosition(observedBody);
                        minDistance = spacecraftPosition.euclideanDistance(observedBodyPosition);
                    } catch (SolarSystemException ex) {
                        showMessage("Error",ex.getMessage());
                    }
                }
                if ("Giotto".equals(selectedBody)) {
                    if (minDistance < 2.0E10 || solarSystem.getSimulationDateTime().after(startGiottoHalley)) {
                        checkBoxStepMode.setSelected(true);
                        startSimulationStepModeForward();
                        double value = Math.min(100.0, Math.max(5.0, minDistance / 2.0E06));
                        sliderZoomView.setValue(95.0 - 0.3 * value);
                        if ("Halley".equals(observedBody)) {
                            sliderSimulationSpeed.setValue(value*0.1);
                        }
                        else {
                            sliderSimulationSpeed.setValue(100);
                        }
                    } else {
                        checkBoxStepMode.setSelected(false);
                        startSimulationForward();
                        double value = Math.min(100.0, Math.max(0.0, (minDistance - 2.0E08) / 7.0E06));
                        sliderZoomView.setValue(65.0 - 0.3 * value);
                        sliderSimulationSpeed.setValue(value);
                    }
                }
                if ("Galileo".equals(selectedBody)) {
                    if ("Jupiter".equals(closestBody)) {
                        checkBoxesBodies.get("JupiterMoons").setSelected(true);
                        String closestMoonFound = "";
                        double minMoonDistance = Double.MAX_VALUE;
                        for (String moonName : SolarSystemParameters.getInstance().getMoonsOfPlanet("Jupiter")) {
                            Vector3D moonPosition = null;
                            try {
                                moonPosition = solarSystem.getPosition(moonName);
                            } catch (SolarSystemException ex) {
                                showMessage("Error",ex.getMessage());
                            }
                            double moonDistance = spacecraftPosition.euclideanDistance(moonPosition);
                            if (moonDistance < minMoonDistance) {
                                minMoonDistance = moonDistance;
                                closestMoonFound = moonName;
                            }
                        }
                        if (minMoonDistance < 1.5E8) {
                            observedBody = closestMoonFound;
                            // Minimum distance to Jupiter moon varies between 200 and 3000 km
                            // At such low distances, simulation progresses slowly
                            // To encounter this problem, minDistance is increased with 20,000 km (= 2.0E07 m)
                            minDistance = 1.5*minMoonDistance + 2.0E07;
                        }
                    }
                }
                if ("Cassini".equals(selectedBody)) {
                    if ("Saturn".equals(closestBody)) {
                        checkBoxesBodies.get("SaturnMoons").setSelected(true);
                        if (startFlybysCassini == null && solarSystem.getSimulationDateTime().after(startCassiniPhoebe)) {
                            initializeFlybysCassini();
                            startFlybysCassiniIterator = startFlybysCassini.iterator();
                            stopFlybysCassiniIterator = stopFlybysCassini.iterator();
                            bodyFlybysCassiniIterator = bodyFlybysCassini.iterator();
                            startFlybysCassiniIterator.next();
                            stopCurrentFlybyCassini = stopFlybysCassiniIterator.next();
                            currentBodyFlybyCassini = bodyFlybysCassiniIterator.next();
                            System.out.println("stopCurrentFlybyCassini = " + CalendarUtil.calendarToString(stopCurrentFlybyCassini));
                        }
                        if (startFlybysCassini != null  &&
                                stopFlybysCassiniIterator.hasNext() &&
                                bodyFlybysCassiniIterator.hasNext() &&
                                solarSystem.getSimulationDateTime().after(stopCurrentFlybyCassini)) {
                            stopCurrentFlybyCassini = stopFlybysCassiniIterator.next();
                            currentBodyFlybyCassini = bodyFlybysCassiniIterator.next();
                            System.out.println("stopCurrentFlybyCassini = " + CalendarUtil.calendarToString(stopCurrentFlybyCassini));
                            try {
                                solarSystem.initializeSimulation(startFlybysCassiniIterator.next());
                            } catch (SolarSystemException e) {
                                e.printStackTrace();
                            }
                        }
                        String closestMoonFound = "";
                        double minMoonDistance = Double.MAX_VALUE;
                        if (startFlybysCassini == null) {
                            for (String moonName : SolarSystemParameters.getInstance().getMoonsOfPlanet("Saturn")) {
                                Vector3D moonPosition = null;
                                try {
                                    moonPosition = solarSystem.getPosition(moonName);
                                } catch (SolarSystemException ex) {
                                    showMessage("Error", ex.getMessage());
                                }
                                double moonDistance = spacecraftPosition.euclideanDistance(moonPosition);
                                if (moonDistance < minMoonDistance) {
                                    minMoonDistance = moonDistance;
                                    closestMoonFound = moonName;
                                }
                            }
                        }
                        else {
                            closestMoonFound = currentBodyFlybyCassini;
                            Vector3D moonPosition = null;
                            try {
                                moonPosition = solarSystem.getPosition(closestMoonFound);
                            } catch (SolarSystemException ex) {
                                showMessage("Error", ex.getMessage());
                            }
                            minMoonDistance = spacecraftPosition.euclideanDistance(moonPosition);
                        }
                        if (minMoonDistance < 5.0E8 && "Titan".equals(closestMoonFound)) {
                            Vector3D titanPosition = solarSystem.getParticle("Titan").getPosition();
                            if (spacecraftPosition.magnitude() < titanPosition.magnitude() || minMoonDistance < 1.5E8) {
                                observedBody = "Titan";
                                minDistance = Math.min(minDistance, 10.0 * minMoonDistance + 2.0E07);
                            }
                        }
                        else {
                            if (minMoonDistance < 1.5E8) {
                                observedBody = closestMoonFound;
                                minDistance = Math.min(minDistance, 5.0 * minMoonDistance);
                            }
                        }
                    }
                }
                if ("Rosetta".equals(selectedBody)) {
                    if (!automaticSimulationFast &&
                            (minDistance < 2.0E08 && (!"67P/Churyumov-Gerasimenko".equals(observedBody) ||
                                    solarSystem.getSimulationDateTime().after(startRosetta67P)))) {
                        checkBoxStepMode.setSelected(true);
                        startSimulationStepModeForward();
                        double value = Math.min(100.0, Math.max(5.0, minDistance / 2.0E06));
                        sliderZoomView.setValue(95.0 - 0.3 * value);
                        if ("67P/Churyumov-Gerasimenko".equals(observedBody)) {
                            sliderSimulationSpeed.setValue(100);
                        }
                        else {
                            sliderSimulationSpeed.setValue(value);
                        }
                    } else {
                        checkBoxStepMode.setSelected(false);
                        startSimulationForward();
                        double value = Math.min(100.0, Math.max(0.0, (minDistance - 2.0E08) / 7.0E06));
                        sliderZoomView.setValue(65.0 - 0.3 * value);
                        sliderSimulationSpeed.setValue(value);
                    }
                }
                else {
                    if ("Apollo 8".equals(selectedBody)) {
                        checkBoxStepMode.setSelected(true);
                        startSimulationStepModeForward();
                        observedBody = "Earth";
                        double value = Math.min(100.0, Math.max(1.0,(minDistance - 6.4E6) / 1.0E05));
                        sliderZoomView.setValue(100.0 - 0.1*value);
                        if (minDistance < 1.5E7) {
                            if (solarSystem.getSimulationDateTime().after(startEarthRise) &&
                                    solarSystem.getSimulationDateTime().before(endEarthRise)) {
                                // Real-time simulation during Earth Rise
                                sliderSimulationSpeed.setValue(0.0);
                            } else{
                                // Slow simulation near the Earth and the Moon
                                if ("Earth".equals(closestBodyFound)) {
                                    sliderSimulationSpeed.setValue(5.0);
                                }
                                else {
                                    sliderSimulationSpeed.setValue(15.0);
                                }
                            }
                        } else {
                            // Fast simulation when flying from the Earth to the Moon and vice versa
                            sliderSimulationSpeed.setValue(100.0);
                        }
                        if (solarSystem.getSimulationDateTime().after(entryTrajectInitApolloEight)) {
                            bodiesShown.remove("Apollo 8");
                            selectedBody = "Earth";
                            radioTelescopeView.setSelected(true);
                            pauseSimulation();
                        }
                    }
                    else {
                        if (!automaticSimulationFast && (minDistance < 1.0E09 ||
                                (selectedBody.startsWith("Pioneer") && !"Earth".equals(observedBody) && minDistance < 2.5E09) ||
                                ("Cassini".equals(selectedBody) && "Titan".equals(observedBody))) ||
                                ("Galileo".equals(selectedBody) && moons.get("Jupiter").contains(observedBody))) { // Check Voyager
                            checkBoxStepMode.setSelected(true);
                            startSimulationStepModeForward();
                            double value = Math.min(100.0, Math.max(5.0, minDistance / 1.0E07));
                            sliderZoomView.setValue(95.0 - 0.3 * value);
                            if (moons.get("Uranus").contains(observedBody)) {
                                value = Math.min(value,10.0);
                            }
                            if ("New Horizons".equals(selectedBody)) {
                                sliderSimulationSpeed.setValue(Math.max(20.0,value));
                            }
                            else {
                                sliderSimulationSpeed.setValue(value);
                            }
                        } else {
                            checkBoxStepMode.setSelected(false);
                            startSimulationForward();
                            double value = Math.min(100.0, Math.max(0.0, (minDistance - 1.0E09) / 7.0E07));
                            sliderZoomView.setValue(65.0 - 0.3 * value);
                            sliderSimulationSpeed.setValue(value);
                        }
                    }
                }
            } else {
                // minDistance >= 8.0E09 (8 million km) and spacecraft has passed the planet system
                if (!"Galileo".equals(selectedBody)) {
                    checkBoxesBodies.get("JupiterMoons").setSelected(false);
                }
                if (!"Cassini".equals(selectedBody)) {
                    checkBoxesBodies.get("SaturnMoons").setSelected(false);
                }
                checkBoxesBodies.get("UranusMoons").setSelected(false);
                checkBoxesBodies.get("NeptuneMoons").setSelected(false);
                checkBoxStepMode.setSelected(false);
                startSimulationFastForward();
                sliderZoomView.setValue(20.0);
                sliderSimulationSpeed.setValue(100.0);
            }
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
            eventDateTime = CalendarUtil.createGregorianCalendar(new GregorianCalendar());
        }
        else {
            eventDateTime = CalendarUtil.createGregorianCalendar(settings.getSimulationStartDateTime());
        }
        dateTimeSelector.setDateTime(eventDateTime);
        try {
            solarSystem.initializeSimulation(eventDateTime);
        } catch (SolarSystemException ex) {
            showMessage("Error",ex.getMessage());
        }

        for (String bodyName : checkBoxesBodies.keySet()) {
            if (settings.getBodiesShown().contains(bodyName)) {
                checkBoxesBodies.get(bodyName).setSelected(true);
            }
            else {
                checkBoxesBodies.get(bodyName).setSelected(false);
            }
        }
        if (settings.getBodiesShown().contains("Shoemaker-Levy 9")) {
            bodiesShown.add("Shoemaker-Levy 9");
        }
        else {
            bodiesShown.remove("Shoemaker-Levy 9");
        }
        selectedBody = settings.getSelectedBody();
        observedBody = "Earth";

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

        if (settings.getViewMode().equals(SolarSystemViewMode.TELESCOPE)) {
            radioTelescopeView.setSelected(true);
        }
        else {
            radioSpacecraftView.setSelected(true);
        }

        if (settings.isAutomaticView()) {
            checkBoxAutomaticView.setSelected(true);
            updateVisualizationSettings();
        }
        else {
            checkBoxAutomaticView.setSelected(false);
        }

        latitude = settings.getLatitude();
        textFieldLatitude.setText(DECIMAL_FORMAT_LATLON.format(latitude));
        sliderLatitude.setValue(latitude);
        longitude = settings.getLongitude();
        textFieldLongitude.setText(DECIMAL_FORMAT_LATLON.format(longitude));
        sliderLongitude.setValue(longitude);
    }

    /**
     * Create a list of visualization settings for event selector.
     */
    private List<VisualizationSettings> createVisualizationSettings() {
        List<VisualizationSettings> events = new ArrayList<>();
        VisualizationSettings init = new VisualizationSettings();
        init.setEventName("Initial settings (current time)");
        init.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth")));
        init.setSelectedBody("Sun");
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
                "Uranus","Neptune","Pluto System")));
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
                "Juno","Vesta","Ida","Eros","Gaspra","Bennu","Florence")));
        ast.setValueZoomView(23);
        events.add(ast);
        VisualizationSettings mar = new VisualizationSettings();
        mar.setEventName("Mars system (current time)");
        mar.setBodiesShown(new HashSet<>(Arrays.asList("Mars","MarsMoons")));
        mar.setSelectedBody("Mars");
        mar.setStepMode(true);
        mar.setValueZoomView(100);
        events.add(mar);
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
        VisualizationSettings plu = new VisualizationSettings();
        plu.setEventName("Pluto system (current time)");
        plu.setBodiesShown(new HashSet<>(Arrays.asList("Pluto System","Pluto SystemMoons")));
        plu.setSelectedBody("Pluto System");
        plu.setValueTopFrontView(40);
        plu.setValueZoomView(100);
        events.add(plu);
        VisualizationSettings sol = new VisualizationSettings();
        sol.setEventName("Solar eclipse Nancy (1999-08-11)");
        GregorianCalendar solStartDateTime = CalendarUtil.createGregorianCalendar(1999,8,11,9,10, 0);
        sol.setSimulationStartDateTime(solStartDateTime);
        sol.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Moon")));
        sol.setSelectedBody("Sun");
        sol.setShowEphemeris(false);
        sol.setObservationFromEarth(true);
        sol.setShowRuler(false);
        sol.setStepMode(true);
        sol.setValueZoomView(60);
        sol.setValueSimulationSpeed(1);
        sol.setLatitude(48.6921); // Nancy
        sol.setLongitude(6.1844); // Nancy
        events.add(sol);
        VisualizationSettings ann = new VisualizationSettings();
        ann.setEventName("Annular solar eclipse (2012-05-21)");
        GregorianCalendar annStartDateTime = CalendarUtil.createGregorianCalendar(2012,5,21,0,15, 0);
        ann.setSimulationStartDateTime(annStartDateTime);
        ann.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Moon")));
        ann.setSelectedBody("Sun");
        ann.setShowEphemeris(false);
        ann.setObservationFromEarth(true);
        ann.setShowRuler(false);
        ann.setStepMode(true);
        ann.setValueZoomView(60);
        ann.setValueSimulationSpeed(1);
        ann.setLatitude(40.1785); // Red Bluff, CA
        ann.setLongitude(-122.2358); // Red Bluff, CA
        events.add(ann);
        VisualizationSettings lun = new VisualizationSettings();
        lun.setEventName("Lunar eclipse (2019-01-21)");
        GregorianCalendar lunStartDateTime = CalendarUtil.createGregorianCalendar(2019,1,21,3,30, 0);
        lun.setSimulationStartDateTime(lunStartDateTime);
        lun.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Moon")));
        lun.setSelectedBody("Moon");
        lun.setShowEphemeris(false);
        lun.setShowRuler(true);
        lun.setStepMode(true);
        lun.setValueZoomView(80);
        lun.setValueSimulationSpeed(5);
        events.add(lun);
        VisualizationSettings ven = new VisualizationSettings();
        ven.setEventName("Venus transit (2004-06-08)");
        GregorianCalendar venStartDateTime = CalendarUtil.createGregorianCalendar(2004,6,8,5,15, 0);
        ven.setSimulationStartDateTime(venStartDateTime);
        ven.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Venus","Earth")));
        ven.setSelectedBody("Sun");
        ven.setShowEphemeris(false);
        ven.setObservationFromEarth(true);
        ven.setShowRuler(false);
        ven.setStepMode(true);
        ven.setValueZoomView(62);
        ven.setValueSimulationSpeed(5);
        events.add(ven);
        VisualizationSettings mer = new VisualizationSettings();
        mer.setEventName("Mercury transit (2016-05-09)");
        GregorianCalendar merStartDateTime = CalendarUtil.createGregorianCalendar(2016,5,9,11,5, 0);
        mer.setSimulationStartDateTime(merStartDateTime);
        mer.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Mercury","Earth")));
        mer.setSelectedBody("Sun");
        mer.setShowEphemeris(false);
        mer.setObservationFromEarth(true);
        mer.setShowRuler(false);
        mer.setStepMode(true);
        mer.setValueZoomView(62);
        mer.setValueSimulationSpeed(5);
        events.add(mer);
        VisualizationSettings shoe = new VisualizationSettings();
        shoe.setEventName("Impact Shoemaker-Levy July 1994");
        GregorianCalendar shoeStartDateTime = CalendarUtil.createGregorianCalendar(1994,5,8,0,0,0);
        shoe.setSimulationStartDateTime(shoeStartDateTime);
        shoe.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","JupiterMoons",
                "Shoemaker-Levy 9","Galileo")));
        shoe.setSelectedBody("Jupiter");
        shoe.setShowEphemeris(false);
        shoe.setShowRuler(true);
        shoe.setStepMode(false);
        shoe.setValueTopFrontView(30);
        shoe.setValueZoomView(62);
        shoe.setValueSimulationSpeed(50);
        shoe.setAutomaticView(true);
        events.add(shoe);
        VisualizationSettings pvnh = new VisualizationSettings();
        pvnh.setEventName("Spacecraft leaving the Solar System");
        pvnh.setSimulationStartDateTime(trajectoryStartDate.get("Pioneer 10"));
        pvnh.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn","Uranus","Neptune",
                "Pluto System","Arrokoth","Pioneer 10","Pioneer 11","Voyager 1","Voyager 2","New Horizons")));
        pvnh.setSelectedBody("Sun");
        pvnh.setShowEphemeris(false);
        pvnh.setShowRuler(true);
        pvnh.setStepMode(false);
        pvnh.setValueTopFrontView(40);
        pvnh.setValueZoomView(9);
        pvnh.setValueSimulationSpeed(100);
        events.add(pvnh);
        VisualizationSettings pio10 = new VisualizationSettings();
        pio10.setEventName("Launch Pioneer 10 (1972-03-03  01:49)");
        pio10.setSimulationStartDateTime(trajectoryStartDate.get("Pioneer 10"));
        pio10.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Pioneer 10")));
        pio10.setSelectedBody("Pioneer 10");
        pio10.setShowEphemeris(false);
        pio10.setShowRuler(true);
        pio10.setStepMode(false);
        pio10.setValueZoomView(15);
        pio10.setValueSimulationSpeed(100);
        pio10.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        pio10.setAutomaticView(true);
        events.add(pio10);
        VisualizationSettings pio11 = new VisualizationSettings();
        pio11.setEventName("Launch Pioneer 11 (1973-04-06  02:11)");
        pio11.setSimulationStartDateTime(trajectoryStartDate.get("Pioneer 11"));
        pio11.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn","Pioneer 11")));
        pio11.setSelectedBody("Pioneer 11");
        pio11.setShowEphemeris(false);
        pio11.setShowRuler(true);
        pio11.setStepMode(false);
        pio11.setValueTopFrontView(10);
        pio11.setValueZoomView(15);
        pio11.setValueSimulationSpeed(100);
        pio11.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        pio11.setAutomaticView(true);
        events.add(pio11);
        VisualizationSettings marin = new VisualizationSettings();
        marin.setEventName("Launch Mariner 10 (1973-11-03  17:45)");
        marin.setSimulationStartDateTime(trajectoryStartDate.get("Mariner 10"));
        marin.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Mercury","Venus","Earth","Moon",
                "Mariner 10")));
        marin.setSelectedBody("Mariner 10");
        marin.setShowEphemeris(false);
        marin.setShowRuler(true);
        marin.setStepMode(false);
        marin.setValueZoomView(40);
        marin.setValueSimulationSpeed(100);
        marin.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        marin.setAutomaticView(true);
        events.add(marin);
        VisualizationSettings voy1 = new VisualizationSettings();
        voy1.setEventName("Launch Voyager 1 (1977-09-05  12:56)");
        voy1.setSimulationStartDateTime(trajectoryStartDate.get("Voyager 1"));
        voy1.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn","Voyager 1")));
        voy1.setSelectedBody("Voyager 1");
        voy1.setShowEphemeris(false);
        voy1.setShowRuler(true);
        voy1.setStepMode(false);
        voy1.setValueZoomView(15);
        voy1.setValueSimulationSpeed(100);
        voy1.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        voy1.setAutomaticView(true);
        events.add(voy1);
        VisualizationSettings voy2 = new VisualizationSettings();
        voy2.setEventName("Launch Voyager 2 (1977-08-20  14:29)");
        voy2.setSimulationStartDateTime(trajectoryStartDate.get("Voyager 2"));
        voy2.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn","Uranus","Neptune",
                "Voyager 2")));
        voy2.setSelectedBody("Voyager 2");
        voy2.setShowEphemeris(false);
        voy2.setShowRuler(true);
        voy2.setStepMode(false);
        voy2.setValueZoomView(8);
        voy2.setValueSimulationSpeed(100);
        voy2.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        voy2.setAutomaticView(true);
        events.add(voy2);
        VisualizationSettings gio = new VisualizationSettings();
        gio.setEventName("Launch Giotto (1985-07-02  11:23)");
        gio.setSimulationStartDateTime(trajectoryStartDate.get("Giotto"));
        gio.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Moon","Halley",
                "26P/Grigg-Skjellerup","Giotto")));
        gio.setSelectedBody("Giotto");
        gio.setShowEphemeris(false);
        gio.setShowRuler(true);
        gio.setStepMode(false);
        gio.setValueZoomView(20);
        gio.setValueSimulationSpeed(100);
        gio.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        gio.setAutomaticView(true);
        events.add(gio);
        VisualizationSettings gal = new VisualizationSettings();
        gal.setEventName("Launch Galileo (1989-10-18)");
        gal.setSimulationStartDateTime(trajectoryStartDate.get("Galileo"));
        gal.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Venus","Earth","Moon","Mars","Jupiter",
                "Gaspra","Ida","Galileo")));
        gal.setSelectedBody("Galileo");
        gal.setShowEphemeris(false);
        gal.setShowRuler(true);
        gal.setStepMode(false);
        gal.setValueZoomView(20);
        gal.setValueSimulationSpeed(100);
        gal.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        gal.setAutomaticView(true);
        events.add(gal);
        VisualizationSettings cas = new VisualizationSettings();
        cas.setEventName("Launch Cassini (1997-10-15)");
        cas.setSimulationStartDateTime(trajectoryStartDate.get("Cassini"));
        cas.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Venus","Earth","Moon","Jupiter","Saturn",
                "Cassini")));
        cas.setSelectedBody("Cassini");
        cas.setShowEphemeris(false);
        cas.setShowRuler(true);
        cas.setStepMode(false);
        cas.setValueZoomView(20);
        cas.setValueSimulationSpeed(100);
        cas.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        cas.setAutomaticView(true);
        events.add(cas);
        VisualizationSettings ros = new VisualizationSettings();
        ros.setEventName("Launch Rosetta (2004-03-02  07:17)");
        ros.setSimulationStartDateTime(trajectoryStartDate.get("Rosetta"));
        ros.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Mars","67P/Churyumov-Gerasimenko",
                "Rosetta")));
        ros.setSelectedBody("Rosetta");
        ros.setShowEphemeris(false);
        ros.setShowRuler(true);
        ros.setStepMode(false);
        ros.setValueZoomView(20);
        ros.setValueSimulationSpeed(100);
        ros.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        ros.setAutomaticView(true);
        events.add(ros);
        VisualizationSettings nh = new VisualizationSettings();
        nh.setEventName("Launch New Horizons (2006-01-19  19:00)");
        nh.setSimulationStartDateTime(trajectoryStartDate.get("New Horizons"));
        nh.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Earth","Jupiter","Saturn","Uranus","Neptune",
                "Pluto System", "Arrokoth","New Horizons")));
        nh.setSelectedBody("New Horizons");
        nh.setShowEphemeris(false);
        nh.setShowRuler(true);
        nh.setStepMode(false);
        nh.setValueZoomView(5);
        nh.setValueSimulationSpeed(100);
        events.add(nh);
        nh.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        nh.setAutomaticView(true);
        VisualizationSettings ap8 = new VisualizationSettings();
        ap8.setEventName("Launch Apollo 8 (1968-12-21  12.51)");
        ap8.setSimulationStartDateTime(trajectoryStartDate.get("Apollo 8"));
        ap8.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Moon","Earth","Apollo 8")));
        ap8.setSelectedBody("Apollo 8");
        ap8.setShowEphemeris(false);
        ap8.setShowRuler(true);
        ap8.setStepMode(true);
        ap8.setValueZoomView(85);
        ap8.setValueSimulationSpeed(100);
        ap8.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        ap8.setAutomaticView(true);
        events.add(ap8);
        VisualizationSettings iss = new VisualizationSettings();
        iss.setEventName("International Space Station (current time)");
        iss.setBodiesShown(new HashSet<>(Arrays.asList("Sun","Moon","Earth","ISS")));
        iss.setSelectedBody("ISS");
        iss.setShowEphemeris(false);
        iss.setShowRuler(false);
        iss.setStepMode(true);
        iss.setValueZoomView(100);
        iss.setValueSimulationSpeed(1);
        iss.setViewMode(SolarSystemViewMode.FROMSPACECRAFT);
        events.add(iss);
        return events;
    }

    /**
     * Create lists of start/stop times for flybys of Cassini.
     */
    private void initializeFlybysCassini() {
        startFlybysCassini = new ArrayList<>();
        stopFlybysCassini = new ArrayList<>();
        bodyFlybysCassini = new ArrayList<>();
        startFlybysCassini.add(startCassiniPhoebe);
        stopFlybysCassini.add(stopCassiniPhoebe);
        bodyFlybysCassini.add("Phoebe");
        startFlybysCassini.add(startCassiniPassageThroughRings);
        stopFlybysCassini.add(stopCassiniPassageThroughRings);
        //bodyFlybysCassini.add("Saturn");
        bodyFlybysCassini.add("Mimas");
        startFlybysCassini.add(startCassiniEnceladus);
        stopFlybysCassini.add(stopCassiniEnceladus);
        bodyFlybysCassini.add("Enceladus");
        startFlybysCassini.add(startCassiniSaturnMimasA);
        stopFlybysCassini.add(stopCassiniSaturnMimasA);
        bodyFlybysCassini.add("Saturn");
        startFlybysCassini.add(startCassiniSaturnA);
        stopFlybysCassini.add(stopCassiniSaturnA);
        bodyFlybysCassini.add("Saturn");
        startFlybysCassini.add(startCassiniEnceladusA);
        stopFlybysCassini.add(stopCassiniEnceladusA);
        bodyFlybysCassini.add("Enceladus");
        startFlybysCassini.add(startCassiniTethys);
        stopFlybysCassini.add(stopCassiniTethys);
        bodyFlybysCassini.add("Tethys");
        startFlybysCassini.add(startCassiniHyperion);
        stopFlybysCassini.add(stopCassiniHyperion);
        bodyFlybysCassini.add("Hyperion");
        startFlybysCassini.add(startCassiniDioneA);
        stopFlybysCassini.add(stopCassiniDioneA);
        bodyFlybysCassini.add("Dione");
        startFlybysCassini.add(startCassiniTitanA);
        stopFlybysCassini.add(stopCassiniTitanA);
        bodyFlybysCassini.add("Titan");
        startFlybysCassini.add(startCassiniRheaA);
        stopFlybysCassini.add(stopCassiniRheaA);
        bodyFlybysCassini.add("Rhea");
        startFlybysCassini.add(startCassiniIapetus);
        stopFlybysCassini.add(stopCassiniIapetus);
        bodyFlybysCassini.add("Iapetus");
        /*
        startFlybysCassini.add(startCassiniEnceladusB);
        stopFlybysCassini.add(stopCassiniEnceladusB);
        bodyFlybysCassini.add("Enceladus");
        */
        startFlybysCassini.add(startCassiniMimas);
        stopFlybysCassini.add(stopCassiniMimas);
        bodyFlybysCassini.add("Mimas");
        startFlybysCassini.add(startCassiniRheaB);
        stopFlybysCassini.add(stopCassiniRheaB);
        bodyFlybysCassini.add("Rhea");
        startFlybysCassini.add(startCassiniTitanB);
        stopFlybysCassini.add(stopCassiniTitanB);
        bodyFlybysCassini.add("Titan");
        startFlybysCassini.add(startCassiniDioneB);
        stopFlybysCassini.add(stopCassiniDioneB);
        bodyFlybysCassini.add("Dione");
        startFlybysCassini.add(startCassiniEnceladusC);
        stopFlybysCassini.add(stopCassiniEnceladusC);
        bodyFlybysCassini.add("Enceladus");
        startFlybysCassini.add(startCassiniTitanC);
        stopFlybysCassini.add(stopCassiniTitanC);
        bodyFlybysCassini.add("Titan");
        startFlybysCassini.add(startCassiniInsideRings);
        stopFlybysCassini.add(stopCassiniInsideRings);
        bodyFlybysCassini.add("Saturn");
        startFlybysCassini.add(startCassiniEndOfMission);
        stopFlybysCassini.add(stopCassiniEndOfMission);
        bodyFlybysCassini.add("Saturn");
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
                    if (automaticView) {
                        updateVisualizationSettings();
                    }
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
