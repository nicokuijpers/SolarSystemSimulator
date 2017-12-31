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
package experiments;

import ephemeris.EphemerisSolarSystem;
import ephemeris.IEphemeris;
import ephemeris.SolarSystemParameters;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import particlesystem.Particle;
import solarsystem.SolarSystem;
import solarsystem.SolarSystemBody;
import util.Vector3D;

/**
 * Asteroid 3122 Florence passed Earth at a distance of 7,066,000 km on 
 * September 1st, 2017.
 * In this simulation experiment the minimum distance between Earth and Florence 
 * is determined as wel as the date/time it occurred.
 * 
 * @author Nico Kuijpers
 */
public class FlorenceEarthExperiment {
    
    // Ephemeris
    private IEphemeris ephemeris;
    
    // The Solar System
    private final SolarSystem solarSystem;
    
    // Start date for simulation
    private final GregorianCalendar simulationStartDateTime;
    
    // End date for simulation
    private final GregorianCalendar simulationEndDateTime;
    
    // Simulation time step in s
    private final long deltaT;
    
    /**
     * Constructor.
     * Set simulation start and end date. Create the Solar System.
     */
    public FlorenceEarthExperiment() {
        // Set ephemeris
        ephemeris = EphemerisSolarSystem.getInstance();
        
        // Start simulation at April 1st, 2017
        // Note that January is month 0
        simulationStartDateTime = new GregorianCalendar(2017,3,1);
        
        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid 
        // sudden changes in ephemeris due to changes from 
        // winter time to summer time and vice versa
        simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Stop simulation at April 1st, 2018
        simulationEndDateTime = new GregorianCalendar(2018,3,1);
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Create the Solar System
        solarSystem = new SolarSystem(simulationStartDateTime);
        
        // Set General Relativity flag
        // True means General Relativity is applied
        // False means Newton Mechanics is applied
        // solarSystem.setGeneralRelativityFlag(true);
        solarSystem.setGeneralRelativityFlag(false);
        
        // Set simulation time step in seconds
        // deltaT = (long) 60; // 1 minute
        deltaT = (long) (60*60); // 1 hour
    }

    /**
     * Simulate passage of Florence.
     */
    public void simulateFlorenceEarthPassage() {
        // Planet Earth (simulation particle)
        Particle earth = solarSystem.getParticle("earth");
        
        // Asteroid 3122 Florence (simulation particle)
        Particle florence = solarSystem.getParticle("florence");
        
        // Initialize minimum distance between Earth and Florence
        double minimumDistance = SolarSystemParameters.ASTRONOMICALUNIT;
        
        // Initialize date/time of minimum distance
        GregorianCalendar minimumDistanceDateTime = (GregorianCalendar)simulationStartDateTime.clone();
        
        // Initialize simulation date/time
        GregorianCalendar simulationDateTime;
        simulationDateTime = (GregorianCalendar)simulationStartDateTime.clone();
        simulationDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Run simulation
        while(simulationDateTime.before(simulationEndDateTime)) {
            // Advance one time step
            solarSystem.advanceRungeKutta(deltaT);
            
            // Correction for position of Sun
            solarSystem.correctDrift();
            
            // Update simulation date/time
            simulationDateTime.add(Calendar.SECOND, (int)deltaT);
            
            // Position of planet Earth
            Vector3D positionEarth = earth.getPosition();
            
            // Position of asteroid 3122 Florence
            Vector3D positionFlorence = florence.getPosition();
            
            // Current distance between Earth and Florence
            double currentDistance = positionFlorence.euclideanDistance(positionEarth);
            
            // Minimum distance and time of minimum distance
            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                minimumDistanceDateTime = (GregorianCalendar)simulationDateTime.clone();
            }
        }
        
        // Asteroid 3122 Florence passed Earth at a distance of 7,066,000 km 
        // on September 1st, 2017.  
        double expectedDistance = 7066000000.0; // Distance [m]
        
        // Show results on screen
        System.out.println("Results from simulation:");
        if (solarSystem.getGeneralRelativityFlag()) {
            System.out.println("General Relativity with time step " + deltaT + " seconds");
        }
        else {
            System.out.println("Newton Mechanics with time step " + deltaT + " seconds");
        }
        System.out.println("Expected minimum distance between Earth and Florence: " + expectedDistance/1000 + " km");
        System.out.println("Expected date of minimum distance is September 1, 2017");
        System.out.println("Actual minimum distance between Earth and Florence:  " + minimumDistance/1000 + " km");
        System.out.println("Actual date/time of minimum distance: " + calendarToString(minimumDistanceDateTime));
    }
    
    
    /**
     * Compute passage of Florence from ephemeris.
     */
    public void computeFlorenceEarthPassage() {
        
        // Initialize minimum distance between Earth and Florence
        double minimumDistance = SolarSystemParameters.ASTRONOMICALUNIT;
        
        // Initialize date/time of minimum distance
        GregorianCalendar minimumDistanceDateTime = (GregorianCalendar)simulationStartDateTime.clone();
        
        // Initialize current date/time
        GregorianCalendar currentDateTime;
        currentDateTime = (GregorianCalendar)simulationStartDateTime.clone();
        currentDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Compute ephemeris for each time step
        while(currentDateTime.before(simulationEndDateTime)) {
           
            // Update simulation date/time
            currentDateTime.add(Calendar.SECOND, (int)deltaT);
            
            // Position of planet Earth
            Vector3D positionEarth = ephemeris.getBodyPosition("earth", currentDateTime);
            
            // Position of asteroid 3122 Florence
            Vector3D positionFlorence = ephemeris.getBodyPosition("florence", currentDateTime);
            
            // Current distance between Earth and Florence
            double currentDistance = positionFlorence.euclideanDistance(positionEarth);
            
            // Minimum distance and time of minimum distance
            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                minimumDistanceDateTime = (GregorianCalendar)currentDateTime.clone();
            }
        }
        
        // Asteroid 3122 Florence passed Earth at a distance of 7,066,000 km 
        // on September 1st, 2017.  
        double expectedDistance = 7066000000.0; // Distance [m]
        
        // Show results on screen
        System.out.println("Results from Ephemeris:");
        System.out.println("Expected minimum distance between Earth and Florence: " + expectedDistance/1000 + " km");
        System.out.println("Expected date of minimum distance is September 1, 2017");
        System.out.println("Actual minimum distance between Earth and Florence:  " + minimumDistance/1000 + " km");
        System.out.println("Actual date/time of minimum distance: " + calendarToString(minimumDistanceDateTime));
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
     * Main method.
     * Simulate and compute passage of Florence.
     * @param args input arguments (not used)
     */
    public static void main (String[] args) {
        // Experiment set-up
        FlorenceEarthExperiment experiment = new FlorenceEarthExperiment();
        
        // Run simulation to find shortest distance
        experiment.simulateFlorenceEarthPassage();
        
        // Use ephemeris to find shortest distance
        experiment.computeFlorenceEarthPassage();
    }
}
