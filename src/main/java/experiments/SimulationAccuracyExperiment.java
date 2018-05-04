/*
 * Copyright (c) 2017 Nico Kuijpers
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
 * ephemeris data from DE405 over a period of 300 years. Simulation time step is 1 hour.
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
        bodyNames.add("mercury");
        bodyNames.add("venus");
        bodyNames.add("earth");
        bodyNames.add("moon");
        bodyNames.add("mars");
        bodyNames.add("jupiter");
        bodyNames.add("saturn");
        bodyNames.add("uranus");
        bodyNames.add("neptune");
        bodyNames.add("pluto");

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
        double massSun = SolarSystemParameters.getInstance().getMass("sun");
        double muSun   = SolarSystemParameters.getInstance().getMu("sun");
        particleSystem.addParticle("sun",massSun,muSun,positionSun,velocitySun);
        
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
        Particle sun = particleSystem.getParticle("sun");
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
}
