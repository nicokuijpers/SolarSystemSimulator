/*
 * Copyright (c) 2019 Nico Kuijpers and Marco Brassé
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
 * Ephemeris of Neptune moon Triton.
 * @author Marco Brassé
 */

public class EphemerisNeptuneMoons implements IEphemeris {

    /*
     *  Ephemeris of Triton from
     *  Analytical theory of motion and new ephemeris of Triton from observations,
     *  N.V. Emelyanov and M. Yu. Samorodov, MNRAS 454, 2205-2215 (2015).
     *  https://academic.oup.com/mnras/article/454/2/2205/2892708
     *  https://doi.org/10.1093/mnras/stv2116
     */

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // First valid date for accurate computation
    private final GregorianCalendar firstValidDateAccurate;

    // Last valid date for accurate computation
    private final GregorianCalendar lastValidDateAccurate;

    // Bodies for which ephemeris can be computed or approximated
    private static List<String> bodies;

    // Singleton instance
    private static IEphemeris instance = null;

    // Parameters of the model of solar motion
    // N.V. Emelyanov and M.Yu. Samorodov 2015, Table 1
    // Values from the mean elements
    private double a_accent     = 4504449760.0;   // [km]
    private double i_accent     = 27.923658;      // [degrees]
    private double omega_accent = 200.788305;     // [degrees]
    private double u0_accent    = 258.329018;     // [degrees]
    private double u_dot_accent = 0.00598182615;  // [degrees/day]

    // Coefficients for long-period perturbations from the Sun's attraction
    // N.V. Emelyanov and M.Yu. Samorodov 2015, Table 2
    private double KI[] = {0.0,0.00096486,0.00664662,0.00004687,0.00095975,-0.00037627,-0.00000225};
    private double KU[] = {-0.00012327,-0.00279453,-0.04335625,-0.00017215,-0.00233686,0.00170605,0.00000730};
    private double KO[] = {0.00063339,-0.00178908,-0.01560110,-0.00009186,-0.00218071,0.00096231,0.00000536};
    private double k1[] = {2,2,0,-2,2,0,-2};
    private double k2[] = {0,1,1,1,2,2,2};

    // Parameters of the model of Triton's motion obtained from numerical integration of the
    // equations of motion
    // N.V. Emelyanov and M.Yu. Samorodov 2015, Table 3
    private double a         = 354758.98;     // [km]
    private double I0        = 156.86561883;  // [degrees]
    private double u0        = 32.66861530;   // [degrees]
    private double u_dot     = 61.2586972029; // [degrees/day]
    private double omega0    = 72.89882654;   // [degrees]
    private double omega_dot = 0.00143381955; // [degrees/day]
    private double alpha     = Math.toRadians(299.46088779); // Converted from [degrees] to [radians]
    private double delta     = Math.toRadians(43.40655561);  // Converted from [degrees] to [radians]

    // N.V. Emelyanov and M.Yu. Samorodov 2015, Table 3, Formula 5
    private double t0  = 2378520.5; // [JED] Jan 25, 1800
    private double ts  = 2451545.0; // [JED] Jan 1, 2000

    // Convert from 'earth'to 'ecliptic'
    private double eclipticAngle = Math.toRadians(23.43929); // Converted from [degrees] to [radians]

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisNeptuneMoons() {

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.add("Triton");

        // First valid date 3000 BC
        firstValidDate = new GregorianCalendar(3000,0,1);
        firstValidDate.set(Calendar.ERA, GregorianCalendar.BC);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date 3000 AD
        lastValidDate = new GregorianCalendar(3000,0,1);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for accurate computation 1800 AD
        firstValidDateAccurate = new GregorianCalendar(1800,0,1);
        firstValidDateAccurate .setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for accurate computation 2200 AD
        lastValidDateAccurate = new GregorianCalendar(2200,0,1);
        lastValidDateAccurate .setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get instance of EphemerisNeptuneMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisNeptuneMoons();
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
        return Collections.unmodifiableList(bodies);
    }

    @Override
    public Vector3D getBodyPosition(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[0];
    }

    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[1];
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!bodies.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Neptune Moons Ephemeris");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Neptune Moons Ephemeris");
        }

        // Compute Ephemeris Time
        double ET = JulianDateConverter.convertCalendarToJulianDate(date);

        // Compute position and velocity
        return getPositionVelocityTriton(ET);
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

    /**
     * Compute position of Triton in J2000 mean ecliptic coordinates for given ephemeris time
     * @param ET ephemeris time.
     * @return position [m]
     */
    private Vector3D getPositionTriton(double ET)
    {
        /*
         *  Position of Triton for given Ephemeris Time is calculated according to
         *  Analytical theory of motion and new ephemeris of Triton from observations,
         *  N.V. Emelyanov and M. Yu. Samorodov, MNRAS 454, 2205-2215 (2015).
         *  https://academic.oup.com/mnras/article/454/2/2205/2892708
         *  https://doi.org/10.1093/mnras/stv2116
         */

        // Formula 5
        double u_accent = u0_accent + u_dot_accent*(ET-ts);
        double omega_line = omega0 + omega_dot*(ET-t0);

        // Formula 4
        double delta_It = 0.0;
        for (int i=0;i<7;i++)
        {
            double v = k1[i]*u_accent + k2[i]*(omega_accent - omega_line);
            delta_It = delta_It + KI[i]*Math.cos(Math.toRadians(v));
        }
        double delta_Ut = 0.0;
        for (int i=0;i<7;i++)
        {
            double v = k1[i]*u_accent + k2[i]*(omega_accent - omega_line);
            delta_Ut = delta_Ut + KU[i]*Math.sin(Math.toRadians(v));
        }
        double delta_Ot = 0.0;
        for (int i=0;i<7;i++)
        {
            double v = k1[i]*u_accent + k2[i]*(omega_accent-omega_line);
            delta_Ot = delta_Ot + KO[i]*Math.sin(Math.toRadians(v));
        }

        // Formula 2+6, k^=0.0
        double u = Math.toRadians(u0 + u_dot*(ET-t0) + delta_Ut);
        double I = Math.toRadians(I0 + delta_It);
        double omega = Math.toRadians(omega0 + omega_dot*(ET-t0) + delta_Ot);

        // Formula 3
        double x=a*(Math.cos(u)*Math.cos(omega)-Math.sin(u)*Math.sin(omega)*Math.cos(I));
        double y=a*(Math.cos(u)*Math.sin(omega)+Math.sin(u)*Math.cos(omega)*Math.cos(I));
        double z=a*Math.sin(u)*Math.sin(I);

        // Formula 1
        double alfa0=alpha;
        double delta0=delta;
        double xg=-Math.sin(alfa0)*x-Math.cos(alfa0)*Math.sin(delta0)*y+Math.cos(alfa0)*Math.cos(delta0)*z;
        double yg= Math.cos(alfa0)*x-Math.sin(alfa0)*Math.sin(delta0)*y+Math.sin(alfa0)*Math.cos(delta0)*z;
        double zg= Math.cos(delta0)*y + Math.sin(delta0)*z;

        // Convert from 'earth'to 'ecliptic'
        double cos_h=Math.cos(eclipticAngle);
        double sin_h=Math.sin(eclipticAngle);
        double yg1 = cos_h * yg + sin_h * zg;
        double zg1 =-sin_h * yg + cos_h * zg;
        yg = yg1;
        zg = zg1;

        // Position in m, already in J2000 mean ecliptic coordinates (meters)
        Vector3D position = new Vector3D(xg*1000.0, yg*1000.0, zg*1000.0);
        return position;
    }

    /**
     * Compute velocity of Triton in J2000 mean ecliptic coordinates for given ephemeris time.
     * @param ET ephemeris time.
     * @return velocity [m/s]
     */
    private Vector3D getVelocityTriton(double ET)
    {
        // Compute velocity by solving Gauss problem using two positions and time difference
        double mu = SolarSystemParameters.getInstance().getMu("Neptune");
        double deltaT = 4 * 60 * 60; // Time difference [s]
        double deltaET = deltaT/(24 * 60 * 60); // Time difference [days]
        Vector3D position1 = getPositionTriton(ET);
        Vector3D position2 = getPositionTriton(ET + deltaET);
        Vector3D velocity = EphemerisUtil.solveGaussProblem(position1, position2, deltaT, mu);
        return velocity;
    }

    /**
     * Compute position and velocity of Triton in J2000 mean ecliptic coordinates
     * for given ephemeris time.
     * @param ET ephemeris time
     * @return velocity [m/s]
     */
    private Vector3D[] getPositionVelocityTriton(double ET)
    {
        Vector3D position = getPositionTriton(ET);
        Vector3D velocity = getVelocityTriton(ET);
        return new Vector3D[]{position,velocity};
    }

    public static void main (String[] args) {
        EphemerisNeptuneMoons obj = new EphemerisNeptuneMoons();
        Vector3D[] posVel = obj.getPositionVelocityTriton(2451497.500000); // Triton
        System.out.println("Resulting positionVelocity [Triton]");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
    }
}

