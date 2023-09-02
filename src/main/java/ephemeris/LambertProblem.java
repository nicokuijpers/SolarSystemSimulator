/*
 * The java code for the LambertProblem class is based on the C++ implementation
 * created by the PyKEP development team, Advanced Concepts Team (ACT),
 * European Space Agency (ESA). This code has been made publicly available under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * URL to original source code in C++:
 * https://github.com/esa/pykep/blob/master/src/lambert_problem.cpp
 *
 * Copyright (c) 2023 Nico Kuijpers
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

import application.SolarSystemException;
import util.MathUtil;
import util.Vector3D;

public class LambertProblem {

    // Cartesian components of position1
    private Vector3D position1; // m_r1 in C++ code

    // Cartesian components of position2
    private Vector3D position2; // m_r2 in C++ code

    // Time of flight from position1 to position2
    private double timeOfFlight; // m_tof in C++ code

    // Standard gravitational parameters
    private double mu; // m_mu in C++ code

    // Chord
    private double chord; // m_c in C++ code

    // Semiperimeter
    private double semiperimeter; // m_s in C++ code

    // Lambda
    private double lambda; // m_lambda in C++ code

    // Cartesian components of the velocities at position1 for all Nmax*2 + 1 solutions
    private Vector3D[] allVelocities1; // m_v1 in C++ code

    // Cartesian components of the velocities at position2 for all Nmax*2 + 1 solutions
    private Vector3D[] allVelocities2; // m_v2 in C++ code

    // Number of iterations taken to compute each one of the solutions
    private int[] allIters; // m_iters in C++ code

    // x variable for each solution found (0 revs, 1,1,2,2,3,3 ... N,N)
    private double[] allX; // m_x in C++ code

    // Maximum number of revolutions Nmax. The number of solutions to the problem will be Nmax*2 + 1
    private int maxNumberRevolutions; // m_Nmax in C++ code

    /**
     * Get Vector3D with the cartesian components of position1
     */
    public Vector3D getPosition1() {
        return position1;
    }

    /**
     * Get Vector3D with the cartesian components of position2
     */
    public Vector3D getPosition2() {
        return position2;
    }

    /**
     * Get the time of flight between position1 and position2
     */
    public double getTimeOfFlight() {
        return timeOfFlight;
    }

    /**
     * Get non-dimensional time of flight
     */
    public double getNonDimensionalTimeOfFlight() {
        return timeOfFlight * Math.sqrt(2.0 * mu / semiperimeter / semiperimeter / semiperimeter);
    }

    /**
     * Get the standard gravitational parameter
     */
    public double getMu() {
        return mu;
    }

    /**
     * Get chord
     */
    public double getChord() {
        return chord;
    }

    /**
     * Get semiperimeter
     */
    public double getSemiperimeter() {
        return semiperimeter;
    }

    /**
     * Get lambda
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Get array of Vector3D with the cartesian components of the velocities at position1 for
     * all Nmax*2 + 1 solutions
     */
    public Vector3D[] getAllVelocities1() {
        return allVelocities1;
    }

    /**
     * Get array of Vector3D with the cartesian components of the velocities at position2 for
     * all Nmax*2 + 1 solutions
     */
    public Vector3D[] getAllVelocities2() {
        return allVelocities2;
    }

    /**
     * Get array of integers containing the number of iterations taken to compute each one of the solutions
     */
    public int[] allIters() {
        return allIters;
    }

    /**
     * Get array of doubles with the x variable for each solution found (0 revs, 1,1,2,2,3,3 ... N,N)
     */
    public double[] allX() {
        return allX;
    }

    /**
     * Get the maximum number of revolutions Nmax. The number of solutions to the problem will be Nmax*2 + 1
     */
    public int getMaxNumberRevolutions() {
        return maxNumberRevolutions;
    }

    /**
     * Constructs and solves a Lambert problem.
     *
     * @param position1 first cartesian position
     * @param position2 second cartesian position
     * @param tof       time of flight
     * @param mu        gravity parameter
     * @param cw        when true a retrograde orbit is assumed
     * @param multiRevs maximum number of multirevolutions to compute
     * @throws SolarSystemException when tof <= 0.0 or mu <= 0.0
     */
    public LambertProblem(Vector3D position1, Vector3D position2, double tof, double mu, boolean cw, int multiRevs) throws SolarSystemException {

        /*
         * Revisiting Lambert’s problem
         * Dario Izzo
         * Celest Mech Dyn Astr, 2014
         * DOI 10.1007/s10569-014-9587-y
         * https://www.esa.int/gsp/ACT/doc/MAD/pub/ACT-RPR-MAD-2014-RevisitingLambertProblem.pdf
         *
         * The code, written in C++ and exposed to Python, is made available as part of the open source
         * project PyKEP from the European Space Agency github repository https://github.com/esa/pykep/.
         */

        // 0 - Sanity checks
        this.position1 = new Vector3D(position1);
        this.position2 = new Vector3D(position2);
        if (tof <= 0.0) {
            throw new SolarSystemException("LambertProblem: Time of flight is negative");
        }
        this.timeOfFlight = tof;
        if (mu <= 0.0) {
            throw new SolarSystemException("LambertProblem: Gravity parameter is zero or negative");
        }
        this.mu = mu;

        // 1 - Getting lambda and T
        double c = position2.euclideanDistance(position1);
        chord = c; // m_c in C++ code
        double r1 = position1.magnitude(); // R1 in C++ code
        double r2 = position2.magnitude(); // R2 in C++ code
        double s = (c + r1 + r2) / 2.0;
        semiperimeter = s; // m_s in C++ code

        Vector3D normPosition1 = position1.normalize(); // ir1 in C++ code
        Vector3D normPosition2 = position2.normalize(); // ir2 in C++ code
        Vector3D crossPosition1Position2 = normPosition1.crossProduct(normPosition2); // ih in C++ code
        Vector3D normCrossPosition1Position2 = crossPosition1Position2.normalize(); // ih in C++ code
        if (Math.abs(normCrossPosition1Position2.getZ()) < 1.0E-14) {
            throw new SolarSystemException("LambertProblem: The angular momentum vector has no z component, " +
                    "impossible to define automatically clock or counterclockwise");
        }

        double lambda2 = 1.0 - c/s;
        lambda = Math.sqrt(lambda2); // m_lambda in C++ code

        Vector3D crossPosition1; // it1 in C++ code
        Vector3D crossPosition2; // it2 in C++ code
        if (normCrossPosition1Position2.getZ() < 0.0) {
            // Transfer angle is larger than 180 degrees as seen from above the z axis
            lambda = -lambda;
            crossPosition1 = normPosition1.crossProduct(normCrossPosition1Position2);
            crossPosition2 = normPosition1.crossProduct(normCrossPosition1Position2);
        }
        else {
            crossPosition1 = normCrossPosition1Position2.crossProduct(normPosition1);
            crossPosition2 = normCrossPosition1Position2.crossProduct(normPosition2);
        }
        Vector3D normCrossPosition1 = crossPosition1.normalize(); // it1 in C++ code
        Vector3D normCrossPosition2 = crossPosition2.normalize(); // it2 in C++ code

        if (cw) {
            // Retrograde motion
            lambda = -lambda;
            normCrossPosition1.scalarProduct(-1.0); // it2 in C++ code
            normCrossPosition2.scalarProduct(-1.0); // it2 in C++ code
        }

        double lambda3 = lambda * lambda2;
        double T = Math.sqrt(2.0 * mu / s / s / s) * tof;

        // 2 - We now have lambda, T and we will find all x
        // 2.1 - Let us first detect the maximum number of revolutions for which there exists a solution
        int Nmax = (int) Math.floor(T / Math.PI);
        double T00 = Math.acos(lambda) + lambda * Math.sqrt(1.0 - lambda2);
        double T0 = (T00 + Nmax * Math.PI);
        double T1 = 2.0 / 3.0 * (1.0 - lambda3);
        double DT = 0.0;
        double DDT = 0.0;
        double DDDT = 0.0;
        if (Nmax > 0) {
            if (T < T0) { // We use Halley iterations to find xM and TM
                int it = 0;
                double err = 1.0;
                double T_min = T0;
                double x_old = 0.0, x_new = 0.0;
                do {
                    double[] result = dTdx(x_old, T_min);
                    DT = result[0];
                    DDT = result[1];
                    DDDT = result[2];
                    if (DT != 0.0) {
                        x_new = x_old - DT * DDT / (DDT * DDT - DT * DDDT / 2.0);
                    }
                    err = Math.abs(x_old - x_new);
                    T_min = x2tof(x_new, Nmax);
                    x_old = x_new;
                    it++;
                } while (err >= 1.0E-13 && it <= 12);
                if (T_min > T) {
                    Nmax -= 1;
                }
            }
        }
        // We exit this if clause with Nmax being the maximum number of revolutions
        // for which there exists a solution. We crop it to m_multi_revs
        Nmax = Math.min(multiRevs, Nmax);
        maxNumberRevolutions = Nmax; // m_Nmax in C++ code

        // 2.2 We now allocate the memory for the output variables
        int size = Nmax*2 + 1;
        allVelocities1 = new Vector3D[size]; // m_v1 in C++ code
        allVelocities2 = new Vector3D[size]; // m_v2 in C++ code
        allIters = new int[size]; // m_iters in C++ code
        allX = new double[size]; // m_x in C++ code

        // 3 - We may now find all solutions in x,y
        // 3.1 0 rev solution
        // 3.1.1 initial guess
        if (T >= T00) {
            allX[0] = -(T - T00) / (T - T00 + 4);
        } else if (T <= T1) {
            allX[0] = T1 * (T1 - T) / (2.0 / 5.0 * (1 - lambda2 * lambda3) * T) + 1;
        } else {
            allX[0] = Math.pow((T / T00), 0.69314718055994529 / Math.log(T1 / T00)) - 1.0;
        }
        // 3.1.2 Householder iterations
        houseHolder(T, 0, 0, 1.0E-05, 15);
        // 3.2 multi rev solutions
        double tmp;
        for (int i = 1; i < Nmax + 1; ++i) {
            // 3.2.1 left Householder iterations
            tmp = Math.pow((i * Math.PI + Math.PI) / (8.0 * T), 2.0 / 3.0);
            allX[2 * i - 1] = (tmp - 1) / (tmp + 1);
            houseHolder(T, 2*i - 1, i, 1.0E-08, 15);
            // 3.2.1 right Householder iterations
            tmp = Math.pow((8.0 * T) / (i * Math.PI), 2.0 / 3.0);
            allX[2 * i] = (tmp - 1) / (tmp + 1);
            houseHolder(T, 2*i, i, 1.0E-08, 15);
        }

        // 4 - For each found x value we reconstruct the terminal velocities
        double gamma = Math.sqrt(mu * s / 2.0);
        double rho = (r1 - r2) / c;
        double sigma = Math.sqrt(1.0 - rho * rho);
        double vr1, vt1, vr2, vt2, y;
        for (int i = 0; i < allX.length; ++i) {
            y = Math.sqrt(1.0 - lambda2 + lambda2 * allX[i] * allX[i]);
            vr1 = gamma * ((lambda * y - allX[i]) - rho * (lambda * y + allX[i])) / r1;
            vr2 = -gamma * ((lambda * y - allX[i]) + rho * (lambda * y + allX[i])) / r2;
            double vt = gamma * sigma * (y + lambda * allX[i]);
            vt1 = vt / r1;
            vt2 = vt / r2;
            allVelocities1[i] = (normPosition1.scalarProduct(vr1)).plus(normCrossPosition1.scalarProduct(vt1));
            allVelocities2[i] = (normPosition2.scalarProduct(vr2)).plus(normCrossPosition2.scalarProduct(vt2));
            /* C++ code
            for (int j = 0; j < 3; ++j)
                m_v1[i][j] = vr1 * ir1[j] + vt1 * it1[j];
            for (int j = 0; j < 3; ++j)
                m_v2[i][j] = vr2 * ir2[j] + vt2 * it2[j];
            */
        }
    }

    private void houseHolder(double T, int index, int N, double eps, int iterMax) {
        double x0 = allX[index];
        int it = 0;
        double err = 1.0;
        while ((err > eps) && (it < iterMax)) {
            double tof = x2tof(x0, N);
            double[] result = dTdx(tof, x0);
            double DT = result[0];
            double DDT = result[1];
            double DDDT = result[2];
            double delta = tof - T;
            double DT2 = DT * DT;
            double xnew = x0 - delta * (DT2 - delta * DDT / 2.0) / (DT * (DT2 - delta * DDT) + DDDT * delta * delta / 6.0);
            err = Math.abs(x0 - xnew);
            x0 = xnew;
            it++;
        }
        allIters[index] = it;
        allX[index] = x0;
    }

    private double[] dTdx(double T, double x) {
        double l2 = lambda * lambda;
        double l3 = l2 * lambda;
        double umx2 = 1.0 - x * x;
        double y = Math.sqrt(1.0 - l2 * umx2);
        double y2 = y * y;
        double y3 = y2 * y;
        double DT = 1.0 / umx2 * (3.0 * T * x - 2.0 + 2.0 * l3 * x / y);
        double DDT = 1.0 / umx2 * (3.0 * T + 5.0 * x * DT + 2.0 * (1.0 - l2) * l3 / y3);
        double DDDT = 1.0 / umx2 * (7.0 * x * DDT + 8.0 * DT - 6.0 * (1.0 - l2) * l2 * l3 * x / y3 / y2);
        return new double[]{DT, DDT, DDDT};
    }

    private double x2tof2(double x, int N) {
        double a = 1.0 / (1.0 - x * x);
        double tof;
        if (a > 0) // ellipse
        {
            double alfa = 2.0 * Math.acos(x);
            double beta = 2.0 * Math.asin(Math.sqrt(lambda * lambda / a));
            if (lambda < 0.0) beta = -beta;
            tof = ((a * Math.sqrt(a) * ((alfa - Math.sin(alfa)) - (beta - Math.sin(beta)) + 2.0 * Math.PI * N)) / 2.0);
        } else {
            double alfa = 2.0 * MathUtil.acosh(x);
            double beta = 2.0 * MathUtil.asinh(Math.sqrt(-lambda * lambda / a));
            if (lambda < 0.0) beta = -beta;
            tof = (-a * Math.sqrt(-a) * ((beta - Math.sinh(beta)) - (alfa - Math.sinh(alfa))) / 2.0);
        }
        return tof;
    }

    private double x2tof(double x, int N) {
        double battin = 0.01;
        double lagrange = 0.2;
        double dist = Math.abs(x - 1);
        double tof;
        if (dist < lagrange && dist > battin) {
            // We use Lagrange tof expression
            return x2tof2(x, N);
        }
        double K = lambda * lambda;
        double E = x * x - 1.0;
        double rho = Math.abs(E);
        double z = Math.sqrt(1 + K * E);
        if (dist < battin) {
            // We use Battin series tof expression
            double eta = z - lambda * x;
            double S1 = 0.5 * (1.0 - lambda - x * eta);
            double Q = hypergeometricF(S1, 1e-11);
            Q = 4.0 / 3.0 * Q;
            tof = (eta * eta * eta * Q + 4.0 * lambda * eta) / 2.0 + N * Math.PI / Math.pow(rho, 1.5);
            return tof;
        } else {
            // We use Lancaster tof expression
            double y = Math.sqrt(rho);
            double g = x * z - lambda * E;
            double d = 0.0;
            if (E < 0) {
                double l = Math.acos(g);
                d = N * Math.PI + l;
            } else {
                double f = y * (z - lambda * x);
                d = Math.log(f + g);
            }
            tof = (x - lambda * z - d / y) / E;
            return tof;
        }
    }

    private double hypergeometricF(double z, double tol) {
        double Sj = 1.0;
        double Cj = 1.0;
        double err = 1.0;
        double Cj1 = 0.0;
        double Sj1 = 0.0;
        int j = 0;
        while (err > tol) {
            Cj1 = Cj * (3.0 + j) * (1.0 + j) / (2.5 + j) * z / (j + 1);
            Sj1 = Sj + Cj1;
            err = Math.abs(Cj1);
            Sj = Sj1;
            Cj = Cj1;
            j = j + 1;
        }
        return Sj;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Lambert's problem:\n");
        result.append("Mu             = " + mu + "\n");
        result.append("Position 1     = " + position1 + "\n");
        result.append("Position 2     = " + position2 + "\n");
        result.append("Time of flight = " + timeOfFlight + "\n");
        result.append("Chord          = " + chord + "\n");
        result.append("Semiperimeter  = " + semiperimeter + "\n");
        result.append("Lambda         = " + lambda + "\n");
        result.append("Non-dimensional time of flight = " + getNonDimensionalTimeOfFlight() + "\n");
        result.append("Maximum number of revolutions: " + maxNumberRevolutions + "\n");
        result.append("Solutions: \n");
        result.append("0 revs, Iters: " + allIters[0] + ", x: " + allX[0]);
        result.append(", a: " + semiperimeter / 2.0 / (1.0 - allX[0] * allX[0]) + "\n");
        result.append("\tv1= " + allVelocities1[0] + " v2= " + allVelocities2[0] + "\n");
        for (int i = 0; i < maxNumberRevolutions; ++i) {
            int index = 1 + 2*i;
            result.append("" + (i + 1) + " revs, left. Iters: " + allIters[index]);
            result.append(", x: " + allX[index]);
            result.append(", a: " + semiperimeter / 2.0 / (1.0 - allX[index] * allX[index]) + "\n");
            result.append("\tv1= " + allVelocities1[index] + " v2= " + allVelocities2[index] + "\n");
            index++;
            result.append("" + (i + 1) + " revs, right. Iters: " + allIters[index]);
            result.append(", x: " + allX[index]);
            result.append(", a: " + semiperimeter / 2.0 / (1.0 - allX[index] * allX[index]) + "\n");
            result.append("\tv1= " + allVelocities1[index] + " v2= " + allVelocities2[index] + "\n");
        }
        return result.toString();
    }
}
