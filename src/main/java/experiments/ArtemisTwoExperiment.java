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
package experiments;

import application.SolarSystemException;
import ephemeris.CalendarUtil;
import ephemeris.SolarSystemParameters;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Artemis II was a manned lunar flyby mission.
 * Launch: April 1, 22:35:12 UTC or 6:35:12 p.m. EDT (local time at the launch site Kennedy Space Center)
 * Splashdown: April 11, 2026, 00:07:27 UTC or April 10, 5:07:27 p.m. PDT in the Pacific Ocean near
 * San Diego, California.
 * Closest approach to the Moon occurred on the far-side. Closest distance to the lunar surface was
 * about 4,067 miles (6,545 km; 3,534 nmi) and occurred at 23:00 UTC on April 6.
 * Farthest distance from Earth was 252,756 miles (406,771 km; 219,639 nmi) at 23:02 UTC on April 6.
 * The crew experienced a solar eclipse that lasted for 57 minutes, beginning at 01:35 UTC and lasting
 * until 02:32 UTC on April 7.
 *
 * @author Nico Kuijpers
 */
public class ArtemisTwoExperiment {

    // The Solar System
    private SolarSystem solarSystem;

    // Start date/time for simulation
    private final GregorianCalendar simulationStartDateTime;

    // End date/time for simulation
    private final GregorianCalendar simulationEndDateTime;

    // Start date/time for lunar flyby
    private final GregorianCalendar startLunaryFlyby;

    // End date/time for lunar flyby
    private final GregorianCalendar endLunarFlyby;

    // Radius of the Earth
    private final double radiusEarth;

    // Radius of the Moon
    private final double radiusMoon;

    // Simulation time step in s
    private double regularTimeStep;

    // Simulation time step during lunar flyby in s
    private double lunarFlybyTimeStep;

    /**
     * Constructor.
     */
    public ArtemisTwoExperiment() {

        // Start simulation at launch on April 1, 2026, 22:35:12 UTC
        // Note that January is month 0,etc.
        simulationStartDateTime =  new GregorianCalendar(2026, 3, 1, 22, 35, 12);

        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid
        // sudden changes in ephemeris due to changes from
        // winter time to summer time and vice versa
        simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // End simulation at splashdown on April 11, 2026, 00:07:27 UTC
        simulationEndDateTime = new GregorianCalendar(2026,3,11, 0, 7, 27);
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Start date/time for lunar flyby on April 6, 2026, 18:00 UTC
        startLunaryFlyby = new GregorianCalendar(2026,3,6, 18, 0, 0);

        // End date/time for lunar flyby on April 7, 2026, 04:00 UTC
        endLunarFlyby = new GregorianCalendar(2026,3,7, 4, 0, 0);

        // Radius of the Earth
        radiusEarth = 0.5 * SolarSystemParameters.getInstance().getDiameter("Earth");

        // Radius of the Moon
        radiusMoon = 0.5 * SolarSystemParameters.getInstance().getDiameter("Moon");

        // Set simulation time step in seconds
        regularTimeStep = 60.0; // 1 minute

        // Set simulation time step during lunar flyby in seconds
        lunarFlybyTimeStep = 1.0; // 1 second
    }

    /**
     * Simulate journey of Artemis II
     * @param grFlag flag to set Newton Mechanics (false) or General Relativity (true)
     * @param cwpmFlag flag to set Curvature of Wave Propagation Method (CWPM)
     */
    public void simulateJourneyArtemisTwo(boolean grFlag, boolean cwpmFlag) {

        // Create the Solar System
        solarSystem = new SolarSystem(simulationStartDateTime);

        // Set General Relativity flag
        solarSystem.setGeneralRelativityFlag(grFlag);

        // Set Curvature of Wave Propagation Method (CWPM)
        solarSystem.setCurvatureWavePropagationFlag(cwpmFlag);

        // Create spacecraft Artemis II
        solarSystem.createSpacecraft("Artemis II");

        // Initialize simulation
        try {
            solarSystem.initializeSimulation(simulationStartDateTime);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }

        // Initialize maximum distance between spacecraft and surface of the Earth
        double maximumDistanceEarth = -10*SolarSystemParameters.ASTRONOMICALUNIT;

        // Initialize date/time of maximum distance between spacecraft and surface of the Earth
        GregorianCalendar maximumDistanceDateTimeEarth = (GregorianCalendar)simulationStartDateTime.clone();

        // Initialize minimum distance between spacecraft and surface of the Moon
        double minimumDistanceMoon = 10*SolarSystemParameters.ASTRONOMICALUNIT;

        // Initialize date/time of minimim distance between spacecraft and surface of the Moon
        GregorianCalendar minimumDistanceDateTimeMoon = (GregorianCalendar)simulationStartDateTime.clone();

        // Initialize current day
        int currentDay = simulationStartDateTime.get(Calendar.DAY_OF_MONTH);

        // Run simulation
        while(solarSystem.getSimulationDateTime().before(simulationEndDateTime)) {

            // Current simulation date/time
            GregorianCalendar currentDateTime = solarSystem.getSimulationDateTime();

            // Advance one time step
            if (currentDateTime.before(startLunaryFlyby) || !currentDateTime.before(endLunarFlyby)) {
                solarSystem.advanceSimulationSingleStep(regularTimeStep);
            }
            else {
                solarSystem.advanceSimulationSingleStep(lunarFlybyTimeStep);
            }

            // Position of Artemis II, Earth and Moon
            Vector3D positionArtemis = new Vector3D();
            Vector3D positionEarth = new Vector3D();
            Vector3D positionMoon = new Vector3D();
            try {
                positionArtemis = solarSystem.getPosition("Artemis II");
                positionEarth   = solarSystem.getPosition("Earth");
                positionMoon = solarSystem.getPosition("Moon");
            } catch (SolarSystemException e) {
                e.printStackTrace();
            }

            // Show progress
            if (currentDateTime.get(Calendar.DAY_OF_MONTH) != currentDay) {
                currentDay = currentDateTime.get(Calendar.DAY_OF_MONTH);
                System.out.println("Simulation date/time : " + CalendarUtil.calendarToString(currentDateTime));
            }

            // Current distance between spacecraft and surface of the Earth
            double currentDistanceEarth = positionArtemis.euclideanDistance(positionEarth);

            // Current distance between spacecraft and surface of the Moon
            double currentDistanceMoon = positionArtemis.euclideanDistance(positionMoon);

            // Maximum distance and time of maximum distance from Earth
            if (currentDistanceEarth > maximumDistanceEarth) {
                maximumDistanceEarth = currentDistanceEarth;
                maximumDistanceDateTimeEarth = solarSystem.getSimulationDateTime();
            }

            // Minimum distance and time of minimum distance to the Moon
            if (currentDistanceMoon < minimumDistanceMoon) {
                minimumDistanceMoon = currentDistanceMoon;
                minimumDistanceDateTimeMoon = solarSystem.getSimulationDateTime();
            }
        }

        // Show results on screen
        System.out.println("Simulation results:");
        if (solarSystem.getGeneralRelativityFlag()) {
            if (solarSystem.getCurvatureWavePropagationFlag()) {
                System.out.println("Curvature of Wave Propagation Method (CWPM)");
            }
            else {
                System.out.println("General Relativity");
            }
        }
        else {
            System.out.println("Newton Mechanics");
        }
        System.out.println("Regular time step            : " + regularTimeStep + " seconds");
        System.out.println("Time step during Lunar flyby : " + lunarFlybyTimeStep + " seconds");
        System.out.println("Closest distance to the Moon:");
        System.out.println("  date/time           : " + CalendarUtil.calendarToString(minimumDistanceDateTimeMoon));
        System.out.println("  distance to surface : " + (minimumDistanceMoon - radiusMoon)/1000.0 + " km");
        System.out.println("  distance to center  : " + (minimumDistanceMoon)/1000.0 + " km");
        System.out.println("Maximum distance from the Earth:");
        System.out.println("  date/time           : " + CalendarUtil.calendarToString(maximumDistanceDateTimeEarth));
        System.out.println("  distance to surface : " + (maximumDistanceEarth - radiusEarth)/1000.0 + " km");
        System.out.println("  distance to center  : " + maximumDistanceEarth/1000.0 + " km");
    }

    /**
     * Main method.
     * Simulate journey of Artemis II and print results to screen.
     * @param args input arguments (not used)
     */
    public static void main (String[] args) {
        // Experiment set-up
        ArtemisTwoExperiment experiment = new ArtemisTwoExperiment();

        // Run simulation
        experiment.simulateJourneyArtemisTwo(false, false);
        experiment.simulateJourneyArtemisTwo(true, false);
        experiment.simulateJourneyArtemisTwo(true, true);
    }

    /*
        Simulation results:
        Newton Mechanics
        Regular time step            : 60.0 seconds
        Time step during Lunar flyby : 1.0 seconds
        Closest distance to the Moon:
          date/time           : 2026-04-06 23:01:58.000
          distance to surface : 6543.794092039457 km
          distance to center  : 8281.294092039456 km
        Maximum distance from the Earth:
          date/time           : 2026-04-06 23:05:55.000
          distance to surface : 406766.8774631704 km
          distance to center  : 413144.8774631704 km

        Simulation results:
        General Relativity
        Regular time step            : 60.0 seconds
        Time step during Lunar flyby : 1.0 seconds
        Closest distance to the Moon:
          date/time           : 2026-04-06 23:01:58.000
          distance to surface : 6543.784272846068 km
          distance to center  : 8281.28427284607 km
        Maximum distance from the Earth:
          date/time           : 2026-04-06 23:05:55.000
          distance to surface : 406766.8808220712 km
          distance to center  : 413144.8808220712 km

        Simulation results:
        Curvature of Wave Propagation Method (CWPM)
        Regular time step            : 60.0 seconds
        Time step during Lunar flyby : 1.0 seconds
        Closest distance to the Moon:
          date/time           : 2026-04-06 23:01:58.000
          distance to surface : 6543.78577307174 km
          distance to center  : 8281.28577307174 km
        Maximum distance from the Earth:
          date/time           : 2026-04-06 23:05:55.000
          distance to surface : 406766.87768614275 km
          distance to center  : 413144.87768614275 km
     */
}

