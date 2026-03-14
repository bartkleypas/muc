
import org.kleypas.muc.io.LogManager

import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.TerminalBridge
import org.kleypas.muc.cli.CommandProcessor

import org.kleypas.muc.model.Context
import org.kleypas.muc.model.Model
import org.kleypas.muc.model.Message

import org.jline.reader.LineReaderBuilder
import org.jline.reader.LineReader

class Chat {
    def options
    Cli cli
    LogManager logManager
    Context context
    Model model
    TerminalBridge bridge
    CommandProcessor processor

    Chat(def options) {
        this.options = options
    }

    /**
     * The main entry point for the Starship Majel bridge.
     */
    void run() {
        initializeResources()

        new TerminalBridge().withCloseable { b ->
            this.bridge = b
            this.processor = new CommandProcessor(bridge, logManager, context)

            startupSequence()
            executionLoop()
        }
    }

    private void initializeResources() {
        this.cli = new Cli()
        String historyFile = options.chat instanceof String ? options.chat : "Story/Majel.jsonl"
        // Cli has a way to get the encryption key from the environment.
        this.logManager = new LogManager(historyFile, cli.envVars.ENCRYPTION_KEY.getBytes() ?: new byte[0])
        this.context = new Context().enableLogging(logManager)
        this.model = new Model(model: "biggun")

        // Load existing history or initialize system prompt
        if (new File(historyFile).exists()) {
            logManager.readAllEntries().each { context.messages.add(it) }
        } else {
            initializeNewChronicle(historyFile)
        }
    }

    private void initializeNewChronicle(String path) {
        String promptText = new File("Characters/Majel.md").text
        Message systemMsg = context.addMessage(
            role: "system",
            author: "Majel",
            content: promptText
        )
        logManager.appendEntry(systemMsg)
    }

    private void startupSequence() {
        bridge.drawSignature(logManager.getChronicleStats())

        Message last = context.messages.last()
        if (last.role == "system") {
            bridge.printToken("Welcome to the Starship Majel. I am its AI assistant. Welcome to the crew.")
        } else {
            bridge.replayLastTurn(context)
        }
        bridge.flushBuffer()
    }

    private void executionLoop() {
        def reader = LineReaderBuilder.builder().terminal(bridge.terminal).build()

        while (true) {
            Message last = context.messages.last()
            bridge.updateHUD("The Starship Majel", "Navigator", last.getStats())
            bridge.flushBuffer()

            String input = reader.readLine("\u001B[1;32mNavigator\u001B[0m: ")?.trim()

            if (!input || input == "/bye" || input == "q") break
            if (processor.process(input)) {
                if (processor.requestRefresh) {
                    bridge.drawSignature(logManager.getChronicleStats())
                    bridge.updateHUD("The Starship Majel", "Navigator", processor.getStats())
                }
                continue
            }

            processUserTurn(input)
        }
    }

    private void processUserTurn(String input) {
        Message last = context.messages.last()

        Message userMsg = context.addMessage(
            role: "user",
            author: "Navigator",
            content: input,
            parentId: last.messageId,
            stats: processor.getStats()
        )
        logManager.appendEntry(userMsg)

        bridge.printSpeaker(userMsg)
        bridge.terminal.writer().print("${input}\n")

        def virtualContext = new Context(context.messages)
        def currentStats = userMsg.getStats()
        String faderPrefix = currentStats.collect { k, v -> "[${k.toUpperCase()}]" }.join(" ")
        bridge.printSpeaker(last)

        StringBuilder fullOutput = new StringBuilder()
        model.streamResponseWithPrefix(virtualContext, faderPrefix) { token ->
            bridge.printToken(token)
            fullOutput.append(token)
        }

        Message assistantMsg = context.addMessage(
            role: "assistant",
            author: last.author,
            content: fullOutput.toString().trim(),
            parentId: userMsg.messageId,
            stats: currentStats
        )
        logManager.appendEntry(assistantMsg)
    }
}