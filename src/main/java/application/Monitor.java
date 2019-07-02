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
package application;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Monitor for thread synchronization between drawing and simulating.
 * @author Nico Kuijpers
 */
public class Monitor {

    private final Lock monLock = new ReentrantLock();
    private final Condition okToDraw = monLock.newCondition();
    private final Condition okToSimulate = monLock.newCondition();
    private boolean drawing = false;
    private boolean simulating = false;

    /**
     * Start drawing.
     * @throws InterruptedException
     */
    public void startDrawing() throws InterruptedException {
        monLock.lock();
        try {
            while (simulating) {
                okToDraw.await();
            }
            drawing = true;
        }
        finally {
            monLock.unlock();
        }
    }

    /**
     * Stop drawing.
     */
    public void stopDrawing() {
        monLock.lock();
        try {
            drawing = false;
            okToSimulate.signal();
        }
        finally {
            monLock.unlock();
        }
    }

    /**
     * Start simulating.
     * @throws InterruptedException
     */
    public void startSimulating() throws InterruptedException {
        monLock.lock();
        try {
            while (drawing) {
                okToSimulate.await();
            }
            simulating = true;
        }
        finally {
            monLock.unlock();
        }
    }

    /**
     * Stop simulating.
     */
    public void stopSimulating() {
        monLock.lock();
        try {
            simulating = false;
            okToDraw.signal();
        }
        finally {
            monLock.unlock();
        }
    }
}
