package ru.tbank.edu.ab.sem5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;


public class ArraysTest {

    @Test
    @DisplayName("Основы работы с массивами в Java")
    void test0() {
        // 1. Объявление
        String[] strings = new String[10];
        long[] longNumbers = new long[10];
        int[] numbers = new int[] { 0, 1, 2, 3, 4, 5 }; // Объявление + инициализация

        System.out.printf("Значение в массиве strings до инициализации: %s%n", strings[0]);
        System.out.printf("Значение в массиве longNumbers до инициализации: %s%n", longNumbers[0]);

        // 2. Итерация и обращение к элементам массива

        // 2.1 Классический цикл
        for (int i = 0; i < numbers.length; i++) {
            // do something
        }

        // 2.2 for each
        for (var number : numbers) {
            // do something
        }

        // 3. Алгоритмическая сложность операций в ArrayList
        var list = new BasicArrayList<String>(3);

        // O(1)
        list.add("test1");
        list.add("test2");
        list.add("test3");
        System.out.println(list);

        // O(1)
        System.out.println(list.get(1));

        // O(N)
        list.add(1, "test4");
        System.out.println(list);

        // 4. Вспомогательные методы - класс Arrays!
        Arrays.toString(numbers);
        Arrays.copyOf(numbers, numbers.length * 2);
        Arrays.binarySearch(numbers, 3);
        // todo: посмотреть на другие методы в том числе!
    }

    @SuppressWarnings("unchecked")
    private static class BasicArrayList<E> {

        private Object[] elements;
        private int size;

        public BasicArrayList(int size) {
            // elements = new E[size]; - так будет ошибка компиляции, поэтому работаем только с Object
            elements = new Object[size];
        }

        public E get(int index) {
            assert index >= 0 && index < size;
            return (E) elements[index];
        }

        public void add(E element) {
            elements[size++] = element;
        }

        public void add(int index, E element) {
            assert index >= 0 && index < size;

            // 1. Проверяем, а не надо ли нам аллоцировать память
            if (size + 1 >= elements.length) {
                elements = Arrays.copyOf(elements, elements.length * 2);
            }

            // 2. Двигаем элементы вправо
            for (int i = size; i > index; i--) {
                elements[i] = elements[i - 1];
            }

            // 3. Сохраняем элемент
            elements[index] = element;
        }

        @Override
        public String toString() {
            return Arrays.toString(elements);
        }

    }

}
