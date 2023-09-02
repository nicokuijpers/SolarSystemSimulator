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

import com.interactivemesh.jfx.importer.ModelImporter;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import ephemeris.SolarSystemParameters;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;

import java.io.File;

/**
 * Factory for 3D shapes for visualization in JavaFX 3D.
 * Models are read from file in Wavefront format (extension .obj)
 * using ObjModelImporter from library jimObjModelImporterJFX.jar
 * http://www.InteractiveMesh.com
 * http://www.InteractiveMesh.org
 */
public class SolarSystemShapeFactory {

    // File locations and file extensions for spacecraft models
    private static final String DIRECTORYMODELS = "Models/";
    private static final String EXTENSIONMODEL = ".obj";

    // Visualization
    private SolarSystemVisualization visualization;

    // Solar System parameters
    private SolarSystemParameters solarSystemParameters;

    /**
     * Constructor.
     * @param visualization reference to Solar System visualization
     */
    public SolarSystemShapeFactory(SolarSystemVisualization visualization) {
        this.visualization = visualization;
        this.solarSystemParameters = SolarSystemParameters.getInstance();
    }

    /**
     * Create shape for Solar System body or spacecaft.
     * @param bodyName name of the body
     */
    public Node createShape(String bodyName) {
        switch (bodyName) {
            case "Eris":
                return createSphere("Eris", Color.LIGHTSALMON);
            case "Chiron":
                return createSphere("Chiron", Color.CRIMSON);
            case "Ceres":
                return createSphere("Ceres", Color.ORANGE);
            case "Pallas":
                return createSmallBody("Pallas",Color.LIGHTGRAY);
            case "Juno":
                return createSmallBody("Juno",Color.LIGHTGRAY);
            case "Vesta":
                return createSmallBody("Vesta",Color.YELLOW);
            case "Ida":
                //Color colorIda = new Color(164.0/255,152.0/255,138.0/255, 1.0);
                Color colorIda = new Color(172.0/255,156.0/255,130.0/255, 1.0);
                return createSmallBody("Ida",colorIda);
            case "Eros":
                Color colorEros = new Color(164.0/255,152.0/255,138.0/255, 1.0);
                return createSmallBody("Eros",colorEros);
            case "Gaspra":
                Color colorGaspra = new Color(160.0/255,152.0/255,142.0/255, 1.0);
                return createSmallBody("Gaspra",colorGaspra);
            case "Bennu":
                return createSmallBody("Bennu",Color.LIGHTGRAY);
            case "Halley":
                createSmallBody("Halley",Color.GRAY);
            case "Encke":
                return createSphere("Encke", Color.LIGHTGREEN);
            case "67P/Churyumov-Gerasimenko":
                return createSmallBody("67P/Churyumov-Gerasimenko", Color.SNOW);
            case "Hale-Bopp":
                return createSphere("Hale-Bopp", Color.LIGHTBLUE);
            case "Florence":
                return createSphere("Florence", Color.LIGHTGREEN);
            case "Ultima Thule":
                Color colorArrokoth = new Color(159.0/255, 101.0/255, 72.0/255, 1.0);
                return createSmallBody("Ultima Thule", colorArrokoth);
            case "Phobos":
                Color colorPhobos = new Color(176.0 / 255, 148.0 / 255, 127.0 / 255, 1.0);
                return createSmallBody("Phobos", colorPhobos);
            case "Deimos":
                Color colorDeimos = new Color(193.0 / 255, 166.0 / 255, 124.0 / 255, 1.0);
                return createSmallBody("Deimos", colorDeimos);
            case "Mimas":
                return createSphere("Mimas", Color.LIGHTGRAY);
            case "Enceladus":
                return createSphere("Enceladus", Color.ALICEBLUE);
            case "Tethys":
                return createSphere("Tethys", Color.DARKGOLDENROD);
            case "Dione":
                return createSphere("Dione", Color.BISQUE);
            case "Rhea":
                return createSphere("Rhea", Color.ORANGE);
            case "Titan":
                return createSphere("Titan", Color.PEACHPUFF);
            case "Hyperion":
                return createSphere("Hyperion", Color.LIGHTCORAL);
            case "Iapetus":
                return createSphere("Iapetus", Color.ALICEBLUE);
            case "Phoebe":
                return createSphere("Phoebe", Color.CORAL);
            case "Miranda":
                return createSphere("Miranda", Color.LIGHTGRAY);
            case "Ariel":
                return createSphere("Ariel", Color.ALICEBLUE);
            case "Umbriel":
                return createSphere("Umbriel", Color.PEACHPUFF);
            case "Titania":
                return createSphere("Titania", Color.LIGHTSALMON);
            case "Oberon":
                return createSphere("Oberon", Color.BISQUE);
            case "Triton":
                return createSphere("Triton", Color.LIGHTGRAY);
            case "Nereid":
                return createSphere("Nereid", Color.LIGHTSKYBLUE);
            case "Proteus":
                return createSphere("Proteus", Color.BISQUE);
            case "Charon":
                return createSphere("Charon", Color.GRAY);
            case "Nix":
            case "Hydra":
            case "Kerberos":
            case "Styx":
                return createSphere(bodyName, Color.LIGHTGRAY);
            case "Pioneer 10":
            case "Pioneer 11":
            case "Mariner 10":
            case "Voyager 1":
            case "Voyager 2":
            case "New Horizons":
            case "Rosetta":
            case "Galileo":
            case "Cassini":
                return createSpacecraft(bodyName, Color.LIGHTYELLOW);
            case "Apollo 8":
                return createSpacecraft(bodyName, Color.SILVER);
            case "ISS":
                return createISS("ISS");
            default:
                return createSphere(bodyName, Color.WHITE);
        }
    }

    /**
     * Create a sphere representing body of the Solar System.
     * @param sphereName name of the sphere
     * @param color  color for the sphere
     * @return sphere
     */
    public Sphere createSphere(String sphereName, Color color) {
        return createSphere(sphereName, color, false);
    }

    /**
     * Create a sphere representing body of the Solar System.
     * Use a high-resolation image texture when available.
     * @param sphereName name of the sphere
     * @param color  color for the sphere
     * @return sphere
     */
    public Sphere createSphereHighRes(String sphereName, Color color) {
        return createSphere(sphereName, color, true);
    }

    /**
     * Create a sphere representing body of the Solar System.
     * @param sphereName name of the sphere
     * @param color  color for the sphere
     * @param highres use high resolution texture image when available
     * @return sphere
     */
    private Sphere createSphere(String sphereName, Color color, boolean highres) {
        String name = sphereName;
        if (sphereName.startsWith("shadow")) {
            name = sphereName.replaceFirst("shadow","");
        }
        double radius = 0.5 * visualization.screenDiameter(name);
        double flattening = solarSystemParameters.getFlattening(name);
        Sphere sphere;
        if (highres) {
            sphere = new Sphere(radius,256);
        }
        else {
            sphere = new Sphere(radius);
        }
        sphere.scaleYProperty().setValue(1.0 - flattening);
        PhongMaterial material = new PhongMaterial();
        File file;
        Image image;
        // http://planetpixelemporium.com/planets.html
        // https://planet-texture-maps.fandom.com/wiki/Category:Jupiter_Moons
        switch (sphereName) {
            case "Sun":
                // http://planetpixelemporium.com/planets.html
                // Texture image 1024 x 512 pixels
                file = new File("Images/sunmap.jpg");
                // https://www.solarsystemscope.com/textures/
                //file = new File("Images/2k_sun.jpg");
                // http://www.planetaryvisions.com/images_new/4204.jpg
                // Texture image 640 x 320 pixels
                //file = new File("Images/planvis_sun.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseColor(Color.BLACK);
                material.setSelfIlluminationMap(image);
                break;
            case "Moon":
                // http://planetpixelemporium.com/planets.html
                //file = new File("Images/moonmap1k.jpg");
                if (highres) {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 8192 x 4096 pixels (15 MB)
                    file = new File("Images/8k_moon.jpg"); // 15 MB
                }
                else {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 2048 x 1024 pixels (1.1 MB)
                    file = new File("Images/2k_moon.jpg");
                }
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Mercury":
                // http://planetpixelemporium.com/planets.html
                //file = new File("Images/mercurymap.jpg");
                // https://www.solarsystemscope.com/textures/
                file = new File("Images/2k_mercury.jpg");
                // http://www.planetaryvisions.com/images_new/31.jpg
                // Texture image 640 x 320 pixels; one hemisphere
                //file = new File("Images/planvis_mercury.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Venus":
                // http://planetpixelemporium.com/planets.html
                //file = new File("Images/venusmap.jpg");
                // https://www.solarsystemscope.com/textures/
                file = new File("Images/2k_venus_atmosphere.jpg");
                //file = new File("Images/2k_venus_surface.jpg");
                // http://www.planetaryvisions.com/images_new/32.jpg
                // Texture image 640 x 320 pixels
                //file = new File("Images/planvis_venus.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Earth":
                // http://planetpixelemporium.com/planets.html
                // file = new File("Images/earthmap1k.jpg");
                // https://www.solarsystemscope.com/textures/
                //file = new File("Images/2k_earth_daymap.jpg");
                //file = new File("Images/2k_earth_nightmap.jpg");
                //file = new File("Images/2k_earth_daycloud3.jpg");
                //image = new Image(file.toURI().toString());
                //material.setDiffuseMap(image);
                File fileSurfaceEarthDay, fileSurfaceEarthNight;
                if (highres) {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 8192 x 4096 pixels (4.6 MB)
                    fileSurfaceEarthDay = new File("Images/8k_earth_daymap.jpg");
                    Image imageSurfaceEarthDay = new Image(fileSurfaceEarthDay.toURI().toString());
                    material.setDiffuseMap(imageSurfaceEarthDay);
                    // Texture image 8192 x 4096 pixels (3.1 MB)
                    fileSurfaceEarthNight = new File("Images/8k_earth_nightmap.jpg");
                    Image imageSurfaceEarthNight = new Image(fileSurfaceEarthNight.toURI().toString());
                    material.setSelfIlluminationMap(imageSurfaceEarthNight);
                }
                else {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 2048 x 1024 pixels (464 KB)
                    fileSurfaceEarthDay = new File("Images/2k_earth_daymap.jpg");
                    Image imageSurfaceEarthDay = new Image(fileSurfaceEarthDay.toURI().toString());
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 2048 x 1024 pixels (966 KB)
                    File fileCloudsEarth = new File("Images/2k_earth_clouds.jpg");
                    Image imageCloudsEarth = new Image(fileCloudsEarth.toURI().toString());
                    PixelReader pixelReaderSurface = imageSurfaceEarthDay.getPixelReader();
                    PixelReader pixelReaderClouds = imageCloudsEarth.getPixelReader();
                    int width = (int) imageCloudsEarth.getWidth();
                    int height = (int) imageCloudsEarth.getHeight();
                    WritableImage imageLowResEarth = new WritableImage(width, height);
                    PixelWriter pixelWriter = imageLowResEarth.getPixelWriter();
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            int argbClouds = pixelReaderClouds.getArgb(x, y);
                            int redClouds = (argbClouds >> 16) & 0xff;
                            int greenClouds = (argbClouds >> 8) & 0xff;
                            int blueClouds = argbClouds & 0xff;
                            int alphaClouds = (int) ((redClouds + greenClouds + blueClouds) / 3.0);
                            redClouds = (int) Math.min(255.0, 3 * redClouds);
                            greenClouds = (int) Math.min(255.0, 3 * greenClouds);
                            blueClouds = (int) Math.min(255.0, 3 * blueClouds);
                            int argbSurface = pixelReaderSurface.getArgb(x, y);
                            int redSurface = (argbSurface >> 16) & 0xff;
                            int greenSurface = (argbSurface >> 8) & 0xff;
                            int blueSurface = argbSurface & 0xff;
                            int alpha = 255;
                            double factor = alphaClouds/255.0;
                            int red = (int) Math.round(factor*redClouds + (1.0 - factor)*redSurface);
                            int green = (int) Math.round(factor*greenClouds + (1.0 - factor)*greenSurface);
                            int blue = (int) Math.round(factor*blueClouds + (1.0 - factor)*blueSurface);
                            int val = (alpha << 24) | (red << 16) | (green << 8) | blue;
                            pixelWriter.setArgb(x, y, val);
                        }
                    }
                    material.setDiffuseMap(imageLowResEarth);
                }
                break;
            case "Mars":
                // http://planetpixelemporium.com/planets.html
                //file = new File("Images/marsmap1k.jpg");
                // http://www.planetaryvisions.com/images_new/33.jpg
                // Texture image 640 x 320 pixels
                //file = new File("Images/planvis_mars.jpg");
                if (highres) {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 8192 x 4096 pixels (8.4 MB)
                    file = new File("Images/8k_mars.jpg");
                }
                else {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 2048 x 1024 pixels (751 KB)
                    file = new File("Images/2k_mars.jpg");
                }
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Jupiter":
                // http://planetpixelemporium.com/planets.html
                // Texture image 1000 x 500 pixels
                //file = new File("Images/jupiter2_1k.jpg");
                // http://www.planetaryvisions.com/images_new/34.jpg
                // Texture image 640 x 320 pixels
                //file = new File("Images/planvis_jupiter.jpg");
                if (highres) {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 4096 x 2048 pixels (3.1 MB)
                    file = new File("Images/8k_jupiter.jpg");
                }
                else {
                    // https://www.solarsystemscope.com/textures/
                    // Texture image 2048 x 1024 pixels (499 KB)
                    file = new File("Images/2k_jupiter.jpg");
                }
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Saturn":
                // http://planetpixelemporium.com/planets.html
                // Texture image 1800 x 900 pixels (71 KB)
                file = new File("Images/saturnmap.jpg");
                // https://www.solarsystemscope.com/textures/
                // Texture image 2048 x 1024 pixels (200 KB)
                //file = new File("Images/2k_saturn.jpg");
                // Texture image 4096 x 2048 pixels (1.1 MB)
                //file = new File("Images/8k_saturn.jpg");
                // http://www.planetaryvisions.com/images_new/35.jpg
                // Texture image 640 x 320 pixels
                //file = new File("Images/planvis_saturn.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Uranus":
                // http://planetpixelemporium.com/planets.html
                // Texture image 1024 x 512 pixels (9 KB)
                file = new File("Images/uranusmap.jpg");
                // https://www.solarsystemscope.com/textures/
                // Texture image 2048 x 1024 pixels (78 KB)
                //file = new File("Images/2k_uranus.jpg");
                // http://www.planetaryvisions.com/images_new/36.jpg
                // Texture image 640 x 320 pixels (33 KB)
                //file = new File("Images/planvis_uranus.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Neptune":
                // http://planetpixelemporium.com/planets.html
                // Texture image 1024 x 512 pixels (48 KB)
                file = new File("Images/neptunemap.jpg");
                // https://www.solarsystemscope.com/textures/
                // Texture image 2048 x 1024 pixels (242 KB)
                //file = new File("Images/2k_neptune.jpg"); // 242 KB
                // http://www.planetaryvisions.com/images_new/37.jpg
                // Texture image 640 x 320 pixels (34 KB)
                //file = new File("Images/planvis_neptune.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Pluto":
                // http://planetpixelemporium.com/planets.html
                // Does not resemble pictures from New Horizons
                // file = new File("Images/plutomap1k.jpg");
                // http://www.planetaryvisions.com/images_new/38.jpg
                // Texture image 640 x 320 pixels
                // Resembles somewhat pictures from New Horizons
                // file = new File("Images/planvis_pluto.jpg");
                // https://3d-asteroids.space/dwarf/134340-Pluto
                // Texture image color 5926 x 2963 pixels (2.3 MB)
                // Resembles pictures New Horizons, not entire sphere
                // file = new File("Images/PlutoColor2017.jpg");
                // PlutoColor2017.jpg adapted using PhotoShop
                // Texture image 2048 x 1024 pixels (1.5 MB)
                file = new File("Images/PlutoAdapted.jpg");
                // https://www.deviantart.com/bob3studios/art/Pluto-Texture-Map-Fixed-Blur-762286905
                // Texture image 4096 x 2048 pixels (1.6 MB)
                // file = new File("Images/pluto_deviantart.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Eris":
                // https://www.solarsystemscope.com/textures/
                file = new File("Images/2k_eris_fictional.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Ceres":
                // https://www.solarsystemscope.com/textures/
                // file = new File("Images/2k_ceres_fictional.jpg");
                // https://3d-asteroids.space/dwarf/1-Ceres
                // Original texture image grayscale 21093 x 10546 pixels, 35.4 MB
                // Texture image 2048 x 1024 pixels
                file = new File("Images/CeresGrayscale2k.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Phobos":
                // http://www.planetaryvisions.com/images_new/215.jpg
                // Texture image 640 x 320 pixels (72 KB)
                file = new File("Images/planvis_phobos.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Deimos":
                // http://www.planetaryvisions.com/images_new/201.jpg
                // Texture image 640 x 320 pixels (46 KB)
                file = new File("Images/planvis_deimos.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Io":
                // https://planet-texture-maps.fandom.com/wiki/Io
                // Texture image 1440 x 720 pixels (2.1 MB)
                // file = new File("Images/io.png");
                // Texture image 2048 x 1024 pixels (2.4 MB)
                file = new File("Images/Dh_io_texture.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Europa":
                // https://planet-texture-maps.fandom.com/wiki/Europa
                // Texture image 1440 x 720 pixels (183 KB)
                // file = new File("Images/europa.jpg");
                // Texture image 2048 x 1024 pixels (3.5 MB)
                file = new File("Images/Dh_europa_texture.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Ganymede":
                // https://planet-texture-maps.fandom.com/wiki/Ganymede
                // Texture image 450 x 225 pixels (161 KB)
                // file = new File("Images/ganymede.png");
                // Texture image 1440 x 720 pixels (1.5 MB)
                // file = new File("Images/Ganymede_Reworked.png");
                // Texture image 2048 x 1024 pixels (2.4 MB)
                file = new File("Images/Dh_ganymede_texture.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Callisto":
                // https://planet-texture-maps.fandom.com/wiki/Callisto
                // Texture image 1024 x 512 pixels
                // file = new File("Images/callisto.jpg");
                // Texture image 2048 x 1024 pixels (2.7 MB)
                file = new File("Images/Dh_callisto_texture.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Mimas":
                // http://www.planetaryvisions.com/images_new/211.jpg
                // Texture image 640 x 320 pixels
                // file = new File("Images/planvis_mimas.jpg");
                // https://3d-asteroids.space/moons/S1-Mimas
                // Texture image enhanced color 6356 x 3178 pixels, 1.5 MB
                // file = new File("Images/MimasEnhColor.jpg");
                // Texture image 2048 x 1024 pixels
                // file = new File("Images/MimasEnhColor2k.jpg");
                file = new File("Images/MimasGrayscale2017_2k.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Enceladus":
                // http://www.planetaryvisions.com/images_new/203.jpg
                // Texture image 640 x 320 pixels
                // file = new File("Images/planvis_enceladus.jpg");
                // https://3d-asteroids.space/moons/S2-Enceladus
                // Texture image enhanced color 15960 x 7980 pixels (39.9 MB)
                // file = new File("Images/EnceladusEnhColor.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                // file = new File("Images/EnceladusEnhColor2k.jpg");
                file = new File("Images/EnceladusGrayscale2k.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Tethys":
                // http://www.planetaryvisions.com/images_new/220.jpg
                // Texture image 640 x 320 pixels
                // file = new File("Images/planvis_tethys.jpg");
                // https://3d-asteroids.space/moons/S3-Tethys
                // Texture image enhanced color 13467 x 6734 pixels (21.7 MB)
                // file = new File("Images/TethysEnhColor.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                // file = new File("Images/TethysEnhColor2k.jpg");
                file = new File("Images/TethysGrayscale2k.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Dione":
                // http://www.planetaryvisions.com/images_new/202.jpg
                // Texture image 640 x 320 pixels
                // file = new File("Images/planvis_dione.jpg");
                // https://3d-asteroids.space/moons/S4-Dione
                // Texture image enhanced color 14134 x 7067 pixels (21.5 MB)
                // file = new File("Images/DioneEnhColor.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                // file = new File("Images/DioneEnhColor2k.jpg");
                file = new File("Images/DioneGrayscale2k.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Rhea":
                // http://www.planetaryvisions.com/images_new/219.jpg
                // Texture image 640 x 320 pixels
                // file = new File("Images/planvis_rhea.jpg");
                // https://3d-asteroids.space/moons/S5-Rhea
                // Texture image enhanced color 12015 x 6008 pixels, 21.8 MB
                // file = new File("Images/RheaEnhColor.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                // file = new File("Images/RheaEnhColor2k.jpg");
                file = new File("Images/RheaGrayscale2k.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Titan":
                // http://www.planetaryvisions.com/images_new/221.jpg
                // Texture image 640 x 320 pixels
                file = new File("Images/planvis_titan.jpg");
                // https://3d-asteroids.space/moons/S6-Titan
                // Texture image grayscale 4040 x 2020 pixels (895 KB)
                // file = new File("Images/TitanGrayscale.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Hyperion":
                // 3D model: https://3d-asteroids.space/moons/S7-Hyperion
                // http://www.planetaryvisions.com/images_new/207.jpg
                // Texture image 640 x 320 pixels
                file = new File("Images/planvis_hyperion.jpg");
                // https://planet-texture-maps.fandom.com/wiki/Hyperion
                // Semi-fictitious texture image 2048 x 1024 pixels
                // file = new File("Images/Dh_hyperion_texture.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Iapetus":
                // http://www.planetaryvisions.com/images_new/208.jpg
                // Texture image 640 x 320 pixels
                // file = new File("Images/planvis_iapetus.jpg");
                // https://3d-asteroids.space/moons/S8-Iapetus
                // Texture image grayscale 5758 x 2879 pixels (1.7 MB)
                // file = new File("Images/IapetusGrayscale.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                // file = new File("Images/IapetusGrayscale2k.jpg");
                // Texture image color 11740 x 5870 pixels (11.2 MB)
                // file = new File("Images/IapetusColor.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                file = new File("Images/IapetusColor2k.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Phoebe":
                // http://www.planetaryvisions.com/images_new/216.jpg
                // Texture image 640 x 320 pixels (67 KB)
                // file = new File("Images/planvis_phoebe.jpg");
                // https://3d-asteroids.space/data/moons/textures/S9_Phoebe/Phoebe.jpg?size=max
                // Texture image 2880 x 1440 pixels (219 KB)
                // file = new File("Images/PhoebeGrayscale.jpg");
                // https://planet-texture-maps.fandom.com/wiki/Phoebe
                // Semi-fictitious texture image 1024 x 512 pixels (244 KB)
                file = new File("Images/PhoebeMap2.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Miranda":
                // http://www.planetaryvisions.com/images_new/212.jpg
                // Texture image 640 x 320 pixels, southern hemisphere only
                // file = new File("Images/planvis_miranda.jpg");
                // https://3d-asteroids.space/moons/U5-Miranda
                // Texture image 1024 x 512 pixels (59 KB) Southern hemisphere only
                // file = new File("Images/MirandaVoyager.jpg");
                // Same texture image with Northern hemisphere gray
                file = new File("Images/MirandaVoyagerAdapted.jpg");
                // https://planet-texture-maps.fandom.com/wiki/Miranda
                // Semi-realistic map of Miranda
                // Texture image 1024 x 512
                // file = new File("Images/Miranda-0.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Ariel":
                // http://www.planetaryvisions.com/images_new/198.jpg
                // Texture image 640 x 320 pixels, southern hemisphere only
                // file = new File("Images/planvis_ariel.jpg");
                // https://3d-asteroids.space/moons/U1-Ariel
                // Texture image 1024 x 512 pixels (24 KB) Southern hemisphere only
                // file = new File("Images/ArielVoyager.jpg");
                // Same texture image with Northern hemisphere gray
                file = new File("Images/ArielVoyagerAdapted.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Umbriel":
                // http://www.planetaryvisions.com/images_new/224.jpg
                // Texture image 640 x 320 pixels, southern hemisphere only
                // file = new File("Images/planvis_umbriel.jpg");
                // https://3d-asteroids.space/moons/U2-Umbriel
                // Texture image 1024 x 512 pixels (33 KB) southern hemisphere only
                // file = new File("Images/UmbrielVoyager.jpg");
                // Same texture image with Northern hemisphere gray
                file = new File("Images/UmbrielVoyagerAdapted.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Titania":
                // http://www.planetaryvisions.com/images_new/222.jpg
                // Texture image 640 x 320 pixels, southern hemisphere only
                // file = new File("Images/planvis_titania.jpg");
                // https://3d-asteroids.space/moons/U3-Titania
                // file = new File("Images/TitaniaVoyager.jpg");
                // Same texture image with Northern hemisphere gray
                file = new File("Images/TitaniaVoyagerAdapted.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Oberon":
                // http://www.planetaryvisions.com/images_new/214.jpg
                // Texture image 640 x 320 pixels, southern hemisphere only
                // file = new File("Images/planvis_oberon.jpg");
                // https://3d-asteroids.space/moons/U4-Oberon
                // Texture image 1024 x 512 pixels, 34 KB, southern hemisphere only
                // file = new File("Images/OberonVoyager.jpg");
                // Same texture image with Northern hemisphere gray
                file = new File("Images/OberonVoyagerAdapted.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Triton":
                // http://www.planetaryvisions.com/images_new/223.jpg
                // Texture image 640 x 320 pixels, southern hemisphere only
                // file = new File("Images/planvis_triton.jpg");
                // https://3d-asteroids.space/moons/N1-Triton
                // Texture image 14138 x 7069 pixels (11.9 MB) Southern hemisphere only
                // file = new File("Images/TritonVoyager.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                // Northern hemisphere filled with texture from central regions
                file = new File("Images/TritonVoyagerAdapted.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Nereid":
                // https://planet-texture-maps.fandom.com/wiki/Nereid
                // Texture images created by Izak1273, complete fiction
                // Texture image 1000 x 500 pixels (40 KB)
                // file = new File("Images/Nereid.jpg");
                // Texture image improved by Izak1273, complete fiction
                // Texture image 312 x 156 pixels (67 KB)
                file = new File("Images/Nereidimproved.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Proteus":
                // http://www.planetaryvisions.com/images_new/218.jpg
                // Texture image 640 x 320 pixels (38 KB)
                // file = new File("Images/planvis_proteus.jpg");
                // https://planet-texture-maps.fandom.com/wiki/Proteus
                // This is a proteus map made using voyager 2 images (based on a phil stooke map)
                // Texture image 2048 x 1024 pixels (468 KB)
                // Note that a large part of the original image is black,
                // therefore black pixels are converted to gray pixels.
                file = new File("Images/Proteus.png");
                image = new Image(file.toURI().toString());
                Image imageGrayProteus = convertBlackToGray(image,20);
                material.setDiffuseMap(imageGrayProteus);
                break;
            case "Charon":
                // http://www.planetaryvisions.com/images_new/200.jpg
                // Texture image 180 x 90 pixels (27 KB)
                // file = new File("Images/planvis_charon.jpg");
                // https://3d-asteroids.space/moons/P1-Charon
                // Texture image 12693 x 6347 pixels (14.6 MB)
                // No data available of area south of 30 degrees S and Western hemisphere
                // file = new File("Images/CharonNewHorizons.jpg");
                // Texture image size adapted to 2048 x 1024 pixels
                file = new File("Images/CharonNewHorizonsAdapted.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            default :
                material.setDiffuseColor(color);
                break;
        }
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Create a cylinder representing the rings of the planet with given name.
     * @param planetName name of the planet
     * @param color  color for the ring
     * @return cylinder
     */
    public Cylinder createRing(String planetName, Color color) {
        double height = 0.0;
        double radius;
        switch(planetName) {
            case "Saturn":
                // Set outer radius of rings of Saturn
                // https://en.wikipedia.org/wiki/Rings_of_Saturn
                double outerRadiusRingSaturn = 1.4022E08; // Distance of F-ring from the center of Saturn
                double radiusSaturn = 0.5 * solarSystemParameters.getDiameter("Saturn");
                double screenDiameterSaturn = visualization.screenDiameter("Saturn");
                radius = (outerRadiusRingSaturn/radiusSaturn) * 0.5 * screenDiameterSaturn;
                break;
            case "Uranus":
                // Set outer radius of rings of Urnanus
                // https://nssdc.gsfc.nasa.gov/planetary/factsheet/uranringfact.html
                double outerRadiusRingUranus = 5.1149E07; // Epsilon ring 51,149 km
                double radiusUranus = 0.5 * solarSystemParameters.getDiameter("Uranus");
                double screenDiameterUranus = visualization.screenDiameter("Uranus");
                radius = (outerRadiusRingUranus/radiusUranus) * 0.5 * screenDiameterUranus;
                break;
            case "Sun":
                // Corona of the Sun as visible during Solar eclipse
                // https://en.wikipedia.org/wiki/Solar_eclipse#/media/File:Solar_eclipse_1999_4_NR.jpg
                double screenDiameterSun = visualization.screenDiameter("Sun");
                radius = 1.02*screenDiameterSun;
                break;
            default:
                radius = visualization.screenDiameter(planetName);
                break;
        }
        Cylinder ring = new Cylinder();
        ring.setHeight(height);
        ring.setRadius(radius);
        PhongMaterial material = new PhongMaterial();
        File file;
        Image image;
        // http://planetpixelemporium.com/planets.html
        switch (planetName) {
            case "Saturn":
                // http://planetpixelemporium.com/planets.html
                //file = new File("Images/saturnringpattern.gif");
                //image = new Image(file.toURI().toString());
                //material.setBumpMap(image);
                // http://planetpixelemporium.com/planets.html
                //file = new File("Images/saturnringcolor.jpg");
                // Created by Marco
                //file = new File("Images/marcoring.png");
                // https://www.roblox.com/library/278998727/Saturns-Rings-Texture
                //file = new File("Images/saturnringroblox.png");
                // https://www.pngkit.com/downpic/u2e6a9t4e6i1o0e6_exit-vr-mr2/
                //file = new File("Images/pngkit_saturn-rings-png_3319332.png");
                // https://www.deviantart.com/alpha-element/art/Stock-Image-Saturn-Rings-393767006
                // file = new File("Images/stock_image___saturn_rings_by_alpha_element_d6ifske-pre.png");
                // https://www.deviantart.com/niko22966/art/Rings-of-Saturn-419585311
                //file = new File("Images/rings_of_saturn_by_niko22966_d6xt63j-pre.jpg");
                // Constructed using Matlab script
                //file = new File("Images/saturnring.png");
                // https://www.deviantart.com/alpha-element/art/Stock-Image-Saturn-Rings-393767006 (2000 x 2000 pixels)
                file = new File("Images/stockSaturnRing_2000x2000.png");
                // LARGE FILE 7000 x 7000 pixels 38,3 MB
                // file = new File("Images/stockSaturnRingLarge.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Uranus":
                //file = new File("Images/uranusringtrans.gif");
                //image = new Image(file.toURI().toString());
                //material.setBumpMap(image);
                // file = new File("Images/uranusringcolor.jpg");
                // https://favpng.com/png_view/uranus-the-trooth-texture-mapping-planet-mykolaiv-png/0rw9hFBt
                // file = new File("Images/uranus-the-trooth-texture-mapping-planet-mykolaiv-png-favpng-S8qEyEkyC6KSUvJYUZbQFTE85.jpg");
                // https://www.pngguru.com/free-transparent-background-png-clipart-mjabs
                // file = new File("planet-cartoon-uranus-trooth-texture-mapping-mykolaiv-color-circumstellar-habitable-zone-circle-png-clipart.jpg");
                // Constructed using Matlab script
                file = new File("Images/uranusring.png");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            case "Sun":
                // https://en.wikipedia.org/wiki/Solar_eclipse
                // Image size 2158 x 2160 pixels
                file = new File("Images/Solar_eclipse_1999_4_NR.jpg");
                image = new Image(file.toURI().toString());
                material.setDiffuseMap(image);
                break;
            default :
                material.setDiffuseColor(color);
                break;
        }
        ring.setMaterial(material);
        return ring;
    }

    /**
     * Create a sphere with clouds for the earth.
     * The sphere is transparent except for the clouds.
     * @param cloudFactor factor to determine radius of the sphere
     * @return sphere with clouds
     */
    public Sphere createCloudsEarth(double cloudFactor) {
        return createCloudsEarth(cloudFactor, false);
    }

    /**
     * Create a sphere with clouds for the earth.
     * The sphere is transparent except for the clouds.
     * Use a high-resolation texture image when available.
     * @param cloudFactor factor to determine radius of the sphere
     * @return sphere with clouds
     */
    public Sphere createCloudsEarthHighRes(double cloudFactor) {
        return createCloudsEarth(cloudFactor, true);
    }

    /**
     * Create a sphere with clouds for the earth.
     * The sphere is transparent except for the clouds.
     * @param cloudFactor factor to determine radius of the sphere
     * @param highres use high resolution texture image when true
     * @return sphere with clouds
     */
    private Sphere createCloudsEarth(double cloudFactor, boolean highres) {
        double radius = cloudFactor * 0.5 * visualization.screenDiameter("Earth");
        double flattening = solarSystemParameters.getFlattening("Earth");
        Sphere sphere = new Sphere();
        sphere.setRadius(radius);
        sphere.scaleYProperty().setValue(1.0 - flattening);
        PhongMaterial material = new PhongMaterial();
        File fileCloudsEarth;
        if (highres) {
            // https://www.solarsystemscope.com/textures/
            // Texture image 8192 x 4096 pixels (11.6 MB)
            fileCloudsEarth = new File("Images/8k_earth_clouds.jpg");
        }
        else {
            // https://www.solarsystemscope.com/textures/
            // Texture image 2048 x 1024 pixels (966 KB)
            fileCloudsEarth = new File("Images/2k_earth_clouds.jpg");
        }
        Image imageCloudsEarth = new Image(fileCloudsEarth.toURI().toString());
        PixelReader pixelReader = imageCloudsEarth.getPixelReader();
        int width = (int) imageCloudsEarth.getWidth();
        int height = (int) imageCloudsEarth.getHeight();
        WritableImage imageCloudsEarthTransparent = new WritableImage(width, height);
        PixelWriter pixelWriter = imageCloudsEarthTransparent.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = pixelReader.getArgb(x,y);
                int red = (argb>>16) & 0xff;
                int green = (argb>>8) & 0xff;
                int blue = argb & 0xff;
                int alpha = (int) ((red + green + blue)/3.0);
                red = (int) Math.min(255.0,3*red);
                green = (int) Math.min(255.0,3*green);
                blue = (int) Math.min(255.0,3*blue);
                int val = (alpha<<24) | (red<<16) | (green<<8) | blue;
                pixelWriter.setArgb(x,y,val);
            }
        }
        material.setDiffuseMap(imageCloudsEarthTransparent);
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Create the shadow for a planet of the Solar System.
     * @param planetName name of the planet
     * @param shadowFactor factor to determine length of the shadow
     * @param color  color for the shadow
     * @return shadow (cylinder)
     */
    public Cylinder createShadow(String planetName, double shadowFactor, Color color) {
        double radiusPlanet = 0.5*visualization.screenDiameter(planetName);
        double flattening = solarSystemParameters.getFlattening(planetName);
        Cylinder shadow = new Cylinder((1.0 - flattening)*radiusPlanet, shadowFactor*radiusPlanet);
        shadow.setMaterial(new PhongMaterial(color));
        return shadow;
    }

    /**
     * Create a 3D shape for small Solar System body with given name.
     * @param bodyName name of the small body
     * @param color color for the small body
     * @return 3D shape
     */
    public Shape3D createSmallBody(String bodyName, Color color) {

        // Create shape for small Solar System body
        double radius = 0.5 * visualization.screenDiameter(bodyName);
        Shape3D shape;
        ModelImporter objImporter;
        Node[] objMesh;
        PhongMaterial material;
        switch (bodyName) {
            case "Pallas":
                // Read model for Pallas from file
                // https://3d-asteroids.space/asteroids/2-Pallas
                // Afmetingen: [Xmax-min 0.379028, Ymax-min 0.352875, Zmax-min 0.334046]
                objImporter = new ObjModelImporter();
                objImporter.read("Models/Pallas_Torppa_scaled.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Juno":
                // Read model for Juno from file
                // https://3d-asteroids.space/asteroids/3-Juno
                // https://3d-asteroids.space/data/asteroids/models/j/3_Juno_103.obj
                // https://3d-asteroids.space/data/asteroids/models/j/3_Juno_786.obj
                // Selected ID is 7, model: ../Models/juno.scaled.obj
                // zoomFactor 0.160537 (Xmax-min 0.160537, Ymax-min 0.157464, Zmax-min 0.129657)
                objImporter = new ObjModelImporter();
                objImporter.read("Models/juno_scaled.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Vesta":
                // Read model for Vesta from file
                // https://3d-asteroids.space/asteroids/4-Vesta
                // https://3d-asteroids.space/data/asteroids/models/v/4_Vesta.obj
                // Create ellipsoid instead of reading 3D model from file
                shape = new Sphere(radius);
                double meanDiameter = 525.40;
                shape.scaleXProperty().setValue(572.6/meanDiameter);
                shape.scaleYProperty().setValue(557.2/meanDiameter);
                shape.scaleZProperty().setValue(446.4/meanDiameter);
                // https://3d-asteroids.space/asteroids/4-Vesta
                // Texture image color 8192 x 4096 pixels, 5.5 MB
                // File fileDiffuseMap = new File("Images/VestaColor.jpg");
                // https://astropedia.astrogeology.usgs.gov/download/Vesta/Dawn/DLR/HAMO/thumbs/Vesta_Dawn_HAMO_ClrShade_DLR_Global_1024.jpg
                // Image converted to grayscale image
                // File fileDiffuseMap = new File("Images/Vesta_Dawn_HAMO_ClrShade_DLR_Global_1024_GRAY.jpg");
                File fileDiffuseMap = new File("Images/VestaGRAY2.jpg");
                Image imageDiffuseMap = new Image(fileDiffuseMap.toURI().toString());
                material = new PhongMaterial();
                material.setDiffuseMap(imageDiffuseMap);
                shape.setMaterial(material);
                break;
            case "Ida":
                // Read model for 243 Ida from file
                // https://3d-asteroids.space/asteroids/243-Ida
                // Original file Ida Stooke.obj
                // Scaled with factor 0.0002
                objImporter = new ObjModelImporter();
                objImporter.read("Models/Ida_scale0002.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Eros":
                // Read model for 433 Eros from file
                // https://3d-asteroids.space/asteroids/433-Eros
                // Afmetingen: [Xmax-min 0.023681, Ymax-min 0.012243, Zmax-min 0.008672]
                objImporter = new ObjModelImporter();
                objImporter.read("Models/Eros_Gaskell_50k_poly_scaled.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Gaspra":
                // Read model for 951 Gaspra from file
                // https://3d-asteroids.space/asteroids/951-Gaspra
                // Original file Gaspra Stooke.obj
                // Scaled with factor 0.0002
                objImporter = new ObjModelImporter();
                objImporter.read("Models/Gaspra_scale0002.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Bennu":
                // Read model for 101955 Bennu from file
                // https://www.asteroidmission.org/updated-bennu-shape-model-3d-files/
                objImporter = new ObjModelImporter();
                objImporter.read("Models/Bennu_v20_200k_scaled.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Halley":
                // Read model for Halley's comet from file
                // https://3d-asteroids.space/data/comets/models/1P_Halley_mod1.obj
                // This object does not have textures
                // Scale 0.0005
                // Selected ID is 6, model: ../Models/halley.scaled_0-dot-0005.obj
                // Number of vertices 2528 and number of faces 5048
                // Afmetingen: (Xmax-min 0.003767, Ymax-min 0.003792, Zmax-min 0.007572)
                objImporter = new ObjModelImporter();
                objImporter.read("Models/halley_scaled_0-dot-0005.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "67P/Churyumov-Gerasimenko":
                // Read model 67P/Churyumov-Gerasimenko from file
                objImporter = new ObjModelImporter();
                // objImporter.read("Models/CSHP_DV_130_01_______00200.obj");
                // Selected ID is 3, model: ../Models/CSHP_DV_130_01_______00200.scaled_0-dot-000598.obj
                // Afmetingen: Xmax-min 0.003029, Ymax-min 0.002223, Zmax-min 0.001982
                objImporter.read("Models/CSHP_DV_130_01_scaled.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Ultima Thule":
                // Read model and texture Ultima Thule from file
                objImporter = new ObjModelImporter();
                // objImporter.read("Models/ultima-thule-3d.obj");
                // Selected ID is 7, model: ../Models/ultima-thule-3d.scaled2.obj
                // zoomFactor 0.022717 (Xmax-min 0.022717, Ymax-min 0.014049, Zmax-min 0.015846)
                // objImporter.read("Models/ultima-thule-3d_scaled2.obj");
                objImporter.read("Models/ultima-thule-3d_scaled.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Phobos":
                // Read model for Phobos from file
                // https://3d-asteroids.space/moons/M1-Phobos
                // https://3d-asteroids.space/data/moons/models/M1_Phobos.obj
                // File name "Phobos Thomas.obj"
                // Phobos has dimensions of 27 km × 22 km × 18 km
                // https://en.wikipedia.org/wiki/Phobos_(moon)
                // Original file was scaled with factor 0.00002
                // The scaled model appears 10 times larger than in reality
                objImporter = new ObjModelImporter();
                // objImporter.read("Models/phobos-scaled-00002.obj");
                // Number of faces reduced with factor 0.5 using Blender Decimate Modifier tool
                objImporter.read("Models/phobos-scaled-00002-reduced.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            case "Deimos":
                // Read model for Deimos from file
                // https://3d-asteroids.space/moons/M2-Deimos
                // https://3d-asteroids.space/data/moons/models/M2_Deimos.obj
                // File name "Deimos Thomas.obj"
                // Deimos has dimensions of 15 km × 12.2 km × 11 km
                // https://en.wikipedia.org/wiki/Deimos_(moon)
                // Original file was scaled with factor 0.00002
                // The scaled model appears 10 times larger than in reality
                objImporter = new ObjModelImporter();
                objImporter.read("Models/deimos-scaled-00002.obj");
                objMesh = (Node[]) objImporter.getImport();
                objImporter.close();
                shape = (Shape3D) objMesh[0];
                material = new PhongMaterial();
                material.setDiffuseColor(color);
                shape.setMaterial(material);
                break;
            default:
                shape = new Sphere();
                break;
        }
        return shape;
    }

    /**
     * Create a 3D shape for given spacecraft
     * @param spacecraftName name of the spacecraft
     * @param color color for the spacecraft
     * @return 3D shape of spacecraft
     */
    public Shape3D createSpacecraft(String spacecraftName, Color color) {
        /*
         * Models are read from file in Wavefront format (extension .obj)
         * using ObjModelImporter from library jimObjModelImporterJFX.jar
         * http://www.InteractiveMesh.com
         * http://www.InteractiveMesh.org
         *
         * Models were adapted using Blender Version 2.82a (2.82a 2020-03-12)
         * https://www.blender.org
         *
         * All spacecraft models were moved and scaled such that the parabolic
         * antenna is near the origin and the size is approximately 2.5 mm
         * Voyager spacecraft have an apparent size of approximately 5000 km
         * New Horizons has an apparent size of approximately 800 km
         *
         * Models of Pioneer 10 and 11 are the same and are adapted from
         * https://free3d.com/3d-model/pioneer-10-v1--330916.html
         * Geometry was moved to the origin, rotated, and scaled with factor 0.000145
         *
         * Model of Mariner 10 is adapted from
         * https://free3d.com/3d-model/mariner-10-v1--105152.html
         * Geometry was scaled with factor 0.00000168
         *
         * Model of Voyager 1 is adapted from
         * https://free3d.com/3d-model/voyager-1-v2--602253.html
         * Geometry was moved to the origin and scaled with factor 0.00025
         *
         * Model of Voyager 2 is adapted from
         * https://free3d.com/3d-model/voyager-2-v1--717773.html
         * Geometry was moved to the origin, scaled with factor 0.0001 and
         * then moved 0.0022 m in Z-direction.
         * Rotation along X-axis 90 degrees, rotation along Y-axis 90 degrees
         *
         * Model of New Horizons is adapted from
         * https://commons.wikimedia.org/wiki/File:New_Horizons.stl
         * and converted to Wavefront .obj format online using
         * https://anyconv.com/stl-to-obj-converter/
         * Geometry was moved to the origin, scaled with factor 0.0004 and
         * then moved -0.0007 m in X-direction
         *
         * Model of Rosetta is adapted from
         * https://nasa3d.arc.nasa.gov/detail/eoss-rosetta
         * Geometry is scaled such that it is 5 times smaller than 67P
         *
         * Model of Cassini is adapted from
         * https://nasa3d.arc.nasa.gov/detail/jpl-vtad-cassini
         * Geometry was scaled with factor 0.0002 and 90 degrees rotated along X-axis
         *
         * Model of Galileo is the same as Pioneer 10 / 11
         */
        String fileName = DIRECTORYMODELS + spacecraftName + EXTENSIONMODEL;
        Shape3D shape;
        ModelImporter objImporter = new ObjModelImporter();
        objImporter.read(fileName);
        Node[] objMesh = (Node[]) objImporter.getImport();
        objImporter.close();
        shape = (Shape3D) objMesh[0];
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        shape.setMaterial(material);
        return shape;
    }

    /**
     * Create part of the International Space Station.
     * @param filename file name
     * @return 3D shape including material
     */
    private Shape3D createPartISS(String filename) {
        // Read model from file
        ModelImporter objImporter = new ObjModelImporter();
        objImporter.read(filename);
        Node[] objMesh = (Node[]) objImporter.getImport();
        objImporter.close();

        // Set size
        Node node = objMesh[0];
        node.scaleXProperty().setValue(1);
        node.scaleYProperty().setValue(1);
        node.scaleZProperty().setValue(1);
        node.setTranslateX(0);
        node.setTranslateY(0);
        node.setTranslateZ(0);
        Shape3D shape = (Shape3D) node;
        return shape;
    }

    /**
     * Create the International Space Station as a collection of
     * 3D shapes. Together, the 3D shapes form a group.
     * @return collection of 3D shapes representing the ISS
     */
    public Group createISS(String spacecraftName) {
        Shape3D ISS_A = createPartISS("Models/ISS/iss.out.A.obj");
        Shape3D ISS_B = createPartISS("Models/ISS/iss.out.B.obj");
        Shape3D ISS_C = createPartISS("Models/ISS/iss.out.C.obj");
        Shape3D ISS_D = createPartISS("Models/ISS/iss.out.D.obj");
        Shape3D ISS_E = createPartISS("Models/ISS/iss.out.E.obj");
        Shape3D ISS_F = createPartISS("Models/ISS/iss.out.F.obj");

        final Group ISS = new Group();
        ISS.getChildren().add(ISS_A);
        ISS.getChildren().add(ISS_B);
        ISS.getChildren().add(ISS_C);
        ISS.getChildren().add(ISS_D);
        ISS.getChildren().add(ISS_E);
        ISS.getChildren().add(ISS_F);

        return ISS;
    }

    /**
     * Convert black pixels to gray pixels in image.
     * @param image image for which the color of pixels will be changed
     * @param threshold threshold value in between 0 and 255
     * @return converted image
     */
    private Image convertBlackToGray(Image image, int threshold) {
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage imageResult = new WritableImage(width, height);
        PixelWriter pixelWriter = imageResult.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = pixelReader.getArgb(x,y);
                int alpha = (argb>>24) & 0xff;
                int red = (argb>>16) & 0xff;
                int green = (argb>>8) & 0xff;
                int blue = argb & 0xff;
                if (red < threshold && green < threshold && blue < threshold) {
                    red = 128;
                    green = 128;
                    blue = 128;
                }
                int val = (alpha << 24) | (red << 16) | (green << 8) | blue;
                pixelWriter.setArgb(x, y, val);
            }
        }
        return imageResult;
    }
}
