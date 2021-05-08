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

import java.text.DecimalFormat;
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
    private static final double DIAMETERNEWHORIZONS = 8.0E5; //  800 km
    private static final double DIAMETERGIOTTO      = 2.5E5; //  250 km
    private static final double DIAMETERROSETTA     = 2.5E5; //  250 km
    private static final double DIAMETERISS         = 2.0E5; //  200 km
    private static final double DIAMETERAPOLLO      = 2.0E5; //  200 km

    // Decimal format
    private static final DecimalFormat DF_ONEDECIMAL = new DecimalFormat("0.0");
    private static final DecimalFormat DF_MAXTWODECIMALS = new DecimalFormat("0.0#");

    // View mode for 3D visualization
    // 1. View selected object from the Earth
    // 2. View nearest object from position of spacecraft
    // 3. View nearest object such that both object and spacecraft are in view
    private SolarSystemViewMode viewMode = SolarSystemViewMode.TELESCOPE;

    //https://www.genuinecoder.com/javafx-3d-tutorial-object-transform-rotation-with-mouse/
    //Tracks drag starting point for x and y
    private double anchorX, anchorY;

    //Keep track of current angle for x and y
    private double anchorAngleX = 0.0;
    private double anchorAngleY = 0.0;

    // View can be adjusted by mouse drag
    private double angleX = 0.0;
    private double angleY = 0.0;

    // Zoom can be adjusted by mouse scroll
    private double zoom = INITZOOM;

    // Material for sphere representing shadow of the Earth
    private PhongMaterial materialTransparent;
    private PhongMaterial materialShadowEarth;

    // Scene
    private SmartGroup solarSystemSmartGroup;
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
    private Shape3D pallas, juno, vesta, eros, bennu;
    private Shape3D halley, churyumov, ultimaThule;
    private Shape3D pioneer10, pioneer11, voyager1, voyager2, newhorizons, rosetta, apollo8;
    private Map<String,Node> bodies;
    private Map<String,Rotate> bodyRotationsX;
    private Map<String,Rotate> bodyRotationsY;
    private Map<String,Rotate> bodyRotationsZ;
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
        spacecraftNames.add("Giotto");
        spacecraftNames.add("Rosetta");
        spacecraftNames.add("Apollo 8");
        spacecraftNames.add("ISS");

        // Obtain all shapes created by the factory
        bodies = factory.getShapes();

        // Set body rotations for all shapes
        for (String name : bodies.keySet()) {
            Node node = bodies.get(name);
            setBodyRations(name,node);
        }

        // Rotate the rings of Saturn and Uranus with the planet
        ringSaturn.getTransforms().add(bodyRotationsX.get("Saturn"));
        ringSaturn.getTransforms().add(bodyRotationsZ.get("Saturn"));
        ringSaturn.getTransforms().add(bodyRotationsY.get("Saturn"));
        ringUranus.getTransforms().add(bodyRotationsX.get("Uranus"));
        ringUranus.getTransforms().add(bodyRotationsZ.get("Uranus"));
        ringUranus.getTransforms().add(bodyRotationsY.get("Uranus"));

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
        setCameraSettings(50.0);

        // Define the scene using a subscene
        solarSystemSmartGroup = new SmartGroup();
        for (Node node : bodies.values()) {
            solarSystemSmartGroup.getChildren().add(node);
        }
        solarSystemSmartGroup.getChildren().add(ringSaturn);
        solarSystemSmartGroup.getChildren().add(ringUranus);
        solarSystemSmartGroup.getChildren().add(coronaSun);
        solarSystemSmartGroup.getChildren().add(shadowEarth);
        solarSystemSmartGroup.getChildren().add(pointLight);
        solarSystemSmartGroup.getChildren().add(locationOnEarth);
        subScene = new SubScene(
                solarSystemSmartGroup,
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
            angleX = anchorAngleX - (anchorY - event.getSceneY());
            angleY = anchorAngleY + anchorX - event.getSceneX();
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
        if (viewMode.equals(SolarSystemViewMode.TELESCOPE)) {
            if ("Earth".equals(selectedBody) || "E-M Barycenter".equals(selectedBody)) {
                displayObservedBody.setText("Earth observed from the Sun");
            }
            else {
                Vector3D position;
                try {
                    position = solarSystem.getPosition(selectedBody);
                } catch (SolarSystemException e) {
                    selectedBody = "Sun";
                    position = solarSystem.getParticle("Sun").getPosition();
                }
                double[] result =
                        EphemerisUtil.computeAzimuthElevationDistance(position,latitude,longitude,solarSystem.getSimulationDateTime());
                double azimuth = result[0];
                double elevation = result[1];
                double distance = result[2];
                StringBuilder sb = new StringBuilder(selectedBody);
                sb.append(" observed from the Earth");
                sb.append(" (lat ");
                sb.append(DF_MAXTWODECIMALS.format(latitude));
                sb.append(" lon ");
                sb.append(DF_MAXTWODECIMALS.format(longitude));
                sb.append(")\n");
                sb.append("Azimuth ");
                sb.append(DF_ONEDECIMAL.format(azimuth));
                sb.append("\u00B0\n");
                sb.append("Elevation ");
                sb.append(DF_ONEDECIMAL.format(elevation));
                sb.append("\u00B0\n");
                sb.append("Distance ");
                sb.append(DF_MAXTWODECIMALS.format(distance));
                sb.append(" a.u.\n");
                displayObservedBody.setText(sb.toString());
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
            StringBuilder sb = new StringBuilder(observedBody);
            sb.append(" observed from ");
            sb.append(selectedBody);
            sb.append("\n");
            sb.append("Distance ");
            sb.append(distanceKm);
            sb.append(" km\n");
            sb.append("Velocity ");
            sb.append(velocity);
            sb.append(" m/s");
            displayObservedBody.setText(sb.toString());
        }
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
     */
    private void setCameraSettings(double fieldOfView) {
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
            positionCamera.setZ(0.0);
        } else {
            // View observed object from position of spacecraft
            camera.setNearClip(0.0000001 * SCREENDEPTH);
            camera.setFarClip(0.5 * SCREENDEPTH);
            positionCamera.setZ(0.0);
        }
    }

    /**
     * Set body rotations.
     * @param name Name of the body
     * @param node 3D geometry representing the body
     */
    private void setBodyRations(String name, Node node) {
        Rotate bodyRotationX = new Rotate(0.0,Rotate.X_AXIS);
        Rotate bodyRotationY = new Rotate(0.0,Rotate.Y_AXIS);
        Rotate bodyRotationZ = new Rotate(0.0,Rotate.Z_AXIS);
        node.getTransforms().add(bodyRotationX);
        node.getTransforms().add(bodyRotationZ); // Note the order
        node.getTransforms().add(bodyRotationY);
        bodyRotationsX.put(name,bodyRotationX);
        bodyRotationsY.put(name,bodyRotationY);
        bodyRotationsZ.put(name,bodyRotationZ);
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
            case "Giotto":
                diameter = DIAMETERGIOTTO;
                break;
            case "Rosetta":
                diameter = DIAMETERROSETTA;
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
     * Update y-rotation of all visible objects on the basis of sidereal rotation period.
     * Take the direction of the camera into account
     * @param cameraDirection direction of camera
     */
    private void updateBodyRotations(Vector3D cameraDirection) {

        // Angle theta of camera in X-Y plane and angle phi with X-Y plane
        double camX = cameraDirection.getX();
        double camY = cameraDirection.getY();
        double camZ = cameraDirection.getZ();
        double camRho = cameraDirection.magnitude();
        double camThetaRad = Math.atan2(camY,camX);
        double camThetaDeg = Math.toDegrees(camThetaRad);
        double camPhiRad = Math.acos(camZ/camRho);
        double camPhiDeg = Math.toDegrees(camPhiRad);

        // Rotate along Y-axis to simulate rotation of the body
        GregorianCalendar dateTime = solarSystem.getSimulationDateTime();
        double nrDaysPastJ2000 = EphemerisUtil.computeNrDaysPastJ2000(dateTime);
        for (String name : bodies.keySet()) {
            Node node = bodies.get(name);
            if (node.isVisible()) {
                if (spacecraftNames.contains(name)) {
                    if ("Apollo 8".equals(name)) {
                        // Rotate spacecraft such that it is directed in the direction of movement
                        Vector3D positionEarth = solarSystem.getParticle("Earth").getPosition();
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
                        bodyRotationsX.get(name).setAngle(camPhiDeg - angleZdeg);
                    }
                    else {
                        // Rotate spacecraft such that parabolic antenna is directed towards the Earth
                        // The models of the spacecraft are constructed such that the parabolic antenna is
                        // directed towards the camera when not rotated
                        Vector3D spacecraftPosition = solarSystem.getParticle(name).getPosition();
                        Vector3D earthPosition = solarSystem.getParticle("Earth").getPosition();
                        Vector3D directionToEarth = spacecraftPosition.direction(earthPosition);
                        double angleXYrad = Math.atan2(directionToEarth.getY(), directionToEarth.getX());
                        double angleXYdeg = Math.toDegrees(angleXYrad);
                        bodyRotationsY.get(name).setAngle(camThetaDeg - angleXYdeg);
                        double angleZrad = Math.acos(directionToEarth.getZ());
                        double angleZdeg = Math.toDegrees(angleZrad);
                        bodyRotationsX.get(name).setAngle(camPhiDeg - angleZdeg);
                    }
                } else {
                    // Rotate along y-axis to visualize rotation
                    double siderealRotationPeriodHours = solarSystemParameters.getSiderealRotationalPeriod(name);
                    double siderealRotationPeriodDays = siderealRotationPeriodHours / 24.0;
                    double nrRotations = nrDaysPastJ2000 / siderealRotationPeriodDays;
                    double rotationAngleDegY = camThetaDeg - (nrRotations % 1.0) * 360.0;
                    if ("Earth".equals(name)) {
                        // When the Earth is observed from the Sun, the Greenwich meridian
                        // should be in the center at noon (12:00:00) UTC.
                        // However, because of Earth's uneven angular velocity in its
                        // elliptical orbit and its axial tilt, noon (12:00:00) GMT is
                        // rarely the exact moment the Sun crosses the Greenwich meridian and
                        // reaches its highest point in the sky there.
                        // https://en.wikipedia.org/wiki/Greenwich_Mean_Time
                        rotationAngleDegY += 81.0;
                    }
                    if ("Moon".equals(name)) {
                        // Correction to see the front side of the Moon when viewing the full Moon
                        rotationAngleDegY -= 30.0;
                    }
                    if ("Jupiter".equals(name)) {
                        // Correction to see Red Spot at the right position
                        // https://skyandtelescope.org/observing/celestial-objects-to-watch/jupiters-moons-javascript-utility/#
                        rotationAngleDegY += 150.0;
                    }
                    if ("Pluto".equals(name)) {
                        // Correction to see Pluto's Big Heart from New Horizons July 13, 20.00
                        // https://www.nasa.gov/feature/new-horizons-spacecraft-displays-pluto-s-big-heart-0
                        rotationAngleDegY += 180.0;
                    }
                    bodyRotationsY.get(name).setAngle(rotationAngleDegY);

                    // Rotate along x-axis and z-axis to visualize obliquity
                    double[] rotationPoleEquatorial = solarSystemParameters.getRotationPole(name);
                    double[] rotationPoleEcliptic = EphemerisUtil.equatorialToEcliptic(rotationPoleEquatorial);
                    double lambda = rotationPoleEcliptic[0];
                    double beta = rotationPoleEcliptic[1];
                    double thetaDeg = lambda - camThetaDeg;
                    double phiDeg = (90.0 - beta) + (90.0 - camPhiDeg);
                    double thetaRad = Math.toRadians(thetaDeg);
                    double sinTheta = Math.sin(thetaRad);
                    double cosTheta = Math.cos(thetaRad);
                    Point3D rotationAxis = new Point3D(cosTheta, 0.0, sinTheta);
                    bodies.get(name).setRotationAxis(rotationAxis);
                    bodies.get(name).setRotate(phiDeg);
                    if ("Saturn".equals(name)) {
                        ringSaturn.setRotationAxis(rotationAxis);
                        ringSaturn.setRotate(phiDeg);
                    }
                    if ("Uranus".equals(name)) {
                        ringUranus.setRotationAxis(rotationAxis);
                        ringUranus.setRotate(phiDeg);
                    }
                }
            }
        }
    }

    /**
     * Translate and rotate position for observation from camera position and
     * camera direction
     * Perspective is taken into account.
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
    private void updateObjectPositions(Vector3D cameraPosition, Vector3D cameraDirection) {

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
                positionBody = translateRotatePosition(cameraPosition, cameraDirection, positionBody);

                // Set position
                node.setTranslateX(screenX(positionBody));
                node.setTranslateY(screenY(positionBody));
                node.setTranslateZ(screenZ(positionBody));
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
        positionPointLight =
                translateRotatePosition(cameraPosition,cameraDirection,positionPointLight);
        pointLight.setTranslateX(screenX(positionPointLight));
        pointLight.setTranslateY(screenY(positionPointLight));
        pointLight.setTranslateZ(screenZ(positionPointLight));

        // Check for Solar Eclipse
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
                coronaSun.setRadius(2.04*sun.getRadius());
                coronaSun.setTranslateX(sun.getTranslateX());
                coronaSun.setTranslateY(sun.getTranslateY());
                coronaSun.setTranslateZ(sun.getTranslateZ());
                coronaSun.setRotationAxis(Rotate.X_AXIS);
                coronaSun.setRotate(90.0);
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
     * @param cameraPosition position of the camera within Solar System
     * @param lookAtPos position to look at within Solor System
     */
    private void lookAt(Vector3D cameraPosition, Vector3D lookAtPos) {

        // Position (px, py, pz) is camera position relative to look-at position
        double px = cameraPosition.getX() - lookAtPos.getX();
        double py = cameraPosition.getY() - lookAtPos.getY();
        double pz = cameraPosition.getZ() - lookAtPos.getZ();

        // Convert (px, py, pz) to spherical coordinates (rho, theta, phi)
        double rho = Math.sqrt(px*px + py*py + pz*pz);
        double theta = Math.atan2(py,px); // radians
        double phi = Math.acos(pz/rho); // radians

        // Adjust theta and phi for mouse input
        theta += Math.toRadians(angleY);

        // Convert (rho, theta, phi) to rectangular coordinates (qx, qy, qz)
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);
        double qx = rho * sinPhi * cosTheta;
        double qy = rho * sinPhi * sinTheta;
        double qz = rho * cosPhi;

        // Camera position adjusted for mouse input
        cameraPosition = lookAtPos.plus(new Vector3D(qx,qy,qz));

        // Camera direction
        Vector3D cameraDirection = lookAtPos.minus(cameraPosition);
        cameraDirection = cameraDirection.normalize().scalarProduct(-1.0);
        if (viewMode.equals(SolarSystemViewMode.TELESCOPE)) {
            cameraPosition = lookAtPos.plus(cameraDirection.scalarProduct(0.5 * SCREENSCALE));
        }
        updateObjectPositions(cameraPosition,cameraDirection);
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

        // Let camera look in the direction of the Earth
        Vector3D lookAtPosition = new Vector3D(earthPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Set field of view of camera
        double diameterEarth = diameterBody("Earth");
        double fieldOfView = diameterEarth / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView);
    }


    /**
     * View the Earth-Moon system from space.
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

        // Let camera look in the direction of the Earth
        Vector3D lookAtPosition = new Vector3D(earthPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Set field of view of camera
        double diameterEarthMoonSystem = earthPosition.euclideanDistance(moonPosition);
        double fieldOfView = 0.6 * (diameterEarthMoonSystem / FIELDOFVIEWFACTORTELESCOPE);
        setCameraSettings(fieldOfView);
    }

    /**
     * View from the Earth towards the Sun.
     * @throws SolarSystemException
     */
    private void viewFromEarthToSun() throws SolarSystemException {

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
        double sunRadiusFactor = 1.0;
        if (moon.isVisible()) {
            // Correction of Sun radius for Solar eclipse
            Vector3D moonPosition = solarSystem.getPosition("Moon");
            double moonDistance = cameraPosition.euclideanDistance(moonPosition);
            sunRadiusFactor = CORRECTIONSUNRADIUSSOLARECLIPSE*(moonDistance/sunDistance);
        }
        if (mercury.isVisible()) {
            Vector3D mercuryPosition = solarSystem.getPosition("Mercury");
            Vector3D mercuryDirection = cameraPosition.direction(mercuryPosition);
            double mercuryAngleDeg = mercuryDirection.angleDeg(sunDirection);
            if (mercuryAngleDeg < 1.0) {
                // Correction of Sun radius for Mercury transit
                double mercuryDistance = cameraPosition.euclideanDistance(mercuryPosition);
                sunRadiusFactor = CORRECTIONSUNRADIUSMERCURYTRANSIT*(mercuryDistance/sunDistance);
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
            }
        }
        sun.setRadius(sunRadiusFactor * 0.5*screenDiameter("Sun"));

        // Let camera look in the direction of the Sun
        Vector3D lookAtPosition = new Vector3D(sunPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Set field of view of camera
        double diameterSun = diameterBody("Sun");
        double fieldOfView = sunRadiusFactor * diameterSun / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView);
    }

    /**
     * View from the Earth towards the selected body
     * @throws SolarSystemException
     */
    private void viewFromEarthToSelectedBody() throws SolarSystemException {

        // Set camera on the surface of the Earth
        Vector3D geocentricPosition =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 0.0,
                        solarSystem.getSimulationDateTime());
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D cameraPosition = earthPosition.plus(geocentricPosition);

        // Let camera look in the direction of the selected body
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
        Vector3D lookAtPosition = new Vector3D(bodyPosition);
        lookAt(cameraPosition,lookAtPosition);

        // Set field of view of the camera
        double diameter = diameterBody(selectedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView);
    }

    /**
     * View from the Earth towards the observed body
     * @throws SolarSystemException
     */
    private void viewFromEarthToObservedBody() throws SolarSystemException {

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

        // Set field of view of the camera
        double diameter = diameterBody(observedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView);
    }

    /**
     * View from the surface of the Earth towards the Moon
     * @throws SolarSystemException
     */
    private void viewFromEarthToMoon() throws SolarSystemException {

        // Set camera on the surface of the Earth
        Vector3D geocentricPosition =
                EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitude, longitude, 0.0,
                        solarSystem.getSimulationDateTime());
        Vector3D earthPosition = solarSystem.getPosition("Earth");
        Vector3D cameraPosition = earthPosition.plus(geocentricPosition);

        // Let camera look in direction of the Moon
        Vector3D moonPosition = solarSystem.getPosition("Moon");
        Vector3D lookatPosition = new Vector3D(moonPosition);
        lookAt(cameraPosition,lookatPosition);

        // Set field of view of the camera
        double diameter = diameterBody(selectedBody);
        double fieldOfView = diameter / FIELDOFVIEWFACTORTELESCOPE;
        setCameraSettings(fieldOfView);
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
        double distanceFromCenter = spacecraftPosition.euclideanDistance(bodyPosition);
        double distanceFromSurface = distanceFromCenter - 0.5*diameter;
        if ("Rosetta".equals(selectedBody) && distanceFromSurface < 1.0E11) {
            // Rosetta comes close to the surface of the Earth and Mars
            // Increase field of view depending on the distance to the surface
            fieldOfView += (1.0E11 - distanceFromSurface)/5.0E9;
        }
        fieldOfView = Math.min(Math.max(fieldOfView,4.0),90.0);
        setCameraSettings(fieldOfView);
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
        setCameraSettings(40.0);
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
            setCameraSettings(30.0);
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
                setCameraSettings(fieldOfView);
            }
            else {
                setCameraSettings(30.0);
            }
        }
    }

    /**
     * Update the 3D scene
     * @param bodiesShown   Names of bodies to be shown
     * @param selectedBody  Selected body
     * @param observedBody  Body that is being observed
     * @param viewMode      Selected view mode
     * @param latitude      latitude of location on the Earth [degrees]
     * @param longitude     longitude of lociation on the Earth [degrees]
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

        // To visualize a blood moon
        shadowEarth.setVisible("Moon".equals(selectedBody));

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
                        viewFromEarthToSun();
                        break;
                    case "Earth":
                        sun.setVisible(false);
                        viewFromSunToEarth();
                        break;
                    case "Moon":
                        earth.setVisible(false);
                        moon.setVisible(true);
                        shadowEarth.setVisible(true);
                        viewFromEarthToMoon();
                        break;
                    case "E-M Barycenter":
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
                        if ("Earth".equals(observedBody)) {
                            sun.setVisible(false);
                            viewFromSunToEarth();
                        }
                        else {
                            viewFromEarthToObservedBody();
                        }
                        break;
                    default:
                        if (bodies.keySet().contains(this.selectedBody)) {
                            earth.setVisible(false);
                            shadowEarth.setVisible(false);
                            viewFromEarthToSelectedBody();
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

