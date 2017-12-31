/*
 * Copyright (c) 2017 Nico Kuijpers
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import util.Vector3D;

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
        majorPlanets.add("mercury");
        majorPlanets.add("venus");
        majorPlanets.add("earth");
        majorPlanets.add("mars");
        majorPlanets.add("jupiter");
        majorPlanets.add("saturn");
        majorPlanets.add("uranus");
        majorPlanets.add("neptune");
        majorPlanets.add("pluto");
     
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
        double orbitElements[] = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
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
        double orbitElementsEarth[] = EphemerisUtil.computeOrbitalElements(orbitPars,date);
         
        // Compute (x,y,z) velocity [m/s] from orbital elements
        Vector3D velocity = EphemerisUtil.computeVelocity(orbitElementsEarth);
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
        Vector3D velocity = EphemerisUtil.computeVelocity(orbitElements);
        
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
