/*
 * Copyright (c) 2021 Nico Kuijpers and Marco Brassé
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

import java.util.*;

/**
 * Ephemeris for major planets, the Earth, and the Moon of the Solar System.
 * This ephemeris is valid from January 1, 1600 through December 31, 2200.
 * @author Nico Kuijpers and Marco Brassé
 */

public class EphemerisAccurateBSP implements IEphemeris {

    // File name of BSP files
    private final String BSPfilenameA = "EphemerisFilesBSP/de405_1600_1899.bsp";
    private final String BSPfilenameB = "EphemerisFilesBSP/de405_1900_2200.bsp";

    // Bodies for which ephemeris can be computed or approximated
    private List<String> bodies;

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // First valid julian date for de405_1900_2200.bsp is Jan 1, 1900
    // Julian date for January 1, 1990, 00:00 UTC is 2415020.5
    private final double firstValidJulianDateTimeB = 2415020.5;

    // Current Julian date/time for which positions and velocities are available
    private double currentJulianDateTime;

    // Positions of the major planets, Moon, and the Sun for current Julian date/time
    private final Vector3D[] currentPositions = new Vector3D[12];

    // Velocities of the major planets, Moon, and the Sun for current Julian date/time
    private final Vector3D[] currentVelocities = new Vector3D[12];

    // Indices for the major planets, Moon, and the Sun
    private final Map<String, Integer> indexMap;

    // Singleton instance
    private static IEphemeris instance = null;

    // Read ephemeris from BSP file
    private SPK[] spk = new SPK[2];

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisAccurateBSP() {

        /*
         * BSP file de405.bsp was obtained using ftp
         * ftp ssd.jpl.nasa.gov
         * cd pub/eph/planets/bsp
         * First valid date 2305424.50 = December 9, 1599
         * Last valid date 2525008.50 = February 20, 2201
         *
         * BSP files de405_1600_1899.bsp and de405_1900_2200.bsp were generated from de405.bsp
         * using
         * python -m jplephem excerpt 1000 1900 de405.bsp de405_1600_1899.bsp
         * python -m jplephem excerpt 1900 3000 de405.bsp de405_1900_2200.bsp
         * https://pypi.org/project/jplephem/
         *
         * de405.bsp:
         * First valid date 2305424.50 = December 9, 1599
         * Last valid date 2525008.50 = February 20, 2201
         * File type NAIF/DAF and format BIG-IEEE with 15 segments:
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Mercury Barycenter (1)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Venus Barycenter (2)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Earth Barycenter (3)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Mars Barycenter (4)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Jupiter Barycenter (5)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Saturn Barycenter (6)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Uranus Barycenter (7)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Neptune Barycenter (8)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Pluto Barycenter (9)
         * 2305424.50..2525008.50 Type 2 Solar System Barycenter (0) -> Sun (10)
         * 2305424.50..2525008.50 Type 2 Earth Barycenter (3) -> Moon (301)
         * 2305424.50..2525008.50 Type 2 Earth Barycenter (3) -> Earth (399)
         * 2305424.50..2525008.50 Type 2 Mercury Barycenter (1) -> Mercury (199)
         * 2305424.50..2525008.50 Type 2 Venus Barycenter (2) -> Venus (299)
         * 2305424.50..2525008.50 Type 2 Mars Barycenter (4) -> Mars (499)
         *
         * de405_1600_1899.bsp:
         * First valid date 2305424.50 = December 9, 1599, 00:00 UTC
         * Last valid date 2415024.50 = January 5, 1900, 00:00 UTC
         * File type NAIF/DAF and format BIG-IEEE with 15 segments:
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Mercury Barycenter (1)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Venus Barycenter (2)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Earth Barycenter (3)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Mars Barycenter (4)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Jupiter Barycenter (5)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Saturn Barycenter (6)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Uranus Barycenter (7)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Neptune Barycenter (8)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Pluto Barycenter (9)
         * 2305424.50..2415024.50 Type 2 Solar System Barycenter (0) -> Sun (10)
         * 2305424.50..2415024.50 Type 2 Earth Barycenter (3) -> Moon (301)
         * 2305424.50..2415024.50 Type 2 Earth Barycenter (3) -> Earth (399)
         * 2305424.50..2525008.50 Type 2 Mercury Barycenter (1) -> Mercury (199)
         * 2305424.50..2525008.50 Type 2 Venus Barycenter (2) -> Venus (299)
         * 2305424.50..2525008.50 Type 2 Mars Barycenter (4) -> Mars (499)
         *
         * de405_1900_2200.bsp:
         * First valid date 2305424.50 = December 28, 1899, 00:00 UTC
         * Last valid date 2415024.50 = February 20, 2201, 00:00 UTC
         * File type NAIF/DAF and format BIG-IEEE with 15 segments:
         * 2415016.50..2525008.50 Type 2 Solar System Barycenter (0) -> Mercury Barycenter (1)
         * 2415008.50..2525008.50 Type 2 Solar System Barycenter (0) -> Venus Barycenter (2)
         * 2415008.50..2525008.50 Type 2 Solar System Barycenter (0) -> Earth Barycenter (3)
         * 2414992.50..2525008.50 Type 2 Solar System Barycenter (0) -> Mars Barycenter (4)
         * 2414992.50..2525008.50 Type 2 Solar System Barycenter (0) -> Jupiter Barycenter (5)
         * 2414992.50..2525008.50 Type 2 Solar System Barycenter (0) -> Saturn Barycenter (6)
         * 2414992.50..2525008.50 Type 2 Solar System Barycenter (0) -> Uranus Barycenter (7)
         * 2414992.50..2525008.50 Type 2 Solar System Barycenter (0) -> Neptune Barycenter (8)
         * 2414992.50..2525008.50 Type 2 Solar System Barycenter (0) -> Pluto Barycenter (9)
         * 2415008.50..2525008.50 Type 2 Solar System Barycenter (0) -> Sun (10)
         * 2415020.50..2525008.50 Type 2 Earth Barycenter (3) -> Moon (301)
         * 2415020.50..2525008.50 Type 2 Earth Barycenter (3) -> Earth (399)
         * 2305424.50..2525008.50 Type 2 Mercury Barycenter (1) -> Mercury (199)
         * 2305424.50..2525008.50 Type 2 Venus Barycenter (2) -> Venus (299)
         * 2305424.50..2525008.50 Type 2 Mars Barycenter (4) -> Mars (499)
         */

        // Indices for planets, moon, and sun for DE405 ephemeris
        indexMap = new HashMap<>();
        indexMap.put("Mercury", 0);
        indexMap.put("Venus", 1);
        indexMap.put("EarthMoonBarycenter", 2);
        indexMap.put("Mars", 3);
        indexMap.put("Jupiter", 4);
        indexMap.put("Saturn", 5);
        indexMap.put("Uranus", 6);
        indexMap.put("Neptune", 7);
        indexMap.put("Pluto System", 8);
        indexMap.put("Sun", 9);
        indexMap.put("Earth", 10);
        indexMap.put("Moon", 11);

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.addAll(indexMap.keySet());

        // First valid date Jan 1, 1600
        firstValidDate = new GregorianCalendar(1600,0,1,0,0);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date Jan 1, 2201
        lastValidDate = new GregorianCalendar(2201,0,1,0,0);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Open ephemeris file to read ephemeris from Jan 1, 1600 through Dec 31, 1899 file when needed
        spk[0] = null;

        // Open ephemeris file to read ephemeris from Jan 1, 1900 through Dec 31, 2200
        spk[1] = new SPK();
        spk[1].initWithBSPFile(BSPfilenameB);

        // Initialize current Julian date/time, positions, and velocities
        GregorianCalendar today = new GregorianCalendar();
        today.setTimeZone(TimeZone.getTimeZone("UTC"));
        currentJulianDateTime = JulianDateConverter.convertCalendarToJulianDate(today);
        planetaryEphemeris(currentJulianDateTime);
    }

    /**
     * Get instance of EphemerisPlutoMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisAccurateBSP();
        }
        return instance;
    }

    @Override
    public GregorianCalendar getFirstValidDate() {
        return firstValidDate;
    }

    @Override
    public GregorianCalendar getLastValidDate() {
        return lastValidDate;
    }

    @Override
    public List<String> getBodies() {
        return Collections.unmodifiableList(bodies);
    }

    @Override
    public Vector3D getBodyPosition(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[0];
    }

    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[1];
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {

        // Check whether body name is valid
        if (!bodies.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris of Solar System");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris of Solar System");
        }

        // Determine position and velocity at given date for body with given name
        updateEphemeris(date);
        int index = indexMap.get(name);
        Vector3D position = currentPositions[index];
        Vector3D velocity = currentVelocities[index];

        // Position and velocity are computed for J2000 frame
        Vector3D positionInvTrans = EphemerisUtil.inverseTransformJ2000(position);
        Vector3D velocityInvTrans = EphemerisUtil.inverseTransformJ2000(velocity);
        return new Vector3D[]{positionInvTrans,velocityInvTrans};
    }

    @Override
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Update current positions and velocities of the major planes, the Moon,
     * and the Sun.
     * @param date current date/time for ephemeris
     */
    public void updateEphemeris(GregorianCalendar date) {
        // Compute Julian date/time
        double julianDateTime = JulianDateConverter.convertCalendarToJulianDate(date);

        // Ensure that positions and velocities are available for given Julian date/time
        if (julianDateTime != currentJulianDateTime) {
            currentJulianDateTime = julianDateTime;
            planetaryEphemeris(julianDateTime);
        }
    }

    /**
     * Calculate position and velocity at given Julian date/time
     * of the major planets, the Moon, and the Sun.
     *
     * @param julianDateTime Julian date/time
     */
    private void planetaryEphemeris(double julianDateTime) {

        /*
         * Determine the ephemeris positions and velocities of each major planet,
         * the Moon and the Sun and store the results in arrays.
         * Note that the indices in the arrays are defined as follows
         * Mercury = 0, Venus = 1, Earth-Moon barycenter = 2,
         * Mars = 3, Jupiter = 4, Saturn = 5, Uranus = 6, Neptune = 7, Pluto = 8,
         * Sun = 9, Earth = 10, Moon = 11
         * Further note that ephemeris of Earth (target 399) and Moon (target 301) is
         * relative to Earth-Moon barycenter (observer 3)
         * Ephemeris of all other objects is relative to Solar System Barycenter (observer 0)
         */

        // Set index to read ephemeris from either de405_1600_1899.bsp or de405_1900_2200.bsp
        int index;
        if (julianDateTime < firstValidJulianDateTimeB) {
            if (spk[0] == null) {
                // Open ephemeris file de405_1600_1899.bsp to read ephemeris from Jan 1, 1600 through Dec 31, 1899
                spk[0] = new SPK();
                spk[0].initWithBSPFile(BSPfilenameA);
            }
            index = 0;
        }
        else {
            // Ephemeris file de405_1900_2200.bsp to read ephemeris from Jan 1, 1900 through Dec 31, 2199 is already open
            index = 1;
        }

        // Number of seconds past J2000
        double et = EphemerisUtil.computeNrSecondsPastJ2000(julianDateTime);

        // Position and velocity of all planets and the Sun relative to Solar System Barycenter (observer 0)
        for (int i = 0; i < 10; i++) {
            Vector3D[] positionVelocity = spk[index].getPositionVelocity(et, i + 1, 0);
            currentPositions[i] = positionVelocity[0];
            currentVelocities[i] = positionVelocity[1];
        }

        // Position and velocity of all planets relative to the Sun
        for (int i = 0; i < 9; i++) {
            currentPositions[i] = currentPositions[i].minus(currentPositions[9]);
            currentVelocities[i] = currentVelocities[i].minus(currentVelocities[9]);
        }

        // Position of the Earth relative to the Sun
        Vector3D[] positionVelocityEarth = spk[index].getPositionVelocity(et, 399, 3);
        currentPositions[10] = positionVelocityEarth[0].plus(currentPositions[2]);
        currentVelocities[10] = positionVelocityEarth[1].plus(currentVelocities[2]);

        // Position of the Moon relative to the Sun
        Vector3D[] positionVelocityMoon = spk[index].getPositionVelocity(et, 301, 3);
        currentPositions[11] = positionVelocityMoon[0].plus(currentPositions[2]);
        currentVelocities[11] = positionVelocityMoon[1].plus(currentVelocities[2]);
    }
}

