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

import ephemeris.SolarSystemParameters;
import particlesystem.Particle;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.*;

/**
 * Halley's Comet is a short-period comet visible from Earth every 
 * 74–79 years. In this experiment the computed perihelion passages are
 * checked against known perihelion passages obtained from
 * https://en.wikipedia.org/wiki/Halley%27s_Comet
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
    private final int timeStep = -(60 * 60); // -1 hour

    // List of expected perihelion passages
    List<Calendar> expectedPassageDates;

    // List of actual perihelion passages
    List<Calendar> actualPassageDates;

    /**
     * Default constructor. Experiment set-up.
     */
    public HalleyPerihelionPassageExperiment() {

        // Orbital elements of Halley's Comet at Epoch 2449400.5 (1994-Feb-17.0)
        simulationStartDate = new GregorianCalendar(1994,1,17);
        simulationStartDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        simulationStartDate.set(Calendar.ERA,GregorianCalendar.AD);
        
        // Stop at January 1st, 300BC
        simulationEndDate = new GregorianCalendar(300,0,1);
        simulationEndDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        simulationEndDate.set(Calendar.ERA,GregorianCalendar.BC);

        // Create Solar System
        solarSystem = new SolarSystem(simulationStartDate);

        // Set General Relativity or Newton Mechanics
        solarSystem.setGeneralRelativityFlag(false); // Newton Mechanics
        // solarSystem.setGeneralRelativityFlag(true); // General Relativity

        // Expected perihelion passages obtained from
        // https://en.wikipedia.org/wiki/Halley%27s_Comet
        // Note that Jan = 0 for GregorianCalendar
        expectedPassageDates = new ArrayList<>();
        GregorianCalendar gc;
        gc = new GregorianCalendar(240,4,15);
        gc.set(Calendar.ERA,GregorianCalendar.BC);
        expectedPassageDates.add(gc);
        gc = new GregorianCalendar(164,4,20);
        gc.set(Calendar.ERA,GregorianCalendar.BC);
        expectedPassageDates.add(gc);
        gc = new GregorianCalendar(87,7,15);
        gc.set(Calendar.ERA,GregorianCalendar.BC);
        expectedPassageDates.add(gc);
        gc = new GregorianCalendar(12,9,8);
        gc.set(Calendar.ERA,GregorianCalendar.BC);
        expectedPassageDates.add(gc);
        expectedPassageDates.add(new GregorianCalendar(66,0,26));
        expectedPassageDates.add(new GregorianCalendar(141,2,25));
        expectedPassageDates.add(new GregorianCalendar(218,3,6));
        expectedPassageDates.add(new GregorianCalendar(295,3,7));
        expectedPassageDates.add(new GregorianCalendar(374,1,13));
        expectedPassageDates.add(new GregorianCalendar(451,6,3));
        expectedPassageDates.add(new GregorianCalendar(530,10,15));
        expectedPassageDates.add(new GregorianCalendar(607,2,26));
        expectedPassageDates.add(new GregorianCalendar(684,10,26));
        expectedPassageDates.add(new GregorianCalendar(760,5,10));
        expectedPassageDates.add(new GregorianCalendar(837,1,25));
        expectedPassageDates.add(new GregorianCalendar(912,6,27));
        expectedPassageDates.add(new GregorianCalendar(989,8,2));
        expectedPassageDates.add(new GregorianCalendar(1066,2,25));
        expectedPassageDates.add(new GregorianCalendar(1145,3,19));
        expectedPassageDates.add(new GregorianCalendar(1222,8,10));
        expectedPassageDates.add(new GregorianCalendar(1301,9,22));
        expectedPassageDates.add(new GregorianCalendar(1378,10,9));
        expectedPassageDates.add(new GregorianCalendar(1456,0,8));
        expectedPassageDates.add(new GregorianCalendar(1531,7,26));
        expectedPassageDates.add(new GregorianCalendar(1607,9,27));
        expectedPassageDates.add(new GregorianCalendar(1682,8,15));
        expectedPassageDates.add(new GregorianCalendar(1758,2,13));
        expectedPassageDates.add(new GregorianCalendar(1835,10,16));
        expectedPassageDates.add(new GregorianCalendar(1910,3,20));
        expectedPassageDates.add(new GregorianCalendar(1986,1,9));

        // Initialize actual perihelion passages
        actualPassageDates = new ArrayList<>();
    }

    /**
     * Compute the perihelion passages of Halley's Comet.
     */
    private void computePerihelionPassages() {
        // Halley's Comet
        Particle halley = solarSystem.getParticle("Halley");

        // Initialize minimum distance between Halley's Comet and the Sun
        double minDistance = 100 * SolarSystemParameters.ASTRONOMICALUNIT;

        // Initialize date/time of minimum distance
        Calendar minDistanceDate = (Calendar) simulationStartDate.clone();

        // Compute distance between Halley' Comet and the Sun each day
        // Date of minimum distance corresponds to perihelion passage
        while (solarSystem.getSimulationDateTime().after(simulationEndDate)) {

            // Advance one time step backward in time
            solarSystem.advanceSimulationSingleStep(timeStep);

            // Position of Halley's Comet
            Vector3D positionHalley = halley.getPosition();

            // Distance between Halley's Comet and the Sun
            double distance = positionHalley.magnitude();

            // Minimum distance and time of minimum distance
            if (distance < minDistance) {
                minDistance = distance;
                minDistanceDate = (Calendar) solarSystem.getSimulationDateTime().clone();
            } else {
                // Store perihelion passage
                Calendar passageDate = (Calendar) minDistanceDate.clone();
                actualPassageDates.add(passageDate);

                // Print date/time of perihelion passage
                System.out.println("Perihelion passage: " + calendarToString(minDistanceDate));

                // Simulate for a period of 40 years
                Calendar simulationEndDatePeriod = (Calendar) solarSystem.getSimulationDateTime().clone();
                simulationEndDatePeriod.add(Calendar.YEAR, -40);
                while (solarSystem.getSimulationDateTime().after(simulationEndDatePeriod)) {
                    solarSystem.advanceSimulationSingleStep(timeStep);
                }

                // Initialize minimum distance 
                minDistance = 100 * SolarSystemParameters.ASTRONOMICALUNIT;
            }
        }

        // Reverse list of passage dates
        Collections.reverse(actualPassageDates);
    }

    /**
     * Convert date to string.
     * @param date
     * @return date as string
     */
    private String calendarToString(Calendar date) {
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1;
        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
        StringBuilder result = new StringBuilder();
        if (date.get(Calendar.ERA) == GregorianCalendar.BC) {
            result.append("BC ");
        }
        else {
            result.append("AD ");
        }
        result.append(String.format("%4d",year));
        result.append("-");
        result.append(String.format("%02d",month));
        result.append("-");
        result.append(String.format("%02d",dayOfMonth));
        return result.toString();
    }

    /**
     * Print results after running experiment.
     */
    private void printResults() {
        int nrPassages = Math.min(expectedPassageDates.size(),actualPassageDates.size());
        System.out.println("Computed perihelion passages (actual) compared to known passages (expected):");
        for (int i = 0; i < nrPassages; i++) {
            Date expectedDate = expectedPassageDates.get(i).getTime();
            Date actualDate = actualPassageDates.get(i).getTime();
            long differenceInMilliseconds = Math.abs(actualDate.getTime() - expectedDate.getTime());
            long differenceInDays = differenceInMilliseconds / (1000 * 60 * 60 * 24);
            System.out.print("Expected date: " + calendarToString(expectedPassageDates.get(i)));
            System.out.print("  Actual date: " + calendarToString(actualPassageDates.get(i)));
            System.out.println("  Difference: " + differenceInDays + " days");
        }
    }

    /**
     * Main method.
     * Run experiment and print results.
     *
     * @param args input arguments (not used)
     */
    public static void main(String[] args) {
        // Experiment set-up
        HalleyPerihelionPassageExperiment experiment = new HalleyPerihelionPassageExperiment();

        // Run experiment
        experiment.computePerihelionPassages();

        // Print results
        experiment.printResults();
    }

    /*
        Results (Newton Mechanics):
        Computed perihelion passages (actual) compared to known passages (expected):
        Expected date: BC  240-05-15  Actual date: BC  240-07-29  Difference: 75 days
        Expected date: BC  164-05-20  Actual date: BC  163-01-19  Difference: 244 days
        Expected date: BC   87-08-15  Actual date: BC   87-08-18  Difference: 3 days
        Expected date: BC   12-10-08  Actual date: BC   12-10-12  Difference: 4 days
        Expected date: AD   66-01-26  Actual date: AD   66-01-10  Difference: 15 days
        Expected date: AD  141-03-25  Actual date: AD  141-02-26  Difference: 26 days
        Expected date: AD  218-04-06  Actual date: AD  218-04-12  Difference: 6 days
        Expected date: AD  295-04-07  Actual date: AD  295-03-30  Difference: 7 days
        Expected date: AD  374-02-13  Actual date: AD  374-01-11  Difference: 32 days
        Expected date: AD  451-07-03  Actual date: AD  451-06-03  Difference: 29 days
        Expected date: AD  530-11-15  Actual date: AD  530-09-22  Difference: 53 days
        Expected date: AD  607-03-26  Actual date: AD  607-03-29  Difference: 3 days
        Expected date: AD  684-11-26  Actual date: AD  684-10-19  Difference: 37 days
        Expected date: AD  760-06-10  Actual date: AD  760-05-17  Difference: 23 days
        Expected date: AD  837-02-25  Actual date: AD  837-01-29  Difference: 26 days
        Expected date: AD  912-07-27  Actual date: AD  912-07-15  Difference: 11 days
        Expected date: AD  989-09-02  Actual date: AD  989-09-28  Difference: 26 days
        Expected date: AD 1066-03-25  Actual date: AD 1066-04-15  Difference: 21 days
        Expected date: AD 1145-04-19  Actual date: AD 1145-05-17  Difference: 28 days
        Expected date: AD 1222-09-10  Actual date: AD 1222-10-09  Difference: 29 days
        Expected date: AD 1301-10-22  Actual date: AD 1301-10-09  Difference: 12 days
        Expected date: AD 1378-11-09  Actual date: AD 1378-10-14  Difference: 25 days
        Expected date: AD 1456-01-08  Actual date: AD 1456-05-17  Difference: 130 days
        Expected date: AD 1531-08-26  Actual date: AD 1531-08-06  Difference: 19 days
        Expected date: AD 1607-10-27  Actual date: AD 1607-10-13  Difference: 13 days
        Expected date: AD 1682-09-15  Actual date: AD 1682-09-12  Difference: 2 days
        Expected date: AD 1758-03-13  Actual date: AD 1759-03-06  Difference: 358 days
        Expected date: AD 1835-11-16  Actual date: AD 1835-11-08  Difference: 7 days
        Expected date: AD 1910-04-20  Actual date: AD 1910-04-16  Difference: 3 days
        Expected date: AD 1986-02-09  Actual date: AD 1986-02-09  Difference: 0 days

        Results (General Relativity):
        Computed perihelion passages (actual) compared to known passages (expected):
        Expected date: BC  240-05-15  Actual date: BC  240-07-30  Difference: 76 days
        Expected date: BC  164-05-20  Actual date: BC  163-01-21  Difference: 246 days
        Expected date: BC   87-08-15  Actual date: BC   87-08-19  Difference: 4 days
        Expected date: BC   12-10-08  Actual date: BC   12-10-15  Difference: 7 days
        Expected date: AD   66-01-26  Actual date: AD   66-01-13  Difference: 12 days
        Expected date: AD  141-03-25  Actual date: AD  141-02-26  Difference: 26 days
        Expected date: AD  218-04-06  Actual date: AD  218-04-12  Difference: 6 days
        Expected date: AD  295-04-07  Actual date: AD  295-03-30  Difference: 7 days
        Expected date: AD  374-02-13  Actual date: AD  374-01-11  Difference: 32 days
        Expected date: AD  451-07-03  Actual date: AD  451-06-03  Difference: 29 days
        Expected date: AD  530-11-15  Actual date: AD  530-09-22  Difference: 53 days
        Expected date: AD  607-03-26  Actual date: AD  607-03-29  Difference: 3 days
        Expected date: AD  684-11-26  Actual date: AD  684-10-19  Difference: 37 days
        Expected date: AD  760-06-10  Actual date: AD  760-05-17  Difference: 23 days
        Expected date: AD  837-02-25  Actual date: AD  837-01-29  Difference: 26 days
        Expected date: AD  912-07-27  Actual date: AD  912-07-15  Difference: 11 days
        Expected date: AD  989-09-02  Actual date: AD  989-09-28  Difference: 26 days
        Expected date: AD 1066-03-25  Actual date: AD 1066-04-15  Difference: 21 days
        Expected date: AD 1145-04-19  Actual date: AD 1145-05-17  Difference: 28 days
        Expected date: AD 1222-09-10  Actual date: AD 1222-10-09  Difference: 29 days
        Expected date: AD 1301-10-22  Actual date: AD 1301-10-09  Difference: 12 days
        Expected date: AD 1378-11-09  Actual date: AD 1378-10-14  Difference: 25 days
        Expected date: AD 1456-01-08  Actual date: AD 1456-05-17  Difference: 130 days
        Expected date: AD 1531-08-26  Actual date: AD 1531-08-06  Difference: 19 days
        Expected date: AD 1607-10-27  Actual date: AD 1607-10-13  Difference: 13 days
        Expected date: AD 1682-09-15  Actual date: AD 1682-09-12  Difference: 2 days
        Expected date: AD 1758-03-13  Actual date: AD 1759-03-06  Difference: 358 days
        Expected date: AD 1835-11-16  Actual date: AD 1835-11-08  Difference: 7 days
        Expected date: AD 1910-04-20  Actual date: AD 1910-04-16  Difference: 3 days
        Expected date: AD 1986-02-09  Actual date: AD 1986-02-09  Difference: 0 days
     */
}
