package ru.tbank.edu.ab.sem5_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tbank.edu.utils.events.EnrichedEvent;
import ru.tbank.edu.utils.events.EventUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


class ParallelStreamExampleTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static class EventsSpliterator implements Spliterator<EnrichedEvent> {

        private final List<EnrichedEvent> events;

        public EventsSpliterator(final List<EnrichedEvent> events) {
            this.events = new ArrayList<>(events);
        }

        @Override
        public boolean tryAdvance(Consumer<? super EnrichedEvent> action) {
            for (var event : events) {
                action.accept(event);
            }
            return false;
        }

        @Override
        public Spliterator<EnrichedEvent> trySplit() {
            var uniquePlaces = events.stream()
                    .map(EnrichedEvent::place)
                    .distinct()
                    .toList();
            if (uniquePlaces.size() <= 1) {
                return null;
            }

            var randomPlace = uniquePlaces.get(RANDOM.nextInt(0, uniquePlaces.size()));
            var eventsPartition = events.stream()
                    .filter(it -> Objects.equals(it.place(), randomPlace))
                    .toList();
            events.removeAll(eventsPartition);
            return new EventsSpliterator(eventsPartition);
        }

        @Override
        public long estimateSize() {
            return events.size();
        }

        @Override
        public int characteristics() {
            return Spliterator.CONCURRENT | Spliterator.SUBSIZED;
        }

    }

    @Test
    @DisplayName("Пример параллелизации Stream'а")
    void test() {
        var events = EventUtils.getEvents(100);
        StreamSupport.stream(new EventsSpliterator(events), true)
                .map(event ->
                        String.format(
                                "Обрабатываем событие в %s на потоке %s",
                                event.place(), Thread.currentThread().getName()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((key, value) -> System.out.println(key + ": " + value + " событий"));
    }

}
