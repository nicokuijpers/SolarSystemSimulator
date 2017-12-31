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
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.Vector3D;

/**
 * Accurate Ephemeris for Sun, Moon, and major planets including Pluto.
 * This ephemeris is valid for Julian dates from 2414992.5 (December 4, 1899)
 * to 2524624.5 (February 1, 2200).
 * @author Nico Kuijpers
 */
public class EphemerisDE405 implements IEphemeris {
    
    // Length of an A.U.[km]
    private static final double au_km = 149597870.691;
    
    // Length of an A.U.[m]
    private static final double au_m = 149597870691.0;
    
    // DE405 ephemeris
    private final DECheck testBody;
    
    // Indices for planets, moon, and sun for DE405 ephemeris
    private final Map<String,Integer> indexMap;
    
    // Names of planets, moon, and sun for which ephemeris can be computed
    private final List<String> bodies;
    
    // First valid date
    private final GregorianCalendar firstValidDate;
    
    // Last valid date
    private final GregorianCalendar lastValidDate;
    
    // Singleton instance
    private static IEphemeris instance = null;
    
    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisDE405() {
        // Indices for planets, moon, and sun for DE405 ephemeris
        indexMap = new HashMap<>();
        indexMap.put("mercury",1);
        indexMap.put("venus",2);
        indexMap.put("earth",3);
        indexMap.put("mars",4);
        indexMap.put("jupiter",5);
        indexMap.put("saturn",6);
        indexMap.put("uranus",7);
        indexMap.put("neptune",8);
        indexMap.put("pluto",9);
        indexMap.put("moon",10);
        indexMap.put("sun",11);
        
        // Names of planets, moon, and sun for which ephemeris can be computed
        bodies = new ArrayList<>();
        for (String body : indexMap.keySet()) {
            bodies.add(body);
        }
        
        // First valid date
        firstValidDate = 
                JulianDateConverter.convertJulianDateToCalendar(2414992.5);
        
        // Last valid date
        lastValidDate = 
                JulianDateConverter.convertJulianDateToCalendar(2524624.5);
        
        // DE405 ephemeris
        testBody = new DECheck();
    }

    /**
     * Get instance of EphemerisDE405.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisDE405();
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
        // Names of planets, moon, and sun for which DE405 ephemeris is known
        return Collections.unmodifiableList(bodies);
    }

    @Override
    public Vector3D getBodyPosition(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }
        
        // Compute Julian date
        double julianDate = JulianDateConverter.convertCalendarToJulianDate(date);
        
        // This is the call to "planetary_ephemeris", which will put planetary 
        // positions into the array "planet_r", and planetary velocities into the array "planet_rprime"
	testBody.planetary_ephemeris(julianDate);
        
        // Position of body with given name
        // Conversion from [AU] to [m]
        int indexp = indexMap.get(name);
        double xp = testBody.planet_r[indexp][1] * au_m;
        double yp = testBody.planet_r[indexp][2] * au_m;
        double zp = testBody.planet_r[indexp][3] * au_m;
        Vector3D positionBody = new Vector3D(xp,yp,zp);
        
        // Position of Sun
        int indexs = indexMap.get("sun");
        double xs = testBody.planet_r[indexs][1] * au_m;
        double ys = testBody.planet_r[indexs][2] * au_m;
        double zs = testBody.planet_r[indexs][3] * au_m;
        Vector3D positionSun = new Vector3D(xs,ys,zs);
        
        // Position of body relative to sun
        Vector3D position = positionBody.minus(positionSun);
        
        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(position);
    }

    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }
        
        // Compute Julian date
        double julianDate = JulianDateConverter.convertCalendarToJulianDate(date);
        
        // This is the call to "planetary_ephemeris", which will put planetary 
        // positions into the array "planet_r", and planetary velocities into 
        // the array "planet_rprime"
	testBody.planetary_ephemeris(julianDate);
        
        // Velocity of body with given name
        // Conversion from [au/day] to [m/s]
        int indexp = indexMap.get(name);
        double xp = testBody.planet_rprime[indexp][1] * au_m/86400;
        double yp = testBody.planet_rprime[indexp][2] * au_m/86400;
        double zp = testBody.planet_rprime[indexp][3] * au_m/86400;
        Vector3D velocityBody =  new Vector3D(xp,yp,zp);
        
        // Velocity of Sun
        // Conversion from [au/day] to [m/s]
        int indexs = indexMap.get("sun");
        double xs = testBody.planet_rprime[indexs][1] * au_m/86400;
        double ys = testBody.planet_rprime[indexs][2] * au_m/86400;
        double zs = testBody.planet_rprime[indexs][3] * au_m/86400;
        Vector3D velocitySun =  new Vector3D(xs,ys,zs);
        
        // Velocity of body relative to sun
        Vector3D velocity = velocityBody.minus(velocitySun);
        
        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(velocity);
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {
        Vector3D position = getBodyPosition(name,date);
        Vector3D velocity = getBodyVelocity(name,date);
        return new Vector3D[]{position,velocity};
    }

    @Override
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }
        
        // Compute Julian date
        double julianDate = JulianDateConverter.convertCalendarToJulianDate(date);
        
        // This is the call to "planetary_ephemeris", which will put planetary 
        // positions into the array "planet_r", and planetary velocities into the array "planet_rprime"
	testBody.planetary_ephemeris(julianDate);
        
        // Position of body with given name
        // Conversion from [AU] to [m]
        int indexp = indexMap.get(name);
        double xp = testBody.planet_r[indexp][1] * au_m;
        double yp = testBody.planet_r[indexp][2] * au_m;
        double zp = testBody.planet_r[indexp][3] * au_m;
        Vector3D position = new Vector3D(xp,yp,zp);
        
        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(position);
    }

    @Override
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }
        
        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }
        
        // Compute Julian date
        double julianDate = JulianDateConverter.convertCalendarToJulianDate(date);
        
        // This is the call to "planetary_ephemeris", which will put planetary 
        // positions into the array "planet_r", and planetary velocities into 
        // the array "planet_rprime"
	testBody.planetary_ephemeris(julianDate);
        
        // Velocity of body with given name
        // Conversion from [au/day] to [m/s]
        int indexp = indexMap.get(name);
        double xp = testBody.planet_rprime[indexp][1] * au_m/86400;
        double yp = testBody.planet_rprime[indexp][2] * au_m/86400;
        double zp = testBody.planet_rprime[indexp][3] * au_m/86400;
        Vector3D velocity =  new Vector3D(xp,yp,zp);
       
        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(velocity);
    }

    @Override
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date) {
        Vector3D position = getBodyPositionBarycenter(name,date);
        Vector3D velocity = getBodyVelocityBarycenter(name,date);
        return new Vector3D[]{position,velocity};
    }
    
    /**
     * Inverse transformation for 23.4 degrees J2000 frame.
     * This transformation is performed such that the J2000 ecliptic plane
     * becomes the x-y plane.
     * @param coordinates input coordinates
     * @return coordinates after transformation
     */
    private Vector3D inverseTransformJ2000(Vector3D coordinates) {
        double sinEP = -0.397776995;
        double cosEP = Math.sqrt(1.0 - sinEP*sinEP);
        double x = coordinates.getX();
        double y = coordinates.getY();
        double z = coordinates.getZ();
        return new Vector3D(x, cosEP*y - sinEP*z, sinEP*y + cosEP*z);
    }
}
