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

import ephemeris.*;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class ApolloEight extends Spacecraft implements Serializable {

    /**
     * Apollo 8 was the first crewed spacecraft to leave low Earth orbit and the first to
     * reach the Moon, orbit it, and return. Its three-astronaut crew — Frank Borman,
     * James Lovell, and William Anders — were the first humans to fly to the Moon,
     * to witness and photograph an Earthrise, and to escape the gravity of a celestial body.
     *
     * Launch 21 December 1968 at 12.51.00 UTC
     * Splashdown 27 December 1968 at 15.52 UTC
     *
     * https://en.wikipedia.org/wiki/Apollo_8
     *
     * National Aeronautics and Space Administration - First Manned Lunar Orbit Mision
     * Press Kit Project Apollo 8 - Release date Sunday December 15, 1968
     * https://www.nasa.gov/specials/apollo50th/pdf/A08_PressKit.pdf
     *
     * NASA Scientific Visualization Studio - Earthrise
     * https://svs.gsfc.nasa.gov/3936
     *
     * Trajectory parameters obtained from
     * National Aeronautics and Space Administration - Apollo 8 Mission Report
     * https://www.hq.nasa.gov/alsj/a410/A08_MissionReport.pdf
     *
     * Trajectory parameters obtained from
     * National Aeronautics and Space Administration - Apollo 8 Mission Report
     * Supplement 1 - Trajectory Reconstruction and Postflight Analysis
     * November 1969
     * https://www.ibiblio.org/apollo/Documents/19740072902.pdf
     * Note that coordinates published in this document are for the Besselian year 1969,
     * see NASA's publication on Earthrise: https://svs.gsfc.nasa.gov/3936
     *
     * Apollo 8 The Second Mission: Testing the CSM in Lunar Orbit 21 Dec - 17 Dec 1968
     * https://history.nasa.gov/SP-4029/Apollo_08a_Summary.htm
     *
     * https://vivliostyle.github.io/vivliostyle_doc/samples/adaptive-layout/apollo8/index.xhtml
     *
     * Translunar Injection Apollo 8 and Apollo 10 through Apollo 17
     * http://www.apolloproject.com/sp-4029/Apollo_18-24_Translunar_Injection.htm
     *
     * IBM Real-Time Computer Complex
     * https://www.ibm.com/ibm/history/exhibits/space/space_realtime.html
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // One foot = 0.3048 m
    // https://en.wikipedia.org/wiki/Foot_(unit)
    private static final double FOOT = 0.3048;

    // One nautical mile = 1852 m
    // https://en.wikipedia.org/wiki/Nautical_mile
    private static final double NAUTICALMILE = 1852.0;

    // Equatorial radius of the Earth [m]
    // https://en.wikipedia.org/wiki/Earth_radius
    private static final double EARTHRADIUS = 6378137.0;

    // Launch 21 December 1968 at 12:51:00 UTC (flight time 00:00)
    private static final GregorianCalendar launch =
            new GregorianCalendar(1968, 11, 21, 12, 51, 0);

    // S-IC center engine cutoff 21 December 1968 at 12:53:06 UTC (flight time 00:02:05.9)
    private static final GregorianCalendar centerEngineSICcutoff =
            new GregorianCalendar(1968, 11, 21, 12, 53, 6);

    // S-IC outboard engine cutoff 21 December 1968 at 12:53:34 UTC (flight time 00:02:33.8)
    private static final GregorianCalendar outboardEngineSICcutoff =
            new GregorianCalendar(1968, 11, 21, 12, 53, 34);

    // S-II engine cutoff 21 December 1968 at 12:59:44 UTC (flight time 00:08:44.0)
    private static final GregorianCalendar engineSIIcutoff =
            new GregorianCalendar(1968, 11, 21, 12, 59, 44);

    // S-IVB engine cutoff 21 December 1968 at 13:02:25 UTC (flight time 00:11:25.0)
    private static final GregorianCalendar firstSIVBcutoff =
            new GregorianCalendar(1968, 11, 21, 13, 02, 25);

    // Parking orbit insertion 21 December 1968 at 13:02:35 UTC (flight time 00:11:35.0)
    private static final GregorianCalendar parkingOrbitInsertion =
            new GregorianCalendar(1968, 11, 21, 13, 02, 35);

    // S-IVB restart preparation 21 December 1968 at 15:32:00 UTC (flight time 02:40:59.5)
    private static final GregorianCalendar preparationSIVBrestart =
            new GregorianCalendar(1968, 11, 21, 15, 32, 0);

    // S-IVB ignition 21 December 1968 at 15:41:37 UTC (flight time 02:50:37.1)
    private static final GregorianCalendar secondSIVBignition =
            new GregorianCalendar(1968, 11, 21, 15, 41, 37);

    // S-IVB cutoff 21 December 1968 at 15:46:56 UTC (flight time 02:55:55.5)
    private static final GregorianCalendar secondSIVBcutoff =
            new GregorianCalendar(1968, 11, 21, 15, 46, 56);

    // Translunar injection 21 December 1968 at 15:47:05 UTC (flight time 02:56:05.5)
    private static final GregorianCalendar transLunarInjection =
            new GregorianCalendar(1968, 11, 21, 15, 47, 05);

    // Spacecraft/S-IVB separation on 21 December 1968 at 16:11:59 UTC (flight time 03:20:59)
    private static final GregorianCalendar separationSIVB =
            new GregorianCalendar(1968, 11, 21, 16, 11, 59);

    // CMC Nav update ACNX114 (ECI) 21 December 1968 at 17:36:54 (UTC)
    private static final GregorianCalendar dateTimeACNX114 =
            new GregorianCalendar(1968, 11, 21, 17, 36, 54);

    // First midcourse correction cutoff 21 December 1968 at 23:51:02 UTC (flight time 11:00:01.9))
    private static final GregorianCalendar firstMidcourseCorrection =
            new GregorianCalendar(1968, 11, 21, 23, 51, 2);

    // Trajectory update RIDX251 (ECI) 22 December 1968 at 0:46:36 (UTC)
    private static final GregorianCalendar dateTimeRIDX251 =
            new GregorianCalendar(1968, 11, 22, 0, 46, 36);

    // Trajectory update GWMX337 (ECI) 22 December 1968 at 19:05:54 (UTC)
    private static final GregorianCalendar dateTimeGWMX337 =
            new GregorianCalendar(1968, 11, 22, 19, 5, 54);

    // CMC Nav update for MCC-4 GDSX374 (ECI) 23 December 1968 at 04:47:48 (UTC)
    private static final GregorianCalendar dateTimeGDSX374 =
            new GregorianCalendar(1968, 11, 23, 4, 47, 48);

    // Second midcourse correction cutoff 24 December 1968 at 01:51:08 UTC (flight time 61:00:07.8))
    private static final GregorianCalendar secondMidcourseCorrection =
            new GregorianCalendar(1968, 11, 24, 1, 51, 8);

    // CMC Nav update for LOI1 HSKX417 (MCI) 24 December 1968 at 01:57:36
    private static final GregorianCalendar dateTimeHSKX417 =
            new GregorianCalendar(1968, 11, 24, 1, 57, 36);

    // Lunar orbit insertion cutoff 24 December 1968 at 10:03:27 UTC (flight time 69:12:27.3)
    private static final GregorianCalendar lunarOrbitInsertion =
            new GregorianCalendar(1968, 11, 24, 10, 03, 27);

    // Lunar orbit Rev 1 HSKX470 (MCI) 24 December 1968 at 10:24:00
    private static final GregorianCalendar dateTimeHSKX470 =
            new GregorianCalendar(1968, 11, 24, 10, 24, 0);

    // Lunar orbit circulation cutoff 24 December 1968 at 14:26:16 UTC (flight time 73:35:16)
    private static final GregorianCalendar lunarOrbitCirculation =
            new GregorianCalendar(1968, 11, 24, 14, 26, 16);

    // Lunar orbit Rev 4 MADX530 (MCI) 24 December 1968 at 16:39:06
    private static final GregorianCalendar dateTimeMADX530 =
            new GregorianCalendar(1968, 11, 24, 16, 39, 6);

    // Transearth injection cutoff 25 December 1968 at 06:13:40 UTC (flight time 89:22:40.3)
    private static final GregorianCalendar transearthInjection =
            new GregorianCalendar(1968, 11, 25, 6, 13, 40);

    // CMC Nav update for MCC-5 BDAX690 (MCI) 25 December 1968 at 06:38:36 UTC
    private static final GregorianCalendar dateTimeBDAX690 =
            new GregorianCalendar(1968, 11, 25, 6, 38, 36);

    // Third midcourse correction ignition 25 December 1968 at 20:50:54 UTC (flight time 103:59:54)
    private static final GregorianCalendar thirdMidcourseCorrectionIgnition =
            new GregorianCalendar(1968, 11, 25, 20, 50, 54);

    // Third midcourse correction cutoff 25 December 1968 at 20:51:08 UTC (flight time 104:00:08)
    private static final GregorianCalendar thirdMidcourseCorrectionCutoff =
            new GregorianCalendar(1968, 11, 25, 20, 51, 8);

    // Trajectory update GWMX777 (ECI) 25 December 1968 at 20:57:24 UTC
    private static final GregorianCalendar dateTimeGWMX777 =
            new GregorianCalendar(1968, 11, 25, 20, 57, 24);

    // Trajectory update CROX856 (ECI) 26 December 1968 at 22:16:24 UTC
    private static final GregorianCalendar dateTimeCROX856 =
            new GregorianCalendar(1968, 11, 26, 22, 16, 24);

    // Trajectory update NBEX863 (ECI) 27 December 1968 at 00:15:42 UTC
    private static final GregorianCalendar dateTimeNBEX863 =
            new GregorianCalendar(1968, 11, 27, 0, 15, 42);

    // Entry trajectory initialization 27 December 1968 at 15:36:52 (flight time 146:45:52.10, altitude = 496428.7 ft)
    private static final GregorianCalendar entryTrajectoryInitialization =
            new GregorianCalendar(1968,11,27,15,36,52);

    // Splashdown 27 December 1968 at 15.52 UTC
    private static final GregorianCalendar splashdown =
            new GregorianCalendar(1968, 11, 27, 15, 52, 0);

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public ApolloEight(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {

        List<SpacecraftTrajectory> trajectories = new ArrayList<>();

        // Gravitational parameter of the Earth
        double muEarth = SolarSystemParameters.getInstance().getMu("Earth");

        // S-IC center engine cutoff 21 December 1968 at 12:53:06 UTC (flight time 00:02:05.9)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeSICA          =    28.72; // Latitude 28.72 North [degrees]
        double longitudeSICA         =   -80.19; // Longitude 80.19 West [degrees]
        double altideSICA            =    22.4;  // Altitude [nautical miles]
        double velocityMagnitudeSICA =  6214.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleSICA      =    24.53; // Space-fixed flight-path angle [degrees]
        double headingSICA           =    76.57; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocitySICA = convertEarthTrajectoryParametersToPositionVelocity(
                        centerEngineSICcutoff, latitudeSICA, longitudeSICA, altideSICA,
                        velocityMagnitudeSICA, headingSICA, flightAgngleSICA);
        Vector3D positionSICA = positionVelocitySICA[0]; // Related to Earth
        Vector3D velocitySICA = positionVelocitySICA[1]; // Related to Earth
        double[] orbitParsCenterEngineSICcutoff =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionSICA, velocitySICA, centerEngineSICcutoff);

        // S-IC outboard engine cutoff 21 December 1968 at 12:53:34 UTC (flight time 00:02:33.8)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeSICB          =    28.85; // Latitude 28.85 North [degrees]
        double longitudeSICB         =   -79.73; // Longitude 79.73 West [degrees]
        double altideSICB            =    35.5;  // Altitude [nautical miles]
        double velocityMagnitudeSICB =  8900.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleSICB      =    20.70; // Space-fixed flight-path angle [degrees]
        double headingSICB           =    75.39; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocitySICB = convertEarthTrajectoryParametersToPositionVelocity(
                        outboardEngineSICcutoff, latitudeSICB, longitudeSICB, altideSICB,
                        velocityMagnitudeSICB, headingSICB, flightAgngleSICB);
        Vector3D positionSICB = positionVelocitySICB[0]; // Related to Earth
        Vector3D velocitySICB = positionVelocitySICB[1]; // Related to Earth
        double[] orbitParsOutboardEngineSICcutoff =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionSICB, velocitySICB, outboardEngineSICcutoff);

        // S-II engine cutoff 21 December 1968 at 12:59:44 UTC (flight time 00:08:44.0)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeSII          =    31.72; // Latitude 31.72 North [degrees]
        double longitudeSII         =   -65.39; // Longitude 65.39 West [degrees]
        double altideSII            =   103.4;  // Altitude [nautical miles]
        double velocityMagnitudeSII = 22379.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleSII      =     0.65; // Space-fixed flight-path angle [degrees]
        double headingSII           =    81.78; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocitySII = convertEarthTrajectoryParametersToPositionVelocity(
                        engineSIIcutoff, latitudeSII, longitudeSII, altideSII,
                        velocityMagnitudeSII, headingSII, flightAgngleSII);
        Vector3D positionSII = positionVelocitySII[0]; // Related to Earth
        Vector3D velocitySII = positionVelocitySII[1]; // Related to Earth
        double[] orbitParsEngineSIIcutoff =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionSII, velocitySII, engineSIIcutoff);

        // First S-IVB engine cutoff 21 December 1968 at 13:02:25 UTC (flight time 00:11:25.0)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeSIVBfirst          =    32.63; // Latitude 32.63 North [degrees]
        double longitudeSIVBfirst         =   -54.06; // Longitude 54.06 West [degrees]
        double altideSIVBfirst            =   103.3;  // Altitude [nautical miles]
        double velocityMagnitudeSIVBfirst = 25562.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleSIVBfirst      =     0.00; // Space-fixed flight-path angle [degrees]
        double headingSIVBfirst           =    88.10; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocitySIVBfirst = convertEarthTrajectoryParametersToPositionVelocity(
                        firstSIVBcutoff, latitudeSIVBfirst, longitudeSIVBfirst, altideSIVBfirst,
                        velocityMagnitudeSIVBfirst, headingSIVBfirst, flightAgngleSIVBfirst);
        Vector3D positionSIVBfirst = positionVelocitySIVBfirst[0]; // Related to Earth
        Vector3D velocitySIVBfirst = positionVelocitySIVBfirst[1]; // Related to Earth
        double[] orbitParsFirstSIVBcutoff =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionSIVBfirst, velocitySIVBfirst, firstSIVBcutoff);

        // Parking orbit insertion 21 December 1968 at 13:02:35 UTC (flight time 00:11:35.0)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudePOI          =    32.63; // Latitude 32.63 North [degrees]
        double longitudePOI         =   -54.06; // Longitude 54.06 West [degrees]
        double altidePOI            =   103.3;  // Altitude [nautical miles]
        double velocityMagnitudePOI = 25562.0;  // Space-fixed velocity [ft/sec]
        double flightAgnglePOI      =     0.00; // Space-fixed flight-path angle [degrees]
        double headingPOI           =    88.10; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocityPOI = convertEarthTrajectoryParametersToPositionVelocity(
                        parkingOrbitInsertion, latitudePOI, longitudePOI, altidePOI,
                        velocityMagnitudePOI, headingPOI, flightAgnglePOI);
        Vector3D positionPOI = positionVelocityPOI[0]; // Related to Earth
        Vector3D velocityPOI = positionVelocityPOI[1]; // Related to Earth
        double[] orbitParsParkingOrbitInsertion =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionPOI, velocityPOI, parkingOrbitInsertion);

        // Second S-IVB cutoff 21 December 1968 at 15:46:56 UTC (flight time 02:55:55.5)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeSIVBsecond          =    21.12; // Latitude 21.12 North [degrees]
        double longitudeSIVBsecond         =  -144.79; // Longitude 144.79 West [degrees]
        double altideSIVBsecond            =   179.3;  // Altitude [nautical miles]
        double velocityMagnitudeSIVBsecond = 35532.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleSIVBsecond      =     7.44; // Space-fixed flight-path angle [degrees]
        double headingSIVBsecond           =    67.16; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocitySIVBsecond = convertEarthTrajectoryParametersToPositionVelocity(
                        secondSIVBcutoff, latitudeSIVBsecond, longitudeSIVBsecond, altideSIVBsecond,
                        velocityMagnitudeSIVBsecond, headingSIVBsecond, flightAgngleSIVBsecond);
        Vector3D positionSIVBsecond = positionVelocitySIVBsecond[0]; // Related to Earth
        Vector3D velocitySIVBsecond = positionVelocitySIVBsecond[1]; // Related to Earth
        double[] orbitParsSecondSIVBcutoff =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionSIVBsecond, velocitySIVBsecond, secondSIVBcutoff);

        // Translunar injection 21 December 1968 at 15:47:05 UTC (flight time 02:56:05.5)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeTLI          =    21.48; // Latitude 21.48 North [degrees]
        double longitudeTLI         =  -143.02; // Longitude 143.02 West [degrees]
        double altideTLI            =   187.1;  // Altitude [nautical miles]
        double velocityMagnitudeTLI = 35505.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleTLI      =     7.90; // Space-fixed flight-path angle [degrees]
        double headingTLI           =    67.49; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocityTLI = convertEarthTrajectoryParametersToPositionVelocity(
                        transLunarInjection, latitudeTLI, longitudeTLI, altideTLI,
                        velocityMagnitudeTLI, headingTLI, flightAgngleTLI);
        Vector3D positionTLI = positionVelocityTLI[0]; // Related to Earth
        Vector3D velocityTLI = positionVelocityTLI[1]; // Related to Earth
        double[] orbitParsTransLunarInjection =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionTLI, velocityTLI, transLunarInjection);

        // First midcourse correction cutoff 21 December 1968 at 23:51 UTC (flight time 11:00:01.9))
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeFMC          =    -1.71; // Latitude 1.71 South [degrees]
        double longitudeFMC         =  -123.75; // Longitude 123.75 West [degrees]
        double altideFMC            = 52771.7;  // Altitude [nautical miles]
        double velocityMagnitudeFMC =  8172.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleFMC      =    73.75; // Space-fixed flight-path angle [degrees]
        double headingFMC           =   120.54; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocityFMC = convertEarthTrajectoryParametersToPositionVelocity(
                        firstMidcourseCorrection, latitudeFMC, longitudeFMC, altideFMC,
                        velocityMagnitudeFMC, headingFMC, flightAgngleFMC);
        Vector3D positionFMC = positionVelocityFMC[0]; // Related to Earth
        Vector3D velocityFMC = positionVelocityFMC[1]; // Related to Earth
        double[] orbitParsFirstMidcourseCorrection =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionFMC, velocityFMC, firstMidcourseCorrection);

        // CMC Nav update ACNX114 (ECI) 21 December 1968 at 17:36:54 (UTC)
        Vector3D positionACNX114 = new Vector3D(0.7009101885600919E8,-0.9791818362686327E8, 0.1986783119715735E8);
        Vector3D velocityACNX114 = new Vector3D(0.1239781389700085E5,-0.7792484085880250E4,-0.9231990610602071E3);
        double[] orbitParsACNX114 = computeOrbitalParametersECI(positionACNX114, velocityACNX114, dateTimeACNX114);

        // Trajectory update RIDX251 (ECI) 22 December 1968 at 0:46:36 (UTC)
        Vector3D positionRIDX251 = new Vector3D(0.3028613738208454E9,-0.2070581394026110E9,-0.1476941508035234E8);
        Vector3D velocityRIDX251 = new Vector3D(0.7199318757430428E4,-0.2702416036567140E4,-0.1381939510887763E4);
        double[] orbitParsRIDX251 = computeOrbitalParametersECI(positionRIDX251, velocityRIDX251, dateTimeRIDX251);

        // Trajectory update GWMX337 (ECI) 22 December 1968 at 19:05:54 (UTC)
        Vector3D positionGWMX337 = new Vector3D(0.6667998537303794E9,-0.3186791704143134E9,-0.9665110157397467E8);
        Vector3D velocityGWMX337 = new Vector3D(0.4438685189768659E4,-0.1123518118521038E4,-0.1119123143856043E4);
        double[] orbitParsGWMX337 = computeOrbitalParametersECI(positionGWMX337, velocityGWMX337, dateTimeGWMX337);

        // CMC Nav update for MCC-4 GDSX374 (ECI) 23 December 1968 at 04:47:48 (UTC)
        Vector3D positionGDSX374 = new Vector3D(0.8095798116060003E9,-0.3525155018494783E9,-0.1340804859433953E9);
        Vector3D velocityGDSX374 = new Vector3D(0.3778990371256117E4,-0.8391382200501079E3,-0.1029421049341403E4);
        double[] orbitParsGDSX374 = computeOrbitalParametersECI(positionGDSX374, velocityGDSX374, dateTimeGDSX374);

        // CMC Nav update for LOI1 HSKX417 (MCI) 24 December 1968 at 01:57:36
        Vector3D positionHSKX417 = new Vector3D(-0.2040511865131031E8, 0.1053210316162362E9, 0.7702548516859442E8);
        Vector3D velocityHSKX417 = new Vector3D( 0.1029293268646228E4,-0.3195724207390687E4,-0.2363168451485381E4);
        double[] orbitParsHSKX417 = computeOrbitalParametersMCI(positionHSKX417, velocityHSKX417, dateTimeHSKX417);

        // Lunar orbit Rev 1 HSKX470 (MCI) 24 December 1968 at 10:24:00
        Vector3D positionHSKX470 = new Vector3D(-0.6500301419376045E6,-0.5141146285857345E7,-0.3331757301070410E7);
        Vector3D velocityHSKX470 = new Vector3D(-0.5368479892376022E4, 0.8828009760134887E2, 0.5521537530737183E3);
        double[] orbitParsHSKX470 = computeOrbitalParametersMCI(positionHSKX470, velocityHSKX470, dateTimeHSKX470);

        // Lunar orbit Rev 4 MADX530 (MCI) 24 December 1968 at 16:39:06
        Vector3D positionMADX530 = new Vector3D(-0.2219481515628747E6,-0.5066114498637041E7,-0.3323216628434067E7);
        Vector3D velocityMADX530 = new Vector3D(-0.5327174723080408E4,-0.6278346322793148E2, 0.4537341239086353E3);
        double[] orbitParsMADX530 = computeOrbitalParametersMCI(positionMADX530, velocityMADX530, dateTimeMADX530);

        // CMC Nav update for MCC-5 BDAX690 (MCI) 25 December 1968 at 06:38:36 UTC
        Vector3D positionBDAX690 = new Vector3D(-0.4514872025422723E7,-0.9673074022523472E7,-0.6183341083551337E7);
        Vector3D velocityBDAX690 = new Vector3D(-0.6064523962043475E4,-0.3171951958472816E4,-0.1631713969719616E4);
        double[] orbitParsBDAX690 = computeOrbitalParametersMCI(positionBDAX690, velocityBDAX690, dateTimeBDAX690);

        // Trajectory update GWMX777 (ECI) 25 December 1968 at 20:57:24 (UTC)
        Vector3D positionGWMX777 = new Vector3D( 0.1016315147348728E10,-0.1812222096834394E9,-0.1022694679406591E9);
        Vector3D velocityGWMX777 = new Vector3D(-0.4018885713523305E4,  0.1284804826676237E4, 0.8435568814881932E3);
        double[] orbitParsGWMX777 = computeOrbitalParametersECI(positionGWMX777, velocityGWMX777, dateTimeGWMX777);

        // Trajectory update CROX856 (ECI) 26 December 1968 at 22:16:24 (UTC)
        Vector3D positionCROX856 = new Vector3D( 0.5793411134249913E9,-0.5115267687009499E8,-0.1862137658606085E8);
        Vector3D velocityCROX856 = new Vector3D(-0.5984266391387524E4, 0.1582063761351456E4, 0.9905498224071284E3);
        double[] orbitParsCROX856 = computeOrbitalParametersECI(positionCROX856, velocityCROX856, dateTimeCROX856);

        // Trajectory update NBEX863 (ECI) 27 December 1968 at 00:15:42 (UTC)
        Vector3D positionNBEX863 = new Vector3D( 0.5353918922668272E9,-0.3972826880786872E8,-0.1150160687704205E8);
        Vector3D velocityNBEX863 = new Vector3D(-0.6304215580623974E4, 0.1608486121318874E4, 0.9994594262706302E3);
        double[] orbitParsNBEX863 = computeOrbitalParametersECI(positionNBEX863, velocityNBEX863, dateTimeNBEX863);

        // Third midcourse correction ignition 25 December 1968 at 20:50:54 UTC (flight time 103:59:54)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeTMCA          =     -5.67; // Latitude 5.67 South [degrees]
        double longitudeTMCA         =    -57.27; // Longitude 57.27 West [degrees]
        double altideTMCA            = 165561.5;  // Altitude [nautical miles]
        double velocityMagnitudeTMCA =   4299.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleTMCA      =    -80.59; // Space-fixed flight-path angle [degrees]
        double headingTMCA           =     52.65; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocityTMCA = convertEarthTrajectoryParametersToPositionVelocity(
                        thirdMidcourseCorrectionIgnition, latitudeTMCA, longitudeTMCA, altideTMCA,
                        velocityMagnitudeTMCA, headingTMCA, flightAgngleTMCA);
        Vector3D positionTMCA = positionVelocityTMCA[0];
        Vector3D velocityTMCA = positionVelocityTMCA[1];
        double[] orbitParsThirdMidcourseCorrectionIgnition =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionTMCA, velocityTMCA, thirdMidcourseCorrectionIgnition);

        // Third midcourse correction cutoff 25 December 1968 at 20:51:08 UTC (flight time 104:00:08)
        // Apollo 8 Mission Report Table 5-II - Trajectory parameters
        double latitudeTMCB          =     -5.67; // Latitude 5.67 South [degrees]
        double longitudeTMCB         =    -57.33; // Longitude 57.33 West [degrees]
        double altideTMCB            = 167552.0;  // Altitude [nautical miles]
        double velocityMagnitudeTMCB =   4298.0;  // Space-fixed velocity [ft/sec]
        double flightAgngleTMCB      =    -80.60; // Space-fixed flight-path angle [degrees]
        double headingTMCB           =     52.65; // Space-fixed heading of angle [degrees East of North]
        Vector3D[] positionVelocityTMCB = convertEarthTrajectoryParametersToPositionVelocity(
                        thirdMidcourseCorrectionCutoff, latitudeTMCB, longitudeTMCB, altideTMCB,
                        velocityMagnitudeTMCB, headingTMCB, flightAgngleTMCB);
        Vector3D positionTMCB = positionVelocityTMCB[0];
        Vector3D velocityTMCB = positionVelocityTMCB[1];
        double[] orbitParsThirdMidcourseCorrectionCutoff =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                        muEarth, positionTMCB, velocityTMCB, thirdMidcourseCorrectionCutoff);

        // Entry trajectory initialization 27 December 1968 at 15:36:52 (flight time 146:45:52.10, altitude = 496428.7 ft)
        // Apollo 8 Mission Report Supplement 1 - Trajectory reconstruction and postflight analysis
        // Section 3.4 Entry Trajectory (page 3-17)
        Vector3D positionETI = new Vector3D(-16379409.7, 11270585.0, 7948921.5);
        Vector3D velocityETI = new Vector3D(-19430.2512, -23956.4296, -18830.7826);
        double[] orbitParsEntryTrajectory = computeOrbitalParametersECI(positionETI, velocityETI, entryTrajectoryInitialization);

        // From launch till S-IC center engine cutoff
        trajectories.add(
                new SpacecraftTrajectory(launch, centerEngineSICcutoff,
                        "Earth", orbitParsCenterEngineSICcutoff));

        // From S-IC center engine cutoff till S-IC outboard engine cutoff
        trajectories.add(
                new SpacecraftTrajectory(centerEngineSICcutoff, outboardEngineSICcutoff,
                        "Earth", orbitParsCenterEngineSICcutoff));

        // From S-IC outboard engine cutoff till S-II engine cutoff
        trajectories.add(
                new SpacecraftTrajectory(outboardEngineSICcutoff, engineSIIcutoff,
                        "Earth", orbitParsOutboardEngineSICcutoff));

        // From S-II engine cutoff till parking orbit insertion
        trajectories.add(
                new SpacecraftTrajectory(engineSIIcutoff, parkingOrbitInsertion,
                        "Earth", orbitParsFirstSIVBcutoff));

        // From parking orbit insertion till translunar injection
        trajectories.add(
                new SpacecraftTrajectory(parkingOrbitInsertion, transLunarInjection,
                        "Earth", orbitParsParkingOrbitInsertion));

        // From translunar injection till first midcourse correction
        trajectories.add(
                new SpacecraftTrajectory(transLunarInjection, firstMidcourseCorrection,
                        "Earth", orbitParsACNX114));

        // From first midcourse correction till second midcourse correction
        trajectories.add(
                new SpacecraftTrajectory(firstMidcourseCorrection, secondMidcourseCorrection,
                        "Earth", orbitParsRIDX251));

        // From second midcourse correction till lunar orbit insertion
        trajectories.add(
                new SpacecraftTrajectory(secondMidcourseCorrection, lunarOrbitInsertion,
                        "Moon", orbitParsHSKX417));

        // From lunar orbit insertion till lunar orbit circulation
        trajectories.add(
                new SpacecraftTrajectory(lunarOrbitInsertion, lunarOrbitCirculation,
                        "Moon", orbitParsHSKX470));

        // From lunar orbit circulation till transearth injection
        trajectories.add(
                new SpacecraftTrajectory(lunarOrbitCirculation, transearthInjection,
                        "Moon", orbitParsMADX530));

        // From transearth injection till third midcourse correction
        trajectories.add(
                new SpacecraftTrajectory(transearthInjection, thirdMidcourseCorrectionCutoff,
                        "Moon", orbitParsBDAX690));

        // From third midcourse correction till entry trajectory initialization
        trajectories.add(
                new SpacecraftTrajectory(thirdMidcourseCorrectionCutoff, entryTrajectoryInitialization,
                        "Earth", orbitParsGWMX777));

        // From entry trajectory initialization till splashdown
        trajectories.add(
                new SpacecraftTrajectory(entryTrajectoryInitialization, splashdown,
                        "Earth", orbitParsEntryTrajectory));

        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(launch)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(centerEngineSICcutoff)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(outboardEngineSICcutoff)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(engineSIIcutoff)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(parkingOrbitInsertion)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(transLunarInjection)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(firstMidcourseCorrection)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(secondMidcourseCorrection)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(lunarOrbitInsertion)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(lunarOrbitCirculation)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(transearthInjection)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(thirdMidcourseCorrectionCutoff)));
        solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(entryTrajectoryInitialization)));
    }

    /**
     * Convert Earth trajectory parameters as used for the Apollo 8 mission to ecliptic position
     * and velocity with respect to the Earth
     * @param dateTime          Date/time [GC]
     * @param latitude          Latitude North (pos) or South (neg) from Earth equator [degrees]
     * @param longitude         Longitude East (pos) or West (neg) from Greenwich meridian [degrees]
     * @param altitude          Perpendicular distance from the Earth [nautical miles]
     * @param velocityMagnitude Magnitude of velocity [feet/sec]
     * @param heading           Heading (East of North) [degrees]
     * @param flightAngle       Flight angle [degrees]
     * @return position [m] and velocity [m/s] in J2000 ecliptic plane with center body Earth
     */
    private Vector3D[] convertEarthTrajectoryParametersToPositionVelocity(
            GregorianCalendar dateTime, double latitude, double longitude, double altitude,
            double velocityMagnitude, double heading, double flightAngle) {

        // Position of the Earth with respect to the Sun at given date/time
        Vector3D positionEarth = EphemerisSolarSystem.getInstance().getBodyPosition("Earth", dateTime);

        // Angle of the Earth with respect to the Sun at given date/time
        double angleEarthRad = Math.atan2(positionEarth.getY(),positionEarth.getX());
        double angleEarthDeg = Math.toDegrees(angleEarthRad);

        // Angle of the Sun with respect to the Earth at given date/time
        double angleSunDeg = angleEarthDeg - 180.0;

        // Time angle since noon [degrees]
        double dateTimeJED = JulianDateConverter.convertCalendarToJulianDate(dateTime);
        double fractionDateTimeSinceNoon = dateTimeJED % 1.0;
        double siderealRotationPeriodHours = SolarSystemParameters.getInstance().getSiderealRotationalPeriod("Earth");
        double siderealRotationPeriodDays = siderealRotationPeriodHours / 24.0;
        double timeAngleDeg = 360.0 * (fractionDateTimeSinceNoon / siderealRotationPeriodDays);

        // Latitude, longitude, altitude
        double latitudeRad = Math.toRadians(latitude);
        double longitudeRad = Math.toRadians(angleSunDeg + timeAngleDeg + longitude);
        double altitudeMeter = altitude * NAUTICALMILE;

        // Square of ellipticity of the Earth = 2*f - f*f, where f is flattening
        double earthEllipticity = SolarSystemParameters.getInstance().getEllipticity("Earth");
        double earthEllipticitySquare = earthEllipticity * earthEllipticity;

        // Equatorial position relative to the Earth taking ellipticity into account
        double sinLat = Math.sin(latitudeRad);
        double N = EARTHRADIUS / Math.sqrt(1.0 - earthEllipticitySquare*sinLat*sinLat);
        double posX = (N + altitudeMeter)*Math.cos(latitudeRad)*Math.cos(longitudeRad);
        double posY = (N + altitudeMeter)*Math.cos(latitudeRad)*Math.sin(longitudeRad);
        double posZ = ((1.0 - earthEllipticitySquare)*N + altitudeMeter)*Math.sin(latitudeRad);
        Vector3D positionEquatorial = new Vector3D(posX, posY, posZ);

        // Choose h == 0 to define local Up vector
        double posXzero = N*Math.cos(latitudeRad)*Math.cos(longitudeRad);
        double posYzero = N*Math.cos(latitudeRad)*Math.sin(longitudeRad);
        double posZzero = (1.0 - earthEllipticitySquare)*N*Math.sin(latitudeRad);

        // Compute Up, East, North vectors
        Vector3D dirUp = new Vector3D(posX - posXzero, posY - posYzero, posZ - posZzero);
        dirUp = dirUp.normalize();
        Vector3D dirEast = (new Vector3D(0.0, 0.0,1.0)).crossProduct(dirUp);
        dirEast = dirEast.normalize();
        Vector3D dirNorth = dirUp.crossProduct(dirEast);
        dirNorth = dirNorth.normalize();

        // Apply input data to compute velocity
        double veloLatitudeRad = Math.toRadians(flightAngle);
        double veloLongitudeRad = Math.toRadians(90.0 - heading);
        double veloX = Math.cos(veloLatitudeRad)*Math.cos(veloLongitudeRad);
        double veloY = Math.cos(veloLatitudeRad)*Math.sin(veloLongitudeRad);
        double veloZ = Math.sin(veloLatitudeRad);
        Vector3D velocityNonRotated = new Vector3D(veloX, veloY, veloZ);
        Vector3D rotX = new Vector3D(dirEast.getX(), dirNorth.getX(), dirUp.getX());
        Vector3D rotY = new Vector3D(dirEast.getY(), dirNorth.getY(), dirUp.getY());
        Vector3D rotZ = new Vector3D(dirEast.getZ(), dirNorth.getZ(), dirUp.getZ());
        Vector3D velocityRotated = velocityNonRotated.rotate(rotX, rotY, rotZ);
        Vector3D velocityEquatorial = velocityRotated.scalarProduct(velocityMagnitude*FOOT);

        // Position and velocity relative to the Earth in J2000 ecliptic coordinates
        Vector3D positionEcliptic = EphemerisUtil.inverseTransformJ2000(positionEquatorial);
        Vector3D velocityEcliptic = EphemerisUtil.inverseTransformJ2000(velocityEquatorial);
        return new Vector3D[]{positionEcliptic,velocityEcliptic};
    }


    /**
     * Compute orbital parameters in J2000 ecliptic plane from position and velocity in feet and feet/sec
     * defined in the earth-centered inertial coordinate frame B1969.
     *
     * @param positionECI position in B1969 ECI [feet]
     * @param velocityECI velocity in B1969 ECI [feet/sec]
     * @param dateTime    date/time
     * @return orbital parameters in J2000 ecliptic plane with center body Earth
     */
    private double[] computeOrbitalParametersECI(Vector3D positionECI, Vector3D velocityECI, GregorianCalendar dateTime) {
        // Convert position and velocity to J2000 ecliptic plane
        Vector3D positionB1969Equatorial = positionECI.scalarProduct(FOOT);
        Vector3D velocityB1969Equatorial = velocityECI.scalarProduct(FOOT);
        Vector3D positionJ2000Equatorial = EphemerisUtil.transformFromB1969ToJ2000(positionB1969Equatorial);
        Vector3D velocityJ2000Equatorial = EphemerisUtil.transformFromB1969ToJ2000(velocityB1969Equatorial);
        Vector3D positionJ2000Ecliptic = EphemerisUtil.inverseTransformJ2000(positionJ2000Equatorial);
        Vector3D velocityJ2000Ecliptic = EphemerisUtil.inverseTransformJ2000(velocityJ2000Equatorial);

        // Gravitational parameter of the Earth
        double muEarth = SolarSystemParameters.getInstance().getMu("Earth");

        // Orbital parameters in J2000 ecliptic plane with center body Earth
        double[] orbitPars= EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                muEarth, positionJ2000Ecliptic, velocityJ2000Ecliptic, dateTime);

        return orbitPars;
    }

    /**
     * Compute orbital parameters in J2000 ecliptic plane from position and velocity in feet and feet/sec
     * defined in the moon-centered inertial coordinate frame B1969.
     *
     * @param positionMCI position in B1969 MCI [feet]
     * @param velocityMCI velocity in B1969 MCI [feet/sec]
     * @param dateTime    date/time
     * @return orbital parameters in J2000 ecliptic plane with center body Moon
     */
    private double[] computeOrbitalParametersMCI(Vector3D positionMCI, Vector3D velocityMCI, GregorianCalendar dateTime) {
        // Convert position and velocity to J2000 ecliptic plane
        Vector3D positionB1969Equatorial = positionMCI.scalarProduct(FOOT);
        Vector3D velocityB1969Equatorial = velocityMCI.scalarProduct(FOOT);
        Vector3D positionJ2000Equatorial = EphemerisUtil.transformFromB1969ToJ2000(positionB1969Equatorial);
        Vector3D velocityJ2000Equatorial = EphemerisUtil.transformFromB1969ToJ2000(velocityB1969Equatorial);
        Vector3D positionJ2000Ecliptic = EphemerisUtil.inverseTransformJ2000(positionJ2000Equatorial);
        Vector3D velocityJ2000Ecliptic = EphemerisUtil.inverseTransformJ2000(velocityJ2000Equatorial);

        // Gravitational parameter of the Moon
        double muMoon = SolarSystemParameters.getInstance().getMu("Moon");

        // Orbital parameters in J2000 ecliptic plane with center body Moon
        double[] orbitPars= EphemerisUtil.computeOrbitalParametersFromPositionVelocity(
                muMoon, positionJ2000Ecliptic, velocityJ2000Ecliptic, dateTime);

        return orbitPars;
    }
}