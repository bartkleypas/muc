#!/usr/bin/env groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.JsonParserType

import Cli
import Character
import Illustrator
import Inventory
import Item
import Location
import Model
import Rng

Cli cli = new Cli()
def options = cli.parse(args)

if (options.json) {
    cli.logLevel = LogLevel.JSON
}

if (options.test) {

    // Holds our testRoutines in a map
    // Note: we are loading closures, so we need
    // to run them in a loop later
    def testRoutines = [:]

    // A quick refresher on random. Uses Rng.groovy, and should return
    // fast
    testRoutines["RNG"] = {
        // list of strings we want to log to the console.
        def out = []

        def coin = new Coin() // 2 sided dice, ie: Coin

        out.add("### Picked up our lucky coin. It is showing:")
        out.add(coin)

        coin = coin.flip()
        out.add("### Flipping it, we get this:")
        out.add(coin)

        def flips = 8
        def results = []

        for (int i = 0; i < flips; i++) {
            def flip = coin.flip()
            def value = flip.val ? 1 : 0 // recast to "int"? stil a String, but now a numeral
            results.add(value)
        }
        out.add("### Flipping it a bunch (in binary):")
        out.add(results.join())

        def d20 = new Dice(DiceType.D20)
        out.add("### Rolling a D20 on the desk:")
        out.add(d20)

        cli.log(out.join('\r\n'))
    }

    // Build the house
    def home
    def location = new Location(0.00f, 0.00f, 0.00f)
    testRoutines["Location"] = {
        def out = []
        home = new Poi(location, "Phiglit's and Epwna's Home")

        home.description = "A forest cottage in the Pacific North West"
        out.add("### POI:")
        out.add(home.toString())
        cli.log(out.join('\r\n'))
    }

    // Fill it with characters (should still be quick)
    def phiglit
    testRoutines["Character"] = {
        def out = []
        out.add("### A Hero:")

        def hero = new Character(name: "Phiglit")
        hero.description = "The Reclusive Code Wizard"
        hero.bio = "A middle aged male human, standing about 6 feet tall. Has grey hair with a full beard and mustashe. Wearing a zippered hoodie and bluejeans. Runs Arch Linux, btw."
        hero.armorType = ArmorType.LIGHT
        hero.location = location
        home.addOccupant(hero)

        out.add(hero.toString())

        def tool = new Item("Linux terminal of Justice", ItemType.TOOL)
        tool.description = "A rugged and powerful pocket computer, used for sending instructions to chatbots."
        hero.inventory.addItem(tool)
        out.add("### With ${tool.name} in hand, our hero now has this:")
        out.add(hero.toString())

        phiglit = hero

        def characterSheet = "Characters/${hero.name}.json"
        out.add("### Writing character sheet to:\r\n${characterSheet}")
        hero.exportCharacterSheet(characterSheet)

        cli.log(out.join('\r\n'))
    }

    // Epwna does a lot of inventory testing for us
    def epwna
    testRoutines["Inventory"] = {
        def out = []
        out.add("### A Hero:")

        def hero = new Character(name: "Epwna")
        hero.description = "The Crafty Tinkerer"
        hero.bio = "A middle aged female human, standing around 5 feet tall. She is wearing pink overalls, and has a short yellow and red pixie haircut. Enjoys carving and carpentry, as well as a skilled sculpter. Takes inspiration from nature and especially birds."
        hero.armorType = ArmorType.LIGHT
        hero.location = location
        home.addOccupant(hero)

        out.add(hero.toString())

        out.add("### And she comes prepared, with:")
        def tool = new Item("Chisle of Carving", ItemType.TOOL)
        tool.description = "A chisle, that oddly never dulls, and is always just the right size for the job."
        hero.inventory.addItem(tool)
        out.add(hero.inventory.toString())

        hero.inventory.useItem(tool)
        out.add("### Our hero used ${tool.name}, but it should still be in her inventory:")
        out.add(hero.inventory.toString())

        def potion = new Item("Healing potion", ItemType.CONSUMABLE)
        potion.description = "Some brain-sauce for the static tantrums"
        potion.stack = 3
        hero.inventory.addItem(potion)
        out.add("### After picking up a stack of ${potion.stack} ${potion.name}(s), our hero now has this:")
        out.add(hero.inventory.toString())

        hero.inventory.useItem(potion)
        out.add("### And after downing a breakfast of champions (${potion.name}), they are left with this:")
        out.add(hero.inventory.toString())

        def muffin = new Item("Blueberry Muffin", ItemType.CONSUMABLE)
        muffin.description = "A muffin of infinite tasty goodness"
        hero.inventory.addItem(muffin)
        out.add("### Our hero picked up a ${muffin.name}, filling her inventory slots in ${hero.inventory.name}:")
        out.add(hero.inventory.toString())

        out.add("### Adding anything else to ${hero.inventory.name} should fail:")
        def straw = new Item("Straw", ItemType.CONSUMABLE)
        try {
            hero.inventory.addItem(straw)
        } catch (e) {
            out.add("!!! Yup. here is the error:")
            out.add("${e.getMessage()}")
        }

        out.add("### Using a potion:")
        hero.inventory.useItem(potion)
        out.add(hero.inventory.toString())

        out.add("### And another:")
        hero.inventory.useItem(potion)
        out.add(hero.inventory.toString())

        out.add("### And... another? (should fail):")
        try {
            hero.inventory.useItem(potion)
        } catch (e) {
            out.add("!!! Yup. With this error:")
            out.add("${e.getMessage()}")
        }

        out.add("### Make sure one is still left in the bag.")
        potion.stack = 1
        hero.inventory.addItem(potion)
        out.add("### Finally, our hero ends up like this:")
        out.add(hero.toString())

        epwna = hero

        def characterSheet = "Characters/${hero.name}.json"
        out.add("### Writing character sheet to:\r\n${characterSheet}")
        hero.exportCharacterSheet(characterSheet)

        cli.log(out.join('\r\n'))
    }

    // Ok. Now stuff gets complicated, and time consuming.
    // George is going to be building up some story context for us.
    def george
    testRoutines["Narrator"] = {
        // We know this stuff. George needs a character sheet
        def hero = new Character(name: "George")
        hero.description = "The Narrator"
        hero.bio = "An 18 inch tall Barred Owl (Strix varia), and narrator of our adventure. Speaks in a baritone voice in a smooth and measured cadence."

        def pen = new Item("A Pen of Writing", ItemType.TOOL)
        pen.description = "An ornate and efficient fountain pen, mostly decorative and symbolic, but used to narrate an ongoing story."
        hero.inventory.addItem(pen)
        home.addOccupant(hero)

        def characterSheet = "Characters/${hero.name}.json"
        cli.log("### Writing character sheet to:\r\n${characterSheet}")
        hero.exportCharacterSheet(characterSheet)

        // Now the fun stuff. Interacting with our hero
        def context = new Context()

        // Load up our narrators instructions
        def georgePrompt = new File("Characters/George.prompt").text

        // some background to feed to the narrator
        def background = [
            georgePrompt,
            "Your character sheet:",
            hero.toJson(),
            "Your location:",
            home.toJson(),
        ].join('\r\n')

        context.addMessage("system", background)
        def input = "Good morning ${hero.name}. My name is ${phiglit.name}. Would you please describe yourself and your location? Please be as detailed as you wish."
        cli.log("### Phiglit says:\r\n${input}")
        context.addMessage("Phiglit", input)

        def model = new Model(model: "narrator", body: context)
        def output = model.generateResponse(context.swizzleSpeaker(hero.name))

        cli.log("### ${hero.name} says:\r\n${output}")
        context.addMessage(hero.name, output)

        context.exportContext("Story/Chapter_0.json")
        george = hero
    }

    // more... MORE... *MORE*
    // Ok. This gets a bit nuts, but basically we are feeding a prompt with a similar context as what was fed
    // into George. Basically we do what we did before, except this time, with some existing context (story)!
    // Then, we feed Rosie with some instructions for generating StableDiffusion inputs.
    // Electric Sheep indeed.
    def rosie
    testRoutines["Illustrator"] = {
        def hero = new Character(name: "Rosie")
        hero.description = "The Illustrator"
        hero.bio = "A tiny and energetic Anna's hummingbird (Calypte anna), around 4 inches in length. Raised from a hatchling by Epwna, and usually found hovering somewhere close to her. Speaks quickly in disjointed sentences. Has a soft, but squeaky voice."


        def brush = new Item("Paintbrush of Illusion", ItemType.TOOL)
        brush.description = "An ornate and detailed paintbrush, mostly symbolic, but used for illustrating an ongoing story."
        hero.inventory.addItem(brush)
        home.addOccupant(hero)

        rosie = hero

        def characterSheet = "Characters/${hero.name}.json"
        cli.log("### Writing character sheet to:\r\n${characterSheet}")
        hero.exportCharacterSheet(characterSheet)

        // Load up our illustrators instructions
        def context = new Context()
        def rosiePrompt = new File("Characters/Rosie.prompt").text
        context.addMessage("system", rosiePrompt)

        def model = new Model(model: "illustrator", body: context)

        def story = new Context().importContext("Story/Chapter_0.json")
        def lastMessage = story.messages[-1]
        assert lastMessage.content.contains("<IMAGE_DESC>")
        context.addMessage("Epwna", lastMessage.content)

        def output = model.generateResponse(context.swizzleSpeaker(hero.name))
        cli.log("### ${hero.name} says:\r\n${output}")

        def cleaned = output.stripIndent().trim()
        assert cleaned.contains('```json')

        trimmed = cleaned.substring(cleaned.indexOf('{'), cleaned.lastIndexOf('}') + 1)
        json = new JsonSlurper().parseText(trimmed)

        canvas = new Illustrator()
        canvas.style = ImageType.LANDSCAPE
        canvas.title = "${home.name}"
        prompt = canvas.getPrompt("${json.prompt}, ${json.style}")

        // bypass image generation because it is effing busted in ROCm on
        // my hardware. Curse you python!!!
        return

        def img = canvas.generateImage(prompt)
        context.addMessage("system", "recipt:\r\n${img}")
        cli.log(img)
    }

    // Now, we should have some files backing our characters above.
    // We can try loading those instead of the objects we have
    // in the global scope. Eventually, we can discard the global
    // state change stuff in the earlier steps entirely.
    testRoutines["Story"] = {
        // Load George from the character sheet we built earlier
        def hero = new Character()
        def heroSheet = "Characters/George.json"
        cli.log("### Loading hero character sheet:\r\n${heroSheet}")
        hero = hero.importCharacterSheet(heroSheet)
        assert hero.name == "George"


        // Load some context for our story
        def context = new Context()
        def georgePrompt = new File("Characters/George.prompt").text

        def background = [
            georgePrompt,
            "You are:",
            hero.toJson(),
            "You are located:",
            home.toJson(),
            "You are joined by:"
        ]

        // Load some party members from json files
        home.occupants.each { name ->
            if (name == hero.name) { return }
            def characterSheet = "Characters/${name}.json"
            cli.log("### Loading character sheet:\r\n${characterSheet}")
            def character = new Character()
            character = character.importCharacterSheet(characterSheet)
            assert character.name == name
            background.add(character.toJson())
        }

        context.addMessage("system", background.join("\r\n"))
        def story = new Context()
        story = story.importContext("Story/Chapter_0.json")
        context.messages.addAll(story.messages)
        def model = new Model(model: "narrator", body: context)

        context.addMessage("Phiglit", "Ok Narrator ${hero.name}. Would you please continue the story?")
        def output = model.generateResponse(context.swizzleSpeaker("George"))
        context.addMessage(hero.name, output)
        context.exportContext("Story/Chapter_0.json")
        cli.log("### ${hero.name} says:\r\n${output}")

        // If we don't have a -c (--chat) arg, return. otherwise continue the
        // story!
        if (!options.chat) { return }
        while (true) {
            def input = cli.waitForInput()

            if (input.contains("/bye")) { return }

            context.addMessage("user", input)
            output = model.generateResponse(context.swizzleSpeaker("George"))
            cli.log("### The assistant says:\r\n${output}")
            context.addMessage("George", output)
            if (output.contains('```json')) {
                def cleaned = output.stripIndent().trim()
                def trimmed = cleaned.substring(cleaned.indexOf('{'), cleaned.lastIndexOf('}') + 1)
                def jsonMk2 = new JsonSlurper().parseText(trimmed)
                def canvas = new Illustrator()
                canvas.style = ImageType.SQUARE
                canvas.title = "Illustration"

                def prompt = canvas.getPrompt("${jsonMk2.prompt}, ${jsonMk2.style}")
                def img = canvas.generateImage(prompt)
                context.addMessage("system", "recipt:\r\n${img}")
                cli.log(img)
            }
            context.exportContext("Story/Chapter_0.json")
        }
    }

    cli.log("# Running test targets")
    testRoutines.each { name, test ->
        cli.log("## Running ${name} tests")
        test()
    }

    cli.log("# And this concludes the tests. If you can read this, it means everything 'passed'")
}

// Prompt for input to generate an image.
if (options.image) {
    def illustrator = new Illustrator()
    illustrator.style = ImageType.PORTRAIT
    illustrator.title = "ComfyUI"
    println "Prompt here:"
    def input = System.in.newReader().readLine()
    def prompt = illustrator.getPrompt(input)

    illustrator.generateImage(prompt)
}

// Get directly to a chat.
if (options.chat) {
    if (options.test) { return }
    def context = new Context()
    def georgePrompt = new File("Characters/George.prompt").text
    context.addMessage("system", "${georgePrompt}")
    def model = new Model(model: "narrator", body: context)

    while (true) {
        input = cli.waitForInput()
        if (input.contains("/bye")) { return }
        context.addMessage("Phiglit", input)
        def output = model.generateResponse(context.swizzleSpeaker("George"))
        cli.log("### Assistant:\r\n${output}")
        context.addMessage("George", output)
        context.exportContext("Story/Chat.json")
    }
}