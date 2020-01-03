/*
 * Java version by Marco Brassé, restructured from original Fortran source:
 * ftp://ftp.imcce.fr/pub/ephem/satel/gust86
 *-----------------------------------------------------------------------
 *     CALCUL DES COORDONNEES DES SATELLITES D'URANUS (GUST86)
 *     version 0.0
 *     URANIAN satellites theory  Laskar and Jacobson (1987)
 *     URANUS position (VSOP85, Bretagnon 1985)
 *     several routines from G. Francou
 *     (c)  Bureau des Longitudes 1988
 *-----------------------------------------------------------------------
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
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
 * Main class for feasibility study
 * @author Marco Brassé
 */

public class EphemerisUranusMoons implements IEphemeris {

    private static double DMOD(double a, double b)
    {
        double res;
        // according to
        // https://gcc.gnu.org/onlinedocs/gfortran/MOD.html
        // return A - (INT(A/P) * P)
        // implementation belowed checked against GNU F77 compiler (Marco)
        if (b<=0.0) return a;
        if (a>=0.0)
        {
            return a - (Math.floor(a/b)*b);
        }
        else
        {
            return -(Math.abs(a) - (Math.floor(Math.abs(a)/b)*b));
        }
    }
    //
    // KEPLKH (RL,RK,RH,F,IT,IPRT)
    //
    public double KEPLKH(double RL, double RK, double RH)
    {
        double F=0.0;
        if (RL==0.0) {
            return F;
        }
        double ITMAX=20;
        double EPS=1.0E-16;
        double F0=RL;
        double E=0.0;
        double E0=Math.abs(RL);
        for (int IT=1; IT<=ITMAX; IT++)
        {

            double SF=Math.sin(F0);
            double CF=Math.cos(F0);
            double FF0 =F0-RK*SF+RH*CF-RL;
            double FPF0=1.0-RK*CF-RH*SF;
            double SDIR=FF0/FPF0;
            int K=-1;
            do
            {
                K=K+1;
                F=F0-SDIR*Math.pow((0.5),K);
                E=Math.abs(F-F0);
            }
            while (E>E0);
            // note K==0 replaced by K==1
            if (K==0 && E<EPS && FF0<EPS) return F;
            F0=F;
            E0=E;
        }
        return F;
    }



    //
    // SUBROUTINE MIREL (T,RN,RL,RK,RH,RQ,RP)
    //
    private void MIRANDA(double T)
    {
        S_RN = 4443522.67E-06
                -34.92E-06*Math.cos(AN[1]-3.0*AN[2]+2.0*AN[3])
                +8.47E-06*Math.cos(2.0*AN[1]-6.0*AN[2]+4.0*AN[3])
                +1.31E-06*Math.cos(3.0*AN[1]-9.0*AN[2]+6.0*AN[3])
                -52.28E-06*Math.cos(AN[1]-AN[2])
                -136.65E-06*Math.cos(2.0*AN[1]-2.0*AN[2]);
        S_RL =  -238051.58e-06
                +4445190.55E-06*T
                +25472.17E-06*Math.sin(AN[1]-3.0*AN[2]+2.0*AN[3])
                -3088.31E-06*Math.sin(2.0*AN[1]-6.0*AN[2]+4.0*AN[3])
                -318.10E-06*Math.sin(3.0*AN[1]-9.0*AN[2]+6.0*AN[3])
                -37.49E-06*Math.sin(4.0*AN[1]-12.0*AN[2]+8.0*AN[3])
                -57.85E-06*Math.sin(AN[1]-AN[2])
                -62.32E-06*Math.sin(2.0*AN[1]-2.0*AN[2])
                -27.95E-06*Math.sin(3.0*AN[1]-3.0*AN[2]);

        S_RK = 1312.38E-06*Math.cos(AE[1])
                +71.81E-06*Math.cos(AE[2])
                +69.77E-06*Math.cos(AE[3])
                +6.75E-06*Math.cos(AE[4])
                +6.27E-06*Math.cos(AE[5])
                -123.31E-06*Math.cos(-AN[1]+2.0*AN[2])
                +39.52E-06*Math.cos(-2.0*AN[1]+3.0*AN[2])
                +194.10E-06*Math.cos(AN[1]);

        S_RH = 1312.38E-06*Math.sin(AE[1])
                +71.81E-06*Math.sin(AE[2])
                +69.77E-06*Math.sin(AE[3])
                +6.75E-06*Math.sin(AE[4])
                +6.27E-06*Math.sin(AE[5])
                -123.31E-06*Math.sin(-AN[1]+2.0*AN[2])
                +39.52E-06*Math.sin(-2.0*AN[1]+3.0*AN[2])
                +194.10E-06*Math.sin(AN[1]);

        S_RQ = 37871.71E-06*Math.cos(AI[1])
                +27.01E-06*Math.cos(AI[2])
                +30.76E-06*Math.cos(AI[3])
                +12.18E-06*Math.cos(AI[4])
                +5.37E-06*Math.cos(AI[5]);

        S_RP = 37871.71E-06*Math.sin(AI[1])
                +27.01E-06*Math.sin(AI[2])
                +30.76E-06*Math.sin(AI[3])
                +12.18E-06*Math.sin(AI[4])
                +5.37E-06*Math.sin(AI[5]);
    }
    //
    // SUBROUTINE ARIEL (T,RN,RL,RK,RH,RQ,RP)
    //
    private void ARIEL(double T)
    {
        S_RN = 2492542.57E-06
                +2.55E-06*Math.cos(AN[1]-3.0*AN[2]+2.0*AN[3])
                -42.16E-06*Math.cos(AN[2]-AN[3]);
        S_RL =   3098046.41E-06
                +2492952.52E-06*T
                -1860.50E-06*Math.sin(AN[1]-3.0*AN[2]+2.0*AN[3])
                +219.99E-06*Math.sin(2.0*AN[1]-6.0*AN[2]+4.0*AN[3])
                +23.10E-06*Math.sin(3.0*AN[1]-9.0*AN[2]+6.0*AN[3])
                +4.30E-06*Math.sin(4.0*AN[1]-12.0*AN[2]+8.0*AN[3])
                -90.11E-06*Math.sin(AN[2]-AN[3])
                -91.07E-06*Math.sin(2.0*AN[2]-2.0*AN[3])
                -42.75E-06*Math.sin(3.0*AN[2]-3.0*AN[3])
                -16.49E-06*Math.sin(2.0*AN[2]-2.0*AN[4]);
        S_RK =    -3.35E-06*Math.cos(AE[1])
                +1187.63E-06*Math.cos(AE[2])
                +861.59E-06*Math.cos(AE[3])
                +71.50E-06*Math.cos(AE[4])
                +55.59E-06*Math.cos(AE[5])
                -84.60E-06*Math.cos(-AN[2]+2.0*AN[3])
                +91.81E-06*Math.cos(-2.0*AN[2]+3.0*AN[3])
                +20.03E-06*Math.cos(-AN[2]+2.0*AN[4])
                +89.77E-06*Math.cos(AN[2]);
        S_RH =    -3.35E-06*Math.sin(AE[1])
                +1187.63E-06*Math.sin(AE[2])
                +861.59E-06*Math.sin(AE[3])
                +71.50E-06*Math.sin(AE[4])
                +55.59E-06*Math.sin(AE[5])
                -84.60E-06*Math.sin(-AN[2]+2.0*AN[3])
                +91.81E-06*Math.sin(-2.0*AN[2]+3.0*AN[3])
                +20.03E-06*Math.sin(-AN[2]+2.0*AN[4])
                +89.77E-06*Math.sin(AN[2]);
        S_RQ =   -121.75E-06*Math.cos(AI[1])
                +358.25E-06*Math.cos(AI[2])
                +290.08E-06*Math.cos(AI[3])
                +97.78E-06*Math.cos(AI[4])
                +33.97E-06*Math.cos(AI[5]);
        S_RP =   -121.75E-06*Math.sin(AI[1])
                +358.25E-06*Math.sin(AI[2])
                +290.08E-06*Math.sin(AI[3])
                +97.78E-06*Math.sin(AI[4])
                +33.97E-06*Math.sin(AI[5]);
    }
    //
    // SUBROUTINE UMBRIEL (T,RN,RL,RK,RH,RQ,RP)
    //
    private void UMBRIEL(double T)
    {
        S_RN = 1515954.90E-06
                +9.74E-06*Math.cos(AN[3]-2.0*AN[4]+AE[3])
                -106.00E-06*Math.cos(AN[2]-AN[3])
                +54.16E-06*Math.cos(2.0*AN[2]-2.0*AN[3])
                -23.59E-06*Math.cos(AN[3]-AN[4])
                -70.70E-06*Math.cos(2.0*AN[3]-2.0*AN[4])
                -36.28E-06*Math.cos(3.0*AN[3]-3.0*AN[4]);
        double RL1= 2285401.69E-06
                +1516148.11E-06*T
                +660.57E-06*Math.sin(AN[1]-3.0*AN[2]+2.0*AN[3])
                -76.51E-06*Math.sin(2.0*AN[1]-6.0*AN[2]+4.0*AN[3])
                -8.96E-06*Math.sin(3.0*AN[1]-9.0*AN[2]+6.0*AN[3])
                -2.53E-06*Math.sin(4.0*AN[1]-12.0*AN[2]+8.0*AN[3])
                -52.91E-06*Math.sin(AN[3]-4.0*AN[4]+3.0*AN[5])
                -7.34E-06*Math.sin(AN[3]-2.0*AN[4]+AE[5])
                -1.83E-06*Math.sin(AN[3]-2.0*AN[4]+AE[4])
                +147.91E-06*Math.sin(AN[3]-2.0*AN[4]+AE[3]);
        double RL2=   -7.77E-06*Math.sin(AN[3]-2.0*AN[4]+AE[2])
                +97.76E-06*Math.sin(AN[2]-AN[3])
                +73.13E-06*Math.sin(2.0*AN[2]-2.0*AN[3])
                +34.71E-06*Math.sin(3.0*AN[2]-3.0*AN[3])
                +18.89E-06*Math.sin(4.0*AN[2]-4.0*AN[3])
                -67.89E-06*Math.sin(AN[3]-AN[4])
                -82.86E-06*Math.sin(2.0*AN[3]-2.0*AN[4]);
        double RL3=    -33.81E-06*Math.sin(3.0*AN[3]-3.0*AN[4])
                -15.79E-06*Math.sin(4.0*AN[3]-4.0*AN[4])
                -10.21E-06*Math.sin(AN[3]-AN[5])
                -17.08E-06*Math.sin(2.0*AN[3]-2.0*AN[5]);
        S_RL=RL1+RL2+RL3;
        double RK1=   -0.21E-06*Math.cos(AE[1])
                -227.95E-06*Math.cos(AE[2])
                +3904.69E-06*Math.cos(AE[3])
                +309.17E-06*Math.cos(AE[4])
                +221.92E-06*Math.cos(AE[5])
                +29.34E-06*Math.cos(AN[2])
                +26.20E-06*Math.cos(AN[3])
                +51.19E-06*Math.cos(-AN[2]+2.0*AN[3])
                -103.86E-06*Math.cos(-2.0*AN[2]+3.0*AN[3])
                -27.16E-06*Math.cos(-3.0*AN[2]+4.0*AN[3]);
        double RK2=    -16.22E-06*Math.cos(AN[4])
                +549.23E-06*Math.cos(-AN[3]+2.0*AN[4])
                +34.70E-06*Math.cos(-2.0*AN[3]+3.0*AN[4])
                +12.81E-06*Math.cos(-3.0*AN[3]+4.0*AN[4])
                +21.81E-06*Math.cos(-AN[3]+2.0*AN[5])
                +46.25E-06*Math.cos(AN[3]);
        S_RK=RK1+RK2;
        double RH1=   -0.21E-06*Math.sin(AE[1])
                -227.95E-06*Math.sin(AE[2])
                +3904.69E-06*Math.sin(AE[3])
                +309.17E-06*Math.sin(AE[4])
                +221.92E-06*Math.sin(AE[5])
                +29.34E-06*Math.sin(AN[2])
                +26.20E-06*Math.sin(AN[3])
                +51.19E-06*Math.sin(-AN[2]+2.0*AN[3])
                -103.86E-06*Math.sin(-2.0*AN[2]+3.0*AN[3])
                -27.16E-06*Math.sin(-3.0*AN[2]+4.0*AN[3]);
        double RH2=    -16.22E-06*Math.sin(AN[4])
                +549.23E-06*Math.sin(-AN[3]+2.0*AN[4])
                +34.70E-06*Math.sin(-2.0*AN[3]+3.0*AN[4])
                +12.81E-06*Math.sin(-3.0*AN[3]+4.0*AN[4])
                +21.81E-06*Math.sin(-AN[3]+2.0*AN[5])
                +46.25E-06*Math.sin(AN[3]);
        S_RH=RH1+RH2;
        S_RQ=    -10.86E-06*Math.cos(AI[1])
                -81.51E-06*Math.cos(AI[2])
                +1113.36E-06*Math.cos(AI[3])
                +350.14E-06*Math.cos(AI[4])
                +106.50E-06*Math.cos(AI[5]);
        S_RP=  -10.86E-06*Math.sin(AI[1])
                -81.51E-06*Math.sin(AI[2])
                +1113.36E-06*Math.sin(AI[3])
                +350.14E-06*Math.sin(AI[4])
                +106.50E-06*Math.sin(AI[5]);
    }
    //
    // SUBROUTINE TITEL (T,RN,RL,RK,RH,RQ,RP)
    //
    private void TITANIA(double T)
    {
        double RN1 = 721663.16E-06
                -2.64E-06*Math.cos(AN[3]-2.0*AN[4]+AE[3])
                -2.16E-06*Math.cos(2.0*AN[4]-3.0*AN[5]+AE[5])
                +6.45E-06*Math.cos(2.0*AN[4]-3.0*AN[5]+AE[4])
                -1.11E-06*Math.cos(2.0*AN[4]-3.0*AN[5]+AE[3]);
        double RN2 =    -62.23E-06*Math.cos(AN[2]-AN[4])
                -56.13E-06*Math.cos(AN[3]-AN[4])
                -39.94E-06*Math.cos(AN[4]-AN[5])
                -91.85E-06*Math.cos(2.0*AN[4]-2.0*AN[5])
                -58.31E-06*Math.cos(3.0*AN[4]-3.0*AN[5])
                -38.60E-06*Math.cos(4.0*AN[4]-4.0*AN[5])
                -26.18E-06*Math.cos(5.0*AN[4]-5.0*AN[5])
                -18.06E-06*Math.cos(6.0*AN[4]-6.0*AN[5]);
        S_RN=RN1+RN2;
        double RL1 =  856358.79E-06
                +721718.51E-06*T
                +20.61E-06*Math.sin(AN[3]-4.0*AN[4]+3.0*AN[5])
                -2.07E-06*Math.sin(AN[3]-2.0*AN[4]+AE[5])
                -2.88E-06*Math.sin(AN[3]-2.0*AN[4]+AE[4])
                -40.79E-06*Math.sin(AN[3]-2.0*AN[4]+AE[3])
                +2.11E-06*Math.sin(AN[3]-2.0*AN[4]+AE[2])
                -51.83E-06*Math.sin(2.0*AN[4]-3.0*AN[5]+AE[5])
                +159.87E-06*Math.sin(2.0*AN[4]-3.0*AN[5]+AE[4]);
        double RL2 =     -35.05E-06*Math.sin(2.0*AN[4]-3.0*AN[5]+AE[3])
                -1.56E-06*Math.sin(3.0*AN[4]-4.0*AN[5]+AE[5])
                +40.54E-06*Math.sin(AN[2]-AN[4])
                +46.17E-06*Math.sin(AN[3]-AN[4])
                -317.76E-06*Math.sin(AN[4]-AN[5])
                -305.59E-06*Math.sin(2.0*AN[4]-2.0*AN[5])
                -148.36E-06*Math.sin(3.0*AN[4]-3.0*AN[5])
                -82.92E-06*Math.sin(4.0*AN[4]-4.0*AN[5]);
        double RL3 =     -49.98E-06*Math.sin(5.0*AN[4]-5.0*AN[5])
                -31.56E-06*Math.sin(6.0*AN[4]-6.0*AN[5])
                -20.56E-06*Math.sin(7.0*AN[4]-7.0*AN[5])
                -13.69E-06*Math.sin(8.0*AN[4]-8.0*AN[5]);
        S_RL=RL1+RL2+RL3;
        double RK1 =       -0.02E-06*Math.cos(AE[1])
                -1.29E-06*Math.cos(AE[2])
                -324.51E-06*Math.cos(AE[3])
                +932.81E-06*Math.cos(AE[4])
                +1120.89E-06*Math.cos(AE[5])
                +33.86E-06*Math.cos(AN[2])
                +17.46E-06*Math.cos(AN[4])
                +16.58E-06*Math.cos(-AN[2]+2.0*AN[4])
                +28.89E-06*Math.cos(AN[3])
                -35.86E-06*Math.cos(-AN[3]+2.0*AN[4]);
        double RK2 =      -17.86E-06*Math.cos(AN[4])
                -32.10E-06*Math.cos(AN[5])
                -177.83E-06*Math.cos(-AN[4]+2.0*AN[5])
                +793.43E-06*Math.cos(-2.0*AN[4]+3.0*AN[5])
                +99.48E-06*Math.cos(-3.0*AN[4]+4.0*AN[5])
                +44.83E-06*Math.cos(-4.0*AN[4]+5.0*AN[5])
                +25.13E-06*Math.cos(-5.0*AN[4]+6.0*AN[5])
                +15.43E-06*Math.cos(-6.0*AN[4]+7.0*AN[5]);
        S_RK=RK1+RK2;
        double RH1 =       -0.02E-06*Math.sin(AE[1])
                -1.29E-06*Math.sin(AE[2])
                -324.51E-06*Math.sin(AE[3])
                +932.81E-06*Math.sin(AE[4])
                +1120.89E-06*Math.sin(AE[5])
                +33.86E-06*Math.sin(AN[2])
                +17.46E-06*Math.sin(AN[4])
                +16.58E-06*Math.sin(-AN[2]+2.0*AN[4])
                +28.89E-06*Math.sin(AN[3])
                -35.86E-06*Math.sin(-AN[3]+2.0*AN[4]);
        double RH2 =      -17.86E-06*Math.sin(AN[4])
                -32.10E-06*Math.sin(AN[5])
                -177.83E-06*Math.sin(-AN[4]+2.0*AN[5])
                +793.43E-06*Math.sin(-2.0*AN[4]+3.0*AN[5])
                +99.48E-06*Math.sin(-3.0*AN[4]+4.0*AN[5])
                +44.83E-06*Math.sin(-4.0*AN[4]+5.0*AN[5])
                +25.13E-06*Math.sin(-5.0*AN[4]+6.0*AN[5])
                +15.43E-06*Math.sin(-6.0*AN[4]+7.0*AN[5]);

        S_RH=RH1+RH2;
        S_RQ =        -1.43E-06*Math.cos(AI[1])
                -1.06E-06*Math.cos(AI[2])
                -140.13E-06*Math.cos(AI[3])
                +685.72E-06*Math.cos(AI[4])
                +378.32E-06*Math.cos(AI[5]);
        S_RP =        -1.43E-06*Math.sin(AI[1])
                -1.06E-06*Math.sin(AI[2])
                -140.13E-06*Math.sin(AI[3])
                +685.72E-06*Math.sin(AI[4])
                +378.32E-06*Math.sin(AI[5]);
    }
    //
    // SUBROUTINE OBREL (T,RN,RL,RK,RH,RQ,RP)
    //
    private void OBERON(double T)
    {
        double RN1 = 466580.54E-06
                +2.08E-06*Math.cos(2.0*AN[4]-3.0*AN[5]+AE[5])
                -6.22E-06*Math.cos(2.0*AN[4]-3.0*AN[5]+AE[4])
                +1.07E-06*Math.cos(2.0*AN[4]-3.0*AN[5]+AE[3])
                -43.10E-06*Math.cos(AN[2]-AN[5]);
        double RN2 =    -38.94E-06*Math.cos(AN[3]-AN[5])
                -80.11E-06*Math.cos(AN[4]-AN[5])
                +59.06E-06*Math.cos(2.0*AN[4]-2.0*AN[5])
                +37.49E-06*Math.cos(3.0*AN[4]-3.0*AN[5])
                +24.82E-06*Math.cos(4.0*AN[4]-4.0*AN[5])
                +16.84E-06*Math.cos(5.0*AN[4]-5.0*AN[5]);
        S_RN=RN1+RN2;
        double RL1 =  -915591.80E-06
                +466692.12E-06*T
                -7.82E-06*Math.sin(AN[3]-4.0*AN[4]+3.0*AN[5])
                +51.29E-06*Math.sin(2.0*AN[4]-3.0*AN[5]+AE[5])
                -158.24E-06*Math.sin(2.0*AN[4]-3.0*AN[5]+AE[4])
                +34.51E-06*Math.sin(2.0*AN[4]-3.0*AN[5]+AE[3])
                +47.51E-06*Math.sin(AN[2]-AN[5])
                +38.96E-06*Math.sin(AN[3]-AN[5])
                +359.73E-06*Math.sin(AN[4]-AN[5]);
        double RL2 =      282.78E-06*Math.sin(2.0*AN[4]-2.0*AN[5])
                +138.60E-06*Math.sin(3.0*AN[4]-3.0*AN[5])
                +78.03E-06*Math.sin(4.0*AN[4]-4.0*AN[5])
                +47.29E-06*Math.sin(5.0*AN[4]-5.0*AN[5])
                +30.00E-06*Math.sin(6.0*AN[4]-6.0*AN[5])
                +19.62E-06*Math.sin(7.0*AN[4]-7.0*AN[5])
                +13.11E-06*Math.sin(8.0*AN[4]-8.0*AN[5]);
        S_RL=RL1+RL2;
        double RK1 =       0.00E-06*Math.cos(AE[1])
                -0.35E-06*Math.cos(AE[2])
                +74.53E-06*Math.cos(AE[3])
                -758.68E-06*Math.cos(AE[4])
                +1397.34E-06*Math.cos(AE[5])
                +39.00E-06*Math.cos(AN[2])
                +17.66E-06*Math.cos(-AN[2]+2.0*AN[5]);
        double RK2 =      32.42E-06*Math.cos(AN[3])
                +79.75E-06*Math.cos(AN[4])
                +75.66E-06*Math.cos(AN[5])
                +134.04E-06*Math.cos(-AN[4]+2.0*AN[5])
                -987.26E-06*Math.cos(-2.0*AN[4]+3.0*AN[5])
                -126.09E-06*Math.cos(-3.0*AN[4]+4.0*AN[5])
                -57.42E-06*Math.cos(-4.0*AN[4]+5.0*AN[5])
                -32.41E-06*Math.cos(-5.0*AN[4]+6.0*AN[5])
                -19.99E-06*Math.cos(-6.0*AN[4]+7.0*AN[5])
                -12.94E-06*Math.cos(-7.0*AN[4]+8.0*AN[5]);
        S_RK=RK1+RK2;
        double RH1 =       0.00E-06*Math.sin(AE[1])
                -0.35E-06*Math.sin(AE[2])
                +74.53E-06*Math.sin(AE[3])
                -758.68E-06*Math.sin(AE[4])
                +1397.34E-06*Math.sin(AE[5])
                +39.00E-06*Math.sin(AN[2])
                +17.66E-06*Math.sin(-AN[2]+2.0*AN[5]);
        double RH2 =      32.42E-06*Math.sin(AN[3])
                +79.75E-06*Math.sin(AN[4])
                +75.66E-06*Math.sin(AN[5])
                +134.04E-06*Math.sin(-AN[4]+2.0*AN[5])
                -987.26E-06*Math.sin(-2.0*AN[4]+3.0*AN[5])
                -126.09E-06*Math.sin(-3.0*AN[4]+4.0*AN[5])
                -57.42E-06*Math.sin(-4.0*AN[4]+5.0*AN[5])
                -32.41E-06*Math.sin(-5.0*AN[4]+6.0*AN[5])
                -19.99E-06*Math.sin(-6.0*AN[4]+7.0*AN[5])
                -12.94E-06*Math.sin(-7.0*AN[4]+8.0*AN[5]);
        S_RH=RH1+RH2;
        S_RQ =       -0.44E-06*Math.cos(AI[1])
                -0.31E-06*Math.cos(AI[2])
                +36.89E-06*Math.cos(AI[3])
                -596.33E-06*Math.cos(AI[4])
                +451.69E-06*Math.cos(AI[5]);
        S_RP =       -0.44E-06*Math.sin(AI[1])
                -0.31E-06*Math.sin(AI[2])
                +36.89E-06*Math.sin(AI[3])
                -596.33E-06*Math.sin(AI[4])
                +451.69E-06*Math.sin(AI[5]);
    }
    //
    // SUBROUTINE ELLIPX (ELL,RMU,XYZ)
    //
    /*
     *---- ELLIPX  1.1  18 MARS 1986  J. LASKAR -----------------------------
     *
     *     CALCUL DES COORDONNEES RECTANGULAIRES (POSITIONS ET VITESSES) ET
     *     DE LEURS DERIVEES PARTIELLES PAR RAPPORT AUX ELEMENTS ELLIPTIQUES
     *     A PARTIR DES ELEMENTS ELLIPTIQUES.
     *     IS         : NUMBER SAT
     *     ELL(6)     : ELEMENTS ELLIPTIQUES A: DEMI-GRAND AXE
     *                                       L: LONGITUDE MOYENNE
     *                                       K: EXC*COS(LONG NOEUD+ ARG PERI)
     *                                       H: EXC*SIN(LONG NOEUD+ ARG PERI)
     *                                       Q: SIN(I/2)*COS(LONG NOEUD)
     *                                       P: SIN(I/2)*SIN(LONG NOEUD)
     *     RMU        : CONSTANTE DE GRAVITATION DU PROBLEME DE DEUX CORPS
     *                  RMU = G*M1*(1+M2/M1) M1 MASSE CENTRALE
     *                                       M2 MASSE DU CORPS CONSIDERE
     *     XYZ(6)     : (1:3) POSITIONS ET (4:6) VITESSES
     *     SUBROUTINE UTILISEE : KEPLKH
     *
     */
    private void ELLIPX( int IS, double[][] ELL, double RMU[], double[][] XYZ)
    {
        double ROT[][]=new double[4][3];
        double TX1[] = new double[3];
        double TX1T[] = new double[3];
        double RA,RL,RK,RH,RQ,RP,F;
        double RN,PHI,RKI,PSI;

        RA=ELL[1][IS];
        RL=ELL[2][IS];
        RK=ELL[3][IS];
        RH=ELL[4][IS];
        RQ=ELL[5][IS];
        RP=ELL[6][IS];
        RN=Math.sqrt(RMU[IS]/(RA*RA*RA));
        PHI=Math.sqrt(1.0-RK*RK-RH*RH);
        RKI=Math.sqrt(1.0-RQ*RQ-RP*RP);
        PSI=1.0/(1.0+PHI);
        //
        ROT[1][1]=1.0-2.0*RP*RP;
        ROT[1][2]=2.0*RP*RQ;
        ROT[2][1]=2.0*RP*RQ;
        ROT[2][2]=1.0-2.0*RQ*RQ;
        ROT[3][1]=-2.0*RP*RKI;
        ROT[3][2]= 2.0*RQ*RKI;

        F=KEPLKH (RL,RK,RH);
        double SF    = Math.sin(F);
        double CF    = Math.cos(F);
        double RLMF  =-RK*SF+RH*CF;
        double UMRSA =RK*CF+RH*SF;
        double ASR   =1.0/(1.0-UMRSA);
        double RSA   =1.0/ASR;        // variable not used ???
        double RNA2SR=RN*RA*ASR;

        TX1[1] =RA*(CF-PSI*RH*RLMF-RK);
        TX1[2] =RA*(SF+PSI*RK*RLMF-RH);
        TX1T[1]=RNA2SR*(-SF+PSI*RH*UMRSA);
        TX1T[2]=RNA2SR*( CF-PSI*RK*UMRSA);

        for (int I=1; I<=3; I++)
        {
            XYZ[I][IS]  =0.0;
            XYZ[I+3][IS]=0.0;
            for (int J=1; J<=2; J++)
            {
                XYZ[I][IS]  =XYZ[I][IS]  +ROT[I][J]*TX1[J];
                XYZ[I+3][IS]=XYZ[I+3][IS]+ROT[I][J]*TX1T[J];
            }
        }
    }

    //
    // SUBROUTINE GUST86
    //
    private void GUST86(double aDateJulian, int IS, double[] XYZ, double[] VXYZ)
    {
        double T;
        double RL=0.0;
        XYZ[0]=0.0;
        XYZ[1]=0.0;
        XYZ[2]=0.0;
        VXYZ[0]=0.0;
        VXYZ[1]=0.0;
        VXYZ[2]=0.0;
        for (int j = 1; j<=5; j++) {
            for (int i = 1; i <= 6; i++) R[i][j]=0.0;
        }
        T=aDateJulian-T0;
        //System.out.println("Time is " + T);
        for (int I=1; I<=5; I++) {
            AN[I]=FQN[I]*T+PHN[I];
            AE[I]=FQE[I]*DGRAD/ANJ*T+PHE[I];
            AI[I]=FQI[I]*DGRAD/ANJ*T+PHI[I];
            AN[I]=DMOD(AN[I],DPI);
            AE[I]=DMOD(AE[I],DPI);
            AI[I]=DMOD(AI[I],DPI);
        }
        if (IS==1) MIRANDA(T);
        else if (IS==2) ARIEL(T);
        else if (IS==3) UMBRIEL(T);
        else if (IS==4) TITANIA(T);
        else if (IS==5) OBERON(T);
        else MIRANDA(T);
        EL[1][IS]=Math.pow((RMU[IS]*SEJ2/S_RN/S_RN),(1.0/3.0));
        RL=DMOD(S_RL,DPI);
        if (RL<0.0) RL=RL+DPI;
        EL[2][IS]=RL;
        EL[3][IS]=S_RK;
        EL[4][IS]=S_RH;
        EL[5][IS]=S_RQ;
        EL[6][IS]=S_RP;
        ELLIPX (IS,EL,RMU,XU);

        for (int IV=1; IV<=3; IV++)
        {
            XE[IV][IS]=0.0;
            XE[IV+3][IS]=0.0;
            for (int J=1; J<=3; J++)
            {
                XE[IV][IS]=XE[IV][IS]+TRANS[IV][J]*XU[J][IS];
                XE[IV+3][IS]=XE[IV+3][IS]+TRANS[IV][J]*XU[J+3][IS];
            }
        }
        for (int IV=1; IV<=6; IV++)
        {
            R[IV][IS]=XE[IV][IS];
        }
        // assign result variables
        XYZ[0]=R[1][IS];
        XYZ[1]=R[2][IS];
        XYZ[2]=R[3][IS];
        VXYZ[0]=R[4][IS];
        VXYZ[1]=R[5][IS];
        VXYZ[2]=R[6][IS];
    }

    //
    // SUBROUTINE GUST86 Initialize
    //
    private void GUST86_INIT()
    {
        DPI=2.0*Math.PI;
        DGRAD = Math.PI/180.0;
        double SEJ = 86400.0;
        SEJ2 = SEJ*SEJ;
        ANJ = 365.25;
        double GMU = GMSU;
        for (int i=1; i<=5; i++) GMU=GMU-GMS[i];
        RMU[0] = 0.0;
        for (int i=1; i<=5; i++) RMU[i]=GMU+GMS[i];
        // ALF is defined as constant
        // DEL is defined as constant
        SA=Math.sin(ALF);
        CA=Math.cos(ALF);
        SD=Math.sin(DEL);
        CD=Math.cos(DEL);
        for (int i=0; i<=3; i++) {
            for (int j=0; j<=3; j++)
            {
                TRANS[i][j]=0.0;
            }
        }
        TRANS[1][1]=SA;
        TRANS[2][1]=-CA;
        TRANS[3][1]=0.0;
        TRANS[1][2]=CA*SD;
        TRANS[2][2]=SA*SD;
        TRANS[3][2]=-CD;
        TRANS[1][3]=CA*CD;
        TRANS[2][3]=SA*CD;
        TRANS[3][3]=SD;
    }
    // added first (0.0) element due to indexing from 1 in fortran
    private final double FQN[] = { 0.0,4445190.550E-06,2492952.519E-06,1516148.111E-06, 721718.509E-06,466692.120E-06 };
    private final double FQE[] = { 0.0,20.082,6.217,2.865,2.078,0.386 };
    private final double FQI[]= { 0.0,-20.309,-6.288,-2.836,-1.843,-0.259 } ;
    private final double PHN[] = { 0.0,-238051.0E-06,3098046.0E-06,2285402.0E-06,856359.0E-06,-915592.0E-06 };
    private final double PHE[] = {0.0,0.611392,2.408974,2.067774,0.735131,0.426767};
    private final double PHI[] = {0.0,5.702313,0.395757,0.589326,1.746237,4.206896 };
    private final double GMS[] = { 0.0,4.4,86.1,84.0,230.0,200.0 };
    private final double GMSU = 5794554.5;
    private final double ALF=1.3370385623111227;
    private final double DEL=0.26236177166923647;
    //private final double ALF = 76.60666666666667;
    //private final double DEL = 15.03222222222222;
    //private final double UA = 149597870.0;
    private final double T1950 = 2433282.423;
    private final double T2000 = 2451545.0;
    private final double T0 = 2444239.5;
    //
    private double R[][];
    private double EL[][];
    private double XU[][];
    private double XE[][];
    private double RMU[];
    private double TRANS[][];
    private double AN[];
    private double AE[];
    private double AI[];
    private double DPI;
    private double DGRAD;
    private double SEJ2;
    private double SA;
    private double CA;
    private double SD;
    private double CD;
    private double ANJ;
    //
    private double S_RN;
    private double S_RL;
    private double S_RK;
    private double S_RH;
    private double S_RQ;
    private double S_RP;

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // First valid date for accurate computation
    private final GregorianCalendar firstValidDateAccurate;

    // Last valid date for accurate computation
    private final GregorianCalendar lastValidDateAccurate;

    // Bodies for which ephemeris can be computed or approximated
    private static List<String> bodies;

    // Singleton instance
    private static IEphemeris instance = null;


    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisUranusMoons() {

        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.add("Miranda");
        bodies.add("Ariel");
        bodies.add("Umbriel");
        bodies.add("Titania");
        bodies.add("Oberon");

        // First valid date 3000 BC
        firstValidDate = new GregorianCalendar(3000,0,1);
        firstValidDate.set(Calendar.ERA, GregorianCalendar.BC);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date 3000 AD
        lastValidDate = new GregorianCalendar(3000,0,1);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for accurate computation 1150 AD
        firstValidDateAccurate = new GregorianCalendar(1150,0,1);
        firstValidDateAccurate .setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for accurate computation 2750 AD
        lastValidDateAccurate = new GregorianCalendar(2750,0,1);
        lastValidDateAccurate .setTimeZone(TimeZone.getTimeZone("UTC"));

        // Standard gravitional parameters
        double au = SolarSystemParameters.ASTRONOMICALUNIT;
        double nrSecsDay = 86400.0;
        //
        // initalize javafied Fortran variables
        //
        R = new double[7][6];
        EL = new double[7][6];
        XU = new double[7][6];
        XE = new double [7][6];
        RMU = new double[6];
        TRANS = new double[4][4];
        AN = new double[6];
        AE = new double[6];
        AI = new double[6];
        // read and process Fortran data file
        GUST86_INIT();
    }


    /**
     * Get instance of EphemerisUranusMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisUranusMoons();
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
            throw new IllegalArgumentException("Unknown body " + name + " for Galilean Moons Ephemeris");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Galilean Moons Ephemeris");
        }

        // Compute Ephemeris Time
        double ET = JulianDateConverter.convertCalendarToJulianDate(date);

        // Determine satellite number
        int nsat;
        if ("Miranda".equals(name)) nsat = 1;
        else if ("Ariel".equals(name)) nsat = 2;
        else if ("Umbriel".equals(name)) nsat = 3;
        else if ("Titania".equals(name)) nsat = 4;
        else if ("Oberon".equals(name)) nsat = 5;
        else /* choose Miranda as default */ nsat = 1;

        // Check whether Accurate Ephemeris can be used
        if (!date.before(firstValidDateAccurate) && !date.after(lastValidDateAccurate)) {
            return getPositionVelocity(ET, nsat, 0);
        }
        else {
            return getPositionVelocity(ET, nsat, 0);
        }
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

    private Vector3D[] getPositionVelocity(double ET, int nsat, int not_used)
    {
        //!*  From original explanation in Fortran code:
        //!*  ET : Time in Julian days (TDB) )
        //!*  nsat: satellite number 1,2,3,4,5
        //!*  return: (x,y,z,vx,vy,vz), Uranus centered EME50 coordinates
        //
        // Positions and velocities of the satellites  (nsat)
        // 1 MIRANDA.
        // 2 ARIEL.
        // 3 UMBRIEL.
        // 4 TITANIA.
        // 5 OBERON.
        //
        // referred to the center of Uranus and to the mean
        // ecliptic and mean equinox for J2000.0 epoch
        //
        //double UA = SolarSystemParameters.ASTRONOMICALUNIT;
        //double SEC_PER_YR=86400.0*365.25;
        double[] XYZ0 = new double[3];
        double[] VXYZ0 = new double[3];
        double[] XYZ = new double[3];
        double[] VXYZ = new double[3];
        // call Javafied Fortran subroutine
        GUST86(ET, nsat, XYZ0, VXYZ0);
        // B1950 => J2000 conversion (by single precession matrix )
        // see http://www2.arnes.si/~gljsentvid10/b1950.html
        // TODO USE EphmerisUlil.transformFromB1950ToJ2000()
        XYZ[0] = 0.9999257080 * XYZ0[0] - 0.0111789372 * XYZ0[1] - 0.0048590035 * XYZ0[2];
        XYZ[1] = 0.0111789372 * XYZ0[0] + 0.9999375134 * XYZ0[1] - 0.0000271626 * XYZ0[2];
        XYZ[2] = 0.0048590036 * XYZ0[0] - 0.0000271579 * XYZ0[1] + 0.9999881946 * XYZ0[2];
        VXYZ[0] = 0.9999257080 * VXYZ0[0] - 0.0111789372 * VXYZ0[1] - 0.0048590035 * VXYZ0[2];
        VXYZ[1] = 0.0111789372 * VXYZ0[0] + 0.9999375134 * VXYZ0[1] - 0.0000271626 * VXYZ0[2];
        VXYZ[2] = 0.0048590036 * VXYZ0[0] - 0.0000271579 * VXYZ0[1] + 0.9999881946 * VXYZ0[2];

        // Position in m, already in J2000 mean ecliptic coordinates (meters)
        Vector3D position = new Vector3D(XYZ[0]*1000.0, XYZ[1]*1000.0, XYZ[2]*1000.0);

        // Velocity in m/s, already in J2000 mean ecliptic coordinates (meters/sec)
        Vector3D velocity = new Vector3D(VXYZ[0]*1000.0, VXYZ[1]*1000.0, VXYZ[2]*1000.0);

        // Position and velocity are computed for J2000 frame
        Vector3D positionInvTrans = EphemerisUtil.inverseTransformJ2000(position);
        Vector3D velocityInvTrans = EphemerisUtil.inverseTransformJ2000(velocity);
        return new Vector3D[]{positionInvTrans,velocityInvTrans};
    }

    public static void main (String[] args) {
        Vector3D[] posVel = new Vector3D[2];
        EphemerisUranusMoons obj=new EphemerisUranusMoons();
        // Date 1995-07-10, 14:00:00
        posVel=obj.getPositionVelocity(2449909.0833333335, 1, 0); // MIRANDA
        System.out.println("Resulting positionVelocity [MIRANDA]");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
        posVel=obj.getPositionVelocity(2449909.0833333335, 2, 0); // ARIEL
        System.out.println("Resulting positionVelocity [ARIEL]");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
        posVel=obj.getPositionVelocity(2449909.0833333335, 3, 0); // UMBRIEL
        System.out.println("Resulting positionVelocity [UMBRIEL]");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
        posVel=obj.getPositionVelocity(2449909.0833333335, 4, 0); // TITANIA
        System.out.println("Resulting positionVelocity [TITANIA]");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
        posVel=obj.getPositionVelocity(2449909.0833333335, 5, 0); // OBERON
        System.out.println("Resulting positionVelocity [OBERON]");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
    }
}

