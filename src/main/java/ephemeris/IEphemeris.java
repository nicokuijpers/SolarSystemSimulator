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
import java.util.List;

/**
 *
 * @author Nico Kuijpers
 */
public interface IEphemeris {
    
    
    /**
     * Get the first date for which Ephemeris is valid.
     * @return first date for which Ephemeris is valid
     */
    public GregorianCalendar getFirstValidDate();
    
    /**
     * Get the last date for which Ephemeris is valid.
     * @return last date for which Ephemeris is valid
     */
    public GregorianCalendar getLastValidDate();
    
    /**
     * Get the names of bodies for which ephemeris data can be obtained.
     * @return list of body names
     */
    public List<String> getBodies();
    
    /**
     * Get position [m] of body from Ephemeris.
     * Position is computed relative to the position of the sun.
     * @param name body name
     * @param date date/time
     * @return position of body [m]
     */
    public Vector3D getBodyPosition(String name, GregorianCalendar date);
    
    /**
     * Get velocity [m/s] of body from Ephemeris.
     * Velocity is computed relative to the velocity of the sun.
     * @param name body name
     * @param date date/time
     * @return velocity of body [m/s]
     */
    public Vector3D getBodyVelocity(String name, GregorianCalendar date);
    
    /**
     * Get position [m] and velocity [m/s] of body from Ephemeris.
     * Position and velocity are computed relative to the position and
     * velocity of the sun.
     * @param name body name
     * @param date date/time
     * @return array containing position [m] and velocity [m/s]
     */
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date);
    
    /**
     * Get position [m] of body from Ephemeris.
     * Position is computed with respect to the position of the barycenter 
     * of the solar system.
     * @param name body name
     * @param date date/time
     * @return position of body [m]
     */
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date);
    
    /**
     * Get velocity [m/s] of body from Ephemeris.
     * Velocity is computed with respect to the velocity of the barycenter 
     * of the solar system.
     * @param name body name
     * @param date date/time
     * @return velocity of body [m/s]
     */
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date);
    
    /**
     * Get position [m] and velocity [m/s] of body from Ephemeris.
     * Position and velocity are computed relative to the position and
     * velocity of the barycenter of the solar system.
     * @param name body name
     * @param date date/time
     * @return array containing position [m] and velocity [m/s]
     */
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date);
}
