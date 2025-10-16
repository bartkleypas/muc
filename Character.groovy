import groovy.json.JsonOutput

enum ArmorType {
    NAKED,
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

    Character(String name) {
        this.name = name
        this.description = description
        this.bio = bio
        this.health = 10
        this.armorType = ArmorType.NAKED
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
        def output = [
            "Character:",
            "- name: ${name}",
            "  description: ${description}",
            "  bio: ${bio}",
            "  health: ${health}",
            "  armor: ${armorType}",
            "  location: ${location.lat}, ${location.lon}, ${location.alt}",
            "  inventory:",
            inventory
        ]
        return output.join('\r\n')
    }
}
