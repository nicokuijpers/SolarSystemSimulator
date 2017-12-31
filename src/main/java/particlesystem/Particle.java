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
package particlesystem;

import java.io.Serializable;
import java.util.Collection;
import util.Vector3D;

/**
 * Represents a single particle of a particle system.
 * @author Nico Kuijpers
 */
public class Particle implements Serializable {
    
    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    /**
     * Gravitational constant G = 6.67408(31) x 10^-11 m3 kg-1 s-2
     */
    //  https://en.wikipedia.org/wiki/Gravitational_constant
    public static final double GRAVITATIONALCONSTANT = 6.6740831E-11;
    
    /**
     * Light speed c = 299792458.0 m/s
     */
    // https://simple.wikipedia.org/wiki/Speed_of_light
    // https://ipnpr.jpl.nasa.gov/progress_report/42-196/196C.pdf
    // Page 47, Table 4
    public static final double LIGHTSPEED = 299792458.0;
    public static final double LIGHTSPEEDSQUARE = 8987551787368176400.0;
    
    private double mass;
    private double mu;
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D acceleration;
    private Vector3D accelerationNewtonMechanics;
    private double potentialEnergy;
    
    // Runge-Kutta method
    Vector3D formerPosition, k1, k2, k3, k4;
    Vector3D formerVelocity, l1, l2, l3, l4;
    
    /**
     * Constructor when standard gravitational parameter is not known.
     * @param mass      mass in kg
     * @param position  position in m
     * @param velocity  velocity in m/s
     */
    public Particle(double mass, Vector3D position, Vector3D velocity) {
        this.mass = mass;
        this.mu = GRAVITATIONALCONSTANT * mass;
        this.position = position;
        this.velocity = velocity;
    }
    
    /**
     * Constructor when standard gravitational parameter is known.
     * @param mass      mass in kg
     * @param mu        standard gravitational parameter in m3/s2
     * @param position  position in m
     * @param velocity  velocity in m/s
     */
    public Particle(double mass, double mu, Vector3D position, Vector3D velocity) {
        this.mass = mass;
        this.mu = mu;
        this.position = position;
        this.velocity = velocity;
    }

    /**
     * Get mass of particle in kg.
     * @return mass in kg
     */
    public double getMass() {
        return mass;
    }
    
    /**
     * Set mass of particle in kg.
     * standard gravitational parameter will be adjusted.
     * @param mass in kg
     */
    public void setMass(double mass) {
        this.mass = mass;
        this.mu = GRAVITATIONALCONSTANT * mass;
    }
    
    /**
     * Get position of particle in m.
     * @return position in m
     */
    public Vector3D getPosition() {
        return position;
    }
    
    /**
     * Set position of particle in m.
     * @param position in m
     */
    public void setPosition(Vector3D position) {
        this.position = position;
    }
    
    /**
     * Get velocity of particle in m/s.
     * @return velocity in m/s
     */
    public Vector3D getVelocity() {
        return velocity;
    }
    
    /**
     * Set velocity of particle in m/s.
     * @param velocity in m/s
     */
    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }
    
    /**
     * Get momentum of particle in kg m/s.
     * Momentum p is defined by p = m * v, where m is mass 
     * and v is velocity.
     * @return momentum in kg m/s
     */
    public Vector3D getMomentum() {
        // https://en.wikipedia.org/wiki/Momentum
        return velocity.scalarProduct(mass);
    }
    
    /**
     * Get kinetic energy of particle in J.
     * @return kinetic energy in J
     */
    public double getKineticEnergy() {
        // https://en.wikipedia.org/wiki/Kinetic_energy
        // Note that a non-rotating particle is assumed
        double v = velocity.magnitude();
        return 0.5 * mass * v*v;
    }
    
    /**
     * Get potential energy of particle in J.
     * @return potential energy in J
     */
    public double getPotentialEnergy() {
        return potentialEnergy;
    }
    
    /**
     * Correct for drift in position and velocity
     * @param driftPosition  drift in position to correct for
     * @param driftVelocity  drift in velocity to correct for
     */
    public void correctDrift(Vector3D driftPosition, Vector3D driftVelocity) {
        position = position.minus(driftPosition);
        velocity = velocity.minus(driftVelocity);
    }
    
    /**
     * Adjust kinetic energy of particle.
     * @param factor factor to adjust kinetic engergy
     */
    public void adjustKineticEnergy(double factor) {
        // Ekin = 0.5 * mass * v*v
        velocity = velocity.scalarProduct(Math.sqrt(factor));
    }
    
    /**
     * Compute total acceleration applied to this particle using
     * Newton Mechanics.
     * The potential energy for this particle is also computed.
     * @param particles all particles
     */
    public void computeAccelerationNewtonMechanics(Collection<Particle> particles) {
        acceleration = new Vector3D();
        potentialEnergy = 0.0;
        for (Particle p : particles) {
            if (p != this) {
                // Add acceleration from other particle
                acceleration.addVector(accelerationNewtonMechanics(p));
                // Add contribution to potential energy
                potentialEnergy += potentialEngergy(p);
            }
        }
        // Every pair of particles is counted twice, so divide by 2
        potentialEnergy = 0.5 * potentialEnergy;
        
        // Set acceleration computed by Newton Mechanics
        // such that it can be used to compute acceleration by
        // General Relativity
        accelerationNewtonMechanics = new Vector3D(acceleration);   
    }
    
    /**
     * Compute total acceleration applied to this particle using
     * General Relativity.
     * Note that the acceleration computed using Newton Mechanics
     * is used to compute acceleration using General Relativity.
     * @param particles all particles
     */
    public void computeAccelerationGeneralRelativity(Collection<Particle> particles) {
        /*
         * The gravitational acceleration of each body due to external
         * point masses is derived from the isotropic, parameterized
         * post-Newtonian (PPN) n-body metric.
         * See Equation (27) from W.M. Folkner et al., 
         * The Planetary and Lunar Ephemerides DE430 and DE431,
         * IPN Progress Report 42-196, February 15, 2014
         * https://ipnpr.jpl.nasa.gov/progress_report/42-196/196C.pdf
         */
        
        /* 
         * In Equation (27), the term acceleration of body B
         * appears in two terms on the right-hand side. Since
         * these terms are divided by c2, using the Newtonian
         * acceleration for these terms is accurate to O(c^(-2)).
         */
        
        // beta is PPN parameter measuring the nonlinearity in super
        // position of gravity (Page 47, Table 4)
        double beta = 1.0;
                
        // gamma is the PPn parameter measuring space curvature produced
        // by unit rest mass (Page 47, Table 4)
        double gamma = 1.0;
        
        // Notation:
        // Particle A = this, Particle B = p, and Particle C = q
        // GM_A, GM_B, and GM_C = standard gravitational parameter mu = G*M for particle A, B, and C
        // vec_r_A, vec_r_B, and vec_r_C = (x,y,z) position of particle A, B, and C
        // vec_v_A, vec_v_B, and vec_v_C = (x,y,z) velocity of particle A, B, and C
        // vec_a_A, vec_a_B, and vec_a_C = (x,y,z) acceleration of particle A, B, and C
        // v_A, v_B, and v_C = velocity magnitude of particle A, B, and C
        // c = speed of light
        // distAB = r_AB = Euclidean distance between Particle A and Particle B
        // diffPositionAB = vec_r_A - vec_r_B = difference between (x,y,z) position A and (x,y,z) position B
        // diffVelocityAB = vec_v_A - vec_v_B = difference between (x,y,z) velocity A and (x,y,z) velocity B
        
        // Compute first term of Equation (27)
        Vector3D firstTermVector = new Vector3D();
        for (Particle p : particles) {
            if (p != this) {
                // distAB = r_AB = Euclidean distance between A and B
                double distAB = this.position.euclideanDistance(p.position);
                
                // factor = GM_B / r_AB^3
                double factor = p.mu/(distAB*distAB*distAB);
                
                // sumCnotA = (Sum C : C != A : GM_C / r_AC)
                double sumCnotA = 0.0;
                for (Particle q : particles) {
                    if (q != this) {
                        double distAC = this.position.euclideanDistance(q.position);
                        sumCnotA = sumCnotA + q.mu/distAC;
                    }
                }
                
                // sumCnotB = (Sum C : C != B : GM_C / r_BC)
                double sumCnotB = 0.0;
                for (Particle q : particles) {
                    if (q != p) {
                        double distBC = p.position.euclideanDistance(q.position);
                        sumCnotB = sumCnotB + q.mu/distBC;
                    }
                }
                
                // vAdivc = v_A/c
                double vAdivc = this.velocity.magnitude()/LIGHTSPEED;
                
                // vBdivc = v_B/c
                double vBdivc = p.velocity.magnitude()/LIGHTSPEED;
                
                // vAdotvB = vec_v_A . vec_v_B (= vector dot product of v_A and v_B)
                double vAdotvB = this.velocity.dotProduct(p.velocity);
                
                // diffPositionAB = vec_r_A - vec_r_B
                Vector3D diffPositionAB = this.position.minus(p.position);
                
                // diffPositionBA = vec_r_B - vec_r_A
                Vector3D diffPositionBA = p.position.minus(this.position);
                
                // rAminrBdotvBdivrAB = (vec_r_A - vec_r_B) . vec_v_B / r_AB
                double rAminrBdotvBdivrAB = diffPositionAB.dotProduct(p.velocity) / distAB;
                
                // rBminrAdotaB = (vec_r_B - vec_r_A) . vec_a_B
                // Use acceleration computed using Newton Mechanics
                double rBminrAdotaB = diffPositionBA.dotProduct(p.accelerationNewtonMechanics);
                
                // factorCurlyBraces = the part of Equation (27) between curly braces
                double factorCurlyBraces = 
                    1.0 - 
                    (2*(beta + gamma)*sumCnotA)/(LIGHTSPEED*LIGHTSPEED) -
                    ((2*beta - 1.0)*sumCnotB)/(LIGHTSPEED*LIGHTSPEED) +
                    gamma*vAdivc*vAdivc + 
                    (1.0 + gamma)*vBdivc*vBdivc - 
                    (2.0*(1.0 + gamma)*vAdotvB)/(LIGHTSPEED*LIGHTSPEED) -
                    (3.0/(2.0*LIGHTSPEED*LIGHTSPEED))*rAminrBdotvBdivrAB*rAminrBdotvBdivrAB +
                    (1.0/(2.0*LIGHTSPEED*LIGHTSPEED))*rBminrAdotaB;

                // Add factor * (vec_r_B - vec_r_A) * factorCurlyBraces to the first term
                firstTermVector.addVector(diffPositionBA.scalarProduct(factor*factorCurlyBraces));
            }
        }
        
        // Compute second term of Equation (27)
        Vector3D secondTermVector = new Vector3D();
        for (Particle p : particles) {
            if (p != this) {
                // distAB = r_AB = Euclidean distance between A and B
                double distAB = this.position.euclideanDistance(p.position);
                
                // factor = GM_B / r_AB^3
                double factor = p.mu/(distAB*distAB*distAB);
                
                // diffPositionAB = vec_r_A - vec_r_B
                Vector3D diffPositionAB = this.position.minus(p.position);
                
                // vAgamma = (2 + 2*gamma)*vec_v_A
                Vector3D vAgamma = this.velocity.scalarProduct(2.0 + 2.0*gamma);
                
                // vBgamma = (1 + 2*gamma)*vec_v_B
                Vector3D vBgamma = p.velocity.scalarProduct(1.0 + 2.0*gamma);
                
                // vAgammaminusvBgamma = (2 + 2*gamma)*vec_v_A - (1 + 2*gamma)*vec_v_B
                Vector3D vAgammaminusvBgamma = vAgamma.minus(vBgamma);
                
                // dotProduct = [vec_r_A - vec_r_B].[(2 + 2*gamma)*vec_v_A - (1 + 2*gamma)*vec_v_B]
                double dotProduct = diffPositionAB.dotProduct(vAgammaminusvBgamma);
                
                // diffVelocityAB = vec_v_A - vec_v_B
                Vector3D diffVelocityAB = this.velocity.minus(p.velocity);
               
                // Add factor * dotProduct * (vec_v_A - vec_v_B) to the second term
                secondTermVector.addVector(diffVelocityAB.scalarProduct(factor*dotProduct));
            }
        }
        
        // Multiply second term vector with 1.0/c^2
        double secondTermFactor = 1.0/(LIGHTSPEED*LIGHTSPEED);
        secondTermVector = secondTermVector.scalarProduct(secondTermFactor);
        
        // Compute third term of Equation (27)
        Vector3D thirdTermVector = new Vector3D();
        for (Particle p : particles) {
            if (p != this) {
                // distAB = r_AB = Euclidean distance between A and B
                double distAB = this.position.euclideanDistance(p.position);
                
                // factor = GM_B / r_AB
                double factor = p.mu/distAB;
                
                // Add (GM_B / r_AB) * vec_a_B to the third term
                // Use acceleration computed using Newton Mechanics
                thirdTermVector.addVector(p.accelerationNewtonMechanics.scalarProduct(factor));
            }
        }
        
        // Multiply third term vector with (3 + 4*gamma)/(2*c^2)
        double thirdTermFactor = (3.0 + 4.0*gamma) / (2*LIGHTSPEED*LIGHTSPEED);
        thirdTermVector = thirdTermVector.scalarProduct(thirdTermFactor);
        
        // Add first, second, and third term to obtain acceleration
        acceleration = new Vector3D();
        acceleration.addVector(firstTermVector);
        acceleration.addVector(secondTermVector);
        acceleration.addVector(thirdTermVector);
    }

    /**
     * Initialize velocity for leapfrog algorithm.
     * @param deltaT time step in s
     */
    public void initStateLeapfrog(long deltaT) {
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        // Compute velocity v(-1/2) at time -0.5 * deltaT
        // v(-1/2) = v(0) - 0.5 * deltaT * a(0)
        velocity = velocity.minus(acceleration.scalarProduct(0.5*deltaT));
    }

    /**
     * Update velocity and position of particle using leapfrog algorithm.
     * @param deltaT time step in s
     */
    public void updateStateLeapfrog(long deltaT) {
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        // Compute velocity v(n+1/2)
        // v(n+1/2) = v(n-1/2) + deltaT * a(n)
        velocity.addVector(acceleration.scalarProduct(deltaT));
        // Compute position p(n+1)
        // p(n+1) = p(n) + deltaT * v(n+1/2)
        position.addVector(velocity.scalarProduct(deltaT));
    }
    
    /**
     * Update velocity and position of particle using Runge-Kutta method.
     * Step 1: compute k1 and l1.
     * @param deltaT time step in s
     */
    public void updateStateRungeKuttaA(long deltaT) {
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        // Store position and velocity of current simulation time
        formerPosition = new Vector3D(position);
        formerVelocity = new Vector3D(velocity);
        // Compute k1 and l1 for Runge-Kutta method
        k1 = acceleration.scalarProduct(deltaT);
        l1 = formerVelocity.scalarProduct(deltaT);
        // Set velocity for General Relativity step B
        velocity = formerVelocity.plus(k1.scalarProduct(0.5));
        // Set position to compute forces for RK step B
        position = formerPosition.plus(l1.scalarProduct(0.5));
    }
    
    /**
     * Update velocity and position of particle using Runge-Kutta method.
     * Step 2: compute k2 and l2.
     * @param deltaT time step in s
     */
    public void updateStateRungeKuttaB(long deltaT) {
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        // Compute k2 and l2 for Runge-Kutta method
        k2 = acceleration.scalarProduct(deltaT);
        l2 = (formerVelocity.plus(k1.scalarProduct(0.5))).scalarProduct(deltaT);
        // Set velocity for General Relativity step C
        velocity = formerVelocity.plus(k2.scalarProduct(0.5));
        // Set position to compute forces for RK step C
        position = formerPosition.plus(l2.scalarProduct(0.5));
    }
    
    /**
     * Update velocity and position of particle using Runge-Kutta method.
     * Step 3: compute k3 and l3.
     * @param deltaT time step in s
     */
    public void updateStateRungeKuttaC(long deltaT) {
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        // Compute k3 and l3 for Runge-Kutta method
        k3 = acceleration.scalarProduct(deltaT);
        l3 = (formerVelocity.plus(k2.scalarProduct(0.5))).scalarProduct(deltaT);
        // Set velocity for General Relativity step D
        velocity = formerVelocity.plus(k3);
        // Set position to compute forces for RK step D
        position = formerPosition.plus(l3);   
    }
    
    /**
     * Update velocity and position of particle using Runge-Kutta method.
     * Step 4: compute k4 and l4; compute new position and velocity.
     * @param deltaT time step in s
     */
    public void updateStateRungeKuttaD(long deltaT) {
        // http://physics.bu.edu/py502/lectures3/cmotion.pdf
        // Compute k4 and l4 for Runge-Kutta method
        k4 = acceleration.scalarProduct(deltaT);
        l4 = (formerVelocity.plus(k3)).scalarProduct(deltaT);
        // Compute new velocity using k1, k2, k3, k4
        velocity = new Vector3D(formerVelocity);
        Vector3D velocityTerm = new Vector3D();
        velocityTerm.addVector(k1);
        velocityTerm.addVector(k2.scalarProduct(2.0));
        velocityTerm.addVector(k3.scalarProduct(2.0));
        velocityTerm.addVector(k4);
        velocity.addVector(velocityTerm.scalarProduct(1.0/6.0));
        // Compute new position using l1, l2, l3, l4
        position = new Vector3D(formerPosition);
        Vector3D positionTerm = new Vector3D();
        positionTerm.addVector(l1);
        positionTerm.addVector(l2.scalarProduct(2.0));
        positionTerm.addVector(l3.scalarProduct(2.0));
        positionTerm.addVector(l4);
        position.addVector(positionTerm.scalarProduct(1.0/6.0));
    }

    /**
     * Compute acceleration applied by another particle using Newton Mechanics.
     * @param p other particle
     * @return acceleration in m/s2
     */
    private Vector3D accelerationNewtonMechanics(Particle p) {

        /*
         * Gravitational force = (G*M*m)/r2 = (mu*m)/r2, where
         * G = gravitational constant, M = mass of the other body, mu = G*M,
         * m = mass of this body, and r is distance between the bodies.
         * Acceleration = Gravitational force / mass, thus
         * Acceleration = (G*M)/r2 = mu/r2
         */
        
        // Square of distance r2
        double distanceSquare = position.euclideanDistanceSquare(p.position);
        
        // Magnitude of acceleration = mu/r2
        double accelerationMagnitude = p.mu/distanceSquare;
        
        // Direction of gravitational force
        Vector3D direction = position.direction(p.position);
        
        // Acceleration
        return direction.scalarProduct(accelerationMagnitude);
    }
    
    /**
     * Compute contribution to potential energy by another particle.
     * @param p other particle
     * @return contribution to potential energy in J
     */
    private double potentialEngergy(Particle p) {
        // Distance
        double distance = position.euclideanDistance(p.position);
        
        // Contribution to potential energy
        // http://www.physics.arizona.edu/~varnes/Teaching/141Hspring2004/Notes/Lecture38.pdf
        // Use standard gravitional parameter mu = G*M of other particle
        double Epot  = -(p.mu * this.mass) / distance;
        
        // Contribution to potential energy
        return Epot;
    }
}
