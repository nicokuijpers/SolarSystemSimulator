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

public class NewHorizons extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/New_Horizons
     * Trajectory corrections
     * On January 28 and 30, 2006, mission controllers guided the probe through its first
     * trajectory-correction maneuver (TCM), which was divided into two parts (TCM-1A and TCM-1B).
     * The total velocity change of these two corrections was about 18 meters per second (65 km/h; 40 mph).
     * TCM-1 was accurate enough to permit the cancellation of TCM-2, the second of three originally
     * scheduled corrections. On March 9, 2006, controllers performed TCM-3, the last of three scheduled
     * course corrections. The engines burned for 76 seconds, adjusting the spacecraft's velocity by
     * about 1.16 m/s (4.2 km/h; 2.6 mph). Further trajectory maneuvers were not needed until
     * September 25, 2007 (seven months after the Jupiter flyby), when the engines were fired for
     * 15 minutes and 37 seconds, changing the spacecraft's velocity by 2.37 m/s (8.5 km/h; 5.3 mph),
     * followed by another TCM, almost three years later on June 30, 2010, that lasted 35.6 seconds,
     * when New Horizons had already reached the halfway point (in time traveled) to Pluto.
     *
     * On July 14, 2014, mission controllers performed a sixth trajectory-correction maneuver (TCM)
     * since its launch to enable the craft to reach Pluto.
     *
     * http://spaceflight101.com/newhorizons/project-history-mission-profile/
     * The final pre-encounter trajectory correction maneuver was conducted on June 30, 2015,
     * a 23-second thruster burn changing the vehicle’s velocity by 0.8 Kilometers per Hour
     * to properly position itself for the flyby that is basically targeting a 100 by 150-Kilometer
     * window that has to be passed within 100 seconds of a specified time so that the science
     * sequence can work out.
     *
     * https://en.wikipedia.org/wiki/New_Horizons
     * The new mission began on October 22, 2015, when New Horizons carried out the first in a series
     * of four initial targeting maneuvers designed to send it toward 2014 MU69. The maneuver, which
     * started at approximately 19:50 UTC and used two of the spacecraft's small hydrazine-fueled thrusters,
     * lasted approximately 16 minutes and changed the spacecraft's trajectory by about 10 meters per
     * second (33 ft/s). The remaining three targeting maneuvers took place on October 25, October 28,
     * and November 4, 2015.
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch January 19, 2006, 19:00 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(2006, 0, 19, 19, 0, 0);

    // Schedule simulated correction on January 31, 2006, after TCM-1A and TCM-1B on January 28 and 30
    private static final GregorianCalendar correctionA =
            new GregorianCalendar(2006, 0, 31, 0, 0, 0);

    // Schedule simulated correction on March 10, 2006, after TCM-3 on March 9, 2006
    private static final GregorianCalendar correctionB =
            new GregorianCalendar(2006, 2, 10, 0, 0, 0);

    // Schedule simulated correction on September 26, 2007, seven months after Jupiter fly by
    private static final GregorianCalendar correctionC =
            new GregorianCalendar(2007, 8, 26, 0, 0, 0);

    // Schedule simulated correction on July 1, 2010, after TCM on June 30, 2010 at the halfway point to Pluto
    private static final GregorianCalendar correctionD =
            new GregorianCalendar(2010, 6, 1, 0, 0, 0);

    // Schedule simulated correction on July 15, 2014, after TCM on July 14, 2014
    private static final GregorianCalendar correctionE =
            new GregorianCalendar(2014, 6, 15, 0, 0, 0);

    // Schedule simulated correction on July 1, 2015, after TCM on June 30, 2015
    private static final GregorianCalendar correctionF =
            new GregorianCalendar(2015, 6, 1, 0, 0, 0);

    // Schedule simulated correction on November 5, 2015, after targeting manoeuvres
    // on October 22, 25, 28, and November 4
    private static final GregorianCalendar correctionG =
            new GregorianCalendar(2015, 10, 5, 0, 0, 0);

    // Schedule simulated correction on December 1, 2018, one month before Ultima Thule flyby
    private static final GregorianCalendar correctionH =
            new GregorianCalendar(2018, 11, 1, 0, 0, 0);

    // Schedule simulated correction on January 1, 2019, shortly before Ultima Thule flyby
    private static final GregorianCalendar correctionI =
            new GregorianCalendar(2019, 0, 1, 0, 0, 0);

    // Jupiter fly February 28, 2007, 05:43:40 UTC (distance 2.3 million km)
    private static final GregorianCalendar jupiterFlyBy =
            new GregorianCalendar(2007, 1, 28, 5, 43, 40);

    // Pluto fly by July 14, 2015, 11:49 UTC
    // distance 12,472 km from the surface and 13,658 km from the center of Pluto
    private static final GregorianCalendar plutoFlyBy =
            new GregorianCalendar(2015, 6, 14, 11, 49, 0);

    // Ultima Thule fly by January 1, 2019, 05:33 UTC (velocity 14.3 km/s, distance 3,500 km)
    private static final GregorianCalendar ultimaThuleFlyBy =
            new GregorianCalendar(2019, 0, 1, 5, 33, 0);

    // End of trajectory January 1, 2025
    private static final GregorianCalendar endOfTrajectory =
            new GregorianCalendar(2025, 0, 1, 5, 33, 0);

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2006-01-31, Stop=2006-02-01, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2453766.500000000 = A.D. 2006-Jan-31 00:00:00.0000 TDB
     * EC= 1.032154572649074E+00 QR= 9.840868925792915E-01 IN= 8.702593969192690E-01
     * OM= 1.199136529266685E+02 W = 3.597221433789401E+02 Tp=  2453755.397929675411
     * N = 5.821273656923798E-03 MA= 6.462818951810567E-02 TA= 1.577334372811838E+01
     * A =-3.060488171680472E+01 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisNewHorizonsA = -3.060488171680472E+01; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsA = 1.032154572649074E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsA = 8.702593969192690E-01; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsA = 3.597221433789401E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsA = 1.199136529266685E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsA = 2453755.397929675411;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsA = 5.821273656923798E-03; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRA = new double[]
            {axisNewHorizonsA, eccentricityNewHorizonsA, inclinationNewHorizonsA, argPeriapsisNewHorizonsA,
                    longNodeNewHorizonsA, periapsisPassageNewHorizonsA, meanMotionNewHorizonsA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2006-03-10, Stop=2006-03-11, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2453804.500000000 = A.D. 2006-Mar-10 00:00:00.0000 TDB
     * EC= 1.031949793328875E+00 QR= 9.841214520945972E-01 IN= 8.705353247968802E-01
     * OM= 1.199466080945568E+02 W = 3.596916509986618E+02 Tp=  2453755.397572083864
     * N = 5.765448612068675E-03 MA= 2.830975248795066E-01 TA= 5.822817166816290E+01
     * A =-3.080212262923047E+01 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisNewHorizonsB = -3.080212262923047E+01; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsB = 1.031949793328875E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsB = 8.705353247968802E-01; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsB = 3.596916509986618E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsB = 1.199466080945568E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsB = 2453755.397572083864;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsB = 5.765448612068675E-03; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRB = new double[]
            {axisNewHorizonsB, eccentricityNewHorizonsB, inclinationNewHorizonsB, argPeriapsisNewHorizonsB,
                    longNodeNewHorizonsB, periapsisPassageNewHorizonsB, meanMotionNewHorizonsB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2007-09-26, Stop=2007-09-27, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2454369.500000000 = A.D. 2007-Sep-26 00:00:00.0000 TDB
     * EC= 1.403148775321735E+00 QR= 2.267817752591807E+00 IN= 2.345916777788549E+00
     * OM= 2.295523822098868E+02 W = 2.880636932635999E+02 Tp=  2453772.331886103377
     * N = 7.387370234286945E-02 MA= 4.411501949465163E+01 TA= 1.012810307481700E+02
     * A =-5.625262660867479E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisNewHorizonsC = -5.625262660867479E+00; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsC = 1.403148775321735E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsC = 2.345916777788549E+00; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsC = 2.880636932635999E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsC = 2.295523822098868E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsC = 2453772.331886103377;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsC = 7.387370234286945E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRC = new double[]
            {axisNewHorizonsC, eccentricityNewHorizonsC, inclinationNewHorizonsC, argPeriapsisNewHorizonsC,
                    longNodeNewHorizonsC, periapsisPassageNewHorizonsC, meanMotionNewHorizonsC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2010-07-01, Stop=2010-07-02, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2455378.500000000 = A.D. 2010-Jul-01 00:00:00.0000 TDB
     *  EC= 1.400120740010104E+00 QR= 2.264778464258518E+00 IN= 2.348588177262165E+00
     *  OM= 2.296294915996170E+02 W = 2.878960378778723E+02 Tp=  2453771.284654943272
     *  N = 7.319005590753830E-02 MA= 1.176321809601678E+02 TA= 1.191556889549182E+02
     *  A =-5.660237617778407E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
     private static final double axisNewHorizonsD = -5.660237617778407E+00; // Semi-major axis [au]
     private static final double eccentricityNewHorizonsD = 1.400120740010104E+00; // Eccentricity [-]
     private static final double inclinationNewHorizonsD = 2.348588177262165E+00; // Inclination [degrees]
     private static final double argPeriapsisNewHorizonsD = 2.878960378778723E+02; // Arg perifocus [degrees]
     private static final double longNodeNewHorizonsD = 2.296294915996170E+02; // Long asc node [degrees]
     private static final double periapsisPassageNewHorizonsD = 2453771.284654943272;  // Time of periapsis [JD]
     private static final double meanMotionNewHorizonsD = 7.319005590753830E-02; // Mean motion [degrees/day]
     private static final double[] ORBITPARSCORRD = new double[]
             {axisNewHorizonsD, eccentricityNewHorizonsD, inclinationNewHorizonsD, argPeriapsisNewHorizonsD,
                     longNodeNewHorizonsD, periapsisPassageNewHorizonsD, meanMotionNewHorizonsD};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2014-07-15, Stop=2014-07-16, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2456853.500000000 = A.D. 2014-Jul-15 00:00:00.0000 TDB
     *  EC= 1.397872242736354E+00 QR= 2.247687579530392E+00 IN= 2.348400416948234E+00
     *  OM= 2.296022208761585E+02 W = 2.877609150526156E+02 Tp=  2453773.918376181275
     *  N = 7.340330562577639E-02 MA= 2.260514711326839E+02 TA= 1.258932723783370E+02
     *  A =-5.649269635076791E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisNewHorizonsE = -5.649269635076791E+00; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsE = 1.397872242736354E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsE = 2.348400416948234E+00; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsE = 2.877609150526156E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsE = 2.296022208761585E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsE = 2453773.918376181275;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsE = 7.340330562577639E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRE = new double[]
            {axisNewHorizonsE, eccentricityNewHorizonsE, inclinationNewHorizonsE, argPeriapsisNewHorizonsE,
                    longNodeNewHorizonsE, periapsisPassageNewHorizonsE, meanMotionNewHorizonsE};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2015-07-01, Stop=2015-07-02, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2457204.500000000 = A.D. 2015-Jul-01 00:00:00.0000 TDB
     *  EC= 1.399315027119753E+00 QR= 2.253903818470181E+00 IN= 2.348272394777127E+00
     *  OM= 2.295961930267935E+02 W = 2.878396680602254E+02 Tp=  2453774.283111465164
     *  N = 7.349782475538491E-02 MA= 2.521134797464919E+02 TA= 1.266409568421320E+02
     *  A =-5.644425241713329E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisNewHorizonsF = -5.644425241713329E+00; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsF = 1.399315027119753E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsF = 2.348272394777127E+00; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsF = 2.878396680602254E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsF = 2.295961930267935E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsF = 2453774.283111465164;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsF = 7.349782475538491E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRF = new double[]
            {axisNewHorizonsF, eccentricityNewHorizonsF, inclinationNewHorizonsF, argPeriapsisNewHorizonsF,
                    longNodeNewHorizonsF, periapsisPassageNewHorizonsF, meanMotionNewHorizonsF};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2015-11-05, Stop=2015-11-06, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2457331.500000000 = A.D. 2015-Nov-05 00:00:00.0000 TDB
     * EC= 1.416476743207705E+00 QR= 2.344221949774322E+00 IN= 2.235423081756657E+00
     * OM= 2.252474692424266E+02 W = 2.930844783397293E+02 Tp=  2453770.841885065660
     * N = 7.380607560443675E-02 MA= 2.627982020323835E+02 TA= 1.260031812984911E+02
     * A =-5.628698331914316E+00 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     */
    private static final double axisNewHorizonsG = -5.628698331914316E+00; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsG = 1.416476743207705E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsG = 2.235423081756657E+00; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsG = 2.930844783397293E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsG = 2.252474692424266E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsG = 2453770.841885065660;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsG = 7.380607560443675E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRG = new double[]
            {axisNewHorizonsG, eccentricityNewHorizonsG, inclinationNewHorizonsG, argPeriapsisNewHorizonsG,
                    longNodeNewHorizonsG, periapsisPassageNewHorizonsG, meanMotionNewHorizonsG};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2018-12-01, Stop=2019-01-01, Step=1 MO
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2458453.500000000 = A.D. 2018-Dec-01 00:00:00.0000 TDB
     *  EC= 1.423246590526184E+00 QR= 2.381539791722414E+00 IN= 2.231839785012886E+00
     *  OM= 2.250977613899296E+02 W = 2.935784781509246E+02 Tp=  2453768.446808363311
     *  N = 7.384269065341301E-02 MA= 3.459569335248075E+02 TA= 1.274722235173446E+02
     *  A =-5.626837510401844E+00 AD= 9.999999999999998E+99 PR= 9.999999999999998E+99
     */
    private static final double axisNewHorizonsH = -5.626837510401844E+00; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsH = 1.423246590526184E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsH = 2.231839785012886E+00; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsH = 2.935784781509246E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsH = 2.250977613899296E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsH = 2453768.446808363311;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsH = 7.384269065341301E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRH = new double[]
            {axisNewHorizonsH, eccentricityNewHorizonsH, inclinationNewHorizonsH, argPeriapsisNewHorizonsH,
                    longNodeNewHorizonsH, periapsisPassageNewHorizonsH, meanMotionNewHorizonsH};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : New Horizons (spacecraft) [NH New_Horizons] [-98]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2018-12-01, Stop=2019-01-01, Step=1 MO
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2458484.500000000 = A.D. 2019-Jan-01 00:00:00.0000 TDB
     *  EC= 1.423498352586861E+00 QR= 2.382657591051644E+00 IN= 2.232642151939460E+00
     *  OM= 2.251348307428457E+02 W = 2.935530987895121E+02 Tp=  2453768.554234809708
     *  N = 7.385658274070957E-02 MA= 3.483036386074741E+02 TA= 1.274999877991278E+02
     *  A =-5.626131899917977E+00 AD= 9.999999999999998E+99 PR= 9.999999999999998E+99
     */
    private static final double axisNewHorizonsI = -5.626131899917977E+00; // Semi-major axis [au]
    private static final double eccentricityNewHorizonsI = 1.423498352586861E+00; // Eccentricity [-]
    private static final double inclinationNewHorizonsI = 2.232642151939460E+00; // Inclination [degrees]
    private static final double argPeriapsisNewHorizonsI = 2.935530987895121E+02; // Arg perifocus [degrees]
    private static final double longNodeNewHorizonsI = 2.251348307428457E+02; // Long asc node [degrees]
    private static final double periapsisPassageNewHorizonsI = 2453768.554234809708;  // Time of periapsis [JD]
    private static final double meanMotionNewHorizonsI = 7.385658274070957E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRI = new double[]
            {axisNewHorizonsI, eccentricityNewHorizonsI, inclinationNewHorizonsI, argPeriapsisNewHorizonsI,
                    longNodeNewHorizonsI, periapsisPassageNewHorizonsI, meanMotionNewHorizonsI};

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public NewHorizons(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {

        List<SpacecraftTrajectory> trajectories = new ArrayList<>();

        // From Earth to Jupiter
        trajectories.add(
                new SpacecraftTrajectory(launch, correctionA, "Sun", ORBITPARSCORRA));
        trajectories.add(
                new SpacecraftTrajectory(correctionA, correctionB, "Sun", ORBITPARSCORRA));
        trajectories.add(
                new SpacecraftTrajectory(correctionB, jupiterFlyBy, "Sun", ORBITPARSCORRB));

        // From Jupiter to Pluto
        trajectories.add(
                new SpacecraftTrajectory(jupiterFlyBy, correctionC, "Sun", ORBITPARSCORRB));
        trajectories.add(
                new SpacecraftTrajectory(correctionC, correctionD, "Sun", ORBITPARSCORRC));
        trajectories.add(
                new SpacecraftTrajectory(correctionD, correctionE, "Sun", ORBITPARSCORRD));
        trajectories.add(
                new SpacecraftTrajectory(correctionE, correctionF, "Sun", ORBITPARSCORRE));
        trajectories.add(
                new SpacecraftTrajectory(correctionF, plutoFlyBy, "Sun", ORBITPARSCORRF));

        // From Pluto to Ultima Thule
        trajectories.add(
                new SpacecraftTrajectory(plutoFlyBy, correctionG, "Sun", ORBITPARSCORRF));
        trajectories.add(
                new SpacecraftTrajectory(correctionG, correctionH, "Sun", ORBITPARSCORRG));
        trajectories.add(
                new SpacecraftTrajectory(correctionH, correctionI, "Sun", ORBITPARSCORRH));
        trajectories.add(
                new SpacecraftTrajectory(correctionI, ultimaThuleFlyBy, "Sun", ORBITPARSCORRI));

        // Beyond Ultima Thule
        trajectories.add(
                new SpacecraftTrajectory(ultimaThuleFlyBy, endOfTrajectory, "Sun", ORBITPARSCORRI));

        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {

        // Corrections for New Horizons
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionD)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionE)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionF)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionG)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionH)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionI)));

        // Corrections for Ultima Thule on December 1, 2018 and January 1, 2019 before fly by
        solarSystem.addSpacecraftEvent(new SpacecraftEvent("Ultima Thule",correctionH));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent("Ultima Thule",correctionI));
    }
}