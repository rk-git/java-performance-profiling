/*
 * File: StarvingCrawler.java
 * Description: Demonstration of thread starvation caused by nested task submission
 *              and blocking via Future.get() inside a fixed thread pool executor.
 *              This code is for educational and diagnostic purposes only.
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

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.concurrent.*;

public class StarvationDemo {
    // Number of worker threads to be used by the executor
    public static final Integer NUM_THREADS = 10;

    public static final int WAIT_TIME = 30000;

    @Category({"StarvationDemo", "Events"})
    @Label("NestedTaskSubmitted")
    @Registered(true) // ← 🔥 This makes it show up in JMC by default
    static class NestedTaskSubmittedEvent extends Event {
        @Label("Thread")
        String threadName;

        @Label("Time")
        long timestamp;
    }

    /*
     * The executor where the thread starvation is caused.
     */
    private static final ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS,
            factory -> {
                Thread thread = new Thread(factory);
                // Name the threads so the threads are conspicuous in JMC
                thread.setName("starving-thread-" + UUID.randomUUID());
                return thread;
            });

    public static void main(String[] args) {
        /* This is the client of the executor that cuases the thread starvation:
         * the client submits an outer task that consumers a worker; each such worker
         * (as you see below) spawns an auxiliary tasks. Hence N worksers are
         * spawned that each in turn spawn a worker each, causing the system to
         * block.
         */
        for (int i = 0; i < NUM_THREADS; i++) {
            pool.submit(new CrawlTask("https://example.com/page" + i));
        }

        pool.shutdown();
    }

    static class CrawlTask implements Runnable {
        private final String url;

        CrawlTask(final String url) {
            this.url = url;
        }

        @Override
        public void run() {
            final String threadName = Thread.currentThread().getName();
            System.out.println(threadName + ": Crawling " + url);

            // Simulate discovering more URLs
            for (int i = 0; i < 2; i++) {
                final String newUrl = url + "/link" + i;

                System.out.println(threadName + ": Submitting nested task...");

                NestedTaskSubmittedEvent evt = new NestedTaskSubmittedEvent();
                evt.threadName = threadName;
                evt.timestamp = System.currentTimeMillis();
                evt.commit();  // 🔴 appears in JFR timeline!

                // 🔴 Nested submission + blocking on future.get() → Starvation
                Future<?> future = pool.submit(new CrawlTask(newUrl));
                try {
                    future.get(); // 🔥 This is the key to inducing thread starvation
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            System.out.println(threadName + ": Finished crawling " + url);
            try {
                Thread.sleep(WAIT_TIME); // Give time for JFR to capture starvation
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            pool.shutdown();
        }
    }
}

/*
        Runnable blockingTask = () -> {
            final String threadName = Thread.currentThread().getName();
            Future<?> nested = pool.submit(() -> {
                System.out.println(threadName + ": Running nested task");
            });
            try {
                nested.get(); // Wait for nested task to complete
                System.out.println(threadName + ": Nested task done");
            } catch (Exception e) { }
        };
 */
