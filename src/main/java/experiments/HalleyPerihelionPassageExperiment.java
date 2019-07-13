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
package experiments;

import ephemeris.CalendarUtil;
import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.*;

/**
 * Halley's Comet is a short-period comet visible from Earth every 
 * 74–79 years. In this experiment the computed perihelion passages are
 * checked against known perihelion passages.
 * Note that in this experiment time advances backward.
 *
 * @author Nico Kuijpers
 */
public class HalleyPerihelionPassageExperiment {

    // Solar system
    private SolarSystem solarSystem;

    // Start date for simulation
    private GregorianCalendar simulationStartDate;

    // End date for simulation
    private GregorianCalendar simulationEndDate;

    // Simulation time step in s
    private final int timeStep = (-60 * 60); // -1 hour

    public HalleyPerihelionPassageExperiment() {

        // Orbital elements of Halley's Comet at Epoch 2449400.5 (1994-Feb-17.0)
        simulationStartDate = new GregorianCalendar(1994,1,17);
        simulationStartDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        simulationStartDate.set(Calendar.ERA,GregorianCalendar.AD);
        
        // Stop at January 1st, 300BC
        simulationEndDate = new GregorianCalendar(300,0,1);
        simulationEndDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        simulationEndDate.set(Calendar.ERA,GregorianCalendar.BC);
        
        // Create solar system
        solarSystem = new SolarSystem(simulationStartDate);
    }

    private void runHalleyPerihelionPassage() {
         // Halley's Comet
        Particle halley = solarSystem.getParticle("Halley");
        
        // The Sun
        Particle sun = solarSystem.getParticle("Sun");

        // List to store perihelion passages
        List<Calendar> passageDates = new ArrayList<>();

        // Initialize minimum distance between Halley's Comet and the Sun
        double minimumDistance = 100 * SolarSystemParameters.ASTRONOMICALUNIT;
        
        // Initialize date/time of minimum distance
        Calendar minimumDistanceDate = (Calendar)simulationStartDate.clone();
        
        // Compute distance between Halley' Comet and the Sun each day
        // Date of minimum distance corresponds to perihelion passage
        Calendar simulationDate = (Calendar)simulationStartDate.clone();
        while(solarSystem.getSimulationDateTime().after(simulationEndDate)) {

            // Advance one time step backward in time
            solarSystem.advanceSimulationSingleStep(timeStep);
            
            // Position of Halley's Comet
            Vector3D positionHalley = halley.getPosition();
            
            // Current distance between Halley's Comet and the Sun
            double currentDistance = positionHalley.magnitude();
            
            // Minimum distance and time of minimum distance
            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                minimumDistanceDate = (Calendar)solarSystem.getSimulationDateTime().clone();
            }
            else {
                // Store perihelion passage
                Calendar passageDate = (Calendar)minimumDistanceDate.clone();
                passageDates.add(passageDate);
                
                // Print date/time of perihelion passage
                System.out.println(CalendarUtil.calendarToString(minimumDistanceDate));
                
                // Simulate for a period of 40 years
                Calendar simulationEndDatePeriod = (Calendar)solarSystem.getSimulationDateTime().clone();
                simulationEndDatePeriod.add(Calendar.YEAR, -40);
                while(solarSystem.getSimulationDateTime().after(simulationEndDatePeriod)) {
                    solarSystem.advanceSimulationSingleStep(timeStep);
                }
                
                // Initialize minimum distance 
                minimumDistance = 100 * SolarSystemParameters.ASTRONOMICALUNIT;
            }
        }
        
        // Compute time span between two passages
        int formerPassageYear = -1000; // Not defined
        for (Calendar date : passageDates) {
            int currentPassageYear = date.get(Calendar.YEAR);
            if (date.get(Calendar.ERA) == GregorianCalendar.BC) {
                currentPassageYear = -currentPassageYear + 1;
            }
            if (formerPassageYear == -1000) {
                formerPassageYear = currentPassageYear;
                System.out.println("Year : " + currentPassageYear);
            }
            else {
                int period = formerPassageYear - currentPassageYear;
                formerPassageYear = currentPassageYear;
                System.out.println("Year : " + currentPassageYear + " Period : " + period);
            }
        }
    }

    /**
     * Main method.
     * Simulate trajectories of Voyager 1, Voyager 2, and New Horizons
     *
     * @param args input arguments (not used)
     */
    public static void main(String[] args) {
        // Experiment set-up
        HalleyPerihelionPassageExperiment experiment = new HalleyPerihelionPassageExperiment();

        // Run experiment
        experiment.runHalleyPerihelionPassage();
    }

    /*
        Output:
        1986-02-09 11:00
        1910-04-16 02:00
        1835-11-08 18:00
        1759-03-06 07:00
        1682-09-12 02:00
        1607-10-13 17:00
        1531-08-06 19:00
        1456-05-17 09:00
        1378-10-14 14:00
        1301-10-09 11:00
        1222-10-09 14:00
        1145-05-17 21:00
        1066-04-15 18:00
        989-09-28 20:00
        912-07-15 17:00
        837-01-29 02:00
        760-05-17 22:00
        684-10-19 23:00
        607-03-29 16:00
        530-09-22 08:00
        451-06-03 01:00
        374-01-11 08:00
        295-03-30 03:00
        218-04-12 10:00
        141-02-26 05:00
        66-01-10 18:00
        12-10-12 07:00
        87-08-18 21:00
        163-01-19 12:00
        240-07-29 09:00
        Year : 1986
        Year : 1910 Period : 76
        Year : 1835 Period : 75
        Year : 1759 Period : 76
        Year : 1682 Period : 77
        Year : 1607 Period : 75
        Year : 1531 Period : 76
        Year : 1456 Period : 75
        Year : 1378 Period : 78
        Year : 1301 Period : 77
        Year : 1222 Period : 79
        Year : 1145 Period : 77
        Year : 1066 Period : 79
        Year : 989 Period : 77
        Year : 912 Period : 77
        Year : 837 Period : 75
        Year : 760 Period : 77
        Year : 684 Period : 76
        Year : 607 Period : 77
        Year : 530 Period : 77
        Year : 451 Period : 79
        Year : 374 Period : 77
        Year : 295 Period : 79
        Year : 218 Period : 77
        Year : 141 Period : 77
        Year : 66 Period : 75
        Year : -11 Period : 77
        Year : -86 Period : 75
        Year : -162 Period : 76
        Year : -239 Period : 77
     */
}
