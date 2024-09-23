package ru.tbank.edu.fintech.lecture8.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.error;
import static ru.tbank.edu.fintech.lecture8.utils.LoggingUtils.trace;


@UtilityClass
public class ThreadUtils {

    @SneakyThrows
    public static void sleep(long ms) {
        trace("Stopping thread {0} for {1} ms", getCurrentThreadName(), ms);
        Thread.sleep(ms);
    }

    @SneakyThrows
    public static void withThreadInterruptionHandled(InterruptibleRunnable runnable) {
        withThreadInterruptionHandled(runnable, () -> {
            // do nothing
        });
    }

    @SneakyThrows
    public static void withThreadInterruptionHandled(InterruptibleRunnable runnable, Runnable onThreadInterruption) {
        try {
            runnable.run();
        } catch (InterruptedException exception) {
            error("Thread {0} has been interrupted, calling corresponding handler", getCurrentThreadName(), exception);
            onThreadInterruption.run();
            Thread.currentThread().interrupt();
        }
    }

    public static String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * @see Runnable
     */
    public interface InterruptibleRunnable {

        void run() throws Exception;

    }

}
