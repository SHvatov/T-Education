package ru.tbank.edu.ab.sem3;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


public class SealedVsEnum {

    public static class SealedExample {

        @Data
        public static sealed abstract class Color {

            private final String rgb;

            @SneakyThrows
            public static Optional<Color> fromRgb(String rgb) {
                return Arrays.stream(Color.class.getPermittedSubclasses())
                        .map(it -> {
                            try {
                                // noinspection deprecation
                                return it.newInstance();
                            } catch (Throwable exception) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(Color.class::isInstance)
                        .map(Color.class::cast)
                        .filter(it -> Objects.equals(it.rgb, rgb))
                        .findFirst();
            }

        }

        public static final class Red extends Color {

            public Red() {
                super("#FF0000");
            }

        }

        public static final class Green extends Color {

            public Green() {
                super("#00FF00");
            }

        }

        public static final class Blue extends Color {

            public Blue() {
                super("#0000FF");
            }

        }

    }

    public static class EnumExample {

        @Getter
        @RequiredArgsConstructor
        public enum Color {
            RED("#FF0000"), GREEN("#00FF00"), BLUE("#0000FF");

            private final String rgb;

            public static Optional<Color> fromRgb(String rgb) {
                return Arrays.stream(values())
                        .filter(it -> Objects.equals(it.rgb, rgb))
                        .findFirst();
            }
        }

    }

}
