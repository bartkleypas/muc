
import org.kleypas.muc.character.Character
import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.CommandProcessor
import org.kleypas.muc.cli.TerminalBridge
import org.kleypas.muc.io.LogManager
import org.kleypas.muc.location.Location
import org.kleypas.muc.location.Poi
import org.kleypas.muc.model.Model
import org.kleypas.muc.model.ModelType
import org.kleypas.muc.model.Message
import org.kleypas.muc.model.Context
import org.kleypas.muc.model.resonance.Resonance

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
    Resonance vibe

    Character player
    Character partner
    Location location
    Poi poi

    Chat(def options) {
        this.options = options
    }

    /**
     * The main entry point for the Starship Majel bridge.
     */
    void run() {
        initializeResources()

        introductions()

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

        this.location = new Location()

        this.poi = new Poi(
            location: this.location,
            name: "Starship Majel"
        )

        // Load existing history or initialize system prompt
        if (new File(historyFile).exists()) {
            logManager.readAllEntries().each { context.addMessage(it) }
        } else {
            initializeNewChronicle(promptFile)
        }

        if (context.messages && !context.messages.isEmpty()) {
            Message lastMessage = context.messages.last()
            if (lastMessage.vibe) {
                this.vibe = lastMessage.vibe.clone()
            } else {
                this.vibe = new Resonance()
            }
        } else {
            this.vibe = new Resonance()
        }
    }

    private void introductions() {
        this.player = new Character(
            name: "Navigator",
            description: "A Seasoned Navigator",
            location: poi.location
        )
        this.poi.addOccupant(this.player)

        this.partner = new Character(
            name: "Majel",
            description: "The Starship Majal on-board digital assistant.",
            location: poi.location
        )
    }

    private void initializeNewChronicle(String path) {
        String promptText = new File(path).text
        // promptText = SystemPromptHelper.buildFullSystemPrompt(promptText)
        Message systemMsg = context.addMessage(
            role: "system",
            author: "Partner",
            content: promptText,
            vibe: new Resonance()
        )
        logManager.appendEntry(systemMsg)
    }

    private void startupSequence() {
        bridge.drawSignature(logManager.getChronicleStats())

        Message last = context.messages.last()
        if (last.role == "system") {
            bridge.printToken("Welcome to ${poi.name}. I am its AI assistant. Welcome to the crew.")
        } else {
            bridge.replayLastTurn(context)
        }
        bridge.flushBuffer()
    }

    private void executionLoop() {
        def reader = LineReaderBuilder.builder().terminal(bridge.terminal).build()
        while (true) {
            bridge.updateHUD(poi.name, player.name, this.vibe.asMap())
            String input = reader.readLine("\u001B[1;32m${player.name}\u001B[0m: ")
            if (!input || input == "/bye" || input == "q") break
            if (processor.process(input)) {
                if (processor.requestRefresh) {
                    this.vibe = processor.vibe
                    this.context = processor.context
                }
                continue
            }

            Message userMsg = processUserTurn(input)
            context.addMessage(userMsg)
            logManager.appendEntry(userMsg)

            Message assistantMsg = processModelTurn(userMsg)
            context.addMessage(assistantMsg)
            logManager.appendEntry(assistantMsg)
            bridge.flushBuffer()
        }
    }

    private Message processUserTurn(String input) {
        Message last = context.messages.last()

        Message mine = new Message(
            role: "user",
            author: player.name,
            content: input,
            parentId: last.messageId,
            vibe: this.vibe
        )
        bridge.printSpeaker(mine) // needs a message object
        bridge.terminal.writer().print("${input}\n")
        return mine
    }

    private Message processModelTurn(Message userMsg) {
        String faderPrefix = userMsg.vibe.toPrefix()
        bridge.terminal.writer().print("\u001B[1;36m${partner.name}\u001B[0m: ")

        StringBuilder fullOutput = new StringBuilder()
        model.streamResponseWithPrefix(context, faderPrefix) { token ->
            bridge.printToken(token)
            fullOutput.append(token)
        }

        String text = fullOutput.toString().trim()

        // OK! Now we can pick up the vibes off of the model with ResonanceEngine!
        // def deltas = ResonanceEngine.calculate(text)
        // this.vibe = usrMsg.vibe + deltas
        Message assistantMsg = context.addMessage(
            role: "assistant",
            author: partner.name,
            content: text,
            parentId: userMsg.messageId,
            vibe: userMsg.vibe // Will be updated once it is fixed above.
        )

        return assistantMsg
    }
}