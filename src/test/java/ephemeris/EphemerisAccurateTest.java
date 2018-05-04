/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ephemeris;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import util.Vector3D;

/**
 * Test consistency of AccurateEphemeris by predicting the position of each
 * body for the next hour, based on previous position and velocity and the
 * current velocity.
 * @author Nico Kuijpers
 */
public class EphemerisAccurateTest {
    
    // Major planets
    private static List<String> majorBodies;
    
    // Accurate ephemeris
    private IEphemeris ephemerisAccurate = null;
    
    public EphemerisAccurateTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        majorBodies = new ArrayList<>();
        majorBodies.add("sun");
        majorBodies.add("mercury");
        majorBodies.add("venus");
        majorBodies.add("earth");
        majorBodies.add("moon");
        majorBodies.add("mars");
        majorBodies.add("jupiter");
        majorBodies.add("saturn");
        majorBodies.add("uranus");
        majorBodies.add("neptune");
        majorBodies.add("pluto");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        ephemerisAccurate = EphemerisAccurate.getInstance();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    @Ignore("This test is ignored as it takes a relatively long time")
    public void testPositionVelocityMajorPlanets() {
        // Start test at January 1, 1620
        // Note that January is month 0
        GregorianCalendar date = new GregorianCalendar(1620,0,1);
                
        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid 
        // sudden changes in ephemeris due to changes from 
        // winter time to summer time and vice versa
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // End test at January 31, 2200
        GregorianCalendar endDate = new GregorianCalendar(2200,0,31);
        
        // Previous position and velocity
        Map<String,Vector3D> positionPrevious = new HashMap<>();
        Map<String,Vector3D> velocityPrevious = new HashMap<>();
        
        // Start test
        while (date.before(endDate)) {
            
            for (String bodyName : majorBodies) {

                // Compute (x,y,z) position [m] for body from ephemeris
                Vector3D position = ephemerisAccurate.getBodyPosition(bodyName, date);
         
                // Compute (x,y,z) velocity [m/s] for body from ephemeris
                Vector3D velocity = ephemerisAccurate.getBodyVelocity(bodyName, date);
                
                // Compare actual position to predicted position
                if (positionPrevious.get(bodyName) != null &&
                    velocityPrevious.get(bodyName) != null) {
                    /*
                     * Compute predicted position from position and velocity
                     * of the previous hour and the current velocity:
                     * Predicted position = 
                     *     previous position + 
                     *     0.5 * deltaTime * previous velocity +
                     *     0.5 * deltaTime * current velocity
                     * with deltaTime = 3600 s
                     */
                    Vector3D positionPredicted = 
                        positionPrevious.get(bodyName).
                        plus(velocityPrevious.get(bodyName).scalarProduct(1800.0).
                        plus(velocity.scalarProduct(1800.0)));
                
                        double difference = positionPredicted.euclideanDistance(position);
                        assertEquals("Difference between position and predicted position for " + 
                            bodyName, 0.0, difference, 500.0); // 500 m
                }
                
                // Store position and velocity of body for the next hour
                positionPrevious.put(bodyName,position);
                velocityPrevious.put(bodyName,velocity);
            }
            date.add(Calendar.MINUTE, 60);
        }
    }
}
