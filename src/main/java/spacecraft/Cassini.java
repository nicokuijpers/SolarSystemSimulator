/*
 * Copyright (c) 2021 Nico Kuijpers
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

import ephemeris.CalendarUtil;
import ephemeris.JulianDateConverter;
import solarsystem.SolarSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

public class Cassini extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/Cassini–Huygens
     *
     * CASSINI FINAL MISSION REPORT 2018 1 VOLUME 4: APPENDIX A – SUPPLEMENTARY MATERIAL
     * https://pds-atmospheres.nmsu.edu/data_and_services/atmospheres_data/Cassini/Cassini/DOCS%20for%20events%20&%20Configuration%20page/maneuver%20history.pdf
     *
     *
     * Launch October 15, 1997
     * End of mission September 15, 2017
     *
     * Orbital parameters are read from file and are obtained from
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Ephemeris Type [change] :  ELEMENTS
     * Target Body [change] :     Cassini (spacecraft) [-82]
     * Center [change] :          Sun (body center) [500@10]
     * Time Span [change] :       Start=1997-10-16, Stop=2017-09-15, Step=1 d
     * Table Settings [change] :  defaults
     * Display/Output [change] :  default (formatted HTML)
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Schedule an event for launch on October 15, 1997 at 09:28
    private static final GregorianCalendar launch =
            new GregorianCalendar(1997, Calendar.OCTOBER, 15, 9, 28, 0);

    // Saturn Orbit Insertion 01-Jul-2004 01:12:08
    private static final GregorianCalendar saturnOrbitInsertion =
            new GregorianCalendar(2004, Calendar.JULY,1,1,12,8);

    // End of trajectory September 15, 2017
    private static final GregorianCalendar endOfTrajectory =
            new GregorianCalendar(2017, Calendar.SEPTEMBER, 15, 0, 0, 0);

    // Dates and orbital parameters
    private double[] orbitDates;
    private double[][] orbitPars;
    private List<GregorianCalendar> eventDateTimes;

    /**
     * Constructor.
     * @param name           name of spacecraft
     * @param centerBodyName name of the center body
     * @param solarSystem    the Solar System
     */
    public Cassini(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    /**
     * Date/time Saturn Orbit Insertion.
     * @return date/time of Saturn Orbit Insertion
     */
    public static GregorianCalendar getSaturnOrbitInsertion() {
        return saturnOrbitInsertion;
    }

    /**
     * Read dates and orbital parameters from file.
     * @param fileName file name
     */
    private void readOrbitParametersFromFile(String fileName) {
        int nrRows = 0;
        int nrCols = 7;
        File file = new File(fileName);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            Locale locale = Locale.US;
            scanner.useLocale(locale);
            while (scanner.hasNext("%")) {
                scanner.nextLine();
            }
            if (scanner.hasNextInt()) {
                nrRows = scanner.nextInt();
            }
            orbitDates = new double[nrRows];
            orbitPars = new double[nrRows][nrCols];
            int row = 0;
            while (row < nrRows && scanner.hasNextDouble()) {
                orbitDates[row] = scanner.nextDouble();
                int col = 0;
                while (col < nrCols && scanner.hasNextDouble()) {
                    orbitPars[row][col++] = scanner.nextDouble();
                }
                row++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found " + fileName);
        }
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {
        readOrbitParametersFromFile("EphemerisFiles/orbitParsCassiniSaturn_OTM_extended.txt");
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        eventDateTimes = new ArrayList<>();
        int index = 0;
        while (index < orbitPars.length) {
            GregorianCalendar startDateTime = JulianDateConverter.convertJulianDateToCalendar(orbitDates[index]);
            GregorianCalendar stopDateTime;
            if (index < orbitPars.length - 1) {
                stopDateTime = JulianDateConverter.convertJulianDateToCalendar(orbitDates[index + 1]);
            }
            else {
                stopDateTime = endOfTrajectory;
            }
            String centerBodyName;
            if (startDateTime.before(saturnOrbitInsertion)) {
                centerBodyName = "Sun";
            }
            else {
                centerBodyName = "Saturn";
            }
            trajectories.add(
                    new SpacecraftTrajectory(startDateTime, stopDateTime, centerBodyName, orbitPars[index]));
            eventDateTimes.add(startDateTime);
            index++;
        }
        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        for (GregorianCalendar dateTime : eventDateTimes) {
            solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), CalendarUtil.createGregorianCalendar(dateTime)));
        }
    }
}
