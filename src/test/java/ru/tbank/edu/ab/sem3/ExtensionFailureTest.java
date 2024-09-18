package ru.tbank.edu.ab.sem3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

class ExtensionFailureTest {

    @Test
    @DisplayName("Проверка работы класса LoggingHashSet")
    void test1() {
        var set = new ExtensionFailure.LoggingHashSet<String>(System.out);
        set.add("test");
        set.addAll(Set.of("Hello", "World"));
    }

    @Test
    @DisplayName("Проверка работы класса LoggingHashSetV2")
    void test2() {
        var set = new ExtensionFailure.LoggingHashSetV2<String>(System.out);
        set.add("test");
        set.addAll(Set.of("Hello", "World"));
    }

}
