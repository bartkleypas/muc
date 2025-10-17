import groovy.json.JsonOutput

class Location {
    float lat
    float lon
    float alt

    Location(float lat, float lon, float alt) {
        this.lat = lat
        this.lon = lon
        this.alt = alt
    }

    @Override
    String toString() {
        def output = [
            "lat: ${lat}",
            "lon: ${lon}",
            "alt: ${alt}"
        ]
        return output.join(", ")
    }
}

class Poi {
    String name
    String description
    Location location
    Inventory inventory
    List<String> occupants
    int occupantsMax
    int occupantsCurrent
    float radius

    Poi(Location location, String name) {
        this.name = name
        this.description = description
        this.location = location
        this.inventory = new Inventory(name: name, slotsMax: 10)
        this.occupants = []
        this.occupantsMax = 10
        this.occupantsCurrent = 0
        this.radius = 10 // in meters? some units i guess
    }

    void addOccupant(Character occupant) {
        if (occupantsCurrent >= occupantsMax) {
            throw new RuntimeException("Occupants max reached. Cannot add new occupant.")
        }
        occupants.add(occupant.name)
        occupantsCurrent++
    }

    void removeOccupant(Character occupant) {
        if (!occupants.containsKey(occupant.name)) {
            throw new RuntimeException("Occupant not found.")
        }
        occupants.remove(occupant)
        occupantsCurrent--
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
            "- name: ${name}",
            "  description: ${description}",
            "  location: ${location}",
            "  inventory:\r\n${inventory}",
            "  occupants:",
            "    max: ${occupantsMax}",
            "    occ: ${occupantsCurrent}"
        ]
        occupants.each { output.add("    - ${it}") }
        return output.join("\r\n")
    }
}
