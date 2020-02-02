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
package experiments;

import application.SolarSystemException;
import ephemeris.CalendarUtil;
import ephemeris.EphemerisSolarSystem;
import ephemeris.IEphemeris;
import ephemeris.SolarSystemParameters;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.*;

/**
 * In this experiment the distance between simulated positions and epthemeris of the
 * Galilean, Saturnian, Uranian, and Neptunian moons is determined after 25 years of simulation.
 *
 * @author Nico Kuijpers
 */
public class SolarSystemMoonsExperiment {

    // Ephemeris
    private IEphemeris ephemeris;

    // The Solar System
    private SolarSystem solarSystem;

    // Start date for simulation
    private final GregorianCalendar simulationStartDateTime;

    // End date for simulation
    private final GregorianCalendar simulationEndDateTime;

    // Start date for collecting results
    private final GregorianCalendar startCollectingResultsDateTime;

    // Planets to gather results from
    private final List<String> planets;

    // Moons to gather results from
    private final Map<String,List<String>> moons;

    // Collect results
    private final Map<String,List<Double>> results;

    /**
     * Constructor.
     * Set simulation start and end date. Create the Solar System.
     */
    public SolarSystemMoonsExperiment() {
        // Set ephemeris
        ephemeris = EphemerisSolarSystem.getInstance();

        // Start simulation at January 1, 1990
        // Note that January is month 0
        // simulationStartDateTime = new GregorianCalendar(1990,0,1,0,0);
        simulationStartDateTime = new GregorianCalendar(1990,0,1,0,0);

        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid
        // sudden changes in ephemeris due to changes from
        // winter time to summer time and vice versa
        simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Stop simulation at January 1, 1992
        simulationEndDateTime = new GregorianCalendar(1992,0,1,0,0 );
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Start collecting results at January 1, 1991
        startCollectingResultsDateTime = new GregorianCalendar(1991,0,1,0,0 );

        // Define planets to gather results from
        planets = new ArrayList<>(Arrays.asList("Jupiter","Saturn","Uranus","Neptune"));

        // Define moons to gather results from
        moons = new HashMap<>();
        moons.put("Jupiter",
                new ArrayList<>(Arrays.asList("Io","Europa","Ganymede","Callisto")));
        moons.put("Saturn",
                new ArrayList<>(Arrays.asList("Mimas","Enceladus","Tethys","Dione","Rhea","Titan","Iapetus")));
        moons.put("Uranus",
                new ArrayList<>(Arrays.asList("Miranda","Ariel","Umbriel","Titania","Oberon")));
        moons.put("Neptune",
                new ArrayList<>(Arrays.asList("Triton")));

        // Store results
        results = new HashMap<>();
        for (String planetName : planets) {
            for (String moonName : moons.get(planetName)) {
                results.put(moonName,new ArrayList<>());
            }
        }

        // Create the Solar System
        solarSystem = new SolarSystem(simulationStartDateTime);
        for (String planetName : planets) {
            try {
                solarSystem.createPlanetSystem(planetName);
            }
            catch (SolarSystemException ex) {
                System.err.println(ex.getMessage());
            }
        }

        // Set General Relativity flag
        // True means General Relativity is applied
        // False means Newton Mechanics is applied
        // solarSystem.setGeneralRelativityFlag(true);
        solarSystem.setGeneralRelativityFlag(false);
    }

    /**
     * Simulate the Solar System.
     */
    public void simulate() {
        while(solarSystem.getSimulationDateTime().before(simulationEndDateTime)) {
            // Advance one time step
            solarSystem.advanceSimulationForward(1);

            // Collect results for the last year
            if (solarSystem.getSimulationDateTime().after(startCollectingResultsDateTime)) {
                for (String moonName : results.keySet()) {
                    double distance = computeDistance(moonName,solarSystem.getSimulationDateTime());
                    results.get(moonName).add(distance);
                }
                startCollectingResultsDateTime.add(Calendar.HOUR,24);
            }
        }
    }

    /**
     * Show results of simulation.
     */
    public void showResults() {
        System.out.println("Experiment date/time       : " +
                CalendarUtil.calendarToString(new GregorianCalendar()));
        System.out.println("Simulation start date/time : " +
                CalendarUtil.calendarToString(simulationStartDateTime));
        System.out.println("Simulation end date/time   : " +
                CalendarUtil.calendarToString(simulationEndDateTime));
        System.out.println("Deviation is averaged over final year of simulation");
        for (String planetName : planets) {
            System.out.println("Planet: " + planetName);
            for (String moonName : moons.get(planetName)) {
                double sum = 0.0;
                for (double d : results.get(moonName)) {
                    sum += d;
                }
                double deviation = sum / results.get(moonName).size();
                System.out.println("Deviation " + 0.001*deviation + " km for " + moonName);
            }
        }
    }

    /**
     * Compute distance between actual position of moon and expected position for given date.
     * @param moonName    Name of the moon
     * @param dateTime    Date to determine position and velocity of the moon
     * @return distance [m]
     */
    private double computeDistance(String moonName, GregorianCalendar dateTime) {

        // Obtain position and velocity of moon relative to planet from Ephemeris
        Vector3D[] positionAndVelocityMoon = ephemeris.getBodyPositionVelocity(moonName, dateTime);
        Vector3D positionMoonExpected = positionAndVelocityMoon[0];

        // Obtain position of moon relative to planet from simulation results
        String planetName = SolarSystemParameters.getInstance().getPlanetOfMoon(moonName);
        Vector3D positionPlanet = solarSystem.getParticle(planetName).getPosition();
        Vector3D positionMoon = solarSystem.getParticle(moonName).getPosition();
        Vector3D positionMoonActual = positionMoon.minus(positionPlanet);

        // Compute distance
        return positionMoonExpected.euclideanDistance(positionMoonActual);
    }

    /**
     * Main method.
     * Simulate and compute deviation for each moon.
     * @param args input arguments (not used)
     */
    public static void main (String[] args) {
        // Experiment set-up
        SolarSystemMoonsExperiment experiment = new SolarSystemMoonsExperiment();

        // Run simulation and show results
        experiment.simulate();
        experiment.showResults();
    }
}

