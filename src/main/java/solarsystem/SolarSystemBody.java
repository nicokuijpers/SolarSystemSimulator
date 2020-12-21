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
package solarsystem;

import util.Vector3D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a body of the Solar System.
 * @author Nico Kuijpers
 */
public class SolarSystemBody implements Serializable {
    
    // Default serialVersion id
    private static final long serialVersionUID = 1L;

    // Default diameter for small objects such as spacecraft
    private static final double MINIMUMDIAMETER = 5.0;
    
    private String name;
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D[] orbit;
    private List<Vector3D> trajectory;
    private double diameter;
    private SolarSystemBody centerBody;

    /**
     * Default constructor.
     */
    public SolarSystemBody() {
        this.trajectory = new ArrayList<>();
        this.diameter = MINIMUMDIAMETER;
    }

    /**
     * Constructor.
     * @param name       name of body
     * @param position   position in m
     * @param velocity   velocity in m/s
     * @param orbit      orbit
     * @param diameter   diameter in m
     * @param centerBody center body
     */
    public SolarSystemBody(String name, Vector3D position, Vector3D velocity, Vector3D[] orbit,
                           double diameter, SolarSystemBody centerBody) {
        this.name = name;
        this.position = position;
        this.velocity = velocity;
        this.orbit = orbit;
        this.trajectory = new ArrayList<>();
        this.diameter = diameter;
        this.centerBody = centerBody;
    }
    
    /**
     * Get name of body.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of body.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get position of body in m.
     * @return position
     */
    public Vector3D getPosition() {
        return position;
    }

    
    /**
     * Set position of body.
     * @param position new position in m
     */
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    /**
     * Set velocity of body.
     * @param velocity new velocity in m/s
     */
    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    /**
     * Get velocity of body in m/s.
     * @return velocity
     */
    public Vector3D getVelocity() {
        return velocity;
    }

    /**
     * Get orbit of body.
     * @return orbit
     */
    public Vector3D[] getOrbit() {
        if (centerBody == null || centerBody.getName().equals("Sun")) {
            // This body is a planet with center body sun
            return orbit;
        }
        else {
            // This body is a moon with a planet as center body
            Vector3D planetPosition = centerBody.getPosition();
            Vector3D[] orbitAroundCenterBody = new Vector3D[orbit.length];
            for (int i = 0; i < orbit.length; i++) {
                orbitAroundCenterBody[i] = planetPosition.plus(orbit[i]);
            }
            return orbitAroundCenterBody;
        }
    }
    
    /**
     * Set orbit of body.
     * @param orbit new orbit in m
     */
    public void setOrbit(Vector3D[] orbit) {
        this.orbit = orbit;
    }

    /**
     * Initialize trajectory.
     */
    public void initTrajectory() {
        trajectory.clear();
    }

    /**
     * Update trajectory.
     */
    public void updateTrajectory(Vector3D currentPosition, Vector3D currentVelocity) {
        Vector3D trajectoryPosition, trajectoryVelocity;
        if (centerBody != null) {
            trajectoryPosition = currentPosition.minus(centerBody.getPosition());
            trajectoryVelocity = currentVelocity.minus(centerBody.getVelocity());
        }
        else {
            trajectoryPosition = new Vector3D(currentPosition);
            trajectoryVelocity = new Vector3D(currentVelocity);
        }
        if (trajectory.isEmpty()) {
            trajectory.add(trajectoryPosition);
        } else {
            Vector3D formerPosition = trajectory.get(trajectory.size() - 1);
            if (formerPosition.euclideanDistance(trajectoryPosition) > 1E06 ||
                    formerPosition.direction(trajectoryPosition).angleDeg(trajectoryVelocity) > 1.0) {
                trajectory.add(trajectoryPosition);
            }
        }
    }

    /**
     * Get trajectory.
     */
    public List<Vector3D> getTrajectory() {
        return trajectory;
    }

    /**
     * Get diameter of body in m.
     * @return diameter
     */
    public double getDiameter() {
        return diameter;
    }
    
    /**
     * Set diameter of body.
     * @param diameter new diameter in m
     */
    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }
    
     /**
     * Get reference to center body.
     * @return name of center body
     */
    public SolarSystemBody getCenterBody() {
        return centerBody;
    }

    /**
     * Set reference to center body.
     * @param centerBody reference to center body
     */
    public void setCenterBody(SolarSystemBody centerBody) {
        this.centerBody = centerBody;
    }
}
