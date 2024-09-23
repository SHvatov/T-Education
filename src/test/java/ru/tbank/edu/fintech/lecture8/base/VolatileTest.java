package ru.tbank.edu.fintech.lecture8.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.stream.IntStream;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;


public class VolatileTest {

    @SneakyThrows
    @RepeatedTest(10)
    @DisplayName("Пример использования volatile")
    void test() {
        var counter = VolatileCounter.builder()
                .maxValue(1_000)
                .value(0)
                .build();

        var modifier = Thread.ofPlatform()
                .name("modifier")
                .unstarted(() -> {
                    while (counter.increment()) {
                        info("[{0} нс] Значение счетчика увеличено до {1}", System.nanoTime(), counter.getValue());
                    }
                });

        IntStream.range(0, 3).forEach(it ->
                Thread.ofPlatform()
                        .name("reader-%s".formatted(it))
                        .start(() -> {
                            while (true) {
                                info("[{0} нс] Значение счетчика в потоке: {1}", System.nanoTime(), counter.getValue());
                                sleep(1);
                            }
                        }));

        modifier.start();
        sleep(100);
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class VolatileCounter {

        private final long start = System.currentTimeMillis();

        private int value;
        private int maxValue;
        // private volatile int maxValue;

        public boolean increment() {
            if (value < maxValue) {
                value = (int) (System.currentTimeMillis() - start);
                return true;
            }
            return false;
        }

    }

}
