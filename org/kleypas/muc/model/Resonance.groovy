package org.kleypas.muc.model

class Resonance {
    Double nurturance = 1.0
    Double playfulness = 1.0
    Double steadfastness = 1.0
    Double attunement = 1.0
    Double sarcasm = 1.0

    Resonance(Map stats = [:]) {
        stats.each { k, v ->
            if (this.hasProperty(k)) {
                this."$k" = (v != null) ? v : 1.0
            }
        }
    }

    Map asMap() {
        return [nurturance: nurturance, playfulness: playfulness, 
                steadfastness: steadfastness, attunement: attunement, sarcasm: sarcasm]
    }
}