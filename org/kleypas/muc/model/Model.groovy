package org.kleypas.muc.model

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Handles interaction with the language model (e.g., calling Ollama).
 *
 * <p>This class encapsulates the configuration for the Ollama client and
 * provides a convenience method to send prompts and retrieve model
 * responses.</p>
 */
class Model {

    Provider provider
    String model
    String role
    Boolean stream
    Double temperature
    Context body

    /**
     * Default constructor.
     *
     * <p>Initialises the provider, default model, role, streaming flag,
     * temperature and an empty {@link Context} for the conversation.</p>
     */
    Model() {
        this.provider = new Provider()
        this.model = "gemma3:latest"
        this.role = "assistant"
        this.stream = false
        this.temperature = 0.7
        this.body = new Context()
    }

    /**
     * Sends a prompt to the Ollama service and returns the generated response.
     *
     * @param body the {@link Context} containing the message history to send.
     * @return the generated response from the Ollama service.
     * @throws RuntimeException if the HTTP request fails or the API returns
     *                          an unexpected status code.
     */
    String generateResponse(Context body) {
        URL url = new URL(provider.apiUrl)
        def post = url.openConnection()

        def postData = [
            messages: body.messages,
            model: this.model,
            stream: this.stream,
            temperature: this.temperature
        ]

        def json = JsonOutput.toJson(postData)

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(json.getBytes("UTF-8"))

        def responseCode = post.getResponseCode()

        if (responseCode != 200) {
            def errMsg = post.getInputStream().getText()
            throw new RuntimeException("Something went wrong:\r\n${errMsg}")
        }

        def resp = post.getInputStream().getText()
        def data = new JsonSlurper().parseText(resp)

        if (!data) {
            throw new RuntimeException("Null data")
        }

        def output = data.choices[0].message.content
        return output
    }
}