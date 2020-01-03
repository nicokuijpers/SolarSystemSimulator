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
package ephemeris;

import particlesystem.Particle;

import java.util.*;

/**
 *
 * @author Nico Kuijpers
 */
public class SolarSystemParameters {
    
    /**
     * Astronomical unit. Defined as distance between Sun and Earth in m
     * https://en.wikipedia.org/wiki/Astronomical_unit
     */
    // public static final double ASTRONOMICALUNIT = 1.49597870700E11;
    // From DECheck.java
    public static final double ASTRONOMICALUNIT = 1.49597870691E11;
    
    // Number of days per century
    // https://www.grc.nasa.gov/www/k-12/Numbers/Math/Mathematical_Thinking/calendar_calculations.htm
    private static final double nrDaysPerCentury = 36524.25;

    // Speed of light in m/s
    // https://en.wikipedia.org/wiki/Speed_of_light
    public static final double SPEEDOFLIGHT = 299792458.0;

    // Axial tilt of the Earth in degrees
    // https://en.wikipedia.org/wiki/Axial_tilt
    public static final double AXIALTILT = 23.43678;
    
    /**
     * Masses of sun, planets, asteroids, comets, and moons of solar system in kg.
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/sunfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/mercuryfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/venusfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/earthfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/moonfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/marsfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/jupiterfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/saturnfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/uranusfact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/neptunefact.html
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/plutofact.html
     * https://en.wikipedia.org/wiki/Eris_(dwarf_planet)
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/chironfact.html: 2E18 - 1E19
     * https://nl.wikipedia.org/wiki/2060_Chiron: 2.7E18
     * https://de.wikipedia.org/wiki/(2060)_Chiron: 2.4E18 - 3.0E18
     * https://en.wikipedia.org/wiki/Ceres_(dwarf_planet)
     * https://en.wikipedia.org/wiki/2_Pallas
     * https://en.wikipedia.org/wiki/3_Juno
     * https://en.wikipedia.org/wiki/4_Vesta
     * https://en.wikipedia.org/wiki/433_Eros
     * https://en.wikipedia.org/wiki/Halley%27s_Comet
     * https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19730024004.pdf (page 2)
     * https://en.wikipedia.org/wiki/67P/Churyumov–Gerasimenko
     * Mass of comet C/1995 O1 (Hale-Bopp) is unknown. Estimated mass 1.0E13 kg
     * Mass of comet D/1993 F2-A (Shoemaker-Levy 9) is unknown. Estimated mass 1.0E13 kg
     * Mass of asteroid 3122 Florence is unknown. Estimated mass 1.0E13 kg
     * Mass of Ultima Thule is computed frorm standard gravitational parameter mu = G*M
     * https://en.wikipedia.org/wiki/Io_(moon)
     * https://en.wikipedia.org/wiki/Europa_(moon)
     * https://en.wikipedia.org/wiki/Ganymede_(moon)
     * https://en.wikipedia.org/wiki/Callisto_(moon)
     */
    private static final double SUNMASS       =  1988500E24;
    private static final double MERCURYMASS   =        0.33011E24;
    private static final double VENUSMASS     =        4.8675E24;
    private static final double EARTHMASS     =        5.9723E24; 
    private static final double MOONMASS      =        0.07346E24;
    private static final double MARSMASS      =        0.64171E24;
    private static final double JUPITERMASS   =     1898.19E24;
    private static final double SATURNMASS    =      568.34E24;
    private static final double URANUSMASS    =       86.813E24;
    private static final double NEPTUNEMASS   =      102.413E24;
    private static final double PLUTOMASS     =        0.01303E24;
    private static final double ERISMASS      =        1.66E22;
    private static final double CHIRONMASS    =        2.7E18;  // 2.4E18 - 3.0E18
    private static final double CERESMASS     =        9.393E20;
    private static final double PALLASMASS    =        2.11E20;
    private static final double JUNOMASS      =        2.67E19;
    private static final double VESTAMASS     =        2.59076E20;
    private static final double EROSMASS      =        6.687E15;
    private static final double HALLEYMASS    =        2.2E14;
    private static final double ENCKEMASS     =        9.2E15; // nominal model
    private static final double CGMASS        =        9.982E12;
    private static final double HBMASS        =        1.0E13; // estimated
    private static final double SL9MASS       =        1.0E13; // estimated
    private static final double FLORENCEMASS  =        1.0E13; // estimated
    private static final double UTMASS        =
            (Math.pow(2.9591220828559093E-04,1.0/3.0)*ASTRONOMICALUNIT/
                    Math.pow(86400,2.0))/ Particle.GRAVITATIONALCONSTANT;
    /*
    private static final double IOMASS        =        8.931938E22;
    private static final double EUROPAMASS    =        4.799844E22;
    private static final double GANYMEDEMASS  =        1.4819E23;
    private static final double CALLISTOMASS  =        1.075938E23;
    */
    // https://ssd.jpl.nasa.gov/horizons.cgi#results
    private static final double IOMASS        =        8.933E22;
    private static final double EUROPAMASS    =        4.797E22;
    private static final double GANYMEDEMASS  =        1.482E23;
    private static final double CALLISTOMASS  =        1.076E23;
    private static final double MIMASMASS     =        3.75E19;    //     3.75  10^19 kg
    private static final double ENCELADUSMASS =        1.0805E20;  //    10.805 10^19 kg
    private static final double TETHYSMASS    =        6.176E20;   //    61.76 +- 0.11  10^19 kg
    private static final double DIONEMASS     =        1.09572E21; //   109.572 10^19 kg
    private static final double RHEAMASS      =        2.309E21;   //   230.9   10^19 kg
    private static final double TITANMASS     =        1.34553E23; // 13455.3   10^19 kg
    private static final double HYPERIONMASS  =        1.08E19;    //     1.08 +- 0.52 10^19 kg
    private static final double IAPETUSMASS   =        1.8059E21;  //   180.59  10^19 kg
    private static final double MIRANDAMASS   =        6.59E19;    //     0.659 +- 0.075 10^20 kg
    private static final double ARIELMASS     =        1.353E21;   //    13.53 +- 1.20 10^20 kg
    private static final double UMBRIELMASS   =        1.172E21;   //    11.72 +- 1.35 10^20 kg
    private static final double TITANIAMASS   =        3.527E21;   //    35.27 +- 0.90 10^20 kg
    private static final double OBERONMASS    =        3.014E21;   //    30.14 +- 0.75 10^20 kg
    private static final double TRITONMASS    =        2.147E22;   //   214.7  +- 0.7  10^20 kg
    
    /** 
     * Standard gravitational parameter mu = G*M in m3/s2.
     * The value of mu is known to greater accuracy than either G or M.
     * See https://en.wikipedia.org/wiki/Standard_gravitational_parameter
     */
    /*
    private static final double SUNMU = 1.327124400189E20;
    private static final double MERCURYMU = 2.20329E13;
    private static final double VENUSMU = 3.248599E14;
    private static final double EARTHMU = 3.9860044189E14;
    private static final double MOONMU = 4.90486959E12;
    private static final double MARSMU = 4.2828372E13;
    private static final double CERESMU = 6.26325E10;
    private static final double JUPITERMU = 1.266865349E17;
    private static final double SATURNMU = 3.79311879E16;
    private static final double URANUSMU = 5.7939399E15;
    private static final double NEPTUNEMU = 6.8365299E15;
    private static final double PLUTOMU = 8.719E11;
    private static final double ERISMU = 1.1089E12;
    */
    /** 
     * Standard gravitational parameter mu = G*M in m3/s2.
     * The value of mu is known to greater accuracy than either G or M.
     * See https://en.wikipedia.org/wiki/Standard_gravitational_parameter
     * Parameters defined below are from
     * ftp://ssd.jpl.nasa.gov/pub/ssd/Horizons_doc.pdf (see page 52)
     * Note that GM-values are given in km3/s2; multiply with 1E09 for m3/s2
     * REMARK: PLUTOMU differs greatly from G * PLUTOMASS!!
     * Eris: https://en.wikipedia.org/wiki/Standard_gravitational_parameter
     * Chiron: G * mass, mass estimated.
     * Ceres: https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=1
     * Pallas: https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2
     * Juno: G * mass; https://en.wikipedia.org/wiki/3_Juno
     * Vesta: https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=4
     * Eros: https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=433;old=0;orb=0;cov=0;log=0;cad=0#phys_par
     * Halley:
     * Encke: G * mass, mass estimated.
     * https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19730024004.pdf (page 2)
     * 67P/Churyumov–Gerasimenko: G * mass
     * C/1995 O1 (Hale-Bopp): G * mass with estimated mass 1.0E13 kg.
     * D/1993 F2-A (Shoemaker-Levy 9): G * mass with estimated mass 1.0E13 kg.
     * 3122 Florence: G * mass with estimated mass 1.0E13 kg.
     * Ultima Thule (HORIZONS) Keplerian GM =2.9591220828559093E-04 au^3/d^2
     * Io, Europa, Ganymade, and Callisto (Table 5, km^3 / s^2)
     * http://www.esa.int/gsp/ACT/doc/MAD/ACT-RPT-MAD-GTOC6-problem_stmt.pdf
     * Titan: https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Miranda, Arial, Umbriel, Titania, and Oberon: Table 2b from
     * J. Laskar and R.A. Jacobson, GUST86. An analytical ephemeris of the Uranian satellites,
     * Astron. Atrophys. 188, 212-224 (1987)
     * Triton: Table 5 from
     * R.A. Jacobson, The orbits of the Neptunian satellites and the orientation of the pole
     * of Neptune, The Astronomical Journal, 137:4322-4329, 2009 May
     */
    private static final double SUNMU      = 1.3271244001798698E20;
    private static final double MERCURYMU  = 2.2032080486417923E13;
    private static final double VENUSMU    = 3.2485859882645978E14;
    private static final double EARTHMU    = 3.9860043289693922E14;
    private static final double MOONMU     = 4.9028005821477636E12;
    private static final double MARSMU     = 4.2828314258067119E13;
    private static final double JUPITERMU  = 1.2671276785779600E17;
    private static final double SATURNMU   = 3.7940626061137281E16;
    private static final double URANUSMU   = 5.7945490070718741E15;
    private static final double NEPTUNEMU  = 6.8365340638792608E15;
    private static final double PLUTOMU    = 9.8160088770700440E11; // 8.719E11;
    private static final double ERISMU     = 1.1089E12; // wikipedia
    private static final double CERESMU    = 6.26284E10;// 6.26325E10 wikipedia
    private static final double PALLASMU   = 1.43E10; // 14.3 km3/s2
    private static final double VESTAMU    = 1.78E10; // 17.8 km3/s2
    private static final double EROSMU     = 4.463E05; // 4.463e-04 km3/s2
    private static final double UTMU       = Math.pow(2.9591220828559093E-04,1.0/3.0)*ASTRONOMICALUNIT/
                                              Math.pow(86400,2.0); // Converted to m3/s2;

    /*
    private static final double IOMU        = 5.959916E12;
    private static final double EUROPAMU    = 3.202739E12;
    private static final double GANYMEDEMU  = 9.887834E12;
    private static final double CALLISTOMU  = 7.179289E12;
    */
    // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/jup310.cmt
    private static final double IOMU        = 5.959924010272514E12; // 5.959924010272514E+03
    private static final double EUROPAMU    = 3.202739815114734E12; // 3.202739815114734E+03
    private static final double GANYMEDEMU  = 9.887819980080976E12; // 9.887819980080976E+03
    private static final double CALLISTOMU  = 7.179304867611079E12; // 7.179304867611079E+03

    /*
    private static final double MIMASMU     = 2.504E09;   //    2.504 km^3/s^2
    private static final double ENCELADUSMU = 7.211E09;   //    7.211 +- 0.011 km^3/s^2
    private static final double TETHYSMU    = 4.121E10;   //   41.21 +- 0.007 km^3/s^2
    private static final double DIONEMU     = 7.3113E10;  //   73.113 +- 0.02 km^3/s^2
    private static final double RHEAMU      = 1.5394E11;  //  153.94 +- 0.16 km^3/s^2
    private static final double TITANMU     = 8.97814E12; // 8978.14 +- 0.06 km^3/s^2
    private static final double HYPERIONMU  = 3.708E08;   //    0.3708 +- 0.02 km^3/s^2
    private static final double IAPETUSMU   = 1.2053E11;  //  120.53 +- 0.03 km^3/s^2
    */
    // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/sat425.cmt
    private static final double MIMASMU     = 2.503571968933995E09; // 2.503571968933995E+00
    private static final double ENCELADUSMU = 7.210561107781245E09; // 7.210561107781245E+00
    private static final double TETHYSMU    = 4.121342803076796E10; // 4.121342803076796E+01
    private static final double DIONEMU     = 7.311606189909810E10; // 7.311606189909810E+01
    private static final double RHEAMU      = 1.539416369781934E11; // 1.539416369781934E+02
    private static final double TITANMU     = 8.978137026107361E12; // 8.978137026107361E+03
    private static final double HYPERIONMU  = 3.708107681569874E08; // 3.708107681569874E-01
    private static final double IAPETUSMU   = 1.205273089601815E11; // 1.205273089601815E+02

    /*
    private static final double MIRANDAMU   = 4.4E09;     //    4.4 km^3/s^2
    private static final double ARIELMU     = 8.61E10;    //   86.1 km^3/s^2
    private static final double UMBRIELMU   = 8.4E10;     //   84.0 km^3/s^2
    private static final double TITANIAMU   = 2.3E11;     //  230.0 km^3/s^2
    private static final double OBERONMU    = 2.0E11;     //  200.0 km^3/s^2
    */
    // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/ura111.cmt
    private static final double MIRANDAMU   = 4.319516899232100E09; // 4.319516899232100E+00
    private static final double ARIELMU     = 8.346344431770477E10; // 8.346344431770477E+01
    private static final double UMBRIELMU   = 8.509338094489388E10; // 8.509338094489388E+01
    private static final double TITANIAMU   = 2.269437003741248E11; // 2.269437003741248E+02
    private static final double OBERONMU    = 2.053234302535623E11; // 2.053234302535623E+02

    // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/nep081.cmt
    private static final double TRITONMU    = 1.427598140725034E12; //  1427.6 km^3/s^2

    /**
     * Diameter of sun, planets, asteroids, comets, and moons of solar system in m.
     * https://en.wikipedia.org/wiki/Sun: diameter of the Sun 1.3914 million km
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/ : diameters of planets and moon
     * https://en.wikipedia.org/wiki/Eris_(dwarf_planet): mean radius 1163 +/- 6 km
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/chironfact.html: 148 to 208 km
     * https://nl.wikipedia.org/wiki/2060_Chiron: 233 +/- 13 km
     * https://de.wikipedia.org/wiki/(2060)_Chiron: 218 +/- 20 km
     * https://en.wikipedia.org/wiki/Ceres_(dwarf_planet): mean radius 473 km
     * https://en.wikipedia.org/wiki/2_Pallas: mean dimensions 512 +/- 6 km  
     * https://en.wikipedia.org/wiki/3_Juno: dimensions 233 km
     * https://en.wikipedia.org/wiki/4_Vesta: mean dimensions 525.4 +/- 0.2 km
     * https://en.wikipedia.org/wiki/433_Eros: mean diameter 16.84 +/- 0.06 km
     * https://en.wikipedia.org/wiki/Halley%27s_Comet: mean diameter 11 km
     * https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19730024004.pdf (page 2): radius 1.3 km
     * https://en.wikipedia.org/wiki/67P/Churyumov–Gerasimenko: largest diameter 4.1 km
     * https://en.wikipedia.org/wiki/Comet_Hale–Bopp: dimensions 40 - 80 km 
     * https://en.wikipedia.org/wiki/Comet_Shoemaker–Levy_9: diameter unknown
     * https://en.wikipedia.org/wiki/3122_Florence: maximum reported dimension 4.9 km
     * https://www.dw.com/en/nasa-faraway-mini-world-ultima-thule-is-snowman-shaped/a-46938428
     * Radius Io obtained from HORIZONS 1821.3 +- 0.2 km, diameter = 3642.6 km
     * Radius Europa obtained from HORIZONS 1565 +- 8 km, diameter = 3130 km
     * Radius Ganymede obtained from HORIZONS 2634 +- 10 km, diameter = 5268 km
     * Radius Callisto obtained from HORIZONS 2403 +- 5 km, diameter = 4806 km
     * Radius Mimas from HORIZONS 198.8 +- 1.5 km, diameter = 397.6 km
     * Radius Enceladus from HORIZONS 252.3 +- 0.6, diameter = 504.6 km
     * Radius Tethys from HORIZONS 536.3 +- 1.5, diameter = 1072.6 km
     * Radius Dione from HORIZONS 562.5 +- 5 , diameter = 1125 km
     * Radius Rhea from HORIZONS 764.5 +- 2 , diameter = 1529 km
     * Radius Titan from HORIZONS 2575.5 +- 2.0 km, diameter = 5151 km
     * Radius Hyperion from HORIZONS 133 +- 8, diameter = 266 km
     * Radius Iapetus from HORIZONS 734.5 +- 4.0 km, diameter = 1469 km
     * Radius Miranda from HORIZONS 240 x 234.2 x 232.9 km, diameter = 480 km
     * Radius Ariel from HORIZONS 581.1 x 577.9 x 577.7 km, diameter = 1162 km
     * Radius Umbriel from HORIZONS 584.7 +- 2.8 km, diameter = 1170 km
     * Radius Titania from HORIZONS 788.9 +- 1.8 km, diameter = 1578 km
     * Radius Oberon from HORIZONS 761.4 +- 2.6 km, diameter = 1523 km
     * Radius Triton from HORIZONS 1352.6 +- 2.4 km, diameter = 2705 km
     */
    private static final double SUNDIAMETER       =  1.3914E09;  // 1.3914 million km
    private static final double MERCURYDIAMETER   =  4.879E06;   //   4879 km
    private static final double VENUSDIAMETER     =  1.2104E07;  //  12104 km
    private static final double EARTHDIAMETER     =  1.2756E07;  //  12756 km 
    private static final double MOONDIAMETER      =  3.475E06;   //   3475 km
    private static final double MARSDIAMETER      =  6.792E06;   //   6792 km
    private static final double JUPITERDIAMETER   =  1.42984E08; // 142894 km
    private static final double SATURNDIAMETER    =  1.20536E08; // 120536 km
    private static final double URANUSDIAMETER    =  5.1118E07;  //  51118 km
    private static final double NEPTUNEDIAMETER   =  4.9528E07;  //  49528 km
    private static final double PLUTODIAMETER     =  2.370E06;   //   2370 km
    private static final double ERISDIAMETER      =  2.326E06;   //   2326 km
    private static final double CHIRONDIAMETER    =  2.33E05;    //    233 km
    private static final double CERESDIAMETER     =  9.46E05;    //    946 km
    private static final double PALLASDIAMETER    =  5.12E05;    //    512 km
    private static final double JUNODIAMETER      =  2.33E05;    //    233 km
    private static final double VESTADIAMETER     =  5.254E05;   //    525.4 km
    private static final double EROSDIAMETER      =  1.684E04;   //     16.84 km
    private static final double HALLEYDIAMETER    =  1.1E04;     //     11 km
    private static final double ENCKEDIAMETER     =  2.6E03;     //      2.6 km
    private static final double CGDIAMETER        =  4.1E03;     //      4.1 km
    private static final double HBDIAMETER        =  8.0E04;     //     80 km
    private static final double SL9DIAMETER       =  1.0E04;     //   estimated
    private static final double FLORENCEDIAMETER  =  4.9E03;     //      4.9 km
    private static final double UTDIAMETER        =  3.3E04;     //     33 km
    private static final double IODIAMETER        =  3.6426E06;  //   3642.6 km
    private static final double EUROPADIAMETER    =  3.130E06;   //   3130 km
    private static final double GANYMEDEDIAMETER  =  5.268E06;   //   5268 km
    private static final double CALLISTODIAMETER  =  4.806E06;   //   4806 km
    private static final double MIMASDIAMETER     =  3.976E05;   //    397.6 km
    private static final double ENCELADUSDIAMETER =  5.046E05;   //    504.6 km
    private static final double TETHYSDIAMETER    =  1.0726E06;  //   1072.6 km
    private static final double DIONEDIAMETER     =  1.125E06;   //   1125 km
    private static final double RHEADIAMETER      =  1.529E06;   //   1529 km
    private static final double TITANDIAMETER     =  5.151E06;   //   5151 km
    private static final double HYPERIONDIAMETER  =  2.66E05;    //    266 km
    private static final double IAPETUSDIAMETER   =  1.469E06;   //   1469 km
    private static final double MIRANDADIAMETER   =  4.80E05;    //    480 km
    private static final double ARIELDIAMETER     =  1.162E06;   //   1162 km
    private static final double UMBRIELDIAMETER   =  1.170E06;   //   1170 km
    private static final double TITANIADIAMETER   =  1.578E06;   //   1578 km
    private static final double OBERONDIAMETER    =  1.523E06;   //   1523 km
    private static final double TRITONDIAMETER    =  2.705E06;   //   2705 km

    /**
     * Ellipticity of oblate planets Jupiter, Saturn, Uranus, and Neptune
     * Values of ellipticity are obtained from Table 1 in
     * A.M. Hofmeister, R.E. Criss, and E.M. Criss,
     * Verified solutions for the gravitational attraction to an oblate spheroid:
     * Implications for planet mass and satellite orbits,
     * Planetary and Space Science 152 (2018) 68-81
     * https://doi.org/10.1016/j.pss.2018.01.005
     * https://www.sciencedirect.com/science/article/pii/S003206331730257X
     */
    private static final double JUPITERELLIPTICITY = 0.354;
    private static final double SATURNELLIPTICITY = 0.432;
    private static final double URANUSELLIPTICITY = 0.213;
    private static final double NEPTUNEELLIPTICITY = 0.184;

    /**
     * Equatorial radius in km of oblate planets Jupiter, Saturn, Uranus, and Neptune
     * Jupiter: https://en.wikipedia.org/wiki/Jupiter
     * Saturn: https://en.wikipedia.org/wiki/Saturn
     * Uranus: https://en.wikipedia.org/wiki/Uranus
     * Neptune: Table 4 in R.A. Jacobsen, J.E. Riedel, and A.H. Taylor,
     * The orbits of Triton and Nereid from spacecraft and Earthbased observations
     * Astronomy and Astrophysics 247, 565-575 (1991)
     * http://adsabs.harvard.edu/full/1991A%26A...247..565J
     *
    private static final double JUPITEREQUATORIALRADIUS = 71492; // 71,492 km
    private static final double SATURNEQUATORIALRADIUS  = 60268; // 60,268 km
    private static final double URANUSEQUATORIALRADIUS  = 25559; // 25,559 +/- 4 km
    private static final double NEPTUNEEQUATORIALRADIUS = 25225; // 25,225 km
    */

    /**
     * Gravitational parameter [m3/s2] of oblate planets Jupiter, Saturn, Uranus, and Neptune
     * https://ssd.jpl.nasa.gov/?gravity_fields_op
     *
     * Jupiter:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/jup310.cmt
     *  Jupiter      599    1.266865341960128E+08
     *
     * Saturn:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/sat425.cmt
     *  Saturn       699    3.793120627544314E+07
     *
     * Uranus:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/ura111.cmt
     *  Uranus       799    5.793951322279009E+06
     *
     * Neptune:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/nep081.cmt
     *  Neptune      899    6.835099502439672E+06
     */
    private static final double JUPITEROBLATEMU = 1.266865341960128E17; // 1.266865341960128E+08
    private static final double SATURNOBLATEMU  = 3.793120627544314E16; // 3.793120627544314E+07
    private static final double URANUSOBLATEMU  = 5.793951322279009E15; // 5.793951322279009E+06
    private static final double NEPTUNEOBLATEMU = 6.835099502439672E15; // 6.835099502439672E+06

    /**
     * Equatorial radius [m] of oblate planets Jupiter, Saturn, Uranus, and Neptune
     * https://ssd.jpl.nasa.gov/?gravity_fields_op
     *
     * Jupiter:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/jup310.cmt
     *  RADIUS    7.149200000000000E+04
     *
     * Saturn:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/sat425.cmt
     *  RADIUS    6.033000000000000E+04
     *
     * Uranus:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/ura111.cmt
     *  RADIUS   2.555900000000000E+04
     *
     * Neptune:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/nep081.cmt
     *  RADIUS   2.522500000000000E+04
     */
    private static final double JUPITEREQUATORIALRADIUS = 71492000; // 71492 km
    private static final double SATURNEQUATORIALRADIUS  = 60330000; // 60330 km
    private static final double URANUSEQUATORIALRADIUS  = 25559000; // 25559 km
    private static final double NEPTUNEEQUATORIALRADIUS = 25225000; // 25225 km

    /**
     * Zonal coefficients [-] of oblate planets Jupiter, Saturn, Uranus, and Neptune
     * https://ssd.jpl.nasa.gov/?gravity_fields_op
     *                   Jupiter          Saturn          Uranus          Neptune
     *  J2 x 10^6  14695.62 ± 0.29  16290.71 ± 0.27  3510.68 ± 0.70  3408.43 ± 4.50
     *  J4 x 10^6   -591.31 ± 2.06   -935.83 ± 2.77   -34.17 ± 1.30   -33.40 ± 2.90
     *  J6 x 10^6     20.78 ± 4.87     86.14 ± 9.64
     *  J8 x 10^6                     -10.
     *
     *  Jupiter:
     *   https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/jup310.cmt
     *   J502     1.469562477069651E-02   J503     0.000000000000000E+00
     *   J504    -5.913138887463315E-04   J506     2.077510523748891E-05
     *
     *  Saturn:
     *   https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/sat425.cmt
     *   J602        1.629056318614283E-02   J603        8.021547362926897E-08
     *   J604       -9.352748124585591E-04   J605       -1.294023676822416E-07
     *   J606        8.640173180043068E-05   J607        3.627869335910333E-07
     *   J608       -1.449699463354960E-05
     *
     *  Uranus:
     *   https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/ura111.cmt
     *   J702     3.510685384697763E-03   J704    -3.416639735448987E-05
     *
     *  Neptune:
     *   https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/nep081.cmt
     *   J802     3.408428530717952E-03   J804    -3.339891759006578E-05
     */
    private static final double[] JUPITERZONALCOEFFICIENTS =
            new double[]{0.0, 0.0, 1.469562477069651E-02, 0.0,  -5.913138887463315E-04,
                    0.0, 2.077510523748891E-05};
    private static final double[] SATURNZONALCOEFFICIENTS =
            new double[]{0.0, 0.0, 1.629056318614283E-02, 8.021547362926897E-08,
                    -9.352748124585591E-04, -1.294023676822416E-07, 8.640173180043068E-05,
                     3.627869335910333E-07, -1.449699463354960E-05};
    private static final double[] URANUSZONALCOEFFICIENTS =
            new double[]{0.0, 0.0, 3.510685384697763E-03,  0.0, -3.416639735448987E-05};
    private static final double[] NEPTUNEZONALCOEFFICIENTS =
            new double[]{0.0, 0.0, 3.408428530717952E-03,  0.0, -3.339891759006578E-05};

    /**
     * Right ascension and declination of z-axis of oblate planets Jupiter, Saturn,
     * Uranus, and Neptune
     * https://ssd.jpl.nasa.gov/?gravity_fields_op
     *
     * Jupiter:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/jup310.cmt
     *  POLTIM   2.451545000000000E+06 JED
     *  ZACPL5   2.680570781451589E+02 degrees
     *  ZDEPL5   6.449582320291580E+01 degrees
     *  DACPL5  -6.554328185586419E-03 degrees/century
     *  DDEPL5   2.476496988122852E-03 degrees/century
     *
     * Saturn:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/sat425.cmt
     *  POLTIM   2.451545000000000E+06 JED
     *  ZACPL6   4.059381052563325E+01 degrees
     *  ZDEPL6   8.353456912851105E+01 degrees
     *  DACPL6  -5.022619663580526E-02 degrees/century
     *  DDEPL6  -5.776335861947523E-03 degrees/century
     *
     * Uranus:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/ura111.cmt
     *  POLTIM   2.451545000000000E+06 JED
     *  ZACPL7   7.730990252631723E+01 degrees
     *  ZDEPL7   1.517245819840212E+01 degrees
     *  DACPL7   1.734056155021870E-04 degrees/century
     *  DDEPL7   1.903682577001328E-05 degrees/century
     *
     * Neptune:
     *  https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/nep081.cmt
     *  POLTIM   2.447763500000000E+06 JED
     *  ZACPL8   2.994608612607558E+02 degrees
     *  ZDEPL8   4.340481079071409E+01 degrees
     *  DACPL8   0.000000000000000E+00 degrees/century
     *  DDEPL8   0.000000000000000E+00 degrees/century
     */
    private static final double[] JUPITERZAXISPARAMETERS =
            new double[]{2.451545000000000E+06, 2.680570781451589E+02, 6.449582320291580E+01,
                        -6.554328185586419E-03, 2.476496988122852E-03};
    private static final double[] SATURNZAXISPARAMETERS =
            new double[]{2.451545000000000E+06, 4.059381052563325E+01, 8.353456912851105E+01,
                        -5.022619663580526E-02, -5.776335861947523E-03};
    private static final double[] URANUSZAXISPARAMETERS =
            new double[]{2.451545000000000E+06, 7.730990252631723E+01, 1.517245819840212E+01,
                         1.734056155021870E-04, 1.903682577001328E-05};
    private static final double[] NEPTUNEZAXISPARAMETERS =
            new double[]{2.447763500000000E+06, 2.994608612607558E+02, 4.340481079071409E+01,
                         0.0, 0.0};

/*    
=====================================================================
  These data are to be used as described in the related document
  titled "Keplerian Elements for Approximate Positions of the
  Major Planets" by E.M. Standish (JPL/Caltech) available from
  the JPL Solar System Dynamics web site (http://ssd.jpl.nasa.gov/).
=====================================================================


Table 1.

Keplerian elements and their rates, with respect to the mean ecliptic
and equinox of J2000, valid for the time-interval 1800 AD - 2050 AD.

               a              e               I                L            long.peri.      long.node.
           AU, AU/Cy     rad, rad/Cy     deg, deg/Cy      deg, deg/Cy      deg, deg/Cy     deg, deg/Cy
-----------------------------------------------------------------------------------------------------------
Mercury   0.38709927      0.20563593      7.00497902      252.25032350     77.45779628     48.33076593
          0.00000037      0.00001906     -0.00594749   149472.67411175      0.16047689     -0.12534081
Venus     0.72333566      0.00677672      3.39467605      181.97909950    131.60246718     76.67984255
          0.00000390     -0.00004107     -0.00078890    58517.81538729      0.00268329     -0.27769418
EM Bary   1.00000261      0.01671123     -0.00001531      100.46457166    102.93768193      0.0
          0.00000562     -0.00004392     -0.01294668    35999.37244981      0.32327364      0.0
Mars      1.52371034      0.09339410      1.84969142       -4.55343205    -23.94362959     49.55953891
          0.00001847      0.00007882     -0.00813131    19140.30268499      0.44441088     -0.29257343
Jupiter   5.20288700      0.04838624      1.30439695       34.39644051     14.72847983    100.47390909
         -0.00011607     -0.00013253     -0.00183714     3034.74612775      0.21252668      0.20469106
Saturn    9.53667594      0.05386179      2.48599187       49.95424423     92.59887831    113.66242448
         -0.00125060     -0.00050991      0.00193609     1222.49362201     -0.41897216     -0.28867794
Uranus   19.18916464      0.04725744      0.77263783      313.23810451    170.95427630     74.01692503
         -0.00196176     -0.00004397     -0.00242939      428.48202785      0.40805281      0.04240589
Neptune  30.06992276      0.00859048      1.77004347      -55.12002969     44.96476227    131.78422574
          0.00026291      0.00005105      0.00035372      218.45945325     -0.32241464     -0.00508664
Pluto    39.48211675      0.24882730     17.14001206      238.92903833    224.06891629    110.30393684
         -0.00031596      0.00005170      0.00004818      145.20780515     -0.04062942     -0.01183482
*/   

    private static final double[] MERCURYORBITPARS1800AD2050AD = new double[]
    { 0.38709927,  0.20563593,  7.00497902,    252.25032350,  77.45779628,  48.33076593,
      0.00000037,  0.00001906, -0.00594749, 149472.67411175,   0.16047689,  -0.12534081,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};

    private static final double[] VENUSORBITPARS1800AD2050AD = new double[]
    { 0.72333566,  0.00677672,  3.39467605,    181.97909950, 131.60246718,  76.67984255,
      0.00000390, -0.00004107, -0.00078890,  58517.81538729,   0.00268329,  -0.27769418,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};
    
    private static final double[] EARTHORBITPARS1800AD2050AD = new double[]
    { 1.00000261,  0.01671123, -0.00001531,    100.46457166, 102.93768193,   0.0,
      0.00000562, -0.00004392, -0.01294668,  35999.37244981,   0.32327364,   0.0,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};
    
    private static final double[] MARSORBITPARS1800AD2050AD = new double[]
    { 1.52371034,  0.09339410,  1.84969142,     -4.55343205, -23.94362959,  49.55953891,
      0.00001847,  0.00007882, -0.00813131,  19140.30268499,   0.44441088,  -0.29257343,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};

    private static final double[] JUPITERORBITPARS1800AD2050AD = new double[]
    { 5.20288700,  0.04838624,  1.30439695,     34.39644051,  14.72847983, 100.47390909,
     -0.00011607, -0.00013253, -0.00183714,   3034.74612775,   0.21252668,   0.20469106,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};
    
    private static final double[] SATURNORBITPARS1800AD2050AD = new double[]
    { 9.53667594,  0.05386179,  2.48599187,     49.95424423,  92.59887831, 113.66242448,
     -0.00125060, -0.00050991,  0.00193609,   1222.49362201,  -0.41897216,  -0.28867794,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};
    
    private static final double[] URANUSORBITPARS1800AD2050AD = new double[]
    {19.18916464,  0.04725744,  0.77263783,    313.23810451, 170.95427630, 74.01692503,
     -0.00196176, -0.00004397, -0.00242939,    428.48202785,   0.40805281,  0.04240589,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};
    
    private static final double[] NEPTUNEORBITPARS1800AD2050AD = new double[]
    {30.06992276,  0.00859048,  1.77004347,    -55.12002969,  44.96476227, 131.78422574,
      0.00026291,  0.00005105,  0.00035372,    218.45945325,  -0.32241464,  -0.00508664,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};

    private static final double[] PLUTOORBITPARS1800AD2050AD = new double[]
    {39.48211675,  0.24882730, 17.14001206,    238.92903833, 224.06891629, 110.30393684,
     -0.00031596,  0.00005170,  0.00004818,    145.20780515,  -0.04062942,  -0.01183482,
      0.00000000,  0.00000000,  0.00000000,      0.00000000};
        
        
/*    
=====================================================================
  These data are to be used as described in the related document
  titled "Keplerian Elements for Approximate Positions of the
  Major Planets" by E.M. Standish (JPL/Caltech) available from
  the JPL Solar System Dynamics web site (http://ssd.jpl.nasa.gov/).
=====================================================================


Table 2a.

Keplerian elements and their rates, with respect to the mean ecliptic and equinox of J2000,
valid for the time-interval 3000 BC -- 3000 AD.  NOTE: the computation of M for Jupiter through
Pluto *must* be augmented by the additional terms given in Table 2b (below).

               a              e               I                L            long.peri.      long.node.
           AU, AU/Cy     rad, rad/Cy     deg, deg/Cy      deg, deg/Cy      deg, deg/Cy     deg, deg/Cy
------------------------------------------------------------------------------------------------------
Mercury   0.38709843      0.20563661      7.00559432      252.25166724     77.45771895     48.33961819
          0.00000000      0.00002123     -0.00590158   149472.67486623      0.15940013     -0.12214182
Venus     0.72332102      0.00676399      3.39777545      181.97970850    131.76755713     76.67261496
         -0.00000026     -0.00005107      0.00043494    58517.81560260      0.05679648     -0.27274174
EM Bary   1.00000018      0.01673163     -0.00054346      100.46691572    102.93005885     -5.11260389
         -0.00000003     -0.00003661     -0.01337178    35999.37306329      0.31795260     -0.24123856
Mars      1.52371243      0.09336511      1.85181869       -4.56813164    -23.91744784     49.71320984
          0.00000097      0.00009149     -0.00724757    19140.29934243      0.45223625     -0.26852431
Jupiter   5.20248019      0.04853590      1.29861416       34.33479152     14.27495244    100.29282654
         -0.00002864      0.00018026     -0.00322699     3034.90371757      0.18199196      0.13024619
Saturn    9.54149883      0.05550825      2.49424102       50.07571329     92.86136063    113.63998702
         -0.00003065     -0.00032044      0.00451969     1222.11494724      0.54179478     -0.25015002
Uranus   19.18797948      0.04685740      0.77298127      314.20276625    172.43404441     73.96250215
         -0.00020455     -0.00001550     -0.00180155      428.49512595      0.09266985      0.05739699
Neptune  30.06952752      0.00895439      1.77005520      304.22289287     46.68158724    131.78635853
          0.00006447      0.00000818      0.00022400      218.46515314      0.01009938     -0.00606302
Pluto    39.48686035      0.24885238     17.14104260      238.96535011    224.09702598    110.30167986
          0.00449751      0.00006016      0.00000501      145.18042903     -0.00968827     -0.00809981
------------------------------------------------------------------------------------------------------



Table 2b.

Additional terms which must be added to the computation of M
for Jupiter through Pluto, 3000 BC to 3000 AD, as described
in the related document.

                b             c             s            f 
---------------------------------------------------------------
Jupiter   -0.00012452    0.06064060   -0.35635438   38.35125000
Saturn     0.00025899   -0.13434469    0.87320147   38.35125000
Uranus     0.00058331   -0.97731848    0.17689245    7.67025000
Neptune   -0.00041348    0.68346318   -0.10162547    7.67025000
Pluto     -0.01262724
---------------------------------------------------------------
*/
    
    /**
     * Keplerian elements and their rates for Mercury
     */
    private static final double[] MERCURYORBITPARS = new double[]
    {0.38709843, 0.20563661,  7.00559432,    252.25166724,   77.45771895, 48.33961819,
     0.00000000, 0.00002123, -0.00590158, 149472.67486623,    0.15940013, -0.12214182,
     0.00000000, 0.00000000,  0.00000000,      0.00000000};
    
    /**
     * Keplerian elements and their rates for Venus
     */
    private static final double[] VENUSORBITPARS = new double[]
    {0.72332102,  0.00676399, 3.39777545,   181.97970850, 131.76755713, 76.67261496,
    -0.00000026, -0.00005107, 0.00043494, 58517.81560260,   0.05679648, -0.27274174,
     0.00000000,  0.00000000, 0.00000000,     0.00000000};
    
    /**
     * Keplerian elements and their rates for Earth
     * Parameters for earth instead of Earth-Moon center of gravity 
     * https://nssdc.gsfc.nasa.gov/planetary/factsheet/earthfact.html
     * Earth Mean Orbital Elements (J2000)
     *  Semimajor axis (AU)                  1.00000011  
     *  Orbital eccentricity                 0.01671022   
     *  Orbital inclination (deg)            0.00005  
     *  Longitude of ascending node (deg)  -11.26064  
     *  Longitude of perihelion (deg)      102.94719  
     *  Mean Longitude (deg)               100.46435
     */
    // Earth-Moon center of gravity
    private static final double[] EARTHORBITPARS = new double[] // Earth-Moon center of gravity
    {1.00000018,  0.01673163, -0.00054346,   100.46691572,  102.93005885, -5.11260389,
    // {1.00000011,  0.01671022,  0.00005,      100.46435,   102.94719,   -11.26064,
    -0.00000003, -0.00003661, -0.01337178, 35999.37306329,  0.31795260, -0.24123856,
     0.00000000,  0.00000000,  0.00000000,     0.00000000};
    
    /**
     * Keplerian elements and their rates for Mars
     */
    private static final double[] MARSORBITPARS = new double[]
    {1.52371243, 0.09336511,  1.85181869,    -4.56813164, -23.91744784, 49.71320984,
     0.00000097, 0.00009149, -0.00724757, 19140.29934243,   0.45223625, -0.26852431,
     0.00000000, 0.00000000,  0.00000000,     0.00000000};
    
    /**
     * Keplerian elements and their rates for Jupiter
     */
    private static final double[] JUPITERORBITPARS = new double[]
    {5.20248019, 0.04853590,  1.29861416,   34.33479152, 14.27495244, 100.29282654,
    -0.00002864, 0.00018026, -0.00322699, 3034.90371757,  0.18199196,   0.13024619,
    -0.00012452, 0.06064060, -0.35635438,   38.35125000};
    
    
    /**
     * Keplerian elements and their rates for Saturn
     */
    private static final double[] SATURNORBITPARS = new double[]
    {9.54149883,  0.05550825, 2.49424102,   50.07571329, 92.86136063, 113.63998702,
    -0.00003065, -0.00032044, 0.00451969, 1222.11494724,  0.54179478,  -0.25015002,
     0.00025899, -0.13434469, 0.87320147,   38.35125000};
    
    /**
     * Keplerian elements and their rates for Uranus
     */
    private static final double[] URANUSORBITPARS = new double[]
    {19.18797948,  0.04685740,  0.77298127, 314.20276625, 172.43404441, 73.96250215,
     -0.00020455, -0.00001550, -0.00180155, 428.49512595,   0.09266985,  0.05739699,
      0.00058331, -0.97731848,  0.17689245,   7.67025000};
    
    /**
     * Keplerian elements and their rates for Neptune
     */
    private static final double[] NEPTUNEORBITPARS = new double[]
    {30.06952752, 0.00895439,  1.77005520, 304.22289287, 46.68158724, 131.78635853,
      0.00006447, 0.00000818,  0.00022400, 218.46515314,  0.01009938,  -0.00606302,
     -0.00041348, 0.68346318, -0.10162547,   7.67025000};
    
    /**
     * Keplerian elements and their rates for Pluto
     */
    private static final double[] PLUTOORBITPARS = new double[]
    {39.48686035, 0.24885238, 17.14104260, 238.96535011, 224.09702598, 110.30167986,
      0.00449751, 0.00006016,  0.00000501, 145.18042903,  -0.00968827,  -0.00809981,
     -0.01262724, 0.00000000,  0.00000000,   0.00000000};
    
    /**
     * Keplerian orbital parameters for dwarf planet 136199 Eris.
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=136199
     * https://en.wikipedia.org/wiki/Eris_(dwarf_planet)
     * Eris is the most massive and second-largest dwarf planet known
     * in the Solar System.
     */
    private static final double axisEris = 67.64968008508858; // [au]
    private static final double eccentricityEris = 0.4417142619088136; // [-]
    private static final double inclinationEris = 44.20390955432094; // [degrees]
    private static final double argPerihelionEris = 151.5223022346903; // [degrees]
    private static final double longNodeEris = 35.87791199490014	; // [degrees]
    private static final double perihelionPassageEris = 2545575.799683113451; // [JED]
    private static final double meanMotionEris = 0.001771354370292503; // [degrees/day]
    private static final double orbitalPeriodEris = 203234.3194775608; // [days]
    private static final double[] ERISORBITPARS = new double[]
    {axisEris, eccentricityEris, inclinationEris, argPerihelionEris, longNodeEris,
     perihelionPassageEris, meanMotionEris};
    
    /**
     * Keplerian orbital parameters for dwarf planet 2060 Chiron or 95P/Chiron.
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2060
     * https://en.wikipedia.org/wiki/2060_Chiron
     * Chiron was discovered on 1 November 1977 by Charles Kowal from images 
     * taken on 18 October at Palomar Observatory.
     * Chiron's orbit was found to be highly eccentric (0.37), with perihelion 
     * just inside the orbit of Saturn and aphelion just outside the perihelion 
     * of Uranus (it does not reach the average distance of Uranus, however). 
     * According to the program Solex, Chiron's closest approach to Saturn in 
     * modern times was around May 720, when it came within 30.5±2.0 million km 
     * of Saturn. During this passage Saturn's gravity caused Chiron's semi-major
     * axis to decrease from 14.55±0.12 AU[14] to 13.7 AU.[3] It does not come 
     * nearly as close to Uranus; Chiron crosses Uranus's orbit where the latter
     * is farther than average from the Sun. 
     * Chiron attracted considerable interest because it was the first object 
     * discovered in such an orbit, well outside the asteroid belt. Chiron is 
     * classified as a centaur, the first of a class of objects orbiting between
     * the outer planets. Chiron is a Saturn–Uranus object because its perihelion
     * lies in Saturn's zone of control and its aphelion lies in that of 
     * Uranus. Centaurs are not in stable orbits and will be removed by gravitational
     * perturbation by the giant planets over a period of millions of years, 
     * moving to different orbits or leaving the Solar System altogether. Chiron
     * is probably a refugee from the Kuiper belt and will probably become a 
     * short-period comet in about a million years. Chiron came to perihelion 
     * (closest point to the Sun) in 1996.
     */
    private static final double axisChiron = 13.64821600709919; // [au]
    private static final double eccentricityChiron = 0.3822544351242399; // [-]
    private static final double inclinationChiron = 6.949678708401436; // [degrees]
    private static final double argPerihelionChiron = 339.6766969686663; // [degrees]
    private static final double longNodeChiron = 209.200869875238; // [degrees]
    private static final double perihelionPassageChiron = 2450143.772120038983; // [JED]
    private static final double meanMotionChiron = 0.01954745593835608	; // [degrees/day]
    private static final double orbitalPeriodChiron = 18416.71883723789; // [days]
    private static final double[] CHIRONORBITPARS = new double[]
    {axisChiron, eccentricityChiron, inclinationChiron, argPerihelionChiron, longNodeChiron,
     perihelionPassageChiron, meanMotionChiron};
    
    /**
     * Keplerian orbital parameters for asteroid 1 Ceres.
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=1
     */
    private static final double axisCeres = 2.767409329208225; // [au]
    private static final double eccentricityCeres = 0.07560729117115973; // [-]
    private static final double inclinationCeres = 10.59321706277403; // [degrees]
    private static final double argPerihelionCeres = 73.02374264688446	; // [degrees]
    private static final double longNodeCeres = 80.3088826123586; // [degrees]
    private static final double perihelionPassageCeres = 2458236.411182414352; // [JED]
    private static final double meanMotionCeres = 0.2140888123385267; // [degrees/day]
    private static final double orbitalPeriodCeres = 1681.545131049408; // [days]
    private static final double[] CERESORBITPARS = new double[]
    {axisCeres, eccentricityCeres, inclinationCeres, argPerihelionCeres, longNodeCeres,
     perihelionPassageCeres, meanMotionCeres}; 
    
    /**
     * Keplerian orbital parameters for asteroid 2 Pallas
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2
     * https://en.wikipedia.org/wiki/2_Pallas
     */
    private static final double axisPallas = 2.773085152812061; // [au]
    private static final double eccentricityPallas = 0.2305974109006172; // [-]
    private static final double inclinationPallas = 34.83791913233102; // [degrees]
    private static final double argPerihelionPallas = 309.9915581445374; // [degrees]
    private static final double longNodePallas = 173.0871774252975; // [degrees]
    private static final double perihelionPassagePallas = 2458320.736325116834; // [JED]
    private static final double meanMotionPallas = 0.213431868021857; // [degrees/day]
    private static final double orbitalPeriodPallas = 1686.720935053304; // [days]
    private static final double[] PALLASORBITPARS = new double[]
    {axisPallas, eccentricityPallas, inclinationPallas, argPerihelionPallas, longNodePallas,
     perihelionPassagePallas, meanMotionPallas}; 
    
    /**
     * Keplerian orbital parameters for asteroid 3 Juno
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3
     * https://en.wikipedia.org/wiki/3_Juno
     */
    private static final double axisJuno = 2.668531209360437; // [au]
    private static final double eccentricityJuno = 0.256853452328373; // [-]
    private static final double inclinationJuno = 12.98996127586185; // [degrees]
    private static final double argPerihelionJuno = 248.2064931516843; // [degrees]
    private static final double longNodeJuno = 169.8582922221972; // [degrees]
    private static final double perihelionPassageJuno = 2458446.171166688112; // [JED]
    private static final double meanMotionJuno = 0.2260974396170018; // [degrees/day]
    private static final double orbitalPeriodJuno = 1592.233864345491; // [days]
    private static final double[] JUNOORBITPARS = new double[]
    {axisJuno, eccentricityJuno, inclinationJuno, argPerihelionJuno, longNodeJuno,
     perihelionPassageJuno, meanMotionJuno}; 
    
    /**
     * Keplerian orbital parameters for asteroid 4 Vesta
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=4
     * https://en.wikipedia.org/wiki/4_Vesta
     */
    private static final double axisVesta = 2.361777559799509; // [au]
    private static final double eccentricityVesta = 0.08915261042902074; // [-]
    private static final double inclinationVesta = 7.140019358926029; // [degrees]
    private static final double argPerihelionVesta = 150.9430865320649; // [degrees]
    private static final double longNodeVesta = 103.8358792056089; // [degrees]
    private static final double perihelionPassageVesta = 2458248.301104802767; // [JED]
    private static final double meanMotionVesta = 0.2715473607287919; // [degrees/day]
    private static final double orbitalPeriodVesta = 1325.735588200211; // [days]
    private static final double[] VESTAORBITPARS = new double[]
    {axisVesta, eccentricityVesta, inclinationVesta, argPerihelionVesta, longNodeVesta,
     perihelionPassageVesta, meanMotionVesta}; 
    
    /**
     * Keplerian orbital parameters for asteroid 433 Eros
     * https://ssd.jpl.nasa.gov/sbdb_query.cgi
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=433
     */
    private static final double axisEros = 1.457940027169433; // [au]
    private static final double eccentricityEros = 0.2225889698361087; // [-]
    private static final double inclinationEros = 10.82759100791667; // [degrees]
    private static final double argPerihelionEros = 178.8165910772738; // [degrees]
    private static final double longNodeEros = 304.3221633760257; // [degrees]
    private static final double perihelionPassageEros = 2457873.186399170510; // [JED]
    private static final double meanMotionEros = 0.559879523918286; // [degrees/day]
    private static final double orbitalPeriodEros = 642.9954742416008; // [days]
    private static final double[] EROSORBITPARS = new double[]
    {axisEros, eccentricityEros, inclinationEros, argPerihelionEros, longNodeEros,
     perihelionPassageEros, meanMotionEros};
    
    
    /**
     * JPL/HORIZONS                      1P/Halley                2017-May-28 08:05:31
     * Rec #:900033 (+COV)   Soln.date: 2001-Aug-02_13:51:39   # obs: 7428 (1835-1994)
     *
     * IAU76/J2000 helio. ecliptic osc. elements (au, days, deg., period=Julian yrs): 
     *
     * EPOCH=  2449400.5 ! 1994-Feb-17.0000000 (TDB)    RMSW= n.a.                  
     *  EC= .9671429084623044   QR= .5859781115169086   TP= 2446467.3953170511      
     *  OM= 58.42008097656843   W= 111.3324851045177    IN= 162.2626905791606       
     *  A= 17.83414429255373    MA= 38.384264476436     ADIST= 35.08231047359055    
     *  PER= 75.315892782197    N= .013086564           ANGMOM= .01846886           
     *  DAN= 1.77839            DDN= .8527              L= 306.1250589              
     *  B= 16.4859355           MOID= .0637815          TP= 1986-Feb-05.8953170511  
     *
     * Keplerian orbital parameters for comet 1P/Halley
     * https://ssd.jpl.nasa.gov/sbdb.cgi?soln=SAO%2F1910;cad=0;cov=0;sstr=1P;orb=1;log=0;old=0#elem
     */
    private static final double axisHalley = 17.83414429255373; // A [au]
    private static final double eccentricityHalley = 0.9671429084623044; // EC [-]
    private static final double inclinationHalley = 162.2626905791606; // IN [degrees]
    private static final double argPerihelionHalley = 111.3324851045177; // [degrees]
    private static final double longNodeHalley = 58.42008097656843; // OM [degrees]
    private static final double perihelionPassageHalley = 2446467.395317050925; // TP [JED]
    private static final double meanMotionHalley = 0.01308656479244564; // [degrees/day]
    private static final double orbitPeriodHalley = 75.315892782197; // PER [years]  
    private static final double[] HALLEYORBITPARS = new double[]
    {axisHalley, eccentricityHalley, inclinationHalley, argPerihelionHalley, longNodeHalley,
     perihelionPassageHalley, meanMotionHalley}; 
    
    /**
     * Keplerian orbital parameters for comet 2P/Encke
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2P
     * https://en.wikipedia.org/wiki/Comet_Encke
     */
    private static final double axisEncke = 2.215103855763232; // [au]
    private static final double eccentricityEncke = 0.8482929263100047; // [-]
    private static final double inclinationEncke = 11.78089864093374; // [degrees]
    private static final double argPerihelionEncke = 186.5416777104336; // [degrees]
    private static final double longNodeEncke = 334.5688235640465; // [degrees]
    private static final double perihelionPassageEncke = 2456618.220238561292; // [JED]
    private static final double meanMotionEncke = 0.2989598963807595; // [degrees/day]
    private static final double orbitalPeriodEncke = 1204.17488886703; // [days]
    private static final double[] ENCKEORBITPARS = new double[]
    {axisEncke, eccentricityEncke, inclinationEncke, argPerihelionEncke, longNodeEncke,
     perihelionPassageEncke, meanMotionEncke};
    
    /**
     * Keplerian orbital parameters for comet 67P/Churyumov-Gerasimenko
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=67P
     * https://en.wikipedia.org/wiki/67P/Churyumov–Gerasimenko
     * Rosetta spacecraft: https://en.wikipedia.org/wiki/Rosetta_(spacecraft)
     */
    private static final double axisCG = 3.464737502510219; // [au]
    private static final double eccentricityCG = 0.6405823233437267; // [-]
    private static final double inclinationCG = 7.043680712713979; // [degrees]
    private static final double argPerihelionCG = 12.69446409956478; // [degrees]
    private static final double longNodeCG = 50.18004588418096; // [degrees]
    private static final double perihelionPassageCG = 2454891.027525088560; // [JED]
    private static final double meanMotionCG = 0.1528264653077319; // [degrees/day]
    private static final double orbitalPeriodCG = 2355.612944885578; // [days]
    private static final double[] CGORBITPARS = new double[]
    {axisCG, eccentricityCG, inclinationCG, argPerihelionCG, longNodeCG,
     perihelionPassageCG, meanMotionCG};
    
    
    /**
     * Keplerian orbital parameters for comet D/1993 F2-A (Shoemaker-Levy 9).
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=1993%20F2-A
     * https://en.wikipedia.org/wiki/Comet_Shoemaker–Levy_9
     * Comet Shoemaker–Levy 9 (formally designated D/1993 F2) was a comet that 
     * broke apart in July 1992 and collided with Jupiter in July 1994, providing 
     * the first direct observation of an extraterrestrial collision of 
     * Solar System objects.
     * Orbital parameters are not valid before 1992-Jul-15 00:00 UT
     * Orbital paramerers are not valid after 1994-Jul-16 20:11 UT
     * REMARK: Parameters mass and mu = G*M are unknown.
     */
    private static final double axisSL9 = 6.86479462772464; // [au]
    private static final double eccentricitySL9 = 0.216209166902718; // [-]
    private static final double inclinationSL9 = 6.00329387351007; // [degrees]
    private static final double argPerihelionSL9 = 354.8935191875186; // [degrees]
    private static final double longNodeSL9 = 220.5376550079234; // [degrees]
    private static final double perihelionPassageSL9 = 2449435.603196492293; // [JED]
    private static final double meanMotionSL9 = 0.05479775297461272; // [degrees/day]
    private static final double orbitalPeriodSL9 = 6569.612446823952; // [days]
    private static final double[] SL9ORBITPARS = new double[]
    {axisSL9, eccentricitySL9, inclinationSL9, argPerihelionSL9, longNodeSL9,
     perihelionPassageSL9, meanMotionSL9};
    
    /**
     * Keplerian orbital parameters for comet C/1995 O1 (Hale-Bopp).
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=1995%20O1
     * https://en.wikipedia.org/wiki/Comet_Hale–Bopp
     * Hale–Bopp was discovered on July 23, 1995 separately by Alan Hale and
     * Thomas Bopp prior to it becoming naked-eye visible on Earth. Although
     * predicting the maximum apparent brightness of new comets with any degree
     * of certainty is difficult, Hale–Bopp met or exceeded most predictions
     * when it passed perihelion on April 1, 1997. It was visible to the naked
     * eye for a record 18 months, twice as long as the previous record holder,
     * the Great Comet of 1811. Accordingly, Hale–Bopp was dubbed the Great 
     * Comet of 1997.
     * REMARK: Parameters mass and mu = G*M are unknown.
     */
    private static final double axisHB = 191.0064717884599; // [au]
    private static final double eccentricityHB = 0.995213296666182; // [-]
    private static final double inclinationHB = 89.43269534883738; // [degrees]
    private static final double argPerihelionHB = 130.5768076894707; // [degrees]
    private static final double longNodeHB = 282.4722897964125; // [degrees]
    private static final double perihelionPassageHB = 2450539.628109521717; // [JED]
    private static final double meanMotionHB = 0.0003733635782842797; // [degrees/day]
    private static final double orbitalPeriodHB = 964207.6006832551; // [days]
    private static final double[] HBORBITPARS = new double[]
    {axisHB, eccentricityHB, inclinationHB, argPerihelionHB, longNodeHB,
     perihelionPassageHB, meanMotionHB};
    
    /**
     * Keplerian orbital parameters for asteroid 3122 Florence.
     * https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3122
     * https://echo.jpl.nasa.gov/asteroids/Florence/Florence_planning.html
     * https://www.scientias.nl/forse-aardscheerder-schiet-op-1-september-planeet/
     * https://en.wikipedia.org/wiki/3122_Florence
     * 3122 Florence, provisional designation 1981 ET3, is a stony asteroid of 
     * the Amor group, classified as near-Earth object and potentially hazardous
     * asteroid, approximately 5 kilometers in diameter. It was discovered on 
     * 2 March 1981 by American astronomer Schelte Bus at Siding Spring Observatory.
     * Florence orbits the Sun at a distance of 1.0–2.5 AU once every 2 years and 
     * 4 months (859 days). Its orbit has an eccentricity of 0.42 and an 
     * inclination of 22° with respect to the ecliptic.
     * Florence is classified as a potentially hazardous asteroid (PHA), due to 
     * both its absolute magnitude (H ≤ 22) and its minimum orbit intersection 
     * distance (MOID ≤ 0.05 AU). 
     * On 2017-Sep-01 it will pass 0.04723 AU (7,066,000 km; 4,390,000 mi) 
     * from Earth, brightening to apparent magnitude 8.5, when it will be 
     * visible in small telescopes for several nights as it moves through the 
     * constellations Piscis Austrinus, Capricornus, Aquarius and Delphinus.
     * Naming citation was published on 6 April 1993 (M.P.C. 21955).
     * REMARK: Parameters mass and mu = G*M are unknown.
     */
    private static final double axisFlorence = 1.769132445343428; // [au]
    private static final double eccentricityFlorence = 0.4233004309875272; // [-]
    private static final double inclinationFlorence = 22.15078418498147; // [degrees]
    private static final double argPerihelionFlorence = 27.84698807748255; // [degrees]
    private static final double longNodeFlorence = 336.0951180796379; // [degrees]
    private static final double perihelionPassageFlorence = 2458020.940196224544; // [JED]
    private static final double meanMotionFlorence = 0.418854854065512; // [degrees/day]
    private static final double orbitalPeriodFlorence = 859.4862790910698; // [days]
    private static final double[] FLORENCEORBITPARS = new double[]
    {axisFlorence, eccentricityFlorence, inclinationFlorence, argPerihelionFlorence, 
     longNodeFlorence, perihelionPassageFlorence, meanMotionFlorence};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Ultima Thule (3713011) [2486958]
     * Observer Location [change] : Sun (body center) [500@10]
     * Time Span [change]         : Start=2019-01-01, Stop=2019-01-02, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * This pre-computed trajectory is consistent with the New Horizons spacecraft
     * Kuiper-Belt extended mission, with the planned 3500 km flyby of Ultima Thule
     * on 2019-Jan-1 @ 05:33 UTC.
     *
     *  2458484.729166667 = A.D. 2019-Jan-01 05:30:00.0000 TDB
     *  EC= 4.098363091038795E-02 QR= 4.272551661427943E+01 IN= 2.451767154639498E+00
     *  OM= 1.589787485075718E+02 W = 1.746745157307338E+02 Tp=  2471795.170112812892
     *  N = 3.314454945917162E-03 MA= 3.158831431737062E+02 TA= 3.124912422291994E+02
     *  A = 4.455139452399387E+01 AD= 4.637727243370831E+01 PR= 1.086151436282029E+05
     *
     private static final double axisUTAU           = 4.455139452399387E+01; // Semi-major axis [au]
     private static final double eccentricityUT     = 4.098363091038795E-02; // Eccentricity [-]
     private static final double inclinationUT      = 2.451767154639498E+00; // Inclination [degrees]
     private static final double argPeriapsisUT     = 1.746745157307338E+02; // Arg perifocus [degrees]
     private static final double longNodeUT         = 1.589787485075718E+02; // Long asc node [degrees]
     private static final double periapsisPassageUT = 2471795.170112812892;  // Time of periapsis [JD]
     private static final double meanMotionUT       = 3.314454945917162E-03; // Mean motion [degrees/day]
     private static final double[] UTORBITPARS = new double[]
     {axisUTAU, eccentricityUT, inclinationUT, argPeriapsisUT, longNodeUT,
     periapsisPassageUT, meanMotionUT};
     */

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Ultima Thule (3713011) [2486958]
     * Observer Location [change] : Sun (body center) [500@10]
     * Time Span [change]         : Start=2006-01-19, Stop=2006-01-20, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * This pre-computed trajectory is consistent with the New Horizons spacecraft
     * Kuiper-Belt extended mission, with the planned 3500 km flyby of Ultima Thule
     * on 2019-Jan-1 @ 05:33 UTC.
     *
     *  2453755.291666667 = A.D. 2006-Jan-19 19:00:00.0000 TDB
     *  EC= 3.697176831510071E-02 QR= 4.274830978442073E+01 IN= 2.451474757830545E+00
     *  OM= 1.589482983942412E+02 W = 1.775741605683169E+02 Tp=  2472638.909251091536
     *  N = 3.332607599482501E-03 MA= 2.970683125324245E+02 TA= 2.932176608369780E+02
     *  A = 4.438946686913731E+01 AD= 4.603062395385389E+01 PR= 1.080235188973050E+05
     */
     private static final double axisUTAU           = 4.438946686913731E+01; // Semi-major axis [au]
     private static final double eccentricityUT     = 3.697176831510071E-02; // Eccentricity [-]
     private static final double inclinationUT      = 2.451474757830545E+00; // Inclination [degrees]
     private static final double argPeriapsisUT     = 1.775741605683169E+02; // Arg perifocus [degrees]
     private static final double longNodeUT         = 1.589482983942412E+02; // Long asc node [degrees]
     private static final double periapsisPassageUT = 2472638.909251091536;  // Time of periapsis [JD]
     private static final double meanMotionUT       = 3.332607599482501E-03; // Mean motion [degrees/day]
     private static final double[] UTORBITPARS = new double[]
     {axisUTAU, eccentricityUT, inclinationUT, argPeriapsisUT, longNodeUT,
     periapsisPassageUT, meanMotionUT};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris type    : OBSERVER
     * Target Body       : Moon [Luna] [301]
     * Observer Location : Geocentric [500]
     * Time Span         : Start=2017-05-28, Stop=2017-06-27, Step=1 d
     * Table Settings    : defaults
     * Display/Output    : default (formatted HTML)
     * NOTE: ORBIT PARAMETERS ARE NOT CORRECTED FOR DATE
     */
    private static final double axisMoonAU           = 2.548289534512777E-03; // Semi-major axis [au]
    private static final double eccentricityMoon     = 6.476694137484437E-02; // Eccentricity [-]
    private static final double inclinationMoon      = 5.240010960708354E+00; // Inclination [degrees]
    private static final double argPeriapsisMoon     = 3.081359025079810E+02; // Arg perifocus [degrees]
    private static final double longNodeMoon         = 1.239837037681769E+02; // Long asc node [degrees]
    private static final double periapsisPassageMoon = 2451533.965359302238;  // Time of periapsis [JD]
    private static final double meanMotionMoon       = 1.335975862260855E+01; // Mean motion [degrees/day]
    private static final double[] MOONORBITPARS = new double[]
    {axisMoonAU, eccentricityMoon, inclinationMoon, argPeriapsisMoon, longNodeMoon, 
     periapsisPassageMoon, meanMotionMoon};
    
    
    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Io (JI) [501]
     * Observer Location [change] : Jupiter System Barycenter [500@5]
     * Time Span [change]         : Start=2000-01-01, Stop=2000-01-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     */
    private static final double axisIoAU           = 2.821786546733507E-03; // Semi-major axis [au]
    private static final double eccentricityIo     = 3.654784965339888E-03; // Eccentricity [-]
    private static final double inclinationIo      = 2.212609179741271E+00; // Inclination [degrees]
    private static final double argPeriapsisIo     = 6.218469675691234E+01; // Arg perifocus [degrees]
    private static final double longNodeIo         = 3.368501231726219E+02; // Long asc node [degrees]
    private static final double periapsisPassageIo = 2451545.103514090180;  // Time of periapsis [JD]
    private static final double meanMotionIo       = 2.031615704411821E+02; // Mean motion [degrees/day]
    private static final double[] IOORBITPARS = new double[]
    {axisIoAU, eccentricityIo, inclinationIo, argPeriapsisIo, longNodeIo, 
     periapsisPassageIo, meanMotionIo};
    
    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Europa (JII) [502]
     * Observer Location [change] : Jupiter System Barycenter [500@5]
     * Time Span [change]         : Start=2000-01-01, Stop=2000-01-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     */
    private static final double axisEuropaAU           = 4.484929379399280E-03; // Semi-major axis [au]
    private static final double eccentricityEuropa     = 9.470425146083724E-03; // Eccentricity [-]
    private static final double inclinationEuropa      = 1.790857714257787E+00; // Inclination [degrees]
    private static final double argPeriapsisEuropa     = 2.557899602714836E+02; // Arg perifocus [degrees]
    private static final double longNodeEuropa         = 3.326257958798038E+02; // Long asc node [degrees]
    private static final double periapsisPassageEuropa = 2451545.154986763373;  // Time of periapsis [JD]
    private static final double meanMotionEuropa       = 1.013931372961153E+02; // Mean motion [degrees/day]
    private static final double[] EUROPAORBITPARS = new double[]
    {axisEuropaAU, eccentricityEuropa, inclinationEuropa, argPeriapsisEuropa, 
     longNodeEuropa, periapsisPassageEuropa, meanMotionEuropa}; 
    
    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Ganymede (JIII) [503]
     * Observer Location [change] : Jupiter System Barycenter [500@5]
     * Time Span [change]         : Start=2000-01-01, Stop=2000-01-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     */
    private static final double axisGanymedeAU           = 7.156339844320714E-03; // Semi-major axis [au]
    private static final double eccentricityGanymede     = 1.318103012416448E-03; // Eccentricity [-]
    private static final double inclinationGanymede      = 2.214135822185767E+00; // Inclination [degrees]
    private static final double argPeriapsisGanymede     = 3.167413036642092E+02; // Arg perifocus [degrees]
    private static final double longNodeGanymede         = 3.431712649776430E+02; // Long asc node [degrees]
    private static final double periapsisPassageGanymede = 2451546.588401503861;  // Time of periapsis [JD]
    private static final double meanMotionGanymede       = 5.030036883436198E+01; // Mean motion [degrees/day]
    private static final double[] GANYMEDEORBITPARS = new double[]
    {axisGanymedeAU, eccentricityGanymede, inclinationGanymede, argPeriapsisGanymede, 
     longNodeGanymede, periapsisPassageGanymede, meanMotionGanymede}; 
    
    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Callisto (JIV) [504]
     * Observer Location [change] : Jupiter System Barycenter [500@5]
     * Time Span [change]         : Start=2000-01-01, Stop=2000-01-02, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     */
    private static final double axisCallistoAU           = 1.258560648085115E-02; // Semi-major axis [au]
    private static final double eccentricityCallisto     = 7.432943295907821E-03; // Eccentricity [-]
    private static final double inclinationCallisto      = 2.016903900389733E+00; // Inclination [degrees]
    private static final double argPeriapsisCallisto     = 1.632112921781330E+01; // Arg perifocus [degrees]
    private static final double longNodeCallisto         = 3.379421848030697E+02; // Long asc node [degrees]
    private static final double periapsisPassageCallisto = 2451541.062475862447;  // Time of periapsis [JD]
    private static final double meanMotionCallisto       = 2.156802147671815E+01; // Mean motion [degrees/day]
    private static final double[] CALLISTOORBITPARS = new double[]
    {axisCallistoAU, eccentricityCallisto, inclinationCallisto, argPeriapsisCallisto, 
     longNodeCallisto, periapsisPassageCallisto, meanMotionCallisto};

     /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Io (JI) [501]
     * Observer Location [change] : Jupiter (body center) [500@599]
     * Time Span [change]         : Start=1979-07-09, Stop=1977-07-10, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
      * *
      * 2444064.437500000 = A.D. 1979-Jul-09 22:30:00.0000 TDB
      *  EC= 4.464069846305494E-03 QR= 2.808511485227889E-03 IN= 2.179905298844204E+00
      *  OM= 3.378976714091401E+02 W = 2.090801050866877E+02 Tp=  2444064.145622990560
      *  N = 2.032332690733699E+02 MA= 5.931911877792535E+01 TA= 5.976031049336378E+01
      *  A = 2.821105095417602E-03 AD= 2.833698705607314E-03 PR= 1.771363525476900E+00
     *
     private static final double axisIoAU           = 2.821105095417602E-03; // Semi-major axis [au]
     private static final double eccentricityIo     = 4.464069846305494E-03; // Eccentricity [-]
     private static final double inclinationIo      = 2.179905298844204E+00; // Inclination [degrees]
     private static final double argPeriapsisIo     = 2.090801050866877E+02; // Arg perifocus [degrees]
     private static final double longNodeIo         = 3.378976714091401E+02; // Long asc node [degrees]
     private static final double periapsisPassageIo = 2444064.145622990560;  // Time of periapsis [JD]
     private static final double meanMotionIo       = 2.032332690733699E+02; // Mean motion [degrees/day]
     private static final double[] IOORBITPARS = new double[]
     {axisIoAU, eccentricityIo, inclinationIo, argPeriapsisIo, longNodeIo,
     periapsisPassageIo, meanMotionIo};
      */

     /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Europa (JII) [502]
     * Observer Location [change] : Jupiter (body center) [500@599]
     * Time Span [change]         : Start=1979-07-09, Stop=1977-07-10, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
      * 2444064.437500000 = A.D. 1979-Jul-09 22:30:00.0000 TDB
      *  EC= 9.387957322704407E-03 QR= 4.444762203181402E-03 IN= 2.253032329893990E+00
      *  OM= 3.503598726198932E+02 W = 7.745870708442311E+00 Tp=  2444064.095360492356
      *  N = 1.013214937274340E+02 MA= 3.466608598681508E+01 TA= 3.528393136496152E+01
      *  A = 4.486884887012564E-03 AD= 4.529007570843726E-03 PR= 3.553046710586796E+00
     *
     private static final double axisEuropaAU           = 4.486884887012564E-03; // Semi-major axis [au]
     private static final double eccentricityEuropa     = 9.387957322704407E-03; // Eccentricity [-]
     private static final double inclinationEuropa      = 2.253032329893990E+00; // Inclination [degrees]
     private static final double argPeriapsisEuropa     = 7.745870708442311E+00; // Arg perifocus [degrees]
     private static final double longNodeEuropa         = 3.503598726198932E+02; // Long asc node [degrees]
     private static final double periapsisPassageEuropa = 2444064.095360492356;  // Time of periapsis [JD]
     private static final double meanMotionEuropa       = 1.013214937274340E+02; // Mean motion [degrees/day]
     private static final double[] EUROPAORBITPARS = new double[]
     {axisEuropaAU, eccentricityEuropa, inclinationEuropa, argPeriapsisEuropa,
     longNodeEuropa, periapsisPassageEuropa, meanMotionEuropa};
      */

     /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Ganymede (JIII) [503]
     * Observer Location [change] : Jupiter (body center) [500@599]
     * Time Span [change]         : Start=1979-07-09, Stop=1979-07-10, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
      * 2444064.437500000 = A.D. 1979-Jul-09 22:30:00.0000 TDB
      *  EC= 1.247036336893339E-03 QR= 7.147635725762605E-03 IN= 2.040028856429598E+00
      *  OM= 3.417668823947965E+02 W = 2.384131483164773E+02 Tp=  2444061.339921466075
      *  N = 5.030068952448489E+01 MA= 1.558103361030715E+02 TA= 1.558688073671426E+02
      *  A = 7.156560216399621E-03 AD= 7.165484707036636E-03 PR= 7.156959544754600E+00
     *
     private static final double axisGanymedeAU           = 7.156560216399621E-03; // Semi-major axis [au]
     private static final double eccentricityGanymede     = 1.247036336893339E-03; // Eccentricity [-]
     private static final double inclinationGanymede      = 2.040028856429598E+00; // Inclination [degrees]
     private static final double argPeriapsisGanymede     = 2.384131483164773E+02; // Arg perifocus [degrees]
     private static final double longNodeGanymede         = 3.417668823947965E+02; // Long asc node [degrees]
     private static final double periapsisPassageGanymede = 2444061.339921466075;  // Time of periapsis [JD]
     private static final double meanMotionGanymede       = 5.030068952448489E+01; // Mean motion [degrees/day]
     private static final double[] GANYMEDEORBITPARS = new double[]
     {axisGanymedeAU, eccentricityGanymede, inclinationGanymede, argPeriapsisGanymede,
     longNodeGanymede, periapsisPassageGanymede, meanMotionGanymede};
      */

     /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Callisto (JIV) [504]
     * Observer Location [change] : Jupiter (body center) [500@599]
     * Time Span [change]         : Start=1979-07-09, Stop=1979-07-10, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
      * 2444064.437500000 = A.D. 1979-Jul-09 22:30:00.0000 TDB
      *  EC= 7.835734158106549E-03 QR= 1.249249486072064E-02 IN= 2.072797100534001E+00
      *  OM= 3.394822939858797E+02 W = 3.598648006728227E+02 Tp=  2444063.691855364013
      *  N = 2.155397593895479E+01 MA= 1.607160654440537E+01 TA= 1.632254239586581E+01
      *  A = 1.259115581039419E-02 AD= 1.268981676006773E-02 PR= 1.670225488882388E+01
     *
     private static final double axisCallistoAU           = 1.259115581039419E-02; // Semi-major axis [au]
     private static final double eccentricityCallisto     = 7.835734158106549E-03; // Eccentricity [-]
     private static final double inclinationCallisto      = 2.072797100534001E+00; // Inclination [degrees]
     private static final double argPeriapsisCallisto     = 3.598648006728227E+02; // Arg perifocus [degrees]
     private static final double longNodeCallisto         = 3.394822939858797E+02; // Long asc node [degrees]
     private static final double periapsisPassageCallisto = 2444063.691855364013;  // Time of periapsis [JD]
     private static final double meanMotionCallisto       = 2.155397593895479E+01; // Mean motion [degrees/day]
     private static final double[] CALLISTOORBITPARS = new double[]
     {axisCallistoAU, eccentricityCallisto, inclinationCallisto, argPeriapsisCallisto,
     longNodeCallisto, periapsisPassageCallisto, meanMotionCallisto};
      */

    /** FOR TEST PURPOSES
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Callisto (JIV) [504]
     * Observer Location [change] : Sun (body center) [500@10]
     * Time Span [change]         : Start=2000-01-01, Stop=2000-01-02, Step=30 m
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     * 2451544.500000000 = A.D. 2000-Jan-01 00:00:00.0000 TDB
     *  EC= 1.463402256336346E+00 QR= 4.810961611782971E+00 IN= 1.173099259407824E+00
     *  OM= 1.207096686230750E+02 W = 2.948438791339357E+02 Tp=  2451678.653757016174
     *  N = 2.946411930880803E-02 MA=-3.952722302454301E+00 TA= 3.407729998288843E+02
     *  A =-1.038182603990403E+01 AD= 6.684586453809735E+91 PR= 1.157407291666667E+95
     *
    private static final double axisCallistoAUT           = -1.038182603990403E+01; // Semi-major axis [au]
    private static final double eccentricityCallistoT     = 1.463402256336346E+00; // Eccentricity [-]
    private static final double inclinationCallistoT      = 1.173099259407824E+00; // Inclination [degrees]
    private static final double argPeriapsisCallistoT     = 2.948438791339357E+02; // Arg perifocus [degrees]
    private static final double longNodeCallistoT         = 1.207096686230750E+02; // Long asc node [degrees]
    private static final double periapsisPassageCallistoT = 2451678.653757016174;  // Time of periapsis [JD]
    private static final double meanMotionCallistoT       = 2.946411930880803E-02; // Mean motion [degrees/day]
    private static final double[] CALLISTOORBITPARSTEST = new double[]
            {axisCallistoAUT, eccentricityCallistoT, inclinationCallistoT, argPeriapsisCallistoT,
                    longNodeCallistoT, periapsisPassageCallistoT, meanMotionCallistoT};
     */

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Mimas (SI) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 2.294076748949336E-02 QR= 1.216152212442891E-03 IN= 2.833548787776371E+01
     *  OM= 1.662380994388547E+02 W = 3.110660983844796E+02 Tp=  2444555.468582638539
     *  N = 3.794894129614744E+02 MA= 1.192255598685642E+01 TA= 1.248129166805152E+01
     *  A = 1.244706740366238E-03 AD= 1.273261268289585E-03 PR= 9.486430654036375E-01
     */
    private static final double axisMimasAU           = 1.244706740366238E-03; // Semi-major axis [au]
    private static final double eccentricityMimas     = 2.294076748949336E-02; // Eccentricity [-]
    private static final double inclinationMimas      = 2.833548787776371E+01; // Inclination [degrees]
    private static final double argPeriapsisMimas     = 3.110660983844796E+02; // Arg perifocus [degrees]
    private static final double longNodeMimas         = 1.662380994388547E+02; // Long asc node [degrees]
    private static final double periapsisPassageMimas = 2444555.468582638539;  // Time of periapsis [JD]
    private static final double meanMotionMimas       = 3.794894129614744E+02; // Mean motion [degrees/day]
    private static final double[] MIMASORBITPARS = new double[]
            {axisMimasAU, eccentricityMimas, inclinationMimas, argPeriapsisMimas,
                    longNodeMimas, periapsisPassageMimas, meanMotionMimas};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Enceladus (SII) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 4.483389662009040E-03 QR= 1.584906509048897E-03 IN= 2.805397904088017E+01
     *  OM= 1.695244324305456E+02 W = 2.726068154542818E+02 Tp=  2444556.014053585008
     *  N = 2.623417662340597E+02 MA= 2.251422746212319E+02 TA= 2.247795280296231E+02
     *  A = 1.592044263842871E-03 AD= 1.599182018636845E-03 PR= 1.372255760749931E+00
     */
    private static final double axisEnceladusAU           = 1.592044263842871E-03; // Semi-major axis [au]
    private static final double eccentricityEnceladus     = 4.483389662009040E-03; // Eccentricity [-]
    private static final double inclinationEnceladus      = 2.805397904088017E+01; // Inclination [degrees]
    private static final double argPeriapsisEnceladus     = 2.726068154542818E+02; // Arg perifocus [degrees]
    private static final double longNodeEnceladus         = 1.695244324305456E+02; // Long asc node [degrees]
    private static final double periapsisPassageEnceladus = 2444556.014053585008;  // Time of periapsis [JD]
    private static final double meanMotionEnceladus       = 2.623417662340597E+02; // Mean motion [degrees/day]
    private static final double[] ENCELADUSORBITPARS = new double[]
            {axisEnceladusAU, eccentricityEnceladus, inclinationEnceladus, argPeriapsisEnceladus,
                    longNodeEnceladus, periapsisPassageEnceladus, meanMotionEnceladus};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Tethys (SIII) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 1.765221092417035E-03 QR= 1.970712342111551E-03 IN= 2.700778748710897E+01
     *  OM= 1.702432284406243E+02 W = 3.229877171493513E+02 Tp=  2444555.393274717033
     *  N = 1.899825673459838E+02 MA= 2.027594327456301E+01 TA= 2.034618692144220E+01
     *  A = 1.974197236714391E-03 AD= 1.977682131317230E-03 PR= 1.894910701698181E+00
     */
    private static final double axisTethysAU           = 1.974197236714391E-03; // Semi-major axis [au]
    private static final double eccentricityTethys     = 1.765221092417035E-03; // Eccentricity [-]
    private static final double inclinationTethys      = 2.700778748710897E+01; // Inclination [degrees]
    private static final double argPeriapsisTethys     = 3.229877171493513E+02; // Arg perifocus [degrees]
    private static final double longNodeTethys         = 1.702432284406243E+02; // Long asc node [degrees]
    private static final double periapsisPassageTethys = 2444555.393274717033;  // Time of periapsis [JD]
    private static final double meanMotionTethys       = 1.899825673459838E+02; // Mean motion [degrees/day]
    private static final double[] TETHYSORBITPARS = new double[]
            {axisTethysAU, eccentricityTethys, inclinationTethys, argPeriapsisTethys,
                    longNodeTethys, periapsisPassageTethys, meanMotionTethys};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Dione (SIV) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 2.175557127959605E-03 QR= 2.514064366570496E-03 IN= 2.803416128061381E+01
     *  OM= 1.695809000369315E+02 W = 3.193174176587208E+02 Tp=  2444556.303023209330
     *  N = 1.317697228848951E+02 MA= 2.541858542513983E+02 TA= 2.539461679426581E+02
     *  A = 2.519545782356523E-03 AD= 2.525027198142550E-03 PR= 2.732038833491903E+00
     */
    private static final double axisDioneAU           = 2.519545782356523E-03; // Semi-major axis [au]
    private static final double eccentricityDione     = 2.175557127959605E-03; // Eccentricity [-]
    private static final double inclinationDione      = 2.803416128061381E+01; // Inclination [degrees]
    private static final double argPeriapsisDione     = 3.193174176587208E+02; // Arg perifocus [degrees]
    private static final double longNodeDione         = 1.695809000369315E+02; // Long asc node [degrees]
    private static final double periapsisPassageDione = 2444556.303023209330;  // Time of periapsis [JD]
    private static final double meanMotionDione       = 1.317697228848951E+02; // Mean motion [degrees/day]
    private static final double[] DIONEORBITPARS = new double[]
            {axisDioneAU, eccentricityDione, inclinationDione, argPeriapsisDione,
                    longNodeDione, periapsisPassageDione, meanMotionDione};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Rhea (SV) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 1.077873250919286E-03 QR= 3.519529004414233E-03 IN= 2.776323154892035E+01
     *  OM= 1.699392472553282E+02 W = 1.616205502165373E+02 Tp=  2444553.785297347233
     *  N = 7.968347273234254E+01 MA= 1.366334620792505E+02 TA= 1.367181923615887E+02
     *  A = 3.523326704022749E-03 AD= 3.527124403631264E-03 PR= 4.517875384387965E+00
     */
    private static final double axisRheaAU           = 3.523326704022749E-03; // Semi-major axis [au]
    private static final double eccentricityRhea     = 1.077873250919286E-03; // Eccentricity [-]
    private static final double inclinationRhea      = 2.776323154892035E+01; // Inclination [degrees]
    private static final double argPeriapsisRhea     = 1.616205502165373E+02; // Arg perifocus [degrees]
    private static final double longNodeRhea         = 1.699392472553282E+02; // Long asc node [degrees]
    private static final double periapsisPassageRhea = 2444553.785297347233;  // Time of periapsis [JD]
    private static final double meanMotionRhea       = 7.968347273234254E+01; // Mean motion [degrees/day]
    private static final double[] RHEAORBITPARS = new double[]
            {axisRheaAU, eccentricityRhea, inclinationRhea, argPeriapsisRhea,
                    longNodeRhea, periapsisPassageRhea, meanMotionRhea};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Titan (SVI) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 2.883100757260863E-02 QR= 7.930970353173764E-03 IN= 2.774693775242916E+01
     *  OM= 1.693222701040926E+02 W = 1.546395634824284E+02 Tp=  2444553.225263091270
     *  N = 2.257349567824485E+01 MA= 5.134876377910118E+01 TA= 5.398732093364459E+01
     *  A = 8.166416365241102E-03 AD= 8.401862377308443E-03 PR= 1.594790656845183E+01
     */
    private static final double axisTitanAU           = 8.166416365241102E-03; // Semi-major axis [au]
    private static final double eccentricityTitan     = 2.883100757260863E-02; // Eccentricity [-]
    private static final double inclinationTitan      = 2.774693775242916E+01; // Inclination [degrees]
    private static final double argPeriapsisTitan     = 1.546395634824284E+02; // Arg perifocus [degrees]
    private static final double longNodeTitan         = 1.693222701040926E+02; // Long asc node [degrees]
    private static final double periapsisPassageTitan = 2444553.225263091270;  // Time of periapsis [JD]
    private static final double meanMotionTitan       = 2.257349567824485E+01; // Mean motion [degrees/day]
    private static final double[] TITANORBITPARS = new double[]
            {axisTitanAU, eccentricityTitan, inclinationTitan, argPeriapsisTitan,
                    longNodeTitan, periapsisPassageTitan, meanMotionTitan};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Hyperion (SVII) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 1.272431943004429E-01 QR= 8.666112626930228E-03 IN= 2.766098902279464E+01
     *  OM= 1.679386964197648E+02 W = 1.854300936975505E+02 Tp=  2444562.071466295980
     *  N = 1.684238675061948E+01 MA= 2.493208231217351E+02 TA= 2.365127508721484E+02
     *  A = 9.929584702560890E-03 AD= 1.119305677819155E-02 PR= 2.137464275879776E+01
     */
    private static final double axisHyperionAU           = 9.929584702560890E-03; // Semi-major axis [au]
    private static final double eccentricityHyperion     = 1.272431943004429E-01; // Eccentricity [-]
    private static final double inclinationHyperion      = 2.766098902279464E+01; // Inclination [degrees]
    private static final double argPeriapsisHyperion     = 1.854300936975505E+02; // Arg perifocus [degrees]
    private static final double longNodeHyperion         = 1.679386964197648E+02; // Long asc node [degrees]
    private static final double periapsisPassageHyperion = 2444562.071466295980;  // Time of periapsis [JD]
    private static final double meanMotionHyperion       = 1.684238675061948E+01; // Mean motion [degrees/day]
    private static final double[] HYPERIONORBITPARS = new double[]
            {axisHyperionAU, eccentricityHyperion, inclinationHyperion, argPeriapsisHyperion,
                    longNodeHyperion, periapsisPassageHyperion, meanMotionHyperion};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Iapetus (SVIII) [606]
     * Observer Location [change] : Saturn System Barycenter [500@6]
     * Time Span [change]         : Start=1980-11-12, Stop=1980-11-12, Step=1 d
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Nov 12, 1980 is date of flyby of Voyager 1.
     *
     * 2444555.500000000 = A.D. 1980-Nov-12 00:00:00.0000 TDB
     *  EC= 2.856225061595786E-02 QR= 2.312483296093841E-02 IN= 1.749827907600319E+01
     *  OM= 1.403973722109327E+02 W = 2.261903994839276E+02 Tp=  2444517.435074096080
     *  N = 4.537354975447288E+00 MA= 1.727140809408426E+02 TA= 1.731149457095495E+02
     *  A = 2.380475020205992E-02 AD= 2.448466744318143E-02 PR= 7.934137883151000E+01
     */
    private static final double axisIapetusAU           = 2.380475020205992E-02; // Semi-major axis [au]
    private static final double eccentricityIapetus     = 2.856225061595786E-02; // Eccentricity [-]
    private static final double inclinationIapetus      = 1.749827907600319E+01; // Inclination [degrees]
    private static final double argPeriapsisIapetus     = 2.261903994839276E+02; // Arg perifocus [degrees]
    private static final double longNodeIapetus         = 1.403973722109327E+02; // Long asc node [degrees]
    private static final double periapsisPassageIapetus = 2444517.435074096080;  // Time of periapsis [JD]
    private static final double meanMotionIapetus       = 4.537354975447288E+00; // Mean motion [degrees/day]
    private static final double[] IAPETUSORBITPARS = new double[]
            {axisIapetusAU, eccentricityIapetus, inclinationIapetus, argPeriapsisIapetus,
                    longNodeIapetus, periapsisPassageIapetus, meanMotionIapetus};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Miranda (UV) [705]
     * Center                     : Uranus System Barycenter [500@7]
     * Time Span [change]         : Start=1986-01-24, Stop=1986-01-24, Step=6 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Jan 24, 1986 is date of flyby of Voyager 2.
     *
     * 2446455.250000000 = A.D. 1986-Jan-24 18:00:00.0000 TDB
     *  EC= 1.334113739191030E-03 QR= 8.664871813807257E-04 IN= 1.019201605356678E+02
     *  OM= 1.690199433071529E+02 W = 3.223465505057740E+02 Tp=  2446455.060737770516
     *  N = 2.548265562097606E+02 MA= 4.822904210236982E+01 TA= 4.834318747087165E+01
     *  A = 8.676447181199060E-04 AD= 8.688022548590863E-04 PR= 1.412725601893964E+00
     */
    private static final double axisMirandaAU           = 8.676447181199060E-04; // Semi-major axis [au]
    private static final double eccentricityMiranda     = 1.334113739191030E-03; // Eccentricity [-]
    private static final double inclinationMiranda      = 1.019201605356678E+02; // Inclination [degrees]
    private static final double argPeriapsisMiranda     = 3.223465505057740E+02; // Arg perifocus [degrees]
    private static final double longNodeMiranda         = 1.690199433071529E+02; // Long asc node [degrees]
    private static final double periapsisPassageMiranda = 2446455.060737770516;  // Time of periapsis [JD]
    private static final double meanMotionMiranda       = 2.548265562097606E+02; // Mean motion [degrees/day]
    private static final double[] MIRANDAORBITPARS = new double[]
            {axisMirandaAU, eccentricityMiranda, inclinationMiranda, argPeriapsisMiranda,
                    longNodeMiranda, periapsisPassageMiranda, meanMotionMiranda};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Ariel (UI) [701]
     * Center                     : Uranus System Barycenter [500@7]
     * Time Span [change]         : Start=1986-01-24, Stop=1986-01-24, Step=6 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Jan 24, 1986 is date of flyby of Voyager 2.
     *
     * 2446455.250000000 = A.D. 1986-Jan-24 18:00:00.0000 TDB
     *  EC= 1.828146549847673E-03 QR= 1.273608250361064E-03 IN= 9.771964547613906E+01
     *  OM= 1.676311983381491E+02 W = 3.268039389316039E+02 Tp=  2446454.735053625889
     *  N = 1.428903374678187E+02 MA= 7.358086117257147E+01 TA= 7.378193771139888E+01
     *  A = 1.275940857237032E-03 AD= 1.278273464113000E-03 PR= 2.519414583096482E+00
     */
    private static final double axisArielAU           = 1.275940857237032E-03; // Semi-major axis [au]
    private static final double eccentricityAriel     = 1.828146549847673E-03; // Eccentricity [-]
    private static final double inclinationAriel      = 9.771964547613906E+01; // Inclination [degrees]
    private static final double argPeriapsisAriel     = 3.268039389316039E+02; // Arg perifocus [degrees]
    private static final double longNodeAriel         = 1.676311983381491E+02; // Long asc node [degrees]
    private static final double periapsisPassageAriel = 2446454.735053625889;  // Time of periapsis [JD]
    private static final double meanMotionAriel       = 1.428903374678187E+02; // Mean motion [degrees/day]
    private static final double[] ARIELORBITPARS = new double[]
            {axisArielAU, eccentricityAriel, inclinationAriel, argPeriapsisAriel,
                    longNodeAriel, periapsisPassageAriel, meanMotionAriel};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Umbriel (UII) [702]
     * Center                     : Uranus System Barycenter [500@7]
     * Time Span [change]         : Start=1986-01-24, Stop=1986-01-24, Step=6 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Jan 24, 1986 is date of flyby of Voyager 2.
     *
     * 2446455.250000000 = A.D. 1986-Jan-24 18:00:00.0000 TDB
     *  EC= 4.264444554427566E-03 QR= 1.770865367553330E-03 IN= 9.769328000824999E+01
     *  OM= 1.675902215804727E+02 W = 2.949950690755990E+02 Tp=  2446456.513212617952
     *  N = 8.683327893081464E+01 MA= 2.503111063928964E+02 TA= 2.498518364737965E+02
     *  A = 1.778449466696910E-03 AD= 1.786033565840491E-03 PR= 4.145875917997222E+00
     */
    private static final double axisUmbrielAU           = 1.778449466696910E-03; // Semi-major axis [au]
    private static final double eccentricityUmbriel     = 4.264444554427566E-03; // Eccentricity [-]
    private static final double inclinationUmbriel      = 9.769328000824999E+01; // Inclination [degrees]
    private static final double argPeriapsisUmbriel     = 2.949950690755990E+02; // Arg perifocus [degrees]
    private static final double longNodeUmbriel         = 1.675902215804727E+02; // Long asc node [degrees]
    private static final double periapsisPassageUmbriel = 2446456.513212617952;  // Time of periapsis [JD]
    private static final double meanMotionUmbriel       = 8.683327893081464E+01; // Mean motion [degrees/day]
    private static final double[] UMBRIELORBITPARS = new double[]
            {axisUmbrielAU, eccentricityUmbriel, inclinationUmbriel, argPeriapsisUmbriel,
                    longNodeUmbriel, periapsisPassageUmbriel, meanMotionUmbriel};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Titania (UIII) [703]
     * Center                     : Uranus System Barycenter [500@7]
     * Time Span [change]         : Start=1986-01-24, Stop=1986-01-24, Step=6 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Jan 24, 1986 is date of flyby of Voyager 2.
     *
     * 2446455.250000000 = A.D. 1986-Jan-24 18:00:00.0000 TDB
     *  EC= 2.699684405758504E-03 QR= 2.907991492514086E-03 IN= 9.784186655153967E+01
     *  OM= 1.676200525421978E+02 W = 1.913287392441945E+02 Tp=  2446458.712360218167
     *  N = 4.136011948310419E+01 MA= 2.167963676760602E+02 TA= 2.166115685681959E+02
     *  A = 2.915863403473766E-03 AD= 2.923735314433446E-03 PR= 8.704036750838251E+00
     */
    private static final double axisTitaniaAU           = 2.915863403473766E-03; // Semi-major axis [au]
    private static final double eccentricityTitania     = 2.699684405758504E-03; // Eccentricity [-]
    private static final double inclinationTitania      = 9.784186655153967E+01; // Inclination [degrees]
    private static final double argPeriapsisTitania     = 1.913287392441945E+02; // Arg perifocus [degrees]
    private static final double longNodeTitania         = 1.676200525421978E+02; // Long asc node [degrees]
    private static final double periapsisPassageTitania = 2446458.712360218167;  // Time of periapsis [JD]
    private static final double meanMotionTitania       = 4.136011948310419E+01; // Mean motion [degrees/day]
    private static final double[] TITANIAORBITPARS = new double[]
            {axisTitaniaAU, eccentricityTitania, inclinationTitania, argPeriapsisTitania,
                    longNodeTitania, periapsisPassageTitania, meanMotionTitania};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Oberon (UIV) [704]
     * Center                     : Uranus System Barycenter [500@7]
     * Time Span [change]         : Start=1986-01-24, Stop=1986-01-24, Step=6 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Jan 24, 1986 is date of flyby of Voyager 2.
     *
     * 2446455.250000000 = A.D. 1986-Jan-24 18:00:00.0000 TDB
     *  EC= 3.479588686913168E-04 QR= 3.899090046075020E-03 IN= 9.783394065511513E+01
     *  OM= 1.677619229560692E+02 W = 3.089743224447537E+02 Tp=  2446454.454100156669
     *  N = 2.673396445911760E+01 MA= 2.127755813031945E+01 TA= 2.129203341544224E+01
     *  A = 3.900447241284487E-03 AD= 3.901804436493955E-03 PR= 1.346601625623177E+01
     */
    private static final double axisOberonAU           = 3.900447241284487E-03; // Semi-major axis [au]
    private static final double eccentricityOberon     = 3.479588686913168E-04; // Eccentricity [-]
    private static final double inclinationOberon      = 9.783394065511513E+01; // Inclination [degrees]
    private static final double argPeriapsisOberon     = 3.089743224447537E+02; // Arg perifocus [degrees]
    private static final double longNodeOberon         = 1.677619229560692E+02; // Long asc node [degrees]
    private static final double periapsisPassageOberon = 2446454.454100156669;  // Time of periapsis [JD]
    private static final double meanMotionOberon       = 2.673396445911760E+01; // Mean motion [degrees/day]
    private static final double[] OBERONORBITPARS = new double[]
            {axisOberonAU, eccentricityOberon, inclinationOberon, argPeriapsisOberon,
                    longNodeOberon, periapsisPassageOberon, meanMotionOberon};

    /**
     * https://ssd.jpl.nasa.gov/horizons.cgi#results
     * Results from HORIZONS
     * Ephemeris Type [change]    : ELEMENTS
     * Target Body [change]       : Triton (NI) [801]
     * Center                     : Neptune System Barycenter [500@8]
     * Time Span [change]         : Start=1989-08-25, Stop=1989-08-26, Step=6 h
     * Table Settings [change]    : defaults
     * Display/Output [change]    : default (formatted HTML)
     *
     * Aug 25, 1989 is date of flyby of Voyager 2.
     *
     * 2447763.500000000 = A.D. 1989-Aug-25 00:00:00.0000 TDB
     *  EC= 1.217220754327991E-05 QR= 2.370934201605266E-03 IN= 1.309092040930801E+02
     *  OM= 2.131742023027621E+02 W = 2.781135619426180E+02 Tp=  2447763.663316546939
     *  N = 6.125517670911833E+01 MA= 3.499960160500586E+02 TA= 3.499957737408385E+02
     *  A = 2.370963061459727E-03 AD= 2.370991921314189E-03 PR= 5.877054305296145E+00
     */
    private static final double axisTritonAU           = 2.370963061459727E-03; // Semi-major axis [au]
    private static final double eccentricityTriton     = 1.217220754327991E-05; // Eccentricity [-]
    private static final double inclinationTriton      = 1.309092040930801E+02; // Inclination [degrees]
    private static final double argPeriapsisTriton     = 2.781135619426180E+02; // Arg perifocus [degrees]
    private static final double longNodeTriton         = 2.131742023027621E+02; // Long asc node [degrees]
    private static final double periapsisPassageTriton = 2447763.663316546939;  // Time of periapsis [JD]
    private static final double meanMotionTriton       = 6.125517670911833E+01; // Mean motion [degrees/day]
    private static final double[] TRITONORBITPARS = new double[]
            {axisTritonAU, eccentricityTriton, inclinationTriton, argPeriapsisTriton,
                    longNodeTriton, periapsisPassageTriton, meanMotionTriton};
    
    // Singleton instance
    private static SolarSystemParameters instance = null;
    
    // Mass in kg for solar system bodies
    private final Map<String,Double> massMap;
    
    // Standard gravitational parameter in m3/s2 for solar system bodies
    private final Map<String,Double> muMap;
    
    // Diameter in m for solar system bodies
    private final Map<String,Double> diameterMap;

    // Ellipticity of oblate planet
    private final Map<String,Double> ellipticityMap;

    // Gravatitational parameter in m3/s2 of oblate planet
    private final Map<String,Double> oblateMuMap;

    // Equatorial radius in m of oblate planet
    private final Map<String,Double> equatorialRadiusMap;

    // Zonal coefficients of oblate planet
    private final Map<String,double[]> zonalCoefficientsMap;

    // Parameters of z-axis of oblate planet
    private final Map<String,double[]> zAxisParametersMap;

    // Orbital parameters (Keplerian elements and their rates) for solar system bodies
    private final Map<String,double[]> orbitParametersMap;
    
    // List of names of solar system bodies
    private final List<String> planets;
    
    // Map of moon names and their planets
    private final Map<String,String> moons;

    /**
     * Constructor. Singleton pattern.
     */
    private SolarSystemParameters() {
        
        // Masses in kg
        massMap = new HashMap<>();
        massMap.put("Sun",SUNMASS);
        massMap.put("Mercury",MERCURYMASS);
        massMap.put("Venus",VENUSMASS);
        massMap.put("Earth",EARTHMASS);
        massMap.put("Moon",MOONMASS);
        massMap.put("Mars",MARSMASS);
        massMap.put("Jupiter",JUPITERMASS);
        massMap.put("Saturn",SATURNMASS);
        massMap.put("Uranus",URANUSMASS);
        massMap.put("Neptune",NEPTUNEMASS);
        massMap.put("Pluto",PLUTOMASS);
        massMap.put("Eris",ERISMASS);
        massMap.put("Chiron",CHIRONMASS);
        massMap.put("Ceres",CERESMASS);
        massMap.put("Pallas",PALLASMASS);
        massMap.put("Juno",JUNOMASS);
        massMap.put("Vesta",VESTAMASS);
        massMap.put("Eros",EROSMASS);
        massMap.put("Halley",HALLEYMASS);
        massMap.put("Encke",ENCKEMASS);
        massMap.put("67P/Churyumov-Gerasimenko",CGMASS);
        massMap.put("Shoemaker-Levy 9",SL9MASS);
        massMap.put("Hale-Bopp",HBMASS);
        massMap.put("Florence",FLORENCEMASS);
        massMap.put("Ultima Thule",UTMASS);
        massMap.put("Io",IOMASS);
        massMap.put("Europa",EUROPAMASS);
        massMap.put("Ganymede",GANYMEDEMASS);
        massMap.put("Callisto",CALLISTOMASS);
        massMap.put("Mimas",MIMASMASS);
        massMap.put("Enceladus",ENCELADUSMASS);
        massMap.put("Tethys",TETHYSMASS);
        massMap.put("Dione",DIONEMASS);
        massMap.put("Rhea",RHEAMASS);
        massMap.put("Titan",TITANMASS);
        massMap.put("Hyperion",HYPERIONMASS);
        massMap.put("Iapetus",IAPETUSMASS);
        massMap.put("Miranda",MIRANDAMASS);
        massMap.put("Ariel",ARIELMASS);
        massMap.put("Umbriel",UMBRIELMASS);
        massMap.put("Titania",TITANIAMASS);
        massMap.put("Oberon",OBERONMASS);
        massMap.put("Triton",TRITONMASS);
        
        // Standard gravitational parameter in m3/s2
        muMap = new HashMap<>();
        muMap.put("Sun",SUNMU);
        muMap.put("Mercury",MERCURYMU);
        muMap.put("Venus",VENUSMU);
        muMap.put("Earth",EARTHMU);
        muMap.put("Moon",MOONMU);
        muMap.put("Mars",MARSMU);
        muMap.put("Jupiter",JUPITERMU);
        muMap.put("Saturn",SATURNMU);
        muMap.put("Uranus",URANUSMU);
        muMap.put("Neptune",NEPTUNEMU);
        muMap.put("Pluto",PLUTOMU);
        muMap.put("Eris",ERISMU);
        muMap.put("Ceres",CERESMU);
        muMap.put("Pallas",PALLASMU);
        muMap.put("Vesta",VESTAMU);
        muMap.put("Eros",EROSMU);
        muMap.put("Ultima Thule",UTMU);
        muMap.put("Io",IOMU);
        muMap.put("Europa",EUROPAMU);
        muMap.put("Ganymede",GANYMEDEMU);
        muMap.put("Callisto",CALLISTOMU);
        muMap.put("Mimas",MIMASMU);
        muMap.put("Enceladus",ENCELADUSMU);
        muMap.put("Tethys",TETHYSMU);
        muMap.put("Dione",DIONEMU);
        muMap.put("Rhea",RHEAMU);
        muMap.put("Titan",TITANMU);
        muMap.put("Hyperion",HYPERIONMU);
        muMap.put("Iapetus",IAPETUSMU);
        muMap.put("Miranda",MIRANDAMU);
        muMap.put("Ariel",ARIELMU);
        muMap.put("Umbriel",UMBRIELMU);
        muMap.put("Titania",TITANIAMU);
        muMap.put("Oberon",OBERONMU);
        muMap.put("Triton",TRITONMU);
        
        // Diameters in m
        diameterMap = new HashMap<>();
        diameterMap.put("Sun",SUNDIAMETER);
        diameterMap.put("Mercury",MERCURYDIAMETER);
        diameterMap.put("Venus",VENUSDIAMETER);
        diameterMap.put("Earth",EARTHDIAMETER);
        diameterMap.put("Moon",MOONDIAMETER);
        diameterMap.put("Mars",MARSDIAMETER);
        diameterMap.put("Jupiter",JUPITERDIAMETER);
        diameterMap.put("Saturn",SATURNDIAMETER);
        diameterMap.put("Uranus",URANUSDIAMETER);
        diameterMap.put("Neptune",NEPTUNEDIAMETER);
        diameterMap.put("Pluto",PLUTODIAMETER);
        diameterMap.put("Eris",ERISDIAMETER);
        diameterMap.put("Chiron",CHIRONDIAMETER);
        diameterMap.put("Ceres",CERESDIAMETER);
        diameterMap.put("Pallas",PALLASDIAMETER);
        diameterMap.put("Juno",JUNODIAMETER);
        diameterMap.put("Vesta",VESTADIAMETER);
        diameterMap.put("Eros",EROSDIAMETER);
        diameterMap.put("Halley",HALLEYDIAMETER);
        diameterMap.put("Encke",ENCKEDIAMETER);
        diameterMap.put("67P/Churyumov-Gerasimenko",CGDIAMETER);
        diameterMap.put("Shoemaker-Levy 9",SL9DIAMETER);
        diameterMap.put("Hale-Bopp",HBDIAMETER);
        diameterMap.put("Florence",FLORENCEDIAMETER);
        diameterMap.put("Ultima Thule",UTDIAMETER);
        diameterMap.put("Io",IODIAMETER);
        diameterMap.put("Europa",EUROPADIAMETER);
        diameterMap.put("Ganymede",GANYMEDEDIAMETER);
        diameterMap.put("Callisto",CALLISTODIAMETER);
        diameterMap.put("Mimas",MIMASDIAMETER);
        diameterMap.put("Enceladus",ENCELADUSDIAMETER);
        diameterMap.put("Tethys",TETHYSDIAMETER);
        diameterMap.put("Dione",DIONEDIAMETER);
        diameterMap.put("Rhea",RHEADIAMETER);
        diameterMap.put("Titan",TITANDIAMETER);
        diameterMap.put("Hyperion",HYPERIONDIAMETER);
        diameterMap.put("Iapetus",IAPETUSDIAMETER);
        diameterMap.put("Miranda",MIRANDADIAMETER);
        diameterMap.put("Ariel",ARIELDIAMETER);
        diameterMap.put("Umbriel",UMBRIELDIAMETER);
        diameterMap.put("Titania",TITANIADIAMETER);
        diameterMap.put("Oberon",OBERONDIAMETER);
        diameterMap.put("Triton",TRITONDIAMETER);

        // Ellipticity of oblate planet
        ellipticityMap = new HashMap<>();
        ellipticityMap.put("Jupiter",JUPITERELLIPTICITY);
        ellipticityMap.put("Saturn",SATURNELLIPTICITY);
        ellipticityMap.put("Uranus",URANUSELLIPTICITY);
        ellipticityMap.put("Neptune",NEPTUNEELLIPTICITY);

        // Gravatitational parameter [km3/s2] of oblate planet
        oblateMuMap = new HashMap<>();
        oblateMuMap.put("Jupiter",JUPITEROBLATEMU);
        oblateMuMap.put("Saturn",SATURNOBLATEMU);
        oblateMuMap.put("Uranus",URANUSOBLATEMU);
        oblateMuMap.put("Neptune",NEPTUNEOBLATEMU);

        // Equatorial radius [m] of oblate planet
        equatorialRadiusMap = new HashMap<>();
        equatorialRadiusMap.put("Jupiter",JUPITEREQUATORIALRADIUS);
        equatorialRadiusMap.put("Saturn",SATURNEQUATORIALRADIUS);
        equatorialRadiusMap.put("Uranus",URANUSEQUATORIALRADIUS);
        equatorialRadiusMap.put("Neptune",NEPTUNEEQUATORIALRADIUS);

        // Zonal coefficients [-] of oblate planet
        zonalCoefficientsMap = new HashMap<>();
        zonalCoefficientsMap.put("Jupiter",JUPITERZONALCOEFFICIENTS);
        zonalCoefficientsMap.put("Saturn",SATURNZONALCOEFFICIENTS);
        zonalCoefficientsMap.put("Uranus",URANUSZONALCOEFFICIENTS);
        zonalCoefficientsMap.put("Neptune",NEPTUNEZONALCOEFFICIENTS);

        // Parameters of z-axis of oblate planet
        zAxisParametersMap = new HashMap<>();
        zAxisParametersMap.put("Jupiter",JUPITERZAXISPARAMETERS);
        zAxisParametersMap.put("Saturn",SATURNZAXISPARAMETERS);
        zAxisParametersMap.put("Uranus",URANUSZAXISPARAMETERS);
        zAxisParametersMap.put("Neptune",NEPTUNEZAXISPARAMETERS);
        
        // Orbital parameters: Keplerian elements and their rates (Mercury - Pluto)
        orbitParametersMap = new HashMap<>();
        orbitParametersMap.put("Mercury",MERCURYORBITPARS);
        orbitParametersMap.put("Venus",VENUSORBITPARS);
        orbitParametersMap.put("Earth",EARTHORBITPARS);
        orbitParametersMap.put("Moon",MOONORBITPARS);
        orbitParametersMap.put("Mars",MARSORBITPARS);
        orbitParametersMap.put("Jupiter",JUPITERORBITPARS);
        orbitParametersMap.put("Saturn",SATURNORBITPARS);
        orbitParametersMap.put("Uranus",URANUSORBITPARS);
        orbitParametersMap.put("Neptune",NEPTUNEORBITPARS);
        orbitParametersMap.put("Pluto",PLUTOORBITPARS); // Dwarf planet Pluto
        orbitParametersMap.put("Eris",ERISORBITPARS); // Dwarf planet Eris
        orbitParametersMap.put("Chiron",CHIRONORBITPARS); // Centaur astroid Chiron
        orbitParametersMap.put("Ceres",CERESORBITPARS); // Dwarf planet Ceris
        orbitParametersMap.put("Pallas",PALLASORBITPARS); // Asteroid 2 Pallas
        orbitParametersMap.put("Juno",JUNOORBITPARS); // Asteroid 3 Juno
        orbitParametersMap.put("Vesta",VESTAORBITPARS); // Asteroid 4 Vesta
        orbitParametersMap.put("Eros",EROSORBITPARS); // Asteroid 433 Eros
        orbitParametersMap.put("Halley",HALLEYORBITPARS);// Comet P1/Halley
        orbitParametersMap.put("Encke",ENCKEORBITPARS);// Comet P2/Encke
        orbitParametersMap.put("67P/Churyumov-Gerasimenko",CGORBITPARS);// Comet P67/Churyumov-Gerasimenko
        orbitParametersMap.put("Shoemaker-Levy 9",SL9ORBITPARS);// Comet D/1993 F2-A Shoemaker-Levy 9
        orbitParametersMap.put("Hale-Bopp",HBORBITPARS);// Comet C/1995 O1 Hale-Bopp
        orbitParametersMap.put("Florence",FLORENCEORBITPARS);// Asteroid 3122 Florence
        orbitParametersMap.put("Ultima Thule",UTORBITPARS);// Kuiper belt object Ultima Thule
        orbitParametersMap.put("Io",IOORBITPARS);// Io, moon of Jupiter
        orbitParametersMap.put("Europa",EUROPAORBITPARS);// Europa, moon of Jupiter
        orbitParametersMap.put("Ganymede",GANYMEDEORBITPARS);// Ganymede, moon of Jupiter
        orbitParametersMap.put("Callisto",CALLISTOORBITPARS);// Callisto, moon of Jupiter
        orbitParametersMap.put("Mimas",MIMASORBITPARS);// Mimas, moon of Saturn
        orbitParametersMap.put("Enceladus",ENCELADUSORBITPARS);// Enceladus, moon of Saturn
        orbitParametersMap.put("Tethys",TETHYSORBITPARS);// Tethys, moon of Saturn
        orbitParametersMap.put("Dione",DIONEORBITPARS);// Dione, moon of Saturn
        orbitParametersMap.put("Rhea",RHEAORBITPARS);// Rhea, moon of Saturn
        orbitParametersMap.put("Titan",TITANORBITPARS);// Titan, moon of Saturn
        orbitParametersMap.put("Hyperion",HYPERIONORBITPARS);// Hyperion, moon of Saturn
        orbitParametersMap.put("Iapetus",IAPETUSORBITPARS);// Iapetus, moon of Saturn
        orbitParametersMap.put("Miranda",MIRANDAORBITPARS);// Miranda, moon of Uranus
        orbitParametersMap.put("Ariel",ARIELORBITPARS);// Ariel, moon of Uranus
        orbitParametersMap.put("Umbriel",UMBRIELORBITPARS);// Umbriel, moon of Uranus
        orbitParametersMap.put("Titania",TITANIAORBITPARS);// Titania, moon of Uranus
        orbitParametersMap.put("Oberon",OBERONORBITPARS);// Oberon, moon of Uranus
        orbitParametersMap.put("Triton",TRITONORBITPARS);// Triton, moon of Neptune
        
        // Planet names (treat dwarf planets, astroids, and comets as planet)
        planets = new ArrayList<>();
        planets.addAll(orbitParametersMap.keySet());
        planets.remove("Moon");
        planets.remove("Io");
        planets.remove("Europa");
        planets.remove("Ganymede");
        planets.remove("Callisto");
        planets.remove("Mimas");
        planets.remove("Enceladus");
        planets.remove("Tethys");
        planets.remove("Dione");
        planets.remove("Rhea");
        planets.remove("Titan");
        planets.remove("Hyperion");
        planets.remove("Iapetus");
        planets.remove("Miranda");
        planets.remove("Ariel");
        planets.remove("Umbriel");
        planets.remove("Titania");
        planets.remove("Oberon");
        planets.remove("Triton");
        
        // Moon names
        moons = new HashMap<>();
        moons.put("Moon","Earth");
        moons.put("Io","Jupiter");
        moons.put("Europa","Jupiter");
        moons.put("Ganymede","Jupiter");
        moons.put("Callisto","Jupiter");
        moons.put("Mimas","Saturn");
        moons.put("Enceladus","Saturn");
        moons.put("Tethys","Saturn");
        moons.put("Dione","Saturn");
        moons.put("Rhea","Saturn");
        moons.put("Titan","Saturn");
        moons.put("Hyperion","Saturn");
        moons.put("Iapetus","Saturn");
        moons.put("Miranda","Uranus");
        moons.put("Ariel","Uranus");
        moons.put("Umbriel","Uranus");
        moons.put("Titania","Uranus");
        moons.put("Oberon","Uranus");
        moons.put("Triton","Neptune");
    }
    
    /**
     * Get instance of SolarSystemParameters.
     * @return instance
     */
    public static SolarSystemParameters getInstance() {
        if (instance == null) {
            instance = new SolarSystemParameters();
        }
        return instance;
    }
    
    /**
     * Get names of planets.
     * @return names of planets
     */
    public List<String> getPlanets() {
        return Collections.unmodifiableList(planets);
    }
    
    /**
     * Get names of moons.
     * @return names of moons
     */
    public List<String> getMoons() {
        return Collections.unmodifiableList(new ArrayList(moons.keySet()));
    }
    
    /**
     * Get name of planet for moon
     * @param moonName name of moon
     * @return name of planet
     */
    public String getPlanetOfMoon(String moonName) {
        String planetName = moons.get(moonName);
        return planetName;
    }

    /**
     * Get names of moons of planet
     * @param planetName name of planet
     * @return names of moons
     */
    public List<String> getMoonsOfPlanet(String planetName) {
        List<String> moonsOfPlanet = new ArrayList<>();
        for (String moonName : moons.keySet()) {
            if (planetName.equals(getPlanetOfMoon(moonName))) {
                moonsOfPlanet.add(moonName);
            }
        }
        return moonsOfPlanet;
    }
    
    /**
     * Get mass of planet with given name.
     * @param name name of planet
     * @return mass in kg
     */
    public double getMass(String name) {
        return massMap.get(name);
    }
    
    /**
     * Get standard gravitational parameter mu of planet with given name.
     * @param name name of planet
     * @return mu in m3/s2
     */
    public double getMu(String name) {
        // Standard gravitational parameter mu = G*M in m3/s2.
        // The value of mu is known to greater accuracy than either G or M.
        // See https://en.wikipedia.org/wiki/Standard_gravitational_parameter
        if (muMap.containsKey(name)) {
            // Standard gravitational parameter is known
            return muMap.get(name);
        }
        else {
            // Standard gravitational parameter is not known
            return Particle.GRAVITATIONALCONSTANT * massMap.get(name);
        }
    }
    
    /**
     * Get orbital parameters, i.e., Keplerian elements and 
     * their rates, for planet with given name
     * @param name name of planet
     * @return orbital parameters of planet
     */
    public double[] getOrbitParameters (String name) {
        return orbitParametersMap.get(name);
    }
    
    /**
     * Get diameter of body with given name.
     * @param name name of body
     * @return diameter in m
     */
    public double getDiameter(String name) {
        return diameterMap.get(name);
    }

    /**
     * Get ellipticity of oblate planet with given name
     * @param name name of oblate planet
     * @return ellipticity
     */
    public double getEllipticity(String name) {
        return ellipticityMap.get(name);
    }

    /**
     * Get gravitational parameter of oblate planet with given name
     * @param name name of oblate planet
     * @return gravitational parameter in m3/s2
     */
    public double getOblateMu(String name) {
        return oblateMuMap.get(name);
    }

    /**
     * Get equatorial radius of oblate planet with given name
     * @param name name of oblate planet
     * @return equatorial radius in km
     */
    public double getEquatorialRadius(String name) {
        return equatorialRadiusMap.get(name);
    }

    /**
     * Get zonal coefficients of oblate planet with given name
     * @param name name of oblate planet
     * @return zonal coefficients
     */
    public double[] getZonalCoefficients(String name) {
        return zonalCoefficientsMap.get(name);
    }

    /**
     * Get parameters of z-axis of oblate planet with given name
     * @param name name of oblate planet
     * @return parameters of z-axis
     */
    public double[] getZaxisParameters(String name) {
        return zAxisParametersMap.get(name);
    }
}
