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
package spacecraft;

import ephemeris.*;
import util.Vector3D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * SpacecrafTrajectory. Orbital parameters for spacecraft trajectory valid between two dates.
 * @author Nico Kuijpers
 */
public class SpacecraftTrajectory implements Serializable {

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Start date/time for this trajectory
    private GregorianCalendar startDateTime;

    // Stop date/time for this trajectory
    private GregorianCalendar stopDateTime;

    // Name of center body
    private String centerBodyName;

    // Orbital parameters
    private double[] orbitPars;

    /**
     * Constructor.
     * @param startDateTime   Start date/time
     * @param stopDateTime    Stop date/time
     * @param centerBodyName  Name of center body
     * @param orbitPars       Orbital parameters
     */
    public SpacecraftTrajectory(Calendar startDateTime, Calendar stopDateTime,
                                String centerBodyName, double[] orbitPars) {

        this.startDateTime = CalendarUtil.createGregorianCalendar(startDateTime);
        this.stopDateTime = CalendarUtil.createGregorianCalendar(stopDateTime);
        this.centerBodyName = centerBodyName;
        this.orbitPars = orbitPars;
    }

    /**
     * Get start date/time of this trajectory.
     * @return start date/time [GC]
     */
    public GregorianCalendar getStartDateTime() {
        return startDateTime;
    }

    /**
     * Get stop date/time of this trajectory.
     * @return stop date/time [GC]
     */
    public GregorianCalendar getStopDateTime() {
        return stopDateTime;
    }

    /**
     * Compute position at given date/time relative to the Sun.
     * It is assumed that the Sun is located at the origin.
     * @param dateTime
     * @return position vector [m]
     */
    public Vector3D computePosition(GregorianCalendar dateTime) {

        double Teph = JulianDateConverter.convertCalendarToJulianDate(dateTime);
        double[] orbitElements = EphemerisUtil.computeOrbitalElementsFromPerihelionPassage(orbitPars,Teph);
        Vector3D positionSpacecraft = EphemerisUtil.computePosition(orbitElements);
        if ("Sun".equals(centerBodyName)) {
            return positionSpacecraft;
        }
        Vector3D positionCenterBody = EphemerisSolarSystem.getInstance().getBodyPosition(centerBodyName,dateTime);
        return positionCenterBody.plus(positionSpacecraft);
    }

    /**
     * Compute velocity at given date/time relative to the Sun.
     * It is assumed that the Sun is located at the origin with zero velocity.
     * @param dateTime
     * @return velocity vector [m/s]
     */
    public Vector3D computeVelocity(GregorianCalendar dateTime) {

        double Teph = JulianDateConverter.convertCalendarToJulianDate(dateTime);
        double[] orbitElements = EphemerisUtil.computeOrbitalElementsFromPerihelionPassage(orbitPars,Teph);
        double muCenterBody = SolarSystemParameters.getInstance().getMu(centerBodyName);
        Vector3D velocitySpacecraft = EphemerisUtil.computeVelocity(muCenterBody,orbitElements);
        if ("Sun".equals(centerBodyName)) {
            return velocitySpacecraft;
        }
        Vector3D velocityCenterBody = EphemerisSolarSystem.getInstance().getBodyVelocity(centerBodyName, dateTime);
        return velocityCenterBody.plus(velocitySpacecraft);
    }

    /**
     * Compute trajectory relative to the Sun.
     * @return list of (x,y,z) positions [m]
     */
    public List<Vector3D> computeTrajectory() {
        List<Vector3D> trajectory = new ArrayList<>();
        GregorianCalendar dateTime = CalendarUtil.createGregorianCalendar(startDateTime);
        double meanMotion = orbitPars[6]; // degrees/day
        int timeStepInHours = (int) Math.max(1.0,Math.floor(1.0/(meanMotion/24.0)));
        while (dateTime.before(stopDateTime)) {
            trajectory.add(computePosition(dateTime));
            dateTime.add(Calendar.HOUR,timeStepInHours);
        }
        trajectory.add(computePosition(stopDateTime));
        return trajectory;
    }
}
