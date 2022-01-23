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

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Ephemeris for the entire Galileo mission.
 * This ephemeris is valid from October 19, 1989 through September 22, 2003.
 * @author Nico Kuijpers
 */

public class EphemerisGalileoBSP implements IEphemeris {

    // Bodies for which ephemeris can be computed or approximated
    private List<String> bodies;

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // Singleton instance
    private static IEphemeris instance = null;

    // Read ephemeris from BSP file
    private SPK spk;

    // Compute position and velocity in case no record is found
    private Vector3D positionStored = new Vector3D();
    private Vector3D velocityStored = new Vector3D();
    private GregorianCalendar dateTimeStored = null;

    // Use ephemeris with center body Sun for Interplanetary cruise
    private final IEphemeris ephemerisGalileoCruise;

    // Use ephemeris with center body Jupiter for Primary tour, GEM, and GMM
    private final IEphemeris ephemerisGalileoJupiter;

    // Use ephemeris of Solar System for Jupiter with respect to the Sun
    private final IEphemeris ephemerisSolarSystem;

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisGalileoBSP() {

        /*
         * https://naif.jpl.nasa.gov/pub/naif/GLL/kernels/spk/aareadme.txt
         * Galileo Orbiter:
         *
         * Phase                             Coverage                    SPK file
         * ================================  ==========================  ============
         * Interplanetary cruise             1989-OCT-19 to 1995-JUL-02  s970311a.bsp
         * Primary tour                      1995-JUL-01 to 1998-JAN-01  s980326a.bsp
         * GEM (Galileo Europa Mission)      1997-DEC-01 to 2000-FEB-01  s000131a.bsp
         * GMM (Galileo Millenium Mission)   2000-FEB-01 to 2003-SEP-22  s030916a.bsp
         *
         * Galileo Probe:
         *
         * Phase                             Coverage                    SPK file
         * ================================  ==========================  ============
         * Probe separation through descent  1995-JUL-13 to 1995-DEC-07  s960730a.bsp
         */

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.add("Galileo");

        // Use ephemeris with center body Sun for interplanetary cruise
        ephemerisGalileoCruise = EphemerisGalileoCruiseBSP.getInstance();

        // Use ephemeris with center body Jupiter for Primary tour, GEM, and GMM
        ephemerisGalileoJupiter = EphemerisGalileoJupiterBSP.getInstance();

        // Use ephemeris of Solar System for Jupiter with respect to the Sun
        ephemerisSolarSystem = EphemerisSolarSystem.getInstance();

        // First valid date from ephemeris of Interplanetary cruise
        firstValidDate = ephemerisGalileoCruise.getFirstValidDate();

        // Last valid date from ephemeris for Primary tour, GEM, and GMM
        lastValidDate = ephemerisGalileoJupiter.getLastValidDate();
    }

    /**
     * Get instance of Ephemeris for Galileo spacecraft.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisGalileoBSP();
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
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris of Galileo spacecraft");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris of Galileo spacecraft");
        }

        // Check if ephemeris for Interplanetary cruise can be used
        if (date.before(ephemerisGalileoCruise.getLastValidDate())) {
            // Use ephemeris with center body Sun for interplanetary cruise
            return ephemerisGalileoCruise.getBodyPositionVelocity(name, date);
        }

        // Use ephemeris with center body Jupiter for Primary tour, GEM, and GMM
        Vector3D[] bodyPosVel = ephemerisGalileoJupiter.getBodyPositionVelocity(name, date);
        Vector3D[] jupiterPosVel = ephemerisSolarSystem.getBodyPositionVelocity("Jupiter", date);
        return new Vector3D[]{jupiterPosVel[0].plus(bodyPosVel[0]), jupiterPosVel[1].plus(bodyPosVel[1])};
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

