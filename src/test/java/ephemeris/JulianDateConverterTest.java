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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 * Unit test for class JulianDateConverter.
 * @author Nico Kuijpers
 */
public class JulianDateConverterTest {
    
    public JulianDateConverterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Method: convertCalendarToJulianDate(Calendar date)
     * Convert calendar date to Julian date.
     * Conversion of a date before October 15, 1582 is done assuming the
     * the Julian Calendar. Conversion of date October 15, 1582 or later 
     * is done assuming the Gregorian Calendar. Conversion of a date before 
     * Jan 1, 4713 BC, 12h is not supported and an InvalidParameterException 
     * will be thrown.
     * @param date date (Julian or Gregorian Calendar)
     * @return Julian date
     * @throws InvalidParameterException
     */
    
    @Test (expected = InvalidParameterException.class)
    public void testConvertCalendarToJulianDateException() {
        // Conversion of a date before Jan 1, 4713 BC, 12h is not supported 
        GregorianCalendar date = new GregorianCalendar(4713,0,1,11,0);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        date.set(Calendar.ERA,GregorianCalendar.BC);
        JulianDateConverter.convertCalendarToJulianDate(date);
    }
    
    @Test
    public void testConvertCalendarToJulianDateJDZero() {
        // Julian Date is 0 at Jan 1, 4713 BC 12h
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar date = new GregorianCalendar(4713,0,1,12,0);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        date.set(Calendar.ERA,GregorianCalendar.BC);
        double expResult = 0.0;
        double result = JulianDateConverter.convertCalendarToJulianDate(date);
        assertEquals(expResult, result, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDateBC() {
        // Date: Jan 1, 1 BC Time: 0:00
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar date = new GregorianCalendar(1,0,1,0,0);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        date.set(Calendar.ERA,GregorianCalendar.BC);
        double expResult = 1721057.5;
        double result = JulianDateConverter.convertCalendarToJulianDate(date);
        assertEquals(expResult, result, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDateAD() {
        // Date: Jan 1, 1 AD Time: 0:00
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar date = new GregorianCalendar(1,0,1,0,0);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResult = 1721423.5;
        double result = JulianDateConverter.convertCalendarToJulianDate(date);
        assertEquals(expResult, result, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDate1Jan1970() {
        // Date: January 1, 1970 Time: 0:00
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar date = new GregorianCalendar(1970,0,1,0,0);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResult = 2440587.5;
        double result = JulianDateConverter.convertCalendarToJulianDate(date);
        assertEquals(expResult, result, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDate1Jan2000() {
        // Date: January 1, 2000 Time: 0:00
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        GregorianCalendar date = new GregorianCalendar(2000,0,1,0,0);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResult = 2451544.5;
        double result = JulianDateConverter.convertCalendarToJulianDate(date);
        assertEquals(expResult, result, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDate24May2017() {
        // Date: May 24, 2017 Time: 0:00
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar date = new GregorianCalendar(2017,4,24,0,0);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResult = 2457897.5;
        double result = JulianDateConverter.convertCalendarToJulianDate(date);
        assertEquals(expResult, result, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDate24May2017HoursMin() {
        // Date: May 24, 2017 Time: 17:46
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // JD 2457898.240278
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        // JD 2457898.2402778
        GregorianCalendar date = new GregorianCalendar(2017,4,24,17,46);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResult = 2457898.2402778;
        double result = JulianDateConverter.convertCalendarToJulianDate(date);
        assertEquals(expResult, result, 1.0E-07);
    }

    @Test
    public void testConvertCalendarToJulianDateLeapYear1996() {
        // 1996 was a leap year since it is divisible by 4
        // and not divisible by 400
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar dateA = new GregorianCalendar(1996,1,27,0,0);
        dateA.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateB = new GregorianCalendar(1996,1,28,0,0);
        dateB.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateC = new GregorianCalendar(1996,1,29,0,0);
        dateC.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateD = new GregorianCalendar(1996,2,1,0,0);
        dateD.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResultA = 2450140.5;
        double expResultB = 2450141.5;
        double expResultC = 2450142.5;
        double expResultD = 2450143.5;
        double resultA = JulianDateConverter.convertCalendarToJulianDate(dateA);
        double resultB = JulianDateConverter.convertCalendarToJulianDate(dateB);
        double resultC = JulianDateConverter.convertCalendarToJulianDate(dateC);
        double resultD = JulianDateConverter.convertCalendarToJulianDate(dateD);
        assertEquals("Wrong Julian Date for Feb 27, 1996",expResultA, resultA, 1.0E-14);
        assertEquals("Wrong Julian Date for Feb 28, 1996",expResultB, resultB, 1.0E-14);
        assertEquals("Wrong Julian Date for Feb 29, 1996",expResultC, resultC, 1.0E-14);
        assertEquals("Wrong Julian Date for Mar 1, 1996",expResultD, resultD, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDateLeapYear2000() {
        // 2000 was a leap year since it is divisible by 400
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar dateA = new GregorianCalendar(2000,1,27,0,0);
        dateA.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateB = new GregorianCalendar(2000,1,28,0,0);
        dateB.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateC = new GregorianCalendar(2000,1,29,0,0);
        dateC.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateD = new GregorianCalendar(2000,2,1,0,0);
        dateD.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResultA = 2451601.5;
        double expResultB = 2451602.5;
        double expResultC = 2451603.5;
        double expResultD = 2451604.5;
        double resultA = JulianDateConverter.convertCalendarToJulianDate(dateA);
        double resultB = JulianDateConverter.convertCalendarToJulianDate(dateB);
        double resultC = JulianDateConverter.convertCalendarToJulianDate(dateC);
        double resultD = JulianDateConverter.convertCalendarToJulianDate(dateD);
        assertEquals("Wrong Julian Date for Feb 27, 2000",expResultA, resultA, 1.0E-14);
        assertEquals("Wrong Julian Date for Feb 28, 2000",expResultB, resultB, 1.0E-14);
        assertEquals("Wrong Julian Date for Feb 29, 2000",expResultC, resultC, 1.0E-14);
        assertEquals("Wrong Julian Date for Mar 1, 2000",expResultD, resultD, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDateNoLeapYear1900() {
        // 1900 was not a leap year since it is divisable by 100, but
        // not divisable by 400
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar dateA = new GregorianCalendar(1900,1,27,0,0);
        dateA.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateB = new GregorianCalendar(1900,1,28,0,0);
        dateB.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateC = new GregorianCalendar(1900,2,1,0,0);
        dateC.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateD = new GregorianCalendar(1900,2,2,0,0);
        dateD.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResultA = 2415077.5;
        double expResultB = 2415078.5;
        double expResultC = 2415079.5;
        double expResultD = 2415080.5;
        double resultA = JulianDateConverter.convertCalendarToJulianDate(dateA);
        double resultB = JulianDateConverter.convertCalendarToJulianDate(dateB);
        double resultC = JulianDateConverter.convertCalendarToJulianDate(dateC);
        double resultD = JulianDateConverter.convertCalendarToJulianDate(dateD);
        assertEquals("Wrong Julian Date for Feb 27, 1900",expResultA, resultA, 1.0E-14);
        assertEquals("Wrong Julian Date for Feb 28, 1900",expResultB, resultB, 1.0E-14);
        assertEquals("Wrong Julian Date for Mar 1, 1900",expResultC, resultC, 1.0E-14);
        assertEquals("Wrong Julian Date for Mar 2, 1900",expResultD, resultD, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDateLeapYear1600() {
        // 1600 was a leap year since it is divisible by 400
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar dateA = new GregorianCalendar(1600,1,27,0,0);
        dateA.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateB = new GregorianCalendar(1600,1,28,0,0);
        dateB.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateC = new GregorianCalendar(1600,1,29,0,0);
        dateC.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateD = new GregorianCalendar(1600,2,1,0,0);
        dateD.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResultA = 2305504.5;
        double expResultB = 2305505.5;
        double expResultC = 2305506.5;
        double expResultD = 2305507.5;
        double resultA = JulianDateConverter.convertCalendarToJulianDate(dateA);
        double resultB = JulianDateConverter.convertCalendarToJulianDate(dateB);
        double resultC = JulianDateConverter.convertCalendarToJulianDate(dateC);
        double resultD = JulianDateConverter.convertCalendarToJulianDate(dateD);
        assertEquals("Wrong Julian Date for Feb 27, 1600",expResultA, resultA, 1.0E-14);
        assertEquals("Wrong Julian Date for Feb 28, 1600",expResultB, resultB, 1.0E-14);
        assertEquals("Wrong Julian Date for Feb 29, 1600",expResultC, resultC, 1.0E-14);
        assertEquals("Wrong Julian Date for Mar 1, 1600",expResultD, resultD, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDateGregorianChange() {
        // In 1582 there was a 10-day gap
        // The day after October 4, 1582 is October 15, 1582
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar dateA = new GregorianCalendar(1582,9,3,0,0);
        dateA.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateB = new GregorianCalendar(1582,9,4,0,0);
        dateB.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateC = new GregorianCalendar(1582,9,15,0,0);
        dateC.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar dateD = new GregorianCalendar(1582,9,16,0,0);
        dateD.setTimeZone(TimeZone.getTimeZone("UTC"));
        double expResultA = 2299158.5;
        double expResultB = 2299159.5;
        double expResultC = 2299160.5;
        double expResultD = 2299161.5;
        double resultA = JulianDateConverter.convertCalendarToJulianDate(dateA);
        double resultB = JulianDateConverter.convertCalendarToJulianDate(dateB);
        double resultC = JulianDateConverter.convertCalendarToJulianDate(dateC);
        double resultD = JulianDateConverter.convertCalendarToJulianDate(dateD);
        assertEquals("Wrong Julian Date for Oct 3, 1582",expResultA, resultA, 1.0E-14);
        assertEquals("Wrong Julian Date for Oct 4, 1582",expResultB, resultB, 1.0E-14);
        assertEquals("Wrong Julian Date for Oct 15, 1582",expResultC, resultC, 1.0E-14);
        assertEquals("Wrong Julian Date for Oct 16, 1582",expResultD, resultD, 1.0E-14);
    }
    
    @Test
    public void testConvertCalendarToJulianDateRangeStepDay() {
        // Julian Date is 0 at Jan 1, 4713 BC 12h
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar startDate = new GregorianCalendar(4713,0,1,12,0);
        startDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        startDate.set(Calendar.ERA,GregorianCalendar.BC);
        GregorianCalendar endDate = new GregorianCalendar(5000,0,1,12,0);
        endDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar date = startDate;
        double expResult = 0.0;
        int nrMinutesPerDay = 24*60;
        while (date.before(endDate)) {
            double result = JulianDateConverter.convertCalendarToJulianDate(date);
            assertEquals(expResult, result, 1.0E-14);
            date.add(Calendar.MINUTE, nrMinutesPerDay);
            expResult = expResult + 1.0;
        }
    }
    
    @Test
    @Ignore("This test is ignored because it takes a long time")
    public void testConvertCalendarToJulianDateRangeStepHour() {
        // Julian Date is 0 at Jan 1, 4713 BC 12h
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        GregorianCalendar startDate = new GregorianCalendar(4713,0,1,12,0);
        startDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        startDate.set(Calendar.ERA,GregorianCalendar.BC);
        GregorianCalendar endDate = new GregorianCalendar(5000,0,1,12,0);
        endDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar date = startDate;
        double expResult = 0.0;
        while (date.before(endDate)) {
            double result = JulianDateConverter.convertCalendarToJulianDate(date);
            assertEquals(expResult, result, 1.0E-09);
            date.add(Calendar.MINUTE, 60);
            // Use current result to compute expected result for the next test
            // to avoid a cummulative error when computing expected result
            expResult = result + 1.0/24;
        }
    }
    
    /**
     * Method: convertJulianDateToCalendar(double julianDate)
     * Convert Julian date to calendar date.
     * Conversion of date October 15, 1582 or later is done assuming 
     * the Gregorian Calendar. Conversion of a date before October 15, 1582 
     * is not supported and an InvalidParameterException will be thrown.
     * The result will be a GregorianCalendar object with time zone UTC.
     * @param julianDate Julian date
     * @return Gregorian calendar date
     * @throws InvalidParameterException
     */
    
    @Test (expected = InvalidParameterException.class)
    public void testConvertJulianDateToCalendarJDZero() {
        // Julian Date is 0 at Jan 1, 4713 BC 12h
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        // Conversion of a date before October 15, 1582 is not supported
        double julianDate = 0.0;
        JulianDateConverter.convertJulianDateToCalendar(julianDate);
    }
    
    @Test (expected = InvalidParameterException.class)
    public void testConvertJulianDateToCalendar14October1582() {
        // Date: October 15, 1582 Time: 0:00, JD = 2299160.5
        // Date: October 4,  1582 Time: 0:00, JD = 2299159.5
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        // Conversion of a date before October 15, 1582 is not supported
        double julianDate = 2299159.5; // October 4, 1582
        JulianDateConverter.convertJulianDateToCalendar(julianDate);
    }
    
    @Test
    public void testConvertJulianDateToCalendar15October1582() {
        // Date: October 15, 1582 Time: 0:00, JD = 2299160.5
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        double julianDate = 2299160.5;
        GregorianCalendar expResult = new GregorianCalendar(1582,9,15,0,0);
        expResult.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar actResult = JulianDateConverter.convertJulianDateToCalendar(julianDate);
        assertEquals(0, actResult.compareTo(expResult));
    }
    
    @Test
    public void testConvertJulianDateToCalendar24May2017() {
        // Date: May 24, 2017 Time: 0:00
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        double julianDate = 2457897.5;
        GregorianCalendar expResult = new GregorianCalendar(2017,4,24,0,0);
        expResult.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar actResult = JulianDateConverter.convertJulianDateToCalendar(julianDate);
        assertEquals(0, actResult.compareTo(expResult));
    }
    
    @Test
    public void testConvertJulianDateToCalendar24May2017HoursMin() {
        // Date: May 24, 2017 Time: 17:46
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // JD 2457898.240278
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        // JD 2457898.2402778
        double julianDate = 2457898.2402778;
        GregorianCalendar expResult = new GregorianCalendar(2017,4,24,17,46);
        expResult.setTimeZone(TimeZone.getTimeZone("UTC"));
        GregorianCalendar actResult = JulianDateConverter.convertJulianDateToCalendar(julianDate);
        assertEquals(0, actResult.compareTo(expResult));
    }
    
    @Test
    public void testConvertJulianDateToCalendarRangeStepDay() {
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        // Conversion of a date before October 15, 1582 is not supported
        // Date: October 15, 1582 Time: 0:00, JD = 2299160.5
        // Date: January 1, 5000 Time: 0:00, JD = 3547272.5
        double julianDate = 2299160.5;
        GregorianCalendar expResult = new GregorianCalendar(1582,9,15,0,0);
        expResult.setTimeZone(TimeZone.getTimeZone("UTC"));
        int nrMinutesPerDay = 24*60;
        while (julianDate < 3547272.5) {
            GregorianCalendar actResult = JulianDateConverter.convertJulianDateToCalendar(julianDate);
            assertEquals(0, actResult.compareTo(expResult));
            julianDate = julianDate + 1.0;
            expResult.add(Calendar.MINUTE, nrMinutesPerDay);
        }
    }
    
    @Test
    @Ignore("This test is ignored because it takes a long time")
    public void testConvertJulianDateToCalendarRangeStep45Minutes() {
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        // https://ssd.jpl.nasa.gov/tc.cgi#top
        // Conversion of a date before October 15, 1582 is not supported
        // Date: October 15, 1582 Time: 0:00, JD = 2299160.5
        // Date: January 1, 5000 Time: 0:00, JD = 3547272.5
        GregorianCalendar expResult = new GregorianCalendar(1582,9,15,0,0);
        expResult.setTimeZone(TimeZone.getTimeZone("UTC"));
        double julianDate = 2299160.5;
        while (julianDate < 3547272.5) {
            Calendar actResult = JulianDateConverter.convertJulianDateToCalendar(julianDate);
            assertEquals(expResult.get(Calendar.YEAR), actResult.get(Calendar.YEAR));
            assertEquals(expResult.get(Calendar.MONTH), actResult.get(Calendar.MONTH));
            assertEquals(expResult.get(Calendar.DAY_OF_MONTH), actResult.get(Calendar.DAY_OF_MONTH));
            assertEquals(expResult.get(Calendar.HOUR), actResult.get(Calendar.HOUR));
            assertEquals(expResult.get(Calendar.MINUTE), actResult.get(Calendar.MINUTE));
            julianDate = julianDate + 0.03125;
            expResult.add(Calendar.MINUTE, 45);
        }
    }
}
