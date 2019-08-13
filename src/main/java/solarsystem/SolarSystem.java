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
package solarsystem;

import application.SolarSystemException;
import ephemeris.EphemerisSolarSystem;
import ephemeris.EphemerisUtil;
import ephemeris.IEphemeris;
import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import spacecraft.*;
import util.Vector3D;

import java.io.Serializable;
import java.util.*;

/**
 * Represents the Solar System.
 * @author Nico Kuijpers
 */
public class SolarSystem extends ParticleSystem implements Serializable {

    // Ephemeris for the Solar System
    private static final IEphemeris ephemeris = EphemerisSolarSystem.getInstance();

    // Solar System parameters
    private static final SolarSystemParameters solarSystemParameters = SolarSystemParameters.getInstance();

    // The Sun
    private SolarSystemBody sun;

    // Planets of the Solar System
    private Map<String, SolarSystemBody> planets;
    
    // Moons of the Solar System
    private Map<String, SolarSystemBody> moons;

    // Particles for Galilean moons
    private Map<String, Particle> gmParticles;
    
     // Spacecraft in the Solar System
    private Map<String, Spacecraft> spacecraft;
    
    // Center bodies of moons and spacecraft
    private Map<String,String> centerBodies;

    // Galilean moons
    private GalileanMoons galileanMoons;
    
    // Simulation date/time
    private GregorianCalendar simulationDateTime;
    
    // Simulation time step (1 hour)
    private final long deltaT = (long) (60 * 60);

    // Spacecraft events
    private List<SpacecraftEvent> spacecraftEvents;
    private SpacecraftEvent nextEvent;
    
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
        
        // Initialize hash maps for planets and moons
        planets = new HashMap<>();
        moons = new HashMap<>();
        gmParticles = new HashMap<>();
        spacecraft = new HashMap<>();
        centerBodies = new HashMap<>();
        
        // Create the Sun
        Vector3D positionSun = new Vector3D(); // Origin
        Vector3D velocitySun = new Vector3D(); // Zero velocity
        double massSun = solarSystemParameters.getMass("Sun");
        double muSun   = solarSystemParameters.getMu("Sun");
        double diameterSun = solarSystemParameters.getDiameter("Sun");
        sun = new SolarSystemBody("Sun", positionSun, velocitySun, null, diameterSun, null);
        this.addParticle("Sun", massSun, muSun, positionSun, velocitySun);

        // Create the planets
        List<String> planetNames = solarSystemParameters.getPlanets();
        for (String name : planetNames) {
            double mass = solarSystemParameters.getMass(name);
            double mu = solarSystemParameters.getMu(name);
            double diameter = solarSystemParameters.getDiameter(name);
            createPlanet(name, mass, mu, diameter, simulationDateTime);
        }

        // Create a separate particle system containing Jupiter and the Galilean moons
        galileanMoons = new GalileanMoons();

        // Create the moons
        List<String> moonNames = solarSystemParameters.getMoons();
        for (String moonName : moonNames) {
            String planetName = solarSystemParameters.getPlanetOfMoon(moonName);
            double mass = solarSystemParameters.getMass(moonName);
            double mu = solarSystemParameters.getMu(moonName);
            double diameter = solarSystemParameters.getDiameter(moonName);
            createMoon(planetName, moonName, mass, mu, diameter, simulationDateTime);
        }

        // Spacecraft events
        spacecraftEvents = new LinkedList<>();
        nextEvent = null;
        
        // Create spacecraft Voyager 2 (Voyager 2 was launched before Voyager 1)
        Spacecraft voyagerTwo = new VoyagerTwo("Voyager 2",simulationDateTime,this);
        createSpacecraft(voyagerTwo);

        // Create spacecraft Voyager 1
        Spacecraft voyagerOne = new VoyagerOne("Voyager 1",simulationDateTime,this);
        createSpacecraft(voyagerOne);

        // Create spacecraft New Horizons
        Spacecraft newHorizons = new NewHorizons("New Horizons",simulationDateTime,this);
        createSpacecraft(newHorizons);
    }

    /**
     * Get reference to particle with given name.
     * @param name    Name of particle
     * @return particle with given name
     */
    @Override
    public Particle getParticle(String name) {
        Particle particle = super.getParticle(name);
        if (particle != null) {
            return particle;
        }
        else {
            particle = galileanMoons.getParticle(name);
            if (particle != null) {
                Particle jupiter = super.getParticle("Jupiter");
                Vector3D position = jupiter.getPosition().plus(particle.getPosition());
                Vector3D velocity = jupiter.getVelocity().plus(particle.getVelocity());
                gmParticles.get(name).setPosition(position);
                gmParticles.get(name).setVelocity(velocity);
                return gmParticles.get(name);
            }
            return null;
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

        // Schedule next spacecraft event
        scheduleNextSpacecraftEvent();
    }

    /**
     * Advance simulation of Galilean moons with time steps
     * of at most 10 minutes.
     * @param deltaT simulation time step [s]
     */
    private void advanceGalileanMoons(long deltaT) {
        long timeStep = Math.min(Math.abs(deltaT),10*60L);
        long totalTime = 0L;
        if (deltaT < 0) {
            while (totalTime > deltaT) {
                galileanMoons.advanceRungeKutta(-timeStep);
                totalTime -= timeStep;
            }
        }
        else {
            while (totalTime < deltaT) {
                galileanMoons.advanceRungeKutta(timeStep);
                totalTime += timeStep;
            }
        }
        galileanMoons.correctDrift();
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
            advanceGalileanMoons(deltaT);
            simulationDateTime.add(Calendar.SECOND, (int) deltaT);
            checkForSpacecraftEvent();
        }
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
            advanceGalileanMoons(-deltaT);
            simulationDateTime.add(Calendar.SECOND, (int) -deltaT);
        }
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
        advanceGalileanMoons((long) timeStep);
        simulationDateTime.add(Calendar.SECOND, timeStep);
        checkForSpacecraftEvent();
    }
    
    /**
     * Get body with given name.
     * @param name  Name of the body
     * @return body with given name
     */
    public SolarSystemBody getBody(String name) {
        if ("Sun".equals(name)) {
            return sun;
        }
        if (planets.containsKey(name)) {
            return planets.get(name);
        }
        if (moons.containsKey(name)) {
            return moons.get(name);
        }
        if (spacecraft.containsKey(name)) {
            return spacecraft.get(name);
        }
        return null;
    }
    
    /**
     * Move all bodies to positions corresponding to simulation date/time.
     * Bodies are moved when the simulation date/time is valid, i.e.,
     * between 3000 BC and AD 3000.
     * Note that the corresponding particles are not moved.
     */
    public void moveBodies() {

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
            double muSun = this.getParticle("Sun").getMu();
            Vector3D[] orbit = EphemerisUtil.computeOrbit(muSun,position,velocity);
            planet.setPosition(position);
            planet.setVelocity(velocity);
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
            double muPlanet = this.getParticle(planetName).getMu();
            Vector3D[] orbit;
            if ("Moon".equals(name)) {
                // Position and velocity of the Earth's moon are relative to the Sun
                Vector3D positionRelativeToPlanet = positionMoon.minus(positionPlanet);
                Vector3D velocityRelativeToPlanet = velocityMoon.minus(velocityPlanet);
                orbit = EphemerisUtil.computeOrbit(muPlanet,
                   positionRelativeToPlanet,velocityRelativeToPlanet);
            } else {
                // Position and velocity of other moons are relative to planet
                orbit = EphemerisUtil.computeOrbit(muPlanet,positionMoon,velocityMoon);
                positionMoon.addVector(positionPlanet);
                velocityMoon.addVector(velocityPlanet);
            }
            
            // Set position and orbit
            moon.setPosition(positionMoon);
            moon.setVelocity(velocityMoon);
            moon.setOrbit(orbit);
        }

        // Move each spacecraft to position of simulation date/time
        for (Spacecraft craft : spacecraft.values()) {
            craft.updateStatus(simulationDateTime);
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

            // Position and velocity of Galilean moons are relative to planet Jupiter
            Particle particle = galileanMoons.getParticle(name);
            if (particle == null) {
                particle = this.getParticle(name);
            }
            particle.setPosition(position);
            particle.setVelocity(velocity);
        }

        // Move each spacecraft particle to position of present date
        for (Spacecraft craft : spacecraft.values()) {
            craft.updateStatus(simulationDateTime);
            Vector3D position = craft.getPosition();
            Vector3D velocity = craft.getVelocity();
            Particle particle = getParticle(craft.getName());
            if (particle != null) {
                particle.setPosition(position);
                particle.setVelocity(velocity);
            }
        }
    }

    /**
     * Create planet, compute position and velocity, and add the planet 
     * as well as corresponding particle to the Solar System.
     * It is assumed that the body is orbiting the Sun.
     * @param name      Name of the body
     * @param mass      Mass of the body in kg
     * @param mu        Standard gravitational parameter in m3/s2
     * @param diameter  Diameter of the body in m
     * @param date      Date to determine position of the moon.
     */
    private void createPlanet(String name, double mass, double mu, double diameter, GregorianCalendar date) {
        
        // Obtain position and velocity from Ephemeris
        Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(name, date);
        Vector3D position = positionAndVelocity[0];
        Vector3D velocity = positionAndVelocity[1];
        
        // Compute orbit relative to the sun
        double muSun = solarSystemParameters.getMu("Sun");
        Vector3D[] orbit = EphemerisUtil.computeOrbit(muSun, position, velocity);
        
        // Add the new body to the solar system for computation
        this.planets.put(name, new SolarSystemBody(name, position, velocity, orbit, diameter, sun));
        
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
        Vector3D positionRelativeToPlanet;
        Vector3D velocityRelativeToPlanet;
        Vector3D[] orbit;
        double muPlanet = solarSystemParameters.getMu(planetName);
        if ("Moon".equals(moonName)) {
            // Position and velocity of the Earth's moon are relative to the Sun
            positionRelativeToPlanet = positionMoon.minus(positionPlanet);
            velocityRelativeToPlanet = velocityMoon.minus(velocityPlanet);
        }
        else {
            // Position and velocity of other moons are relative to planet
            positionRelativeToPlanet = new Vector3D(positionMoon);
            velocityRelativeToPlanet = new Vector3D(velocityMoon);
            positionMoon.addVector(positionPlanet);
            velocityMoon.addVector(velocityPlanet);
        }
        orbit = EphemerisUtil.computeOrbit(muPlanet,
                positionRelativeToPlanet,velocityRelativeToPlanet);


        // Add the new moon to the Solar System for computation
        SolarSystemBody planet = this.getBody(planetName);
        this.moons.put(moonName, 
                new SolarSystemBody(moonName, positionMoon, velocityMoon, orbit, diameter, planet));
        
        // Add the new moon as particle for simulation
        if ("Moon".equals(moonName)) {
            // Earth's moon may apply forces to other particles
            this.addParticle(moonName, mass, mu, positionMoon, velocityMoon);
        }
        else {
            // Galilean moon
            galileanMoons.addParticle(moonName, mass, mu, positionRelativeToPlanet, velocityRelativeToPlanet);
            gmParticles.put(moonName, new Particle(mass, mu, positionMoon, velocityMoon));
        }

        // Define center body for this moon
        centerBodies.put(moonName, planetName);
    }

    /**
     * Create spacecraft, compute position and velocity, and add the spacecraft
     * as well as corresponding particle to the Solar System.
     * @param craft Reference to spacecraft
     */
    private void createSpacecraft(Spacecraft craft) {

        // Add the spacecraft to the Solar System for computation
        spacecraft.put(craft.getName(),craft);

        // Add the new spacecraft as particle without mass for simulation
        Vector3D position = craft.getPosition();
        Vector3D velocity = craft.getVelocity();
        this.addParticleWithoutMass(craft.getName(), position, velocity);
    }

    /**
     * Add spacecraft event. Retain order of date/time.
     * @param event
     */
    public void addSpacecraftEvent(SpacecraftEvent event) {
        int index = 0;
        while (index < spacecraftEvents.size()-1) {
            if (event.getDateTime().after(spacecraftEvents.get(index).getDateTime()) &&
                    event.getDateTime().before(spacecraftEvents.get(index+1).getDateTime())) {
                spacecraftEvents.add(index+1,event);
                return;
            }
            index++;
        }
        spacecraftEvents.add(event);
    }

    /**
     * Schedule next spacecraft event.
     * The first event that comes after the current simulation time will be scheduled.
     */
    private void scheduleNextSpacecraftEvent() {
        int index = 0;
        nextEvent = null;
        boolean ready = false;
        while (index < spacecraftEvents.size() && !ready) {
            SpacecraftEvent event = spacecraftEvents.get(index);
            if (event.getDateTime().after(simulationDateTime)) {
                nextEvent = event;
                ready = true;
            }
            index++;
        }
    }

    /**
     * Check whether a spacecraft event has occurred and perform event if one
     * has occurred.
     */
    private void checkForSpacecraftEvent() {
        // Check for spacecraft event
        if (nextEvent != null && !nextEvent.getDateTime().after(simulationDateTime)) {
            // Move spacecraft particle for scheduled spacecraft
            Spacecraft craft = spacecraft.get(nextEvent.getSpacecraftName());
            craft.updateStatus(simulationDateTime);
            Vector3D position = craft.getPosition();
            Vector3D velocity = craft.getVelocity();
            Particle particle = getParticle(craft.getName());
            if (particle != null) {
                particle.setPosition(position);
                particle.setVelocity(velocity);
            }
            scheduleNextSpacecraftEvent();
        }
    }

    /**
     * Get mass of particle with given name.
     * @param name
     * @return mass [kg]
     * @throws SolarSystemException when particle does not exist
     */
    public double getMass(String name) throws SolarSystemException {
        Particle particle = this.getParticle(name);
        if (particle != null) {
            return particle.getMass();
        }
        throw new SolarSystemException("Particle with name " + name + " does not exist");
    }

    /**
     * Set mass of particle with given name.
     * @param name
     * @param mass [kg]
     * @throws SolarSystemException when particle does not exist
     */
    public void setMass(String name, double mass) throws SolarSystemException {
        Particle particle = this.getParticle(name);
        if (particle != null) {
            particle.setMass(mass);
        }
        else {
            throw new SolarSystemException("Particle with name " + name + " does not exist");
        }
    }

    /**
     * Get standard gravitational parameter of particle with given name.
     * @param name
     * @return standard gravitational parameter [m3/s2]
     * @throws SolarSystemException when particle does not exist
     */
    public double getMu(String name) throws SolarSystemException {
        Particle particle = this.getParticle(name);
        if (particle != null) {
            return particle.getMu();
        }
        throw new SolarSystemException("Particle with name " + name + " does not exist");
    }

    /**
     * Get position of particle with given name.
     * Return (0,0,0) if no particle exists with given name.
     * @param name
     * @return position [m]
     * @throws SolarSystemException when particle does not exist
     */
    public Vector3D getPosition(String name) throws SolarSystemException {
        Particle particle = this.getParticle(name);
        if (particle != null) {
            return particle.getPosition();
        }
        throw new SolarSystemException("Particle with name " + name + " does not exist");
    }

    /**
     * Get velocity of particle with given name.
     * Return (0,0,0) if no particle exists with given name.
     * @param name
     * @return velocity [m/s]
     * @throws SolarSystemException when particle does not exist
     */
    public Vector3D getVelocity(String name) throws SolarSystemException {
        Particle particle = this.getParticle(name);
        if (particle != null) {
            return particle.getVelocity();
        }
        throw new SolarSystemException("Particle with name " + name + " does not exist");
    }

    /**
     * Set position and velocity of particle with given name.
     * @param name
     * @param position
     * @param velocity
     * @throws SolarSystemException when particle does not exist
     */
    public void setPositionVelocity(String name, Vector3D position, Vector3D velocity) throws SolarSystemException {
        Particle particle = super.getParticle(name);
        if (particle != null) {
            particle.setPosition(position);
            particle.setVelocity(velocity);
        }
        else {
            particle = galileanMoons.getParticle(name);
            Particle jupiter = this.getParticle("Jupiter");
            if (jupiter == null) {
                throw new SolarSystemException("Particle with name jupiter does not exist");
            }
            if (particle != null) {
                particle.setPosition(position.minus(jupiter.getPosition()));
                particle.setVelocity(velocity.minus(jupiter.getVelocity()));
            }
            particle = gmParticles.get(name);
            if (particle != null) {
                particle.setPosition(position);
                particle.setVelocity(velocity);
            }
        }
        if (particle == null) {
            throw new SolarSystemException("Particle with name " + name + " does not exist");
        }
    }

    /**
     * Correct for drift of entire particle system by adjusting
     * position and velocity of all particles.
     * In case the particle system contains a particle named "sun"
     * drift is corrected for by subtracting position and velocity of
     * the particle named "sun" for all particles, including "sun".
     * In case the system does not contain a particle named "sun"
     * drift is corrected for by subtracting position and velocity of
     * the center of mass of the particle system.
     */
    @Override
    public void correctDrift() {
        // Check whether solar system contains particle named "sun"
        Particle sun = getParticle("Sun");
        if (sun != null) {
            // Current position and velocity of the sun
            Vector3D positionSun = sun.getPosition();
            Vector3D velocitySun = sun.getVelocity();

            // Adjust position and velocity of all particles
            correctDrift(positionSun,velocitySun);
        }
        else {
            super.correctDrift();
        }
    }
}
