package ru.tbank.edu.fintech.lecture8;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SyncTreadTest {

    @SneakyThrows
    @RepeatedTest(10)
    @DisplayName("Решаем задачу с увеличением счетчика в несколько потоков")
    void test0() {
        var counter = SynchronizedCounter.builder()
                .maxValue(1000)
                .value(0)
                .build();

        var latch = new CountDownLatch(1);
        IntStream.range(0, 10).forEach(it ->
                Thread.ofPlatform()
                        .name("thread-%s".formatted(it))
                        .priority(1)
                        .start(() -> incrementToMaxValue(latch, counter)));

        latch.countDown();
        Thread.sleep(500);

        System.out.println("Значение счетчика: " + counter.getValue());
        assertEquals(counter.getMaxValue(), counter.getValue());
    }

    private static void incrementToMaxValue(CountDownLatch latch, Counter counter) {
        System.out.printf(
                "Thread %s waiting to start...%n",
                Thread.currentThread().getName());
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.printf(
                "Thread %s has started!%n",
                Thread.currentThread().getName());
        counter.incrementToMaxValue();
    }

    private interface Counter {

        int getValue();

        int getMaxValue();

        boolean increment();

        default void incrementToMaxValue() {
            // noinspection StatementWithEmptyBody
            while (increment()) {
                // noop
            }
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class NativeCounter implements Counter {

        private int maxValue;
        private int value;

        @Override
        public boolean increment() {
            if (value < maxValue) {
                System.out.printf("Thread %s has entered the method%n", Thread.currentThread().getName());
                value += 1;
                return true;
            }
            return false;
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class SynchronizedCounter implements Counter {

        private int maxValue;
        private int value;

        @Override
        public synchronized boolean increment() {
            if (value < maxValue) {
                System.out.printf("Thread %s has entered the method%n", Thread.currentThread().getName());
                value += 1;
                return true;
            }
            return false;
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class VolatileCounter implements Counter {

        private int maxValue;
        private volatile int value;

        // Note: volatile здесь нам никак не поможет
        @Override
        public boolean increment() {
            if (value < maxValue) {
                System.out.printf("Thread %s has entered the method%n", Thread.currentThread().getName());
                value++;
                return true;
            }
            return false;
        }

    }

}
