/**
 * Handles interaction with the language model (e.g., calling Ollama).
 */

import groovy.json.JsonOutput

class Model {

    Provider provider
    String model
    String role
    MessageBody body

    Model() {
        this.provider = new Provider()
        this.model = "gemma3:latest"
        this.role = "assistant"
        this.body = new MessageBody(
            new ArrayList<>(),
            model,
            false, // stream?
            0.7
        )
    }

    /**
     * Sends a prompt to the Ollama service and returns the generated response.
     *
     * @param prompt The prompt to send to the Ollama service.
     * @return The generated response from the Ollama service, or null if an error occurred.
     */
    String generateResponse(MessageBody body) {
        URL url = new URL(provider.apiUrl)
        def post = url.openConnection()

        def json = JsonOutput.toJson(body)

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

class MessageBody {
    List<Map<String, Object>> messages
    String model
    Boolean stream
    Double temperature

    MessageBody(List<Map<String, Object>> messages, String model, Boolean stream, Double temperature) {
        this.messages = new ArrayList<>(messages)
        this.model = model
        this.stream = stream
        this.temperature = temperature
    }

    void addMessage(String role, String content) {
        Map<String, Object> newMessage = Map.of("role", role, "content", content)
        this.messages.add(newMessage)
    }

    List<Map<String, Object>> getMessages() {
        return messages
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