/**
 * Cli.groovy
 *
 * A utility class for parsing command-line arguments, loading environment variables, and running shell commands.
 *
 */

import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor

enum LogLevel {
    INFO,
    WARN,
    DEBUG,
    JSON
}

class Cli {

    CliBuilder cliBuilder
    OptionAccessor options
    LogLevel logLevel
    Logger logger

    /**
     * The .env file containing environment variables.
     */
    File envFile = new File("Secrets/.env")
    Map envVars = [
        'SHELL': 'zsh',
    ]

    /**
     * Constructs a new Cli instance, initializing CLI options and loading environment variables.
     */
    Cli() {
        this.logLevel = LogLevel.INFO
        this.logger = new Logger()
        cliBuilder = new CliBuilder(
            usage: 'groovy main [args]'
        )

        if (envFile.exists()) {
            envVars.putAll(loadDotEnv(envFile))
        }

        cliBuilder.with {
            h(longOpt: 'help', 'Try to help')
            t(longOpt: 'test', 'Run tests')
            j(longOpt: 'json', 'json output')
            i(longOpt: 'image', 'generate an image')
        }
    }

    /**
     * Loads environment variables from a .env file.
     *
     * @param path the File object pointing to the .env file
     * @return a Properties object containing the loaded variables
     */
    public loadDotEnv(File path) {
        assert path.exists()
        Properties props = new Properties()
        path.withInputStream { props.load(it) }
        return props
    }

    /**
     * Parses the provided command-line arguments using the defined CLI options.
     *
     * @param args the list of command-line arguments
     * @return the parsed OptionAccessor object
     */
    public parse(args) {
        options = cliBuilder.parse(args)
        if (!options) {
            println '\n!!! Error while parsing command-line arguments. !!!\n'
            cliBuilder.usage()
            System.exit(1)
        }
        if (options.help) {
            cliBuilder.usage()
            System.exit(0)
        }
        return options
    }

    String log(String logLine) {
        println logLine
    }
}

class Logger {

}

class RetryHandler {
    private int maxAttempts
    private int delay
    private int tryNumber

    public RetryHandler(int maxAttempts, int delay) {
        this.maxAttempts = maxAttempts
        this.delay = delay // in seconds
        this.tryNumber = 0
    }

    /**
     * Attempts to execute a command with linear backoff and logs the output.
     *
     * @param command the command to attempt
     */
    public Runnable retryCommand(Runnable command) {
        if (maxAttempts < 1) {
            throw new RuntimeException("Failed with ${maxAttempts} attempts left")
        }

        println("Try number: ${tryNumber + 1}")
        println("Attempts left: ${maxAttempts}")
        int timeOut = (delay * tryNumber) * 1000 // in milliseconds
        println("Delay: ${timeOut}ms")

        try {
            sleep(timeOut)

            command.run()

        } catch (Exception e) {
            tryNumber++
            maxAttempts--
            println(e)

            return retryCommand(command)
        }
    }
}