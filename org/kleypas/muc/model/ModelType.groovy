package org.kleypas.muc.model

// An enumeration representing the different types of models available at our disposal.
// Basically, the three model sizes represent tiers of hardware size and performance.
enum ModelType {
    // BIG("magistral:24b", 0.8, true),    // Reverb engine. Will happily think really hard forever and never actually say anything. Watch for unconstrained harmonics in thinking phase.
    BIG("gemma4:26b", 1.0, false, false),          // Brilliant narrator. No notes.
    // MEDIUM("gpt-oss:20b", 0.8, true, true),   // Has documented issues transitioning smoothly from thinking and prose. Also not the most convincing narrator. Skip it!
    MEDIUM("gemma3:12b", 0.7, true, false),       // Good usable compromise and keeps things tight in flight.
    // MEDIUM("qwen3.5:9b", 0.7, true, false),    // Not big enough, and another unconstrained harmonics danger.
    SMALL("gemma3:4b", 0.5, true, false),         // Almost too small, but could work in small deployment constraints.
    XSMALL("gemma3:1b", 0.4, false, false)

    final String modelId
    final Double defaultTemp
    final Boolean supportsVibe
    final Boolean supportsThinking

    ModelType(String id, Double temp, Boolean vibes, Boolean thinking = false) {
        this.modelId = id
        this.defaultTemp = temp
        this.supportsVibe = vibes
        this.supportsThinking = thinking
    }
}