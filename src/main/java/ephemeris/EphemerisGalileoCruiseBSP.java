/*
 * Copyright (c) 2022 Nico Kuijpers and Marco Brassé
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

import util.Vector3D;

import java.util.*;

/**
 * Ephemeris for the interplanetary cruise of the Galileo mission.
 * This ephemeris is valid from October 19, 1989 through July 1, 1995.
 * @author Nico Kuijpers
 */

public class EphemerisGalileoCruiseBSP implements IEphemeris {

    // Files name of BSP file
    private final String BSPfilename = "EphemerisFilesBSP/s970311a.bsp";

    // Target codes for BSP file
    private Map<String,Integer> targets;

    // Bodies for which ephemeris can be computed or approximated
    private List<String> bodies;

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // Singleton instance
    private static IEphemeris instance = null;

    // Read ephemeris from BSP file
    private SPK spk;

    // Compute position and velocity in case no record is found
    private Vector3D positionStored = new Vector3D();
    private Vector3D velocityStored = new Vector3D();
    private GregorianCalendar dateTimeStored = null;

    // Use ephemeris of Solar System for Jupiter with respect to the Sun
    private final IEphemeris ephemerisSolarSystem;

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisGalileoCruiseBSP() {

        /*
         * https://naif.jpl.nasa.gov/pub/naif/GLL/kernels/spk/aareadme.txt
         * Galileo Orbiter:
         *
         * Phase                             Coverage                    SPK file
         * ================================  ==========================  ============
         * Interplanetary cruise             1989-OCT-19 to 1995-JUL-02  s970311a.bsp
         * Primary tour                      1995-JUL-01 to 1998-JAN-01  s980326a.bsp
         * GEM (Galileo Europa Mission)      1997-DEC-01 to 2000-FEB-01  s000131a.bsp
         * GMM (Galileo Millenium Mission)   2000-FEB-01 to 2003-SEP-22  s030916a.bsp
         *
         * Galileo Probe:
         *
         * Phase                             Coverage                    SPK file
         * ================================  ==========================  ============
         * Probe separation through descent  1995-JUL-13 to 1995-DEC-07  s960730a.bsp
         */

        /*
        python -m jplephem daf s970311a.bsp
         1 dpfil-longarc-19oct89-15feb90.nio -321964226.73959994 -321357871.7154367 -77 399 11 1 5249 7481
         2 dpfil-longarc-19oct89-15feb90.nio -321357871.7154367 -312681542.81514347 -77 10 11 1 7482 14106
         3 DE-0125LE-0125 -321964226.739609 -291988742.8176524 2 0 11 2 14107 14566
         4 DE-0125LE-0125 -321964226.739609 -291988742.8176524 3 0 11 2 14567 15651
         5 DE-0125LE-0125 -321964226.739609 -291988742.8176524 5 0 11 2 15652 16003
         6 DE-0125LE-0125 -321964226.739609 -291988742.8176524 10 0 11 2 16004 16571
         7 DE-0125LE-0125 -321964226.739609 -291988742.8176524 301 3 11 2 16572 19919
         8 DE-0125LE-0125 -321964226.739609 -291988742.8176524 399 3 11 2 19920 23267
         9 DE-0125LE-0125 -291988742.8176524 -260452741.81765184 3 0 11 2 23268 24399
        10 DE-0125LE-0125 -291988742.8176524 -260452741.81765184 5 0 11 2 24400 24780
        11 DE-0125LE-0125 -291988742.8176524 -260452741.81765184 10 0 11 2 24781 25395
        12 DE-0125LE-0125 -291988742.8176524 -260452741.81765184 301 3 11 2 25396 28895
        13 DE-0125LE-0125 -291988742.8176524 -260452741.81765184 399 3 11 2 28896 32395
        14 dpfil-venus-od14a.nio -312681542.81514347 -312496734.7348492 -77 10 11 1 32396 32468
        15 dpfil-venus-od14a.nio -312496734.7348492 -312379142.81505847 -77 2 11 1 32469 33045
        16 dpfil-venus-od33.nio -312379142.81505847 -311817542.81490993 -77 2 11 1 33046 36214
        17 dpfil-900521-od22-post4b.nio -311817542.81491035 -311698974.47912645 -77 2 11 1 36215 36359
        18 dpfil-900521-od22-post4b.nio -311698974.47912645 -302011142.8152427 -77 10 11 1 36360 41328
        19 dpfil-900927-od27.nio -302011142.8152427 -295531142.8171837 -77 10 11 1 41329 44785
        20 dpfil-901102-od29-all.nio -295531142.8171837 -294235142.81743824 -77 10 11 1 44786 45434
        21 dpfil-901124-od34-tcm8fd.nio -294235142.816 -288187142.81728184 -77 10 11 1 45435 48603
        22 dpfil-901208-od37a-e1.nio -288187142.81728184 -286315898.4529269 -77 10 11 1 48604 50476
        23 dpfil-901208-od37a-e1.nio -286315898.4529269 -285762917.6804335 -77 399 11 1 50477 54437
        24 dpfil-901208-od37a-e1.nio -285762917.6804335 -285163142.81644845 -77 10 11 1 54438 55446
        25 dpfil-910109-od39-nogas.nio -285163142.81644845 -277732741.81444085 -77 10 11 1 55447 65024
        26 dpfil-910624-tcm10-dsn-od44.nio -277732741.81444085 -269870341.81541556 -77 10 11 1 65281 70609
        27 dpfil-911001-tcm11-od47.nio -269870341.81541556 -264686341.81700984 -77 10 11 1 70610 74642
        28 dpfil-920401-od52-gaspra.nio -264686341.81700984 -258897541.81761292 -77 10 11 1 74643 78243
        29 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 3 0 11 2 78244 78435
        30 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 5 0 11 2 78436 78497
        31 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 9511010 0 11 2 78498 78629
        32 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 10 0 11 2 78630 78727
        33 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 301 3 11 2 78728 79339
        34 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 399 3 11 2 79340 79951
        35 dpfil-920514-od54-gaspra.nio -258897541.81761292 -258211672.55327797 -77 10 11 1 79952 80600
        36 dpfil-920514-od54-gaspra.nio -258211672.55327797 -257692598.70979333 -77 9511010 11 1 80601 81537
        37 dpfil-920514-od54-gaspra.nio -257692598.70979333 -252504000.0 -77 10 11 1 81538 84778
        38 DE-0125LE-0125 -255182341.8169259 -204983939.81597695 3 0 11 2 84779 86568
        39 DE-0125LE-0125 -255182341.8169259 -204983939.81597695 5 0 11 2 86569 87152
        40 DE-0125LE-0125 -255182341.8169259 -204983939.81597695 10 0 11 2 87153 88096
        41 DE-0125LE-0125 -255182341.8169259 -204983939.81597695 301 3 11 2 88097 93686
        42 DE-0125LE-0125 -255182341.8169259 -204983939.81597695 399 3 11 2 93687 99276
        43 dpfil-961024-od53.nio -252503999.99908498 -243086341.814403 -77 10 11 1 99277 110150
        44 dpfil-961030-od56.nio -243086341.814403 -236001540.8161522 -77 10 11 1 110151 115983
        45 dpfil-961105-od58.nio -236001540.81620002 -228916740.81765124 -77 10 11 1 115984 122536
        46 dpfil-920925-od59.nio -228916740.81765124 -227188740.81760076 -77 10 11 1 122537 123545
        47 dpfil-921030-od61-runout.nio -227188740.81760076 -225028740.817273 -77 10 11 1 123546 124770
        48 dpfil-921208-od67.nio -225028740.817273 -223300740.81683433 -77 10 11 1 124771 126643
        49 dpfil-921211-od68-gopex2-e2.nio -223300740.81683433 -222609540.81662688 -77 399 11 1 126644 130100
        50 dpfil-930301-od70-tcm19-dsn.nio -222609540.81662688 -216388740.81473413 -77 10 11 1 130101 135861
        51 dpfil-930505-od71-idacent-1rimlate.nio -216388740.81473413 -210772740.81448665 -77 10 11 1 136193 140081
        52 dpfil-930802-od72-tcm20-dsn.nio -210772740.81448665 -207575940.81517416 -77 10 11 1 140082 141522
        53 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 3 0 11 2 141523 141902
        54 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 5 0 11 2 141903 142022
        55 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 2431010 0 11 2 142023 142282
        56 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 10 0 11 2 142283 142474
        57 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 301 3 11 2 142475 143580
        58 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 399 3 11 2 143581 144686
        59 dpfil-940114-od79-prel-tcm22a-orbiter.ni -207575940.81517416 -204958794.54265428 -77 10 11 1 144687 146055
        60 dpfil-940114-od79-prel-tcm22a-orbiter.ni -204958794.54265428 -204897539.816005 -77 2431010 11 1 146056 146344
        61 dpfil-940520-od82-final-ida-recon.nio -204897539.816005 -197294400.0 -77 2431010 11 1 146345 152105
        62 dpfil-940204-od80-mvf.nio -197294400.0 -195364755.3121488 -77 2431010 11 1 152106 154410
        63 dpfil-940204-od80-mvf.nio -195364755.3121488 -189345539.81607103 -77 10 11 1 154411 156211
        64 DE-0125LE-0125 -195307139.81756976 -181742339.81434864 3 0 11 2 156212 156732
        65 DE-0125LE-0125 -195307139.81756976 -181742339.81434864 5 0 11 2 156733 156910
        66 DE-0125LE-0125 -195307139.81756976 -181742339.81434864 10 0 11 2 156911 157196
        67 DE-0125LE-0125 -195307139.81756976 -181742339.81434864 301 3 11 2 157197 158720
        68 DE-0125LE-0125 -195307139.81756976 -181742339.81434864 399 3 11 2 158721 160244
        69 dpfil-940405-od81.nio -189345539.81607103 -181742339.81434864 -77 10 11 1 160245 162765
        70 dpfil-950606-od87.nio -181742339.81434864 -156513538.81564668 -77 10 20 1 162766 170615
        71 DE-0142LE-0142 -181742339.81434864 -142084738.8159064 3 0 20 2 170616 171808
        72 DE-0142LE-0142 -181742339.81434864 -142084738.8159064 5 0 20 2 171809 172202
        73 DE-0142LE-0142 -181742339.81434864 -142084738.8159064 10 0 20 2 172203 173221
        74 DE-0142LE-0142 -181742339.81434864 -142084738.8159064 301 3 20 2 173222 177981
        75 DE-0142LE-0142 -181742339.81434864 -142084738.8159064 399 3 20 2 177982 182741
        76 dpfil-951023-od93.nio -156513538.81564668 -142084738.8159064 -77 10 20 1 183041 186929
        */

        /*
        python -m jplephem spk s970311a.bsp
        File type NAIF/DAF and format BIG-IEEE with 76 segments:
        2447818.56..2447825.58  Type 1  Earth (399) -> Galileo Orbiter (-77)
        2447825.58..2447926.00  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2447818.56..2448165.50  Type 2  Solar System Barycenter (0) -> Venus Barycenter (2)
        2447818.56..2448165.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2447818.56..2448165.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2447818.56..2448165.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2447818.56..2448165.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2447818.56..2448165.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2448165.50..2448530.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2448165.50..2448530.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2448165.50..2448530.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2448165.50..2448530.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2448165.50..2448530.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2447926.00..2447928.14  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2447928.14..2447929.50  Type 1  Venus Barycenter (2) -> Galileo Orbiter (-77)
        2447929.50..2447936.00  Type 1  Venus Barycenter (2) -> Galileo Orbiter (-77)
        2447936.00..2447937.37  Type 1  Venus Barycenter (2) -> Galileo Orbiter (-77)
        2447937.37..2448049.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448049.50..2448124.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448124.50..2448139.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448139.50..2448209.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448209.50..2448231.16  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448231.16..2448237.56  Type 1  Earth (399) -> Galileo Orbiter (-77)
        2448237.56..2448244.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448244.50..2448330.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448330.50..2448421.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448421.50..2448481.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448481.50..2448548.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448530.50..2448591.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2448530.50..2448591.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2448530.50..2448591.50  Type 2  Solar System Barycenter (0) -> Gaspra (9511010)
        2448530.50..2448591.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2448530.50..2448591.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2448530.50..2448591.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2448548.50..2448556.44  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448556.44..2448562.45  Type 1  Gaspra (9511010) -> Galileo Orbiter (-77)
        2448562.45..2448622.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448591.50..2449172.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2448591.50..2449172.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2448591.50..2449172.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2448591.50..2449172.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2448591.50..2449172.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2448622.50..2448731.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448731.50..2448813.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448813.50..2448895.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448895.50..2448915.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448915.50..2448940.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448940.50..2448960.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2448960.50..2448968.50  Type 1  Earth (399) -> Galileo Orbiter (-77)
        2448968.50..2449040.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449040.50..2449105.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449105.50..2449142.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449172.50..2449284.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2449172.50..2449284.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2449172.50..2449284.50  Type 2  Solar System Barycenter (0) -> Ida (2431010)
        2449172.50..2449284.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2449172.50..2449284.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2449172.50..2449284.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2449142.50..2449172.79  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449172.79..2449173.50  Type 1  Ida (2431010) -> Galileo Orbiter (-77)
        2449173.50..2449261.50  Type 1  Ida (2431010) -> Galileo Orbiter (-77)
        2449261.50..2449283.83  Type 1  Ida (2431010) -> Galileo Orbiter (-77)
        2449283.83..2449353.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449284.50..2449441.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2449284.50..2449441.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2449284.50..2449441.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2449284.50..2449441.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2449284.50..2449441.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2449353.50..2449441.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449441.50..2449733.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        2449441.50..2449900.50  Type 2  Solar System Barycenter (0) -> Earth Barycenter (3)
        2449441.50..2449900.50  Type 2  Solar System Barycenter (0) -> Jupiter Barycenter (5)
        2449441.50..2449900.50  Type 2  Solar System Barycenter (0) -> Sun (10)
        2449441.50..2449900.50  Type 2  Earth Barycenter (3) -> Moon (301)
        2449441.50..2449900.50  Type 2  Earth Barycenter (3) -> Earth (399)
        2449733.50..2449900.50  Type 1  Sun (10) -> Galileo Orbiter (-77)
        */

        // Target codes for BSP file
        targets = new HashMap<>();
        targets.put("Galileo",-77);

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.addAll(targets.keySet());

        // Use ephemeris of Solar System for Jupiter with respect to the Sun
        ephemerisSolarSystem = EphemerisSolarSystem.getInstance();

        // First valid date for ephemeris of Interplanetary cruise is October 19, 1989 at 01:30
        firstValidDate = new GregorianCalendar(1989,9,19, 1, 30);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for ephemeris of Interplanetary cruise is July 2, 1995 at 00:00
        lastValidDate = new GregorianCalendar(1995,6,2, 0, 00);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get instance of EphemerisPlutoMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisGalileoCruiseBSP();
        }
        return instance;
    }

    @Override
    public GregorianCalendar getFirstValidDate() {
        return firstValidDate;
    }

    @Override
    public GregorianCalendar getLastValidDate() {
        return lastValidDate;
    }

    @Override
    public List<String> getBodies() {
        return Collections.unmodifiableList(bodies);
    }

    @Override
    public Vector3D getBodyPosition(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[0];
    }

    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[1];
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {

        // Check whether body name is valid
        if (!bodies.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name +
                    " for ephemeris of interplanetary cruise of Galileo mission");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid ephemeris of interplanetary cruise of Galileo mission");
        }

        // Julian ephemeris date
        double julianDate = JulianDateConverter.convertCalendarToJulianDate(date);

        // Number of seconds past J2000
        double et = EphemerisUtil.computeNrSecondsPastJ2000(julianDate);

        // Target
        int target = targets.get(name);

        Vector3D positionJ2000 = new Vector3D();
        Vector3D velocityJ2000 = new Vector3D();

        // Initialize SPK and open file to read when needed for the first time
        if (spk == null) {
            // Open ephemeris file s970311a.bsp to read ephemeris
            spk = new SPK();
            spk.initWithBSPFile(BSPfilename);
        }

        // Determine position and velocity
        Vector3D positionB1950 = new Vector3D();
        Vector3D velocityB1950 = new Vector3D();
        if ((et >= -321964226.73959994 && et <= -321357871.7154367) ||
                (et >= -286315898.4529269 && et <= -285762917.6804335) ||
                (et >= -223300740.81683433 && et <= -222609540.81662688)) {
            // Earth (399) is observer for Galileo (-77)
            // Earth-Moon barycenter (3) is observer for Earth (399)
            // Launch phase:
            //  1 dpfil-longarc-19oct89-15feb90.nio -321964226.73959994 -321357871.7154367 -77 399 11 1 5249 7481
            //  4 DE-0125LE-0125 -321964226.739609 -291988742.8176524 3 0 11 2 14567 15651
            //  8 DE-0125LE-0125 -321964226.739609 -291988742.8176524 399 3 11 2 19920 23267
            // First encounter with Earth:
            // 23 dpfil-901208-od37a-e1.nio -286315898.4529269 -285762917.6804335 -77 399 11 1 50477 54437
            //  9 DE-0125LE-0125 -291988742.8176524 -260452741.81765184 3 0 11 2 23268 24399
            // 13 DE-0125LE-0125 -291988742.8176524 -260452741.81765184 399 3 11 2 28896 32395
            // Second encounter with Earth:
            // 49 dpfil-921211-od68-gopex2-e2.nio -223300740.81683433 -222609540.81662688 -77 399 11 1 126644 130100
            // 38 DE-0125LE-0125 -255182341.8169259 -204983939.81597695 3 0 11 2 84779 86568
            // 42 DE-0125LE-0125 -255182341.8169259 -204983939.81597695 399 3 11 2 93687 99276
            Vector3D[] bodyPosVel = spk.getPositionVelocity(et, target, 399);
            Vector3D[] earthPosVel = spk.getPositionVelocity(et, 399, 3);
            Vector3D[] earthMoonBaryPosVel = spk.getPositionVelocity(et, 3, 0);
            Vector3D[] sunPosVel = spk.getPositionVelocity(et, 10, 0);
            positionB1950 = earthMoonBaryPosVel[0].minus(sunPosVel[0]).plus(earthPosVel[0]).plus(bodyPosVel[0]);
            velocityB1950 = earthMoonBaryPosVel[1].minus(sunPosVel[1]).plus(earthPosVel[1]).plus(bodyPosVel[1]);
        }
        if ((et >= -312496734.7348492 && et <= -311817542.81490993) ||
               (et >= -311817542.81491035 && et <= -311698974.47912645)) {
            // Venus is observer (2)
            // 15 dpfil-venus-od14a.nio -312496734.7348492 -312379142.81505847 -77 2 11 1 32469 33045
            // 16 dpfil-venus-od33.nio -312379142.81505847 -311817542.81490993 -77 2 11 1 33046 36214
            // 17 dpfil-900521-od22-post4b.nio -311817542.81491035 -311698974.47912645 -77 2 11 1 36215 36359
            Vector3D[] bodyPosVel = spk.getPositionVelocity(et, target, 2);
            Vector3D[] venusPosVel = spk.getPositionVelocity(et, 2, 0);
            Vector3D[] sunPosVel = spk.getPositionVelocity(et, 10, 0);
            positionB1950 = venusPosVel[0].minus(sunPosVel[0]).plus(bodyPosVel[0]);
            velocityB1950 = venusPosVel[1].minus(sunPosVel[1]).plus(bodyPosVel[1]);
        }
        if ((et >= -258211672.55327797 && et <= -257692598.70979333)) {
            // Gaspra is observer (9511010)
            // 31 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 9511010 0 11 2 78498 78629
            // 32 Body 9 = Gaspra, DE-0125LE-0125 -260452741.81765184 -255182341.8169259 10 0 11 2 78630 78727
            // 36 dpfil-920514-od54-gaspra.nio -258211672.55327797 -257692598.70979333 -77 9511010 11 1 80601 81537
            Vector3D[] bodyPosVel = spk.getPositionVelocity(et, target, 9511010);
            Vector3D[] gaspraPosVel = spk.getPositionVelocity(et, 9511010, 0);
            Vector3D[] sunPosVel = spk.getPositionVelocity(et, 10, 0);
            positionB1950 = gaspraPosVel[0].minus(sunPosVel[0]).plus(bodyPosVel[0]);
            velocityB1950 = gaspraPosVel[1].minus(sunPosVel[1]).plus(bodyPosVel[1]);
        }
        if ((et >= -204958794.54265428 && et <= -195364755.3121488)) {
            // Ida is observer (2431010)
            // 55 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 2431010 0 11 2 142023 142282
            // 56 Body 9 = Ida, DE-0125 -204983939.81597695 -195307139.81756976 10 0 11 2 142283 142474
            // 60 dpfil-940114-od79-prel-tcm22a-orbiter.ni -204958794.54265428 -204897539.816005 -77 2431010 11 1 146056 146344
            // 61 dpfil-940520-od82-final-ida-recon.nio -204897539.816005 -197294400.0 -77 2431010 11 1 146345 152105
            // 62 dpfil-940204-od80-mvf.nio -197294400.0 -195364755.3121488 -77 2431010 11 1 152106 154410
            Vector3D[] bodyPosVel = spk.getPositionVelocity(et, target, 2431010);
            Vector3D[] idaPosVel = spk.getPositionVelocity(et, 2431010, 0);
            Vector3D[] sunPosVel = spk.getPositionVelocity(et, 10, 0);
            positionB1950 = idaPosVel[0].minus(sunPosVel[0]).plus(bodyPosVel[0]);
            velocityB1950 = idaPosVel[1].minus(sunPosVel[1]).plus(bodyPosVel[1]);
        }
        // Sun is observer (10)
        if (positionB1950.magnitude() < 1000.0) {
            // Sun is observer (10)
            Vector3D[] bodyPosVel = spk.getPositionVelocity(et, target, 10);
            positionB1950 = bodyPosVel[0];
            velocityB1950 = bodyPosVel[1];
        }

        // Position of Galileo wrt the Sun in B1950 reference frame
        if (positionB1950.magnitude() > 1000.0) {

            // Convert from reference frame B1950 to J2000
            //positionJ2000 = EphemerisUtil.transformFromB1950ToJ2000(positionB1950);
            //velocityJ2000 = EphemerisUtil.transformFromB1950ToJ2000(velocityB1950);
            positionJ2000 = EphemerisUtil.transformFromB1950ToJ2000_for_GalileoBSP(positionB1950);
            velocityJ2000 = EphemerisUtil.transformFromB1950ToJ2000_for_GalileoBSP(velocityB1950);

            // Position and velocity are computed for J2000 frame
            Vector3D positionInvTrans = EphemerisUtil.inverseTransformJ2000(positionJ2000);
            Vector3D velocityInvTrans = EphemerisUtil.inverseTransformJ2000(velocityJ2000);

            // Store position and velocity for later use
            positionStored = new Vector3D(positionInvTrans);
            velocityStored = new Vector3D(velocityInvTrans);
            dateTimeStored = date;

            return new Vector3D[]{positionInvTrans, velocityInvTrans};
        }

        // Check if position and velocity were obtained from SPK
        if (positionJ2000.magnitude() > 1000.0) {

            // Position and velocity are computed for J2000 frame
            Vector3D positionInvTrans = EphemerisUtil.inverseTransformJ2000(positionJ2000);
            Vector3D velocityInvTrans = EphemerisUtil.inverseTransformJ2000(velocityJ2000);

            return new Vector3D[]{positionInvTrans, velocityInvTrans};
        }

        // No position and velocity found, cannot estimate position and velocity
        return new Vector3D[]{new Vector3D(), new Vector3D()};
    }

    @Override
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

