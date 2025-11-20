package org.kleypas.muc.model

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