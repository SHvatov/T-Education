package ru.tbank.edu.fintech.lecture8.java.util.concurrent.executor;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.error;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleepAndGet;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.withThreadInterruptionHandled;


public class ExecutorTest {

    @Test
    @DisplayName("Пример создания и использования ExecutorService с фиксированным кол-вом потоков")
    void test0() {
        try (var service = Executors.newFixedThreadPool(3)) {
            var times = new AtomicInteger();
            while (times.get() < 10) {
                service.submit(() -> {
                    var time = times.incrementAndGet();
                    info("Hello, world x{0} from thread {1}", time, getCurrentThreadName());
                });
                sleep(10);
            }
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Освобождение ресурсов из под ExecutorService")
    void test1() {
        var useJava19 = true;
        var service = Executors.newFixedThreadPool(3);
        try {
            // doing something
        } finally {
            if (useJava19) {
                service.close(); // Since Java 19
            } else {
                // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
                service.shutdown(); // Disable new tasks from being submitted
                try {
                    // Wait a while for existing tasks to terminate
                    if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                        service.shutdownNow(); // Cancel currently executing tasks
                        // Wait a while for tasks to respond to being cancelled
                        if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                            error("Pool did not terminate");
                        }
                    }
                } catch (InterruptedException ie) {
                    // (Re-)Cancel if current thread also interrupted
                    service.shutdownNow();
                    // Preserve interrupt status
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Освобождение ресурсов из под ExecutorService - пример")
    void test2() {
        // noinspection resource
        var service = Executors.newFixedThreadPool(1);

        service.submit(() -> {
            var iteration = 0L;
            while (!Thread.currentThread().isInterrupted()) {
                info("Infinite ping loop. {0} iteration.", iteration++);
                withThreadInterruptionHandled(
                        () -> sleep(100),
                        () -> info("Thread has been interrupted, meaning that task is being cancelled."));
            }
        });

        service.submit(() -> {
            for (int iteration = 0; iteration < 1_000; iteration++) {
                info("Ping x{0}", iteration);
            }
        });

        service.shutdown(); // Начиная отсюда мы не сможем сабмитить задачи

        assertThrows(
                RejectedExecutionException.class,
                () -> service.submit(() -> System.out.println("Hello, world!")));

        Thread.sleep(1_000); // Но задачи все еще крутятся

        var result = service.shutdownNow(); // Потокам послан сигнал о прерывании
        assertEquals(1, result.size());

        assertTrue(service.awaitTermination(30, TimeUnit.SECONDS));
    }

    @Test
    @SneakyThrows
    @DisplayName("Разница между submit & invoke")
    void test3() {
        try (var service = Executors.newFixedThreadPool(5)) {
            var future = service.submit(() -> {
                var iteration = 0;
                while (iteration++ < 10) {
                    info("Ping!");
                    withThreadInterruptionHandled(() -> sleep(500));
                }
                info("Pong!");
            });
            assertFalse(future.isDone());

            var tasks = List.<Callable<Integer>>of(
                    () -> sleepAndGet(2_000, 256),
                    () -> sleepAndGet(1_000, 101),
                    () -> sleepAndGet(500, 30));

            info("Calling invokeAny...");
            var value = service.invokeAny(tasks); // пример с деревом
            assertEquals(30, value);

            info("Calling invokeAll...");
            var start = System.currentTimeMillis();
            var futures = service.invokeAll(tasks);
            assertTrue(futures.stream().allMatch(Future::isDone));

            var timeSpent = System.currentTimeMillis() - start;
            assertTrue(timeSpent > 1_500);

            info("Waiting for submit to complete...");
        }
    }

    @Test
    @DisplayName("Планирование задач с помощью ScheduledThreadPool")
    void test10() {
        try (var service = Executors.newScheduledThreadPool(2)) {
            service.scheduleAtFixedRate(() -> info("Ping!"), 0, 1, TimeUnit.SECONDS);
            sleep(5_000);
        }
    }

}
