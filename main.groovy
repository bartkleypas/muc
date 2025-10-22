#!/usr/bin/env groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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
        home = new Poi(location, "Phiglit's House")

        home.description = "A cheerful little cottage."
        out.add("### POI:")
        out.add(home.toString())
        cli.log(out.join('\r\n'))
    }

    // Fill it with characters (should still be quick)
    def phiglit
    testRoutines["Character"] = {
        def out = []
        out.add("### A Hero:")

        def hero = new Character(name: "Phiglit", role: "user")
        hero.description = "The Old and Wise Wizard"
        hero.bio = "He is a friendly neighborhood human Wizard, well versed in esoteric spells and languages."
        hero.location = location

        out.add(hero.toString())

        hero.armorType = ArmorType.LIGHT
        out.add("### After putting on his trousers and robe, our hero is now:")
        out.add(hero.toString())

        def tool = new Item("Staff of Justice", ItemType.TOOL)
        tool.description = "A wizards staff"
        hero.inventory.addItem(tool)
        out.add("### With ${tool.name} in hand, our hero now has this:")
        out.add(hero.toString())

        home.addOccupant(hero)

        phiglit = hero
        cli.log(out.join('\r\n'))
    }

    // Epwna does a lot of inventory testing for us
    def epwna
    testRoutines["Inventory"] = {
        def out = []
        out.add("### A Hero:")

        def hero = new Character(name: "Epwna", role: "user")
        hero.description = "A skilled hobbit carpenter."
        hero.bio = "She makes just the right magical tools, and shares them with everyone to use."
        hero.armorType = ArmorType.LIGHT
        hero.location = location
        home.addOccupant(hero)

        out.add("### And she comes prepared, with:")
        def tool = new Item("Chisel of Glory", ItemType.TOOL)
        tool.description = "A permanantly sharp and well worn chisel, that is always the best tool for the job."
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

        out.add("### Finally, our hero ends up like this:")
        out.add(hero.toString())

        epwna = hero
        cli.log(out.join('\r\n'))
    }

    // Ok. Now stuff gets complicated, and time consuming.
    // George is going to be building up some story context for us.
    def george
    def story
    testRoutines["Narrator"] = {
        // We know this stuff. George needs a character sheet
        def hero = new Character(name: "George", role: "assistant")
        hero.description = "The Narrator"
        hero.bio = "He is a talented author and biographer, built by hand and raised by Phiglit. Designed to be the custodian of the Phiglit household, and its interpreter."
        def pen = new Item("A Pen of Writing", ItemType.TOOL)
        pen.description = "An ornate and efficient fountain pen, used to narrate an ongoing story."
        hero.inventory.addItem(pen)
        home.addOccupant(hero)

        // Add a lil' backstory
        def context = [
            "You are:",
            hero.toJson(),
            "You are located in:",
            home.toJson(),
            "You are joined by:",
            phiglit.toJson(),
            epwna.toJson(),
        ]

        // Now the fun stuff. Interacting with our hero
        def model = new Model()

        def input = "Good morning ${hero.name}. My name is ${phiglit.name}. Would you please describe yourself in detail, including any gear you have on hand?"
        cli.log("### ${phiglit.name} says:\r\n${input}")
        context.add(input)

        // Smash the context together into a multi-line string
        def prompt = context.join("\r\n")

        // Feed the prompt to the model, and transform its response out of the json.
        def data = new JsonSlurper().parseText(model.generateResponse(prompt))
        def output = data.choices[0].message.content
        context.add(output)
        cli.log("### ${hero.name} says:\r\n${output}")

        story = context
        george = hero
    }

    // more... MORE... *MORE*
    // Ok. This gets a bit nuts, but basically we are feeding a prompt with a similar context as what was fed
    // into George. Adding the story confuses the bots identity... Anyway. Basically we do what we did before
    // and feed Rosie with some instructions for generating StableDiffusion inputs.
    // Electric Sheep indeed.
    def rosie
    testRoutines["Illustrator"] = {
        def hero = new Character(name: "Rosie", role: "assistant")
        hero.description = "A Creative Artist"
        hero.bio = "She is an accomplished artist, built by hand and raised by Epwna. She is fluent in prompts used to paint on a digital canvas."

        def brush = new Item("Paintbrush of Illusion", ItemType.TOOL)
        brush.description = "An elegent and efficient paintbrush, used to illustrate an ongoing story."
        hero.inventory.addItem(brush)
        home.addOccupant(hero)

        def context = [
            "You are:",
            hero.toJson(),
            "You are located in:",
            home.toJson(),
            "You are joined by:",
            phiglit.toJson(),
            epwna.toJson(),
            george.toJson(),
        ]

        def model = new Model()
        def input = "Good morning to you ${hero.name}, and welcome. I'm ${george.name}. Would you please describe yourself, and any gear you might have on hand?"
        cli.log("### ${george.name} says:\r\n${input}")

        context.add(input)
        def prompt = context.join("\r\n")
        data = new JsonSlurper().parseText(model.generateResponse(prompt))
        output = data.choices[0].message.content

        context.add(output)
        cli.log("### ${hero.name} says:\r\n${output}")

        // Rosie, please generate some portraits.
        home.occupants.each { name ->
            def str = "Thank you. Would you please write a prompt for ComfyUI to generate a portrait of ${name}? Please tailor your descriptions as appropriate for the Dreamshaper model, and output your response as json."
            if (name == hero.name) {
                str = "Thank you ${hero.name}. Would you please write a prompt for ComfyUI to generate a picture of yourself in the ${home.name}? Please tailor your description as appropriate for the Dreamshaper model, and output your response as json."
            }
            def imgContext = []
            imgContext.addAll(context)
            imgContext.add(str)
            cli.log("### ${name} says:\r\n${str}")
            def imgPrompt = imgContext.join("\r\n")
            def resp = new JsonSlurper().parseText(model.generateResponse(imgPrompt))
            def out = resp.choices[0].message.content
            cli.log(out)

            def lol = out.readLines()
            def tidy = lol.size() > 1 ? lol.subList(1, lol.size() -1) : []
            out = new JsonSlurper().parseText(tidy.join())

            def art = new Illustrator()
            art.style = ImageType.PORTRAIT
            art.title = "${name}"
            imgPrompt = art.getPrompt(out.prompt)

            def ret = art.generateImage(imgPrompt)
            cli.log(ret)
        }

        // Finally, a picture of our home.
        input = "That looks wonderful. Would you please write a prompt for ComfyUI to generate an image of our home, ${home.name}? Please tailor your description as appropriate for the Dreamshaper model. Please output your response as json."
        context.add(input)
        cli.log("### George says:\r\n${input}")
        prompt = context.join("\r\n")
        data = new JsonSlurper().parseText(model.generateResponse(prompt))
        output = data.choices[0].message.content
        cli.log(output)

        lines = output.readLines()
        cleaned = lines.size() > 1 ? lines.subList(1, lines.size() -1) : []
        data = new JsonSlurper().parseText(cleaned.join())

        canvas = new Illustrator()
        canvas.style = ImageType.LANDSCAPE
        canvas.title = "${home.name}"
        prompt = canvas.getPrompt(data.prompt)

        cli.log(canvas.generateImage(prompt))

        story = context
        rosie = hero
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