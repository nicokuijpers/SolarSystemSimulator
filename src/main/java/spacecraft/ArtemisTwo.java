/*
 * Copyright (c) 2020 Nico Kuijpers and Marco Brassé
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

import application.SolarSystemException;
import ephemeris.*;
import jdk.nashorn.internal.runtime.regexp.joni.EncodingHelper;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class ArtemisTwo extends Spacecraft implements Serializable {

    /**
     * Artemis 2 was launched on April 1, 2026 from Kennedy Space Center. It was the first crewed
     * spacecraft to leave low Earth orbit and fly to the Moon since the Apollo programme.
     *
     * https://ssd.jpl.nasa.gov/horizons/app.html#/
     * Revised: Apr 10, 2026        Artemis II / Spacecraft (Earth)             -1024
     *           19:28 UTC       https://www.nasa.gov/mission/artemis-ii/
     *                          https://www.nasa.gov/artemis-ii-press-kit/
     *
     *  BACKGROUND:
     *   Four astronauts will fly aboard NASA's Orion 2 spacecraft to confirm the
     *   spacecraft's systems operate as designed in preparation for a sustained
     *   human presence on the Moon.
     *
     *   Artemis II astronauts are NASA's Reid Wiseman (Commander), Victor Glover
     *   (pilot), Christina Koch (Mission Specialist), and CSA (Canadian Space
     *   Agency) astronaut Jeremy Hansen (Mission Specialist).
     *
     *   "Artemis II" refers to a specific mission within the broader Artemis program.
     *   "Orion" refers to the spacecraft/capsule the astronauts ride within. The
     *     crew has chosen the nickname "Integrity" for the Artemis II Orion capsule.
     *   "SLS" refers to the Space Launch System, or rocket launching Orion into space
     *
     * MAJOR EVENTS:
     *   Launched April 1 @ 22:35:12 UTC from pad LC-39B at the Kennedy Space
     *   Center, USA.  Mission length is 9 days.
     *
     *   MET column below gives timings of major dynamical events RELATIVE TO LAUNCH.
     *   Format is "days/HH:MM:SS" since launch. Given launch time, MET converted to
     *   absolute UTC date/time is also given, format "date HH:MM:{SS}". As the
     *   mission develops, some times may differ somewhat from planning:
     *
     *   Day 1        MET     Apr  UTC
     *     launch   0/00:00    1 22:35:12 - Launch April 1, 22:35:12 UTC
     *     launch + 0/00:20    1 22:55    - Orion deploys solar arrays in Earth orbit
     *              0/00:49:50 1 23:25:02 - Perigee raise maneuver (2223 x 185 km)
     *              0/01:47:57 2 00:23:09 - Apogee raise maneuver  (70377 km)
     *              0/03:24:18 2 01:59:30 - Orion/ICPS separation
     *              0/04:50    2 03:25    - Orion upper stage separation burn
     *              0/05:00    2 03:35    - ICPS disposal burn for Pacific splashdown
     *              0/05:04    2 03:39    - Cubesats deploy at one minute intervals
     *              0/12:55    2 11:30    - Perigee raise burn
     *   Day 2
     *     launch + 1/01:14    2 23:49    - Start Translunar Injection burn (5m 55s)
     *              1/01:19    2 23:54    - End TLI (delta-v= 388 m/s)
     *              1/01:35    3 00:10    - Earth shadow entrance
     *              1/02:41    3 01:17    - Earth shadow exit
     *   Day 3
     *     launch + 2/00:08    3 22:43    - [CANCELLED] Trajectory correction burn #1
     *   Day 4
     *     launch + 3/01:08    4 23:43    - [CANCELLED] Trajectory correction burn #2
     *   Day 5
     *     launch + 4/04:28:05 6 03:03:17 - Begin trajectory correction burn #3 (OTC-3)
     *              4/04:28:23 6 03:03:35   End OTC-3 (length 18 sec, delta-v 3 m/s)
     *              4/07:03:32 6 05:38:44 - Orion enters lunar SOI, 62800 km from Moon
     *                                       center (Hill sphere definition)
     *   Day 6
     *     launch + 5/00:26    6 23:01    - Closest approach to Moon center (8282 km)
     *              5/00:30    6 23:05    - Maximum distance Earth center (413146.2 km)
     *   Day 7
     *     launch + 5/18:52    7 16:23    - Orion exits lunar SOI, 62800 km from Moon
     *                                       center (Hill sphere definition)
     *              6/01:28    8 00:03    - Return trajectory correction burn #1
     *                                       Total delta-v = 0.49 m/s, length 15 sec.
     *   Day 8
     *     launch + 7/04:20    9 02:55    - [CANCELLED] Manual piloting demonstration
     *                                       Oxidizer tank pressure characterization
     *   Day 9
     *     launch + 8/03:18   10 01:53    - Return trajectory correction burn #2
     *                                       Total delta-v = 1.62 m/s, length 9 sec.
     *   Day 10
     *     launch + 8/19:18   10 17:53    - Return trajectory correction burn #3
     *                                       Total delta-v = 1.28 m/s, length 8 sec.
     *              9/00:58   10 23:33    - Orion 2 crew and service module separation
     *              9/01:01   10 23:36    - Crew module raise burn
     *                                       (length 19 sec., total delta-v= 3.0 m/s)
     *              9/01:18   10 23:53    - Entry interface (122 km above Earth)
     *                                      Events by altitude:
     *                                       11 km : Forward bay cover deploys
     *                                       7.6 km: Droque parachute deploys
     *                                       2.9 km: Pilot parachutes deploy @ 2.9 km,
     *                                               pulling out 3 main parachutes
     *              9/01:32   11 00:07    - Splashdown, Pacific Ocean, off Baja Cali.
     *                                       2.5 hours before sunset
     *              9/01:47   11 00:22    - Orion 2 final power down
     *              9/02:17   11 00:52    - Astronaut egress complete
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch 1 April 2026 at 22:35:12 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(2026, 3, 1, 22, 35, 12);

    // Correction earth orbit (first valid data from HORIZONS) on 2 April at 02:00 UTC
    private static final GregorianCalendar correctionEarthOrbit =
            new GregorianCalendar(2026, 3, 2, 0, 0, 0);

    // Correction after Perigee Raise Burn on 2 April at 11:30 UTC
    private static final GregorianCalendar perigeeRaiseBurn =
            new GregorianCalendar(2026, 3, 2, 11, 40, 0);

    // Correction after Translunar Injection Burn on 2 April 2026 from 23:49 UTC till 23.54 UTC
    private static final GregorianCalendar transLunarInjectionBurn =
            new GregorianCalendar(2026, 3, 3, 0, 0, 0);

    // Correction after Orbital Trajectory Correction Burn #3 on 6 April 2026 from 03:03:17 UTC till 03:03:35 UTC
    private static final GregorianCalendar orbitalTrajectoryCorrectionBurnThree =
            new GregorianCalendar(2026, 3, 6, 3, 10, 0);

    // Correction after Return Trajectory Correction Burn #1 on 8 April 2026 at 00:03
    private static final GregorianCalendar returnTrajectoryCorrectionBurnOne =
            new GregorianCalendar(2026, 3, 8, 0, 10, 0);

    // Correction after Return Trajectory Correction Burn #2 on 10 April 2026 at 01:53
    private static final GregorianCalendar returnTrajectoryCorrectionBurnTwo =
            new GregorianCalendar(2026, 3, 10, 2, 0, 0);

    // Correction after Return Trajectory Correction Burn #3 on 10 April 2026 at 17:53
    private static final GregorianCalendar returnTrajectoryCorrectionBurnThree =
            new GregorianCalendar(2026, 3, 10, 18, 0, 0);

    // Extra correction during reentry on 10 April 2026 at 22.00
    private static final GregorianCalendar correctionReentryA =
            new GregorianCalendar(2026, 3, 10, 22, 0, 0);

    // Extra correction during reentry on 10 April 2026 at 23.00
    private static final GregorianCalendar correctionReentryB =
            new GregorianCalendar(2026, 3, 10, 23, 0, 0);

    // Extra correction during reentry on 10 April 2026 at 23.30
    private static final GregorianCalendar correctionReentryC =
            new GregorianCalendar(2026, 3, 10, 23, 30, 0);

    // Extra correction during reentry on 10 April 2026 at 23.40
    private static final GregorianCalendar correctionReentryD =
            new GregorianCalendar(2026, 3, 10, 23, 40, 0);

    // Extra correction during reentry on 10 April 2026 at 23.50
    private static final GregorianCalendar correctionReentryE =
            new GregorianCalendar(2026, 3, 10, 23, 50, 0);

    // Splashdown 11 April 2026 at 00:07 UTC
    private static final GregorianCalendar splashdown =
            new GregorianCalendar(2026, 3, 11, 00, 0, 0);

    /**
     * https://ssd.jpl.nasa.gov/horizons/app.html#/
     * Results from HORIZONS
     *
     * *******************************************************************************
     * Ephemeris / WWW_USER Sat Apr 11 09:15:57 2026 Pasadena, USA      / Horizons
     * *******************************************************************************
     * Target body name: Artemis II (spacecraft) (-1024) {source: Artemis_II_merged}
     * Center body name: Earth (399)                     {source: DE441}
     * Center-site name: BODY CENTER
     * *******************************************************************************
     * Start time      : A.D. 2026-Apr-06 03:00:00.0000 TDB
     * Stop  time      : A.D. 2026-Apr-06 03:30:00.0000 TDB
     * Step-size       : 5 minutes
     * *******************************************************************************
     * Center geodetic : 0.0, 0.0, 0.0                   {E-lon(deg),Lat(deg),Alt(km)}
     * Center cylindric: 0.0, 0.0, 0.0                   {E-lon(deg),Dxy(km),Dz(km)}
     * Center radii    : 6378.137, 6378.137, 6356.752 km {Equator_a, b, pole_c}
     * Keplerian GM    : 3.9860043550702260E+05 km^3/s^2
     * Output units    : KM-S, deg, Julian Day Number (Tp)
     * Calendar mode   : Mixed Julian/Gregorian
     * Output type     : GEOMETRIC osculating elements
     * Output format   : 10
     * EOP file        : eop.260410.p260707
     * EOP coverage    : DATA-BASED 1962-JAN-20 TO 2026-APR-10. PREDICTS-> 2026-JUL-06
     * Reference frame : Ecliptic of J2000.0
     * *******************************************************************************
     * JDTDB
     *    EC    QR   IN
     *    OM    W    Tp
     *    N     MA   TA
     *    A     AD   PR
     * *******************************************************************************
     * Symbol meaning:
     * JDTDB    Julian Day Number, Barycentric Dynamical Time
     * EC     Eccentricity, e
     * QR     Periapsis distance, q (km)
     * IN     Inclination w.r.t X-Y plane, i (degrees)
     * OM     Longitude of Ascending Node, OMEGA, (degrees)
     * W      Argument of Perifocus, w (degrees)
     * Tp     Time of periapsis (Julian Day Number)
     * N      Mean motion, n (degrees/sec)
     * MA     Mean anomaly, M (degrees)
     * TA     True anomaly, nu (degrees)
     * A      Semi-major axis, a (km)
     * AD     Apoapsis distance (km)
     * PR     Sidereal orbit period (sec)
     */

    /**
     * Correction earth orbit (first valid data from HORIZONS) on 2 April at 02:00 UTC
     * 2461132.583333333 = A.D. 2026-Apr-02 02:00:00.0000 TDB
     *  EC= 8.464822646577275E-01 QR= 6.379747003855852E+03 IN= 4.902317411330654E+00
     *  OM= 3.584658115413244E+02 W = 7.831786456150894E+01 Tp=  2461132.519955520984
     *  N = 4.269963519716327E-03 MA= 2.338164984475578E+01 TA= 1.341036440585212E+02
     *  A = 4.155706824121664E+04 AD= 7.673438947857743E+04 PR= 8.430985378158838E+04
     */
    private static final double axisArtemisTwoEarthOrbitKm = 4.155706824121664E+04; // Semi-major axis [km]
    private static final double axisArtemisTwoEarthOrbitAU = (1.0E3*axisArtemisTwoEarthOrbitKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoEarthOrbit = 8.464822646577275E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoEarthOrbit = 4.902317411330654E+00; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoEarthOrbit = 7.831786456150894E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoEarthOrbit = 3.584658115413244E+02; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoEarthOrbit = 2461132.519955520984;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoEarthOrbitDegSec = 4.269963519716327E-03; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoEarthOrbitDegDay = 24*60*60*meanMotionArtemisTwoEarthOrbitDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOEARTHORBIT = new double[]
            {axisArtemisTwoEarthOrbitAU, eccentricityArtemisTwoEarthOrbit, inclinationArtemisTwoEarthOrbit,
                    argPeriapsisArtemisTwoEarthOrbit, longNodeArtemisTwoEarthOrbit,
                    periapsisPassageArtemisTwoEarthOrbit, meanMotionArtemisTwoEarthOrbitDegDay};

    /**
     * Correction after Perigee Raise Burn on 2 April 2026 at 11.30
     * 2461132.979166667 = A.D. 2026-Apr-02 11:30:00.0000 TDB
     *  EC= 8.436802677906320E-01 QR= 6.488118767918359E+03 IN= 4.923078893720360E+00
     *  OM= 3.587000640165174E+02 W = 7.831839473864194E+01 Tp=  2461132.519452949520
     *  N = 4.277933542857628E-03 MA= 1.699163767306303E+02 TA= 1.784052240530535E+02
     *  A = 4.150543681349485E+04 AD= 7.652275485907133E+04 PR= 8.415277993297731E+04
     * 2461132.982638889 = A.D. 2026-Apr-02 11:35:00.0000 TDB
     *  EC= 8.436815133510210E-01 QR= 6.488062426841033E+03 IN= 4.923077066204092E+00
     *  OM= 3.586999730423151E+02 W = 7.831846194747901E+01 Tp=  2461132.519453172572
     *  N = 4.277938135476552E-03 MA= 1.711998582127571E+02 TA= 1.786086823902132E+02
     *  A = 4.150540710779978E+04 AD= 7.652275178875853E+04 PR= 8.415268959000429E+04
     * 2461132.986111111 = A.D. 2026-Apr-02 11:40:00.0000 TDB
     *  EC= 8.436827541855899E-01 QR= 6.488006332709936E+03 IN= 4.923075261931619E+00
     *  OM= 3.586998817659967E+02 W = 7.831853034928008E+01 Tp=  2461132.519453390501
     *  N = 4.277942677934236E-03 MA= 1.724833421011313E+02 TA= 1.788119501809927E+02
     *  A = 4.150537772660684E+04 AD= 7.652274912050374E+04 PR= 8.415260023396091E+04
     */
    private static final double axisArtemisTwoPRBKm = 4.150537772660684E+04; // Semi-major axis [km]
    private static final double axisArtemisTwoPRBAU = (1.0E3*axisArtemisTwoPRBKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoPRB = 8.436827541855899E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoPRB= 4.923075261931619E+00; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoPRB = 7.831853034928008E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoPRB = 3.586998817659967E+02; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoPRB = 2461132.519453390501;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoPRBDegSec = 4.277942677934236E-03; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoPRBDegDay = 24*60*60*meanMotionArtemisTwoPRBDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOPRB = new double[]
            {axisArtemisTwoPRBAU, eccentricityArtemisTwoPRB, inclinationArtemisTwoPRB,
                    argPeriapsisArtemisTwoPRB, longNodeArtemisTwoPRB,
                    periapsisPassageArtemisTwoPRB, meanMotionArtemisTwoPRBDegDay};

    /**
     * Correction after Translunar Injection Burn on 2 April 2026 from 23:49 UTC till 23.54 UTC
     * 2461133.500000000 = A.D. 2026-Apr-03 00:00:00.0000 TDB
     *  EC= 9.717206065908000E-01 QR= 6.572276777214271E+03 IN= 5.002371406898455E+00
     *  OM= 3.483541664749471E+02 W = 8.959594223927053E+01 Tp=  2461133.493934918195
     *  N = 3.228664247074402E-04 MA= 1.691894539182952E-01 TA= 4.489895682695094E+01
     *  A = 2.324051538911732E+05 AD= 4.582380310051322E+05 PR= 1.115012192197463E+06
     */
    private static final double axisArtemisTwoTLIKm = 2.324051538911732E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoTLIAU = (1.0E3*axisArtemisTwoTLIKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoTLI = 9.717206065908000E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoTLI= 5.002371406898455E+00; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoTLI = 8.959594223927053E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoTLI = 3.483541664749471E+02; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoTLI = 2461133.493934918195;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoTLIDegSec = 3.228664247074402E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoTLIDegDay = 24*60*60*meanMotionArtemisTwoTLIDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOTLI = new double[]
            {axisArtemisTwoTLIAU, eccentricityArtemisTwoTLI, inclinationArtemisTwoTLI,
                    argPeriapsisArtemisTwoTLI, longNodeArtemisTwoTLI,
                    periapsisPassageArtemisTwoTLI, meanMotionArtemisTwoTLIDegDay};

    /**
     * Correction after Orbital Trajectory Correction Burn #3 on 6 April 2026 from 03:03:17 UTC till 03:03:35 UTC
     * 2461136.631944444 = A.D. 2026-Apr-06 03:10:00.0000 TDB
     *  EC= 9.837364457423720E-01 QR= 3.760437376324105E+03 IN= 5.020406926733377E+00
     *  OM= 3.468097491328465E+02 W = 8.917244352044980E+01 Tp=  2461133.541656893212
     *  N = 3.253547639711695E-04 MA= 8.686999671736325E+01 TA= 1.751677796159924E+02
     *  A = 2.312186694713652E+05 AD= 4.586769015664063E+05 PR= 1.106484489748859E+06
     */
    private static final double axisArtemisTwoOTCThreeKm = 2.312186694713652E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoOTCThreeAU = (1.0E3*axisArtemisTwoOTCThreeKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoOTCThree = 9.837364457423720E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoOTCThree = 5.020406926733377E+00; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoOTCThree = 8.917244352044980E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoOTCThree = 3.468097491328465E+02; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoOTCThree = 2461133.541656893212;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoOTCThreeDegSec = 3.253547639711695E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoOTCThreeDegDay = 24*60*60*meanMotionArtemisTwoOTCThreeDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOOTCTHREE = new double[]
            {axisArtemisTwoOTCThreeAU, eccentricityArtemisTwoOTCThree, inclinationArtemisTwoOTCThree,
                    argPeriapsisArtemisTwoOTCThree, longNodeArtemisTwoOTCThree,
                    periapsisPassageArtemisTwoOTCThree, meanMotionArtemisTwoOTCThreeDegDay};

    /**
     * 2461138.506944444 = A.D. 2026-Apr-08 00:10:00.0000 TDB
     *  EC= 9.790636336211950E-01 QR= 4.785909944798032E+03 IN= 2.362112278863607E+01
     *  OM= 5.811720093393955E+01 W = 9.252448624730494E+00 Tp=  2461141.468477439135
     *  N = 3.309761485004400E-04 MA= 2.753109978452695E+02 TA= 1.856732287133516E+02
     *  A = 2.285931502251045E+05 AD= 4.524003905054110E+05 PR= 1.087691670928733E+06
     */
    private static final double axisArtemisTwoReturnOneKm = 2.285931502251045E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReturnOneAU = (1.0E3*axisArtemisTwoReturnOneKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReturnOne = 9.790636336211950E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReturnOne = 2.362112278863607E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReturnOne = 9.252448624730494E+00; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReturnOne = 5.811720093393955E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReturnOne = 2461141.468477439135;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReturnOneDegSec = 3.309761485004400E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReturnOneDegDay = 24*60*60*meanMotionArtemisTwoReturnOneDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWORETURNONE = new double[]
            {axisArtemisTwoReturnOneAU, eccentricityArtemisTwoReturnOne, inclinationArtemisTwoReturnOne,
                    argPeriapsisArtemisTwoReturnOne, longNodeArtemisTwoReturnOne,
                    periapsisPassageArtemisTwoReturnOne, meanMotionArtemisTwoReturnOneDegDay};

    /**
     * 2461140.583333333 = A.D. 2026-Apr-10 02:00:00.0000 TDB
     *  EC= 9.717660911586257E-01 QR= 6.425059602305448E+03 IN= 1.984663295830503E+01
     *  OM= 5.479376207837097E+01 W = 1.126867251302425E+01 Tp=  2461141.497665585484
     *  N = 3.332209421317024E-04 MA= 3.336761098515436E+02 TA= 1.958481207416036E+02
     *  A = 2.275653590299216E+05 AD= 4.487056584575376E+05 PR= 1.080364270315620E+06
     */
    private static final double axisArtemisTwoReturnTwoKm = 2.275653590299216E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReturnTwoAU = (1.0E3*axisArtemisTwoReturnTwoKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReturnTwo = 9.717660911586257E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReturnTwo = 1.984663295830503E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReturnTwo = 1.126867251302425E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReturnTwo = 5.479376207837097E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReturnTwo = 2461141.497665585484;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReturnTwoDegSec = 3.332209421317024E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReturnTwoDegDay = 24*60*60*meanMotionArtemisTwoReturnTwoDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWORETURNTWO = new double[]
            {axisArtemisTwoReturnTwoAU, eccentricityArtemisTwoReturnTwo, inclinationArtemisTwoReturnTwo,
                    argPeriapsisArtemisTwoReturnTwo, longNodeArtemisTwoReturnTwo,
                    periapsisPassageArtemisTwoReturnTwo, meanMotionArtemisTwoReturnTwoDegDay};

    /**
     * 2461141.250000000 = A.D. 2026-Apr-10 18:00:00.0000 TDB
     *  EC= 9.717874174847012E-01 QR= 6.410449090051092E+03 IN= 1.965381255745188E+01
     *  OM= 5.448039279790372E+01 W = 1.158346113829811E+01 Tp=  2461141.497676557396
     *  N = 3.339820276565422E-04 MA= 3.528530335776188E+02 TA= 2.091159245160468E+02
     *  A = 2.272195069903640E+05 AD= 4.480285648906769E+05 PR= 1.077902312666399E+06
     */
    private static final double axisArtemisTwoReturnThreeKm = 2.272195069903640E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReturnThreeAU = (1.0E3*axisArtemisTwoReturnThreeKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReturnThree = 9.717874174847012E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReturnThree = 1.965381255745188E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReturnThree = 1.158346113829811E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReturnThree = 5.448039279790372E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReturnThree = 2461141.497676557396;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReturnThreeDegSec = 3.339820276565422E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReturnThreeDegDay = 24*60*60*meanMotionArtemisTwoReturnThreeDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWORETURNTHREE = new double[]
            {axisArtemisTwoReturnThreeAU, eccentricityArtemisTwoReturnThree, inclinationArtemisTwoReturnThree,
                    argPeriapsisArtemisTwoReturnThree, longNodeArtemisTwoReturnThree,
                    periapsisPassageArtemisTwoReturnThree, meanMotionArtemisTwoReturnThreeDegDay};

    /**
     * 2461141.416666667 = A.D. 2026-Apr-10 22:00:00.0000 TDB
     *  EC= 9.717265902398176E-01 QR= 6.425073009884108E+03 IN= 1.968536975615348E+01
     *  OM= 5.456572591057085E+01 W = 1.146555377306498E+01 Tp=  2461141.497724520974
     *  N = 3.339194353408541E-04 MA= 3.576614290801544E+02 TA= 2.265931052635781E+02
     *  A = 2.272479005674269E+05 AD= 4.480707281249698E+05 PR= 1.078104362606279E+06
     */
    private static final double axisArtemisTwoReentryAKm = 2.272479005674269E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReentryAAU = (1.0E3*axisArtemisTwoReentryAKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReentryA = 9.717265902398176E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReentryA = 1.968536975615348E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReentryA = 1.146555377306498E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReentryA = 5.456572591057085E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReentryA = 2461141.497724520974;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReentryADegSec = 3.339194353408541E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReentryADegDay = 24*60*60*meanMotionArtemisTwoReentryADegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOREENTRYA = new double[]
            {axisArtemisTwoReentryAAU, eccentricityArtemisTwoReentryA, inclinationArtemisTwoReentryA,
                    argPeriapsisArtemisTwoReentryA, longNodeArtemisTwoReentryA,
                    periapsisPassageArtemisTwoReentryA, meanMotionArtemisTwoReentryADegDay};

    /**
     * 2461141.458333333 = A.D. 2026-Apr-10 23:00:00.0000 TDB
     *  EC= 9.717240076420318E-01 QR= 6.425172284694524E+03 IN= 1.968414305009588E+01
     *  OM= 5.455716632362972E+01 W = 1.147326027158505E+01 Tp=  2461141.497724812943
     *  N = 3.339574484661732E-04 MA= 3.588634012653277E+02 TA= 2.434397882637283E+02
     *  A = 2.272306557221126E+05 AD= 4.480361391595306E+05 PR= 1.077981646025376E+06
     */
    private static final double axisArtemisTwoReentryBKm = 2.272306557221126E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReentryBAU = (1.0E3*axisArtemisTwoReentryBKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReentryB = 9.717240076420318E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReentryB = 1.968414305009588E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReentryB = 1.147326027158505E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReentryB = 5.455716632362972E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReentryB = 2461141.497724812943;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReentryBDegSec = 3.339574484661732E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReentryBDegDay = 24*60*60*meanMotionArtemisTwoReentryBDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOREENTRYB = new double[]
            {axisArtemisTwoReentryBAU, eccentricityArtemisTwoReentryB, inclinationArtemisTwoReentryB,
                    argPeriapsisArtemisTwoReentryB, longNodeArtemisTwoReentryB,
                    periapsisPassageArtemisTwoReentryB, meanMotionArtemisTwoReentryBDegDay};

    /**
     * 2461141.479166667 = A.D. 2026-Apr-10 23:30:00.0000 TDB
     *  EC= 9.716738647269094E-01 QR= 6.425068658047846E+03 IN= 1.968390487421493E+01
     *  OM= 5.453785179905719E+01 W = 1.149437867504463E+01 Tp=  2461141.497725382447
     *  N = 3.348542726187754E-04 MA= 3.594630701994339E+02 TA= 2.682314514708600E+02
     *  A = 2.268247537514099E+05 AD= 4.472244388447719E+05 PR= 1.075094539438213E+06
     */
    private static final double axisArtemisTwoReentryCKm = 2.268247537514099E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReentryCAU = (1.0E3*axisArtemisTwoReentryCKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReentryC = 9.716738647269094E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReentryC = 1.968390487421493E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReentryC = 1.149437867504463E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReentryC = 5.453785179905719E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReentryC = 2461141.497725382447;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReentryCDegSec = 3.348542726187754E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReentryCDegDay = 24*60*60*meanMotionArtemisTwoReentryCDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOREENTRYC = new double[]
            {axisArtemisTwoReentryCAU, eccentricityArtemisTwoReentryC, inclinationArtemisTwoReentryC,
                    argPeriapsisArtemisTwoReentryC, longNodeArtemisTwoReentryC,
                    periapsisPassageArtemisTwoReentryC, meanMotionArtemisTwoReentryCDegDay};

    /**
     * 2461141.486111111 = A.D. 2026-Apr-10 23:40:00.0000 TDB
     *  EC= 9.723031384765733E-01 QR= 6.429664408302925E+03 IN= 1.968565949337211E+01
     *  OM= 5.452277716868670E+01 W = 1.144130160029900E+01 Tp=  2461141.497718014289
     *  N = 3.234110821857209E-04 MA= 3.596756715836968E+02 TA= 2.873749995617287E+02
     *  A = 2.321441511654509E+05 AD= 4.578586379225989E+05 PR= 1.113134397148666E+06
     */
    private static final double axisArtemisTwoReentryDKm = 2.321441511654509E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReentryDAU = (1.0E3*axisArtemisTwoReentryDKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReentryD = 9.723031384765733E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReentryD = 1.968565949337211E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReentryD = 1.144130160029900E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReentryD = 5.452277716868670E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReentryD = 2461141.497718014289;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReentryDDegSec = 3.234110821857209E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReentryDDegDay = 24*60*60*meanMotionArtemisTwoReentryDDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOREENTRYD = new double[]
            {axisArtemisTwoReentryDAU, eccentricityArtemisTwoReentryD, inclinationArtemisTwoReentryD,
                    argPeriapsisArtemisTwoReentryD, longNodeArtemisTwoReentryD,
                    periapsisPassageArtemisTwoReentryD, meanMotionArtemisTwoReentryDDegDay};

    /**
     * 2461141.493055556 = A.D. 2026-Apr-10 23:50:00.0000 TDB
     *  EC= 9.728281190407529E-01 QR= 6.428156746196935E+03 IN= 1.968813720459143E+01
     *  OM= 5.451330940816940E+01 W = 1.146939365354010E+01 Tp=  2461141.497718166560
     *  N = 3.143702094697593E-04 MA= 3.598733560825132E+02 TA= 3.230130690932063E+02
     *  A = 2.365738594187848E+05 AD= 4.667195620913727E+05 PR= 1.145146674703063E+06
     */
    private static final double axisArtemisTwoReentryEKm = 2.365738594187848E+05; // Semi-major axis [km]
    private static final double axisArtemisTwoReentryEAU = (1.0E3*axisArtemisTwoReentryEKm)/SolarSystemParameters.ASTRONOMICALUNIT; // Semi-major axis [au]
    private static final double eccentricityArtemisTwoReentryE = 9.728281190407529E-01; // Eccentricity [-]
    private static final double inclinationArtemisTwoReentryE = 1.968813720459143E+01; // Inclination [degrees]
    private static final double argPeriapsisArtemisTwoReentryE = 1.146939365354010E+01; // Arg perifocus [degrees]
    private static final double longNodeArtemisTwoReentryE = 5.451330940816940E+01; // Long asc node [degrees]
    private static final double periapsisPassageArtemisTwoReentryE = 2461141.497718166560;  // Time of periapsis [JD]
    private static final double meanMotionArtemisTwoReentryEDegSec = 3.143702094697593E-04; // Mean motion [degrees/sec]
    private static final double meanMotionArtemisTwoReentryEDegDay = 24*60*60*meanMotionArtemisTwoReentryEDegSec; // Mean motion [degrees/day]
    private static final double[] ORBITPARSARTEMISTWOREENTRYE = new double[]
            {axisArtemisTwoReentryEAU, eccentricityArtemisTwoReentryE, inclinationArtemisTwoReentryE,
                    argPeriapsisArtemisTwoReentryE, longNodeArtemisTwoReentryE,
                    periapsisPassageArtemisTwoReentryE, meanMotionArtemisTwoReentryEDegDay};

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public ArtemisTwo(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {

        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        trajectories.add(
                new SpacecraftTrajectory(launch, correctionEarthOrbit,
                        "Earth", ORBITPARSARTEMISTWOEARTHORBIT));
        trajectories.add(
                new SpacecraftTrajectory(correctionEarthOrbit, perigeeRaiseBurn,
                        "Earth", ORBITPARSARTEMISTWOEARTHORBIT));
        trajectories.add(
                new SpacecraftTrajectory(perigeeRaiseBurn, transLunarInjectionBurn,
                        "Earth", ORBITPARSARTEMISTWOPRB));
        trajectories.add(
                new SpacecraftTrajectory(transLunarInjectionBurn, orbitalTrajectoryCorrectionBurnThree,
                        "Earth", ORBITPARSARTEMISTWOTLI));
        trajectories.add(
                new SpacecraftTrajectory(orbitalTrajectoryCorrectionBurnThree, returnTrajectoryCorrectionBurnOne,
                        "Earth", ORBITPARSARTEMISTWOOTCTHREE));
        trajectories.add(
                new SpacecraftTrajectory(returnTrajectoryCorrectionBurnOne, returnTrajectoryCorrectionBurnTwo,
                        "Earth", ORBITPARSARTEMISTWORETURNONE));
        trajectories.add(
                new SpacecraftTrajectory(returnTrajectoryCorrectionBurnTwo, returnTrajectoryCorrectionBurnThree,
                        "Earth", ORBITPARSARTEMISTWORETURNTWO));
        trajectories.add(
                new SpacecraftTrajectory(returnTrajectoryCorrectionBurnThree, correctionReentryA,
                        "Earth", ORBITPARSARTEMISTWORETURNTHREE));
        trajectories.add(
                new SpacecraftTrajectory(correctionReentryA, correctionReentryB,
                        "Earth", ORBITPARSARTEMISTWOREENTRYA));
        trajectories.add(
                new SpacecraftTrajectory(correctionReentryB, correctionReentryC,
                        "Earth", ORBITPARSARTEMISTWOREENTRYB));
        trajectories.add(
                new SpacecraftTrajectory(correctionReentryC, correctionReentryD,
                        "Earth", ORBITPARSARTEMISTWOREENTRYC));
        trajectories.add(
                new SpacecraftTrajectory(correctionReentryD, correctionReentryE,
                        "Earth", ORBITPARSARTEMISTWOREENTRYD));
        trajectories.add(
                new SpacecraftTrajectory(correctionReentryE, splashdown,
                        "Earth", ORBITPARSARTEMISTWOREENTRYE));

        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionEarthOrbit)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(perigeeRaiseBurn)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(transLunarInjectionBurn)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(orbitalTrajectoryCorrectionBurnThree)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(returnTrajectoryCorrectionBurnOne)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(returnTrajectoryCorrectionBurnTwo)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(returnTrajectoryCorrectionBurnThree)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionReentryA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionReentryB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionReentryC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionReentryD)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionReentryE)));
    }
}