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

import ephemeris.EphemerisAccurate;
import ephemeris.EphemerisAccurateBSP;
import ephemeris.IEphemeris;
import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import solarsystem.OblatePlanet;
import util.Vector3D;

import java.util.*;

/**
 * Experiment to determine accuracy of simulation results for the following bodies of
 * the Solar System: Mercury, Venus, Earth, Moon, Mars, Jupiter, Saturn, Uranus, and
 * Pluto System.
 * Simulation results using Newton Mechanics, General Relativity (PPN method) and
 * Curvature of Wave Propagation Method (CWPM) are compared to ephemeris data from
 * DE405 over a period of 600 years. Simulation time step is 1 hour.
 * 
 * @author Nico Kuijpers
 */
public class SimulationAccuracyExperiment {
    
    // Ephemeris
    private final IEphemeris ephemeris;

    // Particle system
    private ParticleSystem particleSystem;
    
    // Names of bodies to be simulated (except Sun)
    private final List<String> bodyNames;
    
    // Simulation date/time
    private GregorianCalendar simulationDateTime;
    
    // Simulation time step [seconds]
    private final long deltaT;
    
    // Store deviations in position to compute average
    private Map<String,List<Double>> deviations;
    
    /**
     * Constructor.
     * Set ephemeris, names of Solar System bodies, and simulation time step.
     */
    public SimulationAccuracyExperiment() {
        // Set ephemeris
        ephemeris = EphemerisAccurateBSP.getInstance();
        
        // Define the bodies of the Solar System to be simulated (except Sun)
        bodyNames = new ArrayList<>();
        bodyNames.add("Mercury");
        bodyNames.add("Venus");
        bodyNames.add("Earth");
        bodyNames.add("Moon");
        bodyNames.add("Mars");
        bodyNames.add("Jupiter");
        bodyNames.add("Saturn");
        bodyNames.add("Uranus");
        bodyNames.add("Neptune");
        bodyNames.add("Pluto System");

        // Set simulation time step to 1 hour
        deltaT = (long) (60 * 60);
    }

    /**
     * Initialize simulation.
     * Simulation will start January 1st, 1620.
     */
    private void initSimulation() {
        // Start simulation at January 1st, 1620
        // Note that January is month 0
        simulationDateTime = new GregorianCalendar(1600,0,1);
        
        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid 
        // sudden changes in ephemeris due to changes from 
        // winter time to summer time and vice versa
        simulationDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Store deviations in position to compute average
        deviations = new HashMap<>();
        for (String name : bodyNames) {
            deviations.put(name, new ArrayList<>());
        }
        
        // Create the Solar System
        particleSystem = new ParticleSystem();
        
        // Create the Sun 
        Vector3D positionSun = new Vector3D(); // Origin
        Vector3D velocitySun = new Vector3D(); // Zero velocity
        double massSun = SolarSystemParameters.getInstance().getMass("Sun");
        double muSun   = SolarSystemParameters.getInstance().getMu("Sun");
        Particle sun = new Particle(massSun, muSun, positionSun, velocitySun);
        particleSystem.addParticle("Sun", sun);
        
        // Create the remaining bodies of the Solar System
        for (String name : bodyNames) {
            createBody(name,simulationDateTime);
        } 
    }

    
    /**
     * Run simulation for given number of years.
     * Simulation time step is 1 hour. Position of Solar System bodies is
     * checked against ephemeris data each day.
     * @param nrYears number of years
     */
    private void runSimulation(int nrYears) {
        // End date/time of the simulation
        GregorianCalendar simulationEndDateTime = (GregorianCalendar) simulationDateTime.clone();
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        simulationEndDateTime.add(Calendar.YEAR, nrYears);
        
        // Check initial position and velocity
        int hour = 0;
        int day = 0;
        checkPosition(simulationDateTime,day);
        
        // Run simulation
        while (simulationDateTime.before(simulationEndDateTime)) {
            // Advance 1 hour
            if (particleSystem.getGeneralRelativityFlag()) {
                particleSystem.advanceRungeKutta(deltaT);
            }
            else {
                particleSystem.advanceABM4(deltaT/2);
                particleSystem.advanceABM4(deltaT/2);
            }
            
            // Correction for position of Sun
            Particle sun = particleSystem.getParticle("Sun");
            particleSystem.correctDrift(sun.getPosition(),sun.getVelocity());
            
            // Update simulation date/time
            simulationDateTime.add(Calendar.SECOND, (int) deltaT);

            // Check position of Solar System bodies each day
            hour++;
            if (hour == 24) {
                hour = 0;
                day++;
                checkPosition(simulationDateTime,day);
            }
        }
    }

    /**
     * Run simulation using Newton Mechanics for given number of years.
     * @param nrYears number of years
     */
    public void simulateNewtonMechanics(int nrYears) {
        // Initialize simulation
        initSimulation();
        
        // Do not apply General Relativity when computing acceleration
        particleSystem.setGeneralRelativityFlag(false);
        
        // Show message on screen
        System.out.println("Running simulation using Newton Mechanics for " + nrYears + " years");
        
        // Run simulation
        runSimulation(nrYears);
    }

    /**
     * Run simulation using General Relativity for given number of years.
     * @param nrYears number of years
     */
    public void simulateGeneralRelativity(int nrYears) {
        // Initialize simulation
        initSimulation();
        
        // Apply General Relativity when computing acceleration
        particleSystem.setGeneralRelativityFlag(true);

        // Do not apply Curvature of Wave Propagation Method when computing acceleration
        particleSystem.setCurvatureWavePropagationFlag(false);
        
        // Show message on screen
        System.out.println("Running simulation using General Relativity for " + nrYears + " years");
        
        // Run simulation
        runSimulation(nrYears);
    }

    /**
     * Run simulation using Curvature of Wave Propagation Method (CWPM) for given number of years.
     * @param nrYears number of years
     */
    public void simulateCurvatureWavePropagation(int nrYears) {
        // Initialize simulation
        initSimulation();

        // Apply General Relativity when computing acceleration
        particleSystem.setGeneralRelativityFlag(true);

        // Apply Curvature of Wave Propagation Method when computing acceleration
        particleSystem.setCurvatureWavePropagationFlag(true);

        // Show message on screen
        System.out.println("Running simulation using Curvature of Wave Propagation Method (CWPM) for " + nrYears + " years");

        // Run simulation
        runSimulation(nrYears);
    }
    
    /**
     * Check position of Solar System bodies against ephemeris data.
     * @param dateTime simulation date/time
     * @param day      current day of simulation
     */
    private void checkPosition(GregorianCalendar dateTime, int day) {
        
        // Position [m] of the Sun
        Particle sun = particleSystem.getParticle("Sun");
        Vector3D positionSun = sun.getPosition();

        // Position [m] of the other bodies
        for (String name : bodyNames) {
            // Expected position [m] of body with respect to the Sun
            Vector3D positionExpected = ephemeris.getBodyPosition(name,dateTime);

            // Actual position [m] of body
            Particle planet = particleSystem.getParticle(name);
            Vector3D positionBody = planet.getPosition();

            // Actual position of body with respect to the Sun
            Vector3D positionActual = positionBody.minus(positionSun);

            // Deviation [m] between actual position to expected position
            double deviationPosition = positionActual.euclideanDistance(positionExpected);
            
            // Store deviation in position to compute average
            (deviations.get(name)).add(deviationPosition);
            
            // Show average deviation after 365 days of simulation
            if (day > 0 && day % 365 == 0) {
                // Compute average deviation in position over the past 365 days
                double sumDeviations = 0.0;
                for (double deviation : deviations.get(name)) {
                    sumDeviations += deviation;
                }
                double averageDeviation = sumDeviations / (deviations.get(name)).size();
                (deviations.get(name)).clear();
                
                // Show average deviation on screen every 100 years
                if (day % 36500 == 0) {
                    System.out.println("Year: " + day / 365 + " body: " + name +
                            " average deviation: " + averageDeviation / 1000 + " km");
                }
            }
        }
    }
    
    /**
     * Create body of the Solar System corresponding to given name.
     * Create and initialize a new particle with mass, standard gravitational
     * parameter, position, and velocity corresponding to given date/time.
     * The new particle will be added to the particle system.
     * @param name     name of the body
     * @param dateTime date/time to initialize position and velocity
     */
    private void createBody(String name, GregorianCalendar dateTime) {
        // Obtain mass of body
        double mass = SolarSystemParameters.getInstance().getMass(name);
        
        // Obtain parameter mu = G * m, where G = Gravitational constant and m = mass
        double mu = SolarSystemParameters.getInstance().getMu(name);
         
        // Obtain initial (x,y,z) position [m] from ephemeris
        Vector3D position = ephemeris.getBodyPosition(name, dateTime);
        
        // Obtain intial (x,y,z) velocity [m/s] from ephemeris
        Vector3D velocity = ephemeris.getBodyVelocity(name, dateTime);

        // Add particle for simulation
        if ("Earth".equals(name)) {
            // Use oblateness to compute acceleration of Earth's Moon
            OblatePlanet planet =
                    new OblatePlanet(name, dateTime, mass, mu, position, velocity);
            particleSystem.addParticle(name, planet);
        } else {
            Particle particle = new Particle(mass, mu, position, velocity);
            particleSystem.addParticle(name, particle);
        }
    }
    
    /**
     * Main method.
     * Run two simulations for 580 years using Newton Mechanics and General Relativity, 
     * respectively. Simulation results are compared to Ephemeris DE405 data. 
     * Simulation time step is 1 hour.
     * @param args input arguments (not used)
     */
    public static void main (String[] args) {
        // Experiment set-up
        SimulationAccuracyExperiment experiment = new SimulationAccuracyExperiment();
        
        // Run simulation using Newton Mechanics for 600 years
        Long startNM = System.currentTimeMillis();
        experiment.simulateNewtonMechanics(600);
        Long stopNM = System.currentTimeMillis();
        System.out.println("Computation time Newton Mechanics : " + (stopNM - startNM)/1000 + " s");

        // Run simulation using General Relativity for 600 years
        Long startGR = System.currentTimeMillis();
        experiment.simulateGeneralRelativity(600);
        Long stopGR = System.currentTimeMillis();
        System.out.println("Computation time General Relativity : " + (stopGR - startGR)/1000 + " s");

        // Run simulation using Curvature of Wave Propagation Method (CWPM) for 600 years
        Long startCWPM = System.currentTimeMillis();
        experiment.simulateCurvatureWavePropagation(600);
        Long stopCWPM = System.currentTimeMillis();
        System.out.println("Computation time Curvature of Wave Propagation Method : " + (stopCWPM - startCWPM)/1000 + " s");
    }

    /*
        Running simulation using Newton Mechanics for 600 years
        Year: 100 body: Mercury average deviation: 15696.344408740277 km
        Year: 100 body: Venus average deviation: 9228.146075506156 km
        Year: 100 body: Earth average deviation: 5916.612366864066 km
        Year: 100 body: Moon average deviation: 5922.7420981041405 km
        Year: 100 body: Mars average deviation: 2396.826905150947 km
        Year: 100 body: Jupiter average deviation: 316.09479720192513 km
        Year: 100 body: Saturn average deviation: 25.182541181543954 km
        Year: 100 body: Uranus average deviation: 108.43526053082994 km
        Year: 100 body: Neptune average deviation: 34.47117068562416 km
        Year: 100 body: Pluto System average deviation: 55.14515805115282 km

        Year: 200 body: Mercury average deviation: 31412.838970582376 km
        Year: 200 body: Venus average deviation: 18421.432710679255 km
        Year: 200 body: Earth average deviation: 11863.689599155057 km
        Year: 200 body: Moon average deviation: 11871.83302058216 km
        Year: 200 body: Mars average deviation: 4372.196872023489 km
        Year: 200 body: Jupiter average deviation: 700.9589053923255 km
        Year: 200 body: Saturn average deviation: 79.59395322377586 km
        Year: 200 body: Uranus average deviation: 249.82190974105814 km
        Year: 200 body: Neptune average deviation: 64.57753326455845 km
        Year: 200 body: Pluto System average deviation: 163.98136337132297 km

        Year: 300 body: Mercury average deviation: 47244.76944377595 km
        Year: 300 body: Venus average deviation: 27787.07067992593 km
        Year: 300 body: Earth average deviation: 17811.061061810804 km
        Year: 300 body: Moon average deviation: 17817.826290829653 km
        Year: 300 body: Mars average deviation: 6303.0617907412225 km
        Year: 300 body: Jupiter average deviation: 855.7191210767357 km
        Year: 300 body: Saturn average deviation: 65.72469669606453 km
        Year: 300 body: Uranus average deviation: 387.11924427800545 km
        Year: 300 body: Neptune average deviation: 148.16586248524357 km
        Year: 300 body: Pluto System average deviation: 122.40852621087913 km

        Year: 400 body: Mercury average deviation: 63326.31933716076 km
        Year: 400 body: Venus average deviation: 36906.22324212632 km
        Year: 400 body: Earth average deviation: 23757.319045108747 km
        Year: 400 body: Moon average deviation: 23759.63999602375 km
        Year: 400 body: Mars average deviation: 8842.541485263371 km
        Year: 400 body: Jupiter average deviation: 1450.9931557426466 km
        Year: 400 body: Saturn average deviation: 187.26296402207436 km
        Year: 400 body: Uranus average deviation: 486.7255721417548 km
        Year: 400 body: Neptune average deviation: 155.4683958456844 km
        Year: 400 body: Pluto System average deviation: 395.17673800677 km

        Year: 500 body: Mercury average deviation: 79734.46112997102 km
        Year: 500 body: Venus average deviation: 46291.40222342086 km
        Year: 500 body: Earth average deviation: 29705.498485231463 km
        Year: 500 body: Moon average deviation: 29702.642004979953 km
        Year: 500 body: Mars average deviation: 12205.582108296192 km
        Year: 500 body: Jupiter average deviation: 1431.2870059853458 km
        Year: 500 body: Saturn average deviation: 199.52213657731124 km
        Year: 500 body: Uranus average deviation: 581.699817023832 km
        Year: 500 body: Neptune average deviation: 238.25674047009062 km
        Year: 500 body: Pluto System average deviation: 231.75938519706827 km

        Year: 600 body: Mercury average deviation: 96487.19403592826 km
        Year: 600 body: Venus average deviation: 55470.825460066946 km
        Year: 600 body: Earth average deviation: 35650.3457572258 km
        Year: 600 body: Moon average deviation: 35641.69785507082 km
        Year: 600 body: Mars average deviation: 15791.579923819347 km
        Year: 600 body: Jupiter average deviation: 2033.8688697596333 km
        Year: 600 body: Saturn average deviation: 237.8991623604191 km
        Year: 600 body: Uranus average deviation: 723.5117772827939 km
        Year: 600 body: Neptune average deviation: 284.47840830165484 km
        Year: 600 body: Pluto System average deviation: 420.86375878060113 km
        Computation time Newton Mechanics : 36 s

        Running simulation using General Relativity for 600 years
        Year: 100 body: Mercury average deviation: 2.2160851425307246 km
        Year: 100 body: Venus average deviation: 1.6864158183041007 km
        Year: 100 body: Earth average deviation: 45.34697429913651 km
        Year: 100 body: Moon average deviation: 46.22053263098865 km
        Year: 100 body: Mars average deviation: 171.52349325056505 km
        Year: 100 body: Jupiter average deviation: 93.58293964033635 km
        Year: 100 body: Saturn average deviation: 137.27024487345452 km
        Year: 100 body: Uranus average deviation: 54.68083021595303 km
        Year: 100 body: Neptune average deviation: 73.20649981680536 km
        Year: 100 body: Pluto System average deviation: 43.650260944872805 km

        Year: 200 body: Mercury average deviation: 9.029623188774496 km
        Year: 200 body: Venus average deviation: 2.393530536751434 km
        Year: 200 body: Earth average deviation: 91.04458659847317 km
        Year: 200 body: Moon average deviation: 139.11680212169503 km
        Year: 200 body: Mars average deviation: 330.7015288375295 km
        Year: 200 body: Jupiter average deviation: 206.8820929492279 km
        Year: 200 body: Saturn average deviation: 319.53755048126027 km
        Year: 200 body: Uranus average deviation: 126.12072768464519 km
        Year: 200 body: Neptune average deviation: 121.18428635286209 km
        Year: 200 body: Pluto System average deviation: 146.25767797785446 km

        Year: 300 body: Mercury average deviation: 31.562177553581137 km
        Year: 300 body: Venus average deviation: 3.504136148261649 km
        Year: 300 body: Earth average deviation: 136.89304172046081 km
        Year: 300 body: Moon average deviation: 417.57202207169206 km
        Year: 300 body: Mars average deviation: 445.6642375682609 km
        Year: 300 body: Jupiter average deviation: 294.38966791024853 km
        Year: 300 body: Saturn average deviation: 413.0272246877849 km
        Year: 300 body: Uranus average deviation: 201.2507782264417 km
        Year: 300 body: Neptune average deviation: 257.1146797956943 km
        Year: 300 body: Pluto System average deviation: 112.89116007300497 km

        Year: 400 body: Mercury average deviation: 66.43096516972626 km
        Year: 400 body: Venus average deviation: 5.314413609489632 km
        Year: 400 body: Earth average deviation: 182.7379386220461 km
        Year: 400 body: Moon average deviation: 848.4580255543099 km
        Year: 400 body: Mars average deviation: 569.46897183995 km
        Year: 400 body: Jupiter average deviation: 423.636284038079 km
        Year: 400 body: Saturn average deviation: 607.3622384743568 km
        Year: 400 body: Uranus average deviation: 259.65169472135165 km
        Year: 400 body: Neptune average deviation: 286.0299313650391 km
        Year: 400 body: Pluto System average deviation: 323.38891998483734 km

        Year: 500 body: Mercury average deviation: 113.68586141294976 km
        Year: 500 body: Venus average deviation: 6.758164378218149 km
        Year: 500 body: Earth average deviation: 228.12955605878182 km
        Year: 500 body: Moon average deviation: 1422.7893536417675 km
        Year: 500 body: Mars average deviation: 732.5966358850064 km
        Year: 500 body: Jupiter average deviation: 500.4461049736107 km
        Year: 500 body: Saturn average deviation: 729.4851825742157 km
        Year: 500 body: Uranus average deviation: 310.1904652871179 km
        Year: 500 body: Neptune average deviation: 404.5642674027708 km
        Year: 500 body: Pluto System average deviation: 224.90006533057098 km

        Year: 600 body: Mercury average deviation: 173.4693348251751 km
        Year: 600 body: Venus average deviation: 7.712660570977729 km
        Year: 600 body: Earth average deviation: 273.067076327504 km
        Year: 600 body: Moon average deviation: 2158.52360762621 km
        Year: 600 body: Mars average deviation: 867.6119567994816 km
        Year: 600 body: Jupiter average deviation: 635.7367234863823 km
        Year: 600 body: Saturn average deviation: 808.6094386616495 km
        Year: 600 body: Uranus average deviation: 372.66956595628983 km
        Year: 600 body: Neptune average deviation: 496.530300102512 km
        Year: 600 body: Pluto System average deviation: 346.72740611799645 km
        Computation time General Relativity : 461 s

        Running simulation using Curvature of Wave Propagation Method (CWPM) for 600 years
        Year: 100 body: Mercury average deviation: 69.39295411325449 km
        Year: 100 body: Venus average deviation: 3.7377374585809218 km
        Year: 100 body: Earth average deviation: 49.70854995509628 km
        Year: 100 body: Moon average deviation: 51.57942171469133 km
        Year: 100 body: Mars average deviation: 176.23300009916647 km
        Year: 100 body: Jupiter average deviation: 92.68439995408697 km
        Year: 100 body: Saturn average deviation: 136.64043180308371 km
        Year: 100 body: Uranus average deviation: 54.81094303604363 km
        Year: 100 body: Neptune average deviation: 71.60031084725304 km
        Year: 100 body: Pluto System average deviation: 42.82416118834642 km

        Year: 200 body: Mercury average deviation: 91.58419955889808 km
        Year: 200 body: Venus average deviation: 8.633020686340213 km
        Year: 200 body: Earth average deviation: 100.02657868094153 km
        Year: 200 body: Moon average deviation: 131.75228379075136 km
        Year: 200 body: Mars average deviation: 339.70505164916545 km
        Year: 200 body: Jupiter average deviation: 204.79002447306684 km
        Year: 200 body: Saturn average deviation: 318.0960196961095 km
        Year: 200 body: Uranus average deviation: 125.92665626691256 km
        Year: 200 body: Neptune average deviation: 120.4725925489998 km
        Year: 200 body: Pluto System average deviation: 145.95108725268562 km

        Year: 300 body: Mercury average deviation: 122.9032713303424 km
        Year: 300 body: Venus average deviation: 13.102855261561555 km
        Year: 300 body: Earth average deviation: 150.33962453582208 km
        Year: 300 body: Moon average deviation: 397.69921706922247 km
        Year: 300 body: Mars average deviation: 459.22834908348665 km
        Year: 300 body: Jupiter average deviation: 291.90630166823007 km
        Year: 300 body: Saturn average deviation: 411.4434297576111 km
        Year: 300 body: Uranus average deviation: 200.70741394687005 km
        Year: 300 body: Neptune average deviation: 253.1600289897645 km
        Year: 300 body: Pluto System average deviation: 114.05444361087015 km

        Year: 400 body: Mercury average deviation: 150.56627650533093 km
        Year: 400 body: Venus average deviation: 16.85208046690599 km
        Year: 400 body: Earth average deviation: 200.60126637403917 km
        Year: 400 body: Moon average deviation: 819.7501831581628 km
        Year: 400 body: Mars average deviation: 587.9228597715771 km
        Year: 400 body: Jupiter average deviation: 419.2951147647167 km
        Year: 400 body: Saturn average deviation: 604.3943678385083 km
        Year: 400 body: Uranus average deviation: 259.6175352071738 km
        Year: 400 body: Neptune average deviation: 283.2222948398999 km
        Year: 400 body: Pluto System average deviation: 320.26828218956246 km

        Year: 500 body: Mercury average deviation: 132.7710272584092 km
        Year: 500 body: Venus average deviation: 21.030145940220347 km
        Year: 500 body: Earth average deviation: 250.56306526672265 km
        Year: 500 body: Moon average deviation: 1385.8622336101607 km
        Year: 500 body: Mars average deviation: 756.6372265996325 km
        Year: 500 body: Jupiter average deviation: 496.1290612152379 km
        Year: 500 body: Saturn average deviation: 726.5233584306108 km
        Year: 500 body: Uranus average deviation: 311.2789437563982 km
        Year: 500 body: Neptune average deviation: 400.8705324181377 km
        Year: 500 body: Pluto System average deviation: 227.21669479939436 km

        Year: 600 body: Mercury average deviation: 143.6523531802757 km
        Year: 600 body: Venus average deviation: 25.603293823387503 km
        Year: 600 body: Earth average deviation: 299.7956081887951 km
        Year: 600 body: Moon average deviation: 2113.96050077713 km
        Year: 600 body: Mars average deviation: 897.6909726757672 km
        Year: 600 body: Jupiter average deviation: 629.6477415351101 km
        Year: 600 body: Saturn average deviation: 804.9792291816616 km
        Year: 600 body: Uranus average deviation: 374.17289177904286 km
        Year: 600 body: Neptune average deviation: 489.17118210969545 km
        Year: 600 body: Pluto System average deviation: 345.6900101200245 km
        Computation time Curvature of Wave Propagation Method : 94 s
     */
}
