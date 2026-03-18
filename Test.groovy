// -- Test all the things!

import org.kleypas.muc.cli.*
import org.kleypas.muc.io.*
import org.kleypas.muc.rng.*
import org.kleypas.muc.location.*
import org.kleypas.muc.character.*
import org.kleypas.muc.inventory.*
import org.kleypas.muc.model.*
import org.kleypas.muc.model.resonance.*
import org.kleypas.muc.illustrator.*

class Test {
    Cli cli
    Logger logger
    Model model
    LogManager logManager
    Context context
    Message systemMsg
    Resonance vibe
    String rngResults
    String locationResults
    String characterResults
    String inventoryResults
    String narratorResults
    String storyResults

    Test() {
        this.logger = new Logger()
        logger.setLevel(LogLevel.INFO)
    }

    void run() {
        logger.info("# Starting Unit Tests")
        initializeResources()

        rngTest()
        locationTest()
        characterTest()
        inventoryTest()
        narratorTest()
        // illustrator()
        faderTest()
        storyTest()
        logger.info("# Tests completed successfully.")
    }

    private void initializeResources() {
        this.cli = new Cli()
        String historyFile = "Story/UnitTests.jsonl"
        this.logManager = new LogManager(historyFile)
        this.context = new Context().enableLogging(logManager)
        this.model = new Model(ModelType.MEDIUM)
        this.vibe = new Resonance()

        initializeNewChronicle(historyFile)
    }

    private void initializeNewChronicle(String path) {
        // Delete existing unit test results to start fresh
        if (new File(path).exists()) {
           new File(path).delete()
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

    void rngTest() {
        def sb = []
        sb.add("## Running RNG tests")
        Coin coin = new Coin() // 2 sided dice, ie: Coin
        sb.add("### Picked up our lucky coin. It is showing:\n${coin}")

        coin = coin.flip()
        sb.add("### Flipping it, we get this:\n${coin}")

        def flips = 8
        def results = []
        for (int i = 0; i < flips; i++) {
            def flip = coin.flip()
            def value = flip.val ? 1 : 0 // recast to "int"? stil a String, but now a numeral
            results.add(value)
        }
        sb.add("### Flipping it a bunch (in binary):\n${results.join()}")

        def d20 = new Dice(DiceType.D20)
        sb.add("### Rolling a D20 on the desk:\n${d20}")
        this.rngResults = sb.join("\n")
        logger.info(sb.join("\n"))
    }

    void locationTest() {
        def sb = []
        sb.add("## Running Location tests")

        Location location = new Location()
        sb.add("### Location:\n${location.toMd()}")

        Poi library = new Poi(
            location: location,
            name: "The Library of George the Radiant Owl",
            description: "An infinate library, and grand repository of information. The walls and texts of the library are swirling code and shimmering vellum that distort and pulse with energy. There is a melancholic tune from a distant lute that weaves into the fabric of the building, and the faint, ethereal voice of the mothership echoing in the alcoves. There is a perpetual clinging scent of aged ink and long lost lore. A place to contemplate an adventure, or journal adventures about to begin."
        )
        sb.add("### POI:\n${library.toMd()}")
        this.locationResults = sb.join("\n")
        logger.info(sb.join("\n"))
    }

    void characterTest() {
        def sb = []
        sb.add("#### Character Sheet for Phiglit:")
        Character hero = new Character(name: "Phiglit")
        hero.description = "The Reclusive Code Wizard"
        hero.bio = "A middle aged male human, standing about 6 feet tall. Has grey hair with a full beard and mustashe. Wearing a zippered hoodie and bluejeans. Runs Arch Linux, btw."
        hero.armorType = ArmorType.LIGHT
        hero.location = new Location(0.00f, 0.00f, 0.00f)

        Item tool = new Item("Linux terminal of Justice", ItemType.TOOL)
        tool.description = "A rugged and powerful pocket computer, used for sending instructions to chatbots."
        hero.inventory.addItem(tool)
        sb.add(hero.toMd())
        this.characterResults = sb.join("\n")
        logger.info(sb.join("\n"))
    }

    // Epwna does a lot of inventory testing for us
    void inventoryTest() {
        def sb = []
        sb.add("## Running Inventory tests")
        Character hero = new Character(name: "Epwna")
        hero.description = "The Crafty Tinkerer"
        hero.bio = "A middle aged female human, standing around 5 feet tall. She is wearing pink overalls, and has a short yellow and red pixie haircut. Enjoys carving and carpentry, as well as a skilled sculpter. Takes inspiration from nature and especially birds."
        hero.armorType = ArmorType.LIGHT
        hero.location = new Location(0.00f, 0.00f, 0.00f)
        sb.add("### A Hero:\n${hero.toMd()}")

        Item tool = new Item("Chisel of Carving", ItemType.TOOL)
        tool.description = "A chisel, that oddly never dulls, and is always just the right size for the job."
        hero.inventory.addItem(tool)
        sb.add("### And she comes prepared with:\n${hero.inventory.toMd()}")

        hero.inventory.useItem(tool)
        sb.add("### Our hero used ${tool.name}, but it should still be in her inventory:\n${hero.inventory.toMd()}")

        Item potion = new Item("Healing potion", ItemType.CONSUMABLE)
        potion.description = "Some brain-sauce for the static tantrums"
        potion.stack = 3
        hero.inventory.addItem(potion)
        sb.add("### After picking up a stack of ${potion.stack} ${potion.name}(s), our hero now has this:\n${hero.inventory.toMd()}")

        hero.inventory.useItem(potion)
        sb.add("### And after downing a breakfast of champions (${potion.name}), they are left with this:\n${hero.inventory.toMd()}")

        Item muffin = new Item("Blueberry Muffin", ItemType.CONSUMABLE)
        muffin.description = "A muffin of infinite tasty goodness"
        hero.inventory.addItem(muffin)
        sb.add("### Our hero picked up a ${muffin.name}, filling her inventory slots in ${hero.inventory.name}:\n${hero.inventory.toMd()}")

        sb.add("### Adding anything else to ${hero.inventory.name} should fail:")
        Item straw = new Item("Straw", ItemType.CONSUMABLE)
        try {
            hero.inventory.addItem(straw)
        } catch (e) {
            sb.add("!!! Yup. here is the error:\n${e.getMessage()}")
        }

        hero.inventory.useItem(potion)
        sb.add("### After using a potion:\n${hero.inventory.toMd()}")

        hero.inventory.useItem(potion)
        sb.add("### And another:\n${hero.inventory.toMd()}")

        sb.add("### And... another? (should fail):")
        try {
            hero.inventory.useItem(potion)
        } catch (e) {
            sb.add("!!! Yup. With this error:\n${e.getMessage()}")
        }

        sb.add("### Make sure one is still left in the bag.")
        potion.stack = 1
        hero.inventory.addItem(potion)
        sb.add("### Finally, our hero ends up like this:\n${hero.toMd()}")
        this.inventoryResults = "#### Character sheet for Epwna:\n${hero.toMd()}"
        logger.info(sb.join("\n"))
    }

    void narratorTest() {
        def sb = []
        this.context.messages[0].content = "${systemMsg.content}\n${this.locationResults}\n${this.rngResults}"
        try {
            sb.add("## Running Unified Narrator Test")

            // The Interaction
            String input = "Good morning George. My name is Phiglit. Here is my character sheet:\n${characterResults}\nWould you please describe yourself, and where we are?"
            sb.add("### User says:\n${input}")

            Message userMsg = context.addMessage(
                role: "user",
                author: "Traveler",
                content: input,
                parentId: systemMsg.messageId,
                vibe: this.vibe
            )
            logManager.appendEntry(userMsg)

            // Generate and Log
            String output = model.generateResponse(context)
            sb.add("### George says:\n${output}")
            Message modelMsg = context.addMessage(
                role: "assistant",
                author: "George",
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe
            )
            logManager.appendEntry(modelMsg)

        } finally {
            sb.add("## Character Test done.")
            logger.info(sb.join("\n"))
        }
    }

    void illustratorTest() {
        Logger.info "## Running Illustrator tests"
        Character hero = new Character(name: "Rosie")
        hero.description = "The Illustrator"
        hero.bio = "A tiny and energetic Anna's hummingbird (Calypte anna), around 4 inches in length. Raised from a hatchling by Epwna, and usually found hovering somewhere close to her. Speaks quickly in disjointed sentences. Has a soft, but squeaky voice."

        Item brush = new Item("Paintbrush of Illusion", ItemType.TOOL)
        brush.description = "An ornate and detailed paintbrush, mostly symbolic, but used for illustrating an ongoing story."
        hero.inventory.addItem(brush)

        def characterSheet = "Characters/${hero.name}.json"
        Logger.info "### Writing character sheet to:\r\n${characterSheet}"
        hero.exportCharacterSheet(characterSheet)

        Logger.info "### Reading last message from:\r\nStory/Chapter_0.json"
        Context story = new Context()
        story = story.importContext("Story/Chapter_0.json")
        Message lastMessage = story.messages[-1]
        assert lastMessage.content.contains("<IMAGE_DESC>")

        Illustrator canvas = new Illustrator()
        canvas.style = ImageType.LANDSCAPE
        canvas.title = "Illustration"
        def comfyJson = canvas.promptToJson(lastMessage.content)

        Logger.info "### ComfyUI Json to send:\r\n${comfyJson}"
        // Image generation is currently offline, so bailing out before we send off a request.
        return

        def img = canvas.generateImage(comfyJson)
        Logger.info "### Recipt:\r\n${img}"
    }

    void faderTest() {
        logger.info("## Running vibe checks")
        def sb = []

        try {
            // See `ResonanceType` class for these keys and the range of values (0.0 - 2.0 in a Double):
            this.vibe = new Resonance(
                warmth: 0.2,
                cynicism: 1.8,
                efficiency: 1.0,
                resonance: 1.0,
                gravity: 0.5
            )
            sb.add("### Adjusted Impulse: ${this.vibe.asMap()}")

            String input = "George, tell me what you think of this 'Refinery' we are building. Be honest."

            Message userMsg = context.addMessage(
                role: "user",
                author: "Traveler",
                content: input,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(userMsg)

            String output = model.generateResponse(context, this.vibe.toPrefix())
            sb.add("### George's Spikey Response:\n${output}")

            def deltas = ResonanceEngine.calculate(output)
            this.vibe + deltas

            sb.add("### Post-Calculation Vibe: ${this.vibe.asMap()}")

            Message modelMsg = context.addMessage(
                role: "assistant",
                author: "George",
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(modelMsg)

        } finally {
            logger.info(sb.join("\n"))
            logger.info("## Fader Stress Test complete")
        }
    }

    void storyTest() {
        def sb = []
        try {
            sb.add("## Running Story tests, resuming from the UnitTests.jsonl file we crafted in the Narrator testing above.")
            this.context = logManager.readAllEntries()

            Message lastMsg = context.messages.last()
            this.vibe = lastMsg.vibe

            String input = "I would like to introduce you to Ewpna. This is her character sheet:\n${inventoryResults}"
            input = "${input}\nI think I would like to pick up the electric bass and strike up a relaxed and groovy bassline. Currently it is sitting in its stand by the hearth."
            sb.add("### User says:\n${input}")
            Message userMsg = context.addMessage(
                role: "user",
                author: "Traveler",
                content: input,
                parentId: lastMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(userMsg)

            String output = model.generateResponse(context, this.vibe.toPrefix())
            sb.add("### George says:\n${output}")
            Message modelMsg = context.addMessage(
                role: "assistant",
                author: "George",
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(modelMsg)
        } finally {
            sb.add("## Story Resume Tests done.")
            logger.info(sb.join("\n"))
        }
    }

    // NOTE: Will break test execution waiting for input.
    void tuiTest() {
        // Using Groovy's 'use' or a simple try-with-resources equivalent
        def bridge = new TerminalBridge()
        try {
            bridge.drawSignature()
            int rad = 80
            boolean running = true

            while (running) {
                bridge.updateHUD("The Grand Repository", "Phiglit (Ready)", rad)

                char key = (char) bridge.readKey()
                if (key == 'r' && rad < 100) rad += 5
                else if (key == 'e' && rad > 0) rad -= 5
                else if (key == 'q') running = false
            }
        } finally {
            bridge.close()
        }
    }
}