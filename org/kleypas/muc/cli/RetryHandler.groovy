package org.kleypas.muc.cli

import groovy.transform.TypeChecked
import java.util.concurrent.TimeUnit

/**
 * Utility class to execute a closure with configurable retry logic and exponential backoff.
 * Adheres to Style B: strong typing, explicit definitions, and structured error handling.
 */
@TypeChecked
public class RetryHandler {

    private final int maxRetries
    private final long initialDelayMs

    // Explicit access modifier and type for constructor
    public RetryHandler(int maxRetries, long initialDelayMs) {
        // Assertions are good for Style B to validate inputs immediately
        assert maxRetries >= 0 : "Max retries must be non-negative."
        assert initialDelayMs >= 0 : "Initial delay must be non-negative."

        this.maxRetries = maxRetries
        this.initialDelayMs = initialDelayMs
    }

    /**
     * Executes an action closure, retrying on failure up to the maximum number of times.
     * Uses exponential backoff for delays.
     *
     * @param action The closure to execute. Must return a boolean: true for success, false for retryable failure.
     * @return true if the action succeeded within the max retries, false otherwise.
     */
    public boolean execute(Closure<Boolean> action) {
        int attempt = 0
        long currentDelay = this.initialDelayMs

        // Strong typing for the loop control variable 'attempt'
        while (attempt <= this.maxRetries) {
            
            try {
                Logger.debug("Attempt ${attempt + 1} of ${this.maxRetries + 1}...")

                // Execute the action. We use call() for explicit closure invocation (Style B).
                if (action.call()) {
                    Logger.debug("Action succeeded on attempt ${attempt + 1}.")
                    return true
                }
                
                // If action returns false, we proceed to retry
                Logger.warn("Action returned failure (retryable).")

            } catch (Exception e) {
                // Any exception is caught and logged, treating it as a failure requiring retry
                Logger.warn("Action failed with exception: ${e.getMessage()}. Retrying...")
            }
            
            // Check if we have any retries left before delaying
            if (attempt < this.maxRetries) {
                // Apply exponential backoff: delay * 2^attempt
                long delay = (long) (currentDelay * Math.pow(2, attempt))
                Logger.info("Delaying for ${delay}ms before next attempt.")
                
                // Groovy/Java standard method for waiting (explicit type conversion for clarity)
                TimeUnit.MILLISECONDS.sleep(delay)
            }

            attempt++
        }

        // If loop finishes without returning true, all attempts failed
        Logger.error("Action failed after ${this.maxRetries + 1} attempts.")
        return false
    }
}