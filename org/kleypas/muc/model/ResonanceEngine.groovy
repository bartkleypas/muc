package org.kleypas.muc.model

class ResonanceEngine {
    private static final Double CEILING = 2.0
    private static final Double BASELINE = 1.0
    private static final Double FLOOR = 0.5
    private static final Double DECAY_RATE = 0.02

    /**
     * The "Regex Palette" - looking for George's signature moves
     */
    private static final Map<String, Map<String, Double>> SIGNATURES = [
        "\\*Hoo-hoo\\*"         : [playfulness: 0.1, nurturance: 0.05],
        "\\*Adjusts spectacles\\*": [steadfastness: 0.1],
        "\\*listening\\*"       : [attunement: 0.1, nurturance: 0.05],
        "\\*measured cadence\\*" : [steadfastness: 0.1],
        "\\*A smooth baritone\\*" : [steadfastness: 0.05, nurturance: 0.05]
    ]

    /**
     * Analyzes a message and returns a Map of the calculated resonance adjustments.
     */
    static Map<String, Double> calculate(String content) {
        def scores = [nurturance: 0.0, playfulness: 0.0, steadfastness: 0.0, attunement: 0.0]
        
        SIGNATURES.each { pattern, adjustments ->
            if (content =~ /(?i)${pattern}/) {
                adjustments.each { key, val -> scores[key] += val }
            }
        }
        return scores
    }

    /*
     * Applies the calculated deltas to a Message,
     * ensuring no stat exceeds the 2.0 ceiling.
     */
    static Message applyResonance(Message msg, Map<String, Double> deltas) {
        // List of stats to process
        ['nurturance', 'playfulness', 'steadfastness', 'attunement'].each { stat ->
            Double currentVal = msg."${stat}"
            Double change = deltas[stat] ?: 0.0

            if (change > 0) {
                // Apply the boost and clamp to ceiling
                msg."${stat}" = Math.min(CEILING, currentVal + change)
            } else {
                // Passive decay toward baseline
                if (currentVal > BASELINE) {
                    msg."${stat}" = Math.max(BASELINE, currentVal - DECAY_RATE)
                } else if (currentVal < BASELINE) {
                    // If they somehow dipped below baseline, drift back UP to it
                    msg."${stat}" = Math.min(BASELINE, currentVal + DECAY_RATE)
                }
            }
        }
        return msg
    }
}