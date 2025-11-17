/**
 * Handles interaction with the language model (e.g., calling Ollama).
 */

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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
            def resp = post.getInputStream().getText()
            def data = new JsonSlurper().parseText(resp)
            def output = data.choices[0].message.content
            return output
        }
        println post.getInputStream().getText()
        throw new RuntimeException("Something Went wrong.")
    }
}

// A very utilitarian class that should probably be its own
// file by now.
class Context {
    List<Message> messages

    Context() {
        this.messages = new ArrayList<>()
    }

    // I should use this constructor more i guess.
    // Doesn't come up often?
    Context(List<Message> messages) {
        this.messages = new ArrayList<>(messages)
    }

    // I probably over-use this method. And it probably
    // gets me into trouble.
    void addMessage(String sender, String content) {
        this.messages.add(new Message(sender, content))
    }

    // I thought we were over getters and setters. Here I
    // am doing it too. Sigh.
    List<Message> getMessages() {
        return messages
    }

    // Write a file with our context to filePath
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

    Context importContext(String filePath) {
        File inFile = new File(filePath)
        assert inFile.exists()
        def json = new JsonSlurper().parse(inFile)
        def msgs = json.messages.collect { new Message(it.role, it.content) }
        return new Context(msgs)
    }

    // Basically make the "speaker" into an assistant, turn everyone
    // else into a "user", and prepend "${speaker} says:" to the resulting
    // strings in the returned context.messages list.
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

// I need to use this more. I keep using the context.addMessage() helper
// method to basically bypass this class. Still inportant for json obj
// imports? Maybe?
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
