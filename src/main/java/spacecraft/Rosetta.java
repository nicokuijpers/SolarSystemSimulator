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
import ephemeris.EphemerisUtil;
import ephemeris.JulianDateConverter;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

public class Rosetta extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/Rosetta_(spacecraft)
     * Rosetta was launched on 2 March 2004 at 07:17 UTC from the Guiana Space Centre in French Guiana.
     *
     * To achieve the required velocity to rendezvous with 67P, Rosetta used gravity assist manoeuvres to
     * accelerate throughout the inner Solar System. The comet's orbit was known before Rosetta's launch,
     * from ground-based measurements, to an accuracy of approximately 100 km (62 mi). Information gathered
     * by the onboard cameras beginning at a distance of 24 million kilometres (15,000,000 mi) were processed
     * at ESA's Operation Centre to refine the position of the comet in its orbit to a few kilometres.
     *
     * The first Earth flyby was on 4 March 2005.
     *
     * On 25 February 2007, the craft was scheduled for a low-altitude flyby of Mars, to correct the trajectory.
     * This was not without risk, as the estimated altitude of the flyby was a mere 250 kilometres (160 mi).
     * During that encounter, the solar panels could not be used since the craft was in the planet's shadow,
     * where it would not receive any solar light for 15 minutes, causing a dangerous shortage of power. The
     * craft was therefore put into standby mode, with no possibility to communicate, flying on batteries that
     * were originally not designed for this task. This Mars manoeuvre was therefore nicknamed
     * "The Billion Euro Gamble". The flyby was successful, with Rosetta even returning detailed images of
     * the surface and atmosphere of the planet, and the mission continued as planned.
     *
     * The second Earth flyby was on 13 November 2007 at a distance of 5,700 km (3,500 mi). In observations
     * made on 7 and 8 November, Rosetta was briefly mistaken for a near-Earth asteroid about 20 m (66 ft) in
     * diameter by an astronomer of the Catalina Sky Survey and was given the provisional designation 2007 VN84.
     * Calculations showed that it would pass very close to Earth, which led to speculation that it could impact
     * Earth. However, astronomer Denis Denisenko recognised that the trajectory matched that of Rosetta, which
     * the Minor Planet Center confirmed in an editorial release on 9 November.
     *
     * The spacecraft performed a close flyby of asteroid 2867 Šteins on 5 September 2008. Its onboard cameras
     * were used to fine-tune the trajectory, achieving a minimum separation of less than 800 km (500 mi).
     * Onboard instruments measured the asteroid from 4 August to 10 September. Maximum relative speed between
     * the two objects during the flyby was 8.6 km/s (19,000 mph; 31,000 km/h).
     *
     * Rosetta's third and final flyby of Earth happened on 12 November 2009. Distance of flyby was 2478 km.
     * On 10 July 2010, Rosetta flew by 21 Lutetia, a large main-belt asteroid, at a minimum distance of
     * 3,168±7.5 km (1,969±4.7 mi) at a velocity of 15 kilometres per second (9.3 mi/s). The flyby
     * provided images of up to 60 metres (200 ft) per pixel resolution and covered about 50% of the surface,
     * mostly in the northern hemisphere. The 462 images were obtained in 21 narrow- and broad-band filters
     * extending from 0.24 to 1 μm. Lutetia was also observed by the visible–near-infrared imaging spectrometer
     * VIRTIS, and measurements of the magnetic field and plasma environment were taken as well.
     *
     * After leaving its hibernation mode in January 2014 and getting closer to the comet Rosetta began a series of
     * eight burns in May 2014. These reduced the relative velocity between the spacecraft and 67P from
     * 775 m/s (2,540 ft/s) to 7.9 m/s (26 ft/s).
     *
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Flybys:
     *    Earth: 2005-Mar-04 22:10 UTC (8340 km from center, ~1962 km altitude)
     *    Mars : 2007-Feb-25           (250 km altitude)
     *    Earth: 2007-Nov-13 20:57 UTC (11,678 km (center), alt=0.83 Earth-radii)
     *    2867 Steins: 2008-Sep-05 18:37 UTC (800 km, 8.6 km/s)
     *    Earth: 2009-Nov-13 07:45:40 UTC (alt~2490 km, lat=-8.2 deg, long.=109.0 deg)
     *    21 Lutetia: 2010-Jul-10 15:44:45 UTC (event time, 3235 km, 15 km/s)
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Schedule an event for launch March 2, 2004, 07:17 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(2004, 2, 2, 7, 17, 0);

    // Schedule an event 2 hours after launch when orbital parameters are known
    private static final GregorianCalendar launchB =
            new GregorianCalendar(2004, 2, 2, 10, 0, 0);

    // Schedule an extra event 4 hours after launch TODO
    private static final GregorianCalendar launchCorrectionB =
            new GregorianCalendar(2004, 2, 2, 11, 17, 0);

    // Schedule simulated correction on March 4, 2004, after launch on March 2, 2004
    private static final GregorianCalendar correctionLaunch =
            new GregorianCalendar(2004, 2, 4, 0, 0, 0);

    // Schedule simulated correction on March 5, 2005, after first Earth fly by on March 4, 2005
    private static final GregorianCalendar correctionA =
            new GregorianCalendar(2005, 2, 5, 0, 0, 0);

    // Schedule simulated correction on February 26, 2007, after Mars fly by on February 25, 2007
    private static final GregorianCalendar correctionB =
            new GregorianCalendar(2007, 1, 26, 0, 0, 0);

    // Schedule simulated correction on November 14, 2007, after second Earth fly by on November 13, 2007
    private static final GregorianCalendar correctionC =
            new GregorianCalendar(2007, 10, 14, 0, 0, 0);

    // Schedule simulated correction on November 13, 2009, after third Earth fly by on November 12, 2009
    private static final GregorianCalendar correctionD =
            new GregorianCalendar(2009, 10, 13, 0, 0, 0);

    // Schedule simulated correction on June 1, 2014, after series of eight burns in May 2014
    private static final GregorianCalendar correctionE =
            new GregorianCalendar(2014, 5, 1, 0, 0, 0);

    // End of trajectory January 1, 2025
    private static final GregorianCalendar endOfTrajectory =
            new GregorianCalendar(2025, 0, 1, 0, 0, 0);

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2004-03-02 10:00, Stop=2004-03-03 00:00, Step=1 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2453066.916666667 = A.D. 2004-Mar-02 10:00:00.0000 TDB
     *  EC= 2.597403935579663E-01 QR= 8.493497725316816E-01 IN= 2.437804180513474E+00
     *  OM= 1.620634169991943E+02 W = 7.222260932133044E+01 Tp=  2453123.912768201903
     *  N = 8.019555358958194E-01 MA= 3.142916608490734E+02 TA= 2.878318376238989E+02
     *  A = 1.147367443988976E+00 AD= 1.445385115446271E+00 PR= 4.489026933368124E+02
     */
    private static final double axisRosettaLaunch = 1.147367443988976E+00; // Semi-major axis [au]
    private static final double eccentricityRosettaLaunch = 2.597403935579663E-01; // Eccentricity [-]
    private static final double inclinationRosettaLaunch = 2.437804180513474E+00; // Inclination [degrees]
    private static final double argPeriapsisRosettaLaunch = 7.222260932133044E+01; // Arg perifocus [degrees]
    private static final double longNodeRosettaLaunch = 1.620634169991943E+02; // Long asc node [degrees]
    private static final double periapsisPassageRosettaLaunch = 2453123.912768201903;  // Time of periapsis [JD]
    private static final double meanMotionRosettaLaunch = 8.019555358958194E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSLAUNCH = new double[]
            {axisRosettaLaunch, eccentricityRosettaLaunch, inclinationRosettaLaunch, argPeriapsisRosettaLaunch,
                    longNodeRosettaLaunch, periapsisPassageRosettaLaunch, meanMotionRosettaLaunch};



    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2004-03-03, Stop=2004-03-04, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2453067.500000000 = A.D. 2004-Mar-03 00:00:00.0000 TDB
     *  EC= 1.181253916924631E-01 QR= 8.749069208007698E-01 IN= 4.775211047953511E-01
     *  OM= 1.616181303474116E+02 W = 9.672061019820825E+01 Tp=  2453149.795950699598
     *  N = 9.974050281501289E-01 MA= 2.779176049758404E+02 TA= 2.643602582083620E+02
     *  A = 9.920990042789197E-01 AD= 1.109291087757070E+00 PR= 3.609366203694462E+02
     *
    private static final double axisRosettaLaunch = 9.920990042789197E-01; // Semi-major axis [au]
    private static final double eccentricityRosettaLaunch = 1.181253916924631E-01; // Eccentricity [-]
    private static final double inclinationRosettaLaunch = 4.775211047953511E-01; // Inclination [degrees]
    private static final double argPeriapsisRosettaLaunch = 9.672061019820825E+01; // Arg perifocus [degrees]
    private static final double longNodeRosettaLaunch = 1.616181303474116E+02; // Long asc node [degrees]
    private static final double periapsisPassageRosettaLaunch = 2453149.795950699598;  // Time of periapsis [JD]
    private static final double meanMotionRosettaLaunch = 9.974050281501289E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSLAUNCH = new double[]
            {axisRosettaLaunch, eccentricityRosettaLaunch, inclinationRosettaLaunch, argPeriapsisRosettaLaunch,
                    longNodeRosettaLaunch, periapsisPassageRosettaLaunch, meanMotionRosettaLaunch};
     */

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2004-03-03, Stop=2004-03-04, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2453068.500000000 = A.D. 2004-Mar-04 00:00:00.0000 TDB
     *  EC= 1.099305282166358E-01 QR= 8.815010335404413E-01 IN= 4.352276775418791E-01
     *  OM= 1.614802360823107E+02 W = 9.717440650284165E+01 Tp=  2453150.842839940917
     *  N = 1.000013109815799E+00 MA= 2.776560805594139E+02 TA= 2.650415133091126E+02
     *  A = 9.903732927433688E-01 AD= 1.099245551946296E+00 PR= 3.599952805281839E+02
     */
    private static final double axisRosettaLaunchA = 9.903732927433688E-01; // Semi-major axis [au]
    private static final double eccentricityRosettaLaunchA = 1.099305282166358E-01; // Eccentricity [-]
    private static final double inclinationRosettaLaunchA = 4.352276775418791E-01; // Inclination [degrees]
    private static final double argPeriapsisRosettaLaunchA = 9.717440650284165E+01; // Arg perifocus [degrees]
    private static final double longNodeRosettaLaunchA = 1.614802360823107E+02; // Long asc node [degrees]
    private static final double periapsisPassageRosettaLaunchA = 2453150.842839940917;  // Time of periapsis [JD]
    private static final double meanMotionRosettaLaunchA = 1.000013109815799E+00; // Mean motion [degrees/day]
    private static final double[] ORBITPARSLAUNCHA = new double[]
            {axisRosettaLaunchA, eccentricityRosettaLaunchA, inclinationRosettaLaunchA, argPeriapsisRosettaLaunchA,
                    longNodeRosettaLaunchA, periapsisPassageRosettaLaunchA, meanMotionRosettaLaunchA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2005-03-05, Stop=2005-03-06, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2453434.500000000 = A.D. 2005-Mar-05 00:00:00.0000 TDB
     *  EC= 4.246705828252503E-01 QR= 9.912674884262668E-01 IN= 1.984710392201771E+00
     *  OM= 3.443686164528800E+02 W = 1.768206388030037E+02 Tp=  2453431.755702217575
     *  N = 4.358054439478253E-01 MA= 1.195979913450196E+00 TA= 3.270134831548257E+00
     *  A = 1.722956377398622E+00 AD= 2.454645266370977E+00 PR= 8.260566842370589E+02
     */
    private static final double axisRosettaA = 1.722956377398622E+00; // Semi-major axis [au]
    private static final double eccentricityRosettaA = 4.246705828252503E-01; // Eccentricity [-]
    private static final double inclinationRosettaA = 1.984710392201771E+00; // Inclination [degrees]
    private static final double argPeriapsisRosettaA = 1.768206388030037E+02; // Arg perifocus [degrees]
    private static final double longNodeRosettaA = 3.443686164528800E+02; // Long asc node [degrees]
    private static final double periapsisPassageRosettaA = 2453431.755702217575;  // Time of periapsis [JD]
    private static final double meanMotionRosettaA = 4.358054439478253E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRA = new double[]
            {axisRosettaA, eccentricityRosettaA, inclinationRosettaA, argPeriapsisRosettaA,
                    longNodeRosettaA, periapsisPassageRosettaA, meanMotionRosettaA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2007-02-26, Stop=2007-02-27, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2454157.500000000 = A.D. 2007-Feb-26 00:00:00.0000 TDB
     *  EC= 3.381411011419823E-01 QR= 7.841710220233996E-01 IN= 1.897385139640902E+00
     *  OM= 5.101265469585732E+01 W = 7.979902663010598E+01 Tp=  2454004.981078150216
     *  N = 7.642509870481207E-01 MA= 1.165627365671427E+02 TA= 1.445743516641696E+02
     *  A = 1.184800904507624E+00 AD= 1.585430786991849E+00 PR= 4.710494406954986E+02
     */
    private static final double axisRosettaB = 1.184800904507624E+00; // Semi-major axis [au]
    private static final double eccentricityRosettaB = 3.381411011419823E-01; // Eccentricity [-]
    private static final double inclinationRosettaB = 1.897385139640902E+00; // Inclination [degrees]
    private static final double argPeriapsisRosettaB = 7.979902663010598E+01; // Arg perifocus [degrees]
    private static final double longNodeRosettaB = 5.101265469585732E+01; // Long asc node [degrees]
    private static final double periapsisPassageRosettaB = 2454004.981078150216;  // Time of periapsis [JD]
    private static final double meanMotionRosettaB = 7.642509870481207E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRB = new double[]
            {axisRosettaB, eccentricityRosettaB, inclinationRosettaB, argPeriapsisRosettaB,
                    longNodeRosettaB, periapsisPassageRosettaB, meanMotionRosettaB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2007-11-14, Stop=2007-11-15, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2454418.500000000 = A.D. 2007-Nov-14 00:00:00.0000 TDB
     *  EC= 4.447288616001949E-01 QR= 9.080658370985866E-01 IN= 7.954064305847544E+00
     *  OM= 5.101357878578169E+01 W = 4.285618637616749E+01 Tp=  2454451.587459082715
     *  N = 4.712872806981350E-01 MA= 3.444063013836474E+02 TA= 3.172648262789367E+02
     *  A = 1.635355728582391E+00 AD= 2.362645620066194E+00 PR= 7.638652998797651E+02
     */
    private static final double axisRosettaC = 1.635355728582391E+00; // Semi-major axis [au]
    private static final double eccentricityRosettaC = 4.447288616001949E-01; // Eccentricity [-]
    private static final double inclinationRosettaC = 7.954064305847544E+00; // Inclination [degrees]
    private static final double argPeriapsisRosettaC = 4.285618637616749E+01; // Arg perifocus [degrees]
    private static final double longNodeRosettaC = 5.101357878578169E+01; // Long asc node [degrees]
    private static final double periapsisPassageRosettaC = 2454451.587459082715;  // Time of periapsis [JD]
    private static final double meanMotionRosettaC = 4.712872806981350E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRC = new double[]
            {axisRosettaC, eccentricityRosettaC, inclinationRosettaC, argPeriapsisRosettaC,
                    longNodeRosettaC, periapsisPassageRosettaC, meanMotionRosettaC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2009-11-13, Stop=2009-11-14, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2455148.500000000 = A.D. 2009-Nov-13 00:00:00.0000 TDB
     *  EC= 4.336173299046805E-01 QR= 9.096639367550371E-01 IN= 7.814844911187232E+00
     *  OM= 5.093751003400914E+01 W = 4.285406474133396E+01 Tp=  2455182.208213137928
     *  N = 4.842253729044619E-01 MA= 3.436776279233776E+02 TA= 3.167590290039723E+02
     *  A = 1.606094227074330E+00 AD= 2.302524517393623E+00 PR= 7.434554654595273E+02
     */
    private static final double axisRosettaD = 1.606094227074330E+00; // Semi-major axis [au]
    private static final double eccentricityRosettaD = 4.336173299046805E-01; // Eccentricity [-]
    private static final double inclinationRosettaD = 7.814844911187232E+00; // Inclination [degrees]
    private static final double argPeriapsisRosettaD = 4.285406474133396E+01; // Arg perifocus [degrees]
    private static final double longNodeRosettaD = 5.093751003400914E+01; // Long asc node [degrees]
    private static final double periapsisPassageRosettaD = 2455182.208213137928;  // Time of periapsis [JD]
    private static final double meanMotionRosettaD = 4.842253729044619E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRD = new double[]
            {axisRosettaD, eccentricityRosettaD, inclinationRosettaD, argPeriapsisRosettaD,
                    longNodeRosettaD, periapsisPassageRosettaD, meanMotionRosettaD};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rosetta (spacecraft) [-226]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=2014-06-01, Stop=2014-06-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2456809.500000000 = A.D. 2014-Jun-01 00:00:00.0000 TDB
     *  EC= 6.430939063750597E-01 QR= 1.179111235352668E+00 IN= 6.792085645587203E+00
     *  OM= 4.704733340031434E+01 W = 1.926939565745894E+01 Tp=  2457252.331993903033
     *  N = 1.641356244831843E-01 MA= 2.873154941396021E+02 TA= 2.175592186805966E+02
     *  A = 3.303701607828957E+00 AD= 5.428291980305246E+00 PR= 2.193308132427291E+03
     */
    private static final double axisRosettaE = 3.303701607828957E+00; // Semi-major axis [au]
    private static final double eccentricityRosettaE = 6.430939063750597E-01; // Eccentricity [-]
    private static final double inclinationRosettaE = 6.792085645587203E+00; // Inclination [degrees]
    private static final double argPeriapsisRosettaE = 1.926939565745894E+01; // Arg perifocus [degrees]
    private static final double longNodeRosettaE = 4.704733340031434E+01; // Long asc node [degrees]
    private static final double periapsisPassageRosettaE = 2457252.331993903033;  // Time of periapsis [JD]
    private static final double meanMotionRosettaE = 1.641356244831843E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRE = new double[]
            {axisRosettaE, eccentricityRosettaE, inclinationRosettaE, argPeriapsisRosettaE,
                    longNodeRosettaE, periapsisPassageRosettaE, meanMotionRosettaE};

    // Dates and orbital parameters
    private double[] orbitDates;
    private double[][] orbitPars;
    private List<GregorianCalendar> eventDateTimes;

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public Rosetta(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    /**
     * Read dates and orbital parameters from file.
     * @param fileName
     */
    private void readOrbitParametersFromFile(String fileName) {
        int nrRows = 0;
        int nrCols = 7;
        File file = new File(fileName);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNext("%")) {
                scanner.nextLine();
            }
            if (scanner.hasNextInt()) {
                nrRows = scanner.nextInt();
            }
            orbitDates = new double[nrRows];
            orbitPars = new double[nrRows][nrCols];
            int row = 0;
            while (row < nrRows && scanner.hasNextDouble()) {
                orbitDates[row] = scanner.nextDouble();
                int col = 0;
                while (col < nrCols && scanner.hasNextDouble()) {
                    orbitPars[row][col++] = scanner.nextDouble();
                }
                row++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found " + fileName);
        }
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {
        readOrbitParametersFromFile("EphemerisFiles/orbitParsRosetta.txt");
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        eventDateTimes = new ArrayList<>();

        // Define first trajectory from launch until first date
        // During launch Rosetta is so close that it starts orbiting the Earth
        GregorianCalendar firstDateTime = JulianDateConverter.convertJulianDateToCalendar(orbitDates[0]);
        trajectories.add(
                new SpacecraftTrajectory(launch, firstDateTime,"Sun", ORBITPARSLAUNCH));
        for (int i = 0; i < 10; i++) {
            GregorianCalendar dateTime = CalendarUtil.createGregorianCalendar(launch);
            dateTime.add(Calendar.HOUR,i);
            eventDateTimes.add(dateTime);
        }

        int rowStart = 0;
        while (rowStart + 1 < orbitPars.length) {
            GregorianCalendar startDateTime = JulianDateConverter.convertJulianDateToCalendar(orbitDates[rowStart]);
            GregorianCalendar stopDateTime = startDateTime;
            boolean stop = false;
            int rowStop = rowStart;
            while (rowStop + 1 < orbitDates.length && !stop) {
                rowStop++;
                stopDateTime = JulianDateConverter.convertJulianDateToCalendar(orbitDates[rowStop]);
                double[] orbitElementsStop = EphemerisUtil.computeOrbitalElements(orbitPars[rowStop],stopDateTime);
                Vector3D positionStop = EphemerisUtil.computePosition(orbitElementsStop);
                double[] orbitElementsPredicted = EphemerisUtil.computeOrbitalElements(orbitPars[rowStart],stopDateTime);
                Vector3D positionPredicted = EphemerisUtil.computePosition(orbitElementsPredicted);
                double deviation = positionPredicted.euclideanDistance(positionStop);
                stop = deviation > 1.0E7; // 10000 km
            }
            trajectories.add(
                    new SpacecraftTrajectory(startDateTime, stopDateTime,"Sun", orbitPars[rowStart]));
            eventDateTimes.add(startDateTime);
            rowStart = rowStop;
        }
        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        for (GregorianCalendar dateTime : eventDateTimes) {
            solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), dateTime));
        }
        // The orbit of 67P/Churyumov-Gerasimenko changes due to gravitational pull by Jupiter
        // Update position and velocity of 67P/Churyumov-Gerasimenko each year on May 28
        for (int year = 2004; year < 2015; year++) {
            Calendar dateTimeCG = new GregorianCalendar(year,4,28,0,0);
            solarSystem.addSpacecraftEvent(new SpacecraftEvent("67P/Churyumov-Gerasimenko",dateTimeCG));
        }
    }
}