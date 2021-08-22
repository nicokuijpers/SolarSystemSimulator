/*
 * Copyright (c) 2020 Nico Kuijpers
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

import ephemeris.EphemerisUtil;
import ephemeris.JulianDateConverter;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

public class ISS extends Spacecraft implements Serializable {

    /**
     * https://en.wikipedia.org/wiki/International_Space_Station
     * The International Space Station (ISS) is a modular space station (habitable artificial satellite)
     * in low Earth orbit. It is a multinational collaborative project between five participating space
     * agencies: NASA (United States), Roscosmos (Russia), JAXA (Japan), ESA (Europe), and CSA (Canada).
     * The ownership and use of the space station is established by intergovernmental treaties and
     * agreements. The station serves as a microgravity and space environment research laboratory in
     * which scientific research is conducted in astrobiology, astronomy, meteorology, physics, and
     * other fields. The ISS is suited for testing the spacecraft systems and equipment required for
     * possible future long-duration missions to the Moon and Mars.
     *
     * The first module of the ISS, Zarya, was launched on 20 November 1998
     */

    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Launch November 20, 1998
    private static final GregorianCalendar launch =
            new GregorianCalendar(1998, 10, 20, 0, 0, 0);

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
    public ISS(String name, String centerBodyName, SolarSystem solarSystem) {
        super(name, centerBodyName, solarSystem);
    }

    /**
     * Read dates and orbital parameters from file.
     * @param fileName
     */
    private void readOrbitParametersFromFile(String fileName) {
        int nrRows = 0;
        int nrCols = 7;
        File file = new File(fileName);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            scanner.useLocale(Locale.US);
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
        readOrbitParametersFromFile("EphemerisFiles/orbitParsISS.txt");
        List<SpacecraftTrajectory> trajectories = new ArrayList<>();
        eventDateTimes = new ArrayList<>();
        int rowStart = 0;
        while (rowStart + 1 < orbitPars.length) {
            GregorianCalendar startDateTime = JulianDateConverter.convertJulianDateToCalendar(orbitDates[rowStart]);
            GregorianCalendar stopDateTime = startDateTime;
            boolean stop = false;
            int rowStop = rowStart;
            while (rowStop + 1 < orbitDates.length && !stop) {
                rowStop++;
                stopDateTime = JulianDateConverter.convertJulianDateToCalendar(orbitDates[rowStop]);
                double[] orbitElementsStop = EphemerisUtil.computeOrbitalElements(orbitPars[rowStop],stopDateTime);
                Vector3D positionStop = EphemerisUtil.computePosition(orbitElementsStop);
                double[] orbitElementsPredicted = EphemerisUtil.computeOrbitalElements(orbitPars[rowStart],stopDateTime);
                Vector3D positionPredicted = EphemerisUtil.computePosition(orbitElementsPredicted);
                double deviation = positionPredicted.euclideanDistance(positionStop);
                stop = deviation > 1.0E6; // 1000 km
            }
            trajectories.add(
                    new SpacecraftTrajectory(startDateTime, stopDateTime,"Earth", orbitPars[rowStart]));
            eventDateTimes.add(startDateTime);
            rowStart = rowStop;
        }
        // Change stop date/time of final trajectory to October 2030 by adding 10 years to stop time
        SpacecraftTrajectory trajectory = trajectories.get(trajectories.size() - 1);
        trajectory.getStopDateTime().add(Calendar.YEAR,10);
        return trajectories;
    }

    @Override
    protected void defineEvents(SolarSystem solarSystem) {
        for (GregorianCalendar dateTime : eventDateTimes) {
            solarSystem.addSpacecraftEvent(new SpacecraftEvent(getName(), dateTime));
        }
    }
}