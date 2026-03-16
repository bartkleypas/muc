
import org.kleypas.muc.cli.*
import org.kleypas.muc.io.*
import org.kleypas.muc.model.*
import org.kleypas.muc.model.resonance.*
import org.kleypas.muc.util.*

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
    String location
    Resonance vibe

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
        String promptFile = "Characters/Majel.md"
        this.logManager = new LogManager(historyFile, cli.envVars.ENCRYPTION_KEY.getBytes() ?: new byte[0])
        this.context = new Context().enableLogging(logManager)
        this.model = new Model(ModelType.BIG)
        this.location = "The Starship Majel"

        // Load existing history or initialize system prompt
        if (new File(historyFile).exists()) {
            logManager.readAllEntries().each { context.messages.add(it) }
        } else {
            initializeNewChronicle(promptFile)
        }
    }

    private void initializeNewChronicle(String path) {
        String promptText = new File(path).text
        Message systemMsg = context.addMessage(
            role: "system",
            author: "Majel",
            content: promptText,
            vibe: new Resonance() // Starts us off whith the global vibes.
        )
        logManager.appendEntry(systemMsg)
    }

    private void startupSequence() {
        bridge.drawSignature(logManager.getChronicleStats())

        Message last = context.messages.last()
        if (last.role == "system") {
            bridge.printToken("Welcome to ${location}. I am its AI assistant. Welcome to the crew.")
        } else {
            bridge.replayLastTurn(context)
        }
        bridge.flushBuffer()
    }

    private void executionLoop() {
        def reader = LineReaderBuilder.builder().terminal(bridge.terminal).build()

        while (true) {
            Message last = context.messages.last()
            bridge.updateHUD(location, last)
            bridge.flushBuffer()

            String input = reader.readLine("\u001B[1;32mNavigator\u001B[0m: ")?.trim()

            if (!input || input == "/bye" || input == "q") break
            if (processor.process(input)) {
                if (processor.requestRefresh) {
                    this.vibe = processor.vibe
                    bridge.updateHUD(location, last)
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
            vibe: this.vibe
        )
        logManager.appendEntry(userMsg)

        bridge.printSpeaker(userMsg)
        bridge.terminal.writer().print("${input}\n")

        def virtualContext = new Context(context.messages)
        String faderPrefix = userMsg.vibe.toPrefix()
        bridge.printSpeaker(last)

        StringBuilder fullOutput = new StringBuilder()
        model.streamResponseWithPrefix(virtualContext, faderPrefix) { token ->
            bridge.printToken(token)
            fullOutput.append(token)
        }

        String text = fullOutput.toString().trim()

        // OK! Now we can pick up the vibes off of the model with ResonanceEngine!
        // def deltas = ResonanceEngine.calculate(text)
        // this.vibe = usrMsg.vibe + deltas
        Message assistantMsg = context.addMessage(
            role: "assistant",
            author: last.author,
            content: text,
            parentId: userMsg.messageId,
            vibe: userMsg.vibe.clone() // Will be updated once it is fixed above.
        )

        logManager.appendEntry(assistantMsg)
    }
}