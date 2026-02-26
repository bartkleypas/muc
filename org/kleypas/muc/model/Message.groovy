package org.kleypas.muc.model

import java.time.Instant
import java.util.UUID

/**
 * Represents a single turn of a conversation, ie; a message.
 */
class Message {
    String messageId = UUID.randomUUID().toString()
    String parentId
    String timestamp = Instant.now().toString()
    String role
    String content

    Resonance resonance = new Resonance()

    String bookmark = null
    Boolean encrypted = false
    Boolean isCurrent = false

    Message(Map args = [:]) {
        args.each { k, v ->
            if (k == 'stats' || k == 'inheritedStats') {
                this.resonance = new Resonance(v)
            } else if (this.hasProperty(k)) {
                this."$k" = v
            }
        }
    }

    Map getStats() { resonance.asMap() }
}