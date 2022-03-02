/*
 * Copyright (c) 2022 Nico Kuijpers
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
package experiments;

import application.SolarSystemException;
import ephemeris.CalendarUtil;
import ephemeris.SolarSystemParameters;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.GregorianCalendar;

/**
 * This experiment is designed to determine the minimum distance between Galileo spacecraft
 * and Solar System bodies Earth, Venus, Jupiter, and Galilean moons.
 * @author Nico Kuijpers
 */
public class SpacecraftGalileoExperiment {

    // Name of spacecraft
    private static final String spacecraftName = "Galileo";

    // Time step [s]
    private static final double timeStep = 60.0; // 1 minute
    //private static final double timeStep = 10.0; // 10 seconds

    private SolarSystem solarSystem;

    /**
     * Simulate Solar System and spacecraft during flyby of given Solar System body.
     * @param bodyName  name of Solar System body
     * @param startDateTime start of simulation
     * @param endDateTime end of simulation
     * @param expectedDistanceKm expected distance to surface [km]
     */
    private void flyby(String bodyName, GregorianCalendar startDateTime, GregorianCalendar endDateTime, double expectedDistanceKm) {

        // Set simulation time
        try {
            solarSystem.initializeSimulation(startDateTime);
        } catch (SolarSystemException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

        // Initialize time at which minimum distance occurred
        GregorianCalendar dateTimeMinDist = solarSystem.getSimulationDateTime();

        // Initialize minimum distance to Solar System body
        double minDistance = Double.MAX_VALUE;

        // Perform simulation
        while (solarSystem.getSimulationDateTime().before(endDateTime)) {
            solarSystem.advanceSimulationSingleStep(timeStep);

            // Update minimum distance to Solar System body found thusfar
            Vector3D positionBody = null;
            Vector3D positionSpacecraft = null;
            try {
                positionBody = solarSystem.getPosition(bodyName);
                positionSpacecraft = solarSystem.getPosition(spacecraftName);
            } catch (SolarSystemException e) {
                e.printStackTrace();
            }
            if (positionBody != null && positionSpacecraft != null) {
                double distance = positionSpacecraft.euclideanDistance(positionBody);
                if (distance < minDistance) {
                    minDistance = distance;
                    dateTimeMinDist = solarSystem.getSimulationDateTime();
                }
            }
        }

        // Print results
        double radiusBody = SolarSystemParameters.getInstance().getDiameter(bodyName)/2.0;
        double minDistanceCenterKm = minDistance / 1000.0;
        double minDistanceSurfaceKm = (minDistance - radiusBody) / 1000.0;
        System.out.println(bodyName + ":");
        System.out.println("Date/time : " + CalendarUtil.calendarToString(dateTimeMinDist));
        System.out.println("Minimum distance to body center   : " + minDistanceCenterKm + " km");
        System.out.println("Minimum distance to body surface  : " + minDistanceSurfaceKm + " km");
        System.out.println("Expected distance to body surface : " + expectedDistanceKm + " km\n");
    }

    /**
     * Perform experiment.
     */
    private void performExperiment() {

        // Create solar system
        solarSystem = new SolarSystem();
        solarSystem.setGeneralRelativityFlag(false); // Newton Mechanics
        // solarSystem.setGeneralRelativityFlag(true); // General Relativity

        // Create Jupiter system
        try {
            solarSystem.createPlanetSystem("Jupiter");
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }

        // Create spacecraft
        solarSystem.createSpacecraft(spacecraftName);

        // Start and end date/time during flyby's
        GregorianCalendar startDateTime, endDateTime;

        // Venus encounter Feb 10, 1990, 05:59 UTC (altitude 16 123 km, relative velocity 8.2 km/s
        startDateTime = CalendarUtil.createGregorianCalendar(1990,2,8,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1990,2,12,0,0,0);
        flyby("Venus", startDateTime, endDateTime, 16123);

        // First Earth encounter Dec 8, 1990, 20:35 UTC (altitude 960 km, relative velocity 13.7 km/s)
        startDateTime = CalendarUtil.createGregorianCalendar(1990,12,6,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1990,12,10,0,0,0);
        flyby("Earth", startDateTime, endDateTime, 960);

        // Second Earth encounter Dec 8, 1992, 15:35 UTC (passes the Moon over the North Pole at 111000 km)
        // Distance to the surface of the Earth is unknown TODO
        startDateTime = CalendarUtil.createGregorianCalendar(1992,12,6,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1992,12,10,0,0,0);
        flyby("Earth", startDateTime, endDateTime, 300);

        // Io flyby Dec 7, 1995 (closest approach 1000 km)
        startDateTime = CalendarUtil.createGregorianCalendar(1995,12,5,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1995,12,8,0,0,0);
        flyby("Io", startDateTime, endDateTime, 1000);

        // 27-Jun-1996 G1   835 km gravity-assist reduced Galileo's orbital period from 210 to 72 days
        startDateTime = CalendarUtil.createGregorianCalendar(1996,6,25,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1996,6,28,0,0,0);
        flyby("Ganymede", startDateTime, endDateTime, 835);

        // 06-Sep-1996 G2   260 km gravity-assist put Galileo into coplanar orbit with other Galilean satellites
        startDateTime = CalendarUtil.createGregorianCalendar(1996,9,4,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1996,9,7,0,0,0);
        flyby("Ganymede", startDateTime, endDateTime, 260);

        // 04-Nov-1996 C3  1136 km
        startDateTime = CalendarUtil.createGregorianCalendar(1996,11,2,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1996,11,5,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 1136);

        // 19-Dec-1996 E4   692 km
        startDateTime = CalendarUtil.createGregorianCalendar(1996,12,17,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1996,12,20,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 692);

        // 20-Feb-1997 E6   586 km
        startDateTime = CalendarUtil.createGregorianCalendar(1997,2,18,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1997,2,21,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 586);

        // 05-Apr-1997 G7  3102 km (extra correction added on 04-Apr-1997 00:00 and 05-Apr-1997 07:30)
        startDateTime = CalendarUtil.createGregorianCalendar(1997,4,3,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1997,4,6,0,0,0);
        flyby("Ganymede", startDateTime, endDateTime, 3102);

        // 07-May-1997 G8  1603 km (extra correction added on 07-May-1997 16:30 and 12-May-1997 00:00 and )
        startDateTime = CalendarUtil.createGregorianCalendar(1997,5,5,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1997,5,8,0,0,0);
        flyby("Ganymede", startDateTime, endDateTime, 1603);

        // 25-Jun-1997 C9   418 km (extra correction added on 25-Jun-1997 14:10)
        startDateTime = CalendarUtil.createGregorianCalendar(1997,6,23,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1997,6,26,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 418);

        // 17-Sep-1997 C10  539 km (extra correction added on 16-Sep-1997 12.00 and 17-Sep-1997 00:40)
        startDateTime = CalendarUtil.createGregorianCalendar(1997,9,15,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1997,9,18,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 539);

        // 06-Nov-1997 E11 2042 km (extra correction added on 06-Nov-1997 21:00)
        startDateTime = CalendarUtil.createGregorianCalendar(1997,11,4,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1997,11,7,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 2042);

        // 16-Dec-1997 E12  196 km (extra correction added on 16-Dec-1997 12:30)
        startDateTime = CalendarUtil.createGregorianCalendar(1997,12,14,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1997,12,17,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 196);

        // 10-Feb-1998 E13 3562 km
        startDateTime = CalendarUtil.createGregorianCalendar(1998,2,8,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1998,2,11,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 3562);

        // 28-Mar-1998 E14 1645 km (extra correction added on 29-Mar-1998 13:50)
        startDateTime = CalendarUtil.createGregorianCalendar(1998,3,26,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1998,3,30,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 1645);

        // 31-May-1998 E15 2515 km (extra correction added on 31-May-1998 21:30)
        startDateTime = CalendarUtil.createGregorianCalendar(1998,5,29,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1998,6,1,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 2515);

        // 21-Jul-1998 E16 1830 km (extra correction added on 21-Jul-1998 05:20)
        startDateTime = CalendarUtil.createGregorianCalendar(1998,7,19,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1998,7,22,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 1830);

        // 26-Sep-1998 E17 3582 km
        startDateTime = CalendarUtil.createGregorianCalendar(1998,9,24,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1998,9,27,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 3582);

        // 22-Nov-1998 E18 2273 km (extra correction added on 22-Nov-1998 12:00)
        startDateTime = CalendarUtil.createGregorianCalendar(1998,11,20,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1998,11,23,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 2273);

        // 01-Feb-1999 E19 1439 km (extra correction added on 01-Feb-1999 02:50)
        startDateTime = CalendarUtil.createGregorianCalendar(1999,1,30,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1999,2,2,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 1439);

        // 05-May-1999 C20 1315 km (extra correction added on 05-May-1999 14:30)
        startDateTime = CalendarUtil.createGregorianCalendar(1999,5,3,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1999,5,6,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 1315);

        // 30-Jun-1999 C21 1047 km (extra correction added on 30-Jun-1999 08:20)
        startDateTime = CalendarUtil.createGregorianCalendar(1999,6,28,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1999,7,1,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 1047);

        // 14-Aug-1999 C22 2296 km (extra correction added on 14-Aug-1999 09:00)
        startDateTime = CalendarUtil.createGregorianCalendar(1999,8,12,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1999,8,15,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 2296);

        // 16-Sep-1999 C23 1057 km (extra correction added on 16-Sep-1999 18:00)
        startDateTime = CalendarUtil.createGregorianCalendar(1999,9,14,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1999,9,17,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 1057);

        // 11-Oct-1999 I24  611 km (extra correction added on 11-Oct-1999 05:00)
        startDateTime = CalendarUtil.createGregorianCalendar(1999,10,9,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1999,10,12,0,0,0);
        flyby("Io", startDateTime, endDateTime, 611);

        // 25-Nov-1999 I25  300 km (extra correction added on 26-Nov-1999 04:30)
        startDateTime = CalendarUtil.createGregorianCalendar(1999,11,23,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(1999,11,27,0,0,0);
        flyby("Io", startDateTime, endDateTime, 300);

        // 03-Jan-2000 E26  351 km (extra correction added on 03-Jan-2000 18:30)
        startDateTime = CalendarUtil.createGregorianCalendar(2000,1,1,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2000,1,4,0,0,0);
        flyby("Europa", startDateTime, endDateTime, 351);

        // 22-Feb-2000 I27  198 km (extra correction added on 22-Feb-2000 14:20)
        startDateTime = CalendarUtil.createGregorianCalendar(2000,2,20,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2000,2,23,0,0,0);
        flyby("Io", startDateTime, endDateTime, 198);

        // 20-May-2000 G28 1000 km (extra correction added on 20-May-2000 10:40)
        startDateTime = CalendarUtil.createGregorianCalendar(2000,5,18,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2000,5,21,0,0,0);
        flyby("Ganymede", startDateTime, endDateTime, 1000);

        // 28-Dec-2000 G29 2321 km (extra correction added on 28-Dec-2000 09:00)
        startDateTime = CalendarUtil.createGregorianCalendar(2000,12,26,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2000,12,29,0,0,0);
        flyby("Ganymede", startDateTime, endDateTime, 2321);

        // 25-May-2001 C30  138 km (extra correction added on 25-May-2001 12:00)
        startDateTime = CalendarUtil.createGregorianCalendar(2001,5,23,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2001,5,26,0,0,0);
        flyby("Callisto", startDateTime, endDateTime, 138);

        // 05-Aug-2001 I31  200 km (extra correction added on 06-Aug-2001 05:30)
        startDateTime = CalendarUtil.createGregorianCalendar(2001,8,3,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2001,8,7,0,0,0);
        flyby("Io", startDateTime, endDateTime, 200);

        // 16-Oct-2001 I32  181 km (extra correction added on 16-Oct-2001 02:00)
        startDateTime = CalendarUtil.createGregorianCalendar(2001,10,14,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2001,10,17,0,0,0);
        flyby("Io", startDateTime, endDateTime, 181);

        // 17-Jan-2002 I33  102 km (extra correction added on 17-Jan-2002 14:40)
        startDateTime = CalendarUtil.createGregorianCalendar(2002,1,15,0,0,0);
        endDateTime = CalendarUtil.createGregorianCalendar(2002,1,18,0,0,0);
        flyby("Io", startDateTime, endDateTime, 102);
    }

    /**
     * Main method.
     * Validate ephemeris for spacecraft Galileo.
     * @param args input arguments (not used)
     */
    public static void main(String[] args) {
        // Experiment set-up
        SpacecraftGalileoExperiment experiment = new SpacecraftGalileoExperiment();

        // Run experiment
        experiment.performExperiment();
    }

    /*
        Results with timestep of 1 minute and Newton Mechanics:
        Venus:
        Date/time : 1990-02-10 06:00:00.000
        Minimum distance to body center   : 22179.348478231033 km
        Minimum distance to body surface  : 16127.348478231035 km
        Expected distance to body surface : 16123.0 km

        Earth:
        Date/time : 1990-12-08 20:36:00.000
        Minimum distance to body center   : 7344.608714921441 km
        Minimum distance to body surface  : 966.6087149214409 km
        Expected distance to body surface : 960.0 km

        Earth:
        Date/time : 1992-12-08 15:10:00.000
        Minimum distance to body center   : 6684.752754898726 km
        Minimum distance to body surface  : 306.752754898726 km
        Expected distance to body surface : 300.0 km

        Io:
        Date/time : 1995-12-07 17:47:00.000
        Minimum distance to body center   : 2724.6965107188594 km
        Minimum distance to body surface  : 903.3965107188593 km
        Expected distance to body surface : 1000.0 km

        Ganymede:
        Date/time : 1996-06-27 06:30:00.000
        Minimum distance to body center   : 3468.900184252033 km
        Minimum distance to body surface  : 834.900184252033 km
        Expected distance to body surface : 835.0 km

        Ganymede:
        Date/time : 1996-09-06 19:01:00.000
        Minimum distance to body center   : 2906.666213938567 km
        Minimum distance to body surface  : 272.6662139385673 km
        Expected distance to body surface : 260.0 km

        Callisto:
        Date/time : 1996-11-04 13:35:00.000
        Minimum distance to body center   : 3549.9215127078983 km
        Minimum distance to body surface  : 1146.9215127078985 km
        Expected distance to body surface : 1136.0 km

        Europa:
        Date/time : 1996-12-19 06:54:00.000
        Minimum distance to body center   : 2256.4971704372483 km
        Minimum distance to body surface  : 691.4971704372484 km
        Expected distance to body surface : 692.0 km

        Europa:
        Date/time : 1997-02-20 17:07:00.000
        Minimum distance to body center   : 2152.2344116944773 km
        Minimum distance to body surface  : 587.2344116944773 km
        Expected distance to body surface : 586.0 km

        Ganymede:
        Date/time : 1997-04-05 07:11:00.000
        Minimum distance to body center   : 5736.793262820654 km
        Minimum distance to body surface  : 3102.7932628206536 km
        Expected distance to body surface : 3102.0 km

        Ganymede:
        Date/time : 1997-05-07 15:57:00.000
        Minimum distance to body center   : 4237.91766318866 km
        Minimum distance to body surface  : 1603.9176631886605 km
        Expected distance to body surface : 1603.0 km

        Callisto:
        Date/time : 1997-06-25 13:49:00.000
        Minimum distance to body center   : 2821.9356939295003 km
        Minimum distance to body surface  : 418.9356939295004 km
        Expected distance to body surface : 418.0 km

        Callisto:
        Date/time : 1997-09-17 00:20:00.000
        Minimum distance to body center   : 2937.79708846811 km
        Minimum distance to body surface  : 534.7970884681097 km
        Expected distance to body surface : 539.0 km

        Europa:
        Date/time : 1997-11-06 20:33:00.000
        Minimum distance to body center   : 3608.6971778126185 km
        Minimum distance to body surface  : 2043.6971778126187 km
        Expected distance to body surface : 2042.0 km

        Europa:
        Date/time : 1997-12-16 12:04:00.000
        Minimum distance to body center   : 1771.5052144364747 km
        Minimum distance to body surface  : 206.50521443647472 km
        Expected distance to body surface : 196.0 km

        Europa:
        Date/time : 1998-02-10 17:59:00.000
        Minimum distance to body center   : 5124.106070652633 km
        Minimum distance to body surface  : 3559.1060706526328 km
        Expected distance to body surface : 3562.0 km

        Europa:
        Date/time : 1998-03-29 13:22:00.000
        Minimum distance to body center   : 3208.2700390931213 km
        Minimum distance to body surface  : 1643.2700390931213 km
        Expected distance to body surface : 1645.0 km

        Europa:
        Date/time : 1998-05-31 21:14:00.000
        Minimum distance to body center   : 4080.1541498551246 km
        Minimum distance to body surface  : 2515.1541498551246 km
        Expected distance to body surface : 2515.0 km

        Europa:
        Date/time : 1998-07-21 05:05:00.000
        Minimum distance to body center   : 3401.7633251127327 km
        Minimum distance to body surface  : 1836.7633251127327 km
        Expected distance to body surface : 1830.0 km

        Europa:
        Date/time : 1998-09-26 03:55:00.000
        Minimum distance to body center   : 5148.5808166275365 km
        Minimum distance to body surface  : 3583.580816627537 km
        Expected distance to body surface : 3582.0 km

        Europa:
        Date/time : 1998-11-22 11:39:00.000
        Minimum distance to body center   : 3839.1315227689115 km
        Minimum distance to body surface  : 2274.1315227689115 km
        Expected distance to body surface : 2273.0 km

        Europa:
        Date/time : 1999-02-01 02:21:00.000
        Minimum distance to body center   : 3005.490395951551 km
        Minimum distance to body surface  : 1440.490395951551 km
        Expected distance to body surface : 1439.0 km

        Callisto:
        Date/time : 1999-05-05 13:57:00.000
        Minimum distance to body center   : 3729.470207708612 km
        Minimum distance to body surface  : 1326.4702077086122 km
        Expected distance to body surface : 1315.0 km

        Callisto:
        Date/time : 1999-06-30 07:48:00.000
        Minimum distance to body center   : 3451.01588986078 km
        Minimum distance to body surface  : 1048.01588986078 km
        Expected distance to body surface : 1047.0 km

        Callisto:
        Date/time : 1999-08-14 08:32:00.000
        Minimum distance to body center   : 4702.438080915003 km
        Minimum distance to body surface  : 2299.438080915003 km
        Expected distance to body surface : 2296.0 km

        Callisto:
        Date/time : 1999-09-16 17:28:00.000
        Minimum distance to body center   : 3455.879715503941 km
        Minimum distance to body surface  : 1052.8797155039408 km
        Expected distance to body surface : 1057.0 km

        Io:
        Date/time : 1999-10-11 04:34:00.000
        Minimum distance to body center   : 2432.9255884020877 km
        Minimum distance to body surface  : 611.6255884020878 km
        Expected distance to body surface : 611.0 km

        Io:
        Date/time : 1999-11-26 04:06:00.000
        Minimum distance to body center   : 2130.8651088144293 km
        Minimum distance to body surface  : 309.5651088144295 km
        Expected distance to body surface : 300.0 km

        Europa:
        Date/time : 2000-01-03 18:01:00.000
        Minimum distance to body center   : 1919.0516349982481 km
        Minimum distance to body surface  : 354.0516349982482 km
        Expected distance to body surface : 351.0 km

        Io:
        Date/time : 2000-02-22 13:48:00.000
        Minimum distance to body center   : 2020.522653146294 km
        Minimum distance to body surface  : 199.22265314629394 km
        Expected distance to body surface : 198.0 km

        Ganymede:
        Date/time : 2000-05-20 10:11:00.000
        Minimum distance to body center   : 3446.2776786069844 km
        Minimum distance to body surface  : 812.2776786069842 km
        Expected distance to body surface : 1000.0 km

        Ganymede:
        Date/time : 2000-12-28 08:27:00.000
        Minimum distance to body center   : 4980.861881496253 km
        Minimum distance to body surface  : 2346.8618814962524 km
        Expected distance to body surface : 2321.0 km

        Callisto:
        Date/time : 2001-05-25 11:25:00.000
        Minimum distance to body center   : 2542.369129703385 km
        Minimum distance to body surface  : 139.36912970338528 km
        Expected distance to body surface : 138.0 km

        Io:
        Date/time : 2001-08-06 05:00:00.000
        Minimum distance to body center   : 2024.0706272385996 km
        Minimum distance to body surface  : 202.7706272385996 km
        Expected distance to body surface : 200.0 km

        Io:
        Date/time : 2001-10-16 01:24:00.000
        Minimum distance to body center   : 2012.9364277944799 km
        Minimum distance to body surface  : 191.63642779447977 km
        Expected distance to body surface : 181.0 km

        Io:
        Date/time : 2002-01-17 14:10:00.000
        Minimum distance to body center   : 1937.0391214145175 km
        Minimum distance to body surface  : 115.73912141451751 km
        Expected distance to body surface : 102.0 km
     */
}



