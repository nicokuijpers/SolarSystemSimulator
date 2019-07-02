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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Task running on a separate thread that can be paused and resumed.
 *
 * @author Nico Kuijpers
 */
public abstract class PausableTask implements Runnable {

    /*
     * This code is extended from
     * https://stackoverflow.com/questions/22937592/resume-interrupted-thread
     * http://handling-thread.blogspot.com/2012/05/pause-and-resume-thread.html
     */

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> publisher;
    private boolean paused = true;

    /**
     * Task to be performed. Must be implemented.
     */
    abstract void task();

    @Override
    public void run() {
        while(!Thread.currentThread().interrupted()) {
            task();
        }
    }

    /**
     * Start task.
     */
    public void start() {
        publisher = executor.submit(this);
        paused = false;
    }

    /**
     * Pause task.
     */
    public void pause() {
        if (!paused) {
            publisher.cancel(true);
            paused = true;
        }
    }

    /**
     * Resume task.
     */
    public void resume() {
        if (paused) {
            start();
        }
    }

    /**
     * Stop task.
     */
    public void stop() {
        executor.shutdownNow();
        paused = true;
    }

    /**
     * Let task sleep for some period of time.
     * @param period Period of time in milliseconds
     * @throws InterruptedException
     */
    public void sleep(int period) throws InterruptedException {
        try {
            Thread.sleep(period);
        }
        catch (InterruptedException e) {
            if (!paused) {
                pause();
            }
            throw new InterruptedException();
        }
    }

    /**
     * Check whether task is paused.
     * @return true when task is paused, false otherwise
     */
    public boolean isPaused() {
        return paused;
    }
}
