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
import java.util.GregorianCalendar;
import java.util.List;

public class MarinerTen extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/Mariner_10
     * Mariner 10 was an American robotic space probe launched by NASA on 3 November 1973, to fly by the planets
     * Mercury and Venus. It was the first spacecraft to perform flybys of multiple planets.
     *
     * The mission objectives were to measure Mercury's environment, atmosphere, surface, and body characteristics
     * and to make similar investigations of Venus. Secondary objectives were to perform experiments in the
     * interplanetary medium and to obtain experience with a dual-planet gravity assist mission.
     *
     * Mariner 10 was the first spacecraft to make use of an interplanetary gravitational slingshot maneuver,
     * using Venus to bend its flight path and bring its perihelion down to the level of Mercury's orbit.
     * This maneuver put the spacecraft into an orbit that repeatedly brought it back to Mercury.
     *
     * Launch 3 November at 17:45 UTC. A trajectory correction maneuver was made on 13 November 1973.
     * In January 1974, Mariner 10 made ultraviolet observations of Comet Kohoutek. Another mid-course correction
     * was made on 21 January 1974.
     *
     * The spacecraft passed Venus on 5 February 1974, the closest approach being 5,768 kilometers (3,584 mi) at
     * 17:01 UTC. Earth occultation occurred between 17:07 and 17:11 UTC. The probe continued photographing Venus
     * until 13 February 1974.
     *
     * The spacecraft flew past Mercury three times. The first Mercury encounter took place at 20:47 UTC on
     * 29 March 1974, at a range of 703 kilometers (437 mi), passing on the shadow side. After looping once around
     * the Sun while Mercury completed two orbits, Mariner 10 flew by Mercury again on 21 September 1974, at a more
     * distant range of 48,069 kilometers (29,869 mi) below the southern hemisphere. After losing roll control in
     * October 1974, a third and final encounter, the closest to Mercury, took place on 16 March 1975, at a range
     * of 327 kilometers (203 mi), passing almost over the north pole.
     *
     * With its maneuvering gas just about exhausted, Mariner 10 started another orbit of the Sun. Engineering tests
     * were continued until 24 March 1975, when final depletion of the nitrogen supply was signaled by the onset of
     * an un-programmed pitch turn. Commands were sent immediately to the spacecraft to turn off its transmitter, and
     * radio signals to Earth ceased. Mariner 10 is presumably still orbiting the Sun, although its electronics have
     * probably been damaged by the Sun's radiation. Mariner 10 has not been spotted or tracked from Earth since it
     * stopped transmitting. The only ways it would not be orbiting would be if it had been hit by an asteroid or
     * gravitationally perturbed by a close encounter with a large body.
     *
     * Overview of trajectory from launch until first encounter with Mercury
     * From this overview it can be deduced that TCM 3 took place on March 16, 1974
     * https://en.wikipedia.org/wiki/Mariner_10#/media/File:Mariner-10-Trajectory-first_half.PNG
     * Launch November 3, 1973
     * TCM 1 November 13, 1973
     * TCM 2 January 21, 1974
     * Venus encounter February 5, 1974
     * TCM 3 March 16, 1974
     * First Mercury encounter March 29, 1974
     *
     * NASA Technical Memorandum 33-734 Volume II
     * Mariner Venus-Mercury 1973 Project
     * Final Report
     * Extended Mission - Mercury II and III Encounters
     * Contains dates of TCMs 4 through 8, but no orbital parameters
     * https://core.ac.uk/download/pdf/42884958.pdf
     *
     * https://www.nasa.gov/sites/default/files/atoms/files/beyond-earth-tagged.pdf
     * Nation: USA (55)
     * Objective(s): Mercury flyby, Venus flyby
     * Spacecraft: Mariner-73J / Mariner-J
     * Spacecraft Mass: 502.9 kg
     * Mission Design and Management: NASA / JPL
     * Launch Vehicle: Atlas Centaur (AC-34 / Atlas 3D no. 5014D / Centaur D-1A)
     * Launch Date and Time: 3 November 1973 / 05:45:00 UT
     * Page 121 and 122
     * After mid-course corrections on 13  November 1973 and 21 January 1974, Mariner 10 approached
     * Venus for a gravity-assist maneuver to send it towards Mercury. Closest flyby range was 5,768
     * kilometers at 17:01 UT on 5 February. Assisted by Venusian gravity, the spacecraft now headed
     * to the innermost planet, which it reached after another mid-course correction on 16 March 1974.
     * Closest encounter came at 20:47 UT on 29 March 1974 at a range of 703 kilometers.
     * Leaving Mercury behind, the spacecraft looped around the Sun and headed back to its target, helped
     * along by subsequent course corrections on 9 May, 10 May, and 2 July 1974. Mariner 10 flew by
     * Mercury once more on at 20:59 UT on 21 September 1974 at a more distant 48,069 kilometers range,
     * adding imagery of the southern polar region. Mariner 10 once again sped away from Mercury before
     * a final and third encounter with Mercury, enabled by three maneuvers (on 30 October 1974, 13 February
     * 1975, and 7 March 1975), the last one actually to avoid impact with the planet. The third flyby, at
     * 22:39 UT on 16 March 1975, was the closest to Mercury, at a range of 327 kilometers. Last contact
     * with the spacecraft was at 12:21 UT on 24 March 1975 after the spacecraft exhausted its supply of
     * gas for attitude control.
     *
     * http://www.astronautix.com/m/mariner10.html
     * Mariner 10 (also known as Mariner Venus Mercury 1973) was placed in a parking orbit after
     * launch for approximately 25 minutes, then placed in orbit around the Sun en route to Venus.
     *
     * See also: The Mariner 10 mission
     * https://www.lpl.arizona.edu/~shane/PTYS_395_MERCURY/reading/ch2_exp_mer.pdf
     *
     * Please note that we were not able to find orbital parameters or exact positions and velocities of the
     * spacecraft. Orbital parameters are found by solving Lambert's problem using time and position of the
     * spacecraft on the basis of the data described above.
     * See also: https://en.wikipedia.org/wiki/Lambert%27s_problem
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch November 3, 1973 at 17:45 UTC
    private static final GregorianCalendar launch =
            new GregorianCalendar(1973, 10, 3, 17, 45, 0);

    // Schedule simulated correction on November 5, 1973, 0:00 UTC after leaving the Earth
    private static final GregorianCalendar correctionA =
            new GregorianCalendar(1973, 10, 5, 0, 0, 0);

    // Schedule simulated correction on November 6, 1973, 0:00 UTC after leaving the Earth
    private static final GregorianCalendar correctionB =
            new GregorianCalendar(1973, 10, 6, 0, 0, 0);

    // Schedule simulated correction on November 14, 1973, 0:00 UTC after TCM 1 on November 13
    private static final GregorianCalendar correctionC =
            new GregorianCalendar(1973, 10, 14, 0, 0, 0);

    // Schedule simulated correction on January 22, 1974, 0:00 UTC after TCM 2 on January 21, 1974
    private static final GregorianCalendar correctionD =
            new GregorianCalendar(1974, 0, 22, 0, 0, 0);

    // Schedule simulated correction on March 17, 1974, 0:00 UTC after TCM 3 on March 16, 1974
    private static final GregorianCalendar correctionE =
            new GregorianCalendar(1974, 2, 17, 0, 0, 0);

    // Schedule simulated correction on March 29, 1974, 0:00 UTC before first encounter with Mercury at 20:47 UTC
    private static final GregorianCalendar correctionF =
            new GregorianCalendar(1974, 2, 29, 0, 0, 0);

    // Schedule simulated correction on March 31, 1974, 0:00 UTC after first encounter with Mercury on March 29
    private static final GregorianCalendar correctionG =
            new GregorianCalendar(1974, 2, 31, 0, 0, 0);

    // Schedule simulated correction on May 11, 1974, 0:00 UTC after course corrections on May 9 and May 10
    private static final GregorianCalendar correctionH =
            new GregorianCalendar(1974, 4, 11, 0, 0, 0);

    // Schedule simulated correction on July 3, 1974, 0:00 UTC after course correction on July 2
    private static final GregorianCalendar correctionI =
            new GregorianCalendar(1974, 6, 3, 0, 0, 0);

    // Schedule simulated correction on September 21, 1974, 0:00 UTC before second encounter with Mercury on that day
    private static final GregorianCalendar correctionJ =
            new GregorianCalendar(1974, 8, 21, 0, 0, 0);

    // Schedule simulated correction on September 23, 1974, 0:00 UTC after second encounter with Mercury on September 21
    private static final GregorianCalendar correctionK =
            new GregorianCalendar(1974, 8, 23, 0, 0, 0);

    // Schedule simulated correction on October 31, 1974, 0:00 UTC after course correction on October 30
    private static final GregorianCalendar correctionL =
            new GregorianCalendar(1974, 9, 31, 0, 0, 0);

    // Schedule simulated correction on February 14, 1975, 0:00 UTC after course correction on February 13
    private static final GregorianCalendar correctionM =
            new GregorianCalendar(1975, 1, 14, 0, 0, 0);

    // Schedule simulated correction on March 8, 1975, 0:00 UTC after course correction on March 7
    private static final GregorianCalendar correctionN =
            new GregorianCalendar(1975, 2, 8, 0, 0, 0);

    // Schedule simulated correction on March 16, 1975, 0:00 UTC before third encounter with Mercury on that day
    private static final GregorianCalendar correctionO =
            new GregorianCalendar(1975, 2, 16, 0, 0, 0);

    // Schedule simulated correction on March 18, 1975, 0:00 UTC after third encounter with Mercury on March 16
    private static final GregorianCalendar correctionP =
            new GregorianCalendar(1975, 2, 18, 0, 0, 0);

    // Contact with Mariner 10 was terminated on March 24, 1975
    private static final GregorianCalendar deactivated =
            new GregorianCalendar(1975, 2, 24, 0, 0, 0);

    // Launch took place on November 3, 1973 at 17:45 UTC from Cape Canaveral, Launch Complex 36B
    // https://en.wikipedia.org/wiki/Cape_Canaveral_Launch_Complex_36
    // Coordinates of Launch complex 36: 28°28′14″N 80°32′24″W
    // Height of launch site is same height as that of parking orbit, 200 km
    // Chosen values for latitude and longitude give a nice view on Florida and Cape Canaveral at time of launch
    private static final double latitudeLaunchSite = 19.0;
    private static final double longitudeLaunchSite = -82.5;
    private static final double heightLaunchSite = 200000;
    private static final double heightParkingOrbit = 200000;
    private static final double radiusEarth = 0.5 * SolarSystemParameters.getInstance().getDiameter("Earth");
    private static final Vector3D positionLaunchRelativeToEarth =
            EphemerisUtil.computePositionFromLatitudeLongitudeHeight(latitudeLaunchSite,
                    longitudeLaunchSite, heightLaunchSite, launch);

    // Launch from parking orbit in opposite direction of the velocity vector of the Earth
    // Time of launch from parking orbit is chosen such that parking orbit is circular with height 200 km
    // The spacecraft will remain in the parking orbit for about 23 minutes
    // Use orbital parameters with center body Earth until November 4, 12.00 UTC, to compensate for
    // gravitational pull by the Earth
    private static final GregorianCalendar launchFromParkingOrbitDateTime =
            new GregorianCalendar(1973, 10, 3, 18, 8, 10);
    private static final GregorianCalendar leavingEarthDateTime =
            new GregorianCalendar(1973, 10, 4, 12, 0, 0);
    private static final Vector3D earthPositionLaunchFromParkingOrbit =
            EphemerisSolarSystem.getInstance().getBodyPosition("Earth", launchFromParkingOrbitDateTime);
    private static final Vector3D earthVelocityLaunchFromParkingOrbit =
            EphemerisSolarSystem.getInstance().getBodyVelocity("Earth", launchFromParkingOrbitDateTime);
    private static final Vector3D positionLaunchFromParkingOrbitRelativeToEarth =
            earthVelocityLaunchFromParkingOrbit.normalize().scalarProduct(-(radiusEarth + heightParkingOrbit));
    private static final Vector3D spacecraftPositionLaunchFromParkingOrbit =
            earthPositionLaunchFromParkingOrbit.plus(positionLaunchRelativeToEarth);

    // Determine orbital parameters for Earth to Venus
    // Encounter with Venus took place on 5 February 1974
    // Closest approach was 5,768 km at 17:01 UTC
    // Assume closest approach of Venus was in the direction of the velocity vector of Venus
    private static final GregorianCalendar venusEncounterDateTime =
            new GregorianCalendar(1974, 1, 5, 17, 1, 0);
    private static final Vector3D venusPositionEncounter = EphemerisSolarSystem.getInstance().getBodyPosition("Venus", venusEncounterDateTime);
    private static final Vector3D venusVelocityEncounter = EphemerisSolarSystem.getInstance().getBodyVelocity("Venus", venusEncounterDateTime);
    private static final double distanceEncounterVenus = 0.5 * SolarSystemParameters.getInstance().getDiameter("Venus") + 5768000;
    private static final Vector3D spacecraftPositionEncounterVenus =
            venusPositionEncounter.plus(venusVelocityEncounter.normalize().scalarProduct(distanceEncounterVenus));

    // Determine orbital parameters for the flight near Venus with center body Venus
    private static final GregorianCalendar arrivalVenusDateTime =
            new GregorianCalendar(1974, 1, 5, 0, 0, 0);
    private static final GregorianCalendar departureVenusDateTime =
            new GregorianCalendar(1974, 1, 6, 12, 0, 0);

    // Determine orbital parameters for Venus to Mercury
    // First Mercury encounter took place on 29 March 1974 at 20:47 UTC
    // Mariner 10 passed Mercury at the shadow side with closest distance 703 km
    private static final GregorianCalendar mercuryEncounterDateTimeA =
            new GregorianCalendar(1974, 2, 29, 20, 47, 0);
    private static final Vector3D mercuryPositionA = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", mercuryEncounterDateTimeA);
    private static final double distanceEncounterMercuryA = 0.5 * SolarSystemParameters.getInstance().getDiameter("Mercury") + 703000;
    private static final double xShiftEncounterA = 0.0;
    private static final double yShiftEncounterA = -distanceEncounterMercuryA;
    private static final double zShiftEncounterA = 0.0;
    private static final Vector3D spacecraftPositionEncounterMercuryA =
            mercuryPositionA.plus(new Vector3D(xShiftEncounterA, yShiftEncounterA, zShiftEncounterA));

    // Determine orbital parameters for the flight near Mercury with center body Mercury for first encounter
    private static final GregorianCalendar arrivalMercuryDateTimeA =
            new GregorianCalendar(1974, 2, 29, 20, 0, 0);
    private static final GregorianCalendar departureMercuryDateTimeA =
            new GregorianCalendar(1974, 2, 29, 21, 30, 0);

    // Determine orbital parameters for trajectory from first Mercury encounter to second Mercury encounter
    // Second Mercury encounter took place on 21 September 1974 at 20:59 UTC
    // Mariner 10 passed Mercury below the southern hemisphere at a distance of 48,069 km
    private static final GregorianCalendar mercuryEncounterDateTimeB =
            new GregorianCalendar(1974, 8, 21, 20, 59, 0);
    private static final Vector3D mercuryPositionB = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", mercuryEncounterDateTimeB);
    private static final double distanceEncounterMercuryB = 0.5 * SolarSystemParameters.getInstance().getDiameter("Mercury") + 48069000;
    private static final double xShiftEncounterB = 0.0;
    private static final double yShiftEncounterB = Math.sqrt(0.5)*distanceEncounterMercuryB;
    private static final double zShiftEncounterB = -Math.sqrt(0.5)*distanceEncounterMercuryB;
    private static final Vector3D spacecraftPositionEncounterMercuryB =
            mercuryPositionB.plus(new Vector3D(xShiftEncounterB, yShiftEncounterB, zShiftEncounterB));

    // Arrival at Mercury and departure from Mercury during second encounter
    // Orbital parameters for the flight near Mercury during second encounter have center body Sun
    private static final GregorianCalendar arrivalMercuryDateTimeB =
            new GregorianCalendar(1974, 8, 21, 0, 0, 0);
    private static final GregorianCalendar departureMercuryDateTimeB =
            new GregorianCalendar(1974, 8, 22, 12, 0, 0);

    // Determine orbital parameters for second Mercury encounter to third Mercury encounter
    // Third Mercury encounter took place on 16 March 1975 at 22:39 UTC
    // Mariner 10 passed Mercury almost over the north pole at a distance of 327 km
    private static final GregorianCalendar mercuryEncounterDateTimeC =
            new GregorianCalendar(1975, 2, 16, 22, 39, 0);
    private static final Vector3D mercuryPositionC = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", mercuryEncounterDateTimeC);
    private static final double distanceEncounterMercuryC = 0.5 * SolarSystemParameters.getInstance().getDiameter("Mercury") + 327000;
    private static final double angleRadEncounterC = Math.toRadians(70.0);
    private static final double xShiftEncounterC = 0.0;
    private static final double yShiftEncounterC = -Math.cos(angleRadEncounterC)*distanceEncounterMercuryC;
    private static final double zShiftEncounterC = Math.sin(angleRadEncounterC)*distanceEncounterMercuryC;
    private static final Vector3D spacecraftPositionEncounterMercuryC =
            mercuryPositionC.plus(new Vector3D(xShiftEncounterC, yShiftEncounterC, zShiftEncounterC));

    // Determine orbital parameters for the flight near Mercury with center body Mercury for third encounter
    private static final GregorianCalendar arrivalMercuryDateTimeC =
            new GregorianCalendar(1975, 2, 16, 21, 40, 0);
    private static final GregorianCalendar departureMercuryDateTimeC =
            new GregorianCalendar(1975, 2, 16, 23, 40, 0);

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public MarinerTen(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {

        // Orbital parameters with center body Earth for launch till launch from parking orbit
        double[] orbitParsLaunch =
                computeOrbitalParameters(launch, launchFromParkingOrbitDateTime,
                        positionLaunchRelativeToEarth, positionLaunchFromParkingOrbitRelativeToEarth, "Earth", false ,0);

        // Orbital parameters with center body Sun for trajectory Earth to Venus
        double[] orbitParsEarthVenus =
                computeOrbitalParameters(launchFromParkingOrbitDateTime, venusEncounterDateTime,
                        spacecraftPositionLaunchFromParkingOrbit, spacecraftPositionEncounterVenus, "Sun", false ,0);

        // Position of spacecraft with respect to the Sun when leaving the Earth
        double[] orbitElementsLeavingEarthRelativeToSun = EphemerisUtil.computeOrbitalElements(orbitParsEarthVenus, leavingEarthDateTime);
        Vector3D positionLeavingEarthRelativeToSun = EphemerisUtil.computePosition(orbitElementsLeavingEarthRelativeToSun);
        Vector3D earthPositionLeavingEarth = EphemerisSolarSystem.getInstance().getBodyPosition("Earth", leavingEarthDateTime);
        Vector3D positionLeavingEarthRelativeToEarth = positionLeavingEarthRelativeToSun.minus(earthPositionLeavingEarth);

        // Orbital parameters with center body Earth for launch from parking orbit until spacecraft leaves the Earth
        double[] orbitParsNearEarth =
                computeOrbitalParameters(launchFromParkingOrbitDateTime, leavingEarthDateTime,
                        positionLaunchFromParkingOrbitRelativeToEarth, positionLeavingEarthRelativeToEarth, "Earth", false, 0);

        // Position of spacecraft at Venus arrival
        double[] orbitElementsArrivalVenusRelativeToSun = EphemerisUtil.computeOrbitalElements(orbitParsEarthVenus, arrivalVenusDateTime);
        Vector3D positionArrivalVenusRelativeToSun = EphemerisUtil.computePosition(orbitElementsArrivalVenusRelativeToSun);
        Vector3D positionVenusArrivalVenus = EphemerisSolarSystem.getInstance().getBodyPosition("Venus", arrivalVenusDateTime);
        Vector3D positionArrivalVenusRelativeToVenus = positionArrivalVenusRelativeToSun.minus(positionVenusArrivalVenus);

        // Position of spacecraft during encounter with Venus
        Vector3D positionVenusEncounterVenus = EphemerisSolarSystem.getInstance().getBodyPosition("Venus", venusEncounterDateTime);
        Vector3D positionEncounterVenusRelativeToVenus = spacecraftPositionEncounterVenus.minus(positionVenusEncounterVenus);

        // Orbital parameters with center body Venus during Venus encounter
        double[] orbitParsEncounterVenus =
                computeOrbitalParameters(arrivalVenusDateTime, venusEncounterDateTime,
                        positionArrivalVenusRelativeToVenus, positionEncounterVenusRelativeToVenus, "Venus", false, 0);

        // Position of spacecraft when leaving Venus
        double[] orbitElementsDepartureVenusRelativeToVenus = EphemerisUtil.computeOrbitalElements(orbitParsEncounterVenus, departureVenusDateTime);
        Vector3D positionDepartureVenusRelativeToVenus = EphemerisUtil.computePosition(orbitElementsDepartureVenusRelativeToVenus);
        Vector3D positionVenusDepartureVenus = EphemerisSolarSystem.getInstance().getBodyPosition("Venus", departureVenusDateTime);
        Vector3D positionDepartureVenusRelativeToSun = positionVenusDepartureVenus.plus(positionDepartureVenusRelativeToVenus);

        // Orbital parameters with center body Sun from departure from Venus until arrival at Mercury
        double[] orbitParsVenusMercuryA =
                computeOrbitalParameters(departureVenusDateTime, mercuryEncounterDateTimeA,
                        positionDepartureVenusRelativeToSun, spacecraftPositionEncounterMercuryA, "Sun", false ,0);

        // Position of spacecraft at TCM after first Mercury encounter
        double[] orbitElementsCorrectionH = EphemerisUtil.computeOrbitalElements(orbitParsVenusMercuryA, correctionH);
        Vector3D spacecraftPositionCorrectionH = EphemerisUtil.computePosition(orbitElementsCorrectionH);

        // Orbital parameters for TCM after first Mercury encounter until second Mercury encounter
        double[] orbitParsCorrectionHMercuryB =
                computeOrbitalParameters(correctionH, mercuryEncounterDateTimeB,
                        spacecraftPositionCorrectionH, spacecraftPositionEncounterMercuryB, "Sun", false ,0);

        // Position of spacecraft at TCM after second Mercury encounter
        double[] orbitElementsCorrectionL = EphemerisUtil.computeOrbitalElements(orbitParsCorrectionHMercuryB, correctionL);
        Vector3D spacecraftPositionCorrectionL = EphemerisUtil.computePosition(orbitElementsCorrectionL);

        // Orbital parameters with center body Sun from second Mercury encounter until first TCM after Mercury encounter
        double[] orbitParsMercuryBCorrectionL =
                computeOrbitalParameters(mercuryEncounterDateTimeB, correctionL,
                        spacecraftPositionEncounterMercuryB, spacecraftPositionCorrectionL, "Sun", false ,0);

        // Orbital parameters for TCM after second Mercury encounter until third Mercury encounter
        double[] orbitParsCorrectionLMercuryC =
                computeOrbitalParameters(correctionL, mercuryEncounterDateTimeC,
                        spacecraftPositionCorrectionL, spacecraftPositionEncounterMercuryC, "Sun", false ,0);

        // Position of spacecraft when it was deactivated after third Mercury encounter
        double[] orbitElementsDeactivated = EphemerisUtil.computeOrbitalElements(orbitParsCorrectionLMercuryC, deactivated);
        Vector3D spacecraftPositionDeactivated = EphemerisUtil.computePosition(orbitElementsDeactivated);

        // Position of spacecraft at Mercury arrival for first encounter
        double[] orbitElementsArrivalMercuryRelativeToSunA = EphemerisUtil.computeOrbitalElements(orbitParsVenusMercuryA, arrivalMercuryDateTimeA);
        Vector3D positionArrivalMercuryRelativeToSunA = EphemerisUtil.computePosition(orbitElementsArrivalMercuryRelativeToSunA);
        Vector3D positionMercuryArrivalMercuryA = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", arrivalMercuryDateTimeA);
        Vector3D positionArrivalMercuryRelativeToMercuryA = positionArrivalMercuryRelativeToSunA.minus(positionMercuryArrivalMercuryA);

        // Position of spacecraft at Mercury during first encounter
        Vector3D positionMercuryEncounterMercuryA = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", mercuryEncounterDateTimeA);
        Vector3D positionEncounterMercuryRelativeToMercuryA = spacecraftPositionEncounterMercuryA.minus(positionMercuryEncounterMercuryA);

        // Orbital parameters with center body Mercury during first Mercury encounter
        double[] orbitParsEncounterMercuryA =
                computeOrbitalParameters(arrivalMercuryDateTimeA, mercuryEncounterDateTimeA,
                        positionArrivalMercuryRelativeToMercuryA, positionEncounterMercuryRelativeToMercuryA, "Mercury", false, 0);

        // Position of spacecraft when leaving Mercury after first encounter
        double[] orbitElementsDepartureMercuryRelativeToMercuryA = EphemerisUtil.computeOrbitalElements(orbitParsEncounterMercuryA, departureMercuryDateTimeA);
        Vector3D positionDepartureMercuryRelativeToMercuryA = EphemerisUtil.computePosition(orbitElementsDepartureMercuryRelativeToMercuryA);
        Vector3D positionMercuryDepartureMercuryA = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", departureMercuryDateTimeA);
        Vector3D positionDepartureMercuryRelativeToSunA = positionMercuryDepartureMercuryA.plus(positionDepartureMercuryRelativeToMercuryA);

        // Orbital parameters with center body Sun from departure from first Mercury encounter until first TCM after Mercury encounter
        double[] orbitParsMercuryACorrectionH =
                computeOrbitalParameters(departureMercuryDateTimeA, correctionH,
                        positionDepartureMercuryRelativeToSunA, spacecraftPositionCorrectionH, "Sun", false ,0);

        // Position of spacecraft at Mercury arrival for third encounter
        double[] orbitElementsArrivalMercuryRelativeToSunC = EphemerisUtil.computeOrbitalElements(orbitParsCorrectionLMercuryC, arrivalMercuryDateTimeC);
        Vector3D positionArrivalMercuryRelativeToSunC = EphemerisUtil.computePosition(orbitElementsArrivalMercuryRelativeToSunC);
        Vector3D positionMercuryArrivalMercuryC = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", arrivalMercuryDateTimeC);
        Vector3D positionArrivalMercuryRelativeToMercuryC = positionArrivalMercuryRelativeToSunC.minus(positionMercuryArrivalMercuryC);

        // Position of spacecraft at Mercury during third encounter
        Vector3D positionMercuryEncounterMercuryC = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", mercuryEncounterDateTimeC);
        Vector3D positionEncounterMercuryRelativeToMercuryC = spacecraftPositionEncounterMercuryC.minus(positionMercuryEncounterMercuryC);

        // Orbital parameters with center body Mercury during third Mercury encounter
        double[] orbitParsEncounterMercuryC =
                computeOrbitalParameters(arrivalMercuryDateTimeC, mercuryEncounterDateTimeC,
                        positionArrivalMercuryRelativeToMercuryC, positionEncounterMercuryRelativeToMercuryC, "Mercury", false, 0);

        // Position of spacecraft when leaving Mercury after third encounter
        double[] orbitElementsDepartureMercuryRelativeToMercuryC = EphemerisUtil.computeOrbitalElements(orbitParsEncounterMercuryC, departureMercuryDateTimeC);
        Vector3D positionDepartureMercuryRelativeToMercuryC = EphemerisUtil.computePosition(orbitElementsDepartureMercuryRelativeToMercuryC);
        Vector3D positionMercuryDepartureMercuryC = EphemerisSolarSystem.getInstance().getBodyPosition("Mercury", departureMercuryDateTimeC);
        Vector3D positionDepartureMercuryRelativeToSunC = positionMercuryDepartureMercuryC.plus(positionDepartureMercuryRelativeToMercuryC);

        // Orbital parameters with center body Sun from departure from third Mercury encounter until spacecraft was deactivated
        double[] orbitParsMercuryCDeactivated =
                computeOrbitalParameters(departureMercuryDateTimeC, deactivated,
                        positionDepartureMercuryRelativeToSunC, spacecraftPositionDeactivated, "Sun", false ,0);

        // Trajectory for launch until launch from parking orbit
        SpacecraftTrajectory trajectoryLaunch =
                new SpacecraftTrajectory(launch, launchFromParkingOrbitDateTime, "Earth", orbitParsLaunch);

        // Trajectory for launch from parking orbit until spacecraft leaves the Earth
        SpacecraftTrajectory trajectoryNearEarth =
                new SpacecraftTrajectory(launchFromParkingOrbitDateTime, leavingEarthDateTime, "Earth", orbitParsNearEarth);

        // Trajectory for leaving Earth until arrival at Venus
        SpacecraftTrajectory trajectoryEarthVenus =
                new SpacecraftTrajectory(leavingEarthDateTime, arrivalVenusDateTime, "Sun", orbitParsEarthVenus);

        // Trajectory during Venus encounter
        SpacecraftTrajectory trajectoryVenusEncounter =
                new SpacecraftTrajectory(arrivalVenusDateTime, departureVenusDateTime, "Venus", orbitParsEncounterVenus);

        // Trajectory for departure from Venus until arrival at Mercury for first encounter
        SpacecraftTrajectory trajectoryVenusMercuryA =
                new SpacecraftTrajectory(departureVenusDateTime, arrivalMercuryDateTimeA, "Sun", orbitParsVenusMercuryA);

        // Trajectory during first Mercury encounter
        SpacecraftTrajectory trajectoryMercuryEncounterA =
                new SpacecraftTrajectory(arrivalMercuryDateTimeA, departureMercuryDateTimeA, "Mercury", orbitParsEncounterMercuryA);

        // Trajectory for departure from Mercury until TCM after first Mercury encounter
        SpacecraftTrajectory trajectoryMercuryACorrectionH =
                new SpacecraftTrajectory(departureMercuryDateTimeA, correctionH, "Sun", orbitParsMercuryACorrectionH);

        // Trajectory for TCM after first Mercury encounter until second encounter with Mercury
        SpacecraftTrajectory trajectoryCorrectionHMercuryB =
                new SpacecraftTrajectory(correctionH, mercuryEncounterDateTimeB, "Sun", orbitParsCorrectionHMercuryB);

        // Trajectory for second encounter with Mercury until TCM after second Mercury encounter
        SpacecraftTrajectory trajectoryMercuryBCorrectionL =
                new SpacecraftTrajectory(mercuryEncounterDateTimeB, correctionL, "Sun", orbitParsMercuryBCorrectionL);

        // Trajectory for TCM after second Mercury encounter until arrival at Mercury for third encounter
        SpacecraftTrajectory trajectoryCorrectionLMercuryC =
                new SpacecraftTrajectory(correctionL, arrivalMercuryDateTimeC, "Sun", orbitParsCorrectionLMercuryC);

        // Trajectory during third Mercury encounter
        SpacecraftTrajectory trajectoryMercuryEncounterC =
                new SpacecraftTrajectory(arrivalMercuryDateTimeC, departureMercuryDateTimeC, "Mercury", orbitParsEncounterMercuryC);

        // Trajectory for departure from Mercury until spacecraft was deactivated
        SpacecraftTrajectory trajectoryMercuryCDeactivated =
                new SpacecraftTrajectory(departureMercuryDateTimeC, deactivated, "Sun", orbitParsMercuryCDeactivated);

        // Create list of trajectories
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        trajectories.add(trajectoryLaunch);
        trajectories.add(trajectoryNearEarth);
        trajectories.add(trajectoryEarthVenus);
        trajectories.add(trajectoryVenusEncounter);
        trajectories.add(trajectoryVenusMercuryA);
        trajectories.add(trajectoryMercuryEncounterA);
        trajectories.add(trajectoryMercuryACorrectionH);
        trajectories.add(trajectoryCorrectionHMercuryB);
        trajectories.add(trajectoryMercuryBCorrectionL);
        trajectories.add(trajectoryCorrectionLMercuryC);
        trajectories.add(trajectoryMercuryEncounterC);
        trajectories.add(trajectoryMercuryCDeactivated);
        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launchFromParkingOrbitDateTime)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(leavingEarthDateTime)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionC)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionD)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(arrivalVenusDateTime)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(departureVenusDateTime)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionE)));
        //solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionF)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(arrivalMercuryDateTimeA)));
        //solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionG)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(departureMercuryDateTimeA)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionH)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionI)));
        //solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionJ)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(arrivalMercuryDateTimeB)));
        //solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionK)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(departureMercuryDateTimeB)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionL)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionM)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionN)));
        //solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionO)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(arrivalMercuryDateTimeC)));
        //solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(correctionP)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(departureMercuryDateTimeC)));
    }


    /**
     * Determine orbital parameters for Mariner 10 for given ephemeris date/time.
     * @param ephemerisDateTime Date/time for which orbital parameters should be determined
     * @param encounterDateTime Date/time of encounter with planet
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

        // Obtain desired velocity of spacecraft at time of ephemeris by solving Lambert's problem
        double tof = (encounterDateTime.getTimeInMillis() - ephemerisDateTime.getTimeInMillis()) / 1000.0;
        double mu = SolarSystemParameters.getInstance().getMu(centerBodyName);
        Vector3D ephemerisVelocity = new Vector3D();
        try {
            LambertProblem lambertProblem = new LambertProblem(ephemerisPosition, encounterPosition, tof, mu, cw, multiRevs);
            int index = lambertProblem.getMaxNumberRevolutions();
            ephemerisVelocity = lambertProblem.getAllVelocities1()[index];
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }

        // Compute orbital parameters from position and velocity of spacecraft at time of ephemeris
        double[] orbitParametersSpacecraft = EphemerisUtil.computeOrbitalParametersFromPositionVelocity(mu,
                ephemerisPosition, ephemerisVelocity, ephemerisDateTime);

        return orbitParametersSpacecraft;
    }
}
