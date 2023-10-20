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
import ephemeris.EphemerisSolarSystem;
import ephemeris.IEphemeris;
import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * In this simulation experiment the minimum distance between Giotto and
 * Halley's Comet, the Earth, and 26P/Grigg-Skjellerup is determined
 * as well as the date/time it occurred.
 *
 * @author Nico Kuijpers
 */
public class GiottoHalleyExperiment {

    // Ephemeris
    private IEphemeris ephemeris;

    // The Solar System
    private SolarSystem solarSystem;

    // Start date for simulation
    private final GregorianCalendar simulationStartDateTime;

    // End date for simulation
    private final GregorianCalendar simulationEndDateTime;

    // Simulation time step in s
    private final long deltaT;

    /**
     * Constructor.
     * Set simulation start and end date. Create the Solar System.
     */
    public GiottoHalleyExperiment() {
        // Set ephemeris
        ephemeris = EphemerisSolarSystem.getInstance();

        // Start simulation at July 15, 1985
        // Note that January is month 0,etc.
        simulationStartDateTime = new GregorianCalendar(1985,6,15);

        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid
        // sudden changes in ephemeris due to changes from
        // winter time to summer time and vice versa
        simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Stop simulation at July 23, 1992
        simulationEndDateTime = new GregorianCalendar(1992,6,23);
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Create the Solar System
        solarSystem = new SolarSystem(simulationStartDateTime);

        // Create spacecraft Giotto
        solarSystem.createSpacecraft("Giotto");

        // Set General Relativity flag
        // True means General Relativity is applied
        // False means Newton Mechanics is applied
        //solarSystem.setGeneralRelativityFlag(true);
        solarSystem.setGeneralRelativityFlag(false);

        // Initialize simulation
        try {
            solarSystem.initializeSimulation(simulationStartDateTime);
        } catch (SolarSystemException e) {
            e.printStackTrace();
        }

        // Set simulation time step in seconds
        deltaT = (long) 60; // 1 minute
        //deltaT = (long) (60*60); // 1 hour
    }

    /**
     * Simulate passage of Halley's Comet, the Earth, and
     * 26P/Grigg-Skjellerup
     */
    public void simulateHalleyEarthGSPassage() {

        // Halley's Comet (simulation particle)
        Particle halley = solarSystem.getParticle("Halley");

        // Earth (simulation particle)
        Particle earth = solarSystem.getParticle("Earth");

        // Comet 26P/Grigg-Skjellerup (simulation particle)
        Particle gs = solarSystem.getParticle("26P/Grigg-Skjellerup");

        // Spacecraft Giotto (simulation particle)
        Particle giotto = solarSystem.getParticle("Giotto");

        // Initialize minimum distance between Giotto and Halley's Comet
        double minimumDistanceHalley = 5*SolarSystemParameters.ASTRONOMICALUNIT;

        // Initialize minimum distance between Giotto and Earth
        double minimumDistanceEarth = 5*SolarSystemParameters.ASTRONOMICALUNIT;

        // Initialize minimum distance between Giotto and Comet 26P/Grigg-Skjellerup
        double minimumDistanceGS = 5*SolarSystemParameters.ASTRONOMICALUNIT;

        // Initialize date/time of minimum distance between Giotto and Halley's Comet
        GregorianCalendar minimumDistanceDateTimeHalley = (GregorianCalendar)simulationStartDateTime.clone();

        // Initialize date/time of minimum distance between Giotto and Earth
        GregorianCalendar minimumDistanceDateTimeEarth = (GregorianCalendar)simulationStartDateTime.clone();

        // Initialize date/time of minimum distance between Giotto and Comet 26P/Grigg-Skjellerup
        GregorianCalendar minimumDistanceDateTimeGS = (GregorianCalendar)simulationStartDateTime.clone();

        // Initialize simulation date/time
        GregorianCalendar simulationDateTime;
        simulationDateTime = (GregorianCalendar)simulationStartDateTime.clone();
        simulationDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Initialize current day
        int currentDay = simulationStartDateTime.get(Calendar.DAY_OF_MONTH);

        // Run simulation
        while(solarSystem.getSimulationDateTime().before(simulationEndDateTime)) {

            // Advance one time step
            solarSystem.advanceSimulationSingleStep(deltaT);

            // Current simulation date/time
            GregorianCalendar currentDateTime = solarSystem.getSimulationDateTime();

            // Show progress
            if (currentDateTime.get(Calendar.DAY_OF_MONTH) != currentDay) {
                currentDay = currentDateTime.get(Calendar.DAY_OF_MONTH);
                System.out.println("Simulation date/time : " + CalendarUtil.calendarToString(currentDateTime));
            }

            // Position of Halley's Comet
            Vector3D positionHalley = halley.getPosition();

            // Position of Earth
            Vector3D positionEarth = earth.getPosition();

            // Position of Comet 26P/Grigg-Skjellerup
            Vector3D positionGS = gs.getPosition();

            // Position of spacecraft Giotto
            Vector3D positionGiotto = giotto.getPosition();

            // Current distance between Giotto and Halley's Comet
            double currentDistanceHalley = positionGiotto.euclideanDistance(positionHalley);

            // Current distance between Giotto and Earth
            double currentDistanceEarth = positionGiotto.euclideanDistance(positionEarth);

            // Current distance between Giotto and Comet 26P/Grigg-Skjellerup
            double currentDistanceGS = positionGiotto.euclideanDistance(positionGS);

            // Minimum distance and time of minimum distance between Giotto and Halley's Comet
            if (currentDistanceHalley < minimumDistanceHalley) {
                minimumDistanceHalley = currentDistanceHalley;
                minimumDistanceDateTimeHalley = solarSystem.getSimulationDateTime();
            }

            // Minimum distance and time of minimum distance between Giotto and Earth
            if (solarSystem.getSimulationDateTime().get(Calendar.YEAR) > 1986 &&
                    currentDistanceEarth < minimumDistanceEarth) {
                minimumDistanceEarth = currentDistanceEarth;
                minimumDistanceDateTimeEarth = solarSystem.getSimulationDateTime();
            }

            // Minimum distance and time of minimum distance between Giotto and Comet 26P/Grigg-Skjellerup
            if (currentDistanceGS < minimumDistanceGS) {
                minimumDistanceGS = currentDistanceGS;
                minimumDistanceDateTimeGS = solarSystem.getSimulationDateTime();
            }
        }

        // Show results on screen
        System.out.println("Results from simulation:");
        if (solarSystem.getGeneralRelativityFlag()) {
            System.out.println("General Relativity with time step " + deltaT + " seconds");
        }
        else {
            System.out.println("Newton Mechanics with time step " + deltaT + " seconds");
        }
        System.out.println("Minimum distance between Giotto and Halley's Comet: "
                + minimumDistanceHalley/1000 + " km");
        System.out.println("Date/time of minimum distance: "
                + CalendarUtil.calendarToString(minimumDistanceDateTimeHalley));
        System.out.println("Minimum distance between Giotto and Earth: "
                + minimumDistanceEarth/1000 + " km");
        System.out.println("Date/time of minimum distance: "
                + CalendarUtil.calendarToString(minimumDistanceDateTimeEarth));
        System.out.println("Minimum distance between Giotto and Comet 26P/Grigg-Skjellerup: "
                + minimumDistanceGS/1000 + " km");
        System.out.println("Date/time of minimum distance: "
                + CalendarUtil.calendarToString(minimumDistanceDateTimeGS));
    }

    /**
     * Main method.
     * Simulate and compute passage of Florence.
     * @param args input arguments (not used)
     */
    public static void main (String[] args) {
        // Experiment set-up
        GiottoHalleyExperiment experiment = new GiottoHalleyExperiment();

        // Run simulation to find shortest distances
        experiment.simulateHalleyEarthGSPassage();
    }

    /*
        Results from simulation:
        Newton Mechanics with time step 60 seconds
        Minimum distance between Giotto and Halley's Comet: 654.6562128406282 km
        Date/time of minimum distance: 1986-03-14 00:03:00.000
        Minimum distance between Giotto and Earth: 25743.36588420604 km
        Date/time of minimum distance: 1990-07-02 12:41:00.000
        Minimum distance between Giotto and Comet 26P/Grigg-Skjellerup: 339.2530530314829 km
        Date/time of minimum distance: 1992-07-10 15:19:00.000
     */
}

