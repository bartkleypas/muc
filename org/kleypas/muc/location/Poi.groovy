package org.kleypas.muc.location

import groovy.json.JsonOutput

import org.kleypas.muc.inventory.Inventory

/**
 * A point of interest (POI) that can hold inventory and occupants.
 *
 * <p>Typical usage:
 * <pre>
 *     Poi entrance = new Poi(new Location(51.5, -0.1, 30), "Main Entrance")
 *     entrance.addOccupant(new Character(name: "Alice"))
 *     println(entrance.toJsonPretty())
 * </pre></p>
 */
class Poi {
    /** Human‑readable name of the POI. */
    String name
    /** Description of the POI. */
    String description
    /** Geographic location of the POI. */
    Location location
    /** Inventory container associated with the POI. */
    Inventory inventory
    /** List of occupant names currently present. */
    List<String> occupants
    /** Maximum number of occupants allowed. */
    int occupantsMax
    /** Current number of occupants. */
    int occupantsCurrent
    /** Influence radius in meters. */
    float radius

    /**
     * Constructs a {@code Poi} with the given location and name.
     *
     * @param location geographic coordinates
     * @param name human‑readable name
     */
    Poi(Location location, String name) {
        this.name = name
        this.description = description
        this.location = location
        this.inventory = new Inventory(name: name, slotsMax: 10)
        this.occupants = []
        this.occupantsMax = 10
        this.occupantsCurrent = 0
        this.radius = 10 // in meters
    }

    /**
     * Adds an occupant to the POI.
     *
     * @param occupant the {@code Character} to add
     * @throws RuntimeException if the maximum occupant count has been reached
     */
    void addOccupant(Character occupant) {
        if (occupantsCurrent >= occupantsMax) {
            throw new RuntimeException("Occupants max reached. Cannot add new occupant.")
        }
        occupants.add(occupant.name)
        occupantsCurrent++
    }

    /**
     * Removes an occupant from the POI.
     *
     * @param occupant the {@code Character} to remove
     * @throws RuntimeException if the occupant is not present
     */
    void removeOccupant(Character occupant) {
        if (!occupants.containsKey(occupant.name)) {
            throw new RuntimeException("Occupant not found.")
        }
        occupants.remove(occupant)
        occupantsCurrent--
    }

    /**
     * Serialises this {@code Poi} to JSON.
     *
     * @return compact JSON representation
     */
    String toJson() {
        def output = JsonOutput.toJson(this)
        return output
    }

    /**
     * Serialises this {@code Poi} to pretty‑printed JSON.
     *
     * @return pretty‑printed JSON representation
     */
    String toJsonPretty() {
        def output = JsonOutput.prettyPrint(JsonOutput.toJson(this))
        return output
    }

    public File exportPoi(String filePath) {
        final File outFile = new File(filePath)
        outFile.parentFile?.mkdirs()

        def output = this.toJsonPretty()
        outFile.text = output
        return outFile
    }

    /**
     * {@inheritDoc}
     *
     * @return a YAML‑style string representation of the POI
     */
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