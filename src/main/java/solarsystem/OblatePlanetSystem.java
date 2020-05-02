/*
 * Copyright (c) 2019 Nico Kuijpers and Marco Brassé
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

import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import util.Vector3D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Represents a planet system with an oblate planet.
 * @author Nico Kuijpers and Marco Brassé
 */
public class OblatePlanetSystem extends ParticleSystem implements Serializable {

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Solar System parameters
    private static final SolarSystemParameters solarSystemParameters = SolarSystemParameters.getInstance();

    // Name of the planet
    private String planetName;

    // Particles from Solar System
    private List<Particle> solarSystemParticles;
    
    /**
     * Constructor.
     * Create planet at origin of the particle system.
     * @param planetSystemName name of the planet system
     * @param solarSystem reference to the Solar System
     */
    public OblatePlanetSystem(String planetSystemName, SolarSystem solarSystem) {

        // Set name of the planet
        this.planetName = planetSystemName;

        // Particles from Solar System (except central planet)
        solarSystemParticles = new ArrayList<>();
        solarSystemParticles.add(solarSystem.getParticle("Sun"));
        solarSystemParticles.add(solarSystem.getParticle("Jupiter"));
        solarSystemParticles.add(solarSystem.getParticle("Saturn"));
        solarSystemParticles.add(solarSystem.getParticle("Uranus"));
        solarSystemParticles.add(solarSystem.getParticle("Neptune"));
        solarSystemParticles.remove(solarSystem.getParticle(planetName));

        // Create central planet
        double massPlanet = solarSystemParameters.getMass(planetName);
        double muPlanet = solarSystemParameters.getMu(planetName);
        GregorianCalendar dateTime = solarSystem.getSimulationDateTime();
        OblatePlanet planet = new OblatePlanet(planetName, dateTime,
                massPlanet, muPlanet, new Vector3D(), new Vector3D());
        this.addParticle(planetName, planet);
    }
    
    @Override
    public void setGeneralRelativityFlag(boolean flag) {
        // Do not apply General Relativity for oblate planet system
        super.setGeneralRelativityFlag(false);
    }

    @Override
    protected void computeAcceleration() 
    {
        // Compute acceleration using Newton mechanics
        // Include the Sun and large planets from the Solar System
        // Note that General Relativity is not applied within oblate planet system
        List<Particle> tempParticles = new ArrayList<>(particles.values());
        tempParticles.addAll(solarSystemParticles);
        for (Particle p : particles.values()) {
            p.computeAccelerationNewtonMechanics(tempParticles);
        }
    }

    @Override
    public void correctDrift() {
        // Move planet to the origin of this particle system
        Particle planet = getParticle(planetName);
        if (planet != null) {
            // Current position and velocity of the planet
            Vector3D positionPlanet = planet.getPosition();
            Vector3D velocityPlanet = planet.getVelocity();

            // Adjust position and velocity of all particles
            correctDrift(positionPlanet, velocityPlanet);
        }
    }
}




