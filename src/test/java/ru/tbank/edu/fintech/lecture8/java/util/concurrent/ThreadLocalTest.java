package ru.tbank.edu.fintech.lecture8.java.util.concurrent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;


public class ThreadLocalTest {

    @Test
    @DisplayName("Пример использовать локальных для потока переменных")
    void test() {
        var threadLocal = ThreadLocal.withInitial(AtomicInteger::new);

        IntStream.range(0, 10).forEach(it ->
                Thread.ofPlatform()
                        .name("thread-%s".formatted(it))
                        .start(() -> {
                            var localAtomic = threadLocal.get();
                            while (!Thread.currentThread().isInterrupted()) {
                                info("Значение локального счетчика: {0}", localAtomic.getAndIncrement());
                                sleep(100);
                            }
                        }));

        // debug Thread.currentThread().threadLocals
        sleep(1_000);
    }

}
