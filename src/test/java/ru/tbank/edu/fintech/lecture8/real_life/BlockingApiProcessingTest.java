package ru.tbank.edu.fintech.lecture8.real_life;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.error;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;


public class BlockingApiProcessingTest {

    private static final String MOCK_URL =
            "https://run.mocky.io/v3/73339955-5c2a-4f02-a15d-69872ca78806"
                    + "?mocky-delay=%sms";

    private static final int MAX_QUEUE_OFFER_ATTEMPTS = 2;
    private static final int MAX_QUEUE_POLL_ATTEMPTS = 30;

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
    @DisplayName("Пример блокирующего похода и обработки данных из смежной системы")
    void test() {
        try (
                var api = Executors.newFixedThreadPool(3);
                var processor = Executors.newSingleThreadExecutor()) {

            final var queue = new ArrayBlockingQueue<Client>(3);

            processor.submit(() -> processClients(queue));

            for (int i = 0; i < 10; i++) {
                api.submit(() -> getClient(queue));
            }
        }
    }

    @SneakyThrows
    private void processClients(ArrayBlockingQueue<Client> queue) {
        var processedClients = 0;
        var emptyQueueChecks = MAX_QUEUE_POLL_ATTEMPTS;
        while (!Thread.currentThread().isInterrupted()) {
            Client client;
            while ((client = queue.poll(100, TimeUnit.MILLISECONDS)) == null) {
                if (--emptyQueueChecks <= 0) {
                    info("Исчерпано максимальное кол-во попыток проверки очереди на наличие данных!");
                    info("Общее кол-во обработанных клиентов: {0}", processedClients);
                    return;
                }

                info("В очереди нет информации о клиентах, ждем-с...");
                sleep(100);
            }

            info("Получена информация о клиенте! Эмулируем долгую операцию обработки...");
            sleep(2_000);
            info("Обработка клиента завершена успешно!");
            processedClients++;
            emptyQueueChecks = MAX_QUEUE_POLL_ATTEMPTS;
        }
    }

    @SneakyThrows
    private static void getClient(BlockingQueue<Client> queue) {
        info("Запрашиваем данные о клиенте в потоке {0}...", getCurrentThreadName());

        // Генерируем случайную задержку при ответе
        var delay = 1_000 + RANDOM.nextInt(100, 1_000);

        var start = System.currentTimeMillis();
        var client = getClient(delay);

        var timeSpent = System.currentTimeMillis() - start;
        info("Получена информация о клиенте в потоке {0}. Затраченное время: {1}.", getCurrentThreadName(), timeSpent);

        var attempts = MAX_QUEUE_OFFER_ATTEMPTS;
        while (!queue.offer(client, 500, TimeUnit.MILLISECONDS)) {
            error("Очередь переполнена, передать данные о клиенте невозможно...");
            if (--attempts > 0) {
                info("Ждем освобождения очереди...");
                sleep(100);
                continue;
            }

            error("Исчерпано максимальное кол-во попыток, данные о клиенте будут потеряны...");
            break;
        }
    }

    @SneakyThrows
    private static Client getClient(int delay) {
        var url = URI.create(MOCK_URL.formatted(delay)).toURL();

        var connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");

            var response = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return OBJECT_MAPPER.readValue(response.toString(), Client.class);
        } finally {
            connection.disconnect();
        }
    }

}
