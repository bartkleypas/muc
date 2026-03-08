package org.kleypas.muc.model

class Resonance {
    public static final Double MIN = 0.0
    public static final Double MAX = 2.0
    public static final Double DEFAULT = 1.0

    Double nurturance = DEFAULT
    Double playfulness = DEFAULT
    Double steadfastness = DEFAULT
    Double attunement = DEFAULT
    Double sarcasm = DEFAULT

    Resonance(Map stats = [:]) {
        stats.each { k, v ->
            if (this.hasProperty(k)) {
                this."$k" = (v != null) ?
                    Math.max(MIN, Math.min(MAX, v.toDouble())) : DEFAULT
            }
        }
    }

    static Double randomValue() {
        return (Math.random() * (MAX - MIN) + MIN).round(2)
    }

    Map asMap() {
        return [nurturance: nurturance, playfulness: playfulness, 
                steadfastness: steadfastness, attunement: attunement, sarcasm: sarcasm]
    }
}