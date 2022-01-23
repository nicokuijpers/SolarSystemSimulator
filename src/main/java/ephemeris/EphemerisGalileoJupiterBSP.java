/*
 * Copyright (c) 2022 Nico Kuijpers and Marco Brassé
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
 * Ephemeris for the Galileo mission after the interplanetary cruise.
 * It includes the Primary tour, Galileo Europa Mission (GEM), and
 * Galileo Millennium Mission (GMM). Note that for this ephemeris,
 * the center body is Jupiter (not the Sun).
 * This ephemeris is valid from July 1, 1995 through September 22, 2003.
 * @author Nico Kuijpers
 */

public class EphemerisGalileoJupiterBSP implements IEphemeris {

    // Files name of BSP files
    private final String BSPfilenameA = "EphemerisFilesBSP/s980326a.bsp";
    private final String BSPfilenameB = "EphemerisFilesBSP/s000131a.bsp";
    private final String BSPfilenameC = "EphemerisFilesBSP/s030916a.bsp";

    // Target codes for BSP file
    private Map<String,Integer> targets;

    // Bodies for which ephemeris can be computed or approximated
    private List<String> bodies;

    // First valid date for ephemeris Primary tour 1995-JUL-01 to 1998-JAN-01
    private final GregorianCalendar firstValidDateA;

    // First valid date for ephemeris GEM (Galileo Europa Mission) 1997-DEC-01 to 2000-FEB-01
    private final GregorianCalendar firstValidDateB;

    // First valid date for ephemeris GMM (Galileo Millenium Mission) 2000-FEB-01 to 2003-SEP-22
    private final GregorianCalendar firstValidDateC;

    // Last valid date for ephemeris GMM (Galileo Millenium Mission) 2000-FEB-01 to 2003-SEP-22
    private final GregorianCalendar lastValidDateC;

    // Use ephemeris of Solar System for Jupiter with respect to the Sun
    private final IEphemeris ephemerisSolarSystem;

    // Singleton instance
    private static IEphemeris instance = null;

    // Read ephemeris from BSP file
    private SPK[] spk = new SPK[3];

    // Compute position and velocity in case no record is found
    private Vector3D positionStored = new Vector3D();
    private Vector3D velocityStored = new Vector3D();
    private GregorianCalendar dateTimeStored = null;

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisGalileoJupiterBSP() {

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

        /*
        python -m jplephem spk s980326a.bsp
        File type NAIF/DAF and format BIG-IEEE with 168 segments:
        2449899.50..2449974.46  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449974.46..2450042.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2449899.50..2450042.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2449899.50..2450042.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450042.50..2450063.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450042.50..2450063.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450042.50..2450063.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450042.50..2450063.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450063.50..2450252.21  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450063.50..2450252.21  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450063.50..2450252.21  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450063.50..2450252.21  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450063.50..2450252.21  Type 2  Earth Barycenter (3) -> Moon (301)
        2450063.50..2450252.21  Type 2  Earth Barycenter (3) -> Earth (399)
        2450063.50..2450252.21  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450063.50..2450252.21  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450063.50..2450252.21  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450063.50..2450252.21  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450063.50..2450252.21  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450252.21..2450316.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450252.21..2450316.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450252.21..2450316.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450252.21..2450316.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450252.21..2450316.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2450252.21..2450316.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2450252.21..2450316.50  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450252.21..2450316.50  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450252.21..2450316.50  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450252.21..2450316.50  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450252.21..2450316.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450316.50..2450328.00  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450316.50..2450328.00  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450316.50..2450328.00  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450316.50..2450328.00  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450316.50..2450328.00  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450316.50..2450328.00  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450316.50..2450328.00  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450316.50..2450328.00  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450316.50..2450328.00  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450316.50..2450328.00  Type 2  Earth Barycenter (3) -> Moon (301)
        2450316.50..2450328.00  Type 2  Earth Barycenter (3) -> Earth (399)
        2450328.00..2450376.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450328.00..2450376.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450328.00..2450376.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450328.00..2450376.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450328.00..2450376.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2450328.00..2450376.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2450328.00..2450376.50  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450328.00..2450376.50  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450328.00..2450376.50  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450328.00..2450376.50  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450328.00..2450376.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450376.50..2450420.04  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450376.50..2450420.04  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450376.50..2450420.04  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450376.50..2450420.04  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450376.50..2450420.04  Type 2  Earth Barycenter (3) -> Moon (301)
        2450376.50..2450420.04  Type 2  Earth Barycenter (3) -> Earth (399)
        2450376.50..2450420.04  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450376.50..2450420.04  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450376.50..2450420.04  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450376.50..2450420.04  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450376.50..2450420.04  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450420.04..2450486.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450420.04..2450486.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450420.04..2450486.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450420.04..2450486.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450420.04..2450486.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2450420.04..2450486.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2450420.04..2450486.50  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450420.04..2450486.50  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450420.04..2450486.50  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450420.04..2450486.50  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450420.04..2450486.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450486.50..2450524.92  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450486.50..2450524.92  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450486.50..2450524.92  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450486.50..2450524.92  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450486.50..2450524.92  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450486.50..2450524.92  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450486.50..2450621.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450486.50..2450621.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450486.50..2450621.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450486.50..2450621.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2450486.50..2450621.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2450524.92..2450570.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450524.92..2450570.50  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450524.92..2450570.50  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450524.92..2450570.50  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450524.92..2450570.50  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450524.92..2450570.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450570.50..2450621.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450570.50..2450621.50  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450570.50..2450621.50  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450570.50..2450621.50  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450570.50..2450621.50  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450570.50..2450621.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450621.50..2450683.38  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450621.50..2450683.38  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450621.50..2450683.38  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450621.50..2450683.38  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450621.50..2450683.38  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450621.50..2450683.38  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450621.50..2450683.38  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450621.50..2450683.38  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450621.50..2450683.38  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450621.50..2450683.38  Type 2  Earth Barycenter (3) -> Moon (301)
        2450621.50..2450683.38  Type 2  Earth Barycenter (3) -> Earth (399)
        2450683.38..2450742.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450683.38..2450742.50  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450683.38..2450742.50  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450683.38..2450742.50  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450683.38..2450742.50  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450683.38..2450742.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450683.38..2450742.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450683.38..2450742.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450683.38..2450742.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450683.38..2450742.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2450683.38..2450742.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2450742.50..2450780.12  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450742.50..2450780.12  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450742.50..2450780.12  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450742.50..2450780.12  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450742.50..2450780.12  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450742.50..2450780.12  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450742.50..2450780.12  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450742.50..2450780.12  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450742.50..2450780.12  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450742.50..2450780.12  Type 2  Earth Barycenter (3) -> Moon (301)
        2450742.50..2450780.12  Type 2  Earth Barycenter (3) -> Earth (399)
        2450780.12..2450814.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
        2450780.12..2450814.50  Type 2  Jupiter Barycenter (5) -> Io (501)
        2450780.12..2450814.50  Type 2  Jupiter Barycenter (5) -> Europa (502)
        2450780.12..2450814.50  Type 2  Jupiter Barycenter (5) -> Ganymede (503)
        2450780.12..2450814.50  Type 2  Jupiter Barycenter (5) -> Callisto (504)
        2450780.12..2450814.50  Type 2  Jupiter Barycenter (5) -> Jupiter (599)
        2450780.12..2450814.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2450780.12..2450814.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2450780.12..2450814.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2450780.12..2450814.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2450780.12..2450814.50  Type 2  Earth Barycenter (3) -> Earth (399)
        */

        // Target codes for BSP file
        targets = new HashMap<>();
        targets.put("Galileo",-77);

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.addAll(targets.keySet());

        // Use ephemeris of Solar System for Jupiter with respect to the Sun
        ephemerisSolarSystem = EphemerisSolarSystem.getInstance();

        // First valid date for ephemeris Primary tour 1995-JUL-01 to 1998-JAN-01
        firstValidDateA = new GregorianCalendar(1995,6,1, 0, 0);
        firstValidDateA.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for ephemeris GEM (Galileo Europa Mission) 1997-DEC-02 to 2000-FEB-01
        firstValidDateB = new GregorianCalendar(1997,11,2, 0, 0);
        firstValidDateB.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for ephemeris GMM (Galileo Millenium Mission) 2000-FEB-01 to 2003-SEP-22
        firstValidDateC = new GregorianCalendar(2000,1,1, 0, 0);
        firstValidDateC.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for ephemeris GMM (Galileo Millenium Mission) 2000-FEB-01 to 2003-SEP-22
        lastValidDateC = new GregorianCalendar(2003, 8, 22, 0, 0);
        lastValidDateC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get instance of EphemerisPlutoMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisGalileoJupiterBSP();
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
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris of Galileo spacecraft (Jupiter)");
        }

        // Check whether date is valid
        if (date.before(firstValidDateA) || date.after(lastValidDateC)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris of Galileo");
        }

        // Julian ephemeris date
        double julianDate = JulianDateConverter.convertCalendarToJulianDate(date);

        // Number of seconds past J2000
        double et = EphemerisUtil.computeNrSecondsPastJ2000(julianDate);

        // Target
        int target = targets.get(name);

        // Position and velocity in B1950 frame
        Vector3D positionB1950;
        Vector3D velocityB1950;

        // Initialize SPK and open file to read when needed for the first time
        int index;
        if (date.before(firstValidDateB)) {
            index = 0;
            if (spk[index] == null) {
                // Open ephemeris s980326a.bsp file to read ephemeris
                spk[index] = new SPK();
                spk[index].initWithBSPFile(BSPfilenameA);
            }
        }
        else {
            if (date.before(firstValidDateC)) {
                index = 1;
                if (spk[index] == null) {
                    // Open ephemeris file s000131a.bsp to read ephemeris
                    spk[index] = new SPK();
                    spk[index].initWithBSPFile(BSPfilenameB);
                }
            }
            else {
                index = 2;
                if (spk[index] == null) {
                    // Open ephemeris file s030916a.bsp to read ephemeris
                    spk[index] = new SPK();
                    spk[index].initWithBSPFile(BSPfilenameC);
                }
            }
        }

        // From July 1, 1995 00:00 UTC till September 13, 1995 22:59:00
        // Position and velocity for Galileo are only known with respect to the Sun
        // From November 20, 1995 00:00 UTC (JD 2450041.50) through November 21, 1995 00:00 UTC (JD 2450042.50)
        // position and velocity for Galileo is known with respect to Jupiter Barycenter (5),
        // but position and velocity of Jupiter (599) with respect to Jupiter Barycenter (5) is not known.
        if (julianDate <= 2450042.50) {
            Vector3D positionB1950sun;
            Vector3D velocityB1950sun;
            if (julianDate <= 2449974.45764) {  // September 13, 1995 22:59:00
                // 2449899.50..2449974.46  Type 1  Sun (10) -> Galileo Orbiter (-77)
                // Data for position and velocity of Jupiter is not available
                // Determine position and velocity with respect to the Sun
                Vector3D[] bodyPosVel = spk[index].getPositionVelocity(et, target, 10);
                positionB1950sun = bodyPosVel[0];
                velocityB1950sun = bodyPosVel[1];
            }
            else {
                // 2449974.45764 < julianDate <= 2450042.50
                // 2449974.46..2450042.50  Type 1  Jupiter Barycenter (5) -> Galileo Orbiter (-77)
                // 2449899.50..2450042.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
                // 2449899.50..2450042.50  Type 2  Solar System Barycenter (0) -> Sun (10)
                Vector3D[] bodyPosVel = spk[index].getPositionVelocity(et, target, 5);
                Vector3D[] jupiterBaryPosVel = spk[index].getPositionVelocity(et, 5, 0);
                Vector3D[] sunPosVel = spk[index].getPositionVelocity(et, 10, 0);
                positionB1950sun = bodyPosVel[0].plus(jupiterBaryPosVel[0]).minus(sunPosVel[0]);
                velocityB1950sun = bodyPosVel[1].plus(jupiterBaryPosVel[1]).minus(sunPosVel[1]);
            }

            // Vector3D positionJ2000sun = EphemerisUtil.transformFromB1950ToJ2000(positionB1950);
            // Vector3D velocityJ2000sun = EphemerisUtil.transformFromB1950ToJ2000(velocityB1950);
            Vector3D positionJ2000sun = EphemerisUtil.transformFromB1950ToJ2000_for_GalileoBSP(positionB1950sun);
            Vector3D velocityJ2000sun = EphemerisUtil.transformFromB1950ToJ2000_for_GalileoBSP(velocityB1950sun);
            Vector3D positionInvTransSun = EphemerisUtil.inverseTransformJ2000(positionJ2000sun);
            Vector3D velocityInvTransSun = EphemerisUtil.inverseTransformJ2000(velocityJ2000sun);

            // Position and velocity of Jupiter
            Vector3D[] jupiterPosVel = EphemerisAccurateBSP.getInstance().getBodyPositionVelocity("Jupiter", date);
            Vector3D positionJupiter = jupiterPosVel[0];
            Vector3D velocityJupiter = jupiterPosVel[1];

            // Position and velocity of body with respect to Jupiter
            Vector3D positionInvTrans = positionInvTransSun.minus(positionJupiter);
            Vector3D velocityInvTrans = velocityInvTransSun.minus(velocityJupiter);

            return new Vector3D[]{positionInvTrans, velocityInvTrans};
        }

        // With the exception of 24 hours from November 20, 1995 00:00 UTC (JD 2450041.50)
        // through November 21, 1995 00:00 UTC (JD 2450042.50) position and velocity
        // for Galileo and Jupiter are known with respect to the Jupiter Barycenter
        // from September 13, 1995 23:02:24 (JD 2449974.46) onwards
        // Jupiter Barycenter (5) is observer for Galileo (-77)
        // Jupiter Barycenter (5) is observer for Jupiter (599)
        Vector3D[] bodyPosVel = spk[index].getPositionVelocity(et, target, 5);
        Vector3D[] jupiterPosVel = spk[index].getPositionVelocity(et, 599, 5);
        positionB1950 = bodyPosVel[0].minus(jupiterPosVel[0]);
        velocityB1950 = bodyPosVel[1].minus(jupiterPosVel[1]);

        // Position of Galileo wrt Jupiter in B1950 reference frame
        if (positionB1950.magnitude() > 1000.0) {

            // Convert from reference frame B1950 to J2000
            // Vector3D positionJ2000 = EphemerisUtil.transformFromB1950ToJ2000(positionB1950);
            // Vector3D velocityJ2000 = EphemerisUtil.transformFromB1950ToJ2000(velocityB1950);
            Vector3D positionJ2000 = EphemerisUtil.transformFromB1950ToJ2000_for_GalileoBSP(positionB1950);
            Vector3D velocityJ2000 = EphemerisUtil.transformFromB1950ToJ2000_for_GalileoBSP(velocityB1950);

            // Position and velocity are computed for J2000 frame
            Vector3D positionInvTrans = EphemerisUtil.inverseTransformJ2000(positionJ2000);
            Vector3D velocityInvTrans = EphemerisUtil.inverseTransformJ2000(velocityJ2000);

            return new Vector3D[]{positionInvTrans, velocityInvTrans};
        }

        // No position and velocity found, cannot estimate position and velocity
        return new Vector3D[]{new Vector3D(), new Vector3D()};
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

