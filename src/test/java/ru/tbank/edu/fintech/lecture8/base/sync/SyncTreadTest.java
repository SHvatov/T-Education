package ru.tbank.edu.fintech.lecture8.base.sync;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.withThreadInterruptionHandled;


public class SyncTreadTest {

    private static final int MAX_ATTEMPTS = 10;
    // private static final int MAX_ATTEMPTS = 1;

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("getCounters")
    @DisplayName("Решаем задачу с увеличением счетчика в несколько потоков")
    void test(Counter counter) {
        info("Тестируем счетчик {0}", counter.getClass().getSimpleName());

        var latch = new CountDownLatch(1);
        IntStream.range(0, 10).forEach(it ->
                Thread.ofPlatform()
                        .name("thread-%s".formatted(it))
                        .priority(1)
                        .start(() -> doTest(latch, counter)));

        latch.countDown();
        sleep(5_000);
        // sleep(1_000);

        info("Значение счетчика: {0}", counter.getValue());
        assertEquals(counter.getMaxValue(), counter.getValue());
    }

    private static Stream<Arguments> getCounters() {
        return IntStream.range(0, MAX_ATTEMPTS).boxed()
                .flatMap(ignored ->
                        Stream.of(
                                Arguments.of(
                                        NativeCounter.builder()
                                                .maxValue(1_000)
                                                .value(0)
                                                .build()),
                                Arguments.of(
                                        VolatileCounter.builder()
                                                .maxValue(1_000)
                                                .value(0)
                                                .build()),
                                Arguments.of(
                                        SynchronizedCounter.builder()
                                                .maxValue(1_000)
                                                .value(0)
                                                .build())));
    }

    private static void doTest(CountDownLatch latch, Counter counter) {
        info("Поток {0} ожидает своего старта!", getCurrentThreadName());
        withThreadInterruptionHandled(latch::await);
        info("Поток {0} стартовал!", getCurrentThreadName());
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
                sleep(1); // Эмулируем нагрузку и переключение потоков
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
                sleep(1); // Эмулируем нагрузку и переключение потоков
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
                sleep(1); // Эмулируем нагрузку и переключение потоков
                value++;
                return true;
            }
            return false;
        }

    }

}
