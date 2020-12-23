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

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Convert Gregorian calendar date to Julian date and vice versa.
 * @author Nico Kuijpers
 */
public class JulianDateConverter {
    
    /**
     * Convert Gregorian calendar date to Julian date.
     * Conversion of a date before October 15, 1582 is done assuming the
     * the Julian Calendar. Conversion of date October 15, 1582 or later 
     * is done assuming the Gregorian Calendar. Conversion of a date before 
     * Jan 1, 4713 BC, 12h is not supported and an InvalidParameterException 
     * will be thrown.
     * @param date date (Julian of Gregorian Calendar)
     * @return Julian date
     * @throws InvalidParameterException
     */
    public static double convertCalendarToJulianDate(GregorianCalendar date) 
        throws InvalidParameterException {
        
        // Obtain era, year, month, day, hour, minute
        // Note that the month-value is zero-based, i.e., Jan = 0, Feb = 1, etc.
        int era = date.get(Calendar.ERA);
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1; // Month is zero-based
        int day = date.get(Calendar.DAY_OF_MONTH);
        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);
        int second = date.get(Calendar.SECOND);
        int millisecond = date.get(Calendar.MILLISECOND);
        
        // Adjust year for era (BC or AD)
        // The sequence of years at the transition from BC to AD is
        // ..., 2 BC, 1 BC, 1 AD, 2 AD,...
        // https://docs.oracle.com/javase/8/docs/api/java/util/GregorianCalendar.html
        if (era == GregorianCalendar.BC) {
            year = -year + 1;
        }
 
        // https://quasar.as.utexas.edu/BillInfo/JulianDatesG.html
        // If the month is January or February, subtract 1 from the year, 
        // and add 12 to the month. Thus, January and February are considered
        // the 13th and 14th month of the previous year.
        if (month == 1 || month == 2) {
           year = year - 1;
           month = month + 12;
        }
        int a = year / 100;
        int b = a / 4;
        int c = 2 - a + b;
        double e = Math.floor(365.25 * (year + 4716));
        double f = Math.floor(30.6001 * (month + 1));
        double julianDate = c + day + e + f - 1524.5 +
                ((hour + (minute / 60.0) + (second / 3600.0) + (millisecond / 3.6E6)) / 24.0);
        if (julianDate < 2299160.5) {
            // Date occurs before Gregorian change on October 15, 1582 (Gregorian)
            julianDate = julianDate - c;
        } 
        
        // Check result
        if (julianDate < 0.0) {
            throw new InvalidParameterException("Julian date before Jan 1, 4713 BC 12:00 not supported");
        }
        return julianDate;
    }
    
    /**
     * Convert Julian date to Gregorian calendar date.
     * Conversion of date October 15, 1582 or later is done assuming 
     * the Gregorian Calendar. Conversion of a date before October 15, 1582 
     * is not supported and an InvalidParameterException will be thrown.
     * The result will be a GregorianCalendar object with time zone UTC.
     * @param julianDate Julian date
     * @return Gregorian calendar date
     * @throws InvalidParameterException
     */
    public static GregorianCalendar convertJulianDateToCalendar(double julianDate) 
        throws InvalidParameterException {
        // Check input Julian date
        if (julianDate < 2299160.5) {
            throw new InvalidParameterException("Julian date before 2299160.5 not supported");
        }
        
        // https://quasar.as.utexas.edu/BillInfo/JulianDatesG.html
        double q = julianDate + 0.5;
        int z = (int) Math.floor(q);
        int w = (int) Math.floor((z - 1867216.25)/36524.25);
        int x = (int) Math.floor(w/4.0);
        int a = z + 1 + w - x;
        int b = a + 1524;
        int c = (int) Math.floor((b - 122.1)/365.25);
        int d = (int) Math.floor(365.25 * c);
        int e = (int) Math.floor((b - d)/30.6001);
        int f = (int) Math.floor(30.6001 * e);
        int millisecond = (int) Math.floor((q - z)*24*3600*1000) % 1000;
        int second = (int) Math.floor((q - z)*24*3600) % 60;
        int minute = (int) Math.floor((q - z)*24*60) % 60;
        int hour = (int) Math.floor((q - z)*24);
        int day = (int) Math.floor(b - d - f + (q - z));
        int month = e - 1;
        if (month > 12) {
            month = month - 12;
        }
        int year = c - 4715;
        if (month > 2) {
            year = year - 1;
        }
        
        // Note that the month-value is zero-based, i.e., Jan = 0, Feb = 1, etc.
        GregorianCalendar date = new GregorianCalendar(year,month-1,day,hour,minute,second);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        date.add(Calendar.MILLISECOND,millisecond);

        return date;
    }   
}