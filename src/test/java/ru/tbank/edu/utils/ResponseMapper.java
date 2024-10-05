package ru.tbank.edu.utils;

import lombok.SneakyThrows;

import java.util.function.Function;


@FunctionalInterface
public interface ResponseMapper<I, O> extends Function<I, O> {

    O map(I response) throws Exception;

    @Override
    @SneakyThrows
    default O apply(I i) {
        return map(i);
    }

}
