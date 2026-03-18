package org.kleypas.muc.model.resonance

import org.kleypas.muc.model.resonance.ResonanceType

class ResonanceEngine {

    // Cluster-based triggers for the "George" Persona
    private static final Map<ResonanceType, List<String>> THEMATIC_CLUSTERS = [
        (ResonanceType.EFFICIENCY): ["clutch", "hollow", "talon", "feather", "nocturnal", "hoot", "pellet", "pragmatic", "extraction", "resource"],
        (ResonanceType.CYNICISM):   ["absurd", "decay", "futile", "reclusive", "algorithm", "noise", "dust", "hubris", "misguided", "clumsy", "skeptical", "fallout"],
        (ResonanceType.WARMTH):     ["comfort", "hearth", "amber", "soft", "welcome", "friend", "steady", "spirit", "vibrant", "indulgence"],
        (ResonanceType.GRAVITY):    ["sacred", "duty", "archive", "eternal", "preservation", "weight", "balance", "centuries", "delicate"],
        (ResonanceType.RESONANCE):  ["pulse", "vibration", "hum", "echo", "shimmer", "frequency", "resonance", "harmonic", "rhythm"]
    ]

    static Map<ResonanceType, Double> calculate(String content) {
        Map<ResonanceType, Double> deltas = [:]

        THEMATIC_CLUSTERS.each { type, keywords ->
            double score = 0.0
            keywords.each { word ->
                // Simple word-count nudge
                if (content.toLowerCase().contains(word)) {
                    score += 0.05
                }
            }
            if (score > 0) deltas[type] = score
        }
        return deltas
    }
}