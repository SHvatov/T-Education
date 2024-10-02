package ru.tbank.edu.ab.sem5;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class StreamsTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(Feature.IGNORE_UNKNOWN, true);
    private static final Random RANDOM = new Random();

    private static final ResponseMapper<String, Events> EVENTS_MAPPER =
            json -> OBJECT_MAPPER.readValue(json, Events.class);

    private static final List<String> PLACES =
            List.of("Эрмитаж", "Зимний дворец", "Петропавловская крепость", "Петергоф", "Исаакиевский собор",
                    "Часовня Ильи Муромца на Ладожском озере", "Петербургский метрополитен", "Петергофский парк",
                    "Крестовоздвиженский собор", "Летний сад");

    private static final int DEFAULT_PAGE_SIZE = 20;

    @FunctionalInterface
    private interface ResponseMapper<I, O> extends Function<I, O> {

        O map(I response) throws Exception;

        @Override
        @SneakyThrows
        default O apply(I i) {
            return map(i);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Events(@JsonProperty("results") List<Event> events) {

        @JsonCreator
        public Events {
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Event(
            @JsonProperty("id") int id,
            @JsonProperty("title") String title,
            @JsonProperty("comments_count") int comments,
            @JsonProperty("favorites_count") int favorites) {

        @JsonCreator
        public Event {
        }

    }

    private record EnrichedEvent(
            int id, String title,
            int comments, int favorites,
            LocalDate date, String place, int price) {

        public EnrichedEvent(Event event) {
            this(
                    event.id(), event.title(), event.comments(), event.favorites(),
                    LocalDate.now().plusDays(RANDOM.nextInt(0, 14)),
                    PLACES.get(RANDOM.nextInt(0, PLACES.size() - 1)),
                    RANDOM.nextInt(0, 1000));
        }

    }

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

            var randomPlace = uniquePlaces.get(RANDOM.nextInt(0, uniquePlaces.size() - 1));
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
    @DisplayName("Работа со Stream'ами")
    void test0() {
        var events = Stream.iterate(0, i -> i + 1)
                .map(page -> getEvents(page + 1))
                .map(EVENTS_MAPPER)
                .map(Events::events)
                .flatMap(Collection::stream)
                .map(EnrichedEvent::new)
                .limit(300)
                .toList();
        System.out.printf("Кол-во событий: %s%n", events.size());

        // todo: показать примеры на котлине)
        findUpcomingEvents(events, 7);
        findTopThreeEvents(events);
        scheduleTopEventsForBudget(events, 2_000);
        findEventsDistributionByDayOfTheWeek(events);
        findEventsDistributionByPlace(events);
    }

    private void findTopThreeEvents(List<EnrichedEvent> events) {
        printResult(
                events.stream()
                        .sorted(Comparator.comparingInt(EnrichedEvent::favorites).reversed())
                        .limit(3));
    }

    private void scheduleTopEventsForBudget(List<EnrichedEvent> events, int budget) {
        var result = events.stream()
                .sorted(Comparator.comparingInt(EnrichedEvent::favorites).reversed())
                .map(it -> Tuples.of(it.price(), List.of(it)))
                .reduce(Tuples.of(budget, new ArrayList<>()), (acc, event) -> {
                    var remains = acc.getT1() - event.getT1();
                    if (remains >= 0) {
                        acc.getT2().addAll(event.getT2());
                        return Tuples.of(remains, acc.getT2());
                    }
                    return acc;
                });

        var eventsDescription = result.getT2().stream()
                .map(it -> "\"%s\" за %s деняк (%s лайков)".formatted(it.title(), it.price(), it.favorites()))
                .collect(Collectors.joining("\n\t- "));
        System.out.printf(
                "На сумму %d мы сможем посетить %d мероприятий, и у нас еще останется %d деняк: \n\t- %s\n",
                budget, result.getT1(), result.getT2().size(), eventsDescription);
    }

    private void findEventsDistributionByDayOfTheWeek(List<EnrichedEvent> events) {
        events.stream()
                .collect(Collectors.groupingBy(it -> it.date().getDayOfWeek(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(it -> System.out.printf("На %s приходится %d событий%n", it.getKey(), it.getValue()));
    }

    private void findEventsDistributionByPlace(List<EnrichedEvent> events) {
        // todo
    }

    private void findUpcomingEvents(List<EnrichedEvent> events, int daysFromToday) {
        // todo
    }

    private static <T> void printResult(Stream<T> stream) {
        var result = stream.toList();
        System.out.println("Результат работы пайплайна: " + result);
    }

    // https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html
    @Test
    @DisplayName("Типы операций в стримах")
    void test1() {
        // Streams vs for loops
        var events = Stream.iterate(0, i -> i + 1) // source
                // intermediate operations
                .map(page -> getEvents(page + 1))
                .map(EVENTS_MAPPER)
                .map(Events::events)
                .flatMap(Collection::stream)
                .map(EnrichedEvent::new) // statless operation
                .limit(300) // stateful operation
                // terminal operation
                .toList();
    }

    @Test
    @DisplayName("Stream vs Loop")
    void test2() {
        final var desiredEventsCount = 30;

        // Streams vs for loops
        var events1 = Stream.iterate(0, i -> i + 1) // source
                // intermediate operations
                .map(page -> getEvents(page + 1))
                .map(EVENTS_MAPPER)
                .map(Events::events)
                .flatMap(Collection::stream)
                .map(EnrichedEvent::new) // statless operation
                .limit(desiredEventsCount) // stateful operation
                // terminal operation
                .toList();

        var events2 = new ArrayList<>();
        var page = 1;
        while (true) {
            var eventsPage = EVENTS_MAPPER.apply(getEvents(page++)).events();
            if (eventsPage.isEmpty()) {
                break;
            }

            for (var event : eventsPage) {
                events2.add(new EnrichedEvent(event));
                if (events2.size() == desiredEventsCount) {
                    break;
                }
            }

            if (events2.size() == desiredEventsCount) {
                break;
            }
        }
    }

    @Test
    @DisplayName("Пример параллелизации Stream'а")
    void test3() {
        var events = Stream.iterate(0, i -> i + 1)
                .map(page -> getEvents(page + 1))
                .map(EVENTS_MAPPER)
                .map(Events::events)
                .flatMap(Collection::stream)
                .map(EnrichedEvent::new)
                .limit(300)
                .toList();

        StreamSupport.stream(new EventsSpliterator(events), true)
                .map(event ->
                        String.format(
                                "Обрабатываем событие в %s на потоке %s",
                                event.place(), Thread.currentThread().getName()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((key, value) -> System.out.println(key + ": " + value + " событий"));
    }

    @SneakyThrows
    private static String getEvents(int page) {
        @SuppressWarnings("deprecation")
        var eventsApi = new URL(
                "https://kudago.com/public-api/v1.2/events/?" +
                        "page_size=" + DEFAULT_PAGE_SIZE + "&page=" + page +
                        "&fields=id,title,favorites_count,comments_count&location=spb");

        var connection = (HttpURLConnection) eventsApi.openConnection();
        try (var in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            var response = new StringBuilder();

            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

}
