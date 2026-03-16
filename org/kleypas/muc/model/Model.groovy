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
    ModelType type
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
    Model(ModelType type = ModelType.MEDIUM) {
        this.provider = new Provider()
        this.type = type
        this.model = type.modelId
        this.role = "assistant"
        this.stream = false
        this.temperature = type.defaultTemp
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
            model: this.type.modelId,
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

    /**
    * Streams the response from the model, allowing for real-time output.
    */
    void streamResponse(Context body, Closure onToken) {
        URL url = new URL(provider.apiUrl)
        def post = url.openConnection()

        def postData = [
            messages: body.messages,
            model: this.type.modelId,
            stream: true,
            temperature: this.temperature
        ]

        def json = JsonOutput.toJson(postData)

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(json.getBytes("UTF-8"))

        if (post.getResponseCode() == 200) {
            // Use a reader to process the stream line-by-line
            post.getInputStream().withReader { reader ->
                String line
                while ((line = reader.readLine()) != null) {
                    line = line.trim()
                    if (!line || line == "data: [DONE]") continue

                    if (line.startsWith("data: ")) {
                        line = line.substring(6).trim()
                    }

                    try {
                        def data = new JsonSlurper().parseText(line)
                        def token = data.choices ? data.choices[0]?.delta?.content : data.message?.content
                        if (token) {
                            onToken(token)
                        }
                    } catch (Exception e) {
                        println "\r\n[DEBUG] Failed to parse line: ${line}"
                    }
                }
            }
        } else {
            throw new RuntimeException("Stream failed: " + post.getErrorStream().getText())
        }
    }

    /**
     * Streams the response, but primes the assistant with fader tokens first.
     * This preserves the KV cache of the history while ensuring the model generates
     * text in the correct "mood".
     */
    void streamResponseWithPrefix(Context body, String assistantPrefix, Closure onToken) {
        if (!this.type.supportsVibe) {
            println "[DEBUG] Model class ${type} bypassing prefix suture for neutral judgement."
            streamResponse(body, onToken)
        }

        URL url = new URL(provider.apiUrl)
        def post = url.openConnection()

        // Temp version of messages list
        def messages = body.messages.collect { [role: it.role, content: it.content]}

        if (assistantPrefix) {
            messages << [role: "assistant", content: assistantPrefix]
        }

        // Suture the faders into the beginning of an "open" assistant response
        def postData = [
            messages: messages,
            model: this.type.modelId,
            stream: true,
            temperature: this.temperature
        ]

        def json = JsonOutput.toJson(postData)

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(json.getBytes("UTF-8"))

        if (post.getResponseCode() == 200) {
            // Use a reader to process the stream line-by-line
            post.getInputStream().withReader { reader ->
                String line
                while ((line = reader.readLine()) != null) {
                    line = line.trim()
                    if (!line || line == "data: [DONE]") continue

                    if (line.startsWith("data: ")) {
                        line = line.substring(6).trim()
                    }

                    try {
                        def data = new JsonSlurper().parseText(line)
                        def token = data.choices ? data.choices[0]?.delta?.content : data.message?.content
                        if (token) {
                            onToken(token)
                        }
                    } catch (Exception e) {
                        println "\r\n[DEBUG] Failed to parse line: ${line}"
                    }
                }
            }
        } else {
            throw new RuntimeException("Stream failed: " + post.getErrorStream().getText())
        }
    }
}