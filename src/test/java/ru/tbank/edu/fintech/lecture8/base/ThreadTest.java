package ru.tbank.edu.fintech.lecture8.base;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ru.tbank.edu.fintech.lecture8.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.ThreadUtils.sleep;
import static ru.tbank.edu.fintech.lecture8.ThreadUtils.withThreadInterruptionHandled;


public class ThreadTest {

    @Test
    @SneakyThrows
    @DisplayName("Базовая база - демонстрация создания и работы с несколькими потоками")
    void test0() {
        var helloWorldThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                info("Hello, world!");
                withThreadInterruptionHandled(() -> sleep(2_000));
            }
            info("Goodbye, world!");
        });

        var tickThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                info("Tick!");
                withThreadInterruptionHandled(() -> sleep(1_000));
            }
            info("Tuck!");
        });

        var interruptionThread = new Thread(() -> {
            sleep(5_000);

            info("Interrupting all threads!");
            helloWorldThread.interrupt();
            tickThread.interrupt();
        });

        interruptionThread.start();
        helloWorldThread.start();
        tickThread.start();

        interruptionThread.join();
        helloWorldThread.join();
        tickThread.join();
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание потоков")
    void test1() {
        var thread = Thread.ofPlatform()
                .name("platform-thread")
                .daemon(false)
                .start(() -> greetTheWorld(1_000));

        // will live, even when JVM dies
        var daemon = Thread.ofPlatform()
                .name("daemon-thread")
                .daemon()
                .start(() -> greetTheWorld(2_000));

        // virtual thread - Project Loom
        var virtual = Thread.ofVirtual()
                .name("virtual-thread")
                .start(() -> greetTheWorld(3_000));

        Thread.sleep(5_000);

        thread.interrupt();
        daemon.interrupt();
        virtual.interrupt();

        thread.join();
        daemon.join();
        virtual.join();
    }

    @Test
    @SneakyThrows
    @DisplayName("Управление потоками - start & interrupt")
    void test2() {
        var thread0 = Thread.ofPlatform()
                .name("thread-0")
                .start(() -> greetTheWorld(1_000));

        Thread.sleep(3_000);

        thread0.join(); // blocks current thread
        thread0.interrupt(); // interrupts thread
    }

    @Test
    @SneakyThrows
    @DisplayName("Управление потоками - группы")
    void test3() {
        var group = new ThreadGroup("custom");

        Thread.ofPlatform()
                .name("thread-0")
                .group(group)
                .priority(1)
                .start(() -> greetTheWorld(1_000));

        Thread.ofPlatform()
                .name("thread-1")
                .group(group)
                .priority(2)
                .start(() -> greetTheWorld(1_000));

        Thread.sleep(5_000);
        info("Total active threads: {0}", group.activeCount());

        group.interrupt();

        Thread.sleep(1_000);
        info("Total active threads: {0}", group.activeCount());
    }

    private static void greetTheWorld(long sleepMs) {
        while (!Thread.currentThread().isInterrupted()) {
            info("Hello, world, from {0}", getCurrentThreadName());
            sleep(sleepMs);
        }
    }

}
