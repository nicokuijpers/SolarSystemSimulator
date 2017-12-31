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
package solarsystem;

import particlesystem.ParticleSystem;
import particlesystem.Particle;
import ephemeris.SolarSystemParameters;
import ephemeris.EphemerisSolarSystem;
import ephemeris.EphemerisUtil;
import ephemeris.IEphemeris;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import util.Vector3D;

/**
 * Represents the Solar System.
 * @author Nico Kuijpers
 */
public class SolarSystem extends ParticleSystem {
    
    // The Sun
    private SolarSystemBody sun;

    // Planets of the Solar System
    private Map<String,SolarSystemBody> planets;
    
    // Moons of the Solar System
    private Map<String,SolarSystemBody> moons;
    
    // Center bodies of moons
    private Map<String,String> centerBodies;
    
    // Ephemeris for the Solar System
    private final IEphemeris ephemeris;
    
    // Solar System parameters
    private final SolarSystemParameters solarSystemParameters;
    
    // Simulation date/time
    private GregorianCalendar simulationDateTime;
    
    // Simulation time step (1 hour)
    private final long deltaT = (long) (60 * 60);
    
    /**
     * Constructor: create the Solar System and initialize for current date/time.
     */
    public SolarSystem() {
        this(new GregorianCalendar());
    }
        
    /**
     * Constructor: create the Solar System and initialize for given date/time.
     * @param dateTime initial simulation date/time
     */
    public SolarSystem(GregorianCalendar dateTime) {
        // Constructor of ParticleSystem
        super();
        
        // Initialize simulation date/time
        simulationDateTime = new GregorianCalendar();
        
        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid 
        // sudden changes in ephemeris due to changes from 
        // winter time to summer time and vice versa
        simulationDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Set simulation dateTime/time to given dateTime/time
        simulationDateTime.set(Calendar.ERA, dateTime.get(Calendar.ERA));
        simulationDateTime.set(Calendar.YEAR, dateTime.get(Calendar.YEAR));
        simulationDateTime.set(Calendar.MONTH, dateTime.get(Calendar.MONTH));
        simulationDateTime.set(Calendar.DAY_OF_MONTH, dateTime.get(Calendar.DAY_OF_MONTH));
        simulationDateTime.set(Calendar.HOUR_OF_DAY, dateTime.get(Calendar.HOUR_OF_DAY));
        simulationDateTime.set(Calendar.MINUTE, dateTime.get(Calendar.MINUTE));
        simulationDateTime.set(Calendar.SECOND, 0);
        simulationDateTime.set(Calendar.MILLISECOND, 0);
        
        // Ephemeris for Solar System
        ephemeris = EphemerisSolarSystem.getInstance();
        
        // Solar System parameters
        solarSystemParameters = SolarSystemParameters.getInstance();
        
        // Initialize hash maps for planets and moons
        planets = new HashMap<>();
        moons = new HashMap<>();
        centerBodies = new HashMap<>();
        
        // Create the Sun
        Vector3D positionSun = new Vector3D(); // Origin
        Vector3D velocitySun = new Vector3D(); // Zero velocity
        double massSun = solarSystemParameters.getMass("sun");
        double muSun   = solarSystemParameters.getMu("sun");
        double diameterSun = solarSystemParameters.getDiameter("sun");
        sun = new SolarSystemBody("sun", positionSun, null, diameterSun, null);
        this.addParticle("sun", massSun, muSun, positionSun, velocitySun);

        // Create the planets
        List<String> planetNames = solarSystemParameters.getPlanets();
        for (String name : planetNames) {
            double mass = solarSystemParameters.getMass(name);
            double mu = solarSystemParameters.getMu(name);
            double diameter = solarSystemParameters.getDiameter(name);
            createBody(name, mass, mu, diameter, simulationDateTime);
        }

        // Create the moons
        List<String> moonNames = solarSystemParameters.getMoons();
        for (String moonName : moonNames) {
            String planetName = solarSystemParameters.getPlanetOfMoon(moonName);
            double mass = solarSystemParameters.getMass(moonName);
            double mu = solarSystemParameters.getMu(moonName);
            double diameter = solarSystemParameters.getDiameter(moonName);
            createMoon(planetName, moonName, mass, mu, diameter, simulationDateTime);
        }
    }
    
    /**
     * Get current simulation date/time.
     * @return current simulation date/time
     */
    public GregorianCalendar getSimulationDateTime() {
        return (GregorianCalendar) simulationDateTime.clone();
    }
    
    /**
     * Initialize simulation for given era, date, and time.
     * @param dateTime era, date, and time
     * @throws SolarSystemException when date before 3000 BC or after AD 3000
     */
    public void initializeSimulation(GregorianCalendar dateTime) throws SolarSystemException {
        // Check whether simulation date/time is valid
        if (dateTime.before(ephemeris.getFirstValidDate())) {
            throw new SolarSystemException("Date not valid before 3000 BC");
        }
        if (dateTime.after(ephemeris.getLastValidDate())) {
            throw new SolarSystemException("Date not valid after AD 3000");
        }

        // Set simulation date/time to given date/time
        simulationDateTime.set(Calendar.ERA, dateTime.get(Calendar.ERA));
        simulationDateTime.set(Calendar.YEAR, dateTime.get(Calendar.YEAR));
        simulationDateTime.set(Calendar.MONTH, dateTime.get(Calendar.MONTH));
        simulationDateTime.set(Calendar.DAY_OF_MONTH, dateTime.get(Calendar.DAY_OF_MONTH));
        simulationDateTime.set(Calendar.HOUR_OF_DAY, dateTime.get(Calendar.HOUR_OF_DAY));
        simulationDateTime.set(Calendar.MINUTE, dateTime.get(Calendar.MINUTE));
        simulationDateTime.set(Calendar.SECOND, 0);
        simulationDateTime.set(Calendar.MILLISECOND, 0);
        
        // Compute new positions and orbits for all bodies
        // corresponding to current simulation date/time
        moveBodies();
        
        // Move corresponding particles to positions
        // corresponding to current simulation date/time
        moveBodyParticles();
    } 

    /**
     * Advance forward in time for given number of simulation time steps.
     * @param nrTimeSteps number of time steps
     */
    public void advanceSimulationForward(int nrTimeSteps) {
        for (int i = 0; i < nrTimeSteps; i++) {
            // Advance using Runge-Kutta scheme
            advanceRungeKutta(deltaT);
            correctDrift();
            simulationDateTime.add(Calendar.SECOND, (int) deltaT);
        }
        // Compute new positions and orbits for all bodies
        // corresponding to current simulation date
        moveBodies();
    }
    
    /**
     * Advance backward in time for given number of simulation time steps.
     * @param nrTimeSteps number of time steps
     */
    public void advanceSimulationBackward(int nrTimeSteps) {
        for (int i = 0; i < nrTimeSteps; i++) {
            // Advance using Runge-Kutta scheme
            advanceRungeKutta(-deltaT);
            correctDrift();
            simulationDateTime.add(Calendar.SECOND, (int) -deltaT);
        }
        // Compute new positions and orbits for all bodies
        // corresponding to current simulation date
        moveBodies();
    }

    /**
     * Advance a single time step forward or backward.
     * Note that the time step should not exceed 1 hour.
     * @param timeStep time step in seconds
     */
    public void advanceSimulationSingleStep(int timeStep) {
        // Advance using Runge-Kutta scheme
        timeStep = Math.min(timeStep,3600);
        timeStep = Math.max(timeStep,-3600);
        advanceRungeKutta((long) timeStep);
        correctDrift();
        simulationDateTime.add(Calendar.SECOND, timeStep);
        // Compute new positions and orbits for all bodies
        // corresponding to current simulation date
        moveBodies();
    }
    
    /**
     * Get body with given name.
     * @param name  Name of the body
     * @return body with given name
     */
    public SolarSystemBody getBody(String name) {
        if ("sun".equals(name)) {
            return sun;
        }
        if (planets.containsKey(name)) {
            return planets.get(name);
        }
        if (moons.containsKey(name)) {
            return moons.get(name);
        }
        return null;
    }
    
    /**
     * Move all bodies to positions corresponding to simulation date/time.
     * Bodies are moved when the simulation date/time is valid, i.e.,
     * between 3000 BC and AD 3000.
     * Note that the corresponding particles are not moved.
     */
    private void moveBodies() {
        // Check whether simulation date/time is valid for ephemeris
        if (simulationDateTime.before(ephemeris.getFirstValidDate()) ||
            simulationDateTime.after(ephemeris.getLastValidDate())) {
            return;
        }
        
        // Move each planet to position of simulation date/time
        for (String name : planets.keySet()) {
            SolarSystemBody planet = planets.get(name);
            Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(name, simulationDateTime);
            Vector3D position = positionAndVelocity[0];
            Vector3D velocity = positionAndVelocity[1];
            Vector3D[] orbit = EphemerisUtil.computeOrbit("sun",position,velocity);
            planet.setPosition(position);
            planet.setOrbit(orbit);
        }
        
        // Move each moon to position of simulation date/time
        for (String name : moons.keySet()) {
            // Obtain position and velocity of moon from Ephemeris
            SolarSystemBody moon = moons.get(name);
            Vector3D[] positionAndVelocityMoon = ephemeris.getBodyPositionVelocity(name, simulationDateTime);
            Vector3D positionMoon = positionAndVelocityMoon[0];
            Vector3D velocityMoon = positionAndVelocityMoon[1];
        
            // Obtain position and velocity of planet from Ephemeris
            String planetName = centerBodies.get(name);
            Vector3D[] positionAndVelocityPlanet = ephemeris.getBodyPositionVelocity(planetName, simulationDateTime);
            Vector3D positionPlanet = positionAndVelocityPlanet[0];
            Vector3D velocityPlanet = positionAndVelocityPlanet[1];
        
            // Compute orbit of moon relative to planet
            Vector3D positionRelativeToPlanet = positionMoon.minus(positionPlanet);
            Vector3D velocityRelativeToPlanet = velocityMoon.minus(velocityPlanet);
            Vector3D[] orbitMoon = EphemerisUtil.computeOrbit(planetName,
                positionRelativeToPlanet,velocityRelativeToPlanet);
            
            // Set position and orbit
            moon.setPosition(positionMoon);
            moon.setOrbit(orbitMoon);
        }    
    }
    
    /**
     * Move all body particles to positions corresponding to simulation date/time.
     * Particles are moved when the simulation date/time is valid, i.e.,
     * between 3000 BC and AD 3000.
     */
    private void moveBodyParticles() {
        // Check whether simulation date/time is valid for ephemeris
        if (simulationDateTime.before(ephemeris.getFirstValidDate()) ||
            simulationDateTime.after(ephemeris.getLastValidDate())) {
            return;
        }
        
        // Move each planet particle to position of present date
        for (String name : planets.keySet()) {
            Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(name, simulationDateTime);
            Vector3D position = positionAndVelocity[0];
            Vector3D velocity = positionAndVelocity[1];
            Particle particle = getParticle(name);
            if (particle != null) {
                particle.setPosition(position);
                particle.setVelocity(velocity);
            }
        }
        
        // Move each moon particle to position of present date
        for (String name : moons.keySet()) {
            Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(name, simulationDateTime);
            Vector3D position = positionAndVelocity[0];
            Vector3D velocity = positionAndVelocity[1];
            Particle particle = getParticle(name);
            if (particle != null) {
                particle.setPosition(position);
                particle.setVelocity(velocity);
            }
        }    
    }
    
    /**
     * Create body, compute position and velocity, and add the body 
     * as well as corresponding particle to the Solar System.
     * It is assumed that the body is orbiting the Sun.
     * @param name      Name of the body
     * @param mass      Mass of the body in kg
     * @param mu        Standard gravitational parameter in m3/s2
     * @param diameter  Diameter of the body in m
     * @param date      Date to determine position of the moon.
     */
    private void createBody(String name, double mass, double mu, double diameter, GregorianCalendar date) {
        
        // Obtain position and velocity from Ephemeris
        Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(name, date);
        Vector3D position = positionAndVelocity[0];
        Vector3D velocity = positionAndVelocity[1];
        
        // Compute orbit relative to the sun
        Vector3D[] orbit = EphemerisUtil.computeOrbit("sun",position,velocity);
        
        // Add the new body to the solar system for computation
        this.planets.put(name, new SolarSystemBody(name, position, orbit, diameter, null));
        
        // Add the new planet as particle for simulation
        this.addParticle(name, mass, mu, position, velocity);
    }
    
    /**
     * Create moon, compute position and velocity, and add the moon 
     * as well as corresponding particle to the Solar System.
     * @param planetName  Name of the planet
     * @param moonName    Name of the moon
     * @param mass        Mass of the moon in kg
     * @param mu          Standard gravitational parameter in m3/s2
     * @param diameter    Diameter of the moon in m
     * @param date        Date to determine position of the moon.
     */
    private void createMoon(String planetName, String moonName, 
            double mass, double mu, double diameter, GregorianCalendar date) {
 
        // Obtain position and velocity of moon from Ephemeris
        Vector3D[] positionAndVelocityMoon = ephemeris.getBodyPositionVelocity(moonName, date);
        Vector3D positionMoon = positionAndVelocityMoon[0];
        Vector3D velocityMoon = positionAndVelocityMoon[1];
        
        // Obtain position and velocity of planet from Ephemeris
        Vector3D[] positionAndVelocityPlanet = ephemeris.getBodyPositionVelocity(planetName, date);
        Vector3D positionPlanet = positionAndVelocityPlanet[0];
        Vector3D velocityPlanet = positionAndVelocityPlanet[1];
        
        // Compute orbit of moon relative to planet
        Vector3D positionRelativeToPlanet = positionMoon.minus(positionPlanet);
        Vector3D velocityRelativeToPlanet = velocityMoon.minus(velocityPlanet);
        Vector3D[] orbit = EphemerisUtil.computeOrbit(planetName,
                positionRelativeToPlanet,velocityRelativeToPlanet);
        
        // Add the new moon to the Solar System for computation
        SolarSystemBody planet = this.getBody(planetName);
        this.moons.put(moonName, 
                new SolarSystemBody(moonName, positionMoon, orbit, diameter, planet));
        
        // Add the new moon as particle for simulation
        this.addParticle(moonName, mass, mu, positionMoon, velocityMoon);
        
        // Define center body for this moon
        centerBodies.put(moonName, planetName);
    }

    /**
     * Load simulation state from file.
     * @param file file to load state from
     * @throws SolarSystemException when simulation state cannot be loaded or read from file
     */
    public void loadSimulationState(File file) throws SolarSystemException {
        try {
            FileInputStream fileIn = new FileInputStream(file);
            try (ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
                simulationDateTime = (GregorianCalendar) objectIn.readObject();
                particles = (Map<String,Particle>) objectIn.readObject();
                sun = (SolarSystemBody) objectIn.readObject();
                planets = (Map<String,SolarSystemBody>) objectIn.readObject();
                moons = (Map<String,SolarSystemBody>) objectIn.readObject();
                centerBodies = (Map<String,String>) objectIn.readObject();
            }
        } catch (IOException ex) {
            throw new SolarSystemException("Cannot load simulation state from file " + file.getName());
        } catch (ClassNotFoundException ex) {
            throw new SolarSystemException("Cannot read simulation state from file " + file.getName());
        }
    }
        
    /**
     * Save simulation state to file.
     * @param file to save state to
     * @throws SolarSystemException when simulation state cannot be saved to file
     */
    public void saveSimulationState(File file) throws SolarSystemException {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            try (ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
                objectOut.writeObject(simulationDateTime);
                objectOut.writeObject(particles);
                objectOut.writeObject(sun);
                objectOut.writeObject(planets);
                objectOut.writeObject(moons);
                objectOut.writeObject(centerBodies);
            }
        } catch (IOException ex) {
            throw new SolarSystemException("Cannot save simulation state to file");
        }
    }
}
