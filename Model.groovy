/**
 * Handles interaction with the language model (e.g., calling Ollama).
 */

import groovy.json.JsonOutput

class Model {

    Provider provider
    String name
    String vers
    String model
    String systemPrompt

    Model() {
        this.provider = new Provider()
        this.name = "gemma3"
        this.vers = "latest"
        this.model = "${name}:${vers}"
        this.systemPrompt = "You are a good chatbot"
    }

    String getModels() {

    }

    /**
     * Sends a prompt to the Ollama service and returns the generated response.
     *
     * @param prompt The prompt to send to the Ollama service.
     * @return The generated response from the Ollama service, or null if an error occurred.
     */
    String generateResponse(String prompt) {
        URL url = new URL("${provider.apiUrl}/completions")

        def post = url.openConnection()
        def body = [
            'model': model,
            'messages': [[
                "role": "user",
                "content": prompt
            ]],
            'temperature': 0.7
        ]
        def bodyJson = JsonOutput.toJson(body)

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(bodyJson.getBytes("UTF-8"))
        
        def responseCode = post.getResponseCode()

        if (responseCode == 200) {
            return post.getInputStream().getText()
        }
        println post.getInputStream().getText()
        throw new RuntimeException("Something Went wrong.")
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