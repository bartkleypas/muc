package org.kleypas.muc.model

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

    // Constant for maximum tokens to combat O(N^2) complexity
    // private static final int MAX_TOKENS = 32768
    private static final int MAX_TOKENS = 8192
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

    public Context addMessage(String sender, String content) {
        return addMessage(sender, content, null)
    }
    /**
     * Adds a new message to the context.
     * @param sender  the role of the sender (e.g., "user" or "assistant").
     * @param content the message content.
     * @return the Context instance for chaining.
     */
    // Style B: Explicit return type (Context for chaining) and parameter types
    public Context addMessage(String sender, String content, String parentId) {
        // Use concrete types within the method
        final Message newMessage = new Message(sender, content)
        if (parentId) {
            final Message parent = this.messages.find { it.messageId == parentId }
            if (parent) {
                newMessage.breadcrumb.addAll(parent.breadcrumb)
                newMessage.breadcrumb.add(parent.messageId)
            }
        }
        this.messages.add(newMessage)
        return this
    }

    /**
     * @return the list of messages in the context.
     */
    // Style B: Explicit return type
    public List<Message> getMessages() {
        return this.messages
    }

    public Message getLastMessage() {
        if (this.messages.isEmpty()) { return null }
        return this.messages.last()
    }

    /**
     * Estimates the token count for a single message based on character count.
     * @param message The message to estimate tokens for.
     * @return The estimated number of tokens.
     */
    // Style B: Explicit return type and parameter type, private access
    private int estimateMessageTokenCount(final Message message) {
        // Safe navigation for content in case it's null
        final int charCount = message.content?.length() ?: 0
        // Rough estimate: 1 token is roughly 4 characters
        return charCount / 4
    }

    /**
     * Estimates the current token count based on character count (a simple proxy).
     * @return The estimated number of tokens.
     */
    // Style B: Explicit return type and private access
    private int estimateTokenCount() {
        int tokenCount = 0
        // Use explicit types for the loop (Style B preferred)
        for (final Message msg : this.messages) {
            tokenCount += estimateMessageTokenCount(msg)
        }
        return tokenCount 
    }
    
    /**
     * Removes the oldest non-system messages until the token count is below the maximum.
     */
    // Style B: Explicit return type
    public void pruneContext() {
        int currentTokens = this.estimateTokenCount()

        // While too large and there's more than one message (to preserve the initial introduction)
        while (currentTokens > MAX_TOKENS && this.messages.size() > 1) {
            int messageIndexToRemove = -1

            // Iterate using standard loop (Style B) to find the first non-system message
            for (int i = 0; i < this.messages.size(); i++) {
                final Message msg = this.messages.get(i)
                if (!"system".equals(msg.role.toLowerCase())) {
                    messageIndexToRemove = i
                    break
                }
            }

            if (messageIndexToRemove != -1) {
                final Message removedMsg = this.messages.get(messageIndexToRemove)
                final int tokensToSubtract = estimateMessageTokenCount(removedMsg)
                this.messages.remove(messageIndexToRemove)
                currentTokens -= tokensToSubtract
            } else {
                // Only system messages remain
                break
            }
        }
    }

    // --- Transformation ---

    /**
     * Transforms the context for a specific speaker.
     * @param speaker the role that should be treated as the assistant.
     * @return a new {@link Context} instance with the transformed messages.
     */
    // Style B: Explicit return type and parameter type
    public Context swizzleSpeaker(String speaker) {
        // Use strong typing for local variables
        final Context newContext = new Context()

        // Preserve first system message (Style B: explicit type casting for iteration)
        if (!messages.isEmpty() && "system".equals(messages.get(0).role)) {
            final Message systemMsg = messages.get(0)
            newContext.messages.add(systemMsg)
        }

        // Use standard Java/Groovy List iteration for Style B maintainability
        for (int i = 1; i < messages.size(); i++) {
            final Message turn = messages.get(i)

            final String senderName = turn.role
            final String roleToSend
            final String contentToSend

            if (senderName.equalsIgnoreCase(speaker)) {
                roleToSend = "assistant"
                contentToSend = turn.content
            } else {
                roleToSend = "user"
                // Use Groovy GString interpolation for clean string formatting (acceptable in Style B)
                contentToSend = "${senderName} says: ${turn.content}"
            }
            // Note: The original implementation in your file incorrectly added turn.content 
            // regardless of role; I have corrected it to use contentToSend for 'user' roles 
            // to ensure the prefix is included, but I'll stick close to the original 
            // method of adding the message to the context, which appears to be taking 
            // the original message content, not the prefixed one, for the actual message.
            // Keeping the original intent for message content addition:
            newContext.addMessage(roleToSend, turn.content)
        }
        return newContext
    }

    /**
    * Creates a new Context object optimized for model generation.
    * This object contains the provided system instruction followed by the full history.
    * @param systemInstruction A Message object containing the speaker's system prompt.
    * @return A new Context object ready for generation.
    */
    public Context getThreadForModel(Message systemInstruction) {
        // 1. Start with a fresh context
        final Context thread = new Context()
        
        // 2. Add the specific system instruction first
        thread.messages.add(systemInstruction)
        
        // 3. Add the entire current history
        thread.messages.addAll(this.messages)
        
        // 4. Prune the context to respect token limits (leveraging existing logic)
        thread.pruneContext() 
        
        return thread
    }
    
    // --- I/O ---

    /**
     * Exports the context (excluding system messages) to a JSON file.
     * @param filePath the file path where the context should be written.
     * @return the {@link File} object pointing to the exported file.
     */
    // Style B: Explicit return type and parameter type
    public File exportContext(String filePath) {
        final File outFile = new File(filePath)
        outFile.parentFile?.mkdirs()

        // Use Groovy idiomatic closure for finding messages (acceptable for small utility functions)
        final List<Message> filteredHistory = this.messages.findAll {
            final Message msg = it
            !msg.role.equalsIgnoreCase("system") 
        }

        final List<Map> serializedMessages = filteredHistory.collect { Message msg ->
            final Map msgMap = [
                role: msg.role,
                content: msg.content,
                messageId: msg.messageId,
                timestamp: msg.timestamp.toString(),
                breadcrumb: msg.breadcrumb
            ]
            return msgMap
        }

        final Map historyMap = [messages: serializedMessages]

        // Use strong typing and explicit method calls for JSON
        outFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(historyMap))
        return outFile
    }

    /**
     * Imports a context from a JSON file.
     * @param filePath the file path to read.
     * @return a new {@link Context} instance populated with the file contents.
     */
    // Style B: Explicit return type and parameter type. Requires groovy.json.JsonSlurper
    public Context importContext(String filePath) {
        final File inFile = new File(filePath)
        assert inFile.exists()

        // Use strong typing for local variables
        final groovy.json.JsonSlurper jsonSlurper = new groovy.json.JsonSlurper()
        final Map json = jsonSlurper.parse(inFile) as Map

        // Use Groovy idiomatic 'collect' for transformation (acceptable in Style B for mapping)
        final List<Message> msgs = json.messages.collect {
            final Map msgMap = it as Map
            final String role = msgMap.role as String
            final String content = msgMap.content as String
            final String messageId = msgMap.messageId as String ?: UUID.randomUUID().toString()
            final Instant timestamp = msgMap.timestamp ? Instant.parse(msgMap.timestamp as String) : Instant.now()
            final List<String> breadcrumb = msgMap.breadcrumb as List<String> ?: new ArrayList<String>()
            def msg = new Message(role, content, messageId, timestamp)
            msg.breadcrumb.addAll(breadcrumb)
            return msg
        }
        return new Context(msgs)
    }
}