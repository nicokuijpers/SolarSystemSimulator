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

import java.io.Serializable;
import java.util.Calendar;

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

    // Name of the spacecraft or small body
    private String spacecraftOrBodyName;

    // Date/time of this event
    private Calendar dateTime;

    /**
     * Constructor. Set name of spacecraft or small body and date/time for this event.
     * @param spacecraftOrBodyName name of spacecraft or small body
     * @param dateTime date/time of this event
     */
    public SpacecraftEvent(String spacecraftOrBodyName, Calendar dateTime) {
        this.spacecraftOrBodyName = spacecraftOrBodyName;
        this.dateTime = dateTime;
    }

    /**
     * Get name of spacecraft.
     * @return spacecraft name.
     */
    public String getSpacecraftOrBodyName() {
        return spacecraftOrBodyName;
    }

    /**
     * Get date/time of this event.
     * @return date/time [Calendar]
     */
    public Calendar getDateTime() {
        return dateTime;
    }
}
