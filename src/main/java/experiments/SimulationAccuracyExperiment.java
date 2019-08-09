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
import ephemeris.IEphemeris;
import ephemeris.SolarSystemParameters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import util.Vector3D;

/**
 * Experiment to determine accuracy of simulation results for the following bodies of
 * the Solar System: Mercury, Venus, Earth, Moon, Mars, Jupiter, Saturn, Uranus, and
 * Pluto.
 * Simulation results using Newton Mechanics and General Relativity are compared to
 * ephemeris data from DE405 over a period of 580 years. Simulation time step is 1 hour.
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
        ephemeris = EphemerisAccurate.getInstance();
        
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
        bodyNames.add("Pluto");

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
        simulationDateTime = new GregorianCalendar(1620,0,1);
        
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
        particleSystem.addParticle("Sun",massSun,muSun,positionSun,velocitySun);
        
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
            particleSystem.advanceRungeKutta(deltaT);
            
            // Correction for position of Sun
            particleSystem.correctDrift();
            
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
        
        // Show message on screen
        System.out.println("Running simulation using General Relativity for " + nrYears + " years");
        
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
            if (day % 365 == 0) {
                // Compute average deviation in position over the past 365 days
                double sumDeviations = 0.0;
                for (double deviation : deviations.get(name)) {
                    sumDeviations += deviation;
                }
                double averageDeviation = sumDeviations / (deviations.get(name)).size();
                (deviations.get(name)).clear();
                
                // Show average deviation on screen
                System.out.println("Year: " + day/365 + " body: " + name +
                        " average deviation: " + averageDeviation/1000 + " km");
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
        particleSystem.addParticle(name, mass, mu, position, velocity);
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
        
        // Run simulation using Newton Mechanics for 580 years
        experiment.simulateNewtonMechanics(580);
        
        // Run simulation using General Relativity for 580 years
        experiment.simulateGeneralRelativity(580);
    }

    /*
        Results
        Running simulation using Newton Mechanics for 580 years
        Year: 100 body: Mercury average deviation: 18651.583194490602 km
        Year: 100 body: Venus average deviation: 8765.837724536346 km
        Year: 100 body: Earth average deviation: 5876.205613169912 km
        Year: 100 body: Moon average deviation: 5954.956687681072 km
        Year: 100 body: Mars average deviation: 4310.288495779539 km
        Year: 100 body: Jupiter average deviation: 322.3976264087076 km
        Year: 100 body: Saturn average deviation: 211.01382573772125 km
        Year: 100 body: Uranus average deviation: 130.35728265947537 km
        Year: 100 body: Neptune average deviation: 130.43348732643636 km
        Year: 100 body: Pluto average deviation: 76.58386015476631 km

        Year: 200 body: Mercury average deviation: 37335.51118437043 km
        Year: 200 body: Venus average deviation: 17659.21805118538 km
        Year: 200 body: Earth average deviation: 11782.808245770959 km
        Year: 200 body: Moon average deviation: 11930.386307840496 km
        Year: 200 body: Mars average deviation: 8763.515054596855 km
        Year: 200 body: Jupiter average deviation: 763.9645837030145 km
        Year: 200 body: Saturn average deviation: 489.1737850564723 km
        Year: 200 body: Uranus average deviation: 268.80452685831784 km
        Year: 200 body: Neptune average deviation: 155.44399508105494 km
        Year: 200 body: Pluto average deviation: 80.9992757949874 km

        Year: 300 body: Mercury average deviation: 56127.22340244562 km
        Year: 300 body: Venus average deviation: 26380.931438767166 km
        Year: 300 body: Earth average deviation: 17689.132773553112 km
        Year: 300 body: Moon average deviation: 17850.52836028824 km
        Year: 300 body: Mars average deviation: 12634.570812166914 km
        Year: 300 body: Jupiter average deviation: 1064.399543686393 km
        Year: 300 body: Saturn average deviation: 756.6331448999065 km
        Year: 300 body: Uranus average deviation: 413.05081313222166 km
        Year: 300 body: Neptune average deviation: 296.84056142687166 km
        Year: 300 body: Pluto average deviation: 80.10501770584065 km

        Year: 400 body: Mercury average deviation: 75195.34514248552 km
        Year: 400 body: Venus average deviation: 35347.71925664574 km
        Year: 400 body: Earth average deviation: 23594.39574782688 km
        Year: 400 body: Moon average deviation: 23844.397185147936 km
        Year: 400 body: Mars average deviation: 15618.342505079518 km
        Year: 400 body: Jupiter average deviation: 1363.4102042484506 km
        Year: 400 body: Saturn average deviation: 876.3318680476077 km
        Year: 400 body: Uranus average deviation: 576.6481130427593 km
        Year: 400 body: Neptune average deviation: 362.9321318953938 km
        Year: 400 body: Pluto average deviation: 172.00843459476766 km

        Year: 500 body: Mercury average deviation: 94674.02617504861 km
        Year: 500 body: Venus average deviation: 44044.11028850319 km
        Year: 500 body: Earth average deviation: 29501.411511713057 km
        Year: 500 body: Moon average deviation: 29753.436325477272 km
        Year: 500 body: Mars average deviation: 18216.3418997041 km
        Year: 500 body: Jupiter average deviation: 1960.0045999900474 km
        Year: 500 body: Saturn average deviation: 1315.7648717835823 km
        Year: 500 body: Uranus average deviation: 762.8208479025428 km
        Year: 500 body: Neptune average deviation: 423.9939231023964 km
        Year: 500 body: Pluto average deviation: 123.01666966566057 km

        Year: 580 body: Mercury average deviation: 110579.13594426884 km
        Year: 580 body: Venus average deviation: 51136.13456698557 km
        Year: 580 body: Earth average deviation: 34226.08918745122 km
        Year: 580 body: Moon average deviation: 34324.88117117762 km
        Year: 580 body: Mars average deviation: 24949.306410071655 km
        Year: 580 body: Jupiter average deviation: 2214.908440562485 km
        Year: 580 body: Saturn average deviation: 1292.7868861682161 km
        Year: 580 body: Uranus average deviation: 867.6146720251757 km
        Year: 580 body: Neptune average deviation: 541.9015250786042 km
        Year: 580 body: Pluto average deviation: 172.72175184568306 km

        Results General Relativity
        Running simulation using General Relativity for 580 years
        Year: 100 body: Mercury average deviation: 6.5872504996988965 km
        Year: 100 body: Venus average deviation: 0.7913719838889641 km
        Year: 100 body: Earth average deviation: 22.244676585576364 km
        Year: 100 body: Moon average deviation: 1782.9640678254214 km
        Year: 100 body: Mars average deviation: 27.487353755048634 km
        Year: 100 body: Jupiter average deviation: 179.5019725749893 km
        Year: 100 body: Saturn average deviation: 1.9511890912301417 km
        Year: 100 body: Uranus average deviation: 59.91169602875984 km
        Year: 100 body: Neptune average deviation: 91.16752352418072 km
        Year: 100 body: Pluto average deviation: 90.62129606910963 km

        Year: 200 body: Mercury average deviation: 3.993786330625798 km
        Year: 200 body: Venus average deviation: 0.7806876843992967 km
        Year: 200 body: Earth average deviation: 42.67821943077017 km
        Year: 200 body: Moon average deviation: 3413.4585779637127 km
        Year: 200 body: Mars average deviation: 85.23787096879305 km
        Year: 200 body: Jupiter average deviation: 391.69927190041057 km
        Year: 200 body: Saturn average deviation: 16.90892493401689 km
        Year: 200 body: Uranus average deviation: 128.1999190186336 km
        Year: 200 body: Neptune average deviation: 97.68282957051868 km
        Year: 200 body: Pluto average deviation: 91.34766130040434 km

        Year: 300 body: Mercury average deviation: 17.115084379494913 km
        Year: 300 body: Venus average deviation: 1.241210843427374 km
        Year: 300 body: Earth average deviation: 61.172511835421936 km
        Year: 300 body: Moon average deviation: 4875.105323834315 km
        Year: 300 body: Mars average deviation: 180.82237823327452 km
        Year: 300 body: Jupiter average deviation: 570.0664723547256 km
        Year: 300 body: Saturn average deviation: 25.720937065495797 km
        Year: 300 body: Uranus average deviation: 207.05738509369172 km
        Year: 300 body: Neptune average deviation: 188.29534854609886 km
        Year: 300 body: Pluto average deviation: 98.22483323992032 km

        Year: 400 body: Mercury average deviation: 47.06389889892487 km
        Year: 400 body: Venus average deviation: 2.1963158271700944 km
        Year: 400 body: Earth average deviation: 77.95404865220482 km
        Year: 400 body: Moon average deviation: 6227.4272610123535 km
        Year: 400 body: Mars average deviation: 267.30529654399936 km
        Year: 400 body: Jupiter average deviation: 756.8249499169876 km
        Year: 400 body: Saturn average deviation: 11.449753542330138 km
        Year: 400 body: Uranus average deviation: 291.4169938944626 km
        Year: 400 body: Neptune average deviation: 233.35663833887125 km
        Year: 400 body: Pluto average deviation: 228.65573074738205 km

        Year: 500 body: Mercury average deviation: 89.4974605822749 km
        Year: 500 body: Venus average deviation: 2.955757565791811 km
        Year: 500 body: Earth average deviation: 93.08150308129471 km
        Year: 500 body: Moon average deviation: 7412.670370539636 km
        Year: 500 body: Mars average deviation: 352.9815099127901 km
        Year: 500 body: Jupiter average deviation: 981.6780899729404 km
        Year: 500 body: Saturn average deviation: 7.853732782338081 km
        Year: 500 body: Uranus average deviation: 375.16913087907534 km
        Year: 500 body: Neptune average deviation: 265.9289455740811 km
        Year: 500 body: Pluto average deviation: 130.79139807243513 km

        Year: 580 body: Mercury average deviation: 132.61907816604767 km
        Year: 580 body: Venus average deviation: 2.9594120983529164 km
        Year: 580 body: Earth average deviation: 104.20405796724033 km
        Year: 580 body: Moon average deviation: 8246.649366904086 km
        Year: 580 body: Mars average deviation: 469.1001588520055 km
        Year: 580 body: Jupiter average deviation: 1130.0653915229404 km
        Year: 580 body: Saturn average deviation: 34.28755363795837 km
        Year: 580 body: Uranus average deviation: 431.8087797649835 km
        Year: 580 body: Neptune average deviation: 344.614522933986 km
        Year: 580 body: Pluto average deviation: 237.55086586273305 km
     */
}
