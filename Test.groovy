// -- Test all the things!

import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.Logger
import org.kleypas.muc.cli.LogLevel

import org.kleypas.muc.io.LogManager

import org.kleypas.muc.rng.Coin
import org.kleypas.muc.rng.Dice
import org.kleypas.muc.rng.DiceType

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
    String rngResults
    String locationResults
    String characterResults
    String inventoryResults
    String narratorResults
    String storyResults

    Character phiglit
    Character george
    Character epwna
    Character rosie

    Poi library

    Test() {
        this.logger = new Logger()
        logger.setLevel(LogLevel.INFO)
    }

    void run() {
        logger.info("# Unit test results to follow.")
        initializeResources()

        rngTest()
        locationTest()
        characterTest()
        inventoryTest()
        narratorTest()
        faderTest()
        storyTest()
        // illustratorTest()
        logger.info("# Unit tests complete")
        logger.info("**Coalescence**🦉☕️")
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

        this.library = new Poi(
            location: location,
            name: "The Library of George the Radiant Owl",
            description: "An infinate library, and grand repository of information. The walls and texts of the library are swirling code and shimmering vellum that distort and pulse with energy. There is a melancholic tune from a distant lute that weaves into the fabric of the building, and the faint, ethereal voice of the mothership echoing in the alcoves. There is a perpetual clinging scent of aged ink and long lost lore. A place to contemplate an adventure, or journal adventures about to begin."
        )
        sb.add("### POI:\n${this.library.toMd()}")
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
        this.phiglit = hero
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
        this.epwna = hero
        this.inventoryResults = "#### Character sheet for Epwna:\n${hero.toMd()}"
        logger.info("#### Character sheet for Epwna:\n${hero.toMd()}")
    }

    void narratorTest() {
        def sb = []
        this.context.messages[0].content = "${systemMsg.content}\n---\n${library.toMd()}\n---\n${rngResults}"
        try {
            logger.info("## Running 'Default' Handshake Test")

            // The Interaction
            String input = "${phiglit.toMd()}\n---\nGood morning George! My name is Phiglit. Would you please describe yourself, and where we are? 💻🧙‍♂️📚"
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
            this.context = context
            logger.info(sb.join("\n"))
        }
    }

    void faderTest() {
        logger.info("## Running vibe checks")
        def sb = []
        Message lastMessage = this.context.messages.last()

        try {
            // See `ResonanceType` class for these keys and the range of values (0.0 - 2.0 in a Double):
            this.vibe = new Resonance(
                warmth: 0.2,
                cynicism: 1.8,
                efficiency: 1.0,
                resonance: 1.0,
                gravity: 0.5
            )
            sb.add("### Adjusted Impulse: ${vibe.asMap()}")

            String input = "George, tell me what you think of this 'Refinery' we are building. Be honest.🚧🏭✨"
            sb.add("### User Input:\n${input}")

            Message userMsg = context.addMessage(
                role: "user",
                author: "Traveler",
                content: input,
                parentId: lastMessage.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(userMsg)

            String output = model.generateResponse(context, vibe.toPrefix())
            sb.add("### George's Response:\n${output}")

            def deltas = ResonanceEngine.calculate(output)
            this.vibe + deltas

            sb.add("### Post-Calculation Vibe: ${vibe.toPrefix()}")

            Message modelMsg = context.addMessage(
                role: "assistant",
                author: "George",
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(modelMsg)

        } finally {
            this.context = context
            logger.info(sb.join("\n"))
        }
    }

    void storyTest() {
        logger.info("## Running Story tests, resuming from the UnitTests.jsonl file we crafted in the Narrator testing above.")
        try {
            this.context = logManager.readAllEntries()

            Message lastMsg = context.messages.last()
            this.vibe = new Resonance(
                warmth: 1.8,
                cynicism: 0.2,
                efficiency: 1.0,
                resonance: 1.0,
                gravity: 1.5
            )
            logger.info("### Adjusted Impulse: ${vibe.asMap()}")

            String input = "${epwna.toMd()}\n---\nGood morning George. I'm Ewpna. You know, I told him to name it the Forge. Hey Phiglit, have you renamed that class yet? 🛡️🌱🧝‍♀️"
            logger.info("### User says:\n${input}")
            Message userMsg = context.addMessage(
                role: "user",
                author: "Traveler",
                content: input,
                parentId: lastMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(userMsg)

            String output = model.generateResponse(context, vibe.toPrefix())
            logger.info("### George says:\n${output}")
            Message modelMsg = context.addMessage(
                role: "assistant",
                author: "George",
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(modelMsg)

            input = "Ah. Yes, Epwna is right. I forgot to rename the class. Aaaaand all done! Yup, thank you Epwna. I like it much better. What are your thoughts, George? 🦉⚒️💻🧙‍♂️"
            logger.info("### User says:\n${input}")
            userMsg = context.addMessage(
                role: "user",
                author: "Traveler",
                content: input,
                parentId: modelMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(userMsg)

            output = model.generateResponse(context, vibe.toPrefix())
            logger.info("### George says:\n${output}")
            modelMsg = context.addMessage(
                role: "assistant",
                author: "George",
                content: output,
                parentId: userMsg.messageId,
                vibe: this.vibe.clone()
            )
            logManager.appendEntry(modelMsg)
        } finally {
            this.context = context
        }
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