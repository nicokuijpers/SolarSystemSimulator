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
import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.*;

/**
 * Determine velocity versus distance from the Sun for
 * Voyager 1, Voyager 2, and New Horizons.
 * @author Nico Kuijpers
 */
public class SpacecraftVelocityDistanceExperiment {

    // Launch Voyager 1: September 5, 1977, 12:56:00 UTC
    private final GregorianCalendar startDateVoyagerOne =
            new GregorianCalendar(1977, 8, 5, 12, 56, 0);

    // End of simulation for Voyager 1: January 1, 1981
    private final GregorianCalendar endDateVoyagerOne =
            new GregorianCalendar(1981, 0, 1, 0, 0, 0);

    // Launch Voyager 2: August 20, 1977, 14:29:00 UTC
    private final GregorianCalendar startDateVoyagerTwo =
            new GregorianCalendar(1977, 7, 20, 14, 29, 0);

    // End of simulation for Voyager 2: January 1, 1990
    private final GregorianCalendar endDateVoyagerTwo =
            new GregorianCalendar(1990, 0, 1, 0, 0, 0);

    // Launch New Horizons: January 19, 2006, 19:00 UTC
    private final GregorianCalendar startDateNewHorizons =
            new GregorianCalendar(2006, 0, 19, 19, 0, 0);

    // End of simulation for New Horizons: January 1, 2020
    private final GregorianCalendar endDateNewHorizons =
            new GregorianCalendar(2020, 0, 1, 0, 0, 0);

    // Simulation start dates
    private final Map<String,GregorianCalendar> startDates = new HashMap<>();

    // Simulation end dates
    private final Map<String,GregorianCalendar> endDates = new HashMap<>();

    // Solar system
    private SolarSystem solarSystem;

    // Start date for simulation
    private GregorianCalendar simulationStartDate;

    // End date for simulation
    private GregorianCalendar simulationEndDate;

    public SpacecraftVelocityDistanceExperiment() {

        // Define start dates for simulation
        startDates.put("Voyager 1", startDateVoyagerOne);
        startDates.put("Voyager 2", startDateVoyagerTwo);
        startDates.put("New Horizons", startDateNewHorizons);

        // Define end dates for simulation
        endDates.put("Voyager 1", endDateVoyagerOne);
        endDates.put("Voyager 2", endDateVoyagerTwo);
        endDates.put("New Horizons", endDateNewHorizons);
    }

    private void simulateSpacecraftTrajectory(String spacecraftName) {

        // Set simulation start date
        simulationStartDate = startDates.get(spacecraftName);
        simulationStartDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Set simulation stop date
        simulationEndDate = endDates.get(spacecraftName);
        simulationEndDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Create solar system
        solarSystem = new SolarSystem();
        solarSystem.setGeneralRelativityFlag(false); // Newton Mechanics
        // solarSystem.setGeneralRelativityFlag(true); // General Relativity
        try {
            solarSystem.initializeSimulation(simulationStartDate);
        } catch (SolarSystemException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

        // Store distance from the Sun and velocity
        List<Double> distances = new ArrayList<>();
        List<Double> velocities = new ArrayList<>();

        // Initialize former distance and former velocity
        double formerDistance = 0.0;
        double formerVelocity = 0.0;

        // Start simulation
        Calendar simulationDateTime;
        simulationDateTime = (Calendar) simulationStartDate.clone();
        while (simulationDateTime.before(simulationEndDate)) {

            // Advance simulation with a time step of 1 hour = 3600 seconds
            solarSystem.advanceSimulationSingleStep(3600);

            // Update simulation date/time
            simulationDateTime = solarSystem.getSimulationDateTime();

            // Spacecraft particle
            Particle spacecraft = solarSystem.getParticle(spacecraftName);

            // Current position of spacecraft w.r.t. the Sun
            Vector3D positionSpacecraft = spacecraft.getPosition();

            // Current velocity of spacecraft w.r.t. the Sun
            Vector3D velocitySpacecraft = spacecraft.getVelocity();

            // Current distance from the Sun in A.U.
            double distance = positionSpacecraft.magnitude()/ SolarSystemParameters.ASTRONOMICALUNIT;

            // Current velocity in km/s
            double velocity = velocitySpacecraft.magnitude()/1000.0;

            if (Math.abs(distance - formerDistance) > 0.01 || Math.abs(velocity - formerVelocity) > 0.01) {
                distances.add(distance);
                velocities.add(velocity);
                formerDistance = distance;
                formerVelocity = velocity;
            }
        }

        // Print results
        System.out.println("Spacecraft " + spacecraftName);
        System.out.println("Launch " + CalendarUtil.calendarToString(simulationStartDate));
        System.out.println("Distance in A.U. and velocity in km/s:");
        int nrEntries = Math.min(distances.size(),velocities.size());
        for (int i = 0; i < nrEntries; i++) {
            System.out.println(distances.get(i) + "\t" + velocities.get(i));
        }
    }

    /**
     * Main method.
     * Simulate trajectories of Voyager 1, Voyager 2, and New Horizons
     *
     * @param args input arguments (not used)
     */
    public static void main(String[] args) {
        // Experiment set-up
        SpacecraftVelocityDistanceExperiment experiment = new SpacecraftVelocityDistanceExperiment();

        // Run experiments
        experiment.simulateSpacecraftTrajectory("Voyager 1");
        experiment.simulateSpacecraftTrajectory("Voyager 2");
        experiment.simulateSpacecraftTrajectory("New Horizons");
    }
}



