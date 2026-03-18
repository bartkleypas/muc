package org.kleypas.muc.model

// An enumeration representing the different types of models available at our disposal.
// Basically, the three model sizes represent tiers of hardware size and performance.
enum ModelType {
    BIG("gemma3:27b", 0.8, true),
    MEDIUM("gemma3:12b", 0.7, true),
    SMALL("gemma3:1b", 0.4, false)

    final String modelId
    final Double defaultTemp
    final Boolean supportsVibe

    ModelType(String id, Double temp, Boolean vibes) {
        this.modelId = id
        this.defaultTemp = temp
        this.supportsVibe = vibes
    }
}