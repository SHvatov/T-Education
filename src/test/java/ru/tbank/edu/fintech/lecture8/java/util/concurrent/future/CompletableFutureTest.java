package ru.tbank.edu.fintech.lecture8.java.util.concurrent.future;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;


public class CompletableFutureTest {

    @Test
    @SneakyThrows
    @DisplayName("Java 8 - апгрейд для Future")
    void test0() {
        try (var executor = Executors.newFixedThreadPool(2)) {
            // Что это такое?
            Future<Integer> future = CompletableFuture.supplyAsync(() -> 0, executor);
            CompletionStage<Integer> stage = CompletableFuture.supplyAsync(() -> 0, executor);

            // 2. Что под капотом?
            // result + stack, AltResult
            CompletableFuture.supplyAsync(() -> 0, executor)
                    .thenApplyAsync(it -> it + 1, executor)
                    .thenAccept(it -> info("Итоговое значение: {0}", it))
                    // 3. Как дождаться? Новый метод join!
                    .join();

            // 3. Как завершить Future?
            // 3.1 complete*
            var f1 = prepareDelayedFuture(1_000, executor);
            // f1.cancel(true);
            f1.complete(-10); // future still completes
            assertEquals(-10, f1.resultNow());

            f1 = prepareDelayedFuture(1_000, executor);
            f1.completeExceptionally(new RuntimeException());
            assertNotNull(f1.exceptionNow());

            f1 = prepareDelayedFuture(1_000, executor);
            f1.completeOnTimeout(-10, 500, TimeUnit.MILLISECONDS);
            assertEquals(-10, f1.get());

            // 3.2 cancel
            var start = System.currentTimeMillis();
            f1 = prepareDelayedFuture(5_000, executor);
            f1.cancel(true);
            assertTrue(f1.isCancelled());
            assertTrue(System.currentTimeMillis() - start < 5_000);

            // 3.3 timeout

            // 3.4 source complete
            f1 = prepareDelayedFuture(5_000, executor);
            var f2 = f1.thenApply(ignored -> -1);

            f1.completeExceptionally(new RuntimeException());
            assertTrue(f2.isCompletedExceptionally());

            // 4. Где это все исполняется?
            CompletableFuture
                    .runAsync(() -> info("Я работаю в потоке {0}", getCurrentThreadName()), executor)
                    .thenRunAsync(() -> info("Я работаю в дефолтном потоке {0}", getCurrentThreadName()))
                    .thenRun(() -> info("А я вообще работаю в потоке {0}", getCurrentThreadName()))
                    .join();
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Пример сложного флоу с использованием CompletableFuture")
    void test1() {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var id = UUID.randomUUID();

            var fioSupplier = CompletableFuture.supplyAsync(() -> getClientFio(id), executor);
            var paymentDataSupplier = CompletableFuture.supplyAsync(() -> getPaymentData(id), executor)
                    .thenComposeAsync(paymentData ->
                            CompletableFuture.supplyAsync(
                                            () -> isOperationAllowedByFraudPrevention(paymentData.cardNumber),
                                            executor)
                                    .handle(((fraudPreventionData, _) -> {
                                        if (fraudPreventionData.allowed()) {
                                            return paymentData;
                                        }
                                        throw new RuntimeException("Операция заблокирована отделом безопасности");
                                    })));

            fioSupplier.runAfterBoth(
                            paymentDataSupplier,
                            () -> {
                                var fio = fioSupplier.resultNow();
                                var paymentData = paymentDataSupplier.resultNow();
                                info(
                                        "Транзакция для клиента {0} {1} по карте {2} разрешена",
                                        fio.name(), fio.surname(), paymentData.cardNumber());
                            })
                    .join();
        }
    }

    private static Fio getClientFio(UUID id) {
        info("Получаем ФИО клиента с ИД {0}", id);
        // Эмулируем поход в смежную систему
        sleep(1_500);
        return new Fio("Sergey", "Khvatov");
    }

    private static PaymentData getPaymentData(UUID id) {
        info("Получаем платежную информацию клиента с ИД {0}", id);
        // Эмулируем поход в смежную систему
        sleep(1_000);
        return new PaymentData("0123456789", "000");
    }

    private static FraudPreventionData isOperationAllowedByFraudPrevention(String cardNumber) {
        info("Проверяем, разрешены ли выплаты по карты {0}", cardNumber);
        // Эмулируем поход в смежную систему
        sleep(3_000);
        return new FraudPreventionData(true, "");
    }

    private record Fio(String name, String surname) {

    }

    private record PaymentData(String cardNumber, String cvv) {

    }

    private record FraudPreventionData(boolean allowed, String reason) {

    }

    @Data
    @Accessors(chain = true)
    private static class Client {

        private final UUID id;

        // Note: заполняются походом в смежные системы
        private PaymentData paymentData;
        private Fio fio;

    }

    private static CompletableFuture<Integer> prepareDelayedFuture(int delay, Executor executor) {
        return CompletableFuture.runAsync(() -> {
                    sleep(delay);
                    info("А я все равно завершился!");
                }, executor)
                .thenApplyAsync(_ -> (int) (Math.random() * 30));
    }

}
