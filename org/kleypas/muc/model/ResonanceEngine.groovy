package org.kleypas.muc.model

class ResonanceEngine {

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
     */
    static Message applyResonance(Message message, Map<String, Double> deltas) {
        // List of stats to process
        deltas.each { trait, delta ->
            if (message.resonance.hasProperty(trait)) {
                message.resonance."${trait}" += delta
            } else {
                println ""
            }
        }
        return message
    }
}