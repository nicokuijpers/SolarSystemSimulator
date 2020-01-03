/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ephemeris;

import util.Vector3D;

import java.util.*;

/**
 * Main class for feasibility study
 * @author Marco Brassé
 */

public class EphemerisNeptuneMoons implements IEphemeris {

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

    private double KI[]={0.0,0.00096486,0.00664662,0.00004687,0.00095975,-0.00037627,-0.00000225};
    private double KU[]={-0.00012327,-0.00279453,-0.04335625,-0.00017215,-0.00233686,0.00170605,0.00000730};
    private double KO[]={0.00063339,-0.00178908,-0.01560110,-0.00009186,-0.00218071,0.00096231,0.00000536};
    private double k1[]={2,2,0,-2,2,0,-2};
    private double k2[]={0,1,1,1,2,2,2};
    private double a=354758.98;
    private double I0=156.86561883;
    private double u0=32.66861530;
    private double u_dot=61.2586972029;
    private double omega0=72.89882654;
    private double omega_dot=0.00143381955;
    private double alfa=Math.toRadians(299.46088779);
    private double delta=Math.toRadians(43.40655561);
    private double t0=2378520.5;
    private double ts=2451545.0;
    private double a_accent=4504449760.0;
    private double i_accent =27.923658;
    private double omega_accent=200.788305;
    private double u0_accent = 258.329018;
    private double u_dot_accent=0.00598182615;
    private double eclipticAngle = Math.toRadians(23.43929);

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

        // Standard gravitional parameters
        //double au = SolarSystemParameters.ASTRONOMICALUNIT;
        //double nrSecsDay = 86400.0;

    }


    /**
     * Get instance of EphemerisNeptunusMoons.
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

        // Determine satellite number
        int nsat;
        if ("Triton".equals(name)) nsat = 1;
        else /* choose Triton as default */ nsat = 1;

        // Check whether Accurate Ephemeris can be used
        if (!date.before(firstValidDateAccurate) && !date.after(lastValidDateAccurate)) {
            return getPositionVelocity(ET, nsat, 0);
        }
        else {
            return getPositionVelocity(ET, nsat, 0);
        }
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

    // comments TODO
    private Vector3D[] getPositionVelocity(double ET, int nsat, int not_used)
    {
        Vector3D posVec = getPosition(ET,nsat,0);
        Vector3D velVec = getVelocity(ET,nsat,0);
        return new Vector3D[]{posVec,velVec};
    }

    // comments TODO
    private Vector3D getPosition(double ET, int nsat, int not_used)
    {
        //formula 5
        double u_accent = u0_accent + u_dot_accent*(ET-ts);
        double omega_line = omega0 + omega_dot*(ET-t0);
        //formula 4
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

        //formula 2+6, k^=0.0
        double u = Math.toRadians(u0 + u_dot*(ET-t0) + delta_Ut);
        double I = Math.toRadians(I0 + delta_It);
        double omega = Math.toRadians(omega0 + omega_dot*(ET-t0) + delta_Ot);

        //formula 3
        double x=a*(Math.cos(u)*Math.cos(omega)-Math.sin(u)*Math.sin(omega)*Math.cos(I));
        double y=a*(Math.cos(u)*Math.sin(omega)+Math.sin(u)*Math.cos(omega)*Math.cos(I));
        double z=a*Math.sin(u)*Math.sin(I);

        //formula 1
        double alfa0=alfa;
        double delta0=delta;
        double xg=-Math.sin(alfa0)*x-Math.cos(alfa0)*Math.sin(delta0)*y+Math.cos(alfa0)*Math.cos(delta0)*z;
        double yg= Math.cos(alfa0)*x-Math.sin(alfa0)*Math.sin(delta0)*y+Math.sin(alfa0)*Math.cos(delta0)*z;
        double zg= Math.cos(delta0)*y + Math.sin(delta0)*z;

        //# convert from 'earth'to 'ecliptic'
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

    private Vector3D getVelocity(double ET, int nsat, int not_used)
    {
        //using differentiation
        double deltaT=0.01;
        Vector3D position0 = getPosition(ET-deltaT/2.0,nsat,0);
        Vector3D position1 = getPosition(ET+deltaT/2.0,nsat,0);
        double vx = (position1.getX()-position0.getX())/deltaT;
        double vy = (position1.getY()-position0.getY())/deltaT;
        double vz = (position1.getZ()-position0.getZ())/deltaT;
        // Velocity in m/s, already in J2000 mean ecliptic coordinates (meters/sec)
        Vector3D velocity = new Vector3D(vx/86400.0,vy/86400.0,vz/86400.0);
        return velocity;
    }

    public static void main (String[] args) {
        Vector3D[] posVel = new Vector3D[2];
        EphemerisNeptuneMoons obj=new EphemerisNeptuneMoons();
        posVel=obj.getPositionVelocity(2451497.500000, 1, 0); // Triton
        System.out.println("Resulting positionVelocity [Triton]");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
    }
}

