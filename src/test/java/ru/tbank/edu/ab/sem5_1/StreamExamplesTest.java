package ru.tbank.edu.ab.sem5_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.tbank.edu.fintech.lecture8.utils.LoggingUtils;
import ru.tbank.edu.utils.events.EnrichedEvent;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.tbank.edu.utils.events.EventUtils.getEvents;


class StreamExamplesTest {

    @Test
    @DisplayName("Примеры работы со Stream API")
    void test() {
        var events = getEvents(30);
        System.out.printf("Кол-во событий: %s%n", events.size());

        // 1. Stream - холодный до момента запуска
        var stream = events.stream().filter(it -> it.favorites() > 10);
        System.out.println(stream);
        System.out.println(stream.toList().size());

        // 2. Stream - одноразовый
        var ex = assertThrows(Exception.class, stream::toList);
        LoggingUtils.error("Получено исключение: {0}", ex);

        // 3. Примеры работы со стримами
        findTopThreeEvents(events);
        findUpcomingEvents(events, 7);
        findUpcomingEvents(events, 1);
        scheduleTopEventsForBudget(events, 2_000);
        scheduleTopEventsForBudget(events, 10_000);
        findEventsDistributionByDayOfTheWeek(events);
        findEventsDistributionByPlace(events);

        // 4. Ищем, куда сходить на следующей неделе
        var today = LocalDate.now();
        var nextWeekend = switch (today.getDayOfWeek()) {
            case SATURDAY -> Tuples.of(today, today.plusDays(1));
            case SUNDAY -> Tuples.of(today, today);
            default -> {
                var daysToSaturday = DayOfWeek.SATURDAY.getValue() - today.getDayOfWeek().getValue();
                yield Tuples.of(today.plusDays(daysToSaturday), today.plusDays(daysToSaturday + 1));
            }
        };
        System.out.println(nextWeekend);

        var eventsForNextWeekend = getEvents()
                .limit(300)
                .filter(isDateInPeriod(nextWeekend.getT1(), nextWeekend.getT2()))
                .sorted(
                        Comparator.comparingInt(EnrichedEvent::favorites).reversed()
                                .thenComparing(EnrichedEvent::price))
                .takeWhile(it -> it.favorites() > 100)
                .collect(new Collector<EnrichedEvent, HashMap<Tuple2<LocalDate, String>, EnrichedEvent>, String>() {
                    @Override
                    public Supplier<HashMap<Tuple2<LocalDate, String>, EnrichedEvent>> supplier() {
                        return HashMap::new;
                    }

                    @Override
                    public BiConsumer<HashMap<Tuple2<LocalDate, String>, EnrichedEvent>, EnrichedEvent> accumulator() {
                        return (acc, event) -> {
                            var key = Tuples.of(event.date(), event.place());

                            var prevEvent = acc.putIfAbsent(key, event);
                            if (prevEvent == null) {
                                return;
                            }

                            if (prevEvent.price() > event.price()
                                    && (prevEvent.favorites() - event.favorites() < 10)) {
                                acc.put(key, event);
                            }
                        };
                    }

                    @Override
                    public BinaryOperator<HashMap<Tuple2<LocalDate, String>, EnrichedEvent>> combiner() {
                        return (left, right) -> {
                            var result = new HashMap<Tuple2<LocalDate, String>, EnrichedEvent>();
                            left.forEach((key, leftValue) -> {
                                var rightValue = right.remove(key);
                                if (rightValue == null) {
                                    result.put(key, leftValue);
                                } else {
                                    var resultValue = leftValue.favorites() > rightValue.favorites()
                                            ? leftValue : rightValue;
                                    result.put(key, resultValue);
                                }
                            });
                            result.putAll(right);
                            return result;
                        };
                    }

                    @Override
                    public Function<HashMap<Tuple2<LocalDate, String>, EnrichedEvent>, String> finisher() {
                        return result -> result.entrySet().stream()
                                .sorted(Comparator.comparing(it -> it.getKey().getT1()))
                                .map(it ->
                                        String.format(
                                                "Мероприятие от %s в %s: %s",
                                                it.getKey().getT1(), it.getKey().getT2(), it.getValue()))
                                .collect(Collectors.joining("\n\t- ", "\n\t- ", ""));
                    }

                    @Override
                    public Set<Characteristics> characteristics() {
                        return Set.of();
                    }
                });
        System.out.println("На следующих выходных стоит посетить следующие мероприятия: " + eventsForNextWeekend);
        System.out.println("---");
    }

    private void findTopThreeEvents(List<EnrichedEvent> events) {
        var result = events.stream()
                .sorted(Comparator.comparingInt(EnrichedEvent::favorites).reversed())
                .limit(3)
                .toList();
        printResult(result);
    }

    private static void findUpcomingEvents(List<EnrichedEvent> events, int daysFromToday) {
        assert daysFromToday > 0;

        var today = LocalDate.now();
        var result = events.stream()
                .filter(it -> it.date() != null)
                .filter(isDateInPeriod(today, today.plusDays(daysFromToday)))
                .toList();
        printResult(result);
    }

    private static void scheduleTopEventsForBudget(List<EnrichedEvent> events, int budget) {
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
                budget, result.getT2().size(), result.getT1(), eventsDescription);
        System.out.println("---");
    }

    private static void findEventsDistributionByDayOfTheWeek(List<EnrichedEvent> events) {
        events.stream()
                .collect(Collectors.groupingBy(it -> it.date().getDayOfWeek(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(it -> System.out.printf("На %s приходится %d мероприятий%n", it.getKey(), it.getValue()));
        System.out.println("---");
    }

    private static void findEventsDistributionByPlace(List<EnrichedEvent> events) {
        events.stream()
                .collect(Collectors.groupingBy(EnrichedEvent::place, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(it ->
                        System.out.printf(
                                "В %s было и будет проведено %d мероприятий%n",
                                it.getKey(), it.getValue()));
        System.out.println("---");
    }

    private static Predicate<EnrichedEvent> isDateInPeriod(LocalDate dateFrom, LocalDate dateTo) {
        assert dateFrom.isBefore(dateTo) || dateFrom.isEqual(dateTo);

        return (event) ->
                (event.date().isAfter(dateFrom) || event.date().isEqual(dateFrom))
                        && (event.date().isBefore(dateTo) || event.date().isEqual(dateTo));
    }

    private static void printResult(Collection<EnrichedEvent> result) {
        var description = result.stream()
                .map(it ->
                        String.format(
                                "Мероприятие с ИД %s: дата - %s (%s), место - %s, "
                                        + "стоимость - %s, кол-во лайков - %s",
                                it.id(), it.date(), it.date().getDayOfWeek(),
                                it.place(), it.price(), it.favorites()))
                .collect(Collectors.joining(", \n\t- ", "\n\t- ", ""));
        System.out.println("Результат запроса: " + description);
        System.out.println("---");
    }

}
