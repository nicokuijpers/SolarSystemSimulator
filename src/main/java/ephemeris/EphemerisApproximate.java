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

import java.util.*;

/**
 * Approximate Ephemeris for major planets including Pluto.
 * This ephemeris is valid valid for the time-interval 3000 BC - 3000 AD.
 * @author Nico Kuijpers
 */
public class EphemerisApproximate implements IEphemeris {

    // First valid date
    private final GregorianCalendar firstValidDate;
    
    // Last valid date
    private final GregorianCalendar lastValidDate;
    
    // Major planets for which ephemeris can be approximated
    private static List<String> majorPlanets;
    
    // Singleton instance
    private static IEphemeris instance = null;
    
    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisApproximate() {
        // Names of major planets for which ephemeris can be approximated
        majorPlanets = new ArrayList<>();
        majorPlanets.add("Mercury");
        majorPlanets.add("Venus");
        majorPlanets.add("EarthMoonBarycenter");
        majorPlanets.add("Mars");
        majorPlanets.add("Jupiter");
        majorPlanets.add("Saturn");
        majorPlanets.add("Uranus");
        majorPlanets.add("Neptune");
        majorPlanets.add("Pluto System");
     
        // First valid date 3000 BC
        firstValidDate = new GregorianCalendar(3000,0,1);
        firstValidDate.set(Calendar.ERA, GregorianCalendar.BC);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Last valid date 3000 AD
        lastValidDate = new GregorianCalendar(3000,0,1);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * Get instance of EphemerisApproximate.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisApproximate();
        }
        return instance;
    }
    
    @Override
    public GregorianCalendar getFirstValidDate() {
        return firstValidDate;
    }

    @Override
    public GregorianCalendar getLastValidDate() {
        return lastValidDate;
    }

    @Override
    public List<String> getBodies() {
        // Names of planets for which approximate ephemeris is known
        return Collections.unmodifiableList(majorPlanets);
    }

    @Override
    public Vector3D getBodyPosition(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!majorPlanets.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Approximate Ephemeris");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Approximate Ephemeris");
        }
        
        // Orbital parameters for given body
        double[] orbitPars = SolarSystemParameters.getInstance().getOrbitParameters(name);
        
        // Compute orbital elements for given date
        double[] orbitElements = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
        // Compute (x,y,z) position [m] from orbital elements
        Vector3D position = EphemerisUtil.computePosition(orbitElements);
        return position;
    }

    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!majorPlanets.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Approximate Ephemeris");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Approximate Ephemeris");
        }
        
        // Orbital parameters for given planet
        double[] orbitPars = SolarSystemParameters.getInstance().getOrbitParameters(name);
        
        // Compute orbital elements for given date
        double orbitElements[] = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
        // Compute (x,y,z) velocity [m/s] from orbital elements
        /*
        double mu = SolarSystemParameters.getInstance().getMu("Sun");
        Vector3D velocity = EphemerisUtil.computeVelocity(mu, orbitElements);
        // Vector3D velocity = EphemerisUtil.computeVelocityByDifferentiation(mu, orbitElements);
        return velocity;
        */

        // SEE MATLAB PROGRAM compute_position_velocity.m
        double axis          = orbitElements[0]; // semi-major axis [au]
        double eccentricity  = orbitElements[1]; // eccentricity [-]
        double inclination   = orbitElements[2]; // inclination [degrees]
        double meanAnomaly   = orbitElements[3]; // mean anomaly [degrees]
        double argPerihelion = orbitElements[4]; // argument of perihelion [degrees]
        double longNode      = orbitElements[5]; // longitude of ascending node [degrees]
        double a = axis;
        double e = eccentricity;
        double i = Math.toRadians(inclination);
        double M = Math.toRadians(meanAnomaly);
        double w = Math.toRadians(argPerihelion);
        double o = Math.toRadians(longNode);

        double a_dot = orbitPars[6];  // Semi-major axis [au/century]
        double e_dot = orbitPars[7];  // Eccentricity [/century]
        double i_dot_deg = orbitPars[8];  // Inclination [degrees/century]
        double Lm_dot_deg = orbitPars[9];  // Mean longitude [degrees/century]
        double Lp_dot_deg = orbitPars[10]; // Longitude of perihelion [degrees/century]
        double La_dot_deg = orbitPars[11]; // Longitude of the ascending node [degrees/century]
        double i_dot = Math.toRadians(i_dot_deg);
        double M_dot = Math.toRadians(Lm_dot_deg - Lp_dot_deg);
        double w_dot = Math.toRadians(Lp_dot_deg - La_dot_deg);
        double o_dot = Math.toRadians(La_dot_deg);

        double cos_w = Math.cos(w);
        double sin_w = Math.sin(w);
        double cos_o = Math.cos(o);
        double sin_o = Math.sin(o);
        double cos_i = Math.cos(i);
        double sin_i = Math.sin(i);

        // Compute E from M
        double E = EphemerisUtil.solveKeplerEquationHalley(M,e,1.0-14);
        double cos_E = Math.cos(E);
        double sin_E = Math.sin(E);
        double E_dot = (M_dot + e_dot*sin_E)/(1.0 - e*cos_E);

        // Compute position in orbit
        double x = a*(cos_E - e);
        double y = a*Math.sqrt(1.0 - e*e)*sin_E;

        // Compute derivatives to compute velocity analytically
        double Dx = a*(-sin_E)*E_dot + a_dot*cos_E - e_dot;
        // double Dy = a*(Math.sqrt(1.0 - e*e) * cos_E * E_dot +
        //         (-e*e_dot/Math.sqrt(1.0-e*e)) * sin_E) +
        //         a_dot*(Math.sqrt(1.0 - e*e) * sin_E);
        // FACTOR 2
        double Dy = a*(Math.sqrt(1.0 - e*e) * cos_E * E_dot +
                (-2.0*e*e_dot/Math.sqrt(1.0-e*e)) * sin_E) +
                a_dot*(Math.sqrt(1.0 - e*e) * sin_E);


        // Compute additional auxiliary variables
        double Dcos_w = -sin_w*w_dot;
        double Dsin_w =  cos_w*w_dot;
        double Dcos_o = -sin_o*o_dot;
        double Dsin_o =  cos_o*o_dot;
        double Dcos_i = -sin_i*i_dot;
        double Dsin_i =  cos_i*i_dot;

        double DXdt =
        (cos_w*cos_o - sin_w*sin_o*cos_i) * Dx +
        (cos_w*Dcos_o + Dcos_w*cos_o + -sin_w*(sin_o*Dcos_i+Dsin_o*cos_i) + -Dsin_w*sin_o*cos_i ) * x  +
        (-sin_w*cos_o - cos_w*sin_o*cos_i) * Dy +
        (-sin_w*Dcos_o + -Dsin_w*cos_o + -cos_w*(sin_o*Dcos_i+Dsin_o*cos_i) + -Dcos_w*sin_o*cos_i ) * y ;

        double DYdt =
        (cos_w*sin_o + sin_w*cos_o*cos_i) * Dx +
        (cos_w*Dsin_o + Dcos_w*sin_o + sin_w*(cos_o*Dcos_i + Dcos_o*cos_i) + Dsin_w*cos_o*cos_i) * x  +
        (-sin_w*sin_o + cos_w*cos_o*cos_i) * Dy +
        (-sin_w*Dsin_o + -Dsin_w*sin_o + cos_w*(cos_o*Dcos_i+Dcos_o*cos_i) + Dcos_w*(cos_o*cos_i)) * y;

        double DZdt =
        sin_w*sin_i * Dx +
        (sin_w*Dsin_i + Dsin_w*sin_i) * x +
        cos_w*sin_i * Dy +
        (cos_w*Dsin_i + Dcos_w*sin_i) * y;

        Vector3D velocity = new Vector3D(DXdt,DYdt,DZdt);
        double M_PER_AU = 149597870700.0;
        double SEC_PER_CTY = 86400*365.25*100;
        velocity = velocity.scalarProduct(M_PER_AU / SEC_PER_CTY);
        return velocity;
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!majorPlanets.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Approximate Ephemeris");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Approximate Ephemeris");
        }
        
        // Orbital parameters for given planet
        double[] orbitPars = SolarSystemParameters.getInstance().getOrbitParameters(name);
        
        // Compute orbital elements for given date
        double orbitElements[] = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
        // Compute (x,y,z) position [m] from orbital elements
        Vector3D position = EphemerisUtil.computePosition(orbitElements);
        
        // Compute (x,y,z) velocity [m/s] from orbital elements
        double mu = SolarSystemParameters.getInstance().getMu("Sun");
        Vector3D velocity = EphemerisUtil.computeVelocity(mu, orbitElements);
        
        // Position and velocity as array
        return new Vector3D[]{position,velocity};
    }

    @Override
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
