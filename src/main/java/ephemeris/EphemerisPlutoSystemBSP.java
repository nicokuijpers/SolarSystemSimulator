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
 * Ephemeris for the Pluto System.
 * This ephemeris is valid from 1 Jan 1970 through 31 Dec 2025.
 * @author Nico Kuijpers
 */

public class EphemerisPlutoSystemBSP implements IEphemeris {

    // File name of BSP file
    private final String BSPfilename = "EphemerisFilesBSP/plu058_PlutoSystem_1970_2025.bsp";

    // Observer code for BSP file
    private final int observer = 9;

    // Target codes for BSP file
    private Map<String,Integer> targets;

    // Bodies for which ephemeris can be computed or approximated
    private List<String> bodies;

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // Singleton instance
    private static IEphemeris instance = null;

    // Read ephemeris from BSP file
    private SPK spk = null;

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisPlutoSystemBSP() {

        /*
         * BSP file plu058_PlutoSystem_1970_2025 was generated from plu058.bsp
         * https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/plu058.bsp
         * using
         * python -m jplephem excerpt --targets 9,901,902,903,904,905,999 1970/1/1 2025/12/31 plu058.bsp plu058_PlutoSystem_1970_2025.bsp
         * https://pypi.org/project/jplephem/
         */

        // Target codes for BSP file
        targets = new HashMap<>();
        targets.put("Pluto",999);
        targets.put("Charon",901);
        targets.put("Nix",902);
        targets.put("Hydra",903);
        targets.put("Kerberos",904);
        targets.put("Styx",905);

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.addAll(targets.keySet());

        // First valid date Jan 2, 1970
        firstValidDate = new GregorianCalendar(1970,0,2);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date Dec 31, 2025
        lastValidDate = new GregorianCalendar(2025,11,31);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get instance of EphemerisPlutoMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisPlutoSystemBSP();
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
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris of Pluto System");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris of Pluto System");
        }

        // Initialize SPK and open file to read when needed for the first time
        if (spk == null) {
            spk = new SPK();
            spk.initWithBSPFile(BSPfilename);
        }

        // Number of seconds past J2000
        double et = EphemerisUtil.computeNrSecondsPastJ2000(date);

        // Target
        int target = targets.get(name);

        // Observer is barycenter of Pluto System
        int observer = 9;
        Vector3D[] result = spk.getPositionVelocity(et,target,observer);
        Vector3D position = result[0];
        Vector3D velocity = result[1];

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
}

