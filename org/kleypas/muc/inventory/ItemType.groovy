package org.kleypas.muc.inventory

/**
 * Enumerates the categories of items that can exist in the game.
 * <p>Each constant represents a logical group that can influence
 * how items are handled by the inventory system and game mechanics.</p>
 */
enum ItemType {
    /**
     * A functional object that can be used to interact with the environment
     * (e.g., a pick‑axe, wrench, or lock‑picks).
     */
    TOOL,

    /**
     * Protective gear that can be equipped by a character.
     */
    ARMOR,

    /**
     * Cosmetic or symbolic items that usually have no direct effect
     * on gameplay (e.g., jewelry, amulets).
     */
    TRINKET,

    /**
     * Consumable items that are used once or multiple times
     * and then removed from the inventory.
     */
    CONSUMABLE,

    /**
     * Items that are required to complete quests or
     * trigger narrative events.
     */
    QUEST
}
