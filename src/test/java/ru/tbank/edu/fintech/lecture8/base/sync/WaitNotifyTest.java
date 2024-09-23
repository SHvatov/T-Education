package ru.tbank.edu.fintech.lecture8.base.sync;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;


public class WaitNotifyTest {

    @Test
    @SneakyThrows
    @DisplayName("Пример использования wait & notify")
    void test() {
        var sync = new Object();

        IntStream.range(0, 10).forEach(it ->
                Thread.ofPlatform()
                        .name("waiter-%s".formatted(it))
                        .start(() -> doTest(sync)));

        Thread.sleep(1000);
        synchronized (sync) {
            info("Начинаем работу очереди!");
            sync.notifyAll();
            sleep(1000);
            info("А вот теперь реально начинаем работу очереди!");
        }

        Thread.sleep(10_000);
    }

    @SneakyThrows
    private static void doTest(Object sync) {
        info("Поток {0} ожидает своей очереди", getCurrentThreadName());
        synchronized (sync) {
            sync.wait();
            info("Поток {0} дождался своей очереди", getCurrentThreadName());

            Thread.sleep(100);

            sync.notifyAll();
            info("Поток {0} уведомил всех о наличии свободного места в очереди", getCurrentThreadName());
        }
        info("Поток {0} отпустил монитор на синхронизаторе", getCurrentThreadName());
    }

}
