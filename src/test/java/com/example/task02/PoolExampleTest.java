package com.example.task02;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoolExampleTest {
    @Test
    @Timeout(10)
    void processesAllTasksExactlyOnceOnThreeWorkers() throws Exception {
        AtomicIntegerArray calls = new AtomicIntegerArray(30);
        Set<Thread> workers = ConcurrentHashMap.newKeySet();
        CountDownLatch firstThree = new CountDownLatch(3);

        PoolExample.runTasks(number -> {
            workers.add(Thread.currentThread());
            firstThree.countDown();
            try {
                assertTrue(firstThree.await(3, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
            calls.incrementAndGet(number);
        });

        assertEquals(3, workers.size());
        for (int i = 0; i < calls.length(); i++) {
            assertEquals(1, calls.get(i));
        }
    }
}
