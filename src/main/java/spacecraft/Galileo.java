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

import ephemeris.*;
import solarsystem.SolarSystem;
import util.Vector3D;

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
     * MANEUVER DESIGN FOR GALILEO JUPITER APPROACH AND ORBITAL OPERATIONS
     * Michael G. Wilson, Christopher L. Potts, Robert A. Mase, C. Allen Halsell and Dennis V. Byrnes
     * Jet Propulsion Laboratory, California Institute of Technology, Navigation and Flight Mechanics Section
     * https://trs.jpl.nasa.gov/bitstream/handle/2014/22003/97-0443.pdf?sequence=1&isAllowed=y
     *
     * NAVIGATING GALILEO IN THE JOVIANSYSTEM P.G. Antreasian. T.P. McElrath, R.J. Haw, G.D. Lewis
     * Jet Propulsion Laboratory,California Institute of Technology, Pasadena,California 9 I 109
     * https://trs.jpl.nasa.gov/bitstream/handle/2014/16877/99-0286.pdf?sequence=1
     * See Table 1 Satellite Encounter Dates, Flyby Altitudes. and Latitudes
     *
     * TCM-1 Nov 9-11, 1989 Remove launch bias and first Venus target
     * TCM-2 Dec 22, 1989 Second and final Venus target
     * TCM-3 was not required
     * Venus encounter Feb 10, 1990, 05:59 UTC (altitude 16 123 km, relative velocity 8.2 km/s
     * On Feb 25, 1990, Galileo passed through perihelion at distance 0.70 AU from the Sun
     * TCM-4A Apr 9-12, 1990 First Earth-1 target part 1 (delta V 24.2 m/s)
     * TCM-4B May 11-12, 1990 First Earth-1 target part 2 (delta V 11.0 m/s)
     * TCM-5  Jul 17, 1990 Second Earth-1 target
     * On Aug 23, 1990, Galileo passed through aphelion at distance 1.28 AU from the Sun
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
     * TCM-14 Aug 4, 1992 First Earth-2 target
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
     * Probe release July 13, 1995, 05:30:00 (LET OP WAS 10 JULI)
     * TCM-25A 00N Calibration, Jul 24, 1995, 07:00:00 (LET OP)
     * Orbit Deflection Maneuver (ODM) July 27, 1995, 07:00:00
     * TCM-26 Aug 29, 1995 01:00:20 First (and final) ODM cleanup (LET OP)
     * TCM-27 Nov 17, 1995
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
     * 10-Nov-1989 00:00:00 during TCM-1
     * 11-Nov-1989 10:00:00 during TCM-1
     * 12-Nov-1989 00:00:00 during TCM-1
     * 13-Nov-1989 00:00:00 after TCM-1
     * 05-Dec-1989 00:00:00 extra correction to avoid deviation > 100 km
     * 23-Dec-1989 00:00:00 after TCM-2
     * 11-Jan-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 28-Jan-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 10-Feb-1990 00:00:00 before Venus encounter
     * 10-Feb-1990 12:00:00 after Venus encounter
     * 11-Feb-1990 00:00:00 after Venus encounter
     * 12-Feb-1990 00:00:00 after Venus encounter
     * 24-Feb-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 04-Mar-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 16-Mar-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 01-Apr-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 10-Apr-1990 00:00:00 during TCM-4A
     * 11-Apr-1990 00:00:00 during TCM-4A
     * 12-Apr-1990 00:00:00 during TCM-4A
     * 13-Apr-1990 00:00:00 after TCM-4A
     * 25-Apr-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 12-May-1990 00:00:00 during TCM-4B
     * 13-May-1990 00:00:00 after TCM-4B
     * 07-Jun-1990 01:00:00 extra correction after deviation of more than 200 km
     * 07-Jul-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 08-Jul-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 18-Jul-1990 00:00:00 after TCM-5
     * 20-Aug-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 05-Sep-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 01-Oct-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 10-Oct-1990 00:00:00 after TCM-6
     * 11-Nov-1990 00:00:00 extra correction to avoid deviation > 100 km
     * 14-Nov-1990 00:00:00 after TCM-7
     * 29-Nov-1990 00:00:00 after TCM-8
     * 08-Dec-1990 00:00:00 before first Earth encounter
     * 08-Dec-1990 22:00:00 after first Earth encounter
     * 10-Dec-1990 00:00:00 after first Earth encounter
     * 20-Dec-1990 00:00:00 after TCM-9A
     * 17-Jan-1991 00:00:00 extra correction to avoid deviation > 100 km
     * 07-Feb-1991 00:00:00 extra correction to avoid deviation > 100 km
     * 21-Mar-1991 00:00:00 after TCM-9B
     * 01-May-1991 00:00:00 extra correction
     * 01-Jun-1991 00:00:00 extra correction
     * 03-Jul-1991 00:00:00 after TCM-10
     * 02-Aug-1991 00:00:00 extra correction
     * 01-Sep-1991 00:00:00 extra correction
     * 10-Oct-1991 00:00:00 after TCM-11
     * 25-Oct-1991 00:00:00 after TCM-12
     * 01-Jan-1992 00:10:00 extra correction
     * 19-Apr-1992 00:10:00 extra correction
     * 10-Jul-1992 00:10:00 extra correction
     * 23-Jul-1992 00:00:00 extra correction
     * 05-Aug-1992 00:00:00 after TCM-14
     * 06-Aug-1992 00:00:00 after TCM-14
     * 07-Aug-1992 00:00:00 after TCM-14
     * 08-Aug-1992 00:00:00 after TCM-14
     * 09-Aug-1992 00:00:00 after TCM-14
     * 01-Sep-1992 00:00:00 extra correction
     * 10-Oct-1992 00:00:00 after TCM-15
     * 20-Oct-1992 00:10:00 extra correction
     * 14-Nov-1992 00:10:00 after TCM-16
     * 29-Nov-1992 00:00:00 after TCM-17
     * 08-Dec-1992 00:00:00 before second Earth encounter
     * 08-Dec-1992 20:00:00 after second Earth encounter
     * 10-Dec-1992 00:00:00 after second Earth encounter
     * 01-Feb-1993 00:00:00 extra correction
     * 01-Mar-1993 00:00:00 extra correction
     * 11-Apr-1993 00:00:00 extra correction
     * 04-Jun-1993 00:10:00 extra correction
     * 01-Jul-1993 00:00:00 extra correction
     * 01-Aug-1993 00:00:00 extra correction
     * 01-Sep-1993 00:00:00 extra correction
     * 01-Oct-1993 00:10:00 extra correction
     * 05-Oct-1993 00:00:00 after TCM-22
     * 05-Oct-1993 12:00:00 after TCM-22
     * 06-Oct-1993 00:00:00 after TCM-22
     * 07-Oct-1993 00:00:00 after TCM-22
     * 08-Oct-1993 00:00:00 after TCM-22
     * 09-Oct-1993 00:00:00 after TCM-22
     * 23-Oct-1993 09:00:00 extra correction
     * 01-Dec-1993 00:10:00 extra correction
     * 01-Jan-1994 00:10:00 extra correction
     * 16-Feb-1994 00:00:00 after TCM-22A
     * 30-Mar-1994 00:10:00 extra correction
     * 01-May-1994 00:00:00 extra correction
     * 01-Jun-1994 00:00:00 extra correction
     * 01-Jul-1994 00:00:00 extra correction
     * 01-Aug-1994 00:00:00 extra correction
     * 01-Sep-1994 00:00:00 extra correction
     * 01-Oct-1994 00:00:00 extra correction
     * 01-Nov-1994 00:00:00 extra correction
     * 01-Dec-1994 00:00:00 extra correction
     * 01-Jan-1995 00:00:00 extra correction
     * 01-Feb-1995 00:00:00 extra correction
     * 01-Mar-1995 00:00:00 extra correction
     * 10-Mar-1995 00:00:00 extra correction
     * 13-Apr-1995 00:00:00 after TCM-23
     * 01-Jun-1995 00:00:00 extra correction
     * 10-Jul-1995 00:00:00 probe release
     * 27-Jul-1995 08:00:00 Orbit Deflection Maneuver
     * 28-Jul-1995 00:00:00 after Orbit Deflection Maneuver
     * 30-Aug-1995 00:00:00 after TCM-26
     * 01-Oct-1995 00:00:00 extra correction
     * 18-Nov-1995 00:00:00 three weeks before Jupiter Orbit Insertion
     * 21-Nov-1995 01:00:00 two and half weeks before Jupiter Orbit Insertion
     * 25-Nov-1995 00:00:00 two weeks before Jupiter Orbit Insertion
     * 28-Nov-1995 00:00:00 one and half week before Jupiter Orbit Insertion
     * 01-Dec-1995 00:00:00 one week before Jupiter Orbit Insertion, transfer spacecraft to Jupiter System
     * 07-Dec-1995 00:00:00 before IO flyby
     * 08-Dec-1995 00:00:00 after IO flyby
     * 08-Dec-1995 01:00:00 after IO flyby
     * 08-Dec-1995 02:00:00 after IO flyby
     * 08-Dec-1995 04:00:00 after IO flyby
     * 08-Dec-1995 06:00:00 after IO flyby
     * 09-Dec-1995 00:00:00 after Jupiter Orbit Insertion
     * 10-Dec-1995 00:00:00 after Jupiter Orbit Insertion
     * 22-Jan-1995 00:00:00 extra correction
     * 14-Mar-1996 20:00:00 Peri-Jove Raise Maneuver (around 19.30)
     * 15-Mar-1996 00:00:00 after PJR (Peri-Jove Raise Maneuver) on Mar 14, 1996
     * 04-May-1996 00:00:00 after OTM-4
     * 13-Jun-1996 00:00:00 after OTM-5
     * 25-Jun-1996 00:00:00 after OTM-6
     * 27-Jun-1996 07:00:00 extra correction after gravity assist G1
     * 01-Aug-1996 00:00:00 after OTM-7
     * 06-Aug-1996 00:00:00 after OTM-8
     * 07-Aug-1996 00:00:00 after OTM-8
     * 28-Aug-1996 00:00:00 after OTM-9
     * 06-Sep-1996 19:30:00 extra correction after gravity assist G2
     * 10-Sep-1996 00:00:00 after OTM-11
     * 11-Sep-1996 00:00:00 after OTM-11
     * 09-Oct-1996 00:00:00 after OTM-12
     * 10-Oct-1996 00:00:00 after OTM-12
     * 04-Nov-1996 14:00:00 extra correction after C3
     * 11-Nov-1996 00:00:00 after OTM-14
     * 27-Nov-1996 00:00:00 after OTM-15
     * 16-Dec-1996 00:00:00 after OTM-16
     * 19-Dec-1996 07:30:00 extra correction after E4
     * 24-Dec-1996 00:00:00 after OTM-17
     * 07-Feb-1997 00:00:00 after OTM-19
     * 20-Feb-1997 17:30:00 extra correction after E6
     * 24-Feb-1997 00:00:00 after OTM-21
     * 14-Mar-1997 00:00:00 after OTM-22
     * 15-Mar-1997 00:00:00 after OTM-22
     *
     * https://en.wikipedia.org/wiki/Timeline_of_Galileo_(spacecraft)
     * In addition, four corrections are simulated for each encounter:
     *   1 week before the encounter
     *   at 0.00 on the day of encounter
     *   at 0.00 on the next day
     *   at 0.00 the day thereafter
     *   Extra corrections were added based on the results of SpacecraftGalileoEphemerisExperiment with:
     *      Max deviation position  = 100 km
     *      Max deviation velocity  = 100 m/s
     *      Max deviation direction = 1 degree
     *   These corrections occur mainly shortly after flyby of one of the Galilean Moons.
     *   Extra corrections were added when intervals between corrections where larger than one month
     *
     *   Extra corrections approximately 3 hours before encounter where added based on the results of
     *   SpacecraftGalileoExperiment with timestep 1 minute and Newton Mechanics.
     *
     * Orbit: C: Callisto, E: Europa, G: Ganymede, I: Io, J: Jupiter, closest approach
     * 27-Jun-1996 G1   835 km gravity-assist reduced Galileo's orbital period from 210 to 72 days
     * 06-Sep-1996 G2   260 km gravity-assist put Galileo into coplanar orbit with other Galilean satellites
     * 04-Nov-1996 C3  1136 km
     * 19-Dec-1996 E4   692 km
     * 20-Jan-1997 J5  No close encounter to a Jovian moon was scheduled because Earth and Jupiter were in Sun conjunction
     * 20-Feb-1997 E6   586 km
     * 05-Apr-1997 G7  3102 km (extra correction added on 04-Apr-1997 00:00 and 05-Apr-1997 07:30)
     * 07-May-1997 G8  1603 km (extra correction added on 07-May-1997 16:30 and 12-May-1997 00:00 and )
     * 25-Jun-1997 C9   418 km (extra correction added on 25-Jun-1997 14:10)
     * 17-Sep-1997 C10  539 km (extra correction added on 16-Sep-1997 12.00 and 17-Sep-1997 00:40)
     * 06-Nov-1997 E11 2042 km (extra correction added on 06-Nov-1997 21:00)
     * 16-Dec-1997 E12  196 km (extra correction added on 16-Dec-1997 12:30)
     * 10-Feb-1998 E13 3562 km
     * 28-Mar-1998 E14 1645 km (extra correction added on 29-Mar-1998 13:50)
     * 31-May-1998 E15 2515 km (extra correction added on 31-May-1998 21:30)
     * 21-Jul-1998 E16 1830 km (extra correction added on 21-Jul-1998 05:20)
     * 26-Sep-1998 E17 3582 km
     * 22-Nov-1998 E18 2273 km (extra correction added on 22-Nov-1998 12:00)
     * 01-Feb-1999 E19 1439 km (extra correction added on 01-Feb-1999 02:50)
     * 05-May-1999 C20 1315 km (extra correction added on 05-May-1999 14:30)
     * 30-Jun-1999 C21 1047 km (extra correction added on 30-Jun-1999 08:20)
     * 14-Aug-1999 C22 2296 km (extra correction added on 14-Aug-1999 09:00)
     * 16-Sep-1999 C23 1057 km (extra correction added on 16-Sep-1999 18:00)
     * 11-Oct-1999 I24  611 km (extra correction added on 11-Oct-1999 05:00)
     * 25-Nov-1999 I25  300 km (extra correction added on 26-Nov-1999 04:30)
     * 03-Jan-2000 E26  351 km (extra correction added on 03-Jan-2000 18:30)
     * 22-Feb-2000 I27  198 km (extra correction added on 22-Feb-2000 14:20)
     * 20-May-2000 G28 1000 km (extra correction added on 20-May-2000 10:40)
     * 15 June 2000 to 15 November 2000 Magnetosphere–solar wind interaction measurements
     * Extra correction added on 01-Nov-2000 12:30
     * 28-Dec-2000 G29 2321 km (extra correction added on 28-Dec-2000 09:00)
     * 25-May-2001 C30  138 km (extra correction added on 25-May-2001 12:00)
     * 05-Aug-2001 I31  200 km (extra correction added on 06-Aug-2001 05:30)
     * 16-Oct-2001 I32  181 km (extra correction added on 16-Oct-2001 02:00)
     * 17-Jan-2002 I33  102 km (extra correction added on 17-Jan-2002 14:40)
     * Extra correction added on 11-Sep-2002 12:30
     * 04-Nov-2002 Amalthea 34 160 km
     * Extra correction added on 28-Jan-2003 01:30
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
    public void updateStatus(GregorianCalendar dateTime) {
        // Obtain position and velocity from Ephemeris of Galileo
        IEphemeris ephemerisGalileo = EphemerisGalileoBSP.getInstance();
        if (!(dateTime.before(ephemerisGalileo.getFirstValidDate()) || dateTime.after(ephemerisGalileo.getLastValidDate()))) {
            Vector3D[] positionVelocityGalileo = ephemerisGalileo.getBodyPositionVelocity("Galileo", dateTime);
            Vector3D position = positionVelocityGalileo[0];
            Vector3D velocity = positionVelocityGalileo[1];
            setPosition(position);
            setVelocity(velocity);
        }
    }

    @Override
    protected List<SpacecraftTrajectory> defineTrajectories() {
        readOrbitParametersFromFile("EphemerisFiles/orbitParsGalileoJupiter.txt");
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        eventDateTimes = new ArrayList<>();
        double muSun = SolarSystemParameters.getInstance().getMu("Sun");
        double muJupiter = SolarSystemParameters.getInstance().getMu("Jupiter");
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
                // Use this code to obtain orbital parameters from Ephemeris of Interplanetary cruise
                Vector3D[] positionVelocityGalileo = EphemerisGalileoBSP.getInstance().getBodyPositionVelocity("Galileo", startDateTime);
                Vector3D position = positionVelocityGalileo[0];
                Vector3D velocity = positionVelocityGalileo[1];
                double[] orbitParsGalileo =
                        EphemerisUtil.computeOrbitalParametersFromPositionVelocity(muSun, position, velocity, startDateTime);
                trajectories.add(
                        new SpacecraftTrajectory(startDateTime, stopDateTime, centerBodyName, orbitParsGalileo));
            }
            else {
                centerBodyName = "Jupiter";
                // Use this code to obtain orbital parameters from Ephemeris of Primary mission, GEM, and GMM
                Vector3D[] positionVelocityGalileo = EphemerisGalileoJupiterBSP.getInstance().getBodyPositionVelocity("Galileo", startDateTime);
                Vector3D position = positionVelocityGalileo[0];
                Vector3D velocity = positionVelocityGalileo[1];
                double[] orbitParsGalileo =
                        EphemerisUtil.computeOrbitalParametersFromPositionVelocity(muJupiter, position, velocity, startDateTime);
                trajectories.add(
                        new SpacecraftTrajectory(startDateTime, stopDateTime, centerBodyName, orbitParsGalileo));
            }
            /*
            // Use this line to use orbital parameters read from file
            trajectories.add(
                    new SpacecraftTrajectory(startDateTime, stopDateTime, centerBodyName, orbitPars[index]));
            */
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