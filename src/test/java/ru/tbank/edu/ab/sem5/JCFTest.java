package ru.tbank.edu.ab.sem5;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class JCFTest {

    // concurrent modification error

    @Test
    @DisplayName("Работа со списками")
    void test0() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }

        System.out.println(numbers);

        // Get
        System.out.println(numbers.get(5));
        System.out.println(numbers.getFirst());
        System.out.println(numbers.getLast());

        // Add
        numbers.add(100);
        numbers.addFirst(30);
        numbers.addLast(30);

        System.out.println(numbers);

        // Find
        System.out.println(numbers.indexOf(30));
        System.out.println(numbers.lastIndexOf(30));

        // Sort
        numbers.sort(Comparator.naturalOrder());
        System.out.println(numbers);

        // Remove
        numbers.removeIf(it -> it % 2 == 0);
        System.out.println(numbers);

        // Contains
        System.out.println(numbers.contains(2));
        System.out.println(numbers.contains(1));

        // Фабрики и полезные методы
        List.of(1, 2, 3, 4);
        Collections.emptyList();
        Collections.unmodifiableList(numbers);

        System.out.println(Collections.binarySearch(numbers, 7));
        System.out.println(Collections.binarySearch(numbers, 2));
    }

    @Test
    @DisplayName("Работа с множествами")
    void test1() {
        // 1. Виды множеств
        Consumer<Set<String>> init = set -> {
            set.add("test1");
            set.add("test11");
            set.add("test21");
            set.add("test0");
        };

        var hashSet = new HashSet<String>(); // Под капотом - HashMap
        init.accept(hashSet);
        System.out.println(hashSet);

        var linkedHashSet = new LinkedHashSet<String>();
        init.accept(linkedHashSet);
        System.out.println(linkedHashSet);

        var treeSet = new TreeSet<String>(Comparator.naturalOrder()); // Под капотом - TreeMap
        init.accept(treeSet);
        System.out.println(treeSet);

        // 2. Основные методы
        // 2.1 Классическое множество
        hashSet.add("test3");
        hashSet.contains("test1");

        // 2.2 Sorted & Navigable Sets
        System.out.println(treeSet);
        System.out.println(treeSet.headSet("test1", true));
        System.out.println(treeSet.tailSet("test11", true));
        System.out.println(treeSet.descendingSet());
        System.out.println(treeSet.first());
        System.out.println(treeSet.last());
        System.out.println(treeSet.higher("test112"));
        System.out.println(treeSet.lower("test112"));

        // 2.3 Основные операции над множествами
        hashSet.addAll(Set.of("1", "2", "3")); // union
        System.out.println(hashSet);
        hashSet.removeAll(Set.of("1", "2", "3")); // complement
        System.out.println(hashSet);
        hashSet.retainAll(Set.of("test1", "test11", "3", "4")); // intersection
        System.out.println(hashSet);
    }

    @Test
    @DisplayName("Работа с мапами")
    void test2() {
        // 1. Способы создания
        new HashMap<String, String>();
        Map.of("key", "value");
        Collections.emptyMap();

        // 2. Работа с мапами и важность equals & hashCode
        @ToString
        @RequiredArgsConstructor
        class CustomKey {

            private final long created = System.currentTimeMillis();
            private final String key;

            @Override
            public int hashCode() {
                return (int) (System.currentTimeMillis() - created);
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

        }

        var map = new HashMap<CustomKey, String>();
        System.out.println(map.put(new CustomKey("key"), "value1"));
        System.out.println(map.put(new CustomKey("key"), "value2"));
        System.out.println(map.put(new CustomKey("key"), "value3"));

        var key = new CustomKey("key");
        System.out.println(map.put(key, "value4"));
        System.out.println(map.put(key, "value5"));
    }

    @Test
    @DisplayName("Iterable & Iterator")
    void test3() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }

        var iterator = numbers.iterator();
        while (iterator.hasNext()) {
            var value = iterator.next();
            System.out.println(value);
        }

        for (var number : numbers) { // Под капотом - iterator
            System.out.println(number);
            numbers.remove(number); // кидает исключение
        }
        System.out.println(numbers);

        iterator = numbers.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        System.out.println(numbers);
    }

}
