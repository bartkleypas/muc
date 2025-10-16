import groovy.json.JsonOutput

class Inventory {
    String name
    int slotsMax
    int slotsOccupied
    Map<String, List<Item>> items

    Inventory() {
        this.name = "Bag of Holding"
        this.slotsMax = 3
        this.slotsOccupied = 0
        this.items = new HashMap<String, List<Item>>();
    }

    void addItem(Item item) {
        if (slotsOccupied >= slotsMax) {
            throw new RuntimeException("Inventory Full")
        }
        items.put(item.name, new ArrayList<Item>() {{ add(item); }});
        slotsOccupied++
    }

    void removeItem(Item item) {
        if (!items.containsKey(item.name)) {
            throw new RuntimeException("Nothing there, boss.")
        }
        items.remove(item.name)
        slotsOccupied--
    }

    void useItem(Item item) {
        if (!items.containsKey(item.name)) {
            throw new RuntimeException("Nothing there, boss.")
        }
        if (item.type != ItemType.CONSUMABLE) {
            return
        }
        if (item.stack <= 1) {
            println "Exhausted stock of ${item.name}. Removing from bag."
            removeItem(item)
            return
        }
        item.stack--
    }

    String toJson() {
        def output = JsonOutput.toJson(this)
        return output
    }

    String toJsonPretty() {
        def output = JsonOutput.prettyPrint(JsonOutput.toJson(this))
    }

    @Override
    String toString() {
        def output = []
        output.add("  - name: ${name}")
        output.add("    size: ${slotsMax}")
        output.add("    occupied: ${slotsOccupied}")
        items.each { name, item ->
            output.add("    - item: ${name}")
            output.add("      stack: ${item.stack}")
        }
        return output.join("\r\n")
    }
}