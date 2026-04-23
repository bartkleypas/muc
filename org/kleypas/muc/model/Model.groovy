package org.kleypas.muc.model

import org.kleypas.muc.inventory.Inventory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class Model {
    Provider provider
    ModelType type
    Double temperature
    ArrayList toolCall

    Model(ModelType type = ModelType.MEDIUM) {
        this.provider = new Provider()
        this.type = type
        this.temperature = type.defaultTemp
    }

    /**
     * Unified streaming method.
     */
    void streamResponse(Context context, Inventory bag = null, String prefix = null, Closure onToken) {
        // Build the payload
        // Clone the list to avoid side effects on the context
        def messages = new ArrayList(context.messages)

        // Handle stylistic prefix (vibe)
        if (prefix && type.supportsVibe) {
            if (type.supportsThinking) {
                // For thinking models, inject as a system directive to avoid disrupting the reasoning phase
                def systemMsgIndex = messages.findIndexOf { it.role == "system" }
                if (systemMsgIndex != -1) {
                    def systemMsg = messages[systemMsgIndex]
                    messages[systemMsgIndex] = new Message(
                        role: "system",
                        content: systemMsg.content + "\n\nStylistic Directive: ${prefix}"
                    )
                } else {
                    // Fallback: Add a system message if none exists
                    messages.add(0, new Message(role: "system", content: "Stylistic Directive: ${prefix}"))
                }
            } else {
                // Legacy suture for non-thinking models (Assistant pre-fill)
                messages << new Message(role: "assistant", content: prefix)
            }
        }

        // Transform Message objects to maps the API expects
        def apiMessages = messages.collect { msg ->
            def m = [
                role: msg.role,
                content: msg.content ?: ""
            ]
            if (msg.tool_calls) {
                m.tool_calls = msg.tool_calls
            }
            if (msg.tool_call_id) {
                m.tool_call_id = msg.tool_call_id
            }
            return m
        }

        def postData = [
            model: type.modelId,
            messages: apiMessages,
            stream: true,
            think: type.supportsThinking,
            options: [
                temperature: this.temperature,
                num_ctx: 32768, // Bumping the 4k ceiling for the Strix Halo
                num_predict: 8192
            ]
        ]

        // Needs an inventory to build the tool list...
        if (type.supportsTools && bag) {
            postData.tools = bag.getToolInstructions()
        }

        executeRequest(postData, onToken)

    }

    private void executeRequest(Map payload, Closure onToken) {
        URL url = new URL(provider.apiUrl)
        def post = url.openConnection()
        def json = JsonOutput.toJson(payload)

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(json.getBytes("UTF-8"))

        if (post.getResponseCode() == 200) {
            post.getInputStream().withReader { reader ->
                reader.eachLine { line ->
                    if (!line.trim()) return

                    def data = new JsonSlurper().parseText(line)
                    if (data.message?.thinking) {
                        // print(data.message?.thinking) // Noisy, and needs special attention
                    }

                    if (data.message?.content) {
                        onToken(data.message.content)
                    }

                    if (data.message?.tool_calls) {
                        onToolCallToken(data.message.tool_calls)
                    }

                    if (data.done) {
                        return
                    }
                }
            }
        } else {
            throw new RuntimeException("Forge Failure: ${post.errorStream.text}")
        }
    }

    // We might want to see this if tool calls are failing?
    private void onThinkingToken() {

    }

    private void onToolCallToken(ArrayList toolCall) {
        this.toolCall = toolCall
    }
}