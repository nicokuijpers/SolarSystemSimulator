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

import ephemeris.EphemerisUtil;
import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import util.Vector3D;

import java.util.GregorianCalendar;

/**
 * Mercury precession. Two particle system.
 * @author Nico Kuijpers
 */
public class MercuryPrecessionTwoParticleExperiment {

    // Number of seconds per day
    private final long nrSecondsPerDay = (long) 24 * 60 * 60;

    // Average number of days per century
    private final double nrDaysPerCentury = 36524.25;

    // The particle system
    private ParticleSystem particleSystem;

    /**
     * Compute precession by simulating one ore more centuries.
     * @param grFlag flag to set Newton Mechanics (false) or General Relativity (true)
     * @param cwpmFlag flag to set Curvature of Wave Propagation Method (CWPM)
     * @param nrCenturies number of centuries to simulate
     * @return precession [deg]
     */
    private double computePrecession(boolean grFlag, boolean cwpmFlag, int nrCenturies) {

        // Start date is January 1, 2000
        GregorianCalendar startDate = new GregorianCalendar(2000,0,1);

        // Create the particle system
        particleSystem = new ParticleSystem();

        // Set flag to simulate with Newton Mechanics or General Relativity
        particleSystem.setGeneralRelativityFlag(grFlag);

        // Set flag to simulate with Curvature of Wave Propagation Method (CWPM)
        particleSystem.setCurvatureWavePropagationFlag(cwpmFlag);

        // Create the Sun
        Vector3D positionSun = new Vector3D(); // Origin
        Vector3D velocitySun = new Vector3D(); // Zero velocity
        double massSun = SolarSystemParameters.getInstance().getMass("Sun");
        double muSun   = SolarSystemParameters.getInstance().getMu("Sun");
        particleSystem.addParticle("Sun",massSun,muSun,positionSun,velocitySun);

        // Create Mercury
        double[] orbitParsMercury = SolarSystemParameters.getInstance().getOrbitParameters("Mercury");
        double orbitElementsMercury[] = EphemerisUtil.computeOrbitalElements(orbitParsMercury,startDate);
        Vector3D positionMercury = EphemerisUtil.computePosition(orbitElementsMercury);
        Vector3D velocityMercury = EphemerisUtil.computeVelocity(muSun,orbitElementsMercury);
        double massMercury = SolarSystemParameters.getInstance().getMass("Mercury");
        double muMercury = SolarSystemParameters.getInstance().getMu("Mercury");
        particleSystem.addParticle("Mercury",massMercury,muMercury,positionMercury,velocityMercury);

        // Number of days to simulate
        long nrDays = (long) (nrCenturies * nrDaysPerCentury);

        // Number of seconds to simulate
        long nrSeconds = nrDays * nrSecondsPerDay;

        // Set simulation time step to 1 hour
        long deltaT = (long) 60 * 60;

        // Initial position of perihelion of Mercury
        Vector3D initialPositionPerihelionMercury = positionPerihelionMercury();

        // Simulate
        long currentTime = 0L;
        while (currentTime < nrSeconds) {
            particleSystem.advanceRungeKutta(deltaT);
            particleSystem.correctDrift();
            currentTime += deltaT;
        }

        // Final position of perihelion of Mercury
        Vector3D finalPositionPerihelionMercury = positionPerihelionMercury();

        // Precession
        double precessionMercury = finalPositionPerihelionMercury.angleDeg(initialPositionPerihelionMercury);
        return precessionMercury;
    }

    // Determine argument of perihelion for Mercury [degrees]
    private Vector3D positionPerihelionMercury() {


        // Position [m] and velocity [m/s] of the Sun
        Particle sun = particleSystem.getParticle("Sun");
        Vector3D positionSun = sun.getPosition();
        Vector3D velocitySun = sun.getVelocity();

        // Position [m] and velocity [m/s] of Mercury relative to the Sun
        Particle mercury = particleSystem.getParticle("Mercury");
        Vector3D positionMercury = mercury.getPosition().minus(positionSun);
        Vector3D velocityMercury = mercury.getVelocity().minus(velocitySun);

        // Compute orbital elements from position and velocity
        double muSun = SolarSystemParameters.getInstance().getMu("Sun");
        double orbitElements[]
                = EphemerisUtil.computeOrbitalElementsFromPositionVelocity(muSun,positionMercury, velocityMercury);

        // Set mean anomaly to zero to compute position of perihelion
        orbitElements[3] = 0.0;

        // Compute postition of perihelion
        Vector3D positionPerihelion = EphemerisUtil.computePosition(orbitElements);
        return positionPerihelion;
    }

    /**
     * Main method.
     * Run experiment and print results.
     *
     * @param args input arguments (not used)
     */
    public static void main(String[] args) {
        // Experiment set-up
        MercuryPrecessionTwoParticleExperiment experiment = new MercuryPrecessionTwoParticleExperiment();

        // Number of centuries to simulate
        int nrCenturies = 100;

        // Factor to convert precession in degrees to arc seconds per century
        double factor = (double) 3600/nrCenturies;

        // Run experiment with Newton Mechanics
        double precessionNewtonMechanics = experiment.computePrecession(false, false, nrCenturies);

        // Print results
        // https://en.wikipedia.org/wiki/Tests_of_general_relativity#Perihelion_precession_of_Mercury
        System.out.println("Newton Mechanics:");
        System.out.println("Expected precession: zero");
        System.out.println("Simulated precession: " + (precessionNewtonMechanics * factor) + " arc seconds / century");

        // Run experiment with General Relativity
        double precessionGeneralRelativity = experiment.computePrecession(true, false, nrCenturies);

        // Print results
        // https://en.wikipedia.org/wiki/Tests_of_general_relativity#Perihelion_precession_of_Mercury
        System.out.println("General Relativity:");
        System.out.println("Expected precession: " + 42.98 + " arc seconds / century");
        System.out.println("Simulated precession: " + (precessionGeneralRelativity * factor) + " arc seconds / century");

        // Difference
        double differencePrecessionGR = precessionGeneralRelativity - precessionNewtonMechanics;
        System.out.println("Difference in precession between Newton Mechanics and General Relativity:");
        System.out.println("Expected  : " + 42.98 + " arc seconds / century");
        System.out.println("Simulated : " + (differencePrecessionGR * factor) + " arc seconds / century");

        // Run experiment with Curvature of Wave Propagation Method (CWPM)
        double precessionCurvatureWavePropagation = experiment.computePrecession(true, true, nrCenturies);

        // Print results
        // https://en.wikipedia.org/wiki/Tests_of_general_relativity#Perihelion_precession_of_Mercury
        System.out.println("Curvature of Wave Propagation Method:");
        System.out.println("Expected precession: " + 42.98 + " arc seconds / century");
        System.out.println("Simulated precession: " + (precessionCurvatureWavePropagation * factor) + " arc seconds / century");

        // Difference
        double differencePrecessionCWPM = precessionCurvatureWavePropagation - precessionNewtonMechanics;
        System.out.println("Difference in precession between Newton Mechanics and Curvature of Wave Propagation Method:");
        System.out.println("Expected  : " + 42.98 + " arc seconds / century");
        System.out.println("Simulated : " + (differencePrecessionCWPM * factor) + " arc seconds / century");
    }

    /*
        Results after one hundred years of simulation (nrCenturies = 1)
        Newton Mechanics:
        Expected precession: zero
        Simulated precession: 0.12439479095982868 arc seconds / century
        General Relativity:
        Expected precession: 42.98 arc seconds / century
        Simulated precession: 42.89421983650358 arc seconds / century
        Difference in precession between Newton Mechanics and General Relativity:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.769825045543755 arc seconds / century
        Curvature of Wave Propagation Method:
        Expected precession: 42.98 arc seconds / century
        Simulated precession: 42.89420838414011 arc seconds / century
        Difference in precession between Newton Mechanics and Curvature of Wave Propagation Method:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.769813593180274 arc seconds / century

        Results after one thousand years of simulation (nrCenturies = 10)
        Newton Mechanics:
        Expected precession: zero
        Simulated precession: 0.008812135260854349 arc seconds / century
        General Relativity:
        Expected precession: 42.98 arc seconds / century
        Simulated precession: 42.9873413791739 arc seconds / century
        Difference in precession between Newton Mechanics and General Relativity:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.978529243913044 arc seconds / century
        Curvature of Wave Propagation Method:
        Expected precession: 42.98 arc seconds / century
        Simulated precession: 42.98733002193445 arc seconds / century
        Difference in precession between Newton Mechanics and Curvature of Wave Propagation Method:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.97851788667359 arc seconds / century

        Results after ten thousand years of simulation (nrCenturies = 100)
        Newton Mechanics:
        Expected precession: zero
        Simulated precession: 0.002406442467588917 arc seconds / century
        General Relativity:
        Expected precession: 42.98 arc seconds / century
        Simulated precession: 42.98304838160468 arc seconds / century
        Difference in precession between Newton Mechanics and General Relativity:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.980641939137094 arc seconds / century
        Curvature of Wave Propagation Method:
        Expected precession: 42.98 arc seconds / century
        Simulated precession: 42.98303715115304 arc seconds / century
        Difference in precession between Newton Mechanics and Curvature of Wave Propagation Method:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.98063070868545 arc seconds / century
     */
}
