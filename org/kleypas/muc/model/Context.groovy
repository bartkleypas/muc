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
    public List<Message> messages = []

    private LogManager logManager

    // Constant for maximum tokens to combat O(N^2) complexity
    private static final int MAX_TOKENS = 32768
    // private static final int MAX_TOKENS = 16384
    //private static final int MAX_TOKENS = 8192

    public Context() {
        this.messages = []
    }

    public Context(List<Message> messages) {
        this.messages = new ArrayList<>(messages)
    }

    /**
     * Adds a new message to the context.
     */
    public Message addMessage(Map args) {
        Message newMessage = new Message(args)
        this.messages.add(newMessage)
        return newMessage
    }

    public Context enableLogging(LogManager manager) {
        this.logManager = manager
        return this
    }

    /**
     * Rebuilds a specific thread by walking the DAG backwards.
     * Uses the LogManager to retrieve the full history bank.
     * @param leafId The ID of the leaf message
     * @return The reconstructed Context object
     */
    public Context loadBranch(String leafId) {
        if (!this.logManager) throw new IllegalStateException("LogManager not initialized")

        Map<String, Map<String, Object>> nodeMap = this.logManager.readAllEntries().collectEntries { [it.messageId, it] }
        List<Message> branchMessages = []
        String currentId = leafId

        while (currentId && nodeMap.containsKey(currentId)) {
            Map entry = nodeMap.get(currentId)
            branchMessages.add(0, new Message(entry))
            currentId = entry.parentId
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
        Context newContext = new Context()

        this.messages.findAll { !it.role.equalsIgnoreCase("system") }.each { turn ->
            boolean isAssistant = turn.role.equalsIgnoreCase(speaker)

            newContext.addMessage(
                role: isAssistant ? "assistant" : "user",
                content: isAssistant ? turn.content : "${turn.role} says: ${turn.content}",
                parentId: turn.messageId,
                stats: turn.getStats()
            )
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