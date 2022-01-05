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

import java.io.Serializable;

/**
 * Vector in 3D space.
 * @author Nico Kuijpers
 */
public class Vector3D implements Serializable {
    
    // Default serialVersion id
    private static final long serialVersionUID = 1L;
    
    private double x;
    private double y;
    private double z;
    
    /**
     * Default constructor.
     * Initialize (x,y,z) with (0,0,0).
     */
    public Vector3D() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }
    
    /**
     * Constructor.
     * Initialize (x,y,z) with given values.
     * @param x x-value
     * @param y y-value
     * @param z z-value
     */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Copy constructor.
     * Initialize (x,y,z) with given vector.
     * @param v vector to initialize (x,y,z)
     */
    public Vector3D(Vector3D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }
    
    /**
     * Get x-value of vector.
     * @return x-value
     */
    public double getX() {
        return x;
    }
    
    /**
     * Get y-value of vector.
     * @return y-value
     */
    public double getY() {
        return y;
    }
    
    /**
     * Get z-value of vector.
     * @return z-value
     */
    public double getZ() {
        return z;
    }
    
    /**
     * Normalized vector for this vector.
     * @return normalized vector
     */
    public Vector3D normalize() {
        double mag = magnitude();
        Vector3D normal = new Vector3D();
        if (Math.abs(mag) > 0.0) {
            normal = new Vector3D(x/mag,y/mag,z/mag);
        }
        return normal;
    }
    
    /**
     * Direction from this vector to other vector.
     * @param v other vector
     * @return direction to other vector
     */
    public Vector3D direction(Vector3D v) {
        Vector3D direction = new Vector3D(v.x - x,v.y - y,v.z - z);
        return direction.normalize();
    }
    
    /**
     * Magnitude of this vector.
     * @return magnitude
     */
    public double magnitude() {
        return Math.sqrt(x*x + y*y + z*z);
    }
    
    /**
     * Square of magnitude of this vector
     * @return square of magnitude
     */
    public double magnitudeSquare() {
        return x*x + y*y + z*z;
    }
    
    /**
     * Euclidean distance between this vector and other vector.
     * @param v other vector
     * @return Euclidean distance to other vector
     */
    public double euclideanDistance(Vector3D v) {
        double dx = v.x - x;
        double dy = v.y - y;
        double dz = v.z - z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    /**
     * Square of Euclidean distance between this vector and other vector.
     * @param v
     * @return square of Euclidean distance to other vector
     */
    public double euclideanDistanceSquare(Vector3D v) {
        double dx = v.x - x;
        double dy = v.y - y;
        double dz = v.z - z;
        return dx*dx + dy*dy + dz*dz;
    }
    
    /**
     * Add other vector to this vector.
     * @param v other vector
     * @return new vector which is equal to sum of this vector and other vector
     */
    public Vector3D plus(Vector3D v) {
        return new Vector3D(x+v.x,y+v.y,z+v.z);
    }
    
    /**
     * Subtract other vector from this vector.
     * @param v other vector
     * @return new vector which is equal to other vector subtracted
     * from this vector
     */
    public Vector3D minus(Vector3D v) {
        return new Vector3D(x-v.x,y-v.y,z-v.z);
    }
    
    /**
     * Scalar product of this vector and given scalar.
     * @param scalar scalar
     * @return new vector which is scalar product of this vector and
     * given scalar
     */
    public Vector3D scalarProduct(double scalar) {
        return new Vector3D(x*scalar,y*scalar,z*scalar);
    }
    
    /**
     * Dot product of this vector and other vector.
     * @param v other vector
     * @return dot product of this vector and other vector
     */
    public double dotProduct(Vector3D v) {
        // Use algebraic definition of dot product
        return x*v.x + y*v.y + z*v.z;
    }

    /**
     * Angle in radians between this vector and other vector.
     * @param v other vector
     * @return angle in radians between this vector and other vector
     */
    public double angleRad(Vector3D v) {
        return Math.acos(this.dotProduct(v) / (this.magnitude() * v.magnitude()));
    }

    /**
     * Angle in degrees between this vector and other vector.
     * @param v other vector
     * @return angle in degrees between this vector and other vector
     */
    public double angleDeg(Vector3D v) {
        return Math.toDegrees(angleRad(v));
    }

    /**
     * Rotate vector along three axes defined by three vectors.
     * @param vx
     * @param vy
     * @param vz
     * @return rotated vector
     */
    public Vector3D rotate (Vector3D vx, Vector3D vy, Vector3D vz) {
        double rx = vx.x*x + vx.y*y + vx.z*z;
        double ry = vy.x*x + vy.y*y + vy.z*z;
        double rz = vz.x*x + vz.y*y + vz.z*z;
        return new Vector3D(rx,ry,rz);
    }

    /**
     * Cross product of this vector and other vector.
     * @param v other vector
     * @return cross product of this vector and other vector
     */
    public Vector3D crossProduct(Vector3D v) {
        // https://en.wikipedia.org/wiki/Cross_product
        double rx = (y * v.z) - (z * v.y);
        double ry = (z * v.x) - (x * v.z);
        double rz = (x * v.y) - (y * v.x); 
        return new Vector3D(rx,ry,rz);
    }
    
    /**
     * Rotate vector along x-axis
     * @param angle rotation angle in degrees
     * @return new vector which is rotated along x-axis
     */
    public Vector3D rotateXdeg(double angle) {
        // https://en.wikipedia.org/wiki/Rotation_matrix
        double ry = y * Math.cos(Math.toRadians(angle)) - 
                    z * Math.sin(Math.toRadians(angle));
        double rz = y * Math.sin(Math.toRadians(angle)) + 
                    z * Math.cos(Math.toRadians(angle));
        return new Vector3D(x,ry,rz);
    }
    
    /**
     * Rotate vector along y-axis.
     * @param angle rotation angle in degrees
     * @return new vector which is rotated along y-axis
     */
    public Vector3D rotateYdeg(double angle) {
        // https://en.wikipedia.org/wiki/Rotation_matrix
        double rx =  x * Math.cos(Math.toRadians(angle)) +
                     z * Math.sin(Math.toRadians(angle));
        double rz = -x * Math.sin(Math.toRadians(angle)) +
                     z * Math.cos(Math.toRadians(angle));
        return new Vector3D(rx,y,rz);
    }
    
    /**
     * Rotate vector along z-axis.
     * @param angle rotation angle in degrees
     * @return new vector which is rotated along z-axis
     */
    public Vector3D rotateZdeg(double angle) {
        // https://en.wikipedia.org/wiki/Rotation_matrix
        double rx = x * Math.cos(Math.toRadians(angle)) -
                    y * Math.sin(Math.toRadians(angle));
        double ry = x * Math.sin(Math.toRadians(angle)) +
                    y * Math.cos(Math.toRadians(angle));
        return new Vector3D(rx,ry,z);
    }
    
    /**
     * Rotate vector along x-axis.
     * @param angle rotation angle in radians
     * @return new vector which is rotated along x-axis.
     */
    public Vector3D rotateXrad(double angle) {
        // https://en.wikipedia.org/wiki/Rotation_matrix
        double ry = y * Math.cos(angle) - 
                    z * Math.sin(angle);
        double rz = y * Math.sin(angle) + 
                    z * Math.cos(angle);
        return new Vector3D(x,ry,rz);
    }
    
    /**
     * Rotate vector along y-axis.
     * @param angle rotation angle in radians
     * @return new vector which is rotated along y-axis
     */
    public Vector3D rotateYrad(double angle) {
        // https://en.wikipedia.org/wiki/Rotation_matrix
        double rx =  x * Math.cos(angle) +
                     z * Math.sin(angle);
        double rz = -x * Math.sin(angle) +
                     z * Math.cos(angle);
        return new Vector3D(rx,y,rz);
    }
    
    /**
     * Rotate vector along z-axis
     * @param angle rotation angle in radians
     * @return new vector which is rotated along y-axis
     */
    public Vector3D rotateZrad(double angle) {
        // https://en.wikipedia.org/wiki/Rotation_matrix
        double rx = x * Math.cos(angle) -
                    y * Math.sin(angle);
        double ry = x * Math.sin(angle) +
                    y * Math.cos(angle);
        return new Vector3D(rx,ry,z);
    }

    /**
     * Add vector to this vector.
     * @param vector vector to be added
     */
    public void addVector(Vector3D vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
    }
    
    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }
}
