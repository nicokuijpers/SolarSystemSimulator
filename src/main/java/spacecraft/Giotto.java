/*
 * Copyright (c) 2023 Nico Kuijpers
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
import solarsystem.SolarSystem;
import util.Vector3D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Giotto extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/Giotto_(spacecraft)
     * Giotto was a European robotic spacecraft mission from the European Space Agency. The spacecraft flew by
     * and studied Halley's Comet and in doing so became the first spacecraft to make close up observations
     * of a comet. On 13 March 1986, the spacecraft succeeded in approaching Halley's nucleus at a distance of
     * 596 kilometers. It was named after the Early Italian Renaissance painter Giotto di Bondone. He had
     * observed Halley's Comet in 1301 and was inspired to depict it as the star of Bethlehem in his painting
     * Adoration of the Magi.
     *
     * Launch: 11:23:00, 2 July 1985 (UTC)
     * Flyby of Halley's Comet: closest approach on 14 March 1986 at a distance of 596 km (370 mi)
     * Giotto's trajectory was adjusted for an Earth flyby and its science instruments were turned off on
     * 15 March 1986 at 02:00 UTC.
     * Flyby of Earth: closest approach on 2 July 1990 at a distance of 22,730 km (14,120 mi)
     * Flyby of Comet Grigg-Skjellerup: closest approach on 10 July 1992 at aistance of 200 km (120 mi)
     * Deactivated: 23 July 1992
     * Second Earth flyby: In 1999 Giotto made another Earth flyby but was not reactivated.
     *
     * https://www.nasa.gov/sites/default/files/atoms/files/beyond-earth-tagged.pdf
     * Page 165 and 166
     * After launch, and further course corrections on 26 August 1985, 12 February 1986,
     * and 12 March 1986, Giotto was put on a 500-kilometer-range flyby trajectory to
     * the comet’s core. Ballistics data on its precise voyage was based upon tracking
     * information from the Soviet Vega 1 and 2 probes. The Giotto spacecraft eventually
     * passed by Halley on 14 March 1986. Closest encounter was at a distance of 596
     * kilometers at 00:03:02 UT, the spacecraft being the only one among the large
     * armada of spacecraft sent to investigate Halley that actually entered the ionosphere
     * of the comet.
     *
     * After the encounter, ESA decided to redirect the vehicle for a flyby of Earth.
     * The spacecraft was officially put in hibernation mode on 2 April 1986.
     * Course corrections on 10, 20, and 21 March 1986, however, set it on a 22,000-kilometer
     * flyby of Earth on 2 July 1990 for a gravity assist (the first time that Earth
     * had been used for such a purpose) to visit a new target: Comet 26P/Grigg-Skjellerup,
     * which Giotto flew by at 15:30 UT on 10 July 1992 at range of approximately 200 kilometers.
     *
     * After formal conclusion of the encounter, Giotto was put in hibernation on 23 July 1992.
     * The spacecraft repeated a flyby of Earth at 02:40 UT on 1 July 1999 at range
     * of 219,000 kilometers but was not reactivated.
     *
     * https://archives.esac.esa.int/psa/ftp/GIOTTO/GRE/GIO-C-GRE-3-RDR-HALLEY-V1.0/CATALOG/MISSION.CAT
     * The spacecraft passed the nucleus at a distance of 596+/-2 km on the sunward side.
     * The time of closest approach occurred at 00:03:01.84 UT on March 14 (spacecraft event time).
     * However, at 7.6 s before closest approach, Giotto was hit by a large dust particle, whose impact
     * caused the spacecraft angular momentum vector to shift by 1 degree. The effect of the impact was
     * that the next 32 minutes of scientific data were received only intermittently. It is concluded that
     * the spacecraft traversed a region of high dust concentration (dust jet). A few hours after closest
     * approach, a number of the instruments were determined to be inoperable, probably from the passage
     * through the dust jet. About half of the experiments worked flawlessly during the encounter, while
     * the other half suffered damage due to dust impacts. The spacecraft also suffered some damage but
     * it was possible to redirect it to the Earth before it was put into hibernation.
     *
     * Mission Phases - Launch
     * The Giotto spacecraft was launched on July 2, 1985 onboard an Ariane-1 rocket from Kourou,
     * French Guyana.
     *
     * Mission Phases - Cruise
     * The Giotto spacecraft was initially injected into a Geostationary Transfer Orbit.
     * After three revolutions in orbit, the onboard motor was fired near perigee to inject
     * Giotto into a heliocentric orbit.
     * ...
     * After a cruise phase of 8 months, Giotto encountered Comet Halley on Mar 14, 1986.
     *
     * Mission Phases - Encounter
     * There were specific periods of science data availability after the last orbit correction
     * manoeuver that occurred on March 12 at 05:00.  The time of closest approach on March 14
     * is 00:03:01.84 UT, given in SCET or spacecraft event time. (This time can be related to
     * GSRT or ground station received time by the equation GSRT = SCET + 8 min 0.1 s.) Some
     * instruments, such as EPA, MAG, and GRE, ran continuously during the encounter which lasted
     * approximately 4 hours. Other instruments were switched-on for some intervals between
     * March 12 and March 13, but by 20:18 on that day all instruments were functioning.
     * Unfortunately, 7.6 s before closest approach, Giotto was hit by a large dust particle in
     * a dust jet. Only intermittent data was received for the next 32 minutes of the encounter
     * and damage to a number of instruments was substantial.
     *
     * https://nssdc.gsfc.nasa.gov/planetary/giotto.html
     * https://nssdc.gsfc.nasa.gov/nmc/spacecraft/display.action?id=1985-056A
     * The spacecraft encountered Halley on March 13, 1986, at a distance of 0.89 AU from the sun
     * and 0.98 AU from the Earth and an angle of 107 degrees from the comet-sun line. The goal was
     * to come within 500 km of Halley's comet at closest encounter. The actual closest approach was
     * measured at 596 km.
     * ...
     * During the Giotto extended mission, the spacecraft successfully encountered Comet P/Grigg-Skjellerup
     * on July 10, 1992. The closest approach was approximately 200 km. The heliocentric distance of the
     * spacecraft was 1.01 AU, and the geocentric distance, 1.43 AU at the time of the encounter.
     * ...
     * After the P/Grigg-Skjellerup encounter operation were terminated on 23 July 1992. Giotto flew by the
     * Earth on 1 July 1999 at a closest approach of about 219,000 km at approximately 02:40 UT (10:40 p.m. EDT,
     * 30 June). The spacecraft was moving at about 3.5 km/sec relative to Earth.
     *
     * https://sci.esa.int/web/giotto/-/31880-grigg-skjellerup
     * On 2 July 1990, Giotto's orbit was altered as it zipped past the Earth, just 22 730 km above the cloud tops.
     * This was the first time a spacecraft coming from deep space had used the Earth for a gravity assist. During
     * the flyby, observations were made of the Earth's magnetic field and energetic particle environment.
     *
     * After another prolonged hibernation, Giotto's payload was switched on in the evening of 9 July 1992.
     * The Grigg-Skjellerup flyby took place the following day, about 215 million km from Earth. Giotto crossed
     * the bow shock and entered the dust coma about 17 000 km from the comet. Aimed directly at the nucleus,
     * Giotto missed by a mere 100 to 200 km, eventually passing by on the night side at 15.30 GMT. It was the
     * closest ever cometary flyby.
     *
     * https://link.springer.com/referenceworkentry/10.1007/1-4020-4520-4_156
     * Table G3 contains time of closest approach (spacecraft onboard time)
     * Halley: 00:03:02 UT, 14 March 1986 (relative flyby velocity 68.37 km/s)
     * Grigg-Skjellerup: 15:18:43 UT, 10 July 1992 (relative flyby velocity 13.99 km/s)
     *
     * https://sci.esa.int/web/giotto/-/36672-geostationary-transfer-orbit
     * The Ariane 1 rocket places the Giotto spacecraft in a geostationary transfer orbit
     * (nominal parameters, perigee: 200 km, apogee: 36 000 km, inclination: 10°, period: 10.5 hours).
     * After nominally 3 revolutions in the GTO the onboard solid propellant boost motor is fired close
     * to perigee (before firing the spacecraft will be spun up from 15 rpm to 90 rpm) to inject the spacecraft
     * into the heliocentric transfer trajectory.
     *
     * https://projectpluto.com/probes.htm#giott
     * Orbital elements:
     * GIOTTO
     * Range: 1985 07 02   1986 04 03
     *    Perihelion 1985 Mar 3.579075 TT
     * Epoch 1985 Jul 15.0 TT = JDT 2446261.5
     * M 159.31240              (2000.0)            P               Q
     * n   1.19405856     Peri.  204.65838      0.00000000      0.00000000
     * a   0.8799385      Node   281.74779      0.00000000      0.00000000
     * e   0.1783803      Incl.    2.10274      0.00000000      0.00000000
     * P                  H   32.0           G   0.15
     * Ref: Steve Matousek (JPL) SEDS file
     *
     * Orbital elements:
     * GIOTTO
     * Range: 1986 04 03   1999 12 31
     *    Perihelion 1985 Dec 31.056058 TT
     * Epoch 1986 Apr 3.0 TT = JDT 2446523.5
     * M 109.77213              (2000.0)            P               Q
     * n   1.18105734     Peri.  206.80435      0.00000000      0.00000000
     * a   0.8863843      Node   281.34110      0.00000000      0.00000000
     * e   0.1733404      Incl.    2.09192      0.00000000      0.00000000
     * P                  H   32.0           G   0.15
     * Ref: Steve Matousek (JPL) SEDS file
     *
     * https://en.wikipedia.org/wiki/Geostationary_transfer_orbit
     * GTO is a highly elliptical Earth orbit with an apogee (the point in the orbit of the moon or a
     * satellite at which it is furthest from the earth) of 42,164 km (26,199 mi), or a height of
     * 35,786 km (22,236 mi) above sea level, which corresponds to the geostationary altitude.
     * The period of a standard geosynchronous transfer orbit is about 10.5 hours.
     *
     * https://issfd.org/ISSFD_1986/ISSFD_1986_S05_02.pdf
     * T. Morley, F. Hechler, S. Jappe, N. Mottinger, R. Premkumar
     * Giotto Interplanetary Orbit Determination
     * Proc Second International Symposium on Spacecraft Flight Dynamics, Darmstadt, FR Germany, 20-23 Oct 1986
     * Page 173:
     * Giotto was launched on 2 July 1985 and after three revolutions in geocentric orbit the firing of the
     * MAGE 1S motor on 3 July at 19:25 placed the spacecraft into its earth escape trajectory.
     * Page 180:
     * From the tracking data, the best estimates of the encounter parameters were computed to be:
     * miss-distance 610 km; time of closest approach 1986/3/14 00:03:00.4 UT.
     *
     * Position of Giotto relative to Halley's Comet is obtained from
     * https://naif.jpl.nasa.gov/pub/naif/GIOTTO/kernels/spk/
     * using spiceypy and kernel files de405_1900_2200.bsp and giotto_19860305_19860317.bsp
     * According to this kernel file, closest distance was 630.161 km and occurred at 00:03:00
     * X = 258711.735 m
     * Y = 574371.257 m
     * Z = -16391.104 m
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch July 2, 1985, 11:23:00 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(1985, 6, 2, 11, 23, 0);

    // Date/time of injection into heliocentric transfer trajectory after three revolutions in
    // geostationary transfer orbit. July 3, 19:25 UTC
    private static final GregorianCalendar launchFromGTODateTime =
            new GregorianCalendar(1985, 6, 3, 19, 25, 0);


    // Date/time when Giotto leaves the gravitational pull of the Earth
    private static final GregorianCalendar leavingEarthDateTime =
            new GregorianCalendar(1985, 6, 5, 0, 0, 0);

    // Schedule simulated correction on July 15, 1985, 0:00 UTC
    private static final GregorianCalendar correctionA =
            new GregorianCalendar(1985, 6, 15, 0, 0, 0);

    // Schedule simulated correction on August 27, 1985, 0:00 UTC after correction on August 26
    private static final GregorianCalendar correctionB =
            new GregorianCalendar(1985, 7, 27, 0, 0, 0);

    // Schedule simulated correction on February 13, 1986, 0:00 UTC after correction on February 12
    private static final GregorianCalendar correctionC =
            new GregorianCalendar(1986, 1, 13, 0, 0, 0);

    // Schedule simulated correction on March 13, 1986, 0:00 UTC after correction on March 12
    private static final GregorianCalendar correctionD =
            new GregorianCalendar(1986, 2, 13, 0, 0, 0);

    // Schedule simulated correction on March 14, 1986, 08:00 UTC after encounter with Halley's Comet
    private static final GregorianCalendar correctionE =
            new GregorianCalendar(1986, 2, 14, 8, 0, 0);

    // Schedule simulated correction on April 3, 1986, 0:00 UTC
    private static final GregorianCalendar correctionF =
            new GregorianCalendar(1986, 3, 3, 0, 0, 0);

    // Schedule simulated correction on April 3, 1987, 0:00 UTC
    private static final GregorianCalendar correctionG =
            new GregorianCalendar(1987, 3, 3, 0, 0, 0);

    // Schedule simulated correction on April 15, 1988, 0:00 UTC
    private static final GregorianCalendar correctionH =
            new GregorianCalendar(1988, 3, 15, 0, 0, 0);

    // Schedule simulated correction on May 1, 1989, 0:00 UTC
    private static final GregorianCalendar correctionI =
            new GregorianCalendar(1989, 4, 1, 0, 0, 0);

    // Schedule simulated correction on May 15, 1990, 0:00 UTC
    private static final GregorianCalendar correctionJ =
            new GregorianCalendar(1990, 4, 15, 0, 0, 0);

    // Schedule simulated correction on July 10, 1990, 0:00 UTC after Earth flyby on July 2
    private static final GregorianCalendar correctionK =
            new GregorianCalendar(1990, 6, 10, 0, 0, 0);

    // Schedule simulated correction on Jan 1, 1992, 0:00 UTC
    private static final GregorianCalendar correctionL =
            new GregorianCalendar(1992, 0, 1, 0, 0, 0);

    // Schedule simulated correction on June 1, 1992, 0:00 UTC prior to encounter with 26P/Grigg–Skjellerup
    private static final GregorianCalendar correctionM =
            new GregorianCalendar(1992, 5, 1, 0, 0, 0);

    // Schedule simulated correction on July 1, 1992, 0:00 UTC prior to encounter with 26P/Grigg–Skjellerup
    private static final GregorianCalendar correctionN =
            new GregorianCalendar(1992, 6, 1, 0, 0, 0);

    // Encounter with Halley's Comet on March 14, 1986, 00:03:00 UTC
    private static final GregorianCalendar encounterHalleyDateTime =
            new GregorianCalendar(1986, 2, 14, 0, 3, 0);

    // Position of Giotto relative to Halley's Comet at moment of encounter
    private static final Vector3D positionGiottoRelativeToHalley = new Vector3D(258711.735, 574371.257 , -16391.104);

    // Encounter with Earth on July 2, 1990 at 12.00 (Time estimated)
    private static final GregorianCalendar encounterEarthDateTime =
            new GregorianCalendar(1990, 6, 2, 12, 0, 0);

    // Closest distance during encounter with Earth was 22,730 km = 22,730,000 m
    private static final double distanceEncounterEarth = 2.273E07;

    // Arrival date/time to determine orbital parameters for the flight near Earth with center body Earth
    private static final GregorianCalendar arrivalEarthDateTime =
            new GregorianCalendar(1990, 5, 30, 0, 0, 0);

    // Departure date/time to determine orbital parameters for the flight near Earth with center body Earth
    private static final GregorianCalendar departureEarthDateTime =
            new GregorianCalendar(1990, 6, 4, 0, 0, 0);

    // Encounter with 26P/Grigg-Skjellerup on July 10, 1992 at 15:18:43
    private static final GregorianCalendar encounterGSDateTime =
            new GregorianCalendar(1992, 6, 10, 15, 18, 43);

    // Closest distance during encounter with 26P/Grigg-Skjellerup was 200 km = 200000 m
    private static final double distanceEncounterGS = 200000.0;

    // Deactivated on July 23, 1992
    private static final GregorianCalendar deactivated =
            new GregorianCalendar(1992, 6, 23, 0, 0, 0);

    // Distance between center of the Earth and perigee of orbit
    // Radius of the Earth plus 200 km
    private static final double perigeeDistanceGTO =
            0.5*SolarSystemParameters.getInstance().getDiameter("Earth") + 200000;

    // Distance between center of the Earth and apogee of orbit
    // Radius of the Earth plus 36000 km
    private static final double apogeeDistanceGTO =
            0.5*SolarSystemParameters.getInstance().getDiameter("Earth") + 36000000;

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public Giotto(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {

        // Determine orbital parameters of geostationary transfer orbit
        Vector3D positionEarthLaunch = EphemerisSolarSystem.getInstance().getBodyPosition("Earth", launch);
        double muEarth = SolarSystemParameters.getInstance().getMu("Earth");
        double axisGTOm = 0.5*(perigeeDistanceGTO + apogeeDistanceGTO);            // semi-major axis [m]
        double axisGTOau = axisGTOm / SolarSystemParameters.ASTRONOMICALUNIT;      // semi-major axis [au]
        double eccentricityGTO = (apogeeDistanceGTO - perigeeDistanceGTO) /
                (apogeeDistanceGTO + perigeeDistanceGTO);         // eccentricity [-]
        double inclinationGTO = 0.0;                                               // inclination [degrees]
        double argPerifocusGTO =
                Math.toDegrees(Math.atan2(-positionEarthLaunch.getY(),
                        -positionEarthLaunch.getX())); // argument of perifocus [degrees]
        double longNodeGTO = 0.0;                                        // longitude of ascending node [degrees]
        double TperiGTO = JulianDateConverter.convertCalendarToJulianDate(launch); // time of perifocus passage [JED]
        double orbitalPeriodGTOsec = 2*Math.PI*Math.sqrt(axisGTOm*axisGTOm*axisGTOm/muEarth); // orbital period [seconds]
        double orbitalPeriodGTOdays = orbitalPeriodGTOsec / (24 * 60 * 60);                   // orbital period [days]
        double meanMotionGTO = 360.0 / orbitalPeriodGTOdays;                       // mean motion [degrees/day]
        double[] orbitParsGTO = new double[]{axisGTOau, eccentricityGTO, inclinationGTO,
                argPerifocusGTO, longNodeGTO, TperiGTO, meanMotionGTO};

        // Position at launch from GTO relative to the Earth
        double[] orbitElementsLaunchFromGTO = EphemerisUtil.computeOrbitalElements(orbitParsGTO, launchFromGTODateTime);
        Vector3D positionLaunchFromGTORelativeToEarth = EphemerisUtil.computePosition(orbitElementsLaunchFromGTO);

        // Position at launch from GTO relative to the Sun
        Vector3D positionEarthLaunchFromGTO =
                EphemerisSolarSystem.getInstance().getBodyPosition("Earth", launchFromGTODateTime);
        Vector3D positionLaunchFromGTORelativeToSun =
                positionEarthLaunchFromGTO.plus(positionLaunchFromGTORelativeToEarth);

        // Position of Halley's Comet at moment of encounter
        Vector3D positionHalleyEncounter =
                EphemerisSolarSystem.getInstance().getBodyPosition("Halley", encounterHalleyDateTime);

        // Position of Giotto at moment of encounter with Halley's Comet
        Vector3D positionGiottoHalleyEncounter = positionHalleyEncounter.plus(positionGiottoRelativeToHalley);

        // Position and velocity of Earth during passage on July 2, 1990
        Vector3D positionEarthEncounter =
                EphemerisSolarSystem.getInstance().getBodyPosition("Earth", encounterEarthDateTime);
        Vector3D velocityEarthEncounter =
                EphemerisSolarSystem.getInstance().getBodyVelocity("Earth", encounterEarthDateTime);

        // Position of Giotto during passage on July 2, 1990
        // Distance from surface of the Earth was 22,730 km
        // It is assumed that Giotto was behind the Earth at the moment of closest distance
        // The exact time is not known. It is assumed that this time was 12.00 (noon)
        double radiusEarth = 0.5*SolarSystemParameters.getInstance().getDiameter("Earth");
        double distanceEncounterEarthCenter = radiusEarth + distanceEncounterEarth;
        Vector3D positionGiottoEarthEncounter =
                positionEarthEncounter.minus(velocityEarthEncounter.normalize().scalarProduct(distanceEncounterEarthCenter));

        // Orbital parameters relative to the Sun for trajectory from the Earth to Halley's Comet
        double[] orbitParsEarthHalley =
                computeOrbitalParameters(launchFromGTODateTime, encounterHalleyDateTime,
                        positionLaunchFromGTORelativeToSun, positionGiottoHalleyEncounter, "Sun", false ,0);

        // Position of Giotto with respect to the Sun when leaving the Earth
        double[] orbitElementsLeavingEarthRelativeToSun = EphemerisUtil.computeOrbitalElements(orbitParsEarthHalley, leavingEarthDateTime);
        Vector3D positionLeavingEarthRelativeToSun = EphemerisUtil.computePosition(orbitElementsLeavingEarthRelativeToSun);
        Vector3D earthPositionLeavingEarth = EphemerisSolarSystem.getInstance().getBodyPosition("Earth", leavingEarthDateTime);
        Vector3D positionLeavingEarthRelativeToEarth = positionLeavingEarthRelativeToSun.minus(earthPositionLeavingEarth);

        // Orbital parameters with center body Earth for launch from GTO until spacecraft leaves the Earth
        double[] orbitParsNearEarth =
                computeOrbitalParameters(launchFromGTODateTime, leavingEarthDateTime,
                        positionLaunchFromGTORelativeToEarth, positionLeavingEarthRelativeToEarth, "Earth", false, 0);

        // Orbital parameters with center body Sun for trajectory between July 15, 1985 and August 27, 1985
        double[] orbitElementsGiottoCorrectionA = EphemerisUtil.computeOrbitalElements(orbitParsEarthHalley, correctionA);
        Vector3D positionGiottoCorrectionA = EphemerisUtil.computePosition(orbitElementsGiottoCorrectionA);
        double[] orbitParsCorrectionA = computeOrbitalParameters(correctionA, encounterHalleyDateTime,
                positionGiottoCorrectionA, positionGiottoHalleyEncounter, "Sun", false ,0);

        // Orbital parameters with center body Sun for trajectory between August 27, 1985 and February 13, 1986
        double[] orbitElementsGiottoCorrectionB = EphemerisUtil.computeOrbitalElements(orbitParsCorrectionA, correctionB);
        Vector3D positionGiottoCorrectionB = EphemerisUtil.computePosition(orbitElementsGiottoCorrectionB);
        double[] orbitParsCorrectionB = computeOrbitalParameters(correctionB, encounterHalleyDateTime,
                positionGiottoCorrectionB, positionGiottoHalleyEncounter, "Sun", false ,0);

        // Orbital parameters with center body Sun for trajectory between February 13, 1986 and March 13, 1986
        double[] orbitElementsGiottoCorrectionC = EphemerisUtil.computeOrbitalElements(orbitParsCorrectionB, correctionC);
        Vector3D positionGiottoCorrectionC = EphemerisUtil.computePosition(orbitElementsGiottoCorrectionC);
        double[] orbitParsCorrectionC = computeOrbitalParameters(correctionC, encounterHalleyDateTime,
                positionGiottoCorrectionC, positionGiottoHalleyEncounter, "Sun", false ,0);

        // Position of Giotto on April 3, 1986, 0:00 UTC
        double[] orbitElementsGiottoCorrectionF = EphemerisUtil.computeOrbitalElements(orbitParsCorrectionC, correctionF);
        Vector3D positionGiottoCorrectionF = EphemerisUtil.computePosition(orbitElementsGiottoCorrectionF);

        // Orbital parameters relative to the Sun for trajectory April 3, 1986 till encounter with Earth on July 2, 1990
        double[] orbitParsCorrectionF = computeOrbitalParameters(correctionF, encounterEarthDateTime,
                positionGiottoCorrectionF, positionGiottoEarthEncounter, "Sun", false ,8);

        // Position of Giotto at Earth arrival
        double[] orbitElementsArrivalEarthRelativeToSun = EphemerisUtil.computeOrbitalElements(orbitParsCorrectionF, arrivalEarthDateTime);
        Vector3D positionArrivalEarthRelativeToSun = EphemerisUtil.computePosition(orbitElementsArrivalEarthRelativeToSun);
        Vector3D positionEarthArrivalEarth = EphemerisSolarSystem.getInstance().getBodyPosition("Earth", arrivalEarthDateTime);
        Vector3D positionArrivalEarthRelativeToEarth = positionArrivalEarthRelativeToSun.minus(positionEarthArrivalEarth);

        // Position of spacecraft during encounter with Earth
        Vector3D positionEarthEncounterEarth = EphemerisSolarSystem.getInstance().getBodyPosition("Earth", encounterEarthDateTime);
        Vector3D positionEncounterEarthRelativeToEarth = positionGiottoEarthEncounter.minus(positionEarthEncounterEarth);

        // Orbital parameters with center body Earth during Earth encounter
        double[] orbitParsEncounterEarth =
                computeOrbitalParameters(arrivalEarthDateTime, encounterEarthDateTime,
                        positionArrivalEarthRelativeToEarth, positionEncounterEarthRelativeToEarth, "Earth", false, 0);

        // Position of Giotto when leaving Earth
        double[] orbitElementsDepartureEarthRelativeToEarth = EphemerisUtil.computeOrbitalElements(orbitParsEncounterEarth, departureEarthDateTime);
        Vector3D positionDepartureEarthRelativeToEarth = EphemerisUtil.computePosition(orbitElementsDepartureEarthRelativeToEarth);
        Vector3D positionEarthDepartureEarth = EphemerisSolarSystem.getInstance().getBodyPosition("Earth", departureEarthDateTime);
        Vector3D positionDepartureEarthRelativeToSun = positionEarthDepartureEarth.plus(positionDepartureEarthRelativeToEarth);

        // Position of 26P/Grigg–Skjellerup at moment of encounter
        Vector3D positionGSEncounter =
                EphemerisSolarSystem.getInstance().getBodyPosition("26P/Grigg-Skjellerup", encounterGSDateTime);

        // Position of Giotto at moment of encounter with 26P/Grigg–Skjellerup
        // Giotto passed at a distance of 200 km on the night side
        Vector3D positionGiottoGSEncounter =
                positionGSEncounter.plus(positionGSEncounter.normalize().scalarProduct(distanceEncounterGS));

        // Orbital parameters relative to the Sun for trajectory to 26P/Grigg–Skjellerup
        double[] orbitParsEncounterGS =
                computeOrbitalParameters(departureEarthDateTime, encounterGSDateTime,
                        positionDepartureEarthRelativeToSun, positionGiottoGSEncounter, "Sun", false ,1);

        // Define trajectory
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        trajectories.add(
                new SpacecraftTrajectory(launch, launchFromGTODateTime, "Earth", orbitParsGTO));
        trajectories.add(
                new SpacecraftTrajectory(launchFromGTODateTime, leavingEarthDateTime, "Earth", orbitParsNearEarth));
        trajectories.add(
                new SpacecraftTrajectory(leavingEarthDateTime, correctionA, "Sun", orbitParsEarthHalley));
        trajectories.add(
                new SpacecraftTrajectory(correctionA, correctionB, "Sun", orbitParsCorrectionA));
        trajectories.add(
                new SpacecraftTrajectory(correctionB, correctionC, "Sun", orbitParsCorrectionB));
        trajectories.add(
                new SpacecraftTrajectory(correctionC, correctionF, "Sun", orbitParsCorrectionC));
        trajectories.add(
                new SpacecraftTrajectory(correctionF, arrivalEarthDateTime, "Sun", orbitParsCorrectionF));
        trajectories.add(
                new SpacecraftTrajectory(arrivalEarthDateTime, departureEarthDateTime, "Earth", orbitParsEncounterEarth));
        trajectories.add(
                new SpacecraftTrajectory(departureEarthDateTime, deactivated, "Sun", orbitParsEncounterGS));

        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {

        // Corrections including Halley's Comet
        List<String> bodyNamesHalley = new ArrayList<>();
        bodyNamesHalley.add("Giotto");
        bodyNamesHalley.add("Halley");

        // Corrections including 26P/Grigg–Skjellerup
        List<String> bodyNamesGS = new ArrayList<>();
        bodyNamesGS.add("Giotto");
        bodyNamesGS.add("26P/Grigg-Skjellerup");

        // Corrections for Giotto
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        GregorianCalendar eventDateTimeCorrLaunch = (GregorianCalendar) launch.clone();
        eventDateTimeCorrLaunch.add(Calendar.HOUR,5);
        eventDateTimeCorrLaunch.add(Calendar.MINUTE,15);
        for (int i = 0; i < 3; i++) {
            solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(eventDateTimeCorrLaunch)));
            eventDateTimeCorrLaunch.add(Calendar.HOUR,10);
            eventDateTimeCorrLaunch.add(Calendar.MINUTE,30);
        }
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launchFromGTODateTime)));
        GregorianCalendar eventDateTimeCorrLaunchFromGTO = (GregorianCalendar) launchFromGTODateTime.clone();
        for (int i = 0; i < 4; i++) {
            eventDateTimeCorrLaunchFromGTO.add(Calendar.HOUR,1);
            solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(eventDateTimeCorrLaunchFromGTO)));
        }
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(leavingEarthDateTime)));
        GregorianCalendar eventDateTimeCorrLeavingEarth = (GregorianCalendar) leavingEarthDateTime.clone();
        for (int i = 0; i < 8; i++) {
            eventDateTimeCorrLeavingEarth.add(Calendar.HOUR,12);
            solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(eventDateTimeCorrLeavingEarth)));
        }
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionC), bodyNamesHalley));
        //solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionD)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionE)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionF)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionG)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionH)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionI)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionJ)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(arrivalEarthDateTime)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(departureEarthDateTime)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionK)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionL), bodyNamesGS));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionM), bodyNamesGS));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionN), bodyNamesGS));
    }

    /**
     * Determine orbital parameters for Giotto for given ephemeris date/time.
     * @param ephemerisDateTime Date/time for which orbital parameters should be determined
     * @param encounterDateTime Date/time of encounter with planet or comet
     * @param ephemerisPosition Position of spacecraft at ephemeris date/time
     * @param encounterPosition Position of spacecraft at encounter date/time
     * @param centerBodyName    Name of center body
     * @param cw                Retrograde orbit is assumed when true
     * @param multiRevs         Maximum number of multirevolutions to compute
     * @return orbital parameters
     */
    private static double[] computeOrbitalParameters(GregorianCalendar ephemerisDateTime, GregorianCalendar encounterDateTime,
                                                     Vector3D ephemerisPosition, Vector3D encounterPosition, String centerBodyName,
                                                     boolean cw, int multiRevs) {

        /* DEBUG
        System.out.println();
        System.out.println("COMPUTE ORBITAL PARAMETERS");
        System.out.println("Ephemeris date/time " + CalendarUtil.calendarToString(ephemerisDateTime));
        System.out.println("Encounter date/time " + CalendarUtil.calendarToString(encounterDateTime));
        System.out.println("Ephemeris position  " + ephemerisPosition);
        System.out.println("Encounter position  " + encounterPosition);
        System.out.println("Center body         " + centerBodyName);
        System.out.println("Parameter cw        " + cw);
        System.out.println("Parameter mulitRevs " + multiRevs);
        */

        // Obtain desired velocity of spacecraft at time of ephemeris by solving Lambert's problem
        double tof = (encounterDateTime.getTimeInMillis() - ephemerisDateTime.getTimeInMillis()) / 1000.0;
        double mu = SolarSystemParameters.getInstance().getMu(centerBodyName);
        Vector3D ephemerisVelocity = new Vector3D();
        try {
            LambertProblem lambertProblem = new LambertProblem(ephemerisPosition, encounterPosition, tof, mu, cw, multiRevs);
            int index = lambertProblem.getMaxNumberRevolutions();
            ephemerisVelocity = lambertProblem.getAllVelocities1()[index];
            // System.out.println(lambertProblem);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }

        // Compute orbital parameters from position and velocity of spacecraft at time of ephemeris
        double[] orbitParametersSpacecraft = EphemerisUtil.computeOrbitalParametersFromPositionVelocity(mu,
                ephemerisPosition, ephemerisVelocity, ephemerisDateTime);

        /* DEBUG
        System.out.println("ORBIT PARAMETERS:");
        System.out.println(" semi-major axis [au]                  " + orbitParametersSpacecraft[0]);
        System.out.println(" eccentricity [-]                      " + orbitParametersSpacecraft[1]);
        System.out.println(" inclination [degrees]                 " + orbitParametersSpacecraft[2]);
        System.out.println(" argument of perifocus [degrees]       " + orbitParametersSpacecraft[3]);
        System.out.println(" longitude of ascending node [degrees] " + orbitParametersSpacecraft[4]);
        System.out.println(" time of perifocus passage [JED]       " + orbitParametersSpacecraft[5]);
        System.out.println(" mean motion [degrees/day]             " + orbitParametersSpacecraft[6]);
        */

        return orbitParametersSpacecraft;
    }
}
