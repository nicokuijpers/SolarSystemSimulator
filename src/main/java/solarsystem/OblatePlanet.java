/*
 * Copyright (c) 2020 Nico Kuijpers and Marco Brassé
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
import util.Vector3D;

import java.util.GregorianCalendar;

/**
 * Represents an oblate planet.
 * @author Nico Kuijpers and Marco Brassé
 */
public class OblatePlanet extends Particle {

    private static final long serialVersionUID = 1L;

    // Maximum distance to use oblateness is 5 mln km
    private static final double MAXDISTANCEOBLATENESS = 5.0E09;

    // Solar System parameters
    private static final SolarSystemParameters solarSystemParameters = SolarSystemParameters.getInstance();

    // Name of the planet
    private String planetName;

    // Gravitational parameter of oblate plane [m3/s2]
    private double oblateMu;

    // Equatorial radius of oblate planet [m]
    private double equatorialRadius;

    // Zonal coefficients of oblate planet [-]
    private double[] zonalCoefficients;

    // Ecliptic angle [rad]
    private double eclipticAngle = Math.toRadians(SolarSystemParameters.AXIALTILT);
    // From EphemerisNeptuneMoons:
    // private double eclipticAngle = Math.toRadians(23.43929);

    // Right ascension of z-axis of oblate planet at initial dateTime [rad]
    private double alpha;

    // Declination of z-axis of oblate planet at initial dateTime [rad]
    private double delta;

    /**
     * Constructor.
     * Create oblate planet.
     * @param planetName name of the planet
     * @param dateTime   date/time to define z-axis
     * @param mass       mass of the planet [kg]
     * @param mu         gravitational parameter [m3/s2]
     * @param position   initial position [m]
     * @param velocity   initial velocity [m/s]
     */
    public OblatePlanet(String planetName, GregorianCalendar dateTime,
                        double mass, double mu, Vector3D position, Vector3D velocity) {

        // Constructor of Particle
        super(mass, mu, position, velocity);

        // Set name of the planet
        this.planetName = planetName;

        // Set gravitational parameter of oblate planet [m3/s2]
        oblateMu = solarSystemParameters.getOblateMu(planetName);

        // Set equatorial radius of oblate planet [m]
        equatorialRadius = solarSystemParameters.getEquatorialRadius(planetName);

        // Set zonal coefficients of oblate planet [-]
        zonalCoefficients = solarSystemParameters.getZonalCoefficients(planetName);

        // Initialize alpha and delta representing the position of the z-axis
        initZaxis(dateTime);
    }

    @Override
    public void setMass(double mass) {
        super.setMass(mass);
        this.oblateMu = GRAVITATIONALCONSTANT * mass;
    }

    @Override
    public void setMu(double mu) {
        super.setMu(mu);
        this.oblateMu = mu;
    }

    @Override
    protected Vector3D accelerationNewtonMechanics(Particle p) {
        double distance = this.getPosition().euclideanDistance(p.getPosition());
        if (distance > MAXDISTANCEOBLATENESS) {
            // Do not use oblateness
            return super.accelerationNewtonMechanics(p);
        }
        else {
            // Compute acceleration applied by oblate planet to other particle using
            // perturbation forces from zonal coefficients with derivatives of Legendre polynomials
            Vector3D positionPlanet = this.getPosition();
            Vector3D positionParticle = p.getPosition();
            Vector3D position = positionParticle.minus(positionPlanet);
            Vector3D positionEquatorialPlane = transformFromEclipticPlaneToEquatorialPlane(position);
            Vector3D acceleration = gravitationalPotentialDerivativeFromPerturbationsPlanet(positionEquatorialPlane);
            return transformFromEquatorialPlaneToEclipticPlane(acceleration);
        }
    }

    /**
     * Initialize alpha and delta representing the position of the z-axis of the oblate planet
     */
    private void initZaxis(GregorianCalendar dateTime) {

        /*
         * The z-axis of the oblate planet is defined by right ascension alpha and
         * declination delta. Alpha and delta may change over time and are determined
         * by the z-axis parameters.
         */

        // Parameters of z-axis of oblate planet
        double[] zAxisParameters = solarSystemParameters.getZaxisParameters(planetName);

        // Pole epoch of oblate planet [JED]
        double poleEpoch = zAxisParameters[0];

        // Right ascension of z-axis of oblate planet [degrees]
        double rightAscensionZaxis = zAxisParameters[1];

        // Declination of z-axis of oblate planet [degrees]
        double declinationZaxis = zAxisParameters[2];

        // Rate of right ascension of z-axis of oblate planet [degrees/century]
        double rightAscensionZaxisRate = zAxisParameters[3];

        // Rate of declination of z-axis of oblate planet [degrees/century]
        double declinationZaxisRate = zAxisParameters[4];

        // Compute number of centuries since epoch
        double Tjed = JulianDateConverter.convertCalendarToJulianDate(dateTime);
        double nrCenturies = (Tjed - poleEpoch) / EphemerisUtil.NRDAYSPERCENTURY;

        // Initialize alpha and beta
        alpha = Math.toRadians(rightAscensionZaxis + nrCenturies * rightAscensionZaxisRate);
        delta = Math.toRadians(declinationZaxis + nrCenturies * declinationZaxisRate);
    }

    /**
     * Transform 3D vector from ecliptic plane to equatorial plane of planet
     * @param vector vector to be transformed
     * @return vector after transformation
     */
    private Vector3D transformFromEclipticPlaneToEquatorialPlane(Vector3D vector) {

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
     * Calculate the gravitational potential derivative (acceleration)
     * from Legendre polynomials
     * @param position equatorial position
     * @return gravitational potential derivative (acceleration)
     */
    private Vector3D gravitationalPotentialDerivativeFromPerturbationsPlanet(Vector3D position) {

        /*
         * The gravitational potential derivative is calculated using zonal coefficients
         * and Legendre polynomials
         *
         * V(r,theta) = -((G*M)/r) [1 - Sum_1^nmax Jn (a/r)^n Pn(cos theta)],
         *
         * where V             is gravitational potential
         *       r             is distance from center of the planet
         *       theta         is angle between z-axis and line from center to position
         *       G*M           is gravitational parameter of oblate planet
         *       nmax          is maximum order for which zonal coefficients are defined
         *       Jn            are zonal coefficients
         *       a             is equatorial radius
         *       Pn(cos theta) are Legendre polynomials
         *
         * https://en.wikipedia.org/wiki/Geopotential_model
         * https://en.wikipedia.org/wiki/Legendre_polynomials
         *
         * To calculate the Legendre polynomials, a recursive scheme can be used.
         * The code below is adapted from the C-code published at
         * https://www.orbiter-forum.com/showthread.php?t=39469
         *
         * The derivatives and approach are based on C-code by S. Moshier obtained from
         * http://www.moshier.net/ssystem.html
         *
         * Moshier, S. L. (1992),
         * "Comparison of a 7000-year lunar ephemeris with analytical theory",
         * Astronomy and Astrophysics 262, 613-616
         * http://adsabs.harvard.edu/abs/1992A%26A...262..613M
         *
         * See also related question and answers on StackExchange:
         * https://space.stackexchange.com/questions/23408/
         * how-to-calculate-the-planets-and-moons-beyond-newtonss-gravitational-force
         */

        // Maximum order to which the perturbation potential components are to be calculated
        int nmax = zonalCoefficients.length - 1;

        // Values of Legendre polynomials
        double[] P = new double[nmax + 1];

        // Values of derivatives of Legendre polynomials
        double[] DP = new double[nmax + 1];

        // Calculate distance r and xi = cos(theta), where theta is the angle between
        // the z-axis and the line from the center of the planet towards position
        // Note that xi = cos(theta) = sin(pi/2 - theta) = z/r
        double r  = position.magnitude();
        double xi = position.getZ() / r;

        // Zonal coefficients [-]
        double[] J = zonalCoefficients;

        // Equatorial radius [m]
        double a = equatorialRadius;

        // Gravitational parameter [m3/s2]
        double GMplanet = oblateMu;

        // Calculate the P[n] and their derivatives DP[n] up to and
        // including order nmax using a recursive scheme
        /*
         * Legendre polynomials:
         * P0(x) = 1
         * P1(x) = x
         * (n+1) Pn+1(x) = (2n+1) x Pn(x) - n Pn-1(x)
         *
         * Derivatives of Legendre polynomials (S. Moshier):
         * P'0(x) = 0
         * P'1(x) = 1
         * (x^2 - 1) P'n(x) = n[ x Pn(x) - Pn-1(x) ]
         */
        P[0]  = 1.0;
        P[1]  = xi;
        DP[0] = 0.0;
        DP[1] = 1.0;
        for (int n = 2; n <= nmax; n++) {
            P[n]  = ((2 * n - 1) * xi * P[n - 1] + (1 - n) * P[n - 2])/n;
            DP[n] = (n * (xi*P[n] - P[n-1])) / (xi*xi - 1.0);
        }

        // To compute acceleration in body frame
        double sinLat = xi;
        double cosLat = Math.sqrt(1.0 - xi*xi);
        double a1     = cosLat*r;
        double cosLon = position.getX()/a1;
        double sinLon = position.getY()/a1;

        // Calculate the gravitational potential terms from
        // order 2 up to and including order nmax
        // V = -((G*M)/r) [1 - Sum_1^nmax Jn (a/r)^n Pn(cos theta)]
        double[] acc = new double[3];
        acc[0] = 0.0;
        acc[1] = 0.0;
        acc[2] = 0.0;
        double arn = (a/r)*(a/r);
        for (int n = 2; n <= nmax; n++) {
            double t0 = J[n] * (n + 1) * P[n];
            double t1 = 0.0;
            double t2 = -cosLat * J[n] * DP[n];
            acc[0] += arn * t0;
            acc[1] += arn * t1;
            acc[2] += arn * t2;
            arn *= (a/r);
        }
        acc[0] = acc[0] / (r*r);
        acc[1] = acc[1] / (r*r);
        acc[2] = acc[2] / (r*r);

        // Rotate back to equatorial frame
        double ax = cosLat*acc[0] - sinLat*acc[2];
        double ay = acc[1];
        double az = sinLat*acc[0] + cosLat*acc[2];
        acc[0] = (cosLon*ax - sinLon*ay)*GMplanet;
        acc[1] = (sinLon*ax + cosLon*ay)*GMplanet;
        acc[2] = az*GMplanet;

        // Add "J0" term (central force component)
        Vector3D accJ0 = position.normalize().scalarProduct(GMplanet/(r*r));
        acc[0] -= accJ0.getX();
        acc[1] -= accJ0.getY();
        acc[2] -= accJ0.getZ();
        return new Vector3D(acc[0],acc[1],acc[2]);
    }
}
