package org.kleypas.muc.model

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Represents a conversation context used by the model.
 *
 * <p>The context stores a list of {@link Message} objects. It provides
 * helper methods to add messages, export to a JSON file, import from a
 * file, and transform the context for different speakers.</p>
 */
class Context {
    List<Message> messages

    /**
     * Creates an empty context.
     */
    Context() {
        this.messages = new ArrayList<>()
    }

    /**
     * Creates a context with an initial list of messages.
     *
     * @param messages the initial list of messages.
     */
    Context(List<Message> messages) {
        this.messages = new ArrayList<>(messages)
    }

    /**
     * Adds a new message to the context.
     *
     * @param sender  the role of the sender (e.g., "user" or "assistant").
     * @param content the message content.
     */
    void addMessage(String sender, String content) {
        this.messages.add(new Message(sender, content))
    }

    /**
     * @return the list of messages in the context.
     */
    List<Message> getMessages() {
        return messages
    }

    /**
     * Exports the context (excluding system messages) to a JSON file.
     *
     * @param filePath the file path where the context should be written.
     * @return the {@link File} object pointing to the exported file.
     */
    File exportContext(String filePath) {
        File outFile = new File(filePath)
        outFile.parentFile?.mkdirs()

        def history = new Context()
        this.properties.each { key, value ->
            if (key == 'messages') {
                def filteredHistory = this.messages.findAll { it.role != "system" }
                history.messages.addAll(filteredHistory)
            }
        }

        outFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(history))
        return outFile
    }

    /**
     * Imports a context from a JSON file.
     *
     * @param filePath the file path to read.
     * @return a new {@link Context} instance populated with the file contents.
     */
    Context importContext(String filePath) {
        File inFile = new File(filePath)
        assert inFile.exists()
        def json = new JsonSlurper().parse(inFile)
        def msgs = json.messages.collect { new Message(it.role, it.content) }
        return new Context(msgs)
    }

    /**
     * Transforms the context for a specific speaker.
     *
     * <p>Turns the supplied {@code speaker} into an assistant, all other
     * participants become a user, and their messages are prefixed with
     * "<code>&lt;speaker&gt; says:</code>". The first system message is kept
     * intact if present.</p>
     *
     * @param speaker the role that should be treated as the assistant.
     * @return a new {@link Context} instance with the transformed messages.
     */
    Context swizzleSpeaker(String speaker) {
        def ctx = new Context()
        if (!messages.isEmpty() && "system".equals(messages.get(0).role)) {
            Message msg = messages.get(0)
            ctx.messages.add(msg)
        }

        // Boomer loop? Why not.
        for (int i = 1; i < messages.size(); i++) {
            Message turn = messages.get(i)

            String senderName = turn.role
            String roleToSend
            String contentToSend
            if (senderName.equalsIgnoreCase(speaker)) {
                roleToSend = "assistant"
                contentToSend = turn.content
            } else {
                roleToSend = "user"
                contentToSend = "${senderName} says: ${turn.content}"
            }
            ctx.addMessage(roleToSend, turn.content)
        }
        return ctx
    }
}