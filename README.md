# SolarSystemSimulator
Solar System Simulator written in Java. Positions and velocities of Solar System bodies and Spacecraft are continuously updated using
either Newton mechanics or General Relativity. Initial positions and velocities are obtained from Nasa JPL's ephemerides. 
While simulating, both simulated positions and ephemeris data are visualised for comparison. Source code is made publicly
availaible under the MIT licence.

User's manual: [SolarSystemManual.pdf](https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/SolarSystemSimulatorManual.pdf)

Technical notes: [SolarSystemTechnicalNotes.pdf](https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/SolarSystemSimulatorTechnicalNotes.pdf)

Video The Journey of Voyager 1: https://youtu.be/HgLGFVqZAUw

Video The Journey of Voyager 2: https://youtu.be/_JBRZJpCbRg

Video The Grand Tour of Voyager: https://youtu.be/Yq0LxCjSxdI

Video The Journeys of Pioneers 10 and 11: https://youtu.be/a5LRzPxkXR4

Required ephemeris files: 
DE405EphemerisFiles (directory containing ephemeris data as text files)
EphemerisFiles (directory containing .bsp and .txt file)

When downloading source code, the .bsp will not be downloaded. You should download them separately:
https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/EphemerisFiles/de405.bsp
https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/EphemerisFiles/jup365_GalileanMoons_1970_2025.bsp
https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/EphemerisFiles/mar097_MarsSystem_1970_2025.bsp
https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/EphemerisFiles/nep081_NeptuneMoons_1970_2025.bsp
https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/EphemerisFiles/plu058_PlutoSystem_1970_2025.bsp
https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/EphemerisFiles/sat427_SaturnSystem_1970_2025.bsp
https://github.com/nicokuijpers/SolarSystemSimulator/blob/master/EphemerisFiles/ura111_UranusSystem_1970_2025.bsp


Library required for reading models from file: jimObjModelImporterJFX.jar, see http://www.InteractiveMesh.com

Copyright (c) 2017 Nico Kuijpers  

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
Software is furnished to do so, subject to the following conditions: 

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
