import groovy.json.JsonOutput

enum ItemType {
    TOOL,
    ARMOR,
    TRINKET,
    CONSUMABLE,
    QUEST
}

class Item {
    String name
    ItemType type
    String description
    int stack
    int weight

    Item(String name, ItemType type) {
        this.name = name
        this.type = type
        this.description = description
        this.stack = 1 // Sensible default i think?
        this.weight = 1 // This too.
    }
}
