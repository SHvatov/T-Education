package ru.tbank.edu.fintech.lecture8.base.sync;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;


public class WaitNotifyTest {

    @Test
    @SneakyThrows
    @DisplayName("Пример использования wait & notify")
    void test0() {
        var sync = new Object();

        IntStream.range(0, 10).forEach(it ->
                Thread.ofPlatform()
                        .name("waiter-%s".formatted(it))
                        .start(() -> wait(sync)));

        Thread.sleep(1000);
        synchronized (sync) {
            System.out.println("Начинаем работу очереди!");
            sync.notifyAll();
            Thread.sleep(1000);
            System.out.println("А вот теперь реально начинаем работу очереди!");
        }

        Thread.sleep(10_000);
    }

    @SneakyThrows
    private static void wait(Object sync) {
        System.out.printf("Поток %s ожидает своей очереди%n", Thread.currentThread().getName());
        synchronized (sync) {
            sync.wait();

            System.out.printf("Поток %s дождался своей очереди%n", Thread.currentThread().getName());

            System.out.printf("Поток %s отпустил монитор на синхронизаторе%n", Thread.currentThread().getName());
            Thread.sleep(100);

            sync.notifyAll();
            System.out.printf("Поток %s освободил место в очереди%n", Thread.currentThread().getName());
        }
    }

}
