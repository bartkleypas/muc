package org.kleypas.muc.rng

/**
 * A lightweight representation of a coin that can be flipped to produce a
 * {@code true} (heads) or {@code false} (tails) result.
 *
 * <p>The outcome is determined by {@link java.lang.Math#random()} at the time of
 * construction.  Each {@code Coin} instance is immutable once created – the
 * {@link #val} field is final and only set in the constructor.</p>
 *
 * <p>Typical usage:
 * <pre>
 *     def coin = new Coin()
 *     println(coin)          // e.g. "Heads"
 *     def newFlip = coin.flip()
 * </pre></p>
 */
public class Coin {
    /** {@code true} if the coin shows heads, {@code false} if tails. */
    public boolean val

    /**
     * Creates a new {@code Coin} whose value is determined randomly.
     * The probability of {@code true} (heads) is 50 %.
     *
     * @return the newly created {@code Coin}
     */
    public Coin() {
        this.val = Math.random() < 0.5
    }

    /**
     * Returns a brand‑new {@code Coin} that has been flipped independently
     * of the current instance.  The original instance remains unchanged.
     *
     * @return a freshly flipped {@code Coin}
     */
    public Coin flip() {
        return new Coin()
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "Heads"} if {@link #val} is {@code true},
     *         {@code "Tails"} otherwise
     */
    @Override
    public String toString() {
        def out = this.val ? "Heads" : "Tails"
        return out
    }
}