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

import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import util.Vector3D;

import java.io.Serializable;

/**
 * Represents the Galilean Moons of Jupiter.
 * @author Nico Kuijpers
 */
public class GalileanMoons extends ParticleSystem implements Serializable {

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     * Create planet Jupiter at origin of the particle system.
     */
    public GalileanMoons() {
        double mass = SolarSystemParameters.getInstance().getMass("Jupiter");
        double mu = SolarSystemParameters.getInstance().getMu("Jupiter");
        this.addParticle("Jupiter", mass, mu, new Vector3D(), new Vector3D());
    }

    @Override
    public void correctDrift() {
        Particle jupiter = getParticle("Jupiter");
        if (jupiter != null) {
            // Current position and velocity of the Sun
            Vector3D positionJupiter = jupiter.getPosition();
            Vector3D velocityJupiter = jupiter.getVelocity();

            // Adjust position and velocity of all particles
            correctDrift(positionJupiter, velocityJupiter);
        }
    }
}
