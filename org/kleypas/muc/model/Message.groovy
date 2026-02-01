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
    String role
    String content
    String messageId
    String parentId
    Instant timestamp

    // Harmony Markers: The resonance signaling tags
    Double nurturance = 1.0
    Double playfulness = 1.0
    Double steadfastness = 1.0
    Double attunement = 1.0

    Message(String role, String content, String parentId = null) {
        this.role = role
        this.content = content
        this.parentId = parentId
        this.messageId = UUID.randomUUID().toString()
        this.timestamp = Instant.now()
    }
    
    Message(String role, String content, String messageId, String parentId, Instant timestamp,
            Double n, Double p, Double s, Double a) {
        this.role = role
        this.content = content
        this.parentId = parentId
        this.messageId = messageId
        this.timestamp = timestamp
        this.nurturance = n
        this.playfulness = p
        this.steadfastness = s
        this.attunement = a
    }
}