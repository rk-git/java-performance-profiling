/*
 * File: SystemOutContention.java
 * Demonstrates lock contention caused by multiple threads writing to System.out.
 *
 * In real-world Java applications, excessive logging from multiple threads
 * creates a performance bottleneck due to internal synchronization in PrintStream.
 *
 * This class launches many threads that write to System.out in tight loops,
 * highlighting monitor contention on the shared PrintStream lock. Use JFR/JMC
 * to observe the effect under "Monitor Blocked" or "Lock Instances".
 *
 * Use this class to generate JFR recordings and analyze:
 * - Thread blocking times due to System.out locking
 * - Monitor contention on java.io.PrintStream
 * - Impact of I/O on throughput under concurrency
 *
 * Expected observations:
 * - High monitor contention on System.out
 * - Degraded throughput with increasing thread count
 * - Potential increase in GC activity if output buffer saturates
 *
 * Run with: mvn compile exec:exec -Psystemoutcontention
 * Tested on: OpenJDK 17 with -XX:+FlightRecorder
 *
 * Author: Rk <rks.0728@gmail.com>
 * Created: 2025-09-24
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemOutContention {

    public static final int NUM_THREADS = 20;  // Number of threads to simulate contention

    public static void main(String[] args) {
        final int defaultThreads = 8;
        final int availableCores = Runtime.getRuntime().availableProcessors();
        final int poolSize = Math.min(defaultThreads, availableCores); // in addition, let user override with args

        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        // JVM args visibility
        System.out.println(">>>>> JVM args:");
        for (String arg : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            System.out.println("    " + arg);
        }

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(">>>>> Shutdown hook triggered. Shutting down executor...");
            pool.shutdownNow();
        }));

        // Submit NUM_THREADS workers that log to System.out
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            pool.submit(() -> {
                while (true) {
                    // This synchronized call causes contention on shared PrintStream.lock
                    System.out.println("Thread-" + threadId + " logging at " + System.currentTimeMillis());
                    try {
                        Thread.sleep(10);  // Optional small delay to avoid total flooding
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
        }
    }
}
