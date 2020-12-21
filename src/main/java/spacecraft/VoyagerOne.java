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
package spacecraft;

import ephemeris.CalendarUtil;
import ephemeris.SolarSystemParameters;
import solarsystem.SolarSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class VoyagerOne extends Spacecraft implements Serializable {

    /**
     * https://voyager.jpl.nasa.gov/mission/science/hyperbolic-orbital-elements/
     * EARTH INJECTION TO JUPITER Epoch = 9/8/77 09:08:17 ET
     *
     * http://www.dmuller.net/spaceflight/mission.php?mission=voyager1
     * 1977-09-05  12:56:00		Launch
     * 1977-09-11		        TCM 1 part 1
     * 1977-09-13		        TCM 1 part 2
     * 1977-10-29		        TCM 1A
     * 1977-12-10		        Enters Asteroid belt
     * 1977-12-19		        Voyager 1 overtakes Voyager 2
     * 1978-06		            TCM 2 (tbc)
     * 1978-09-08		        Exits Asteroid belt
     * 1979-01-04		        Start Jupiter observation phase
     * 1979-01-29		        TCM 3 (tbc)
     * 1979-02-21		        TCM 4
     * 1979-03-05  06:54		Amalthea flyby (distant)
     * 1979-03-05  12:05:26		Jupiter Flyby
     * 1979-03-05  15:14		Io flyby
     * 1979-03-05  18:19		Europa flyby (distant)
     * 1979-03-06  02:15		Ganymede flyby
     * 1979-03-06  17:08		Callisto flyby
     * 1979-04-09		        TCM 5
     * 1980-08-19  11:45		Ant. And Sun sensor callibrations
     * 1980-08-22		        Start Saturn observation phase
     * 1980-08-23  09:00:00		Cruise science maneuver
     * 1980-10-10  19:09:51		Trajectory Correction Maneuver TCM 8
     * 1980-11-07  03:39:58		Trajectory Correction Maneuver TCM 9
     * 1980-11-12  05:41:21		Titan flyby (targetted) (6,490 km)
     * 1980-11-12  22:16:32		Tethys flyby (distant) (415,670 km)
     * 1980-11-12  23:46:30		Saturn Flyby
     * 1980-11-13  01:43:12		Mimas flyby (88,440 km)
     * 1980-11-13  01:51:16		Enceladus flyby (distant) (202,040 km)
     * 1980-11-13  03:39:40		Dione flyby (distant) (161,520 km)
     * 1980-11-13  05:30:00		Science turns
     * 1980-11-13  06:21:53		Rhea flyby (73,980 km)
     * 1980-11-13  07:30:00		Science turns
     * 1980-11-13  16:44:41		Hyperion flyby (distant) (880,440 km)
     * 1980-11-13  21:30:00		Science turns
     * 1998-02-17		        Voyager 1 overtakes Pioneer 10 and becomes most distant spacecraft from the sun
     * 2003-05-11		        First spacecraft to reach a distance of 90AU from the Sun
     * 2004-12-17		        likely penetrated termination shock
     * 2007-02-02		        Plasma subsystem power off
     * 2007-04-11		        PLS heater off
     * 2008-01-16		        Planetary radio astronomy experiment power off
     * 2010		                Terminate scan platform and UV observations
     * 2015		                Terminate data tape recorder operations
     * 2016		                Terminate gyro operations
     * 2017-09-05  12:56:00		40 years of flight
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch September 5, 1977, 12:56:00 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(1977, 8, 5, 12, 56, 0);

    // Schedule simulated correction after orbit injection to Jupiter on September 8, 1977, 09:08:17 ET
    private static final GregorianCalendar corrEarthToJupiterInj =
            new GregorianCalendar(1977, 8, 8, 10, 0, 0);

    // Schedule simulated correction on September 14, 1977 after TCM1 on September 13, 1977
    private static final GregorianCalendar corrEarthToJupiterA =
            new GregorianCalendar(1977, 8, 14, 0, 0, 0);

    // Schedule simulated correction on January 30, 1979 after TCM3 on January 29, 1979
    private static final GregorianCalendar corrEarthToJupiterB =
            new GregorianCalendar(1979, 0, 30, 0, 0, 0);

    // Update position and velocity of the moons of Jupiter on February 21, 1979 (about two weeks before fly by)
    private static final GregorianCalendar updateJupiterMoons =
            new GregorianCalendar(1979, 1, 21, 0, 0, 0);

    // Schedule simulated correction on February 22, 1979 after TCM4 on February 21, 1979
    private static final GregorianCalendar corrEarthToJupiterC =
            new GregorianCalendar(1979, 1, 22, 0, 0, 0);

    // Jupiter fly by March 5, 1979, 12:05:26 UTC
    private static final GregorianCalendar jupiterFlyBy =
            new GregorianCalendar(1979, 2, 5, 12, 5, 26);

    // Schedule simulated correction on April 10, 1979 after TCM5 on April 9, 1979
    private static final GregorianCalendar corrJupiterToSaturnA =
            new GregorianCalendar(1979, 3, 10, 0, 0, 0);

    // Schedule simulated correction on October 11, 1980 after TCM8 on October 10, 1980
    private static final GregorianCalendar corrJupiterToSaturnB =
            new GregorianCalendar(1980, 9, 11, 0, 0, 0);

    // Update position and velocity of the moons of Saturn on October 29, 1980 (about two weeks before fly by)
    private static final GregorianCalendar updateSaturnMoons =
            new GregorianCalendar(1980, 9, 29, 0, 0, 0);

    // Schedule simulated correction on November 8, 1980 after TCM9 on November 7, 1980
    private static final GregorianCalendar corrJupiterToSaturnC =
            new GregorianCalendar(1980, 10, 8, 0, 0, 0);

    // Saturn fly by November 12, 1980, 23:46:30 UTC
    private static final GregorianCalendar saturnFlyBy =
            new GregorianCalendar(1980, 10, 12, 23, 46, 30);

    // Schedule simulated correction on December 1, 1980 after Saturn fly
    private static final GregorianCalendar corrAfterSaturnFlybyA =
            new GregorianCalendar(1980, 11, 1, 0, 0, 0);

    // Voyager 2 reached a distance of 100 A.U. from the Sun on November 7, 2012
    private static final GregorianCalendar hundredAU =
            new GregorianCalendar(2012, 10, 7);

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1977-09-08, Stop=1977-09-09, Step=1 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2443394.916666667 = A.D. 1977-Sep-08 10:00:00.0000 TDB
     *  EC= 7.977649331703427E-01 QR= 1.008073855963261E+00 IN= 1.038537596704554E+00
     *  OM= 3.432100674927835E+02 W = 3.591554098155171E+02 Tp=  2443391.437160761561
     *  N = 8.856257610384449E-02 MA= 3.081540065024374E-01 TA= 4.538854368721267E+00
     *  A = 4.984663994065690E+00 AD= 8.961254132168119E+00 PR= 4.064922406704613E+03
     */
    private static final double axisVoyagerOneInj = 4.984663994065690E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneInj = 7.977649331703427E-01; // Eccentricity [-]
    private static final double inclinationVoyagerOneInj = 1.038537596704554E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneInj = 3.591554098155171E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneInj = 3.432100674927835E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneInj = 2443391.437160761561;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneInj = 8.856257610384449E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSINJ = new double[]
            {axisVoyagerOneInj, eccentricityVoyagerOneInj, inclinationVoyagerOneInj, argPeriapsisVoyagerOneInj,
                    longNodeVoyagerOneInj, periapsisPassageVoyagerOneInj, meanMotionVoyagerOneInj};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1977-09-14, Stop=1977-09-15, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2443400.500000000 = A.D. 1977-Sep-14 00:00:00.0000 TDB
     *  EC= 7.977536822796012E-01 QR= 1.008028236518895E+00 IN= 1.036274821943479E+00
     *  OM= 3.431907770531905E+02 W = 3.591463496828569E+02 Tp=  2443391.416442248505
     *  N = 8.857597924194803E-02 MA= 8.045850228217150E-01 TA= 1.178660793093393E+01
     *  A = 4.984161135197883E+00 AD= 8.960294033876874E+00 PR= 4.064307310864143E+03
     */
    private static final double axisVoyagerOneEJA = 4.984161135197883E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneEJA = 7.977536822796012E-01; // Eccentricity [-]
    private static final double inclinationVoyagerOneEJA = 1.036274821943479E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneEJA = 3.591463496828569E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneEJA = 3.431907770531905E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneEJA = 2443391.416442248505;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneEJA = 8.857597924194803E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSEARTHJUPITERA = new double[]
            {axisVoyagerOneEJA, eccentricityVoyagerOneEJA, inclinationVoyagerOneEJA, argPeriapsisVoyagerOneEJA,
                    longNodeVoyagerOneEJA, periapsisPassageVoyagerOneEJA, meanMotionVoyagerOneEJA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1979-01-30, Stop=1979-01-31, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2443903.500000000 = A.D. 1979-Jan-30 00:00:00.0000 TDB
     *  EC= 8.085380133431345E-01 QR= 9.761230584455359E-01 IN= 1.096458434622106E+00
     *  OM= 3.405345312912726E+02 W = 1.652774014051279E+00 Tp=  2443398.859098000452
     *  N = 8.561917865097458E-02 MA= 4.320693954289691E+01 TA= 1.436765600182676E+02
     *  A = 5.098260367447891E+00 AD= 9.220397676450247E+00 PR= 4.204665422773267E+03
     */
    private static final double axisVoyagerOneEJB = 5.098260367447891E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneEJB = 8.085380133431345E-01; // Eccentricity [-]
    private static final double inclinationVoyagerOneEJB = 1.096458434622106E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneEJB = 1.652774014051279E+00; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneEJB = 3.405345312912726E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneEJB = 2443398.859098000452;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneEJB = 8.561917865097458E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSEARTHJUPITERB = new double[]
            {axisVoyagerOneEJB, eccentricityVoyagerOneEJB, inclinationVoyagerOneEJB, argPeriapsisVoyagerOneEJB,
                    longNodeVoyagerOneEJB, periapsisPassageVoyagerOneEJB, meanMotionVoyagerOneEJB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1979-02-22, Stop=1979-02-23, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2443926.500000000 = A.D. 1979-Feb-22 00:00:00.0000 TDB
     *  EC= 8.293169377810286E-01 QR= 9.076880085034813E-01 IN= 1.234217897827334E+00
     *  OM= 3.363341478512287E+02 W = 5.442070825755684E+00 Tp=  2443415.579219521955
     *  N = 8.036830625682319E-02 MA= 4.106183775842642E+01 TA= 1.452173505392331E+02
     *  A = 5.317973539395473E+00 AD= 9.728259070287463E+00 PR= 4.479377714513380E+03
     */
    private static final double axisVoyagerOneEJC = 5.317973539395473E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneEJC = 8.293169377810286E-01; // Eccentricity [-]
    private static final double inclinationVoyagerOneEJC = 1.234217897827334E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneEJC = 5.442070825755684E+00; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneEJC = 3.363341478512287E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneEJC = 2443415.579219521955;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneEJC = 8.036830625682319E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSEARTHJUPITERC = new double[]
            {axisVoyagerOneEJC, eccentricityVoyagerOneEJC, inclinationVoyagerOneEJC, argPeriapsisVoyagerOneEJC,
                    longNodeVoyagerOneEJC, periapsisPassageVoyagerOneEJC, meanMotionVoyagerOneEJC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1979-04-10, Stop=1979-04-11, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2443973.500000000 = A.D. 1979-Apr-10 00:00:00.0000 TDB
     *  EC= 2.315804544992808E+00 QR= 5.165810779630927E+00 IN= 2.486978199887935E+00
     *  OM= 1.136112336046863E+02 W = 3.585451546662424E+02 Tp=  2443834.697920749895
     *  N = 1.267019812033117E-01 MA= 1.758649843613281E+01 TA= 2.057744322002908E+01
     *  A =-3.925971223681375E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerOneJSA = -3.925971223681375E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneJSA = 2.315804544992808E+00; // Eccentricity [-]
    private static final double inclinationVoyagerOneJSA = 2.486978199887935E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneJSA = 3.585451546662424E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneJSA = 1.136112336046863E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneJSA = 2443834.697920749895;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneJSA = 1.267019812033117E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSJUPITERSATURNA = new double[]
            {axisVoyagerOneJSA, eccentricityVoyagerOneJSA, inclinationVoyagerOneJSA, argPeriapsisVoyagerOneJSA,
                    longNodeVoyagerOneJSA, periapsisPassageVoyagerOneJSA, meanMotionVoyagerOneJSA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1980-10-11, Stop=1980-10-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2444523.500000000 = A.D. 1980-Oct-11 00:00:00.0000 TDB
     *  EC= 2.259647888258706E+00 QR= 5.167158388860433E+00 IN= 2.467168247167458E+00
     *  OM= 1.133962538507676E+02 W = 3.587534038330778E+02 Tp=  2443833.538226207718
     *  N = 1.186315369551558E-01 MA= 8.185122566527085E+01 TA= 6.853628883762950E+01
     *  A =-4.102065693932400E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerOneJSB = -4.102065693932400E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneJSB = 2.259647888258706E+00; // Eccentricity [-]
    private static final double inclinationVoyagerOneJSB = 2.467168247167458E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneJSB = 3.587534038330778E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneJSB = 1.133962538507676E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneJSB = 2443833.538226207718;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneJSB = 1.186315369551558E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSJUPITERSATURNB = new double[]
            {axisVoyagerOneJSB, eccentricityVoyagerOneJSB, inclinationVoyagerOneJSB, argPeriapsisVoyagerOneJSB,
                    longNodeVoyagerOneJSB, periapsisPassageVoyagerOneJSB, meanMotionVoyagerOneJSB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1980-11-08, Stop=1980-11-09, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2444551.500000000 = A.D. 1980-Nov-08 00:00:00.0000 TDB
     *  EC= 2.313895976534841E+00 QR= 5.135807812893664E+00 IN= 2.478393397445636E+00
     *  OM= 1.140431834587610E+02 W = 3.581014928902181E+02 Tp=  2443842.493303401396
     *  N = 1.275358856935902E-01 MA= 9.042379701339175E+01 TA= 6.984083921490303E+01
     *  A =-3.908838983157871E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerOneJSC = -3.908838983157871E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneJSC = 2.313895976534841E+00; // Eccentricity [-]
    private static final double inclinationVoyagerOneJSC = 2.478393397445636E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneJSC = 3.581014928902181E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneJSC = 1.140431834587610E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneJSC = 2443842.493303401396;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneJSC = 1.275358856935902E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSJUPITERSATURNC = new double[]
            {axisVoyagerOneJSC, eccentricityVoyagerOneJSC, inclinationVoyagerOneJSC, argPeriapsisVoyagerOneJSC,
                    longNodeVoyagerOneJSC, periapsisPassageVoyagerOneJSC, meanMotionVoyagerOneJSC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 1 (spacecraft) [-31]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1980-12-01, Stop=1980-12-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2444574.500000000 = A.D. 1980-Dec-01 00:00:00.0000 TDB
     *  EC= 3.779309074990923E+00 QR= 8.777289217204046E+00 IN= 3.582061662216526E+01
     *  OM= 1.789892689670488E+02 W = 3.383183914954462E+02 Tp=  2444229.521408423781
     *  N = 1.756178794437604E-01 MA= 6.058440870610889E+01 TA= 2.690765978451616E+01
     *  A =-3.158083171168252E+00 AD= 9.999999999999998E+99 PR= 9.999999999999998E+99
     */
    private static final double axisVoyagerOneASA = -3.158083171168252E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerOneASA = 3.779309074990923E+00; // Eccentricity [-]
    private static final double inclinationVoyagerOneASA = 3.582061662216526E+01; // Inclination [degrees]
    private static final double argPeriapsisVoyagerOneASA = 3.383183914954462E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerOneASA = 1.789892689670488E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerOneASA = 2444229.521408423781;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerOneASA = 1.756178794437604E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSAFTERSATURNFLYBYA = new double[]
            {axisVoyagerOneASA, eccentricityVoyagerOneASA, inclinationVoyagerOneASA, argPeriapsisVoyagerOneASA,
                    longNodeVoyagerOneASA, periapsisPassageVoyagerOneASA, meanMotionVoyagerOneASA};

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public VoyagerOne(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {

        List<SpacecraftTrajectory> trajectories = new ArrayList<>();

        // From Earth to Jupiter
        trajectories.add(
                new SpacecraftTrajectory(launch, corrEarthToJupiterInj, "Sun", ORBITPARSINJ));
        trajectories.add(
                new SpacecraftTrajectory(corrEarthToJupiterInj, corrEarthToJupiterA, "Sun", ORBITPARSINJ));
        trajectories.add(
                new SpacecraftTrajectory(corrEarthToJupiterA, corrEarthToJupiterB, "Sun", ORBITPARSEARTHJUPITERA));
        trajectories.add(
                new SpacecraftTrajectory(corrEarthToJupiterB, corrEarthToJupiterC, "Sun", ORBITPARSEARTHJUPITERB));
        trajectories.add(
                new SpacecraftTrajectory(corrEarthToJupiterC, jupiterFlyBy, "Sun", ORBITPARSEARTHJUPITERC));

        // From Jupiter to Saturn
        trajectories.add(
                new SpacecraftTrajectory(jupiterFlyBy, corrJupiterToSaturnA, "Sun", ORBITPARSEARTHJUPITERC));
        trajectories.add(
                new SpacecraftTrajectory(corrJupiterToSaturnA, corrJupiterToSaturnB, "Sun", ORBITPARSJUPITERSATURNA));
        trajectories.add(
                new SpacecraftTrajectory(corrJupiterToSaturnB, corrJupiterToSaturnC, "Sun", ORBITPARSJUPITERSATURNB));
        trajectories.add(
                new SpacecraftTrajectory(corrJupiterToSaturnC, saturnFlyBy, "Sun", ORBITPARSJUPITERSATURNC));

        // From Saturn till Voyager 2 reaches 100 A.U. from the Sun
        trajectories.add(
                new SpacecraftTrajectory(saturnFlyBy, corrAfterSaturnFlybyA, "Sun", ORBITPARSJUPITERSATURNC));
        trajectories.add(
                new SpacecraftTrajectory(corrAfterSaturnFlybyA, hundredAU, "Sun", ORBITPARSAFTERSATURNFLYBYA));
        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        List<String> jupiterMoons = SolarSystemParameters.getInstance().getMoonsOfPlanet("Jupiter");
        List<String> saturnMoons = SolarSystemParameters.getInstance().getMoonsOfPlanet("Saturn");
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterInj)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(updateJupiterMoons), jupiterMoons));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrJupiterToSaturnA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrJupiterToSaturnB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(updateSaturnMoons), saturnMoons));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrJupiterToSaturnC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrAfterSaturnFlybyA)));
    }
}