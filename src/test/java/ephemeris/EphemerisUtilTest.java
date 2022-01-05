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
package ephemeris;

import org.junit.*;
import util.Vector3D;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for class EphemerisUtil.
 * @author Nico Kuijpers
 */
public class EphemerisUtilTest {
    
    public EphemerisUtilTest() {
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
     * Compute number of centuries past J2000.0
     * @param date date
     * @return number of centuries past J2000.0
     */
    @Test
    public void testComputeNrCenturiesPastJ2000Zero() {
        GregorianCalendar date = new GregorianCalendar(2000,0,1,12,0);
        double expResult = 0.0;
        double result = EphemerisUtil.computeNrCenturiesPastJ2000(date);
        assertEquals(expResult, result, 1.0E-14);
    }

    @Test
    public void testComputeNrCenturiesPastJ2000One() {
        GregorianCalendar date = new GregorianCalendar(2100,0,1,12,0);
        double expResult = 1.0;
        double result = EphemerisUtil.computeNrCenturiesPastJ2000(date);
        assertEquals(expResult, result, 1.0E-14);
    }

    /**
     * Compute local sidereal time for given longitude and date/time.
     * @param longitude longitude [degrees]
     * @param dateTime  date/time [UTC]
     * @return local sidereal time [degrees]
     */
    @Test
    public void testComputeLocalSiderealTime() {
        // https://www.aa.quae.nl/en/reken/sterrentijd.html
        // Local sidereal time at 23:00 hours CET on 2006 December 1st
        // as seen from 5° east longitude is 45.61655 degrees
        // In GregorianCalendar January = 0, February = 1, etc.
        double longitude = 5.0;
        GregorianCalendar dateTime =
                new GregorianCalendar(2006,11,1,22,0);
        dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        double localSiderealTimeExpected = 45.61655;
        double localSiderealTimeActual =
                EphemerisUtil.computeLocalSiderealTime(longitude,dateTime);

        Assert.assertEquals(localSiderealTimeExpected,localSiderealTimeActual,1.0E-5);
    }
    
    /**
     * Solve Kepler's equation M = E - e*sin(E), where
     * M is mean anomaly, E is eccentric anomaly, and e is eccentricity.
     * Uses fixed point iteration to solve Kepler's equation.
     * 
     * @param Mrad          mean anomaly in radians
     * @param eccentricity  eccentricity
     * @param maxError      maximum error allowed    
     * @return eccentric anamaly in radians
     */
    @Test
    public void testSolveKeplerEquationFixedPointIteration() {
        long timeStart = System.currentTimeMillis();
        double maxError = 1.0E-8;
        double eccentricity = 0.0;
        while (eccentricity < 1.0) {
            for (int i = 0; i < 360; i++) {
                double M = (double) i;
                double Mrad = Math.toRadians(M);
                double Erad = EphemerisUtil.solveKeplerEquationFixedPointIteration(Mrad,eccentricity,maxError);
                double error = Mrad - (Erad -  eccentricity*Math.sin(Erad));
                assertEquals(error,0.0,maxError);
            }
            eccentricity += 0.001;
        }
        long timeStop = System.currentTimeMillis();
        long timeElapsed = timeStop - timeStart;
        System.out.println("Time elapsed fixed point iteration: " + timeElapsed + " ms");
    }
    
    /**
     * Solve Kepler's equation M = E - e*sin(E), where
     * M is mean anomaly, E is eccentric anomaly, and e is eccentricity.
     * Uses Newton-Raphson to solve Kepler's equation.
     * 
     * @param Mrad          mean anomaly in radians
     * @param eccentricity  eccentricity
     * @param maxError      maximum error allowed    
     * @return eccentric anamaly in radians
     */
    @Test
    public void testSolveKeplerEquationNewtonRaphson() {
        long timeStart = System.currentTimeMillis();
        double maxError = 1.0E-14;
        double eccentricity = 0.0;
        while (eccentricity < 1.0) {
            for (int i = 0; i < 360; i++) {
                double M = (double) i;
                double Mrad = Math.toRadians(M);
                double Erad = EphemerisUtil.solveKeplerEquationNewtonRaphson(Mrad,eccentricity,maxError);
                double error = Mrad - (Erad -  eccentricity*Math.sin(Erad));
                assertEquals(error,0.0,maxError);
            }
            eccentricity += 0.001;
        }
        long timeStop = System.currentTimeMillis();
        long timeElapsed = timeStop - timeStart;
        System.out.println("Time elapsed Newton-Raphson: " + timeElapsed + " ms");
    }
    
    /**
     * Solve Kepler's equation M = E - e*sin(E), where
     * M is mean anomaly, E is eccentric anomaly, and e is eccentricity.
     * Uses Halley's method to solve Kepler's equation.
     * 
     * @param Mrad          mean anomaly in radians
     * @param eccentricity  eccentricity
     * @param maxError      maximum error allowed    
     * @return eccentric anamaly in radians
     */
    @Test
    public void testSolveKeplerEquationHalley() {
        long timeStart = System.currentTimeMillis();
        double maxError = 1.0E-14;
        double eccentricity = 0.0;
        while (eccentricity < 1.0) {
            for (int i = 0; i < 360; i++) {
                double M = (double) i;
                double Mrad = Math.toRadians(M);
                double Erad = EphemerisUtil.solveKeplerEquationHalley(Mrad,eccentricity,maxError);
                double error = Mrad - (Erad -  eccentricity*Math.sin(Erad));
                assertEquals(error,0.0,maxError);
            }
            eccentricity += 0.001;
        }
        long timeStop = System.currentTimeMillis();
        long timeElapsed = timeStop - timeStart;
        System.out.println("Time elapsed Halley's method : " + timeElapsed + " ms");
    }
    
    /**
     * Solve the hyperbolic version of Kepler's equation M = e*sinh(H) - H, 
     * where M is mean anomaly, H is hyperbolic anomaly, and e is eccentricity.
     * Uses Halley's method to solve Kepler's equation.
     *
     * @param Mrad mean anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @param maxError maximum error allowed [radians]
     * @return hyperbolic anomaly [radians]
     */
    @Test
    public void testSolveHyperbolicKeplerEquationHalley() {
        long timeStart = System.currentTimeMillis();
        double maxError = 1.0E-14;
        double eccentricity = 1.01;
        while (eccentricity < 10.0) {
            for (int i = 0; i < 360; i++) {
                double M = (double) i;
                double Mrad = Math.toRadians(M);
                double Hrad = EphemerisUtil.solveHyperbolicKeplerEquationHalley(Mrad,eccentricity,maxError);
                double error = Mrad - (eccentricity*Math.sinh(Hrad) - Hrad);
                assertEquals(error,0.0,maxError);
            }
            eccentricity += 0.01;
        }
        long timeStop = System.currentTimeMillis();
        long timeElapsed = timeStop - timeStart;
        System.out.println("Time elapsed Halley's method (hyperbolic): " + timeElapsed + " ms");
    }

    /**
     * Compute true anomaly from eccentric anomaly.
     *
     * @param Erad eccentric anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @return true anomaly [radians]
     */
    @Test
    public void testComputeTrueAnomaly() {

        // https://en.wikipedia.org/wiki/True_anomaly
        // tan(nu/2) = sqrt((1 + e)/(1 - e)) * tan(E/2), where
        // nu is true anomaly
        // e is eccentricity
        // E is eccentric anomaly
        // For an ellipse it holds 0 < e < 1
        double e = 0.01;
        while (e <= 0.99) {
            double E = 0.0;
            while (E <= 178.0) {
                double Erad = Math.toRadians(E);
                double nu = EphemerisUtil.computeTrueAnomaly(Erad,e);
                String message = "Wrong true anomaly for e = " + e + " and E = " + E;
                assertEquals(message, Math.tan(nu/2.0),
                        Math.sqrt((1.0 + e)/(1.0 - e)) * Math.tan(Erad/2.0),1.0E-11);
                E += 0.1;
            }
            e += 0.1;
        }
    }

    /**
     * Compute true anomaly from hyperbolic anomaly.
     *
     * @param Hrad hyperbolic anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @return true anomaly [radians]
     */
    @Test
    public void testComputeTrueAnomalyHyperbolic() {
        // https://space.stackexchange.com/questions/24646/finding-x-y-z-vx-vy-vz-from-hyperbolic-orbital-elements
        // https://physics.stackexchange.com/questions/247470/calculating-true-anomaly-of-a-hyperbolic-trajectory-from-time
        // tan(theta/2) = sqrt((e+1)/(e-1)) * tanh(H/2), where
        // theta is true anomaly
        // e is eccentricity
        // H is hyperbolic anomaly
        // For a hyperbole it holds e > 1
        double e = 1.001;
        while (e <= 100.0) {
            double Hrad = 0.0;
            while (Hrad <= 10.0) {
                double theta = EphemerisUtil.computeTrueAnomalyHyperbolic(Hrad,e);
                String message = "Wrong true anomaly for e = " + e + " and Hrad = " + Hrad;
                assertEquals(message, Math.tan(theta/2.0),
                        Math.sqrt((e + 1.0)/(e - 1.0)) * Math.tanh(Hrad/2.0),1.0E-12);
                Hrad += 0.01;
            }
            e += 0.1;
        }
    }
    
    /**
     * Compute position in orbit plane from position in ecliptic plane.
     *
     * @param position position of planet in m
     * @param longNode longitude of ascending node [degrees]
     * @param inclination inclination [degrees]
     * @param argPerihelion argument of perihelion [degrees
     * @return position in orbit plane (typically z will be near zero)
     * Author: Marco
     */
    @Test
    public void testComputeOrbitPositionFromEclipticPosition() {
        Vector3D a = new Vector3D(-246000,-5000,0); // reflects hypothetical orbit position
        Vector3D b;
        Vector3D c;
        b = EphemerisUtil.computeOrbitPositionFromEclipticPosition(a, -50,7,135);
        c = EphemerisUtil.computeEclipticPositionFromOrbitPosition(b, -50,7,135);
        // c must be equal to a (within, say, 1e-6)
        double distance = c.euclideanDistance(a);
        Assert.assertEquals("c not equal to a",0.0,distance,1.0E-10);
    }
    
    /**
     * Compute position in ecliptic plane from position in orbit plane.
     *
     * @param position position of planet in m
     * @param longNode longitude of ascending node [degrees]
     * @param inclination inclination [degrees]
     * @param argPerihelion argument of perihelion [degrees]
     * @return position in ecliptic plane
     * Author: Marco
     */
    @Test
    public void testComputeEclipticPositionFromOrbitPosition() {
        Vector3D a = new Vector3D(-246000,-5000,0); // reflects hypothetical orbit position
        Vector3D b;
        Vector3D c;
        b = EphemerisUtil.computeEclipticPositionFromOrbitPosition(a, -50,7,135);
        c = EphemerisUtil.computeOrbitPositionFromEclipticPosition(b, -50,7,135);
        // c must be equal to a (within, say, 1e-6)
        double distance = c.euclideanDistance(a);
        Assert.assertEquals("c not equal to a",0.0,distance,1.0E-10);
    }
    
    /**
     * Compute orbital elements from position and velocity of planet.
     * @param position Position of planet in m
     * @param velocity Velocity of planet in m/s
     * @return orbitElements orbit elements (Keplerian elements)
     */
    @Test
    public void testComputeOrbitalElementsFromPositionVelocityJupiter() {
        System.out.println("computeOrbitalElementsFromPositionVelocity (Jupiter)");

        // Date is Jan 1, 2017
        GregorianCalendar date = new GregorianCalendar(2017, 0, 1);

        // Orbital parameters for Jupiter
        double[] orbitPars = SolarSystemParameters.getInstance().getOrbitParameters("Jupiter");

        // Jupiter orbits around the sun in 12 years
        int nrDays = (int) (12 * 365.25);
        int day = 0;
        while (day < nrDays) {

            // Compute orbital elements for given date
            double orbitElementsExpected[] = EphemerisUtil.computeOrbitalElements(orbitPars, date);

            // Compute (x,y,z) position of Jupiter [m] from orbital elements
            Vector3D position = EphemerisUtil.computePosition(orbitElementsExpected);

            // Compute (x,y,z) velocity of Jupiter [m/s] from orbital elements
            double muSun = SolarSystemParameters.getInstance().getMu("Sun");
            Vector3D velocity = EphemerisUtil.computeVelocity(muSun,orbitElementsExpected);

            // Compute orbital elements from position and velocity
            double orbitElementsActual[]
                    = EphemerisUtil.computeOrbitalElementsFromPositionVelocity(muSun,position,velocity);

            // Expected orbital elements
            double axisExpected = orbitElementsExpected[0]; // semi-major axis [au]
            double eccentricityExpected = orbitElementsExpected[1]; // eccentricity [-]
            double inclinationExpected = orbitElementsExpected[2]; // inclination [degrees]
            double meanAnomalyExpected = orbitElementsExpected[3]; // mean anomaly [degrees]
            double argPerihelionExpected = orbitElementsExpected[4]; // argument of perihelion [degrees]
            double longNodeExpected = orbitElementsExpected[5]; // longitude of ascending node [degrees]

            // Actual orbital elements
            double axisActual = orbitElementsActual[0]; // semi-major axis [au]
            double eccentricityActual = orbitElementsActual[1]; // eccentricity [-]
            double inclinationActual = orbitElementsActual[2]; // inclination [degrees]
            double meanAnomalyActual = orbitElementsActual[3]; // mean anomaly [degrees]
            double argPerihelionActual = orbitElementsActual[4]; // argument of perihelion [degrees]
            double longNodeActual = orbitElementsActual[5]; // longitude of ascending node [degrees]

            // Compare actual orbital elements to expected orbital elements
            Assert.assertEquals("Wrong semi-major axis(day " + day + ")", axisExpected, axisActual, 1.0E-14);
            Assert.assertEquals("Wrong eccentricity (day " + day + ")", eccentricityExpected, eccentricityActual, 1.0E-13);
            Assert.assertEquals("Wrong inclination (day " + day + ")", inclinationExpected, inclinationActual, 1.0E-12);
            Assert.assertEquals("Wrong mean anomaly (day " + day + ")", meanAnomalyExpected, meanAnomalyActual, 1.0E-08);
            Assert.assertEquals("Wrong arg perihelion (day " + day + ")", argPerihelionExpected, argPerihelionActual, 1.0E-07);
            Assert.assertEquals("Wrong long asc node (day " + day + ")", longNodeExpected, longNodeActual, 1.0E-13);
            
            // Next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            day++;
        }
    }
    
    @Test
    public void testComputeOrbitalElementsFromPositionVelocityMercury() {
        System.out.println("computeOrbitalElementsFromPositionVelocity (Mercury)");
        
        // Start date is Jan 1, 2017
        GregorianCalendar date = new GregorianCalendar(2017,0,1);
        
        // Orbital parameters for Mercury
        double[] orbitPars = SolarSystemParameters.getInstance().getOrbitParameters("Mercury");
       
        // Mercury orbits around the sun in 88 days
        int nrDays = 88;
        int day = 0;
        while (day < nrDays) {

            // Compute orbital elements for given date
            double orbitElementsExpected[] = EphemerisUtil.computeOrbitalElements(orbitPars, date);

            // Compute (x,y,z) position of Mercury [m] from orbital elements
            Vector3D position = EphemerisUtil.computePosition(orbitElementsExpected);

            // Compute (x,y,z) velocity of Mercury [m/s] from orbital elements
            double muSun = SolarSystemParameters.getInstance().getMu("Sun");
            Vector3D velocity = EphemerisUtil.computeVelocity(muSun,orbitElementsExpected);

            // Compute orbital elements from position and velocity
            double orbitElementsActual[]
                    = EphemerisUtil.computeOrbitalElementsFromPositionVelocity(muSun,position,velocity);

            // Expected orbital elements
            double axisExpected = orbitElementsExpected[0]; // semi-major axis [au]
            double eccentricityExpected = orbitElementsExpected[1]; // eccentricity [-]
            double inclinationExpected = orbitElementsExpected[2]; // inclination [degrees]
            double meanAnomalyExpected = orbitElementsExpected[3]; // mean anomaly [degrees]
            double argPerihelionExpected = orbitElementsExpected[4]; // argument of perihelion [degrees]
            double longNodeExpected = orbitElementsExpected[5]; // longitude of ascending node [degrees]

            // Actual orbital elements
            double axisActual = orbitElementsActual[0]; // semi-major axis [au]
            double eccentricityActual = orbitElementsActual[1]; // eccentricity [-]
            double inclinationActual = orbitElementsActual[2]; // inclination [degrees]
            double meanAnomalyActual = orbitElementsActual[3]; // mean anomaly [degrees]
            double argPerihelionActual = orbitElementsActual[4]; // argument of perihelion [degrees]
            double longNodeActual = orbitElementsActual[5]; // longitude of ascending node [degrees]
            
            // Compare actual orbital elements to expected orbital elements
            Assert.assertEquals("Wrong semi-major axis(day " + day + ")", axisExpected, axisActual, 1.0E-14);
            Assert.assertEquals("Wrong eccentricity (day " + day + ")", eccentricityExpected, eccentricityActual, 1.0E-14);
            Assert.assertEquals("Wrong inclination (day " + day + ")", inclinationExpected, inclinationActual, 1.0E-13);
            Assert.assertEquals("Wrong mean anomaly (day " + day + ")", meanAnomalyExpected, meanAnomalyActual, 1.0E-10);
            Assert.assertEquals("Wrong arg perihelion (day " + day + ")", argPerihelionExpected, argPerihelionActual, 1.0E-10);
            Assert.assertEquals("Wrong long asc node (day " + day + ")", longNodeExpected, longNodeActual, 1.0E-13);

            // Next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            day++;
        }
    }

    /**
     * Compute azimuth, elevation, and distance for an object at given position in heliocentric
     * ecliptic J2000 coordinates as observed from given location and date/time from the
     * surface of the Earth.
     * @param position  Position (x,y,z) in ecliptic J2000 coordinates relative to the Sun [m]
     * @param latitude  latitude of observation location [degrees]
     * @param longitude longitude of observation location [degrees]
     * @param dateTime  date/time of observation [UTC]
     * @return azimuth [degrees], elevation [degrees], distance [a.u.]
     */
    @Test
    public void testComputeAzimuthElevationDistanceMars() {
        /*
         * https://theskylive.com/planetarium?obj=mars&date=2021-01-01&h=17&m=24
         * Interactive star map of the sky visible from: [52.0000 N, 4.8333 E]
         * Time: 01-Jan-2021 18:24 Europe/Amsterdam
         * Object: Mars [info|live][less]
         * Right Asc: 01h 40m 21.9s Decl: 11° 21' 57.8" (J2000) [HMS|Dec]
         * Magnitude: -0.22 Altitude: 47° Solar Elongation: 106.1° Constellation: Psc
         * Sun distance: 225.94 Million Km Earth distance: 135.41 Million Km
         * Rise: 12:36 Transit: 19:34 Set: 02:36 Europe/Amsterdam
         */
        double azimuthExpected = 154.1;
        double elevationExpected = 46.7;
        double distanceExpected = 1.3541E11/SolarSystemParameters.ASTRONOMICALUNIT;

        GregorianCalendar dateTime =
                new GregorianCalendar(2021,0,1,17,24);
        dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        double latitude = 52.0000;
        double longitude = 4.8333;

        Vector3D positionMars = EphemerisSolarSystem.getInstance().getBodyPosition("Mars",dateTime);

        double[] result =
                EphemerisUtil.computeAzimuthElevationDistance(positionMars, latitude, longitude, dateTime);
        double azimuthActual = result[0];
        double altitudeActual = result[1];
        double distanceActual = result[2];

        Assert.assertEquals("Wrong azimuth",azimuthExpected,azimuthActual,1.0);
        Assert.assertEquals("Wrong elevation",elevationExpected,altitudeActual,1.0);
        Assert.assertEquals("Wrong distance",distanceExpected,distanceActual,1.0E-3);
    }

    @Test
    public void testComputeAzimuthElevationDistanceConjunctionJupiterSaturn() {
        /*
         * https://theskylive.com/planetarium?objects=sun-moon-jupiter-mars-mercury-venus-saturn-uranus-neptune-pluto&localdata=52.0000%7C5.0000%7C%5B52.0000+N%2C+5.0000+E%5D%7CEurope%2FAmsterdam%7C0&obj=jupiter&h=13&m=00&date=2020-12-21#ra|20.163275462995788|dec|-20.584536554783945|fov|10
         * Interactive star map of the sky visible from: [52.0000 N, 5.0000 E]
         * Time: 21-Dec-2020 14:00 Europe/Amsterdam
         *
         * Object: Jupiter [info|live][less]
         * Right Asc: 20h 09m 47.8s Decl: -20° 35' 04.3" (J2000) [HMS|Dec]
         * Magnitude: -1.97 Altitude: 17° Solar Elongation: 30.3° Constellation: Cap
         * Sun distance: 762.84 Million Km Earth distance: 886.26 Million Km
         * Rise: 10:43 Transit: 14:47 Set: 18:51 Europe/Amsterdam
         *
         * Object: Saturn [info|live][less]
         * Right Asc: 20h 09m 48.0s Decl: -20° 28' 46.8" (J2000) [HMS|Dec]
         * Magnitude: 1.41 Altitude: 17° Solar Elongation: 30.3° Constellation: Cap
         * Sun distance: 1494.28 Million Km Earth distance: 1619.44 Million Km
         * Rise: 10:42 Transit: 14:47 Set: 18:52 Europe/Amsterdam
         */

        double azimuthJupiterExpected = 168.4;
        double elevationJupiterExpected = 16.6;
        double distanceJupiterExpected = 8.8626E11/SolarSystemParameters.ASTRONOMICALUNIT;

        double azimuthSaturnExpected = 168.4;
        double elevationSaturnExpected = 16.8;
        double distanceSaturnExpected = 1.61944E12/SolarSystemParameters.ASTRONOMICALUNIT;

        GregorianCalendar dateTime =
                new GregorianCalendar(2020,11,21,13,0);
        dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        double latitude = 52.0;
        double longitude = 5.0;

        Vector3D positionJupiter =
                EphemerisSolarSystem.getInstance().getBodyPosition("Jupiter",dateTime);
        Vector3D positionSaturn =
                EphemerisSolarSystem.getInstance().getBodyPosition("Saturn",dateTime);

        double[] resultJupiter =
                EphemerisUtil.computeAzimuthElevationDistance(positionJupiter, latitude, longitude, dateTime);
        double azimuthJupiterActual   = resultJupiter[0];
        double elevationJupiterActual = resultJupiter[1];
        double distanceJupiterActual  = resultJupiter[2];

        double[] resultSaturn =
                EphemerisUtil.computeAzimuthElevationDistance(positionSaturn, latitude, longitude, dateTime);
        double azimuthSaturnActual   = resultSaturn[0];
        double elevationSaturnActual = resultSaturn[1];
        double distanceSaturnActual  = resultSaturn[2];

        Assert.assertEquals("Wrong azimuth Jupiter",azimuthJupiterExpected,azimuthJupiterActual,1.0);
        Assert.assertEquals("Wrong elevation Jupiter",elevationJupiterExpected,elevationJupiterActual,1.0);
        Assert.assertEquals("Wrong distance Jupiter",distanceJupiterExpected,distanceJupiterActual,1.0E-3);

        Assert.assertEquals("Wrong azimuth Saturn",azimuthSaturnExpected,azimuthSaturnActual,1.0);
        Assert.assertEquals("Wrong elevation Saturn",elevationSaturnExpected,elevationSaturnActual,1.0);
        Assert.assertEquals("Wrong distance Saturn",distanceSaturnExpected,distanceSaturnActual,1.0E-3);
    }
}
