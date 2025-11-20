package org.kleypas.muc.rng

/**
 * Represents a die of a particular {@link DiceType}.  Each instance is
 * immutable after construction – the number of faces and the last rolled
 * value are fixed.  The {@link #roll} field holds the result of the most
 * recent roll performed during construction or by calling {@link #roll()}.
 *
 * <p>Typical usage:
 * <pre>
 *     Dice d6 = new Dice(DiceType.D6)
 *     println(d6)      // e.g. 4
 *     Dice newRoll = d6.roll()
 * </pre></p>
 */
public class Dice {
    /** The type of die, which determines the number of faces. */
    public DiceType type

    /** The number of faces on this die. */
    public int faces

    /** The last rolled value (1‑based). */
    public int roll

    /**
     * Constructs a die of the given type and performs an initial roll.
     *
     * @param type the {@link DiceType} that specifies how many faces the die has
     */
    public Dice(DiceType type) {
        this.type = type
        this.faces = type.getFaces(type)
        this.roll = (Math.random() * faces) + 1
    }

    /**
     * Performs a new roll and returns a brand‑new {@code Dice} instance
     * representing the result.  The original instance remains unchanged.
     *
     * @return a freshly rolled {@code Dice}
     */
    public Dice roll() {
        return new Dice(type)
    }

    /**
     * {@inheritDoc}
     *
     * @return the numeric value of the most recent roll as a {@code String}
     */
    @Override
    public String toString() {
        return this.roll
    }
}