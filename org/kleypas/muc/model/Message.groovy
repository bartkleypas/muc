package org.kleypas.muc.model

import java.time.Instant
import java.util.List
import java.util.ArrayList
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
    Instant timestamp
    List<String> breadcrumb = new ArrayList<>()

    Message(String role, String content) {
        this.role = role
        this.content = content
        this.messageId = UUID.randomUUID().toString()
        this.timestamp = Instant.now()
    }
    
    Message(String role, String content, String messageId, Instant timestamp) {
        this.role = role
        this.content = content
        this.messageId = messageId
        this.timestamp = timestamp
    }
}