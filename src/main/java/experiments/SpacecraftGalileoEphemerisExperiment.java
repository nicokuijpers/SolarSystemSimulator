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
package experiments;

import ephemeris.*;
import util.Vector3D;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This experiment is designed to confirm that the ephemeris of the Galileo spacecraft
 * is valid during the entire mission.
 * In this experiment, position and velocity of the spacecraft are determined each 1 minute
 * and checked for validity against the previously obtained position and velocity.
 * @author Nico Kuijpers
 */
public class SpacecraftGalileoEphemerisExperiment {

    // Ephemeris for the entire Galileo mission with the Sun as center body
    private static final IEphemeris ephemerisGalileo = EphemerisGalileoBSP.getInstance();

    // Ephemeris for the Interplanetary cruise with  the Sun as center body
    private static final IEphemeris ephemerisGalileoCruise = EphemerisGalileoCruiseBSP.getInstance();

    // Ephemeris for the Galileo mission after the interplanetary cruise with Jupiter as center body
    private static final IEphemeris ephemerisGalileoJupiter = EphemerisGalileoJupiterBSP.getInstance();

    // Time step [s]
    private static final int timeStep = 60; // 1 minute

    // Maximum allowed deviation in position [m]
    private final double maxDeviationPosition = 1.0E05; // 100 km

    // Maximum allowed deviation in velocity [m/s]
    private final double maxDeviationVelocity = 1.0E03; // 1 km/s

    public SpacecraftGalileoEphemerisExperiment() {
    }

    /**
     * Validate ephemeris for spacecraft with given name.
     * @param ephemeris       ephemeris for spacecraft
     * @param centerBody      name of the center body corresponding to ephemeris
     * @param spacecraftName  name of the spacecraft
     * @param timeStep        time step [s]
     */
    private void validateEphemerisForSpacecraft(IEphemeris ephemeris, String centerBody, String spacecraftName, int timeStep) {

        // Initialization
        double muCenterBody = SolarSystemParameters.getInstance().getMu(centerBody);
        GregorianCalendar experimentDateTime = (GregorianCalendar) ephemeris.getFirstValidDate().clone();
        Vector3D[] positionVelocityInit = ephemeris.getBodyPositionVelocity(spacecraftName, experimentDateTime);
        Vector3D positionInit = positionVelocityInit[0];
        Vector3D velocityInit = positionVelocityInit[1];
        double[] orbitParameters =
                EphemerisUtil.computeOrbitalParametersFromPositionVelocity(muCenterBody,positionInit,velocityInit,experimentDateTime);

        // Count number of violations
        int nrViolations = 0;

        // Start experiment
        while (experimentDateTime.before(ephemeris.getLastValidDate())) {

            // Update experiment date/time
            experimentDateTime.add(Calendar.SECOND,timeStep);

            // Predict current position and velocity from orbital parameters
            double[] orbitElements = EphemerisUtil.computeOrbitalElements(orbitParameters,experimentDateTime);
            Vector3D positionPredicted = EphemerisUtil.computePosition(orbitElements);
            Vector3D velocityPredicted = EphemerisUtil.computeVelocity(muCenterBody, orbitElements);

            // Actual position and velocity of spacecraft according to ephemeris
            Vector3D[] positionVelocitySpacecraft = ephemeris.getBodyPositionVelocity(spacecraftName, experimentDateTime);
            Vector3D position = positionVelocitySpacecraft[0];
            Vector3D velocity = positionVelocitySpacecraft[1];

            // Update orbital parameters
            orbitParameters = EphemerisUtil.computeOrbitalParametersFromPositionVelocity(muCenterBody,position,velocity,experimentDateTime);

            // Check whether position and velocity are defined
            if (position.magnitude() < 1.0 || velocity.magnitude() < 1.0) {
                System.out.println("Date/time: " + CalendarUtil.calendarToString(experimentDateTime));
                System.out.println("    No ephemeris for " + spacecraftName +
                        " Julian date = " + JulianDateConverter.convertCalendarToJulianDate(experimentDateTime));
            }

            // Validity check
            double deviationPosition = position.euclideanDistance(positionPredicted);
            double deviationVelocity = velocity.euclideanDistance(velocityPredicted);
            if (deviationPosition > maxDeviationPosition || deviationVelocity > maxDeviationVelocity) {
                nrViolations++;
                System.out.println("Date/time: " + CalendarUtil.calendarToString(experimentDateTime));
                System.out.println("    Center body        : " + centerBody);
                System.out.println("    Deviation position : " + deviationPosition/1000.0 + " km");
                System.out.println("    Deviation velocity : " + deviationVelocity + " m/s");
            }
        }

        // Print number of violations
        System.out.println("Number of violations : " + nrViolations +"\n");
    }

    /**
     * Main method.
     * Validate ephemeris for spacecraft Galileo.
     * @param args input arguments (not used)
     */
    public static void main(String[] args) {
        // Experiment set-up
        SpacecraftGalileoEphemerisExperiment experiment = new SpacecraftGalileoEphemerisExperiment();

        // Run experiment for the entire Galileo mission
        System.out.println("Validity check for entire Galileo mission");
        experiment.validateEphemerisForSpacecraft(ephemerisGalileo, "Sun", "Galileo", timeStep );

        // Run experiment for the Interplanetary cruise of theGalileo mission
        System.out.println("Validity check for Interplanetary cruise of the Galileo mission");
        experiment.validateEphemerisForSpacecraft(ephemerisGalileoCruise, "Sun", "Galileo", timeStep );

        // Run experiment for the Primary tour, GEM, and GMM parts of the Galileo mission
        System.out.println("Validity check for Primary tour, GEM, and GMM parts of the Galileo mission");
        experiment.validateEphemerisForSpacecraft(ephemerisGalileoJupiter, "Jupiter", "Galileo", timeStep);

        // Finished
        System.out.println("Validity checks finished");
    }
}
    /*
        Results with
        Maximum allowed deviation in position 100 km
        Maximum allowed deviation in velocity   1 km/s

        Validity check for entire Galileo mission
        Date/time: 1990-06-07 00:01:00.000
            Center body        : Sun
            Deviation position : 151.92615852221869 km
            Deviation velocity : 0.14836601143661002 m/s
        Date/time: 1992-04-19 00:01:00.000
            Center body        : Sun
            Deviation position : 597.155337175086 km
            Deviation velocity : 0.05344005572126688 m/s
        Date/time: 1992-10-20 00:01:00.000
            Center body        : Sun
            Deviation position : 658.9426381321225 km
            Deviation velocity : 0.7211208303536897 m/s
        Date/time: 1993-07-05 00:02:00.000
            Center body        : Sun
            Deviation position : 143.1779121345211 km
            Deviation velocity : 0.013325001575939832 m/s
        Date/time: 1993-10-23 08:01:00.000
            Center body        : Sun
            Deviation position : 117.45987619108952 km
            Deviation velocity : 0.0035023624731021566 m/s
        Date/time: 1994-03-30 00:02:00.000
            Center body        : Sun
            Deviation position : 144.43077366497283 km
            Deviation velocity : 0.024539111565242896 m/s
        Date/time: 1995-11-21 00:00:00.000
            Center body        : Sun
            Deviation position : 310.1826235971203 km
            Deviation velocity : 0.08907303381546279 m/s
        Date/time: 1995-11-21 00:01:00.000
            Center body        : Sun
            Deviation position : 172.9186863887463 km
            Deviation velocity : 1.1263788242963875 m/s
        Date/time: 1996-07-02 05:28:00.000
            Center body        : Sun
            Deviation position : 156.771294015495 km
            Deviation velocity : 0.7511303187162287 m/s
        Date/time: 1997-01-23 06:06:00.000
            Center body        : Sun
            Deviation position : 267.7855295910289 km
            Deviation velocity : 1.1944767671181316 m/s
        Date/time: 1999-08-11 16:20:00.000
            Center body        : Sun
            Deviation position : 390.41405719990564 km
            Deviation velocity : 7.341528207363376 m/s
        Date/time: 2000-11-01 12:02:00.000
            Center body        : Sun
            Deviation position : 178.70479910015848 km
            Deviation velocity : 0.021451289714875095 m/s
        Date/time: 2002-09-11 12:01:00.000
            Center body        : Sun
            Deviation position : 289.68386517219176 km
            Deviation velocity : 0.03256136352274796 m/s
        Date/time: 2003-01-28 01:01:00.000
            Center body        : Sun
            Deviation position : 406.94739922670635 km
            Deviation velocity : 0.042183220053723186 m/s
        Date/time: 2003-09-21 18:49:00.000
            Center body        : Sun
            Deviation position : 30.25783460368449 km
            Deviation velocity : 1015.4893811052129 m/s
        Date/time: 2003-09-21 18:50:00.000
            Center body        : Sun
            Deviation position : 31.524659168957065 km
            Deviation velocity : 1058.0440468438233 m/s
        Date/time: 2003-09-21 18:51:00.000
            Center body        : Sun
            Deviation position : 32.85183740468746 km
            Deviation velocity : 1102.6325871772347 m/s
        Date/time: 2003-09-21 18:52:00.000
            Center body        : Sun
            Deviation position : 34.239914360253024 km
            Deviation velocity : 1149.2699929907528 m/s
        Date/time: 2003-09-21 18:53:00.000
            Center body        : Sun
            Deviation position : 35.69095829175699 km
            Deviation velocity : 1197.9508250050205 m/s
        Date/time: 2003-09-21 18:54:00.000
            Center body        : Sun
            Deviation position : 37.20159033149421 km
            Deviation velocity : 1248.6353916451783 m/s
        Date/time: 2003-09-21 18:55:00.000
            Center body        : Sun
            Deviation position : 38.7698308040119 km
            Deviation velocity : 1301.246135824934 m/s
        Date/time: 2003-09-21 18:56:00.000
            Center body        : Sun
            Deviation position : 40.39377398275529 km
            Deviation velocity : 1355.6587604274725 m/s
        Date/time: 2003-09-21 18:57:00.000
            Center body        : Sun
            Deviation position : 42.06840770245949 km
            Deviation velocity : 1411.6934806896431 m/s
        Date/time: 2003-09-21 18:58:00.000
            Center body        : Sun
            Deviation position : 43.78399449340801 km
            Deviation velocity : 1469.10592012084 m/s
        Date/time: 2003-09-21 18:59:00.000
            Center body        : Sun
            Deviation position : 45.53428777125963 km
            Deviation velocity : 1527.5781132441039 m/s
        Date/time: 2003-09-21 19:00:00.000
            Center body        : Sun
            Deviation position : 47.30601103868969 km
            Deviation velocity : 1586.7103576448137 m/s
        Date/time: 2003-09-21 19:01:00.000
            Center body        : Sun
            Deviation position : 49.08573464567123 km
            Deviation velocity : 1646.0146420144076 m/s
        Date/time: 2003-09-21 19:02:00.000
            Center body        : Sun
            Deviation position : 50.856444088286274 km
            Deviation velocity : 1704.9120353496996 m/s
        Date/time: 2003-09-21 19:03:00.000
            Center body        : Sun
            Deviation position : 52.598840801387155 km
            Deviation velocity : 1762.7270851340138 m/s
        Date/time: 2003-09-21 19:04:00.000
            Center body        : Sun
            Deviation position : 54.28948596264776 km
            Deviation velocity : 1818.7047307345488 m/s
        Date/time: 2003-09-21 19:05:00.000
            Center body        : Sun
            Deviation position : 55.904637799986 km
            Deviation velocity : 1872.010819443555 m/s
        Date/time: 2003-09-21 19:06:00.000
            Center body        : Sun
            Deviation position : 57.41690536570918 km
            Deviation velocity : 1921.7570236064735 m/s
        Date/time: 2003-09-21 19:07:00.000
            Center body        : Sun
            Deviation position : 58.79978470622002 km
            Deviation velocity : 1967.026651760979 m/s
        Date/time: 2003-09-21 19:08:00.000
            Center body        : Sun
            Deviation position : 60.025631412299006 km
            Deviation velocity : 2006.9087735536345 m/s
        Date/time: 2003-09-21 19:09:00.000
            Center body        : Sun
            Deviation position : 61.0680200154517 km
            Deviation velocity : 2040.5379980931464 m/s
        Date/time: 2003-09-21 19:10:00.000
            Center body        : Sun
            Deviation position : 61.903015966417044 km
            Deviation velocity : 2067.137404296035 m/s
        Date/time: 2003-09-21 19:11:00.000
            Center body        : Sun
            Deviation position : 62.51063273469397 km
            Deviation velocity : 2086.0610847353346 m/s
        Date/time: 2003-09-21 19:12:00.000
            Center body        : Sun
            Deviation position : 62.875505133449415 km
            Deviation velocity : 2096.832306366511 m/s
        Date/time: 2003-09-21 19:13:00.000
            Center body        : Sun
            Deviation position : 62.98828544483812 km
            Deviation velocity : 2099.173245454756 m/s
        Date/time: 2003-09-21 19:14:00.000
            Center body        : Sun
            Deviation position : 62.84607852807282 km
            Deviation velocity : 2093.0243164583726 m/s
        Date/time: 2003-09-21 19:15:00.000
            Center body        : Sun
            Deviation position : 62.45252987237564 km
            Deviation velocity : 2078.541152433565 m/s
        Date/time: 2003-09-21 19:16:00.000
            Center body        : Sun
            Deviation position : 61.81744279359826 km
            Deviation velocity : 2056.0982354785992 m/s
        Date/time: 2003-09-21 19:17:00.000
            Center body        : Sun
            Deviation position : 60.957193467796984 km
            Deviation velocity : 2026.2512093838122 m/s
        Date/time: 2003-09-21 19:18:00.000
            Center body        : Sun
            Deviation position : 59.89224316516262 km
            Deviation velocity : 1989.7099587914777 m/s
        Date/time: 2003-09-21 19:19:00.000
            Center body        : Sun
            Deviation position : 58.64706122650811 km
            Deviation velocity : 1947.2965328855755 m/s
        Date/time: 2003-09-21 19:20:00.000
            Center body        : Sun
            Deviation position : 57.24811814052916 km
            Deviation velocity : 1899.9019611479675 m/s
        Date/time: 2003-09-21 19:21:00.000
            Center body        : Sun
            Deviation position : 55.72182390051741 km
            Deviation velocity : 1848.4443639571464 m/s
        Date/time: 2003-09-21 19:22:00.000
            Center body        : Sun
            Deviation position : 54.09634456082249 km
            Deviation velocity : 1793.831461783819 m/s
        Date/time: 2003-09-21 19:23:00.000
            Center body        : Sun
            Deviation position : 52.39924175727548 km
            Deviation velocity : 1736.929559277826 m/s
        Date/time: 2003-09-21 19:24:00.000
            Center body        : Sun
            Deviation position : 50.652426980824096 km
            Deviation velocity : 1678.540073412216 m/s
        Date/time: 2003-09-21 19:25:00.000
            Center body        : Sun
            Deviation position : 48.88046643846314 km
            Deviation velocity : 1619.3847076432687 m/s
        Date/time: 2003-09-21 19:26:00.000
            Center body        : Sun
            Deviation position : 47.10000051123386 km
            Deviation velocity : 1560.0912258213718 m/s
        Date/time: 2003-09-21 19:27:00.000
            Center body        : Sun
            Deviation position : 45.330700140458084 km
            Deviation velocity : 1501.2013501802878 m/s
        Date/time: 2003-09-21 19:28:00.000
            Center body        : Sun
            Deviation position : 43.58345453350916 km
            Deviation velocity : 1443.161631709472 m/s
        Date/time: 2003-09-21 19:29:00.000
            Center body        : Sun
            Deviation position : 41.87157865467397 km
            Deviation velocity : 1386.334108811493 m/s
        Date/time: 2003-09-21 19:30:00.000
            Center body        : Sun
            Deviation position : 40.203580937576554 km
            Deviation velocity : 1331.0025846612627 m/s
        Date/time: 2003-09-21 19:31:00.000
            Center body        : Sun
            Deviation position : 38.58569931337139 km
            Deviation velocity : 1277.3812924063807 m/s
        Date/time: 2003-09-21 19:32:00.000
            Center body        : Sun
            Deviation position : 37.023785122130725 km
            Deviation velocity : 1225.6239694880799 m/s
        Date/time: 2003-09-21 19:33:00.000
            Center body        : Sun
            Deviation position : 35.519209694109 km
            Deviation velocity : 1175.8328661743897 m/s
        Date/time: 2003-09-21 19:34:00.000
            Center body        : Sun
            Deviation position : 34.07681934000133 km
            Deviation velocity : 1128.067251158058 m/s
        Date/time: 2003-09-21 19:35:00.000
            Center body        : Sun
            Deviation position : 32.695110680773254 km
            Deviation velocity : 1082.351165834976 m/s
        Date/time: 2003-09-21 19:36:00.000
            Center body        : Sun
            Deviation position : 31.373349352296117 km
            Deviation velocity : 1038.6810364970124 m/s
        Number of violations : 62

        Validity check for Interplanetary cruise of the Galileo mission
        Date/time: 1990-06-07 00:01:00.000
            Center body        : Sun
            Deviation position : 151.92615852221869 km
            Deviation velocity : 0.14836601143661002 m/s
        Date/time: 1992-04-19 00:01:00.000
            Center body        : Sun
            Deviation position : 597.155337175086 km
            Deviation velocity : 0.05344005572126688 m/s
        Date/time: 1992-10-20 00:01:00.000
            Center body        : Sun
            Deviation position : 658.9426381321225 km
            Deviation velocity : 0.7211208303536897 m/s
        Date/time: 1993-07-05 00:02:00.000
            Center body        : Sun
            Deviation position : 143.1779121345211 km
            Deviation velocity : 0.013325001575939832 m/s
        Date/time: 1993-10-23 08:01:00.000
            Center body        : Sun
            Deviation position : 117.45987619108952 km
            Deviation velocity : 0.0035023624731021566 m/s
        Date/time: 1994-03-30 00:02:00.000
            Center body        : Sun
            Deviation position : 144.43077366497283 km
            Deviation velocity : 0.024539111565242896 m/s
        Number of violations : 6

        Validity check for Primary tour, GEM, and GMM parts of the Galileo mission
        Date/time: 1995-11-21 00:00:00.000
            Center body        : Jupiter
            Deviation position : 310.182651472387 km
            Deviation velocity : 0.07489990333735713 m/s
        Date/time: 1995-11-21 00:01:00.000
            Center body        : Jupiter
            Deviation position : 172.9186008059062 km
            Deviation velocity : 1.1615791629680043 m/s
        Date/time: 2000-11-01 12:02:00.000
            Center body        : Jupiter
            Deviation position : 178.70476231446884 km
            Deviation velocity : 0.005407727552543998 m/s
        Date/time: 2002-09-11 12:01:00.000
            Center body        : Jupiter
            Deviation position : 289.6838631490976 km
            Deviation velocity : 0.025658671154039258 m/s
        Date/time: 2003-01-28 01:01:00.000
            Center body        : Jupiter
            Deviation position : 406.9473942094898 km
            Deviation velocity : 0.040037638369892777 m/s
        Number of violations : 5

        Validity checks finished
    */



