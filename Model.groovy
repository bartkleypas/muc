/**
 * Handles interaction with the language model (e.g., calling Ollama).
 *
 * <p>This class encapsulates the configuration for the Ollama client and
 * provides a convenience method to send prompts and retrieve model
 * responses.</p>
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

/**
 * Represents a single message in the conversation.
 *
 * <p>The message stores the role (e.g., "user" or "assistant") and the
 * content of the message.</p>
 */
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
 *
 * <p>Loads environment variables from {@code Secrets/.env} (if present)
 * and exposes the API URL, key and token for use by {@link Model}.</p>
 */
class Provider {
    File envFile = new File("Secrets/.env")
    String apiUrl
    String apiKey
    String token

    /**
     * Default constructor.
     *
     * <p>Loads the environment variables from the .env file and assigns
     * them to the instance fields.</p>
     */
    Provider() {
        def envVars = [:]
        if (envFile.exists()) {
            envVars.putAll(loadDotEnv(envFile))
        }

        this.apiUrl = envVars.OLLAMA_API_URL
        this.apiKey = envVars.OLLAMA_API_KEY
        this.token = apiKey
    }

    /**
     * Loads key/value pairs from a .env file.
     *
     * @param path the {@link File} pointing to the .env file.
     * @return a {@link Properties} map of the loaded variables.
     */
    public loadDotEnv(File path) {
        assert path.exists()
        Properties props = new Properties()
        path.withInputStream { props.load(it) }
        return props
    }
}
