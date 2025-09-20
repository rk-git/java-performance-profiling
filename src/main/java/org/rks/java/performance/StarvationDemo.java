package org.rks.java.performance;

import java.util.concurrent.*;

public class StarvationDemo {
    public static final int WAIT_TIME = 30000;
    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // The following task will be submitted and th etask will
        // invoke Future.get(), thereby blocking itself, since the executor has only
        // 2 threads.
        Runnable blockingTask = () -> {
            System.out.println(Thread.currentThread().getName() + ": Submitting nested task...");
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
