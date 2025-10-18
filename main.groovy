#!/usr/bin/env groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import Cli
import Character
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

    def location = new Location(0.00f, 0.00f, 0.00f)
    def home
    testRoutines["Location"] = {
        def out = []
        home = new Poi(location, "Phiglit's House")

        home.description = "A cozy little cottage"
        out.add("### POI:")
        out.add(home.toString())
        cli.log(out.join('\r\n'))
    }

    def phiglit
    testRoutines["Character"] = {
        def out = []
        out.add("### A Hero:")

        def hero = new Character("Phiglit")
        hero.location = location
        hero.description = "The Old and wise Wizard"
        hero.bio = "A friendly neighborhood Code Wizard"

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

    def sally
    testRoutines["Inventory"] = {
        def out = []
        out.add("### A Hero:")

        def hero = new Character("Sally")
        hero.description = "A Natural Wonder"
        hero.bio = "A friendly neighborhood Gardener"
        hero.armorType = ArmorType.LIGHT
        hero.location = location

        out.add("### And she comes prepared, with:")
        def tool = new Item("Shovel of Fortitude", ItemType.TOOL)
        tool.description = "A sturdy and well used shovel"
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

        def muffin = new Item("PoppySeed Muffin", ItemType.CONSUMABLE)
        muffin.description = "A lil' nibble for later"
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

        home.addOccupant(hero)

        sally = hero
        cli.log(out.join('\r\n'))
    }

    def george
    def story
    testRoutines["Narrator"] = {
        def hero = new Character("George")
        hero.description = "The heart of the home."
        hero.bio = "A chatbot, built by hand and raised by Phiglit. Designed to be the custodian of the Phiglit household, and its interpreter."
        home.addOccupant(hero)
        def model = new Model()
        def input = "Good morning. Would you please describe yourself, this location, and some details about it's occupants?"
        def context = [
            "You are:",
            hero.toJson(),
            "You are located in:",
            home.toJson(),
            "You are joined by:",
            phiglit.toJson(),
            sally.toJson(),
            "They say:",
            input
        ]

        cli.log("### Saying:")
        cli.log(input)
        def prompt = context.join("\r\n")
        def data = new JsonSlurper().parseText(model.generateResponse(prompt))
        def output = data.choices[0].message.content
        context.add("You said:")
        context.add(output)
        cli.log("### Model responded:")
        cli.log(output)

        input = "Please describe the house in more detail, and feel free to be creative."
        context.add("They say:")
        context.add(input)
        cli.log("### We respond:")
        cli.log(input)
        prompt = context.join("\r\n")
        data = new JsonSlurper().parseText(model.generateResponse(prompt))
        output = data.choices[0].message.content
        context.add("You said:")
        context.add(output)

        story = context
        george = hero

        cli.log("### Model responded:")
        cli.log(output)
    }

    cli.log("# Running test targets")
    testRoutines.each { name, test ->
        cli.log("## Running ${name} tests")
        test()
    }

    println "# And this concludes the tests. If you can read this, it means everything 'passed'"
}