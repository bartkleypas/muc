package org.kleypas.muc.model.resonance

enum ResonanceType {
    WARMTH("warmth", "Warmth"),
    CYNICISM("cynicism", "Cynicism"),
    EFFICIENCY("efficiency", "Efficiency"),
    RESONANCE("resonance", "Resonance"),
    GRAVITY("gravity", "Gravity")

    final String key
    final String label

    ResonanceType(String key, String label) {
        this.key = key
        this.label = label
    }

    static ResonanceType fromKey(String key) {
        values().find { it.key == key }
    }
}