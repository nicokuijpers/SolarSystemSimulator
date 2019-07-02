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
package ephemeris;

import util.Vector3D;

import java.util.GregorianCalendar;

/**
 * Utilities for Ephemeris computation and orbital mechanics.
 * @author Nico Kuijpers
 */
public class EphemerisUtil {

    /** 
     * Number of days per century.
     */
    // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
    private static final double NRDAYSPERCENTURY = 36525;
    
    /** 
     * Julian Ephemeris Date J2000.0.
     */
    // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
    private static final double J2000 = 2451545.0;
    
    /**
     * Compute number of centuries past J2000.0.
     *
     * @param  Teph Julian Ephemeris Date
     * @return number of centuries past J2000.0
     */
    public static double computeNrCenturiesPastJ2000(double Teph) {

        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        // T = (Teph - 2451545.0) / 36525
        // Teph is Julian Ephemeris Date.
        
        // Compute number of centuries past J2000.0
        return (Teph - J2000) / NRDAYSPERCENTURY;
    }
    
    /**
     * Compute number of centuries past J2000.0.
     *
     * @param date date
     * @return number of centuries past J2000.0
     */
    public static double computeNrCenturiesPastJ2000(GregorianCalendar date) {

        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        // T = (Teph - 2451545.0) / 36525
        // Teph is Julian Ephemeris Date.
        double Teph = JulianDateConverter.convertCalendarToJulianDate(date);

        // Compute number of centuries past J2000.0
        return computeNrCenturiesPastJ2000(Teph);
    }

    /**
     * Inverse transformation for 23.4 degrees J2000 frame.
     * This transformation is performed such that the J2000 ecliptic plane
     * becomes the x-y plane.
     * @param coordinates input coordinates
     * @return coordinates after transformation
     */
    public static Vector3D inverseTransformJ2000(Vector3D coordinates) {
        double sinEP = -0.397776995;
        double cosEP = Math.sqrt(1.0 - sinEP*sinEP);
        double x = coordinates.getX();
        double y = coordinates.getY();
        double z = coordinates.getZ();
        return new Vector3D(x, cosEP*y - sinEP*z, sinEP*y + cosEP*z);
    }

    /**
     * Solve Kepler's equation M = E - e*sin(E), where M is mean anomaly, E is
     * eccentric anomaly, and e is eccentricity.
     * Uses fixed point iteration to solve Kepler's equation.
     *
     * @param Mrad mean anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @param maxError maximum error allowed [radians]
     * @return eccentric anomaly [radians]
     */
    public static double solveKeplerEquationFixedPointIteration(double Mrad, double eccentricity, double maxError) {

        // Fixed point iteration
        // Number of iterations depends on eccentricity
        // https://en.wikipedia.org/wiki/Kepler%27s_equation
        // Fixed point iteration is slower than Newton-Raphson
        double Erad = Mrad;
        double deltaE = 0.0;
        do {
            double previousErad = Erad;
            Erad = Mrad + eccentricity*Math.sin(Erad);
            deltaE = Erad - previousErad;
        } while (Math.abs(deltaE) > maxError);
        return Erad;
    }
        
    /**
     * Solve Kepler's equation M = E - e*sin(E), where M is mean anomaly, E is
     * eccentric anomaly, and e is eccentricity.
     * Uses Newton-Raphson to solve Kepler's equation.
     *
     * @param Mrad mean anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @param maxError maximum error allowed [radians]
     * @return eccentric anomaly [radians]
     */
    public static double solveKeplerEquationNewtonRaphson(double Mrad, double eccentricity, double maxError) {
        
        // Newton-Raphson method 
        // Number of iterations depends on eccentricity
        // https://en.wikipedia.org/wiki/Kepler%27s_equation
        // Newton-Raphson is faster than fixed point iteration
        double Erad = Mrad;
        if (eccentricity > 0.8) {
            Erad = Math.PI;
        }
        double deltaE;
        do {
            Erad = Erad - (Erad - eccentricity * Math.sin(Erad) - Mrad)
                    / (1.0 - eccentricity * Math.cos(Erad));
            deltaE = Erad - eccentricity * Math.sin(Erad) - Mrad;
        } while (Math.abs(deltaE) > maxError);
        return Erad;
    }

    /**
     * Solve Kepler's equation M = E - e*sin(E), where M is mean anomaly, E is
     * eccentric anomaly, and e is eccentricity.
     * Uses Halley's method to solve Kepler's equation.
     *
     * @param Mrad mean anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @param maxError maximum error allowed [radians]
     * @return eccentric anomaly [radians]
     */
    public static double solveKeplerEquationHalley(double Mrad, double eccentricity, double maxError) {

        // Halley's method
        // https://en.wikipedia.org/wiki/Halley's_method
        double Erad = Mrad;
        double sinErad = Math.sin(Erad);
        double cosErad = Math.cos(Erad);
        double deltaE;
        int nrIterations = 0;
        do {
            double Hn = (Erad - eccentricity*sinErad - Mrad)/(1.0 - eccentricity*cosErad); 
            double In = (eccentricity*sinErad) / (2.0*(1.0 - eccentricity*cosErad));
            Erad = Erad - (Hn / (1.0 - Hn*In));
            sinErad = Math.sin(Erad);
            cosErad = Math.cos(Erad);
            deltaE = Erad - eccentricity*sinErad - Mrad;
            nrIterations++;
        } while ((Math.abs(deltaE) > maxError) && (nrIterations < 100));
        return Erad;
    }
    
    /**
     * Solve the hyperbolic version of Kepler's equation M = e*sinh(H) - H, 
     * where M is mean anomaly, H is hyperbolic anomaly, and e is eccentricity.
     * Uses Halley's method to solve Kepler's equation.
     *
     * @param Mrad mean anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @param maxError maximum error allowed [radians]
     * @return hyperbolic anomaly [radians]
     */
    public static double solveHyperbolicKeplerEquationHalley(double Mrad, double eccentricity, double maxError) {

        // Halley's method
        // https://en.wikipedia.org/wiki/Halley's_method
        // f(H) = e sinh(H) - H - M
        // f'(H) = e cosh(H) - 1
        // f''(H) = e sinh(H)
        double Hrad = Mrad;
        double sinhHrad = Math.sinh(Hrad);
        double coshHrad = Math.cosh(Hrad);
        double deltaH;
        int nrIterations = 0;
        do {
            double Hn = (eccentricity*sinhHrad - Hrad - Mrad)/(eccentricity*coshHrad - 1.0); 
            double In = (eccentricity*sinhHrad) / (2.0*(eccentricity*coshHrad - 1.0));
            Hrad = Hrad - (Hn / (1.0 - Hn*In));
            sinhHrad = Math.sinh(Hrad);
            coshHrad = Math.cosh(Hrad);
            deltaH = eccentricity*sinhHrad - Hrad - Mrad;
            nrIterations++;
        } while ((Math.abs(deltaH) > maxError) && (nrIterations < 100));
        return Hrad;
    }
    
    
    /**
     * Compute true anomaly from eccentric anomaly.
     *
     * @param Erad eccentric anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @return true anomaly [radians]
     */
    public static double computeTrueAnomaly(double Erad, double eccentricity) {

        // https://en.wikipedia.org/wiki/True_anomaly
        double x = Math.sqrt(1.0 - eccentricity) * Math.cos(Erad / 2.0);
        double y = Math.sqrt(1.0 + eccentricity) * Math.sin(Erad / 2.0);
        double Frad = 2.0 * Math.atan2(y, x);
        return Frad;
    }
    
    /**
     * Compute true anomaly from hyperbolic anomaly.
     *
     * @param Hrad hyperbolic anomaly [radians]
     * @param eccentricity eccentricity [-]
     * @return true anomaly [radians]
     */
    public static double computeTrueAnomalyHyperbolic(double Hrad, double eccentricity) {

        // https://space.stackexchange.com/questions/24646/finding-x-y-z-vx-vy-vz-from-hyperbolic-orbital-elements
        // https://physics.stackexchange.com/questions/247470/calculating-true-anomaly-of-a-hyperbolic-trajectory-from-time
        // tan(theta/2) = sqrt((e+1)/(e-1)) tanh(H/2)
        // nu = 2*atan(sqrt((e+1)/(e-1)) tanh(H/2)
        return 2.0 * Math.atan(Math.sqrt((eccentricity + 1.0)/(eccentricity - 1.0)) * Math.tanh(Hrad/2.0));
    }
    
    /**
     * Compute orbital elements for given Julian Ephemeris Date from orbital
     * parameters and their rates for Mercury, Venus, Earth, Mars, Jupiter,
     * Saturn, Uranus, Neptune, and Pluto. 
     * Additional terms for Jupiter, Saturn, Uranus, Neptune, and Pluto 
     * are taken into account.
     * 
     * The following orbital parameters are input:
     *   semi-major axis [au]
     *   eccentricity [-]
     *   inclination [degrees]
     *   mean longitude [degrees]
     *   longitude of perihelion [degrees]
     *   longitude of the ascending node [degrees]
     *   rate of semi-major axis [au/century]
     *   rate of eccentricity [/century]
     *   rate of inclination [degrees/century]
     *   rate of mean longitude [degrees/century]
     *   rate of longitude of perihelion [degrees/century]
     *   rate of longitude of the ascending node [degrees/century]
     *   additional parameter b for Jupiter through Pluto [degrees]
     *   additional parameter c for Jupiter through Neptune [degrees]
     *   additional parameter s for Jupiter through Neptune [degrees]
     *   additional parameter f for Jupiter through Neptune [degrees]
     * 
     * The following orbital elements are computed:
     *   semi-major axis [au]
     *   eccentricity [-]
     *   inclination [degrees]
     *   mean anomaly [degrees]
     *   argument of perihelion [degrees]
     *   longitude of ascending node [degrees]
     *
     * @param orbitPars orbital parameters and their rates (Keplerian elements)
     * @param Teph      Julian EphemerisUtil Date
     * @return orbital elements for given date
     */
    public static double[] computeOrbitalElementsForMajorPlanets(
            double[] orbitPars, double Teph) {

        // Computation of six orbital elements (or Keplerian elements) is based on
        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        double axisZero         = orbitPars[0]; // Semi-major axis [au]
        double eccentricityZero = orbitPars[1]; // Eccentricity [-]
        double inclinationZero  = orbitPars[2]; // Inclination [degrees]
        double meanLongZero     = orbitPars[3]; // Mean longitude [degrees]
        double longPeriZero     = orbitPars[4]; // Longitude of perihelion [degrees]
        double longNodeZero     = orbitPars[5]; // Longitude of the ascending node [degrees]
        double axisDot          = orbitPars[6]; // Semi-major axis [au/century]
        double eccentricityDot  = orbitPars[7]; // Eccentricity [/century]
        double inclinationDot   = orbitPars[8]; // Inclination [degrees/century]
        double meanLongDot      = orbitPars[9]; // Mean longitude [degrees/century]
        double longPeriDot      = orbitPars[10]; // Longitude of perihelion [degrees/century]
        double longNodeDot      = orbitPars[11]; // Longitude of the ascending node [degrees/century]

        // Additional parameters for the computation of mean anomaly
        double b = orbitPars[12];
        double c = orbitPars[13];
        double s = orbitPars[14];
        double f = orbitPars[15];
        
        // Compute number of centuries past J2000.0
        double T = computeNrCenturiesPastJ2000(Teph);

        // Compute the value of orbital elements
        double axis = axisZero + T * axisDot;
        double eccentricity = eccentricityZero + T * eccentricityDot;
        double inclination = inclinationZero + T * inclinationDot;
        double meanLong = meanLongZero + T * meanLongDot;
        double longPeri = longPeriZero + T * longPeriDot;
        double longNode = longNodeZero + T * longNodeDot;

        // Argument of perihelion [degrees]
        double argPerihelion = longPeri - longNode;

        // Ensure that argument of perihelion is between -180.0 and 180 degrees
        if (argPerihelion < -180.0) {
            argPerihelion += 360.0;
        }
        if (argPerihelion > 180.0) {
            argPerihelion -= 360.0;
        }

        // Compute mean anomaly [degrees]
        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        // Last three terms must be added for Jupiter through Pluto
        // when using the formulae for 3000 BC to 3000 AD
        double fTrad = Math.toRadians(f*T);
        double meanAnomaly = meanLong - longPeri
                + b*T*T + c*Math.cos(fTrad) + s*Math.sin(fTrad);

        // Ensure that mean anomaly is between -180.0 and 180.0 degrees
        meanAnomaly = meanAnomaly % 360.0;
        if (meanAnomaly < -180.0) {
            meanAnomaly += 360.0;
        }
        if (meanAnomaly > 180.0) {
            meanAnomaly -= 360.0;
        }
        
        // Six orbital elements (or Keplerian elements) for given date
        double[] orbitElements = new double[6];
        orbitElements[0] = axis;          // semi-major axis [au]
        orbitElements[1] = eccentricity;  // eccentricity [-]
        orbitElements[2] = inclination;   // inclination [degrees]
        orbitElements[3] = meanAnomaly;   // mean anomaly [degrees]
        orbitElements[4] = argPerihelion; // argument of perihelion [degrees]
        orbitElements[5] = longNode;      // longitude of ascending node [degrees]
        
        // Return orbital elements for given date
        return orbitElements;
    }
    
    /**
     * Compute orbital elements for given Julian Ephemeris Date from orbital 
     * parameters including date of perihelion passage and mean motion.
     * 
     * The following orbital parameters are input:
     *   semi-major axis [au]
     *   eccentricity [-]
     *   inclination [degrees]
     *   argument of perihelion [degrees]
     *   longitude of ascending node [degrees]
     *   time of perihelion passage [Julian EphemerisUtil Date]
     *   mean motion [degrees/day]
     * 
     * The following orbital elements are computed:
     *   semi-major axis [au]
     *   eccentricity [-]
     *   inclination [degrees]
     *   mean anomaly [degrees]
     *   argument of perihelion [degrees]
     *   longitude of ascending node [degrees]
     *
     * @param orbitPars  orbital parameters including peri passage and mean motion
     * @param Teph       Julian EphemerisUtil Date
     * @return orbit elements for given date
     */
    public static double[] computeOrbitalElementsFromPerihelionPassage(
            double[] orbitPars, double Teph) {
        
        // Orbital parameters
        double axis          = orbitPars[0]; // semi-major axis [au]
        double eccentricity  = orbitPars[1]; // eccentricity [-]
        double inclination   = orbitPars[2]; // inclination [degrees]
        double argPerihelion = orbitPars[3]; // argument of perihelion [degrees]
        double longNode      = orbitPars[4]; // longitude of ascending node [degrees]
        double Tperi         = orbitPars[5]; // time of perihelion passage [JED]
        double meanMotion    = orbitPars[6]; // mean motion [degrees/day]

        // Ensure that argument of perihelion is between -180.0 and 180 degrees
        if (argPerihelion < -180.0) {
            argPerihelion += 360.0;
        }
        if (argPerihelion > 180.0) {
            argPerihelion -= 360.0;
        }

        // Compute mean anomaly
        double meanAnomaly = (Teph - Tperi) * meanMotion;

        // Ensure that mean anomaly is between -180.0 and 180.0 degrees (ellipitcal orbit only)
        if (axis > 0.0) {
            // Orbit is elliptical
            meanAnomaly = meanAnomaly % 360.0;
            if (meanAnomaly < -180.0) {
                meanAnomaly += 360.0;
            }
            if (meanAnomaly > 180.0) {
                meanAnomaly -= 360.0;
            }
        }

        // Six orbital elements for given date
        double[] orbitElements = new double[6];
        orbitElements[0] = axis;          // semi-major axis [au]
        orbitElements[1] = eccentricity;  // eccentricity [-]
        orbitElements[2] = inclination;   // inclination [degrees]
        orbitElements[3] = meanAnomaly;   // mean anomaly [degrees]
        orbitElements[4] = argPerihelion; // argument of perihelion [degrees]
        orbitElements[5] = longNode;      // longitude of ascending node [degrees]
        
        // Return orbital elements for given date
        return orbitElements;
    }
    
    /**
     * Compute orbital elements for given date from orbital parameters.
     * Depending on the number of orbital parameters either the computation
     * is applied for major planets (including Pluto) or the computation is
     * based on the perihelion passage.
     * 
     * @param orbitPars  orbit parameters (number of parameters is either 16 or 7)
     * @param date       date to compute orbit elements
     * @return orbit elements for given date
     */
    public static double[] computeOrbitalElements(double[] orbitPars, GregorianCalendar date) {
        
        // Check number of orbital parameters
        if (orbitPars.length != 16 && orbitPars.length != 7) {
            throw new IllegalArgumentException("Wrong number of orbital parameters");
        }
        
        // Compute Julian Ephemeris Date
        double Teph = JulianDateConverter.convertCalendarToJulianDate(date);
        
        // Compute orbital elements
        if (orbitPars.length == 16) {
            
            // Orbital parameters contain orbital elements (or Keplerian elements)
            // and their rate. Additional parameters are included for Jupiter,
            // Saturn, Uranus, Neptune, and Pluto.
            // Orbital elements are computed for Julian Ephemeris Date.
            // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
            return computeOrbitalElementsForMajorPlanets(orbitPars, Teph);
        } else {
            
            // Orbital parameters contain orbital elements including
            // date of perihelion passage and mean motion.
            // Orbital elements are computed for Julian Ephemeris Date.
            return computeOrbitalElementsFromPerihelionPassage(orbitPars, Teph);
        }
    }
    
    
    /**
     * Compute heliocentric position in orbital plane where x-axis is aligned
     * from the focus to the perihelion. Elliptic orbit.
     *
     * @param axis semi-major axis [au] (must be positive)
     * @param eccentricity eccentricity [-] (must be between zero and one)
     * @param Erad eccentric anamaly [radians]
     * @return heliocentric coordinates [au]
     */
    public static Vector3D computeHeliocentricPosition(double axis,
                                                       double eccentricity, double Erad) {

        // Compute the planet's heliocentric coordinates in its orbital plane
        // where the x-axis is aligned from the focus to the perihelion
        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        double xPos = axis * (Math.cos(Erad) - eccentricity);
        double yPos = axis * Math.sqrt(1.0 - eccentricity * eccentricity) * Math.sin(Erad);
        double zPos = 0.0;
        return new Vector3D(xPos, yPos, zPos);
    }

    /**
     * Compute heliocentric position in orbital plane where x-axis is aligned
     * from the focus to the perihelion. Hyperbolic orbit.
     *
     * @param axis semi-major axis [au] (must be negative)
     * @param eccentricity eccentricity [-] (must be larger than one)
     * @param Hrad hyperbolic anomaly [radians]
     * @return heliocentric coordinates [au]
     */
    public static Vector3D computeHeliocentricPositionHyperbolic(double axis,
                                                                 double eccentricity, double Hrad) {

        // https://studentportalen.uu.se/portal/portal/uusp/student/
        // filearea?uusp.portalpage=true&mode=filearea36238&toolMode=studentUse&entityId=59763&toolAttachmentId=36238
        // See celmek2.pptx (slide 29)
        // x = |a| * (e - cosh(H))
        // y = |a| * sqrt(e^2 - 1) * sinh(H)
        double xPos = Math.abs(axis) * (eccentricity - Math.cosh(Hrad));
        double yPos = Math.abs(axis) * Math.sqrt(eccentricity * eccentricity - 1.0) * Math.sinh(Hrad);
        double zPos = 0.0;
        return new Vector3D(xPos, yPos, zPos);
    }
    
    /**
     * Compute velocity relative to center body in orbital plane where x-axis is aligned
     * from the focus to the periapsis. Elliptic orbit.
     *
     * @param mu standard gravitational parameter of center body [m3/s2]
     * @param a semi-major axis [m]
     * @param e eccentricity [-]
     * @param Frad true anomaly [radians]
     * @return velocity in heliocentric coordinates [m/s]
     */
    public static Vector3D computeVelocityRelativeToCenterbody(double mu, double a,
                                                               double e, double Frad) {

        // http://exoplanets.astro.yale.edu/workshop/EPRV/Bibliography_files/Radial_Velocity.pdf
        // See formula (9)
        double v = Math.sqrt(mu * (1.0 / (a * (1.0 - e*e))));
        double xVelo = -v * Math.sin(Frad);
        double yVelo = v * (Math.cos(Frad) + e);
        return new Vector3D(xVelo, yVelo, 0.0);
    }

    /**
     * Compute velocity relative to center body in orbital plane where x-axis is aligned
     * from the focus to the periapsis. Hyperbolic orbit.
     *
     * @param mu standard gravitational parameter of center body [m3/s2]
     * @param a semi-major axis [m]
     * @param e eccentricity [-]
     * @param Hrad hyperbolic anomaly [radians]
     * @return velocity in heliocentric coordinates [m/s]
     */
    public static Vector3D computeVelocityRelativeToCenterBodyHyperbolic(double mu, double a,
                                                                         double e, double Hrad) {

        // https://studentportalen.uu.se/portal/portal/uusp/student/
        // filearea?uusp.portalpage=true&mode=filearea36238&toolMode=studentUse&entityId=59763&toolAttachmentId=36238
        // See celmek2.pptx (slide 29)
        // sqrt(|a| * (e^2 - 1) * mu) = |a|^2 * sqrt(e^2 - 1) * (e * cosh(H) - 1) * Hdot
        // xVelo = -|a| * Hdot * sinh(H)
        // yVelo = |a| * sqrt(e^2 - 1) * Hdot * cosh(H)
        double Hdot = Math.sqrt(Math.abs(a) * (e*e - 1.0) * mu) /
                      (a*a * Math.sqrt(e*e - 1.0) * (e*Math.cosh(Hrad) - 1.0));
        double xVelo = -Math.abs(a) * Hdot * Math.sinh(Hrad);
        double yVelo = Math.abs(a) * Math.sqrt(e*e - 1.0) * Hdot * Math.cosh(Hrad);
        return new Vector3D(xVelo, yVelo, 0.0);
    }
    
    /**
     * Convert heliocentric coordinates to coordinates in the J2000 ecliptic
     * plane, with the x-axis aligned toward the equinox.
     *
     * @param helioCentric heliocentric coordinates [au]
     * @param argPerihelion argument of perihelion [degrees]
     * @param longNode longitude of the ascending node [degrees]
     * @param inclination inclination [degrees]
     * @return coordinates in the J2000 ecliptic plane [au]
     */
    public static Vector3D convertHeliocentricToEclipticPlaneCoordinates(
            Vector3D helioCentric, double argPerihelion,
            double longNode, double inclination) {

        // Heliocentric coordinates [au]
        double xPrime = helioCentric.getX();
        double yPrime = helioCentric.getY();
        double zPrime = helioCentric.getZ();

        // Compute the coordinates in the J2000 ecliptic plane, with the x-axis
        // aligned toward the equinox
        double sinArgPerihelion = Math.sin(Math.toRadians(argPerihelion));
        double cosArgPerihelion = Math.cos(Math.toRadians(argPerihelion));
        double sinLongNode = Math.sin(Math.toRadians(longNode));
        double cosLongNode = Math.cos(Math.toRadians(longNode));
        double sinInclination = Math.sin(Math.toRadians(inclination));
        double cosInclination = Math.cos(Math.toRadians(inclination));
        double xEcl
                = (cosArgPerihelion * cosLongNode - sinArgPerihelion * sinLongNode * cosInclination) * xPrime
                + (-sinArgPerihelion * cosLongNode - cosArgPerihelion * sinLongNode * cosInclination) * yPrime;
        double yEcl
                = (cosArgPerihelion * sinLongNode + sinArgPerihelion * cosLongNode * cosInclination) * xPrime
                + (-sinArgPerihelion * sinLongNode + cosArgPerihelion * cosLongNode * cosInclination) * yPrime;
        double zEcl
                = (sinArgPerihelion * sinInclination) * xPrime
                + (cosArgPerihelion * sinInclination) * yPrime;

        // coordinates in the J2000 ecliptic plane [au]
        return new Vector3D(xEcl, yEcl, zEcl);
    }
    
    
    /**
     * Compute position in orbit plane from position in ecliptic plane.
     *
     * @param position position of planet in m
     * @param longNode longitude of ascending node [degrees]
     * @param inclination inclination [degrees]
     * @param argPerihelion argument of perihelion [degrees
     * @return position in orbit plane (typically z will be near zero)
     * Author: Marco
     */
    public static Vector3D computeOrbitPositionFromEclipticPosition(Vector3D position,
                                                                    double longNode, double inclination, double argPerihelion) {
        double x, y, z;
        // set up transform matrix. See aprx_pos_planets.pdf, formula 8-33
        // inverse matrix is Rz(argPerihelion)*Rx(inclination)*Rz(longNode)
        // with, for our purpose:
        // Rz(a) = [ cos(a) sin(a) 0; -sin(a) cos(a) 0; 0 0 1 ];
        // Rx(a) = [ 1 0 0; 0 cos(a) sin(a) ; 0 -sin(a) cos(a) ];
        //
        double m_xx, m_xy, m_xz;
        double m_yx, m_yy, m_yz;
        double m_zx, m_zy, m_zz;
        double m_o = Math.toRadians(longNode);
        double m_i = Math.toRadians(inclination);
        double m_w = Math.toRadians(argPerihelion);
        m_xx = Math.cos(m_w) * Math.cos(m_o) - Math.sin(m_w) * Math.cos(m_i) * Math.sin(m_o);
        m_xy = Math.cos(m_w) * Math.sin(m_o) + Math.sin(m_w) * Math.cos(m_i) * Math.cos(m_o);
        m_xz = Math.sin(m_w) * Math.sin(m_i);
        m_yx = -Math.sin(m_w) * Math.cos(m_o) - Math.cos(m_w) * Math.cos(m_i) * Math.sin(m_o);
        m_yy = -Math.sin(m_w) * Math.sin(m_o) + Math.cos(m_w) * Math.cos(m_i) * Math.cos(m_o);
        m_yz = Math.cos(m_w) * Math.sin(m_i);
        m_zx = Math.sin(m_i) * Math.sin(m_o);
        m_zy = -Math.sin(m_i) * Math.cos(m_o);
        m_zz = Math.cos(m_i);
        // compute orbit position through matrix transform
        x = m_xx * position.getX() + m_xy * position.getY() + m_xz * position.getZ();
        y = m_yx * position.getX() + m_yy * position.getY() + m_yz * position.getZ();
        z = m_zx * position.getX() + m_zy * position.getY() + m_zz * position.getZ();

        return new Vector3D(x, y, z);
    }

    /**
     * Compute position in ecliptic plane from position in orbit plane.
     *
     * @param position position of planet in m
     * @param longNode longitude of ascending node [degrees]
     * @param inclination inclination [degrees]
     * @param argPerihelion argument of perihelion [degrees]
     * @return position in ecliptic plane
     * Author: Marco
     */
    public static Vector3D computeEclipticPositionFromOrbitPosition(Vector3D position,
                                                                    double longNode, double inclination, double argPerihelion) {
        double x, y, z;
        // set up transform matrix. See aprx_pos_planets.pdf, formula 8-33
        // matrix is Rz(-longNode)*Rx(-inclination)*Rz(-argPerihelion)
        // with, for our purpose:
        // Rz(a) = [ cos(a) sin(a) 0; -sin(a) cos(a) 0; 0 0 1 ];
        // Rx(a) = [ 1 0 0; 0 cos(a) sin(a) ; 0 -sin(a) cos(a) ];
        //
        double m_xx, m_xy, m_xz;
        double m_yx, m_yy, m_yz;
        double m_zx, m_zy, m_zz;
        double m_o = Math.toRadians(longNode);
        double m_i = Math.toRadians(inclination);
        double m_w = Math.toRadians(argPerihelion);
        m_xx = Math.cos(m_w) * Math.cos(m_o) - Math.sin(m_w) * Math.cos(m_i) * Math.sin(m_o);
        m_xy = -Math.sin(m_w) * Math.cos(m_o) - Math.cos(m_w) * Math.cos(m_i) * Math.sin(m_o);
        m_xz = Math.sin(m_o) * Math.sin(m_i);
        m_yx = Math.cos(m_w) * Math.sin(m_o) + Math.sin(m_w) * Math.cos(m_i) * Math.cos(m_o);
        m_yy = -Math.sin(m_w) * Math.sin(m_o) + Math.cos(m_w) * Math.cos(m_i) * Math.cos(m_o);
        m_yz = -Math.cos(m_o) * Math.sin(m_i);
        m_zx = Math.sin(m_i) * Math.sin(m_w);
        m_zy = Math.sin(m_i) * Math.cos(m_w);
        m_zz = Math.cos(m_i);
        // compute orbit position through matrix transform
        x = m_xx * position.getX() + m_xy * position.getY() + m_xz * position.getZ();
        y = m_yx * position.getX() + m_yy * position.getY() + m_yz * position.getZ();
        z = m_zx * position.getX() + m_zy * position.getY() + m_zz * position.getZ();

        return new Vector3D(x, y, z);
    }


    /**
     * Compute position from orbital elements.
     *
     * @param orbitElements orbital elements (Keplerian elements)
     * @return position (x,y,z) of planet in m
     */
    public static Vector3D computePosition(double[] orbitElements) {

        // Computation of position of planet is based on
        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        // Orbital elements (Keplerian elements)
        double axis          = orbitElements[0]; // semi-major axis [au]
        double eccentricity  = orbitElements[1]; // eccentricity [-]
        double inclination   = orbitElements[2]; // inclination [degrees]
        double meanAnomaly   = orbitElements[3]; // mean anomaly [degrees]
        double argPerihelion = orbitElements[4]; // argument of perihelion [degrees]
        double longNode      = orbitElements[5]; // longitude of ascending node [degrees]

        // Mean anomaly in radians
        double Mrad = Math.toRadians(meanAnomaly);

        // Distinguish between elliptic and hyperbolic orbit
        Vector3D helioCentricPosition;
        if (axis >= 0.0) {
            // Elliptic orbit
            // Obtain eccentric anomaly E by solving
            // Kepler's equation M = E - eccentricity * sin(E)
            // Use Halley's method
            double Erad = solveKeplerEquationHalley(Mrad, eccentricity, 1E-14);

            // Compute the planet's heliocentric coordinates in its orbital plane
            // where the x-axis is aligned from the focus to the perihelion
            helioCentricPosition = computeHeliocentricPosition(axis, eccentricity, Erad);
        }
        else {
            // Hyperbolic orbit
            // Obtain hyperbolic eccentric anomaly H by solving the hyperbolic
            // Kepler equation M = eccentricity * sinh(H) - H
            // Use Halley's method
            double Hrad = solveHyperbolicKeplerEquationHalley(Mrad, eccentricity, 1E-14);

            // Compute the planet's heliocentric coordinates in its orbital plane
            // where the x-axis is aligned from the focus to the perihelion
            helioCentricPosition = computeHeliocentricPositionHyperbolic(axis, eccentricity, Hrad);
        }

        // Compute the coordinates in the J2000 ecliptic plane, with the x-axis
        // aligned toward the equinox
        Vector3D eclipticPlanePosition
                = convertHeliocentricToEclipticPlaneCoordinates(
                        helioCentricPosition, argPerihelion, longNode, inclination);

        // Position of the planet in m
        Vector3D position
                = eclipticPlanePosition.scalarProduct(SolarSystemParameters.ASTRONOMICALUNIT);

        // Return (x,y,z) position of planet in m
        return position;
    }

    /**
     * Compute velocity relative to center body from orbital elements.
     * Velocity is computed by differentiation over position.
     * 
     * @param mu standard gravitational parameter of center body [m3/m2]
     * @param orbitElements orbital elements (Keplerian elements)
     * @return velocity (x,y,z) of body [m/s]
     */
    public static Vector3D computeVelocityByDifferentiation(double mu, double[] orbitElements) {
        
        // Computation of position of planet is based on
        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        // Orbital elements (Keplerian elements)
        double axis          = orbitElements[0]; // semi-major axis [au]
        double eccentricity  = orbitElements[1]; // eccentricity [-]
        double inclination   = orbitElements[2]; // inclination [degrees]
        double meanAnomaly   = orbitElements[3]; // mean anomaly [degrees]
        double argPerihelion = orbitElements[4]; // argument of perihelion [degrees]
        double longNode      = orbitElements[5]; // longitude of ascending node [degrees]
        
        // Current position
        Vector3D currentPosition = computePosition(orbitElements);
        
        // Keplerian elements to compute position before current position
        double[] orbitElementsFormerPosition = new double[6];
        orbitElementsFormerPosition[0] = axis;
        orbitElementsFormerPosition[1] = eccentricity;
        orbitElementsFormerPosition[2] = inclination;
        orbitElementsFormerPosition[3] = meanAnomaly - 0.05;
        orbitElementsFormerPosition[4] = argPerihelion;
        orbitElementsFormerPosition[5] = longNode;
        
        // Position before current position
        Vector3D formerPosition = computePosition(orbitElementsFormerPosition);
        
        // Keplerian elements to compute position after current position
        double[] orbitElementsFollowingPosition = new double[6];
        orbitElementsFollowingPosition[0] = axis;
        orbitElementsFollowingPosition[1] = eccentricity;
        orbitElementsFollowingPosition[2] = inclination;
        orbitElementsFollowingPosition[3] = meanAnomaly + 0.05;
        orbitElementsFollowingPosition[4] = argPerihelion;
        orbitElementsFollowingPosition[5] = longNode;
        
        // Position following current position
        Vector3D followingPosition = computePosition(orbitElementsFollowingPosition);
        
        // Direction of velocity
        Vector3D direction = (followingPosition.minus(formerPosition)).normalize();

        // Compute velocity in m/s based on semi-major axis and actual distance
        // https://en.wikipedia.org/wiki/Orbital_speed
        // Use current position to obtain the actual distance
        double a  = orbitElements[0] * SolarSystemParameters.ASTRONOMICALUNIT;
        double r  = currentPosition.magnitude();
        double v  = Math.sqrt(mu*(2.0/r - 1.0/a));
        
        // Compute velocity as a vector in m/s
        Vector3D velocity = direction.scalarProduct(v);
        
        // Return (x,y,z) velocity of body in m/s
        return velocity;
    }
    
    
    /**
     * Compute velocity relative to center body from orbital elements.
     *
     * @param mu standard gravitational parameter of center body [m3/m2]
     * @param orbitElements orbit elements (Keplerian elements)
     * @return velocity (x,y,z) of planet in m/s
     */
    public static Vector3D computeVelocity(double mu, double[] orbitElements) {

        // Computation of velocity of planet is based on
        // http://exoplanets.astro.yale.edu/workshop/EPRV/Bibliography_files/Radial_Velocity.pdf
        // Orbital elements (Keplerian elements)
        double axis          = orbitElements[0]; // semi-major axis [au]
        double eccentricity  = orbitElements[1]; // eccentricity [-]
        double inclination   = orbitElements[2]; // inclination [degrees]
        double meanAnomaly   = orbitElements[3]; // mean anomaly [degrees]
        double argPerihelion = orbitElements[4]; // argument of perihelion [degrees]
        double longNode      = orbitElements[5]; // longitude of ascending node [degrees]
                
        // Mean anomaly in radians
        double Mrad = Math.toRadians(meanAnomaly);

        // Semi-major axis in m
        double a = axis * SolarSystemParameters.ASTRONOMICALUNIT;

        // Distinguish between elliptic and hyperbolic orbit
        Vector3D heliocentricVelocity;
        if (axis >= 0.0) {
            // Elliptic orbit
            // Obtain the eccentric anomaly E by solving
            // Kepler's equation M = E - eccentricity * sin(E)
            double Erad = solveKeplerEquationHalley(Mrad, eccentricity, 1E-14);

            // True anomaly in radians
            double Frad = computeTrueAnomaly(Erad, eccentricity);

            // Heliocentric velocity in m/s
            heliocentricVelocity = computeVelocityRelativeToCenterbody(mu, a, eccentricity, Frad);
        }
        else {
            // Hyperbolic orbit
            // Obtain hyperbolic eccentric anomaly H by solving the hyperbolic
            // Kepler equation M = eccentricity * sinh(H) - H
            // Use Halley's method
            double Hrad = solveHyperbolicKeplerEquationHalley(Mrad, eccentricity, 1E-14);

            // Heliocentric velocity in m/s
            heliocentricVelocity
                    = computeVelocityRelativeToCenterBodyHyperbolic(mu, a, eccentricity, Hrad);
        }


        // Compute velocity [m/s] in the J2000 ecliptic plane, with the x-axis
        // aligned toward the equinox
        Vector3D eclipticPlaneVelocity
                = convertHeliocentricToEclipticPlaneCoordinates(
                        heliocentricVelocity, argPerihelion, longNode, inclination);

        // Return (x,y,z) velocity of planet in m/s
        return eclipticPlaneVelocity;
    }

    /**
     * Compute orbit from orbital elements.
     *
     * @param orbitElements orbit elements (Keplerian elements)
     * @return position (x,y,z) of planet in m
     */
    public static Vector3D[] computeOrbit(double[] orbitElements) {
        
        // Computation of position of planet is based on
        // https://ssd.jpl.nasa.gov/txt/aprx_pos_planets.pdf
        // Keplerian elements
        double axis          = orbitElements[0]; // semi-major axis [au]
        double eccentricity  = orbitElements[1]; // eccentricity [-]
        double inclination   = orbitElements[2]; // inclination [degrees]
        double meanAnomaly   = orbitElements[3]; // mean anomaly [degrees]
        double argPerihelion = orbitElements[4]; // argument of perihelion [degrees]
        double longNode      = orbitElements[5]; // longitude of ascending node [degrees]

        // Mean anomaly in radians
        double Mrad = Math.toRadians(meanAnomaly);

        // Obtain (hyperbolic) eccentric anomaly E by solving Kepler's equation
        double Erad;
        if (axis >= 0.0) {
            // Elliptic orbit
            // Obtain eccentric anomaly E by solving
            // Kepler's equation M = E - eccentricity * sin(E)
            // Use Halley's method
            Erad = solveKeplerEquationHalley(Mrad, eccentricity, 1E-14);
        }
        else {
            // Hyperbolic orbit
            // Obtain hyperbolic eccentric anomaly H by solving the hyperbolic
            // Kepler equation M = eccentricity * sinh(H) - H
            // Use Halley's method
            Erad = solveHyperbolicKeplerEquationHalley(Mrad, eccentricity, 1E-14);
        }
        
        // Compute orbit (360 segments)
        Vector3D[] orbit = new Vector3D[361];
        double EradStep = 2.0*Math.PI/(orbit.length-1);
        double EradInit = Erad;

        if (axis < 0.0) {
            int nrSteps = (int) Math.floor((Erad + Math.PI) / EradStep);
            EradInit = -Erad + nrSteps*EradStep;
        }
        for (int i = 0; i < orbit.length; i++) {
            
            // Compute the planet's heliocentric coordinates in its orbital plane
            // where the x-axis is aligned from the focus to the perihelion
            Vector3D helioCentricPosition;
            if (axis >= 0.0) {
                // Elliptical orbit
                helioCentricPosition
                    = computeHeliocentricPosition(axis, eccentricity, EradInit + i*EradStep);
            }
            else {
                // Hyperbolic orbit
                helioCentricPosition
                        = computeHeliocentricPositionHyperbolic(axis, eccentricity, EradInit - i*EradStep);
            }

            // Compute the coordinates in the J2000 ecliptic plane, with the x-axis
            // aligned toward the equinox
            Vector3D eclipticPlanePosition
                    = convertHeliocentricToEclipticPlaneCoordinates(
                            helioCentricPosition, argPerihelion, longNode, inclination);

            // Position of the planet in m
            orbit[i] = eclipticPlanePosition.scalarProduct(SolarSystemParameters.ASTRONOMICALUNIT);
        }
        return orbit;
    }

    /**
     * Compute orbital elements from position and velocity of body.
     * 
     * @param mu Standard gravitational parameter of center body [m3/s2]
     * @param position Position of body relative to center body [m]
     * @param velocity Velocity of body relative to center body [m/s]
     * @return orbitElements orbit elements relative to center body
     */
    public static double[] computeOrbitalElementsFromPositionVelocity(double mu,
                                                                      Vector3D position, Vector3D velocity) {
        
        // http://ccar.colorado.edu/asen5070/handouts/cart2kep2002.pdf
        
        // Compute the specific angular momentum, h, and check for a degenerate orbit
        Vector3D angularMomentum = position.crossProduct(velocity);
        double h = angularMomentum.magnitude();
        double hx = angularMomentum.getX();
        double hy = angularMomentum.getY();
        double hz = angularMomentum.getZ();
        
        // Compute the radius, r [m], and velocity, v [m/s]
        double r = position.magnitude();
        double v = velocity.magnitude();
        
        // Compute the specific energy E
        double E = ((v*v)/2.0) - (mu/r);
        
        // Compute semi-major axis, a [m] and axis [AU]
        double a = -mu/(2.0*E);
        double axis = a/ SolarSystemParameters.ASTRONOMICALUNIT;
        
        // Compute eccentricity [-]
        double e = Math.sqrt(1.0 - (h*h)/(a*mu));
        double eccentricity = e;
        
        // Compute inclination, i [rad], and convert to degrees
        // i is in the range 0.0 through pi
        // Inclination is in the range 0 through 180 degrees
        double i = Math.acos(hz/h);
        double inclination = Math.toDegrees(i);
        
        // Compute right ascension of the the ascending node, Omega [rad]
        // Note that the result of atan2 is in the range -pi through pi
        double Omega = Math.atan2(hx, -hy);
        double longNode = Math.toDegrees(Omega);
        
        // Compute argument of latitude, Omega + nu [degrees]
        double px = position.getX();
        double py = position.getY();
        double pz = position.getZ();
        double argy = pz / Math.sin(i);
        double argx = px * Math.cos(Omega) + py * Math.sin(Omega);
        double argLatitude = Math.toDegrees(Math.atan2(argy, argx));

        // Compute true anomaly, nuRad [radians] and nuDegrees [degrees]
        double p = a*(1.0 - e*e);
        argy = (Math.sqrt(p/mu))*(velocity.dotProduct(position));
        argx = p - r;
        double nuRad = Math.atan2(argy, argx);
        double nuDegrees = Math.toDegrees(nuRad);
        
        // Compute argument of perihelion [degrees]
        double argPerihelion = argLatitude - nuDegrees;
        
        // Ensure that argument of perihelion is between -180.0 and 180 degrees
        if (argPerihelion < -180.0) {
            argPerihelion += 360.0;
        }
        if (argPerihelion > 180.0) {
            argPerihelion -= 360.0;
        }

        // Compute mean anomaly M from eccentric anomaly EA
        // Distinguish between elliptic and hyperbolic orbit
        double meanAnomaly;
        if (E <= 0.0) {
            // Elliptic orbit
            // tan(EA/2) = sqrt((1-e)/(1+e)) * tan(nu/2)
            // EA is in the same half plane as nu.
            double arg = Math.sqrt((1.0 - e)/(1.0 + e)) * Math.tan(nuRad/2.0);
            double EA = 2.0*Math.atan(arg);

            // Kepler's equation M = EA - e*sin(EA)
            // http://www.bogan.ca/orbits/kepler/orbteqtn.html
            meanAnomaly = Math.toDegrees(EA - e*Math.sin(EA));

            // Ensure that -180 <= mean anomaly <= +180
            meanAnomaly = meanAnomaly % 360.0;
            if (meanAnomaly < -180.0) {
                meanAnomaly += 360.0;
            }
            if (meanAnomaly > 180.0) {
                meanAnomaly -= 360.0;
            }
        }
        else {
            // Hyperbolic orbit
            // http://www.bogan.ca/orbits/kepler/orbteqtn.html
            // cosh(EA) = (e + cos(nu))/(1 + e cos(nu))
            double arg = (e + Math.cos(nuRad)) / (1.0 + e*Math.cos(nuRad));
            // https://en.wikipedia.org/wiki/Inverse_hyperbolic_functions
            // arcosh(x) = ln(x + sqrt(x^2 - 1))
            double EA = Math.log(arg + Math.sqrt(arg*arg - 1.0));

            // Eccentric Anomaly EA must be in the same half plane as true anomaly nu
            if (nuRad < 0.0) {
                EA = -EA;
            }

            // Hyperbolic Kepler's equation M = e*sinh(EA) - EA
            // http://www.bogan.ca/orbits/kepler/orbteqtn.html
            meanAnomaly = Math.toDegrees(e*Math.sinh(EA) - EA);
        }
        
        // Six Keplerian elements for given date
        double[] orbitElements = new double[6];
        orbitElements[0] = axis;          // semi-major axis [au]
        orbitElements[1] = eccentricity;  // eccentricity [-]
        orbitElements[2] = inclination;   // inclination [degrees]
        orbitElements[3] = meanAnomaly;   // mean anomaly [degrees]
        orbitElements[4] = argPerihelion; // argument of perihelion [degrees]
        orbitElements[5] = longNode;      // longitude of ascending node [degrees]
        
        // Return Keplerian elements
        return orbitElements;
    }
    
    /**
     * Compute orbit relative to center body from position and velocity.
     *
     * @param mu standard gravitational parameter of center body [m3/s2]
     * @param position position (x,y,z) relative to center body [m]
     * @param velocity velocity (x,y,z) relative to center body [m/s]
     * @return orbit positions (x,y,z) of body relative to center body [m]
     */
    public static Vector3D[] computeOrbit(double mu, Vector3D position, Vector3D velocity) {
        double[] orbitElements = computeOrbitalElementsFromPositionVelocity(mu,position,velocity);
        return computeOrbit(orbitElements);
    }
}
