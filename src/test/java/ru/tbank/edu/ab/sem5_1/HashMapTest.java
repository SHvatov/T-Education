package ru.tbank.edu.ab.sem5_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.stream.IntStream;


class HashMapTest {

    @Test
    @DisplayName("HashMap - how to")
    void test() {
        var map = new HashMap<String, String>();

        // Добавление элементов
        System.out.println("put() №1: " + map.put("key", "value"));
        System.out.println("put() №2: " + map.put("key", "value1"));
        System.out.println("putIfAbsent(): " + map.putIfAbsent("key", "value2"));
        System.out.println("get(): " + map.get("key"));
        System.out.println("computeIfAbsent(): " + // compute, computeIfPresent
                map.computeIfAbsent(
                        "key1",
                        (ignored) -> IntStream.range(65, 70)
                                .mapToObj(it -> (char) it)
                                .map(Object::toString)
                                .reduce("", (acc, el) -> acc + el)));

        // Получение элементов
        System.out.println("get(key): " + map.get("key"));
        System.out.println("get(key1): " + map.get("key1"));
        System.out.println("get(key2): " + map.get("key2"));
        System.out.println("containsKey(key1): " + map.containsKey("key1"));
        System.out.println("containsKey(key2): " + map.containsKey("key2"));

        // todo: тут посмотреть, что под капотом и как работает put
        map.put("key3", "value3"); // new
        map.put("key", "value3"); // old
        map.forEach((k, v) -> System.out.printf("Key: %s, Value: %s%n", k, v));

        // Дополнительно
        System.out.println("remove(key) №1: " + map.remove("key"));
        System.out.println("remove(key) №2: " + map.remove("key"));
        System.out.println(map.keySet());
        System.out.println(map.values());
        System.out.println(map.entrySet());
    }

}
