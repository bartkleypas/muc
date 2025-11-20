package org.kleypas.muc.model

/**
 * Represents a single message in the conversation.
 *
 * <p>The message stores the role (e.g., "user" or "assistant") and the
 * content of the message.</p>
 */
class Message {
    String role
    String content

    Message(String role, String content) {
        this.role = role
        this.content = content
    }
}