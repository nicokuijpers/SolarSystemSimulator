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
 * Determine position of rings of Saturn for given date and time.
 * @author Nico Kuijpers
 */
public class EphemerisRingsOfSaturn {

    // Radii of inner and outer visible parts of rings
    // https://en.wikipedia.org/wiki/Rings_of_Saturn
    private final static double innerRadiusRing = 7.45E07;   // Distance of C-ring from the center of Saturn
    private final static double outerRadiusRing = 1.4022E08; // Distance of F-ring from the center of Saturn

    /**
     * Compute positions of inner ring with respect to planet center.
     * @param dateTime date/time
     * @return positions of inner ring.
     */
    public static Vector3D[] innerRingPositions(GregorianCalendar dateTime) {

        // Orbit elements for ring
        double[] orbitElements = computeOrbitElementsRing(dateTime);

        // Set semi-major axis for inner ring
        orbitElements[0] = innerRadiusRing / SolarSystemParameters.ASTRONOMICALUNIT;

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
        orbitElements[0] = outerRadiusRing / SolarSystemParameters.ASTRONOMICALUNIT;

        // Array of positions of outer ring
        return EphemerisUtil.computeOrbit(orbitElements);
    }

    /**
     * Compute orbital elements for ring.
     * @param dateTime date/time
     * @return orbital elements.
     */
    private static double[] computeOrbitElementsRing(GregorianCalendar dateTime) {

        // Number of Julian centuries since Jan 1, 2000
        double T = EphemerisUtil.computeNrCenturiesPastJ2000(dateTime);

        // Inclination of the plane of the rings and longitude of ascending [degrees]
        // Reference: Astronomical Algorithms by Jean Meeus, 1991
        // See formula (44.1)
        double inclination = 28.075216 - 0.012998*T + 0.000004*T*T;
        double longNode = 169.508470 + 1.394681*T + 0.000412*T*T;

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
