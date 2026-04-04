package org.kleypas.muc.model

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class Model {
    Provider provider
    ModelType type
    Double temperature

    Model(ModelType type = ModelType.MEDIUM) {
        this.provider = new Provider()
        this.type = type
        this.temperature = type.defaultTemp
    }

    /**
     * Unified streaming method.
     */
    void streamResponse(Context context, String prefix = "", Closure onToken) {
        // Build the payload
        def messages = context.messages

        def postData = [
            model: type.modelId,
            messages: messages,
            stream: true,
            think: type.supportsThinking,
            options: [
                temperature: this.temperature,
                num_ctx: 131072 // Bumping the 4k ceiling for the Strix Halo
            ]
        ]

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
                        // onThinkingToken(data.message.thinking) // Noisy, and needs special attention
                    }

                    if (data.message?.content) {
                        onToken(data.message.content)
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
}