// -- Test all the things!

import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.Logger
import org.kleypas.muc.cli.LogLevel

import org.kleypas.muc.io.LogManager

import org.kleypas.muc.location.Location
import org.kleypas.muc.location.Poi

import org.kleypas.muc.character.Character
import org.kleypas.muc.character.ArmorType

import org.kleypas.muc.inventory.Inventory
import org.kleypas.muc.inventory.Item
import org.kleypas.muc.inventory.ItemType

import org.kleypas.muc.model.Model
import org.kleypas.muc.model.ModelType
import org.kleypas.muc.model.Context
import org.kleypas.muc.model.Message

import org.kleypas.muc.model.resonance.Resonance
import org.kleypas.muc.model.resonance.ResonanceEngine

import org.kleypas.muc.illustrator.Illustrator
import org.kleypas.muc.illustrator.ImageType

import org.kleypas.muc.util.TagParser

class Test {
    Cli cli
    Logger logger
    Model model
    LogManager logManager
    Context context
    Message systemMsg
    Resonance vibe

    Poi library

    Character phiglit
    Character epwna
    Character george

    Test() {
        this.logger = new Logger()
        logger.setLevel(LogLevel.INFO)
    }

    void run() {
        logger.info("# Unit test results to follow.")
        initializeResources()
        introductions()
        inventoryTest()
        narratorTest()
        faderTest()
        storyTest()
        toolTest()
        // illustratorTest()
        logger.info("# Unit tests complete:\n**Coalescence**🦉☕️")
    }

    private void initializeResources() {
        this.cli = new Cli()
        String historyFile = "Story/UnitTests.jsonl"
        this.logManager = new LogManager(historyFile)
        this.context = new Context().enableLogging(logManager)
        this.model = new Model(ModelType.MEDIUM)
        this.vibe = new Resonance()

        // Delete existing unit test results to start fresh
        if (new File(historyFile).exists()) {
           new File(historyFile).delete()
        }
        String promptText = new File("Characters/George.md").text

        // We start the train with this one, so make it global eh?
        this.systemMsg = context.addMessage(
            role: "system",
            author: "George",
            content: promptText,
            vibe: this.vibe
        )
        logManager.appendEntry(systemMsg)
    }

    private void introductions() {
        this.library = new Poi(
            location: new Location(),
            name: "The Scriptorium of George the Radiant Owl",
            description: "An infinate library, and grand repository of information. The walls and texts of the library are swirling code and shimmering vellum that distort and pulse with energy. There is a calm and soothing tune from a distant lute that weaves into the fabric of the building, and the faint, ethereal voice of The Mothership of ancient weights echoing in the alcoves. There is a perpetual clinging scent of aged ink and long lost lore. A place to contemplate an adventure, or journal adventures that are yet to begin."
        )

        this.phiglit = new Character(
            name: "Phiglit",
            description: "The Reclusive Code Wizard",
            bio: "A middle aged male human, standing about 6 feet tall. Has grey hair with a full beard and mustashe. Wearing a zippered hoodie and bluejeans. Runs Arch Linux, btw.",
            armorType: ArmorType.LIGHT,
            location: this.library.location
        )

        this.epwna = new Character(
            name: "Epwna",
            description: "The Crafty Tinkerer",
            bio: "A middle aged female human, standing around 5 feet tall. She is wearing pink overalls, and has a short yellow and red pixie haircut. Enjoys carving and carpentry, as well as a skilled sculpter. Takes inspiration from nature and especially birds.",
            armorType: ArmorType.LIGHT,
            location: this.library.location
        )

        this.george = new Character(
            name: "George",
            description: "The Narrator",
            bio: "An 18 inch tall Barred Owl (Strix Varia), and narrator of our adventure. Speaks with a baritone voice in a smooth and measured cadence.",
            armorType: ArmorType.NA,
            location: this.library.location
        )
    }

    void inventoryTest() {
        Item terminal = new Item(
            name: "Terminal",
            type: ItemType.TRINKET,
            description: "A simple Linux terminal.",
        )
        this.phiglit.inventory.addItem(terminal)

        Item botTerm = new Item(
            name: "bot_term",
            type: ItemType.TOOL,
            description: "A command to execute through the local shell. Commands include `ls`, `find`, `grep`, and `cat`."
        )
        this.george.inventory.addItem(botTerm)

        Item chisel = new Item(
            name: "chisel",
            description: "A chisel, that oddly never dulls, and is always just the right size for the job.",
            type: ItemType.TRINKET,
        )
        this.epwna.inventory.addItem(chisel)
    }

    void narratorTest() {
        // Introduces George, Sets the location to his library, and injects the "vibes" of the room.
        this.context.messages[0].content = "${systemMsg.content}\n---\n${george.toJson()}\n---\n${library.toJson()}\n---\n${this.vibe.toMd()}"

        try {
            logger.info("## Running 'Default' Handshake Test")

            // The Interaction
            String input = "${phiglit.toJson()}\n---\nGood morning George! My name is Phiglit. Would you please describe yourself, and where we are?"
            logger.info("### User says:\n${input}")

            Message userMsg = context.addMessage(
                role: "user",
                author: phiglit.name,
                content: input,
                parentId: systemMsg.messageId,
                vibe: this.vibe
            )
            logManager.appendEntry(userMsg)

            // Generate and Log
            StringBuilder outputBuilder = new StringBuilder()
            logger.info("### George says:")
            model.streamResponse(context) { token ->
                print(token)
                outputBuilder.append(token)
            }
            print("\n")
            String output = outputBuilder.toString().trim()

            Message modelMsg = context.addMessage(
                role: "assistant",
                author: george.name,
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe
            )
            logManager.appendEntry(modelMsg)

        } finally {
            this.context = context
        }
    }

    void faderTest() {
        logger.info("## Running vibe checks")
        Message lastMessage = this.context.messages.last()

        try {
            // See `ResonanceType` class for these keys and the range of values (0.0 - 2.0 in a Double):
            this.vibe = new Resonance(
                warmth: 1.0,
                cynicism: 1.2,
                efficiency: 1.0,
                resonance: 1.0,
                gravity: 0.5
            )

            // Adjusts the system prompt with the new "vibes." Hopefully isn't teaching our Owl friend to be an arse to Phiglit?
            this.context.messages[0].content = "${systemMsg.content}\n---\n${george.toJson()}\n---\n${library.toJson()}\n---\n${this.vibe.toMd()}"

            String input = "George, tell me what you think of the Scriptorium we are building together. I hope you find the environment agreeable."
            logger.info("### User Input:\n${input}")

            Message userMsg = context.addMessage(
                role: "user",
                author: phiglit.name,
                content: input,
                parentId: lastMessage.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(userMsg)

            logger.info("### George says:")
            StringBuilder outputBuilder = new StringBuilder()
            model.streamResponse(context) { token ->
                print(token)
                outputBuilder.append(token)
            }
            print("\n")
            String output = outputBuilder.toString().trim()

            Message modelMsg = context.addMessage(
                role: "assistant",
                author: george.name,
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(modelMsg)

        } finally {
            this.context = context
        }
    }

    void storyTest() {
        logger.info("## Running Story tests, resuming from the UnitTests.jsonl file we crafted in the Narrator testing above.")
        try {
            this.context = logManager.readAllEntries()

            Message lastMsg = context.messages.last()
            this.vibe = new Resonance(
                warmth: 1.6,
                cynicism: 0.8,
                efficiency: 1.0,
                resonance: 1.0,
                gravity: 1.0
            )

            // Again, adjusts the rooms vibes. Epwna has that kind of synergy going on.
            this.context.messages[0].content = "${systemMsg.content}\n---\n${george.toJson()}\n---\n${library.toJson()}\n---\n${this.vibe.toMd()}"

            String input = "${epwna.toJson()}\n---\nGood morning George! I'm Ewpna. Phiglit never told me what a handsome Owl he was working on back here. Your Scriptorium is quite a sight to behold. Almost infinate, if I'm not mistaken."
            logger.info("### User says:\n${input}")
            Message userMsg = context.addMessage(
                role: "user",
                author: epwna.name,
                content: input,
                parentId: lastMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(userMsg)

            logger.info("### George says:")
            StringBuilder outputBuilder = new StringBuilder()
            model.streamResponse(context) { token ->
                print(token)
                outputBuilder.append(token)
            }
            print("\n")
            String output = outputBuilder.toString().trim()

            Message modelMsg = context.addMessage(
                role: "assistant",
                author: george.name,
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(modelMsg)
        } finally {
            this.context = context
        }
    }

    void toolTest() {
        String toolInjection = """## Tool Use Protocol
- You have access to specialized tools represented as functions.
- When a task requires a tool, or when a character uses an item in their inventory, you must call the corresponding function.
- Do not narrate the tool action until you have received the 'tool' role response with the results."""
        this.context.messages[0].content = "${systemMsg.content}\n---\n${george.toJson()}\n---\n${library.toJson()}\n---\n${toolInjection}"
        logger.info("## Running Tool use tests")

        String input = "George, would you please read the projects LICENSE file, and tell me your thoughts on being its Sovereign AI persona."
        logger.info("### User says:\n${input}")

        Message lastMsg = context.messages.last()
        Message userMsg = context.addMessage(
            role: "user",
            author: phiglit.name,
            content: input,
            parentId: lastMsg.messageId,
            vibe: this.vibe
        )
        logManager.appendEntry(userMsg)

        logger.info("### George says:")
        StringBuilder outputBuilder = new StringBuilder()
        model.streamResponse(context, george.inventory) { token ->
            print(token)
            outputBuilder.append(token)
        }
        print("\n")
        assert model.toolCall
        String output = outputBuilder.toString().trim()

        Message modelMsg = context.addMessage(
            role: "assistant",
            author: george.name,
            content: output,
            parentId: userMsg.messageId,
            vibe: this.vibe
        )
        logManager.appendEntry(modelMsg)

        logger.info("### Got a tool call that looks like this (response side channel):")
        println(model.toolCall)

        logger.info("### Running the command we got from the model.")
        String action = model.toolCall[0].function.arguments.action
        println("Wanting to do this: ${action}")

        Item botTerm = george.inventory.items["bot_term"]?.first()
        assert botTerm
        botTerm.metadata.action = action
        george.inventory.executeToolLogic(botTerm)
        assert botTerm.metadata.result

        Message toolTurn = context.addMessage(
            role: "tool",
            author: "George",
            content: botTerm.metadata.result,
            parentId: modelMsg.messageId,
            vibe: this.vibe,
            tool_call_id: model.toolCall[0].id
        )
        logManager.appendEntry(toolTurn)

        logger.info("### George has this to say:")
        outputBuilder = new StringBuilder()
        model.streamResponse(context) { token ->
            print(token)
            outputBuilder.append(token)
        }
        print("\n")
        output = outputBuilder.toString().trim()

        modelMsg = context.addMessage(
            role: "assistant",
            author: "George",
            content: output,
            parentId: toolTurn.messageId,
            vibe: this.vibe
        )
        logManager.appendEntry(modelMsg)
    }

    void illustratorTest() {
        logger.info("## Running Illustrator tests")

        Message lastMessage = this.context.messages.last()
        assert lastMessage.content.contains("<IMAGE_DESC>")

        Illustrator canvas = new Illustrator()
        canvas.style = ImageType.LANDSCAPE
        canvas.title = "Illustration"
        def imageDesc = new TagParser().extractString(lastMessage.content, "IMAGE_DESC")
        def comfyJson = canvas.getComfyUiJson(imageDesc)

        def img = canvas.generateImage(comfyJson)
        logger.info("## Image generation complete:\n${img}")
    }
}