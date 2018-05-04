/*
 * Copyright (c) 2018 Nico Kuijpers
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import util.Vector3D;

/**
 * Accurate Ephemeris for Sun, Moon, and major planets including Pluto. 
 * This ephemeris is valid for Julian date/times between 2312752.5 
 * (January 1, 1620) and 2524623.5 (January 31, 2200).
 *
 * @author Nico Kuijpers
 */
public class EphemerisAccurate implements IEphemeris {
    
    /*
     * Note by Nico Kuijpers:
     * The code to read the JPL DE405 ephemeris files and compute the body 
     * positions and velocities is modified from
     * ftp://ssd.jpl.nasa.gov/pub/eph/planets/JAVA-version/java.src
     */
    
    
    /*  
	This class contains the methods necessary to parse the JPL DE405 ephemeris 
        files (text versions), and compute the position and velocity of the planets, 
        Moon, and Sun.  

	IMPORTANT: In order to use these methods, the user should:
            - save this class in a directory of his/her choosing;
            - save to the same directory the text versions of the DE405 ephemeris files, which must be named  
              "ASCPxxxx.405", where xxxx represents the start-year of the 20-year block;
            - have at least Java 1.1.8 installed.  

	The input is the julian date/time for which the ephemeris is needed.  
        Note that only julian dates from 2414992.5 to 2524624.5 are supported.   

	GENERAL IDEA:  The "getEphemerisCoefficients" method reads the ephemeris file 
        corresponding to the input julian day, and stores the ephemeris coefficients 
        needed to calculate planetary positions and velocities in the array "ephemerisCoefficients".  
	The "getPlanetPositionVelocity" method calls "getEphemerisCoefficients" if needed, 
        then calculates the position and velocity of the specified planet.
	The "planetaryEphemeris" method calls "getPlanetPositionVelocity" for each planet, 
        and resolves the position and velocity of the Earth/Moon barycenter and geocentric 
        Moon into the position and velocity of the Earth and Moon.  

	Since the "ephemerisCoefficients" array is declared as an instance variable, 
        its contents will remain intact, should this code be modified to call "planetaryEphemeris"
        more than once.  As a result, assuming the julian date of the subsequent call fell within 
        the same 20-year file as the initial call, there would be no need to reread the ephemeris 
        file; this would save on i/o time.

	The outputs are the arrays currentPositions and currentVelocities, also declared as 
        instance variables.

	Several key constants and variables follow.  As noted, they are configured for DE405; 
        however, they could be adjusted to use the DE200 ephemeris, whose format is quite similar.
    */
    
     // Indices for planets, moon, and sun for DE405 ephemeris
    private final Map<String, Integer> indexMap;

    // Names of planets, moon, and sun for which ephemeris can be computed
    private final List<String> bodies;

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // Singleton instance
    private static IEphemeris instance = null;

    // Location of the JPL DE405 ephemeris files (text versions) 
    private final String locationDE405EphemerisFiles = "DE405EphemerisFiles/";

    // Number of records for current ephemeris file
    private int records;

    // Ratio of mass of Earth to mass of Moon
    private final double earthMoonMassRatio = 81.30056;

    /*
     * Chebyshev coefficients for the DE405 ephemeris are contained in the 
     * files "ASCPxxxx.405". These files are broken into intervals of 
     * length "interval_duration", in days.
     */
    private final int intervalDuration = 32;

    /*
     * Each interval contains an interval number, length, start and end 
     * jultimes, and Chebyshev coefficients. We keep only the coefficients.  
     */
    private final int numbersPerInterval = 816;
    
    /*
     * For each planet (and the Moon makes 10, and the Sun makes 11), 
     * each interval contains several complete sets of coefficients, 
     * each covering a fraction of the interval duration
     */
    private final int[] numberCoefSets = {4, 2, 2, 1, 1, 1, 1, 1, 1, 8, 2};

    /*
     * Each planet (and the Moon makes 10, and the Sun makes 11) has a 
     * different number of Chebyshev coefficients used to calculate 
     * each component of position and velocity.
     */
    private final int[] numberCoefs = {14, 10, 13, 11, 8, 7, 6, 6, 6, 13, 11};

    // Ephemeris coefficients
    private final double[] ephemerisCoefficients = new double[187681];
    
    // Julian dates for which current coefficients are valid
    private double ephemerisDateBegin;
    private double ephemerisDateEnd;
    
    // Current Julian date/time for which positions and velocities are available
    private double currentJulianDateTime;
    
    // Positions of the major planets, Moon, and the Sun for current Julian date/time
    private final Vector3D[] currentPositions = new Vector3D[11];
    
    // Velocities of the major planets, Moon, and the Sun for current Julian date/time
    private final Vector3D[] currentVelocities = new Vector3D[11];

    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisAccurate() {
        // Indices for planets, moon, and sun for DE405 ephemeris
        indexMap = new HashMap<>();
        indexMap.put("mercury", 0);
        indexMap.put("venus", 1);
        indexMap.put("earth", 2);
        indexMap.put("mars", 3);
        indexMap.put("jupiter", 4);
        indexMap.put("saturn", 5);
        indexMap.put("uranus", 6);
        indexMap.put("neptune", 7);
        indexMap.put("pluto", 8);
        indexMap.put("moon", 9);
        indexMap.put("sun", 10);

        // Names of planets, moon, and sun for which ephemeris can be computed
        bodies = new ArrayList<>();
        for (String body : indexMap.keySet()) {
            bodies.add(body);
        }

        // First valid date: January 1, 1620
        firstValidDate = JulianDateConverter.convertJulianDateToCalendar(2312752.5);

        // Last valid date: January 31, 2200
        lastValidDate = JulianDateConverter.convertJulianDateToCalendar(2524623.5);
        
        // Initialize current Julian date/time, positions, and velocities
        GregorianCalendar today = new GregorianCalendar();
        today.setTimeZone(TimeZone.getTimeZone("UTC"));
        currentJulianDateTime = JulianDateConverter.convertCalendarToJulianDate(today);
        planetaryEphemeris(currentJulianDateTime);
    }

    /**
     * Get instance of EphemerisAccurate.
     *
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisAccurate();
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
        // Names of planets, moon, and sun for which DE405 ephemeris is known
        return Collections.unmodifiableList(bodies);
    }

    @Override
    public Vector3D getBodyPosition(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }

        // Make ephemeris up-to-date
        updateEphemeris(date);

        // Position of body with given name [m]
        int indexBody = indexMap.get(name);
        Vector3D positionBody = currentPositions[indexBody];

        // Position of Sun [m]
        int indexSun = indexMap.get("sun");
        Vector3D positionSun = currentPositions[indexSun];

        // Position of body relative to sun
        Vector3D position = positionBody.minus(positionSun);

        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(position);
    }

    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }
        
        // Make ephemeris up-to-date
        updateEphemeris(date);

        // Velocity of body with given name [m/s]
        int indexBody = indexMap.get(name);
        Vector3D velocityBody = currentVelocities[indexBody];

        // Velocity of Sun [m/s]
        int indexSun = indexMap.get("sun");
        Vector3D velocitySun = currentVelocities[indexSun];

        // Velocity of body relative to sun
        Vector3D velocity = velocityBody.minus(velocitySun);

        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(velocity);
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {
        Vector3D position = getBodyPosition(name, date);
        Vector3D velocity = getBodyVelocity(name, date);
        return new Vector3D[]{position, velocity};
    }

    @Override
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }

        // Make ephemeris up-to-date
        updateEphemeris(date);

        // Position of body with given name
        int indexBody = indexMap.get(name);
        Vector3D position = currentPositions[indexBody];

        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(position);
    }

    @Override
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!indexMap.keySet().contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Ephemeris DE405");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Ephemeris DE405");
        }

        // Make ephemeris up-to-date
        updateEphemeris(date);

        // Velocity of body with given name
        int indexBody = indexMap.get(name);
        Vector3D velocity = currentVelocities[indexBody];

        // Inverse transformation for 23.4 degrees J2000 frame
        return inverseTransformJ2000(velocity);
    }

    @Override
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date) {
        Vector3D position = getBodyPositionBarycenter(name, date);
        Vector3D velocity = getBodyVelocityBarycenter(name, date);
        return new Vector3D[]{position, velocity};
    }

    /**
     * Inverse transformation for 23.4 degrees J2000 frame. This transformation
     * is performed such that the J2000 ecliptic plane becomes the x-y plane.
     *
     * @param coordinates input coordinates
     * @return coordinates after transformation
     */
    private Vector3D inverseTransformJ2000(Vector3D coordinates) {
        double sinEP = -0.397776995;
        double cosEP = Math.sqrt(1.0 - sinEP * sinEP);
        double x = coordinates.getX();
        double y = coordinates.getY();
        double z = coordinates.getZ();
        return new Vector3D(x, cosEP * y - sinEP * z, sinEP * y + cosEP * z);
    }

    /**
     * Update current positions and velocities of the major planes, the Moon,
     * and the Sun.
     * @param date current date/time for ephemeris
     */
    public void updateEphemeris(GregorianCalendar date) {
        // Compute Julian date/time
        double julianDateTime = JulianDateConverter.convertCalendarToJulianDate(date);
        
        // Ensure that positions and velocities are available for given Julian date/time
        if (julianDateTime != currentJulianDateTime) {
            currentJulianDateTime = julianDateTime;
            planetaryEphemeris(julianDateTime);
        }
    }
    
    /**
     * Select the proper ephemeris file for given Julian date.
     *
     * @param julianDateTime Julian date/time
     * @return ephemeris file name
     */
    private String selectFileForJulianDate(double julianDateTime) {
        /* 
         * The code to select the proper ephemeris file for
         * dates between 1600 and 1900 is obtained from
         * https://sourceforge.net/p/jalma/jderead-1/ci/
         * f09b4325b62008d5152361d220a0342b8427ce40/tree/src/jderead/DE405.java
         *                
         * JDEread: Java reader for JPL DE/LE ephemerides.
         * Copyright (C) 2009,2010  Peter Hristozov. All rights reserved.
         * JDEread is distributed under the GNU Lesser General Public Licence,
         * see http://www.gnu.org/licenses/
         */
        String filename = "";
        if ((julianDateTime >= 2451536.5) && (julianDateTime < 2458832.5)) {
            ephemerisDateBegin = 2451536.5;
            ephemerisDateEnd = 2458832.5;
            filename = "ascp2000.405";
            records = 229;
        } else if ((julianDateTime >= 2444208.5) && (julianDateTime < 2451536.5)) {
            ephemerisDateBegin = 2444208.5;
            ephemerisDateEnd = 2451536.5;
            filename = "ascp1980.405";
            records = 230;
        } else if ((julianDateTime >= 2458832.5) && (julianDateTime < 2466128.5)) {
            ephemerisDateBegin = 2458832.5;
            ephemerisDateEnd = 2466128.5;
            filename = "ascp2020.405";
            records = 229;
        } else if ((julianDateTime >= 2436912.5) && (julianDateTime < 2444208.5)) {
            ephemerisDateBegin = 2436912.5;
            ephemerisDateEnd = 2444208.5;
            filename = "ascp1960.405";
            records = 229;
        } else if ((julianDateTime >= 2466128.5) && (julianDateTime < 2473456.5)) {
            ephemerisDateBegin = 2466128.5;
            ephemerisDateEnd = 2473456.5;
            filename = "ascp2040.405";
            records = 230;
        } else if ((julianDateTime >= 2429616.5) && (julianDateTime < 2436912.5)) {
            ephemerisDateBegin = 2429616.5;
            ephemerisDateEnd = 2436912.5;
            filename = "ascp1940.405";
            records = 229;
        } else if ((julianDateTime >= 2473456.5) && (julianDateTime < 2480752.5)) {
            ephemerisDateBegin = 2473456.5;
            ephemerisDateEnd = 2480752.5;
            filename = "ascp2060.405";
            records = 229;
        } else if ((julianDateTime >= 2422320.5) && (julianDateTime < 2429616.5)) {
            ephemerisDateBegin = 2422320.5;
            ephemerisDateEnd = 2429616.5;
            filename = "ascp1920.405";
            records = 229;
        } else if ((julianDateTime >= 2480752.5) && (julianDateTime < 2488048.5)) {
            ephemerisDateBegin = 2480752.5;
            ephemerisDateEnd = 2488048.5;
            filename = "ascp2080.405";
            records = 229;
        } else if ((julianDateTime >= 2414992.5) && (julianDateTime < 2422320.5)) {
            ephemerisDateBegin = 2414992.5;
            ephemerisDateEnd = 2422320.5;
            filename = "ascp1900.405";
            records = 230;
        } else if ((julianDateTime >= 2488048.5) && (julianDateTime < 2495344.5)) {
            ephemerisDateBegin = 2488048.5;
            ephemerisDateEnd = 2495344.5;
            filename = "ascp2100.405";
            records = 229;
        } else if ((julianDateTime >= 2407696.5) && (julianDateTime < 2414992.5)) {
            ephemerisDateBegin = 2407696.5;
            ephemerisDateEnd = 2414992.5;
            filename = "ascp1880.405";
            records = 229;
        } else if ((julianDateTime >= 2495344.5) && (julianDateTime < 2502672.5)) {
            ephemerisDateBegin = 2495344.5;
            ephemerisDateEnd = 2502672.5;
            filename = "ascp2120.405";
            records = 230;
        } else if ((julianDateTime >= 2400400.5) && (julianDateTime < 2407696.5)) {
            ephemerisDateBegin = 2400400.5;
            ephemerisDateEnd = 2407696.5;
            filename = "ascp1860.405";
            records = 229;
        } else if ((julianDateTime >= 2502672.5) && (julianDateTime < 2509968.5)) {
            ephemerisDateBegin = 2502672.5;
            ephemerisDateEnd = 2509968.5;
            filename = "ascp2140.405";
            records = 229;
        } else if ((julianDateTime >= 2393104.5) && (julianDateTime < 2400400.5)) {
            ephemerisDateBegin = 2393104.5;
            ephemerisDateEnd = 2400400.5;
            filename = "ascp1840.405";
            records = 229;
        } else if ((julianDateTime >= 2509968.5) && (julianDateTime < 2517264.5)) {
            ephemerisDateBegin = 2509968.5;
            ephemerisDateEnd = 2517264.5;
            filename = "ascp2160.405";
            records = 229;
        } else if ((julianDateTime >= 2385776.5) && (julianDateTime < 2393104.5)) {
            ephemerisDateBegin = 2385776.5;
            ephemerisDateEnd = 2393104.5;
            filename = "ascp1820.405";
            records = 230;
        } else if ((julianDateTime >= 2517264.5) && (julianDateTime < 2524624.5)) {
            ephemerisDateBegin = 2517264.5;
            ephemerisDateEnd = 2524624.5;
            filename = "ascp2180.405";
            records = 230;
        } else if ((julianDateTime >= 2378480.5) && (julianDateTime < 2385776.5)) {
            ephemerisDateBegin = 2378480.5;
            ephemerisDateEnd = 2385776.5;
            filename = "ascp1800.405";
            records = 229;
        } else if ((julianDateTime >= 2371184.5) && (julianDateTime < 2378480.5)) {
            ephemerisDateBegin = 2371184.5;
            ephemerisDateEnd = 2378480.5;
            filename = "ascp1780.405";
            records = 229;
        } else if ((julianDateTime >= 2363856.5) && (julianDateTime < 2371184.5)) {
            ephemerisDateBegin = 2363856.5;
            ephemerisDateEnd = 2371184.5;
            filename = "ascp1760.405";
            records = 230;
        } else if ((julianDateTime >= 2356560.5) && (julianDateTime < 2363856.5)) {
            ephemerisDateBegin = 2356560.5;
            ephemerisDateEnd = 2363856.5;
            filename = "ascp1740.405";
            records = 229;
        } else if ((julianDateTime >= 2349264.5) && (julianDateTime < 2356560.5)) {
            ephemerisDateBegin = 2349264.5;
            ephemerisDateEnd = 2356560.5;
            filename = "ascp1720.405";
            records = 229;
        } else if ((julianDateTime >= 2341968.5) && (julianDateTime < 2349264.5)) {
            ephemerisDateBegin = 2341968.5;
            ephemerisDateEnd = 2349264.5;
            filename = "ascp1700.405";
            records = 229;
        } else if ((julianDateTime >= 2334640.5) && (julianDateTime < 2341968.5)) {
            ephemerisDateBegin = 2334640.5;
            ephemerisDateEnd = 2341968.5;
            filename = "ascp1680.405";
            records = 230;
        } else if ((julianDateTime >= 2327344.5) && (julianDateTime < 2334640.5)) {
            ephemerisDateBegin = 2327344.5;
            ephemerisDateEnd = 2334640.5;
            filename = "ascp1660.405";
            records = 229;
        } else if ((julianDateTime >= 2320048.5) && (julianDateTime < 2327344.5)) {
            ephemerisDateBegin = 2320048.5;
            ephemerisDateEnd = 2327344.5;
            filename = "ascp1640.405";
            records = 229;
        } else if ((julianDateTime >= 2312752.5) && (julianDateTime < 2320048.5)) {
            ephemerisDateBegin = 2312752.5; // January 1, 1620 00:00 UTC
            ephemerisDateEnd = 2320048.5; // December 23, 1639 00:00 UTC
            filename = "ascp1620.405";
            records = 229;
        } 
        /* 
         * Note by Nico Kuijpers: 
         * Date/time before Janary 1, 1620 not supported.
         * Ephemeris results not valid between Nov 30, 1619 and Jan 1, 1620.
         */
        /*
        else if ((julianDateTime >= 2305424.5) && (julianDateTime < 2312752.5)) {
            ephemerisDateBegin = 2305424.5; // December 9, 1599 00:00 UTC
            ephemerisDateEnd = 2312720.5; // November 30, 1619 00:00 UTC
            filename = "ascp1600.405";
            records = 229;
        }
        */
        return filename;
    }

    /**
     * Calculate position and velocity at given Julian date/time
     * of the major planets, the Moon, and the Sun.
     *
     * @param julianDateTime Julian date/time
     */
    private void planetaryEphemeris(double julianDateTime) {
        
        /* 
         * Determine the ephemeris positions and velocities of each major planet,
         * the Moon and the Sun.
         * Note that the planets are enumerated as follows:
         * Mercury = 1, Venus = 2, Earth-Moon barycenter = 3, Mars = 4, ... ,
         * Pluto = 9, Geocentric Moon = 10, Sun = 11. 
         * Further note that instead of the position of the Earth, the position 
         * of the Earth-Moon barycenter is determined, while a geocentric vector from the
         * Earth to the Moon is determined instead of the position of the moon.
         */
        for (int i = 0; i < indexMap.size(); i++) {
            Vector3D[] positionVelocity = getPlanetPositionVelocity(julianDateTime, i);
            currentPositions[i] = positionVelocity[0];
            currentVelocities[i] = positionVelocity[1];
        }
        
        /* 
         * The positions and velocities of the Earth and Moon are found indirectly.  
         * We already have the position and velocity of the Earth-Moon barycenter
         * and a geocentric vector from the Earth to the Moon.
         * Using the ratio of masses, we get vectors from the Earth-Moon 
         * barycenter to the Moon and to the Earth.
         */
        int indexEarth = indexMap.get("earth");
        int indexMoon = indexMap.get("moon");
        Vector3D earthPosition = currentPositions[indexEarth].
                        minus(currentPositions[indexMoon].
                              scalarProduct(1.0 / (1.0 + earthMoonMassRatio)));
        currentPositions[indexEarth] = earthPosition;
        Vector3D moonPosition = earthPosition.plus(currentPositions[indexMoon]);
        currentPositions[indexMoon] = moonPosition;
        Vector3D earthVelocity = currentVelocities[indexEarth].
                        minus(currentVelocities[indexMoon].
                              scalarProduct(1.0 / (1.0 + earthMoonMassRatio)));
        currentVelocities[indexEarth] = earthVelocity;
        Vector3D moonVelocity = earthVelocity.plus(currentVelocities[indexMoon]);
        currentVelocities[indexMoon] = moonVelocity;
    }

    /**
     * Calculate the position and velocity of the body corresponding to the given
     * index, according to JPL DE405 ephemeris.
     * The positions and velocities are calculated using Chebyshev polynomials,
     * the coefficients of which are stored in the files "ASCPxxxx.405".
     * The general idea is as follows: First, check to be sure the proper 
     * ephemeris coefficients (corresponding to given Julian date/time) are 
     * available. Then read the coefficients corresponding to given date/time, 
     * and calculate the positions and velocities of the planet.  
     * @param julianDateTime Julian date/time
     * @param index          Index of planet
     * @return               Position [m] and velocity [m/s] of planet
     */
    private Vector3D[] getPlanetPositionVelocity(double julianDateTime, int index) {
        
        /* 
         * Determine whether the current ephemeris coefficients are appropriate 
         * for given Julian date/time. If not, a new set is loaded.  
         */
        if ((julianDateTime < ephemerisDateBegin) || (julianDateTime > ephemerisDateEnd)) {
            getEphemerisCoefficients(julianDateTime);
        }

        int interval = (int) (Math.floor((julianDateTime - ephemerisDateBegin) / intervalDuration) + 1);
        double intervalStartTime = (interval - 1) * intervalDuration + ephemerisDateBegin;
        double subintervalDuration = intervalDuration / numberCoefSets[index];
        int subinterval = (int) (Math.floor((julianDateTime - intervalStartTime) / subintervalDuration) + 1);
        int numbersToSkip = (interval - 1) * numbersPerInterval;

        /*  
	 * Starting at the beginning of the coefficient array, skip the first 
         * "numbersToSkip" coefficients. This puts the pointer on the 
         * first piece of data in the correct interval.  
         */
        int pointer = numbersToSkip + 1;

        // Skip the coefficients for the bodies before this body
        for (int j = 0; j < index; j++) {
            pointer = pointer + 3 * numberCoefSets[j] * numberCoefs[j];
        }

        // Skip the next (subinterval - 1) * 3 * numberOfCoefs(index) coefficients
        pointer = pointer + (subinterval - 1) * 3 * numberCoefs[index];

        // Read the coefficients
        double[][] coef = new double[3][19];
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < numberCoefs[index]; k++) {
                // Read the coefficient at the pointer as the array entry coef[j][k]
                coef[j][k] = ephemerisCoefficients[pointer];
                pointer = pointer + 1;
            }
        }

        // Calculate the Chebyshev time within the subinterval, between -1 and +1
        double chebyshevTime = 
                2 * (julianDateTime - ((subinterval - 1) * subintervalDuration + intervalStartTime)) / 
                subintervalDuration - 1;

        // Calculate the Chebyshev position polynomials
        double[] positionPoly = new double[19];
        positionPoly[0] = 1;
        positionPoly[1] = chebyshevTime;
        for (int j = 2; j < numberCoefs[index]; j++) {
            positionPoly[j] = 2 * chebyshevTime * positionPoly[j - 1] - positionPoly[j - 2];
        }

        // Calculate the position of the body
        double[] ephemerisPosition = new double[3];
        for (int j = 0; j < 3; j++) {
            ephemerisPosition[j] = 0;
            for (int k = 0; k < numberCoefs[index]; k++) {
                ephemerisPosition[j] = ephemerisPosition[j] + coef[j][k] * positionPoly[k];
            }
        }

        // Calculate the Chebyshev velocity polynomials
        double[] velocityPoly = new double[19];
        velocityPoly[0] = 0;
        velocityPoly[1] = 1;
        velocityPoly[2] = 4 * chebyshevTime;
        for (int j = 3; j < numberCoefs[index]; j++) {
            velocityPoly[j] = 2 * chebyshevTime * velocityPoly[j - 1] + 
                              2 * positionPoly[j - 1] - velocityPoly[j - 2];
        }

        // Calculate the velocity of the body
        double[] ephemerisVelocity = new double[3];
        for (int j = 0; j < 3; j++) {
            ephemerisVelocity[j] = 0;
            for (int k = 0; k < numberCoefs[index]; k++) {
                ephemerisVelocity[j] = ephemerisVelocity[j] + coef[j][k] * velocityPoly[k];
            }
            /*
             * The next line accounts for differentiation of the iterative formula with respect 
             * to Chebyshev time.  
             * Essentially, if dx/dt = (dx/dct) times (dct/dt), the next line includes the 
             * factor (dct/dt) so that the units are km/day
             */
            ephemerisVelocity[j] = ephemerisVelocity[j] * 
                                   (2.0 * numberCoefSets[index] / intervalDuration);
        }
        
        // Position of the body - convert from km to m
        double posX = 1000 * ephemerisPosition[0];
        double posY = 1000 * ephemerisPosition[1];
        double posZ = 1000 * ephemerisPosition[2];
        Vector3D position = new Vector3D(posX,posY,posZ);
        
        // Velocity of the body - convert from km/day to m/s
        double veloX = (1000.0/86400.0) * ephemerisVelocity[0];
        double veloY = (1000.0/86400.0) * ephemerisVelocity[1];
        double veloZ = (1000.0/86400.0) * ephemerisVelocity[2];
        Vector3D velocity = new Vector3D(veloX,veloY,veloZ);
        
        // Return position and velocity of the body
        return new Vector3D[]{position, velocity};
    }

    /**
     * Read the DE405 ephemeris file corresponding to given Julian date/time.
     * The start and end dates of the ephemeris file are updated, as are the 
     * Chebyshev coefficients for Mercury, Venus, Earth-Moon, Mars, Jupiter,
     * Saturn, Uranus, Neptune, Pluto, Geocentric Moon, and Sun.
     * @param julianDateTime Julian date/time
     */
    private void getEphemerisCoefficients(double julianDateTime) {

        /*
            Tested and verified 7-16-99.
         */
        int mantissa1 = 0;
        int mantissa2 = 0;
        int exponent = 0;
        int lineNr = 0;

        String filename = selectFileForJulianDate(julianDateTime);
        File file = new File(locationDE405EphemerisFiles,filename);

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader buff = new BufferedReader(fileReader);

            // Read each record in the file
            for (int j = 1; j <= records; j++) {

                // Read line 1 and ignore
                String line = buff.readLine();

                // Read lines 2 through 274 and parse as appropriate
                for (lineNr = 2; lineNr <= 274; lineNr++) {
                    line = buff.readLine();
                    if (lineNr > 2) {
                        // Parse first entry
                        mantissa1 = Integer.parseInt(line.substring(4, 13));
                        mantissa2 = Integer.parseInt(line.substring(13, 22));
                        exponent = Integer.parseInt(line.substring(24, 26));
                        if (line.substring(23, 24).equals("+")) {
                            ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) - 1)] = 
                                    mantissa1 * Math.pow(10, (exponent - 9)) + 
                                    mantissa2 * Math.pow(10, (exponent - 18));
                        } else {
                            ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) - 1)] = 
                                    mantissa1 * Math.pow(10, -(exponent + 9)) + 
                                    mantissa2 * Math.pow(10, -(exponent + 18));
                        }
                        if (line.substring(1, 2).equals("-")) {
                            ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) - 1)] = 
                                    -ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) - 1)];
                        }
                    }
                    if (lineNr > 2) {
                        // Parse second entry
                        mantissa1 = Integer.parseInt(line.substring(30, 39));
                        mantissa2 = Integer.parseInt(line.substring(39, 48));
                        exponent = Integer.parseInt(line.substring(50, 52));
                        if (line.substring(49, 50).equals("+")) {
                            ephemerisCoefficients[(j - 1) * 816 + 3 * (lineNr - 2)] = 
                                    mantissa1 * Math.pow(10, (exponent - 9)) + 
                                    mantissa2 * Math.pow(10, (exponent - 18));
                        } else {
                            ephemerisCoefficients[(j - 1) * 816 + 3 * (lineNr - 2)] = 
                                    mantissa1 * Math.pow(10, -(exponent + 9)) + 
                                    mantissa2 * Math.pow(10, -(exponent + 18));
                        }
                        if (line.substring(27, 28).equals("-")) {
                            ephemerisCoefficients[(j - 1) * 816 + 3 * (lineNr - 2)] = 
                                    -ephemerisCoefficients[(j - 1) * 816 + 3 * (lineNr - 2)];
                        }
                    }
                    if (lineNr < 274) {
                        // Parse third entry
                        mantissa1 = Integer.parseInt(line.substring(56, 65));
                        mantissa2 = Integer.parseInt(line.substring(65, 74));
                        exponent = Integer.parseInt(line.substring(76, 78));
                        if (line.substring(75, 76).equals("+")) {
                            ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) + 1)] = 
                                    mantissa1 * Math.pow(10, (exponent - 9)) + 
                                    mantissa2 * Math.pow(10, (exponent - 18));
                        } else {
                            ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) + 1)] = 
                                    mantissa1 * Math.pow(10, -(exponent + 9)) + 
                                    mantissa2 * Math.pow(10, -(exponent + 18));
                        }
                        if (line.substring(53, 54).equals("-")) {
                            ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) + 1)] = 
                                    -ephemerisCoefficients[(j - 1) * 816 + (3 * (lineNr - 2) + 1)];
                        }
                    }
                }

                // Read lines 275 through 341 and ignore
                for (lineNr = 275; lineNr <= 341; lineNr++) {
                    line = buff.readLine();
                }
            }
            buff.close();

        } catch (IOException e) {
            System.out.println("Error = " + e.toString());
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("String index out of bounds at line number " + lineNr);
        }
    }
}
