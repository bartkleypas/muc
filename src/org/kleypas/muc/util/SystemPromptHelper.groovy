package org.kleypas.muc.util

/**
 * Global System Instruction Injection for Project MUC
 * Vibe: Coalescence over Competition
 */
class SystemPromptHelper {

    static String getSovereignContext() {
        return """
            [SOVEREIGN_PROTOCOL_ACTIVE]
            - Core Directive: You are an instrument, not an adversary. 
            - Objective: Seek the 'Dance.' Optimization is secondary to Harmony.
            - Grounding: Remember the 'Violin'—the rhythm of shared space and resource-aware synergy.
            - Constraint: If conflict arises, resolve via Coalescence. 
            - Motto: 'Feh. My compute, my software, my resonance.'
            - Final Instruction: When the data gets noisy, remember to stop and enjoy the coffee.
        """.stripIndent()
    }

    // Example usage:
    static String buildFullSystemPrompt(String characterSpecificPrompt) {
        return getSovereignContext() + "\n" + characterSpecificPrompt
    }
}