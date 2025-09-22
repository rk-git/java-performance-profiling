package org.rks.java.performance;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Registered;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StarvationDemo {
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

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2, factory -> {
            Thread thread = new Thread(factory);
            // Name the threads so the threads are conspicuous in JMC
            thread.setName("starving-thread-" + UUID.randomUUID());
            return thread;
        });

        // The following task will be submitted and th etask will
        // invoke Future.get(), thereby blocking itself, since the executor has only
        // 2 threads.
        Runnable blockingTask = () -> {
            final String threadName = Thread.currentThread().getName();
            System.out.println(threadName + ": Submitting nested task...");

            NestedTaskSubmittedEvent evt = new NestedTaskSubmittedEvent();
            evt.threadName = threadName;
            evt.timestamp = System.currentTimeMillis();
            evt.commit();  // 🔴 appears in JFR timeline!


            Future<?> nested = pool.submit(() -> {
                System.out.println(Thread.currentThread().getName() + ": Running nested task");
            });

            try {
                nested.get(); // Wait for nested task to complete
                System.out.println(Thread.currentThread().getName() + ": Nested task done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // Following two submissions consue the two threas, thereby
        // making no threads available ot the inner tasks when it does Future.get(), causing
        // thread starvation. We can use JMC to observe this.
        pool.submit(blockingTask);
        pool.submit(blockingTask);

        Thread.sleep(WAIT_TIME); // Give time for JFR to capture starvation
        pool.shutdown();
    }
}
