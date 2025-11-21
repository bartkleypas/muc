package org.kleypas.muc.cli

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A simple, structured logging utility class that respects a minimum logging level.
 * Adheres to Style B requirements for maintainability.
 */
public class Logger {

    // Default to INFO level if not otherwise configured
    private static LogLevel currentLevel = LogLevel.INFO
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss.SSS')

    /**
     * Sets the minimum logging level for the application.
     * Messages below this level will be suppressed.
     * @param newLevel The new minimum LogLevel.
     */
    public static void setLevel(LogLevel newLevel) {
        if (newLevel != null) {
            currentLevel = newLevel
        }
    }

    /**
     * Formats and prints a log message if its level is sufficient.
     * @param level The LogLevel of the message being logged.
     * @param message The main message string.
     * @param optionalThrowable An optional Throwable object to include stack trace information.
     */
    private static void log(LogLevel level, String message, Throwable optionalThrowable) {
        if (level.getLevelValue() >= currentLevel.getLevelValue()) {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER)
            String output = "[${timestamp}] [${level.name()}] ${message}"

            // Standard output choice based on severity
            if (level.getLevelValue() >= LogLevel.WARN.getLevelValue()) {
                System.err.println(output)
                if (optionalThrowable != null) {
                    optionalThrowable.printStackTrace(System.err)
                }
            } else {
                System.out.println(output)
            }
        }
    }

    // --- Public Logging Methods (Convenience Wrappers) ---

    public static void trace(String message) {
        log(LogLevel.TRACE, message, null)
    }

    public static void debug(String message) {
        log(LogLevel.DEBUG, message, null)
    }

    public static void info(String message) {
        log(LogLevel.INFO, message, null)
    }

    public static void warn(String message) {
        log(LogLevel.WARN, message, null)
    }

    public static void error(String message) {
        log(LogLevel.ERROR, message, null)
    }

    public static void error(String message, Throwable t) {
        log(LogLevel.ERROR, message, t)
    }

    public static void fatal(String message) {
        log(LogLevel.FATAL, message, null)
    }

    public static void fatal(String message, Throwable t) {
        log(LogLevel.FATAL, message, t)
    }
}