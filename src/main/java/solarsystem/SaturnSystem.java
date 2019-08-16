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

import particlesystem.Particle;
import particlesystem.ParticleSystem;
import util.Vector3D;

import java.io.Serializable;

/**
 * Represents the Saturn system.
 * @author Nico Kuijpers
 */
public class SaturnSystem extends ParticleSystem implements Serializable {

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     * Create planet Saturn at origin of the particle system.
     */
    public SaturnSystem() {
        // double mass = SolarSystemParameters.getInstance().getMass("Saturn");
        // double mu = SolarSystemParameters.getInstance().getMu("Saturn");
        /*
            Adapt standard gravitational parameter mu of Saturn to obtain accurate results
            for Titan. An optimal value for mu was found by running SpacecraftExperiment
            for different values of mu and optimizing for minimum distance between Voyager 1
            and Titan.

            SATURNMU = 3.7940626061137281E16 m3/s2 (defined in SolarSystemParameters)

            Adapt mass of Saturn to correspond with standard gravitational parameter mu

            SATURNMASS = 568.34E24 kg (defined in SolarSystemParameters)
            GRAVITATIONALCONSTANT = 6.6740831E-11 m3 kg-1 s-2 (defined in Particle)
         */
        double mu = 3.7924955E16;
        double mass = mu/ Particle.GRAVITATIONALCONSTANT;
        this.addParticle("Saturn", mass, mu, new Vector3D(), new Vector3D());
    }

    @Override
    public void correctDrift() {
        Particle saturn = getParticle("Saturn");
        if (saturn != null) {
            // Current position and velocity of Saturn
            Vector3D positionSaturn = saturn.getPosition();
            Vector3D velocitySaturn = saturn.getVelocity();

            // Adjust position and velocity of all particles
            correctDrift(positionSaturn, velocitySaturn);
        }
    }
}
