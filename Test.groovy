
import org.kleypas.muc.cli.Cli
import org.kleypas.muc.cli.Logger
import org.kleypas.muc.cli.LogLevel
import org.kleypas.muc.cli.TerminalBridge

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

import org.kleypas.muc.model.Context
import org.kleypas.muc.model.Message
import org.kleypas.muc.model.Model
import org.kleypas.muc.model.Provider
import org.kleypas.muc.model.TagParser

import org.kleypas.muc.illustrator.Illustrator
import org.kleypas.muc.illustrator.ImageType

class Test {
    Cli cli = new Cli()

    Test() {
        Logger.setLevel(LogLevel.INFO)
    }

    void rng() {
        Logger.info "## Running RNG tests"
        Coin coin = new Coin() // 2 sided dice, ie: Coin
        Logger.info "### Picked up our lucky coin. It is showing:\r\n${coin}"

        coin = coin.flip()
        Logger.info "### Flipping it, we get this:\r\n${coin}"

        def flips = 8
        def results = []
        for (int i = 0; i < flips; i++) {
            def flip = coin.flip()
            def value = flip.val ? 1 : 0 // recast to "int"? stil a String, but now a numeral
            results.add(value)
        }
        Logger.info "### Flipping it a bunch (in binary):\r\n${results.join()}"

        def d20 = new Dice(DiceType.D20)
        Logger.info "### Rolling a D20 on the desk:\r\n${d20}"
    }

    void location() {
        Logger.info "## Running Location tests"
        Location location = new Location(0.00f, 0.00f, 0.00f)
        Logger.info "### Location:\r\n${location}"

        Poi library = new Poi(location, "The Library of George the Radiant Owl")
        library.description = "An infinate library, and grand repository of information. The walls and texts of the library are swirling code and shimmering vellum that distort and pulse with energy. There is a melancholic tune from a distant lute that weaves into the fabric of the building, and the faint, ethereal voice of the mothership echoing in the alcoves. There is a perpetual clinging scent of aged ink and long lost lore. A place to contemplate an adventure, or journal adventures about to begin."
        Logger.info "### POI:\r\n${library.toString()}"

        def locationSheet = "Locations/Library.json"
        library.exportPoi(locationSheet)
    }

    void character() {
        Logger.info "## Running Character tests"
        Character hero = new Character(name: "Phiglit")
        hero.description = "The Reclusive Code Wizard"
        hero.bio = "A middle aged male human, standing about 6 feet tall. Has grey hair with a full beard and mustashe. Wearing a zippered hoodie and bluejeans. Runs Arch Linux, btw."
        hero.armorType = ArmorType.LIGHT
        hero.location = new Location(0.00f, 0.00f, 0.00f)
        Logger.info "### A Hero:\r\n${hero.toString()}"

        Item tool = new Item("Linux terminal of Justice", ItemType.TOOL)
        tool.description = "A rugged and powerful pocket computer, used for sending instructions to chatbots."
        hero.inventory.addItem(tool)
        Logger.info "### With ${tool.name} in hand, our hero now has this:\r\n${hero.toString()}"

        def characterSheet = "Characters/${hero.name}.json"
        Logger.info "### Writing character sheet to:\r\n${characterSheet}"
        hero.exportCharacterSheet(characterSheet)
    }

    // Epwna does a lot of inventory testing for us
    void inventory() {
        Logger.info "## Running Inventory tests"
        Character hero = new Character(name: "Epwna")
        hero.description = "The Crafty Tinkerer"
        hero.bio = "A middle aged female human, standing around 5 feet tall. She is wearing pink overalls, and has a short yellow and red pixie haircut. Enjoys carving and carpentry, as well as a skilled sculpter. Takes inspiration from nature and especially birds."
        hero.armorType = ArmorType.LIGHT
        hero.location = new Location(0.00f, 0.00f, 0.00f)
        Logger.info "### A Hero:\r\n${hero.toString()}"

        Item tool = new Item("Chisel of Carving", ItemType.TOOL)
        tool.description = "A chisel, that oddly never dulls, and is always just the right size for the job."
        hero.inventory.addItem(tool)
        Logger.info "### And she comes prepared with:\r\n${hero.inventory.toString()}"

        hero.inventory.useItem(tool)
        Logger.info "### Our hero used ${tool.name}, but it should still be in her inventory:\r\n${hero.inventory.toString()}"

        Item potion = new Item("Healing potion", ItemType.CONSUMABLE)
        potion.description = "Some brain-sauce for the static tantrums"
        potion.stack = 3
        hero.inventory.addItem(potion)
        Logger.info "### After picking up a stack of ${potion.stack} ${potion.name}(s), our hero now has this:\r\n${hero.inventory.toString()}"

        hero.inventory.useItem(potion)
        Logger.info "### And after downing a breakfast of champions (${potion.name}), they are left with this:\r\n${hero.inventory.toString()}"

        Item muffin = new Item("Blueberry Muffin", ItemType.CONSUMABLE)
        muffin.description = "A muffin of infinite tasty goodness"
        hero.inventory.addItem(muffin)
        Logger.info "### Our hero picked up a ${muffin.name}, filling her inventory slots in ${hero.inventory.name}:\r\n${hero.inventory.toString()}"

        Logger.info "### Adding anything else to ${hero.inventory.name} should fail:"
        Item straw = new Item("Straw", ItemType.CONSUMABLE)
        try {
            hero.inventory.addItem(straw)
        } catch (e) {
            Logger.info "!!! Yup. here is the error:\r\n${e.getMessage()}"
        }

        hero.inventory.useItem(potion)
        Logger.info "### After using a potion:\r\n${hero.inventory.toString()}"

        hero.inventory.useItem(potion)
        Logger.info "### And another:\r\n${hero.inventory.toString()}"

        Logger.info "### And... another? (should fail):"
        try {
            hero.inventory.useItem(potion)
        } catch (e) {
            Logger.info "!!! Yup. With this error:\r\n${e.getMessage()}"
        }

        Logger.info "### Make sure one is still left in the bag."
        potion.stack = 1
        hero.inventory.addItem(potion)
        Logger.info "### Finally, our hero ends up like this:\r\n${hero.toString()}"

        def characterSheet = "Characters/${hero.name}.json"
        Logger.info "### Writing character sheet to:\r\n${characterSheet}"
        hero.exportCharacterSheet(characterSheet)
    }

    void narrator() {
        try {
            Logger.info "## Running Unified Narrator Test"

            // 2. Prep the State
            String georgePrompt = new File("Characters/George.md").text
            String georgeSheet = new File("Characters/George.json").text
            String locationSheet = new File("Locations/Library.json").text
            def defaultHarmony = [nurturance: 0.90, playfulness: 1.10, steadfastness: 1.70, attunement: 1.85]

            // Build the System Anchor
            String fullSystemPrompt = "${georgePrompt}\n### Character Sheet:\n${georgeSheet}\n### Location:\n${locationSheet}"

            // 3. Initialize Context with Streaming
            Context context = new Context().enableLogging("Story/Narrator.jsonl")
            Model model = new Model(model: "biggun", body: context)
            model.body.addMessage("system", fullSystemPrompt)

            // 4. The Interaction
            String input = "Describe the Scriptorium, George."
            Logger.info "### user says: ${input}"

            def systemMessage = model.body.getLastMessage()
            def userMessage = model.body.addMessage("user", input, systemMessage.messageId).getLastMessage()

            // Generate and Log
            def output = model.generateResponse(context.swizzleSpeaker("George"))
            Logger.info "### George says: ${output}"

            def modelMessage = model.body.addMessage("assistant", output, userMessage.messageId).getLastMessage()
            model.body.exportContext("Story/Chapter_0.json")

        } finally {
            Logger.info "Test done."
        }
    }

    void illustrator() {
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

    void story() {
        Logger.info "## Running Story tests"

        Context context = new Context().enableLogging("Story/Story_test.jsonl")
        String georgePrompt = new File("Characters/George.md").text
        context.addMessage("system", georgePrompt)

        Logger.info "### Loading story from:\r\nStory/Chapter_0.json"
        def storyContext = new Context().importContext("Story/Chapter_0.json")

        context.messages.addAll(storyContext.messages)

        def lastMessage = context.getLastMessage()
        Model narrator = new Model(model: "narrator")

        def input = "I think I would like to pick up the electric bass and strike up a relaxed and groovy bassline. Currently it is sitting in its stand by the hearth."
        Logger.info "### user says:\r\n${input}"
        def userMessage = context.addMessage("user", input, lastMessage.messageId).getLastMessage()

        def output = narrator.generateResponse(context.swizzleSpeaker("George"))
        Logger.info "### George says:\r\n${output}"
        def modelMessage = context.addMessage("George", output, userMessage.messageId).getLastMessage()
        context.exportContext("Story/Chapter_0.json")
    }

    // NOTE: Will break test execution waiting for input.
    void tui() {
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