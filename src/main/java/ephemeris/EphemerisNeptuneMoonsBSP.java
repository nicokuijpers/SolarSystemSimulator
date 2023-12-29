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
 * Ephemeris for Neptune System.
 * This ephemeris is valid from 1 Jan 1970 through 31 Dec 2029.
 * @author Nico Kuijpers
 */

public class EphemerisNeptuneMoonsBSP implements IEphemeris {

    // File names of BSP files
    private final String BSPfilenameA = "EphemerisFilesBSP/nep081_NeptuneSystem_1970_1999.bsp";
    private final String BSPfilenameB = "EphemerisFilesBSP/nep081_NeptuneSystem_2000_2029.bsp";

    // Observer code for BSP file
    private final int observer = 8;

    // Target codes for BSP file
    private Map<String,Integer> targets;

    // Bodies for which ephemeris can be computed or approximated
    private List<String> bodies;

    // First valid date for ephemeris 1970-1999
    private final GregorianCalendar firstValidDateA;

    // First valid date for ephemeris 2000-2029
    private final GregorianCalendar firstValidDateB;

    // Last valid date for ephemeris 2000-2029
    private final GregorianCalendar lastValidDateB;

    // Singleton instance
    private static IEphemeris instance = null;

    // Read ephemeris from BSP file
    private SPK[] spk = new SPK[2];

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisNeptuneMoonsBSP() {

        /*
         * The following BSP files are used for the ephemeris of the Neptune System:
         * nep081_NeptuneSystem_1970_1999.bsp
         * nep081_NeptuneSystem_2000_2029.bsp
         *
         * These BSP files are generated from nep081.bsp
         * https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/a_old_versions/nep081.bsp
         * using
         * python -m jplephem excerpt --targets 8,801,802,808,899 1969/12/30 2000/01/02 nep081.bsp nep081_NeptuneSystem_1970_1999.bsp
         * python -m jplephem excerpt --targets 8,801,802,808,899 1999/12/30 2030/01/02 nep081.bsp nep081_NeptuneSystem_2000_2029.bsp
         * https://pypi.org/project/jplephem/
         *
         * nep081_NeptuneSystem_1970_1999.bsp (22.4 MB)
         * Date 1969/12/30 = JD 2440586
         * Date 2000/01/02 = JD 2451546
         * File type DAF/SPK and format LTL-IEEE with 5 segments:
         * 2440582.50..2451546.50  Type 3  Neptune Barycenter (8) -> Triton (801)
         * 2440582.50..2451546.50  Type 3  Neptune Barycenter (8) -> Nereid (802)
         * 2440586.00..2451546.50  Type 3  Neptune Barycenter (8) -> Proteus (808)
         * 2440582.50..2451546.50  Type 3  Neptune Barycenter (8) -> Neptune (899)
         * 2440560.50..2451568.50  Type 2  Solar System Barycenter (0) -> Neptune Barycenter (8)
         *
         * nep081_NeptuneSystem_2000_2029.bsp (22.4 MB)
         * File type DAF/SPK and format LTL-IEEE with 5 segments:
         * 2451542.50..2462506.50  Type 3  Neptune Barycenter (8) -> Triton (801)
         * 2451542.50..2462506.50  Type 3  Neptune Barycenter (8) -> Nereid (802)
         * 2451543.00..2462504.50  Type 3  Neptune Barycenter (8) -> Proteus (808)
         * 2451542.50..2462506.50  Type 3  Neptune Barycenter (8) -> Neptune (899)
         * 2451536.50..2462512.50  Type 2  Solar System Barycenter (0) -> Neptune Barycenter (8)
         */

        // Target codes for BSP file
        targets = new HashMap<>();
        targets.put("Neptune",899);
        targets.put("Triton",801);
        targets.put("Nereid",802);
        targets.put("Proteus",808);

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.addAll(targets.keySet());

        // First valid date for ephemeris 1970-1999 is Jan 1, 1970
        firstValidDateA = new GregorianCalendar(1970,0,1);
        firstValidDateA.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for ephemeris 2000-2029 is Jan 1, 2000
        firstValidDateB = new GregorianCalendar(2000,0,1);
        firstValidDateB.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for ephemeris 2000-2029 Jan 1, 2030
        lastValidDateB = new GregorianCalendar(2030,0,1);
        lastValidDateB.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get instance of EphemerisPlutoMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisNeptuneMoonsBSP();
        }
        return instance;
    }

    @Override
    public GregorianCalendar getFirstValidDate() {
        return firstValidDateA;
    }

    @Override
    public GregorianCalendar getLastValidDate() {
        return lastValidDateB;
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
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris of Neptune System");
        }

        // Check whether date is valid
        if (date.before(firstValidDateA) || date.after(lastValidDateB)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris of Neptune System");
        }

        // Initialize SPK and open file to read when needed for the first time
        int index;
        if (date.before(firstValidDateB)) {
            if (spk[0] == null) {
                // Open ephemeris file to read ephemeris from Jan 1, 1970 through Dec 31, 1999
                spk[0] = new SPK();
                spk[0].initWithBSPFile(BSPfilenameA);
            }
            index = 0;
        }
        else {
            if (spk[1] == null) {
                // Open ephemeris file to read ephemeris from Jan 1, 2000 through Dec 31, 2029
                spk[1] = new SPK();
                spk[1].initWithBSPFile(BSPfilenameB);
            }
            index = 1;
        }

        // Number of seconds past J2000
        double et = EphemerisUtil.computeNrSecondsPastJ2000(date);

        // Target
        int target = targets.get(name);

        // Observer is barycenter of Neptune System
        Vector3D[] moonPosVel = spk[index].getPositionVelocity(et,target,observer);
        Vector3D[] neptunePosVel = spk[index].getPositionVelocity(et,899,observer);
        moonPosVel[0] = moonPosVel[0].minus(neptunePosVel[0]);
        moonPosVel[1] = moonPosVel[1].minus(neptunePosVel[1]);

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

