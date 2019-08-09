/*
 * Copyright (c) 2017 Nico Kuijpers
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for class Vector3D.
 * @author Nico Kuijpers
 */
public class Vector3DTest {
    
    public Vector3DTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDefaultConstructor() {
        Vector3D v = new Vector3D();
        assertEquals(0.0,v.getX(),1.0E-14);
        assertEquals(0.0,v.getY(),1.0E-14);
        assertEquals(0.0,v.getZ(),1.0E-14);
    }
    
    @Test
    public void testConstructor() {
        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        Vector3D v = new Vector3D(x,y,z);
        assertEquals(x,v.getX(),1.0E-14);
        assertEquals(y,v.getY(),1.0E-14);
        assertEquals(z,v.getZ(),1.0E-14);
    }
    
    @Test
    public void testCopyConstructor() {
        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        Vector3D v = new Vector3D(x,y,z);
        Vector3D u = new Vector3D(v);
        assertEquals(x,u.getX(),1.0E-14);
        assertEquals(y,u.getY(),1.0E-14);
        assertEquals(z,u.getZ(),1.0E-14);
    }
    
    @Test
    public void testNormalizeX() {
        Vector3D v = new Vector3D(2.0,0.0,0.0);
        Vector3D expected = new Vector3D(1.0,0.0,0.0);
        Vector3D actual = v.normalize();
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testNormalizeY() {
        Vector3D v = new Vector3D(0.0,2.0,0.0);
        Vector3D expected = new Vector3D(0.0,1.0,0.0);
        Vector3D actual = v.normalize();
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testNormalizeZ() {
        Vector3D v = new Vector3D(0.0,0.0,2.0);
        Vector3D expected = new Vector3D(0.0,0.0,1.0);
        Vector3D actual = v.normalize();
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testNormalizeZeroVector() {
        Vector3D v = new Vector3D();
        Vector3D expected = new Vector3D();
        Vector3D actual = v.normalize();
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testDirectionX() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        Vector3D u = new Vector3D(4.0,2.0,2.0);
        Vector3D expected = new Vector3D(1.0,0.0,0.0);
        Vector3D actual = v.direction(u);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testDirectionY() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        Vector3D u = new Vector3D(2.0,4.0,2.0);
        Vector3D expected = new Vector3D(0.0,1.0,0.0);
        Vector3D actual = v.direction(u);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testDirectionZ() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        Vector3D u = new Vector3D(2.0,2.0,4.0);
        Vector3D expected = new Vector3D(0.0,0.0,1.0);
        Vector3D actual = v.direction(u);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testDirectionEqualVectors() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        Vector3D u = new Vector3D(2.0,2.0,2.0);
        Vector3D expected = new Vector3D(0.0,0.0,0.0);
        Vector3D actual = v.direction(u);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testMagnitude() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        double expected = Math.sqrt(12.0);
        double actual = v.magnitude();
        assertEquals(expected,actual,1.0E-14);
    }
    
    @Test
    public void testMagnitudeSquare() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        double expected = 12.0;
        double actual = v.magnitudeSquare();
        assertEquals(expected,actual,1.0E-14);
    }
    
    @Test
    public void testEuclideanDistance() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        Vector3D u = new Vector3D(2.0,2.0,4.0);
        double expected = 2.0;
        double actual = v.euclideanDistance(u);
        assertEquals(expected,actual,1.0E-14);
    }
    
    @Test
    public void testEuclideanDistanceSquare() {
        Vector3D v = new Vector3D(2.0,2.0,2.0);
        Vector3D u = new Vector3D(2.0,2.0,4.0);
        double expected = 4.0;
        double actual = v.euclideanDistanceSquare(u);
        assertEquals(expected,actual,1.0E-14);
    }
        
    @Test
    public void testPlus() {
        Vector3D v = new Vector3D(1.0,2.0,3.0);
        Vector3D u = new Vector3D(4.0,5.0,6.0);
        Vector3D expected = new Vector3D(5.0,7.0,9.0);
        Vector3D actual = v.plus(u);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testMinus() {
        Vector3D v = new Vector3D(4.0,5.0,6.0);
        Vector3D u = new Vector3D(1.0,2.0,3.0);
        Vector3D expected = new Vector3D(3.0,3.0,3.0);
        Vector3D actual = v.minus(u);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testScalarProduct() {
        Vector3D v = new Vector3D(1.0,2.0,3.0);
        Vector3D expected = new Vector3D(2.0,4.0,6.0);
        Vector3D actual = v.scalarProduct(2.0);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testDotProduct() {
        Vector3D v = new Vector3D(1.0,2.0,3.0);
        Vector3D u = new Vector3D(4.0,5.0,6.0);
        double expected = 32.0;
        double actual = v.dotProduct(u);
        assertEquals(expected,actual,1.0E-14);
    }

    @Test
    public void testAngleRad() {
        Vector3D v = new Vector3D(3.0,0.0,0.0);
        Vector3D u = new Vector3D(0.0,5.0,0.0);
        double expected = 0.5*Math.PI;
        double actual = v.angleRad(u);
        assertEquals(expected,actual,1.0E-14);
    }

    @Test
    public void testAngleDeg() {
        Vector3D v = new Vector3D(3.0,0.0,0.0);
        Vector3D u = new Vector3D(0.0,5.0,0.0);
        double expected = 90.0;
        double actual = v.angleDeg(u);
        assertEquals(expected,actual,1.0E-14);
    }
    
    @Test
    public void testCrossProductA() {
        Vector3D v = new Vector3D();
        Vector3D u = new Vector3D();
        Vector3D expected = new Vector3D();
        Vector3D actual = v.crossProduct(u);
        assertEquals(true,equalVectors(expected,actual));
    }

    @Test
    public void testCrossProductB() {
        Vector3D v = new Vector3D(1.0,0.0,0.0);
        Vector3D u = new Vector3D(0.0,1.0,0.0);
        Vector3D expected = new Vector3D(0.0,0.0,1.0);
        Vector3D actual = v.crossProduct(u);
        assertEquals(true,equalVectors(expected,actual));
    }

    @Test
    public void testCrossProductC() {
        Vector3D v = new Vector3D(0.0,1.0,0.0);
        Vector3D u = new Vector3D(1.0,0.0,0.0);
        Vector3D expected = new Vector3D(0.0,0.0,-1.0);
        Vector3D actual = v.crossProduct(u);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testRotateXdeg() {
        Vector3D v = new Vector3D(0.0,1.0,0.0);
        Vector3D expected = new Vector3D(0.0,0.0,1.0);
        Vector3D actual = v.rotateXdeg(90.0);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testRotateYdeg() {
        Vector3D v = new Vector3D(1.0,0.0,0.0);
        Vector3D expected = new Vector3D(0.0,0.0,-1.0);
        Vector3D actual = v.rotateYdeg(90.0);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testRotateZdeg() {
        Vector3D v = new Vector3D(1.0,0.0,0.0);
        Vector3D expected = new Vector3D(0.0,1.0,0.0);
        Vector3D actual = v.rotateZdeg(90.0);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testRotateXrad() {
        Vector3D v = new Vector3D(0.0,1.0,0.0);
        Vector3D expected = new Vector3D(0.0,0.0,1.0);
        Vector3D actual = v.rotateXrad(0.5*Math.PI);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testRotateYrad() {
        Vector3D v = new Vector3D(1.0,0.0,0.0);
        Vector3D expected = new Vector3D(0.0,0.0,-1.0);
        Vector3D actual = v.rotateYrad(0.5*Math.PI);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testRotateZrad() {
        Vector3D v = new Vector3D(1.0,0.0,0.0);
        Vector3D expected = new Vector3D(0.0,1.0,0.0);
        Vector3D actual = v.rotateZrad(0.5*Math.PI);
        assertEquals(true,equalVectors(expected,actual));
    }
    
    @Test
    public void testAddVector() {
        Vector3D v = new Vector3D(1.0,2.0,3.0);
        Vector3D u = new Vector3D(4.0,5.0,6.0);
        Vector3D expected = new Vector3D(5.0,7.0,9.0);
        v.addVector(u);
        assertEquals(true,equalVectors(expected,v));
    }
    
    @Test
    public void testToString() {
        Vector3D v = new Vector3D(1.0,2.0,3.0);
        String expected = "(1.0,2.0,3.0)";
        String actual = v.toString();
        assertEquals(expected,actual);
    }
    
    private boolean equalVectors(Vector3D u, Vector3D v) {
        boolean result = true;
        result = result && Math.abs(v.getX() - u.getX()) < 1.0E-14;
        result = result && Math.abs(v.getY() - u.getY()) < 1.0E-14;
        result = result && Math.abs(v.getZ() - u.getZ()) < 1.0E-14;
        return result;
    }
}
