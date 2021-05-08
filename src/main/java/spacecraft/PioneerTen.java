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

public class PioneerTen extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/Pioneer_10
     * Pioneer 10 was launched by NASA on March 2, 1972. It was the first spacecraft to complete
     * a mission to the planet Jupiter. It was also the first spacecraft to achieve the escape velocity
     * needed to leave the Solar System.
     *
     * Launch: March 3, 1972 at 01:49:00 UTC
     *
     * Jupiter observation phase:
     * 1973-12-03 Encounter with Jovian system
     * 12:26:00 Callisto flyby at 1,392,300 km
     * 13:56:00 Ganymede flyby at 446,250 km
     * 19:26:00 Europa flyby at 321,000 km
     * 22:56:00 Io flyby at 357,000 km
     * 1973-12-04
     * 02:26:00 Jupiter closest approach at 200,000 km
     * 02:36:00 Jupiter equator plane crossing
     * 02:41:45 Io occultation entry
     * 02:43:16 Io occultation exit
     * 03:42:25 Jupiter occultation entry
     * 03:42:25 Jupiter shadow entry
     * 04:15:35 Jupiter occultation exit
     * 04:47:21 Jupiter shadow exit
     * 1974-01-01 Phase stop
     *
     * https://solarsystem.nasa.gov/missions/pioneer-11/in-depth/
     * After launch, Pioneer 10 reached a maximum escape velocity of 51,682 kilometers per hour, faster than
     * any previous human-made object at that point in time.
     *
     * Controllers carried out two course corrections, on March 7 and March 26, the latter to ensure an
     * occultation experiment with Jupiter’s moon Io.
     *
     * Pioneer 10’s closest approach to Jupiter was at 02:26 UT Dec. 4, 1973, when the spacecraft raced by
     * the planet at a range of 130,354 kilometers at a velocity of approximately 126,000 kilometers/hour.
     *
     * https://solarsystem.nasa.gov/missions/pioneer-10/in-depth/
     * Key Dates
     * March 2, 1972: Launch
     * July 15, 1972: Spacecraft entered the asteroid belt
     * Dec. 4, 1973: Pioneer 10’s closest approach to Jupiter
     * Feb. 1976: Pioneer crossed Saturn’s orbit
     * June 13, 1983: Pioneer 10 crossed the orbit of Neptune
     * March 31, 1997: Routine contact with spacecraft terminated
     * Jan. 23, 2003: Pioneer 10's last signal is received on Earth
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch March 3, 1972 at 01:49:00 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(1972, 2, 3, 1, 49, 0);

    // Schedule simulated correction on March 8, 1972, 0:00 UTC after course correction on March 7, 1972
    private static final GregorianCalendar correctionA =
            new GregorianCalendar(1972, 2, 8, 0, 0, 0);

    // Schedule simulated correction on March 27, 1972, 0:00 UTC after course correction on March 26, 1972
    private static final GregorianCalendar correctionB =
            new GregorianCalendar(1972, 2, 27, 0, 0, 0);

    // Update position and velocity of the moons of Jupiter on November 20, 1973 (about two weeks before fly by)
    private static final GregorianCalendar updateJupiterMoons =
            new GregorianCalendar(1973, 10, 20, 0, 0, 0);

    // Schedule simulated correction on November 21, 1973 (less than two weeks before fly by)
    private static final GregorianCalendar correctionC =
            new GregorianCalendar(1973, 10, 21, 0, 0, 0);

    // Jupiter fly by December 4, 1973, 02:26:00 UTC
    private static final GregorianCalendar jupiterFlyBy =
            new GregorianCalendar(1973, 11, 4, 2, 26, 0);

    // Schedule simulated correction on January 2, 1974, 0:00 UTC after Jupiter observation phase
    private static final GregorianCalendar correctionD =
            new GregorianCalendar(1974, 0, 2, 0, 0, 0);

    // Routine contact with Pioneer 11 was terminated on March 31, 1997
    private static final GregorianCalendar deactivated =
            new GregorianCalendar(1997, 2, 31, 0, 0, 0);


    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 10 (spacecraft) [-23]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1972-03-08, Stop=1972-03-09, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2441384.500000000 = A.D. 1972-Mar-08 00:00:00.0000 TDB
     *  EC= 7.110938592599001E-01 QR= 9.910019653881508E-01 IN= 2.081239697796044E+00
     *  OM= 3.429845067153262E+02 W = 1.772000011647415E+02 Tp=  2441377.428983957972
     *  N = 1.551413119056430E-01 MA= 1.097006705243678E+00 TA= 9.207772300165008E+00
     *  A = 3.430186574952936E+00 AD= 5.869371184517722E+00 PR= 2.320465100997420E+03
     */
    private static final double axisPioneerTenA = 3.430186574952936E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerTenA = 7.110938592599001E-01; // Eccentricity [-]
    private static final double inclinationPioneerTenA = 2.081239697796044E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerTenA = 1.772000011647415E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerTenA = 3.429845067153262E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerTenA = 2441377.428983957972;  // Time of periapsis [JD]
    private static final double meanMotionPioneerTenA = 1.551413119056430E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRA = new double[]
            {axisPioneerTenA, eccentricityPioneerTenA, inclinationPioneerTenA, argPeriapsisPioneerTenA,
                    longNodePioneerTenA, periapsisPassagePioneerTenA, meanMotionPioneerTenA};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 10 (spacecraft) [-23]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1972-03-27, Stop=1972-03-28, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2441403.500000000 = A.D. 1972-Mar-27 00:00:00.0000 TDB
     *  EC= 7.106396948324378E-01 QR= 9.910274530278851E-01 IN= 2.082952231006771E+00
     *  OM= 3.430253236570905E+02 W = 1.771647718961637E+02 Tp=  2441377.430485767778
     *  N = 1.555012829947329E-01 MA= 4.053842910184960E+00 TA= 3.254290919089281E+01
     *  A = 3.424890820646606E+00 AD= 5.858754188265326E+00 PR= 2.315093438889464E+03
     */
    private static final double axisPioneerTenB = 3.424890820646606E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerTenB = 7.106396948324378E-01; // Eccentricity [-]
    private static final double inclinationPioneerTenB = 2.082952231006771E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerTenB = 1.771647718961637E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerTenB = 3.430253236570905E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerTenB = 2441377.430485767778;  // Time of periapsis [JD]
    private static final double meanMotionPioneerTenB = 1.555012829947329E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRB = new double[]
            {axisPioneerTenB, eccentricityPioneerTenB, inclinationPioneerTenB, argPeriapsisPioneerTenB,
                    longNodePioneerTenB, periapsisPassagePioneerTenB, meanMotionPioneerTenB};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 10 (spacecraft) [-23]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1973-11-21, Stop=1973-11-22, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2442007.500000000 = A.D. 1973-Nov-21 00:00:00.0000 TDB
     *  EC= 7.630447891370403E-01 QR= 8.189660862373915E-01 IN= 2.726219564797335E+00
     *  OM= 3.363691047719717E+02 W = 1.830146635482161E+02 Tp=  2441420.807524955366
     *  N = 1.533926649621452E-01 MA= 8.999432226034300E+01 TA= 1.586420581200670E+02
     *  A = 3.456206273138391E+00 AD= 6.093446460039390E+00 PR= 2.346917957836134E+03
     */
    private static final double axisPioneerTenC = 3.456206273138391E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerTenC = 7.630447891370403E-01; // Eccentricity [-]
    private static final double inclinationPioneerTenC = 2.726219564797335E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerTenC = 1.830146635482161E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerTenC = 3.363691047719717E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerTenC = 2441420.807524955366;  // Time of periapsis [JD]
    private static final double meanMotionPioneerTenC = 1.533926649621452E-01; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRC = new double[]
            {axisPioneerTenC, eccentricityPioneerTenC, inclinationPioneerTenC, argPeriapsisPioneerTenC,
                    longNodePioneerTenC, periapsisPassagePioneerTenC, meanMotionPioneerTenC};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Pioneer 10 (spacecraft) [-23]
     * Center [change]            : Sun (body center) [500@10]
     * Time Span [change]         : Start=1974-01-02, Stop=1974-01-03, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * 2442049.500000000 = A.D. 1974-Jan-02 00:00:00.0000 TDB
     *  EC= 1.867977909345460E+00 QR= 5.059877624148231E+00 IN= 3.301928295651899E+00
     *  OM= 3.321246129850354E+02 W = 3.466764282535549E+02 Tp=  2442020.688658506609
     *  N = 7.002563604670944E-02 MA= 2.017532513430378E+00 TA= 4.220204729318017E+00
     *  A =-5.829500462706318E+00 AD= 9.999999999999998E+99 PR= 9.999999999999998E+99
     */
    private static final double axisPioneerTenD = -5.829500462706318E+00; // Semi-major axis [au]
    private static final double eccentricityPioneerTenD = 1.867977909345460E+00; // Eccentricity [-]
    private static final double inclinationPioneerTenD = 3.301928295651899E+00; // Inclination [degrees]
    private static final double argPeriapsisPioneerTenD = 3.466764282535549E+02; // Arg perifocus [degrees]
    private static final double longNodePioneerTenD = 3.321246129850354E+02; // Long asc node [degrees]
    private static final double periapsisPassagePioneerTenD = 2442020.688658506609;  // Time of periapsis [JD]
    private static final double meanMotionPioneerTenD = 7.002563604670944E-02; // Mean motion [degrees/day]
    private static final double[] ORBITPARSCORRD = new double[]
            {axisPioneerTenD, eccentricityPioneerTenD, inclinationPioneerTenD, argPeriapsisPioneerTenD,
                    longNodePioneerTenD, periapsisPassagePioneerTenD, meanMotionPioneerTenD};

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public PioneerTen(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        trajectories.add(
                new SpacecraftTrajectory(launch, correctionA, "Sun", ORBITPARSCORRA));
        trajectories.add(
                new SpacecraftTrajectory(correctionA, correctionB, "Sun", ORBITPARSCORRA));
        trajectories.add(
                new SpacecraftTrajectory(correctionB, correctionC, "Sun", ORBITPARSCORRB));
        trajectories.add(
                new SpacecraftTrajectory(correctionC, jupiterFlyBy, "Sun", ORBITPARSCORRC));
        trajectories.add(
                new SpacecraftTrajectory(jupiterFlyBy, correctionD, "Sun", ORBITPARSCORRD));
        trajectories.add(
                new SpacecraftTrajectory(correctionD, deactivated, "Sun", ORBITPARSCORRD));
        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        List<String> jupiterMoons = SolarSystemParameters.getInstance().getMoonsOfPlanet("Jupiter");
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(updateJupiterMoons), jupiterMoons));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionD)));
    }
}