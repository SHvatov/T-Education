package ru.tbank.edu.ab.sem3;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ExtensionFailure {

    @RequiredArgsConstructor
    public static class LoggingHashSet<T> extends HashSet<T> {
        private final PrintStream out;

        @Override
        public boolean add(T t) {
            out.printf("Добавление элемента %s%n", t);
            return super.add(t);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            out.printf(
                    "Добавление элементов коллекции %s%n",
                    c.stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining(", ")));
            return super.addAll(c);
        }
    }

    @RequiredArgsConstructor
    public static class LoggingHashSetV2<T> implements Set<T> {
        private final PrintStream out;

        @Delegate
        private final Set<T> delegate = new HashSet<>();

        @Override
        public boolean add(T t) {
            out.printf("Добавление элемента %s%n", t);
            return delegate.add(t);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            out.printf(
                    "Добавление элементов коллекции %s%n",
                    c.stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining(", ")));
            return delegate.addAll(c);
        }
    }
}
