/*
 * File: LockContentionDemo.java
 * Demonstrates lock contention under various thread loads.
 *
 * This class runs N threads that continuously contend for a shared lock
 * (e.g., synchronized block). It highlights how lock contention increases
 * latency and reduces throughput as more threads compete for the same resource.
 *
 * Use this demo to generate JFR recordings and analyze:
 * - Thread blocking times
 * - Monitor contention
 * - Lock acquisition latencies
 *
 * Expected output:
 * - Ops/sec (throughput)
 * - Avg latency per operation
 *
 * Run with: mvn compile exec:exec -Plockcontention
 * Tested on: OpenJDK 17, with -XX:+FlightRecorder enabled
 * Author: Rk <rks.0728@gmail.com>
 * Created: 2025-09-21
 *
 * @author rks.0728@gmail.com
 * @since 2025-09-21
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

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LockContentionDemo {

    // Total number of threads that will compete for the shared lock
    public static final Integer NUM_WORKERS = 20;

    // Simulated work duration in milliseconds — each worker holds the lock for this long
    public static final Integer WORK_DURATION = 1000;

    // Shared lock object used for synchronization between threads
    private static final Object lock = new Object();

    public static void main(String[] args) {
        // Thread pool with only 4 worker threads — intentionally fewer than NUM_WORKERS
        // This creates contention both on the CPU threads and on the shared lock
        ExecutorService pool = Executors.newFixedThreadPool(4);
        System.out.println(">>>>> JVM args:");
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            System.out.println("    " + arg);
        }

        // Add JVM shutdown hook for graceful shutdown...
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(">>>>> Shutdown hook triggered. Shutting down executor...");
            pool.shutdownNow();  // Interrupts tasks, ends non-daemon threads

            // Stop any programmatic JFR recordings...
        }));

        for (int i = 0; i < NUM_WORKERS; i++) {
            pool.submit(() -> {
                // Each worker loops forever, repeatedly trying to acquire the lock
                while (true) {
                    workWithLock();
                }
            });
        }
    }

    /*
     * This is the work each thread performs.
     * Instead of real computation, we simulate work by sleeping.
     * In production, this might represent I/O, shared memory updates, etc.
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
