package ru.tbank.edu.fintech.lecture8.real_life;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;


public class NonBlockingApiProcessingTest {

    private static final String MOCK_URL =
            "https://run.mocky.io/v3/73339955-5c2a-4f02-a15d-69872ca78806"
                    + "?mocky-delay=%sms";

    private static final HttpClient CLIENT = HttpClient.create();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();

    private record Fio(String name, String surname) {

    }

    private record PaymentData(String cardNumber, String cvv) {

    }

    // Example:
    // {
    //     "id": "ca9cc3c7-3a7b-49b9-97c4-71f591aad7d8",
    //     "fio": {
    //         "name": "Sergey",
    //         "surname": "Khvatov"
    //     },
    //     "paymentData": {
    //         "cardNumber": "1234567890",
    //         "cvv": "000"
    //     }
    // }
    private record Client(UUID id, Fio fio, PaymentData paymentData) {

    }

    @Test
    @DisplayName("Пример неблокирующего похода и обработки данных из смежной системы")
    void test() {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var scheduler = Schedulers.fromExecutor(executor);

            var clients = IntStream.range(0, 10)
                    .mapToObj(ignored -> getClient().subscribeOn(scheduler))
                    .toList();

            Flux.merge(clients)
                    .publishOn(scheduler)
                    .onBackpressureBuffer(10)
                    // .onBackpressureDrop()
                    .concatMap(NonBlockingApiProcessingTest::processClient)
                    .count()
                    .doOnNext(count -> info("Всего обработано {0} клиентов", count))
                    .then()
                    .block();
        }
    }

    private static Mono<Client> getClient() {
        return Mono.defer(() -> {
                    info("Запрашиваем данные о клиенте в потоке {0}...", getCurrentThreadName());

                    // Генерируем случайную задержку при ответе
                    var delay = 1_000 + RANDOM.nextInt(100, 1_000);
                    return getClient(delay);
                })
                .elapsed()
                .map(it -> {
                    var timeSpent = it.getT1();
                    var client = it.getT2();

                    info(
                            "Получили информацию о клиенте в потоке {0}, затраченное время: {1}",
                            getCurrentThreadName(),
                            timeSpent);
                    return client;
                });
    }

    private static Mono<Client> processClient(Client client) {
        return Mono.fromCallable(() -> {
            info(
                    "Получена информация о клиенте в потоке {0}! Эмулируем долгую операцию обработки...",
                    getCurrentThreadName());
            sleep(1_000);
            info("Обработка клиента завершена успешно!");
            return client;
        }).subscribeOn(Schedulers.single());
    }

    @SneakyThrows
    private static Mono<Client> getClient(int delay) {
        return CLIENT.get()
                .uri(MOCK_URL.formatted(delay))
                .response((rs, bytes) -> {
                    info(
                            "Получен ответ с кодом {0} при выполнении запроса {1} {2}",
                            rs.status().codeAsText(), rs.method(), rs.uri());

                    if (Objects.equals(rs.status(), HttpResponseStatus.OK)) {
                        return bytes.aggregate().asString();
                    }
                    return bytes.aggregate().asString()
                            .flatMap(body ->
                                    Mono.error(
                                            new RuntimeException(
                                                    ("Ошибка при обращении ко внешнему API. "
                                                            + "Тело ответа: %s").formatted(body))));
                })
                .mapNotNull(it -> {
                    try {
                        return OBJECT_MAPPER.readValue(it, Client.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .next();
    }

}
