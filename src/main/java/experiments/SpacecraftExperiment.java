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
    private static final GregorianCalendar startDateVoyagerOne =
            new GregorianCalendar(1977, 8, 5, 12, 56, 0);

    // End of simulation for Voyager 1: January 1, 1981
    private static final GregorianCalendar endDateVoyagerOne =
            new GregorianCalendar(1981, 0, 1, 0, 0, 0);

    // Launch Voyager 2: August 20, 1977, 14:29:00 UTC
    private static final GregorianCalendar startDateVoyagerTwo =
            new GregorianCalendar(1977, 7, 20, 14, 29, 0);

    // End of simulation for Voyager 2: January 1, 1990
    private static final GregorianCalendar endDateVoyagerTwo =
            new GregorianCalendar(1990, 0, 1, 0, 0, 0);

    // Launch New Horizons: January 19, 2006, 19:00 UTC
    private static final GregorianCalendar startDateNewHorizons =
            new GregorianCalendar(2006, 0, 19, 19, 0, 0);

    // End of simulation for New Horizons: January 1, 2020
    private static final GregorianCalendar endDateNewHorizons =
            new GregorianCalendar(2020, 0, 1, 0, 0, 0);

    // Simulation start dates
    private static final Map<String,GregorianCalendar> startDates = new HashMap<>();

    // Simulation end dates
    private static final Map<String,GregorianCalendar> endDates = new HashMap<>();

    // Names of solar system particles passed by spacecraft
    private static final Map<String,List<String>> particleNames = new HashMap<>();

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
        particleNamesVoyagerOne.add("Jupiter");
        particleNamesVoyagerOne.add("Io");
        particleNamesVoyagerOne.add("Europa");
        particleNamesVoyagerOne.add("Ganymede");
        particleNamesVoyagerOne.add("Callisto");
        particleNamesVoyagerOne.add("Saturn");
        particleNames.put("Voyager 1",particleNamesVoyagerOne);

        // Define names of solar system particles passed by Voyager 2
        List<String> particleNamesVoyagerTwo = new ArrayList<>();
        particleNamesVoyagerTwo.add("Callisto");
        particleNamesVoyagerTwo.add("Ganymede");
        particleNamesVoyagerTwo.add("Europa");
        particleNamesVoyagerTwo.add("Jupiter");
        particleNamesVoyagerTwo.add("Io");
        particleNamesVoyagerTwo.add("Saturn");
        particleNamesVoyagerTwo.add("Uranus");
        particleNamesVoyagerTwo.add("Neptune");
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

        // Create solar system
        solarSystem = new SolarSystem();
        // solarSystem.setGeneralRelativityFlag(false);
        solarSystem.setGeneralRelativityFlag(true);
        try {
            solarSystem.initializeSimulation(simulationStartDate);
        } catch (SolarSystemException e) {
            e.printStackTrace();
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
        Distance from center   : 348307.1282230247 km
        Distance from surface  : 276815.1282230247 km
        Velocity during fly by : 36075.82646192992 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15
        Distance from center   : 20470.95189553155 km
        Distance from surface  : 18649.65189553155 km
        Velocity during fly by : 36671.62462268516 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:21
        Distance from center   : 729949.1295519047 km
        Distance from surface  : 728384.1295519047 km
        Velocity during fly by : 35297.22702832879 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:15
        Distance from center   : 124824.70799768873 km
        Distance from surface  : 122190.70799768873 km
        Velocity during fly by : 30928.75914439089 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:10
        Distance from center   : 123872.41174471944 km
        Distance from surface  : 121469.41174471944 km
        Velocity during fly by : 28159.30439135868 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44
        Distance from center   : 190510.55433988734 km
        Distance from surface  : 130242.55433988736 km
        Velocity during fly by : 30479.75306400557 m/s

        Spacecraft Voyager 2
        Launch 1977-08-20 14:29
        Fly by Callisto
        Date and time          : 1979-07-08 12:22
        Distance from center   : 215038.46512467758 km
        Distance from surface  : 212635.46512467758 km
        Velocity during fly by : 13170.33920655332 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:19
        Distance from center   : 53918.31383158057 km
        Distance from surface  : 51284.31383158057 km
        Velocity during fly by : 17393.83709771675 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:52
        Distance from center   : 207574.56604053953 km
        Distance from surface  : 206009.56604053953 km
        Velocity during fly by : 22737.808329276835 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29
        Distance from center   : 721772.1595340864 km
        Distance from surface  : 650280.1595340864 km
        Velocity during fly by : 25178.35532500212 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:17
        Distance from center   : 1129427.439040242 km
        Distance from surface  : 1127606.139040242 km
        Velocity during fly by : 25510.53641671859 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23
        Distance from center   : 159076.1736364184 km
        Distance from surface  : 98808.17363641837 km
        Velocity during fly by : 32321.501506614506 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:57
        Distance from center   : 102340.27640315512 km
        Distance from surface  : 76781.27640315512 km
        Velocity during fly by : 22167.254597603565 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 04:04
        Distance from center   : 35359.091737299736 km
        Distance from surface  : 10595.091737299734 km
        Velocity during fly by : 26412.818552796023 m/s

        Spacecraft New Horizons
        Launch 2006-01-19 19:00
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49
        Distance from center   : 2302925.176787414 km
        Distance from surface  : 2231433.176787414 km
        Velocity during fly by : 22850.51622573588 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:37
        Distance from center   : 14391.351081329158 km
        Distance from surface  : 13206.351081329158 km
        Velocity during fly by : 14531.81583917544 m/s
        Fly by Ultima Thule
        Date and time          : 2019-01-01 06:44
        Distance from center   : 26752.060911203884 km
        Distance from surface  : 26735.560911203884 km
        Velocity during fly by : 14090.862578985309 m/s
     */

    /*
        Results General Relativity (time step 60s):

        Spacecraft Voyager 1
        Launch 1977-09-05 12:56
        Fly by Jupiter
        Date and time          : 1979-03-05 12:06
        Distance from center   : 348307.1863098115 km
        Distance from surface  : 276815.1863098115 km
        Velocity during fly by : 36075.778975258574 m/s
        Fly by Io
        Date and time          : 1979-03-05 15:15
        Distance from center   : 20470.910128909018 km
        Distance from surface  : 18649.61012890902 km
        Velocity during fly by : 36671.6418332497 m/s
        Fly by Europa
        Date and time          : 1979-03-05 17:21
        Distance from center   : 729950.5107014545 km
        Distance from surface  : 728385.5107014545 km
        Velocity during fly by : 35297.25085787984 m/s
        Fly by Ganymede
        Date and time          : 1979-03-06 02:15
        Distance from center   : 124826.19395998894 km
        Distance from surface  : 122192.19395998894 km
        Velocity during fly by : 30928.769992395723 m/s
        Fly by Callisto
        Date and time          : 1979-03-06 17:10
        Distance from center   : 123872.60265196 km
        Distance from surface  : 121469.60265196 km
        Velocity during fly by : 28159.308183671157 m/s
        Fly by Saturn
        Date and time          : 1980-11-12 23:44
        Distance from center   : 190510.2917544303 km
        Distance from surface  : 130242.29175443029 km
        Velocity during fly by : 30479.753306657523 m/s

        Spacecraft Voyager 2
        Launch 1977-08-20 14:29
        Fly by Callisto
        Date and time          : 1979-07-08 12:22
        Distance from center   : 215040.13977529216 km
        Distance from surface  : 212637.13977529216 km
        Velocity during fly by : 13170.330407071437 m/s
        Fly by Ganymede
        Date and time          : 1979-07-09 07:19
        Distance from center   : 53917.154502801764 km
        Distance from surface  : 51283.154502801764 km
        Velocity during fly by : 17393.807335764337 m/s
        Fly by Europa
        Date and time          : 1979-07-09 17:52
        Distance from center   : 207571.7058909368 km
        Distance from surface  : 206006.7058909368 km
        Velocity during fly by : 22737.758755038318 m/s
        Fly by Jupiter
        Date and time          : 1979-07-09 22:29
        Distance from center   : 721772.5647262594 km
        Distance from surface  : 650280.5647262594 km
        Velocity during fly by : 25178.31468657416 m/s
        Fly by Io
        Date and time          : 1979-07-09 23:17
        Distance from center   : 1129426.9367341793 km
        Distance from surface  : 1127605.6367341792 km
        Velocity during fly by : 25510.49902560816 m/s
        Fly by Saturn
        Date and time          : 1981-08-26 03:23
        Distance from center   : 159075.3507253744 km
        Distance from surface  : 98807.35072537437 km
        Velocity during fly by : 32321.532924640243 m/s
        Fly by Uranus
        Date and time          : 1986-01-24 17:57
        Distance from center   : 102339.71894693564 km
        Distance from surface  : 76780.71894693564 km
        Velocity during fly by : 22167.267447055205 m/s
        Fly by Neptune
        Date and time          : 1989-08-25 04:04
        Distance from center   : 35359.22787670523 km
        Distance from surface  : 10595.22787670523 km
        Velocity during fly by : 26412.807821176943 m/s

        Spacecraft New Horizons
        Launch 2006-01-19 19:00
        Fly by Jupiter
        Date and time          : 2007-02-28 05:49
        Distance from center   : 2302923.906352514 km
        Distance from surface  : 2231431.906352514 km
        Velocity during fly by : 22850.536441472108 m/s
        Fly by Pluto
        Date and time          : 2015-07-14 11:37
        Distance from center   : 14391.277671636672 km
        Distance from surface  : 13206.277671636672 km
        Velocity during fly by : 14531.815881100609 m/s
        Fly by Ultima Thule
        Date and time          : 2019-01-01 06:44
        Distance from center   : 26751.980055981283 km
        Distance from surface  : 26735.480055981283 km
        Velocity during fly by : 14090.862582651182 m/s
     */
}



