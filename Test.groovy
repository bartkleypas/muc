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
        this.model = new Model(ModelType.SMALL)
        this.vibe = new Resonance()

        // Delete existing unit test results to start fresh
        if (new File(historyFile).exists()) {
           new File(historyFile).delete()
        }
        String promptText = new File("Characters/George.md").text

        // We start the train with this one, so make it global eh?
        this.systemMsg = context.addMessage(
            role: "system",
            author: "System",
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
        Item cyberDeck = new Item(
            name: "Cyberdeck",
            type: ItemType.TRINKET,
            description: "A hand held Linux computer.",
        )
        this.phiglit.inventory.addItem(cyberDeck)

        Item chisel = new Item(
            name: "chisel",
            description: "A chisel, that oddly never dulls, and is always just the right size for the job.",
            type: ItemType.TRINKET,
        )
        this.epwna.inventory.addItem(chisel)

        Item terminal = new Item(
            name: "terminal",
            type: ItemType.TOOL,
            description: "A command to execute through the local shell. Commands include `ls`, `find`, `grep`, and `cat`."
        )
        this.george.inventory.addItem(terminal)

        Item imgGenerator = new Item(
            name: "groovy_runner",
            type: ItemType.TOOL,
            description: "A command to execute to generate an image. Command is `groovy main.groovy --image \"\$description\"`."
        )
        this.george.inventory.addItem(imgGenerator)
    }

    private Message simulateTurn(Character author, String input, Message parentMsg, Resonance currentVibe) {
        logger.info("### User says:\n${input}")

        Message userMsg = context.addMessage(
            role: "user",
            author: author.name,
            content: input,
            parentId: parentMsg.messageId,
            vibe: currentVibe
        )
        logManager.appendEntry(userMsg)

        String vibePrefix = currentVibe.toPrefix()
        StringBuilder outputBuilder = new StringBuilder()
        logger.info("### George says:")
        model.streamResponse(context, null, vibePrefix) { token ->
            print(token)
            outputBuilder.append(token)
        }
        print("\n")

        Message modelMsg = context.addMessage(
            role: "assistant",
            author: george.name,
            content: outputBuilder.toString().trim(),
            parentId: userMsg.messageId,
            vibe: currentVibe
        )
        logManager.appendEntry(modelMsg)
        return modelMsg
    }

    private Message executeToolTurn(Character author, String input, Message parentMsg, Resonance currentVibe, Inventory inventory, String expectedItemName) {
        logger.info("### User says:\n${input}")

        Message userMsg = context.addMessage(
            role: "user",
            author: author.name,
            content: input,
            parentId: parentMsg.messageId,
            vibe: currentVibe
        )
        logManager.appendEntry(userMsg)

        String vibePrefix = currentVibe.toPrefix()
        logger.info("### George says:")
        StringBuilder outputBuilder = new StringBuilder()
        model.streamResponse(context, inventory, vibePrefix) { token ->
            print(token)
            outputBuilder.append(token)
        }
        print("\n")
        
        assert model.toolCall : "Expected a tool call but got none."
        
        Message modelMsg = context.addMessage(
            role: "assistant",
            author: george.name,
            content: outputBuilder.toString().trim(),
            parentId: userMsg.messageId,
            vibe: currentVibe,
            tool_calls: model.toolCall
        )
        logManager.appendEntry(modelMsg)

        logger.info("### Got a tool call that looks like this (response side channel):")
        println(model.toolCall)

        logger.info("### Running the command we got from the model.")
        String action = model.toolCall[0].function.arguments.action
        println("Wanting to do this: ${action}")

        Item botItem = george.inventory.items[expectedItemName]?.first()
        assert botItem : "Could not find expected item: ${expectedItemName}"
        botItem.metadata.action = action
        george.inventory.useItem(botItem)
        assert botItem.metadata.result : "Tool execution did not produce a result."

        Message toolTurn = context.addMessage(
            role: "tool",
            author: "George",
            content: botItem.metadata.result,
            parentId: modelMsg.messageId,
            vibe: currentVibe,
            tool_call_id: model.toolCall[0].id
        )
        logManager.appendEntry(toolTurn)

        logger.info("### George has this to say:")
        outputBuilder = new StringBuilder()
        model.streamResponse(context, null, vibePrefix) { token ->
            print(token)
            outputBuilder.append(token)
        }
        print("\n")

        Message finalModelMsg = context.addMessage(
            role: "assistant",
            author: "George",
            content: outputBuilder.toString().trim(),
            parentId: toolTurn.messageId,
            vibe: currentVibe
        )
        logManager.appendEntry(finalModelMsg)
        return finalModelMsg
    }

    void narratorTest() {
        try {
            logger.info("## Running 'Default' Handshake Test")
            String input = "${phiglit.toJson()}\n---\nGood morning George! My name is Phiglit. Would you please describe yourself, and where we are?"
            simulateTurn(phiglit, input, systemMsg, this.vibe)
        } finally {
            this.context = context
        }
    }

    void faderTest() {
        logger.info("## Running vibe checks")
        Message lastMessage = this.context.messages.last()

        try {
            this.vibe = new Resonance(
                warmth: 1.0,
                cynicism: 1.2,
                efficiency: 1.0,
                resonance: 1.0,
                gravity: 0.5
            )

            String input = "George, tell me what you think of the Scriptorium we are building together. I hope you find the environment agreeable."
            simulateTurn(phiglit, input, lastMessage, this.vibe.clone())
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

            String input = "${epwna.toJson()}\n---\nGood morning George! I'm Ewpna. Phiglit never told me what a handsome Owl he was working on back here. Your Scriptorium is quite a sight to behold. Almost infinate, if I'm not mistaken."
            simulateTurn(epwna, input, lastMsg, this.vibe.clone())
        } finally {
            this.context = context
        }
    }

    void toolTest() {
        logger.info("## Running Tool use tests")
        Message lastMsg = context.messages.last()
        String input = "George, would you please read the `LICENSE` file?"
        
        executeToolTurn(phiglit, input, lastMsg, this.vibe, george.inventory, "terminal")
    }

    void illustratorTest() {
        logger.info("## Running Illustrator use tests")
        Message lastMsg = context.messages.last()
        String input = "George, would you please create an image of the Scriptorium?"
        
        executeToolTurn(phiglit, input, lastMsg, this.vibe, george.inventory, "groovy_runner")
    }
}