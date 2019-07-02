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
 *
 * @author Nico Kuijpers
 */
public class EphemerisSolarSystem implements IEphemeris {
    
    // Orbit period of the Moon in days
    // Obtained from from HORIZONS web interface 
    // https://ssd.jpl.nasa.gov/horizons.cgi#results.
    private static final double ORBITPERIODMOON = 27.321582;

    // Accurate Ephemeris for Sun, Moon, and major planets including Pluto
    private final IEphemeris ephemerisAccurate;
    
    // Approximate Ephemeris for major planets including Pluto
    private final IEphemeris ephemerisApproximate;

    // Ephemeris for Galilean moons
    private final IEphemeris ephemerisGalileanMoons;
    
    // Solar system parameters
    private final SolarSystemParameters solarSystemParameters;
    
    // First valid date
    private final GregorianCalendar firstValidDate;
    
    // Last valid date
    private final GregorianCalendar lastValidDate;
    
    // Bodies for which ephemeris can be computed or approximated
    private static List<String> bodies;
    
    // Singleton instance
    private static IEphemeris instance = null;
    
    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisSolarSystem() {
        
        // Accurate Ephemeris for Sun, Moon, and major planets including Pluto
        ephemerisAccurate = EphemerisAccurate.getInstance();

        // Approximate Ephemeris for major planets including Pluto
        ephemerisApproximate = EphemerisApproximate.getInstance();

        // Ephemeris for Galilean moons
        ephemerisGalileanMoons = EphemerisGalileanMoons.getInstance();
        
        // Solar System parameters
        solarSystemParameters = SolarSystemParameters.getInstance();
        
        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.add("Sun");
        bodies.addAll(solarSystemParameters.getPlanets());
        bodies.addAll(solarSystemParameters.getMoons());
     
        // First valid date 3000 BC
        firstValidDate = new GregorianCalendar(3000,0,1);
        firstValidDate.set(Calendar.ERA, GregorianCalendar.BC);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Last valid date 3000 AD
        lastValidDate = new GregorianCalendar(3000,0,1);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * Get instance of EphemerisSolarSystem.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisSolarSystem();
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
        // Check whether body name is valid
        if (!bodies.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Solar System Ephemeris");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Solar System Ephemeris");
        }
        
        // Check whether Accurate Ephemeris can be used
        if (ephemerisAccurate.getBodies().contains(name) &&
                date.after(ephemerisAccurate.getFirstValidDate()) &&
                date.before(ephemerisAccurate.getLastValidDate())) {
            
            // (x,y,z) position [m] from Accurate Ephemeris
            return ephemerisAccurate.getBodyPosition(name, date);
        }
       
        // Check whether Approximate Ephemeris can be used
        if (ephemerisApproximate.getBodies().contains(name) &&
                date.after(ephemerisApproximate.getFirstValidDate()) &&
                date.before(ephemerisApproximate.getLastValidDate())) {
            
            // (x,y,z) position [m] from Approximate Ephemeris
            return ephemerisApproximate.getBodyPosition(name, date);
        }

        // Check whether ephemeris for Galilean moons can be used
        if (ephemerisGalileanMoons.getBodies().contains(name) &&
                date.after(ephemerisGalileanMoons.getFirstValidDate()) &&
                date.before(ephemerisGalileanMoons.getLastValidDate())) {

            // (x,y,z) position [m] from Galilean Moons Ephemeris
            return ephemerisGalileanMoons.getBodyPosition(name, date);
        }

        // Approximate position and velocity of the Moon
        if ("Moon".equals(name)) {
            // Position vector (index = 0)
            return approximatePositionVelocityMoon(date)[0];
        }
        
        // Compute position from Solar System parameters
        if (bodies.contains(name)) {
            
            // Obtain orbit parameters
            double[] orbitPars = solarSystemParameters.getOrbitParameters(name);
        
            // Compute orbital elements for given date
            double[] orbitElements = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
            // Compute (x,y,z) position [m] from orbital elements
            return EphemerisUtil.computePosition(orbitElements);
        }

        return null;
    }
                
    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!bodies.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Solar System Ephemeris");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Solar System Ephemeris");
        }
        
        // Check whether Accurate Ephemeris can be used
        if (ephemerisAccurate.getBodies().contains(name) &&
            date.after(ephemerisAccurate.getFirstValidDate()) && 
            date.before(ephemerisAccurate.getLastValidDate())) {
            
            // (x,y,z) velocity [m/s] from Accurate Ephemeris
            return ephemerisAccurate.getBodyVelocity(name, date);
        }
       
        // Check whether Approximate Ephemeris can be used
        if (ephemerisApproximate.getBodies().contains(name) &&
            date.after(ephemerisApproximate.getFirstValidDate()) && 
            date.before(ephemerisApproximate.getLastValidDate())) {
            
            // (x,y,z) velocity [m/s] from Approximate Ephemeris
            return ephemerisApproximate.getBodyVelocity(name, date);
        }

        // Check whether ephemeris for Galilean moons can be used
        if (ephemerisGalileanMoons.getBodies().contains(name) &&
                date.after(ephemerisGalileanMoons.getFirstValidDate()) &&
                date.before(ephemerisGalileanMoons.getLastValidDate())) {

            // (x,y,z) position [m] from Galilean Moons Ephemeris
            return ephemerisGalileanMoons.getBodyVelocity(name, date);
        }

        // Approximate position and velocity of the Moon
        if ("Moon".equals(name)) {
            // Velocity vector (index = 1)
            return approximatePositionVelocityMoon(date)[1];
        }
        
        // Compute velocity from Solar System parameters
        if (bodies.contains(name)) {
            
            // Obtain orbit parameters
            double[] orbitPars = solarSystemParameters.getOrbitParameters(name);
        
            // Compute orbital elements for given date
            double[] orbitElements = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
            // Compute (x,y,z) velocity [m/s] from orbital elements
            String centerBody;
            if (solarSystemParameters.getMoons().contains(name)) {
                centerBody = solarSystemParameters.getPlanetOfMoon(name);
            }
            else {
                centerBody = "Sun";
            }
            double mu = solarSystemParameters.getMu(centerBody);
            return EphemerisUtil.computeVelocity(mu, orbitElements);
        }

        return null;
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!bodies.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Solar System Ephemeris");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Solar System Ephemeris");
        }

        // Check whether Accurate Ephemeris can be used
        if (ephemerisAccurate.getBodies().contains(name) &&
            date.after(ephemerisAccurate.getFirstValidDate()) && 
            date.before(ephemerisAccurate.getLastValidDate())) {
            
            // (x,y,z) position [m] and velocity [m/s] from Accurate Ephemeris
            return ephemerisAccurate.getBodyPositionVelocity(name, date);
        }
       
        // Check whether Approximate Ephemeris can be used
        if (ephemerisApproximate.getBodies().contains(name) &&
            date.after(ephemerisApproximate.getFirstValidDate()) && 
            date.before(ephemerisApproximate.getLastValidDate())) {
            
            // (x,y,z) position [m] and velocity [m/s] from Approximate Ephemeris
            return ephemerisApproximate.getBodyPositionVelocity(name, date);
        }

        // Check whether ephemeris for Galilean moons can be used
        if (ephemerisGalileanMoons.getBodies().contains(name) &&
                date.after(ephemerisGalileanMoons.getFirstValidDate()) &&
                date.before(ephemerisGalileanMoons.getLastValidDate())) {

            // (x,y,z) position [m] from Galilean Moons Ephemeris
            return ephemerisGalileanMoons.getBodyPositionVelocity(name, date);
        }

        // Approximate position and velocity of the Moon
        if ("Moon".equals(name)) {
            return approximatePositionVelocityMoon(date);
        }
        
        // Compute position and velocity from Solar System parameters
        if (bodies.contains(name)) {
            
            // Obtain orbit parameters
            double[] orbitPars = solarSystemParameters.getOrbitParameters(name);
        
            // Compute orbital elements for given date
            double[] orbitElements = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
            // Compute (x,y,z) position [m] from orbital elements
            Vector3D position = EphemerisUtil.computePosition(orbitElements);
            
            // Compute (x,y,z) velocity [m/s] from orbital elements
            String centerBody;
            if (solarSystemParameters.getMoons().contains(name)) {
                centerBody = solarSystemParameters.getPlanetOfMoon(name);
            }
            else {
                centerBody = "Sun";
            }
            double mu = solarSystemParameters.getMu(centerBody);
            Vector3D velocity = EphemerisUtil.computeVelocity(mu, orbitElements);

            return new Vector3D[]{position,velocity};
        }

        return null;
    }

    @Override
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Approximate position [m] and velocity [m/s] of the Moon.
     * It is assumed that position and velocity of the Moon relative to the Earth
     * are the same each orbit of the Moon around the Earth.
     * @param date date/time 
     * @return array containing position [m] and velocity [m/s]
     */
    private Vector3D[] approximatePositionVelocityMoon(GregorianCalendar date) {
        // It is assumed that position and velocity of the Moon relative to the Earth
        // are the same each orbit of the Moon around the Earth.
        // Find a date for which accurate position and velocity of both 
        // the Earth and the Moon are known
        double julianDate = JulianDateConverter.convertCalendarToJulianDate(date);
        double julianDateValid = julianDate;
        if (date.before(ephemerisAccurate.getFirstValidDate())) {
            double julianDateFirstValid = 
                JulianDateConverter.convertCalendarToJulianDate(ephemerisAccurate.getFirstValidDate());
            int nrMoonOrbits = (int) Math.ceil((julianDateFirstValid - julianDate) / ORBITPERIODMOON);
            julianDateValid = julianDate + nrMoonOrbits * ORBITPERIODMOON;
        }
        if (date.after(ephemerisAccurate.getLastValidDate())) {
            double julianDateLastValid = 
                JulianDateConverter.convertCalendarToJulianDate(ephemerisAccurate.getLastValidDate());
            int nrMoonOrbits = (int) Math.ceil((julianDate - julianDateLastValid) / ORBITPERIODMOON);
            julianDateValid = julianDate - nrMoonOrbits * ORBITPERIODMOON;
        }
        GregorianCalendar dateValid = JulianDateConverter.convertJulianDateToCalendar(julianDateValid);
        
        // Accurate position and velocity of the Earth for valid date
        Vector3D[] positionVelocityEarthValid = ephemerisAccurate.getBodyPositionVelocity("Earth",dateValid);
        Vector3D positionEarthValid = positionVelocityEarthValid[0];
        Vector3D velocityEarthValid = positionVelocityEarthValid[1];
        
        // Accurate position and velocity of the Moon for valid date
        Vector3D[] positionVelocityMoonValid = ephemerisAccurate.getBodyPositionVelocity("Moon",dateValid);
        Vector3D positionMoonValid = positionVelocityMoonValid[0];
        Vector3D velocityMoonValid = positionVelocityMoonValid[1];
        
        // Approximate position and velocity of the Earth for given date
        Vector3D[] positionVelocityEarth = ephemerisApproximate.getBodyPositionVelocity("Earth",date);
        Vector3D positionEarth = positionVelocityEarth[0];
        Vector3D velocityEarth = positionVelocityEarth[1];
        
        // Approximate position and velocity of the Moon for given date
        Vector3D positionMoon = positionEarth.plus(positionMoonValid.minus(positionEarthValid));
        Vector3D velocityMoon = velocityEarth.plus(velocityMoonValid.minus(velocityEarthValid));
        return new Vector3D[]{positionMoon,velocityMoon};
    }
}
