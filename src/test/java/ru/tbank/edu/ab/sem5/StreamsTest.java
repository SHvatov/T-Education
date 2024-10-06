package ru.tbank.edu.ab.sem5;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
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

        findUpcomingEvents(events, 7);
        findTopThreeEvents(events);
        scheduleTopEventsForBudget(events, BigDecimal.valueOf(2_000));
        findEventsDistributionByDate(events);
        findEventsDistributionByPlace(events);
    }

    private void findTopThreeEvents(List<EnrichedEvent> events) {
        // todo
        System.out.println("Топ 3 события: \n" + events.stream()
                .sorted(Comparator.comparingInt(EnrichedEvent::favorites).reversed())
                .limit(3)
                .toList()
        );
    }

    private void scheduleTopEventsForBudget(List<EnrichedEvent> events, BigDecimal budget) {
        // todo
        System.out.println("Топ лучших мероприятий с бюджетом " + budget + ": \n" +
                events.stream().filter(event -> event.price <= budget.intValue())
                        .sorted(Comparator.comparingInt(EnrichedEvent::favorites).reversed()).toList()
        );
        /**
         * если честно даже после того как вы оставили комментарий
         * я не понимаю что должен делать этот метод)
         * как мне показалось вы хотите чтобы метод находил список событий
         * с самым большим количеством дозволимым на этот бюджет
         * тут или эврестический алгос или динамическое программирование
         * ни то ни то я не представляю как решать это в стримах
         */
    }

    private void findEventsDistributionByDate(List<EnrichedEvent> events) {
        // todo
        System.out.println("Группировка событий по датам:\n" + events.stream().collect(
                Collectors.groupingBy(EnrichedEvent::date, Collectors.counting())
        ));
    }

    private void findEventsDistributionByPlace(List<EnrichedEvent> events) {
        // todo
        System.out.println("Групировка событий по местам:\n" + events.stream().collect(
                Collectors.groupingBy(EnrichedEvent::place)
        ));
    }

    private void findUpcomingEvents(List<EnrichedEvent> events, int daysFromToday) {
        // todo
        var now = LocalDate.now().plusDays(daysFromToday);
        events.stream().filter(event -> checkDateInUpcomingData(event, now))
                .forEach(event -> System.out.println("Событие " + event + " произойдет в следущие " + daysFromToday + " дней"));
    }

    private boolean checkDateInUpcomingData(final EnrichedEvent event, final LocalDate date) {
        // проверка что событие произойдет в промежутке до переданной date
        return event.date.isEqual(date) ||
                event.date.isBefore(date);
    }

    @Test
    @DisplayName("Пример параллелизации Stream'а")
    void test1() {
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
