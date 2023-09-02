/*
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
package util;

public class MathUtil {

    /**
     * Inverse hyperbolic sine of a double value.
     *
     * @param x the value for which the inverse hyperbolic sine is to be computed
     * @return the arc hyperbolic sine of x
     */
    public static double asinh(double x) {
        // https://en.wikipedia.org/wiki/Inverse_hyperbolic_functions
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    /**
     * Inverse hyperbolic cosine of a double value.
     *
     * @param x the value for which the inverse hyperbolic cosine is to be computed
     * @return the arc hyperbolic cosine of x
     */
    public static double acosh(double x) {
        // https://en.wikipedia.org/wiki/Inverse_hyperbolic_functions
        return Math.log(x + Math.sqrt(x * x - 1.0));
    }
}
