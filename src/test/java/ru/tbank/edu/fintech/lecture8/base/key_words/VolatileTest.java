package ru.tbank.edu.fintech.lecture8.base.key_words;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.stream.IntStream;


public class VolatileTest {

    @SneakyThrows
    @RepeatedTest(1)
    @DisplayName("Пример использования volatile")
    void test0() {
        var counter = VolatileCounter.builder()
                .maxValue(1_000)
                .value(0)
                .build();

        var modifier = Thread.ofPlatform()
                .name("modifier")
                .unstarted(() -> {
                    while (counter.increment()) {
                        System.out.printf(
                                "Значение счетчика в %s нс увеличено до %d%n%n",
                                System.nanoTime(), counter.getValue());
                    }
                });

        IntStream.range(0, 3).forEach(it ->
                Thread.ofPlatform()
                        .name("reader-%s".formatted(it))
                        .start(() -> {
                            while (true) {
                                System.out.printf(
                                        "Значение счетчика в %s нс потоке %s: %d%n",
                                        System.nanoTime(), Thread.currentThread().getName(), counter.getValue());
                                sleep(1);
                            }
                        }));

        modifier.start();
        Thread.sleep(100);
    }

    @SneakyThrows
    private static void sleep(long ms) {
        Thread.sleep(ms);
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class VolatileCounter {

        private final long start = System.currentTimeMillis();

        private int value;
        private int maxValue;

        public boolean increment() {
            if (value < maxValue) {
                value = (int) (System.currentTimeMillis() - start);
                return true;
            }
            return false;
        }

    }

}
