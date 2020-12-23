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
package particlesystem;

import util.Vector3D;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a particle system.
 * @author Nico Kuijpers
 */
public class ParticleSystem implements Serializable {

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Four-step Adams-Bashfort-Moulton method
    private boolean validABM4 = false; // Flag to indicate whether cyclic arrays are valid
    private double deltaTABM4 = 0L;    // Store ABM4 time step in order to detect change
    private int nrValidABM4 = 0;       // Number of valid values in cyclic arrays
    private int indexABM4 = 0;         // Index in cyclic arrays

    /**
     * Flag to indicate whether general relativity
     * should be applied when computing acceleration.
     */
    private boolean generalRelativityFlag = false;
    
    /**
     * List of all particles.
     */
    protected Map<String, Particle> particles;

    /**
     * List of particles with mass.
     * These particles apply force to all other particles.
     */
    protected Map<String, Particle> particlesWithMass;

    /**
     * Default constructor.
     */
    public ParticleSystem() {
        particles = new HashMap<>();
        particlesWithMass = new HashMap<>();
    }

    /**
     * Set/reset flag to apply general relativity when computing
     * acceleration.
     * @param flag flag 
     */
    public void setGeneralRelativityFlag(boolean flag) {
        generalRelativityFlag = flag;
    }
    
    /**
     * Get value of flag to apply general relativity when computing
     * acceleration.
     * @return true when flag is set, false otherwise
     */
    public boolean getGeneralRelativityFlag() {
        return generalRelativityFlag;
    }

    /**
     * Add particle to particle system.
     * @param name     Name of particle
     * @param particle Particle
     */
    public final void addParticle(String name, Particle particle) {
        particles.put(name,particle);
        particlesWithMass.put(name,particle);
    }

    /**
     * Add particle without mass to particle system.
     * Particles without mass are used to simulate small Solar System objects and
     * spacecraft. They do not apply forces on other particles.
     * @param name     Name of particle
     * @param particle Particle
     */
    public final void addParticleWithoutMass(String name, Particle particle) {
        particles.put(name,particle);
    }

    /**
     * Add new particle to particle system when 
     * standard gravitational parameter is known.
     * @param name     Name of particle
     * @param mass     Mass of particle in kg
     * @param mu       Standard gravitational parameter in m3/s2
     * @param position Initial 3D position vector of particle in m
     * @param velocity Initial 3D velocity vector of particle in m/s
     */
    public final void addParticle(String name, double mass, double mu,
                                  Vector3D position, Vector3D velocity) {
        Particle particle = new Particle(mass,mu,position,velocity);
        particles.put(name,particle);
        particlesWithMass.put(name,particle);
    }

    /**
     * Get reference to particle with given name.
     * @param name    Name of particle
     * @return particle with given name
     */
    public Particle getParticle(String name) {
        return particles.get(name);
    }
     
    /**
     * Initialize state for leapfrog algorithm.
     * @param deltaT time step in s
     */
    public void initLeapfrog(double deltaT) {
        // Initialalize state for leapfrog algorithm
        computeAcceleration();
        for (Particle p : particles.values()) {
            p.initStateLeapfrog(deltaT);
        }
    }
    
    /**
     * Advance a time step using leapfrog algorithm.
     * @param deltaT time step in s
     */
    public void advanceLeapfrog(double deltaT) {
        // Use leapfrog algorithm
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        computeAcceleration();
        for (Particle p : particles.values()) {
            p.updateStateLeapfrog(deltaT);
        }
    }

    public void advanceRungeKutta(double deltaT) {
        // Use Runge-Kutta method
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        computeAcceleration();
        for (Particle p : particles.values()) {
            p.updateStateRungeKuttaA(deltaT);
        }
        computeAcceleration();
        for (Particle p : particles.values()) {            
            p.updateStateRungeKuttaB(deltaT);
        }
        computeAcceleration();
        for (Particle p : particles.values()) {
            p.updateStateRungeKuttaC(deltaT);
        }
        computeAcceleration();
        for (Particle p : particles.values()) {
            p.updateStateRungeKuttaD(deltaT);
        }
    }
    /**
     * Four-step Adams-Bashforth-Moulton method.
     * Set/reset flag to indicate whether values stored in cyclic arrays are valid.
     */
    protected void setValidABM4(boolean validABM4) {
        this.validABM4 = validABM4;
    }

    /**
     * Advance a time step using four-step Adams-Bashforth-Moulton method.
     * @param deltaT time step in s
     */
    public void advanceABM4(double deltaT) {
        // https://en.wikiversity.org/wiki/Adams-Bashforth_and_Adams-Moulton_methods
        // Initialize Adams-Bashforth-Moulton using Runge-Kutta method
        // Note that four initial values are needed to start calculation
        if (deltaT != deltaTABM4 || !validABM4) {
            deltaTABM4 = deltaT;
            nrValidABM4 = 0;
            indexABM4 = 3;
            setValidABM4(true);
        }
        if (nrValidABM4 < 4) {
            advanceRungeKutta(deltaT);
            computeAcceleration();
            indexABM4 = (indexABM4 + 1) % 4;
            for (Particle p : particles.values()) {
                p.storeVelocityAccelerationABM4(indexABM4);
            }
            nrValidABM4++;
        }
        else {
            // Adams-Bashforth-Moulton predictor step
            for (Particle p : particles.values()) {
                p.updateStateABM4Predictor(deltaT,indexABM4);
            }
            computeAcceleration();
            indexABM4 = (indexABM4 + 1) % 4;
            for (Particle p : particles.values()) {
                p.storeVelocityAccelerationABM4(indexABM4);
            }
            // Adams-Bashforth-Moulton corrector step
            for (Particle p : particles.values()) {
                p.updateStateABM4Corrector(deltaT,indexABM4);
            }
        }
    }

    /**
     * Compute acceleration using Newton Mechanics for all particles.
     */
    protected void computeAcceleration() {
        // Compute acceleration using Newton mechanics
        for (Particle p : particles.values()) {
            p.computeAccelerationNewtonMechanics(particlesWithMass.values());
        }
        // Compute acceleration using General Relativity
        if (generalRelativityFlag) {
            // Note that the acceleration computed by Newton mechanics
            // is used to compute acceleration using General Relativity
            for (Particle p : particles.values()) {
                p.computeAccelerationGeneralRelativity(particlesWithMass.values());
            }
        }
    }

    /**
     * Correct for drift of entire particle system by adjusting
     * position and velocity of all particles.
     * Drift is corrected for by subtracting position and velocity of
     * the center of mass of the particle system.
     */
    public void correctDrift() {
        // Determine position and velocity of the center of mass
        // Use gravitational parameter mu instead of mass for better accuracy
        Vector3D positionCenterMass = new Vector3D();
        Vector3D velocityCenterMass = new Vector3D();
        double totalMu = 0.0;
        for (Particle p : particles.values()) {
            positionCenterMass.addVector(p.getPosition().scalarProduct(p.getMu()));
            velocityCenterMass.addVector(p.getVelocity().scalarProduct(p.getMu()));
            totalMu += p.getMu();
        }
        if (totalMu != 0.0) {
            positionCenterMass = positionCenterMass.scalarProduct(1.0 / totalMu);
            velocityCenterMass = velocityCenterMass.scalarProduct(1.0 / totalMu);
        }
            
        // Adjust position and velocity of all particles
        correctDrift(positionCenterMass,velocityCenterMass);
    }
    
    /**
     * Correct for drift of the particle system by adjusting
     * position and velocity of all particles.
     * @param driftPosition  drift in position to correct for
     * @param driftVelocity  drift in velocity to correct for
     */
    public void correctDrift(Vector3D driftPosition, Vector3D driftVelocity) {
        for (Particle p : particles.values()) {
            p.correctDrift(driftPosition,driftVelocity);
        }
    }

    /**
     * Compute total kinetic energy of the particle system.
     * @return total kinetic energy in J
     */
    private double computeKineticEnergy() {
        double kineticEnergy = 0.0;
        for (Particle p : particles.values()) {
            kineticEnergy += p.getKineticEnergy();
        }
        return kineticEnergy;
    }
    
    /**
     * Compute total potential energy of the particle system.
     * @return total potential energy in J
     */
    private double computePotentialEnergy() {
        double potentialEnergy = 0.0;
        for (Particle p : particles.values()) {
            potentialEnergy += p.getPotentialEnergy();
        }
        return potentialEnergy;
    }
}
