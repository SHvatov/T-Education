package ru.tbank.edu.fintech.lecture8;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

import java.util.logging.Level;


@Log
@UtilityClass
public class ThreadUtils {

    @SneakyThrows
    public static void sleep(long ms) {
        log.log(Level.FINE, "Stopping thread {0} for {1} ms", new Object[] { getCurrentThreadName(), ms });
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
            log.log(
                    Level.SEVERE,
                    "Thread {0} has been interrupted, calling corresponding handler",
                    new Object[] { getCurrentThreadName(), exception });
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

        void run() throws InterruptedException;

    }

}
