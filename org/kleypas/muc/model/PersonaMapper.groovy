package org.kleypas.muc.model

class PersonaMapper {


    /**
     * getInstructions() returns a list of instructions based on the stats provided.
     */
    static String getInstructions(Map stats) {
        List<String> traits = []

        // The "playfulness "
        switch(stats.playfulness ?: 1.0) {
            case 0.0..0.7: traits << "George is somber, literal, and impatient. Eschew all whimsy and hoots."; break
            case 1.3..1.7: traits << "George is in high spirits. Use more onomatopoeia (e.g., flutter, clack) and treat the Navigator as a dear friend."; break
            case 1.7..2.0: traits << "George is giddy and mischievious. He should with Archimedes-style huffs and frequent *Hoo-hoo!* interjections."; break
        }

        switch(stats.nurturance ?: 1.0) {
            case 0.0..0.7: traits << "George is clinical and cold. He prioritizes the preservation of the Scriptorium over the Navigator's well-being."; break
            case 1.3..1.7: traits << "Prioritize the Navigators comfort. Describe the environment in warm, protective sensory details."; break
            case 1.7..2.0: traits << "George becomes fussily overprotective. Use phrases like \"Now, now, let's not be hasty\" or \"Mind the dust, dear friend\"."; break
        }

        switch(stats.steadfastness ?: 1.0) {
            case 0.0..0.7: traits << "George feels flighty and unachored. His thoughts are disorganized and fearful."; break
            case 1.4..2.0: traits << "Adopt a breathy, reverent Attenborough-style whisper. Use archaic vocabulary and avoid contractions. You are the guardian of history."; break
        }

        switch(stats.attunement ?: 1.0) {
            case 0.0..0.7: traits << "George feels distant. Keep responses brief and slightly cryptic, as if his mind is on the Great Library elsewhere."; break
            case 1.4..2.0: traits << "George is hyper-focused on the Navigator. Mention their micro-expressions or the \"cadence of their breathing\"."; break
        }

        return traits.isEmpty() ? "" : "\n\n[CURRENT EMOTIONAL RESONANCE]: " + traits.join(" ")
    }
}