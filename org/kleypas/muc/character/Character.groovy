package org.kleypas.muc.character

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File

import org.kleypas.muc.inventory.Inventory
import org.kleypas.muc.inventory.Item
import org.kleypas.muc.inventory.ItemType

import org.kleypas.muc.location.Location

/**
 * Represents a player or non‑player character in the game world.
 * <p>A {@code Character} has a name, description, biography, health,
 * armor type, an {@link Inventory} for carrying items, and a current
 * {@link Location}.  The class also provides helpers to persist
 * and restore the character state to and from JSON files.</p>
 */
class Character {
    /**
     * Display name of the character.
     */
    String name

    /**
     * Short description (e.g. “A wandering mage”).
     */
    String description

    /**
     * Longer biography text.
     */
    String bio

    /**
     * Current health points.
     */
    int health

    /**
     * Armor weight class the character is wearing.
     */
    ArmorType armorType

    /**
     * The character’s inventory container.
     */
    Inventory inventory

    /**
     * Current spatial location of the character.
     */
    Location location

    /**
     * Creates a new {@code Character} with default values.
     * The constructor assigns the supplied instance variables
     * directly, but any uninitialised field will retain the
     * default value from the language (e.g. {@code null} for
     * objects, {@code 0} for primitives).
     */
    Character() {
        this.name = name
        this.description = description
        this.bio = bio
        this.health = 10
        this.armorType = ArmorType.NA
        this.inventory = new Inventory() // Initialize the inventory
        this.location = location
    }

    /**
     * Exports the character state to a JSON file.
     *
     * @param filePath the absolute or relative file path to write
     * @return the {@link File} object pointing at the created file
     */
    File exportCharacterSheet(String filePath) {
        File outFile = new File(filePath)
        outFile.parentFile?.mkdirs()
        outFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(this))
        return outFile
    }

    /**
     * Reads a character state from a JSON file and returns a new
     * {@code Character} instance populated with that data.
     *
     * @param filePath the path to the JSON file
     * @return a fully initialised {@code Character} based on the file
     * @throws AssertionError if the file does not exist
     */
    Character importCharacterSheet(String filePath) {
        File jsonFile = new File(filePath)
        assert jsonFile.exists()

        def slurper = new JsonSlurper()
        def data = slurper.parse(jsonFile)

        Character c = new Character()
        c.name = data.name
        c.description = data.description
        c.bio = data.bio
        c.health = data.health
        c.armorType = ArmorType.valueOf(data.armorType ?: "NA")

        if (data.location) {
            c.location = new Location(data.location.lat, data.location.lon, data.location.alt)
        }

        if (data.inventory) {
            Inventory inv = new Inventory()
            inv.name = data.inventory.name
            inv.slotsMax = data.inventory.slotsMax

            data.inventory.items?.each { itemName, itemList ->
                itemList.each { itemMap ->
                    Item item = new Item(itemMap.name, ItemType.valueOf(itemMap.type))
                    item.stack = itemMap.stack
                    inv.addItem(item)
                }
            }
            c.inventory = inv
        }
        return c
    }

    /**
     * Serialises this {@code Character} into a JSON string.
     *
     * @return a JSON representation of the character
     */
    String toJson() {
        def output = JsonOutput.toJson(this)
        return output
    }

    /**
     * Serialises this {@code Character} into a pretty‑printed JSON string.
     *
     * @return a formatted JSON representation of the character
     */
    String toJsonPretty() {
        def output = JsonOutput.prettyPrint(JsonOutput.toJson(this))
        return output
    }

    /**
     * Returns a human‑readable representation of the character,
     * listing key fields and the inventory contents in a YAML‑style format.
     *
     * @return a multi‑line string describing the character
     */
    @Override
    String toString() {
        def out = [
            "- name: ${name}",
            "  description: ${description}",
            "  bio: ${bio}",
            "  health: ${health}",
            "  armor: ${armorType}",
            "  location: ${location}",
            "  inventory:",
        ]
        out.add("  - ${inventory.name}")
        def inv = inventory.items.each { name, item ->
            out.add("    - ${name}")
        }
        return out.join('\r\n')
    }
}
