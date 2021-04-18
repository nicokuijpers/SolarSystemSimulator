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

    // Collect results for planets
    private final Map<String,List<Double>> resultsPlanets;

    // Collect results for moons
    private final Map<String,List<Double>> resultsMoons;

    /**
     * Constructor.
     * Set simulation start and end date. Create the Solar System.
     */
    public SolarSystemMoonsExperiment() {
        // Set ephemeris
        ephemeris = EphemerisSolarSystem.getInstance();

        // Start simulation at January 1, 1985
        // Note that January is month 0
        simulationStartDateTime = new GregorianCalendar(1985,0,1,0,0);

        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid
        // sudden changes in ephemeris due to changes from
        // winter time to summer time and vice versa
        simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Stop simulation at January 1, 1987
        simulationEndDateTime = new GregorianCalendar(1987,0,1,0,0 );
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Start collecting results at January 1, 1986
        startCollectingResultsDateTime = new GregorianCalendar(1986,0,1,0,0 );
        startCollectingResultsDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Define planets to gather results from
        planets = new ArrayList<>(Arrays.asList("Earth","Jupiter","Saturn","Uranus","Neptune"));

        // Define moons to gather results from
        moons = new HashMap<>();
        moons.put("Earth",
                new ArrayList<>(Arrays.asList("Moon")));
        moons.put("Jupiter",
                new ArrayList<>(Arrays.asList("Io","Europa","Ganymede","Callisto")));
        moons.put("Saturn",
                new ArrayList<>(Arrays.asList("Mimas","Enceladus","Tethys","Dione","Rhea","Titan","Iapetus")));
        moons.put("Uranus",
                new ArrayList<>(Arrays.asList("Miranda","Ariel","Umbriel","Titania","Oberon")));
        moons.put("Neptune",
                new ArrayList<>(Arrays.asList("Triton")));

        // Store results
        resultsPlanets = new HashMap<>();
        resultsMoons = new HashMap<>();
        for (String planetName : planets) {
            resultsPlanets.put(planetName,new ArrayList<>());
            for (String moonName : moons.get(planetName)) {
                resultsMoons.put(moonName,new ArrayList<>());
            }
        }

        // Create the Solar System
        solarSystem = new SolarSystem(simulationStartDateTime);
        for (String planetName : planets) {
            try {
                if (!"Earth".equals(planetName)) {
                    solarSystem.createPlanetSystem(planetName);
                }
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
     * Show simulation set-up.
     */
    public void showSimulationSetup() {
        System.out.println("Experiment date/time       : " +
                CalendarUtil.calendarToString(new GregorianCalendar()));
        System.out.println("Simulation start date/time : " +
                CalendarUtil.calendarToString(simulationStartDateTime));
        System.out.println("Simulation end date/time   : " +
                CalendarUtil.calendarToString(simulationEndDateTime));
        System.out.print("Simulation method          : ");
        if (solarSystem.getGeneralRelativityFlag()) {
            System.out.println("General Relativity");
        }
        else {
            System.out.println("Newton Mechanics");
        }
    }

    /**
     * Simulate the Solar System.
     */
    public void simulate() {
        while(solarSystem.getSimulationDateTime().before(simulationEndDateTime)) {
            // Advance one time step
            solarSystem.advanceSimulationForward(1);

            // Collect results for the last year
            if (solarSystem.getSimulationDateTime().after(startCollectingResultsDateTime))
            {
                for (String planetName : resultsPlanets.keySet()) {
                    double distancePlanet = computeDistancePlanet(planetName,solarSystem.getSimulationDateTime());
                    resultsPlanets.get(planetName).add(distancePlanet);
                }
                for (String moonName : resultsMoons.keySet()) {
                    double distanceMoon = computeDistanceMoon(moonName,solarSystem.getSimulationDateTime());
                    resultsMoons.get(moonName).add(distanceMoon);
                }
                startCollectingResultsDateTime.add(Calendar.HOUR,24);
            }
        }
    }

    /**
     * Show results of simulation.
     */
    public void showResults() {
        System.out.println("Deviation is averaged over final year of simulation");
        System.out.println("Deviation for planets is relative to the Sun");
        System.out.println("Deviation for moons is relative to their planet");
        for (String planetName : planets) {
            System.out.println("Planet: " + planetName);
            double deviationPlanet = computeAverage(resultsPlanets.get(planetName));
            System.out.println("Deviation " + 0.001 * deviationPlanet + " km for " + planetName);
            for (String moonName : moons.get(planetName)) {
                double deviationMoon = computeAverage(resultsMoons.get(moonName));
                System.out.println("Deviation " + 0.001 * deviationMoon + " km for " + moonName);
            }
        }
    }

    /**
     * Compute distance between actual position of planet and expected position for given date.
     * @param planetName Name of the planet
     * @param dateTime   Date to determine position and velocity of the planet
     * @return distance [m]
     */
    private double computeDistancePlanet(String planetName, GregorianCalendar dateTime) {
        dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Obtain position of planet from Ephemeris
        // Note that position and velocity from Ephemeris are relative to the Sun
        Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(planetName, dateTime);
        Vector3D positionExpected = positionAndVelocity[0];

        // Obtain position of planet from simulation results
        Vector3D positionActual = solarSystem.getParticle(planetName).getPosition();

        // Compute distance
        return positionExpected.euclideanDistance(positionActual);
    }

    /**
     * Compute distance between actual position of moon and expected position for given date.
     * @param moonName    Name of the body
     * @param dateTime    Date to determine position and velocity of this body
     * @return distance [m]
     */
    private double computeDistanceMoon(String moonName, GregorianCalendar dateTime) {
        if ("Moon".equals(moonName)) {
            // Obtain position of Earths's Moon from Ephemeris
            // Note that position of Earth's Moon is relative to the Sun
            Vector3D positionEarthEphemeris = ephemeris.getBodyPosition("Earth", dateTime);
            Vector3D positionMoonEphemeris = ephemeris.getBodyPosition("Moon", dateTime);
            Vector3D positionMoonRelativeToEarthEphemeris = positionMoonEphemeris.minus(positionEarthEphemeris);

            // Obtain position of the Moon relative to the Earth from simulation results
            Vector3D positionEarthSimulation = solarSystem.getParticle("Earth").getPosition();
            Vector3D positionMoonSimulation = solarSystem.getParticle("Moon").getPosition();
            Vector3D positionMoonRelativeToEarthSimulation = positionMoonSimulation.minus(positionEarthSimulation);

            // Deviation relative to the position of the Earth
            Vector3D deviationMoonRelativeToEarth = positionMoonRelativeToEarthSimulation.minus(positionMoonRelativeToEarthEphemeris);
            return deviationMoonRelativeToEarth.magnitude();

            // USE THE CODE IN NEXT LINES FOR POSITION RELATIVE TO THE SUN
            // Vector3D deviationMoonRelativeToSun = positionMoonSimulation.minus(positionMoonEphemeris);
            // return deviationMoonRelativeToSun.magnitude();
        } else {
            // Obtain position and velocity of moon from Ephemeris
            // Note that positions and velocities of moons are relative to their planet
            Vector3D positionExpected = ephemeris.getBodyPosition(moonName, dateTime);

            // Obtain position of moon relative to planet from simulation results
            String planetName = SolarSystemParameters.getInstance().getPlanetOfMoon(moonName);
            Vector3D positionPlanet = solarSystem.getParticle(planetName).getPosition();
            Vector3D positionMoon = solarSystem.getParticle(moonName).getPosition();
            Vector3D positionActual = positionMoon.minus(positionPlanet);
            // USE THE CODE IN NEXT LINE FOR POSITION RELATIVE TO THE SUN
            // Vector3D positionActual = positionMoon;
            return positionExpected.euclideanDistance(positionActual);
        }
    }

    /**
     * Compute average of list of values.
     * @param values list of values
     * @return average
     */
    private double computeAverage(List<Double> values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    /**
     * Main method.
     * Simulate and compute deviation for each moon.
     * @param args input arguments (not used)
     */
    public static void main (String[] args) throws SolarSystemException {

        // Experiment set-up
        SolarSystemMoonsExperiment experiment = new SolarSystemMoonsExperiment();

        // Run simulation and show results
        experiment.showSimulationSetup();
        experiment.simulate();
        experiment.showResults();
    }

    /*
        Results with oblateness
        Maximum distance to use oblateness is 5 mln km
        OblatePlanet.java: MAXDISTANCEOBLATENESS = 5.0E09;
        Experiment date/time       : 2020-05-03 16:15
        Simulation start date/time : 1985-01-01 00:00
        Simulation end date/time   : 1987-01-01 00:00
        Simulation method          : Newton Mechanics
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 89.08249017983042 km for Earth
        Deviation 4.705380180960665 km for Moon
        Planet: Jupiter
        Deviation 1.871967673371792 km for Jupiter
        Deviation 265.76842807750086 km for Io
        Deviation 273.08982320577377 km for Europa
        Deviation 356.48075043692575 km for Ganymede
        Deviation 240.1640021900852 km for Callisto
        Planet: Saturn
        Deviation 0.48862889427016387 km for Saturn
        Deviation 13855.805815225276 km for Mimas
        Deviation 4131.497031642155 km for Enceladus
        Deviation 8363.314658753736 km for Tethys
        Deviation 4909.333237346177 km for Dione
        Deviation 7751.6761487600925 km for Rhea
        Deviation 3667.2891555251263 km for Titan
        Deviation 1816.8311939720759 km for Iapetus
        Planet: Uranus
        Deviation 0.36148558850427487 km for Uranus
        Deviation 1053.0318517062276 km for Miranda
        Deviation 8735.233510175678 km for Ariel
        Deviation 7382.76174271898 km for Umbriel
        Deviation 3250.42097592909 km for Titania
        Deviation 2351.215580919366 km for Oberon
        Planet: Neptune
        Deviation 0.347131555939914 km for Neptune
        Deviation 4.8536534414634565 km for Triton

        Results without oblateness
        Maximum distance to use oblateness is 0 km
        OblatePlanet.java: MAXDISTANCEOBLATENESS = 0.0;
        Experiment date/time       : 2020-05-03 16:20
        Simulation start date/time : 1985-01-01 00:00
        Simulation end date/time   : 1987-01-01 00:00
        Simulation method          : Newton Mechanics
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 88.7064439648119 km for Earth
        Deviation 27.25963321146101 km for Moon
        Planet: Jupiter
        Deviation 1.8719669678027562 km for Jupiter
        Deviation 587936.4087458943 km for Io
        Deviation 176880.9836747185 km for Europa
        Deviation 117157.81575039409 km for Ganymede
        Deviation 137553.51882510757 km for Callisto
        Planet: Saturn
        Deviation 0.48862756875207963 km for Saturn
        Deviation 236349.83920032796 km for Mimas
        Deviation 241497.49249184923 km for Enceladus
        Deviation 559918.636483489 km for Tethys
        Deviation 338262.33999816794 km for Dione
        Deviation 50130.33125615758 km for Rhea
        Deviation 106383.09336127694 km for Titan
        Deviation 73241.44908880022 km for Iapetus
        Planet: Uranus
        Deviation 0.3614822861675324 km for Uranus
        Deviation 63131.2540673322 km for Miranda
        Deviation 4163.449003004397 km for Ariel
        Deviation 31486.83582275252 km for Umbriel
        Deviation 32520.782443409826 km for Titania
        Deviation 30120.918075848367 km for Oberon
        Planet: Neptune
        Deviation 0.34713047657416507 km for Neptune
        Deviation 77912.18343647943 km for Triton


        Results with oblateness and General Relativity
        Maximum distance to use oblateness is 5 mln km
        OblatePlanet.java: MAXDISTANCEOBLATENESS = 5.0E09;
        Experiment date/time       : 2021-04-18 13:02:37.079
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : General Relativity
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 0.5518453749161654 km for Earth
        Deviation 1.221842442959866 km for Moon
        Planet: Jupiter
        Deviation 0.2430788401062364 km for Jupiter
        Deviation 309.005633936853 km for Io
        Deviation 297.4810856724613 km for Europa
        Deviation 371.1310911965169 km for Ganymede
        Deviation 229.24836607232973 km for Callisto
        Planet: Saturn
        Deviation 0.3134320893571472 km for Saturn
        Deviation 13840.18151284414 km for Mimas
        Deviation 4119.407747906133 km for Enceladus
        Deviation 8352.464395749956 km for Tethys
        Deviation 4898.468550900315 km for Dione
        Deviation 7745.058200850103 km for Rhea
        Deviation 3663.3633011781103 km for Titan
        Deviation 1814.8932791128839 km for Iapetus
        Planet: Uranus
        Deviation 0.33237059134968244 km for Uranus
        Deviation 1056.2239836757165 km for Miranda
        Deviation 8737.780297517123 km for Ariel
        Deviation 7380.670088006834 km for Umbriel
        Deviation 3249.0795142310653 km for Titania
        Deviation 2350.093819756013 km for Oberon
        Planet: Neptune
        Deviation 0.3383170169709396 km for Neptune
        Deviation 29.070493572091056 km for Triton

        Results without oblateness and General Relativity
        Maximum distance to use oblateness is 0 km
        OblatePlanet.java: MAXDISTANCEOBLATENESS = 0.0;
        Experiment date/time       : 2021-04-18 13:06:15.518
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : General Relativity
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 0.4151881299399081 km for Earth
        Deviation 33.04995040278566 km for Moon
        Planet: Jupiter
        Deviation 0.24307691294765513 km for Jupiter
        Deviation 587966.2117082529 km for Io
        Deviation 176905.3857668799 km for Europa
        Deviation 117143.23235288069 km for Ganymede
        Deviation 137542.5519294342 km for Callisto
        Planet: Saturn
        Deviation 0.31343267680216863 km for Saturn
        Deviation 236350.71315463402 km for Mimas
        Deviation 241499.79824216093 km for Enceladus
        Deviation 559919.9062577988 km for Tethys
        Deviation 338271.9469837319 km for Dione
        Deviation 50136.76702493959 km for Rhea
        Deviation 106379.19995061151 km for Titan
        Deviation 73239.50957632838 km for Iapetus
        Planet: Uranus
        Deviation 0.3323705858957742 km for Uranus
        Deviation 63134.24892799694 km for Miranda
        Deviation 4165.978809212782 km for Ariel
        Deviation 31484.829595816635 km for Umbriel
        Deviation 32519.459362859976 km for Titania
        Deviation 30119.80001547284 km for Oberon
        Planet: Neptune
        Deviation 0.33831357170568976 km for Neptune
        Deviation 77945.64354080617 km for Triton
    */
}

