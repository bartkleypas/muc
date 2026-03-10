package org.kleypas.muc.model

class Resonance {
    public static final Double MIN = 0.0
    public static final Double MAX = 2.0
    public static final Double DEFAULT = 1.0

    /* Originals
    Double nurturance = DEFAULT
    Double playfulness = DEFAULT
    Double steadfastness = DEFAULT
    Double attunement = DEFAULT
    Double sarcasm = DEFAULT
    */

    Double warmth = DEFAULT
    Double cynicism = DEFAULT
    Double whimsy = DEFAULT
    Double resonance = DEFAULT
    Double gravity = DEFAULT

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

    Resonance randomize() {
        this.metaClass.properties.each { prop ->
            if (prop.type == Double && !['MIN', 'MAX', 'DEFAULT'].contains(prop.name)) {
                this."${prop.name}" = randomValue()
            }
        }
        return this
    }

    /**
     * Dynamically builds a map of all resonance faders
     */
    Map asMap() {
        return this.metaClass.properties
            .findAll { it.type == Double && !['MIN', 'MAX', 'DEFAULT'].contains(it.name) }
            .collectEntries { [ (it.name): this."${it.name}" ] }
    }
}