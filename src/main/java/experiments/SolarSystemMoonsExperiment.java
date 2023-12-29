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
 * In this experiment the distances between simulated positions and ephemeris of the
 * moons of the Solar System are averaged over 1 year, starting after 1 year simulating.
 * This experiment is used to show the effect taking into account the oblateness of the
 * planet and the size of the simulation time step when simulating planet systems.
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
        planets = new ArrayList<>(Arrays.asList("Earth","Mars","Jupiter","Saturn","Uranus","Neptune","Pluto System"));

        // Define moons to gather results from
        moons = new HashMap<>();
        for (String planetName : planets) {
            moons.put(planetName, SolarSystemParameters.getInstance().getMoonsOfPlanet(planetName));
        }

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
        //solarSystem.setGeneralRelativityFlag(true);
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
        Results with oblateness and Newton Mechanics
        Maximum distance to use oblateness is 5 mln km
        Maximum time step for planet systems is 10 min
        Experiment date/time       : 2023-12-29 14:08:06.450
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : Newton Mechanics
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 89.08229483932105 km for Earth
        Deviation 4.7270877860899345 km for Moon
        Planet: Mars
        Deviation 72.0351537063679 km for Mars
        Deviation 2598.4068206722764 km for Deimos
        Deviation 11846.929912979904 km for Phobos
        Planet: Jupiter
        Deviation 1.8719139896307386 km for Jupiter
        Deviation 65.62993563566728 km for Io
        Deviation 126.01663005519463 km for Europa
        Deviation 77.34259588207485 km for Ganymede
        Deviation 47.8318449526358 km for Callisto
        Planet: Saturn
        Deviation 0.4887024223172703 km for Saturn
        Deviation 42284.45272452298 km for Phoebe
        Deviation 116.57532788062106 km for Tethys
        Deviation 38.80083436181288 km for Iapetus
        Deviation 11603.774424342204 km for Mimas
        Deviation 25.0631497589116 km for Dione
        Deviation 6.703356305201839 km for Rhea
        Deviation 1050.104386585202 km for Enceladus
        Deviation 18.236668042110303 km for Hyperion
        Deviation 20.830837576542162 km for Titan
        Planet: Uranus
        Deviation 0.3611820476735706 km for Uranus
        Deviation 472.1980473469021 km for Miranda
        Deviation 3.769184313684764 km for Umbriel
        Deviation 0.607306168516172 km for Titania
        Deviation 28.1293222672932 km for Ariel
        Deviation 0.3257093592735295 km for Oberon
        Planet: Neptune
        Deviation 0.3468095079868541 km for Neptune
        Deviation 2477.7059571663713 km for Proteus
        Deviation 82.81631602189485 km for Triton
        Deviation 5891.151913520777 km for Nereid
        Planet: Pluto System
        Deviation 0.3431844439551283 km for Pluto System
        Deviation 4.413512251343903 km for Styx
        Deviation 1.245440275735044 km for Pluto
        Deviation 3.648884699751092 km for Kerberos
        Deviation 10.20434318375054 km for Charon
        Deviation 6.277701516840435 km for Hydra
        Deviation 1.926371588742682 km for Nix

        Results with oblateness and Newton Mechanics
        Maximum distance to use oblateness is 5 mln km
        Maximum time step for planet systems is 5 min
        Experiment date/time       : 2023-12-29 14:11:17.478
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : Newton Mechanics
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 89.08229483932105 km for Earth
        Deviation 4.7270877860899345 km for Moon
        Planet: Mars
        Deviation 72.0351537063679 km for Mars
        Deviation 2779.50757770975 km for Deimos
        Deviation 15184.972160335885 km for Phobos
        Planet: Jupiter
        Deviation 1.8719139896307386 km for Jupiter
        Deviation 262.69956830249254 km for Io
        Deviation 84.11791076905935 km for Europa
        Deviation 80.56249829653441 km for Ganymede
        Deviation 47.83374596441141 km for Callisto
        Planet: Saturn
        Deviation 0.4887024223172703 km for Saturn
        Deviation 42284.452592747955 km for Phoebe
        Deviation 23.60772490215371 km for Tethys
        Deviation 38.801618373948315 km for Iapetus
        Deviation 334.5170899834814 km for Mimas
        Deviation 10.385201811099233 km for Dione
        Deviation 7.227576440166821 km for Rhea
        Deviation 19.97538854215489 km for Enceladus
        Deviation 18.2445559352226 km for Hyperion
        Deviation 20.84023516982636 km for Titan
        Planet: Uranus
        Deviation 0.3611820476735706 km for Uranus
        Deviation 15.704025454400657 km for Miranda
        Deviation 4.290173282887127 km for Umbriel
        Deviation 0.5701078018164201 km for Titania
        Deviation 15.988363308757137 km for Ariel
        Deviation 0.32164615742857755 km for Oberon
        Planet: Neptune
        Deviation 0.3468095079868541 km for Neptune
        Deviation 573.3297819937189 km for Proteus
        Deviation 82.83552087201267 km for Triton
        Deviation 5891.604384919176 km for Nereid
        Planet: Pluto System
        Deviation 0.3431844439551283 km for Pluto System
        Deviation 4.388947131690343 km for Styx
        Deviation 1.2425744382900772 km for Pluto
        Deviation 3.642656662867187 km for Kerberos
        Deviation 10.180861528811487 km for Charon
        Deviation 6.282184824082639 km for Hydra
        Deviation 1.9374896661673875 km for Nix

        Results with oblateness and Newton Mechanics
        Maximum distance to use oblateness is 5 mln km
        Maximum time step for planet systems is 1 min
        Experiment date/time       : 2023-12-29 14:15:37.543
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : Newton Mechanics
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 89.08229483932105 km for Earth
        Deviation 4.7270877860899345 km for Moon
        Planet: Mars
        Deviation 72.0351537063679 km for Mars
        Deviation 2785.3988757094417 km for Deimos
        Deviation 7720.6776594015555 km for Phobos
        Planet: Jupiter
        Deviation 1.8719139896307386 km for Jupiter
        Deviation 272.405080260819 km for Io
        Deviation 82.74584949837518 km for Europa
        Deviation 80.71872253940437 km for Ganymede
        Deviation 47.83493183991604 km for Callisto
        Planet: Saturn
        Deviation 0.4887024223172703 km for Saturn
        Deviation 42284.454060185504 km for Phoebe
        Deviation 28.087891537003216 km for Tethys
        Deviation 38.80229143486669 km for Iapetus
        Deviation 31.449758363901285 km for Mimas
        Deviation 9.946845080428822 km for Dione
        Deviation 7.435968659281278 km for Rhea
        Deviation 16.320001311589376 km for Enceladus
        Deviation 18.250086775468375 km for Hyperion
        Deviation 20.84308652462117 km for Titan
        Planet: Uranus
        Deviation 0.3611820476735706 km for Uranus
        Deviation 1.7863737586629418 km for Miranda
        Deviation 4.258516217871377 km for Umbriel
        Deviation 0.5445822663717858 km for Titania
        Deviation 15.508729553046654 km for Ariel
        Deviation 0.3192065813192741 km for Oberon
        Planet: Neptune
        Deviation 0.3468095079868541 km for Neptune
        Deviation 531.222481605872 km for Proteus
        Deviation 82.91888460514444 km for Triton
        Deviation 5891.5580167925345 km for Nereid
        Planet: Pluto System
        Deviation 0.3431844439551283 km for Pluto System
        Deviation 4.434203094868664 km for Styx
        Deviation 1.2259018101841328 km for Pluto
        Deviation 3.625660187579085 km for Kerberos
        Deviation 10.044250367412097 km for Charon
        Deviation 6.284546779400564 km for Hydra
        Deviation 1.9214455397682761 km for Nix

        Results with oblateness and General Relativity
        Maximum distance to use oblateness is 5 mln km
        Maximum time step for planet systems is 5 min
        Experiment date/time       : 2023-12-29 14:31:42.208
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : General Relativity
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 0.5516611152577177 km for Earth
        Deviation 1.2008870178046707 km for Moon
        Planet: Mars
        Deviation 1.0769538564983758 km for Mars
        Deviation 2785.60607839701 km for Deimos
        Deviation 15185.181212670048 km for Phobos
        Planet: Jupiter
        Deviation 0.24302261882205356 km for Jupiter
        Deviation 306.1997813514557 km for Io
        Deviation 60.580873668100814 km for Europa
        Deviation 65.91163842033653 km for Ganymede
        Deviation 45.08890633825107 km for Callisto
        Planet: Saturn
        Deviation 0.3134173228579934 km for Saturn
        Deviation 42283.25368179797 km for Phoebe
        Deviation 34.25846082237559 km for Tethys
        Deviation 39.56386513735534 km for Iapetus
        Deviation 318.92822760353334 km for Mimas
        Deviation 4.168313508690686 km for Dione
        Deviation 13.729313796244963 km for Rhea
        Deviation 9.600676574459039 km for Enceladus
        Deviation 21.754448746851644 km for Hyperion
        Deviation 24.751803146438423 km for Titan
        Planet: Uranus
        Deviation 0.3320570579226854 km for Uranus
        Deviation 12.50641932036392 km for Miranda
        Deviation 6.406405353659458 km for Umbriel
        Deviation 0.7646755400851883 km for Titania
        Deviation 13.444471683941412 km for Ariel
        Deviation 0.8047665507648197 km for Oberon
        Planet: Neptune
        Deviation 0.33800286014792597 km for Neptune
        Deviation 571.5718544711868 km for Proteus
        Deviation 81.59816021052076 km for Triton
        Deviation 5891.553726746774 km for Nereid
        Planet: Pluto System
        Deviation 0.33943624521702204 km for Pluto System
        Deviation 4.440012882957863 km for Styx
        Deviation 1.2422839223922237 km for Pluto
        Deviation 3.6536285097163077 km for Kerberos
        Deviation 10.178481002661645 km for Charon
        Deviation 6.278648274093566 km for Hydra
        Deviation 1.9497893781354296 km for Nix

        Results without oblateness and Newton Mechanics
        Maximum distance to use oblateness is 0 km
        OblatePlanet.java: MAXDISTANCEOBLATENESS = 0.0;
        Maximum time step for planet systems is 5 min
        Experiment date/time       : 2023-12-29 14:39:19.920
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : Newton Mechanics
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 88.70645203939493 km for Earth
        Deviation 27.25930546877883 km for Moon
        Planet: Mars
        Deviation 72.0351931237109 km for Mars
        Deviation 10693.172322611006 km for Deimos
        Deviation 8577.714458550481 km for Phobos
        Planet: Jupiter
        Deviation 1.871911997315279 km for Jupiter
        Deviation 587896.2426260384 km for Io
        Deviation 176572.3348149023 km for Europa
        Deviation 117570.35072461181 km for Ganymede
        Deviation 137344.17667988906 km for Callisto
        Planet: Saturn
        Deviation 0.48870200225093985 km for Saturn
        Deviation 42290.014702481676 km for Phoebe
        Deviation 560813.2926259649 km for Tethys
        Deviation 71398.71949851474 km for Iapetus
        Deviation 236975.4573291552 km for Mimas
        Deviation 342556.4218858387 km for Dione
        Deviation 57868.87723611648 km for Rhea
        Deviation 242341.67627594923 km for Enceladus
        Deviation 90230.69829317875 km for Hyperion
        Deviation 102698.90984005842 km for Titan
        Planet: Uranus
        Deviation 0.3611817485658427 km for Uranus
        Deviation 62119.7260906061 km for Miranda
        Deviation 24109.974348540414 km for Umbriel
        Deviation 29275.645464418245 km for Titania
        Deviation 4589.906421722444 km for Ariel
        Deviation 27773.957690411524 km for Oberon
        Planet: Neptune
        Deviation 0.3468096631068536 km for Neptune
        Deviation 17750.478407162278 km for Proteus
        Deviation 78019.96021939468 km for Triton
        Deviation 7441.36214935742 km for Nereid
        Planet: Pluto System
        Deviation 0.34318463923486664 km for Pluto System
        Deviation 4.387787400733474 km for Styx
        Deviation 1.2411873851370534 km for Pluto
        Deviation 3.6424145078620525 km for Kerberos
        Deviation 10.169496291748104 km for Charon
        Deviation 6.2693297914902075 km for Hydra
        Deviation 1.9186358643905557 km for Nix

        Results without oblateness and General Relativity
        Maximum distance to use oblateness is 0 km
        OblatePlanet.java: MAXDISTANCEOBLATENESS = 0.0;
        Maximum time step for planet systems is 5 min
        Experiment date/time       : 2023-12-29 14:42:56.695
        Simulation start date/time : 1985-01-01 00:00:00.000
        Simulation end date/time   : 1987-01-01 00:00:00.000
        Simulation method          : General Relativity
        Deviation is averaged over final year of simulation
        Deviation for planets is relative to the Sun
        Deviation for moons is relative to their planet
        Planet: Earth
        Deviation 0.41518702889102777 km for Earth
        Deviation 33.049934737436274 km for Moon
        Planet: Mars
        Deviation 1.0769220958925214 km for Mars
        Deviation 10699.046520977165 km for Deimos
        Deviation 8569.453405746488 km for Phobos
        Planet: Jupiter
        Deviation 0.24302231181149342 km for Jupiter
        Deviation 587926.0594078002 km for Io
        Deviation 176596.78817406748 km for Europa
        Deviation 117555.75040827222 km for Ganymede
        Deviation 137333.2099181893 km for Callisto
        Planet: Saturn
        Deviation 0.31341746075640653 km for Saturn
        Deviation 42288.8167933345 km for Phoebe
        Deviation 560814.4121257648 km for Tethys
        Deviation 71396.784461489 km for Iapetus
        Deviation 236976.1959855146 km for Mimas
        Deviation 342565.9671079921 km for Dione
        Deviation 57875.39104022668 km for Rhea
        Deviation 242343.9230171335 km for Enceladus
        Deviation 90227.19974714228 km for Hyperion
        Deviation 102695.01252754721 km for Titan
        Planet: Uranus
        Deviation 0.3320554980939921 km for Uranus
        Deviation 62122.98976467084 km for Miranda
        Deviation 24107.896663702846 km for Umbriel
        Deviation 29274.286372551207 km for Titania
        Deviation 4587.259776867738 km for Ariel
        Deviation 27772.832841485975 km for Oberon
        Planet: Neptune
        Deviation 0.33799927738728436 km for Neptune
        Deviation 17753.99211169334 km for Proteus
        Deviation 78018.6003157257 km for Triton
        Deviation 7441.298256625704 km for Nereid
        Planet: Pluto System
        Deviation 0.3394355559963401 km for Pluto System
        Deviation 4.446613605282195 km for Styx
        Deviation 1.2392006834507767 km for Pluto
        Deviation 3.633434218360619 km for Kerberos
        Deviation 10.153217874502168 km for Charon
        Deviation 6.282407865980751 km for Hydra
        Deviation 1.9401707600716336 km for Nix
    */
}

