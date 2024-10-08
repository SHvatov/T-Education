package ru.tbank.edu.fintech.lecture8.java.util.concurrent;

import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.info;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.getCurrentThreadName;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.sleep;
import static ru.tbank.edu.fintech.lecture8.utils.ThreadUtils.withThreadInterruptionHandled;


public class SynchronizedAndAtomicTest {

    @Test
    @DisplayName("Примеры использования синхронизированных коллекций")
    void test0() {
        var unSyncMap = new TreeMap<>((_, _) -> {
            sleep(10);
            return 0;
        });

        var syncMap = Collections.synchronizedMap(
                new TreeMap<>((_, _) -> {
                    sleep(10);
                    return 0;
                })
        );

        var latch = new CountDownLatch(1);

        final var key = "test";
        final var map = unSyncMap;
        IntStream.range(0, 5).forEach(ignored ->
                Thread.ofPlatform().start(() -> {
                    info("Поток {0} ждет старта!", getCurrentThreadName());
                    withThreadInterruptionHandled(latch::await);
                    info("Поток {0} стартовал!", getCurrentThreadName());
                    var exists = map.putIfAbsent(key, key + ignored) != null;
                    info("[{0} нс] Ключ {1} присутствует в коллекции? {2}.",
                            System.nanoTime(), key, exists ? "Да" : "Нет");
                }));

        sleep(1_000);
        latch.countDown();
        sleep(5_000);
        info("Итоговое значение: {0}", map.get(key));
    }

    @Test
    @DisplayName("Примеры использования атомарных примитивов")
    void test1() {
        final var nonAtomic = new Object() {
            private int value = 0;

            public int value() {
                return value;
            }

            public void increment() {
                sleep(1); // чтобы вызвать явное переключение потоков
                value++;
            }
        };

        var latch0 = new CountDownLatch(1);

        IntStream.range(0, 5).forEach(ignored ->
                Thread.ofPlatform().start(() -> {
                    info("Поток {0} ждет старта!", getCurrentThreadName());
                    withThreadInterruptionHandled(latch0::await);
                    info("Поток {0} стартовал!", getCurrentThreadName());
                    while (nonAtomic.value() < 1_000) {
                        nonAtomic.increment();
                    }
                }));

        latch0.countDown();
        sleep(1000);

        info("Значение не атомарного счетчика: {0}", nonAtomic.value());

        var latch1 = new CountDownLatch(1);

        final var atomic = new AtomicInteger(0);
        IntStream.range(0, 5).forEach(ignored ->
                Thread.ofPlatform().start(() -> {
                    info("Поток {0} ждет старта!", getCurrentThreadName());
                    withThreadInterruptionHandled(latch1::await);
                    info("Поток {0} стартовал!", getCurrentThreadName());
                    while (atomic.updateAndGet(it -> it != 1000 ? it + 1 : it) < 1_000) {
                        sleep(1); // чтобы вызвать явное переключение потоков
                    }
                }));

        latch1.countDown();
        sleep(1000);

        info("Значение атомарного счетчика: {0}", atomic.get());
    }

    @Test
    @DisplayName("Примеры использования синхронизаторов - CountDownLatch")
    void test2_0() {
        record Fio(String name, String surname) {

        }

        record PaymentData(String cardNumber, String cvv) {

        }

        @Data
        @Accessors(chain = true)
        class Client {

            private final UUID id;

            // Note: заполняются походом в смежные системы
            private PaymentData paymentData;
            private Fio fio;

        }

        var client = new Client(UUID.randomUUID());
        var latch = new CountDownLatch(2);

        Thread.ofPlatform().start(() -> {
            withThreadInterruptionHandled(() -> {
                while (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                    info("Все еще ждем, пока будут заполнены все данные о клиенте...");
                }

                info("Все данные о клиенте заполнены: {0}", client);
            });
        });

        Thread.ofPlatform()
                .start(() -> {
                    info("Эмулируем поход в смежную систему за ФИО пользователя");
                    sleep(5_000);

                    info("Получили ФИО пользователя!");
                    client.setFio(new Fio("Sergey", "Khvatov"));
                    latch.countDown();
                });

        Thread.ofPlatform()
                .start(() -> {
                    info("Эмулируем поход в смежную систему за платежной информацией пользователя");
                    sleep(2_000);

                    info("Получили платежную информацию пользователя!");
                    client.setPaymentData(new PaymentData("1234 xxxx 5678", "000"));
                    latch.countDown();
                });

        sleep(6_000);
    }

    @Test
    @DisplayName("Примеры использования синхронизаторов - Exchanger")
    void test2_1() {
        var exchanger = new Exchanger<String>();

        Thread.ofPlatform()
                .start(() -> {
                    info("Данный поток ожидаем секретного сообщения для обмена...");
                    withThreadInterruptionHandled(() -> {
                        var message = exchanger.exchange("Wow! So secure!", 1_000, TimeUnit.MILLISECONDS);
                        info("Полученное в ответ сообщение: {0}", message);
                    });
                });

        Thread.ofPlatform()
                .start(() -> {
                    info("Данный поток генерирует секретное сообщение для обмена...");
                    sleep(500);

                    withThreadInterruptionHandled(() -> {
                        var secretMessage = "Very Secret Message".replace("e", "X");
                        var message = exchanger.exchange(secretMessage, 1_000, TimeUnit.MILLISECONDS);
                        info("Полученное в ответ сообщение: {0}", message);
                    });
                });

        sleep(2_000);
    }

    @Test
    @DisplayName("Примеры использования синхронизаторов - ReentrantLock")
    void test2_2() {
        var lock = new ReentrantReadWriteLock();

        final var nonAtomic = new Object() {
            private int value = 0;

            public int value() {
                return value;
            }

            public void increment() {
                sleep(1); // чтобы вызвать явное переключение потоков
                value++;
            }
        };

        IntStream.range(0, 3).forEach(it ->
                Thread.ofPlatform()
                        .name("reader-%s".formatted(it))
                        .start(() -> {
                            info("Поток {0} только читает значение счетчика!", getCurrentThreadName());

                            var readLock = lock.readLock();
                            while (!Thread.currentThread().isInterrupted()) {
                                withThreadInterruptionHandled(() -> {
                                    while (!readLock.tryLock(300, TimeUnit.MILLISECONDS)) {
                                        info("Возможность чтения все еще заблокирована!");
                                    }
                                });

                                info("Значение счетчика: {0}", nonAtomic.value());
                                sleep(200);
                                readLock.unlock();
                            }
                        }));

        Thread.ofPlatform()
                .name("writer")
                .start(() -> {
                    info("Поток {0} только и читает, и ПИШЕТ значение счетчика!", getCurrentThreadName());

                    var writeLock = lock.writeLock();
                    while (!Thread.currentThread().isInterrupted()) {
                        withThreadInterruptionHandled(() -> {
                            while (!writeLock.tryLock(300, TimeUnit.MILLISECONDS)) {
                                info("Возможность записи все еще заблокирована, так как есть читатели!");
                            }
                        });

                        info("Увеличиваем значение счетчика!");
                        nonAtomic.increment();
                        sleep(500);

                        info("Значение счетчика после увеличения: {0}", nonAtomic.value());
                        writeLock.unlock();
                    }
                });

        sleep(2_000);
    }

}
