package ru.tbank.edu.fintech.lecture8;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class ThreadTest {

    @Test
    @SneakyThrows
    @DisplayName("Базовая база - демонстрация создания и работы с несколькими потоками")
    void test0() {
        var helloWorldThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                System.out.println("Hello, world!");
                try {
                    // noinspection BusyWait
                    Thread.sleep(2_000);
                } catch (InterruptedException exception) {
                    System.out.println("Thread has been interrupted!");
                    break;
                }
            }
            System.out.println("Goodbye, world!");
        });

        var tickThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                System.out.println("Tick!");
                try {
                    // noinspection BusyWait
                    Thread.sleep(1_000);
                } catch (InterruptedException exception) {
                    System.out.println("Thread has been interrupted!");
                    break;
                }
            }
            System.out.println("Tuck!");
        });

        var interruptionThread = new Thread(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Interrupting all threads!");
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
                .unstarted(() -> greetTheWorld(1_000));

        thread0.start();

        Thread.sleep(3_000);

        thread0.join(); // blocks current thread
        thread0.interrupt(); // interrupts thread
    }

    @Test
    @SneakyThrows
    @DisplayName("Управление потоками - группы")
    void test3() {
        var group = new ThreadGroup("custom");

        var thread0 = Thread.ofPlatform()
                .name("thread-0")
                .group(group)
                .priority(1)
                .start(() -> greetTheWorld(1_000));

        var thread1 = Thread.ofPlatform()
                .name("thread-1")
                .group(group)
                .priority(2)
                .start(() -> greetTheWorld(1_000));

        Thread.sleep(5_000);
        System.out.printf("Active threads: %s%n", group.activeCount());

        group.interrupt();
        Thread.sleep(1_000);
    }

    private static void greetTheWorld(long sleep) {
        while (!Thread.interrupted()) {
            System.out.printf("Hello, world, from %s%n", Thread.currentThread().getName());
            try {
                // noinspection BusyWait
                Thread.sleep(sleep);
            } catch (InterruptedException exception) {
                System.out.println("Thread has been interrupted!");
                break;
            }
        }
    }

}
