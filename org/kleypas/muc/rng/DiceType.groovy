package org.kleypas.muc.rng

/**
 * Enumerates the supported die types used by {@link Dice}.  Each constant
 * represents a die with a fixed number of faces that can be derived from
 * its name (e.g. {@code D6} → 6 faces).  The enum keeps the face count in
 * {@link #faces} for quick lookup.
 *
 * <p>Although {@code D2} is technically a coin, it is retained here for
 * consistency with the {@link Coin} class that treats a 2‑faced die as a
 * special case.</p>
 */
enum DiceType {
    /** Two‑faced die, but we also have a unique class for a Coin. */
    D2,
    /** Four‑faced die. */
    D4,
    /** Six‑faced die. */
    D6,
    /** Eight‑faced die. */
    D8,
    /** Ten‑faced die. */
    D10,
    /** Twelve‑faced die. */
    D12,
    /** Twenty‑faced die. */
    D20

    /** Number of faces for the specific {@code DiceType}. */
    int faces

    /**
     * Initializes {@link #faces} based on the enum constant name.
     * The default constructor is automatically invoked when the enum
     * constants are created.
     */
    DiceType() {
        this.faces = getFaces()
    }

    /**
     * Parses the numeric part of the enum constant name to determine
     * the number of faces.  For example, {@code D6} yields {@code 6}.
     *
     * @param type the enum constant whose face count is requested
     * @return the parsed number of faces
     */
    int getFaces(DiceType type) {
        String typeStr = type
        String numStr = typeStr.substring(1)
        faces = Integer.parseInt(numStr)
        return faces
    }
}
