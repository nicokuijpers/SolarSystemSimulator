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

import ephemeris.CalendarUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * SpacecrafEvent. Forces position and velocity of particle with given name
 * to position and velocity of Solar System body at given date/time.
 * Spacecraft events are used to simulate launch and Trajectory Correction Maneuvers (TCMs).
 * They are also used to ensure that small Solar System bodies have accurate position and
 * velocity at the time of encounter with a spacecraft.
 * @author Nico Kuijpers
 */
public class SpacecraftEvent implements Serializable {

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Name of the spacecraft
    private String spacecraftName;

    // Date/time of this event
    private GregorianCalendar dateTime;

    // Names of bodies to be moved
    private List<String> bodyNames;

    /**
     * Constructor. Set name of spacecraft and date/time for this event.
     * Postion and velocity of spacecraft with given name will be updated at
     * given date/time
     * @param spacecraftName name of spacecraft to be updated
     * @param dateTime       date/time of this event
     */
    public SpacecraftEvent(String spacecraftName, Calendar dateTime) {
        this.spacecraftName = spacecraftName;
        this.dateTime = CalendarUtil.createGregorianCalendar(dateTime);
        this.bodyNames = new ArrayList<>();
        this.bodyNames.add(spacecraftName);
    }

    /**
     * Constructor. Set name of spacecraft and date/time for this event.
     * Position and velocity of solar system bodies with given names will
     * be updated at given date/time
     * @param spacecraftName name of the spacecraft
     * @param dateTime       date/time of this event
     * @param bodyNames      names of bodies to be updated
     */
    public SpacecraftEvent(String spacecraftName, Calendar dateTime, List<String> bodyNames) {
        this.spacecraftName = spacecraftName;
        this.dateTime = CalendarUtil.createGregorianCalendar(dateTime);
        this.bodyNames = new ArrayList<>(bodyNames);
    }

    /**
     * Get name of spacecraft.
     * @return spacecraft name.
     */
    public String getSpacecraftName() {
        return spacecraftName;
    }

    /**
     * Get date/time of this event.
     * @return date/time [Calendar]
     */
    public Calendar getDateTime() {
        return dateTime;
    }

    /**
     * Get names of bodies to be updated.
     * @return names of bodies to be updated
     */
    public List<String> getBodyNames() {
        return bodyNames;
    }
}
