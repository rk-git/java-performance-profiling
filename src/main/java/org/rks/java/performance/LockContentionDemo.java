/*
 * File: LockContentionDemo.java
 * Description: Demonstration of lock contention among many threads.
 *              A bunch of workers (threads ) are spawned by an Executor
 *              and seek a lock. Once the lock is obtianed, the worker
 *              performs "work" (sleep) and releases the lock after
 *              which the next contender workder grabs the lock.
 *              This cycle repeatedly indefinitely.
 *  
 * Author: Rk <rk@alwaysup.dev>
 * Created: 2025-09-21
 *
 * Copyright 2025 Rk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rks.java.performance;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Registered;

import java.lang.management.ManagementFactory;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LockContentionDemo {

    public static final Integer NUM_WORKERS = 5;
    public static final Integer WORK_DURATION = 1000;
    private static final Object lock = new Object();

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        System.out.println(">>>>> JVM args:");
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            System.out.println("    " + arg);
        }

        for (int i = 0; i < NUM_WORKERS; i++) {
            pool.submit(() -> {
                while (true) {
                    workWithLock();
                }
            });
        }
    }

    /*
     * THis is the work that each worker is asked to do.
     * In real life, instead of sleeping, the workser would
     * likely be processing data in memory.
     */
    private static void workWithLock() {
        synchronized (lock) {
            // Simulate work holding the lock for some time
            try {
                Thread.sleep(WORK_DURATION); // Hold the lock
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
