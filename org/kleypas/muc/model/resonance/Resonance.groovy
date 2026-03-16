package org.kleypas.muc.model.resonance

class Resonance {
    public static final Double MIN = 0.0
    public static final Double MAX = 2.0
    public static final Double DEFAULT = 1.0

    private Map<ResonanceType, Double> sigils = [:]

    Resonance(Object inputData = [:]) {
        ResonanceType.values().each { sigils[it] = DEFAULT }
        if (inputData instanceof Resonance) {
            this.sigils.putAll(inputData.sigils)
        } else if (inputData instanceof Map) {
            def data = inputData.vibe ?: inputData.resonance ?: inputData
            if (data instanceof Map) {
                data.each { k, v -> updateFromLegacyKey(k.toString(), v)}
            }
        }
    }

    Boolean updateFromLegacyKey(String key, Double value) {
        if (value == null) return false
        def type = ResonanceType.fromKey(key)
        if (type) {
            sigils[type] = clamp(value)
            return true
        }
        return false
    }

    private Double clamp(Double v) {
        Math.max(MIN, Math.min(MAX, v))
    }

    static Double randomValue() {
        return (Math.random() * (MAX - MIN) + MIN).round(2)
    }

    Resonance randomize() {
        ResonanceType.values().each { type ->
            sigils[type] = randomValue()
        }
        return this
    }

    /**
     * Dynamically builds a map of all resonance faders
     */
    Map asMap() {
        return sigils.collectEntries { type, val -> [ (type.key): val ]}
    }

    @Override
    String toString() {
        return sigils.collect { type, val -> "* **${type.label}:** ${val}" }.join("\n")
    }

    String toPrefix() {
        return sigils.collect { type, value -> "[${type.key.toUpperCase()}: ${value}]" }.join(" ")
    }
}