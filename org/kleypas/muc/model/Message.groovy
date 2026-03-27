package org.kleypas.muc.model

import org.kleypas.muc.model.resonance.Resonance

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
    String author
    String content

    Resonance vibe

    String bookmark = null
    Boolean encrypted = false
    Boolean isCurrent = false

    Message(Map args = [:]) {
        this.vibe = args.vibe instanceof Resonance ? args.vibe : new Resonance()
        args.each { k, v ->
            if ((k == 'vibe' || k == 'resonance') && v instanceof Map) {
                this.vibe = new Resonance(v)
            } else if (this.hasProperty(k)) {
                this."$k" = v
            }
        }
    }

    Map asMap() {
        return [
            'timestamp': timestamp,
            'messageId': messageId,
            'parentId': parentId,
            'role': role,
            'author': author,
            'vibe': vibe.asMap(),
            'encrypted': encrypted,
            'content': content,
            'bookmark': bookmark
        ]
    }
}