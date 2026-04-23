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
        def messages = context.messages

        // If we are suturing a vibe/prefix and the model supports it
        if (type.supportsVibe && prefix) {
            messages << [role: "assistant", content: prefix]
        }

        def postData = [
            model: type.modelId,
            messages: messages,
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