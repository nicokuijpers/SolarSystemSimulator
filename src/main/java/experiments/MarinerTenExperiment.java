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
 * Spacecraft Mariner 10 passed Venus and had three encounters with Mercury.
 * For each of the encounters, the minimum distance between Mariner 10
 * and the surface of the planets is determined as well as the date/time
 * it occurred.
 *
 * @author Nico Kuijpers
 */
public class MarinerTenExperiment {

    // The Solar System
    private SolarSystem solarSystem;

    // Start date for simulation
    private final GregorianCalendar simulationStartDateTime;

    // End date for simulation
    private final GregorianCalendar simulationEndDateTime;

    // Final date for first encounter with Mercury
    private final GregorianCalendar firstEncounterFinalDateTime;

    // Final date for second encounter with Mercury
    private final GregorianCalendar secondEncounterFinalDateTime;

    // Radius of Venus
    private final double radiusVenus;

    // Radius of Mercury
    private final double radiusMercury;

    // Simulation time step in s
    private final double deltaT;

    /**
     * Constructor.
     */
    public MarinerTenExperiment() {

        // Start simulation at launch on November 3, 1973 at 17:45 UTC
        // Note that January is month 0,etc.
        simulationStartDateTime =  new GregorianCalendar(1973, 10, 3, 17, 45, 0);

        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid
        // sudden changes in ephemeris due to changes from
        // winter time to summer time and vice versa
        simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Contact with Mariner 10 was terminated on March 24, 1975
        simulationEndDateTime = new GregorianCalendar(1975,2,24);
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First encounter with Mercury should occur before April 15, 1974
        firstEncounterFinalDateTime = new GregorianCalendar(1974, 3, 15);

        // Second encounter with Mercury should occur before October 15, 1974
        secondEncounterFinalDateTime = new GregorianCalendar(1974, 9, 15);

        // Radius of Venus
        radiusVenus = 0.5 * SolarSystemParameters.getInstance().getDiameter("Venus");

        // Radius of Mercury
        radiusMercury = 0.5 * SolarSystemParameters.getInstance().getDiameter("Mercury");

        // Set simulation time step in seconds
        deltaT = 60.0; // 1 minute
    }

    /**
     * Simulate journey of Mariner 10
     */
    public void simulateJourneyMariner10() {

        // Create the Solar System
        solarSystem = new SolarSystem(simulationStartDateTime);

        // Create spacecraft Mariner 10
        solarSystem.createSpacecraft("Mariner 10");

        // Set General Relativity flag
        // True means General Relativity is applied
        // False means Newton Mechanics is applied
        solarSystem.setGeneralRelativityFlag(false);

        // Initialize simulation
        try {
            solarSystem.initializeSimulation(simulationStartDateTime);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }

        // Initialize minimum distance between spacecraft and surface of the planet
        double minimumDistanceVenus    = 10*SolarSystemParameters.ASTRONOMICALUNIT;
        double minimumDistanceMercuryA = 10*SolarSystemParameters.ASTRONOMICALUNIT;
        double minimumDistanceMercuryB = 10*SolarSystemParameters.ASTRONOMICALUNIT;
        double minimumDistanceMercuryC = 10*SolarSystemParameters.ASTRONOMICALUNIT;

        // Initialize date/time of minimum distance
        GregorianCalendar minimumDistanceDateTimeVenus = (GregorianCalendar)simulationStartDateTime.clone();
        GregorianCalendar minimumDistanceDateTimeMercuryA = (GregorianCalendar)simulationStartDateTime.clone();
        GregorianCalendar minimumDistanceDateTimeMercuryB = (GregorianCalendar)simulationStartDateTime.clone();
        GregorianCalendar minimumDistanceDateTimeMercuryC = (GregorianCalendar)simulationStartDateTime.clone();

        // Initialize current day
        int currentDay = simulationStartDateTime.get(Calendar.DAY_OF_MONTH);

        // Run simulation
        while(solarSystem.getSimulationDateTime().before(simulationEndDateTime)) {

            // Advance one time step
            solarSystem.advanceSimulationSingleStep(deltaT);

            // Current simulation date/time
            GregorianCalendar currentDateTime = solarSystem.getSimulationDateTime();

            // Position of Mariner 10, Venus and Mercury
            Vector3D positionMariner = new Vector3D();
            Vector3D positionVenus = new Vector3D();
            Vector3D positionMercury = new Vector3D();
            try {
                positionMariner = solarSystem.getPosition("Mariner 10");
                positionVenus   = solarSystem.getPosition("Venus");
                positionMercury = solarSystem.getPosition("Mercury");
            } catch (SolarSystemException e) {
                e.printStackTrace();
            }

            // Show progress
            if (currentDateTime.get(Calendar.DAY_OF_MONTH) != currentDay) {
                currentDay = currentDateTime.get(Calendar.DAY_OF_MONTH);
                System.out.println("Simulation date/time : " + CalendarUtil.calendarToString(currentDateTime));
            }

            // Current distance between Mariner 10 and surface of Venus
            double currentDistanceVenus = positionMariner.euclideanDistance(positionVenus) - radiusVenus;

            // Current distance between Mariner 10 and surface of Mercury
            double currentDistanceMercury = positionMariner.euclideanDistance(positionMercury) - radiusMercury;

            // Minimum distance and time of minimum distance Venus
            if (currentDistanceVenus < minimumDistanceVenus) {
                minimumDistanceVenus = currentDistanceVenus;
                minimumDistanceDateTimeVenus = solarSystem.getSimulationDateTime();
            }

            // Minimum distance and time of minimum distance Mercury encounters
            if (currentDateTime.before(firstEncounterFinalDateTime)) {
                if (currentDistanceMercury < minimumDistanceMercuryA) {
                    minimumDistanceMercuryA = currentDistanceMercury;
                    minimumDistanceDateTimeMercuryA = solarSystem.getSimulationDateTime();
                }
            }
            else if (currentDateTime.before(secondEncounterFinalDateTime)) {
                if (currentDistanceMercury < minimumDistanceMercuryB) {
                    minimumDistanceMercuryB = currentDistanceMercury;
                    minimumDistanceDateTimeMercuryB = solarSystem.getSimulationDateTime();
                }
            } else {
                if (currentDistanceMercury < minimumDistanceMercuryC) {
                    minimumDistanceMercuryC = currentDistanceMercury;
                    minimumDistanceDateTimeMercuryC = solarSystem.getSimulationDateTime();
                }
            }
        }

        // Show results on screen
        System.out.println("Simulation results:");
        if (solarSystem.getGeneralRelativityFlag()) {
            System.out.println("General Relativity with time step " + deltaT + " seconds");
        }
        else {
            System.out.println("Newton Mechanics with time step " + deltaT + " seconds");
        }
        System.out.println("Encounter with Venus:");
        System.out.println("  date/time : " + CalendarUtil.calendarToString(minimumDistanceDateTimeVenus));
        System.out.println("  distance  : " + minimumDistanceVenus/1000.0 + " km");
        System.out.println("First encounter with Mercury:");
        System.out.println("  date/time : " + CalendarUtil.calendarToString(minimumDistanceDateTimeMercuryA));
        System.out.println("  distance  : " + minimumDistanceMercuryA/1000.0 + " km");
        System.out.println("Second encounter with Mercury:");
        System.out.println("  date/time : " + CalendarUtil.calendarToString(minimumDistanceDateTimeMercuryB));
        System.out.println("  distance  : " + minimumDistanceMercuryB/1000.0 + " km");
        System.out.println("Third encounter with Mercury:");
        System.out.println("  date/time : " + CalendarUtil.calendarToString(minimumDistanceDateTimeMercuryC));
        System.out.println("  distance  : " + minimumDistanceMercuryC/1000.0 + " km");
    }

    /**
     * Main method.
     * Simulate journey of Mariner 10 and print results to screen.
     * @param args input arguments (not used)
     */
    public static void main (String[] args) {
        // Experiment set-up
        MarinerTenExperiment experiment = new MarinerTenExperiment();

        // Run simulation
        experiment.simulateJourneyMariner10();
    }

    /*
        Simulation results:
        Newton Mechanics with time step 60.0 seconds
        Encounter with Venus:
          date/time : 1974-02-05 17:05:00.000
          distance  : 5545.601378502339 km
        First encounter with Mercury:
          date/time : 1974-03-29 20:48:00.000
          distance  : 696.6562722826409 km
        Second encounter with Mercury:
          date/time : 1974-09-21 21:21:00.000
          distance  : 45874.47326370415 km
        Third encounter with Mercury:
          date/time : 1975-03-16 22:39:00.000
          distance  : 399.5944890233339 km

        Simulation results:
        General Relativity with time step 60.0 seconds
        Encounter with Venus:
          date/time : 1974-02-05 17:05:00.000
          distance  : 5562.831969110656 km
        First encounter with Mercury:
          date/time : 1974-03-29 20:48:00.000
          distance  : 699.4492596606943 km
        Second encounter with Mercury:
          date/time : 1974-09-21 21:21:00.000
          distance  : 45950.01747820919 km
        Third encounter with Mercury:
          date/time : 1975-03-16 22:38:00.000
          distance  : 281.7401703928304 km
     */
}

