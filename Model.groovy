/**
 * Handles interaction with the language model (e.g., calling Ollama).
 */

import groovy.json.JsonOutput

class Model {

    Provider provider
    String model
    String role
    Boolean stream
    Double temperature
    Context body

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
     * @param prompt The prompt to send to the Ollama service.
     * @return The generated response from the Ollama service, or null if an error occurred.
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

        if (responseCode == 200) {
            return post.getInputStream().getText()
        }
        println post.getInputStream().getText()
        throw new RuntimeException("Something Went wrong.")
    }
}

class Context {
    List<Message> messages

    Context() {
        this.messages = new ArrayList<>()
    }

    Context(List<Message> messages) {
        this.messages = new ArrayList<>(messages)
    }

    void addMessage(String role, String content) {
        this.messages.add(new Message(role, content))
    }

    List<Message> getMessages() {
        return messages
    }
}

class Message {
    String role
    String content

    Message(String role, String content) {
        this.role = role
        this.content = content
    }
}

/**
 * Holds the configuration details for the Ollama API.
 */
class Provider {
    File envFile = new File("Secrets/.env")
    String apiUrl
    String apiKey
    String token


    Provider() {
        def envVars = [:]
        if (envFile.exists()) {
            envVars.putAll(loadDotEnv(envFile))
        }

        this.apiUrl = envVars.OLLAMA_API_URL
        this.apiKey = envVars.OLLAMA_API_KEY
        this.token = apiKey
    }

    public loadDotEnv(File path) {
        assert path.exists()
        Properties props = new Properties()
        path.withInputStream { props.load(it) }
        return props
    }
}