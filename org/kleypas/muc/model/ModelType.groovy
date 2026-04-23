package org.kleypas.muc.model

// An enumeration representing the different types of models available at our disposal.
// Basically, the three model sizes represent tiers of hardware size and performance.
enum ModelType {
    BIG("gemma4:26b", 0.8, true, true, true),      // Brilliant narrator. No notes.
    MEDIUM("gemma4:e4b", 0.6, true, true, true),   // Good usable compromise and keeps things tight in flight.
    SMALL("gemma4:e2b", 0.6, true, true, true),    // Almost too small, but could work in small deployment constraints.
    XSMALL("gemma3:1b", 0.4, false, false)

    final String modelId
    final Double defaultTemp
    final Boolean supportsVibe
    final Boolean supportsThinking
    final Boolean supportsTools

    ModelType(String id, Double temp, Boolean vibes = false, Boolean thinking = false, Boolean tools = false) {
        this.modelId = id
        this.defaultTemp = temp
        this.supportsVibe = vibes
        this.supportsThinking = thinking
        this.supportsTools = tools
    }
}