package org.kleypas.muc.model

import org.kleypas.muc.io.LogManager

import groovy.json.JsonOutput
import groovy.lang.Closure

import java.io.File
import java.time.Instant
import java.util.ArrayList
import java.util.List


/**
 * Represents the conversational history (Context) for an LLM interaction.
 * Adheres to Style B: Strong typing, explicit return/parameter types, and access modifiers.
 */
class Context {

    // Style B: Explicit List type for messages and explicit access modifier
    public List<Message> messages

    private LogManager logManager

    // Constant for maximum tokens to combat O(N^2) complexity
    private static final int MAX_TOKENS = 32768
    // private static final int MAX_TOKENS = 16384
    //private static final int MAX_TOKENS = 8192

    // --- Constructors ---

    /**
     * Creates an empty context.
     */
    // Style B: Explicit constructor
    public Context() {
        this.messages = new ArrayList<>()
    }

    /**
     * Creates a context with an initial list of messages.
     * @param messages the initial list of messages.
     */
    // Style B: Explicit constructor and parameter type
    public Context(List<Message> messages) {
        // Use type-safe copying
        this.messages = new ArrayList<>(messages)
    }

    // --- Core Message Management ---

    public Message getLastMessage() {
        if (this.messages == null || this.messages.isEmpty()) {
            return null
        }
        return this.messages.get(this.messages.size() - 1)
    }

    public Context enableLogging(LogManager manager) {
        this.logManager = manager
        return this
    }

    /**
     * Adds a new message to the context.
     * @param sender  the role of the sender (e.g., "user" or "assistant").
     * @param content the message content.
     * @return the Context instance for chaining.
     */
    // Style B: Explicit return type (Message type for chaining) and parameter types
    public Message addMessage(String sender, String content, String parentId = null) {
        // Use concrete types within the method
        Message newMessage = new Message(sender, content, parentId)
        this.messages.add(newMessage)
        return newMessage
    }

    /**
     * Rebuilds a specific thread by walking the DAG backwards.
     * Uses the LogManager to retrieve the full history bank.
     * @param leafId The ID of the leaf message
     * @return The reconstructed Context object
     */
    public Context loadBranch(String leafId) {
        if (!this.logManager) throw new IllegalStateException("LogManager not initialized")

        List<Map<String, Object>> allEntries = this.logManager.readAllEntries()
        Map<String, Map<String, Object>> nodeMap = allEntries.collectEntries { [it.messageId, it] }

        List<Message> branchMessages = []
        String currentId = leafId

        while (currentId != null && nodeMap.containsKey(currentId)) {
            Map entry = nodeMap.get(currentId)
            branchMessages.add(0, new Message(
                (String) entry.role,
                (String) entry.content,
                (String) entry.messageId,
                (String) entry.parentId,
                Instant.parse((String) entry.timestamp),
                (Double) entry.nurturance,
                (Double) entry.playfulness,
                (Double) entry.steadfastness,
                (Double) entry.attunement
            ))
            currentId = (String) entry.parentId
        }
        this.messages = branchMessages
        return this
    }

    /**
     * Removes the oldest non-system messages until the token count is below the maximum.
     */
    public void pruneContext() {
        int currentTokens = estimateTokenCount()
        while (currentTokens > MAX_TOKENS && this.messages.size() > 1) {
            // Remove oldest non-system message
            int toRemove = this.messages.findIndexOf { !it.role.equalsIgnoreCase("system") }
            if (toRemove != -1) {
                currentTokens -= estimateMessageTokenCount(this.messages.remove(toRemove))
            } else {
                break
            }
        }
    }

    private int estimateTokenCount() {
        return this.messages.sum { estimateMessageTokenCount(it) } as int
    }

    private int estimateMessageTokenCount(Message msg) {
        return (msg.content?.length() ?: 0) / 4
    }

    // --- Transformation ---

    /**
     * Transforms the context for a specific speaker.
     * @param speaker the role that should be treated as the assistant.
     * @return a new {@link Context} instance with the transformed messages.
     */
    public Context swizzleSpeaker(String speaker) {
        final Context newContext = new Context()

        // Process ONLY the dialogue history (filtering out any system prompts)
        for (Message turn : this.messages) {
            if (turn.role.equalsIgnoreCase("system")) continue

            final String roleToSend
            final String contentToSend

            if (turn.role.equalsIgnoreCase(speaker)) {
                roleToSend = "assistant"
                contentToSend = turn.content
            } else {
                roleToSend = "user"
                contentToSend = "${turn.role} says: ${turn.content}"
            }

            // Use the PREFIXED content to ensure identity clarity in the shared history
            newContext.addMessage(roleToSend, contentToSend, turn.messageId)
        }
        return newContext
    }

    /**
    * Creates a fresh context thread with a new identity, re-parenting the history.
    */
    public Context getThreadForModel(Message identityPrompt) {
        // 1. Filter out all old system messages to prevent "Identity Bleed"
        List<Message> cleanHistory = this.messages.findAll { !it.role.equalsIgnoreCase("system") }

        // 2. Create the new thread
        Context thread = new Context()
        thread.messages.add(identityPrompt)

        if (!cleanHistory.isEmpty()) {
            // 3. THE SUTURE: Re-parent the first historical message to the new identity
            Message firstChild = cleanHistory.first()
            firstChild.parentId = identityPrompt.messageId

            // 4. Add the rest of the chain
            thread.messages.addAll(cleanHistory)
        }

        // 5. Prune to ensure we stay under the token ceiling
        thread.pruneContext()
        return thread
    }
}