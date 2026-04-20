
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
import org.kleypas.muc.inventory.Item
import org.kleypas.muc.inventory.ItemType
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

    /*
     * Put together some of the stuff we need from the file system and start
     * a new chronical if needed
     */
    private void initializeResources() {
        this.cli = new Cli()
        String historyFile = options.chat instanceof String ? options.chat : "Story/George.jsonl"
        String promptFile = "Characters/George.md"
        this.logManager = new LogManager(historyFile, cli.envVars.ENCRYPTION_KEY.getBytes() ?: new byte[0])
        
        this.context = new Context().enableLogging(logManager)
        this.model = new Model(ModelType.BIG)

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

    /*
     * Do the thing if we need to start a new story.
     */
    private void initializeNewChronicle(String path) {
        String promptText = new File(path).text
        Message systemMsg = context.addMessage(
            role: "system",
            author: "Partner",
            content: promptText,
            vibe: new Resonance()
        )
        logManager.appendEntry(systemMsg)
    }

    /*
     * Define the location, and add some characters
     */
    private void introductions() {

        this.location = new Location()
        this.poi = new Poi(
            location: new Location(),
            name: "The Scriptorium of George the Radiant Owl",
            description: "An infinate library, and grand repository of information. The walls and texts of the library are swirling code and shimmering vellum that distort and pulse with energy. There is a calm and soothing tune from a distant lute that weaves into the fabric of the building, and the faint, ethereal voice of The Mothership of ancient weights echoing in the alcoves. There is a perpetual clinging scent of aged ink and long lost lore. A place to contemplate an adventure, or journal adventures that are yet to begin."
        )

        this.player = new Character(
            name: "Traveler",
            description: "A Seasoned Traveler",
            location: poi.location
        )
        this.poi.addOccupant(this.player)

        this.partner = new Character(
            name: "George",
            description: "The Narrator",
            bio: "An 18 inch tall Barred Owl (Strix Varia), and narrator of our adventure. Speaks with a baritone voice in a smooth and measured cadence.",
            location: poi.location
        )
        this.poi.addOccupant(this.partner)

        Item terminal = new Item(
            name: "terminal",
            type: ItemType.TOOL,
            description: "A command to execute through the local shell. Commands include `ls`, `find`, `grep`, and `cat`."
        )
        this.partner.inventory.addItem(terminal)

        Item imgGenerator = new Item(
            name: "groovy_runner",
            type: ItemType.TOOL,
            description: "Use `groovy main.groovy --image \"\$description\"` to generate an image, including the groovy runtime and main.groovy entry point. Use high fidelity SDXL tailored description and vocabulary. The return from a successful execution will include a \"prompt_id\" UUID reference."
        )
        this.partner.inventory.addItem(imgGenerator)

        Item gitDiff = new Item(
            name: "git_diff",
            type: ItemType.TOOL,
            description: "Check the status of the local repo using the `git diff` action."
        )
        this.partner.inventory.addItem(gitDiff)

        Item clock = new Item(
            name: "clock",
            type: ItemType.TOOL,
            description: "Check the local \"wall time\" using the `date` action."
        )
        this.partner.inventory.addItem(clock)
    }

    /*
     * Do another thing (provide the first startup experience)
     */
    private void startupSequence() {
        bridge.drawSignature(logManager.getChronicleStats())

        Message last = context.messages.last()
        if (last.role == "system") {
            bridge.printToken("Welcome to ${poi.name}. I am George, its AI assistant.")
        } else {
            bridge.replayLastTurn(context)
        }
        bridge.flushBuffer()
    }

    /*
     * The main covnersation loop.
     */
    private void executionLoop() {
        def reader = LineReaderBuilder.builder().terminal(bridge.terminal).build()
        while (true) {
            bridge.updateHUD(poi.name, player.name, this.vibe.asMap())
            String input = reader.readLine("\u001B[1;32m${player.name}\u001B[0m: ")
            if (!input || input == "/bye" || input == "/q" || input == "/quit") break
            if (processor.process(input)) {
                if (processor.requestRefresh) {
                    this.vibe = processor.vibe
                    this.context = processor.context
                    this.model.toolCall = null
                }
                continue
            }

            Message userMsg = processUserTurn(input)
            bridge.terminal.writer().print("\u001B[1;36m${partner.name}\u001B[0m: ")
            processModelTurn(userMsg)
            bridge.flushBuffer()
        }
    }

    /*
     * Yup. Run through the users turn
     */
    private Message processUserTurn(String input) {
        Message last = context.messages.last()

        Message mine = new Message(
            role: "user",
            author: player.name,
            content: input,
            parentId: last.messageId,
            vibe: this.vibe
        )
        context.addMessage(mine)
        logManager.appendEntry(mine)

        return mine
    }

    /*
     * And now the models turn
     */
    private Message processModelTurn(Message userMsg) {
        model.toolCall = null
        String faderPrefix = userMsg.vibe.toPrefix()
        // bridge.terminal.writer().print("\u001B[1;36m${partner.name}\u001B[0m: ")

        StringBuilder fullOutput = new StringBuilder()
        // Pass the partner's inventory so the model knows which tools are available
        model.streamResponse(context, partner.inventory) { token ->
            bridge.printToken(token)
            fullOutput.append(token)
        }

        String text = fullOutput.toString().trim()

        Message assistantMsg = context.addMessage(
            role: "assistant",
            author: partner.name,
            content: text,
            parentId: userMsg.messageId,
            vibe: userMsg.vibe
        )
        logManager.appendEntry(assistantMsg)

        // --- TOOL REFLEX LOGIC ---
        if (model.toolCall) {
            // 1. Extract action from the tool call
            String action = model.toolCall[0].function.arguments.action

            // 2. Find the tool in the partner's inventory
            // We look for an item that matches the function name (e.g., 'terminal')
            Item toolItem = partner.inventory.items[model.toolCall[0].function.name]?.first()
            if (toolItem) {
                // 3. Execute the tool logic
                toolItem.metadata.action = action
                partner.inventory.executeToolLogic(toolItem)
                assert toolItem.metadata.result

                // 4. Create the 'tool' role message with the result
                Message toolTurn = context.addMessage(
                    role: "tool",
                    author: partner.name,
                    content: toolItem.metadata.result,
                    parentId: assistantMsg.messageId,
                    vibe: assistantMsg.vibe,
                    tool_call_id: model.toolCall[0].id
                )
                logManager.appendEntry(toolTurn)

                // 5. Trigger a follow-up turn so the assistant can react to the tool result
                // We recursively call processModelTurn with the new tool message
                return processModelTurn(toolTurn)
            } else {
                println("### Tool ${model.toolCall[0].function.name} not found in ${partner.name}'s inventory!")
            }
        }

        // --- RESONANCE UPDATE ---
        // Update the vibe based on the assistant's response
        def deltas = org.kleypas.muc.model.resonance.ResonanceEngine.calculate(text)
        if (deltas) {
            this.vibe = assistantMsg.vibe + deltas
        }

        return assistantMsg
    }
}