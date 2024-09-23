package ru.tbank.edu.fintech.lecture8;

import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

import java.util.logging.Level;


@Log
@UtilityClass
public class LoggingUtils {

    public static void info(String message, Object... args) {
        log.log(Level.INFO, message, args);
    }

    public static void error(String message, Object... args) {
        log.log(Level.SEVERE, message, args);
    }

    public static void trace(String message, Object... args) {
        log.log(Level.FINE, message, args);
    }

}
