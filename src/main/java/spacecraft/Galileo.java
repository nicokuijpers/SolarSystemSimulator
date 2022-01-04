/*
 * Copyright (c) 2022 Nico Kuijpers
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

public class Galileo extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/Galileo_(spacecraft)
     *
     * Delivered into Earth orbit by space shuttle Atlantis on October 18, 1989
     * Arrived at Jupiter on December 7, 1995
     * End of mission September 21, 2003
     *
     * Orbital parameters are read from file and are obtained from
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     *
     * Trajectory Earth - Venus - Earth - Gaspra - Earth - Ida - Jupiter:
     * Ephemeris Type  :  ELEMENTS
     * Target body name: Galileo (spacecraft) (-77)      {source: galileo}
     * Center body name: Jupiter (599)                   {source: galileo}
     * Start time      : A.D. 1989-Oct-19 02:00:00.0000 TDB
     * Stop  time      : A.D. 1995-Oct-15 00:00:00.0000 TDB
     *
     * Arrival at Jupiter and orbiting Jupiter:
     * Ephemeris Type  :  ELEMENTS
     * Target body name: Galileo (spacecraft) (-77)      {source: galileo}
     * Center body name: Sun (10)                        {source: galileo}
     * Start time      : A.D. 1995-Oct-15 01:00:00.0000 TDB
     * Stop  time      : A.D. 2003-Sep-30 12:00:00.0000 TDB
     *
     * Trajectory Correction Maneuvers (TCMs) are obtained from
     *
     * GALILEO TRAJECTORY DESIGN
     * L.A. D'Amario, L.E. Bright, A.A. Wolf,
     * Jet Propulsion Laboratory, California Institute of Technology, Pasadena, California, USA
     * See pages 27-36
     * https://articles.adsabs.harvard.edu//full/1992SSRv...60...23D/0000033.000.html
     *
     * FINAL GALILEO PROPULSION SYSTEM IN-FLIGHT CHARACTERIZATION
     * T.J. Barber, Jet Propulsion Laboratory California Institute of Technology Pasadena, California, USA
     * F.A. Krug and K.P. Renner, Deutsche Forschungsanstalt fur Luft- und Raumfahrt e.v. (DLR) Oberpfaffenhofen, Germany
     * See Table 3 and Table 4, page 12
     * https://trs.jpl.nasa.gov/bitstream/handle/2014/22121/97-0574.pdf?sequence=1
     *
     * TCM-1 Nov 9-11, 1989 Remove launch bias and first Venus target
     * TCM-2 Dec 22, 1989 Second and final Venus target
     * TCM-3 was not required
     * Venus encounter Feb 10, 1990, 05:59 UTC (altitude 16 123 km, relative velocity 8.2 km/s
     * TCM-4A Apr 9-12, 1990 First Earth-1 target part 1 (delta V 24.2 m/s)
     * TCM-4B May 11-12, 1990 First Earth-1 target part 2 (delta V 11.0 m/s)
     * TCM-5  Jul 17, 1990 Second Earth-1 target
     * Galileo passed through aphelion at distance 1.28 AU from the Sun
     * TCM-6 Oct 9, 1990 Third Earth-1 target
     * TCM-7 Nov 13, 1990 Final Earth-1 target
     * TCM-8 Nov 28, 1990 TCM-7 cleanup
     * First Earth encounter Dec 8, 1990, 20:35 UTC (altitude 960 km, relative velocity 13.7 km/s)
     * TCM-9A Dec 19, 1990 Post Earth-1 cleanup (delta V 5.3 m/s)
     * TCM-9B Mar 20, 1991 Gaspra target part 1 (delta V 2.5 m/s)
     * TCM-10 Jul 2, 1991 Gaspra target part 2 (delta V 3.8 m/s)
     * TCM-11 Oct 9, 1991 Gaspra target cleanup
     * TCM-12 Oct 24, 1991 Gaspra target cleanup
     * Gaspra encounter Oct 29, 1991
     * TCM-14 Aug 14, 1992 First Earth-2 target
     * TCM-15 Oct 9, 1992 Second Earth-2 target
     * TCM-16 Nov 13, 1992 Final Earth-2 target
     * TCM-17 Nov 28, 1992 Post Earth-2 cleanup
     * Second Earth encounter Dec 8, 1992, 15:35 UTC (passes the Moon over the North Pole at 111000 km)
     * TCM-19 Mar 9, 1993 Final Ida target
     * TCM-20 Aug 13, 1993 Ida target cleanup
     * Ida encounter Aug 28, 1993
     * TCM-22 Oct 4, 1993 Final probe entry target
     * TCM-22A Feb 15, 1994 Probe target cleanup
     * Impact of comet parts Shoemaker-Levy 9 July 16-22, 1994
     * TCM-23 Apr 12, 1995 Probe target cleanup
     * Probe release July 10, 1995
     * Orbit Deflection Maneuver (ODM) July 27, 1995
     * TCM-26 Aug 29, 1995 First (and final) ODM cleanup
     * Io flyby Dec 7, 1995 (closest approach 1000 km)
     * Probe entry Dec 7, 1995
     * Jupiter Orbit Insertion (JOI) Dec 7, 1995
     * PJR (Peri-Jove Raise maneuver Mar 14, 1996
     * OTM-4 May 3, 1996 First G1 target cleanup
     * OTM-5 Jun 12, 1996 Second G1 target cleanup
     * OTM-6 Jun 24, 1996 Final G1 target cleanup
     * OTM-7 Jun 30, 1996 Post-G1 cleanup
     * OTM-8 Aug 5, 1996 G1 to G2 apoapsis maneuver
     * OTM-9 Aug 27, 1996 First and final G2 target cleanup
     * OTM-11 Sep 9, 1996 Post-G2 cleanup
     * OTM-12 Oct 8, 1996 G2 to C3 apoapsis maneuver
     * OTM-14 Nov 10, 1996 Post C3 cleanup
     * OTM-15 Nov 26, 1996 C3 to E4 apoapsis maneuver
     * OTM-16 Dec 15, 1996 E4 target cleanup
     * OTM-17 Dec 23, 1996 Post E4 cleanup
     * OTM-19 Feb 6, 1997 E4 to E5 apoapsis maneuver
     * OTM-21 Feb 23, 1997 Post E6 cleanup
     * OTM-22 Mar 13, 1997 E6 to G7 apoapsis maneuver
     *
     * On the basis of known TCMs and encounters, the following corrections are simulated:
     * 19-Oct-1989 02:00:00 after launch
     * 20-Oct-1989 00:00:00 correction after launch
     * 12-Nov-1989 00:00:00 after TCM-1
     * 23-Dec-1989 00:00:00 after TCM-2
     * 10-Feb-1990 00:00:00 before Venus encounter
     * 12-Feb-1990 00:00:00 after Venus encounter
     * 13-Apr-1990 00:00:00 after TCM-4A
     * 13-May-1990 00:00:00 after TCM-4B
     * 18-Jul-1990 00:00:00 after TCM-5
     * 10-Oct-1990 00:00:00 after TCM-6
     * 14-Nov-1990 00:00:00 after TCM-7
     * 29-Nov-1990 00:00:00 after TCM-8
     * 08-Dec-1990 00:00:00 before first Earth encounter
     * 10-Dec-1990 00:00:00 after first Earth encounter
     * 20-Dec-1990 00:00:00 after TCM-9A
     * 21-Mar-1991 00:00:00 after TCM-9B
     * 03-Jul-1991 00:00:00 after TCM-10
     * 10-Oct-1991 00:00:00 after TCM-11
     * 25-Oct-1991 00:00:00 after TCM-12
     * 15-Aug-1992 00:00:00 after TCM-14
     * 10-Oct-1992 00:00:00 after TCM-15
     * 14-Nov-1992 00:00:00 after TCM-16
     * 29-Nov-1992 00:00:00 after TCM-17
     * 08-Dec-1992 00:00:00 before second Earth encounter
     * 10-Dec-1992 00:00:00 after second Earth encounter
     * 05-Oct-1993 00:00:00 after TCM-22
     * 16-Feb-1994 00:00:00 after TCM-22A
     * 13-Apr-1995 00:00:00 after TCM-23
     * 10-Jul-1995 00:00:00 probe release
     * 28-Jul-1995 00:00:00 after Orbit Deflection Maneuver
     * 30-Aug-1995 00:00:00 after TCM-26
     * 18-Nov-1995 00:00:00 three weeks before Jupiter Orbit Insertion
     * 25-Nov-1995 00:00:00 two weeks before Jupiter Orbit Insertion
     * 01-Dec-1995 00:00:00 one week before Jupiter Orbit Insertion, transfer spacecraft to Jupiter System
     * 07-Dec-1995 00:00:00 Before IO flyby
     * 09-Dec-1995 00:00:00 after Jupiter Orbit Insertion
     * 15-Mar-1996 00:00:00 after PJR (Peri-Jove Raise Maneuver) on Mar 14, 1996
     * 04-May-1996 00:00:00 after OTM-4
     * 13-Jun-1996 00:00:00 after OTM-5
     * 25-Jun-1996 00:00:00 after OTM-6
     * 01-Aug-1996 00:00:00 after OTM-7
     * 06-Aug-1996 00:00:00 after OTM-8
     * 28-Aug-1996 00:00:00 after OTM-9
     * 10-Sep-1996 00:00:00 after OTM-11
     * 09-Oct-1996 00:00:00 after OTM-12
     * 11-Nov-1996 00:00:00 after OTM-14
     * 27-Nov-1996 00:00:00 after OTM-15
     * 16-Dec-1996 00:00:00 after OTM-16
     * 24-Dec-1996 00:00:00 after OTM-17
     * 07-Feb-1997 00:00:00 after OTM-19
     * 24-Feb-1997 00:00:00 after OTM-21
     * 14-Mar-1997 00:00:00 after OTM-22
     *
     * https://en.wikipedia.org/wiki/Timeline_of_Galileo_(spacecraft)
     * In addition, three corrections are simulated for each encounter:
     *   1 week before the encounter
     *   at 0.00 on the day of encounter
     *   at 0.00 on the next day
     *
     * Orbit: C: Callisto, E: Europa, G: Ganymede, I: Io, J: Jupiter, closest approach
     * 27-Jun-1996 G1   835 km gravity-assist reduced Galileo's orbital period from 210 to 72 days
     * 06-Sep-1996 G2   260 km gravity-assist put Galileo into coplanar orbit with other Galilean satellites
     * 04-Nov-1996 C3  1136 km
     * 19-Dec-1996 E4   692 km
     * 20-Jan-1997 J5  No close encounter to a Jovian moon was scheduled because Earth and Jupiter were in Sun conjunction
     * 20-Feb-1997 E6   586 km
     * 05-Apr-1997 G7  3102 km
     * 07-May-1997 G8  1603 km
     * 25-Jun-1997 C9   418 km
     * 17-Sep-1997 C10  539 km
     * 06-Nov-1997 E11 1266 km
     * 16-Dec-1997 E12  196 km
     * 10-Feb-1998 E13 3562 km
     * 28-Mar-1998 E14 1645 km
     * 31-May-1998 E15 2515 km
     * 21-Jul-1998 E16 1830 km
     * 26-Sep-1998 E17 3582 km
     * 22-Nov-1998 E18 2273 km
     * 01-Feb-1999 E19 1439 km
     * 05-May-1999 C20 1315 km
     * 30-Jun-1999 C21 1047 km
     * 14-Aug-1999 C22 2296 km
     * 16-Sep-1999 C23 1057 km
     * 11-Oct-1999 I24  611 km
     * 25-Nov-1999 I25  300 km
     * 03-Jan-2000 E26  351 km
     * 22-Feb-2000 I27  198 km
     * 20-May-2000 G28 1000 km
     * 15 June 2000 to 15 November 2000 Magnetosphere–solar wind interaction measurements
     * 28-Dec-2000 G29 2321 km
     * 25-May-2001 C30  138 km
     * 05-Aug-2001 I31  200 km
     * 16-Oct-2001 I32  181 km
     * 17-Jan-2002 I33  102 km
     * 04-Nov-2002 Amalthea 34 160 km
     * 21-Sep-2003 Jupiter 35 impact
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Schedule an event for launch on October 18, 1989 at 02:00
    private static final GregorianCalendar launch =
            new GregorianCalendar(1989, Calendar.OCTOBER, 18, 2, 0);

    // Start with Jupiter as center body from October 15, 1995 at 01.00
    private static final GregorianCalendar startJupiterCenterBody =
            new GregorianCalendar(1995, Calendar.OCTOBER,15,1,0);

    // One week before Jupiter Orbit Insertion at December 8, 1995
    private static final GregorianCalendar weekBeforeJupiterOrbitInsertion =
            new GregorianCalendar(1995, Calendar.DECEMBER,1,0,0);

    // End of trajectory September 30, 2003
    private static final GregorianCalendar endOfTrajectory =
            new GregorianCalendar(2003, Calendar.SEPTEMBER, 30, 0, 0);

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
    public Galileo(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    /**
     * Date/time one week before Jupiter Orbit Insertion.
     * @return date/time one week before Jupiter Orbit Insertion
     */
    public static GregorianCalendar getWeekBeforeJupiterOrbitInsertion() {
        return weekBeforeJupiterOrbitInsertion;
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
        readOrbitParametersFromFile("EphemerisFiles/orbitParsGalileoJupiter.txt");
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
            if (startDateTime.before(startJupiterCenterBody)) {
                centerBodyName = "Sun";
            }
            else {
                centerBodyName = "Jupiter";
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