package ephemeris;

import org.junit.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for class CalendarUtil.
 * @author Nico Kuijpers
 */
public class CalendarUtilTest {

    public CalendarUtilTest() {
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

    @Test
    public void testCreateGregorianCalendar() {
        int yearExpected = 2017;
        int monthExpected = 0;
        int dayOfMonthExpected = 15;
        int hourOfDayExpected = 14;
        int minuteExpected = 23;
        int secondExpected = 45;
        int millisecondExpected = 19;
        Calendar cal = new GregorianCalendar(yearExpected,monthExpected,dayOfMonthExpected,
                hourOfDayExpected,minuteExpected,secondExpected);
        cal.add(Calendar.MILLISECOND,millisecondExpected);
        GregorianCalendar calGC = CalendarUtil.createGregorianCalendar(cal);
        int yearActual = calGC.get(Calendar.YEAR);
        int monthActual = calGC.get(Calendar.MONTH);
        int dayOfMonthActual = calGC.get(Calendar.DAY_OF_MONTH);
        int hourOfDayActual = calGC.get(Calendar.HOUR_OF_DAY);
        int minuteActual = calGC.get(Calendar.MINUTE);
        int secondActual = calGC.get(Calendar.SECOND);
        int millisecondActual = calGC.get(Calendar.MILLISECOND);
        assertEquals(yearExpected,yearActual);
        assertEquals(monthExpected,monthActual);
        assertEquals(dayOfMonthExpected,dayOfMonthActual);
        assertEquals(hourOfDayExpected,hourOfDayActual);
        assertEquals(minuteExpected,minuteActual);
        assertEquals(secondExpected,secondActual);
        assertEquals(millisecondExpected,millisecondActual);
    }

    @Test
    public void testCalendarToString() {
        // 2017-01-09 14:23:06.019
        Calendar calendar = new GregorianCalendar(2017,0,9,14, 23,6);
        calendar.set(Calendar.MILLISECOND,19);
        String expectedString = "2017-01-09 14:23:06.019";
        String actualString = CalendarUtil.calendarToString(calendar);
        boolean expectedResult = true;
        boolean actualResult = expectedString.equals(actualString);
        assertEquals(expectedResult, actualResult);
    }
}