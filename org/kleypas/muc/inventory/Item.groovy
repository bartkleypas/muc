package org.kleypas.muc.inventory

/**
 * Represents a single in‑world item.
 * <p>Each {@code Item} has a name, type, description, stack size and weight.
 * The type is represented by the {@link ItemType} enum.  By default an
 * item is created with a stack size of {@code 1} and a weight of {@code 1},
 * which makes it suitable for most consumables and light gear.</p
 */
class Item {
    String name
    ItemType type
    String description
    int stack
    int weight

    /**
     * Constructs a new {@code Item} with the supplied name and type.
     * The description is left {@code null} until explicitly set, and
     * default values for {@code stack} and {@code weight} are {@code 1}.
     *
     * @param name the display name of the item
     * @param type the {@link ItemType} categorising the item
     */
    Item(String name, ItemType type) {
        this.name = name
        this.type = type
        this.description = description
        this.stack = 1   // sensible default for most items
        this.weight = 1  // sensible default for most items
    }
}
