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
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import solarsystem.OblatePlanet;
import util.Vector3D;

import java.util.*;

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
        
        // Run simulation using Newton Mechanics for 580 years
        Long startNM = System.currentTimeMillis();
        experiment.simulateNewtonMechanics(580);
        Long stopNM = System.currentTimeMillis();
        System.out.println("Computation time Newton Mechanics : " + (stopNM - startNM)/1000 + " s");

        // Run simulation using General Relativity for 580 years
        Long startGR = System.currentTimeMillis();
        experiment.simulateGeneralRelativity(580);
        Long stopGR = System.currentTimeMillis();
        System.out.println("Computation time General Relativity : " + (stopGR - startGR)/1000 + " s");
    }

    /*
        Results Newton Mechanics
        Running simulation using Newton Mechanics for 580 years
        Year: 100 body: Mercury average deviation: 18625.928947541976 km
        Year: 100 body: Venus average deviation: 8765.963802701044 km
        Year: 100 body: Earth average deviation: 5821.592604274995 km
        Year: 100 body: Moon average deviation: 5824.731562609695 km
        Year: 100 body: Mars average deviation: 4310.1908176332945 km
        Year: 100 body: Jupiter average deviation: 322.84896156040577 km
        Year: 100 body: Saturn average deviation: 211.25493009717601 km
        Year: 100 body: Uranus average deviation: 130.3617850378943 km
        Year: 100 body: Neptune average deviation: 130.37474073184802 km
        Year: 100 body: Pluto average deviation: 76.56482921474375 km

        Year: 200 body: Mercury average deviation: 37234.661652821604 km
        Year: 200 body: Venus average deviation: 17659.326120819387 km
        Year: 200 body: Earth average deviation: 11673.158364387838 km
        Year: 200 body: Moon average deviation: 11676.131198580695 km
        Year: 200 body: Mars average deviation: 8763.462054267427 km
        Year: 200 body: Jupiter average deviation: 765.6369908053357 km
        Year: 200 body: Saturn average deviation: 490.1674349994961 km
        Year: 200 body: Uranus average deviation: 268.76325505233376 km
        Year: 200 body: Neptune average deviation: 155.2086126619703 km
        Year: 200 body: Pluto average deviation: 80.92563424874656 km

        Year: 300 body: Mercury average deviation: 55901.37857573728 km
        Year: 300 body: Venus average deviation: 26380.95806470156 km
        Year: 300 body: Earth average deviation: 17524.00825726338 km
        Year: 300 body: Moon average deviation: 17524.084579701717 km
        Year: 300 body: Mars average deviation: 12634.752312988034 km
        Year: 300 body: Jupiter average deviation: 1067.7705171110877 km
        Year: 300 body: Saturn average deviation: 758.8472653400294 km
        Year: 300 body: Uranus average deviation: 412.78084834382736 km
        Year: 300 body: Neptune average deviation: 296.59167190298143 km
        Year: 300 body: Pluto average deviation: 79.91238651771071 km

        Year: 400 body: Mercury average deviation: 74794.41845204984 km
        Year: 400 body: Venus average deviation: 35347.67981063984 km
        Year: 400 body: Earth average deviation: 23375.077014003917 km
        Year: 400 body: Moon average deviation: 23375.678865488004 km
        Year: 400 body: Mars average deviation: 15618.685154869578 km
        Year: 400 body: Jupiter average deviation: 1369.0095337751347 km
        Year: 400 body: Saturn average deviation: 879.9790665142723 km
        Year: 400 body: Uranus average deviation: 576.1789847097237 km
        Year: 400 body: Neptune average deviation: 362.2523569538562 km
        Year: 400 body: Pluto average deviation: 171.6984132482397 km

        Year: 500 body: Mercury average deviation: 94047.03384033516 km
        Year: 500 body: Venus average deviation: 44044.08140649164 km
        Year: 500 body: Earth average deviation: 29227.128705950716 km
        Year: 500 body: Moon average deviation: 29228.281647668904 km
        Year: 500 body: Mars average deviation: 18216.946186360303 km
        Year: 500 body: Jupiter average deviation: 1968.9893189681825 km
        Year: 500 body: Saturn average deviation: 1322.0827309327426 km
        Year: 500 body: Uranus average deviation: 762.2288734926788 km
        Year: 500 body: Neptune average deviation: 423.1367367321744 km
        Year: 500 body: Pluto average deviation: 122.66751172932318 km

        Year: 580 body: Mercury average deviation: 109733.554529146 km
        Year: 580 body: Venus average deviation: 51136.22158822352 km
        Year: 580 body: Earth average deviation: 33906.337465320685 km
        Year: 580 body: Moon average deviation: 33891.12403133732 km
        Year: 580 body: Mars average deviation: 24950.17374123736 km
        Year: 580 body: Jupiter average deviation: 2226.618763286607 km
        Year: 580 body: Saturn average deviation: 1300.8029228241148 km
        Year: 580 body: Uranus average deviation: 866.8156399752783 km
        Year: 580 body: Neptune average deviation: 540.7700465028906 km
        Year: 580 body: Pluto average deviation: 172.1684833887763 km
        Computation time Newton Mechanics : 66 s

        Results General Relativity
        Running simulation using General Relativity for 580 years
        Year: 100 body: Mercury average deviation: 7.8899534522612695 km
        Year: 100 body: Venus average deviation: 0.5797586369419712 km
        Year: 100 body: Earth average deviation: 22.191864257816306 km
        Year: 100 body: Moon average deviation: 1783.1675336184387 km
        Year: 100 body: Mars average deviation: 28.25241594712821 km
        Year: 100 body: Jupiter average deviation: 179.39378016491912 km
        Year: 100 body: Saturn average deviation: 2.2004724176383714 km
        Year: 100 body: Uranus average deviation: 59.93625807023895 km
        Year: 100 body: Neptune average deviation: 91.09689732154345 km
        Year: 100 body: Pluto average deviation: 90.59574430045559 km

        Year: 200 body: Mercury average deviation: 4.578553540964244 km
        Year: 200 body: Venus average deviation: 0.5302012982662195 km
        Year: 200 body: Earth average deviation: 42.55626524745852 km
        Year: 200 body: Moon average deviation: 3414.2959970491775 km
        Year: 200 body: Mars average deviation: 86.64014879161807 km
        Year: 200 body: Jupiter average deviation: 390.76447942610463 km
        Year: 200 body: Saturn average deviation: 17.971468803478768 km
        Year: 200 body: Uranus average deviation: 128.20569706010673 km
        Year: 200 body: Neptune average deviation: 97.44158449840842 km
        Year: 200 body: Pluto average deviation: 91.26158929746197 km

        Year: 300 body: Mercury average deviation: 13.079672498509865 km
        Year: 300 body: Venus average deviation: 1.0212771542088963 km
        Year: 300 body: Earth average deviation: 60.95243683923 km
        Year: 300 body: Moon average deviation: 4876.708055641773 km
        Year: 300 body: Mars average deviation: 182.6795722438903 km
        Year: 300 body: Jupiter average deviation: 567.757330477568 km
        Year: 300 body: Saturn average deviation: 28.13859068257029 km
        Year: 300 body: Uranus average deviation: 206.86104627086854 km
        Year: 300 body: Neptune average deviation: 188.03762114033276 km
        Year: 300 body: Pluto average deviation: 98.01547735571295 km

        Year: 400 body: Mercury average deviation: 41.430107929263436 km
        Year: 400 body: Venus average deviation: 1.9445279151787878 km
        Year: 400 body: Earth average deviation: 77.71020300251391 km
        Year: 400 body: Moon average deviation: 6229.486317763341 km
        Year: 400 body: Mars average deviation: 269.5009515610878 km
        Year: 400 body: Jupiter average deviation: 752.6031682539086 km
        Year: 400 body: Saturn average deviation: 15.178521329374027 km
        Year: 400 body: Uranus average deviation: 291.04824661783607 km
        Year: 400 body: Neptune average deviation: 232.6928699636813 km
        Year: 400 body: Pluto average deviation: 228.3269649441087 km

        Year: 500 body: Mercury average deviation: 82.19125238888542 km
        Year: 500 body: Venus average deviation: 2.6313191285966884 km
        Year: 500 body: Earth average deviation: 92.79667089218036 km
        Year: 500 body: Moon average deviation: 7414.950007498352 km
        Year: 500 body: Mars average deviation: 355.4188459543188 km
        Year: 500 body: Jupiter average deviation: 974.4160649201461 km
        Year: 500 body: Saturn average deviation: 7.621008838661802 km
        Year: 500 body: Uranus average deviation: 374.70480309999476 km
        Year: 500 body: Neptune average deviation: 265.1042436883695 km
        Year: 500 body: Pluto average deviation: 130.44153021246137 km

        Year: 580 body: Mercury average deviation: 123.733022181046 km
        Year: 580 body: Venus average deviation: 2.5812393486105574 km
        Year: 580 body: Earth average deviation: 103.85333337572817 km
        Year: 580 body: Moon average deviation: 8249.20828921695 km
        Year: 580 body: Mars average deviation: 472.0843011247135 km
        Year: 580 body: Jupiter average deviation: 1120.3307263253364 km
        Year: 580 body: Saturn average deviation: 26.31542544687817 km
        Year: 580 body: Uranus average deviation: 431.17382739718454 km
        Year: 580 body: Neptune average deviation: 343.52119948799907 km
        Year: 580 body: Pluto average deviation: 236.99795063662842 km
        Computation time General Relativity : 710 s
     */
}
