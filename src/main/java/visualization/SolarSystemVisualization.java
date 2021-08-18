/*
 * Copyright (c) 2020 Nico Kuijpers and Marco Brassé
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
package visualization;

import application.SolarSystemException;
import ephemeris.CalendarUtil;
import ephemeris.EphemerisUtil;
import ephemeris.SolarSystemParameters;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import solarsystem.SolarSystem;
import util.Vector3D;
import util.VectorUtil;

import java.util.*;

/**
 * Visualization of the Solar System using JavaFX 3D.
 */
public class SolarSystemVisualization extends Stage {

    // Screen size and scale
    private static final int SCREENWIDTH  = 900;
    private static final int SCREENHEIGHT = 900;
    private static final int SCREENDEPTH  = 900;
    private static final double SCREENSCALE = 3.0 * SolarSystemParameters.ASTRONOMICALUNIT;
    private static final double FIELDOFVIEWFACTORTELESCOPE  = 1.0E9;
    private static final double FIELDOFVIEWFACTORSPACECRAFT = 1.0E7;

    // Rotate along Z-axis (theta) and Y-axis (phi) by mouse dragging
    private static final double ROTATEFACTOR = 0.5;

    // Zoom can be adjusted by mouse scroll
    private static final double MINZOOM = 0.0;
    private static final double MAXZOOM = 99.9;
    private static final double INITZOOM = 90.0;
    private static final double DELTAZOOMFACTOR = 0.01;

    // Correction for radius of sphere representing the Sun
    private static final double CORRECTIONSUNRADIUSSOLARECLIPSE   = 3.0;  // Correction for Solar eclipse
    private static final double CORRECTIONSUNRADIUSMERCURYTRANSIT = 1.45; // Correction for Mercury transit
    private static final double CORRECTIONSUNRADIUSVENUSTRANSIT   = 1.96; // Correction for Venus transit

    // Solar System bodies with diameter at most 100 km are considered small bodies
    // Also Juno and Pallas are considered small bodies
    private static final double MAXDIAMETERSMALLBODY = 1.0E05; // 100 km
    private static final double SCALESMALLBODY = 2.0E02; // factor 200

    // Spacecraft are visualized much larger than they are in reality
    private static final double DIAMETERPIONEER     = 5.0E6; // 5000 km
    private static final double DIAMETERVOYAGER     = 5.0E6; // 5000 km
    private static final double DIAMETERCASSINI     = 2.5E6; // 2500 km
    private static final double DIAMETERNEWHORIZONS = 8.0E5; //  800 km
    private static final double DIAMETERROSETTA     = 2.5E5; //  250 km
    private static final double DIAMETERISS         = 2.0E5; //  200 km
    private static final double DIAMETERAPOLLO      = 2.0E5; //  200 km

    // Factor to determine the length of the shadows of Jupiter and Saturn
    private static final double SHADOWJUPITERFACTOR = 30.0; // 30 times radius of Jupiter
    private static final double SHADOWSATURNFACTOR = 8.0;   // 8 times radius of Saturn

    // View mode for 3D visualization
    // 1. View selected object from the Earth
    // 2. View nearest object from position of spacecraft
    //
    private SolarSystemViewMode viewMode = SolarSystemViewMode.TELESCOPE;

    //https://www.genuinecoder.com/javafx-3d-tutorial-object-transform-rotation-with-mouse/
    // Tracks drag starting point for x and y
    private double anchorX, anchorY;
    // Keep track of current angle for x and y
    private double anchorAngleX = 0.0;
    private double anchorAngleY = 0.0;
    // Update angle for x and y after drag
    private double angleX = 0.0;
    private double angleY = 0.0;

    // Zoom can be adjusted by mouse scroll
    private double zoom = INITZOOM;

    // Material for sphere representing shadow of the Earth
    private PhongMaterial materialTransparent;
    private PhongMaterial materialShadowEarth;

    // Scene
    private Group solarSystemGroup;
    private SubScene subScene;
    private Text displayObservedBody;
    private Text displayDateTime;
    private Sphere sun, moon;
    private Sphere mercury, venus, earth, mars, jupiter, saturn, uranus, neptune;
    private Sphere pluto, eris, chiron, ceres;
    private Sphere encke, halebopp, shoemaker, florence;
    private Sphere io, europa, ganymede, callisto;
    private Sphere mimas, enceladus, tethys, dione, rhea, titan, hyperion, iapetus;
    private Sphere miranda, ariel, umbriel, titania, oberon;
    private Sphere triton;
    private Sphere shadowIo, shadowEuropa, shadowGanymede, shadowCallisto;
    private Cylinder ringSaturn, ringUranus;
    private Cylinder coronaSun;
    private Cylinder shadowEarth;
    private Cylinder shadowJupiter;
    private Cylinder shadowSaturn;
    private Shape3D pallas, juno, vesta, eros, bennu;
    private Shape3D halley, churyumov, ultimaThule;
    private Shape3D pioneer10, pioneer11, voyager1, voyager2, newhorizons, rosetta, cassini, apollo8;
    private Map<String,Node> bodies;
    private Map<String,Rotate> bodyRotationsX;
    private Map<String,Rotate> bodyRotationsY;
    private Map<String,Rotate> bodyRotationsZ;
    private Map<String,Rotate> bodyRotationsObliquity;
    private Map<String,Rotate> bodyRotationsRevolution;
    private PerspectiveCamera camera;
    private PointLight pointLight;
    private Sphere locationOnEarth;

    // International Space Station
    private Group iss;

    // Names of spacecraft
    private List<String> spacecraftNames;

    // Camera rotation and position
    private Translate pivotCamera;
    private Rotate xRotateCamera;
    private Rotate yRotateCamera;
    private Rotate zRotateCamera;
    private Translate positionCamera;

    // Location Amsterdam, The Netherlands
    private double latitude = 52.3676; // degrees
    private double longitude = 4.9041; // degrees
    private final static double LOCATION_HEIGHT = 1.0E5; // 100 km

    // Reference to the Solar System
    private SolarSystem solarSystem;

    // Solar System parameters
    private SolarSystemParameters solarSystemParameters;

    // Selected body
    private String selectedBody = "Sun";

    // Observed body
    private String observedBody = "Earth";

    /**
     * Constructor.
     * @param solarSystem reference to the Solar System
     */
    public SolarSystemVisualization(SolarSystem solarSystem) {

        // Set reference to SolarSystem
        this.solarSystem = solarSystem;

        // Reference to Solar System parameters
        this.solarSystemParameters = SolarSystemParameters.getInstance();

        // Factory for 3D shapes for visualization
        SolarSystemShapeFactory factory = new SolarSystemShapeFactory(this);

        // Define material for sphere representing shadow of the Earth
        materialTransparent = new PhongMaterial();
        materialTransparent.setDiffuseColor(Color.TRANSPARENT);
        materialShadowEarth = new PhongMaterial();
        Color colorShadowEarth = new Color(0.0,0.0,0.0,0.75);
        materialShadowEarth.setDiffuseColor(colorShadowEarth);

        // Sun, planets, and moons
        bodyRotationsX = new HashMap<>();
        bodyRotationsY = new HashMap<>();
        bodyRotationsZ = new HashMap<>();
        bodyRotationsRevolution = new HashMap<>();
        bodyRotationsObliquity = new HashMap<>();
        sun = factory.createSphere("Sun", Color.BLANCHEDALMOND);
        mercury = factory.createSphere("Mercury", Color.ORANGE);
        venus = factory.createSphere("Venus", Color.PEACHPUFF);
        earth = factory.createSphere("Earth", Color.AQUAMARINE);
        moon = factory.createSphere("Moon", Color.LIGHTGRAY);
        mars = factory.createSphere("Mars", Color.RED);
        jupiter = factory.createSphere("Jupiter", Color.ROSYBROWN);
        saturn = factory.createSphere("Saturn", Color.ORANGE);
        uranus = factory.createSphere("Uranus", Color.LIGHTBLUE);
        neptune = factory.createSphere("Neptune", Color.CADETBLUE);
        pluto = factory.createSphere("Pluto", Color.LIGHTBLUE);
        eris = factory.createSphere("Eris", Color.LIGHTSALMON);
        chiron = factory.createSphere("Chiron", Color.CRIMSON);
        ceres = factory.createSphere("Ceres", Color.ORANGE);
        pallas = factory.createSphere("Pallas", Color.LIGHTGREEN);
        eros = factory.createSphere("Eros", Color.LIGHTBLUE);
        halley = factory.createSphere("Halley", Color.YELLOW);
        encke = factory.createSphere("Encke", Color.LIGHTGREEN);
        halebopp = factory.createSphere("Hale-Bopp", Color.LIGHTBLUE);
        Color colorShoemaker = new Color(254.0/255,216.0/255,177.0/255, 1.0);
        shoemaker = factory.createSphere("Shoemaker-Levy 9", colorShoemaker);
        florence = factory.createSphere("Florence", Color.LIGHTGREEN);
        io = factory.createSphere("Io",Color.YELLOW);
        europa = factory.createSphere("Europa",Color.LIGHTBLUE);
        ganymede = factory.createSphere("Ganymede",Color.LIGHTGRAY);
        callisto = factory.createSphere("Callisto",Color.ORANGE);
        mimas = factory.createSphere("Mimas", Color.LIGHTGRAY);
        enceladus = factory.createSphere("Enceladus", Color.ALICEBLUE);
        tethys = factory.createSphere("Tethys", Color.DARKGOLDENROD);
        dione = factory.createSphere("Dione", Color.BISQUE);
        rhea = factory.createSphere("Rhea", Color.ORANGE);
        titan = factory.createSphere("Titan", Color.PEACHPUFF);
        hyperion = factory.createSphere("Hyperion", Color.LIGHTCORAL);
        iapetus = factory.createSphere("Iapetus", Color.ALICEBLUE);
        miranda = factory.createSphere("Miranda", Color.LIGHTGRAY);
        ariel = factory.createSphere("Ariel", Color.ALICEBLUE);
        umbriel = factory.createSphere("Umbriel", Color.PEACHPUFF);
        titania = factory.createSphere("Titania", Color.LIGHTSALMON);
        oberon = factory.createSphere("Oberon", Color.BISQUE);
        triton = factory.createSphere("Triton", Color.LIGHTGRAY);
        shadowIo = factory.createSphere("shadowIo",Color.BLACK);
        shadowEuropa = factory.createSphere("shadowEuropa",Color.BLACK);
        shadowGanymede = factory.createSphere("shadowGanymede",Color.BLACK);
        shadowCallisto = factory.createSphere("shadowCallisto",Color.BLACK);

        // Rings of Saturn and Uranus
        ringSaturn = factory.createRing("Saturn",Color.ORANGE);
        ringUranus = factory.createRing("Uranus",Color.LIGHTBLUE);

        // Corona for Solar Eclipse
        coronaSun = factory.createRing("Sun",Color.ORANGE);

        // Shadow of the Earth to visualize Lunar eclipse
        shadowEarth = new Cylinder();

        // Shadow of Jupiter to cast shadow over the Galilean moons
        double radiusJup = 0.5*this.screenDiameter("Jupiter");
        double flatteningJup = solarSystemParameters.getFlattening("Jupiter");
        shadowJupiter = new Cylinder((1.0 - flatteningJup)*radiusJup, SHADOWJUPITERFACTOR*radiusJup);
        shadowJupiter.setMaterial(new PhongMaterial(Color.BLACK));
        setBodyRotations("shadowJupiter",shadowJupiter);

        // Shadow of Saturn to cast shadow over the rings and the moons Mimas through Rhea
        double radiusSat = 0.5*this.screenDiameter("Saturn");
        double flatteningSat = solarSystemParameters.getFlattening("Saturn");
        shadowSaturn = new Cylinder((1.0 - flatteningSat)*radiusSat, SHADOWSATURNFACTOR*radiusSat);
        shadowSaturn.setMaterial(new PhongMaterial(Color.BLACK));
        setBodyRotations("shadowSaturn",shadowSaturn);

        // Small Solar System bodies
        pallas = factory.createSmallBody("Pallas",Color.LIGHTGRAY);
        juno = factory.createSmallBody("Juno",Color.LIGHTGRAY);
        vesta = factory.createSmallBody("Vesta",Color.YELLOW);
        Color colorEros = new Color(164.0/255,152.0/255,138.0/255, 1.0);
        eros = factory.createSmallBody("Eros",colorEros);
        bennu = factory.createSmallBody("Bennu",Color.LIGHTGRAY);
        halley = factory.createSmallBody("Halley",Color.GRAY);
        churyumov = factory.createSmallBody("67P/Churyumov-Gerasimenko", Color.SNOW);
        ultimaThule = factory.createSmallBody("Ultima Thule", Color.LIGHTGRAY);

        // Spacecraft
        pioneer10 = factory.createSpacecraft("Pioneer 10", Color.LIGHTYELLOW);
        pioneer11 = factory.createSpacecraft("Pioneer 11", Color.LIGHTYELLOW);
        voyager1 = factory.createSpacecraft("Voyager 1", Color.LIGHTYELLOW);
        voyager2 = factory.createSpacecraft("Voyager 2", Color.LIGHTYELLOW);
        newhorizons = factory.createSpacecraft("New Horizons", Color.LIGHTYELLOW);
        rosetta = factory.createSpacecraft("Rosetta", Color.LIGHTYELLOW);
        cassini = factory.createSpacecraft("Cassini", Color.LIGHTYELLOW);
        apollo8 = factory.createSpacecraft("Apollo 8", Color.LIGHTYELLOW);

        // International Space Station
        iss = factory.createISS("ISS");

        // Names of spacecraft
        spacecraftNames = new ArrayList<>();
        spacecraftNames.add("Pioneer 10");
        spacecraftNames.add("Pioneer 11");
        spacecraftNames.add("Voyager 1");
        spacecraftNames.add("Voyager 2");
        spacecraftNames.add("New Horizons");
        spacecraftNames.add("Rosetta");
        spacecraftNames.add("Cassini");
        spacecraftNames.add("Apollo 8");
        spacecraftNames.add("ISS");

        // Obtain all shapes created by the factory
        bodies = factory.getShapes();

        // Set body rotations for all shapes
        for (String name : bodies.keySet()) {
            Node node = bodies.get(name);
            setBodyRotations(name,node);
        }

        // Rotate the rings of Saturn and Uranus with the planet
        ringSaturn.getTransforms().add(bodyRotationsX.get("Saturn"));
        ringSaturn.getTransforms().add(bodyRotationsZ.get("Saturn"));
        ringSaturn.getTransforms().add(bodyRotationsY.get("Saturn"));
        ringSaturn.getTransforms().add(bodyRotationsObliquity.get("Saturn"));
        ringUranus.getTransforms().add(bodyRotationsX.get("Uranus"));
        ringUranus.getTransforms().add(bodyRotationsZ.get("Uranus"));
        ringUranus.getTransforms().add(bodyRotationsY.get("Uranus"));
        ringUranus.getTransforms().add(bodyRotationsObliquity.get("Uranus"));

        // Light coming from the Sun
        pointLight = new PointLight();
        pointLight.setTranslateX(sun.getTranslateX());
        pointLight.setTranslateY(sun.getTranslateY());
        pointLight.setTranslateZ(sun.getTranslateZ());

        // Small red sphere representing location on Earth corresponding
        // to latitude and longitude
        double radiusLocationOnEarth = 0.01*screenDiameter("Earth");
        locationOnEarth = new Sphere(radiusLocationOnEarth);
        locationOnEarth.setMaterial(new PhongMaterial(Color.RED));

        // Camera
        // https://docs.oracle.com/javafx/8/3d_graphics/camera.htm
        // In JavaFX, the camera's coordinate system is Y-down, which
        // means X axis points to the right, Y axis is pointing down,
        // Z axis is pointing away from the viewer into the screen.
        pivotCamera = new Translate();
        xRotateCamera = new Rotate(0, Rotate.X_AXIS);
        yRotateCamera = new Rotate(0, Rotate.Y_AXIS);
        zRotateCamera = new Rotate(0, Rotate.Z_AXIS);
        positionCamera = new Translate(0.0,0.0,0.0);

        // Create and position camera
        camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                pivotCamera,
                yRotateCamera,
                xRotateCamera,
                zRotateCamera,
                positionCamera
        );
        setCameraSettings(50.0, 1.E06, 40.0* SolarSystemParameters.ASTRONOMICALUNIT);

        // Define the scene using a subscene
        solarSystemGroup = new Group();
        for (Node node : bodies.values()) {
            solarSystemGroup.getChildren().add(node);
        }
        solarSystemGroup.getChildren().add(ringSaturn);
        solarSystemGroup.getChildren().add(ringUranus);
        solarSystemGroup.getChildren().add(coronaSun);
        solarSystemGroup.getChildren().add(shadowEarth);
        solarSystemGroup.getChildren().add(shadowJupiter);
        solarSystemGroup.getChildren().add(shadowSaturn);
        solarSystemGroup.getChildren().add(pointLight);
        solarSystemGroup.getChildren().add(locationOnEarth);
        subScene = new SubScene(
                solarSystemGroup,
                SCREENWIDTH, SCREENHEIGHT,
                true,
                SceneAntialiasing.BALANCED
        );
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);
        Group group = new Group();
        group.getChildren().add(subScene);
        Scene scene = new Scene(group);

        // Set mouse control
        initMouseControl(subScene,this);

        // Display observed body in the left upper corner
        displayObservedBody = new Text();
        displayObservedBody.setX(20.0);
        displayObservedBody.setY(20.0);
        displayObservedBody.setFont(new Font("Arial", 16));
        displayObservedBody.setFill(Color.LIGHTYELLOW);
        group.getChildren().add(displayObservedBody);

        // Display date/time in the right lower corner
        displayDateTime = new Text();
        displayDateTime.setX(SCREENWIDTH - 300.0);
        displayDateTime.setY(SCREENHEIGHT - 40.0);
        displayDateTime.setFont(new Font("Courier", 16));
        displayDateTime.setFill(Color.LIGHTYELLOW);
        group.getChildren().add(displayDateTime);

        // Resize the scene when the window (or stage) is resized
        // https://stackoverflow.com/questions/38216268/how-to-listen-resize-event-of-stage-in-javafx
        final Stage stage = this;
        ChangeListener<Number> stageSizeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                subScene.setWidth(stage.getWidth());
                subScene.setHeight(stage.getHeight());
                displayDateTime.setX(Math.max(stage.getWidth() - 300.0,0.0));
                displayDateTime.setY(Math.max(stage.getHeight() - 40.0,20.0));
            }
        };
        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);

        // Show the scene
        this.setTitle("Solar System Visualization");
        this.setScene(scene);
    }


    /**
     * Set handlers for mouse control.
     * @param scene Reference to the scene
     * @param stage Reference to the stage
     */
    private void initMouseControl(SubScene scene, Stage stage) {
        // https://www.genuinecoder.com/javafx-3d-tutorial-object-transform-rotation-with-mouse/
        // Mouse pressed
        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX;
            anchorAngleY = angleY;
        });

        // Mouse dragged
        scene.setOnMouseDragged(event -> {
            angleX = anchorAngleX + ROTATEFACTOR*(anchorX - event.getSceneX());
            angleY = anchorAngleY - ROTATEFACTOR*(anchorY - event.getSceneY());
            angleX %= 360.0;
            angleY = Math.min(180.0,Math.max(-180.0,angleY));
        });

        // Mouse scroll
        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            zoom += delta*DELTAZOOMFACTOR;
            zoom = Math.min(zoom,MAXZOOM);
            zoom = Math.max(zoom,MINZOOM);
        });
    }


    /**
     * Update display with name of body that is currently observed.
     */
    private void refreshDisplayObservedBody() {
        StringBuilder sb = new StringBuilder();
        if (viewMode.equals(SolarSystemViewMode.TELESCOPE)) {
            if ("Earth".equals(selectedBody) || "EarthMoonBarycenter".equals(selectedBody)) {
                sb.append("Earth observed from the Sun");
            }
            else {
                sb.append(selectedBody + " observed from the Earth");
            }
        }
        else {
            Vector3D positionSelectedBody = null;
            Vector3D positionObservedBody = null;
            Vector3D velocitySelectedBody = null;
            Vector3D velocityObservedBody = null;
            try {
                positionSelectedBody = solarSystem.getPosition(selectedBody);
                velocitySelectedBody = solarSystem.getVelocity(selectedBody);
                positionObservedBody = solarSystem.getPosition(observedBody);
                velocityObservedBody = solarSystem.getVelocity(observedBody);
            } catch (SolarSystemException e) {
                e.printStackTrace();
            }
            double distanceToCenter = positionSelectedBody.euclideanDistance(positionObservedBody);
            double diameter = solarSystemParameters.getDiameter(observedBody);
            double distanceToSurface = distanceToCenter - 0.5*diameter;
            long distanceKm = Math.round(distanceToSurface/1000.0);
            Vector3D relativeVelocity;
            if ("Apollo 8".equals(selectedBody) || "ISS".equals(selectedBody)) {
                relativeVelocity = velocitySelectedBody.minus(velocityObservedBody);
            }
            else {
                relativeVelocity = velocitySelectedBody;
            }
            long velocity = Math.round(relativeVelocity.magnitude());
            sb.append(observedBody + " observed from ");
            sb.append(selectedBody);
            sb.append("\n");
            sb.append("Distance ");
            sb.append(distanceKm);
            sb.append(" km\n");
            sb.append("Velocity ");
            sb.append(velocity);
            sb.append(" m/s");
        }
        displayObservedBody.setText(sb.toString());
    }

    /**
     * Update display with current simulation date/time.
     */
    private void refreshDisplayDateTime() {
        GregorianCalendar dateTime = solarSystem.getSimulationDateTime();
        String dateTimeString = CalendarUtil.calendarToString(dateTime);
        StringBuilder sb = new StringBuilder(dateTimeString);
        int index = dateTimeString.length() - 1;
        sb.replace(index,index + 1," ");
        sb.append("(" + dateTime.getTimeZone().getID() + ")");
        displayDateTime.setText(sb.toString());
    }

    /**
     * Set camera settings depending on visualization view mode.
     * Field of view will be adjusted for zoom.
     * @param fieldOfView field of view
     * @param nearDistance distance to nearest object to be rendered [m]
     * @param farDistance distance to farthest object to be rendered [m]
     */
    private void setCameraSettings(double fieldOfView, double nearDistance, double farDistance) {
        double fieldOfViewZoom = Math.min(90.0, Math.max(0.0, (10.0 - 0.1 * zoom) * fieldOfView));
        camera.setFieldOfView(fieldOfViewZoom);
        zRotateCamera.setAngle(0.0);
        yRotateCamera.setAngle(0.0);
        xRotateCamera.setAngle(0.0);
        if (viewMode.equals(SolarSystemViewMode.TELESCOPE)) {
            camera.setNearClip(0.1 * SCREENDEPTH);
            // Use 3 * SCREENDEPTH to see Saturn when observing Jupiter during
            // the great conjunction on Dec 19, 2020
            camera.setFarClip(3.0 * SCREENDEPTH);
        } else {
            // View observed object from position of spacecraft
            camera.setNearClip(SCREENDEPTH*(nearDistance/SCREENSCALE));
            camera.setFarClip(SCREENDEPTH*(farDistance/SCREENSCALE));
        }
    }

    /**
     * Set body rotations.
     * @param name Name of the body
     * @param node 3D geometry representing the body
     */
    private void setBodyRotations(String name, Node node) {
        Rotate bodyRotationX = new Rotate(0.0,Rotate.X_AXIS);
        Rotate bodyRotationY = new Rotate(0.0,Rotate.Y_AXIS);
        Rotate bodyRotationZ = new Rotate(0.0,Rotate.Z_AXIS);
        Rotate bodyRotationObliquity = new Rotate();
        Rotate bodyRotationRevolution = new Rotate(0.0, Rotate.Y_AXIS);
        node.getTransforms().add(bodyRotationX);
        node.getTransforms().add(bodyRotationZ); // Note the order
        node.getTransforms().add(bodyRotationY);
        node.getTransforms().add(bodyRotationObliquity);
        node.getTransforms().add(bodyRotationRevolution);
        bodyRotationsX.put(name,bodyRotationX);
        bodyRotationsY.put(name,bodyRotationY);
        bodyRotationsZ.put(name,bodyRotationZ);
        bodyRotationsObliquity.put(name,bodyRotationObliquity);
        bodyRotationsRevolution.put(name,bodyRotationRevolution);
    }

    /**
     * Determine diameter for given Solar System body
     * @param bodyName
     * @return diameter [m]
     */
    private double diameterBody(String bodyName) {
        double diameter;
        switch (bodyName) {
            case "Pioneer 10":
            case "Pioneer 11":
                diameter = DIAMETERPIONEER;
                break;
            case "Voyager 1":
            case "Voyager 2":
                diameter = DIAMETERVOYAGER;
                break;
            case "New Horizons":
                diameter = DIAMETERNEWHORIZONS;
                break;
            case "Rosetta":
                diameter = DIAMETERROSETTA;
                break;
            case "Cassini":
                diameter = DIAMETERCASSINI;
                break;
            case "ISS":
                diameter = DIAMETERISS;
                break;
            case "Apollo 8":
                diameter = DIAMETERAPOLLO;
                break;
            case "Pallas":
            case "Juno":
                diameter = SCALESMALLBODY * solarSystemParameters.getDiameter(bodyName);
                break;
            default:
                diameter = solarSystemParameters.getDiameter(bodyName);
                if (diameter <= MAXDIAMETERSMALLBODY) {
                    diameter *= SCALESMALLBODY;
                }
                break;
        }
        return diameter;
    }

    /**
     * Compute x-position on the screen for 3D position in the Solar System.
     * @param position 3D position in m
     * @return x-position in pixels
     */
    private double screenX(Vector3D position) {
        return -SCREENWIDTH * (position.getX() / SCREENSCALE);
    }

    /**
     * Compute y-position on the screen for 3D position in the Solar System.
     * @param position 3D position in m
     * @return y-postion in pixels
     */
    private double screenY(Vector3D position) {
        return -SCREENHEIGHT * (position.getY() / SCREENSCALE);
    }

    /**
     * Compute z-position on the screen for 3D position in the Solar System.
     * @param position 3D position in m
     * @return z-postion in pixels
     */
    private double screenZ(Vector3D position) {
        return SCREENDEPTH * (position.getZ() / SCREENSCALE);
    }

    /**
     * Diameter in pixels of shape for Solar System body.
     * @param bodyName name of Solar System body
     * @return diameter in pixels
     */
    public double screenDiameter(String bodyName) {
        return SCREENWIDTH * (diameterBody(bodyName) / SCREENSCALE);
    }

    /**
     * Update rotation of all visible objects to simulate revolution and obliquity.
     * The direction of the camera with respect to the object is taken into account.
     * @param cameraDirection direction of camera
     */
    private void updateBodyRotations(Vector3D cameraDirection) {

        // Angle theta of camera in X-Y plane and angle phi with X-Y plane
        double camX = cameraDirection.getX();
        double camY = cameraDirection.getY();
        double camZ = cameraDirection.getZ();
        double camThetaRad = Math.atan2(camY,camX);
        double camThetaDeg = Math.toDegrees(camThetaRad);
        double camPhiRad = Math.asin(camZ);
        double camPhiDeg = Math.toDegrees(camPhiRad);

        // Position of the Earth in the Solar System
        Vector3D positionEarth = solarSystem.getParticle("Earth").getPosition();

        // Number of days past J2000 to simulate revolution
        GregorianCalendar dateTime = solarSystem.getSimulationDateTime();
        double nrDaysPastJ2000 = EphemerisUtil.computeNrDaysPastJ2000(dateTime);

        // Update rotations of all visible objects
        for (String name : bodies.keySet()) {
            Node node = bodies.get(name);
            if (node.isVisible()) {
                if (spacecraftNames.contains(name)) {
                    if ("Apollo 8".equals(name)) {
                        // Rotate spacecraft such that it is directed in the direction of movement
                        Vector3D positionApollo = solarSystem.getParticle("Apollo 8").getPosition();
                        double distanceToEarthKm = positionApollo.euclideanDistance(positionEarth)/1.0E3;
                        Vector3D referenceVelocity;
                        if (distanceToEarthKm < 6400) {
                            referenceVelocity = solarSystem.getParticle("Earth").getVelocity();
                        }
                        else {
                            referenceVelocity = solarSystem.getParticle("Moon").getVelocity();
                        }
                        Vector3D spacecraftVelocity = solarSystem.getParticle(name).getVelocity();
                        Vector3D spacecraftDirection = (spacecraftVelocity.minus(referenceVelocity)).normalize();
                        double angleXYrad = Math.atan2(spacecraftDirection.getY(), spacecraftDirection.getX());
                        double angleXYdeg = Math.toDegrees(angleXYrad) - 90.0;
                        bodyRotationsY.get(name).setAngle(camThetaDeg - angleXYdeg);
                        double angleZrad = Math.acos(spacecraftDirection.getZ());
                        double angleZdeg = Math.toDegrees(angleZrad);
                        bodyRotationsX.get(name).setAngle(90.0 - camPhiDeg - angleZdeg);
                    }
                    else {
                        // Rotate spacecraft such that parabolic antenna is directed towards the Earth
                        // The models of the spacecraft are constructed such that the parabolic antenna is
                        // directed towards the camera when not rotated
                        Vector3D positionSpacecraft = solarSystem.getParticle(name).getPosition();
                        Vector3D directionToEarth = positionSpacecraft.direction(positionEarth);
                        double angleXYrad = Math.atan2(directionToEarth.getY(), directionToEarth.getX());
                        double angleXYdeg = Math.toDegrees(angleXYrad);
                        bodyRotationsY.get(name).setAngle(camThetaDeg - angleXYdeg);
                        double angleZrad = Math.acos(directionToEarth.getZ());
                        double angleZdeg = Math.toDegrees(angleZrad);
                        bodyRotationsX.get(name).setAngle(90.0 - camPhiDeg - angleZdeg);
                    }
                } else {
                    // Rotate around y-axis to visualize revolution
                    double siderealRotationPeriodHours = solarSystemParameters.getSiderealRotationalPeriod(name);
                    double siderealRotationPeriodDays = siderealRotationPeriodHours / 24.0;
                    double nrRevolutions = nrDaysPastJ2000 / siderealRotationPeriodDays;
                    double revolutionAngleDeg = -(nrRevolutions % 1.0) * 360.0;
                    if ("Earth".equals(name)) {
                        // When the Earth is observed from the Sun, the Greenwich meridian
                        // should be in the center at noon (12:00:00) UTC.
                        // However, because of Earth's uneven angular velocity in its
                        // elliptical orbit and its axial tilt, noon (12:00:00) GMT is
                        // rarely the exact moment the Sun crosses the Greenwich meridian and
                        // reaches its highest point in the sky there.
                        // https://en.wikipedia.org/wiki/Greenwich_Mean_Time
                        revolutionAngleDeg += 81.0;
                    }
                    if ("Moon".equals(name)) {
                        // Correction to see the front side of the Moon when viewing the full Moon
                        revolutionAngleDeg -= 30.0;
                    }
                    if ("Jupiter".equals(name)) {
                        // Correction to see Red Spot at the right position
                        // https://skyandtelescope.org/observing/celestial-objects-to-watch/jupiters-moons-javascript-utility/#
                        revolutionAngleDeg += 150.0;
                    }
                    if ("Pluto".equals(name)) {
                        // Correction to see Pluto's Big Heart from New Horizons July 13, 20.00
                        // https://www.nasa.gov/feature/new-horizons-spacecraft-displays-pluto-s-big-heart-0
                        revolutionAngleDeg += 180.0;
                    }
                    // Rotate around y-axis
                    bodyRotationsRevolution.get(name).setAngle(revolutionAngleDeg);

                    // Rotate to visualize obliquity
                    double[] rotationPoleEquatorial = solarSystemParameters.getRotationPole(name);
                    double[] rotationPoleEcliptic = EphemerisUtil.equatorialToEcliptic(rotationPoleEquatorial);
                    double lambda = rotationPoleEcliptic[0];
                    double beta = rotationPoleEcliptic[1];
                    double thetaDeg = lambda;
                    double phiDeg = (90.0 - beta);
                    double thetaRad = Math.toRadians(thetaDeg);
                    double sinTheta = Math.sin(thetaRad);
                    double cosTheta = Math.cos(thetaRad);
                    Point3D rotationAxisObliquity = new Point3D(cosTheta, 0.0, sinTheta);
                    bodyRotationsObliquity.get(name).setAxis(rotationAxisObliquity);
                    bodyRotationsObliquity.get(name).setAngle(phiDeg);

                    // Rotate around y-axis and x-axis to take direction of camera into account
                    bodyRotationsY.get(name).setAngle(camThetaDeg);
                    bodyRotationsX.get(name).setAngle(camPhiDeg);

                    // Shadow of Jupiter and Saturn
                    if ("Jupiter".equals(name) || "Saturn".equals(name)) {
                        String shadowName = "shadow" + name;
                        Vector3D positionSun = solarSystem.getParticle("Sun").getPosition();
                        Vector3D positionPlanet = solarSystem.getParticle(name).getPosition();
                        Vector3D directionToSun = positionPlanet.direction(positionSun);
                        double dirX = directionToSun.getX();
                        double dirY = directionToSun.getY();
                        double dirZ = directionToSun.getZ();
                        double thetaDegShadow = Math.toDegrees(Math.atan2(dirY,dirX));
                        double phiDegShadow   = Math.toDegrees(Math.acos(dirZ));
                        double thetaRadShadow = Math.toRadians(thetaDegShadow);
                        double sinThetaShadow = Math.sin(thetaRadShadow);
                        double cosThetaShadow = Math.cos(thetaRadShadow);
                        Point3D rotationAxisShadow = new Point3D(cosThetaShadow, 0.0, sinThetaShadow);
                        bodyRotationsObliquity.get(shadowName).setAxis(rotationAxisShadow);
                        bodyRotationsObliquity.get(shadowName).setAngle(phiDegShadow);
                        bodyRotationsY.get(shadowName).setAngle(camThetaDeg);
                        bodyRotationsX.get(shadowName).setAngle(camPhiDeg);
                    }
                }
            }
        }
    }

    /**
     * Translate and rotate position for observation from camera position and
     * camera direction. It is assumed that the camera is located in the origin (0,0,0)
     * and viewing in the direction of the positive z-axis.
     * @param position 3D position in m
     * @return translated and rotated position
     */
    private Vector3D translateRotatePosition(Vector3D cameraPosition, Vector3D cameraDirection, Vector3D position) {

        // Upward viewing orientation of camera is towards celestial north pole
        double uvoX = 0.0;
        double uvoY = 0.0;
        double uvoZ = 1.0;
        Vector3D upwardViewingOrientation = (new Vector3D(uvoX,uvoY,uvoZ)).normalize();

        // Camera coordinates
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D zc = (cameraDirection.scalarProduct(-1.0)).normalize();
        Vector3D xc = (upwardViewingOrientation.crossProduct(zc)).normalize();
        Vector3D yc = zc.crossProduct(xc);

        // Translate
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D positionTranslated = position.minus(cameraPosition);

        // Rotate
        // https://www.ntu.edu.sg/home/ehchua/programming/opengl/cg_basicstheory.html
        Vector3D positionRotated = positionTranslated.rotate(xc,yc,zc);
        return positionRotated;
    }


    /**
     * Update positions of the objects representing the Solar System bodies.
     */
    private void updateObjectPositions(Vector3D cameraPosition, Vector3D cameraDirection, Vector3D lookAtPosition) {

        Vector3D positionSun = solarSystem.getParticle("Sun").getPosition();
        Vector3D positionEarth = solarSystem.getParticle("Earth").getPosition();
        Vector3D positionMoon = solarSystem.getParticle("Moon").getPosition();
        Vector3D positionJupiter = solarSystem.getParticle("Jupiter").getPosition();
        double diameterJupiter = solarSystemParameters.getDiameter("Jupiter");
        for (String name : bodies.keySet()) {
            Node node = bodies.get(name);
            if (node.isVisible()) {
                Vector3D positionBody;
                if (name.startsWith("shadow")) {
                    // Shadow of Galilean moon
                    String moonName = name.replaceFirst("shadow", "");
                    Vector3D positionGalileanMoon = solarSystem.getParticle(moonName).getPosition();
                    Vector3D directionGalileanMoon = positionSun.direction(positionGalileanMoon);
                    Vector3D positionShadow =
                            VectorUtil.computeIntersectionLineSphere(directionGalileanMoon, positionSun, positionJupiter, diameterJupiter);
                    // Note that positionShadow is null pointer when there is no intersection
                    // Do not show the shadow if the moon is further from the Sun than Jupiter
                    // Assume Sun is located at position (0,0,0)
                    if (positionShadow == null || positionGalileanMoon.magnitude() > positionJupiter.magnitude()) {
                        positionBody = new Vector3D();
                        bodies.get(name).setVisible(false);
                    } else {
                        positionBody = positionShadow;
                        bodies.get(name).setVisible(true);
                    }
                } else {
                    // Not a shadow
                    positionBody = solarSystem.getParticle(name).getPosition();
                }

                // Translate and rotate
                Vector3D positionBodyTranslated = translateRotatePosition(cameraPosition, cameraDirection, positionBody);

                // Set position
                node.setTranslateX(screenX(positionBodyTranslated));
                node.setTranslateY(screenY(positionBodyTranslated));
                node.setTranslateZ(screenZ(positionBodyTranslated));
            }
        }

        // Rings of Saturn
        ringSaturn.setTranslateX(saturn.getTranslateX());
        ringSaturn.setTranslateY(saturn.getTranslateY());
        ringSaturn.setTranslateZ(saturn.getTranslateZ());

        // Rings of Uranus
        ringUranus.setTranslateX(uranus.getTranslateX());
        ringUranus.setTranslateY(uranus.getTranslateY());
        ringUranus.setTranslateZ(uranus.getTranslateZ());

        // Shadow of Jupiter
        double radiusJupiter = 0.5*solarSystemParameters.getDiameter("Jupiter");
        double lengthShadowJupiter = SHADOWJUPITERFACTOR*radiusJupiter;
        Vector3D positionShadowJupiter = positionJupiter.plus(positionJupiter.normalize().scalarProduct(0.5*lengthShadowJupiter));
        Vector3D positionShadowJupiterTranslated = translateRotatePosition(cameraPosition, cameraDirection, positionShadowJupiter);
        shadowJupiter.setTranslateX(screenX(positionShadowJupiterTranslated));
        shadowJupiter.setTranslateY(screenY(positionShadowJupiterTranslated));
        shadowJupiter.setTranslateZ(screenZ(positionShadowJupiterTranslated));

        // Shadow of Saturn
        Vector3D positionSaturn = solarSystem.getParticle("Saturn").getPosition();
        double radiusSaturn = 0.5*solarSystemParameters.getDiameter("Saturn");
        double lengthShadowSaturn = SHADOWSATURNFACTOR*radiusSaturn;
        Vector3D positionShadowSaturn = positionSaturn.plus(positionSaturn.normalize().scalarProduct(0.5*lengthShadowSaturn));
        Vector3D positionShadowSaturnTranslated = translateRotatePosition(cameraPosition, cameraDirection, positionShadowSaturn);
        shadowSaturn.setTranslateX(screenX(positionShadowSaturnTranslated));
        shadowSaturn.setTranslateY(screenY(positionShadowSaturnTranslated));
        shadowSaturn.setTranslateZ(screenZ(positionShadowSaturnTranslated));

        // Small red sphere representing location on Earth
        if (locationOnEarth.isVisible()) {
            Vector3D positionLocation = EphemerisUtil.computePositionFromLatitudeLongitudeHeight(
                    latitude, longitude, LOCATION_HEIGHT, solarSystem.getSimulationDateTime());
            positionLocation.addVector(positionEarth);
            positionLocation = translateRotatePosition(cameraPosition, cameraDirection, positionLocation);
            locationOnEarth.setTranslateX(screenX(positionLocation));
            locationOnEarth.setTranslateY(screenY(positionLocation));
            locationOnEarth.setTranslateZ(screenZ(positionLocation));
        }

        // Point light represents light coming from the Sun
        Vector3D positionPointLight = new Vector3D(positionSun);
        if ("Sun".equals(selectedBody)) {
            // Place point light in between the Sun and the camera, but close to the Sun
            positionPointLight.addVector((
                    positionSun.direction(cameraPosition).
                            scalarProduct(0.05 * SolarSystemParameters.ASTRONOMICALUNIT)));
        }
        Vector3D positionPointLightTranslated =
                translateRotatePosition(cameraPosition,cameraDirection,positionPointLight);
        pointLight.setTranslateX(screenX(positionPointLightTranslated));
        pointLight.setTranslateY(screenY(positionPointLightTranslated));
        pointLight.setTranslateZ(screenZ(positionPointLightTranslated));
        pointLight.setColor(Color.WHITE); // Color of point light may change during lunar eclipse

        // Check for Solar Eclipse
        coronaSun.setTranslateX(sun.getTranslateX());
        coronaSun.setTranslateY(sun.getTranslateY());
        coronaSun.setTranslateZ(sun.getTranslateZ());
        // Rotate the corona such that it is facing the camera
        coronaSun.setRotationAxis(Rotate.X_AXIS);
        coronaSun.setRotate(90.0);
        if ("Sun".equals(selectedBody)) {
            Vector3D positionObservation = EphemerisUtil.computePositionFromLatitudeLongitudeHeight(
                    latitude,longitude,0.0, solarSystem.getSimulationDateTime());
            positionObservation.addVector(positionEarth);
            Vector3D directionSunObservation = positionObservation.direction(positionSun);
            Vector3D directionMoonObservation = positionObservation.direction(positionMoon);
            double angle = directionMoonObservation.angleDeg(directionSunObservation);
            boolean solarEclipse = false;
            if (angle < 0.01) {
                // Check for annular solar eclipse
                double diameterSun = solarSystemParameters.getDiameter("Sun");
                double diameterMoon = solarSystemParameters.getDiameter("Moon");
                double distanceSun = positionObservation.euclideanDistance(positionSun);
                double distanceMoon = positionObservation.euclideanDistance(positionMoon);
                double relativeDiameterSun = diameterSun / distanceSun;
                double relativeDiameterMoon = diameterMoon / distanceMoon;
                if (relativeDiameterMoon >= 0.99*relativeDiameterSun) {
                    solarEclipse = true;
                }
            }
            if (solarEclipse) {
                sun.setVisible(false);
                coronaSun.setVisible(true);
            }
            else {
                sun.setVisible(true);
                coronaSun.setVisible(false);
            }
        }

        // Check for Lunar Eclipse
        if ("Moon".equals(selectedBody)) {
            // Diameter of Earth's shadow at the Moon's distance
            double diameterSun = solarSystemParameters.getDiameter("Sun");
            double diameterEarth = solarSystemParameters.getDiameter("Earth");
            double distanceSunEarth = positionSun.euclideanDistance(positionEarth);
            double distanceEarthMoon = positionEarth.euclideanDistance(positionMoon);
            double distanceSunShadowEarth = distanceSunEarth + distanceEarthMoon;
            double diameterFactor = (diameterSun - diameterEarth) / distanceSunEarth;
            double diameterShadow = diameterSun - diameterFactor * distanceSunShadowEarth;

            // Set position of cylinder representing Earth's shadow
            Vector3D directionEarth = positionEarth.normalize();
            Vector3D positionShadowEarth = positionEarth.plus(directionEarth.scalarProduct(distanceEarthMoon));
            Vector3D positionShadowEarthTranslated = translateRotatePosition(cameraPosition, cameraDirection, positionShadowEarth);
            shadowEarth.setTranslateX(screenX(positionShadowEarthTranslated));
            shadowEarth.setTranslateY(screenY(positionShadowEarthTranslated));
            shadowEarth.setTranslateZ(screenZ(positionShadowEarthTranslated));

            // Set radius and height for cylinder representing Earth's shadow
            // Set height such that face of cylinder is in front of the Moon
            // Set radius such that bloodmoon appears when the Moon is fully covered by Earth's shadow
            double radiusShadow = 0.5 * (diameterShadow/diameterEarth) * screenDiameter("Earth");
            shadowEarth.setRadius(0.99*radiusShadow);
            shadowEarth.setHeight(2.2*moon.getRadius());

            // Rotate cylinder such that it is facing the camera
            shadowEarth.setRotationAxis(Rotate.X_AXIS);
            shadowEarth.setRotate(90.0);

            // Blood moon is visible during total Lunar Eclipse
            double diameterMoon = solarSystemParameters.getDiameter("Moon");
            double radius = 0.5*(diameterShadow - diameterMoon);
            boolean lunarEclipse = (positionMoon.euclideanDistance(positionShadowEarth) < radius);
            if (lunarEclipse) {
                shadowEarth.setMaterial(materialTransparent);
                pointLight.setColor(Color.ORANGERED);
            }
            else {
                shadowEarth.setMaterial(materialShadowEarth);
                pointLight.setColor(Color.WHITE);
            }
        }
    }

    /**
     * Set camera position and direction.
     * Adjust position and direction of camera for mouse input.
     * @param cameraPosition position of the camera within Solar System
     * @param lookAtPosition position to look at within Solar System
     */
    private void lookAt(Vector3D cameraPosition, Vector3D lookAtPosition) {

        // Position (px, py, pz) is camera position relative to look-at position
        Vector3D relativePosition = cameraPosition.minus(lookAtPosition);
        double px = relativePosition.getX();
        double py = relativePosition.getY();
        double pz = relativePosition.getZ();

        // Convert (px, py, pz) to spherical coordinates (rho, theta, phi)
        double rho = Math.sqrt(px*px + py*py + pz*pz); // rho >= 0 [m]
        double thetaRad = Math.atan2(py,px);           // -pi <= theta <= pi [rad]
        double phiRad = Math.acos(pz/rho);             // 0 <= phi <= pi [rad]

        // Adjust theta and phi for mouse input
        double mouseThetaRad = Math.toRadians(angleX);
        double mousePhiRad = Math.toRadians(angleY);
        thetaRad += mouseThetaRad;
        phiRad -= mousePhiRad;
        phiRad = Math.max(0.01, Math.min(Math.PI-0.01,phiRad));

        // Convert (rho, theta, phi) to cartesian coordinates (qx, qy, qz)
        double sinTheta = Math.sin(thetaRad);
        double cosTheta = Math.cos(thetaRad);
        double sinPhi = Math.sin(phiRad);
        double cosPhi = Math.cos(phiRad);
        double qx = rho * cosTheta * sinPhi;
        double qy = rho * sinTheta * sinPhi;
        double qz = rho * cosPhi;

        // Camera position adjusted for mouse input
        cameraPosition = lookAtPosition.plus(new Vector3D(qx,qy,qz));

        // Direction of camera from position to look at
        Vector3D cameraDirection = lookAtPosition.direction(cameraPosition);
        if (viewMode.equals(SolarSystemViewMode.TELESCOPE)) {
            cameraPosition = lookAtPosition.plus(cameraDirection.scalarProduct(0.5 * SCREENSCALE));
        }

        // Update the positions of all visible objects
        updateObjectPositions(cameraPosition, cameraDirection, lookAtPosition);

        // Update the rotations of all visible bodies
        updateBodyRotations(cameraDirection);
    }

    /**
     * View the Earth from the position of the Sun
     * @throws SolarSystemException
     */
    private void viewFromSunToEarth() throws SolarSystemException {

        // Position of the Earth
        Vector3D earthPosition = solarSystem.getPosition("Earth");

        // Position of the Sun
        Vector3D sunPosition = solarSystem.getPosition("Sun");

        // Position of the camera
        Vector3D cameraPosition = new Vector3D(sunPosition);

        // Distance between the camera and the Earth
        double distance = cameraPosition.euclideanDistance(earthPosition);

        // Let camera look in the direction of the Earth
        Vector3D lookAtPosition = new Vector3D(earthPosition);
        lookAt(cameraPosition, lookAtPosition);

        // Set field of view of camera
        double diameterEarth = diameterBody("Earth");
        double fieldOfView = diameterEarth / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView, distance - 1.0E09, distance + 1.0E09);
    }


    /**
     * View the Earth-Moon system from the position of the Sun.
     * @throws SolarSystemException
     */
    private void viewEarthMoonSystem() throws SolarSystemException {

        // Position of the Earth
        Vector3D earthPosition = solarSystem.getPosition("Earth");

        // Position of the Moon
        Vector3D moonPosition = solarSystem.getPosition("Moon");

        // Position of the Sun
        Vector3D sunPosition = solarSystem.getPosition("Sun");

        // Position of the camera
        Vector3D cameraPosition = new Vector3D(sunPosition);

        // Distance between the camera and the Earth
        double distance = cameraPosition.euclideanDistance(earthPosition);

        // Let camera look in the direction of the Earth
        Vector3D lookAtPosition = new Vector3D(earthPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Set field of view of camera
        double diameterEarthMoonSystem = earthPosition.euclideanDistance(moonPosition);
        double fieldOfView = 0.6 * (diameterEarthMoonSystem / FIELDOFVIEWFACTORTELESCOPE);
        setCameraSettings(fieldOfView, distance - 1.0E09, distance + 1.0E09);
    }

    /**
     * View from the surface of the Earth towards the Sun.
     * @throws SolarSystemException
     */
    private void viewFromEarthSurfaceToSun() throws SolarSystemException {

        // Set camera on the surface of the Earth
        Vector3D geocentricPosition =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 0.0,
                        solarSystem.getSimulationDateTime());
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D cameraPosition = earthPosition.plus(geocentricPosition);

        // Adjust radius of sphere representing the Sun for Solar eclipse,
        // Mercury transit or Venus transit
        Vector3D sunPosition = solarSystem.getPosition("Sun");
        Vector3D sunDirection = cameraPosition.direction(sunPosition);
        double sunDistance = cameraPosition.euclideanDistance(sunPosition);
        double nearDistance = 0.9*sunDistance;
        double farDistance = sunDistance;
        double sunRadiusFactor = 1.0;
        if (moon.isVisible()) {
            // Correction of Sun radius for Solar eclipse
            Vector3D moonPosition = solarSystem.getPosition("Moon");
            double moonDistance = cameraPosition.euclideanDistance(moonPosition);
            sunRadiusFactor = CORRECTIONSUNRADIUSSOLARECLIPSE*(moonDistance/sunDistance);
            nearDistance = moonDistance;
        }
        if (mercury.isVisible()) {
            Vector3D mercuryPosition = solarSystem.getPosition("Mercury");
            Vector3D mercuryDirection = cameraPosition.direction(mercuryPosition);
            double mercuryAngleDeg = mercuryDirection.angleDeg(sunDirection);
            if (mercuryAngleDeg < 1.0) {
                // Correction of Sun radius for Mercury transit
                double mercuryDistance = cameraPosition.euclideanDistance(mercuryPosition);
                sunRadiusFactor = CORRECTIONSUNRADIUSMERCURYTRANSIT*(mercuryDistance/sunDistance);
                nearDistance = mercuryDistance;
            }
        }
        if (venus.isVisible()) {
            Vector3D venusPosition = solarSystem.getPosition("Venus");
            Vector3D venusDirection = cameraPosition.direction(venusPosition);
            double venusAngleDeg = venusDirection.angleDeg(sunDirection);
            if (venusAngleDeg < 1.0) {
                // Correction of Sun radius for Venus transit
                double venusDistance = cameraPosition.euclideanDistance(venusPosition);
                sunRadiusFactor = CORRECTIONSUNRADIUSVENUSTRANSIT*(venusDistance/sunDistance);
                nearDistance = venusDistance;
            }
        }
        sun.setRadius(sunRadiusFactor * 0.5*screenDiameter("Sun"));
        coronaSun.setRadius(2.04*sun.getRadius());

        // Let camera look in the direction of the Sun
        Vector3D lookAtPosition = new Vector3D(sunPosition);
        lookAt(cameraPosition, lookAtPosition);

        // Set field of view of camera
        double diameterSun = diameterBody("Sun");
        double fieldOfView = sunRadiusFactor * diameterSun / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView, nearDistance, farDistance);
    }

    /**
     * View from the surface of the Earth towards the selected body
     * @throws SolarSystemException
     */
    private void viewFromEarthSurfaceToSelectedBody() throws SolarSystemException {

        // Set camera on the surface of the Earth
        Vector3D geocentricPosition =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 0.0,
                        solarSystem.getSimulationDateTime());
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D cameraPosition = earthPosition.plus(geocentricPosition);

        // Determine position of the body to look at
        Vector3D bodyPosition;
        try {
            bodyPosition = solarSystem.getPosition(selectedBody);
        }
        catch (SolarSystemException e) {
            // A SolarSystemException 'Particle with name ... does not exist' may be thrown
            // This may occur when the user deselects the planet system in the main application
            this.selectedBody = "Sun";
            bodyPosition = solarSystem.getPosition(selectedBody);
        }

        // Let camera look in the direction of the selected body
        Vector3D lookAtPosition = new Vector3D(bodyPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Distance between the camera and the selected body
        double distance = cameraPosition.euclideanDistance(bodyPosition);

        // Set field of view of the camera
        double diameter = diameterBody(selectedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView, distance - 5.0E9, distance + 5.0E9);
    }

    /**
     * View from the surface of the Earth towards the observed body
     * @throws SolarSystemException
     */
    private void viewFromEarthSurfaceToObservedBody() throws SolarSystemException {

        // Set camera on the surface of the Earth
        Vector3D geocentricPosition =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 0.0,
                        solarSystem.getSimulationDateTime());
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D cameraPosition = earthPosition.plus(geocentricPosition);

        // Let camera look in the direction of the observed body
        Vector3D bodyPosition;
        try {
            bodyPosition = solarSystem.getPosition(observedBody);
        }
        catch (SolarSystemException e) {
            // A SolarSystemException 'Particle with name ... does not exist' may be thrown
            // This may occur when the user deselects the planet system in the main application
            this.observedBody = "Sun";
            bodyPosition = solarSystem.getPosition(observedBody);
        }
        Vector3D lookAtPosition = new Vector3D(bodyPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Distance between the camera and the observed body
        double distance = cameraPosition.euclideanDistance(bodyPosition);

        // Set field of view of the camera
        double diameter = diameterBody(observedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView, distance - 5.0E9, distance + 5.0E9);
    }

    /**
     * View from the surface of the Earth towards the Moon
     * @throws SolarSystemException
     */
    private void viewFromEarthSurfaceToMoon() throws SolarSystemException {

        // Set camera on the surface of the Earth
        Vector3D geocentricPosition =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 0.0,
                        solarSystem.getSimulationDateTime());
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D cameraPosition = earthPosition.plus(geocentricPosition);

        // Let camera look in direction of the Moon
        Vector3D moonPosition = solarSystem.getPosition("Moon");
        Vector3D lookAtPosition = new Vector3D(moonPosition);
        lookAt(cameraPosition, lookAtPosition);

        // Distance from camera position to the Moon
        double distance = cameraPosition.euclideanDistance(moonPosition);

        // Set field of view of the camera
        double diameter = diameterBody(selectedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView, distance - 1.0E7, distance + 1.0E7);
    }

    /**
     * View from the center of the Earth towards the observed body
     * @throws SolarSystemException
     */
    private void viewFromEarthCenterToObservedBody() throws SolarSystemException {

        // Set camera at position of the Earth
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D cameraPosition = new Vector3D(earthPosition);

        // Let camera look in the direction of the observed body
        Vector3D bodyPosition;
        try {
            bodyPosition = solarSystem.getPosition(observedBody);
        }
        catch (SolarSystemException e) {
            // A SolarSystemException 'Particle with name ... does not exist' may be thrown
            // This may occur when the user deselects the planet system in the main application
            this.observedBody = "Sun";
            bodyPosition = solarSystem.getPosition(observedBody);
        }
        Vector3D lookAtPosition = new Vector3D(bodyPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Distance between the camera and the observed body
        double distance = cameraPosition.euclideanDistance(bodyPosition);

        // Set field of view of the camera
        double diameter = diameterBody(observedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView, distance, distance);
    }

    /**
     * View from spacecraft.
     * @throws SolarSystemException
     */
    private void viewFromSpacecraft() throws SolarSystemException {

        // Selected body is spacecraft
        Vector3D spacecraftPosition = solarSystem.getPosition(selectedBody);
        Vector3D cameraPosition = new Vector3D(spacecraftPosition);

        // Let camera look in the direction of the body being observed
        Vector3D bodyPosition = solarSystem.getPosition(observedBody);
        Vector3D lookAtPosition = new Vector3D(bodyPosition);
        if ("New Horizons".equals(selectedBody) && "Ultima Thule".equals(observedBody)) {
            // https://en.wikipedia.org/wiki/New_Horizons
            // Ultima Thule fly by January 1, 2019, 05:33 UTC at a distance of 3,500 km
            // The shape of Ultima Thule is about 200 times larger compared to other objects
            // To get a good view of Ultima Thule the distance between the camera and the shape
            // should be increased
            double distanceKm = spacecraftPosition.euclideanDistance(bodyPosition)/1000.0;
            if (distanceKm < 3.0E5) {
                Vector3D spacecraftDirection = lookAtPosition.direction(spacecraftPosition);
                double factor = Math.min(1.0,(3.0E5 - distanceKm)/3.0E5);
                double shiftDistance = 50*factor*diameterBody(observedBody);
                cameraPosition.addVector(spacecraftDirection.scalarProduct(shiftDistance));
            }
        }
        lookAt(cameraPosition,lookAtPosition);

        // Set field of view of the camera
        double diameter = diameterBody(observedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORSPACECRAFT;
        double distanceFromCenter = cameraPosition.euclideanDistance(bodyPosition);
        double distanceFromSurface = distanceFromCenter - 0.5*diameter;
        if ("Rosetta".equals(selectedBody) && distanceFromSurface < 1.0E11) {
            // Rosetta comes close to the surface of the Earth and Mars
            // Increase field of view depending on the distance to the surface
            fieldOfView += (1.0E11 - distanceFromSurface)/5.0E9;
        }
        fieldOfView = Math.min(Math.max(fieldOfView,4.0),90.0);
        double nearDistance = Math.max(1.0E07,distanceFromSurface - 5.0E09);
        if (distanceFromSurface < 1.0E09) {
            nearDistance = Math.max(1.0E05,distanceFromSurface - 5.0E09);
        }
        double farDistance = distanceFromCenter + 5.0E09;
        setCameraSettings(fieldOfView, nearDistance, farDistance);
    }

    /**
     * View from International Space Station.
     * @throws SolarSystemException
     */
    private void viewFromISS() throws SolarSystemException {
        // Set camera in the direction of the ISS in the X,Y plane
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D issPosition = solarSystem.getPosition("ISS");
        Vector3D directionISS = earthPosition.direction(issPosition);
        Vector3D cameraPosition = earthPosition.plus(directionISS.scalarProduct(1.0E7));

        // Let camera look in the direction of the center of the Earth
        Vector3D lookAtPosition = new Vector3D(earthPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Set field of view of the camera
        double fieldOfView = 40.0;   // 40 degrees
        double nearDistance = 1.E05; // 100 km
        double farDistance = 1.E07;  // 10000 km
        setCameraSettings(fieldOfView, nearDistance, farDistance);
    }

    /**
     * View from Apollo.
     * @throws SolarSystemException
     */
    private void viewFromApollo() throws SolarSystemException {

        // Selected body is spacecraft
        // Set camera at the position of the spacecraft
        Vector3D spacecraftPosition = solarSystem.getPosition(selectedBody);
        Vector3D cameraPosition = new Vector3D(spacecraftPosition);

        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D moonPosition = solarSystem.getPosition("Moon");
        double distanceToEarthKm = spacecraftPosition.euclideanDistance(earthPosition)/1.0E3;
        double distanceToMoonKm = spacecraftPosition.euclideanDistance(moonPosition)/1.0E3;
        if (distanceToMoonKm < 4000.0) {
            // Spacecraft is close to Moon
            // Let camera look in the direction the spacecraft is flying
            Vector3D spacecraftVelocity = solarSystem.getVelocity("Apollo 8");
            Vector3D lookAtPosition = spacecraftPosition.plus(spacecraftVelocity.normalize());
            lookAt(cameraPosition,lookAtPosition);

            // Set field of view of the camera for Earthrise
            double fieldOfView = 30.0;   // 30 degrees
            double nearDistance = 1.E05; // 100 km
            double farDistance = distanceToEarthKm*1.0E03; // distance to Earth in m
            setCameraSettings(fieldOfView, nearDistance, farDistance);
            zRotateCamera.setAngle(-100.0);
            yRotateCamera.setAngle(25.0);
            xRotateCamera.setAngle(0.0);
        }
        else {
            String lookAtBody = "Earth";
            Vector3D lookAtPosition = solarSystem.getPosition(lookAtBody);
            if (distanceToEarthKm < 8000.0) {
                Vector3D spacecraftDirection = lookAtPosition.direction(spacecraftPosition);
                cameraPosition.addVector(spacecraftDirection.scalarProduct(1000.0*(8000.0 - distanceToEarthKm)));
            }
            lookAt(cameraPosition,lookAtPosition);
            if (distanceToEarthKm < 8000.0) {
                double fieldOfView = Math.max(30.0,30.0 + 0.01*(8000.0 - distanceToEarthKm));
                double nearDistance = 1.0E05; // 100 km
                double farDistance = distanceToEarthKm*1.0E03; // distance to Earth in m
                setCameraSettings(fieldOfView, nearDistance, farDistance);
            }
            else {
                double fieldOfView = 30.0;
                double nearDistance = 1.0E06; // 1000 km
                double farDistance = distanceToEarthKm*1.0E03; // distance to Earth in m
                setCameraSettings(30.0, nearDistance, farDistance);
            }
        }
    }

    /**
     * Update the 3D scene.
     * @param bodiesShown   Names of bodies to be shown
     * @param selectedBody  Selected body
     * @param observedBody  Body that is being observed
     * @param viewMode      Selected view mode
     * @param latitude      latitude of location on the Earth [degrees]
     * @param longitude     longitude of location on the Earth [degrees]
     */
    public void update(Set<String> bodiesShown, String selectedBody, String observedBody,
                       SolarSystemViewMode viewMode, double latitude, double longitude) {

        // Set visibility of the objects representing the Solar System bodies
        shadowIo.setVisible(false);
        shadowEuropa.setVisible(false);
        shadowGanymede.setVisible(false);
        shadowCallisto.setVisible(false);
        for (String bodyName : bodies.keySet()) {
            if (bodiesShown.contains(bodyName)) {
                bodies.get(bodyName).setVisible(true);
            } else {
                bodies.get(bodyName).setVisible(false);
            }
        }

        // Shadows of the Galilean Moons
        shadowIo.setVisible(io.isVisible());
        shadowEuropa.setVisible(europa.isVisible());
        shadowGanymede.setVisible(ganymede.isVisible());
        shadowCallisto.setVisible(callisto.isVisible());

        // Rings of Saturn and Uranus
        ringSaturn.setVisible(saturn.isVisible());
        ringUranus.setVisible(uranus.isVisible());

        // Shadow of Saturn
        shadowSaturn.setVisible(saturn.isVisible());

        // Shadow of the Earth to visualize a blood moon
        shadowEarth.setVisible("Moon".equals(selectedBody));

        // Corona of the Sun to visualize total Solar eclipse
        coronaSun.setVisible(false);

        // Reset zoom parameter when another body is selected in the main app
        if (!this.selectedBody.equals(selectedBody)) {
            this.angleX = 0.0;
            this.angleY = 0.0;
            this.zoom = INITZOOM;
            if (selectedBody == null || "".equals(selectedBody)) {
                this.selectedBody = "Sun";
            }
            else {
                this.selectedBody = selectedBody;
            }
        }

        // Set body to be observed
        if (!this.observedBody.equals(observedBody)) {
            this.angleX = 0.0;
            this.angleY = 0.0;
            this.zoom = INITZOOM;
            if (observedBody != null && !"".equals(observedBody)) {
                this.observedBody = observedBody;
            }
            else {
                this.observedBody = "Earth";
            }
        }

        // Set view mode
        if (spacecraftNames.contains(this.selectedBody)) {
            this.viewMode = viewMode;
        }
        else {
            this.viewMode = SolarSystemViewMode.TELESCOPE;
        }

        // Set location
        this.latitude = latitude;
        this.longitude = longitude;

        // Check for impact of Shoemaker-Levy
        if (bodiesShown.contains("Shoemaker-Levy 9")) {
            Vector3D shoemakerLevyPosition = solarSystem.getParticle("Shoemaker-Levy 9").getPosition();
            Vector3D jupiterPosition = solarSystem.getParticle("Jupiter").getPosition();
            double distance = shoemakerLevyPosition.euclideanDistance(jupiterPosition);
            if (distance < 0.49*solarSystemParameters.getDiameter("Jupiter")) {
                shoemaker.setRadius(0.1*jupiter.getRadius());
            }
            else {
                shoemaker.setRadius(screenDiameter("Shoemaker-Levy 9"));
            }
        }

        // Set radius of Earth and Moon to their normal values
        // The sizes of the Earth and the Moon are changed when viewing the Earth-Moon system
        if (!"EarthMoonBarycenter".equals(selectedBody)) {
            earth.setRadius(0.5*screenDiameter("Earth"));
            moon.setRadius(0.5*screenDiameter("Moon"));
        }

        // Display name of observed body
        refreshDisplayObservedBody();

        // Display current simulation date/time
        refreshDisplayDateTime();

        // Update the position and orientation of all visible objects
        try {
            locationOnEarth.setVisible(false);
            if (viewMode.equals(SolarSystemViewMode.FROMSPACECRAFT)) {
                // View from spacecraft
                bodies.get(this.selectedBody).setVisible(false);
                switch (this.selectedBody) {
                    case "ISS":
                        earth.setVisible(true);
                        iss.setVisible(true);
                        viewFromISS();
                        break;
                    case "Apollo 8":
                        earth.setVisible(true);
                        moon.setVisible(true);
                        viewFromApollo();
                        break;
                    default:
                        viewFromSpacecraft();
                        break;
                }
            }
            else {
                // Telescope view
                switch (this.selectedBody) {
                    case "Sun":
                        earth.setVisible(false);
                        moon.setVisible(true);
                        shadowEarth.setVisible(false);
                        viewFromEarthSurfaceToSun();
                        break;
                    case "Earth":
                        sun.setVisible(false);
                        viewFromSunToEarth();
                        break;
                    case "Moon":
                        earth.setVisible(false);
                        moon.setVisible(true);
                        shadowEarth.setVisible(true);
                        viewFromEarthSurfaceToMoon();
                        break;
                    case "EarthMoonBarycenter":
                        earth.setRadius(6.0 * screenDiameter("Earth"));
                        moon.setRadius(6.0 * screenDiameter("Moon"));
                        earth.setVisible(true);
                        moon.setVisible(true);
                        shadowEarth.setVisible(false);
                        viewEarthMoonSystem();
                        break;
                    case "Pioneer 10":
                    case "Pioneer 11":
                    case "Voyager 1":
                    case "Voyager 2":
                    case "New Horizons":
                    case "Rosetta":
                    case "Cassini":
                        if ("Earth".equals(observedBody)) {
                            sun.setVisible(false);
                            viewFromSunToEarth();
                        }
                        else {
                            earth.setVisible(false);
                            //viewFromEarthCenterToObservedBody(); // LET OP
                            viewFromEarthSurfaceToSelectedBody();
                        }
                        break;
                    default:
                        if (bodies.keySet().contains(this.selectedBody)) {
                            earth.setVisible(false);
                            shadowEarth.setVisible(false);
                            viewFromEarthSurfaceToSelectedBody();
                        } else {
                            sun.setVisible(false);
                            earth.setVisible(true);
                            moon.setVisible(true);
                            viewFromSunToEarth();
                        }
                        break;
                }
            }
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }
    }
}

