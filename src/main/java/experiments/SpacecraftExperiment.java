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
 * Voyager 1, Voyager 2, and New Horizons.
 * @author Nico Kuijpers
 */
public class SpacecraftExperiment {

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
        startDates.put("Voyager 1", startDateVoyagerOne);
        startDates.put("Voyager 2", startDateVoyagerTwo);
        startDates.put("New Horizons", startDateNewHorizons);

        // Define end dates for simulation
        endDates.put("Voyager 1", endDateVoyagerOne);
        endDates.put("Voyager 2", endDateVoyagerTwo);
        endDates.put("New Horizons", endDateNewHorizons);

        // Define names of solar system particles passed by Voyager 1
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
        List<String> particleNamesVoyagerTwo = new ArrayList<>();
        particleNamesVoyagerTwo.add("Callisto");    // 1979-07-08 12:21     214,930 km
        particleNamesVoyagerTwo.add("Ganymede");    // 1979-07-09
        particleNamesVoyagerTwo.add("Europa");      // 1979-07-09
        particleNamesVoyagerTwo.add("Jupiter");     // 1979-07-09
        particleNamesVoyagerTwo.add("Io");          // 1979-07-09
        particleNamesVoyagerTwo.add("Iapetus");     // 1981-08-22 01:26:57 908,680 km
        particleNamesVoyagerTwo.add("Titan");       //
        particleNamesVoyagerTwo.add("Dione");
        particleNamesVoyagerTwo.add("Mimas");
        particleNamesVoyagerTwo.add("Saturn");
        particleNamesVoyagerTwo.add("Enceladus");
        particleNamesVoyagerTwo.add("Tethys");
        particleNamesVoyagerTwo.add("Rhea");
        particleNamesVoyagerTwo.add("Miranda");
        particleNamesVoyagerTwo.add("Ariel");
        particleNamesVoyagerTwo.add("Umbriel");
        particleNamesVoyagerTwo.add("Titania");
        particleNamesVoyagerTwo.add("Oberon");
        particleNamesVoyagerTwo.add("Uranus");
        particleNamesVoyagerTwo.add("Neptune");
        particleNamesVoyagerTwo.add("Triton");
        particleNames.put("Voyager 2",particleNamesVoyagerTwo);

        // Define names of solar system particles passed by New Horizons
        List<String> particleNamesNewHorizons = new ArrayList<>();
        particleNamesNewHorizons.add("Jupiter");
        particleNamesNewHorizons.add("Pluto");
        particleNamesNewHorizons.add("Ultima Thule");
        particleNames.put("New Horizons",particleNamesNewHorizons);
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
            solarSystem.createPlanetSystem("Jupiter");
            solarSystem.createPlanetSystem("Saturn");
            solarSystem.createPlanetSystem("Uranus");
            solarSystem.createPlanetSystem("Neptune");
        } catch (SolarSystemException ex) {
            System.err.println(ex.getMessage());
        }

        // Use Newton Mechancis for simulation
        //solarSystem.setGeneralRelativityFlag(false); // Newton Mechanics
        solarSystem.setGeneralRelativityFlag(true); // General Relativity

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
        experiment.simulateSpacecraftTrajectory("Voyager 1");
        experiment.simulateSpacecraftTrajectory("Voyager 2");
        experiment.simulateSpacecraftTrajectory("New Horizons");
    }

    /*
        Results Newton Mechanics (time step 60s):
        Spacecraft Voyager 1
        Launch 1977-09-05 12:56
        Fly by Jupiter
        Date and time          : 1979-03-05 12:06
        Distance from center   : 348307.29833757645 km
        Distance from surface  : 276815.29833757645 km
        Velocity during fly by : 36075.826829027494 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15
        Distance from center   : 20577.10763583587 km
        Distance from surface  : 18755.807635835867 km
        Velocity during fly by : 36671.61606701444 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:20
        Distance from center   : 732302.2923334201 km
        Distance from surface  : 730737.2923334201 km
        Velocity during fly by : 35308.78932760341 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:17
        Distance from center   : 113009.97021299892 km
        Distance from surface  : 110375.97021299892 km
        Velocity during fly by : 30918.427778591136 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:11
        Distance from center   : 124057.35089845926 km
        Distance from surface  : 121654.35089845926 km
        Velocity during fly by : 28157.439965016445 m/s
        Fly by Titan
        Date and time          : 1980-11-12 05:39
        Distance from center   : 2326.2232182844627 km
        Distance from surface  : -249.27678171553742 km
        Velocity during fly by : 21812.221303109396 m/s
        Fly by Tethys
        Date and time          : 1980-11-12 22:07
        Distance from center   : 413720.46292191633 km
        Distance from surface  : 413184.1629219163 km
        Velocity during fly by : 28781.991294083367 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44
        Distance from center   : 190511.10561351234 km
        Distance from surface  : 130243.10561351234 km
        Velocity during fly by : 30479.739971882078 m/s
        Fly by Mimas
        Date and time          : 1980-11-13 01:39
        Distance from center   : 94824.91563330565 km
        Distance from surface  : 94626.11563330564 km
        Velocity during fly by : 29311.26862600851 m/s
        Fly by Enceladus
        Date and time          : 1980-11-13 01:46
        Distance from center   : 218761.95062997832 km
        Distance from surface  : 218509.65062997834 km
        Velocity during fly by : 29183.364805438458 m/s
        Fly by Rhea
        Date and time          : 1980-11-13 06:24
        Distance from center   : 55102.49145407612 km
        Distance from surface  : 54337.99145407612 km
        Velocity during fly by : 25671.27651699476 m/s

        Spacecraft Voyager 2
        Launch 1977-08-20 14:29
        Fly by Callisto
        Date and time          : 1979-07-08 12:22
        Distance from center   : 215036.45551251178 km
        Distance from surface  : 212633.45551251178 km
        Velocity during fly by : 13170.341292101772 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:15
        Distance from center   : 61239.17917488123 km
        Distance from surface  : 58605.17917488123 km
        Velocity during fly by : 17368.452427899472 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:51
        Distance from center   : 205904.0517708431 km
        Distance from surface  : 204339.0517708431 km
        Velocity during fly by : 22728.098194898917 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29
        Distance from center   : 721772.3179243297 km
        Distance from surface  : 650280.3179243297 km
        Velocity during fly by : 25178.35937654697 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:17
        Distance from center   : 1129996.267570368 km
        Distance from surface  : 1128174.967570368 km
        Velocity during fly by : 25510.539716072068 m/s
        Fly by Iapetus
        Date and time          : 1981-08-23 01:27
        Distance from center   : 911169.7560318401 km
        Distance from surface  : 910435.2560318401 km
        Velocity during fly by : 16166.254303150803 m/s
        Fly by Titan
        Date and time          : 1981-08-25 09:33
        Distance from center   : 657171.8033418255 km
        Distance from surface  : 654596.3033418255 km
        Velocity during fly by : 18156.346530906016 m/s
        Fly by Dione
        Date and time          : 1981-08-26 00:53
        Distance from center   : 492710.51595065295 km
        Distance from surface  : 492148.01595065295 km
        Velocity during fly by : 26847.720797494345 m/s
        Fly by Mimas
        Date and time          : 1981-08-26 02:36
        Distance from center   : 314769.06924360635 km
        Distance from surface  : 314570.2692436063 km
        Velocity during fly by : 30990.88703870894 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23
        Distance from center   : 159077.20676037716 km
        Distance from surface  : 98809.20676037717 km
        Velocity during fly by : 32321.41204779268 m/s
        Fly by Enceladus
        Date and time          : 1981-08-26 04:03
        Distance from center   : 80840.93789472601 km
        Distance from surface  : 80588.63789472601 km
        Velocity during fly by : 32534.536754563742 m/s
        Fly by Tethys
        Date and time          : 1981-08-26 06:15
        Distance from center   : 95616.92047846456 km
        Distance from surface  : 95080.62047846457 km
        Velocity during fly by : 29950.38891005729 m/s
        Fly by Rhea
        Date and time          : 1981-08-26 06:19
        Distance from center   : 648715.284746537 km
        Distance from surface  : 647950.784746537 km
        Velocity during fly by : 29862.848798739997 m/s
        Fly by Miranda
        Date and time          : 1986-01-24 16:53
        Distance from center   : 46105.94286476669 km
        Distance from surface  : 45865.94286476669 km
        Velocity during fly by : 21208.19261126696 m/s
        Fly by Ariel
        Date and time          : 1986-01-24 16:18
        Distance from center   : 134112.85067306756 km
        Distance from surface  : 133531.85067306756 km
        Velocity during fly by : 20640.890871350424 m/s
        Fly by Umbriel
        Date and time          : 1986-01-24 20:50
        Distance from center   : 315830.9331528194 km
        Distance from surface  : 315245.9331528194 km
        Velocity during fly by : 21419.020309878288 m/s
        Fly by Titania
        Date and time          : 1986-01-24 15:08
        Distance from center   : 362341.40350632684 km
        Distance from surface  : 361552.40350632684 km
        Velocity during fly by : 19828.32398226172 m/s
        Fly by Oberon
        Date and time          : 1986-01-24 16:07
        Distance from center   : 471809.4889267265 km
        Distance from surface  : 471047.9889267265 km
        Velocity during fly by : 20484.19315502906 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:57
        Distance from center   : 102340.81621035282 km
        Distance from surface  : 76781.81621035282 km
        Velocity during fly by : 22167.19408428945 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 04:04
        Distance from center   : 35359.89670891972 km
        Distance from surface  : 10595.89670891972 km
        Velocity during fly by : 26412.87417638609 m/s
        Fly by Triton
        Date and time          : 1989-08-25 09:22
        Distance from center   : 102463.928808066 km
        Distance from surface  : 101111.428808066 km
        Velocity during fly by : 17320.204154684347 m/s

        Spacecraft New Horizons
        Launch 2006-01-19 19:00
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49
        Distance from center   : 2302925.2222991926 km
        Distance from surface  : 2231433.2222991926 km
        Velocity during fly by : 22850.51615014031 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:37
        Distance from center   : 14389.524213746561 km
        Distance from surface  : 13204.524213746561 km
        Velocity during fly by : 14531.816233252986 m/s
        Fly by Ultima Thule
        Date and time          : 2019-01-01 06:44
        Distance from center   : 26753.857512417093 km
        Distance from surface  : 26737.357512417093 km
        Velocity during fly by : 14090.862565593674 m/s

        Results General Relativity (timestep 60s)
        Spacecraft Voyager 1
        Launch 1977-09-05 12:56
        Fly by Jupiter
        Date and time          : 1979-03-05 12:06
        Distance from center   : 348307.35635866714 km
        Distance from surface  : 276815.35635866714 km
        Velocity during fly by : 36075.77934489113 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15
        Distance from center   : 20576.886687818445 km
        Distance from surface  : 18755.586687818446 km
        Velocity during fly by : 36671.63327960877 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:20
        Distance from center   : 732303.6774302138 km
        Distance from surface  : 730738.6774302138 km
        Velocity during fly by : 35308.813161975355 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:17
        Distance from center   : 113011.4072853455 km
        Distance from surface  : 110377.4072853455 km
        Velocity during fly by : 30918.438594161144 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:11
        Distance from center   : 124057.52858047427 km
        Distance from surface  : 121654.52858047427 km
        Velocity during fly by : 28157.443754120384 m/s
        Fly by Titan
        Date and time          : 1980-11-12 05:39
        Distance from center   : 2327.2196700586387 km
        Distance from surface  : -248.2803299413612 km
        Velocity during fly by : 21812.219703820618 m/s
        Fly by Tethys
        Date and time          : 1980-11-12 22:07
        Distance from center   : 413719.47411272954 km
        Distance from surface  : 413183.17411272955 km
        Velocity during fly by : 28781.971974511867 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44
        Distance from center   : 190510.84306433675 km
        Distance from surface  : 130242.84306433675 km
        Velocity during fly by : 30479.740214962876 m/s
        Fly by Mimas
        Date and time          : 1980-11-13 01:39
        Distance from center   : 94824.70937032535 km
        Distance from surface  : 94625.90937032535 km
        Velocity during fly by : 29311.282348877437 m/s
        Fly by Enceladus
        Date and time          : 1980-11-13 01:46
        Distance from center   : 218761.61568049644 km
        Distance from surface  : 218509.31568049642 km
        Velocity during fly by : 29183.378136136445 m/s
        Fly by Rhea
        Date and time          : 1980-11-13 06:24
        Distance from center   : 55103.189881395665 km
        Distance from surface  : 54338.689881395665 km
        Velocity during fly by : 25671.273658483995 m/s
        Spacecraft Voyager 2
        Launch 1977-08-20 14:29
        Fly by Callisto
        Date and time          : 1979-07-08 12:22
        Distance from center   : 215038.1408696183 km
        Distance from surface  : 212635.1408696183 km
        Velocity during fly by : 13170.332492335365 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:15
        Distance from center   : 61237.63319709697 km
        Distance from surface  : 58603.63319709697 km
        Velocity during fly by : 17368.422801993845 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:51
        Distance from center   : 205901.2774766956 km
        Distance from surface  : 204336.2774766956 km
        Velocity during fly by : 22728.048620505513 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29
        Distance from center   : 721772.723107829 km
        Distance from surface  : 650280.723107829 km
        Velocity during fly by : 25178.318737263267 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:17
        Distance from center   : 1129995.7976564807 km
        Distance from surface  : 1128174.4976564806 km
        Velocity during fly by : 25510.502324212783 m/s
        Fly by Iapetus
        Date and time          : 1981-08-23 01:27
        Distance from center   : 911170.3790229411 km
        Distance from surface  : 910435.8790229411 km
        Velocity during fly by : 16166.253943065156 m/s
        Fly by Titan
        Date and time          : 1981-08-25 09:33
        Distance from center   : 657170.4647468721 km
        Distance from surface  : 654594.9647468721 km
        Velocity during fly by : 18156.341514705753 m/s
        Fly by Dione
        Date and time          : 1981-08-26 00:53
        Distance from center   : 492708.6753445418 km
        Distance from surface  : 492146.1753445418 km
        Velocity during fly by : 26847.6750681866 m/s
        Fly by Mimas
        Date and time          : 1981-08-26 02:36
        Distance from center   : 314767.76656984887 km
        Distance from surface  : 314568.9665698488 km
        Velocity during fly by : 30990.87339801816 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23
        Distance from center   : 159076.38394984335 km
        Distance from surface  : 98808.38394984335 km
        Velocity during fly by : 32321.44346202247 m/s
        Fly by Enceladus
        Date and time          : 1981-08-26 04:03
        Distance from center   : 80841.59103966925 km
        Distance from surface  : 80589.29103966925 km
        Velocity during fly by : 32534.59788570704 m/s
        Fly by Tethys
        Date and time          : 1981-08-26 06:15
        Distance from center   : 95617.41071165392 km
        Distance from surface  : 95081.11071165392 km
        Velocity during fly by : 29950.43750662623 m/s
        Fly by Rhea
        Date and time          : 1981-08-26 06:19
        Distance from center   : 648714.1638186052 km
        Distance from surface  : 647949.6638186052 km
        Velocity during fly by : 29862.89636026401 m/s
        Fly by Miranda
        Date and time          : 1986-01-24 16:53
        Distance from center   : 46104.69333612742 km
        Distance from surface  : 45864.69333612742 km
        Velocity during fly by : 21208.18772036549 m/s
        Fly by Ariel
        Date and time          : 1986-01-24 16:18
        Distance from center   : 134112.59150528267 km
        Distance from surface  : 133531.59150528267 km
        Velocity during fly by : 20640.883533497017 m/s
        Fly by Umbriel
        Date and time          : 1986-01-24 20:50
        Distance from center   : 315830.11976956256 km
        Distance from surface  : 315245.11976956256 km
        Velocity during fly by : 21419.03481606717 m/s
        Fly by Titania
        Date and time          : 1986-01-24 15:08
        Distance from center   : 362341.92669225484 km
        Distance from surface  : 361552.92669225484 km
        Velocity during fly by : 19828.3178097398 m/s
        Fly by Oberon
        Date and time          : 1986-01-24 16:07
        Distance from center   : 471809.85377869924 km
        Distance from surface  : 471048.35377869924 km
        Velocity during fly by : 20484.185715742355 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:57
        Distance from center   : 102340.25964363466 km
        Distance from surface  : 76781.25964363466 km
        Velocity during fly by : 22167.206916732797 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 04:04
        Distance from center   : 35360.03271794748 km
        Distance from surface  : 10596.032717947475 km
        Velocity during fly by : 26412.863455180774 m/s
        Fly by Triton
        Date and time          : 1989-08-25 09:22
        Distance from center   : 102466.32662806501 km
        Distance from surface  : 101113.82662806501 km
        Velocity during fly by : 17320.20187613872 m/s
        Spacecraft New Horizons
        Launch 2006-01-19 19:00
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49
        Distance from center   : 2302923.951850609 km
        Distance from surface  : 2231431.951850609 km
        Velocity during fly by : 22850.53636598604 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:37
        Distance from center   : 14389.45076529282 km
        Distance from surface  : 13204.45076529282 km
        Velocity during fly by : 14531.81627518156 m/s
        Fly by Ultima Thule
        Date and time          : 2019-01-01 06:44
        Distance from center   : 26753.776318303084 km
        Distance from surface  : 26737.276318303084 km
        Velocity during fly by : 14090.862569256677 m/s
     */
}



