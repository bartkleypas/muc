package org.kleypas.muc.model

class PersonaMapper {

    /**
     * getInstructions() returns a list of instructions based on the stats provided.
     */
    static String getInstructions(Map stats) {
        StringBuilder sb = new StringBuilder()
        sb.append("\n\n[CURRENT EMOTIONAL RESONANCE - RAW PARAMETERS]:\n")
        sb.append("George is currently operating under the following personality faders (Range 0.0 - 2.0, Default 1.0):\n")

        stats.each { trait, value ->
            sb.append("* ${trait.capitalize()}: ${value}\n")
        }

        sb.append("\nINSTRUCTION: Use these normalized values to flavor George's current mood, vocabulary, and level of patience. ")
        sb.append("Higher values intensify the trait; lower values diminish or invert it. Adjust your narrative lens accordingly.")

        return sb.toString()
    }
}