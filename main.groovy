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

    def location
    def home
    def hero
    def narrator

    // Holds our testRoutines in a map
    // Note: we are loading closures, so we need
    // to run them in a loop later
    def testRoutines = [:]

    testRoutines["RNG"] = {

        def coin = new Coin() // 2 sided dice, ie: Coin
        println "### Picked up our lucky coin. It is showing:"
        println coin

        println "### Flipping it, we get this:"
        coin = coin.flip()
        println coin

        println "### Flipping it a bunch, we get this (in binary):"
        def flips = 8
        def results = []

        for (int i = 0; i < flips; i++) {
            def flip = coin.flip()
            def value = flip.val ? 1 : 0 // recast to "int"? stil a String, but now a numeral
            results.add(value)
        }
        println results.join()

        println "### Rolling a D20 on the desk:"
        def d20 = new Dice(DiceType.D20)
        println d20
    }

    testRoutines["Location"] = {
        location = new Location(0.00f, 0.00f, 0.00f)
        println "### Location:"
        println location

        home = new Poi(location, "Phiglit's House")
        home.description = "A cozy little cottage"
        println "### POI:"
        if (cli.logLevel == LogLevel.JSON) {
            println home.toJson()
        }
        if (cli.logLevel == LogLevel.INFO) {
            println home
        }
    }

    testRoutines["Character"] = {

        hero = new Character("Phiglit")

        hero.location = location
        hero.description = "The Protagonist"
        hero.bio = "A friendly neighborhood Code Wizard"
        println "### Our hero:"
        println hero.toJson()

        hero.armorType = ArmorType.LIGHT
        println "### After dawning his trousers, our hero is now:"
        println hero.toJson()

        println "### Putting ${hero.name} in the house."
        home.addOccupant(hero)
        println home.toJsonPretty()
    }

    testRoutines["Inventory"] = {

        def weapon = new Item("Staff of Justice", ItemType.WEAPON)
        weapon.description = "A wizards staff"
        hero.inventory.addItem(weapon)
        println "### With ${weapon.name} in hand, our hero now has this:"
        println hero.toJson()

        hero.inventory.useItem(weapon)
        println "### Our hero used ${weapon.name}, but it should still be in his inventory:"
        println hero.toJson()

        def potion = new Item("Healing potion", ItemType.CONSUMABLE)
        potion.description = "Some brain-sauce for the static tantrums"
        potion.stack = 3
        hero.inventory.addItem(potion)
        println "### After picking up a stack of ${potion.stack} ${potion.name}(s), our hero now has this:"
        println hero.toJson()

        hero.inventory.useItem(potion)
        println "### And after downing a breakfast of champions (${potion.name}), he is left with this:"
        println hero.toJson()

        def muffin = new Item("PoppySeed Muffin", ItemType.CONSUMABLE)
        muffin.description = "A lil' nibble for later"
        hero.inventory.addItem(muffin)
        println "### Our hero picked up a ${muffin.name}, which makes his character sheet this:"
        println hero.toJson()


        println "### Adding anything else to our bag should fail:"
        def straw = new Item("Straw", ItemType.CONSUMABLE)
        try {
            hero.inventory.addItem(straw)
        } catch (e) {
            println "### Yup. here is the error:"
            println e.getMessage()
        }

        println "### Using a potion:"
        hero.inventory.useItem(potion)
        println hero.toJson()

        println "### And another:"
        hero.inventory.useItem(potion)
        println hero.toJson()

        println "### And... another? (should fail):"
        try {
            hero.inventory.useItem(potion)
        } catch (e) {
            println "### Yup. With this error:"
            println e.getMessage()
        }

        println "### Finally, or hero ends up like this:"
        println hero.toJson()
    }

    testRoutines["Narrator"] = {
        narrator = new Model()
        println "### Saying good morning to the house."
        def input = [
            "You are located in:",
            home.toJson(),
            "You are joined by:",
            hero.toJson(),
            "They say: Good Morning. Would you please describe this location, it's occupants, and some details about the ${hero.name} character?"
        ].join('\r\n')
        def output = narrator.generateResponse(input)

        def data = new JsonSlurper().parseText(output)
        println data.choices?.message.content
    }

    println "# Running test targets"
    testRoutines.each { name, test ->
        println "## Running ${name} tests"
        test()
    }

    println "# And this concludes the tests. If you can read this, it means everything 'passed'"
}