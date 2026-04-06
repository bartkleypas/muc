package org.kleypas.muc.model

// An enumeration representing the different types of models available at our disposal.
// Basically, the three model sizes represent tiers of hardware size and performance.
enum ModelType {
    BIG("gemma4:26b", 1.0, false, true, true),         // Brilliant narrator. No notes.
    // BIG("magistral:24b", 0.8, false, true),
    // MEDIUM("gpt-oss:20b", 0.8, false, true),   // Has documented issues transitioning smoothly from thinking and prose. Also not the most convincing narrator.
    MEDIUM("gemma3:12b", 0.7, true, false),       // Good usable compromise and keeps things tight in flight.
    // MEDIUM("qwen3.5:9b", 0.7, false, true),    // Fast, but not quite big enough.
    SMALL("gemma3:4b", 0.5, true, false),         // Almost too small, but could work in small deployment constraints.
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