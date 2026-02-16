package org.kleypas.muc.illustrator

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Core class that generates images using a ComfyUI backend.
 *
 * <p>The {@code Illustrator} orchestrates the creation of a JSON payload
 * for the ComfyUI workflow, sends it to the configured {@link ImgProvider},
 * and returns the generated image data.  It supports styling options
 * (portrait, landscape, square) and automatically assigns a random seed
 * if none is supplied.</p>
 */
class Illustrator {
    /** Human‑readable title of the image. */
    String title
    /** Image provider that supplies the API endpoint and credentials. */
    ImgProvider provider
    /** Checkpoint file used by the ComfyUI model. */
    String checkpoint
    /** Style that determines the image dimensions. */
    ImageType style
    /** Raw textual input for the image prompt. */
    String input
    /** Deterministic noise seed. */
    int seed
    /** Number of inference steps. */
    int steps

    /**
     * Creates a new {@code Illustrator} with sensible defaults.
     *
     * <p>The constructor initialises the provider, assigns a random 8‑digit
     * seed, sets the default checkpoint and step count, and chooses the
     * {@link ImageType#PORTRAIT} style.</p>
     */
    Illustrator() {
        this.title = title
        this.provider = new ImgProvider()
        this.checkpoint = "sdxl/sd_xl_base_1.0.safetensors"
        this.style = ImageType.PORTRAIT
        this.input = input
        this.seed = new Random().nextInt(100000000) // 8 digits of random
        this.steps = 25
    }

    /**
     * Builds the ComfyUI workflow JSON from a textual prompt.
     *
     * @param input prompt text for the image generation
     * @return JSON representation of the workflow
     */
    String getComfyUiJson(String input) {
        File jsonTemplate = new File("comfyui.json")
        def json = new JsonSlurper().parse(jsonTemplate)

        String height
        String width
        if (style == ImageType.PORTRAIT) {
            height = 1216
            width = 832
        } else if (style == ImageType.LANDSCAPE) {
            height = 832
            width = 1216
        } else {
            height = 1024
            width = 1024
        }

        json.prompt["4"].inputs.ckpt_name = checkpoint
        json.prompt["5"].inputs.width = width
        json.prompt["5"].inputs.height = height
        json.prompt["6"].inputs.text = input
        json.prompt["10"].inputs.noise_seed = seed
        json.prompt["10"].inputs.steps = steps
        json.prompt["15"].inputs.text = input
        json.prompt["19"].inputs.filename_prefix = title
        def output = new JsonOutput().toJson(json)
        return output
    }

    /**
     * Submits the ComfyUI JSON payload to the API endpoint and returns the
     * raw response body.
     *
     * @param comfyUiJson JSON payload for the POST request
     * @return response body if the request succeeds
     * @throws RuntimeException if the HTTP status is not 200
     */
    String generateImage(String comfyUiJson) {
        URL url = new URL(provider.apiUrl)

        def post = url.openConnection()
        def body = comfyUiJson

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(body.getBytes("UTF-8"))

        def responseCode = post.getResponseCode()

        if (responseCode == 200) {
            return post.getInputStream().getText()
        }
        println post.getInputStream().getText()
        throw new RuntimeException("Something Went wrong.")
    }
}
