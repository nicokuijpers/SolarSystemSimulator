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
import ephemeris.*;
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

    /**
     * Default mass [kg] for particle without mass.
     */
    private final double DEFAULTMASS = 1.0;

    // Ephemeris for the Solar System
    private static final IEphemeris ephemeris = EphemerisSolarSystem.getInstance();

    // Solar System parameters
    private static final SolarSystemParameters solarSystemParameters = SolarSystemParameters.getInstance();

    // The Sun
    private SolarSystemBody sun;

    // The Earth-Moon Barycenter
    private Particle earthMoonBarycenter;

    // Planets of the Solar System
    private Map<String, SolarSystemBody> planets;
    
    // Moons of the Solar System
    private Map<String, SolarSystemBody> moons;

    // Particles for moons of planet systems
    private Map<String, Particle> moonParticles;
    
     // Spacecraft in the Solar System
    private Map<String, Spacecraft> spacecraft;
    
    // Center bodies of moons and spacecraft
    private Map<String,String> centerBodies;

    // Planet systems
    private Map<String,ParticleSystem> planetSystems;
    
    // Simulation date/time
    private GregorianCalendar simulationDateTime;
    
    // Simulation time step 60 min
    // General Relativity: Runge-Kutta scheme with time step 60 min
    // Newton Mechanics: Adams-Bashforth-Moulton scheme with time step 30 min
    private final double deltaT = 3600.0;
    private final double deltaTABM4 = deltaT/2;

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
        simulationDateTime.set(Calendar.SECOND, dateTime.get(Calendar.SECOND));
        simulationDateTime.set(Calendar.MILLISECOND, 0);
        
        // Initialize hash maps for planets and moons
        planets = new HashMap<>();
        moons = new HashMap<>();
        moonParticles = new HashMap<>();
        spacecraft = new HashMap<>();
        centerBodies = new HashMap<>();
        
        // Create the Sun
        Vector3D positionSun = new Vector3D(); // Origin
        Vector3D velocitySun = new Vector3D(); // Zero velocity
        double massSun = solarSystemParameters.getMass("Sun");
        double muSun   = solarSystemParameters.getMu("Sun");
        double diameterSun = solarSystemParameters.getDiameter("Sun");
        sun = new SolarSystemBody("Sun", positionSun, velocitySun, null, diameterSun, null);
        Particle sunParticle = new Particle(massSun, muSun, positionSun, velocitySun);
        this.addParticle("Sun", sunParticle);

        // Create the planets
        List<String> planetNames = solarSystemParameters.getPlanets();
        for (String name : planetNames) {
            double mass = solarSystemParameters.getMass(name);
            double mu = solarSystemParameters.getMu(name);
            double diameter = solarSystemParameters.getDiameter(name);
            createPlanet(name, mass, mu, diameter, simulationDateTime);
        }

        // Create the Moon
        String moonName = "Moon";
        String planetName = solarSystemParameters.getPlanetOfMoon(moonName);
        double mass = solarSystemParameters.getMass(moonName);
        double mu = solarSystemParameters.getMu(moonName);
        double diameter = solarSystemParameters.getDiameter(moonName);
        createMoon(planetName, moonName, mass, mu, diameter, simulationDateTime);

        // Create the Earth-Moon Barycenter as particle without mass
        createPlanet("EarthMoonBarycenter",1.0,1.0,1.0, dateTime);

        // Create storage for the oblate planet systems
        planetSystems = new HashMap<>();

        // Spacecraft events
        spacecraftEvents = new LinkedList<>();
        nextEvent = null;

        // Four-step Adams-Bashforth-Moulton method.
        // Reset flag to indicate that values stored in cyclic arrays are not valid.
        setValidABM4(false);
    }

    /**
     * Set/reset flag to apply general relativity when computing
     * acceleration.
     * @param flag flag
     */
    @Override
    public void setGeneralRelativityFlag(boolean flag) {
        super.setGeneralRelativityFlag(flag);
        for (ParticleSystem planetSystem : planetSystems.values()) {
            planetSystem.setGeneralRelativityFlag(flag);
        }
        setValidABM4(false);
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
        if ("EarthMoonBarycenter".equals(name)) {
            return earthMoonBarycenter;
        }
        String planetName = centerBodies.get(name);
        ParticleSystem planetSystem = planetSystems.get(planetName);
        if (planetSystem != null) {
            particle = planetSystem.getParticle(name);
            if (particle != null) {
                Particle planet = super.getParticle(planetName);
                Vector3D position = planet.getPosition().plus(particle.getPosition());
                Vector3D velocity = planet.getVelocity().plus(particle.getVelocity());
                moonParticles.get(name).setPosition(position);
                moonParticles.get(name).setVelocity(velocity);
                return moonParticles.get(name);
            }
        }
        return null;
    }

    /**
     * Get current simulation date/time.
     * @return current simulation date/time
     */
    public GregorianCalendar getSimulationDateTime() {
        GregorianCalendar gc = (GregorianCalendar) simulationDateTime.clone();
        gc.setTimeZone(TimeZone.getTimeZone("UTC"));
        return gc;
    }

    /**
     * Create planet system for planet with given name.
     * @param planetName name of the planet
     * @throws SolarSystemException
     */
    public void createPlanetSystem(String planetName) throws SolarSystemException {

        // Check whether simulation date/time is valid
        if (simulationDateTime.before(ephemeris.getFirstValidDate())) {
            throw new SolarSystemException("Date not valid before 3000 BC for " + planetName + " System");
        }
        if (simulationDateTime.after(ephemeris.getLastValidDate())) {
            throw new SolarSystemException("Date not valid after AD 3000 for " + planetName + " System");
        }

        // Check whether planet system already exists
        if (!planetSystems.containsKey(planetName)) {
            // Create the planet system
            ParticleSystem planetSystem;
            if ("Pluto System".equals(planetName)) {
                //if ("Mars".equals(planetName) || "Pluto System".equals(planetName)) {
                planetSystem = new ParticleSystem();
                // DEBUG
                /*
                if ("Mars".equals(planetName)) {
                    double massPlanet = solarSystemParameters.getMass(planetName);
                    double muPlanet = solarSystemParameters.getMu(planetName);
                    Particle planet = new Particle(massPlanet, muPlanet, new Vector3D(), new Vector3D());
                    planetSystem.addParticle("Mars Extra", planet);
                }
                 */
            }
            else {
                planetSystem = new OblatePlanetSystem(planetName, this);
            }

            // Set flag to indicate whether general relativity
            // should be applied when computing acceleration
            planetSystem.setGeneralRelativityFlag(getGeneralRelativityFlag());

            // Store reference to this planet system
            planetSystems.put(planetName, planetSystem);

            // Create the moons for this planet system
            for (String moonName : solarSystemParameters.getMoonsOfPlanet(planetName)) {
                double mass = solarSystemParameters.getMass(moonName);
                double mu = solarSystemParameters.getMu(moonName);
                double diameter = solarSystemParameters.getDiameter(moonName);
                createMoon(planetName, moonName, mass, mu, diameter, simulationDateTime);
            }
        }
    }

    /**
     * Remove planet system for planet with given name
     * @param planetName name of the planet
     */
    public void removePlanetSystem(String planetName) {
        if (planetSystems.containsKey(planetName)) {
            // Transfer spacecraft to Solar System
            for (String spacecraftName : spacecraft.keySet()) {
                if (centerBodies.containsKey(spacecraftName) &&
                        planetName.equals(centerBodies.get(spacecraftName))) {
                    transferSpacecraftToSolarSystem(spacecraftName);
                }
            }

            // Remove the moons for this planet system
            for (String moonName : solarSystemParameters.getMoonsOfPlanet(planetName)) {
                removeMoon(moonName);
            }
            // Remove the planet system
            planetSystems.remove(planetName);
        }
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
        simulationDateTime.set(Calendar.SECOND, dateTime.get(Calendar.SECOND));
        simulationDateTime.set(Calendar.MILLISECOND, 0);

        // Initialize trajectories of spacecraft
        for (String spacecraftName : spacecraft.keySet()) {
            this.getBody(spacecraftName).initTrajectory();
        }

        // Compute new positions and orbits for all bodies
        // corresponding to current simulation date/time
        moveBodies();

        // Move corresponding particles to positions
        // corresponding to current simulation date/time
        moveBodyParticles();

        // Schedule next spacecraft event
        scheduleNextSpacecraftEvent();

        // Four-step Adams-Bashforth-Moulton method.
        // Reset flag to indicate that values stored in cyclic arrays are not valid.
        setValidABM4(false);
    }
            
    /**
     * Advance simulation of planet systems with time steps
     * of at most 10 minutes.
     * @param deltaT simulation time step [s]
     */
    private void advancePlanetSystems(double deltaT) {
        // Time step for planet systems is at most 10 minutes
        double timeStep = Math.min(Math.abs(deltaT), 600.0);

        // Position planet systems relative to the Sun
        for (String planetName : planetSystems.keySet()) {
            Particle planet = this.getParticle(planetName);
            Vector3D driftPosition = planet.getPosition();
            Vector3D driftVelocity = planet.getVelocity();
            ParticleSystem planetSystem = planetSystems.get(planetName);
            planetSystem.correctDrift(driftPosition, driftVelocity);
        }

        // Advance planet systems using Runge-Kutta method
        double totalTime = 0.0;
        if (deltaT < 0.0) {
            while (totalTime > deltaT) {
                for (ParticleSystem planetSystem : planetSystems.values()) {
                    planetSystem.advanceRungeKutta(-timeStep);
                }
                totalTime -= timeStep;
            }
        }
        else {
            while (totalTime < deltaT) {
                for (ParticleSystem planetSystem : planetSystems.values()) {
                    planetSystem.advanceRungeKutta(timeStep);
                }
                totalTime += timeStep;
            }
        }

        // Position planet systems such that planet is at the origin
        for (ParticleSystem planetSystem : planetSystems.values()) {
            planetSystem.correctDrift();
        }
    }

    /**
     * Advance forward in time for given number of simulation time steps.
     * @param nrTimeSteps number of time steps
     */
    public void advanceSimulationForward(int nrTimeSteps) {
        for (int i = 0; i < nrTimeSteps; i++) {
            advancePlanetSystems(deltaT);
            if (getGeneralRelativityFlag()) {
                // Runge-Kutta for General Relativity
                advanceRungeKutta(deltaT);
            }
            else {
                // Two times Adams-Bashforth-Moulton for Newton Mechanics
                advanceABM4(deltaTABM4);
                advanceABM4(deltaTABM4);
            }
            correctDrift();
            updateEarthMoonBarycenter();
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
            advancePlanetSystems(-deltaT);
            if (getGeneralRelativityFlag()) {
                // Runge-Kutta for General Relativity
                advanceRungeKutta(-deltaT);
            }
            else {
                // Two times Adams-Bashforth-Moulton for Newton Mechanics
                advanceABM4(-deltaTABM4);
                advanceABM4(-deltaTABM4);
            }
            correctDrift();
            updateEarthMoonBarycenter();
            simulationDateTime.add(Calendar.SECOND, (int) -deltaT);
        }
    }

    /**
     * Advance a single time step forward or backward.
     * Note that the time step should not exceed 1 hour.
     * @param timeStep time step in seconds
     */
    public void advanceSimulationSingleStep(double timeStep) {
        // Advance using Runge-Kutta scheme
        setValidABM4(false);
        timeStep = Math.min(timeStep,3600.0);
        timeStep = Math.max(timeStep,-3600.0);
        advancePlanetSystems(timeStep);
        advanceRungeKutta(timeStep);
        correctDrift();
        updateEarthMoonBarycenter();
        int millisecond = (int) Math.floor(1000.0*timeStep);
        simulationDateTime.add(Calendar.MILLISECOND, millisecond);
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
            double muPlanet;
            if ("Pluto System".equals(planetName)) {
                /*
                 * In the Pluto System, all particles orbit around the barycenter which is
                 * located in between Pluto and Charon, outside of Pluto.
                 * To visualise the orbit of a particle of the Pluto System, the gravitational
                 * parameter is estimated under the assumption that the orbit is circular.
                 * For circular orbits, the gravitational parameter mu can be obtained from
                 * v = sqrt(mu/r),
                 * where v is the velocity relative to the barycenter and r the distance to
                 * the barycenter
                 * https://en.wikipedia.org/wiki/Circular_orbit
                 */
                double v = velocityMoon.magnitude();
                double r = positionMoon.magnitude();
                muPlanet = v * v * r;
            } else {
                muPlanet = this.getParticle(planetName).getMu();
            }
            Vector3D[] orbit;
            if ("Moon".equals(name)) {
                // Position and velocity of the Moon are relative to the Sun
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
     * Move planet particle with given name to position corresponding to simulation date/time.
     * @param planetName
     */
    private void movePlanetParticle(String planetName) {
        Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(planetName, simulationDateTime);
        Vector3D position = positionAndVelocity[0];
        Vector3D velocity = positionAndVelocity[1];
        Particle particle = getParticle(planetName);
        if (particle != null) {
            particle.setPosition(position);
            particle.setVelocity(velocity);
        }
    }

    /**
     * Move moon particle with given name to position corresponding to simulation date/time.
     * @param moonName
     */
    private void moveMoonParticle(String moonName) {
        Vector3D[] positionAndVelocityMoon;
        if ("Triton".equals(moonName)) {
            // Use accurate ephemeris to initialize position and velocity for Triton
            EphemerisTriton ephemerisTriton = (EphemerisTriton) EphemerisTriton.getInstance();
            positionAndVelocityMoon =
                    ephemerisTriton.getBodyPositionVelocityTritonForInitialization(simulationDateTime);
        }
        else {
            positionAndVelocityMoon = ephemeris.getBodyPositionVelocity(moonName, simulationDateTime);
        }
        Vector3D positionMoon = positionAndVelocityMoon[0];
        Vector3D velocityMoon = positionAndVelocityMoon[1];

        if ("Moon".equals(moonName)) {
            // Position and velocity of Earth's moon is relative to the Sun
            Particle particleMoon = this.getParticle(moonName);
            if (particleMoon != null) {
                particleMoon.setPosition(positionMoon);
                particleMoon.setVelocity(velocityMoon);
            }
        }
        else {
            // Position and velocity of other moons are relative to their planet
            String planetName = centerBodies.get(moonName);
            ParticleSystem planetSystem = planetSystems.get(planetName);
            Particle particleMoon = planetSystem.getParticle(moonName);
            if (particleMoon != null) {
                particleMoon.setPosition(positionMoon);
                particleMoon.setVelocity(velocityMoon);
            }
        }
    }

    /**
     * Move spacecraft particle with given name to position corresponding to simulation date/time.
     * @param spacecraftName
     */
    private void moveSpacecraftParticle(String spacecraftName) {
        Spacecraft craft = spacecraft.get(spacecraftName);
        craft.updateStatus(simulationDateTime);
        Vector3D position = craft.getPosition();
        Vector3D velocity = craft.getVelocity();
        try {
            this.setPositionVelocity(craft.getName(),position,velocity);
        } catch (SolarSystemException e) {
            e.printStackTrace();
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
        for (String planetName : planets.keySet()) {
           movePlanetParticle(planetName);
        }
        
        // Move each moon particle to position of present date
        for (String moonName : moons.keySet()) {
            moveMoonParticle(moonName);
        }

        // Move each spacecraft particle to position of present date
        for (String spacecraftName : spacecraft.keySet()) {
            moveSpacecraftParticle(spacecraftName);
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

        // Create particle for simulation
        if ("EarthMoonBarycenter".equals(name)) {
            // Earth-Moon Barycenter is not a particle for simulation
            earthMoonBarycenter = new Particle(mass, mu, position, velocity);
        } else {
            // Add the new planet as particle for simulation
            if (mass >= solarSystemParameters.getMass("Pluto")) {
                // Planet with mass at least mass of Pluto may apply force to other objects
                // Note that Pluto and Eris both may apply forces to other objects
                if ("Earth".equals(name)) {
                    // Use oblateness to compute acceleration of Earth's Moon and nearby spacecraft
                    OblatePlanet planet =
                            new OblatePlanet(name, simulationDateTime, mass, mu, position, velocity);
                    this.addParticle(name, planet);
                } else {
                    // Planet is represented as a point-mass
                    Particle planet = new Particle(mass, mu, position, velocity);
                    this.addParticle(name, planet);
                }
            } else {
                // Planet with mass smaller than the mass of Pluto cannot apply force to other objects
                Particle planet = new Particle(mass, mu, position, velocity);
                this.addParticleWithoutMass(name, planet);
            }
        }
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

        Vector3D[] positionAndVelocityMoon;
        if ("Triton".equals(moonName)) {
            // Use accurate ephemeris to initialize position and velocity for Triton
            EphemerisTriton ephemerisTriton = (EphemerisTriton) EphemerisTriton.getInstance();
            positionAndVelocityMoon =
                    ephemerisTriton.getBodyPositionVelocityTritonForInitialization(date);
        }
        else {
            positionAndVelocityMoon = ephemeris.getBodyPositionVelocity(moonName, date);
        }
        Vector3D positionMoon = positionAndVelocityMoon[0];
        Vector3D velocityMoon = positionAndVelocityMoon[1];

        // Obtain position and velocity of planet from Ephemeris
        Vector3D[] positionAndVelocityPlanet = ephemeris.getBodyPositionVelocity(planetName, date);
        Vector3D positionPlanet = positionAndVelocityPlanet[0];
        Vector3D velocityPlanet = positionAndVelocityPlanet[1];
        
        // Compute orbit of moon relative to planet
        Vector3D positionRelativeToPlanet;
        Vector3D velocityRelativeToPlanet;
        double muPlanet;
        if ("Pluto System".equals(planetName)) {
            /*
             * In the Pluto System, all particles orbit around the barycenter which is
             * located in between Pluto and Charon, outside of Pluto.
             * To visualise the orbit of a particle of the Pluto System, the gravitational
             * parameter is estimated under the assumption that the orbit is circular.
             * For circular orbits, the gravitational parameter mu can be obtained from
             * v = sqrt(mu/r),
             * where v is the velocity relative to the barycenter and r the distance to
             * the barycenter
             * https://en.wikipedia.org/wiki/Circular_orbit
             */
            double v = velocityMoon.magnitude();
            double r = positionMoon.magnitude();
            muPlanet = v * v * r;
        } else {
            muPlanet = this.getParticle(planetName).getMu();
        }
        Vector3D[] orbit;
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
            // Earth's moon applies forces to all other particles in the Solar System
            Particle moon = new Particle(mass, mu, positionMoon, velocityMoon);
            this.addParticle(moonName, moon);
        }
        else {
            // Other moons apply forces only to other moons of their planet
            ParticleSystem planetSystem = planetSystems.get(planetName);
            Particle moon = new Particle(mass, mu, positionRelativeToPlanet, velocityRelativeToPlanet);
            planetSystem.addParticle(moonName, moon);
            moonParticles.put(moonName, new Particle(mass, mu, positionMoon, velocityMoon));
        }

        // Define center body for this moon
        centerBodies.put(moonName, planetName);
    }

    /**
     * Remove moon from the Solar System.
     * @param moonName
     */
    private void removeMoon(String moonName) {
        moons.remove(moonName);
        moonParticles.remove(moonName);
        centerBodies.remove(moonName);
    }

    /**
     * Create spacecraft with given name, compute position and velocity,
     * and add the spacecraft as well as corresponding particle to the Solar System.
     * @param spacecraftName
     */
    public void createSpacecraft(String spacecraftName) {

        Spacecraft craft;
        switch(spacecraftName) {
            case "Apollo 8":
                craft = new ApolloEight("Apollo 8", "Earth", this);
                break;
            case "Pioneer 10":
                craft = new PioneerTen("Pioneer 10", "Sun", this);
                break;
            case "Pioneer 11":
                craft = new PioneerEleven("Pioneer 11", "Sun", this);
                break;
            case "Mariner 10":
                craft = new MarinerTen("Mariner 10", "Sun", this);
                break;
            case "Voyager 2":
                craft = new VoyagerTwo("Voyager 2", "Sun", this);
                break;
            case "Voyager 1":
                craft = new VoyagerOne("Voyager 1", "Sun", this);
                break;
            case "ISS":
                craft = new ISS("ISS", "Earth", this);
                break;
            case "Rosetta":
                craft = new Rosetta("Rosetta", "Sun", this);
                break;
            case "New Horizons":
                craft = new NewHorizons("New Horizons", "Sun", this);
                break;
            case "Cassini":
                craft = new Cassini("Cassini", "Sun", this);
                break;
            case "Galileo":
                craft = new Galileo("Galileo", "Sun", this);
                break;
            case "Giotto":
                craft = new Giotto("Giotto", "Sun", this);
                break;
            default:
                System.err.println("ERROR: No spacecraft with name " + spacecraftName);
                craft = null;
                break;
        }

        if (craft != null) {
            // Add the spacecraft to the Solar System for computation
            spacecraft.put(craft.getName(), craft);

            // Add the new spacecraft as particle without mass for simulation
            Vector3D position = craft.getPosition();
            Vector3D velocity = craft.getVelocity();
            Particle particle = new Particle(DEFAULTMASS, position, velocity);
            this.addParticleWithoutMass(craft.getName(), particle);
        }
    }

    /**
     * Remove spacecraft with given name and corresponding particle.
     * Also the events belonging to that spacecraft are removed.
     * @param spacecraftName
     */
    public void removeSpacecraft(String spacecraftName) {
        if (spacecraft.containsKey(spacecraftName)) {
            // Remove events for this spacecraft
            List<SpacecraftEvent> eventsToBeRemoved = new ArrayList<>();
            for (SpacecraftEvent event : spacecraftEvents) {
                if (event.getSpacecraftName().equals(spacecraftName)) {
                    eventsToBeRemoved.add(event);
                }
            }
            for (SpacecraftEvent event : eventsToBeRemoved) {
                spacecraftEvents.remove(event);
            }
            // Remove spacecraft from planet system and/or Solar System
            if (centerBodies.containsKey(spacecraftName)) {
                transferSpacecraftToSolarSystem(spacecraftName);
            }
            this.removeParticle(spacecraftName);
            spacecraft.remove(spacecraftName);
        }
    }

    /**
     * Add spacecraft event. Retain order of date/time.
     * @param event
     */
    public void addSpacecraftEvent(SpacecraftEvent event) {
        if (!spacecraftEvents.isEmpty() && event.getDateTime().before(spacecraftEvents.get(0).getDateTime())) {
            spacecraftEvents.add(0,event);
            return;
        }
        int index = 0;
        while (index < spacecraftEvents.size() - 1) {
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
     * Check whether a spacecraft event has occurred and update position and
     * velocity of corresponding particle when event has occurred. Spacecraft events
     * are also used to update position and velocity of other objects to increase
     * accuracy during flybys of small objects such as comets, asteroids or Kuiper belt
     * objects.
     */
    private void checkForSpacecraftEvent() {
        if (nextEvent != null && !nextEvent.getDateTime().after(simulationDateTime)) {
            for (String name : nextEvent.getBodyNames()) {
                if (spacecraft.containsKey(name)) {
                    // Transfer Cassini to Saturn System at or after Saturn Orbit Insertion (01-Jul-2004 01:12:08)
                    if ("Cassini".equals(name)) {
                        if (!simulationDateTime.before(Cassini.getSaturnOrbitInsertion())) {
                            if (!planetSystems.containsKey("Saturn")) {
                                try {
                                    this.createPlanetSystem("Saturn");
                                } catch (SolarSystemException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!"Saturn".equals(centerBodies.get(name))) {
                                transferSpacecraftToPlanetSystem("Cassini", "Saturn");
                            }
                        }
                    }
                    // Transfer Galileo to Jupiter System at or after 1 dec 1995 (one week before Jupiter Orbit Insertion)
                    if ("Galileo".equals(name)) {
                        if (!simulationDateTime.before(Galileo.getWeekBeforeJupiterOrbitInsertion())) {
                            if (!planetSystems.containsKey("Jupiter")) {
                                try {
                                    this.createPlanetSystem("Jupiter");
                                } catch (SolarSystemException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!"Jupiter".equals(centerBodies.get(name))) {
                                transferSpacecraftToPlanetSystem("Galileo", "Jupiter");
                            }
                        }
                    }
                    moveSpacecraftParticle(name);
                } else {
                    if (planets.containsKey(name)) {
                        movePlanetParticle(name);
                    }
                    else {
                        if (moons.containsKey(name)) {
                            moveMoonParticle(name);
                        }
                    }
                }
            }
            scheduleNextSpacecraftEvent();
        }
    }

    /**
     * Transfer particle for spacecraft from Solar System to planet system of planet
     * with given name.
     * @param spacecraftName Name of spacecraft to be transferred
     * @param planetName     Name of planet in planet system
     */
    private void transferSpacecraftToPlanetSystem(String spacecraftName, String planetName) {
        if (centerBodies.containsKey(spacecraftName)) {
            if (!centerBodies.get(spacecraftName).equals(planetName)) {
                transferSpacecraftToSolarSystem(spacecraftName);
            }
        }
        else {
            Particle spacecraftParticle = getParticle(spacecraftName);
            Vector3D positionSpacecraft = spacecraftParticle.getPosition();
            Vector3D velocitySpacecraft = spacecraftParticle.getVelocity();
            Vector3D positionPlanet = getParticle(planetName).getPosition();
            Vector3D velocityPlanet = getParticle(planetName).getVelocity();
            Vector3D positionRelativeToPlanet = positionSpacecraft.minus(positionPlanet);
            Vector3D velocityRelativeToPlanet = velocitySpacecraft.minus(velocityPlanet);
            ParticleSystem planetSystem = planetSystems.get(planetName);
            Particle particle = new Particle(DEFAULTMASS, positionRelativeToPlanet, velocityRelativeToPlanet);
            planetSystem.addParticle(spacecraftName, particle);
            this.removeParticle(spacecraftName);
            moonParticles.put(spacecraftName, new Particle(DEFAULTMASS, positionSpacecraft, velocitySpacecraft));
            centerBodies.put(spacecraftName, planetName);
        }
    }

    /**
     * Transfer particle for spacecraft from planet system of planet with given name
     * to Solar System.
     * @param spacecraftName Name of spacecraft to be transferred
     */
    private void transferSpacecraftToSolarSystem(String spacecraftName) {
        if (centerBodies.containsKey(spacecraftName)) {
            Particle spacecraftParticle = getParticle(spacecraftName);
            Vector3D position = spacecraftParticle.getPosition();
            Vector3D velocity = spacecraftParticle.getVelocity();
            Particle particle = new Particle(DEFAULTMASS, position, velocity);
            this.addParticleWithoutMass(spacecraftName, particle);
            String planetName = centerBodies.get(spacecraftName);
            ParticleSystem planetSystem = planetSystems.get(planetName);
            planetSystem.removeParticle(spacecraftName);
            moonParticles.remove(spacecraftName);
            centerBodies.remove(spacecraftName);
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

        // Set mass for planet in planet system
        if (planetSystems.keySet().contains(name)) {
            ParticleSystem planetSystem = planetSystems.get(name);
            planetSystem.getParticle(name).setMass(mass);
        }

        // Set mass for moon in planet system
        if (centerBodies.keySet().contains(name)) {
            String planetName = centerBodies.get(name);
            if (planetSystems.keySet().contains(planetName)) {
                ParticleSystem planetSystem = planetSystems.get(planetName);
                planetSystem.getParticle(name).setMass(mass);
            }
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
            String planetName = centerBodies.get(name);
            if (planetSystems.keySet().contains(planetName)) {
                ParticleSystem planetSystem = planetSystems.get(planetName);
                particle = planetSystem.getParticle(name);
                Particle planet = this.getParticle(planetName);
                if (planet == null) {
                    throw new SolarSystemException("Particle with name " + planetName + " does not exist");
                }
                if (particle != null) {
                    particle.setPosition(position.minus(planet.getPosition()));
                    particle.setVelocity(velocity.minus(planet.getVelocity()));
                }
            }
            particle = moonParticles.get(name);
            if (particle != null) {
                particle.setPosition(position);
                particle.setVelocity(velocity);
            }
            else {
                throw new SolarSystemException("Particle with name " + name + " does not exist");
            }
        }
    }

    /**
     * Correct for drift of entire particle system by adjusting
     * position and velocity of all particles.
     * In case the particle system contains a particle named "Sun"
     * drift is corrected for by subtracting position and velocity of
     * the particle named "sun" for all particles, including "Sun".
     * In case the system does not contain a particle named "Sun"
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

    /**
     * Update position and velocity of Earth-Moon Barycenter.
     */
    private void updateEarthMoonBarycenter() {
        Particle earth = getParticle("Earth");
        Particle moon = getParticle("Moon");
        Vector3D positionBarycenter = new Vector3D();
        Vector3D velocityBarycenter = new Vector3D();
        positionBarycenter.addVector(earth.getPosition().scalarProduct(earth.getMu()));
        velocityBarycenter.addVector(earth.getVelocity().scalarProduct(earth.getMu()));
        positionBarycenter.addVector(moon.getPosition().scalarProduct(moon.getMu()));
        velocityBarycenter.addVector(moon.getVelocity().scalarProduct(moon.getMu()));
        double totalMu = earth.getMu() + moon.getMu();
        earthMoonBarycenter.setPosition(positionBarycenter.scalarProduct(1.0 / totalMu));
        earthMoonBarycenter.setVelocity(velocityBarycenter.scalarProduct(1.0 / totalMu));
    }
}
