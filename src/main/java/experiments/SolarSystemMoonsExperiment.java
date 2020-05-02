
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

import application.SolarSystemException;
import ephemeris.CalendarUtil;
import ephemeris.EphemerisSolarSystem;
import ephemeris.IEphemeris;
import ephemeris.SolarSystemParameters;
import solarsystem.SolarSystem;
import util.Vector3D;

import java.util.*;

        /**
         * In this experiment the distance between simulated positions and epthemeris of the
         * Galilean, Saturnian, Uranian, and Neptunian moons is determined after 25 years of simulation.
         *
         * @author Nico Kuijpers
         */
        public class SolarSystemMoonsExperiment {

            // Ephemeris
            private IEphemeris ephemeris;

            // The Solar System
            private SolarSystem solarSystem;

            // Start date for simulation
            private final GregorianCalendar simulationStartDateTime;

            // End date for simulation
            private final GregorianCalendar simulationEndDateTime;

            // Start date for collecting results
            private final GregorianCalendar startCollectingResultsDateTime;

            // Planets to gather results from
            private final List<String> planets;

            // Moons to gather results from
            private final Map<String,List<String>> moons;

            // Collect results for planets
            private final Map<String,List<Double>> resultsPlanets;

            // Collect results for moons
            private final Map<String,List<Double>> resultsMoons;

            /**
             * Constructor.
             * Set simulation start and end date. Create the Solar System.
             */
            public SolarSystemMoonsExperiment() {
                // Set ephemeris
                ephemeris = EphemerisSolarSystem.getInstance();

                // Start date simulation Jan 1, 1990
                // Note that January is month 0
                simulationStartDateTime = new GregorianCalendar(1990,0,1,0,0);

                // https://www.timeanddate.com/time/aboututc.html
                // Use Coordinated Universal Time (UTC) to avoid
                // sudden changes in ephemeris due to changes from
                // winter time to summer time and vice versa
                simulationStartDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

                // Stop date simulation Jan 1, 2000
                simulationEndDateTime = new GregorianCalendar(2000,0,1,0,0 );
                simulationEndDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

                // Start collecting results at January 1, 1999
                startCollectingResultsDateTime = new GregorianCalendar(1999,0,1,0,0 );
                startCollectingResultsDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

                // Define planets to gather results from
                planets = new ArrayList<>(Arrays.asList("Earth","Jupiter","Saturn","Uranus","Neptune"));

                // Define moons to gather results from
                moons = new HashMap<>();
                moons.put("Earth",
                        new ArrayList<>(Arrays.asList("Moon")));
                moons.put("Jupiter",
                        new ArrayList<>(Arrays.asList("Io","Europa","Ganymede","Callisto")));
                moons.put("Saturn",
                        new ArrayList<>(Arrays.asList("Mimas","Enceladus","Tethys","Dione","Rhea","Titan","Iapetus")));
                moons.put("Uranus",
                        new ArrayList<>(Arrays.asList("Miranda","Ariel","Umbriel","Titania","Oberon")));
                moons.put("Neptune",
                        new ArrayList<>(Arrays.asList("Triton")));

                // Store results
                resultsPlanets = new HashMap<>();
                resultsMoons = new HashMap<>();
                for (String planetName : planets) {
                    resultsPlanets.put(planetName,new ArrayList<>());
                    for (String moonName : moons.get(planetName)) {
                        resultsMoons.put(moonName,new ArrayList<>());
                    }
                }

                // Create the Solar System
                solarSystem = new SolarSystem(simulationStartDateTime);
                for (String planetName : planets) {
                    try {
                        if (!"Earth".equals(planetName)) {
                            solarSystem.createPlanetSystem(planetName);
                        }
                    }
                    catch (SolarSystemException ex) {
                        System.err.println(ex.getMessage());
                    }
                }

                // Set General Relativity flag
                // True means General Relativity is applied
                // False means Newton Mechanics is applied
                // solarSystem.setGeneralRelativityFlag(true);
                solarSystem.setGeneralRelativityFlag(false);
            }

            /**
             * Show simulation set-up.
             */
            public void showSimulationSetup() {
                System.out.println("Experiment date/time       : " +
                        CalendarUtil.calendarToString(new GregorianCalendar()));
                System.out.println("Simulation start date/time : " +
                        CalendarUtil.calendarToString(simulationStartDateTime));
                System.out.println("Simulation end date/time   : " +
                        CalendarUtil.calendarToString(simulationEndDateTime));
                System.out.print("Simulation method          : ");
                if (solarSystem.getGeneralRelativityFlag()) {
                    System.out.println("General Relativity");
                }
                else {
                    System.out.println("Newton Mechanics");
                }
            }

            /**
             * Simulate the Solar System.
             */
            public void simulate() {
                while(solarSystem.getSimulationDateTime().before(simulationEndDateTime)) {
                    // Advance one time step
                    solarSystem.advanceSimulationForward(1);

                    // Collect results for the last year
                    if (solarSystem.getSimulationDateTime().after(startCollectingResultsDateTime))
                    {
                        for (String planetName : resultsPlanets.keySet()) {
                            double distancePlanet = computeDistancePlanet(planetName,solarSystem.getSimulationDateTime());
                            resultsPlanets.get(planetName).add(distancePlanet);
                        }
                        for (String moonName : resultsMoons.keySet()) {
                            double distanceMoon = computeDistanceMoon(moonName,solarSystem.getSimulationDateTime());
                            resultsMoons.get(moonName).add(distanceMoon);
                        }
                        startCollectingResultsDateTime.add(Calendar.HOUR,24);
                    }
                }
            }

            /**
             * Show results of simulation.
             */
            public void showResults() {
                System.out.println("Deviation is averaged over final year of simulation");
                System.out.println("Deviation for planets is relative to the Sun");
                System.out.println("Deviation for moons is relative to their planet");
                for (String planetName : planets) {
                    System.out.println("Planet: " + planetName);
                    double deviationPlanet = computeAverage(resultsPlanets.get(planetName));
                    System.out.println("Deviation " + 0.001 * deviationPlanet + " km for " + planetName);
                    for (String moonName : moons.get(planetName)) {
                        double deviationMoon = computeAverage(resultsMoons.get(moonName));
                        System.out.println("Deviation " + 0.001 * deviationMoon + " km for " + moonName);
                    }
                }
            }

            /**
             * Compute distance between actual position of planet and expected position for given date.
             * @param planetName Name of the planet
             * @param dateTime   Date to determine position and velocity of the planet
             * @return distance [m]
             */
            private double computeDistancePlanet(String planetName, GregorianCalendar dateTime) {
                dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));

                // Obtain position of planet from Ephemeris
                // Note that position and velocity from Ephemeris are relative to the Sun
                Vector3D[] positionAndVelocity = ephemeris.getBodyPositionVelocity(planetName, dateTime);
                Vector3D positionExpected = positionAndVelocity[0];

                // Obtain position of planet from simulation results
                Vector3D positionActual = solarSystem.getParticle(planetName).getPosition();

                // Compute distance
                return positionExpected.euclideanDistance(positionActual);
            }

            /**
             * Compute distance between actual position of moon and expected position for given date.
             * @param moonName    Name of the body
             * @param dateTime    Date to determine position and velocity of this body
             * @return distance [m]
             */
            private double computeDistanceMoon(String moonName, GregorianCalendar dateTime) {
                if ("Moon".equals(moonName)) {
                    // Obtain position of Earths's Moon from Ephemeris
                    // Note that position of Earth's Moon is relative to the Sun
                    Vector3D positionEarthEphemeris = ephemeris.getBodyPosition("Earth", dateTime);
                    Vector3D positionMoonEphemeris = ephemeris.getBodyPosition("Moon", dateTime);
                    Vector3D positionMoonRelativeToEarthEphemeris = positionMoonEphemeris.minus(positionEarthEphemeris);

                    // Obtain position of the Moon relative to the Earth from simulation results
                    Vector3D positionEarthSimulation = solarSystem.getParticle("Earth").getPosition();
                    Vector3D positionMoonSimulation = solarSystem.getParticle("Moon").getPosition();
                    Vector3D positionMoonRelativeToEarthSimulation = positionMoonSimulation.minus(positionEarthSimulation);

                    // Deviation relative to the position of the Earth
                    Vector3D deviationMoonRelativeToEarth = positionMoonRelativeToEarthSimulation.minus(positionMoonRelativeToEarthEphemeris);
                    return deviationMoonRelativeToEarth.magnitude();

                    // USE THE CODE IN NEXT LINES FOR POSITION RELATIVE TO THE SUN
                    // Vector3D deviationMoonRelativeToSun = positionMoonSimulation.minus(positionMoonEphemeris);
                    // return deviationMoonRelativeToSun.magnitude();
                } else {
                    // Obtain position and velocity of moon from Ephemeris
                    // Note that positions and velocities of moons are relative to their planet
                    Vector3D positionExpected = ephemeris.getBodyPosition(moonName, dateTime);

                    // Obtain position of moon relative to planet from simulation results
                    String planetName = SolarSystemParameters.getInstance().getPlanetOfMoon(moonName);
                    Vector3D positionPlanet = solarSystem.getParticle(planetName).getPosition();
                    Vector3D positionMoon = solarSystem.getParticle(moonName).getPosition();
                    Vector3D positionActual = positionMoon.minus(positionPlanet);
                    // USE THE CODE IN NEXT LINE FOR POSITION RELATIVE TO THE SUN
                    // Vector3D positionActual = positionMoon;
                    return positionExpected.euclideanDistance(positionActual);
                }
            }

            /**
             * Compute average of list of values.
             * @param values list of values
             * @return average
             */
            private double computeAverage(List<Double> values) {
                double sum = 0.0;
                for (double value : values) {
                    sum += value;
                }
                return sum / values.size();
            }

            /**
             * Main method.
             * Simulate and compute deviation for each moon.
             * @param args input arguments (not used)
             */
            public static void main (String[] args) throws SolarSystemException {

                // Experiment set-up
                SolarSystemMoonsExperiment experiment = new SolarSystemMoonsExperiment();

                // Run simulation and show results
                experiment.showSimulationSetup();
                experiment.simulate();
                experiment.showResults();
            }
        }

