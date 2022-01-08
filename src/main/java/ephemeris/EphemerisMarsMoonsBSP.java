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
 * Ephemeris for the Martian moons Phobos and Deimos.
 * This ephemeris is valid from 1 Jan 1970 through 31 Dec 2025.
 * @author Nico Kuijpers
 */

public class EphemerisMarsMoonsBSP implements IEphemeris {

    // File names of BSP files
    private final String BSPfilenameA = "EphemerisFilesBSP/mar097_MarsSystem_1970_1989.bsp";
    private final String BSPfilenameB = "EphemerisFilesBSP/mar097_MarsSystem_1990_2009.bsp";
    private final String BSPfilenameC = "EphemerisFilesBSP/mar097_MarsSystem_2010_2029.bsp";

    // Observer code for BSP file
    private final int observer = 4;

    // Target codes for BSP file
    private Map<String,Integer> targets;

    // Bodies for which ephemeris can be computed or approximated
    private List<String> bodies;

    // First valid date for ephemeris 1970-1989
    private final GregorianCalendar firstValidDateA;

    // First valid date for ephemeris 1990-2009
    private final GregorianCalendar firstValidDateB;

    // First valid date for ephemeris 2010-2029
    private final GregorianCalendar firstValidDateC;

    // Last valid date for ephemeris 2010-2029
    private final GregorianCalendar lastValidDateC;

    // Singleton instance
    private static IEphemeris instance = null;

    // Read ephemeris from BSP file
    private SPK[] spk = new SPK[3];

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisMarsMoonsBSP() {

        /*
         * The following BSP files are used for the ephemeris of Phobos and Deimos:
         * mar097_MarsSystem_1970_1989.bsp
         * mar097_MarsSystem_1990_2009.bsp
         * mar097_MarsSystem_2010_2029.bsp
         *
         * These BSP files are generated from mar097.bsp
         * https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/mar097.bsp
         * using
         * python -m jplephem excerpt --targets 4,401,402,499 1969/12/30 1990/01/02 mar097.bsp mar097_MarsSystem_1970_1989.bsp
         * python -m jplephem excerpt --targets 4,401,402,499 1989/12/30 2010/01/02 mar097.bsp mar097_MarsSystem_1990_2009.bsp
         * python -m jplephem excerpt --targets 4,401,402,499 2009/12/30 2030/01/02 mar097.bsp mar097_MarsSystem_2010_2029.bsp
         * https://pypi.org/project/jplephem/
         *
         * mar097_MarsSystem_1970_1989.bsp (46.1 MB)
         * First valid date 2440586.00 = December 30, 1969, 12:00 UTC
         * Last valid date 2447894.25 = January 2, 1990, 18:00 UTC
         * File type DAF/SPK and format LTL-IEEE with 4 segments:
         * 2440586.00..2447894.25  Type 3  Mars Barycenter (4) -> Phobos (401)
         * 2440586.00..2447894.50  Type 3  Mars Barycenter (4) -> Deimos (402)
         * 2440586.00..2447894.25  Type 3  Mars Barycenter (4) -> Mars (499)
         * 2440560.50..2447920.50  Type 2  Solar System Barycenter (0) -> Mars Barycenter (4)
         *
         * mar097_MarsSystem_1990_2009.bsp (46.1 MB)
         * First valid date 2447891.00 = December 30, 1989, 12:00 UTC
         * Last valid date 2455199.25 = January 2, 2010, 18:00 UTC
         * File type DAF/SPK and format LTL-IEEE with 4 segments:
         * 2447891.00..2455199.25  Type 3  Mars Barycenter (4) -> Phobos (401)
         * 2447891.00..2455199.50  Type 3  Mars Barycenter (4) -> Deimos (402)
         * 2447891.00..2455199.25  Type 3  Mars Barycenter (4) -> Mars (499)
         * 2447888.50..2455216.50  Type 2  Solar System Barycenter (0) -> Mars Barycenter (4)
         *
         * mar097_MarsSystem_2010_2029.bsp (46.1 MB)
         * First valid date 2455196.00 = December 30, 2009, 12:00 UTC
         * Last valid date 2462504.25 = January 2, 2030, 18:00 UTC
         * File type DAF/SPK and format LTL-IEEE with 4 segments:
         * 2455196.00..2462504.25  Type 3  Mars Barycenter (4) -> Phobos (401)
         * 2455196.00..2462504.50  Type 3  Mars Barycenter (4) -> Deimos (402)
         * 2455196.00..2462504.25  Type 3  Mars Barycenter (4) -> Mars (499)
         * 2455184.50..2462512.50  Type 2  Solar System Barycenter (0) -> Mars Barycenter (4)
         */

        // Target codes for BSP file
        targets = new HashMap<>();
        targets.put("Mars",499);
        targets.put("Phobos",401);
        targets.put("Deimos",402);

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.addAll(targets.keySet());

        // First valid date for ephemeris 1970-1989 is Jan 1, 1970
        firstValidDateA = new GregorianCalendar(1970,0,1);
        firstValidDateA.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for ephemeris 1990-2009 is Jan 1, 1990
        firstValidDateB = new GregorianCalendar(1990,0,1);
        firstValidDateB.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for ephemeris 2010-2029 is Jan 1, 2010
        firstValidDateC = new GregorianCalendar(2010,0,1);
        firstValidDateC.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for ephemeris 2010-2029 Jan 1, 2030
        lastValidDateC = new GregorianCalendar(2030,0,1);
        lastValidDateC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get instance of EphemerisPlutoMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisMarsMoonsBSP();
        }
        return instance;
    }

    @Override
    public GregorianCalendar getFirstValidDate() {
        return firstValidDateA;
    }

    @Override
    public GregorianCalendar getLastValidDate() {
        return lastValidDateC;
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
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris of Mars System");
        }

        // Check whether date is valid
        if (date.before(firstValidDateA) || date.after(lastValidDateC)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris of Mars System");
        }

        // Initialize SPK and open file to read when needed for the first time
        int index;
        if (date.before(firstValidDateB)) {
            if (spk[0] == null) {
                // Open ephemeris file to read ephemeris from Jan 1, 1970 through Dec 31, 1989
                spk[0] = new SPK();
                spk[0].initWithBSPFile(BSPfilenameA);
            }
            index = 0;
        }
        else {
            if (date.before(firstValidDateC)) {
                if (spk[1] == null) {
                    // Open ephemeris file to read ephemeris from Jan 1, 1990 through Dec 31, 2009
                    spk[1] = new SPK();
                    spk[1].initWithBSPFile(BSPfilenameB);
                }
                index = 1;
            }
            else {
                if (spk[2] == null) {
                    // Open ephemeris file to read ephemeris from Jan 1, 2010 through Dec 31, 2029
                    spk[2] = new SPK();
                    spk[2].initWithBSPFile(BSPfilenameC);
                }
                index = 2;
            }
        }

        // Number of seconds past J2000
        double et = EphemerisUtil.computeNrSecondsPastJ2000(date);

        // Target
        int target = targets.get(name);

        // Observer is barycenter of Jupiter System
        Vector3D[] moonPosVel = spk[index].getPositionVelocity(et,target,observer);
        Vector3D[] marsPosVel = spk[index].getPositionVelocity(et,499,observer);
        moonPosVel[0] = moonPosVel[0].minus(marsPosVel[0]);
        moonPosVel[1] = moonPosVel[1].minus(marsPosVel[1]);

        // Position and velocity are computed for J2000 frame
        Vector3D positionInvTrans = EphemerisUtil.inverseTransformJ2000(moonPosVel[0]);
        Vector3D velocityInvTrans = EphemerisUtil.inverseTransformJ2000(moonPosVel[1]);
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
}

