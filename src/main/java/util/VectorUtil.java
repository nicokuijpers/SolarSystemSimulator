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
package util;

/**
 * Utility class to support geometric computation using vectors in 3D space.
 * @author Nico Kuijpers
 */
public class VectorUtil {

    /**
     * Compute the intersection between a line and a sphere
     * @param directionLine direction of the line (unit vector)
     * @param originLine    origin of the line
     * @param centerSphere  center position of the sphere
     * @param radius        radius of the sphere
     * @return intersection closest to origin of the line if exists, null pointer otherwise
     */
    public static Vector3D computeIntersectionLineSphere(Vector3D directionLine, Vector3D originLine, Vector3D centerSphere, double radius) {

        /* https://en.wikipedia.org/wiki/Line–sphere_intersection
           Equation for a sphere is ||x_vec - c_vec||^2 = r^2,
           where c_vec is center point,
           r is radius, and
           x_vec describes points on the sphere

           Equation for a line is x_vec = o_vec + d l_vec,
           where d is distance along line from starting point,
           l_vec is direction of the line (unit vector),
           o_vec is origin of the line, and
           x_vec describes points on the line

           Solve equation ||o_vec + d l_vec - c_vec||^2 = r^2

           This equation is equivalent to
           a d^2 + b d + c, where
           a = l_vec . l_vec = ||l_vec||^2
           b = 2(l_vec . (o_vec - c_vec))
           c = (o_vec - c_vec) . (o_vec - c_vec) - r^2 = ||o_vec - c_vec||^2 - r^2

           Solution:
           d = -(l_vec . (o_vec - c_vec)) +/- sqrt(D),
           where discriminant D = (l_vec . (o_vec - c_vec))^2 - (||o_vec - c_vec||^2 - r^2)
           if D < 0, no solution exists
           if D = 0, exactly one solution exists
           if D > 0, two solutions exist
         */

        double a = directionLine.dotProduct(directionLine);
        double b = 2*(directionLine.dotProduct(originLine.minus(centerSphere)));
        Vector3D v = originLine.minus(centerSphere);
        double c = v.dotProduct(v) - radius*radius;
        double D = b*b - 4*a*c;
        if (D < 0) {
            return null;
        }
        double d = (-b - Math.sqrt(D))/(2*a);
        return originLine.plus(directionLine.scalarProduct(d));
    }
}
