package org.kleypas.muc.model.resonance

class Resonance implements Cloneable {
    public static final Double MIN = 0.0
    public static final Double MAX = 2.0
    public static final Double DEFAULT = 1.0

    private Map<ResonanceType, Double> sigils = [:]

    def propertyMissing(String name, value) {
        if (!updateFromLegacyKey(name, value)) {
            throw new MissingPropertyException(name, this.class)
        }
    }

    def propertyMissing(String name) {
        def type = ResonanceType.fromKey(name)
        if (type && sigils.containsKey(type)) {
            return sigils[type]
        }
        throw new MissingPropertyException(name, this.class)
    }

    Resonance() {
        ResonanceType.values().each { sigils[it] = DEFAULT }
    }

    static Resonance from(Object input) {
        def res = new Resonance()
        if (input instanceof Resonance) {
            res.sigils.putAll(input.sigils)
        } else if (input instanceof Map) {
            def data = input.vibe ?: input.resonance ?: input

            if (data instanceof Resonance) return from(data)
            if (data instanceof Map) {
                data.each { k, v -> res.updateFromLegacyKey(k.toString()) }
            }
        }
        return res
    }

    Resonance plus(Map deltas) {
        deltas.each { k, v ->
            updateFromLegacyKey(k.toString(), v)
        }
        return this
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

    String toMd() {
        return toPrefix()
    }

    String toPrefix() {
        return sigils.collect { type, value -> "[${type.key.toUpperCase()}: ${value}]" }.join(" ")
    }

    @Override
    Resonance clone() {
        try {
            return (Resonance) super.clone()
        } catch (CloneNotSupportedException e) {
            return new Resonance()
        }
    }
}