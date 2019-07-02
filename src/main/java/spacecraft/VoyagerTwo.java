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
import solarsystem.SolarSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class VoyagerTwo extends Spacecraft implements Serializable {

    /**
     * https://voyager.jpl.nasa.gov/mission/science/hyperbolic-orbital-elements/
     * EARTH INJECTION TO JUPITER Epoch = 8/23/77 11:29:11 ET
     *
     * http://www.dmuller.net/spaceflight/mission.php?mission=voyager2
     * 1977-08-20  14:29:00		Launch
     * 1977-10-11		        TCM 1
     * 1977-12-10		        Enters Asteroid belt
     * 1977-12-19		        Voyager 1 overtakes Voyager 2
     * 1978-05-03		        TCM 2
     * 1978-06		            Primary radIo receiver fails. Remainder of mission flown on using its backup
     * 1978-10-21		        Exits Asteroid belt
     * 1979-04-24		        Start Jupiter observation phase
     * 1979-06-27		        TCM?
     * 1979-07-08  12:21		Callisto flyby (distant) (214,930 km)
     * 1979-07-09  07:14		Ganymede flyby (62,130 km)
     * 1979-07-09		        TCM?
     * 1979-07-09  17:53		Europa flyby (distant) (205,720 km)
     * 1979-07-09  20:01		Amalthea flyby (distant) (558,370 km)
     * 1979-07-09  22:29		Jupiter flyby (721,883 km)
     * 1979-07-09  23:17		Io flyby (distant) (1,129,900 km)
     * 1979-07-23		        TCM?
     * 1980		                TCM B6?
     * 1981-02-26		        TCM B7
     * 1981-06-05		        Start Saturn observation phase
     * 1981-07-19  11:16:25		Trajectory Correction Maneuver TCM 8
     * 1981-08-01  00:00:00		Vertical system scan
     * 1981-08-13  08:00:00		Roll to Procyon
     * 1981-08-15  09:30:00		Roll to Canopus
     * 1981-08-18  21:26:16		Trajectory Correction Maneuver TCM 9
     * 1981-08-23  01:26:57		Iapetus 909,070km
     * 1981-08-24  07:50:00		Roll to Miaplacidus
     * 1981-08-25  01:25:26		Hyperion 470,840km
     * 1981-08-25  09:37:46		Titan flyby (distant) (665,960 km)
     * 1981-08-25  12:40:00		Science turns
     * 1981-08-25  22:57:33		Helene (1980 S6) 318,200km
     * 1981-08-26  01:04:32		Dione flyby (distant) (502,250 km)
     * 1981-08-26  02:22:17		Calypso (1980 S25) 153,518km
     * 1981-08-26  02:24:26		Mimas flyby (distant) (309,990 km)
     * 1981-08-26  02:39:11		Science turns
     * 1981-08-26  02:47:31		Science turns
     * 1981-08-26  03:08:29		Prometheus (1980 S28) 287,170km
     * 1981-08-26  03:08:47		Science turns
     * 1981-08-26  03:19:18		Pandora (1980 S26) 107,000km
     * 1981-08-26  03:24:05		Saturn Flyby
     * 1981-08-26  03:26:06		Science turns
     * 1981-08-26  03:33:02		Atlas (1980 S27) 246,590km
     * 1981-08-26  03:45:16		Enceladus flyby (87,140 km)
     * 1981-08-26  03:50:04		Janus (1980 S1) 222,760km
     * 1981-08-26  04:05:56		Epimetheus (1980 S3) 147,010km
     * 1981-08-26  05:25:39		Science turns
     * 1981-08-26  05:41:53		Science turns
     * 1981-08-26  06:02:47		Telesto (1980 S13) 284,400km
     * 1981-08-26  06:12:30		Tethys flyby (93,000 km)
     * 1981-08-26  06:28:48		Rhea flyby (distant) (645,280 km)
     * 1981-08-26  07:38:00		Science turns
     * 1981-08-26  08:09:10		Roll to Vega
     * 1981-09-04  01:46:00		Roll to Canpous
     * 1981-09-05  01:22:34		Phoebe 2,073,640km
     * 1981-09-05  03:10:00		Roll to Miaplacidus
     * 1981-09-09  22:30:00		Scale factor test
     * 1986-01-24  17:59:47		Uranus Flyby
     * 1986-02-14		        2.5 hrs trajectory correction maneuvre targets Voyager 2 towards Neptune
     * 1989-08-25  03:56:36		Neptune Flyby
     * 1989-08-25		        Triton flyby (39,800 km)
     * 1998-11-13		        Terminate scan platform and UV observations
     * 2007-09-06		        Terminate data tape recorder operations
     * 2008		                Termination shock area (to be edited)
     * 2008-02-22		        Planetary radio astronomy experiment power off
     * 2015		                Terminate gyro operations
     * 2017-08-20  14:29:00		40 years of flight
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch August 20, 1977, 14:29:00 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(1977, 7, 20, 14, 29, 0);

    // Schedule simulated correction after orbit injection to Jupiter on August 23, 1977, 11:29:11 ET
    private static final GregorianCalendar corrEarthToJupiterInj =
            new GregorianCalendar(1977, 7, 23, 11, 29, 11);

    // Schedule simulated correction on October 12, 1977 after TCM1 on October 11, 1977
    private static final GregorianCalendar corrEarthToJupiterA =
            new GregorianCalendar(1977, 9, 12, 0, 0, 0);

    // Schedule simulated correction on May 4, 1978 after TCM2 on May 3, 1978
    private static final GregorianCalendar corrEarthToJupiterB =
            new GregorianCalendar(1978, 4, 4, 0, 0, 0);

    // Schedule simulated correction on June 28, 1979 after TCM? on June 27, 1979
    private static final GregorianCalendar corrEarthToJupiterC =
            new GregorianCalendar(1979, 5, 28, 0, 0, 0);

    // Jupiter fly by July 9, 1979, 22:29:00 UTC (distance 570,000 km)
    private static final GregorianCalendar jupiterFlyBy =
            new GregorianCalendar(1979, 6, 9, 22, 29, 0);

    // Schedule simulated correction on July 24, 1979 after TCM? on July 23, 1979
    private static final GregorianCalendar corrJupiterToSaturnA =
            new GregorianCalendar(1979, 6, 24, 0, 0, 0);

    // Schedule simulated correction on July 20, 1981 after TCM8 on July 19, 1981
    private static final GregorianCalendar corrJupiterToSaturnB =
            new GregorianCalendar(1981, 6, 20, 0, 0, 0);

    // Schedule simulated correction on August 19, 1981 after TCM9 on August 18, 1981
    private static final GregorianCalendar corrJupiterToSaturnC =
            new GregorianCalendar(1981, 7, 19, 0, 0, 0);

    // Saturn fly by August 25, 1981, 03:24:05 UTC (distance 101,000 km)
    private static final GregorianCalendar saturnFlyBy =
            new GregorianCalendar(1981, 7, 25, 3, 24, 5);

    // Schedule simulated correction on September 1, 1981 after Saturn flyby
    // Note that no time/dates for TCM's were found between Saturn and Uranus
    private static final GregorianCalendar corrSaturnToUranusA =
            new GregorianCalendar(1981, 8, 1, 0, 0, 0);

    // Schedule simulated correction on January 1, 1986 prior to Uranus flyby
    // Note that no time/dates for TCM's were found between Saturn and Uranus
    private static final GregorianCalendar corrSaturnToUranusB =
            new GregorianCalendar(1986, 0, 1, 0, 0, 0);

    // Uranus fly by January 24, 1986, 17:59:47 UTC (distance 81,500 km)
    private static final GregorianCalendar uranusFlyBy =
            new GregorianCalendar(1986, 0, 24, 17, 59, 47);

    // Schedule simulated correction on February 15, 1986 after 2.5 hours TCM on February 14, 1986
    private static final GregorianCalendar corrUranusToNeptuneA =
            new GregorianCalendar(1986, 1, 15, 0, 0, 0);

    // Schedule simulated correction on August 1, 1989 prior to Neptune flyby
    // Note that no time/dates for TCM's were found between Uranus and Neptune after the 2.5 hours TCM
    private static final GregorianCalendar corrUranusToNeptuneB =
            new GregorianCalendar(1989, 7, 1, 0, 0, 0);

    // Neptune fly by August 25, 1989, 03:56:36 UTC (distance 4,951 km)
    private static final GregorianCalendar neptuneFlyBy =
            new GregorianCalendar(1989, 7, 25, 3, 56, 36);

    // Voyager 2 reached a distance of 100 A.U. from the Sun on November 7, 2012
    private static final GregorianCalendar hundredAU =
            new GregorianCalendar(2012, 10, 7);

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1977-08-23, Stop=1977-08-24, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     *  2443378.979166667 = A.D. 1977-Aug-23 11:30:00.0000 TDB
     *  EC= 7.244289223444542E-01 QR= 1.002956402691350E+00 IN= 4.831487505958364E+00
     *  OM= 3.277939445108216E+02 W = 1.166663348216089E+01 Tp=  2443385.237225841265
     *  N = 1.419487076881987E-01 MA= 3.591116765875043E+02 TA= 3.519583775690595E+02
     *  A = 3.639556121869250E+00 AD= 6.276155841047149E+00 PR= 2.536127350949667E+03
     */
    private static final double axisVoyagerTwoInj = 3.639556121869250E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoInj = 7.244289223444542E-01; // Eccentricity [-]
    private static final double inclinationVoyagerTwoInj = 4.831487505958364E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoInj = 1.166663348216089E+01; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoInj = 3.277939445108216E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoInj = 2443385.237225841265;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoInj = 1.419487076881987E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSINJ = new double[]
            {axisVoyagerTwoInj, eccentricityVoyagerTwoInj, inclinationVoyagerTwoInj, argPeriapsisVoyagerTwoInj,
                    longNodeVoyagerTwoInj, periapsisPassageVoyagerTwoInj, meanMotionVoyagerTwoInj};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1977-10-12, Stop=1977-10-13, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     *  2443428.500000000 = A.D. 1977-Oct-12 00:00:00.0000 TDB
     *  EC= 7.231396496204042E-01 QR= 1.002958506987806E+00 IN= 4.826203822896407E+00
     *  OM= 3.277811509417505E+02 W = 1.167494096830587E+01 Tp=  2443385.233823801391
     *  N = 1.429455929955237E-01 MA= 6.184709213338521E+00 TA= 4.984460284582570E+01
     *  A = 3.622615176252852E+00 AD= 6.242271845517899E+00 PR= 2.518440704997972E+03
     */
    private static final double axisVoyagerTwoEJA = 3.622615176252852E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoEJA = 7.231396496204042E-01; // Eccentricity [-]
    private static final double inclinationVoyagerTwoEJA = 4.826203822896407E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoEJA = 1.167494096830587E+01; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoEJA = 3.277811509417505E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoEJA = 2443385.233823801391;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoEJA = 1.429455929955237E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSEARTHJUPITERA = new double[]
            {axisVoyagerTwoEJA, eccentricityVoyagerTwoEJA, inclinationVoyagerTwoEJA, argPeriapsisVoyagerTwoEJA,
                    longNodeVoyagerTwoEJA, periapsisPassageVoyagerTwoEJA, meanMotionVoyagerTwoEJA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1978-05-04, Stop=1978-05-05, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     *  2443632.500000000 = A.D. 1978-May-04 00:00:00.0000 TDB
     *  EC= 7.240853185979275E-01 QR= 1.002904035004824E+00 IN= 4.830388747167587E+00
     *  OM= 3.277892894694293E+02 W = 1.169535435048994E+01 Tp=  2443385.307888114825
     *  N = 1.422254186370598E-01 MA= 3.515700159661657E+01 TA= 1.251692674489827E+02
     *  A = 3.634833891072861E+00 AD= 6.266763747140899E+00 PR= 2.531193111961735E+03
     */
    private static final double axisVoyagerTwoEJB = 3.634833891072861E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoEJB = 7.240853185979275E-01; // Eccentricity [-]
    private static final double inclinationVoyagerTwoEJB = 4.830388747167587E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoEJB = 1.169535435048994E+01; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoEJB = 3.277892894694293E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoEJB = 2443385.307888114825;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoEJB = 1.422254186370598E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSEARTHJUPITERB = new double[]
            {axisVoyagerTwoEJB, eccentricityVoyagerTwoEJB, inclinationVoyagerTwoEJB, argPeriapsisVoyagerTwoEJB,
                    longNodeVoyagerTwoEJB, periapsisPassageVoyagerTwoEJB, meanMotionVoyagerTwoEJB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1979-06-28, Stop=1979-06-29, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     *  2444052.500000000 = A.D. 1979-Jun-28 00:00:00.0000 TDB
     *  EC= 7.891761510788325E-01 QR= 7.797887694180565E-01 IN= 6.529618339092345E+00
     *  OM= 3.245288384222724E+02 W = 1.384976078600765E+01 Tp=  2443443.555931052659
     *  N = 1.385537282337765E-01 MA= 8.437147103847956E+01 TA= 1.586715805672647E+02
     *  A = 3.698769249344460E+00 AD= 6.617749729270862E+00 PR= 2.598270032781691E+03
     */
    private static final double axisVoyagerTwoEJC = 3.698769249344460E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoEJC = 7.891761510788325E-01; // Eccentricity [-]
    private static final double inclinationVoyagerTwoEJC = 6.529618339092345E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoEJC = 1.384976078600765E+01; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoEJC = 3.245288384222724E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoEJC = 2443443.555931052659;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoEJC = 1.385537282337765E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSEARTHJUPITERC = new double[]
            {axisVoyagerTwoEJC, eccentricityVoyagerTwoEJC, inclinationVoyagerTwoEJC, argPeriapsisVoyagerTwoEJC,
                    longNodeVoyagerTwoEJC, periapsisPassageVoyagerTwoEJC, meanMotionVoyagerTwoEJC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1979-07-24, Stop=1979-07-25, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     *  2444078.500000000 = A.D. 1979-Jul-24 00:00:00.0000 TDB
     *  EC= 1.521340250626591E+00 QR= 4.985199557067689E+00 IN= 2.663050163509146E+00
     *  OM= 1.204886583014236E+02 W = 3.498208560817077E+02 Tp=  2443860.663857157342
     *  N = 3.333205861234814E-02 MA= 7.260927081126399E+00 TA= 2.903811250635727E+01
     *  A =-9.562276365724800E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerTwoJSA = -9.562276365724800E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoJSA = 1.521340250626591E+00; // Eccentricity [-]
    private static final double inclinationVoyagerTwoJSA = 2.663050163509146E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoJSA = 3.498208560817077E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoJSA = 1.204886583014236E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoJSA = 2443860.663857157342;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoJSA = 3.333205861234814E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSJUPITERSATURNA = new double[]
            {axisVoyagerTwoJSA, eccentricityVoyagerTwoJSA, inclinationVoyagerTwoJSA, argPeriapsisVoyagerTwoJSA,
                    longNodeVoyagerTwoJSA, periapsisPassageVoyagerTwoJSA, meanMotionVoyagerTwoJSA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1981-07-20, Stop=1981-07-21, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     *  2444805.500000000 = A.D. 1981-Jul-20 00:00:00.0000 TDB
     *  EC= 1.299447972947494E+00 QR= 5.028489277622843E+00 IN= 2.550968850867884E+00
     *  OM= 1.193576626078797E+02 W = 3.514654217007830E+02 Tp=  2443852.925182318315
     *  N = 1.432286332207418E-02 MA= 1.364359891770348E+01 TA= 7.953459253659599E+01
     *  A =-1.679253069615718E+01 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerTwoJSB = -1.679253069615718E+01; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoJSB = 1.299447972947494E+00; // Eccentricity [-]
    private static final double inclinationVoyagerTwoJSB = 2.550968850867884E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoJSB = 3.514654217007830E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoJSB = 1.193576626078797E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoJSB = 2443852.925182318315;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoJSB = 1.432286332207418E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSJUPITERSATURNB = new double[]
            {axisVoyagerTwoJSB, eccentricityVoyagerTwoJSB, inclinationVoyagerTwoJSB, argPeriapsisVoyagerTwoJSB,
                    longNodeVoyagerTwoJSB, periapsisPassageVoyagerTwoJSB, meanMotionVoyagerTwoJSB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1981-08-19, Stop=1981-08-20, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     *  2444835.500000000 = A.D. 1981-Aug-19 00:00:00.0000 TDB
     *  EC= 1.348176840820301E+00 QR= 4.968089729535512E+00 IN= 2.552361844015824E+00
     *  OM= 1.194523903989649E+02 W = 3.514878333076780E+02 Tp=  2443872.944983884227
     *  N = 1.828604346940308E-02 MA= 1.760132286638135E+01 TA= 8.054425085080692E+01
     *  A =-1.426886899723353E+01 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerTwoJSC = -1.426886899723353E+01; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoJSC = 1.348176840820301E+00; // Eccentricity [-]
    private static final double inclinationVoyagerTwoJSC = 2.552361844015824E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoJSC = 3.514878333076780E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoJSC = 1.194523903989649E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoJSC = 2443872.944983884227;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoJSC = 1.828604346940308E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSJUPITERSATURNC = new double[]
            {axisVoyagerTwoJSC, eccentricityVoyagerTwoJSC, inclinationVoyagerTwoJSC, argPeriapsisVoyagerTwoJSC,
                    longNodeVoyagerTwoJSC, periapsisPassageVoyagerTwoJSC, meanMotionVoyagerTwoJSC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1981-09-01, Stop=1981-09-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2444848.500000000 = A.D. 1981-Sep-01 00:00:00.0000 TDB
     *  EC= 3.691855201061780E+00 QR= 9.599956720428132E+00 IN= 2.683146360233457E+00
     *  OM= 7.666759808639857E+01 W = 1.131684596637691E+02 Tp=  2444816.092490546405
     *  N = 1.463449374869118E-01 MA= 4.742674945110442E+00 TA= 2.325040315317940E+00
     *  A =-3.566297591579783E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerTwoSUA = -3.566297591579783E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoSUA = 3.691855201061780E+00; // Eccentricity [-]
    private static final double inclinationVoyagerTwoSUA = 2.683146360233457E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoSUA = 1.131684596637691E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoSUA = 7.666759808639857E+01; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoSUA = 2444816.092490546405;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoSUA = 1.463449374869118E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSSATURNURANUSA = new double[]
            {axisVoyagerTwoSUA, eccentricityVoyagerTwoSUA, inclinationVoyagerTwoSUA, argPeriapsisVoyagerTwoSUA,
                    longNodeVoyagerTwoSUA, periapsisPassageVoyagerTwoSUA, meanMotionVoyagerTwoSUA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1986-01-01, Stop=1986-01-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2446431.500000000 = A.D. 1986-Jan-01 00:00:00.0000 TDB
     *  EC= 3.446000639492594E+00 QR= 9.602434951743630E+00 IN= 2.664189491701800E+00
     *  OM= 7.751073600421475E+01 W = 1.123881067013119E+02 Tp=  2444815.115280401893
     *  N = 1.267117422701795E-01 MA= 2.048149239991712E+02 TA= 6.861803252885241E+01
     *  A =-3.925769599854068E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerTwoSUB = -3.925769599854068E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoSUB = 3.446000639492594E+00; // Eccentricity [-]
    private static final double inclinationVoyagerTwoSUB = 2.664189491701800E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoSUB = 1.123881067013119E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoSUB = 7.751073600421475E+01; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoSUB = 2444815.115280401893;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoSUB = 1.267117422701795E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSSATURNURANUSB = new double[]
            {axisVoyagerTwoSUB, eccentricityVoyagerTwoSUB, inclinationVoyagerTwoSUB, argPeriapsisVoyagerTwoSUB,
                    longNodeVoyagerTwoSUB, periapsisPassageVoyagerTwoSUB, meanMotionVoyagerTwoSUB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1986-02-15, Stop=1986-02-16, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2446476.500000000 = A.D. 1986-Feb-15 00:00:00.0000 TDB
     *  EC= 5.815866987110352E+00 QR= 1.440398204357419E+01 IN= 2.493426750135045E+00
     *  OM= 2.604639416712294E+02 W = 3.137736464234107E+02 Tp=  2445299.220436299220
     *  N = 1.905425363672792E-01 MA= 2.243218340809485E+02 TA= 4.526411435418022E+01
     *  A =-2.990942665594040E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerTwoUNA = -2.990942665594040E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoUNA = 5.815866987110352E+00; // Eccentricity [-]
    private static final double inclinationVoyagerTwoUNA = 2.493426750135045E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoUNA = 3.137736464234107E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoUNA = 2.604639416712294E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoUNA = 2445299.220436299220;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoUNA = 1.905425363672792E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSURANUSNEPTUNEA = new double[]
            {axisVoyagerTwoUNA, eccentricityVoyagerTwoUNA, inclinationVoyagerTwoUNA, argPeriapsisVoyagerTwoUNA,
                    longNodeVoyagerTwoUNA, periapsisPassageVoyagerTwoUNA, meanMotionVoyagerTwoUNA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Voyager 2 (spacecraft) [-32]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1989-08-01, Stop=1989-08-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2447739.500000000 = A.D. 1989-Aug-01 00:00:00.0000 TDB
     *  EC= 5.807870731235967E+00 QR= 1.438076629332257E+01 IN= 2.499386708647308E+00
     *  OM= 2.604962418463241E+02 W = 3.136691915414523E+02 Tp=  2445298.913392163347
     *  N = 1.905286124500218E-01 MA= 4.650015799552564E+02 TA= 6.704897153193106E+01
     *  A =-2.991088383448631E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisVoyagerTwoUNB = -2.991088383448631E+00; // Semi-major axis [au]
    private static final double eccentricityVoyagerTwoUNB = 5.807870731235967E+00; // Eccentricity [-]
    private static final double inclinationVoyagerTwoUNB = 2.499386708647308E+00; // Inclination [degrees]
    private static final double argPeriapsisVoyagerTwoUNB = 3.136691915414523E+02; // Arg perifocus [degrees]
    private static final double longNodeVoyagerTwoUNB = 2.604962418463241E+02; // Long asc node [degrees]
    private static final double periapsisPassageVoyagerTwoUNB = 2445298.913392163347;  // Time of periapsis [JD]
    private static final double meanMotionVoyagerTwoUNB = 1.905286124500218E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSURANUSNEPTUNEB = new double[]
            {axisVoyagerTwoUNB, eccentricityVoyagerTwoUNB, inclinationVoyagerTwoUNB, argPeriapsisVoyagerTwoUNB,
                    longNodeVoyagerTwoUNB, periapsisPassageVoyagerTwoUNB, meanMotionVoyagerTwoUNB};

    /**
     * Constructor.
     *
     * @param name        name of spacecraft
     * @param dateTime    current simulation date/time
     * @param solarSystem the Solar System
     */
    public VoyagerTwo(String name, GregorianCalendar dateTime, SolarSystem solarSystem) {
        super(name, dateTime, solarSystem);
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

        // From Saturn to Uranus
        trajectories.add(
                new SpacecraftTrajectory(saturnFlyBy, corrSaturnToUranusA, "Sun", ORBITPARSJUPITERSATURNC));
        trajectories.add(
                new SpacecraftTrajectory(corrSaturnToUranusA, corrSaturnToUranusB, "Sun", ORBITPARSSATURNURANUSA));
        trajectories.add(
                new SpacecraftTrajectory(corrSaturnToUranusB, uranusFlyBy, "Sun", ORBITPARSSATURNURANUSB));

        // From Uranus to Neptune
        trajectories.add(
                new SpacecraftTrajectory(uranusFlyBy, corrUranusToNeptuneA, "Sun", ORBITPARSSATURNURANUSB));
        trajectories.add(
                new SpacecraftTrajectory(corrUranusToNeptuneA, corrUranusToNeptuneB,"Sun", ORBITPARSURANUSNEPTUNEA));
        trajectories.add(
                new SpacecraftTrajectory(corrUranusToNeptuneB, neptuneFlyBy,"Sun", ORBITPARSURANUSNEPTUNEB));

        // From Neptune till 100 A.U. from the Sun
        trajectories.add(
                new SpacecraftTrajectory(neptuneFlyBy, hundredAU, "Sun", ORBITPARSURANUSNEPTUNEB));

        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterInj)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrEarthToJupiterC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrJupiterToSaturnA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrJupiterToSaturnB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrJupiterToSaturnC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrSaturnToUranusA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrSaturnToUranusB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrUranusToNeptuneA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(corrUranusToNeptuneB)));
    }
}