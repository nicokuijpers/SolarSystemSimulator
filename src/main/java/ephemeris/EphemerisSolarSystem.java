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

    // Accurate Ephemeris for Sun, Moon, and major planets including Pluto
    private final IEphemeris ephemerisAccurate;
    
    // Approximate Ephemeris for major planets including Pluto
    private final IEphemeris ephemerisApproximate;

    // Ephemeris for Mars moons
    private final IEphemeris ephemerisMarsMoons;

    // Ephemeris for Galilean moons
    private final IEphemeris ephemerisGalileanMoonsAccurate;
    private final IEphemeris ephemerisGalileanMoonsApproximate;

    // Ephemeris for Saturn moons
    private final IEphemeris ephemerisSaturnMoonsAccurate;
    private final IEphemeris ephemerisSaturnMoonsApproximate;

    // Ephemeris for Uranus moons
    private final IEphemeris ephemerisUranusMoonsAccurate;
    private final IEphemeris ephemerisUranusMoonsApproximate;

    // Ephemeris for Neptune moon Triton
    private final IEphemeris ephemerisTriton;

    // Ephemeris for other moons of Neptune
    private final IEphemeris ephemerisNeptuneMoons;

    // Ephemeris for Pluto System
    private final IEphemeris ephemerisPlutoSystem;
    
    // Solar system parameters
    private final SolarSystemParameters solarSystemParameters;

    // Bodies for which ephemeris can be computed or approximated
    private static List<String> bodies;

    // First valid date
    private final GregorianCalendar firstValidDate;
    
    // Last valid date
    private final GregorianCalendar lastValidDate;

    // First valid date for Ephemeris of moons using BSP files
    private final GregorianCalendar firstValidDateMoonsBSP;

    // Last valid date for Ephemeris of moons using BSP files
    private final GregorianCalendar lastValidDateMoonsBSP;

    // Current Julian date/time for which approximate position and velocity of
    // the Earth and the Moon are available
    private double currentJulianDateTime;

    // Approximate position and velocity of the Earth for current Julian date/time
    private Vector3D currentPositionEarth = new Vector3D();
    private Vector3D currentVelocityEarth = new Vector3D();

    // Approximate position and velocity of the Moon for current Julian date/time
    private Vector3D currentPositionMoon = new Vector3D();
    private Vector3D currentVelocityMoon = new Vector3D();
    
    // Singleton instance
    private static IEphemeris instance = null;
    
    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisSolarSystem() {
        
        // Accurate Ephemeris for Sun, Moon, and major planets including Pluto
        // ephemerisAccurate = EphemerisAccurate.getInstance(); // Uses DE405EphemerisFiles
        ephemerisAccurate = EphemerisAccurateBSP.getInstance(); // Uses de405.bsp

        // Approximate Ephemeris for major planets including Pluto
        ephemerisApproximate = EphemerisApproximate.getInstance();

        // Ephemeris for Mars moons
        ephemerisMarsMoons = EphemerisMarsMoonsBSP.getInstance();

        // Accurate ephemeris for Galilean moons
        ephemerisGalileanMoonsAccurate = EphemerisGalileanMoonsBSP.getInstance();

        // Approximate ephemeris for Galilean moons
        ephemerisGalileanMoonsApproximate = EphemerisGalileanMoons.getInstance();

        // Accurate ephemeris for Saturn moons
        ephemerisSaturnMoonsAccurate = EphemerisSaturnMoonsBSP.getInstance();

        // Approximate ephemeris for Saturn moons
        ephemerisSaturnMoonsApproximate = EphemerisSaturnMoons.getInstance();

        // Accurate ephemeris for Uranus moons
        ephemerisUranusMoonsAccurate = EphemerisUranusMoonsBSP.getInstance();

        // Approximate ephemeris for Uranus moons
        ephemerisUranusMoonsApproximate = EphemerisUranusMoons.getInstance();

        // Ephemeris for Neptune moon Triton
        ephemerisTriton = EphemerisTriton.getInstance();

        // Ephemeris for other moons of Neptune
        ephemerisNeptuneMoons = EphemerisNeptuneMoonsBSP.getInstance();

        // Ephemeris for Pluto System
        ephemerisPlutoSystem = EphemerisPlutoSystemBSP.getInstance();
        
        // Solar System parameters
        solarSystemParameters = SolarSystemParameters.getInstance();
        
        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.add("Sun");
        bodies.add("EarthMoonBarycenter");
        bodies.addAll(solarSystemParameters.getPlanets());
        bodies.addAll(solarSystemParameters.getMoons());
     
        // First valid date 3000 BC
        firstValidDate = new GregorianCalendar(3000,0,1);
        firstValidDate.set(Calendar.ERA, GregorianCalendar.BC);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Last valid date 3000 AD
        lastValidDate = new GregorianCalendar(3000,0,1);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for Ephemeris of moons using BSP file is Jan 2, 1970
        firstValidDateMoonsBSP = new GregorianCalendar(1970,0,2);
        firstValidDateMoonsBSP.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for Ephemeris of moons using BSP file is Dec 31, 2025
        lastValidDateMoonsBSP = new GregorianCalendar(2025,11,31);
        lastValidDateMoonsBSP.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Initialize approximate positions of the Earth and the Moon
        GregorianCalendar today = new GregorianCalendar();
        today.setTimeZone(TimeZone.getTimeZone("UTC"));
        approximatePositionVelocityEarthMoon(today);
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

    /**
     * Select suitable ephemeris based on name, first valid date, and last valid date.
     * @param name body name
     * @param date date/time
     * @return reference to ephemeris if a suitable ephemeris exists, otherwise null
     */
    private IEphemeris selectSuitableEphemeris(String name, GregorianCalendar date) {
        // Check whether Accurate Ephemeris can be used
        if (ephemerisAccurate.getBodies().contains(name) &&
                date.after(ephemerisAccurate.getFirstValidDate()) &&
                date.before(ephemerisAccurate.getLastValidDate())) {
            return ephemerisAccurate;
        }

        // Check whether Approximate Ephemeris can be used
        if (ephemerisApproximate.getBodies().contains(name) &&
                date.after(ephemerisApproximate.getFirstValidDate()) &&
                date.before(ephemerisApproximate.getLastValidDate())) {
            return ephemerisApproximate;
        }

        // Check whether ephemeris for Mars moons can be used
        if (ephemerisMarsMoons.getBodies().contains(name) &&
                date.after(ephemerisMarsMoons.getFirstValidDate()) &&
                date.before(ephemerisMarsMoons.getLastValidDate())) {
            return ephemerisMarsMoons;
        }

        // Check whether accurate ephemeris for Galilean moons can be used
        if (ephemerisGalileanMoonsAccurate.getBodies().contains(name) &&
                date.after(ephemerisGalileanMoonsAccurate.getFirstValidDate()) &&
                date.before(ephemerisGalileanMoonsAccurate.getLastValidDate())) {
            return ephemerisGalileanMoonsAccurate;
        }

        // Check whether approximate ephemeris for Galilean moons can be used
        if (ephemerisGalileanMoonsApproximate.getBodies().contains(name) &&
                date.after(ephemerisGalileanMoonsApproximate.getFirstValidDate()) &&
                date.before(ephemerisGalileanMoonsApproximate.getLastValidDate())) {
            return ephemerisGalileanMoonsApproximate;
        }

        // Check whether accurate ephemeris for Saturn moons can be used
        if (ephemerisSaturnMoonsAccurate.getBodies().contains(name) &&
                date.after(ephemerisSaturnMoonsAccurate.getFirstValidDate()) &&
                date.before(ephemerisSaturnMoonsAccurate.getLastValidDate())) {
            return ephemerisSaturnMoonsAccurate;
        }

        // Check whether approximate ephemeris for Saturn moons can be used
        if (ephemerisSaturnMoonsApproximate.getBodies().contains(name) &&
                date.after(ephemerisSaturnMoonsApproximate.getFirstValidDate()) &&
                date.before(ephemerisSaturnMoonsApproximate.getLastValidDate())) {
            return ephemerisSaturnMoonsApproximate;
        }

        // Check whether accurate ephemeris for Uranus moons can be used
        if (ephemerisUranusMoonsAccurate.getBodies().contains(name) &&
                date.after(ephemerisUranusMoonsAccurate.getFirstValidDate()) &&
                date.before(ephemerisUranusMoonsAccurate.getLastValidDate())) {
            return ephemerisUranusMoonsAccurate;
        }

        // Check whether approximate ephemeris for Uranus moons can be used
        if (ephemerisUranusMoonsApproximate.getBodies().contains(name) &&
                date.after(ephemerisUranusMoonsApproximate.getFirstValidDate()) &&
                date.before(ephemerisUranusMoonsApproximate.getLastValidDate())) {
            return ephemerisUranusMoonsApproximate;
        }

        // Check whether ephemeris for Neptune moon Triton can be used
        if (ephemerisTriton.getBodies().contains(name) &&
                date.after(ephemerisTriton.getFirstValidDate()) &&
                date.before(ephemerisTriton.getLastValidDate())) {
            return ephemerisTriton;
        }

        // Check whether ephemeris for other moons of Neptune can be used
        if (ephemerisNeptuneMoons.getBodies().contains(name) &&
                date.after(ephemerisNeptuneMoons.getFirstValidDate()) &&
                date.before(ephemerisNeptuneMoons.getLastValidDate())) {
            return ephemerisNeptuneMoons;
        }

        // Check whether ephemeris for Pluto System can be used
        if (ephemerisPlutoSystem.getBodies().contains(name) &&
                date.after(ephemerisPlutoSystem.getFirstValidDate()) &&
                date.before(ephemerisPlutoSystem.getLastValidDate())) {
            return ephemerisPlutoSystem;
        }

        // No suitable ephemeris can be found
        return null;
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

        // Check whether a suitable ephemeris can be used
        IEphemeris ephemeris = selectSuitableEphemeris(name, date);
        if (ephemeris != null) {
            return ephemeris.getBodyPosition(name, date);
        }

        // Approximate position of the Earth
        if ("Earth".equals(name)) {
            approximatePositionVelocityEarthMoon(date);
            Vector3D positionEarth = new Vector3D(currentPositionEarth);
            return positionEarth;
        }

        // Approximate position of the Moon
        if ("Moon".equals(name)) {
            approximatePositionVelocityEarthMoon(date);
            Vector3D positionMoon = new Vector3D(currentPositionMoon);
            return positionMoon;
        }

        // Approximate position of other moons of the Solar System
        if (solarSystemParameters.getMoons().contains(name)) {
            // Index 0 is position
            return approximatePositionVelocityMoon(name, date)[0];
        }
        
        // Compute position from Solar System parameters
        if (solarSystemParameters.getPlanets().contains(name)) {
            
            // Obtain orbit parameters
            double[] orbitPars = solarSystemParameters.getOrbitParameters(name);
        
            // Compute orbital elements for given date
            double[] orbitElements = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
            // Compute (x,y,z) position [m] from orbital elements
            return EphemerisUtil.computePosition(orbitElements);
        }

        // Position can not be computed from ephemeris
        return new Vector3D();
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

        // Check whether a suitable ephemeris can be used
        IEphemeris ephemeris = selectSuitableEphemeris(name, date);
        if (ephemeris != null) {
            return ephemeris.getBodyVelocity(name, date);
        }

        // Approximate velocity of the Earth
        if ("Earth".equals(name)) {
            approximatePositionVelocityEarthMoon(date);
            Vector3D velocityEarth = new Vector3D(currentVelocityEarth);
            return velocityEarth;
        }

        // Approximate velocity of the Moon
        if ("Moon".equals(name)) {
            approximatePositionVelocityEarthMoon(date);
            Vector3D velocityMoon = new Vector3D(currentVelocityMoon);
            return velocityMoon;
        }

        // Approximate velocity of other moons of the Solar System
        if (solarSystemParameters.getMoons().contains(name)) {
            // Index 1 is velocity
            return approximatePositionVelocityMoon(name, date)[1];
        }
        
        // Compute velocity from Solar System parameters
        if (solarSystemParameters.getPlanets().contains(name)) {
            
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

        // Velocity can not be computed from ephemeris
        return new Vector3D();
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

        // Check whether a suitable ephemeris can be used
        IEphemeris ephemeris = selectSuitableEphemeris(name, date);
        if (ephemeris != null) {
            return ephemeris.getBodyPositionVelocity(name, date);
        }

        // Approximate position and velocity of the Earth
        if ("Earth".equals(name)) {
            approximatePositionVelocityEarthMoon(date);
            Vector3D positionEarth = new Vector3D(currentPositionEarth);
            Vector3D velocityEarth = new Vector3D(currentVelocityEarth);
            return new Vector3D[]{positionEarth, velocityEarth};
        }

        // Approximate position and velocity of the Moon
        if ("Moon".equals(name)) {
            approximatePositionVelocityEarthMoon(date);
            Vector3D positionMoon = new Vector3D(currentPositionMoon);
            Vector3D velocityMoon = new Vector3D(currentVelocityMoon);
            return new Vector3D[]{positionMoon, velocityMoon};
        }

        // Approximate position and velocity of other moons of the Solar System
        if (solarSystemParameters.getMoons().contains(name)) {
            return approximatePositionVelocityMoon(name, date);
        }
        
        // Compute position and velocity from Solar System parameters
        if (solarSystemParameters.getPlanets().contains(name)) {
            
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

        // Position and velocity can not be computed from ephemeris
        return new Vector3D[]{new Vector3D(), new Vector3D()};
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
     * Approximate position [m] and velocity [m/s] of the Earth and the Moon.
     * It is assumed that position and velocity of the Earth and the Moon relative
     * to the Earth-Moon barycenter are the same each orbit of the Moon around the Earth.
     * @param date date/time
     */
    private void approximatePositionVelocityEarthMoon(GregorianCalendar date) {
        // It is assumed that position and velocity of the Earth and the Moon relative
        // to the Earth-Moon barycenter are the same each orbit of the Moon around the Earth.
        // Find a date for which accurate position and velocity of both 
        // the Earth and the Moon are known
        double julianDateTime = JulianDateConverter.convertCalendarToJulianDate(date);
        if (julianDateTime == currentJulianDateTime) {
            return;
        }
        currentJulianDateTime = julianDateTime;
        double orbitPeriodMoon = solarSystemParameters.getOrbitalPeriod("Moon");
        double julianDateTimeValid = julianDateTime;
        if (date.before(ephemerisAccurate.getFirstValidDate())) {
            double julianDateFirstValid = 
                JulianDateConverter.convertCalendarToJulianDate(ephemerisAccurate.getFirstValidDate());
            int nrMoonOrbits = (int) Math.ceil((julianDateFirstValid - julianDateTime) / orbitPeriodMoon);
            julianDateTimeValid = julianDateTime + nrMoonOrbits * orbitPeriodMoon;
        }
        if (date.after(ephemerisAccurate.getLastValidDate())) {
            double julianDateLastValid = 
                JulianDateConverter.convertCalendarToJulianDate(ephemerisAccurate.getLastValidDate());
            int nrMoonOrbits = (int) Math.ceil((julianDateTime - julianDateLastValid) / orbitPeriodMoon);
            julianDateTimeValid = julianDateTime - nrMoonOrbits * orbitPeriodMoon;
        }
        GregorianCalendar dateValid = JulianDateConverter.convertJulianDateToCalendar(julianDateTimeValid);

        // Accurate position and velocity of the Earth-Moon barycenter for valid date
        Vector3D[] positionVelocityEMBaryValid =
                ephemerisAccurate.getBodyPositionVelocity("EarthMoonBarycenter",dateValid);
        Vector3D positionEMBaryValid = positionVelocityEMBaryValid[0];
        Vector3D velocityEMBaryValid = positionVelocityEMBaryValid[1];

        // Accurate position and velocity of the Earth for valid date
        Vector3D[] positionVelocityEarthValid = ephemerisAccurate.getBodyPositionVelocity("Earth",dateValid);
        Vector3D positionEarthValid = positionVelocityEarthValid[0];
        Vector3D velocityEarthValid = positionVelocityEarthValid[1];
        
        // Accurate position and velocity of the Moon for valid date
        Vector3D[] positionVelocityMoonValid = ephemerisAccurate.getBodyPositionVelocity("Moon",dateValid);
        Vector3D positionMoonValid = positionVelocityMoonValid[0];
        Vector3D velocityMoonValid = positionVelocityMoonValid[1];
        
        // Approximate position and velocity of the Earth-Moon barycenter for given date
        Vector3D[] positionVelocityEMBary = ephemerisApproximate.getBodyPositionVelocity("EarthMoonBarycenter",date);
        Vector3D positionEMBary = positionVelocityEMBary[0];
        Vector3D velocityEMBary = positionVelocityEMBary[1];

        // Approximate position and velocity of the Earth for given date
        currentPositionEarth = positionEarthValid.minus(positionEMBaryValid).plus(positionEMBary);
        currentVelocityEarth = velocityEarthValid.minus(velocityEMBaryValid).plus(velocityEMBary);

        // Approximate position and velocity of the Moon for given date
        currentPositionMoon = positionMoonValid.minus(positionEMBaryValid).plus(positionEMBary);
        currentVelocityMoon = velocityMoonValid.minus(velocityEMBaryValid).plus(velocityEMBary);
    }

    /**
     * Approximate position [m] and velocity [m/s] of a moon of the Solar System.
     * It is assumed that position and velocity of the Moon relative
     * to the planet or barycenter are the same each orbit of the Moon around the planet.
     * @param name of the moon
     * @param date date/time
     * @return array containing position [m] and velocity [m/s]
     */
    private Vector3D[] approximatePositionVelocityMoon(String name, GregorianCalendar date) {
        // It is assumed that position and velocity of the Earth and the Moon relative
        // to the Earth-Moon barycenter are the same each orbit of the Moon around the Earth.
        // Find a date for which accurate position and velocity of both
        // the Earth and the Moon are known
        double julianDateTime = JulianDateConverter.convertCalendarToJulianDate(date);
        double orbitPeriodMoon = solarSystemParameters.getOrbitalPeriod(name);
        double julianDateTimeValid = julianDateTime;
        if (date.before(firstValidDateMoonsBSP)) {
            double julianDateFirstValid =
                    JulianDateConverter.convertCalendarToJulianDate(firstValidDateMoonsBSP);
            int nrMoonOrbits = (int) Math.ceil((julianDateFirstValid - julianDateTime) / orbitPeriodMoon);
            julianDateTimeValid = julianDateTime + nrMoonOrbits * orbitPeriodMoon;
        }
        if (date.after(lastValidDateMoonsBSP)) {
            double julianDateLastValid =
                    JulianDateConverter.convertCalendarToJulianDate(lastValidDateMoonsBSP);
            int nrMoonOrbits = (int) Math.ceil((julianDateTime - julianDateLastValid) / orbitPeriodMoon);
            julianDateTimeValid = julianDateTime - nrMoonOrbits * orbitPeriodMoon;
        }
        GregorianCalendar dateValid = JulianDateConverter.convertJulianDateToCalendar(julianDateTimeValid);

        // Accurate position and velocity of the moon for valid date
        return getBodyPositionVelocity(name, dateValid);
    }
}
