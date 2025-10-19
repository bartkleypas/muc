import groovy.json.JsonOutput

enum ArmorType {
    NA,
    LIGHT,
    MEDIUM,
    HEAVY,
    SUPER_HEAVY
}

class Character {
    String name
    String role
    String description
    String bio
    int health
    ArmorType armorType
    Inventory inventory
    Location location

    Character() {
        this.name = name
        this.role = "user"
        this.description = description
        this.bio = bio
        this.health = 10
        this.armorType = ArmorType.NA
        this.inventory = new Inventory() // Initialize the inventory
        this.location = location
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
