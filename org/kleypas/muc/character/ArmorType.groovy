package org.kleypas.muc.character

/**
 * Enumerates the different armor weight classes available in the game.
 * <p>These values are used by the {@link org.kleypas.muc.character.Character} logic to determine
 * movement speed, protection, and any special abilities that depend on armor type.</p>
 */
enum ArmorType {
    /**
     * No armor equipped. The character has no protection bonus and can move at full speed.
     */
    NA,

    /**
     * Light armor. Provides minimal protection but keeps the character agile.
     */
    LIGHT,

    /**
     * Medium armor. Offers a balanced trade‑off between protection and mobility.
     */
    MEDIUM,

    /**
     * Heavy armor. Grants substantial protection at the cost of speed.
     */
    HEAVY,

    /**
     * Super heavy armor. Maximum protection, typically with significant movement penalties.
     */
    SUPER_HEAVY
}
