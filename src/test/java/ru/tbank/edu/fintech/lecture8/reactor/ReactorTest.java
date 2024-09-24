package ru.tbank.edu.fintech.lecture8.reactor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.error;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;


public class ReactorTest {

    @Test
    @DisplayName("Пример работы с Project Reactor")
    void test0() {
        // Потенциально бесконечный генератор данных
        Flux<Integer> fibonacci = Flux.generate(() -> Tuples.of(1, 1), (state, sink) -> {
            var fib = state.getT1() + state.getT2();
            sink.next(fib);
            return Tuples.of(state.getT2(), fib);
        });

        Flux<Tuple2<Integer, Integer>> fibonacciSquares = fibonacci.map(it -> Tuples.of(it, it * it));

        fibonacciSquares.take(10)
                .doOnNext(it -> info("Квадрат числа Фибоначчи {0} равен {1}", it.getT1(), it.getT2()))
                .flatMap(it -> it.getT2() > 1_000
                        ? Mono.error(new RuntimeException("Зашли за лимиты!"))
                        : Mono.just(it))
                .onErrorContinue(
                        RuntimeException.class,
                        (ex, ignored) -> error("Ошибка при вычислении квадратов чисел Фибоначчи", ex))
                .blockLast();
    }

    @Test
    @DisplayName("Backpressure из коробки")
    void test1() {
        Flux.interval(Duration.ofSeconds(1))
                .take(1_000)
                // .onBackpressureDrop()
                .onBackpressureBuffer(10)
                // .onBackpressureLatest()
                .map(it -> {
                    var value = "Ping " + it;
                    info("[{0}] Генерируем событие: {1}", getCurrentThreadName(), value);
                    return value;
                })
                // .flatMap
                .concatMap(value -> {
                    info("[{0}] Готовимся к обработке события {1}", getCurrentThreadName(), value);
                    return Mono.delay(Duration.ofSeconds(2))
                            .doOnNext(_ -> info("[{0}] Обрабатываем событие {1}", getCurrentThreadName(), value));
                })
                .blockLast();
    }

}
