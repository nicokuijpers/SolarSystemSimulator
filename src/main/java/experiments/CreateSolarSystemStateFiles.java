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

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import solarsystem.SolarSystem;
import solarsystem.SolarSystemException;

/**
 * Program to create Solar System state files with extension .sol.
 * These files can be loaded by the Solar System Simulator to continue the simulation
 * at the date of the file.
 * 
 * @author Nico Kuijpers
 */
public class CreateSolarSystemStateFiles {
    
    // File extension
    private final String EXTENSION = ".sol";
    
    // The Solar System
    private final SolarSystem solarSystem;
    
    // Start date for simulation
    private final GregorianCalendar simulationStartDateTime;
    
    // End date for simulation
    private final GregorianCalendar simulationEndDateTime;
    
    // Interval in years to save simulation state file
    private final int intervalYears;
    
    // File path: directory where files will be saved
    private final String filePath;
    
    /**
     * Constructor.
     * Set simulation start and end date. Create the Solar System.
     */
    public CreateSolarSystemStateFiles() {
        // Simulation start date/time
        simulationStartDateTime = new GregorianCalendar();
        
        // https://www.timeanddate.com/time/aboututc.html
        // Use Coordinated Universal Time (UTC) to avoid 
        // sudden changes in ephemeris due to changes from 
        // winter time to summer time and vice versa
        simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Start simulation at January 1st, 2000
        // Note that January is month 0
        simulationStartDateTime.set(2000,0,1,0,0);
        
        // Stop simulation at January 1st, 2010
        simulationEndDateTime = new GregorianCalendar();
        simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        simulationEndDateTime.set(2010,0,1,0,0);
        
        // Create the Solar System
        solarSystem = new SolarSystem(simulationStartDateTime);
        
        // Set General Relativity flag
        // True means General Relativity is applied
        // False means Newton Mechanics is applied
        // solarSystem.setGeneralRelativityFlag(true);
        solarSystem.setGeneralRelativityFlag(false);
        
        // Set interval in years to save simulation state file
        intervalYears = 2;
        
        // Set directory where files will be saved
        filePath = null;
    }

    /**
     * Simulate the Solar System and create Solar System state files.
     */
    public void createStateFiles() {
        // Initialize simulation date/time
        GregorianCalendar simulationDateTime;
        simulationDateTime = solarSystem.getSimulationDateTime();
        
        // Save initial state
        saveStateFile(simulationDateTime);
        
        // Year at which last state file was saved
        int yearLastSave = simulationDateTime.get(Calendar.YEAR);
        
        // Run simulation
        while(simulationDateTime.before(simulationEndDateTime)) {
            // Advance one day = 24 time steps of 1 hour
            solarSystem.advanceSimulationForward(24);
            
            // Update simulation date/time
            simulationDateTime = solarSystem.getSimulationDateTime();
            
            // Save simulation state
            int year = simulationDateTime.get(Calendar.YEAR);
            if (year - yearLastSave >= intervalYears) {
                // Save simulation state
                saveStateFile(simulationDateTime);
                
                // Update year at which last state file was saved
                yearLastSave = simulationDateTime.get(Calendar.YEAR);
            }
        }
    }   
       
    /**
     * Save current simulation state to file.
     * @param dateTime
     */
    public void saveStateFile(GregorianCalendar dateTime) {
        String fileName = calendarToString(dateTime) + EXTENSION;
        File file = new File(filePath,fileName);
        try {
            System.out.println("Save simulation state to file " + file);
            solarSystem.saveSimulationState(file);
        } catch (SolarSystemException ex) {
            System.err.println("Cannot save simulation state to file " + fileName);
        }
    }    
    
    /**
     * Convert GregorianCalendar to String to create file name.
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
        result.append("_");
        result.append(String.format("%04d", year)).append("-");
        result.append(String.format("%02d", month+1)).append("-");
        result.append(String.format("%02d", day)).append("_");
        result.append(String.format("%02d", hour)).append("-");
        result.append(String.format("%02d", minute));
        
        return result.toString();
    }
    
    /**
     * Main method.
     * Simulate and save simulation state files
     * @param args input arguments (not used)
     */
    public static void main (String[] args) {
        // Experiment set-up
        CreateSolarSystemStateFiles experiment = new CreateSolarSystemStateFiles();
        
        // Run simulation and save simulation state files
        experiment.createStateFiles();
    }
}
