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
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.GregorianCalendar;

/**
 * Mercury precession. Solar system.
 * @author Nico Kuijpers
 */
public class MercuryPrecessionSolarSystemExperiment {

    // The Solar System
    private SolarSystem solarSystem;

    /**
     * Compute precession by simulating one or more centuries.
     * @param grFlag flag to set Newton Mechanics (false) or General Relativity (true)
     * @param cwpmFlag flag to set Curvature of Wave Propagation Method (CWPM)
     * @param nrCenturies number of centuries to simulate
     * @return precession [deg]
     */
    private double computePrecession(boolean grFlag, boolean cwpmFlag, int nrCenturies) {

        // Start date is January 1, 2000
        int startYear = 2000;
        GregorianCalendar startDate = new GregorianCalendar(startYear,0,1);

        // End date
        int endYear = startYear + nrCenturies*100;
        GregorianCalendar endDate = new GregorianCalendar(endYear,0, 1);

        // Create the Solar System
        solarSystem = new SolarSystem(startDate);

        // Set flag to simulate with Newton Mechanics or General Relativity
        solarSystem.setGeneralRelativityFlag(grFlag);

        // Set flag to simulate with Curvature of Wave Propagation Method (CWPM)
        solarSystem.setCurvatureWavePropagationFlag(cwpmFlag);

        // Initial position of perihelion of Mercury
        Vector3D initialPositionPerihelionMercury = positionPerihelionMercury();

        // Simulate
        while (solarSystem.getSimulationDateTime().before(endDate)) {
            solarSystem.advanceSimulationForward(24);
        }

        // Final position of perihelion of Mercury
        Vector3D finalPositionPerihelionMercury = positionPerihelionMercury();

        // Precession
        double precessionMercury = finalPositionPerihelionMercury.angleDeg(initialPositionPerihelionMercury);
        return precessionMercury;
    }

    // Determine argument of perihelion for Mercury [degrees]
    private Vector3D positionPerihelionMercury() {

        // Position [m] and velocity [m/s] of Mercury
        Particle mercury = solarSystem.getParticle("Mercury");
        Vector3D positionMercury = mercury.getPosition();
        Vector3D velocityMercury = mercury.getVelocity();

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
        MercuryPrecessionSolarSystemExperiment experiment = new MercuryPrecessionSolarSystemExperiment();

        // Number of centuries to simulate
        int nrCenturies = 10;

        // Factor to convert precession in degrees to arc seconds per century
        double factor = (double) 3600/nrCenturies;

        // Run experiment with Newton Mechanics
        double precessionNewtonMechanics = experiment.computePrecession(false, false, nrCenturies);

        // Print results
        // https://en.wikipedia.org/wiki/Tests_of_general_relativity#Perihelion_precession_of_Mercury
        System.out.println("Newton Mechanics:");
        System.out.println("Expected precession: " + (574.10 - 42.98) + " arc seconds / century");
        System.out.println("Simulated precession: " + (precessionNewtonMechanics * factor) + " arc seconds / century");

        // Run experiment with General Relativity
        double precessionGeneralRelativity = experiment.computePrecession(true, false, nrCenturies);

        // Print results
        // https://en.wikipedia.org/wiki/Tests_of_general_relativity#Perihelion_precession_of_Mercury
        System.out.println("General Relativity:");
        System.out.println("Observed precession: 574.10 +/- 0.65 arc seconds / century");
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
        System.out.println("Observed precession: 574.10 +/- 0.65 arc seconds / century");
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
        Expected precession: 531.12 arc seconds / century
        Simulated precession: 527.4573673426112 arc seconds / century
        General Relativity:
        Observed precession: 574.10 +/- 0.65 arc seconds / century
        Simulated precession: 570.3820468740086 arc seconds / century
        Difference in precession between Newton Mechanics and General Relativity:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.92467953139738 arc seconds / century
        Curvature of Wave Propagation Method:
        Observed precession: 574.10 +/- 0.65 arc seconds / century
        Simulated precession: 570.3772055347849 arc seconds / century
        Difference in precession between Newton Mechanics and Curvature of Wave Propagation Method:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.91983819217376 arc seconds / century

        Results after one thousand years of simulation (nrCenturies = 10)
        Newton Mechanics:
        Expected precession: 531.12 arc seconds / century
        Simulated precession: 533.6432718198496 arc seconds / century
        General Relativity:
        Observed precession: 574.10 +/- 0.65 arc seconds / century
        Simulated precession: 576.4936063580521 arc seconds / century
        Difference in precession between Newton Mechanics and General Relativity:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.85033453820256 arc seconds / century
        Curvature of Wave Propagation Method:
        Observed precession: 574.10 +/- 0.65 arc seconds / century
        Simulated precession: 576.4933045267816 arc seconds / century
        Difference in precession between Newton Mechanics and Curvature of Wave Propagation Method:
        Expected  : 42.98 arc seconds / century
        Simulated : 42.850032706932026 arc seconds / century
     */
}
