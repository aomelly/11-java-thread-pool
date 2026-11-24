package com.example.task02;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class PoolExample {
    static void runTasks(IntConsumer work) throws InterruptedException, ExecutionException {
        try (ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3, 3, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3))) {
            executor.setRejectedExecutionHandler((task, pool) -> {
                if (pool.isShutdown()) {
                    throw new RejectedExecutionException("Пул остановлен");
                }
                try {
                    pool.getQueue().put(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RejectedExecutionException(e);
                }
            });

            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                int number = i;
                tasks.add(executor.submit(() -> work.accept(number)));
            }

            for (Future<?> task : tasks) {
                task.get();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        AtomicInteger count = new AtomicInteger();
        AtomicInteger inProgress = new AtomicInteger();

        runTasks(number -> {
            int working = inProgress.incrementAndGet();
            System.out.println("start #" + number + ", in progress: " + working);
            try {
                Thread.sleep(Math.round(1000 + Math.random() * 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Задача прервана", e);
            } finally {
                working = inProgress.decrementAndGet();
            }
            System.out.println("end #" + number + ", in progress: " + working
                    + ", done tasks: " + count.incrementAndGet());
        });

        System.out.println("Total: " + count.get());
    }
}
