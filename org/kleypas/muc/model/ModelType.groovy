package org.kleypas.muc.model

// An enumeration representing the different types of models available at our disposal.
// Basically, the three model sizes represent tiers of hardware size and performance.
enum ModelType {
    // BIG("gpt-oss:20b", 0.8, true),
    // BIG("magistral:24b", 0.8, true),
    BIG("gemma3:27b", 0.8, true),
    // MEDIUM("qwen3.5:9b", 0.7, true),
    MEDIUM("gemma3:12b", 0.7, true),
    SMALL("gemma3:4b", 0.5, true),
    XSMALL("gemma3:1b", 0.4, false)

    final String modelId
    final Double defaultTemp
    final Boolean supportsVibe

    ModelType(String id, Double temp, Boolean vibes) {
        this.modelId = id
        this.defaultTemp = temp
        this.supportsVibe = vibes
    }
}