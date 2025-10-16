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
        return output.join("\r\n")
    }
}

class Poi {
    String name
    String description
    Location location
    Inventory inventory
    Map<String, List<Character>> occupants
    int occupantsMax
    int occupantsCurrent
    float radius

    Poi(Location location, String name) {
        this.name = name
        this.description = description
        this.location = location
        this.inventory = new Inventory(name: name, slotsMax: 10)
        this.occupants = new HashMap<String, List<Character>>()
        this.occupantsMax = 10
        this.occupantsCurrent = 0
        this.radius = 10 // in meters? some units i guess
    }

    void addOccupant(Character occupant) {
        if (occupantsCurrent >= occupantsMax) {
            throw new RuntimeException("Occupants max reached. Cannot add new occupant.")
        }
        occupants.put(occupant.name, new ArrayList<Character>() {{ add(occupant) }})
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
            "Location:",
            "- name: ${name}",
            "  description: ${description}",
            "  latitude: ${location.lat}",
            "  longitude: ${location.lon}",
            "  altitude: ${location.alt}",
            "  inventory:\r\n${inventory}"
        ]
        return output.join("\r\n")
    }
}
