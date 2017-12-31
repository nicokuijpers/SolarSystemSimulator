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
package solarsystem;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javafx.scene.control.TextField;

/**
 * Selector to view and set era, date, and time.
 * The format is "G yyyy-MM-dd HH:mm", where 
 * G represents era, either "BC" or "AD"
 * yyyy represents the year (4 digits)
 * MM represents month (2 digits), 01-12
 * dd represents day of month (2 digits), 01-31
 * HH represents hour of day (2 digits), 00-23
 * mm represent minutes (2 digits), 00-59
 * @author Nico Kuijpers
 */
public class DateTimeSelector extends TextField {

    public DateTimeSelector(GregorianCalendar dateTime) {
        // Set text representing given date/time
        setDateTime(dateTime);
    }
    
    /**
     * Set era, date, and time.
     * @param dateTime GregorianCalender-object with era, date, and time
     */
    public final void setDateTime(GregorianCalendar dateTime) {
        this.setText(calendarToString(dateTime));
    }
    
    /**
     * Get era, date, and time.
     * @return GregorianCalendar-object with era, date, and time
     * @throws SolarSystemException when date/time is not valid 
     */
    public GregorianCalendar getDateTime() throws SolarSystemException {
        try {
            GregorianCalendar calendar = stringToCalendar(getText());
            this.setText(calendarToString(calendar));
            return calendar;
        }
        catch (NumberFormatException ex) {
            throw new SolarSystemException(getText() + " is not a valid date");
        }
    }
    
    /**
     * Convert GregorianCalendar to String.
     * @param calendar GregorianCalendar-object
     * @return era, date, and time as string
     */
    private String calendarToString(GregorianCalendar calendar) {
        // Obtain era, date, and time from calendar
        int era = calendar.get(Calendar.ERA);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 0 - 11
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        // Construct string representing era, date, and time
        StringBuilder result = new StringBuilder();
        if (era == GregorianCalendar.BC) {
            result.append("BC");
        }
        else {
            result.append("AD");
        }
        result.append(" ");
        result.append(String.format("%04d", year)).append("-");
        result.append(String.format("%02d", month+1)).append("-");
        result.append(String.format("%02d", day)).append(" ");
        result.append(String.format("%02d", hour)).append(":");
        result.append(String.format("%02d", minute));
        
        // Add time zone
        result.append(" (");
        result.append(calendar.getTimeZone().getID());
        result.append(")");
        
        return result.toString();
    }
    
    /**
     * Convert String to GregorianCalendar.
     * @param text era, date, and time as string
     * @return GregorianCalendar object corresponding to given string
     */
    private GregorianCalendar stringToCalendar(String text) {
        // Obatain era, date, and time from string
        int era = GregorianCalendar.AD;
        if (text.startsWith("BC")) {
            era = GregorianCalendar.BC;
        }
        int year = Integer.parseInt(text.substring(3,7));
        int month = Integer.parseInt(text.substring(8,10));
        int day = Integer.parseInt(text.substring(11,13));
        int hour = Integer.parseInt(text.substring(14,16));
        int minute = Integer.parseInt(text.substring(17,19));
        
        // Create new GregorianCalendar-object corresponding to era, date, and time
        GregorianCalendar calendar = new GregorianCalendar(year,month-1,day,hour,minute);
        calendar.set(Calendar.ERA, era);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar;
    }
}
