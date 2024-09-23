package ru.tbank.edu.fintech.lecture8.java.util.concurrent.future;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.concurrent.Future.State.CANCELLED;
import static java.util.concurrent.Future.State.RUNNING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.withThreadInterruptionHandled;


public class FutureTest {

    @Test
    @DisplayName("Базовая работа с Future")
    void test0() {
        try (var service = Executors.newFixedThreadPool(1)) {
            var future = service.submit(() -> {
                var iteration = 0;
                while (iteration++ < 60 && !Thread.currentThread().isInterrupted()) {
                    info("Ping!");
                    withThreadInterruptionHandled(() -> sleep(1_000));
                }
                info("Pong!");
            });

            var state = future.state();
            assertEquals(state, RUNNING);

            future.cancel(true);
            state = future.state();
            assertEquals(state, CANCELLED);

            assertTrue(future.isDone());
            assertTrue(future.isCancelled());
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Новшества Future в 19 Java")
    void test1() {
        try (var service = Executors.newFixedThreadPool(1)) {
            var future = service.submit(() -> {
                var iteration = 0;
                while (iteration++ < 5 && !Thread.currentThread().isInterrupted()) {
                    info("Ping!");
                    withThreadInterruptionHandled(() -> sleep(1_000));
                }
                info("Pong!");
                return 30;
            });

            assertThrows(IllegalStateException.class, future::resultNow);
            assertThrows(IllegalStateException.class, future::exceptionNow);

            var result = future.get(6_000, TimeUnit.MILLISECONDS);
            assertEquals(30, result);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Как можно дождаться исполнения всех задач")
    void test2() {
        Function<Integer, Double> longComputation = (it) -> {
            sleep(100L * it);
            return Math.pow(it, 2);
        };
        try (var service = Executors.newFixedThreadPool(3)) {
            var futures = IntStream.range(0, 10)
                    .mapToObj(it -> service.submit(() -> longComputation.apply(it)))
                    .toList();

            // 1. Basic
            var results = futures.stream()
                    .map(it -> {
                        try {
                            return it.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
            assertEquals(10, results.size());
            info("Basic - DONE!");

            // 2. via invokeAll

            // 3. via CompletionService
            var completionService = new ExecutorCompletionService<Double>(service);
            IntStream.range(0, 10).forEach(it -> completionService.submit(() -> longComputation.apply(it)));

            results = new ArrayList<>();
            while (results.size() != 10) {
                Future<Double> future;
                while ((future = completionService.poll()) == null) { // also see take
                    info("No tasks are ready, waiting...");
                    sleep(100);
                }
                results.add(future.resultNow());
            }
            assertEquals(10, results.size());
            info("Completion - DONE!");

            // 4. via CompletableFutureTest
            var cfs = IntStream.range(0, 10)
                    .mapToObj(it -> CompletableFuture.supplyAsync(() -> longComputation.apply(it), service))
                    .toList();
            results = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]))
                    .thenApply(ignored -> cfs.stream().map(CompletableFuture::join).toList())
                    .join();
            assertEquals(10, results.size());
            info("CompletableFuture - DONE!");
        }
    }

}
