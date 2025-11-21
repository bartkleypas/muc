package org.kleypas.muc.cli

/**
 * Defines the available logging levels.
 * Log messages with a level less than the configured threshold will be suppressed.
 */
public enum LogLevel {
    /** Detailed diagnostic information, usually only of interest to a developer. */
    TRACE(1),
    /** Fine-grained informational events, usually most useful in debugging an application. */
    DEBUG(2),
    /** Standard application lifecycle events. The default reporting level. */
    INFO(3),
    /** Events that might signify a potential problem or are unexpected. */
    WARN(4),
    /** Serious errors that might disrupt the application's expected behavior. */
    ERROR(5),
    /** Critical events that cause immediate program termination or unrecoverable state. */
    FATAL(6),
    /** Turns logging off. */
    OFF(10)

    private final int levelValue

    /**
     * Private constructor to associate a numeric value with each level.
     * This allows for easy comparison logic in the Logger class.
     * @param value The integer representation of the level.
     */
    private LogLevel(int value) {
        this.levelValue = value
    }

    /**
     * Retrieves the numeric value of this logging level.
     * @return The numeric value.
     */
    public int getLevelValue() {
        return this.levelValue
    }
}