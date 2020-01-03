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

import ephemeris.EphemerisUtil;
import ephemeris.JulianDateConverter;
import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import particlesystem.ParticleSystem;
import util.Vector3D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a planet system with an oblate planet.
 * @author Nico Kuijpers
 */
public class OblatePlanetSystem extends ParticleSystem implements Serializable {

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Solar System parameters
    private static final SolarSystemParameters solarSystemParameters = SolarSystemParameters.getInstance();

    // Name of the planet
    private String planetName;

    // Reference to the Solar System
    private SolarSystem solarSystem;

    // Names of attractors from the Solar System
    private List<String> solarSystemAttractors;

    // Gravitational parameter of oblate plane [m3/s2]
    private double oblateMu;

    // Equatorial radius of oblate planet [m]
    private double equatorialRadius;

    // Zonal coefficients of oblate planet [-]
    private double[] zonalCoefficients;

    // Pole epoch of oblate planet [JED]
    private double poleEpoch;

    // Right ascension of z-axis of oblate planet [degrees]
    private double rightAscensionZaxis;

    // Declination of z-axis of oblate planet [degrees]
    private double declinationZaxis;

    // Rate of right ascension of z-axis of oblate planet [degrees/century]
    private double rightAscensionZaxisRate;

    // Rate of declination of z-axis of oblate planet [degrees/century]
    private double declinationZaxisRate;

    // Ecliptic angle [rad]
    private double eclipticAngle = Math.toRadians(SolarSystemParameters.AXIALTILT);

    // Current value of right ascension of z-axis of oblate planet [rad]
    private double alpha;

    // Current value of declination of z-axis of oblate planet [rad]
    private double delta;

    /**
     * Constructor.
     * Create planet at origin of the particle system.
     * @param solarSystem reference to the Solar System
     */
    public OblatePlanetSystem(String planetName, SolarSystem solarSystem) {

        // Set name of the planet
        this.planetName = planetName;

        // Set reference to Solar System
        this.solarSystem = solarSystem;

        // Names of attractors from the Solar System
        solarSystemAttractors = new ArrayList<>();
        solarSystemAttractors.add("Sun");
        solarSystemAttractors.add("Jupiter");
        solarSystemAttractors.add("Saturn");
        solarSystemAttractors.add("Uranus");
        solarSystemAttractors.add("Neptune");
        solarSystemAttractors.remove(planetName);

        // Set gravitational parameter of oblate planet [m3/s2]
        oblateMu = solarSystemParameters.getOblateMu(planetName);

        // Set equatorial radius of oblate planet [m]
        equatorialRadius = solarSystemParameters.getEquatorialRadius(planetName);

        // Set zonal coefficients of oblate planet [-]
        zonalCoefficients = solarSystemParameters.getZonalCoefficients(planetName);

        // Parameters of z-axis of oblate planet
        double[] zAxisParameters = solarSystemParameters.getZaxisParameters(planetName);

        // Set pole epoch of oblate planet [JED]
        poleEpoch = zAxisParameters[0];

        // Right ascension of z-axis of oblate planet [degrees]
        rightAscensionZaxis = zAxisParameters[1];

        // Declination of z-axis of oblate planet [degrees]
        declinationZaxis = zAxisParameters[2];

        // Rate of right ascension of z-axis of oblate planet [degrees/century]
        rightAscensionZaxisRate = zAxisParameters[3];

        // Rate of declination of z-axis of oblate planet [degrees/century]
        declinationZaxisRate = zAxisParameters[4];

        // Initialize alpha and delta
        updateZaxis();

        // Add planet to the particle system with oblate standard gravitational parameter
        double massPlanet = solarSystemParameters.getMass(planetName);
        this.addParticle(planetName, massPlanet, oblateMu, new Vector3D(), new Vector3D());
    }

    @Override
    protected void computeAcceleration() {
        // Compute acceleration using Newton mechanics
        for (String name : particles.keySet()) {
            if (name.equals(planetName)) {
                getParticle(name).computeAccelerationNewtonMechanics(particlesWithMass.values());
            }
            else {
                Particle moon = getParticle(name);
                Vector3D accelerationFromPlanet = accelerationMoonFromPlanetApproximate(name);
                List<Particle> otherParticles = new ArrayList<>();
                for (String n : particles.keySet()) {
                    if (!n.equals(name) && !n.equals(planetName)) {
                        otherParticles.add(getParticle(n));
                    }
                }
                moon.computeAccelerationNewtonMechanics(otherParticles);
                moon.addAcceleration(accelerationFromPlanet);

                // Add acceleration from the Sun and other planets
                moon.addAcceleration(accelerationFromSolarSystem(name));
            }
        }

        // Compute acceleration using General Relativity
        if (getGeneralRelativityFlag()) {
            // Note that the acceleration computed by Newton mechanics
            // is used to compute acceleration using General Relativity
            for (String name : particles.keySet()) {
                getParticle(name).computeAccelerationGeneralRelativity(particlesWithMass.values());
            }
        }
    }

    /**
     * Compute acceleration from other attractors in the Solar System
     * @param moonName
     * @return
     */
    private Vector3D accelerationFromSolarSystem(String moonName) {
        Vector3D positionPlanet = solarSystem.getParticle(planetName).getPosition();
        Vector3D positionMoon = getParticle(moonName).getPosition().plus(positionPlanet);
        Vector3D acceleration = new Vector3D();
        for (String name : solarSystemAttractors) {
            Vector3D position = solarSystem.getParticle(name).getPosition();
            double mu = solarSystem.getParticle(name).getMu();
            double distanceSquarePlanet = positionPlanet.euclideanDistanceSquare(position);
            double accelerationMagnitudePlanet = mu / distanceSquarePlanet;
            Vector3D directionPlanet = positionPlanet.direction(position);
            Vector3D accelerationPlanet = directionPlanet.scalarProduct(accelerationMagnitudePlanet);
            double distanceSquareMoon = positionMoon.euclideanDistanceSquare(position);
            double accelerationMagnitudeMoon = mu / distanceSquareMoon;
            Vector3D directionMoon = positionMoon.direction(position);
            Vector3D accelerationMoon = directionMoon.scalarProduct(accelerationMagnitudeMoon);
            acceleration.addVector(accelerationMoon.minus(accelerationPlanet));
        }
        return acceleration;
    }

    /**
     * Update alpha and delta representing the position of the z-axis of the oblate planet
     */
    private void updateZaxis() {

        /*
         * The z-axis of the oblate planet is defined by right ascension alpha and
         * declination delta. Alpha and delta may change over time and are determined
         * by the z-axis parameters.
         */
        double Tjed = JulianDateConverter.convertCalendarToJulianDate(solarSystem.getSimulationDateTime());
        double nrCenturies = (Tjed - poleEpoch) / EphemerisUtil.NRDAYSPERCENTURY;
        alpha = Math.toRadians(rightAscensionZaxis + nrCenturies * rightAscensionZaxisRate);
        delta = Math.toRadians(declinationZaxis + nrCenturies * declinationZaxisRate);
    }

    /**
     * Transform 3D vector from ecliptic plane to equatorial plane of planet
     * @param vector vector to be transformed
     * @return vector after transformation
     */
    private Vector3D transformFromEclipticPlaneToEquatorialPlane(Vector3D vector) {

        // The z-axis of the oblate planet changes over time
        updateZaxis();

        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        // Transform from ecliptic plane to Earth equatorial plane
        double cos_h = Math.cos(-eclipticAngle);
        double sin_h = Math.sin(-eclipticAngle);
        double yg1 = cos_h * y + sin_h * z;
        double zg1 =-sin_h * y + cos_h * z;
        double xg1 = x;

        /*
         * Transform from Earth equatorial plane to equatorial plane of oblate planet
         * Apply transposed matrix from Formula 1 in N.V. Emelyanov and M.Yu Samorodov,
         * Analytical theory of motion and new ephemeris of Triton from observations
         * MNRAS 454, 2205-2215 (2015)
         * https://academic.oup.com/mnras/article/454/2/2205/2892708
         * https://doi.org/10.1093/mnras/stv2116
         */
        double xg = -Math.sin(alpha)*xg1 + Math.cos(alpha)*yg1;
        double yg = -Math.cos(alpha)*Math.sin(delta)*xg1 - Math.sin(alpha)*Math.sin(delta)*yg1 + Math.cos(delta)*zg1;
        double zg =  Math.cos(alpha)*Math.cos(delta)*xg1 + Math.sin(alpha)*Math.cos(delta)*yg1 + Math.sin(delta)*zg1;

        return new Vector3D(xg,yg,zg);
    }

    /**
     * Transform 3D vector from equatorial plane of planet to ecliptic plane.
     * @param vector vector to be transformed
     * @return vector after transformation
     */
    private Vector3D transformFromEquatorialPlaneToEclipticPlane(Vector3D vector) {

        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        /*
         * Transform from equatorial plane of oblate planet to Earth equatorial plane
         * Apply Formula 1 from N.V. Emelyanov and M.Yu Samorodov,
         * Analytical theory of motion and new ephemeris of Triton from observations
         * MNRAS 454, 2205-2215 (2015)
         * https://academic.oup.com/mnras/article/454/2/2205/2892708
         * https://doi.org/10.1093/mnras/stv2116
         */
        double xg = -Math.sin(alpha)*x - Math.cos(alpha)*Math.sin(delta)*y + Math.cos(alpha)*Math.cos(delta)*z;
        double yg =  Math.cos(alpha)*x - Math.sin(alpha)*Math.sin(delta)*y + Math.sin(alpha)*Math.cos(delta)*z;
        double zg =  Math.cos(delta)*y + Math.sin(delta)*z;

        // Transform from Earth equatorial plane to ecliptic plane
        double cos_h = Math.cos(eclipticAngle);
        double sin_h = Math.sin(eclipticAngle);
        double yg1 = cos_h * yg + sin_h * zg;
        double zg1 =-sin_h * yg + cos_h * zg;
        yg = yg1;
        zg = zg1;
        return new Vector3D(xg,yg,zg);
    }


    /**
     * Calculate the gravitational potential from Legendre polynomials
     * @param position equatorial position
     * @return graviational potential
     */
    private double gravitationalPotentialFromPerturbationsPlanet(Vector3D position) {
        // Maximum order to which the perturbation potential components are to be calculated
        int nmax = zonalCoefficients.length - 1;

        // Legendre polynomial values
        double[] P = new double[nmax + 1];

        // Calculate 'r' and xi = 'sin(theta)'
        double r = position.magnitude();
        double xi = position.getZ() / r;

        // Zonal coefficients [-]
        double[] J = zonalCoefficients;

        // Equatorial radius [m]
        double a = equatorialRadius;

        // Gravitational parameter [m3/s2]
        double GMplanet = oblateMu;

        // Calculate the P[n] up to and including order 'nmax'
        // using a recursive scheme
        P[1] = xi;
        P[0] = 1.0;
        for (int n = 2; n <= nmax; n++) {
            P[n] = (2 * n - 1) * xi * P[n - 1] + (1 - n) * P[n - 2];
            P[n] /= n;
        }

        // Calculate the gravitational potential terms from
        // order 2 up to and including order 'nmax'
        // V = -((G*M)/r) [1 - Sum_1^nmax Jn (a/r)^n Pn(cos theta)]
        double sum = 0.0;
        double arn = (a/r)*(a/r);
        for (int n = 2; n <= nmax; n++) {
            sum += J[n] * arn * P[n];
            arn *= (a/r);
        }
        double V = -(GMplanet/r) * (1 - sum);
        return V;
    }

    /**
     * Compute acceleration of moon from planet using perturbation forces from
     * zonal coefficients.
     * @param moonName name of moon
     * @return acceleration in m/s2
     */
    private Vector3D accelerationMoonFromPlanetApproximate(String moonName) {
        Vector3D positionPlanet = getParticle(planetName).getPosition();
        Vector3D positionMoon = getParticle(moonName).getPosition();
        Vector3D position = positionMoon.minus(positionPlanet);
        Vector3D positionEquatorialPlane = transformFromEclipticPlaneToEquatorialPlane(position);
        // double deltaXYZ = 1000.0; // 1000 m = 1 km
        double deltaXYZ = 100.0; // 100 m
        Vector3D positionXmin  = positionEquatorialPlane.minus(new Vector3D(0.5*deltaXYZ,0.0,0.0));
        Vector3D positionXplus = positionEquatorialPlane.plus(new Vector3D(0.5*deltaXYZ,0.0,0.0));
        Vector3D positionYmin  = positionEquatorialPlane.minus(new Vector3D(0.0,0.5*deltaXYZ,0.0));
        Vector3D positionYplus = positionEquatorialPlane.plus(new Vector3D(0.0,0.5*deltaXYZ,0.0));
        Vector3D positionZmin  = positionEquatorialPlane.minus(new Vector3D(0.0,0.0,0.5*deltaXYZ));
        Vector3D positionZplus = positionEquatorialPlane.plus(new Vector3D(0.0,0.0,0.5*deltaXYZ));
        double VXmin  = gravitationalPotentialFromPerturbationsPlanet(positionXmin);
        double VXplus = gravitationalPotentialFromPerturbationsPlanet(positionXplus);
        double VYmin  = gravitationalPotentialFromPerturbationsPlanet(positionYmin);
        double VYplus = gravitationalPotentialFromPerturbationsPlanet(positionYplus);
        double VZmin  = gravitationalPotentialFromPerturbationsPlanet(positionZmin);
        double VZplus = gravitationalPotentialFromPerturbationsPlanet(positionZplus);
        double accX = (VXplus - VXmin)/deltaXYZ;
        double accY = (VYplus - VYmin)/deltaXYZ;
        double accZ = (VZplus - VZmin)/deltaXYZ;
        Vector3D accelerationEquatorialPlane = new Vector3D(-accX,-accY,-accZ);
        return transformFromEquatorialPlaneToEclipticPlane(accelerationEquatorialPlane);
    }

    @Override
    public void correctDrift() {
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




