package org.kleypas.muc.cli

import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor
import java.util.Properties
import java.io.File
import java.util.Scanner

/**
 * A utility class for parsing command-line arguments, loading environment variables,
 * and managing external process execution.
 * Adheres to Style B requirements: strong typing, explicit definitions, and clear structure.
 */
public class Cli {

    private static final Set<String> SECRET_KEYS = [
        "OLLAM_API_KEY",
        "API_KEY",
        "SECRET",
        "PASSWORD",
        "AUTH_TOKEN",
        "BEARER",
        "ENCRYPTION_KEY",
        "CLIENT_SECRET"
    ].asImmutable()

    private final CliBuilder cliBuilder
    private OptionAccessor options
    private LogLevel configuredLogLevel = LogLevel.INFO

    private final File envFile = new File("Secrets/.env")
    private Map<String, String> envVars = [
        'SHELL': 'zsh',
    ]

    /**
     * Constructs a new Cli instance and initializes CLI options.
     */
    public Cli() {
        this.cliBuilder = new CliBuilder(
            usage: 'groovy main.groovy [options]'
        )

        if (this.envFile.exists()) {
            this.envVars.putAll(loadDotEnv(this.envFile))
        }

        this.cliBuilder.with {
            h(longOpt: 'help', 'Display this help message')
            b(longOpt: 'build', 'Build the project (A non-mandate mock)')
            t(longOpt: 'test', 'Run integration tests')
            j(longOpt: 'json', 'Enable JSON output format')
            i(longOpt: 'image', 'Activate image generation mode')
            c(longOpt: 'chat', argName: 'str', args: 1, optionalArg: true, 'Start a direct chat interface')
            v(longOpt: 'verbose', 'Enable verbose (INFO) logging')
            d(longOpt: 'debug', 'Enable debug logging')
            debate(longOpt: "debate", "Enable debate mode (Let it cook)")
        }
    }

    /**
     * Loads environment variables from a .env file.
     * @param path The File object pointing to the .env file.
     * @return A map of String keys to String values containing the loaded variables.
     */
    public Map<String, String> loadDotEnv(File path) {
        assert path.exists() : "The .env file path ${path.absolutePath} does not exist."

        Properties props = new Properties()
        path.withInputStream { props.load(it) }

        Map<String, String> envMap = [:]
        props.each { key, value ->
            envMap.put(key.toString(), value.toString())
        }

        logSanitizedEnvVars(envMap)

        return envMap
    }

    /**
     * Creates a copy of the environment map with sensitive values redacted for logging.
     * @param originalMap The map of environment variables loaded from the .env file.
     */
    private void logSanitizedEnvVars(Map<String, String> originalMap) {
        if (Logger.currentLevel.getLevelValue() <= LogLevel.DEBUG.getLevelValue()) {
            Map<String, String> sanitizedMap = [:]

            originalMap.each { key, value ->
                // Check if the key contains any of the identifiers in SECRET_KEYS (case-insensitive)
                def isSecret = SECRET_KEYS.any { secretPart ->
                    key.toUpperCase().contains(secretPart.toUpperCase())
                }

                if (isSecret) {
                    // Redact the value for logging
                    sanitizedMap.put(key, "*** REDACTED ***")
                } else {
                    sanitizedMap.put(key, value)
                }
            }

            Logger.debug("Loaded Environment Variables: ${sanitizedMap}")
        }
    }

    /**
     * Parses the provided command-line arguments using the defined CLI options.
     * Sets the global Logger level based on -v or -d flags if present.
     *
     * @param args The list of command-line arguments.
     * @return The parsed OptionAccessor object.
     */
    public OptionAccessor parse(String[] args) {
        this.options = cliBuilder.parse(args)

        if (!this.options) {
            Logger.error '\n!!! Error while parsing command-line arguments. !!!'
            cliBuilder.usage()
            System.exit(1)
        }

        if (this.options.debug) {
            this.configuredLogLevel = LogLevel.DEBUG
        } else if (this.options.verbose) {
            this.configuredLogLevel = LogLevel.INFO
        } else {
            this.configuredLogLevel = LogLevel.WARN
        }

        Logger.setLevel(this.configuredLogLevel)

        if (this.options.help) {
            cliBuilder.usage()
            System.exit(0)
        }

        Logger.info("CLI parsed successfully. Log level set to: ${this.configuredLogLevel.name()}")

        return this.options
    }

    /**
     * Executes an external shell command and waits for it to finish.
     * This method redirects the command's standard output and error to the Groovy console.
     *
     * @param command The shell command string to execute (e.g., "git status").
     * @return The exit code of the executed process (0 indicates success).
     */
    public int runCommand(String command) { // Strong typing enforced (Style B)
        try {
            Logger.debug("Executing command: ${command}")

            // Groovy's "execute()" method is the idiomatic, powerful way to run external processes
            Process process = command.execute()

            // Wait for the process to finish and handle output/error streams concurrently
            process.waitForProcessOutput(System.out, System.err)
            int exitValue = process.exitValue()

            if (exitValue != 0) {
                Logger.error("Command failed with exit code: ${exitValue}. Command: ${command}")
            } else {
                Logger.debug("Command completed successfully. Exit code: ${exitValue}")
            }
            return exitValue
        } catch (Exception e) {
            Logger.fatal("Failed to execute command: ${command}", e)
            return -1 // Indicate failure due to execution exception
        }
    }

    /**
     * Above methods big brother.
     *
     * @param command The shell command string to execute.
     * @return A Map containing 'exitCode', 'standardOutput', and 'errorOutput'
     */
    public Map runCommandWithOutput(String command) {
        Logger.debug("Executing command: ${command}")

        def process = command.execute()
        process.waitFor()

        // Capture output streams
        String standardOutput = process.in.text.trim()
        String errorOutput = process.err.text.trim()

        def exitCode = process.exitValue()

        if (exitCode != 0) {
            Logger.error("Command FAILED (Exit Code: ${exitCode}). Error: ${errorOutput}")
        } else {
            Logger.debug("Command SUCCESS. Output length: ${standardOutput.length()} bytes")
        }

        return [
            exitCode: exitCode,
            standardOutput: standardOutput,
            errorOutput: errorOutput
        ]
    }

    /**
     * Retrieves a loaded environment variable value.
     * @param key The environment variable key.
     * @return The environment variable value as a String, or null if not found.
     */
    public String getEnv(String key) {
        return this.envVars.get(key)
    }

    /**
     * Retrieves the parsed options object.
     * @return The OptionAccessor containing the parsed CLI arguments.
     */
    public OptionAccessor getOptions() {
        return this.options
    }
}