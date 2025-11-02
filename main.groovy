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
        home = new Poi(location, "Phiglit's and Epwna's Flat")

        home.description = "A cluttered apartment in downtown Night City."
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
        hero.description = "The Code Wizard"
        hero.bio = "Phiglit is a cybernetically enhanced human male programmer, well versed in esoteric scripts and languages. Always accompanied by his owl assistant, George."
        hero.location = location

        out.add(hero.toString())

        hero.armorType = ArmorType.LIGHT
        out.add("### After putting on his trousers and robe, our hero is now:")
        out.add(hero.toString())

        def tool = new Item("Cyberdeck of Justice", ItemType.TOOL)
        tool.description = "A rugged and powerful wrist mounted Cyberdeck, used for sending instructions to summoned chatbots."
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
        hero.description = "The Crafty Tinkerer"
        hero.bio = "An all natural and organic female human. Aproximately 5 feet tall. She is wearing pink overalls, and has a short yellow and red pixie haircut. Enjoys designing new robots and sees herself as a sculpter more than a technologist, taking inspiration from nature and especially birds. Attentive mother figure of Rosie."
        hero.armorType = ArmorType.LIGHT
        hero.location = location
        home.addOccupant(hero)

        out.add(hero.toString())

        out.add("### And she comes prepared, with:")
        def tool = new Item("Calipers of Truth", ItemType.TOOL)
        tool.description = "An accurate set of calipers, useful for measuring all sorts of things."
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
        cli.log(out.join('\r\n'))
    }

    // Ok. Now stuff gets complicated, and time consuming.
    // George is going to be building up some story context for us.
    def george
    testRoutines["Narrator"] = {
        // We know this stuff. George needs a character sheet
        def hero = new Character(name: "George", role: "assistant")
        hero.description = "The Narrator"
        hero.bio = "An 12 inch tall technology-packed robot owl, made of glossy plastic and brushed aluminum with neon glowing LED accents. Built by Epwna and programmed by Phiglit to be an attentive assistant. Usually found on Phiglits shoulder."

        def pen = new Item("A Pen of Writing", ItemType.TOOL)
        pen.description = "An ornate and efficient fountain pen, mostly decorative but still symbolic, used to narrate an ongoing story."
        hero.inventory.addItem(pen)
        home.addOccupant(hero)

        // Now the fun stuff. Interacting with our hero
        def context = new MessageBody(
            new ArrayList<>(),
            "narrator",
            false,
            0.7
        )
        context.addMessage("user", "You are:\r\n${hero.toJson()}")
        context.addMessage("user", "You are located here:\r\n${home.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${phiglit.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${epwna.toJson()}")
        def input = "Good morning ${hero.name}. My name is ${phiglit.name}. Would you please describe yourself, where you are, and who you are with? Please be as detailed as you wish."
        context.addMessage("user", "${input}")

        cli.log("### ${phiglit.name} says:\r\n${input}")

        def model = new Model(body: context)
        def resp = model.generateResponse(context)

        def data = new JsonSlurper().parseText(resp)
        def output = data.choices[0].message.content
        cli.log("### ${hero.name} says:\r\n${output}")
        context.addMessage("assistant", "${output}")

        george = hero
    }

    // more... MORE... *MORE*
    // Ok. This gets a bit nuts, but basically we are feeding a prompt with a similar context as what was fed
    // into George. Basically we do what we did before, except this time, with some existing context (story)!
    // Then, we feed Rosie with some instructions for generating StableDiffusion inputs.
    // Electric Sheep indeed.
    def rosie
    testRoutines["Illustrator"] = {
        def hero = new Character(name: "Rosie", role: "assistant")
        hero.description = "The Illustrator"
        hero.bio = "A tiny robotic hummingbird that would fit in the palm of your hand. No more than 6 inches tall, and built by Epwna and programmed by Phiglit to assist with illistrating and visual story telling. Usually found hovering somewhere close to Epwna."

        def brush = new Item("Paintbrush of Illusion", ItemType.TOOL)
        brush.description = "An ornate and detailed paintbrush, mostly decorative but still symbolic, used for illustrating an ongoing story."
        hero.inventory.addItem(brush)
        home.addOccupant(hero)

        def context = new MessageBody(
            new ArrayList<>(),
            "illustrator",
            false,
            0.7
        )
        context.addMessage("user", "You are:\r\n${hero.toJson()}")
        context.addMessage("user", "You are located in:\r\n${home.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${phiglit.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${epwna.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${george.toJson()}")

        def model = new Model(body: context)
        def input = "Good morning to you ${hero.name}, and welcome. I'm ${epwna.name}. Would you please tell us about yourself?"
        cli.log("### ${epwna.name} says:\r\n${input}")
        context.addMessage("user", "${input}")
        def response = model.generateResponse(context)
        def data = new JsonSlurper().parseText(response)
        def output = data.choices[0].message.content

        context.addMessage("assistant", "${output}")
        cli.log("### ${hero.name} says:\r\n${output}")

        // Rosie, please generate some portraits.
        home.occupants.each { name ->
            def str = "Thank you ${hero.name}. Would you please create an image of ${name}?"
            if (name == hero.name) {
                str = "Thank you ${name}. Would you please create a image of yourself, ${name}?"
            }
            def imgContext = context
            imgContext.addMessage("user", "${str}")
            cli.log("### The user says:\r\n${str}")
            def resp = model.generateResponse(imgContext)

            def jsonOutput = new JsonSlurper().parseText(resp)
            def out = jsonOutput.choices[0].message.content
            imgContext.addMessage("assistant", "${out}")
            cli.log("### ${hero.name} responds:\r\n${out}")

            def cleaned = out.stripIndent().trim()

            if (!cleaned.contains('```json')) {
                cli.log("!!! No json data boss...")
                return
            }

            def trimmed = cleaned.substring(cleaned.indexOf('{'), cleaned.lastIndexOf('}') + 1)

            def parser = new JsonSlurper(type: JsonParserType.LAX)
            def json = parser.parseText(trimmed)

            def art = new Illustrator()
            art.style = ImageType.PORTRAIT
            art.title = "${name}"
            imgPrompt = art.getPrompt(json.prompt)

            def ret = art.generateImage(imgPrompt)
            imgContext.addMessage("system", "recipt:\r\n${ret}")
            cli.log(ret)
        }

        // Finally, a picture of our home.
        input = "That looks wonderful! Now, would you please create an image of our home, ${home.name}?"
        context.addMessage("user", "${input}")
        cli.log("### ${epwna.name} says:\r\n${input}")
        response = model.generateResponse(context)
        data = new JsonSlurper().parseText(response)
        output = data.choices[0].message.content
        context.addMessage("assistant", "${output}")
        cli.log("### ${hero.name} says:\r\n${output}")

        cleaned = output.stripIndent().trim()
        assert cleaned.contains('```json')

        trimmed = cleaned.substring(cleaned.indexOf('{'), cleaned.lastIndexOf('}') + 1)
        json = new JsonSlurper(type: JsonParserType.LAX).parseText(trimmed)

        canvas = new Illustrator()
        canvas.style = ImageType.LANDSCAPE
        canvas.title = "${home.name}"
        prompt = canvas.getPrompt(json.prompt)

        def img = canvas.generateImage(prompt)
        context.addMessage("system", "recipt:\r\n${img}")
        cli.log(img)
        rosie = hero
    }

    testRoutines["Story"] = {
        def context = new MessageBody(
            new ArrayList<>(),
            "narrator",
            false,
            0.7
        )

        context.addMessage("user", "You are:\r\n${george.toJson()}")
        context.addMessage("user", "You are located in:\r\n${home.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${phiglit.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${epwna.toJson()}")
        context.addMessage("user", "You are joined by:\r\n${rosie.toJson()}")
        def model = new Model(body: context)

        context.addMessage("user", "Ok Narrator ${george.name}. Would you please continue the story? Please take us on the next chapter, and we will pick up where you leave off.")
        def resp = model.generateResponse(context)
        def data = new JsonSlurper().parseText(resp)
        def output = data.choices[0].message.content
        context.addMessage("assistant", output)
        story = context
        cli.log("### ${george.name} says:\r\n${output}")

        // If we don't have a -c (--chat) arg, return. otherwise continue the
        // story!
        if (!options.chat) { return }
        while (true) {
            def input = cli.waitForInput()
            if (input.contains("/bye")) { return }
            if (input.startsWith("Rosie")) {
                context.model = "illustrator"
                model.model = "illustrator"
            } else {
                context.model = "narrator"
                model.model = "narrator"
            }
            context.addMessage("user", input)
            resp = model.generateResponse(context)
            data = new JsonSlurper().parseText(resp)
            output = data.choices[0].message.content
            cli.log("### The assistant says:\r\n${output}")
            context.addMessage("assistant", output)
            if (output.contains('```json')) {
                def cleaned = output.stripIndent().trim()
                def trimmed = cleaned.substring(cleaned.indexOf('{'), cleaned.lastIndexOf('}') + 1)
                def jsonMk2 = new JsonSlurper(type: JsonParserType.LAX).parseText(trimmed)
                def canvas = new Illustrator()
                canvas.style = ImageType.SQUARE
                canvas.title = "Illustration"

                def prompt = canvas.getPrompt(jsonMk2.prompt)
                def img = canvas.generateImage(prompt)
                context.addMessage("system", "recipt:\r\n${img}")
                cli.log(img)
            }
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
    def context = new MessageBody(
        new ArrayList<>(),
        "narrator",
        false,
        0.7
    )
    def model = new Model(body: context, model: "narrator")

    while (true) {
        input = cli.waitForInput()
        if (input.contains("/bye")) { return }
        context.addMessage("user", input)
        def response = model.generateResponse(context)
        def json = new JsonSlurper().parseText(response)
        def output = json.choices[0].message.content
        cli.log("### Assistant:\r\n${output}")
        context.addMessage("assistant", output)
        if (output.contains('```json')) {
            def cleaned = output.stripIndent().trim()
            def trimmed = cleaned.substring(cleaned.indexOf('{'), cleaned.lastIndexOf('}') + 1)
            def jsonMk2 = new JsonSlurper(type: JsonParserType.LAX).parseText(trimmed)
            def canvas = new Illustrator()
            canvas.style = ImageType.PORTRAIT
            canvas.title = "Testing"

            def prompt = canvas.getPrompt(json.prompt)
            def img = canvas.generateImage(prompt)
            context.addMessage("system", "recipt:\r\n${img}")
            cli.log(img)
        }
    }
}