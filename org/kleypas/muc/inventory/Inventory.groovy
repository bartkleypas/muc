package org.kleypas.muc.inventory

import groovy.json.JsonOutput

/**
 * Represents a simple inventory container that can hold items
 * up to a maximum number of slots. Each slot may contain a
 * list of items sharing the same name. The class provides
 * basic CRUD operations and JSON serialization helpers.
 */
class Inventory {
    String name
    int slotsMax
    int slotsOccupied
    Map<String, List<Item>> items

    /**
     * Constructs a new {@code Inventory} with default settings.
     * The default inventory is a "Bag of Holding" that can
     * hold up to {@code slotsMax} distinct item names.
     */
    Inventory() {
        this.name = "Bag of Holding"
        this.slotsMax = 20
        this.slotsOccupied = 0
        this.items = new HashMap<String, List<Item>>();
    }

    /**
     * Adds an {@code Item} to the inventory. If the inventory
     * is full (i.e., {@code slotsOccupied} >= {@code slotsMax}),
     * a {@code RuntimeException} is thrown.
     *
     * @param item the {@code Item} to add
     * @throws RuntimeException if the inventory has reached its maximum capacity
     */
    void addItem(Item item) {
        if (slotsOccupied >= slotsMax) {
            throw new RuntimeException("Inventory Full")
        }
        items.put(item.name, new ArrayList<Item>() {{ add(item); }});
        slotsOccupied++
    }

    /**
     * Removes an {@code Item} from the inventory. The method
     * checks for the presence of the item name and throws an
     * exception if the item is not found.
     *
     * @param item the {@code Item} to remove
     * @throws RuntimeException if the item is not present
     */
    void removeItem(Item item) {
        if (!items.containsKey(item.name)) {
            throw new RuntimeException("Nothing there, boss.")
        }
        items.remove(item.name)
        slotsOccupied--
    }

    /**
     * Uses an {@code Item} if it is consumable. If the item
     * stack count is greater than one, the stack is decremented.
     * Otherwise the item is removed from the inventory.
     *
     * @param item the {@code Item} to use
     * @throws RuntimeException if the item is not present in the inventory
     */
    String useItem(Item item) {
        if (!items.containsKey(item.name)) {
            throw new RuntimeException("Nothing there, boss.")
        }
        switch(item.type) {
            case ItemType.CONSUMABLE:
                handleConsumable(item)
                break
            case ItemType.TOOL:
                executeToolLogic(item)
                break
            default:
                println "You brandish the ${item.name} meaningfully."
        }
    }

    void handleConsumable(Item item) {
        if (item.stack <= 1) {
            removeItem(item)
            return
        }
        item.stack--
    }

    String executeToolLogic(Item item) {
        def action = item.metadata.get("action")

        if (!action) {
            throw new RuntimeException("This tool is broken; no metadata found boss.")
        }

        println "Character is activating ${item.name}, trying to do ${action} action..."
        def sout = new StringBuilder(), serr = new StringBuilder()
        def proc = action.execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(1000)
        item.metadata.result = sout.toString()
        return sout.toString()
    }

    /**
     * Serializes the inventory into a JSON string.
     *
     * @return a JSON representation of the inventory
     */
    String toJson() {
        def output = JsonOutput.toJson(this)
        return output
    }

    /**
     * Serializes the inventory into a pretty‑printed JSON string.
     *
     * @return a formatted JSON representation of the inventory
     */
    String toJsonPretty() {
        def output = JsonOutput.prettyPrint(JsonOutput.toJson(this))
    }

    /**
     * Returns a human‑readable string representation of the inventory,
     * listing its name, slot limits, and contained items with stack counts.
     *
     * @return a string describing the inventory contents
     */
    String toMd() {
        def output = []
        output.add("  - name: ${name}")
        output.add("    slots: ${slotsMax}")
        output.add("    taken: ${slotsOccupied}")
        items.each { name, item ->
            output.add("    - item: ${name}")
            output.add("      stack: ${item.stack}")
        }
        return output.join("\r\n")
    }
}