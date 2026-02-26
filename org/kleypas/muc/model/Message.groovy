package org.kleypas.muc.model

import java.time.Instant
import java.util.UUID

/**
 * Represents a single message in the conversation.
 *
 * <p>The message stores the role (e.g., "user" or "assistant") and the
 * content of the message.</p>
 */
class Message {
    String timestamp
    String messageId
    String parentId
    String role
    Boolean encrypted
    Boolean isCurrent = false // Marker for current navigation
    String bookmark = null // User defined bookmark for navigation
    String content

    // Harmony Markers: The resonance signaling tags
    Double nurturance = 1.0
    Double playfulness = 1.0
    Double steadfastness = 1.0
    Double attunement = 1.0

    Message(String role, String content, String parentId = null, Map inheritedStats = [:]) {
        this.timestamp = Instant.now().toString()
        this.parentId = parentId
        this.messageId = UUID.randomUUID().toString()
        this.role = role
        this.encrypted = encrypted
        this.content = content
        this.isCurrent = false

        // Inherit from parent stats if provided
        this.nurturance = inheritedStats.nurturance ?: 1.0
        this.playfulness = inheritedStats.playfulness ?: 1.0
        this.steadfastness = inheritedStats.steadfastness ?: 1.0
        this.attunement = inheritedStats.attunement ?: 1.0
    }
    
    Message(String role, String content, String messageId, String parentId, Instant timestamp,
            Double n, Double p, Double s, Double a, String bookmark = null) {
        this.timestamp = timestamp
        this.messageId = messageId
        this.parentId = parentId
        this.nurturance = n
        this.playfulness = p
        this.steadfastness = s
        this.attunement = a
        this.role = role
        this.encrypted = encrypted
        this.content = content
        this.bookmark = bookmark
        this.isCurrent = false
    }

    public Map getStats() {
        return [
            nurturance: this.nurturance,
            playfulness: this.playfulness,
            steadfastness: this.steadfastness,
            attunement: this.attunement
        ]
    }
}