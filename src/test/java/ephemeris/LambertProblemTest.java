/*
 * Copyright (c) 2023 Nico Kuijpers
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
package ephemeris;

import application.SolarSystemException;
import org.junit.*;
import util.Vector3D;

import java.util.GregorianCalendar;

/**
 * Unit test for class LambertProblem.
 * @author Nico Kuijpers
 */
public class LambertProblemTest {

    public LambertProblemTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Mars. Use orbital parameters to determine positions and expected velocity.
     */
    @Test
    public void testLambertProblemMarsOrbitalParameters() {
        GregorianCalendar dateTime1 = new GregorianCalendar(2023, 7, 1, 0,0,0);
        GregorianCalendar dateTime2 = new GregorianCalendar(2023, 9, 10, 0,0,0);
        double deltaT = (dateTime2.getTimeInMillis() - dateTime1.getTimeInMillis()) / 1000.0;
        double muSun = SolarSystemParameters.getInstance().getMu("Sun");
        double[] orbitPars = SolarSystemParameters.getInstance().getOrbitParameters("Mars");
        double[] orbitElements1 = EphemerisUtil.computeOrbitalElements(orbitPars, dateTime1);
        double[] orbitElements2 = EphemerisUtil.computeOrbitalElements(orbitPars, dateTime2);
        Vector3D position1 = EphemerisUtil.computePosition(orbitElements1);
        Vector3D position2 = EphemerisUtil.computePosition(orbitElements2);
        Vector3D velocityExpected = EphemerisUtil.computeVelocity(muSun, orbitElements1);
        Vector3D velocityActual = new Vector3D();
        try {
            LambertProblem lambertProblem = new LambertProblem(position1, position2, deltaT, muSun, false, 0);
            velocityActual = lambertProblem.getAllVelocities1()[0];
            System.out.println(lambertProblem);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }
        double diffExpected = 0.0;
        double diffActual = velocityActual.euclideanDistance(velocityExpected);
        double diffDelta = 1.0; // 1 m/s
        System.out.println("Velocity expected " + velocityExpected);
        System.out.println("Velocity actual   " + velocityActual);
        System.out.println("Difference is " + diffActual + " m/s");
        Assert.assertEquals(diffExpected, diffActual, diffDelta);
    }

    /**
     * Mars. Use ephemeris to determine positions and expected velocity.
     */
    @Test
    public void testLambertProblemMarsEphemeris() {
        IEphemeris ephemeris = EphemerisSolarSystem.getInstance();
        String planetName = "Mars";
        double muSun = SolarSystemParameters.getInstance().getMu("Sun");
        GregorianCalendar dateTime1 = new GregorianCalendar(2023, 7, 1, 0,0,0);
        GregorianCalendar dateTime2 = new GregorianCalendar(2023, 9, 10, 0,0,0);
        double deltaT = (dateTime2.getTimeInMillis() - dateTime1.getTimeInMillis()) / 1000.0;
        Vector3D position1 = ephemeris.getBodyPosition(planetName, dateTime1);
        Vector3D position2 = ephemeris.getBodyPosition(planetName, dateTime2);
        Vector3D velocityExpected = ephemeris.getBodyVelocity(planetName, dateTime1);
        Vector3D velocityActual = new Vector3D();
        try {
            LambertProblem lambertProblem = new LambertProblem(position1, position2, deltaT, muSun, false, 0);
            velocityActual = lambertProblem.getAllVelocities1()[0];
            System.out.println(lambertProblem);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }
        double diffExpected = 0.0;
        double diffActual = velocityActual.euclideanDistance(velocityExpected);
        double diffDelta = 1.0; // 1 m/s
        System.out.println("Velocity expected " + velocityExpected);
        System.out.println("Velocity actual   " + velocityActual);
        System.out.println("Difference is " + diffActual + " m/s");
        Assert.assertEquals(diffExpected, diffActual, diffDelta);
    }

    /**
     * Dwarf planet Ceres was discovered on January 1, 1801. Information on
     * observations of Ceres in early 1801 were published in September.
     * By this time, the apparent position of Ceres had changed (primarily
     * due to Earth's motion around the Sun). Towards the end of the year,
     * Ceres should have been visible again, but after such a long time,
     * it was difficult to predict its exact position. To recover Ceres,
     * mathematician Carl Friedrich Gauss, then 24 years old, developed
     * an efficient method of orbit determination. Within a few weeks,
     * he predicted the path of Ceres and sent his results to von Zach.
     * On 31 December 1801, von Zach and fellow celestial policeman
     * Heinrich W. M. Olbers found Ceres near the predicted position and
     * continued to record its position.
     * https://en.wikipedia.org/wiki/Ceres_(dwarf_planet)
     */
    @Test
    public void testLambertProblemCeres() {
        IEphemeris ephemeris = EphemerisSolarSystem.getInstance();
        String dwarfPlanetName = "Ceres";
        double muSun = SolarSystemParameters.getInstance().getMu("Sun");
        GregorianCalendar dateTime1 = new GregorianCalendar(1801, 0, 1, 0,0,0);
        GregorianCalendar dateTime2 = new GregorianCalendar(1801, 11, 31, 0,0,0);
        double deltaT = (dateTime2.getTimeInMillis() - dateTime1.getTimeInMillis()) / 1000.0;
        Vector3D position1 = ephemeris.getBodyPosition(dwarfPlanetName, dateTime1);
        Vector3D position2 = ephemeris.getBodyPosition(dwarfPlanetName, dateTime2);
        Vector3D velocityExpected = ephemeris.getBodyVelocity(dwarfPlanetName, dateTime1);
        Vector3D velocityActual = new Vector3D();
        try {
            LambertProblem lambertProblem = new LambertProblem(position1, position2, deltaT, muSun, false, 0);
            velocityActual = lambertProblem.getAllVelocities1()[0];
            System.out.println(lambertProblem);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }
        double diffExpected = 0.0;
        double diffActual = velocityActual.euclideanDistance(velocityExpected);
        double diffDelta = 1.0; // 1 m/s
        System.out.println("Velocity expected " + velocityExpected);
        System.out.println("Velocity actual   " + velocityActual);
        System.out.println("Difference is " + diffActual + " m/s");
        Assert.assertEquals(diffExpected, diffActual, diffDelta);
    }

    /**
     * Io has a near-circular orbit with a period of 1.77 days.
     * Note that Io's orbit is influenced by the other Galilean moons.
     */
    @Test
    public void testLambertProblemIo() {
        IEphemeris ephemeris = EphemerisSolarSystem.getInstance();
        String moonName = "Io";
        double muJupiter = SolarSystemParameters.getInstance().getMu("Jupiter");
        GregorianCalendar dateTime1 = new GregorianCalendar(2023, 7, 1, 0,0,0);
        GregorianCalendar dateTime2 = new GregorianCalendar(2023, 7, 1, 12,0,0);
        double deltaT = (dateTime2.getTimeInMillis() - dateTime1.getTimeInMillis()) / 1000.0;
        Vector3D position1 = ephemeris.getBodyPosition(moonName, dateTime1);
        Vector3D position2 = ephemeris.getBodyPosition(moonName, dateTime2);
        Vector3D velocityExpected = ephemeris.getBodyVelocity(moonName, dateTime1);
        Vector3D velocityActual = new Vector3D();
        try {
            LambertProblem lambertProblem = new LambertProblem(position1, position2, deltaT, muJupiter, false, 0);
            velocityActual = lambertProblem.getAllVelocities1()[0];
            System.out.println(lambertProblem);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }
        double diffExpected = 0.0;
        double diffActual = velocityActual.euclideanDistance(velocityExpected);
        double diffDelta = 5.0; // 5 m/s
        System.out.println("Velocity expected " + velocityExpected);
        System.out.println("Velocity actual   " + velocityActual);
        System.out.println("Difference is " + diffActual + " m/s");
        Assert.assertEquals(diffExpected, diffActual, diffDelta);
    }

    /**
     * Triton has a retrograde orbit with inclination 156.9 degrees with respect to
     * Neptune's equator and an orbital period of 5.9 days.
     */
    @Test
    public void testLambertProblemTriton() {
        IEphemeris ephemeris = EphemerisSolarSystem.getInstance();
        String moonName = "Triton";
        double muNeptune = SolarSystemParameters.getInstance().getMu("Neptune");
        GregorianCalendar dateTime1 = new GregorianCalendar(2023, 7, 1, 0,0,0);
        GregorianCalendar dateTime2 = new GregorianCalendar(2023, 7, 20, 0,0,0);
        double deltaT = (dateTime2.getTimeInMillis() - dateTime1.getTimeInMillis()) / 1000.0;
        Vector3D position1 = ephemeris.getBodyPosition(moonName, dateTime1);
        Vector3D position2 = ephemeris.getBodyPosition(moonName, dateTime2);
        Vector3D velocityExpected = ephemeris.getBodyVelocity(moonName, dateTime1);
        Vector3D velocityActual = new Vector3D();
        try {
            LambertProblem lambertProblem = new LambertProblem(position1, position2, deltaT, muNeptune, true, 4);
            // Three full revolutions, right
            velocityActual = lambertProblem.getAllVelocities1()[6];
            // Retrograde orbit
            velocityActual = velocityActual.scalarProduct(-1.0);
            System.out.println(lambertProblem);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }
        double diffExpected = 0.0;
        double diffActual = velocityActual.euclideanDistance(velocityExpected);
        double diffDelta = 1.0; // 1 m/s
        System.out.println("Velocity expected " + velocityExpected);
        System.out.println("Velocity actual   " + velocityActual);
        System.out.println("Difference is " + diffActual + " m/s");
        Assert.assertEquals(diffExpected, diffActual, diffDelta);
    }
}
