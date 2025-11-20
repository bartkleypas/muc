package org.kleypas.muc.illustrator

/**
 * Provides access to environment configuration for the ComfyUI illustration service.
 * <p>
 * The {@code ImgProvider} class loads environment variables from a .env file located in the
 * {@code Secrets/} directory. It exposes the ComfyUI API URL, API key, and a derived token
 * as {@link String} fields. The token is currently set to the API key value but can be
 * modified to support authentication schemes that require token generation.
 *
 * <p>Typical usage:
 * <pre>
 *     ImgProvider provider = new ImgProvider()
 *     String url = provider.apiUrl
 *     String key = provider.apiKey
 * </pre>
 *
 * <p>All fields are public for quick access but can be made private if encapsulation
 * is desired. The constructor automatically loads the .env file if present.
 */
class ImgProvider {
    /** The file containing the environment variables. */
    File envFile = new File("Secrets/.env")

    /** The ComfyUI API endpoint URL. */
    String apiUrl

    /** The ComfyUI API key. */
    String apiKey

    /** A token derived from the API key; used for authentication. */
    String token

    /**
     * Creates a new {@code ImgProvider} instance.
     * <p>
     * The constructor attempts to load the {@code envFile}. If the file exists, its
     * key‑value pairs are merged into a local {@code Map} and used to initialise
     * {@code apiUrl}, {@code apiKey}, and {@code token}. If the file is absent,
     * the fields remain {@code null}.
     *
     * @throws IllegalArgumentException if the .env file is present but cannot be parsed.
     */
    ImgProvider() {
        def envVars = [:]
        if (envFile.exists()) {
            envVars.putAll(loadDotEnv(envFile))
        }

        this.apiUrl = envVars.COMFYUI_API_URL
        this.apiKey = envVars.COMFYUI_API_KEY
        this.token = apiKey
    }

    /**
     * Loads key/value pairs from a .env file.
     * <p>
     * The method reads the file as a standard Java {@code Properties} file. It
     * asserts that the file exists before attempting to load it. The returned
     * {@code Properties} object can be accessed like a {@link Map} of strings.
     *
     * @param path the file containing the environment variables
     * @return a {@link Properties} instance populated with the file's key/value pairs
     * @throws AssertionError if {@code path} does not exist
     */
    public loadDotEnv(File path) {
        assert path.exists()
        Properties props = new Properties()
        path.withInputStream { props.load(it) }
        return props
    }
}