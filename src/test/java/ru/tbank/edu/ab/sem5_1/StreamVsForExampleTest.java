package ru.tbank.edu.ab.sem5_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tbank.edu.utils.events.EventUtils;

import java.util.ArrayList;


class StreamVsForExampleTest {

    @Test
    @DisplayName("Stream vs Loop")
    void test() {
        final var desiredEventsCount = 30;
        var events1 = EventUtils.getEvents()
                .limit(desiredEventsCount)
                .toList();

        // Non-optimal option
        var page = 1;
        var temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            temp.addAll(EventUtils.getEventsPage(page++));
        }

        var events2 = new ArrayList<>();
        for (int i = 0; i < desiredEventsCount; i++) {
            events2.add(temp.get(i));
        }

        // Optimal option
        page = 1;
        var events3 = new ArrayList<>();
        while (true) {
            var eventsPage = EventUtils.getEventsPage(page++);
            if (eventsPage.isEmpty()) {
                break;
            }

            for (var event : eventsPage) {
                events3.add(event);
                if (events3.size() == desiredEventsCount) {
                    break;
                }
            }

            if (events3.size() == desiredEventsCount) {
                break;
            }
        }
    }

}
