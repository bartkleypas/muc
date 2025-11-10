
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File

enum ArmorType {
    NA,
    LIGHT,
    MEDIUM,
    HEAVY,
    SUPER_HEAVY
}

class Character {
    String name
    String description
    String bio
    int health
    ArmorType armorType
    Inventory inventory
    Location location

    Character() {
        this.name = name
        this.description = description
        this.bio = bio
        this.health = 10
        this.armorType = ArmorType.NA
        this.inventory = new Inventory() // Initialize the inventory
        this.location = location
    }

    File exportCharacterSheet(String filePath) {
        File outFile = new File(filePath)
        outFile.parentFile?.mkdirs()
        outFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(this))
        return outFile
    }

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

    String toJson() {
        def output = JsonOutput.toJson(this)
        return output
    }

    String toJsonPretty() {
        def output = JsonOutput.prettyPrint(JsonOutput.toJson(this))
        return output
    }

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
