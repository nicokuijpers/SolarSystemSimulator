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
import particlesystem.Particle;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.*;

/**
 * Determine date/time and closest distance at flyby of planets and moons for
 * Pioneer 10, Pioneer 11, Voyager 1, Voyager 2, New Horizons, and Rosetta.
 * @author Nico Kuijpers
 */
public class SpacecraftExperiment {

    // Launch Pioneer 10: March 3, 1972, 01:49:00 UTC
    private final GregorianCalendar startDatePioneerTen =
            new GregorianCalendar(1972, 2, 3, 1, 49, 0);

    // End of simulation for Pioneer 10: January 1, 1974
    private final GregorianCalendar endDatePioneerTen =
            new GregorianCalendar(1974, 0, 1, 0, 0, 0);

    // Launch Pioneer 11: April 6, 1973, 02:11:00 UTC.
    private final GregorianCalendar startDatePioneerEleven =
            new GregorianCalendar(1973, 3, 6, 2, 11, 0);

    // End of simulation for Pioneer 11: October 1, 1979
    private final GregorianCalendar endDatePioneerEleven =
            new GregorianCalendar(1979, 9, 1, 0, 0, 0);

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

    // Launch Rosetta: March 2, 2004, 07:17 UTC
    private static final GregorianCalendar startDateRosetta =
            new GregorianCalendar(2004, 2, 2, 7, 17, 0);

    // End of simulation for Rosetta: October 1, 2016
    private final GregorianCalendar endDateRosetta =
            new GregorianCalendar(2016, 9, 1, 0, 0, 0);

    // Simulation start dates
    private final Map<String,GregorianCalendar> startDates = new HashMap<>();

    // Simulation end dates
    private final Map<String,GregorianCalendar> endDates = new HashMap<>();

    // Names of solar system particles passed by spacecraft
    private final Map<String,List<String>> particleNames = new HashMap<>();

    // Solar system
    private SolarSystem solarSystem;

    // Start date for simulation
    private GregorianCalendar simulationStartDate;

    // End date for simulation
    private GregorianCalendar simulationEndDate;

    public SpacecraftExperiment() {

        // Define start dates for simulation
        startDates.put("Pioneer 10", startDatePioneerTen);
        startDates.put("Pioneer 11", startDatePioneerEleven);
        startDates.put("Voyager 1", startDateVoyagerOne);
        startDates.put("Voyager 2", startDateVoyagerTwo);
        startDates.put("New Horizons", startDateNewHorizons);
        startDates.put("Rosetta", startDateRosetta);

        // Define end dates for simulation
        endDates.put("Pioneer 10", endDatePioneerTen);
        endDates.put("Pioneer 11", endDatePioneerEleven);
        endDates.put("Voyager 1", endDateVoyagerOne);
        endDates.put("Voyager 2", endDateVoyagerTwo);
        endDates.put("New Horizons", endDateNewHorizons);
        endDates.put("Rosetta", endDateRosetta);

        // Define names of solar system particles passed by Pioneer 10
        // https://en.wikipedia.org/wiki/Pioneer_10
        List<String> particleNamesPioneerTen = new ArrayList<>();
        particleNamesPioneerTen.add("Callisto");    // 1973-12-03  12:26:00 Callisto flyby at 1,392,300 km
        particleNamesPioneerTen.add("Ganymede");    // 1973-12-03  13:56:00 Ganymede flyby at 446,250 km
        particleNamesPioneerTen.add("Europa");      // 1973-12-03  19:26:00 Europa flyby at 321,000 km
        particleNamesPioneerTen.add("Io");          // 1973-12-03  22:56:00 Io flyby at 357,000 km
        particleNamesPioneerTen.add("Jupiter");     // 1973-12-04  02:26:00 Jupiter closest approach at 200,000 km
        particleNames.put("Pioneer 10",particleNamesPioneerTen);

        // Define names of solar system particles passed by Pioneer 11
        // https://en.wikipedia.org/wiki/Pioneer_11
        List<String> particleNamesPioneerEleven = new ArrayList<>();
        particleNamesPioneerEleven.add("Callisto");    // 1974-12-02  08:21:00 Callisto flyby at 786,500 km.
        particleNamesPioneerEleven.add("Ganymede");    // 1974-12-02  22:09:00 Ganymede flyby at 692,300 km.
        particleNamesPioneerEleven.add("Io");          // 1974-12-03  03:11:00 Io flyby at 314,000 km.
        particleNamesPioneerEleven.add("Europa");      // 1974-12-03  04:15:00 Europa flyby at 586,700 km.
        particleNamesPioneerEleven.add("Jupiter");     // 1974-12-03  05:21:19 Jupiter closest approach at 42,828 km.
        particleNamesPioneerEleven.add("Iapetus");     // 1979-08-29  06:06:10 Iapetus flyby at 1,032,535 km.
        particleNamesPioneerEleven.add("Dione");       // 1979-09-01  15:59:30 Dione flyby at 291,556 km.
        particleNamesPioneerEleven.add("Mimas");       // 1979-09-01  16:26:28 Mimas flyby at 104,263 km.
        particleNamesPioneerEleven.add("Saturn");      // 1979-09-01  16:29:34 Saturn closest approach at 20,591 km.
        particleNamesPioneerEleven.add("Tethys");      // 1979-09-01  18:25:34 Tethys flyby at 329,197 km.
        particleNamesPioneerEleven.add("Enceladus");   // 1979-09-01  18:30:14 Enceladus flyby at 222,027 km.
        particleNamesPioneerEleven.add("Rhea");        // 1979-09-01  22:15:27 Rhea flyby at 345,303 km.
        particleNamesPioneerEleven.add("Titan");       // 1979-09-02  18:00:33 Titan flyby at 362,962 km.
        particleNames.put("Pioneer 11",particleNamesPioneerEleven);

        // Define names of solar system particles passed by Voyager 1
        // https://en.wikipedia.org/wiki/Voyager_1
        List<String> particleNamesVoyagerOne = new ArrayList<>();
        particleNamesVoyagerOne.add("Jupiter");     // 1979-03-05 12:05:26  348,890 km (center of mass)
        particleNamesVoyagerOne.add("Io");          // 1979-03-05 15:14      20,570 km
        particleNamesVoyagerOne.add("Europa");      // 1979-03-05 18:19     733,760 km
        particleNamesVoyagerOne.add("Ganymede");    // 1979-03-06 02:15     114,710 km
        particleNamesVoyagerOne.add("Callisto");    // 1979-03-06 17:08     126,400 km
        particleNamesVoyagerOne.add("Titan");       // 1980-11-12 05:41:21    6,490 km
        particleNamesVoyagerOne.add("Tethys");      // 1980-11-12 22:16:32  415,670 km
        particleNamesVoyagerOne.add("Saturn");      // 1980-11-12 23:46:30  184,300 km (center of mass)
        particleNamesVoyagerOne.add("Mimas");       // 1980-11-13 01:43:12   88,440 km
        particleNamesVoyagerOne.add("Enceladus");   // 1980-11-13 01:51:16  202,040 km
        particleNamesVoyagerOne.add("Rhea");        // 1980-11-13 06:21:53   73,980 km
        // particleNamesVoyagerOne.add("Hyperion"); // 1980-11-13 16:44:41  880,440 km
        particleNames.put("Voyager 1",particleNamesVoyagerOne);

        // Define names of solar system particles passed by Voyager 2
        // https://en.wikipedia.org/wiki/Voyager_2
        List<String> particleNamesVoyagerTwo = new ArrayList<>();
        particleNamesVoyagerTwo.add("Callisto");    // 1979-07-08 12:21      214,930 km
        particleNamesVoyagerTwo.add("Ganymede");    // 1979-07-09 07:14       62,130 km
        particleNamesVoyagerTwo.add("Europa");      // 1979-07-09 17:53      205,720 km
        particleNamesVoyagerTwo.add("Jupiter");     // 1979-07-09 22:29      721,670 km (center of mass)
        particleNamesVoyagerTwo.add("Io");          // 1979-07-09 23:17    1,129,900 km
        particleNamesVoyagerTwo.add("Iapetus");     // 1981-08-22 01:26:57   908,680 km
        // particleNamesVoyagerOne.add("Hyperion"); // 1981-08-25 01:25:26   431,370 km
        particleNamesVoyagerTwo.add("Titan");       // 1981-08-25 09:37:46   666,190 km
        particleNamesVoyagerTwo.add("Dione");       // 1981-08-26 01:04:32   502,310 km
        particleNamesVoyagerTwo.add("Mimas");       // 1981-08-26 02:24:26   309,930 km
        particleNamesVoyagerTwo.add("Saturn");      // 1981-08-26 03:24:05   161,000 km (center of mass)
        particleNamesVoyagerTwo.add("Enceladus");   // 1981-08-26 03:45:16    87,010 km
        particleNamesVoyagerTwo.add("Tethys");      // 1981-08-26 06:12:30    93,010 km
        particleNamesVoyagerTwo.add("Rhea");        // 1981-08-26 06:28:48   645,260 km
        particleNamesVoyagerTwo.add("Miranda");     // 1986-01-24 16:50       29,000 km
        particleNamesVoyagerTwo.add("Ariel");       // 1986-01-24 17:25      127,000 km
        particleNamesVoyagerTwo.add("Umbriel");     // 1986-01-24 17:25      325,000 km
        particleNamesVoyagerTwo.add("Titania");     // 1986-01-24 17:25      365,200 km
        particleNamesVoyagerTwo.add("Oberon");      // 1986-01-24 17:25      470,600 km
        particleNamesVoyagerTwo.add("Uranus");      // 1986-01-24 17:59:47   107,000 km (center of mass)
        particleNamesVoyagerTwo.add("Neptune");     // 1989-08-25 03:56:36     4,950 km (surface)
        particleNamesVoyagerTwo.add("Triton");      // 1989-08-25 09:23       39,800 km
        particleNames.put("Voyager 2",particleNamesVoyagerTwo);

        // Define names of solar system particles passed by New Horizons
        // https://en.wikipedia.org/wiki/New_Horizons
        List<String> particleNamesNewHorizons = new ArrayList<>();
        particleNamesNewHorizons.add("Jupiter");    // 2007-02-28 05:43:40  2.3 million km
        particleNamesNewHorizons.add("Pluto");      // 2015-07-14 11:49     12,472 km (surface)
        particleNamesNewHorizons.add("Arrokoth");   // 2019-01-01 05:34:31   3,537.7 km
        particleNames.put("New Horizons",particleNamesNewHorizons);

        // Define names of solar system particles passed by Rosetta
        // https://en.wikipedia.org/wiki/Rosetta_(spacecraft)
        List<String> particleNamesRosetta = new ArrayList<>();
        particleNamesRosetta.add("Earth"); //
        particleNamesRosetta.add("Mars");  // 2007-02-25   250 km (surface)
        particleNamesRosetta.add("67P/Churyumov-Gerasimenko");
        particleNames.put("Rosetta",particleNamesRosetta);
    }

    private void simulateSpacecraftTrajectory(String spacecraftName) {

        // Set simulation start date
        simulationStartDate = startDates.get(spacecraftName);
        simulationStartDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Set simulation stop date
        simulationEndDate = endDates.get(spacecraftName);
        simulationEndDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Create the Solar System and planet systems
        solarSystem = new SolarSystem();
        try {
            if (spacecraftName.startsWith("Pioneer") || spacecraftName.startsWith("Voyager")) {
                solarSystem.createPlanetSystem("Jupiter");
            }
            if ("Pioneer 11".equals(spacecraftName) || spacecraftName.startsWith("Voyager")) {
                solarSystem.createPlanetSystem("Saturn");
            }
            if ("Voyager 2".equals(spacecraftName)) {
                solarSystem.createPlanetSystem("Uranus");
                solarSystem.createPlanetSystem("Neptune");
            }
            if ("New Horizons".equals(spacecraftName)) {
                solarSystem.createPlanetSystem("Pluto System");
            }
        } catch (SolarSystemException ex) {
            System.err.println(ex.getMessage());
        }

        // Create the spacecraft
        solarSystem.createSpacecraft(spacecraftName);

        // Use Newton Mechanics for simulation
        //solarSystem.setGeneralRelativityFlag(false); // Newton Mechanics

        // Use General Relativity (PPN method) for simulation
        //solarSystem.setGeneralRelativityFlag(true); // General Relativity
        //solarSystem.setCurvatureWavePropagationFlag(false); // Curvature of Wave Propagation

        // Use Curvature of Wave Propagation Method (CWPM) for simulation
        solarSystem.setGeneralRelativityFlag(true); // General Relativity
        solarSystem.setCurvatureWavePropagationFlag(true); // Curvature of Wave Propagation

        // Initialize the Solar System and planet systems
        try {
            solarSystem.initializeSimulation(simulationStartDate);
        } catch (SolarSystemException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

        // Minimum distance between spacecraft and particle
        Map<String, Double> minDistance = new HashMap<>();
        for (String particleName : particleNames.get(spacecraftName)) {
            minDistance.put(particleName, Double.MAX_VALUE);
        }

        // Date/time of minimum distance between spacecraft and particle
        Map<String, Calendar> minDistDate = new HashMap<>();
        for (String particleName : particleNames.get(spacecraftName)) {
            minDistDate.put(particleName, (Calendar) simulationStartDate.clone());
        }

        // Position at time of minimum distance between spacecraft and particle
        Map<String, Vector3D> minDistPosition = new HashMap<>();
        for (String particleName : particleNames.get(spacecraftName)) {
            minDistPosition.put(particleName, new Vector3D());
        }

        // Velocity at time of minimum distance between spacecraft and particle
        Map<String, Vector3D> minDistVelocity = new HashMap<>();
        for (String particleName : particleNames.get(spacecraftName)) {
            minDistVelocity.put(particleName, new Vector3D());
        }

        // Start simulation
        Calendar simulationDateTime;
        simulationDateTime = (Calendar) simulationStartDate.clone();
        while (simulationDateTime.before(simulationEndDate)) {

            // Advance simulation with a time step of 60 seconds
            solarSystem.advanceSimulationSingleStep(60);

            // Update simulation date/time
            simulationDateTime = solarSystem.getSimulationDateTime();

            // Spacecraft particle
            Particle spacecraft = solarSystem.getParticle(spacecraftName);

            // Current position of spacecraft w.r.t. the Sun
            Vector3D positionSpacecraft = spacecraft.getPosition();

            // Current velocity of spacecraft w.r.t. the Sun
            Vector3D velocitySpacecraft = spacecraft.getVelocity();

            // Update minimum distance between spacecraft and other particles
            for (String particleName : particleNames.get(spacecraftName)) {
                Particle particle = solarSystem.getParticle(particleName);
                Vector3D positionParticle = particle.getPosition();
                double distance = positionSpacecraft.euclideanDistance(positionParticle);
                if (distance < minDistance.get(particleName)) {
                    minDistance.put(particleName, distance);
                    minDistDate.put(particleName, (Calendar) simulationDateTime.clone());
                    minDistPosition.put(particleName, new Vector3D(positionSpacecraft));
                    minDistVelocity.put(particleName, new Vector3D(velocitySpacecraft));
                }
            }
        }

        // Print results
        System.out.println("Spacecraft " + spacecraftName);
        System.out.println("Launch " + CalendarUtil.calendarToString(simulationStartDate));
        for (String particleName : particleNames.get(spacecraftName)) {
            double distanceFromCenter = minDistance.get(particleName);
            double diameter = solarSystem.getBody(particleName).getDiameter();
            double distanceFromSurface = distanceFromCenter - 0.5 * diameter;
            System.out.println("Fly by " + particleName);
            System.out.println("Date and time          : " +
                    CalendarUtil.calendarToString(minDistDate.get(particleName)));
            System.out.println("Distance from center   : " +
                    0.001 * distanceFromCenter + " km");
            System.out.println("Distance from surface  : " +
                    0.001 * distanceFromSurface + " km");
            System.out.println("Velocity during fly by : " +
                    minDistVelocity.get(particleName).magnitude() + " m/s");
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
        SpacecraftExperiment experiment = new SpacecraftExperiment();

        // Run experiments
        experiment.simulateSpacecraftTrajectory("Pioneer 10");
        experiment.simulateSpacecraftTrajectory("Pioneer 11");
        experiment.simulateSpacecraftTrajectory("Voyager 1");
        experiment.simulateSpacecraftTrajectory("Voyager 2");
        experiment.simulateSpacecraftTrajectory("New Horizons");
        experiment.simulateSpacecraftTrajectory("Rosetta");
    }
    /*
        Simulation performed December 21, 2020
        Results Newton Mechancis (time step 60s)
        Spacecraft Voyager 1
        Launch 1977-09-05 12:56
        Fly by Jupiter
        Date and time          : 1979-03-05 12:06
        Distance from center   : 348307.29833757645 km
        Distance from surface  : 276815.29833757645 km
        Velocity during fly by : 36075.826829027494 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15
        Distance from center   : 20520.100113501765 km
        Distance from surface  : 18698.800113501766 km
        Velocity during fly by : 36671.61606701444 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:20
        Distance from center   : 732975.4423176781 km
        Distance from surface  : 731410.4423176781 km
        Velocity during fly by : 35308.78932760341 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:17
        Distance from center   : 113168.95968676741 km
        Distance from surface  : 110534.95968676741 km
        Velocity during fly by : 30918.427778591136 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:10
        Distance from center   : 124105.66643136628 km
        Distance from surface  : 121702.66643136628 km
        Velocity during fly by : 28159.30308426901 m/s
        Fly by Titan
        Date and time          : 1980-11-12 05:40
        Distance from center   : 4684.366929080939 km
        Distance from surface  : 2108.866929080939 km
        Velocity during fly by : 21813.667949909264 m/s
        Fly by Tethys
        Date and time          : 1980-11-12 22:15
        Distance from center   : 422177.64244051056 km
        Distance from surface  : 421641.3424405106 km
        Velocity during fly by : 28972.428519531775 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44
        Distance from center   : 190511.10561351234 km
        Distance from surface  : 130243.10561351234 km
        Velocity during fly by : 30479.739971882078 m/s
        Fly by Mimas
        Date and time          : 1980-11-13 01:39
        Distance from center   : 94887.98229978402 km
        Distance from surface  : 94689.18229978402 km
        Velocity during fly by : 29311.26862600851 m/s
        Fly by Enceladus
        Date and time          : 1980-11-13 01:48
        Distance from center   : 212922.82620541507 km
        Distance from surface  : 212670.52620541508 km
        Velocity during fly by : 29146.745039399528 m/s
        Fly by Rhea
        Date and time          : 1980-11-13 06:24
        Distance from center   : 56154.08229101511 km
        Distance from surface  : 55389.58229101511 km
        Velocity during fly by : 25671.27651699476 m/s

        Spacecraft Voyager 2
        Launch 1977-08-20 14:29
        Fly by Callisto
        Date and time          : 1979-07-08 12:22
        Distance from center   : 215539.56268618218 km
        Distance from surface  : 213136.56268618218 km
        Velocity during fly by : 13170.341292101772 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:15
        Distance from center   : 61407.98951779529 km
        Distance from surface  : 58773.98951779529 km
        Velocity during fly by : 17368.452427899472 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:51
        Distance from center   : 205538.80469858614 km
        Distance from surface  : 203973.80469858614 km
        Velocity during fly by : 22728.098194898917 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29
        Distance from center   : 721772.3179243297 km
        Distance from surface  : 650280.3179243297 km
        Velocity during fly by : 25178.35937654697 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:18
        Distance from center   : 1129812.1723818842 km
        Distance from surface  : 1127990.8723818841 km
        Velocity during fly by : 25517.07416110551 m/s
        Fly by Iapetus
        Date and time          : 1981-08-23 01:27
        Distance from center   : 908845.6034178874 km
        Distance from surface  : 908111.1034178874 km
        Velocity during fly by : 16166.254303150803 m/s
        Fly by Titan
        Date and time          : 1981-08-25 09:37
        Distance from center   : 663766.3155168174 km
        Distance from surface  : 661190.8155168174 km
        Velocity during fly by : 18165.227384697635 m/s
        Fly by Dione
        Date and time          : 1981-08-26 01:03
        Distance from center   : 500623.99878533324 km
        Distance from surface  : 500061.49878533324 km
        Velocity during fly by : 27213.001434650654 m/s
        Fly by Mimas
        Date and time          : 1981-08-26 02:33
        Distance from center   : 308951.3079681845 km
        Distance from surface  : 308752.50796818454 km
        Velocity during fly by : 30876.99560155576 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23
        Distance from center   : 159077.20676037716 km
        Distance from surface  : 98809.20676037717 km
        Velocity during fly by : 32321.41204779268 m/s
        Fly by Enceladus
        Date and time          : 1981-08-26 03:41
        Distance from center   : 89517.73909146323 km
        Distance from surface  : 89265.43909146322 km
        Velocity during fly by : 32527.61267775264 m/s
        Fly by Tethys
        Date and time          : 1981-08-26 06:09
        Distance from center   : 94049.64359664418 km
        Distance from surface  : 93513.34359664418 km
        Velocity during fly by : 30083.391246047944 m/s
        Fly by Rhea
        Date and time          : 1981-08-26 06:30
        Distance from center   : 641566.2901272982 km
        Distance from surface  : 640801.7901272982 km
        Velocity during fly by : 29627.095838417186 m/s
        Fly by Miranda
        Date and time          : 1986-01-24 17:01
        Distance from center   : 31934.07506936233 km
        Distance from surface  : 31694.07506936233 km
        Velocity during fly by : 21374.991474550814 m/s
        Fly by Ariel
        Date and time          : 1986-01-24 16:19
        Distance from center   : 129778.65463950277 km
        Distance from surface  : 129197.65463950277 km
        Velocity during fly by : 20671.698281757406 m/s
        Fly by Umbriel
        Date and time          : 1986-01-24 20:51
        Distance from center   : 316194.67913991923 km
        Distance from surface  : 315609.67913991923 km
        Velocity during fly by : 21420.675652121863 m/s
        Fly by Titania
        Date and time          : 1986-01-24 15:08
        Distance from center   : 368394.342428635 km
        Distance from surface  : 367605.342428635 km
        Velocity during fly by : 19834.488090117284 m/s
        Fly by Oberon
        Date and time          : 1986-01-24 16:09
        Distance from center   : 473595.1701008287 km
        Distance from surface  : 472833.6701008287 km
        Velocity during fly by : 20525.751616180227 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:56
        Distance from center   : 101305.76184686647 km
        Distance from surface  : 75746.76184686647 km
        Velocity during fly by : 22196.720745041715 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 03:56:00.000
        Distance from center   : 29453.315306113403 km
        Distance from surface  : 4689.3153061134035 km
        Velocity during fly by : 28076.754905563557 m/s
        Fly by Triton
        Date and time          : 1989-08-25 09:11:00.000
        Distance from center   : 39753.76717583183 km
        Distance from surface  : 38401.26717583183 km
        Velocity during fly by : 17769.757054743604 m/s

        Spacecraft New Horizons
        Launch 2006-01-19 19:00
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49
        Distance from center   : 2302925.2222991926 km
        Distance from surface  : 2231433.2222991926 km
        Velocity during fly by : 22850.51615014031 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:37
        Distance from center   : 13256.341556581314 km
        Distance from surface  : 12071.341556581314 km
        Velocity during fly by : 14531.808243819474 m/s
        Fly by Ultima Thule
        Date and time          : 2019-01-01 05:35
        Distance from center   : 3561.40458165672 km
        Distance from surface  : 3544.90458165672 km
        Velocity during fly by : 14095.250816970856 m/s

        Spacecraft Rosetta
        Launch 2004-03-02 07:17
        Fly by Earth
        Date and time          : 2004-03-02 09:40
        Distance from center   : 8134.146419032116 km
        Distance from surface  : 1756.1464190321155 km
        Velocity during fly by : 27062.920740839192 m/s
        Fly by Mars
        Date and time          : 2007-02-25 01:46
        Distance from center   : 3655.4747147726694 km
        Distance from surface  : 259.47471477266913 km
        Velocity during fly by : 22849.080399982675 m/s
        Fly by 67P/Churyumov-Gerasimenko
        Date and time          : 2015-06-18 11:37
        Distance from center   : 339.2829359666815 km
        Distance from surface  : 337.2329359666815 km
        Velocity during fly by : 31595.336238707136 m/s

        Spacecraft Pioneer 10 (simulation date May 7, 2021)
        Launch 1972-03-03 01:49:00.000
        Fly by Callisto
        Date and time          : 1973-12-03 09:54:00.000
        Distance from center   : 1409298.3380197568 km
        Distance from surface  : 1406895.3380197568 km
        Velocity during fly by : 13975.603699765195 m/s
        Fly by Ganymede
        Date and time          : 1973-12-03 14:05:00.000
        Distance from center   : 448279.71166178194 km
        Distance from surface  : 445645.71166178194 km
        Velocity during fly by : 15500.853803781387 m/s
        Fly by Europa
        Date and time          : 1973-12-03 19:39:00.000
        Distance from center   : 319166.5922292542 km
        Distance from surface  : 317601.5922292542 km
        Velocity during fly by : 19872.920316381624 m/s
        Fly by Io
        Date and time          : 1973-12-03 23:02:00.000
        Distance from center   : 356521.870811877 km
        Distance from surface  : 354700.570811877 km
        Velocity during fly by : 26942.345880889075 m/s
        Fly by Jupiter
        Date and time          : 1973-12-04 02:21:00.000
        Distance from center   : 202359.23881493477 km
        Distance from surface  : 130867.23881493477 km
        Velocity during fly by : 42701.98836175349 m/s

        Spacecraft Pioneer 11 (simulation date May 7, 2021)
        Launch 1973-04-06 02:11:00.000
        Fly by Callisto
        Date and time          : 1974-12-02 08:17:00.000
        Distance from center   : 781210.9397950935 km
        Distance from surface  : 778807.9397950935 km
        Velocity during fly by : 11924.314274116385 m/s
        Fly by Ganymede
        Date and time          : 1974-12-02 22:11:00.000
        Distance from center   : 690444.4922331569 km
        Distance from surface  : 687810.4922331569 km
        Velocity during fly by : 15024.219825877835 m/s
        Fly by Io
        Date and time          : 1974-12-03 03:08:00.000
        Distance from center   : 313382.66861816426 km
        Distance from surface  : 311561.36861816427 km
        Velocity during fly by : 22694.76917893401 m/s
        Fly by Europa
        Date and time          : 1974-12-03 04:19:00.000
        Distance from center   : 586709.960211978 km
        Distance from surface  : 585144.960211978 km
        Velocity during fly by : 29850.970484327274 m/s
        Fly by Jupiter
        Date and time          : 1974-12-03 05:22:00.000
        Distance from center   : 114138.86664760721 km
        Distance from surface  : 42646.86664760721 km
        Velocity during fly by : 40019.62227493421 m/s
        Fly by Iapetus
        Date and time          : 1979-08-29 06:11:00.000
        Distance from center   : 1033134.8036386592 km
        Distance from surface  : 1032400.3036386592 km
        Velocity during fly by : 12460.34714134536 m/s
        Fly by Dione
        Date and time          : 1979-09-01 16:05:00.000
        Distance from center   : 291850.3843786928 km
        Distance from surface  : 291287.8843786928 km
        Velocity during fly by : 35617.805136980285 m/s
        Fly by Mimas
        Date and time          : 1979-09-01 16:33:00.000
        Distance from center   : 104338.7874611191 km
        Distance from surface  : 104139.98746111909 km
        Velocity during fly by : 38715.5406451578 m/s
        Fly by Saturn
        Date and time          : 1979-09-01 16:34:00.000
        Distance from center   : 80748.60658162706 km
        Distance from surface  : 20480.606581627057 km
        Velocity during fly by : 38769.94826750164 m/s
        Fly by Tethys
        Date and time          : 1979-09-01 18:27:00.000
        Distance from center   : 332130.1393643106 km
        Distance from surface  : 331593.83936431055 km
        Velocity during fly by : 31751.925247953033 m/s
        Fly by Enceladus
        Date and time          : 1979-09-01 18:33:00.000
        Distance from center   : 225919.42989918758 km
        Distance from surface  : 225667.12989918757 km
        Velocity during fly by : 31403.795234907757 m/s
        Fly by Rhea
        Date and time          : 1979-09-01 22:46:00.000
        Distance from center   : 339040.0123134408 km
        Distance from surface  : 338275.5123134408 km
        Velocity during fly by : 24758.898329180232 m/s
        Fly by Titan
        Date and time          : 1979-09-02 18:03:00.000
        Distance from center   : 344837.18964169215 km
        Distance from surface  : 342261.68964169215 km
        Velocity during fly by : 20228.842884143705 m/s
     */

    /*
        Simulation performed December 21, 2020
        Results General Relativity (time step 60s)
        Spacecraft Voyager 1
        Launch 1977-09-05 12:56
        Fly by Jupiter
        Date and time          : 1979-03-05 12:06
        Distance from center   : 348307.35635866714 km
        Distance from surface  : 276815.35635866714 km
        Velocity during fly by : 36075.77934489113 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15
        Distance from center   : 20519.885121355637 km
        Distance from surface  : 18698.585121355638 km
        Velocity during fly by : 36671.63327960877 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:20
        Distance from center   : 732976.8163039136 km
        Distance from surface  : 731411.8163039136 km
        Velocity during fly by : 35308.813161975355 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:17
        Distance from center   : 113170.42513319735 km
        Distance from surface  : 110536.42513319735 km
        Velocity during fly by : 30918.438594161144 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:10
        Distance from center   : 124105.8601680871 km
        Distance from surface  : 121702.8601680871 km
        Velocity during fly by : 28159.30687684898 m/s
        Fly by Titan
        Date and time          : 1980-11-12 05:40
        Distance from center   : 4683.491371170795 km
        Distance from surface  : 2107.9913711707945 km
        Velocity during fly by : 21813.66634799477 m/s
        Fly by Tethys
        Date and time          : 1980-11-12 22:15
        Distance from center   : 422177.0238613656 km
        Distance from surface  : 421640.72386136564 km
        Velocity during fly by : 28972.409718301693 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44
        Distance from center   : 190510.84306433675 km
        Distance from surface  : 130242.84306433675 km
        Velocity during fly by : 30479.740214962876 m/s
        Fly by Mimas
        Date and time          : 1980-11-13 01:39
        Distance from center   : 94887.80914220463 km
        Distance from surface  : 94689.00914220462 km
        Velocity during fly by : 29311.282348877437 m/s
        Fly by Enceladus
        Date and time          : 1980-11-13 01:48
        Distance from center   : 212922.96316103716 km
        Distance from surface  : 212670.66316103714 km
        Velocity during fly by : 29146.758246749738 m/s
        Fly by Rhea
        Date and time          : 1980-11-13 06:24
        Distance from center   : 56154.74149284827 km
        Distance from surface  : 55390.24149284827 km
        Velocity during fly by : 25671.273658483995 m/s

        Spacecraft Voyager 2
        Launch 1977-08-20 14:29:00
        Fly by Callisto
        Date and time          : 1979-07-08 12:22:00
        Distance from center   : 215541.24196331901 km
        Distance from surface  : 213138.24196331901 km
        Velocity during fly by : 13170.332492335365 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:15:00
        Distance from center   : 61406.44492081986 km
        Distance from surface  : 58772.44492081986 km
        Velocity during fly by : 17368.422801993845 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:51:00
        Distance from center   : 205535.9859458097 km
        Distance from surface  : 203970.9859458097 km
        Velocity during fly by : 22728.048620505513 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29:00
        Distance from center   : 721772.723107829 km
        Distance from surface  : 650280.723107829 km
        Velocity during fly by : 25178.318737263267 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:18:00
        Distance from center   : 1129811.6708156385 km
        Distance from surface  : 1127990.3708156385 km
        Velocity during fly by : 25517.036840463283 m/s
        Fly by Iapetus
        Date and time          : 1981-08-23 01:27:00
        Distance from center   : 908846.2375971326 km
        Distance from surface  : 908111.7375971326 km
        Velocity during fly by : 16166.253943065156 m/s
        Fly by Titan
        Date and time          : 1981-08-25 09:37:00
        Distance from center   : 663764.8728724387 km
        Distance from surface  : 661189.3728724387 km
        Velocity during fly by : 18165.22233773752 m/s
        Fly by Dione
        Date and time          : 1981-08-26 01:03:00
        Distance from center   : 500622.85424113984 km
        Distance from surface  : 500060.35424113984 km
        Velocity during fly by : 27212.955523740875 m/s
        Fly by Mimas
        Date and time          : 1981-08-26 02:33:00
        Distance from center   : 308950.0064784731 km
        Distance from surface  : 308751.2064784731 km
        Velocity during fly by : 30876.979541783894 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23:00
        Distance from center   : 159076.38394984335 km
        Distance from surface  : 98808.38394984335 km
        Velocity during fly by : 32321.44346202247 m/s
        Fly by Enceladus
        Date and time          : 1981-08-26 03:41:00
        Distance from center   : 89518.11945560142 km
        Distance from surface  : 89265.81945560142 km
        Velocity during fly by : 32527.65993894021 m/s
        Fly by Tethys
        Date and time          : 1981-08-26 06:09:00
        Distance from center   : 94050.34894063746 km
        Distance from surface  : 93514.04894063745 km
        Velocity during fly by : 30083.44142165961 m/s
        Fly by Rhea
        Date and time          : 1981-08-26 06:30:00
        Distance from center   : 641565.137847428 km
        Distance from surface  : 640800.637847428 km
        Velocity during fly by : 29627.140637398785 m/s
        Fly by Miranda
        Date and time          : 1986-01-24 17:01:00
        Distance from center   : 31934.642378201745 km
        Distance from surface  : 31694.642378201745 km
        Velocity during fly by : 21374.99155669474 m/s
        Fly by Ariel
        Date and time          : 1986-01-24 16:19:00
        Distance from center   : 129779.03968212566 km
        Distance from surface  : 129198.03968212566 km
        Velocity during fly by : 20671.693678842632 m/s
        Fly by Umbriel
        Date and time          : 1986-01-24 20:51:00
        Distance from center   : 316193.7983522202 km
        Distance from surface  : 315608.7983522202 km
        Velocity during fly by : 21420.689982286334 m/s
        Fly by Titania
        Date and time          : 1986-01-24 15:08:00
        Distance from center   : 368394.7175267284 km
        Distance from surface  : 367605.7175267284 km
        Velocity during fly by : 19834.483370442013 m/s
        Fly by Oberon
        Date and time          : 1986-01-24 16:09:00
        Distance from center   : 473595.6185112137 km
        Distance from surface  : 472834.1185112137 km
        Velocity during fly by : 20525.746666251263 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:56:00
        Distance from center   : 101305.16067278566 km
        Distance from surface  : 75746.16067278566 km
        Velocity during fly by : 22196.736864859944 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 03:56:00.000
        Distance from center   : 29453.315236361315 km
        Distance from surface  : 4689.3152363613135 km
        Velocity during fly by : 28076.75476094293 m/s
        Fly by Triton
        Date and time          : 1989-08-25 09:11:00.000
        Distance from center   : 39753.77584301235 km
        Distance from surface  : 38401.27584301235 km
        Velocity during fly by : 17769.757073697605 m/s

        Spacecraft New Horizons
        Launch 2006-01-19 19:00:00
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49:00
        Distance from center   : 2302923.951850609 km
        Distance from surface  : 2231431.951850609 km
        Velocity during fly by : 22850.53636598604 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:37:00
        Distance from center   : 13256.271594610027 km
        Distance from surface  : 12071.271594610027 km
        Velocity during fly by : 14531.80829154243 m/s
        Fly by Ultima Thule
        Date and time          : 2019-01-01 05:35:00
        Distance from center   : 3561.40458165672 km
        Distance from surface  : 3544.90458165672 km
        Velocity during fly by : 14095.250816974672 m/s

        Spacecraft Rosetta
        Launch 2004-03-02 07:17:00
        Fly by Earth
        Date and time          : 2004-03-02 09:40:00
        Distance from center   : 8136.20416328313 km
        Distance from surface  : 1758.2041632831292 km
        Velocity during fly by : 27067.19545202558 m/s
        Fly by Mars
        Date and time          : 2007-02-25 01:46:00
        Distance from center   : 3738.1035996640226 km
        Distance from surface  : 342.1035996640227 km
        Velocity during fly by : 22890.79039104631 m/s
        Fly by 67P/Churyumov-Gerasimenko
        Date and time          : 2015-06-18 08:54:00
        Distance from center   : 337.8518250432268 km
        Distance from surface  : 335.8018250432268 km
        Velocity during fly by : 31586.460508391603 m/s
     */

    /*
        Simulation performed December 1, 2025
        Results Newton Mechanics (time step 60s)
        Spacecraft Pioneer 10
        Launch 1972-03-03 01:49:00.000
        Fly by Callisto
        Date and time          : 1973-12-03 09:53:00.000
        Distance from center   : 1409207.545113912 km
        Distance from surface  : 1406804.545113912 km
        Velocity during fly by : 13970.947881231328 m/s
        Fly by Ganymede
        Date and time          : 1973-12-03 14:05:00.000
        Distance from center   : 448282.5680818316 km
        Distance from surface  : 445648.5680818316 km
        Velocity during fly by : 15500.853803053898 m/s
        Fly by Europa
        Date and time          : 1973-12-03 19:39:00.000
        Distance from center   : 319084.73193296423 km
        Distance from surface  : 317519.73193296423 km
        Velocity during fly by : 19872.920314540977 m/s
        Fly by Io
        Date and time          : 1973-12-03 23:02:00.000
        Distance from center   : 356589.81654206006 km
        Distance from surface  : 354768.51654206007 km
        Velocity during fly by : 26942.345876525662 m/s
        Fly by Jupiter
        Date and time          : 1973-12-04 02:21:00.000
        Distance from center   : 202359.23880177198 km
        Distance from surface  : 130867.23880177196 km
        Velocity during fly by : 42701.988358786184 m/s

        Spacecraft Pioneer 11
        Launch 1973-04-06 02:11:00.000
        Fly by Callisto
        Date and time          : 1974-12-02 08:17:00.000
        Distance from center   : 781246.3811103321 km
        Distance from surface  : 778843.3811103321 km
        Velocity during fly by : 11924.314274018887 m/s
        Fly by Ganymede
        Date and time          : 1974-12-02 22:11:00.000
        Distance from center   : 690468.2898088091 km
        Distance from surface  : 687834.2898088091 km
        Velocity during fly by : 15024.21982551495 m/s
        Fly by Io
        Date and time          : 1974-12-03 03:08:00.000
        Distance from center   : 313412.00282707764 km
        Distance from surface  : 311590.70282707765 km
        Velocity during fly by : 22694.769177048543 m/s
        Fly by Europa
        Date and time          : 1974-12-03 04:19:00.000
        Distance from center   : 586713.023484859 km
        Distance from surface  : 585148.023484859 km
        Velocity during fly by : 29850.970479415937 m/s
        Fly by Jupiter
        Date and time          : 1974-12-03 05:22:00.000
        Distance from center   : 114138.86667373242 km
        Distance from surface  : 42646.86667373242 km
        Velocity during fly by : 40019.62226690484 m/s
        Fly by Iapetus
        Date and time          : 1979-08-29 06:11:00.000
        Distance from center   : 1033152.5942051548 km
        Distance from surface  : 1032418.0942051548 km
        Velocity during fly by : 12460.347140843514 m/s
        Fly by Dione
        Date and time          : 1979-09-01 16:05:00.000
        Distance from center   : 291855.92146148597 km
        Distance from surface  : 291293.42146148597 km
        Velocity during fly by : 35617.804952920866 m/s
        Fly by Mimas
        Date and time          : 1979-09-01 16:33:00.000
        Distance from center   : 104515.26110302626 km
        Distance from surface  : 104316.46110302626 km
        Velocity during fly by : 38715.5406511452 m/s
        Fly by Saturn
        Date and time          : 1979-09-01 16:34:00.000
        Distance from center   : 80748.6060151736 km
        Distance from surface  : 20480.6060151736 km
        Velocity during fly by : 38769.94828218049 m/s
        Fly by Tethys
        Date and time          : 1979-09-01 18:27:00.000
        Distance from center   : 332020.126271457 km
        Distance from surface  : 331483.826271457 km
        Velocity during fly by : 31751.92533719468 m/s
        Fly by Enceladus
        Date and time          : 1979-09-01 18:33:00.000
        Distance from center   : 225613.35164417076 km
        Distance from surface  : 225361.05164417077 km
        Velocity during fly by : 31403.7953176936 m/s
        Fly by Rhea
        Date and time          : 1979-09-01 22:46:00.000
        Distance from center   : 339138.33050732565 km
        Distance from surface  : 338373.83050732565 km
        Velocity during fly by : 24758.898332637167 m/s
        Fly by Titan
        Date and time          : 1979-09-02 18:03:00.000
        Distance from center   : 344747.80402729346 km
        Distance from surface  : 342172.30402729346 km
        Velocity during fly by : 20228.842873881316 m/s

        Spacecraft Voyager 1
        Launch 1977-09-05 12:56:00.000
        Fly by Jupiter
        Date and time          : 1979-03-05 12:06:00.000
        Distance from center   : 348307.2983560417 km
        Distance from surface  : 276815.2983560417 km
        Velocity during fly by : 36075.82682653201 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15:00.000
        Distance from center   : 20524.855738555594 km
        Distance from surface  : 18703.555738555595 km
        Velocity during fly by : 36671.61606720273 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:20:00.000
        Distance from center   : 732878.1362367232 km
        Distance from surface  : 731313.1362367232 km
        Velocity during fly by : 35308.78932824331 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:17:00.000
        Distance from center   : 113155.68272062937 km
        Distance from surface  : 110521.68272062937 km
        Velocity during fly by : 30918.427778939116 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:10:00.000
        Distance from center   : 124184.72897512112 km
        Distance from surface  : 121781.72897512112 km
        Velocity during fly by : 28159.30308438011 m/s
        Fly by Titan
        Date and time          : 1980-11-12 05:40:00.000
        Distance from center   : 4896.901281195745 km
        Distance from surface  : 2321.401281195745 km
        Velocity during fly by : 21813.66795076577 m/s
        Fly by Tethys
        Date and time          : 1980-11-12 22:15:00.000
        Distance from center   : 422210.9943920385 km
        Distance from surface  : 421674.69439203845 km
        Velocity during fly by : 28972.42854821398 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44:00.000
        Distance from center   : 190511.10516006206 km
        Distance from surface  : 130243.10516006204 km
        Velocity during fly by : 30479.739991941795 m/s
        Fly by Mimas
        Date and time          : 1980-11-13 01:39:00.000
        Distance from center   : 94783.30577257625 km
        Distance from surface  : 94584.50577257625 km
        Velocity during fly by : 29311.26861642774 m/s
        Fly by Enceladus
        Date and time          : 1980-11-13 01:48:00.000
        Distance from center   : 212482.30840600797 km
        Distance from surface  : 212230.00840600798 km
        Velocity during fly by : 29146.74502884562 m/s
        Fly by Rhea
        Date and time          : 1980-11-13 06:24:00.000
        Distance from center   : 56106.15644035341 km
        Distance from surface  : 55341.65644035341 km
        Velocity during fly by : 25671.276506426246 m/s

        Spacecraft Voyager 2
        Launch 1977-08-20 14:29:00.000
        Fly by Callisto
        Date and time          : 1979-07-08 12:22:00.000
        Distance from center   : 215368.0870579236 km
        Distance from surface  : 212965.0870579236 km
        Velocity during fly by : 13170.341291802357 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:15:00.000
        Distance from center   : 61495.8652636238 km
        Distance from surface  : 58861.8652636238 km
        Velocity during fly by : 17368.452426746695 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:51:00.000
        Distance from center   : 205590.22572007234 km
        Distance from surface  : 204025.22572007234 km
        Velocity during fly by : 22728.09819272083 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29:00.000
        Distance from center   : 721772.3179647592 km
        Distance from surface  : 650280.3179647592 km
        Velocity during fly by : 25178.359374577936 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:18:00.000
        Distance from center   : 1129829.186255339 km
        Distance from surface  : 1128007.8862553393 km
        Velocity during fly by : 25517.074159252516 m/s
        Fly by Iapetus
        Date and time          : 1981-08-23 01:28:00.000
        Distance from center   : 908885.1361993174 km
        Distance from surface  : 908150.6361993174 km
        Velocity during fly by : 16166.416457516134 m/s
        Fly by Titan
        Date and time          : 1981-08-25 09:37:00.000
        Distance from center   : 663951.4233163835 km
        Distance from surface  : 661375.9233163835 km
        Velocity during fly by : 18165.227388156567 m/s
        Fly by Dione
        Date and time          : 1981-08-26 01:04:00.000
        Distance from center   : 500652.1693744575 km
        Distance from surface  : 500089.6693744575 km
        Velocity during fly by : 27250.33975741702 m/s
        Fly by Mimas
        Date and time          : 1981-08-26 02:33:00.000
        Distance from center   : 308695.9498475217 km
        Distance from surface  : 308497.14984752174 km
        Velocity during fly by : 30876.995708732717 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23:00.000
        Distance from center   : 159077.20622966945 km
        Distance from surface  : 98809.20622966945 km
        Velocity during fly by : 32321.412119324374 m/s
        Fly by Enceladus
        Date and time          : 1981-08-26 03:41:00.000
        Distance from center   : 89661.40567822871 km
        Distance from surface  : 89409.1056782287 km
        Velocity during fly by : 32527.612727845073 m/s
        Fly by Tethys
        Date and time          : 1981-08-26 06:09:00.000
        Distance from center   : 94051.63778084052 km
        Distance from surface  : 93515.33778084052 km
        Velocity during fly by : 30083.391209929167 m/s
        Fly by Rhea
        Date and time          : 1981-08-26 06:30:00.000
        Distance from center   : 641512.7576970981 km
        Distance from surface  : 640748.2576970981 km
        Velocity during fly by : 29627.09580258339 m/s
        Fly by Miranda
        Date and time          : 1986-01-24 17:01:00.000
        Distance from center   : 31784.31776936306 km
        Distance from surface  : 31544.31776936306 km
        Velocity during fly by : 21374.991060557164 m/s
        Fly by Ariel
        Date and time          : 1986-01-24 16:19:00.000
        Distance from center   : 129855.2312653822 km
        Distance from surface  : 129274.2312653822 km
        Velocity during fly by : 20671.69799841878 m/s
        Fly by Umbriel
        Date and time          : 1986-01-24 20:51:00.000
        Distance from center   : 316188.7244150444 km
        Distance from surface  : 315603.7244150444 km
        Velocity during fly by : 21420.67560262536 m/s
        Fly by Titania
        Date and time          : 1986-01-24 15:08:00.000
        Distance from center   : 368410.0919780897 km
        Distance from surface  : 367621.0919780897 km
        Velocity during fly by : 19834.487952648706 m/s
        Fly by Oberon
        Date and time          : 1986-01-24 16:09:00.000
        Distance from center   : 473579.87891685066 km
        Distance from surface  : 472818.37891685066 km
        Velocity during fly by : 20525.7513606039 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:56:00.000
        Distance from center   : 101305.7709069307 km
        Distance from surface  : 75746.7709069307 km
        Velocity during fly by : 22196.720303456652 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 03:56:00.000
        Distance from center   : 29453.917699754533 km
        Distance from surface  : 4689.917699754533 km
        Velocity during fly by : 28076.783807931573 m/s
        Fly by Triton
        Date and time          : 1989-08-25 09:11:00.000
        Distance from center   : 39776.142261516005 km
        Distance from surface  : 38423.642261516005 km
        Velocity during fly by : 17769.699064285687 m/s

        Spacecraft New Horizons
        Launch 2006-01-19 19:00:00.000
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49:00.000
        Distance from center   : 2302925.2222724627 km
        Distance from surface  : 2231433.2222724627 km
        Velocity during fly by : 22850.516150186115 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:36:00.000
        Distance from center   : 11551.017862449231 km
        Distance from surface  : 10366.017862449231 km
        Velocity during fly by : 14531.900041303776 m/s
        Fly by Arrokoth
        Date and time          : 2019-01-01 05:35:00.000
        Distance from center   : 3561.40458165672 km
        Distance from surface  : 3544.90458165672 km
        Velocity during fly by : 14095.250816970905 m/s

        Spacecraft Rosetta
        Launch 2004-03-02 07:17:00.000
        Fly by Earth
        Date and time          : 2004-03-02 09:40:00.000
        Distance from center   : 8134.144336794235 km
        Distance from surface  : 1756.144336794235 km
        Velocity during fly by : 27062.91457631745 m/s
        Fly by Mars
        Date and time          : 2007-02-25 01:46:00.000
        Distance from center   : 3571.4708003560163 km
        Distance from surface  : 175.47080035601604 km
        Velocity during fly by : 22789.83895534331 m/s
        Fly by 67P/Churyumov-Gerasimenko
        Date and time          : 2015-07-12 00:00:00.000
        Distance from center   : 234.36421949466967 km
        Distance from surface  : 232.3142194946697 km
        Velocity during fly by : 33230.38623321409 m/s
     */

    /*
        Simulation performed December 3, 2025
        Results Curvature of Wave Propagation Method (time step 60s)
        Spacecraft Pioneer 10
        Launch 1972-03-03 01:49:00.000
        Fly by Callisto
        Date and time          : 1973-12-03 09:53:00.000
        Distance from center   : 1409208.2268904836 km
        Distance from surface  : 1406805.2268904836 km
        Velocity during fly by : 13970.932162141911 m/s
        Fly by Ganymede
        Date and time          : 1973-12-03 14:05:00.000
        Distance from center   : 448281.29969838605 km
        Distance from surface  : 445647.29969838605 km
        Velocity during fly by : 15500.826856686457 m/s
        Fly by Europa
        Date and time          : 1973-12-03 19:39:00.000
        Distance from center   : 319086.2913891285 km
        Distance from surface  : 317521.2913891285 km
        Velocity during fly by : 19872.8458696972 m/s
        Fly by Io
        Date and time          : 1973-12-03 23:02:00.000
        Distance from center   : 356587.98586707015 km
        Distance from surface  : 354766.68586707016 km
        Velocity during fly by : 26942.153067037994 m/s
        Fly by Jupiter
        Date and time          : 1973-12-04 02:21:00.000
        Distance from center   : 202359.3306985732 km
        Distance from surface  : 130867.3306985732 km
        Velocity during fly by : 42701.78764305098 m/s

        Spacecraft Pioneer 11
        Launch 1973-04-06 02:11:00.000
        Fly by Callisto
        Date and time          : 1974-12-02 08:17:00.000
        Distance from center   : 781245.822932619 km
        Distance from surface  : 778842.822932619 km
        Velocity during fly by : 11924.310360370844 m/s
        Fly by Ganymede
        Date and time          : 1974-12-02 22:11:00.000
        Distance from center   : 690467.7252261948 km
        Distance from surface  : 687833.7252261948 km
        Velocity during fly by : 15024.189599217532 m/s
        Fly by Io
        Date and time          : 1974-12-03 03:08:00.000
        Distance from center   : 313411.6576820588 km
        Distance from surface  : 311590.3576820588 km
        Velocity during fly by : 22694.571410175606 m/s
        Fly by Europa
        Date and time          : 1974-12-03 04:19:00.000
        Distance from center   : 586713.0015283254 km
        Distance from surface  : 585148.0015283254 km
        Velocity during fly by : 29850.5270095809 m/s
        Fly by Jupiter
        Date and time          : 1974-12-03 05:22:00.000
        Distance from center   : 114138.74445860356 km
        Distance from surface  : 42646.74445860356 km
        Velocity during fly by : 40019.38354473239 m/s
        Fly by Iapetus
        Date and time          : 1979-08-29 06:11:00.000
        Distance from center   : 1033151.657309835 km
        Distance from surface  : 1032417.157309835 km
        Velocity during fly by : 12460.345658275457 m/s
        Fly by Dione
        Date and time          : 1979-09-01 16:05:00.000
        Distance from center   : 291857.4146333921 km
        Distance from surface  : 291294.9146333921 km
        Velocity during fly by : 35616.9056771392 m/s
        Fly by Mimas
        Date and time          : 1979-09-01 16:33:00.000
        Distance from center   : 104516.49148945791 km
        Distance from surface  : 104317.69148945791 km
        Velocity during fly by : 38715.318753196036 m/s
        Fly by Saturn
        Date and time          : 1979-09-01 16:34:00.000
        Distance from center   : 80747.60684283823 km
        Distance from surface  : 20479.606842838228 km
        Velocity during fly by : 38769.7611985874 m/s
        Fly by Tethys
        Date and time          : 1979-09-01 18:27:00.000
        Distance from center   : 332020.1456549341 km
        Distance from surface  : 331483.8456549341 km
        Velocity during fly by : 31752.3033620588 m/s
        Fly by Enceladus
        Date and time          : 1979-09-01 18:33:00.000
        Distance from center   : 225614.3511637363 km
        Distance from surface  : 225362.05116373632 km
        Velocity during fly by : 31404.14998529078 m/s
        Fly by Rhea
        Date and time          : 1979-09-01 22:46:00.000
        Distance from center   : 339140.0532608504 km
        Distance from surface  : 338375.5532608504 km
        Velocity during fly by : 24758.94089644545 m/s
        Fly by Titan
        Date and time          : 1979-09-02 18:03:00.000
        Distance from center   : 344757.4762006058 km
        Distance from surface  : 342181.9762006058 km
        Velocity during fly by : 20228.819508224857 m/s

        Spacecraft Voyager 1
        Launch 1977-09-05 12:56:00.000
        Fly by Jupiter
        Date and time          : 1979-03-05 12:06:00.000
        Distance from center   : 348307.36767789663 km
        Distance from surface  : 276815.36767789663 km
        Velocity during fly by : 36075.77883148162 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15:00.000
        Distance from center   : 20524.707548913328 km
        Distance from surface  : 18703.407548913325 km
        Velocity during fly by : 36671.6326647317 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:20:00.000
        Distance from center   : 732879.0993664034 km
        Distance from surface  : 731314.0993664034 km
        Velocity during fly by : 35308.8126914881 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:17:00.000
        Distance from center   : 113156.76559071764 km
        Distance from surface  : 110522.76559071764 km
        Velocity during fly by : 30918.438307245604 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:10:00.000
        Distance from center   : 124184.88948576481 km
        Distance from surface  : 121781.88948576481 km
        Velocity during fly by : 28159.3065600026 m/s
        Fly by Titan
        Date and time          : 1980-11-12 05:40:00.000
        Distance from center   : 4896.150857087881 km
        Distance from surface  : 2320.6508570878814 km
        Velocity during fly by : 21813.6663555264 m/s
        Fly by Tethys
        Date and time          : 1980-11-12 22:15:00.000
        Distance from center   : 422210.52708706714 km
        Distance from surface  : 421674.22708706715 km
        Velocity during fly by : 28972.40980307345 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44:00.000
        Distance from center   : 190510.84572799582 km
        Distance from surface  : 130242.84572799581 km
        Velocity during fly by : 30479.740168937235 m/s
        Fly by Mimas
        Date and time          : 1980-11-13 01:39:00.000
        Distance from center   : 94783.10838094419 km
        Distance from surface  : 94584.30838094419 km
        Velocity during fly by : 29311.282270673055 m/s
        Fly by Enceladus
        Date and time          : 1980-11-13 01:48:00.000
        Distance from center   : 212482.14996855386 km
        Distance from surface  : 212229.84996855384 km
        Velocity during fly by : 29146.758175624625 m/s
        Fly by Rhea
        Date and time          : 1980-11-13 06:24:00.000
        Distance from center   : 56106.853431387346 km
        Distance from surface  : 55342.353431387346 km
        Velocity during fly by : 25671.273724183182 m/s

        Spacecraft Voyager 2
        Launch 1977-08-20 14:29:00.000
        Fly by Callisto
        Date and time          : 1979-07-08 12:22:00.000
        Distance from center   : 215369.43346799788 km
        Distance from surface  : 212966.43346799788 km
        Velocity during fly by : 13170.332521434584 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:15:00.000
        Distance from center   : 61494.64533246093 km
        Distance from surface  : 58860.64533246093 km
        Velocity during fly by : 17368.422882795952 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:51:00.000
        Distance from center   : 205588.02500437704 km
        Distance from surface  : 204023.02500437704 km
        Velocity during fly by : 22728.048657803207 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29:00.000
        Distance from center   : 721772.732251584 km
        Distance from surface  : 650280.732251584 km
        Velocity during fly by : 25178.31865043251 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:18:00.000
        Distance from center   : 1129828.8697759267 km
        Distance from surface  : 1128007.5697759266 km
        Velocity during fly by : 25517.036728754978 m/s
        Fly by Iapetus
        Date and time          : 1981-08-23 01:28:00.000
        Distance from center   : 908885.7621339281 km
        Distance from surface  : 908151.2621339281 km
        Velocity during fly by : 16166.4160987855 m/s
        Fly by Titan
        Date and time          : 1981-08-25 09:37:00.000
        Distance from center   : 663950.0869914828 km
        Distance from surface  : 661374.5869914828 km
        Velocity during fly by : 18165.222366962113 m/s
        Fly by Dione
        Date and time          : 1981-08-26 01:04:00.000
        Distance from center   : 500651.12026638136 km
        Distance from surface  : 500088.62026638136 km
        Velocity during fly by : 27250.29385912496 m/s
        Fly by Mimas
        Date and time          : 1981-08-26 02:33:00.000
        Distance from center   : 308694.9188214407 km
        Distance from surface  : 308496.11882144073 km
        Velocity during fly by : 30876.979220693738 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23:00.000
        Distance from center   : 159076.39405465574 km
        Distance from surface  : 98808.39405465573 km
        Velocity during fly by : 32321.442788931014 m/s
        Fly by Enceladus
        Date and time          : 1981-08-26 03:41:00.000
        Distance from center   : 89661.89574043626 km
        Distance from surface  : 89409.59574043626 km
        Velocity during fly by : 32527.659190394013 m/s
        Fly by Tethys
        Date and time          : 1981-08-26 06:09:00.000
        Distance from center   : 94052.31994387746 km
        Distance from surface  : 93516.01994387746 km
        Velocity during fly by : 30083.44100582499 m/s
        Fly by Rhea
        Date and time          : 1981-08-26 06:30:00.000
        Distance from center   : 641511.5647013027 km
        Distance from surface  : 640747.0647013027 km
        Velocity during fly by : 29627.140273166657 m/s
        Fly by Miranda
        Date and time          : 1986-01-24 17:01:00.000
        Distance from center   : 31784.820798410947 km
        Distance from surface  : 31544.820798410947 km
        Velocity during fly by : 21374.990353596302 m/s
        Fly by Ariel
        Date and time          : 1986-01-24 16:19:00.000
        Distance from center   : 129855.5763293888 km
        Distance from surface  : 129274.5763293888 km
        Velocity during fly by : 20671.69291434674 m/s
        Fly by Umbriel
        Date and time          : 1986-01-24 20:51:00.000
        Distance from center   : 316187.8852252072 km
        Distance from surface  : 315602.8852252072 km
        Velocity during fly by : 21420.689593023184 m/s
        Fly by Titania
        Date and time          : 1986-01-24 15:08:00.000
        Distance from center   : 368410.4469901041 km
        Distance from surface  : 367621.4469901041 km
        Velocity during fly by : 19834.483029349612 m/s
        Fly by Oberon
        Date and time          : 1986-01-24 16:09:00.000
        Distance from center   : 473580.3123547944 km
        Distance from surface  : 472818.8123547944 km
        Velocity during fly by : 20525.745986467326 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:56:00.000
        Distance from center   : 101305.19387221131 km
        Distance from surface  : 75746.19387221131 km
        Velocity during fly by : 22196.735364994103 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 03:56:00.000
        Distance from center   : 29454.01096694516 km
        Distance from surface  : 4690.01096694516 km
        Velocity during fly by : 28076.772240349175 m/s
        Fly by Triton
        Date and time          : 1989-08-25 09:11:00.000
        Distance from center   : 39775.77957064574 km
        Distance from surface  : 38423.27957064574 km
        Velocity during fly by : 17769.693618130565 m/s

        Spacecraft New Horizons
        Launch 2006-01-19 19:00:00.000
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49:00.000
        Distance from center   : 2302923.9753426486 km
        Distance from surface  : 2231431.9753426486 km
        Velocity during fly by : 22850.53631918755 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:36:00.000
        Distance from center   : 11551.05513949029 km
        Distance from surface  : 10366.05513949029 km
        Velocity during fly by : 14531.900079914481 m/s
        Fly by Arrokoth
        Date and time          : 2019-01-01 05:35:00.000
        Distance from center   : 3561.40458165672 km
        Distance from surface  : 3544.90458165672 km
        Velocity during fly by : 14095.25081697473 m/s

        Spacecraft Rosetta
        Launch 2004-03-02 07:17:00.000
        Fly by Earth
        Date and time          : 2004-03-02 09:40:00.000
        Distance from center   : 8134.144535621027 km
        Distance from surface  : 1756.1445356210265 km
        Velocity during fly by : 27062.914992075162 m/s
        Fly by Mars
        Date and time          : 2007-02-25 01:46:00.000
        Distance from center   : 3652.3166570314606 km
        Distance from surface  : 256.3166570314607 km
        Velocity during fly by : 22834.691018268513 m/s
        Fly by 67P/Churyumov-Gerasimenko
        Date and time          : 2015-07-12 00:00:00.000
        Distance from center   : 239.6653709265202 km
        Distance from surface  : 237.6153709265202 km
        Velocity during fly by : 33230.38623321409 m/s
     */
}



