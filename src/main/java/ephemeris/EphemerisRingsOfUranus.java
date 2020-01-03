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

import util.Vector3D;

import java.util.GregorianCalendar;

/**
 * Determine position of rings of Uranus for given date and time.
 * @author Nico Kuijpers
 */
public class EphemerisRingsOfUranus {

    // https://nssdc.gsfc.nasa.gov/planetary/factsheet/uranringfact.html
    private final static double innerRadiusRing = 4.4718E07; // Alpha ring 44,718 km
    private final static double outerRadiusRing = 5.1149E07; // Epsilon ring 51,149 km

    /**
     * Compute positions of inner ring with respect to planet center.
     * @param dateTime date/time
     * @return positions of inner ring.
     */
    public static Vector3D[] innerRingPositions(GregorianCalendar dateTime) {

        // Orbit elements for ring
        double[] orbitElements = computeOrbitElementsRing(dateTime);

        // Set semi-major axis for inner ring
        orbitElements[0] = innerRadiusRing/ SolarSystemParameters.ASTRONOMICALUNIT;

        // Array of positions of inner ring
        return EphemerisUtil.computeOrbit(orbitElements);
    }

    /**
     * Compute positions of outer ring with respect to planet center.
     * @param dateTime date/time
     * @return positions of outer ring.
     */
    public static Vector3D[] outerRingPositions(GregorianCalendar dateTime) {

        // Orbit elements for ring
        double[] orbitElements = computeOrbitElementsRing(dateTime);

        // Set semi-major axis for outer ring
        orbitElements[0] = outerRadiusRing/ SolarSystemParameters.ASTRONOMICALUNIT;

        // Array of positions of outer ring
        return EphemerisUtil.computeOrbit(orbitElements);
    }

    /**
     * Compute orbital elements for ring.
     * @param dateTime date/time
     * @return orbital elements.
     */
    private static double[] computeOrbitElementsRing(GregorianCalendar dateTime) {

        /*
        // Parameters of z-axis of Uranus
        double[] zAxisParameters = SolarSystemParameters.getInstance().getZaxisParameters("Uranus");

        // Pole epoch of oblate planet [JED]
        double poleEpoch = zAxisParameters[0];

        // Right ascension of z-axis of oblate planet [degrees]
        double rightAscensionZaxis = zAxisParameters[1];

        // Declination of z-axis of oblate planet [degrees]
        double declinationZaxis = zAxisParameters[2];

        // Rate of right ascension of z-axis of oblate planet [degrees/century]
        double rightAscensionZaxisRate = zAxisParameters[3];

        // Rate of declination of z-axis of oblate planet [degrees/century]
        double declinationZaxisRate = zAxisParameters[4];

        // Compute current right ascension and declination of z-axis
        double Tjed = JulianDateConverter.convertCalendarToJulianDate(dateTime);
        double nrCenturies = (Tjed - poleEpoch) / EphemerisUtil.NRDAYSPERCENTURY;
        double alphaDeg = rightAscensionZaxis + nrCenturies * rightAscensionZaxisRate;
        double deltaDeg = declinationZaxis + nrCenturies * declinationZaxisRate;

        // Orbital elements
        double[] orbitElements = new double[6];
        orbitElements[0] = 1.0;         // semi-major axis
        orbitElements[1] = 0.0;         // eccentricity
        orbitElements[2] = alphaDeg;    // inclination
        orbitElements[3] = 0.0;         // mean anomaly
        orbitElements[4] = 0.0;         // argument of Perifocus
        orbitElements[5] = -deltaDeg;   // longitude of ascending node
        return orbitElements;
        */

        // Alpha and delta in radians obtained from EphemerisUranusMoons
        double alfa  = 1.3370385623111227;
        double delta = 0.26236177166923647;
        double inclination = Math.toDegrees(alfa);
        double longNode = Math.toDegrees(-delta);

        // Orbital elements
        double[] orbitElements = new double[6];
        orbitElements[0] = 1.0;         // semi-major axis
        orbitElements[1] = 0.0;         // eccentricity
        orbitElements[2] = inclination; // inclination
        orbitElements[3] = 0.0;         // mean anomaly
        orbitElements[4] = 0.0;         // argument of Perifocus
        orbitElements[5] = longNode;    // longitude of ascending node
        return orbitElements;
    }
}
