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

    Message(String role, String content, String parentId = null) {
        this.role = role
        this.content = content
        this.parentId = parentId
        this.messageId = UUID.randomUUID().toString()
        this.timestamp = Instant.now()
    }
    
    Message(String role, String content, String messageId, String parentId, Instant timestamp) {
        this.role = role
        this.content = content
        this.parentId = parentId
        this.messageId = messageId
        this.timestamp = timestamp
    }
}