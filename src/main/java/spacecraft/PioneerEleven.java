/*
 * Copyright (c) 2021 Nico Kuijpers
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
package spacecraft;

import ephemeris.CalendarUtil;
import ephemeris.SolarSystemParameters;
import solarsystem.SolarSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class PioneerEleven extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/Pioneer_11
     * Pioneer 11 was launched by NASA on April 6, 1973, to study the asteroid belt, the environment
     * around Jupiter and Saturn, solar winds, and cosmic rays. It was the first probe to encounter
     * Saturn, the second to fly through the asteroid belt, and the second to fly by Jupiter.
     *
     * Launch and trajectory:
     * Launch April 6, 1973 at 02:11:00 UTC.
     * Pioneer 11 was launched on a trajectory directly aimed at Jupiter without any prior gravitational
     * assists. In May 1974, Pioneer was retargeted to fly past Jupiter on a north–south trajectory enabling
     * a Saturn flyby in 1979. The maneuver used 17 pounds of propellant, lasted 42 minutes and 36 seconds
     * and increased Pioneer 11's speed by 230 km/h. It also made two mid-course corrections,
     * on April 11, 1973 and November 7, 1974.
     *
     * Jupiter observation phase:
     * 1974-12-02 Encounter with Jovian system.
     * 08:21:00 Callisto flyby at 786,500 km.
     * 22:09:00 Ganymede flyby at 692,300 km.
     * 1974-12-03
     * 03:11:00 Io flyby at 314,000 km.
     * 04:15:00 Europa flyby at 586,700 km.
     * 05:00:21 Jupiter shadow entry.
     * 05:01:01 Jupiter occultation entry.
     * 05:21:19 Jupiter closest approach at 42,828 km.
     * 05:33:52 Jupiter shadow exit.
     * 05:43:03 Jupiter occultation exit.
     * 22:29:00 Amalthea flyby at 127,500 km.
     * 1975-01-01 Phase stop.
     *
     * Saturn observation phase:
     * 1979-08-29 Encounter with Saturnian system.
     * 06:06:10 Iapetus flyby at 1,032,535 km.
     * 11:53:33 Phoebe flyby at 13,713,574 km.
     * 1979-08-31
     * 12:32:33 Hyperion flyby at 666,153 km.
     * 1979-09-01
     * 14:26:56 Descending ring plane crossing.
     * 14:50:55 Epimetheus flyby at 6,676 km.
     * 15:06:32 Atlas flyby at 45,960 km.
     * 15:59:30 Dione flyby at 291,556 km.
     * 16:26:28 Mimas flyby at 104,263 km.
     * 16:29:34 Saturn closest approach at 20,591 km.
     * 16:35:00 Saturn occultation entry.
     * 16:35:57 Saturn shadow entry.
     * 16:51:11 Janus flyby at 228,988 km.
     * 17:53:32 Saturn occultation exit.
     * 17:54:47 Saturn shadow exit.
     * 18:21:59 Ascending ring plane crossing.
     * 18:25:34 Tethys flyby at 329,197 km.
     * 18:30:14 Enceladus flyby at 222,027 km.
     * 20:04:13 Calypso flyby at 109,916 km.
     * 22:15:27 Rhea flyby at 345,303 km.
     * 1979-09-02
     * 18:00:33 Titan flyby at 362,962 km.
     * 1979-10-05 Phase stop.
     *
     * https://solarsystem.nasa.gov/missions/pioneer-11/in-depth/
     * Pioneer 11 passed through the asteroid belt without damage by mid-March 1974. Soon, on April 26, 1974,
     * it performed a midcourse correction (after an earlier one on April 11, 1973) to guide it much closer
     * to Jupiter than Pioneer 10 and to ensure a polar flyby.
     *
     * Pioneer 11 penetrated the Jovian bow shock on Nov. 25, 1974, at 03:39 UT. The spacecraft’s closest approach
     * to Jupiter occurred at 05:22 UT on Dec. 3, 1974, at a range of about 26,400 miles (42,500 kilometers) from
     * the planet’s cloud tops, three times closer than Pioneer 10. By this time, it was traveling faster than
     * any human-made object at the time, more than 106,000 miles per hour (171,000 kilometers per hour).
     *
     * Because of its high speed during the encounter, the spacecraft’s exposure to Jupiter’s radiation belts
     * spanned a shorter time than its predecessor although it was actually closer to the planet.
     *
     * Pioneer 11 repeatedly crossed Jupiter’s bow shock, indicating that the Jovian magnetosphere changes its
     * boundaries as it is buffeted by the solar wind. Besides the many images of the planet (and better pictures
     * of the Great Red Spot), Pioneer 11 took about 200 images of the moons of Jupiter. The vehicle then used
     * Jupiter’s massive gravitational field to swing back across the solar system to set it on a course to Saturn.
     *
     * After its Jupiter encounter, on April 16, 1975, the micrometeoroid detector was turned off since it was
     * issuing spurious commands which were interfering with other instruments. Course corrections on May 26, 1976,
     * and July 13, 1978, sharpened its trajectory towards Saturn.
     *
     * Pioneer 11 detected Saturn’s bow shock on Aug. 31, 1979, about 932,000 miles (1.5 million kilometers) out
     * from the planet, thus providing the first conclusive evidence of the existence of Saturn’s magnetic field.
     *
     * The spacecraft crossed the planet’s ring plane beyond the outer ring at 14:36 UT Sept. 1, 1979, and then
     * passed by the planet at 16:31 UT for a close encounter at a range of about 13,000 miles (20,900 kilometers).
     * It was moving at a relative velocity of about 71,000 miles per hour (114,000 kilometers per hour) at
     * the point of closest approach.
     *
     * https://solarsystem.nasa.gov/missions/pioneer-11/in-depth/
     * Key Dates
     * April 6, 1973: Launch
     * Mid-March, 1974: Flew through the asteroid belt
     * Dec. 3, 1974: Jupiter flyby
     * Sept. 1, 1979: Saturn flyby
     * Feb. 23, 1990: Crossed the orbit of Neptune
     * Sept. 30, 1995: NASA Ames Research Center made last contact with the spacecraft
     * Nov. 24, 1995: Scientists received last engineering data from Pioneer 11
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch April 6, 1973 at 02:11:00 UTC.
    private static final GregorianCalendar launch =
            new GregorianCalendar(1973, 3, 6, 2, 11, 0);

    // Schedule simulated correction on April 7, 1973, 0:00 UTC after launch on April 6, 1973 at 02:11
    private static final GregorianCalendar correctionLaunch =
            new GregorianCalendar(1973, 3, 7, 0, 0, 0);

    // Schedule simulated correction on April 12, 1973, 0:00 UTC after midcourse correction on April 11, 1973
    private static final GregorianCalendar correctionJupA =
            new GregorianCalendar(1973, 3, 12, 0, 0, 0);

    // Schedule simulated correction on April 27, 1974, 0:00 UTC after midcourse correction on April 26, 1974
    private static final GregorianCalendar correctionJupB =
            new GregorianCalendar(1974, 3, 27, 0, 0, 0);

    // Update position and velocity of the moons of Jupiter on November 19, 1974 (about two weeks before fly by)
    private static final GregorianCalendar updateJupiterMoons =
            new GregorianCalendar(1974, 10, 19, 0, 0, 0);

    // Schedule simulated correction on November 20, 1974, 0:00 UTC (less than two weeks before fly by)
    private static final GregorianCalendar correctionJupC =
            new GregorianCalendar(1974, 10, 20, 0, 0, 0);

    // Jupiter fly by December 3, 1974, 05:21:19 UTC
    private static final GregorianCalendar jupiterFlyBy =
            new GregorianCalendar(1974, 11, 3, 5, 21, 19);

    // Schedule simulated correction on January 2, 1975, 0:00 UTC after Jupiter observation phase
    private static final GregorianCalendar correctionSatA =
            new GregorianCalendar(1975, 0, 2, 0, 0, 0);

    // Schedule simulated correction on May 27, 1976, 0:00 UTC after course correction on May 26, 1976
    private static final GregorianCalendar correctionSatB =
            new GregorianCalendar(1976, 4, 27, 0, 0, 0);

    // Schedule simulated correction on July 14, 1978, 0:00 UTC after course correction on July 13, 1978
    private static final GregorianCalendar correctionSatC =
            new GregorianCalendar(1978, 6, 14, 0, 0, 0);

    // Update position and velocity of the moons of Saturn on August 16, 1979 (about two weeks before fly by)
    private static final GregorianCalendar updateSaturnMoons =
            new GregorianCalendar(1979, 7, 16, 0, 0, 0);

    // Schedule simulated correction on August 17, 1979, 0:00 UTC (less than two weeks before fly by)
    private static final GregorianCalendar correctionSatD =
            new GregorianCalendar(1979, 7, 17, 0, 0, 0);

    // Saturn fly by September 1, 1979, 16:29:34 UTC
    private static final GregorianCalendar saturnFlyBy =
            new GregorianCalendar(1979, 8, 1, 16, 29, 34);

    // Schedule simulated correction on October 6, 1979, 0:00 UTC after Saturn observation phase
    private static final GregorianCalendar correctionAfterSaturn =
            new GregorianCalendar(1979, 9, 6, 0, 0, 0);

    // Last engineering data received from Pioneer 11 on November 24, 1995
    private static final GregorianCalendar deactivated =
            new GregorianCalendar(1995, 10, 24, 0, 0, 0);


    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1973-04-07, Stop=1973-04-08, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2441779.500000000 = A.D. 1973-Apr-07 00:00:00.0000 TDB
     *  EC= 7.165968614227921E-01 QR= 1.000852121561598E+00 IN= 3.044895505746199E+00
     *  OM= 1.649559413364747E+01 W = 1.791822519390500E+02 Tp=  2441777.935359611642
     *  N = 1.485101668787099E-01 MA= 2.323650051882723E-01 TA= 2.017543733567565E+00
     *  A = 3.531549179681851E+00 AD= 6.062246237802104E+00 PR= 2.424076462684312E+03
     */
    private static final double axisPioneerElevenLaunch = 3.531549179681851E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenLaunch = 7.165968614227921E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenLaunch = 3.044895505746199E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenLaunch = 1.791822519390500E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenLaunch = 1.649559413364747E+01; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenLaunch = 2441777.935359611642;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenLaunch = 1.485101668787099E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRLAUNCH = new double[]
            {axisPioneerElevenLaunch, eccentricityPioneerElevenLaunch, inclinationPioneerElevenLaunch,
                    argPeriapsisPioneerElevenLaunch, longNodePioneerElevenLaunch, periapsisPassagePioneerElevenLaunch,
                    meanMotionPioneerElevenLaunch};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1973-04-12, Stop=1973-04-13, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2441784.500000000 = A.D. 1973-Apr-12 00:00:00.0000 TDB
     *  EC= 7.148359132468077E-01 QR= 1.000789984781653E+00 IN= 3.061637688598987E+00
     *  OM= 1.654858713793612E+01 W = 1.790665215598436E+02 Tp=  2441777.887811570428
     *  N = 1.499104449198571E-01 MA= 9.912361093812513E-01 TA= 8.497998735376916E+00
     *  A = 3.509523222844788E+00 AD= 6.018256460907923E+00 PR= 2.401433737271995E+03
     */
    private static final double axisPioneerElevenJupA = 3.509523222844788E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenJupA = 7.148359132468077E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenJupA = 3.061637688598987E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenJupA = 1.790665215598436E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenJupA = 1.654858713793612E+01; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenJupA = 2441777.887811570428;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenJupA = 1.499104449198571E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRJUPA = new double[]
            {axisPioneerElevenJupA, eccentricityPioneerElevenJupA, inclinationPioneerElevenJupA,
                    argPeriapsisPioneerElevenJupA, longNodePioneerElevenJupA,
                    periapsisPassagePioneerElevenJupA, meanMotionPioneerElevenJupA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1974-04-27, Stop=1974-04-28, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2442164.500000000 = A.D. 1974-Apr-27 00:00:00.0000 TDB
     *  EC= 7.167301147624041E-01 QR= 1.000291514331764E+00 IN= 3.067401163067526E+00
     *  OM= 1.636865605464599E+01 W = 1.795200687074992E+02 Tp=  2441779.438625193201
     *  N = 1.485302140356604E-01 MA= 5.719324841693451E+01 TA= 1.410212499072576E+02
     *  A = 3.531231403199666E+00 AD= 6.062171292067568E+00 PR= 2.423749284529868E+03
     */
    private static final double axisPioneerElevenJupB = 3.531231403199666E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenJupB = 7.167301147624041E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenJupB = 3.067401163067526E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenJupB = 1.795200687074992E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenJupB = 1.636865605464599E+01; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenJupB = 2441779.438625193201;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenJupB = 1.485302140356604E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRJUPB = new double[]
            {axisPioneerElevenJupB, eccentricityPioneerElevenJupB, inclinationPioneerElevenJupB,
                    argPeriapsisPioneerElevenJupB, longNodePioneerElevenJupB,
                    periapsisPassagePioneerElevenJupB, meanMotionPioneerElevenJupB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1974-11-20, Stop=1974-11-21, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2442371.500000000 = A.D. 1974-Nov-20 00:00:00.0000 TDB
     *  EC= 7.633509137563081E-01 QR= 8.431563378514454E-01 IN= 3.722737736456684E+00
     *  OM= 1.131580469586333E+01 W = 1.834282403991153E+02 Tp=  2441815.088392942678
     *  N = 1.465545001047631E-01 MA= 8.154462492476961E+01 TA= 1.559188768538987E+02
     *  A = 3.562897077841225E+00 AD= 6.282637817831005E+00 PR= 2.456424058917723E+03
     */
    private static final double axisPioneerElevenJupC = 3.562897077841225E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenJupC = 7.633509137563081E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenJupC = 3.722737736456684E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenJupC = 1.834282403991153E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenJupC = 1.131580469586333E+01; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenJupC = 2441815.088392942678;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenJupC = 1.465545001047631E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRJUPC = new double[]
            {axisPioneerElevenJupC, eccentricityPioneerElevenJupC, inclinationPioneerElevenJupC,
                    argPeriapsisPioneerElevenJupC, longNodePioneerElevenJupC,
                    periapsisPassagePioneerElevenJupC, meanMotionPioneerElevenJupC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1975-01-02, Stop=1975-01-03, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2442414.500000000 = A.D. 1975-Jan-02 00:00:00.0000 TDB
     *  EC= 8.283927926090304E-01 QR= 3.710174891974352E+00 IN= 1.594850113510878E+01
     *  OM= 3.551192952251519E+02 W = 5.952241332091318E+01 Tp=  2442803.224639895372
     *  N = 9.804281828598976E-03 MA= 3.561888340767441E+02 TA= 2.996478169917880E+02
     *  A = 2.162015773336092E+01 AD= 3.953014057474748E+01 PR= 3.671865071747368E+04
     */
    private static final double axisPioneerElevenSatA = 2.162015773336092E+01; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenSatA = 8.283927926090304E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenSatA = 1.594850113510878E+01; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenSatA = 5.952241332091318E+01; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenSatA = 3.551192952251519E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenSatA = 2442803.224639895372;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenSatA = 9.804281828598976E-03; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRSATA = new double[]
            {axisPioneerElevenSatA, eccentricityPioneerElevenSatA, inclinationPioneerElevenSatA,
                    argPeriapsisPioneerElevenSatA, longNodePioneerElevenSatA,
                    periapsisPassagePioneerElevenSatA, meanMotionPioneerElevenSatA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1976-05-27, Stop=1976-05-28, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2442925.500000000 = A.D. 1976-May-27 00:00:00.0000 TDB
     *  EC= 7.767125402393182E-01 QR= 3.732780552810668E+00 IN= 1.528310666480358E+01
     *  OM= 3.547326108932828E+02 W = 6.013080898255838E+01 Tp=  2442811.149083401542
     *  N = 1.441955590276679E-02 MA= 1.648889434421438E+00 TA= 2.044418315426359E+01
     *  A = 1.671737659074736E+01 AD= 2.970197262868405E+01 PR= 2.496609482480136E+04
     */
    private static final double axisPioneerElevenSatB = 1.671737659074736E+01; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenSatB = 7.767125402393182E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenSatB = 1.528310666480358E+01; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenSatB = 6.013080898255838E+01; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenSatB = 3.547326108932828E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenSatB = 2442811.149083401542;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenSatB = 1.441955590276679E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRSATB = new double[]
            {axisPioneerElevenSatB, eccentricityPioneerElevenSatB, inclinationPioneerElevenSatB,
                    argPeriapsisPioneerElevenSatB, longNodePioneerElevenSatB,
                    periapsisPassagePioneerElevenSatB, meanMotionPioneerElevenSatB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1978-07-14, Stop=1978-07-15, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2443703.500000000 = A.D. 1978-Jul-14 00:00:00.0000 TDB
     *  EC= 7.768471461630478E-01 QR= 3.729664676263016E+00 IN= 1.529829655856134E+01
     *  OM= 3.546723467074987E+02 W = 6.019116573834166E+01 Tp=  2442812.041726432741
     *  N = 1.442457615049656E-02 MA= 1.285890775205970E+01 TA= 9.676435560048468E+01
     *  A = 1.671349755171903E+01 AD= 2.969733042717505E+01 PR= 2.495740576665798E+04
     */
    private static final double axisPioneerElevenSatC = 1.671349755171903E+01; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenSatC = 7.768471461630478E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenSatC = 1.529829655856134E+01; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenSatC = 6.019116573834166E+01; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenSatC = 3.546723467074987E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenSatC = 2442812.041726432741;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenSatC = 1.442457615049656E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRSATC = new double[]
            {axisPioneerElevenSatC, eccentricityPioneerElevenSatC, inclinationPioneerElevenSatC,
                    argPeriapsisPioneerElevenSatC, longNodePioneerElevenSatC,
                    periapsisPassagePioneerElevenSatC, meanMotionPioneerElevenSatC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1979-08-17, Stop=1979-08-18, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2444102.500000000 = A.D. 1979-Aug-17 00:00:00.0000 TDB
     *  EC= 8.001788790935739E-01 QR= 3.650113751413104E+00 IN= 1.602663400421248E+01
     *  OM= 3.542368231013274E+02 W = 6.076800972059898E+01 Tp=  2442839.358245376032
     *  N = 1.262427835340719E-02 MA= 1.594625311018443E+01 TA= 1.114984882916883E+02
     *  A = 1.826690659553657E+01 AD= 3.288369943966003E+01 PR= 2.851648149082825E+04
     */
    private static final double axisPioneerElevenSatD = 1.826690659553657E+01; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenSatD = 8.001788790935739E-01; // Eccentricity [-]
    private static final double inclinationPioneerElevenSatD = 1.602663400421248E+01; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenSatD = 6.076800972059898E+01; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenSatD = 3.542368231013274E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenSatD = 2442839.358245376032;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenSatD = 1.262427835340719E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRSATD = new double[]
            {axisPioneerElevenSatD, eccentricityPioneerElevenSatD, inclinationPioneerElevenSatD,
                    argPeriapsisPioneerElevenSatD, longNodePioneerElevenSatD,
                    periapsisPassagePioneerElevenSatD, meanMotionPioneerElevenSatD};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 11 (spacecraft) [-24]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1979-10-06, Stop=1979-10-07, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2444152.500000000 = A.D. 1979-Oct-06 00:00:00.0000 TDB
     *  EC= 2.219810084768459E+00 QR= 9.351843470735702E+00 IN= 1.678459771734419E+01
     *  OM= 1.605387019820623E+02 W = 1.265096655816868E+01 Tp=  2444209.031807411462
     *  N = 4.642977292055878E-02 MA=-2.624758980895157E+00 TA= 3.565070280559686E+02
     *  A =-7.666638919869921E+00 AD= 9.999999999999998E+99 PR= 9.999999999999998E+99
     */
    private static final double axisPioneerElevenFinal = -7.666638919869921E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerElevenFinal = 2.219810084768459E+00; // Eccentricity [-]
    private static final double inclinationPioneerElevenFinal = 1.678459771734419E+01; // Inclination [degrees]
    private static final double argPeriapsisPioneerElevenFinal = 1.265096655816868E+01; // Arg perifocus [degrees]
    private static final double longNodePioneerElevenFinal = 1.605387019820623E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerElevenFinal = 2444209.031807411462;  // Time of periapsis [JD]
    private static final double meanMotionPioneerElevenFinal = 4.642977292055878E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRFINAL = new double[]
            {axisPioneerElevenFinal, eccentricityPioneerElevenFinal, inclinationPioneerElevenFinal,
                    argPeriapsisPioneerElevenFinal, longNodePioneerElevenFinal,
                    periapsisPassagePioneerElevenFinal, meanMotionPioneerElevenFinal};


    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public PioneerEleven(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        trajectories.add(
                new SpacecraftTrajectory(launch, correctionLaunch, "Sun", ORBITPARSCORRLAUNCH));
        trajectories.add(
                new SpacecraftTrajectory(correctionLaunch, correctionJupA, "Sun", ORBITPARSCORRLAUNCH));
        trajectories.add(
                new SpacecraftTrajectory(correctionJupA, correctionJupB, "Sun", ORBITPARSCORRJUPA));
        trajectories.add(
                new SpacecraftTrajectory(correctionJupB, correctionJupC, "Sun", ORBITPARSCORRJUPB));
        trajectories.add(
                new SpacecraftTrajectory(correctionJupC, jupiterFlyBy, "Sun", ORBITPARSCORRJUPC));
        trajectories.add(
                new SpacecraftTrajectory(jupiterFlyBy, correctionSatA, "Sun", ORBITPARSCORRSATA));
        trajectories.add(
                new SpacecraftTrajectory(correctionSatA, correctionSatB, "Sun", ORBITPARSCORRSATA));
        trajectories.add(
                new SpacecraftTrajectory(correctionSatB, correctionSatC, "Sun", ORBITPARSCORRSATB));
        trajectories.add(
                new SpacecraftTrajectory(correctionSatC, correctionSatD, "Sun", ORBITPARSCORRSATC));
        trajectories.add(
                new SpacecraftTrajectory(correctionSatD, saturnFlyBy, "Sun", ORBITPARSCORRSATD));
        trajectories.add(
                new SpacecraftTrajectory(saturnFlyBy, correctionAfterSaturn, "Sun", ORBITPARSCORRFINAL));
        trajectories.add(
                new SpacecraftTrajectory(correctionAfterSaturn, deactivated, "Sun", ORBITPARSCORRFINAL));
        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        List<String> jupiterMoons = SolarSystemParameters.getInstance().getMoonsOfPlanet("Jupiter");
        List<String> saturnMoons = SolarSystemParameters.getInstance().getMoonsOfPlanet("Saturn");
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionLaunch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionJupA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionJupB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(updateJupiterMoons), jupiterMoons));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionJupC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionSatA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionSatB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionSatC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionSatD)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(updateSaturnMoons), saturnMoons));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionAfterSaturn)));
    }
}